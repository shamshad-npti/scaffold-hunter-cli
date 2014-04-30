/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.Timer;

/**
 * A resize listener, which informs a {@link VCanvas} of a {@link ScaffoldTreeView} about resize events.
 * 
 * @author schrins
 *
 */
public class VCanvasResizeListener implements ComponentListener, ActionListener {

    private VCanvas canvas;
    private Timer resizeTimer;    
    private final int RESIZE_DELAY = 500; // minimum time between forwarded resize events

    /**
     * Creates a new listener.
     * @param canvas the {@link VCanvas}, which is informed about any resize events.
     */
    public VCanvasResizeListener(VCanvas canvas) {
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
        if (resizeTimer==null) {
            // create new timer
            resizeTimer = new Timer(RESIZE_DELAY,this);
            resizeTimer.start();
        }
        else {
            // reset existing timer, because the delay has not elapsed
            resizeTimer.restart();
        }
    }

    @Override
    public void componentShown(ComponentEvent arg0) {
        // nothing to do
    }
    
    /**
     * @param event
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        if (event.getSource()==resizeTimer) {
            // no new events for RESIZE_DELAY milliseconds -> forward resize to canvas
            resizeTimer.stop();
            resizeTimer = null;
            canvas.autoZoomToOverview();
        }
    }
}
