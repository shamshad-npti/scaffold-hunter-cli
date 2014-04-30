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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import java.awt.Color;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.umd.cs.piccolo.PNode;

/**
 * A node in a VTree
 * 
 * @author Henning Garus
 */
public abstract class VNode extends PNode {

    protected final GlobalConfig globalConfig;
    /**
     * The <b>color</b> which is used to highlight the scaffolds
     * if the filter option is active.
     */
    protected Color color;
    /**
     * <b>parent</b> is the predeccessor of this VNode.
     */
    private VNode parent;
    /**
     * <b>children</b> is a list containing all descendants of this VNode.
     */
    protected List<VNode> children = Lists.newArrayList();
    /**
     * <b>edges</b> is a list of VEdges connected to this VNode
     */
    private List<VEdge> edges = Lists.newArrayList();
    private VEdge parentEdge;
    /**
     * <b>cursor</b> indicates if the cursor is on this node;
     */
    protected boolean cursor;
    
    /**
     * Number of leaves in the subtree under this VNode
     */
    private int subtreeleaves;
    
    private double nodeSize = 1;


    /**
     * @param globalConfig 
     * @param nodeSize 
     * 
     */
    public VNode(GlobalConfig globalConfig, double nodeSize) {
        super();
        this.globalConfig = globalConfig;
        this.nodeSize = nodeSize;
    }

    

    /**
     * Returns the color of the node background or <code>null</code> if the
     * node is not colored.
     * 
     * @return the background color of the node or <code>null</code>
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * Sets the color for the node background. If the color is set to 
     * <code>null</code> the node background will not be colored.
     * 
     * @param color The color which will be applied to the node
     */
    public void setColor(Color color) {
        this.color = color;
        invalidatePaint();
    }

    /**
     * Sets and unsets the keyboard cursor for this node.
     * 
     * @param activate <code>true</code> if this node should have the cursor
     */
    public void setCursor(boolean activate) {
        cursor = activate;
        invalidatePaint();
    }

    /**
     * Sets the parent VNode for this node.
     * @param parent Is of Type VNode.
     */
    public void setTreeParent(ScaffoldNode parent) {
        this.parent = parent;
        //XXX ???
        if (parent != null)
            setPaint(parent.getPaint());
        else
            setPaint(Color.WHITE);
    }

    /**
     * Returns the parent of this VNode
     * 
     * @return the parent of this node or <code>null</code> if this node is
     *  root
     */
    public VNode getTreeParent() {
        return parent;
    }

    /**
     * Returns the next sibling in clockwise order of this node
     * 
     * @return the vnode which is the next clockwise sibling of this node
     */
    public VNode getClockwiseSibling() {
        if (parent == null) return this;
    
        int index = parent.getTreeChildren().indexOf(this);
        index = (index +1 ) % parent.getTreeChildren().size();
    
        return parent.getTreeChildren().get(index);
    }

    /**
     * Returns the next sibling in counterclockwise order of this node
     * 
     * @return the vnode which is the next counterclockwise sibling of this
     *  node
     */
    public VNode getAnticlockwiseSibling() {
        if (parent == null) return this;
    
        int index = parent.getTreeChildren().indexOf(this) - 1;
        if (index < 0) index = parent.getTreeChildren().size() - 1;
    
        return parent.getTreeChildren().get(index);
    }

    /**
     * Returns all children of this node. The returned list cannot be modified.
     * 
     * @return the list of children of this node
     */
    public List<VNode> getTreeChildren() {
        return Collections.unmodifiableList(this.children);
    }

    /**
     * Returns the edges adjacent to this node.
     * 
     * @return the adjacent edges of this node
     */
    public List<VEdge> getEdges() {
        return Collections.unmodifiableList(this.edges);
    }

    /**
     * This method adds an edge to this VNode.
     * 
     * @param edge the vedge to be added to this node
     */
    public void addEdge(VEdge edge) {
        this.edges.add(edge);
    }

    /**
     * @return the parentEdge
     */
    protected VEdge getParentEdge() {
        return parentEdge;
    }

    /**
     * @param parentEdge the parentEdge to set
     */
    protected void setParentEdge(VEdge parentEdge) {
        this.parentEdge = parentEdge;
    }

    /**
     * Adds a child node to this <code>VNode</code>.
     * 
     * @param child the child to be added to this node
     */
    public void addTreeChild(ScaffoldNode child) {
        this.children.add(child);
    }

    /**
     * Removes a child node from this <code>VNode</code>. Returns
     * <code>true</code> if the child was removed. If <code>child</code> was
     * not a child of this node <code>false</code> will be returned.
     * 
     * @param child the vnode to be removed from the children of this node
     * 
     * @return true if <code>child</code> was removed from this node.
     *  <code>false</code> if it was not a child of this node.
     */
    public boolean removeTreeChild(VNode child) {
        return this.children.remove(child);
    }

    /**
     * Returns the number of children
     * 
     * @return the number of children of this node
     */
    public int getChildCount() {
        return children.size();
    }

    /**
     * Returns the number of leaves in the subtree whose root is this node.
     * <p>
     * The returned value is only valid after
     * <code>VTree.countSubTreeLeaves</code> has been called and only as long
     * as no further nodes have been added to or removed from the
     * <code>VTree</code>.
     * 
     * @return the number of leaves in the subtree under this node
     */
    public int getNumLeaves() {
        return this.subtreeleaves;
    }

    /**
     * TODO this is ugly
     * @param num
     */
    public void setNumLeaves(int num) {
        this.subtreeleaves = num;
    }

    /**
     * Removes an edge from this <code>VNode</code>. Returns
     * <code>true</code> if the edge was removed. If <code>edge</code> was
     * not a child of this node <code>false</code> will be returned.
     * 
     * @param edge the vedge to be removed from the edges of this node
     * 
     * @return true if <code>edge</code> was removed from this node.
     *  <code>false</code> if it was not an edge of this node.
     */
    public boolean removeEdge(VEdge edge) {
        return this.edges.remove(edge);
    }

    /**
     * @return <b>true</b> if children of the <code>Scaffold</code> associated
     * with this node have not yet been added to its children,
     * <code>false</code> otherwise.
     */
    public abstract boolean isExpandable();
    
    /**
     * @return <code>true</code> if this node or any of its descendants are
     * expandable, <code>false</code> otherwise.
     * 
     * @see #isExpandable
     */
    public boolean isSubtreeExpandable() {
        if (isExpandable())
            return true;
        for (VNode c : getTreeChildren()) {
            if (c.isSubtreeExpandable())
                return true;
        }
        return false;
    }

    /**
     * @return <code>true</code> if this node has any children
     */
    public boolean isReducible() {
        return !children.isEmpty();
    }
    
    /**
     * sort the subtree rooted at this node according to some comparator
     * 
     * @param comparator the comparator used for sorting
     */
    void sortSubtree(Comparator<VNode> comparator) {
        Collections.sort(children, comparator);
        for (VNode c : children)
            c.sortSubtree(comparator);
    }
    
    /**
     * Scales this VNode around the <code>factor</code> and
     * the size of the corresponding SVG.
     * @param factor 1.0 = 100%
     */
    public void scaleNode(double factor) {
        this.setScale(this.getScale()*factor);
        nodeSize *= factor;
    }
    
    /**
     * Returns the actual size of the corresponding SVG.
     * 
     * @return svgSize
     */
    public double getNodeSize() {
        return nodeSize;
    }

    /**
     * Sets this VNode and the size of the corresponding SVG
     * to the original size.
     */
    public void normalizeNode() {
        this.setScale(1.0);
    }

}