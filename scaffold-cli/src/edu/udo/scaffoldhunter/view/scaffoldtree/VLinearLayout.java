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

import java.awt.geom.Point2D;
import java.util.HashMap;

import com.google.common.collect.Maps;

/**
 * The <b>LinearLayout</b> is a classic tree layout where
 * all subtrees are arranged vertically and each subtree
 * is drawn from left to right.
 * 
 * @author Gorecki
 * @author Kriege
 * @author Schrader
 * @author Wiesniewski
 */
public class VLinearLayout extends VLayout {

    /**
     * <b>hdist</b> is the distance between each layer of nodes.
     * It can be modified with <code>updateRadii()</code>.
     */
    private double hdist = 1000;

    /**
     * <b>vdist</b> is the distance between each node on one layer.
     */
    private double vdist;

    /**
     * <b>minhdist</b> is the minimum horizontal distance between
     * each layer.
     */
    private double minhdist = 300;

    /**
     * <b>maxhdist</b> is the maximum horizontal distance between
     * each layer.
     */
    private double maxhdist = 10000;

    /**
     * <b>coords</b> contains for each VNode in the VTree a Point2D
     * as coordinates. The keyset is a set of ScaffoldIDs of all VNodes
     * in the VTree.
     */
    private HashMap<VNode,Point2D> coords;

    /**
     * <b>width</b> contains for each VNode in the VTree the number of
     * leaves in his subtree. These values indicate how much vertical space
     * each subtree needs.
     */
    private HashMap<VNode,Integer> widths;

    /**
     * Creates a new VLinearLayout for the given <b>VTree</b>.
     * @param vtree that will be drawn with this layout.
     * @param state 
     */
    public VLinearLayout(VTree vtree, ScaffoldTreeViewState state){
        super(vtree, state);
        this.coords = Maps.newHashMap();
        this.widths = Maps.newHashMap();
    }

    @Override
    protected void drawLayout() {

        // Search for the highest SVG, to avoid overlapping of nodes
        double maxheight = 0;
        for (ScaffoldNode node : vtree.getVNodes()) {
            maxheight = Math.max(maxheight, node.getScaffold().getSvgHeight());
        }
        vdist = maxheight;

        calcWidths(vtree.getRoot());
        calcCoords(vtree.getRoot(),0,0);

        // Move Nodes to the calculated coordinates
        moveNodes();
    }

    /**
     * This method moves the nodes in <b>vtree</b> to their coordinates
     * which are contained in <b>coords</b>.
     */
    private void moveNodes(){
        for (VNode node : vtree.getVNodes()) {
            Point2D xy = coords.get(node);
            centerNodeOn(node, xy);
        }
    }

    /**
     * Calculates for each node the number of leaves in his
     * subtree. These Values get stored in <b>widths</b>.
     * @param node The root VNode of the VTree.
     */
    private void calcWidths(VNode node){
        int numchildren = node.getChildCount();
        if ( numchildren > 0){
            for (int i=0; i<numchildren; i++) {
                calcWidths(node.getTreeChildren().get(i));
            }
            int mywidth = 0;
            for (VNode n : node.getTreeChildren()) {
                mywidth += widths.get(n);
            }
            widths.put(node, mywidth);
        } else {
            widths.put(node, 1);
        }
    }

    /**
     * Calculates the coordinates for all nodes. The coordinates get stored as Point2D
     * objects in <b>coord</b>. Make shure you have called <code>calcWidths()</code> first.
     * @param node The root VNode of the VTree.
     * @param x Can be used as an offset. Normally use 0.
     * @param y Can be used as an offset. Normally use 0.
     */
    private void calcCoords(VNode node, double x, double y){
        //TODO in the original SH there was no check if not null needed, need to check if it's still working correctly 
        if(node.getParentEdge()!=null) {
            node.getParentEdge().setVisible(true); 
        }
        Point2D newpoint = new Point2D.Double(x,y);
        coords.put(node, newpoint);
        double newx = (vtree.getNodesDepth(node) + 1) * hdist;
        double ypointer = y - ((widths.get(node)*vdist) / 2);
        double inc = 0;
        double previous = 0;
        for (VNode child : node.getTreeChildren()){
            int hiswidth = widths.get(child);
            inc = ((hiswidth * vdist) / 2) + previous;
            ypointer += inc;
            double newy = ypointer;
            previous = ((hiswidth * vdist) / 2);
            calcCoords(child,newx, newy);
        }
    }

    @Override
    public void updateLayout(){
        doLayout(true);
    }

    @Override
    public void updateRadii(double delta) {
        this.hdist += delta;
        this.hdist = Math.max(this.hdist, this.minhdist);
        this.hdist = Math.min(this.hdist, this.maxhdist);
        this.doLayout(true);
    };

}
