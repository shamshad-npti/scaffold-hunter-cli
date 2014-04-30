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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * This class represents a line for the scene graph between nodes of the
 * dendrogram
 * 
 * @author Philipp Lewe
 * 
 */
public class DendrogramEdge extends PPath {
    
    private final Stroke stroke = new PFixedWidthStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
    private DendrogramViewNode parentNode = null;
    private DendrogramViewNode childNode = null;
    
    /**
     * Creates a DendrogramEgde from parentNode to childNode
     * 
     * @param parentNode the parentNode
     * @param childNode the childNode
     */
    public DendrogramEdge(DendrogramViewNode parentNode, DendrogramViewNode childNode) {
        super();
        this.parentNode = parentNode;
        this.childNode = childNode;
        setStroke(stroke);
    }
        
    /**
     * Tells the DendrogramEdge to recalculate the line between parent and child
     */
    public void calcLine() {
        Point2D[] linePoints = new Point2D[2];
        
        // line between parent and child
        linePoints[0] = new Point2D.Double(childNode.getRelativePos().x, 0);
        linePoints[1] = childNode.getRelativePos();
        
        setPathToPolyline(linePoints);
    }
    
    /**
     * @return the parentNode
     */
    public DendrogramViewNode getParentNode() {
        return parentNode;
    }

    /**
     * @return the childNode
     */
    public DendrogramViewNode getChildNode() {
        return childNode;
    }

    /**
     * @param clusterColor the clusterColor to set
     */
    public void setClusterColor(Color clusterColor) {
        setStrokePaint(clusterColor);
        invalidatePaint();
    }
}

