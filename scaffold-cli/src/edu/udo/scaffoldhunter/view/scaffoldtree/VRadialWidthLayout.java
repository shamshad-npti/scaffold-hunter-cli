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

import edu.umd.cs.piccolo.util.PBounds;

/**
 * This layout is a modification of the radial layout. The angle extent
 * of annulus wedges is allocated according to the width of subtrees. Instead
 * of using the number of leaves in the subtree as their width this layout
 * respects the real width of the graphical representation of a node. The width
 * of a node is the diameter of a circle completely containing its
 * graphics. The width of a subtree is the maximum of its roots width and the
 * summation of the width of its children.
 * 
 */
public class VRadialWidthLayout extends VRadialLayout {

    /**
     *<code>minimumRadiusAdd</code> is the minimum value that must be added to the actual
     *radius to draw the next layer of nodes to avoid overlap
     */
    private double minimumRadiusAdd;
    
    /**
     * Padding around a node to avoid overlap
     */
    private static final int nodePadding = 5;//10;

    private HashMap<VNode,Double> width;

    /**
     * Creates a new VRadialLayout for the <code>vtree</code>
     * @param vtree that will use this layout
     * @param state 
     */
    public VRadialWidthLayout (VTree vtree, ScaffoldTreeViewState state) {
        super(vtree, state);
        width = Maps.newHashMap();
    }

    /**
     * Calculates and draws the layout of the nodes.
     * Draws also the special layer that underlines the alignment of nodes.
     */
    @Override
    public void drawLayout() {
        width.clear();
        angles.clear();
        radii.clear();
        
        //check if user factor is not zero. On older sessions it might be zero.
        if(state.getRadialWidthLayoutRadiusUserFactor() == 0) {
            state.setRadialWidthLayoutRadiusUserFactor(1.0);
        }
        
        minimumRadiusAdd = 0;
        double widthBound = calculateWidth(vtree.getRoot());
        double firstRadiusBound = widthBound/(2*Math.PI);
        double radiusAdd = state.getRadialWidthLayoutRadiusAdd() * state.getRadialWidthLayoutRadiusUserFactor();
        radiusAdd = Math.max(radiusAdd, minimumRadiusAdd);
        
        // refine width and radius
        calculateRadialWidth(vtree.getRoot(), firstRadiusBound, radiusAdd);

        if(vtree.getRoot().getTreeChildren().size() == 1)
            //special case: Subtree with only one child
            firstRadius = width.get(vtree.getRoot())/(Math.PI);
        else
            firstRadius = width.get(vtree.getRoot())/(2*Math.PI);

        firstRadius = Math.max(firstRadius, radiusAdd);
        
        radii.add(0d);
        int depth = vtree.getMaxDepth(vtree.getRoot(), 0);
        for (int i = 0; i < depth; i ++)
            radii.add(i*radiusAdd+firstRadius);

        // Calculate the angles
        calculateLayout(vtree.getRoot(), 0, Math.PI*2);
        drawNodes(vtree.getRoot(), 0);
        drawCircles();
    }

    /**
     * Calculates the width of the given node recursively taking
     * the width of all children into account. 
     * 
     * Note: This method updates the member variable minimumRadiusAdd.
     */
    private double calculateWidth(VNode v) {

        double nodeWidth = getDiameter(v) + nodePadding;

        double subtreeWidth = 0;
        // calculate the width and check if we need to increase the minimumRadiusAdd
        for (VNode c : v.getTreeChildren()) {
            subtreeWidth += calculateWidth(c);
            double distance = (nodeWidth+getDiameter(c))/2.0 + 2*nodePadding;
            minimumRadiusAdd = Math.max(minimumRadiusAdd, distance);
        }

        double w = Math.max(nodeWidth, subtreeWidth);

        return w;
    }
    
    /**
     * Calculates the width of the given node recursively taking
     * the width of all children into account. The width is adjusted
     * according to the space gain caused by the radial layout.
     * 
     * Note: This method updates the member variable width.
     */
    private double calculateRadialWidth(VNode v, double childRadius, double radiusAdd) {

        double nodeWidth = getDiameter(v) + 10;

        double subtreeWidth = 0;
        for (VNode c : v.getTreeChildren()) {
            subtreeWidth += calculateRadialWidth(c, childRadius+radiusAdd, radiusAdd);
        }

        if (vtree.getRoot() != v) {
            subtreeWidth *= (childRadius-radiusAdd)/childRadius;
        }
        // The child level will be at least minimumRadiusAdd away, but during our
        // subtree traversal we only have a temporary value. Using it might result
        // in uneven spacing.

        double w = Math.max(nodeWidth, subtreeWidth);
        width.put(v, w);

        return w;
    }
    
    private double getDiameter(VNode v) {
        if (v instanceof ScaffoldNode && ((ScaffoldNode)v).getScaffold().isImaginaryRoot()) return 0;
        PBounds b;
        if (v == vtree.getRoot())
            // ignores the scaling of the node
            b = v.getBoundsReference();
        else
            b = v.getFullBoundsReference();
        return 2 * b.getOrigin().distance(b.getCenter2D());
    }

    /**
     * This method calculates the angle for all nodes in the subtree
     * under the given node. It also recalculates the radius for each layer
     * that the nodes do not overlap.
     * @param Vnode v, the root of the subtree
     * @param angle1
     * @param angle2 the angles describe the cone of the subtree in which
     * the nodes will be drawn
     */
    private void calculateLayout(VNode v, double angle1, double angle2){

        //****************************************************************
        // Calculation of the angles
        //****************************************************************

        //Saving the angle of the VNode v in the HashMap angles
        angles.put(v, new Sector(angle1, angle2));

        double s;
        double alpha;

        double w = 0;
        for (VNode c : v.getTreeChildren())
            w += width.get(c);

        //special case: Subtree with only one child
        if((vtree.getRoot() == v) && v.getTreeChildren().size() == 1) {
            s = (Math.PI)/w;
            alpha = angle1;
        } else {
            //normal case:
            s = (angle2 - angle1)/w;
            alpha = angle1;
        }

        //****************************************************************
        //Calculating the layout for all children of v
        //****************************************************************

        for (VNode c : v.getTreeChildren()) {
            calculateLayout(c, alpha, alpha + (s * width.get(c)));
            alpha = alpha + (s * width.get(c));
        }

    }

    /**
     * This method calculates the x and y coordinates for each node of
     * the tree with the root <code>v</code> and draws them at this point.
     * @param v the root of the tree
     * @param depth the depth of the node where 0 is the depth of a node on the
     *  first radius.
     */
    public void drawNodes(VNode v, int depth){

        //****************************************************************
        //Calculating the coordinates and drawing the nodes
        //****************************************************************
        //special case: actual node v is the root
        if (vtree.getRoot() == v) {

            //Resize the root of a subtree in a new tab
            double factor = (firstRadius/v.getNodeSize())-1.5;
            factor = Math.max(factor,1);
            v.setScale(factor);
            Point2D c = v.getFullBoundsReference().getCenter2D();
            v.centerFullBoundsOnPoint(c.getX(),c.getY());

            //Set the edges from the root to the first layer of nodes to invisible
            for(VEdge e: v.getEdges()) { 
               e.setVisible(!vtree.getVCanvas().isHideSubtreeEdges());
            }
        }

        Sector sec = angles.get(v);
        double angle = (sec.getStartAngle()+sec.getEndAngle())/2;
        double x = Math.cos(angle)*radii.get(depth);
        double y = Math.sin(angle)*radii.get(depth);

        centerNodeOn(v, new Point2D.Double(x,y));

        for (VNode c : v.getTreeChildren())
            drawNodes(c, depth + 1);
    }

    /**
     * Rescales the radii while zooming.
     */
    
    @Override
    public void updateLayout() {
        updateLayout(true);
    }
    
    private void updateLayout(boolean doLayout) {
        if(!state.isFixedRadii()) {
            double scaleDown = Math.min(vtree.getVCanvas().getCamera().getViewScale(), 1);
            state.setRadialWidthLayoutRadiusAdd(1500*(1-Math.sqrt(scaleDown)));
        }
        if (doLayout)
            this.doLayout(true);
    }

    /**
     * Rescales the radii manually.
     * @param delta the difference between the old and the new radius
     */
    @Override
    public void updateRadii(double delta) {
        double newRadiusFactor = state.getRadialWidthLayoutRadiusUserFactor() * delta;
        newRadiusFactor = Math.min(10000 / state.getRadialWidthLayoutRadiusAdd(), newRadiusFactor);
        newRadiusFactor = Math.max(minimumRadiusAdd / state.getRadialWidthLayoutRadiusAdd(), newRadiusFactor);
        state.setRadialWidthLayoutRadiusUserFactor(newRadiusFactor);
        this.doLayout(true);
    };
    
    /**
     * Resets the radii to the default values.
     */
    @Override
    public void resetRadii() {
        state.setRadialWidthLayoutRadiusUserFactor(1.0);
        this.doLayout(true);
    };
    
    @Override
    public double getOuterRadius() {
        return radii.get(radii.size() - 1) + minimumRadiusAdd;
    }

}
