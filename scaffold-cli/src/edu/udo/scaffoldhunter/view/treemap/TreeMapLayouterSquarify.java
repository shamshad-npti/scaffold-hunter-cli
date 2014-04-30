/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

package edu.udo.scaffoldhunter.view.treemap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.util.Position;
import edu.udo.scaffoldhunter.view.treemap.nodes.TreeMapNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * A tree map layouter, which implements the "squarify" algorithm of van Wijk.
 * 
 * @author Lappie
 * @author schrins
 * 
 */
public class TreeMapLayouterSquarify implements TreeMapLayouter {

    private static Logger logger = LoggerFactory.getLogger(TreeMapView.class);

    /**
     * This variable is updated with the latest free position of the main node.
     * 
     * Defaults to the bottom left corner and is updates from that position onwards
     */
    private Position currentLayouterPosition;
    
    private TreeMapNode currentNode;
    
    private PBounds currentBounds;

    /**
     * Calculates basic ratio's required for the positions and the canvas
     */
    public TreeMapLayouterSquarify() {
    }
    
    @Override
    public void layoutNodes(TreeMapNode node) {
        // all unplotted nodes have to be inside this node
        for (TreeMapNode child : node.getUnplottedTreeMapChildren()) {
            child.setVisible(false);
        }
        
        if (node.getPlottedTreeMapChildren().size() == 0)
            return;
        
        if(node.getDrawSize() <= 0) {
            logger.error("Squarify: Trying to draw a node with size <= 0");
            return;
        }
        
        if(node.getWidth() < 0 || node.getHeight() < 0) {
            logger.error("Squarify: TreeMapNode is too small to be squarified");
            return;
        }        
        
        squarify(node);

        // recursion on child nodes
        for (TreeMapNode child : node.getPlottedTreeMapChildren()) {
            layoutNodes(child);
            child.setVisible(true);
        }
    }
    
    // ****************************************************************
    // MAIN ALGORITHM
    // ****************************************************************
    
    /**
     * Positions all the given items in a way that tries to keep them as
     * 'squareful' as possible. Children will be nested. 
     * 
     * Uses Van Wijk's squarify algorithm: win.tue.nl/~vanwijk/stm.pdf
     */
    private void squarify(TreeMapNode node) {
        currentNode = node; // the currently layouted node
        currentBounds = node.getChildrenBounds(); // the bounds, which are used for layout
        currentLayouterPosition = new Position(currentBounds.getX(), currentBounds.getY() + currentBounds.getHeight()); // the current layouter position
        
        //calculate the ratio so that the pixelWidth and pixelHeight don't have to match 
        // the width and height corresponding to the size. 
        prepareChildrenLayout(node);
        double nodeExtendedSurfaceRatio = (currentBounds.getWidth() * currentBounds.getHeight()) / getExtendedChildrenDrawSize(node);        
        double nodeSurfaceRatio = (currentBounds.getWidth() * currentBounds.getHeight()) / node.getDrawSize();
        
        List<Double> sizesToPlace = new ArrayList<Double>();
        List<TreeMapNode> itemsToPlace = new ArrayList<TreeMapNode>();
        sortTreeMapNodes(node);
        
        for (TreeMapNode item : node.getPlottedTreeMapChildren()) {
            double score1 = worst(sizesToPlace, width(itemsToPlace));
            sizesToPlace.add(item.getDrawSize()*nodeSurfaceRatio);
            itemsToPlace.add(item);
            double score2 = worst(sizesToPlace, width(itemsToPlace));
            if (score1 <= score2) { // Opposite of what the algorithm of
                                    // van Wijk tells us (?)
                itemsToPlace.remove(item); //the last one was not good enough
                layoutRow(itemsToPlace, nodeExtendedSurfaceRatio);

                itemsToPlace.clear();
                itemsToPlace.add(item);
                sizesToPlace.clear();
                sizesToPlace.add(item.getDrawSize()*nodeSurfaceRatio);
            }
        }
        layoutRow(itemsToPlace, nodeExtendedSurfaceRatio);
    }

    /**
     * Tells us the minimum 'width'(/height) that is still available.
     * 
     * @return minimum of horizontal width vs vertical height
     */
    private double width(List<TreeMapNode> itemsToPlace) {
        
        double subtractClearArea = 0.0; 
        if(itemsToPlace.size() > 0) {
            double b = getEstimatedInnerBoundWidth(currentNode);
            subtractClearArea = itemsToPlace.size() * 2.0*b;
        }
        
        double availableWidth = currentBounds.getX() + currentBounds.getWidth() - currentLayouterPosition.getX() - subtractClearArea;
        double availableHeight = currentLayouterPosition.getY() - currentBounds.getY() - subtractClearArea;
        
        if(Double.isNaN(availableHeight) || Double.isNaN(availableWidth)) {
            logger.error("TreeMapPositioner: NaN detected");
        }
        
        if(availableWidth <= 0 && availableHeight <= 0) {
            double sumSize = 0.0;
            for(TreeMapNode child : currentNode.getPlottedTreeMapChildren()) {
                sumSize += child.getDrawSize();
            }
            if(sumSize < currentNode.getDrawSize())
                logger.error("TreeMapPositioner. Sum of child do not match parent. This is not allowed.");
            logger.error("TreeMapPositioner: Both height and width are less than zero. This is impossible!");
            availableWidth = currentBounds.getX() + currentBounds.getWidth() - currentLayouterPosition.getX();
            availableHeight = currentLayouterPosition.getY() - currentBounds.getY();
        }
        if(availableWidth <= 0) { //this is possible because of our subtracted border. 
            return availableHeight;
        }
        if(availableHeight <= 0) { //this is possible because of our subtracted border.
            return availableWidth;
        }
        
        return Math.min(availableWidth, availableHeight);
    }

    /**
     * Gives a score to how good these items can be place together in the given
     * width.
     * 
     * @param items
     *            the sizes of the items to place. NOT extended Size
     * @param freeWidth
     *            the width still available
     * @return the score
     */
    private double worst(List<Double> items, double freeWidth) {
        Preconditions.checkArgument(freeWidth > 0, "available width should be more than 0");
        if (items.size() == 0)
            return 0.0;

        double sumSize = 0.0;
        for (double item : items)
            sumSize += item;

        double min = Collections.min(items);
        double max = Collections.max(items);
        double wRatio = (freeWidth * freeWidth * max) / (sumSize * sumSize);
        double hRatio = (sumSize * sumSize) / (freeWidth * freeWidth * min);
        return Math.max(wRatio, hRatio);
    }

    /**
     * Lay the given nodes out in the main node. Dependent on where the most
     * space is, the nodes will be placed in either a column or a row.
     * 
     * @param itemsToPlace
     */
    private void layoutRow(List<TreeMapNode> itemsToPlace, double nodeSurfaceRatio) {
        if(itemsToPlace.isEmpty())
            return;
        double totalSize = 0;
        for (TreeMapNode item : itemsToPlace) {
            totalSize += getExtendedDrawSize(item);
        }

        // to keep track where we are in the row/column:
        Position currentFreePos = (Position) currentLayouterPosition.clone();

        double freeWidth = currentBounds.getX() + currentBounds.getWidth() - currentFreePos.getX();
        double freeHeight = currentFreePos.getY() - currentBounds.getY();

        if (freeHeight > freeWidth) { // we'll make a row
            double height = (totalSize*nodeSurfaceRatio) / freeWidth;
            double widthStep = freeWidth / totalSize;
            
            for (TreeMapNode item : itemsToPlace) {
                double width = widthStep * getExtendedDrawSize(item);
                item.setBounds(currentFreePos.getX(), currentFreePos.getY() - height, width, height);
                currentFreePos.addX(width);
            }
            //line up horizontally
            currentLayouterPosition.subtractY(height);
        } else { // we'll make a column
            double width = (totalSize*nodeSurfaceRatio) / freeHeight;
            double heightStep = freeHeight / totalSize;
            
            for (TreeMapNode item : itemsToPlace) {
                double height = heightStep * getExtendedDrawSize(item);
                item.setBounds(currentFreePos.getX(), currentFreePos.getY() - height, width, height);
                currentFreePos.subtractY(height);
            }
            //line up vertically
            currentLayouterPosition.addX(width);
        }
    }
    
    /**
     * Sorts the list of plotted children nodes descending by their size
     */
    private void sortTreeMapNodes(TreeMapNode node) {
        Comparator<TreeMapNode> c = new Comparator<TreeMapNode>() {
            @Override
            public int compare(TreeMapNode node1, TreeMapNode node2) {
                return Double.compare(node2.getDrawSize(), node1.getDrawSize());
            }
        };
        Collections.sort(node.getPlottedTreeMapChildren(), c);
    }

    // ****************************************************************
    // HELPER FUNCTIONS FOR NODES
    // ****************************************************************
    
    //*****************************************************************************************
    //  The following image shows the borders and elements, of which a TreeMapPNode consists:
    //
    //    +-----------------------------------------------------------+
    //    '                   ^                                       '
    //    ' outerBorderHeight |                                       '
    //    '        ___________V_____________________________          '
    //    '<----->|                                         |         '  ^
    //    '       |              titleBounds                |         '  |
    //    '       |_________________________________________|         '  |
    //    '       |                                         |         '  |
    //    '       |                                         |         '  |
    //    '       |                                         |         '  | innerBounds
    //    '       |             childrenBounds              |         '  |  (height)
    //    '       |                                         |         '  |
    //    '       |                                         |         '  |
    //    '       |                                         |         '  |
    //    '       |       innerBorderSpace = sum of outer   |         '  |
    //    '       |       space of all children             |         '  |
    //    '       |_________________________________________|         '  V
    //    '                                                           '
    //    '                  outerBorderSpace                         '
    //    +-----------------------------------------------------------+
    //    
    //            <----------------------------------------->
    //                       innerBounds (width)
    //    <------------------------------------------------------------>
    //                        (exclusive)Bounds
    //
    //  The exclusive bounds represent the actual size of a node. However, only the inner bounds will be
    //  drawn to leave some space between sibling nodes. Currently the relation between outerBorderSpace
    //  and area of the innerBounds is 1:5 and not dependent on the nodes absolute size or number of chil-
    //  dren. InnerBorderSpace equals outerBorderSpace.
    //
    //  The drawSize of the node does not match the area of the innerBounds in practice. The layout is cal-
    //  culated top-bottom, so a node's layout properties have to be fixed before anything is known about
    //  its children nodes. The extendedDrawSize equals the area of the exclusiveBounds.
    //  
    //  The method getEstimatedChildBorderHeight() returns an estimation of how broad the childrens' bor-
    //  ders will be. The problem why we cannot calculate it properly is as follows:
    //  We know that innerBorderSpace is 20% of the innerBounds and we know that the sum of all childrens'
    //  outerBoundSpaces equals the innerBorderSpace of this node. But we do not know the childrens' peri-
    //  meter (only their area), because we have not layouted them yet. Therefore we assume that every child
    //  will be a perfect square and take the estimate perimeter to calculate the childrens' borderHeight.
    
    /**
     * This method should be called on already layouted nodes. It determines the actual border width 
     * from the already calculated full and inner bounds. It estimates the border width of the node's
     * children. This border width might be overwritten once this method is called for the children
     * nodes.
     */
    private void prepareChildrenLayout(TreeMapNode node) {
        node.setOuterBorderWidth((node.getBounds().getWidth() - node.getInnerBounds().getWidth()) * 0.5);        
             
        node.setDrawSizeRatio(node.getInnerBounds().getHeight() * node.getInnerBounds().getWidth() / node.getDrawSize());
        if(node.getDrawSizeRatio() <= 0) {
            logger.error("DrawSizeRatio of {} is <= 0", node.getTitle());
        }
        for(TreeMapNode child : node.getPlottedTreeMapChildren()) {
            child.setDrawSizeRatio(node.getDrawSizeRatio());
        }    
        
        double innerBorderWidth = getEstimatedChildBorderHeight(node);
        for(TreeMapNode child : node.getPlottedTreeMapChildren()) {
            child.setOuterBorderWidth(innerBorderWidth);
        }
    }
    
    private double getEstimatedInnerBoundWidth(TreeMapNode node) {
        if(node.getPlottedTreeMapChildren().isEmpty())
            return 0.0;
        else
            return node.getPlottedTreeMapChildren().get(0).getOuterBorderWidth();
    }
    
    private double getClearAreaLogicalSize(TreeMapNode node) {
        double x = 0.08 + 0.015 * (Math.log(node.getPlottedTreeMapChildren().size())/Math.log(2));
        return x * node.getDrawSize();
    }
    
    private double getEstimatedChildBorderHeight(TreeMapNode node) {
        double sumChildrenPerimeter = 0.0;
        for(TreeMapNode child : node.getPlottedTreeMapChildren()) {
            sumChildrenPerimeter += getEstimatedPerimeter(child);
        }
        
        double C = getClearAreaLogicalSize(node) * node.getDrawSizeRatio();
        double N = node.getPlottedTreeMapChildren().size();
        double X = sumChildrenPerimeter;
        
        // C = X*b + N*4b^2 solve for b
        // b^2 + X/4N - C/4N = 0
        // b = - X/8N + sqrt((X/8N)^2 + C/4N)
        
        double b = Math.sqrt((C/(4*N))+((X*X)/(64*N*N)))-(X/(8*N));
        
        if(Double.isNaN(b) || b < 0.0) {
            logger.warn("Estimated child border height of {} was {}", node.getTitle(),b);
            return .000001 * (node.getWidth()+node.getHeight()); // we have to return something
        }        
        return b;
    }
    
    private double getEstimatedPerimeter(TreeMapNode node) {
        // this is not the actual perimeter, but only an estimation
        // it can be used, when this node has not been layouted yet
        double result = 4*Math.sqrt(node.getDrawSizeRatio() * node.getDrawSize());
        if(Double.isNaN(result)) {
            logger.warn("Estimated perimeter of {} was NaN", node.getTitle());
            return 0.0;
        }
        return result;
    }
    
    /**
     * Return the size + the size that is added to keep enough space around this node for a clear border
     * @return size + added size for the clear border
     */
    private double getExtendedDrawSize(TreeMapNode node) {
        if(node.getTreeMapParent() == null)
            return node.getDrawSize()+getClearAreaLogicalSize(node);
        double outerBorderSpace = node.getOuterBorderWidth() * (getEstimatedPerimeter(node) + node.getOuterBorderWidth()); // this is the estimated border space in pixel
        double normalizedBorderSpace = outerBorderSpace / node.getDrawSizeRatio(); // normalize space to the logical drawSize of the node
        return normalizedBorderSpace + node.getDrawSize();
    }
    
    /**
     * Return the extended size of all children.  
     * @return the sum of extended size of all children
     */
    private double getExtendedChildrenDrawSize(TreeMapNode node) { //TODO, shouldn't this just be size + C ??
        double sumSize = 0.0;
        for(TreeMapNode child : node.getPlottedTreeMapChildren()) // only add drawn children here (see TreeMapNode)
            sumSize += getExtendedDrawSize(child);
        return sumSize;
    }
}
