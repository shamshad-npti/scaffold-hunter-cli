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
import java.util.HashMap;

import com.google.common.collect.Maps;

import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * The <b>BalloonLayout</b> is a radial layout where all nodes are
 * drawn on circles around their ancestors.
 * 
 * @author Gorecki
 * @author Kriege
 * @author Schrader
 * @author Wiesniewski
 */
public class VBalloonLayout extends VLayout{
    private final static Color circleColor = Color.LIGHT_GRAY;
    private final static float circleStrokeWidth = 1f;
    /**
     * The <b>circlelayer</b> contains the circles that will be drawn under
     * the scaffolds.
     */
    private PLayer circlelayer;
    /**
     * The minimum radius for every node.
     */
    private double minrad = 300;
    /**
     * The minimum radius for every node withouot children.
     */
    private double minrad_leave;
    /**
     * The scaling factor corrects the approximation of the circumference.
     */
    private double scale = 1.2;
    /**
     * The VTree that will be drawn in this layout.
     */
    private VTree vtree;
    /**
     * Contains the <b>Point2D</b> coordinates of every node in the VTree.
     */
    private HashMap<VNode,Point2D> coords;
    /**
     * Contains a value for every node in the VTree that indicates the maximum
     * radius of his subtree.
     */
    private HashMap<VNode,Double> maxradius;
    /**
     * Contains a radius for every node of the VTree.
     */
    private HashMap<VNode,Double> radius;

    /**
     * Creates a new <b>VBalloonLayout</b> for the given VTree.
     * @param vtree This VTree will be drawn with this layout.
     * @param state 
     */
    public VBalloonLayout(VTree vtree, ScaffoldTreeViewState state){
        super(vtree, state);
        this.vtree = vtree;
        circlelayer = new PLayer();
        coords = Maps.newHashMap();
        maxradius = Maps.newHashMap();
        radius = Maps.newHashMap();
    }

    /**
     * This method clears all Hashmaps.
     */
    private void initMaps(){
        coords.clear();
        radius.clear();
    }

    /**
     * This method searches for the greatest scaffold and sets
     * the <b>minrad_leave</b> on this value. This avoids overlapping of
     * the scaffolds.
     */
    public void calcMinRadLeave(){
        int h;
        int w;
        int max;
        for (ScaffoldNode node : vtree.getVNodes()) {
            h = node.getScaffold().getSvgHeight();
            w = node.getScaffold().getSvgWidth();
            max = Math.max(h, w)/2;
            this.minrad_leave = Math.max(max, this.minrad_leave);
        }
    }

    @Override
    public void drawLayout(){
        initMaps();
        calcMinRadLeave();
        calculateRadii(vtree.getRoot());
        calculateCoords(vtree.getRoot(),0,0,0);
        moveNodes();
        // Draw circles
        if (circlelayer.getChildrenCount()>0)
            circlelayer.removeAllChildren();
        drawCircles(vtree.getRoot());
    }

    /**
     * This method sets all nodes to their calculated position
     * contained in <b>coords</b>.
     */
    private void moveNodes(){
        for (VNode node : vtree.getVNodes()) {
            Point2D xy = coords.get(node);
            centerNodeOn(node, xy);
        }
    }
    /**
     * This method calculates recursively the radii for all nodes in vtree
     * starting with the given node. Given node should be the <b>vtree</b> root.
     * @param node <b>vtree</b> root.
     */
    private void calculateRadii(VNode node){
        int numChild = node.getChildCount();
        /*
         * Go down the vtree recursively
         */
        if (numChild != 0){
            for (VNode n : node.getTreeChildren()){
                calculateRadii(n);
            }

            /*
             * Calculate the sum of all child radii and set the maximum radius
             */
            double maxchildradius = 0;
            double sumradii = 0;
            double newrad = 0;
            for (VNode n : node.getTreeChildren()){
                maxchildradius = Math.max(maxchildradius,
                        maxradius.get(n));
                sumradii += maxradius.get(n);
            }

            /*
             * node's radius is sum of all child radii devided by PI.
             * To correct this approximation multiply with scale.
             * circ = 2 * PI * r => r = ( ( circ / 2 ) / PI) * scale
             */
            newrad = Math.max(minrad ,sumradii/Math.PI);
            if (newrad < maxchildradius)
                newrad = maxchildradius;
            radius.put(node, newrad * scale);
            /*
             * node's maximum radius is radius plus maximum child radius
             */
            maxradius.put(node, (newrad + maxchildradius) * scale);
        } else {
            /*
             * If node has no children set his radius and maximum radius
             * to minrad_leave.
             */
            radius.put(node, minrad_leave);
            maxradius.put(node, minrad_leave);

        }
    }

    /**
     * Calculates recursively the coordinates for all nodes.
     * @param node should be the root of vtree
     * @param x can be used as offset for the x coordinate else 0
     * @param y can be used as offset for the y coordinate else 0
     * @param parentphi can be used as offset for starting angle else 0
     */
    private void calculateCoords(VNode node, double x, double y, double parentphi){
        /*
         * If some edges are set invisible turn them on again
         */
        if(node.getParentEdge()!=null) {
            node.getParentEdge().setVisible(true);
        }
        /*
         * Save the given coordinates for this node
         */
        Point2D newcoord = new Point2D.Double(x,y);
        coords.put(node, newcoord);

        int numchild = node.getChildCount();

        if (numchild > 0){
            /*
             * polar coordinates for the child nodes
             */
            double phi = 0;
            double newx = 0;
            double newy = 0;
            double rad = radius.get(node);
            /*
             * If this node has only one child use the given phi
             * from the ancestor
             */
            if (numchild == 1){
                newx = rad * Math.cos(parentphi);
                newy = rad * Math.sin(parentphi);
                phi = parentphi;
                calculateCoords(node.getTreeChildren().get(0), x+newx, y+newy, phi % (2*Math.PI));
            } else if (numchild == 2) {
                /*
                 * For two children place the nodes by hand
                 */
                phi = parentphi - (Math.PI/6);
                newx = rad * Math.cos(phi);
                newy = rad * Math.sin(phi);
                calculateCoords(node.getTreeChildren().get(0), x+newx, y+newy, phi % (2*Math.PI));
                phi = parentphi + (Math.PI/6);
                newx = rad * Math.cos(phi);
                newy = rad * Math.sin(phi);
                calculateCoords(node.getTreeChildren().get(1), x+newx, y+newy, phi % (2*Math.PI));
            } else if (numchild == 3) {
                /*
                 * For three children place the nodes by hand
                 */
                newx = rad * Math.cos(parentphi);
                newy = rad * Math.sin(parentphi);
                calculateCoords(node.getTreeChildren().get(0), x+newx, y+newy, phi % (2*Math.PI));
                double leftphi = parentphi - (2*Math.PI/3);
                newx = rad * Math.cos(leftphi);
                newy = rad * Math.sin(leftphi);
                calculateCoords(node.getTreeChildren().get(1), x+newx, y+newy, leftphi % (2*Math.PI));
                double rightphi = parentphi + (2*Math.PI/3);
                newx = rad * Math.cos(rightphi);
                newy = rad * Math.sin(rightphi);
                calculateCoords(node.getTreeChildren().get(2), x+newx, y+newy, rightphi % (2*Math.PI));
            } else {
                /*
                 * If node has more than three children distribute them proportionally
                 * to their maximum radius.
                 */
                double previous = 0;
                phi = parentphi + Math.PI;
                double freecirc = 0;
                for (VNode n : node.getTreeChildren()){
                    freecirc += Math.atan( maxradius.get(n) / radius.get(node) );
                }
                freecirc = ((Math.PI - freecirc) / numchild);
                for (VNode child : node.getTreeChildren()){
                    double addphi = 0;
                    addphi = Math.atan(maxradius.get(child)
                            / (radius.get(node) )) + freecirc;
                    phi += addphi + previous;
                    newx = rad * Math.cos(phi);
                    newy = rad * Math.sin(phi);
                    previous = addphi;
                    calculateCoords(child, x+newx, y+newy, phi % (2*Math.PI));
                }
            }
        }
    }
    /**
     * Draws recursively the circles under the scaffold rings starting with
     * the given node.
     * @param v should be the root of vtree
     */
    private void drawCircles(VNode v){
        Point2D center = coords.get(v);
        double rad = radius.get(v);
        PPath circle = PPath.createEllipse(0f,0f,(float)rad*2,(float)rad*2);
        PFixedWidthStroke stroke = new PFixedWidthStroke(circleStrokeWidth);
        circle.setStroke(stroke);
        circle.setStrokePaint(circleColor);
        circle.centerFullBoundsOnPoint(center.getX(),center.getY());
        if (v.getTreeChildren().size() > 0){
            circlelayer.addChild(circle);
            for(int i=0; i<v.getChildCount(); i++){
                drawCircles(v.getTreeChildren().get(i));
            }
        }
    }

    //******************************************************************************
    //Get- & Set Methoden
    //******************************************************************************

    @Override
    public PLayer getBackgroundLayer(){
        return circlelayer;
    }
    @Override
    public void updateLayout(){
        doLayout(true);
    }
}
