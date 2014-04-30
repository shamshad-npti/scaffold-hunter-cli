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

package edu.udo.scaffoldhunter.view.treemap;

import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * A listener with its only purpose is to give the new dimension of the frame to
 * the canvas.
 * 
 * @author Lappie
 * 
 */
class TreeMapCanvasResizeListener implements ComponentListener {

    private Container container;
    private TreeMapCanvas canvas;
    

    public TreeMapCanvasResizeListener(Container container, TreeMapCanvas canvas) {
        this.container = container;
        this.canvas = canvas;
    }

    @Override
    public void componentHidden(ComponentEvent arg0) {
        // nothing to do
    }

    @Override
    public void componentMoved(ComponentEvent arg0) {
        // nothing to do
    }

    @Override
    public void componentResized(ComponentEvent arg0) {
        canvas.updateDimension(container.getSize());
    }

    @Override
    public void componentShown(ComponentEvent arg0) {
        // nothing to do
    }
}