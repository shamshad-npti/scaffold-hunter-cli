/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * This abstract Canvas gives the necessary function to couple this canvas with 
 * an dependentCanvas, e.g. for use with a minimap or magnifymap.
 * 
 * In other words, this forms the base of your canvas, while a dependentCanvas can
 * be created which can be connected to this canvas. 
 * 
 * @author Lappie
 *
 */
public abstract class AbstractIndependentCanvas extends PCanvas {

    protected int animationSpeed = 1000;
    
    protected boolean cameraAnimation = true;
    
    /**
     * @param cameraAnimation the cameraAnimation to set
     */
    public void setCameraAnimation(boolean cameraAnimation) {
        this.cameraAnimation = cameraAnimation;
    }

    /**
     * @return the cameraAnimation
     */
    public boolean isCameraAnimation() {
        return cameraAnimation;
    }

    /**
     * @return the animationSpeed
     */
    public int getAnimationSpeed() {
        return animationSpeed;
    }

    /**
     * @param animationSpeed the animationSpeed to set
     */
    public void setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
    
    /**
     * Focus the camera on the given point in the global coordinate system.
     * @param p
     * @param scale zoom to the given point
     * @param animation animate the zoom to the given node
     */
    public void focusOn(Point2D p, boolean scale, boolean animation){
        Rectangle2D dest = new Rectangle2D.Double(p.getX(),p.getY(),0,0);
        PTransformActivity pan = getCamera().animateViewToCenterBounds(dest, false, cameraAnimation && animation ? animationSpeed : 0);
        if (scale) {
            // && (camera.getViewScale() < VISControl.getInstance().getSemanicZoomLevelThreshold(1))
            Rectangle2D.Double zoomArea = new Rectangle2D.Double(p.getX()-500, p.getY()-500, 1000, 1000);
            PTransformActivity zoom =
                getCamera().animateViewToCenterBounds(zoomArea, true, cameraAnimation && animation ? animationSpeed : 0);
            if (zoom != null) zoom.startAfter(pan);
        }
    }
    
    
    /**
     * Return the layer where the nodes are placed. 
     * @return layer with the nodes
     */
    public abstract PLayer getNodeLayer();
    
    /**
     * Zoom in on the given point. 
     * @param position point to zoom to (view coordinates)
     */
    public abstract void zoomIn(Point2D position);
    
    /**
     * Zoom out from the given point
     * @param position the point to zoom out from
     */
    public abstract void zoomOut(Point2D position);
    
    /**
     * Handle the right mouse button event
     * @param event the event where the right mouse button is released
     */
    public abstract void handleRightMouseButton(final PInputEvent event);
}
