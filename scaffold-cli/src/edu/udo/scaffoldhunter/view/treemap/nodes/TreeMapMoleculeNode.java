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
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.view.treemap.TreeMapCanvas;
import edu.udo.scaffoldhunter.view.treemap.TreeMapView;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.umd.cs.piccolo.PNode;

/**
 * This TreeMapNode holds a Molecule, and no more. This Node should never
 * have children. 
 * 
 * It offers the same functionality as TreeMapNode only has some special
 * settings. 
 * 
 * @author Lappie
 *
 */
public class TreeMapMoleculeNode extends TreeMapNode {

    private static Logger logger = LoggerFactory.getLogger(TreeMapView.class);
    
    /**
     * @param molecule
     * @param selection
     * @param config
     * @param viewerDimension
     * @param svgCache
     * @param canvas
     */
    public TreeMapMoleculeNode(Molecule molecule, Selection selection, GlobalConfig config, Dimension viewerDimension,
            SVGCache svgCache, TreeMapCanvas canvas) {
        super(selection, config, viewerDimension, svgCache, canvas);
        this.molecule = molecule;
        setTitle(molecule.getTitle());
        
        setSVGLoaderData(svgCache, molecule);
    }

    @Override
    protected boolean isBorderDashed() {
        return true;
    }

    @Override
    public List<Molecule> getMolecules() {
        return Arrays.asList(molecule);
    }
    
    @Override
    public Structure getStructure() {        
        return molecule;
    }

    @Override
    public boolean isMoleculeNode() {
        return true;
    }
    
    /**
     * A Molecule Node can only have a single text node as child
     */
    @Override
    public void addChild(final int index, final PNode child) {
        if(child instanceof TreeMapTextNode && getChildrenCount() == 0)
            super.addChild(index, child);
        else
            logger.error("Molecule-node can only have one text node child.");
    }
}
