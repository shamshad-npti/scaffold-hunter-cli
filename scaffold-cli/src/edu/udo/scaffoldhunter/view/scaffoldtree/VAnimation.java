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

package edu.udo.scaffoldhunter.view.scaffoldtree;


import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivityScheduler;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This class is responsible for all layout animations and provides
 * methods for camera animations that should be scheduled after
 * the next layout animation.
 */
public class VAnimation {

    private VCanvas vcanvas;

    private PTransformActivity lastAnimationTransform;
    private boolean animate;
    private boolean layoutAnimation = true;
    
    /**
     * @return the layoutAnimation
     */
    public boolean isLayoutAnimation() {
        return layoutAnimation;
    }

    /**
     * @param layoutAnimation the layoutAnimation to set
     */
    public void setLayoutAnimation(boolean layoutAnimation) {
        this.layoutAnimation = layoutAnimation;
    }

    private VNode nodeToFocus;
    private boolean nodeToFocusScale;
    private boolean zoomToOverview;
    private CameraNodeAnchor cameraNodeAnchor;
    private boolean animatingLayout;

    /**
     * Create a new <code>VAnimation</code> for vcanvas.
     * 
     * @param vcanvas the vcanvas associated with this animation
     */
    public VAnimation (VCanvas vcanvas) {
        this.vcanvas = vcanvas;
        this.cameraNodeAnchor = new CameraNodeAnchor(vcanvas.getCamera());
    }

    /**
     * All animations created by centerNodeOn() commands after this
     * method is called belong to a single layout update.
     * 
     * @param disableAnimation to toggle LayoutAnimation
     */
    public void startLayoutAnimationList(boolean disableAnimation) {
        //TODO there's only one place, where it will be animated, find a better way, esp for our two variables animate, layoutAnimation
        animate = ((!disableAnimation) && layoutAnimation);
        lastAnimationTransform = null;
    }

    /**
     * This method should be called after all centerNodeOn() calls of
     * a layout update.
     */
    public void stopLayoutAnimationList() {
        // getActivityScheduler() returns null when animating nodes that are not installed
        // in the scene graph
        if (lastAnimationTransform != null && lastAnimationTransform.getActivityScheduler() != null) {
            PActivityScheduler ps = lastAnimationTransform.getActivityScheduler();
            ps.removeActivity(lastAnimationTransform);
            ps.addActivity(lastAnimationTransform, true); // process last
            lastAnimationTransform.setDelegate( new PActivity.PActivityDelegate(){
                @Override
                public void activityStarted(PActivity activity) {
                    VAnimation.this.layoutAnimationStarted();
                }
                @Override
                public void activityStepped(PActivity activity) {}
                @Override
                public void activityFinished(PActivity activity) {
                    VAnimation.this.layoutAnimationFinished();
                }
            });
        } else {
            layoutAnimationStarted();
            layoutAnimationFinished();
        }
    }

    /**
     * This method will be called when the layout animation starts
     */
    public void layoutAnimationStarted() {
        // fire property changes before animation
        vcanvas.firePropertyChange(VCanvas.LAYOUT_CHANGING, false, true);
        // disable zoom
        vcanvas.enableZoom(false);
        animatingLayout = true;
    }

    /**
     * This method will be called when the layout animation is finished
     */
    public void layoutAnimationFinished() {
        // fire property changes after animation
        vcanvas.firePropertyChange(VCanvas.LAYOUT_CHANGING, true, false);
        // enable zoom
        vcanvas.enableZoom(true);

        // start camera animation after layout animation
        if (nodeToFocus != null) {
            vcanvas.focusOn(nodeToFocus, nodeToFocusScale);
            nodeToFocus = null;
        }
        if (zoomToOverview) {
            vcanvas.zoomToOverview();
            zoomToOverview = false;
        }
        // remove camera anchor
        if (cameraNodeAnchor.isFixed()) {
            cameraNodeAnchor.release(true);
        }

        vcanvas.getVTree().getLayout().layoutAnimationFinished();
        animatingLayout = false;
        // after a layout change the drawing area has changed, so that the zoom factor must be adjusted
        vcanvas.firePropertyChange(VCanvas.ZOOM_FACTOR, 0d, vcanvas.getZoomFactor());
    }

    /**
     * Move this node from its current position to the new values specified.
     * The given position will become the center of this node's FullBounds.
     * If animation is enabled the node's movement will be animated if it
     * is currently visible or will be visible after animation.
     * 
     * @param node the vnode which will be moved to position
     * @param position the position the node will be moved to
     */
    public void centerNodeOn(VNode node, Point2D position) {
        if ( (animate) && (animateNode(node, position)) ) {

            PAffineTransform at = node.getTransform();

            double dx = position.getX() - node.getFullBoundsReference().getCenterX();
            double dy = position.getY() - node.getFullBoundsReference().getCenterY();

            at.setOffset(at.getTranslateX() + dx, at.getTranslateY() + dy);

            lastAnimationTransform = node.animateToTransform(at, vcanvas.isCameraAnimation() ? vcanvas.getAnimationSpeed() : 0);
        } else
            node.centerFullBoundsOnPoint(position.getX(), position.getY());
    }


    /**
     * This method determines if the node will be animated. If the nodes
     * current position and the specified position don't affect the current view
     * no animation is needed.
     * @param node
     * @param position
     * @return <code>true</code> if the nodes movment affects the current view,
     * <code>false</code> otherwise
     */
    private boolean animateNode(VNode node, Point2D position) {
        if (cameraNodeAnchor.isFixed())
            return true;
        // the node is currently visible
        if (vcanvas.getVisibleNodes().contains(node))
            return true;
        // the node will be visible at the new position
        if (vcanvas.getCamera().getViewBounds().contains(position))
            return true;
        // the nodes edge will be visible during movement
        //TODO
        return false;
    }

    /**
     * @return <code>true</code> if a layout animation is running.
     */
    public boolean isAnimatingLayout() {
        return animatingLayout;
    }

    /**
     * Will start VCanvas.focusOn() after the layout has been recomputed
     * and changes were animated.
     * @param node
     * @param scale
     * 
     * @see VCanvas#focusOn(VNode, boolean)
     */
    public void delayedFocusOn(VNode node, boolean scale) {
        nodeToFocus = node;
        nodeToFocusScale = scale;
    }

    /**
     * If set to <code>true</code>: Will zoom out to show the whole graph after
     * the layout has been recomputed and changes were animated. Afterwards the
     * value will be reset to false.
     * 
     * @param delayedOverview 
     */
    public void setDelayedOverview(boolean delayedOverview) {
        zoomToOverview = delayedOverview;
    }

    /**
     * Will move the camera during the next layout animation as
     * the given node moves, so that the node seems to be fixed.
     * 
     * @param node the node on which the camera should be fixed
     */
    public void fixCameraOnNode(VNode node) {
        cameraNodeAnchor.fixNode(node);
    }

    /**
     * Fixes the camera on a node. If the node moves during layout animation
     * the camera follows the node.
     */
    private static class CameraNodeAnchor implements PropertyChangeListener {
        private PCamera camera;
        private VNode node;
        private double distX;
        private double distY;


        public CameraNodeAnchor(PCamera camera) {
            this.camera = camera;
        }


        public void fixNode(VNode node) {
            Point2D cameraCenter = camera.getViewBounds().getCenter2D();
            Point2D nodeCenter = node.getFullBoundsReference().getCenter2D();
            this.distX = cameraCenter.getX()-nodeCenter.getX();
            this.distY = cameraCenter.getY()-nodeCenter.getY();

            // ensure that the listener is only added to a single node
            if (this.node != null) {
                release(false);
            }

            this.node = node;
            node.addPropertyChangeListener(VNode.PROPERTY_FULL_BOUNDS, this);
        }

        public boolean isFixed() {
            return this.node != null;
        }

        public void release(boolean ensureCorrectLocation) {
            this.node.removePropertyChangeListener(VNode.PROPERTY_FULL_BOUNDS, this);

            // make sure the camera is at the right position
            // this may trigger a PROPERTY_FULL_BOUNDS change event!
            if (ensureCorrectLocation)
                relocateCamera();

            this.node = null;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            relocateCamera();
        }

        public void relocateCamera() {
            PDimension delta = camera.getViewBounds().deltaRequiredToCenter(node.getFullBoundsReference());
            camera.translateView(delta.width-distX, delta.height-distY);
        }
    }
}
