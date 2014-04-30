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

package edu.udo.scaffoldhunter.view.treemap.loading;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.VoidNullaryDBFunction;
import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.view.treemap.TreeMapCanvas;
import edu.udo.scaffoldhunter.view.treemap.nodes.TreeMapNode;
import edu.udo.scaffoldhunter.view.treemap.sidebar.ColorLegend;

/**
 * This class is responsible for loading all propertydata from the database and
 * putting it in the right place. All that while handling any database errors. 
 * 
 * @author Lappie
 * 
 */
public class TreeMapDbsLoader {
    
    private static Logger logger = LoggerFactory.getLogger(TreeMapDbsLoader.class);

    private TreeMapCanvas canvas;

    private DbManager db;

    private Double colorMinimumValue = 0.0;
    private Double colorMaximumValue = 0.0;

    private Double sizeMinimumValue = 0.0;
    private Double sizeMaximumValue = 0.0;

    Map<Scaffold, Double> sizeResults = new HashMap<Scaffold, Double>();
    Map<Scaffold, Double> colorResults = new HashMap<Scaffold, Double>();

    private Subset subset;

    private ColorLegend legend;

    /**
     * This is the minimal size that will be added to a TreeMapNode once the set
     * of values contains negative values. This is actually dependent on the
     * difference between maximum and minimum size
     */
    private double sizeOffset = 0.001;

    /**
     * Creates a new TreeMapLoader which loads the data from the database and fills the treemapnodes with it 
     * @param db
     * @param subset
     */
    public TreeMapDbsLoader(DbManager db, Subset subset) {
        this.db = db;
        this.subset = subset;
    }

    /**
     * Set the canvas, necessary for obtaining the root treemap so that it can be updated with new data
     * @param canvas
     */
    public void setCanvas(TreeMapCanvas canvas) {
        this.canvas = canvas;
    }

    /**
     * load the treemapnodes with the sizes of the molecules they obtain. 
     * This is done cumulative. 
     */
    public void loadMoleculeSizes() {
        canvas.noSizes(false);
        reloadMoleculeSizes(canvas.getRootNode());
    }

    /**
     * Obtain the scaffold root from the database with the given subset
     * @return current scaffold root
     */
    public Scaffold getRootScaffold() {
        return DBExceptionHandler.callDBManager(db, new DBFunction<Scaffold>() {
            @Override
            public Scaffold call() throws DatabaseException {
                return db.getScaffolds(subset, true);
            }
        });
    }

    /**
     * load the sizes of the treemapnodes with the corresponding value for the given property
     * This is done cumulative. 
     * 
     * @param pd The property that will be loaded
     * @return false is loading failed. 
     */
    public boolean loadSize (final PropertyDefinition pd) {
        if (pd == null) {
            canvas.noSizes(true);
            return false; //no sizes to display 
        }
        canvas.noSizes(false);

        // initialization
        
        final List<PropertyDefinition> properties = Arrays.asList(pd);
        final List<Molecule> molecules = canvas.getRootNode().getAllMolecules();
        final List<Scaffold> scaffolds = new ArrayList<Scaffold>();
        for(TreeMapNode node : canvas.getRootNode().getAllTreeMapChildrenNodes()) {
            if(node.getStructure() instanceof Scaffold) {
                scaffolds.add((Scaffold)node.getStructure());
            }
        }
        
        // acquire property data        
        DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {
            @Override
            public void voidCall() throws DatabaseException {
                if(pd.isScaffoldProperty())
                    db.lockAndLoad(properties, scaffolds);
                else
                    db.lockAndLoad(properties, molecules);
            }
        });
        
        // load property data  
        
        if(pd.isScaffoldProperty()) {
            // calculation for scaffold properties
            sizeResults.clear();
            for(Scaffold s : scaffolds) {
                sizeResults.put(s, s.getNumPropertyValue(pd));
            }
        }
        else {
            // calculation for molecule properties
            sizeResults = DBExceptionHandler.callDBManager(db, new DBFunction<Map<Scaffold, Double>>() {
                @Override
                public Map<Scaffold, Double> call() throws DatabaseException {
                    return db.getAccNumProperties(pd, AccumulationFunction.Sum, subset, canvas.getRootNode() 
                            .getScaffold(), false);
                }
            });
        }
        
        // calculate minimum and maximum  
        
        List<Double> individualResults = new ArrayList<Double>();
        for(Scaffold s : sizeResults.keySet()) { //Add values for scaffold
            if(sizeResults.get(s) != null)
                individualResults.add(sizeResults.get(s));
        }
        
        if(!pd.isScaffoldProperty()) {
            for(Molecule molecule : molecules) {//Add values for molecules
                Double sizeValue = molecule.getNumPropertyValue(pd);
                if(sizeValue != null)
                    individualResults.add(sizeValue);
            }
        }
                
        sizeMinimumValue = Collections.min(individualResults);
        sizeMaximumValue = Collections.max(individualResults);
        
        // calculate offset
        
        sizeOffset = Math.abs(sizeMaximumValue - sizeMinimumValue) * 0.01;
        if(sizeOffset == 0.0) {
            logger.warn("Data does not contain any variation. Could not be drawn.");
            return false;
        }
        
        // check non-emptiness
        
        if(individualResults.isEmpty()) {
            logger.warn("Sizes could not be loaded");
            return false;
        }
        
        // calculate sizes
        
        reloadPropertySizes(canvas.getRootNode(), pd);
        
        // unlock and unload
        
        if(molecules.size() > 0) {
            try {
                if(pd.isScaffoldProperty())
                    db.unlockAndUnload(properties, scaffolds);
                else
                    db.unlockAndUnload(properties, molecules);
            } catch (Exception e) {
                logger.warn("Trying to unlock data, where no data has been locked before");
            }
        }
        return true;
    }

    /**
     * load the treemapnode-colors with the corresponding value for the given property
     * This is done cumulative. 
     * 
     * @param pd The property that will be loaded
     * @param loadMoleculeData if false, no molecules will be loaded. This can affect the color
     * representation due to the fact that the minimum and maximum values will change
     * @param acc the accumulation function for calculating scaffold colors
     * @param cumulative whether the accumulation function is applied to all molecules in the subtree
     * @param subsetInterval whether the maximum/minimum color should refer to the maximum/minimum of the current subset (true)
     * or to the whole data set (false)
     * @return false if loading failed
     */
    public boolean loadColor(final PropertyDefinition pd, 
            boolean loadMoleculeData, 
            final AccumulationFunction acc, 
            final boolean cumulative,
            final boolean subsetInterval) {
        if (pd == null) {
            for(TreeMapNode node : canvas.getRootNode().getAllTreeMapChildrenNodes())
                node.resetColorValue();
            canvas.getRootNode().resetColorValue();
            legend.reset();
            return true; //loading was a success, just nothing to do
        }
                
        // load and calculate properties from database
        
        final List<PropertyDefinition> properties = Arrays.asList(pd);
        final List<Molecule> molecules = canvas.getRootNode().getAllMolecules();
        final List<Scaffold> scaffolds = new ArrayList<Scaffold>();
        for(TreeMapNode node : canvas.getRootNode().getAllTreeMapChildrenNodes()) {
            if(node.getStructure() instanceof Scaffold) {
                scaffolds.add((Scaffold)node.getStructure());
            }
        }       
        
        DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {
            @Override
            public void voidCall() throws DatabaseException {
                if(pd.isScaffoldProperty())
                    db.lockAndLoad(properties, scaffolds);
                else
                    db.lockAndLoad(properties, molecules);
            }
        });
        
        colorResults = DBExceptionHandler.callDBManager(db, new DBFunction<Map<Scaffold, Double>>() {
            @Override
            public Map<Scaffold, Double> call() throws DatabaseException {
                return db.getAccNumProperties(pd, acc, subset, canvas
                        .getRootNode().getScaffold(), cumulative);
            }
        });
        
        // calculate colors
                  
        colorMinimumValue = Double.POSITIVE_INFINITY;
        colorMaximumValue = Double.NEGATIVE_INFINITY;
        
        reloadColors(canvas.getRootNode(), pd, acc, cumulative);
        
        // calculate color borders
        
        double[] minmax = DBExceptionHandler.callDBManager(db, new DBFunction<double[]>() {
            @Override
            public double[] call() throws DatabaseException {
                if(subsetInterval)
                    return db.getAccPropertyMinMax(subset.getSession().getTree(), pd, acc, subset, cumulative, false, canvas.getDisplayMolecules());
                else
                    return db.getAccPropertyMinMax(subset.getSession().getTree(), pd, acc, subset.getSession().getSubset(), cumulative, false, canvas.getDisplayMolecules());
            }
        });
        colorMinimumValue = minmax[0];
        colorMaximumValue = minmax[1];
                
        // unload data
        
        try {
            if(pd.isScaffoldProperty())
                db.unlockAndUnload(properties, scaffolds);
            else
                db.unlockAndUnload(properties, molecules);
        } catch (Exception e) {
            logger.warn("Trying to unlock data, where no data has been locked before");
        }
        
        // paint
        
        legend.setMin(colorMinimumValue.floatValue());
        legend.setMax(colorMaximumValue.floatValue());
        
        legend.setTitle(pd.getTitle());
        legend.repaint();
        
        return true;
    }

    /**
     * Return the relative size. Which makes sure it is not negative and not zero. 
     * 
     * to prevent becoming zero an offset is added. 
     * @param node
     * @return the relative size
     */
    private double getRelativeDrawSize(Double size) {
        if (size == null || Double.isNaN(size)) {
            return sizeOffset;
        }
        if (sizeMinimumValue < 0) { //else we don't need to make it relative. 
            return size - sizeMinimumValue + sizeOffset; // relative size
        }
        if(size == 0.0) {
            return sizeOffset;
        }
        return size;
    }
    
    private Double addDouble(Double one, Double two) {
        if(one == null)
            return null;
        if(two == null)
            return one;
        return one + two;
    }
    
    /**
     * Get the size depending on whether the node is a scaffold or a molecule. 
     */
    private Double getSize(TreeMapNode node, PropertyDefinition pd) {
        if(node.isMoleculeNode()) {
            return node.getMolecules().get(0).getNumPropertyValue(pd);
        }
        return sizeResults.get(node.getStructure());
    }

    /**
     * Tell the given node and all its children to reload its size value and set
     * it to the given property value
     */
    private void reloadPropertySizes(TreeMapNode node, PropertyDefinition pd) {        
        if (node.getPlottedTreeMapChildren().size() == 0) {
            Double size = getSize(node, pd);
            node.setDrawSize(getRelativeDrawSize(size));
            node.setSizeValue(size);
            return;
        }
        
        double drawSize = 0.0; //drawing size can never be null
        Double sizeValue = 0.0; //this can

        //all children:
        for (TreeMapNode child : node.getPlottedTreeMapChildren()) {
            reloadPropertySizes(child, pd);
            drawSize += child.getDrawSize();
            sizeValue = addDouble(sizeValue, child.getSizeValue());
        }
        
        //this node is the sum of its children
        node.setDrawSize(drawSize);
        node.setSizeValue(sizeValue);
    }

    /**
     * Tell the given node and all its children to reload its size value and set
     * it to the number of molecules it holds
     */
    private void reloadMoleculeSizes(TreeMapNode node) {
        Preconditions.checkArgument(node.getStructure() != null);
        double size = 0;
        for (TreeMapNode child : node.getPlottedTreeMapChildren()) {
            reloadMoleculeSizes(child);
            size += child.getDrawSize();
        }
        if(size == 0)
            size = 1;
        node.setDrawSize(size);
        node.setSizeValue(size);
    }
    
    private Double getColor(TreeMapNode node, PropertyDefinition pd) {
        if(node.isMoleculeNode()) {
            if(pd.isScaffoldProperty())
                return null;
            else
                return node.getMolecules().get(0).getNumPropertyValue(pd);
        }
        return colorResults.get(node.getStructure());
    }
    
    private void reloadColors(TreeMapNode node, PropertyDefinition pd, AccumulationFunction acc, boolean cumulative) {        
        if(node.getTreeMapChildren().size() > 0) {
            for (TreeMapNode child : node.getTreeMapChildren()) {
                reloadColors(child, pd, acc, cumulative); //recursion here
            }
        }
        
        Double colorValue = getColor(node, pd);
        node.setColorValue(colorValue);
        node.setColorPropertyValue(colorValue);
        //TODO: Not longer needed
        /*if(colorValue != null) {
            colorMinimumValue = Math.min(colorMinimumValue, colorValue);
            colorMaximumValue = Math.max(colorMaximumValue, colorValue);
        }*/
    }

    /**
     * @param legend : Set the colorlegend so that the min and max values can be updated
     */
    public void setLegend(ColorLegend legend) {
        this.legend = legend;
    }

    /**
     * Set this to be the new subset, from now all that will be loaded will be
     * from this subset
     * 
     * @param subset
     */
    public void setSubset(Subset subset) {
        this.subset = subset;
    }
}
