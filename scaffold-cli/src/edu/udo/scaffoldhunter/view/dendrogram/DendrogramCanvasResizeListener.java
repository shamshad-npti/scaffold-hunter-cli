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

package edu.udo.scaffoldhunter.view.dendrogram;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages
 * 
 * @author Philipp Lewe
 * 
 */
public class DendrogramCanvasResizeListener implements ComponentListener {
    private static Logger logger = LoggerFactory.getLogger(DendrogramCanvasResizeListener.class);

    DendrogramCanvas canvas;
//    boolean isFirstStart = true;
//    boolean startZoomSet = false;

    /**
     * @param canvas
     */
    public DendrogramCanvasResizeListener(DendrogramCanvas canvas) {
        this.canvas = canvas;
    }
    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.
     * ComponentEvent)
     */
    @Override
    public void componentResized(ComponentEvent e) {
        logger.trace("canvas resized");
                canvas.adaptToParentSize();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.
     * ComponentEvent)
     */
    @Override
    public void componentMoved(ComponentEvent e) {
        logger.trace("canvas moved");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.
     * ComponentEvent)
     */
    @Override
    public void componentShown(ComponentEvent e) {
        logger.trace("canvas shown");
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.
     * ComponentEvent)
     */
    @Override
    public void componentHidden(ComponentEvent e) {
        logger.trace("canvas hidden");
    }
}
