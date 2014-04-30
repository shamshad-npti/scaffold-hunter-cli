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

package edu.udo.scaffoldhunter.model.dataimport;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.dataimport.ImportJob.SourcePropertyMapping;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * Represents all {@link ImportJob}s for one import process and provides methods
 * which work on these import sources on the whole.
 * 
 * @author Henning Garus
 * 
 */
public class ImportProcess {

    private final List<ImportJob> importJobs = Lists.newArrayList();
    private final PlainDocument datasetName = new PlainDocument();
    private final Document datasetDescription = new PlainDocument();
    private final Set<String> existingDatasets;
    
    private final Dataset dataset;
    
    private boolean isPrepared = false;

    /**
     * Create a new ImportProcess, to create a new Dataset.
     * 
     * @param defaultDatasetName
     *            the name which is given to the dataset initially
     * @param existingDatasets 
     *            a set containing the titles of all existing datasets
     */
    public ImportProcess(String defaultDatasetName, Set<String> existingDatasets) {
        dataset = null;
        this.existingDatasets = existingDatasets;
        try {
            datasetName.insertString(0, defaultDatasetName, null);
        } catch (BadLocationException e) {
            // When the day comes where 0 is a bad insert location we have a
            // problem
            throw new AssertionError();
        }
    }

    /**
     * Create a new ImportProcess to add properties to an existing dataset
     * 
     * @param dataset
     *            the dataset to which properties will be added
     */
    public ImportProcess(Dataset dataset) {
        this.dataset = dataset;
        this.existingDatasets = Sets.newHashSet();
        try {
            datasetName.insertString(0, dataset.getTitle(), null);
            datasetDescription.insertString(0, dataset.getComment(), null);
        } catch (BadLocationException e) {
            // 0 is not a bad location EVER!
            throw new AssertionError();
        }
    }

    /**
     * 
     * @return the list of import sources represented by this class
     */
    public List<ImportJob> getJobs() {
        return Collections.unmodifiableList(importJobs);
    }

    private ImmutableMultimap<ImportJob, Integer> getPropertiesWithUndefinedMergeStrategy() {
        ImmutableMultimap.Builder<ImportJob, Integer> builder = ImmutableMultimap.builder();
        Set<PropertyDefinition> propertyDefinitions = Sets.newHashSet();
        for (ImportJob s : importJobs) {
            int i = -1;
            for (SourcePropertyMapping m : s.getPropertyMappings().values()) {
                i++;
                PropertyDefinition propDef = m.getPropertyDefiniton();
                if (propDef == null)
                    continue;
                if (propertyDefinitions.contains(propDef) && m.getMergeStrategy() == MergeStrategy.NONE)
                    builder.put(s, i);
                else
                    propertyDefinitions.add(propDef);
            }

        }
        return builder.build();
    }

    /**
     * 
     * @return a multimap which contains for each import job the property
     *         definitions which are already defined by a previous import job.
     */
    public ImmutableMultimap<ImportJob, PropertyDefinition> getMergedPropeties() {
        ImmutableMultimap.Builder<ImportJob, PropertyDefinition> builder = ImmutableMultimap.builder();
        Set<PropertyDefinition> allDefinitions = Sets.newHashSet();
        Set<PropertyDefinition> definitionsFromJ = Sets.newHashSet();
        for (ImportJob j : importJobs) {
            definitionsFromJ.clear();
            for (SourcePropertyMapping m : j.getPropertyMappings().values()) {
                PropertyDefinition propDef = m.getPropertyDefiniton();
                if (propDef != null)
                    definitionsFromJ.add(m.getPropertyDefiniton());
            }

            builder.putAll(j, Sets.intersection(allDefinitions, definitionsFromJ));
            allDefinitions.addAll(definitionsFromJ);
        }
        return builder.build();

    }

    // TODO maybe speed this up
    /**
     * Check for some imported property if a merge strategy should be set, but
     * is not.
     * <p>
     * A merge strategy should be set for a <code>SourcePropertyMapping</code> A
     * whenever there is another <code>SourcePropertyMapping</code> B previous
     * to A, either for the same source or a source preceding the source of A,
     * which maps to the same <code>PropertyDefinition</code>.
     * <p>
     * An imported property is described by its source and an index which is its
     * position inside its source. This method uses an index to describe a
     * property to make calling it from table based methods more convenient.
     * 
     * @param source
     *            the source containing the property mapping for which this
     *            query is undertaken
     * @param index
     *            the index of the property mapping
     * @return <code>true</code> if the merge strategy for the property mapping
     *         should be set but is not. <code>false</code> otherwise.
     * 
     */
    public boolean isUndefinedMergeStrategy(ImportJob source, int index) {
        return getPropertiesWithUndefinedMergeStrategy().get(source).contains(index);
    }

    /**
     * Get a list of <code>PropertyDefinitions</code> to which properties from
     * this sources are mapped. The list contains each property exactly once.
     * 
     * @return a list of property definitions to which properties from this
     *         sources are mapped.
     */
    public List<PropertyDefinition> getPropertyDefinitions() {
        List<PropertyDefinition> propertyDefinitions = Lists.newArrayList();
        Set<String> keys = Sets.newHashSet();
        for (ImportJob s : importJobs) {
            for (SourcePropertyMapping m : s.getPropertyMappings().values()) {
                if (m.getPropertyDefiniton() == null || keys.contains(m.getPropertyDefiniton().getKey()))
                    continue;
                keys.add(m.getPropertyDefiniton().getKey());
                propertyDefinitions.add(m.getPropertyDefiniton());
            }
        }
        return propertyDefinitions;
    }

    /**
     * prepares each job for the import process: Sets up initial mappings for
     * the properties provided by each job.
     * 
     * @param propertyDefinitons
     */
    public void prepareImport(Map<String, PropertyDefinition> propertyDefinitons) {
        Iterator<ImportJob> i = importJobs.iterator();
        while (i.hasNext()) {
            ImportJob job = i.next();
            job.computePluginResults(propertyDefinitons);
        }
        isPrepared = true;
    }

    /**
     * 
     * @return a <code>JobsModel</code> allowing acces to the jobs in this
     *         process through a ListModel interface.
     */
    public JobsModel getJobsModel() {
        return new JobsModel();
    }

    /**
     * A ListModel which holds all the jobs, which are part of this import
     * process and which provides additional methods to add and remove jobs and
     * to move jobs around in the list.
     */
    public class JobsModel extends AbstractListModel {

        private int selectedIndex = -1;

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.ListModel#getSize()
         */
        @Override
        public int getSize() {
            return importJobs.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see javax.swing.ListModel#getElementAt(int)
         */
        @Override
        public Object getElementAt(int index) {
            if (index < 0 || index >= importJobs.size())
                return null;
            return importJobs.get(index);
        }

        /**
         * Add a job to the list
         * 
         * @param job
         *            job to be added to the list
         */
        public void add(ImportJob job) {
            importJobs.add(job);
            fireIntervalAdded(this, importJobs.size() - 1, importJobs.size() - 1);
        }

        /**
         * Remove the currently selected element. If no element is selected do
         * nothing.
         */
        public void removeSelectedElement() {
            if (selectedIndex != -1) {
                importJobs.remove(selectedIndex);
                fireIntervalRemoved(this, selectedIndex, selectedIndex);
                selectedIndex = -1;
            }
        }

        /**
         * Move the current selection upward/backward in the list. If no element
         * or the uppermost element is selected do nothing.
         */
        public void moveSelectionUp() {
            if (selectedIndex <= 0)
                return;
            ImportJob src = importJobs.remove(selectedIndex);
            importJobs.add(selectedIndex - 1, src);
            selectedIndex--;
            fireContentsChanged(this, selectedIndex, selectedIndex + 1);
        }

        /**
         * Move the current selection downward/forward in the list. If no
         * element or the uppermost element is selected do nothing.
         */
        public void moveSelectionDown() {
            if (selectedIndex == importJobs.size() - 1 || selectedIndex == -1)
                return;
            ImportJob src = importJobs.remove(selectedIndex);
            importJobs.add(selectedIndex + 1, src);
            selectedIndex++;
            fireContentsChanged(this, selectedIndex - 1, selectedIndex);
        }

        /**
         * 
         * @return a list selection model holding a single selection from the
         *         JobsModel
         */
        public ListSelectionModel getListSelectionModel() {
            class SelectionModel extends DefaultListSelectionModel {

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.DefaultListSelectionModel#getSelectionMode()
                 */
                @Override
                public int getSelectionMode() {
                    return SINGLE_SELECTION;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.DefaultListSelectionModel#clearSelection()
                 */
                @Override
                public void clearSelection() {
                    int oldindex = selectedIndex;
                    selectedIndex = -1;
                    fireValueChanged(oldindex, oldindex);
                }

                private SelectionModel() {
                    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * javax.swing.DefaultListSelectionModel#isSelectedIndex(int)
                 */
                @Override
                public boolean isSelectedIndex(int index) {
                    return index == selectedIndex;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see javax.swing.DefaultListSelectionModel#isSelectionEmpty()
                 */
                @Override
                public boolean isSelectionEmpty() {
                    return selectedIndex == -1;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * javax.swing.DefaultListSelectionModel#getLeadSelectionIndex()
                 */
                @Override
                public int getLeadSelectionIndex() {
                    return selectedIndex;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * javax.swing.DefaultListSelectionModel#moveLeadSelectionIndex
                 * (int)
                 */
                @Override
                public void moveLeadSelectionIndex(int leadIndex) {
                    selectedIndex = leadIndex;
                    super.moveLeadSelectionIndex(leadIndex);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * javax.swing.DefaultListSelectionModel#getMinSelectionIndex()
                 */
                @Override
                public int getMinSelectionIndex() {
                    return selectedIndex;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * javax.swing.DefaultListSelectionModel#getMaxSelectionIndex()
                 */
                @Override
                public int getMaxSelectionIndex() {
                    return selectedIndex;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see
                 * javax.swing.DefaultListSelectionModel#setSelectionInterval
                 * (int, int)
                 */
                @Override
                public void setSelectionInterval(int index0, int index1) {
                    int oldindex = selectedIndex;
                    selectedIndex = index0;
                    fireValueChanged(Math.min(oldindex, selectedIndex), Math.max(oldindex, selectedIndex));
                }

            }
            return new SelectionModel();
        }
    }

    /**
     * @return a document holding the name of the dataset, which is either
     *         edited or created during this import.
     */
    public Document getDatasetNameDocument() {
        return datasetName;
    }

    /**
     * @return a document holding the comment of the dataset, which is either
     *         edited or created during this import.
     */
    public Document getDatasetDescriptionDocument() {
        return datasetDescription;
    }

    /**
     * 
     * @return the title of the dataset created by this import process
     */
    public String getDatasetTitle() {
        int l = datasetName.getLength();
        try {
            return datasetName.getText(0, l);
        } catch (BadLocationException e) {
            // getText should never throw since we use the current length.
            throw new AssertionError(e);
        }
    }

    /**
     * 
     * @return the comment for the dataset created by this import process
     */
    public String getDatasetComment() {
        int l = datasetDescription.getLength();
        try {
            return datasetDescription.getText(0, l);
        } catch (BadLocationException e) {
            // getText should never throw since we use the current length.
            throw new AssertionError(e);
        }
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @return the existingDatasets
     */
    public Set<String> getExistingDatasets() {
        return existingDatasets;
    }
    
    

    /**
     * @return the isPrepared
     */
    public boolean isPrepared() {
        return isPrepared;
    }
}
