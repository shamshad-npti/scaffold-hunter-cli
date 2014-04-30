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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import edu.udo.scaffoldhunter.view.util.ExportPaintNode;
import edu.udo.scaffoldhunter.view.util.SelectionState;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * This class represents an edge connecting two nodes. It is diplayed as a
 * simple line.
 * 
 * @author kriege
 */
public class VEdge extends PNode implements PropertyChangeListener, ExportPaintNode {

    private static final double DEFAULT_EDGE_WIDTH = 1.5;
    private static final double SELECTED_EDGE_WIDTH = 2.0;

    private ScaffoldNode parent;
    private ScaffoldNode child;
    private Color parentColor = null;
    private Color childColor = null;
    private Color mixed = null;
    private Line2D.Double line = new Line2D.Double();
    private Double width = null;

    /**
     * Creates an edge connecting <b>node1</b> and <b>node2</b>. This edge will
     * also be added to the nodes.
     * 
     * @param parent
     *            first node adjacent to this edge
     * @param child
     *            second node adjacent to this edge
     */
    public VEdge(ScaffoldNode parent, ScaffoldNode child) {
        this.parent = parent;
        this.child = child;
        this.parent.addEdge(this);
        this.child.addEdge(this);
        this.parent.addPropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS, this);
        this.child.addPropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS, this);
        this.setPickable(false);
        this.updateCoords();
    }

    void setColors(Color parent, Color child) {
        this.parentColor = parent;
        this.childColor = child;
        if (parent != null && child != null) {
            int r = (parent.getRed() + child.getRed()) / 2;
            int g = (parent.getGreen() + child.getGreen()) / 2;
            int b = (parent.getBlue() + child.getBlue()) / 2;
            mixed = new Color(r, g, b);
        }
    }

    void uncolor() {
        this.parentColor = null;
        this.childColor = null;
        this.mixed = null;
    }

    /**
     * Updates the coordinates of the drawn line and triggers a repaint. This
     * method is used when the position of <b>node1</b> or <b>node2</b> changes.
     */
    public void updateCoords() {
        line.setLine(parent.getFullBounds().getCenter2D(), child.getFullBounds().getCenter2D());
        Rectangle2D r = line.getBounds2D();
        // adding 1 to the width and height prevents the bounds from
        // being marked as empty and is much faster than createStrokedShape()
        setBounds(r.getX(), r.getY(), r.getWidth() + 1, r.getHeight() + 1);
        invalidatePaint();
    }

    @Override
    public void paint(PPaintContext p) {
        paint(p, false);
    }
    
    private void paint(PPaintContext p, boolean export) {
        Graphics2D g = p.getGraphics();

        Color parent;
        Color child;
        
        // child and parent colors are calculated seperately and then used for gradient coloration
        if (this.parent.getSelection() == SelectionState.SELECTED) {
            // TODO universal config
            parent = Color.RED;
        } else if (this.parent.getSelection() == SelectionState.HALFSELECTED) {
            // TODO universal config
            parent = Color.ORANGE;
        } else if(this.parentColor != null) {
            parent = parentColor;
        } else
            parent = new Color(0.0f, 0.6f, 0.2f);
        
        if (this.child.getSelection() == SelectionState.SELECTED) {
            // TODO universal config
            child = Color.RED;
        } else if (this.child.getSelection() == SelectionState.HALFSELECTED) {
            // TODO universal config
            child = Color.ORANGE;
        } else if(this.childColor != null) {
            child = childColor;
        } else
            child = new Color(0.0f, 0.6f, 0.2f);
        
        g.setPaint(new GradientPaint(line.getP1(), parent, line.getP2(), child));               
        
        if (export) {
            g.setStroke(new BasicStroke(2 * (float)getEdgeWidth()));
        } else {
            g.setStroke(new PFixedWidthStroke((float)getEdgeWidth()));
        }
        g.draw(line);        
    }
    
    @Override
    public void exportPaint(PPaintContext paintContext) {
        paint(paintContext, true);
    }

    /**
     * @return the width
     */
    public double getEdgeWidth() {
        if (width == null) {
            if (parent.getSelection() == SelectionState.UNSELECTED) {
                return DEFAULT_EDGE_WIDTH;
            } else {
                return SELECTED_EDGE_WIDTH;
            }
        } else {
            return width;
        }
    }

    /**
     * @param width
     *            the width to set can be <code>null</code>, in that case the
     *            default width for selected and unselected will be used.
     */
    public void setEdgeWidth(Double width) {
        this.width = width;
        invalidatePaint();
    }

    /**
     * Removes all references of nodes to this edge
     */
    public void dispose() {
        parent.removePropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS, this);
        child.removePropertyChangeListener(PNode.PROPERTY_FULL_BOUNDS, this);
        parent.removeEdge(this);
        child.removeEdge(this);
        child.setParentEdge(null);
    }

    /*
     * Whenever the FullBounds of node1 or node2 change the coordinates of this
     * edge will be updated.
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        this.updateCoords();
    }

}
