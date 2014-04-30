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


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.udo.scaffoldhunter.view.util.AbstractIndependentCanvas;
import edu.udo.scaffoldhunter.view.util.DependentCanvasMap;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * This class provides a magnified view on the connected
 * <b>VCanvas</b>.
 * 
 * @author Wagner
 */
public class VMagnifyingMap extends DependentCanvasMap {

    /**
     * The change listener to know when to update the VMagMap's view
     */
    private VMagMapEventHandler magEvents;

    /**
     * A constant scaling factor
     */
    private static final double ZOOM_FACTOR = 5.0;
    
    private static final double FIXED_SCALE = 0.7;

    /**
     * If true a fixed scale {@link #FIXED_SCALE} is used and the 
     * constant zoom factor {@link #ZOOM_FACTOR} depending on the
     * canvas zoom otherwise.
     */
    private static final boolean FIX_SCALE = true;

    /**
     * Creates a new instance.
     */
    public VMagnifyingMap() {
        super();
        initialize();
    }

    /**
     * Creates a new instance and connects a canvas to it.
     * @param canvas the VCanvas this magnifying map depends on
     */
    public VMagnifyingMap(VCanvas canvas) {
        super(canvas);
        initialize();
    }
    
    private void initialize() {
        magEvents = new VMagMapEventHandler();
        
        // high quality shouldn't slow down the program's speed; can be changed if needed
        setDefaultRenderQuality(PPaintContext.HIGH_QUALITY_RENDERING);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void connect(AbstractIndependentCanvas canvas) {
        super.connect(canvas);
        
        viewedCanvas.addInputEventListener(magEvents);
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public void disconnect() {
        viewedCanvas.removeInputEventListener(magEvents);

        super.disconnect();
    }

    
    /**
     * Returns the zoom factor of the map
     *  @return double: zoom factor
     */
    public double getZoomFactor() {
        return ZOOM_FACTOR;
    }

    /**
     * This method checks the state of the mouse and updates the VMagMap
     */
    public void centerOnMouse() {

        // rescale the camera
        double scale;
        if (FIX_SCALE) {
            scale = FIXED_SCALE;
        } else {
            scale = viewedCanvas.getCamera().getViewScale() * ZOOM_FACTOR;
        }
        
        if (SemanticZoomLevel.VERY_CLOSE.scaleIsBelowThreshold(scale)) {
            getCamera().setViewScale(scale);
        } else {
            // prevent exceeding maximum scale 
            getCamera().setViewScale(SemanticZoomLevel.VERY_CLOSE.getThreshold() - 0.01);
        }

        Point2D centerVCanvas = viewedCanvas.getMousePosition();
        if (centerVCanvas == null) return; // the canvas is not under the mouse pointer
        centerVCanvas = viewedCanvas.getCamera().localToView(centerVCanvas);// center of the VCanvas local
        Rectangle2D centerView = new Rectangle2D.Double(centerVCanvas.getX(),centerVCanvas.getY(),0,0);
        // move the VMagMaps' PoV
        this.getCamera().animateViewToCenterBounds(centerView,true,0);
    }



    //****************************************************************
    // Inner Class: EventHandler
    //****************************************************************

    class VMagMapEventHandler extends PBasicInputEventHandler implements ActionListener {

        VMagMapEventHandler() {
            PInputEventFilter ef = new PInputEventFilter();
            ef.rejectAllEventTypes();
            ef.setAcceptsMouseMoved(true);
            ef.setAcceptsMouseWheelRotated(true);
            setEventFilter(ef);
        }

        @Override
        public void mouseMoved(PInputEvent event) {
            centerOnMouse();
            event.setHandled(true);
        }

        @Override
        public void mouseWheelRotated(PInputEvent event) {
            centerOnMouse();
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            centerOnMouse();
        }
    }
    
    //mark as not serializable
    private void writeObject(ObjectOutputStream stream) throws IOException {
        throw new NotSerializableException();
    }
    
    private void readObject(ObjectInputStream stream) throws IOException {
        throw new NotSerializableException();
    }

}