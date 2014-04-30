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
import java.util.ArrayList;
import java.util.List;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.view.treemap.TreeMapCanvas;
import edu.udo.scaffoldhunter.view.util.SVGCache;

/**
 * This node is meant to hold a scaffold with children in the TreeMap. 
 * It inheritances all functionality from TreeMapNode but offers some special
 * functions and settings like loading a scaffold. 
 * 
 * @author Lappie
 *
 */
public class TreeMapScaffoldNode extends TreeMapNode {
    
    private boolean hasNoScaffoldChildren = false;
    
    /**
     * @param scaffold 
     * @param selection
     * @param config
     * @param viewerDimension
     * @param svgCache
     * @param canvas
     */
    public TreeMapScaffoldNode(Scaffold scaffold, Selection selection, GlobalConfig config, Dimension viewerDimension,
            SVGCache svgCache, TreeMapCanvas canvas) {
        super(selection, config, viewerDimension, svgCache, canvas);
        loadScaffold(scaffold);
        
        if(isLeaf())
            setSVGLoaderData(svgCache, scaffold);
    }

    /**
     * Load a new scaffold and remove the data from the old scaffold. 
     * 
     * NOTE: Only loads parentNodes. if leafnodes are necessary you have to call loadScaffoldLeafs
     * or loadMoleculeLeafs
     * @param scaffold
     */
    protected void loadScaffold(Scaffold scaffold) {
        this.scaffold = scaffold;
        setTitle(scaffold.getTitle());
        
        removeAllChildren();
        
        hasNoScaffoldChildren = true;
        for (Scaffold childScaffold : scaffold.getChildren()) {
            addScaffoldParentNode(childScaffold);
            hasNoScaffoldChildren = false;
        }
        
        for(Molecule m : scaffold.getMolecules()) {
            TreeMapMoleculeNode molNode = new TreeMapMoleculeNode(m, selection, globalConfig, viewerDimension, svgCache, canvas);
            addChild(molNode);
        }
    }

    private void addScaffoldParentNode(Scaffold childScaffold) {
        TreeMapNode child = new TreeMapScaffoldNode(childScaffold, selection, globalConfig, viewerDimension, svgCache, canvas);
        addChild(child);
    }
    
    /**
     * @return the scaffold belonging to this node
     */
    public Scaffold getScaffold() {
        return scaffold;
    }
    
    @Override
    public Structure getStructure() {
        return scaffold;
    }

    @Override
    public List<Molecule> getMolecules() {
        List<Molecule> molecules = new ArrayList<Molecule>();
        return molecules;
    }
    
    @Override
    protected boolean isBorderDashed() {
        return !canvas.getDisplayMolecules() && hasNoScaffoldChildren;
    }

    @Override
    public boolean isMoleculeNode() {
        return false;
    }
    
    @Override
    public boolean isLeaf() {
        return !canvas.getDisplayMolecules() && hasNoScaffoldChildren;
    }
}
