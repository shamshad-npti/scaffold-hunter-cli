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

import java.awt.geom.Point2D;
import java.util.ListIterator;

import edu.udo.scaffoldhunter.gui.util.StructureSVGPanel;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * @author Philipp Lewe
 * 
 */
public class DendrogramNodeZoomPanel extends StructureSVGPanel {
    
    DendrogramCanvas canvas;
    
    /**
     * @param canvas
     *            the associated canvas
     * @param svgCache
     *            the {@link SVGCache} where new svgs are retrieved from
     * 
     */
    public DendrogramNodeZoomPanel(DendrogramCanvas canvas, SVGCache svgCache) {
        super(svgCache);
        this.canvas = canvas;
        canvas.addInputEventListener(new ActiveNodeListener());
    }


    private class ActiveNodeListener extends PBasicInputEventHandler {

        public ActiveNodeListener() {
            getEventFilter().rejectAllEventTypes();
            getEventFilter().setAcceptsMouseMoved(true);
            getEventFilter().setAcceptsMouseExited(true);
        }

        @Override
        public void mouseMoved(PInputEvent event) {
            super.mouseMoved(event);
            PCamera camera = event.getCamera();
            Point2D eventPoint = camera.localToGlobal(camera.viewToLocal(event.getPosition()));
            
            @SuppressWarnings("rawtypes")
            ListIterator childNodes = canvas.getLeafNodes().getChildrenIterator();
            
            DendrogramViewNode nearestNode = null;
            double nearestDist = Double.MAX_VALUE;
            
            while (childNodes.hasNext()) {
                PNode node = (PNode) childNodes.next();
                
                if(node instanceof DendrogramViewNode) {
                    DendrogramViewNode n = (DendrogramViewNode) node;
                    assert n.isLeaf();
                    
                    Point2D nodeCenter = n.localToGlobal(n.getBounds().getCenter2D());
                    
                    if(nodeCenter.distance(eventPoint) < nearestDist) {
                        nearestNode = n;
                        nearestDist = nodeCenter.distance(eventPoint);
                    }
                }
            }
            
            if (nearestNode != null) {
                updateSVG(nearestNode.getModel().getContent());
            }
        }

        @Override
        public void mouseExited(PInputEvent event) {
            super.mouseExited(event);
            updateSVG(null);
        }
    }

}
