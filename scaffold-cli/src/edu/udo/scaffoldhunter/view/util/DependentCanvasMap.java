/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See the file README.txt in the root directory of the Scaffold Hunter
 * source tree for details.
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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.udo.scaffoldhunter.view.RenderingQuality;
import edu.udo.scaffoldhunter.view.scaffoldtree.VCanvas;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;

/**
 * Base class for components providing an additional view on a PCanvas.
 * This class provides mechanisms to connect to a PCanvas and preserves
 * consistency with the canvas, such that modifications like adding or
 * removing layers are reflected by this view. 
 * 
 * @author Kriege
 *
 */
public class DependentCanvasMap extends PCanvas {
    
    /**
     * Canvas viewed by this component. 
     */
    protected AbstractIndependentCanvas viewedCanvas;
    
    /**
     * Change listeners to know when to update.
     */
    private PropertyChangeListener layerListener;
    private PropertyChangeListener interactionListener;
    
    
    /**
     * Creates a new instance.
     */
    public DependentCanvasMap() {
        // create listeners
        interactionListener = new InteractionListener();
        layerListener = new LayerListener();
        
        // remove default pan and zoom
        removeInputEventListener(getPanEventHandler());
        removeInputEventListener(getZoomEventHandler());
    }

    
    /**
     * Creates a new instance and connects a canvas to it.
     * @param canvas the VCanvas this component depends on
     */
    public DependentCanvasMap(AbstractIndependentCanvas canvas) {
        this();
        connect(canvas);
    }

    
    /**
     * @return true iff the component is connected to a canvas
     */
    public boolean isConnected() {
        return viewedCanvas != null;
    }
  
    
    /**
     * Connects a new canvas to this component. If it is already
     * connected to a canvas, it will be disconnected first.
     * 
     * @param canvas the <b>VCanvas</b> that should be viewed
     */
    public void connect(AbstractIndependentCanvas canvas) {
        if (isConnected()) disconnect();

        viewedCanvas = canvas;
        synchronizeLayers();
        viewedCanvas.getCamera().addPropertyChangeListener(PCamera.PROPERTY_LAYERS, layerListener);
        viewedCanvas.addPropertyChangeListener(VCanvas.PROPERTY_INTERACTING, interactionListener);
    }
    
    
    /**
     * Stop this component from receiving events from the viewed canvas
     * and remove all layers.
     */
    public void disconnect() {
        // return if not connected to a canvas
        if (!isConnected()) return;

        // add the layers of the viewedCanvas to the camera 
        // indices represent the order of the layers
        while (!getCamera().getLayersReference().isEmpty()) {
            getCamera().removeLayer(0);
        }
        viewedCanvas.getCamera().removePropertyChangeListener(PCamera.PROPERTY_LAYERS, layerListener);
        viewedCanvas.removePropertyChangeListener(VCanvas.PROPERTY_INTERACTING, interactionListener);
        viewedCanvas = null;
    }
    
    
    /**
     * Removes all references to this object.
     */
    @SuppressWarnings("deprecation")
    public void dispose() {
        disconnect();
        // stop all activities
        getRoot().getActivityScheduler().removeAllActivities();
        // PCanvas sets a static referance CURRENT_ZCANVAS to every
        // canvas at creation time. This may result in a memory leak if
        // a canvas is created and closed again. Setting the inherited
        // reference to null should prevent this.
        if (PCanvas.CURRENT_ZCANVAS == this) PCanvas.CURRENT_ZCANVAS = null;
        removeInputSources();
    }


    /**
     * Synchronizes the layers of the two canvases.
     */
    public void synchronizeLayers() {
        // remove current layers
        while (!getCamera().getLayersReference().isEmpty()) {
            getCamera().removeLayer(0);
        }
        // add the layers of the viewed canvas
        for (int i = 0;  i < viewedCanvas.getCamera().getLayerCount(); i++) {
            getCamera().addLayer(i, viewedCanvas.getCamera().getLayer(i));
        }
    }
    

    /**
     * Synchronizes rendering quality changes because of animation.
     */
    @Override
    public boolean getAnimating() {
        if (viewedCanvas == null) return false;
        return (getRoot().getActivityScheduler().getAnimating() ||
                viewedCanvas.getRoot().getActivityScheduler().getAnimating());
    }
    
    
    /**
     * Change the rendering quality.
     * 
     * @param newquality quality to be set
     */
    public void setRenderingQuality(RenderingQuality newquality) {
        newquality.setQuality(this);
    }
    
    @Override
    public void setBounds(final int x, final int y, final int width, final int height) {
        // negative values here might result in mirror transformations
        if ((width<=0) || (height<=0)) return;
        super.setBounds(x, y, width, height);
    }
    
    /**
     * Synchronizes the interaction status. The rendering quality of this
     * component may depend on the interaction status.
     */
    private class InteractionListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if ((Boolean)event.getNewValue()) {
                setInteracting(true);
            } else {
                setInteracting(false);
            }
        }
    }
    
    
    /**
     * Triggers synchronization of layers.
     */
    private class LayerListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            synchronizeLayers();
        }
    }

}
