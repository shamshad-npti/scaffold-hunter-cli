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
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;



/**
 * This class visualizes a circular sector with a label by extending
 * the Piccolo scenegraph element class PNode.
 * The arc is defined by the position of two nodes and the fixed
 * center.
 * 
 * @author Kriege
 */
public class CircularSector extends PNode {

    private Arc2D.Double arc;
    private VNode firstNode;
    private VNode lastNode;
    private PText label;
    private VRadialLayout layout;
    private static Logger logger = LoggerFactory.getLogger(ScaffoldTreeView.class);

    /**
     * Creates a new circular sector
     * @param firstNode the first node (in the clockwise oder of siblings) that
     * lies in this sector
     * @param lastNode the last node in this sector
     * @param label the text label of the sector
     * @param color	the color of the sector
     * @param layout
     */
    public CircularSector (VNode firstNode, VNode lastNode, String label, Color color, VRadialLayout layout) {

        this.layout = layout;
        this.firstNode = firstNode;
        this.lastNode = lastNode;

        this.arc = new Arc2D.Double();
        arc.setArcType(Arc2D.PIE);

        if (!label.equals("")) {
            // cut off digits behind the dot
            DecimalFormat format = new DecimalFormat("#0.00");
            try{
                Double value = Double.parseDouble(label);
                label = format.format(value);
            }
            catch(NumberFormatException e) {
                logger.warn("label {} in ScaffoldTreeView was no correct number", label);
            }
            this.label = new PText(label);
            this.addChild(this.label);
        }

        updateArc();

        setPaint(color);
    }

    /**
     * Updates the size of the sector according to the position of the nodes
     */
    void updateArc() {
        double r = layout.getOuterRadius();
        arc.setFrame(-r, -r, 2*r, 2*r);

        double start = 2*Math.PI - layout.getSector(lastNode).getEndAngle();
        double end   = 2*Math.PI - layout.getSector(firstNode).getStartAngle();
        arc.setAngleStart(Math.toDegrees(start));
        arc.setAngleExtent(Math.toDegrees(end-start));
        setBounds(arc.getBounds2D());

        // calculate label position
        double ang =  (start+end)/2;
        double x = Math.cos(ang) * layout.getInnerRadius()*0.75;
        double y = - Math.sin(ang) * layout.getInnerRadius()*0.75;

        if (label != null) {
            // rotate the text for better space utilization
            double rotation = (2*Math.PI - ang)%(2*Math.PI);
            if(rotation > 0.5*Math.PI && rotation < 1.5*Math.PI)
                rotation -= Math.PI;
            label.setRotation(rotation);
            
            // make size dependent on radius and available angle
            double maxSize = layout.getInnerRadius()*0.06*(end-start);
            label.centerFullBoundsOnPoint(x, y);
            label.setScale(Math.min(maxSize, layout.getInnerRadius()*0.004*Math.sqrt(end-start)));
        }

        invalidatePaint();
    }

    /**
     * Color the background of all nodes in this sector in this sectors color.
     */
    public void colorNodesBackground() {
        LinkedList<VNode> q = Lists.newLinkedList();
        VNode v = firstNode;
        do {
            q.offer(v);
            v = v.getClockwiseSibling();
        } while (v.getAnticlockwiseSibling() != lastNode);

        while (!q.isEmpty()) {
            v = q.poll();
            v.setPaint(getPaint());
            q.addAll(v.getTreeChildren());
        }
    }

    /*
	private int getDepth() {
		VNode v = firstNode;
		int depth = 0;
		do {
			int newdepth = layout.getVTree().getMaxDepth(v, 0);
			if (newdepth > depth) depth = newdepth;
			v = v.getClockwiseSibling();
		} while (v.getAnticlockwiseSibling() != lastNode);
		return depth + 1;
	}
     */

    @Override
    public void paint(PPaintContext aPaintContext) {
        Graphics2D g = aPaintContext.getGraphics();

        g.setPaint(getPaint());
        g.fill(arc);

        g.setPaint(Color.BLACK);
        g.setStroke(new BasicStroke(1));
        g.draw(arc);
    }

    //mark as not serializable
    private void writeObject(ObjectOutputStream stream) throws IOException {
        throw new NotSerializableException();
    }
    
    private void readObject(ObjectInputStream stream) throws IOException {
        throw new NotSerializableException();
    }
}
