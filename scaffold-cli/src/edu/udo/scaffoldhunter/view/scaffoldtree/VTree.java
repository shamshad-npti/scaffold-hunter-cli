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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;

/**
 * This class represents a visual instance of the scaffold tree. A
 * <code>VTree</code> consists of a set of <code>VNode</code>s connected by
 * <code>VEdge</code>s. Both can be integrated in the piccolo scene graph and
 * inherit important properties for visualization.
 * <p>
 * A <code>VTree</code> usually is a subtree of a <code>Tree</code> and this
 * class provides methods for constructing a <code>VTree</code> by traversing
 * the underlying <code>Tree</code>. This mechanism is also used when expanding
 * the visible tree step by step.
 * <p>
 * Every <code>VNode</code> created will also be installed in the piccolo
 * scenegraph and the connectivity and consistency of <code>VNode</code>s and
 * <code>VEdge</code>s is assured.
 * <p>
 * Whenever nodes are added to the <code>VTree</code> a flag is set to trigger a
 * layout update within the current ui cycle.
 * 
 */
public class VTree {

    /**
     * This is the layout of the VTree.
     */
    private VLayout layout;

    private List<VNodeListener> vnodeListeners = Lists.newArrayList();

    /**
     * If this flag is <code>true</code> the layout will be recomputed in this
     * UI cycle.
     */
    private boolean layoutInvalid = false;
    
    private boolean showScaffoldDetailsNodes = false;
    private Comparator<? super Molecule> moleculeComparator = null;

    /**
     * This is the root of the VTree.
     */
    private ScaffoldNode vroot;

    private Map<Scaffold, ScaffoldNode> vnodes;
    
    /**
     * The canvas displaying this <code>VTree</code>
     */
    private final VCanvas vcanvas;

    private final SVGCache svgCache;
    private final Selection selection;
    private final BannerPool bannerPool;
    private final GlobalConfig globalConfig;

    /**
     * Creates an empty <b>VTree</b>.
     * 
     * @param acanvas
     *            the canvas displaying the VTree
     *            the initial layout of the tree
     * @param svgCache
     *            the svg cache which will is used for the scaffold svgs
     * @param selection
     * @param bannerPool
     * @param globalConfig
     */
    public VTree(VCanvas acanvas, SVGCache svgCache, Selection selection, BannerPool bannerPool,
            GlobalConfig globalConfig) {
        this.vcanvas = acanvas;
        this.vnodes = new HashMap<Scaffold, ScaffoldNode>();
        this.svgCache = svgCache;
        this.selection = selection;
        this.bannerPool = bannerPool;
        this.globalConfig = globalConfig;
    }

    // ****************************************************************
    // VTree construction - This methods are used to construct a VTree
    // by traversing the DTree. VNodes are installed to the Piccolo
    // scenegraph here.
    // ****************************************************************

    /**
     * Expands the given node by the entire subtree
     * 
     * @param node
     *            the node to be expanded
     * @return <code>true</code> iff the node was expandable
     */
    public boolean expandSubtree(VNode node) {
        if (node.isSubtreeExpandable() && node instanceof ScaffoldNode) {
            buildBranch((ScaffoldNode)node, -1);
            return true;
        } else
            return false;
    }

    /**
     * Expands the given node by its children
     * 
     * @param node
     *            the node to be expanded
     * @return <code>true</code> iff the node was expandable
     */
    public boolean expand(VNode node) {
        if (node.isExpandable() && node instanceof ScaffoldNode) {
            buildBranch((ScaffoldNode)node, 1); 
            return true;
        } else
            return false;
    }

    /**
     * Cut off the subtree starting at the given node
     * 
     * @param node
     *            the node to be expanded
     * @return <code>true</code> if the node was actually reduced
     */
    public boolean reduce(VNode node) {
        if (node.isReducible()) {
            destroyBranch(node);
            return true;
        } else
            return false;
    }

    /**
     * This method builds a whole branch starting with a VNode. First it gets
     * the children list from the according <code>Tree</code>, and starts
     * recursively the buildBranch method. This happens until the given
     * <code>level</code> number is reached. The nodes are sorted afterwards using
     * the current sorting on the VTree.
     * <p>
     * If successors of <code>node</code> already exit they will be ignored.
     * 
     * @param node
     *            the root of the branch to build
     * @param level
     *            the depth of the branch; if <code>level</code> equals 1 only
     *            the children of the given node will be added (if they exist in
     *            the <code>DTree</code>). if <code>level</code> equals -1 the
     *            entire subtree will be constructed.
     */
    public void buildBranch(ScaffoldNode node, int level) {
       
        buildBranchNoSort(node, level);
        // For every node expansion the node and its subnodes have to be sorted according to the current tree sorting
        vcanvas.getSorting().sortSubtree(node);
    }
    
    private void buildBranchNoSort(ScaffoldNode node, int level) {
        if (level == 0)
            return;

        if (node.isExpandable()) {
            for (Scaffold s : node.getScaffold().getChildren()) {
                if (!containsScaffold(s))
                    addScaffoldNode(s, node, node.getFullBoundsReference().getCenter2D());
            }
        }        
                
        for (ScaffoldNode childNode : Iterables.filter(node.getTreeChildren(), ScaffoldNode.class))
            buildBranchNoSort(childNode, level - 1);
        
    }

    /**
     * Rebuild the Scaffold tree from <code>node</code> according to
     * <code>state</code>. For all scaffolds which are marked as displayed by
     * state a vnode is created and displayed.
     * 
     * @param node the root of the subtree to rebuild
     * @param state the state which rebuilding will recreate
     */
    public void buildBranch(ScaffoldNode node, ScaffoldTreeViewState state) {
        if (node.isExpandable()) {
            for (Scaffold s : node.getScaffold().getChildren()) {
                if (state.isOpenScaffold(s)) {
                    addScaffoldNode(s, node, node.getFullBoundsReference().getCenter2D());
                }
            }
        }
        for (ScaffoldNode child : Iterables.filter(node.getTreeChildren(), ScaffoldNode.class)) {
            buildBranch(child, state);
        }
    }

    /**
     * This method recursively deletes all successors of the given node.
     * 
     * @param node
     *            the successors of this nodes will be removed
     */
    public void destroyBranch(VNode node) {
        while (node.isReducible()) {
            VNode c = node.getTreeChildren().get(node.getTreeChildren().size() - 1);
            destroyBranch(c);
            removeVNode(c);
        }
    }

    /**
     * If the root of the <code>VTree</code> is not the root of the
     * <code>Tree</code> (the <code>VTree</code> is a subtree), this method
     * creates a <code>VNode</code> for the parent of the <code>Scaffold</code>
     * corresponding to the current root of the <code>VTree</code>. If the
     * imaginary root is added, all children of the newly created node will also
     * be added (except the existing one). In the normal case only the new root
     * is added. The root of the VTree will be changed.
     */
    public void expandRootByParent() {
        Scaffold newRoot = vroot.getScaffold().getParent();
        ScaffoldNode newVRoot = addScaffoldNode(newRoot, null, new Point2D.Double(0, 0));
        addVEdge(newVRoot, vroot);
        setRoot(newVRoot);
        // if (newVRoot.isImaginaryRoot())
        // buildBranch(newVRoot, 1);
    }

    /**
     * Creates and adds a <code>VNode</code> according to the given
     * <code>Scaffold</code>. Other <code>VNode</code>s will be constructed if
     * required to assure connectivity.
     * 
     * @param scaffold
     *            the scaffold that should be integrated in the
     *            <code>VTree</code>
     * @param expandAll 
     *            if true all children of predecessors of the given 
     *            node will be expanded
     */
    public void installVNode(Scaffold scaffold, boolean expandAll) {
        if (scaffold == null)
            return;
        if (vnodes.containsKey(scaffold))
            return;
        // find connection to current vtree
        Stack<Scaffold> path = new Stack<Scaffold>();
        Scaffold currentScaffold = scaffold;
        path.add(currentScaffold);
        while (!vnodes.containsKey(currentScaffold) && (currentScaffold.getParent() != null)) {
            currentScaffold = currentScaffold.getParent();
            path.add(currentScaffold);
        }
        // if the path from the new node up the tree does not lead
        // to a node already found in the graph, change the root
        if (!vnodes.containsKey(currentScaffold)) {
            // expand current root
            while (!path.contains(vroot.getScaffold()))
                expandRootByParent();
            // now installVNode() should find the connection to the
            // graph
            installVNode(scaffold, expandAll);
            return;
        }
        // create nodes on path
        ScaffoldNode currentVNode = vnodes.get(path.pop());
        while (!path.empty()) {
            if (expandAll) {
                expand(currentVNode);
                currentVNode = vnodes.get(path.pop());
            } else {
                currentVNode = addScaffoldNode(path.pop(), currentVNode, currentVNode.getFullBoundsReference().getCenter2D());
            }
        }
    }

    /**
     * This method adds a VNode to the VTree and installs the VNode to the
     * piccolo scenegraph.
     * <p>
     * If the node represents the imaginary root it will not be displayed
     * 
     * @param scaffold
     *            the scaffold for which a vnode should be created
     * @param parent
     *            Unless parent is <code>null</code> a <code>VEdge</code> will
     *            be created to connect the new node with parent.
     * @param showUpPosition
     *            the coordinates where the new node will appear
     * @return the new vnode
     */
    private ScaffoldNode addScaffoldNode(Scaffold scaf, ScaffoldNode parent, Point2D showUpPosition) {
        ScaffoldNode node = new ScaffoldNode(scaf, svgCache, selection, bannerPool, globalConfig);
        vnodes.put(node.getScaffold(), node);
        node.setMoleculeComparator(moleculeComparator);
        node.setShowDetailsNode(showScaffoldDetailsNodes);
        node.centerFullBoundsOnPoint(showUpPosition.getX(), showUpPosition.getY());
        if (!node.getScaffold().isImaginaryRoot()) {
            // add icon
            if (node.getIcon() != null)
                getVCanvas().getVActivity().addIcon(node.getIcon());
            getVCanvas().getVActivity().addUnscaledNode(node.getLabel());
            // add node to the layer
            getNodeLayer().addChild(node);
        }
        // trigger layout update
        layoutInvalid = true;
        if (parent != null) {
            addVEdge(parent, node);
        }
        notifyVNodeListeners(node, false);
        return node;
    }

    /**
     * This method creates a VEdge between two nodes, adds it to the VTree and
     * installs the edge in the piccolo scenegraph. Parent/child references are
     * updated to assure consistency.<br>
     * The edge will not be displayed if <code>parent.isImaginaryRoot()</code>
     * returns <code>true</code.>
     * 
     * @param parent
     *            a node
     * @param child
     *            a child of node
     */
    private void addVEdge(ScaffoldNode parent, ScaffoldNode child) {
        // update references
        child.setTreeParent(parent);
        parent.addTreeChild(child);
        if (!parent.getScaffold().isImaginaryRoot()) {
            // create and install new edge
            VEdge edge = new VEdge(parent, child);
            child.setParentEdge(edge);
            getEdgeLayer().addChild(edge);
        }
    }

    /**
     * Removes a VNode from the VTree, release all references and removes all
     * edges.
     * 
     * @param node
     *            that should be removed
     */
    private void removeVNode(VNode node) {
        // remove edges
        VEdge currentEdge;
        while (node.getEdges().size() != 0) {
            currentEdge = node.getEdges().get(0);
            currentEdge.dispose();
//            if (getEdgeLayer().getAllNodes().contains(currentEdge)) //XXX: This check is not necessary and consumes more time then the deletion itself
                getEdgeLayer().removeChild(currentEdge); //TODO: Inefficient implementation of PNode, takes O(n) time!
        }
        // remove node
//        if (getNodeLayer().getAllNodes().contains(node)) //XXX: This check is not necessary and consumes more time then the deletion itself
            getNodeLayer().removeChild(node); //TODO: Inefficient implementation of PNode, takes O(n) time!
        // clear references
        if (node.getTreeParent() != null)
            node.getTreeParent().removeTreeChild(node);
        for (VNode n : node.getTreeChildren()) {
            n.setTreeParent(null);
        }
        // trigger layout update
        layoutInvalid = true;
        if (node instanceof ScaffoldNode) {
            ScaffoldNode s = (ScaffoldNode)node;
            vnodes.remove(s.getScaffold());
            // remove icon
            if (s.getIcon() != null)
                getVCanvas().getVActivity().removeIcon(s.getIcon());
            getVCanvas().getVActivity().removeUnscaledNode(s.getLabel());
            notifyVNodeListeners(s, true);
        }
    }

    /**
     * Creates a root (<b>VNode</b>) that is based on the given <b>Scaffold</b>.
     * If the tree was not empty before no changes will be done and
     * <code>null</code> is returned.
     * 
     * @param droot
     * @return the created root
     */
    protected ScaffoldNode createVRoot(Scaffold droot) {
        if (vroot != null)
            return null;
        vroot = addScaffoldNode(droot, null, new Point2D.Double(0, 0));
        return vroot;
    }

    // ****************************************************************
    // calculate properties of the VTree
    // ****************************************************************

    /**
     * Returns the depth of the given node in this tree
     * 
     * @param v
     *            the node whose depth will be returned
     * @return the depth of <code>v</code>
     */
    public int getNodesDepth(VNode v) {
        VNode temp = v;
        int depth = 0;
        while (temp != this.vroot) {
            depth++;
            temp = temp.getTreeParent();
        }
        return depth;
    }

    /**
     * Returns a list of all the scaffolds on the given hierarchy level of the
     * subtree starting at the specified root.
     * 
     * @param root
     *            the root of the subtree considered
     * @param level
     *            the hierarchy level relative to the given root. If level is 0,
     *            the result will be the root of the tree.
     * @return a list containing all VNodes on the given level
     */
    public List<VNode> getNodesOnLevel(VNode root, int level) {
        LinkedList<VNode> result = Lists.newLinkedList();
        result.add(root);
        for (int i = 0; i < level; i++) {
            if (result.isEmpty()) return result;
            VNode last = result.getLast();
            VNode n;
            do {
                n = result.pop();
                result.addAll(n.getTreeChildren());
            } while (n != last);
        }
        return result;
    }

    /**
     * Returns a list of all the scaffolds on the given hierarchy level starting
     * from the root.
     * 
     * @param level
     *            the hierarchy level relative to the root of this
     *            <code>VTree</code>
     * @return a list of the nodes on the specified level
     * 
     * @see VTree#getNodesOnLevel(VNode, int)
     */
    public List<VNode> getNodesOnLevel(int level) {
        return getNodesOnLevel(getRoot(), level);
    }

    /**
     * Returns the number of leaves of the subtree starting from the given root.
     * 
     * @param v
     *            should be the root of the subtree
     * @return number of leaves of the subtree
     */
    public int countSubTreeLeaves(VNode v) {

        int count = 0;

        for (VNode node : v.getTreeChildren())
            count += countSubTreeLeaves(node);

        if (count == 0)
            count = 1;

        v.setNumLeaves(count);
        return count;
    }

    /**
     * Returns the maximum depth of the tree starting with the given node v.
     * 
     * @param v
     *            root of the tree
     * @param d
     *            can be used as offset else 0
     * @return maximum depth of the tree
     */
    public int getMaxDepth(VNode v, int d) {
        int depth = d;
        for (VNode node : v.getTreeChildren())
            depth = Math.max(depth, this.getMaxDepth(node, d + 1));
        return depth;
    }

    // ****************************************************************
    // SET and GET methods
    // ****************************************************************

    private PLayer getNodeLayer() {
        return vcanvas.getNodeLayer();
    }

    private PLayer getEdgeLayer() {
        return vcanvas.getEdgeLayer();
    }

    boolean getLayoutInvalid() {
        return layoutInvalid;
    }

    void setLayoutInvalid(boolean layoutInvalid) {
        this.layoutInvalid = layoutInvalid;
    }

    /**
     * 
     * @param layout
     *            the layout to be applied to this <code>VTree</code>
     */
    public void setLayout(VLayout layout) {
        // remove the old circle layer
        if ((this.layout != null) && (this.layout.getBackgroundLayer() != null))
            getCamera().removeLayer(this.layout.getBackgroundLayer());
        // change the layout
        this.layout = layout;
        // install the new circle layer
        if (layout.getBackgroundLayer() != null)
            getCamera().addLayer(0, layout.getBackgroundLayer());
        // reset root scale (the radial layout resizes the root)
        if (vroot != null)
            vroot.setScale(1);
    }

    /**
     * @return <code>true</code> if this VTree is a subtree of the underlying
     *         <code>Tree</code>. The root of subtrees is displayed.
     */
    public boolean isSubtree() {
        return (!vroot.getScaffold().isImaginaryRoot());
    }

    /**
     * The camera is used as a special layer for icons that are not effected by
     * zooming.
     * 
     * @return the camera associated to the canvas
     */
    public PCamera getCamera() {
        return vcanvas.getCamera();
    }

    /**
     * @return the root of this <code>VTree</code>
     */
    public ScaffoldNode getRoot() {
        return vroot;
    }

    /**
     * Sets a new root for this VTree. The node must be part of the
     * <code>VTree</code>. This method also resets the scale of the old root.
     * 
     * @param v
     */
    void setRoot(ScaffoldNode v) {
        vroot.setScale(1);
        vroot = v;
    }

    /**
     * @param v
     *            the node whose right neighbor is returned
     * @return the next node on the same layer as <code>v</code>.
     */
    public VNode getRightLayerNode(VNode v) {
        int level = getNodesDepth(v);
        List<VNode> list = getNodesOnLevel(getRoot(), level);

        int nodeIndex = list.indexOf(v);
        int nextNodeIndex = (nodeIndex + 1) % list.size();

        return list.get(nextNodeIndex);
    }

    /**
     * @param v
     *            the node whose left neighbor is returned
     * @return the previous node on the same layer as <code>v</code>.
     */
    public VNode getLeftLayerNode(VNode v) {
        int level = getNodesDepth(v);
        List<VNode> list = getNodesOnLevel(getRoot(), level);

        int nodeIndex = list.indexOf(v);
        int nextNodeIndex = nodeIndex - 1;

        if (nextNodeIndex < 0)
            nextNodeIndex = list.size() - 1;

        return list.get(nextNodeIndex);
    }

    /**
     * @return the layout of the VTree
     */
    public VLayout getLayout() {
        return this.layout;
    }

    /**
     * Returns the associated canvas.
     * 
     * @return the canvas displaying this <code>VTree</code>
     */
    public VCanvas getVCanvas() {
        return this.vcanvas;
    }

    /**
     * @param scaffold
     *            the scaffold whose vnode is returned
     * @return the vnode which represents <code>scaffold</code> or
     *         <code>null</code> if there is no such node in this vtree.
     */
    public ScaffoldNode getVNode(Scaffold scaffold) {
        return vnodes.get(scaffold);
    }

    /**
     * @param scaffold
     *            the scaffold for which is checked if it is represented in this
     *            vtree
     * @return true iff the vtree contains a node representing
     *         <code>scaffold</code>
     */
    public boolean containsScaffold(Scaffold scaffold) {
        return vnodes.containsKey(scaffold);
    }

    /**
     * @return an unmodifiable collection of all vnodes contained in this tree
     */
    public Collection<ScaffoldNode> getVNodes() {
        return Collections.unmodifiableCollection(vnodes.values());
    }

    /**
     * @return the showScaffoldDetailsNodes
     */
    public boolean isShowScaffoldDetailsNodes() {
        return showScaffoldDetailsNodes;
    }

    /**
     * @param showScaffoldDetailsNodes the showScaffoldDetailsNodes to set
     */
    public void setShowScaffoldDetailsNodes(boolean showScaffoldDetailsNodes) {
        this.showScaffoldDetailsNodes = showScaffoldDetailsNodes;
        for (ScaffoldNode n : Iterables.filter(vnodes.values(),ScaffoldNode.class)) {
            n.setShowDetailsNode(showScaffoldDetailsNodes);
        }
    }
    
    /**
     * Set the comparator used for sorting molecules in ScaffoldDetailNodes
     * @param molComparator
     */
    public void setMoleculeComparator(Comparator<? super Molecule> molComparator) {
        this.moleculeComparator = molComparator;
        if (isShowScaffoldDetailsNodes()) {
            for (ScaffoldNode s : Iterables.filter(getVNodes(), ScaffoldNode.class)) {
                s.setMoleculeComparator(molComparator);
            }
        }
    }

    /**
     * Reorders the scaffolds on the first ring according to the given
     * properties. This method is synchronized, because multiple simultaneous sortings could cause race conditions.
     * Also writes data into a given {@link SortState} object, which are generated during the sort process.
     * @param <T>
     *          The comparable type over which the scaffold nodes are sorted.
     * 
     * @param comparator
     *          A comparator, which is used for sorting VNodes after they are mapped to objects of type <T>
     * @param function
     *          A mapping, which maps a VNode into an object of type <T>. Afterwards the VNode can be sorted using the comparator
     * @param colorSegments
     *          If true the tree is also colored mapping the property range to a color gradient.
     * @param addCaption
     *          If true, each color segment receives a label, which shows the value of the given property.
     * @param color
     *          The color, which is used for mapping values.
     * @param node 
     *          Indicates the node, for which the sorting is applied. If null, the sorting is applied to whole tree. 
     *          If not null only one node and its subnodes are sorted without recoloring.
     * @param sortState
     *          This object contains information for the sorting legend in the sidebar. It is passed to the VTree to add some values 
     *          concerning sorting values and colors.          
     */
    public synchronized <T> void propertySort(Comparator<T> comparator, Function<VNode, T> function, boolean colorSegments,
            boolean addCaption, Color color, VNode node, SortState sortState) {
        
        Ordering<VNode> vnodeOrdering = Ordering.from(comparator).onResultOf(function);
        
        if(node != null) {
            // if node is not null then the sorting is only applied to this single node
            node.sortSubtree(vnodeOrdering);
            layoutInvalid = true;
            return;
        }
        else {
            // else the sorting is applied to the whole tree and coloring is added to the tree
            vroot.sortSubtree(vnodeOrdering);
        }
        List<VNode> nodes = getRoot().getTreeChildren();
        if (nodes.isEmpty()) return;

        layout.clearSeparators();
        if (colorSegments) {
            ArrayList<VNode> separators = Lists.newArrayList();
            ArrayList<String> captions = new ArrayList<String>();
            separators.add(nodes.get(0));
            T lastValue = function.apply(nodes.get(0));
            if (lastValue != null)
                captions.add(addCaption ? lastValue.toString() : "");
            else
                captions.add("");
            for (VNode n : nodes) {
                T currentValue = function.apply(n);
                if (currentValue == lastValue)
                    continue; // both null
                if (currentValue != null && lastValue != null && currentValue.equals(lastValue))
                    continue;
                separators.add(n);
                lastValue = currentValue;
                if (lastValue != null)
                    captions.add(addCaption ? lastValue.toString() : "");
                else
                    captions.add("");
            }
            ArrayList<Color> colors = new ArrayList<Color>();
            double colorLevelIncrement = 255.0 / separators.size();
            for (int i = 0; i < separators.size(); i++) {
                colors.add(new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(colorLevelIncrement * (i+1))));
            }
            
            // This part generates the data for the SortState
            int segments = Math.min(11, separators.size());
                      
            ArrayList<Color> colorsForSortState = new ArrayList<Color>(segments);
            ArrayList<Double> values = new ArrayList<Double>(segments);
            
            /* I assume that the given function maps a ScaffoldNode into a Double value. If that is not the case
             * then this part is skipped by a caught CastException and no data is written into the SortState.
             * This will result into the Sort Legend (sidebar) not showing visual information about the colors.
             */
            try{
                for (int i = 0; i < (segments - 1); i++) {
                    values.add((Double)function.apply(separators.get(((i)*separators.size() / (segments - 1)))));
                }
                values.add((Double)function.apply(separators.get(separators.size() - 1)));
                
                for (int i = 0; i < (segments - 1); i++) {
                    colorsForSortState.add(colors.get(((i)*separators.size() / (segments - 1))));
                }   
                colorsForSortState.add(colors.get(separators.size() - 1));
                
                sortState.setValues(values);
                sortState.setColors(colorsForSortState);
            }
            catch(ClassCastException e){
                
            }
            
            /*
             * float hsb[] = Color.RGBtoHSB(color.getRed(), color.getGreen(),
             * color.getBlue(), null); float colorLevelIncrement = (1-hsb[2])
             * /separators.size(); for (int i = 0; i < separators.size(); i++)
             * colors.add(Color.getHSBColor(hsb[0], hsb[1],
             * 1-(i*colorLevelIncrement)));
             */

            layout.setSeparators(separators, captions, colors);
        }

        layoutInvalid = true;
        return;
    }

    /**
     * @return the svgCache
     */
    public SVGCache getSvgCache() {
        return svgCache;
    }

    /**
     * add a new listener who will be notified when vnodes are added or removed
     * 
     * @param listener
     *            the new listener
     * 
     */
    public void addVNodeListener(VNodeListener listener) {
        vnodeListeners.add(listener);
    }

    /**
     * remove the specified listener
     * 
     * @param listener
     *            the listener to be removed
     */
    public void removeVNodeListener(VNodeListener listener) {
        vnodeListeners.remove(listener);
    }

    private void notifyVNodeListeners(ScaffoldNode vnode, boolean removed) {
        for (VNodeListener l : vnodeListeners) {
            if (removed)
                l.vnodeRemoved(vnode);
            else
                l.vnodeAdded(vnode);
        }
    }
    
    /**
     * Remove all vnodes from the tree
     */
    public void clear() {
        destroyBranch(vroot);
        removeVNode(vroot);
        vroot = null;
    }
}