/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffold Hunter source tree
 * for details.
 *
 * Scaffold Hunter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Scaffold Hunter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package edu.udo.scaffoldhunter.view.util;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.udo.scaffoldhunter.view.scaffoldtree.VCanvas;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PInputEventListener;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This class provides a birds eye view on the connected <b>VCanvas</b>. The
 * viewed area of the connected canvas is represented by a rectangle which can
 * be dragged.
 * 
 * @author Wiesniewski
 * @author Kriege
 */
public class MiniMap extends DependentCanvasMap implements PropertyChangeListener {

    /**
     * The name that is given to this camera so that it can be identified as
     * another camera looking at the layer
     */
    public final static String CAMERA_NAME = "MiniMap-Camera";

    /**
     * This is the node that shows the viewed area of the connected canvas.
     */
    private PPath areaVisiblePNode;

    /**
     * The layer which determines the viewed area.
     */
    private PLayer viewedLayer;

    /**
     * The change listeners to know when to update the mini map.
     */
    private PropertyChangeListener layoutListener;
    private PropertyChangeListener cameraListener;
    private PropertyChangeListener initializationListener;

    /**
     * The activity to update the mini map. This activity will be scheduled by
     * the <b>PActivityScheduler</b>.
     */
    private MiniMapUpdateActivity updateActivity;

    /**
     * This value defines the delay of updates to sync the mini map with the
     * <b>viewedCanvas</b> (default 300).
     */
    private int updateDelay = 300;

    /**
     * Flags to notify the activity if an update is required and what has to be
     * updated.
     */
    boolean recenterRequired;
    boolean updateRequired;

    /**
     * Creates a new instance.
     */
    public MiniMap() {
        super();
        initialize();
    }

    /**
     * Creates a new instance and connects a canvas to it.
     * 
     * @param canvas
     *            the VCanvas this mini map depends on
     */
    public MiniMap(AbstractIndependentCanvas canvas) {
        super(canvas);
        initialize();
    }

    private void initialize() {
        // create listeners
        layoutListener = new LayoutListener();
        cameraListener = new CameraListener();
        initializationListener = new InitializationListener();

        // create the coverage node
        areaVisiblePNode = PPath.createRectangle(0, 0, 100, 100);
        areaVisiblePNode.setPaint(Color.red);
        areaVisiblePNode.setTransparency(0.4f);

        getCamera().addChild(areaVisiblePNode);
        areaVisiblePNode.addInputEventListener(new VMiniMapDragHandler());
        getCamera().addInputEventListener(new VMiniMapCameraHandler());
        addInputEventListener(new WheelHandler());

        // updateActivity
        updateActivity = new MiniMapUpdateActivity();
        getCamera().setName(CAMERA_NAME);
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        updateMiniMap(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(AbstractIndependentCanvas canvas) {
        super.connect(canvas);

        viewedLayer = viewedCanvas.getNodeLayer();
        viewedCanvas.getCamera().addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, cameraListener);
        viewedCanvas.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, cameraListener);
        viewedCanvas.addPropertyChangeListener(VCanvas.LAYOUT_CHANGING, layoutListener);
        getCamera().addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, initializationListener);

        // start update activity
        getRoot().getActivityScheduler().addActivity(updateActivity, true);
        updateMiniMap(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        viewedCanvas.getCamera().removePropertyChangeListener(PCamera.PROPERTY_BOUNDS, cameraListener);
        viewedCanvas.getCamera().removePropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, cameraListener);
        viewedCanvas.removePropertyChangeListener(VCanvas.LAYOUT_CHANGING, layoutListener);
        getCamera().removePropertyChangeListener(PCamera.PROPERTY_BOUNDS, initializationListener);
        viewedLayer = null;

        // stop update activity and remove it from the activity scheduler.
        updateActivity.terminate();

        super.disconnect();
    }

    /**
     * This method keeps the mini map in sync with the viewed canvas. If the
     * camera of the connected canvas moves (pan or zoom), the
     * <b>viewedAreaPNode</b> representing the viewed area will be updated by
     * this method.
     * 
     * @param recenter
     *            recenter the camera on the area covered by nodes. If
     *            <code>true</code> the camera will recenter and zoom out so
     *            that the entire area is displayed.
     */
    private void updateMiniMap(boolean recenter) {
        PCamera cam = getCamera();
        PCamera viewedCam = viewedCanvas.getCamera();
        if (recenter) {
            recenterRequired = false;
            // getFullBounds() may trigger PROPERTY_FULL_BOUNDS change!
            PBounds existingArea = viewedLayer.getFullBounds();
            if (!existingArea.isEmpty()) {
                double insetX = existingArea.getWidth() / 20;
                double insetY = existingArea.getHeight() / 20;
                existingArea.inset(-insetX, -insetY);
                cam.animateViewToCenterBounds(existingArea, true, 0);
            }
        }

        PBounds viewedArea = viewedCam.getViewBounds();
        areaVisiblePNode.setBounds(cam.viewToLocal(viewedArea));
        updateRequired = false;
    }

    /**
     * Sets the minimum delay between two updates of the mini map. Low values
     * may slow down the performance when zooming or panning.
     * 
     * @param delay
     *            (default 300)
     */
    public void setUpdateDelay(int delay) {
        updateDelay = delay;
    }

    /**
     * Runs the update according to the flag <b>recenterRequired</b>.
     */
    private class MiniMapUpdateActivity extends PActivity {
        public MiniMapUpdateActivity() {
            super(-1, updateDelay);
        }

        @Override
        protected void activityStep(long elapsedTime) {
            if (updateRequired)
                updateMiniMap(recenterRequired);
        }
    }

    /**
     * Listen to layout changes which require to recenter the camera of the mini
     * map.
     */
    private class LayoutListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (!((Boolean) event.getNewValue())) {
                recenterRequired = true;
                updateRequired = true;
            }
        }
    }

    /**
     * Listen to property changes from the camera of the viewed canvas.
     */
    private class CameraListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            updateRequired = true;
        }
    }

    /**
     * Listen to property changes from this camera and updates the mini map.
     * This listener is required during initialization only because the mini map
     * canvas sometimes changes after connect().
     */
    private class InitializationListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            recenterRequired = true;
            updateRequired = true;
        }
    }

    /**
     * The drag handler for <b>areaVisibleNode</b>.
     */
    private class VMiniMapDragHandler extends PDragSequenceEventHandler {
        @Override
        protected void startDrag(PInputEvent e) {
            super.startDrag(e);
            // Sync the interaction status of the source canvas.
            // The interaction status causes a change of the
            // rendering quality.
            viewedCanvas.setInteracting(true);
        }

        @Override
        protected void drag(PInputEvent e) {
            PDimension dim = e.getDelta();
            viewedCanvas.getCamera().translateView(0 - dim.getWidth(), 0 - dim.getHeight());
            updateMiniMap(false);
        }

        @Override
        protected void endDrag(PInputEvent e) {
            super.endDrag(e);
            // Sync the interaction status of the source canvas.
            // The interaction status causes a change of the
            // rendering quality.
            viewedCanvas.setInteracting(false);
        }
    }

    /**
     * The Camera handler for the mini map. Consumes all events except those for
     * <b>visibleAreaPNode</b>. Handles clicks and causes the
     * <b>viewedCanvas</b> to focus on the clicked point. If the mouse is
     * dragged a rectangle is spanned and the <b>viewedCanvas</b> will display
     * the whole area covered by the rectangle.
     */
    private class VMiniMapCameraHandler implements PInputEventListener {
        private PPath rectangle;
        private Point2D startPoint;

        public VMiniMapCameraHandler() {
            super();
        }

        @Override
        public void processEvent(PInputEvent event, int type) {
            // this will be handled by the VMiniMapDragHandler
            if (event.getPickedNode() == areaVisiblePNode)
                return;

            switch (type) {
            case MouseEvent.MOUSE_PRESSED:
                startSelection(event);
                break;
            case MouseEvent.MOUSE_DRAGGED:
                updateSelection(event);
                break;
            case MouseEvent.MOUSE_RELEASED:
                if (event.isRightMouseButton() && rectangle.getBounds().width == 0 && rectangle.getBounds().height == 0) { //only on click, no drag
                    getCamera().removeChild(rectangle);
                    viewedCanvas.handleRightMouseButton(event);
                } else
                    finishSelection(event);
                break;
            }
            event.setHandled(true);
        }

        private void startSelection(PInputEvent e) {
            startPoint = getCamera().viewToLocal(e.getPosition());
            rectangle = new PPath();
            getCamera().addChild(rectangle);
        }

        private void updateSelection(PInputEvent e) {
            Point2D p = getCamera().viewToLocal(e.getPosition());
            PBounds b = new PBounds();
            b.add(startPoint);
            b.add(p);
            rectangle.setPathTo(b);
        }

        private void finishSelection(PInputEvent e) {
            getCamera().removeChild(rectangle);
            Rectangle2D selectedArea = getCamera().localToView(rectangle.getBounds());
            if ((selectedArea.getWidth() == 0) && (selectedArea.getHeight() == 0))
                viewedCanvas.focusOn(e.getPosition(), false, true);
            else
                viewedCanvas.getCamera().animateViewToCenterBounds(selectedArea, true,
                        viewedCanvas.isCameraAnimation() ? viewedCanvas.getAnimationSpeed() : 0);
        }

    }

    // mark as not serializable
    private void writeObject(ObjectOutputStream stream) throws IOException {
        throw new NotSerializableException();
    }

    private void readObject(ObjectInputStream stream) throws IOException {
        throw new NotSerializableException();
    }

    private class WheelHandler extends PBasicInputEventHandler {

        WheelHandler() {
            super();
            PInputEventFilter filter = new PInputEventFilter();
            filter.rejectAllEventTypes();
            filter.setAcceptsMouseWheelRotated(true);
        }

        @Override
        public void mouseWheelRotated(PInputEvent event) {
            if (event.getWheelRotation() < 0) {
                viewedCanvas.zoomIn(event.getPosition());
            } else if (event.getWheelRotation() > 0) {
                viewedCanvas.zoomOut(event.getPosition());
            }
            viewedCanvas.focusOn(event.getPosition(), false, false);

            // avoid sidebar scrolling when using mouse wheel zoom
            event.getSourceSwingEvent().consume();
        }
    }
}
