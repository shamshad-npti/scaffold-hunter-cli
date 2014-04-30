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

import static com.google.common.base.Predicates.not;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.data.Job;
import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * Describes some source to be imported and the mappings used for the actual
 * import process.
 * 
 * @author Henning Garus
 * 
 */
public class ImportJob implements Job, MessageListener {

    private final ImportPlugin plugin;
    private Object pluginArguments;
    private final String jobName;
    private PluginResults results = null;
    private String titleProperty;
    private String sourceMergeBy = null;
    private PropertyDefinition internalMergeBy;
    private MergeStrategy titleMergeStrategy = MergeStrategy.DONT_OVERWRITE;
    private MergeStrategy structureMergeStrategy = MergeStrategy.DONT_OVERWRITE;
    private Map<String, SourcePropertyMapping> propertyMappings;
    private final List<MessageListener> messageListeners = Lists.newLinkedList();

    /**
     * Creates a new import job
     * 
     * @param name
     *            the name of the job
     * @param plugin
     *            the plugin responsible for this job
     * @param pluginArguments
     *            the argument which will be given to the plugin to perform this
     *            job
     * 
     */
    public ImportJob(String name, ImportPlugin plugin, Object pluginArguments) {
        this.plugin = plugin;
        this.pluginArguments = pluginArguments;
        this.jobName = name;
    }

    /**
     * Calls the plugin with its arguments and sets up the property mappings
     * based on the plugin results
     * 
     * @param propertyDefinitions
     *            a map from keys to property definitions, if a plugin maps to a
     *            property definition with a key in this map the mapping will be
     *            changed to map to the property defintion in the map. If a
     *            plugin maps to a property definition not in the map it will be
     *            added to the map.
     */
    public void computePluginResults(Map<String, PropertyDefinition> propertyDefinitions) {
        if (results != null) {
            results.removeMessageListener(this);
        }
        this.results = plugin.getResults(pluginArguments);
        results.addMessageListener(this);

        Map<String, PropertyDefinition> sourceProperties = results.getSourceProperties();
        propertyMappings = new LinkedHashMap<String, ImportJob.SourcePropertyMapping>(sourceProperties.size());
        for (Map.Entry<String, PropertyDefinition> propDef : sourceProperties.entrySet()) {
            if (propDef.getValue() != null) {
                if (!propDef.getValue().getKey().isEmpty()
                        && propertyDefinitions.containsKey(propDef.getValue().getKey()))
                    propDef.setValue(propertyDefinitions.get(propDef.getValue().getKey()));
                else {
                    propertyDefinitions.put(propDef.getValue().getKey(), propDef.getValue());
                }
            }
            propertyMappings.put(propDef.getKey(), new SourcePropertyMapping(propDef.getValue()));
        }
        titleProperty = results.getTitleMapping();
        if (titleProperty == null)
            titleProperty = sourceProperties.keySet().iterator().next();

        // TODO do we always have at least one property?
        if (titleProperty == null)
            this.titleProperty = propertyMappings.keySet().iterator().next();
    }

    /**
     * @return the source property which is mapped to the <code>Molecule</code>
     *         title
     */
    public String getTitleProperty() {
        return titleProperty;
    }

    /**
     * 
     * @param titlePropery
     *            the source property which is mapped to the
     *            <code>Molecule</code> title
     */
    public void setTitleProperty(String titlePropery) {
        this.titleProperty = titlePropery;
    }

    /**
     * @return the strategy used to merge the <code>Molecule</code> titles
     *         should the same molecule be imported from several sources
     */
    public MergeStrategy getTitleMergeStrategy() {
        return titleMergeStrategy;
    }

    /**
     * @param titleMergeStrategy
     *            the strategy used to merge the <code>Molecule</code> titles
     *            should the same molecule be imported from several sources
     */
    public void setTitleMergeStrategy(MergeStrategy titleMergeStrategy) {
        this.titleMergeStrategy = titleMergeStrategy;
    }

    /**
     * @param structureMergeStrategy
     *            the strategy used to merge molecular structures, should the
     *            same molecule be imported from several sources
     */
    public void setStructureMergeStrategy(MergeStrategy structureMergeStrategy) {
        this.structureMergeStrategy = structureMergeStrategy;
    }

    /**
     * @return the sourceMergeBy
     */
    public String getSourceMergeBy() {
        return sourceMergeBy;
    }

    /**
     * @param sourceMergeBy
     *            the sourceMergeBy to set
     */
    public void setSourceMergeBy(String sourceMergeBy) {
        this.sourceMergeBy = sourceMergeBy;
    }

    /**
     * @return the internalMergeBy
     */
    public PropertyDefinition getInternalMergeBy() {
        return sourceMergeBy == null ? null : internalMergeBy;
    }

    /**
     * @param propertyDefinition
     *            the internalMergeBy to set
     */
    public void setInternalMergeBy(PropertyDefinition propertyDefinition) {
        this.internalMergeBy = propertyDefinition;
    }

    /**
     * @param index
     *            the index or row of the Property name
     * @return the property name for the given Index
     */
    public String getPropertyName(int index) {
        return Iterables.get(propertyMappings.keySet(), index);
    }

    /**
     * @param index
     *            the index or row of the PropertyMapping
     * @return SourcePropertyMapping for the given Index. The
     *         sourceMergeByProperty is not filtered out. Therefore this Mapping
     *         might not be used by the actual import process
     */
    public SourcePropertyMapping getPropertyMapping(int index) {
        return Iterables.get(propertyMappings.values(), index);
    }

    /**
     * @return the strategy used to merge molecular structures, should the same
     *         molecule be imported from several sources
     */
    public MergeStrategy getStructureMergeStrategy() {
        return structureMergeStrategy;
    }

    /**
     * @return the textual description of this source
     */
    public String getDescription() {
        return plugin.getDescription();
    }

    /**
     * @return the propertyMappings defined for this source. The
     *         sourceMergeByProperty is filtered out.
     */
    public Map<String, SourcePropertyMapping> getPropertyMappings() {
        return Collections.unmodifiableMap(Maps.filterKeys(propertyMappings,
                not(getStringFilterPredicate(Collections.singleton(sourceMergeBy)))));
    }

    /**
     * @return the pluginArguments
     */
    public Object getPluginArguments() {
        return pluginArguments;
    }

    /**
     * @param pluginArguments
     *            the pluginArguments to set
     */
    public void setPluginArguments(Object pluginArguments) {
        this.pluginArguments = pluginArguments;
    }

    /**
     * @return the plugin
     */
    public ImportPlugin getPlugin() {
        return plugin;
    }

    /**
     * @return the jobName
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * @return the results
     */
    public PluginResults getResults() {
        return results;
    }

    /**
     * creates a new property definitions for all properties which are not
     * mapped to a property definition already.
     * 
     * @param propertyDefinitions
     *            the list of property definitions which are associated with a
     *            new dataset
     */
    public void mapAllUnmappedProperties(PropertyDefinitionList propertyDefinitions) {
        PropertyType propType;
        for (Entry<String, SourcePropertyMapping> e : propertyMappings.entrySet()) {
            if (e.getValue().getPropertyDefiniton() == null) {
                PropertyDefinition propDef = propertyDefinitions.getByTitle(e.getKey());
                if (propDef == null) {
                    propType = (!results.getProbablyNumeric().contains(e.getKey())) ? PropertyType.StringProperty
                            : PropertyType.NumProperty;
                    propDef = new PropertyDefinition(e.getKey(), "", propType, null, true, false);
                    propertyDefinitions.add(propDef);
                }
                e.getValue().setPropertyDefiniton(propDef);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", jobName, plugin.getTitle());
    }

    /**
     * 
     * @return a combobox model, which allows selecting the title property
     */
    public ComboBoxModel getTitlePropertyModel() {
        class TitlePropertyModel extends AbstractListModel implements ComboBoxModel {

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ListModel#getElementAt(int)
             */
            @Override
            public Object getElementAt(int index) {
                return getPropertyName(index);
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ListModel#getSize()
             */
            @Override
            public int getSize() {
                return propertyMappings.size();
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ComboBoxModel#getSelectedItem()
             */
            @Override
            public Object getSelectedItem() {
                return titleProperty;
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
             */
            @Override
            public void setSelectedItem(Object property) {
                assert (propertyMappings.containsKey(property));
                titleProperty = (String) property;
            }
        }

        return new TitlePropertyModel();
    }

    /**
     * 
     * @return a combobox model, which allows setting the title merge strategy
     */
    public ComboBoxModel getTitleMergeStrategyModel() {
        class TitleMergeStrategyModel extends AbstractListModel implements ComboBoxModel {

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ListModel#getElementAt(int)
             */
            @Override
            public Object getElementAt(int index) {
                return Iterables.get(MergeStrategy.getStringStrategies(), index);
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ListModel#getSize()
             */
            @Override
            public int getSize() {
                return MergeStrategy.getStringStrategies().size();
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ComboBoxModel#getSelectedItem()
             */
            @Override
            public Object getSelectedItem() {
                if (titleMergeStrategy == MergeStrategy.NONE)
                    return null;
                return titleMergeStrategy;
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
             */
            @Override
            public void setSelectedItem(Object selected) {
                assert (MergeStrategy.getStringStrategies().contains(selected));
                titleMergeStrategy = (MergeStrategy) selected;
            }

        }
        return new TitleMergeStrategyModel();
    }

    /**
     * @return a combobox model which allows setting the structure merge
     *         strategy
     */
    public ComboBoxModel getStructureMergeStrategyModel() {
        class StructureMergeStrategyModel extends AbstractListModel implements ComboBoxModel {

            private final ImmutableSet<MergeStrategy> validStrategies = Sets.immutableEnumSet(MergeStrategy.OVERWRITE,
                    MergeStrategy.DONT_OVERWRITE);

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ListModel#getElementAt(int)
             */
            @Override
            public Object getElementAt(int index) {
                return Iterables.get(validStrategies, index);
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ListModel#getSize()
             */
            @Override
            public int getSize() {
                return validStrategies.size();
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ComboBoxModel#getSelectedItem()
             */
            @Override
            public Object getSelectedItem() {
                return structureMergeStrategy;
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
             */
            @Override
            public void setSelectedItem(Object strategy) {
                assert (validStrategies.contains(strategy));
                structureMergeStrategy = (MergeStrategy) strategy;
            }

        }
        return new StructureMergeStrategyModel();
    }

    /**
     * 
     * @return a combobox model which allows setting the property used for
     *         merging
     */
    public ComboBoxModel getMergeByModel() {
        class MergeByModel extends AbstractListModel implements ComboBoxModel {

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ListModel#getSize()
             */
            @Override
            public int getSize() {
                return propertyMappings.size() + 1;
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ListModel#getElementAt(int)
             */
            @Override
            public Object getElementAt(int index) {
                if (index == 0) {
                    return null;
                }
                return Iterables.get(propertyMappings.keySet(), index - 1, null);
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ComboBoxModel#setSelectedItem(java.lang.Object)
             */
            @Override
            public void setSelectedItem(Object anItem) {
                sourceMergeBy = anItem != null ? anItem.toString() : null;
            }

            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.ComboBoxModel#getSelectedItem()
             */
            @Override
            public Object getSelectedItem() {
                return sourceMergeBy;
            }
        }
        return new MergeByModel();
    }

    /**
     * String filter
     * 
     * @param titles
     *            all accepted titles
     * @return the {@link Predicate}
     */
    public static Predicate<String> getStringFilterPredicate(final Collection<String> titles) {
        return new Predicate<String>() {
            @Override
            public boolean apply(String input) {
                return titles.contains(input);
            };
        };
    }

    /**
     * A Mapping which describes how one source property should be imported: It
     * specifies the PropertyDefinition the source property should be mapped to,
     * if the property definition is <code>null</code> the source property will
     * not be imported. <br>
     * Furthermore it specifies a merge strategy to determine behavior if a
     * previous property (either from the same source or a preceding source) is
     * mapped to the same property definition. <br>
     * It may also specify a transform function which will be applied to the
     * property values during import.
     * 
     * @author Henning Garus
     * 
     */
    public static class SourcePropertyMapping {

        private PropertyDefinition propertyDefiniton = null;
        private MathFunction transformFunction = null;
        private MergeStrategy mergeStrategy = MergeStrategy.NONE;

        /**
         * Create a new source property mapping
         * 
         * @param propertyDefiniton
         *            the property definiton this mapping maps to, may be
         *            <code>null</code>
         */
        public SourcePropertyMapping(PropertyDefinition propertyDefiniton) {
            this.propertyDefiniton = propertyDefiniton;
        }

        /**
         * @return the propertyDefiniton
         */
        public PropertyDefinition getPropertyDefiniton() {
            return propertyDefiniton;
        }

        /**
         * @return the transformFunction
         */
        public MathFunction getTransformFunction() {
            return transformFunction;
        }

        /**
         * @return the mergeStrategy
         */
        public MergeStrategy getMergeStrategy() {
            return mergeStrategy;
        }

        /**
         * @param propertyDefiniton
         *            the propertyDefiniton to set
         */
        public void setPropertyDefiniton(PropertyDefinition propertyDefiniton) {
            this.propertyDefiniton = propertyDefiniton;
        }

        /**
         * @param transformFunction
         *            the transformFunction to set
         */
        public void setTransformFunction(MathFunction transformFunction) {
            this.transformFunction = transformFunction;
        }

        /**
         * @param mergeStrategy
         *            the mergeStrategy to set
         */
        public void setMergeStrategy(MergeStrategy mergeStrategy) {
            this.mergeStrategy = mergeStrategy;
        }

    }

    /**
     * add a message listener
     * 
     * @param listener
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /**
     * remove a message listener
     * 
     * @param listener
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    private void sendMessage(Message message) {
        for (MessageListener l : messageListeners)
            l.receiveMessage(message);
    }

    @Override
    public void receiveMessage(Message message) {
        // receive message from job
        Message msg = new Message(message.getType(), message.getMoleculeTitle(), message.getPropertyDefinition(), this);
        sendMessage(msg);
    }

    /**
     * @return the number of PropertyMappings
     */
    public int getPropertyMappingSize() {
        return propertyMappings.size();
    }
}
