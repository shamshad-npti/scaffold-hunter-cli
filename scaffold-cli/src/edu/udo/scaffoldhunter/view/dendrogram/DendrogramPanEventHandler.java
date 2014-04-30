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

package edu.udo.scaffoldhunter.view.dendrogram;

import java.awt.Rectangle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PPanEventHandler;

/**
 * @author Philipp Lewe
 * 
 */
public class DendrogramPanEventHandler extends PPanEventHandler {
    private static Logger logger = LoggerFactory.getLogger(DendrogramPanEventHandler.class);
    DendrogramCanvas canvas;

    double deltaX;
    double deltaY;

    /**
     * @param canvas
     */
    public DendrogramPanEventHandler(DendrogramCanvas canvas) {
        super();
        this.canvas = canvas;
        setAutopan(false);
        setMinDragStartDistance(1.0);
    }

    @Override
    protected void startDrag(PInputEvent event) {
        super.startDrag(event);
        deltaX = event.getDelta().width;
        deltaY = event.getDelta().height;
    }

    /**
     * Pans the camera in response to the pan event provided.
     * 
     * @param event
     *            contains details about the drag used to translate the view
     */
    @Override
    protected void pan(final PInputEvent event) {
        logger.trace("canvas full bounds: {}", canvas.getLayer().getFullBounds());
        logger.trace("canvas visible rect: {}", canvas.getVisibleRect());
        logger.trace("position: {}, pcanvas position: {}", event.getPosition(), event.getCanvasPosition());
        logger.trace("delta: {}", event.getDelta());
        
        Rectangle r = canvas.getVisibleRect();
//        PDimension d = event.getDelta();
//        
//        deltaX += d.getWidth();
//        deltaY += d.getHeight();
//        
//        int deltaXrounded = (int)deltaX;
//        int deltaYrounded = (int)deltaY;
//        
//        deltaX -= deltaXrounded;
//        deltaY -= deltaYrounded;
//        
//        double newLocX = Math.max(0, (r.getLocation().x - deltaXrounded));
//        double newLocY = Math.max(0, (r.getLocation().y - deltaYrounded));
        
        double newLocX = (r.getLocation().x - event.getDelta().width);
        double newLocY = (r.getLocation().y - event.getDelta().height);
        
        r.setLocation((int)newLocX, (int)newLocY);
        
        canvas.scrollRectToVisible(r);
    }
}
