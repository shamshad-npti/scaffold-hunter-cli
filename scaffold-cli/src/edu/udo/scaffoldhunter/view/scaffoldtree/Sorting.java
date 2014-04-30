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
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Scaffolds;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * Helper class used for sorting a Scaffold Tree.
 * <p>
 * loads necessary properties in the background and then sorts the tree on the
 * AWT event thread
 * 
 * @author Henning Garus
 * 
 */
public class Sorting {

    private final SortSettings selectedSortSettings;

    private final DbManager db;
    private final Dataset dataset;
    private final VCanvas canvas;

    private SortJob lastSortJob = null;
        
    private SortLegendPanel sortLegendPanel;
    private final SortState sortState;

    /**
     * Creates a new Sorting object. Uses a {@link SortSettings} object to read the sort settings it should use.
     * @param db
     *            the db manager
     * @param dataset
     * @param canvas
     *            the canvas containing the {@link VTree} to be sorted
     * @param sortSettings
     *            the object, which contains sort settings, which are set, but not yet applied
     * @param sortState 
     *            the object, which contains information about the current applied sort settings
     */
    public Sorting(DbManager db, Dataset dataset, VCanvas canvas, SortSettings sortSettings, SortState sortState) {
        this.db = db;
        this.dataset = dataset;
        this.canvas = canvas;
        if(sortState == null) {
            sortState = new SortState();
        }
        this.sortState = sortState;
        this.selectedSortSettings = sortSettings;
        
    }

    /**
     * Returns the object, which is read by the Sorting to gather sort instructions.
     * @return the sortSettings
     */
    public SortSettings getSelectedSortSettings() {
        return selectedSortSettings;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }
    
    /**
     * Returns the {@link SortState}, which contains the currently applied sort settings for the Scaffold Tree.
     * @return the SortState
     */
    public SortState getSortState() {
        return sortState;
    }

    /**
     * Returns the panel, which is updated on every sort process.
     * @param sortLegendPanel
     */
    public void setSortLegendPanel(SortLegendPanel sortLegendPanel) {
        this.sortLegendPanel = sortLegendPanel;
    }
    
    /**
     * Associates a {@link SortLegendPanel} with this object. Every time a sorting on the scaffold tree is performed,
     * the panel is updated and shows information about the current sorting.
     * @param sortLegendPanel
     */
    public void getSortLegendPanel(SortLegendPanel sortLegendPanel) {
        this.sortLegendPanel = sortLegendPanel;
    }

    /**
     * Copies the sort settings from selectedSortSettings into the sortState. This method should be used before sorting the tree, 
     * when there are changed in the selectedSortSettings, which should be applied to the sorting.
     */
    public void apply() {
        sortState.setSortSettings(selectedSortSettings.clone());
    }
    
    /**
     * Applies the sorting to the tree using the settings in the sortState. This function should not be called if a
     * sort is currently in progress. The progress listener can be used to
     * ensure that the previous sort is finished.
     * 
     * @param subset
     *            the subset currently displayed by the tree
     * @param progressListener
     *            a progress listener on which finish will be called, once the
     *            sort has been finished
     */
    public void sortTree(Subset subset, ProgressListener<Void> progressListener) {
        VTree vtree = canvas.getVTree();
        if (vtree.getRoot() == null || vtree.getRoot().getTreeChildren().isEmpty()) return;
        
        PropertyDefinition propDef = sortState.getSortSettings().getPropDef(dataset);
        if (propDef == null) {
            sortLegendPanel.refresh();
            return;
        }
        SortJob job = new SortJob(subset, propDef, sortState.getSortSettings().function, sortState.getSortSettings().cumulative,
                sortState.getSortSettings().descending, sortState.getSortSettings().addCaption, 
                sortState.getSortSettings().colorSegments, sortState.getSortSettings().background);
        lastSortJob = job;
        
        SortWorker sortWorker = new SortWorker(db, vtree, job, null, sortLegendPanel, sortState);
        sortWorker.addProgressListener(progressListener);
        sortWorker.execute();
    }
    
    /**
     * Causes a single VNode to be sorted according to the last used sorting properties. If the tree has not been sorted before,
     * then this method will have no effect.
     * @param node 
     *          the node from the VTree, which is meant to be sorted
     */
    public void sortSubtree(ScaffoldNode node) {
        if(lastSortJob == null)
            return;
        
        VTree vtree = canvas.getVTree();
        if (vtree.getRoot() == null || vtree.getRoot().getTreeChildren().isEmpty()) return;
        
        SortWorker sortWorker = new SortWorker(db, vtree, lastSortJob, node, sortLegendPanel, sortState);
        sortWorker.execute();
    }

    private static class SortWorker extends ProgressWorker<Void, SortJob> {

        private final DbManager db;
        private final SortJob job;
        private final VTree vtree;
        private final ScaffoldNode node;
        private final Iterable<Scaffold> scaffolds;
        private final SortLegendPanel sortLegendPanel;
        private final SortState sortState;

        public SortWorker(DbManager db, VTree vtree, SortJob job, ScaffoldNode node, SortLegendPanel sortLegendPanel, SortState sortState) {
            super();
            this.db = db;
            this.job = job;
            this.vtree = vtree;
            // the node is not used in the SortWorker itself. It is given to the vtree afterwards. If null, the whole tree
            // is sorted afterwards. Otherwise only the node and its subnodes are sorted
            this.node = node;
            if(node != null)
                scaffolds = Scaffolds.getSubtreePreorderIterable(node.getScaffold());
            else
                scaffolds = Scaffolds.getSubtreePreorderIterable(vtree.getRoot().getScaffold());
            
            this.sortLegendPanel = sortLegendPanel; // the panel is given to the worker, so it can invoke a refresh in a dedicated thread
            this.sortState = sortState;  // the sortState is given to the worker to refresh the colors and values, which can be used by GUI elements
        }

        @Override
        protected Void doInBackground() throws Exception {
            if (job.propDef.isScaffoldProperty()) {               
                try {                    
                db.lockAndLoad(Collections.singleton(job.propDef), scaffolds);
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }
                if (job.cumulative && !job.propDef.isStringProperty()) {
                    job.derivedProperty = Scaffolds.getTreeCumulativeNumValues(vtree.getRoot().getScaffold(), job.propDef,
                            job.accumulation);
                    db.unlockAndUnload(Collections.singleton(job.propDef), scaffolds);
                }
            } else {                
                assert !job.propDef.isStringProperty();
                job.derivedProperty = db.getAccNumProperties(job.propDef, job.accumulation, job.subset, 
                        vtree.getRoot().getScaffold(), job.cumulative);
            }
            publish(job);
            return null;
        }

        @Override
        // this is somewhat ugly, the alternative would be some duplicated code
        // where the Function produces exactly the type ordered by the ordering
        @SuppressWarnings("rawtypes")
        protected void process(List<SortJob> chunks) {
            assert chunks.size() == 1;
            final PropertyDefinition propDef = job.propDef;            
           
            Function<Scaffold, Comparable> scaffoldTransformation;

            if (propDef.isScaffoldProperty()) {
                if (propDef.isStringProperty()) {
                    scaffoldTransformation = new Function<Scaffold, Comparable>() {
                        @Override
                        public String apply(Scaffold input) {
                            return input != null ? input.getStringPropertyValue(propDef) : null;
                        }
                    };
                } else {
                    if (job.cumulative) {
                        final Map<Scaffold, Double> cumulativeValues = job.derivedProperty;
                        scaffoldTransformation = new Function<Scaffold, Comparable>() {
                            @Override
                            public Double apply(Scaffold input) {
                                return input != null ? cumulativeValues.get(input) : null;
                                
                            }
                        };
                    } else {
                        scaffoldTransformation = new Function<Scaffold, Comparable>() {
                            @Override
                            public Double apply(Scaffold input) {
                                return input != null ? input.getNumPropertyValue(propDef) : null;
                            }
                        };
                    }
                }
            } else {
                assert !propDef.isStringProperty();
                final Map<Scaffold, Double> derivedProperty = job.derivedProperty;
                scaffoldTransformation = new Function<Scaffold, Comparable>() {
                    @Override
                    public Double apply(Scaffold input) {
                        return derivedProperty.get(input);
                    }
                };
            }
            Ordering<Comparable> ordering = new Ordering<Comparable>() {
                @Override
                public int compare(Comparable left, Comparable right) {
                    if (left == null) {
                        if (right == null) {
                            return 0;
                        } else {
                            return -1;
                        }
                    } else {
                        if (right == null) {
                            return 1;
                        } else {
                            @SuppressWarnings("unchecked")
                            int compareTo = left.compareTo(right);
                            return compareTo;
                        }
                    }
                }
            };
            if (job.descending) {
                ordering = ordering.reverse();
            }

            Function<VNode, Comparable> vnodeTransform = Functions.compose(scaffoldTransformation, new Function<VNode, Scaffold>() {
                /* (non-Javadoc)
                 * @see com.google.common.base.Function#apply(java.lang.Object)
                 */
                @Override
                public Scaffold apply(VNode input) {
                    if (input instanceof ScaffoldNode) {
                        return ((ScaffoldNode)input).getScaffold();
                    } 
                    return null;
                }
            });
                        
            vtree.propertySort(ordering, vnodeTransform, job.colorSegments, job.addCaption, job.background, node, sortState);   
            
            // sortPanel is updated after new data has been written into the sort state. only executed when sorting the whole tree
            if(node == null) {
                assert(sortLegendPanel != null);
                if(sortLegendPanel != null)
                    sortLegendPanel.refresh();
            }
            
            if (propDef.isScaffoldProperty() && !job.cumulative) {
                db.unlockAndUnload(Collections.singleton(propDef), scaffolds);
            }
        }
    }

    private static class SortJob {

        private Map<Scaffold, Double> derivedProperty;

        private final Subset subset;

        private final PropertyDefinition propDef;

        private final AccumulationFunction accumulation;

        private final boolean cumulative;

        private final boolean descending;

        private final boolean colorSegments;

        private final boolean addCaption;

        private final Color background;

        public SortJob(Subset subset, PropertyDefinition propDef, AccumulationFunction accumulation,
                boolean cumulative, boolean descending, boolean addCaption, boolean colorSegments, Color background) {
            this.subset = subset;
            this.propDef = propDef;
            this.cumulative = cumulative;
            this.descending = descending;
            this.background = background;
            this.accumulation = accumulation;
            this.addCaption = addCaption;
            this.colorSegments = colorSegments;
        }
    }

    /**
     * Settings for sorting a scaffold tree
     * 
     * @author Henning Garus
     */
    public static class SortSettings implements Serializable, Cloneable {

        private String propertyDefinition = null;
        private String propertyDefinitionTitle = null;
        private boolean cumulative = false;
        private boolean descending = false;
        private Color background = Color.ORANGE.darker();
        private AccumulationFunction function = AccumulationFunction.Average;
        private boolean colorSegments = true;
        private boolean addCaption = true;

        /**
         * Returns whether the cumulation function is used recursively on all subnodes.
         * @return true if this is the case
         */
        public boolean isCumulative() {
            return cumulative;
        }

        /**
         * Sets whether the cumulation function is used recursively on all subnodes.
         * @param cumulative
         *            the boolean value
         */
        public void setCumulative(boolean cumulative) {
            this.cumulative = cumulative;
        }

        /**
         * Returns whether the scaffold tree is divided into colored segments after sorting.
         * @return true if this is the case
         */
        public boolean isColorSegments() {
            return colorSegments;
        }

        /**
         * Sets whether the scaffold tree is divided into colored segments after sorting.
         * @param colorSegments
         *            the boolean value
         */
        public void setColorSegments(boolean colorSegments) {
            this.colorSegments = colorSegments;
        }

        /**
         * Returns whether the values are added to the color segments in the scaffold tree.
         * @return true if this is the case
         */
        public boolean isAddCaption() {
            return addCaption;
        }

        /**
         * Sets whether the values are added to the color segments in the scaffold tree.
         * @param addCaption
         *            the boolean value
         */
        public void setAddCaption(boolean addCaption) {
            this.addCaption = addCaption;
        }

        /**
         * Returns the {@link PropertyDefinition}, which is stored in the sort settings.
         * @param dataset
         *            the dataset where the property definition can be found
         * @return the property definition
         */
        public PropertyDefinition getPropDef(Dataset dataset) {
            return dataset.getPropertyDefinitions().get(propertyDefinition);
        }

        /**
         * Stores a Returns whether the values are added to the color segments in the scaffold tree. Also the stores its title.
         * @param propDef
         *            the property definition
         */
        public void setPropDef(PropertyDefinition propDef) {
            if(propDef != null) {
                this.propertyDefinition = propDef.getKey();
                this.propertyDefinitionTitle = propDef.getTitle();
            }
            else {
                this.propertyDefinition = null;
                this.propertyDefinitionTitle = null;
            }
        }

        /**
         * Returns whether the sorting order is descending.
         * @return true, if it is descending and false, if it is ascending
         */
        public boolean isDescending() {
            return descending;
        }

        /**
         * Sets whether the sorting order is descending.
         * @param descending
         *            true, if it should be descending and false, if it should be ascending
         */
        public void setDescending(boolean descending) {
            this.descending = descending;
        }

        /**
         * Returns the used background color for the color segments of the scaffold tree.
         * @return the color
         */
        public Color getBackground() {
            return background;
        }

        /**
         * Sets the color used for the color segments of the scaffold tree.
         * @param background the color
         */
        public void setBackground(Color background) {
            this.background = background;
        }

        /**
         * Returns the function type, which is used for accumulating molecule properties for scaffolds.
         * @return the function
         */
        public AccumulationFunction getFunction() {
            return function;
        }

        /**
         * Sets the function type, which is used for accumulating molecule properties for scaffolds.
         * @param function
         *            the function
         */
        public void setFunction(AccumulationFunction function) {
            this.function = function;
        }
        
        /**
         * Returns the title of the stored {@link PropertyDefinition}.
         * @return the title
         */
        public String getPropDefTitle() {
            return propertyDefinitionTitle;
        }
        
        @Override
        public SortSettings clone() {
            SortSettings sortSettings = new SortSettings();
            sortSettings.setAddCaption(isAddCaption());
            sortSettings.setBackground(getBackground());
            sortSettings.setColorSegments(isColorSegments());
            sortSettings.setCumulative(isCumulative());
            sortSettings.setDescending(isDescending());
            sortSettings.setFunction(getFunction());
            sortSettings.propertyDefinition = propertyDefinition;
            sortSettings.propertyDefinitionTitle = propertyDefinitionTitle;
            return sortSettings;
        }
    }
}
