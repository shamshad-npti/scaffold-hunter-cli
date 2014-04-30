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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * @author Philipp Lewe
 * 
 */
public class DendrogramZoomHandler extends PBasicInputEventHandler implements KeyListener {
    // the canvas this eventHandler is associated with
    private DendrogramCanvas canvas;

    /**
     * Creates a new DendrogramZoomHandler for the <code>canvas</code>
     * 
     * @param canvas
     *            that will use this EventHandler
     */
    public DendrogramZoomHandler(DendrogramCanvas canvas) {
        super();
        this.canvas = canvas;
        canvas.addKeyListener(this);
        getEventFilter().rejectAllEventTypes();
        getEventFilter().setAcceptsMouseWheelRotated(true);
        getEventFilter().setAcceptsKeyPressed(true);
        getEventFilter().setAcceptsKeyReleased(true);
        getEventFilter().setAcceptsKeyTyped(true);
    }

    /**
     * Evaluates mouse wheel rotation and calls the correct zoom method.
     * 
     * @param event
     *            the generated mouseWheelRotation event
     */
    @Override
    public void mouseWheelRotated(PInputEvent event) {
        if (event.getWheelRotation() > 0) {
            if (event.isControlDown()) {
                canvas.zoomOutVertical(event.getPosition());
            } else {
                canvas.zoomOutHorizontal(event.getPosition());
            }
        } else {
            if (event.isControlDown()) {
                canvas.zoomInVertical(event.getPosition());
            } else {
                canvas.zoomInHorizontal(event.getPosition());
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent event) {
        
        switch (event.getKeyCode()) {
        case KeyEvent.VK_PLUS:
            canvas.zoomInHorizontal(canvas.getViewportCenter());
            break;

        case KeyEvent.VK_EQUALS:
            canvas.zoomInHorizontal(canvas.getViewportCenter());
            break;

        case KeyEvent.VK_MINUS:
            canvas.zoomOutHorizontal(canvas.getViewportCenter());
            break;

        case KeyEvent.VK_0:
            canvas.zoomToOverview();
            break;
            
        default:
            break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }
}