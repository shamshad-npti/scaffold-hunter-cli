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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * This layout is a modified radial layout which is based
 * on the paper 'Drawing Free Trees' from Peter Eades
 * 
 * @author Gorecki
 * @author Kriege
 * @author Schrader
 * @author Wiesniewski
 */
public class VRadialLayout extends VLayout{
    private final static float circleStrokeWidth = 1f;
    private final static Color circleColor = Color.LIGHT_GRAY;
    
    /**
     * <code>circlelayer</code> is a special layer with circles that underlines
     * the alignment of nodes.
     */
    private PLayer circlelayer;

    /**
     *<code>minimum_distance</code> is the minimum distance between to nodes, so
     *that they do not overlap.
     */
    private double minimum_distance;

    /**
     *<code>maximum_SVG_size</code> is the maximum size of all SVGs in the actual
     *VTree.
     */
    private double maximum_SVG_size;

    /**
     *<code>radii</code> is an ArrayList, that saves the radius for each layer of nodes.
     */
    protected ArrayList<Double> radii;

    /**
     * <code>firstRadius</code> saves the radius of the first layer of nodes.
     */
    protected double firstRadius;

    /**
     *<code>angles</code> is a HashMap, that saves the angles for each nodes.
     */
    protected HashMap<VNode,Sector> angles;

    private ArrayList<VNode> separators;
    private ArrayList<CircularSector> sectors;

    /**
     * Creates a new VRadialLayout for the <code>vtree</code>
     * @param vtree that will use this layout
     * @param state
     */
    public VRadialLayout (VTree vtree, ScaffoldTreeViewState state) {
        super(vtree, state);
        radii  = new ArrayList<Double>();
        angles = Maps.newHashMap();
        circlelayer = new PLayer();
        separators = Lists.newArrayList();
        sectors = new ArrayList<CircularSector>();
        firstRadius = 0;
    }

    /**
     * The radial layout returns a special layer with circles that underlines
     * the alignment of nodes and divides the first ring in sectors.
     * @return a layer that will be displayed under all nodes and edges.
     */
    @Override
    public PLayer getBackgroundLayer() {
        return circlelayer;
    }

    /**
     * Calculates and draws the layout of the nodes.
     * Draws also the special layer that underlines the alignment of nodes.
     */
    @Override
    public void drawLayout() {
        vtree.countSubTreeLeaves(vtree.getRoot());

        radii.clear();
        angles.clear();
        //Reset the maximum_SVG_size to the size of the root SVG
        maximum_SVG_size = vtree.getRoot().getNodeSize();
        //Calcuate the angles and radii
        calculateLayout(vtree.getRoot(), 0, 0, Math.PI*2);
        //Snap the radii together
        for(int i = radii.size();i>2;i--) {
            radii.set(i-2,radii.get(i-1)-state.getRadialWidthLayoutRadiusAdd());
        }
        drawNodes(vtree.getRoot());
        drawCircles();
    };

    /**
     * This method calculates the angle for all nodes in the subtree
     * under the given node. It also recalculates the radius for each layer
     * that the nodes do not overlap.
     * @param v the root of the subtree
     * @param radius the actual radius for the layer where VNode v is
     * @param angle1 
     * @param angle2 the angles describe the cone of the subtree in which
     * the nodes will be drawn
     */
    private void calculateLayout(VNode v, double radius, double angle1, double angle2){

        //****************************************************************
        // Calculation of the angles
        //****************************************************************

        //Saving the angle of the VNode v in the HashMap angles
        angles.put(v, new Sector(angle1, angle2));

        double s;
        double alpha;

        //special case: Subtree with only one child
        if((vtree.getRoot() == v) && v.getTreeChildren().size() == 1) {
            s = (Math.PI/2)/(v.getNumLeaves());
            alpha = angle1;
        } else {
            //normal case:
            s = (angle2 - angle1)/v.getNumLeaves();
            alpha = angle1;
        }


        //****************************************************************
        // Calculation of the radii
        //****************************************************************

        double new_radius;

        //Saving the radius in the ArrayList radii
        int depth = vtree.getNodesDepth(v);
        if(radii.size() <= depth){
            radii.add(depth,radius);
        } else if (radii.get(depth) < radius) {
            radii.set(depth,radius);
        }

        //Calculating the new maximum_SVG_size for the radii of the next layer
        for(VNode n: v.getTreeChildren()) {
            maximum_SVG_size = Math.max(n.getNodeSize(), maximum_SVG_size);
        }

        //minimum distance between two nodes is more than the maximum SVG size
        minimum_distance = maximum_SVG_size+5;

        //radius with no node overlapping
        new_radius = minimum_distance / (2 * (Math.sin((s/2))));

        //temp_radius is the minimal radius that must be between to circles
        double temp_radius = radius + state.getRadialWidthLayoutRadiusAdd();
        if (new_radius < temp_radius) new_radius = temp_radius;


        //****************************************************************
        //Calculating the layout for all children of v
        //****************************************************************

        for (int i=0; i<v.getChildCount(); i++){
            VNode u = v.getTreeChildren().get(i);
            calculateLayout(u, new_radius, alpha, alpha + (s * u.getNumLeaves()));
            alpha = alpha + (s * u.getNumLeaves());
        }

    }

    /**
     * This method calculates the x and y coordinates for each node of
     * the tree with the root <code>v</code> and draws them at this point.
     * @param v the root of the tree
     */
    public void drawNodes(VNode v){
        //****************************************************************
        //Calculating the coordinates and drawing the nodes
        //****************************************************************


        //special case: actual node v is the root
        if (vtree.getRoot() == v) {

            //Resize the root of a subtree in a new tab
            double rootSVGSize = v.getNodeSize();
            if(radii.size()>= 2) firstRadius = radii.get(1);
            if(firstRadius==0) firstRadius = 1;
            double factor = (firstRadius/rootSVGSize)-1.5;
            factor = Math.max(factor,1);
            v.setScale(factor);
            Point2D c = v.getFullBoundsReference().getCenter2D();
            v.centerFullBoundsOnPoint(c.getX(),c.getY());

            //Set the edges from the root to the first layer of nodes to invisible
            for(VEdge e: v.getEdges()) {
                e.setVisible(vtree.getVCanvas().isHideSubtreeEdges());
            }
        }

        Sector sec = getSector(v);
        double angle = (sec.getStartAngle()+sec.getEndAngle())/2;
        double x = Math.cos(angle)*radii.get(vtree.getNodesDepth(v));
        double y = Math.sin(angle)*radii.get(vtree.getNodesDepth(v));

        centerNodeOn(v, new Point2D.Double(x,y));

        for (int i=0; i<v.getChildCount(); i++){
            VNode u = v.getTreeChildren().get(i);
            drawNodes(u);
        }
    }

    /**
     * This method draws the circles on which the nodes are lying.
     */
    public void drawCircles(){

        ArrayList<PNode> tmp = new ArrayList<PNode>();
        for (Object p : circlelayer.getChildrenReference())
            if (p instanceof PPath)
                tmp.add((PPath)p);
        circlelayer.removeChildren(tmp);


        int depth = vtree.getMaxDepth(vtree.getRoot(),0);
        double tempradius;

        for (int i=depth; i>0; i--){
            tempradius = radii.get(i)*2;
            PPath circle = PPath.createEllipse(0,0,(float)tempradius,(float)tempradius);  //radius*2*i,radius*2*i;
            PFixedWidthStroke stroke = new PFixedWidthStroke(circleStrokeWidth);
            circle.setStroke(stroke);
            circle.setStrokePaint(circleColor);
            circle.setPaint(null);
            circlelayer.addChild(circle);
            circle.centerFullBoundsOnPoint(0,0);
        }
    }

    @Override
    public void setSeparators(ArrayList<VNode> separators, ArrayList<String> caption, ArrayList<Color> colors) {
        this.separators = separators;
        clearSeparators();
        VNode n1, n2;

        for (int i = 0; i < separators.size(); i++) {
            n1 = separators.get(i);
            if (i == separators.size()-1)
                n2 = separators.get(0);
            else
                n2 = separators.get(i+1);

            addSector(new CircularSector(n1, n2.getAnticlockwiseSibling(), caption.get(i), colors.get(i), this));
        }
    }

    @Override
    public void clearSeparators() {
        circlelayer.removeChildren(sectors);
        sectors.clear();
        for (VNode v : vtree.getVNodes()) {
            v.setPaint(Color.WHITE);
        }
    }

    private void addSector (CircularSector s) {
        sectors.add(s);
    }

    private boolean separatorsValid() {
        int i = 0;
        List<VNode> nodes = vtree.getRoot().getTreeChildren();
        for (VNode v : separators) {
            while (i < nodes.size() && nodes.get(i) != v) i++;
        }
        return i < nodes.size();
    }

    @Override
    public void layoutAnimationFinished() {
        if (!sectors.isEmpty()) {
            if (!separatorsValid()) {
                clearSeparators();
                return;
            }
            for (CircularSector s : sectors) {
                s.updateArc();
                if (!circlelayer.getChildrenReference().contains(s)) {
                    circlelayer.addChild(0, s); // drawn in the back behind the circles
                    s.colorNodesBackground();
                }
            }
        }
    }

    /**
     * Rescales the radii while zooming.
     */
    @Override
    public void updateLayout() {
        double viewScale = vtree.getVCanvas().getCamera().getViewScale();

        if(!state.isFixedRadii()) {
            double newMRA = 1500*(1-Math.min(viewScale,1))*(1-Math.min(viewScale,1));
            newMRA = Math.max(newMRA, 1.2*maximum_SVG_size);
            state.setRadialWidthLayoutRadiusAdd(newMRA);
            this.doLayout(true);
        }
    }

    /**
     * Rescales the radii manually.
     * @param delta the difference between the old and the new radius
     */
    @Override
    public void updateRadii(double delta) {
        double radiusAdd = state.getRadialWidthLayoutRadiusAdd();
        radiusAdd += delta;
        radiusAdd = Math.max(radiusAdd,minimum_distance);
        radiusAdd = Math.min(radiusAdd,10000);
        state.setRadialWidthLayoutRadiusAdd(radiusAdd);
        this.doLayout(true);
    };

    /**
     * Returns  the sector assigned to node <code>v</code> All
     * successors of <code>v</code> will be layed out within this
     * sector.
     * @param v
     * @return the sector assigned to the node <code>v</code>.
     */
    public Sector getSector(VNode v) {
        return angles.get(v);
    }

    /**
     * @return a radius which encompasses all nodes on the outer ring
     */
    public double getOuterRadius() {
        return 2 * radii.get(radii.size()-1) - radii.get(radii.size() - 2);
    }

    /**
     * @return the radius of the inner ring
     */
    public double getInnerRadius() {
        return radii.get(1);
    }

    /**
     * A sector defined by two angles
     */
    public static class Sector {
        private double startAngle;
        private double endAngle;

        /**
         * @param startAngle
         * @param endAngle
         */
        public Sector (double startAngle, double endAngle) {
            this.startAngle = startAngle;
            this.endAngle = endAngle;
        }

        /**
         * @return the start angle
         */
        public double getStartAngle() { return startAngle; }
        /**
         * @return the end angle
         */
        public double getEndAngle() { return endAngle; }
    }

}
