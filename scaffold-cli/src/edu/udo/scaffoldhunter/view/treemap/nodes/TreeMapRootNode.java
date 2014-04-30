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

package edu.udo.scaffoldhunter.view.treemap.nodes;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.view.treemap.TreeMapCanvas;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SelectionState;

/**
 * The same as a ScaffoldParent node, only this node will be the root. It has some
 * special properties like default positions
 * 
 * The root will have already set bounds, and load all the children.
 * 
 * @author Lappie
 * 
 */
public class TreeMapRootNode extends TreeMapScaffoldNode implements PropertyChangeListener {
    
    private Subset subset;
    
    private final String DEFAULT_TITLE = "Root";

    /**
     * Load the treeMapNode
     *  
     * @param scaffold The matching scaffold
     * @param selection The selected nodes
     * @param config 
     *            the global settings for among others the selection colors
     * @param viewerDimension 
     *            the dimension through which is being viewed, this allows for proper semantic zooming.
     * @param svgCache necessary to load the svg from the scaffolds
     * @param subset the current subset of the tree map
     * @param canvas for drawing the SVG
     */
    public TreeMapRootNode(Scaffold scaffold, Selection selection, GlobalConfig config, Dimension viewerDimension, SVGCache svgCache, Subset subset, TreeMapCanvas canvas) {
        super(scaffold, selection, config, viewerDimension, svgCache, canvas);

        updateExlusiveBounds(0, 0, TreeMapCanvas.TREEMAP_WIDTH, TreeMapCanvas.TREEMAP_HEIGHT);

        this.subset = subset;
        subset.addPropertyChangeListener(Subset.TITLE_PROPERTY, titleListener);
        setTitle(subset.getTitle());
        
        selection.addPropertyChangeListener(Selection.SELECTION_PROPERTY, this);
        
        validateSelectionOfAllNodes();
    }
    
    /**
     * Set this to true if only the root node has to be drawn. All child nodes will no longer be visible.
     * @param onlyDrawRootNode
     */
    public void onlyDrawRootNode(boolean onlyDrawRootNode) {
        TreeMapNode.onlyDrawRootNode = onlyDrawRootNode;
    }
    
    /**
     * Validate the selection of all nodes. If for example a new node or subset is loaded this is important to check. 
     */
    public void validateSelectionOfAllNodes() {
        validateSelection(this);
    }
    
    /**
     * Validates the selection of the subtree rooted at the given node,
     * @param node starting point for validation
     */
    public void validateSelection(TreeMapNode node) {
        if (node.isMoleculeNode()) {
            if (selection.containsAll(node.getMolecules())) {
                node.setSelectionState(SelectionState.SELECTED);
            } else {
                node.setSelectionState(SelectionState.UNSELECTED);
            }
        } else {
            // validate children
            for (TreeMapNode child : node.getTreeMapChildren()) {
                validateSelection(child);
            }
            // validate this node
            boolean all = true;
            boolean some = false; 
            for (TreeMapNode child : node.getTreeMapChildren()) {
                if (child.getSelectionState() != SelectionState.SELECTED) {
                    all = false;
                }
                if (child.getSelectionState() != SelectionState.UNSELECTED) {
                    some = true;
                }
                if (all == false && some == true) break;
            }
            for (Molecule mol : node.getMolecules()) {
                if (!selection.contains(mol)) {
                    all = false;
                } else {
                    some = true;
                }
                if (all == false && some == true) break;
            }
            if (all) {
                node.setSelectionState(SelectionState.SELECTED);
            } else if (some) {
                node.setSelectionState(SelectionState.HALFSELECTED);
            } else {
                node.setSelectionState(SelectionState.UNSELECTED);
            }
        }
    }
    
    /**
     * Update the viewer dimension, the pixels that a users uses to watch the canvas. 
     * @param viewerDimension
     */
    public void updateViewerDimension(Dimension viewerDimension) {
        this.viewerDimension = viewerDimension;
        for(TreeMapNode child : getAllTreeMapChildrenNodes())
            child.setViewerDimension(viewerDimension);
    }

    @Override
    public void loadScaffold(Scaffold scaffold) {
        super.loadScaffold(scaffold);
        setNodeAndChildrensLevel(1);
        setTitle(generateTitle(subset, scaffold));
    }
    
    private String generateTitle(Subset sub, Scaffold scaf) {
        // create the root title from the subset name and scaffold title
        String rootTitle = sub != null ? sub.getTitle() : DEFAULT_TITLE;
        if(scaf.getTitle() != null)
            rootTitle += " (" + scaf.getTitle() + ")";
        return rootTitle;
    }
    
    private PropertyChangeListener titleListener = new GenericPropertyChangeListener<Set<Structure>>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Set<Structure>> ev) {
            Subset newSubset = ((Subset)ev.getSource());
            setTitle(generateTitle(newSubset, scaffold));
        }
    };
    
    /**
     * This method should be called, when the subset is changed.
     * @param subset the new subset
     */
    public void setSubset(Subset subset) {
        this.subset.removePropertyChangeListener(Subset.TITLE_PROPERTY, titleListener);
        this.subset = subset;
        this.subset.addPropertyChangeListener(Subset.TITLE_PROPERTY, titleListener);
    }
        
    /**
     * Destroy this node and remove its property listeners. 
     */
    public void destroy() {
        selection.removePropertyChangeListener(Selection.SELECTION_PROPERTY, this);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        validateSelectionOfAllNodes();
    }
    
    @Override
    public String getTitleShort() {
        if(getStructure().getTitle() != null)
            return getStructure().getTitle();
        else
            return subset.getTitle();
    }
}
