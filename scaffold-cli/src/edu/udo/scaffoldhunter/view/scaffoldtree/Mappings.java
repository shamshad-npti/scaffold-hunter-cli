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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Table;

import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.SwingWorker;
import edu.udo.scaffoldhunter.gui.util.VoidUnaryDBFunction;
import edu.udo.scaffoldhunter.model.MappingType;
import edu.udo.scaffoldhunter.model.VisualFeature;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.SHPredicates;
import edu.udo.scaffoldhunter.model.util.Scaffolds;
import edu.udo.scaffoldhunter.view.scaffoldtree.config.ConfigMapping;
import edu.udo.scaffoldhunter.view.scaffoldtree.config.ConfigMapping.Interval;

/**
 * Manages the mappings from visual features (such as node color) of some
 * <code>VTree</code> to Scaffold properties.
 * 
 * @author Henning Garus
 * 
 */
public class Mappings implements PropertyChangeListener, VNodeListener {

    private static final Logger logger = LoggerFactory.getLogger(Mappings.class);

    private static final double MINIMUM_NODE_SCALE = 0.5;
    private static final double MAXIMUM_NODE_SCALE = 5.0;

    private static final double MINIMUM_EDGE_WIDTH = 1.0;
    private static final double MAXIMUM_EDGE_WIDTH = 15.0;

    private final List<ScaffoldNode> addedNodes = Lists.newArrayList();
    private final Timer waitOnAddedVNode = new Timer(500, new NoMoreVNodes());

    private final LoaderAndApplier loaderAndApplier;

    private Map<VisualFeature, Map<Scaffold, Double>> accumulatedValueCache = Maps.newEnumMap(VisualFeature.class);

    // XXX mapping specific setting?
    private static final int ROUND_LABEL_FACTOR = 100000;

    private final ScaffoldTreeView view;
    private final VTree vtree;
    private Subset subset;
    private final Map<VisualFeature, VisualMapping> mappings = Maps.newEnumMap(VisualFeature.class);

    /**
     * Create a new Mappings Object.
     * 
     * @param view
     *            the scaffold tree view
     * @param canvas
     *            the canvas for which mappings should be managed
     * @param subset
     *            the current subset.
     */
    public Mappings(ScaffoldTreeView view, VCanvas canvas, Subset subset) {
        this.view = view;
        this.vtree = canvas.getVTree();
        this.subset = subset;
        this.loaderAndApplier = new LoaderAndApplier();
        this.loaderAndApplier.execute();
        waitOnAddedVNode.setRepeats(false);
        vtree.addVNodeListener(this);
    }

    /**
     * Add mappings for several visual features. If no mapping is provided for a
     * feature the current mapping of that feature will be disabled.
     * 
     * @param newMappings
     *            the mappings to be set
     */
    public void addMappings(Map<VisualFeature, ConfigMapping> newMappings) {
        for (VisualFeature feature : VisualFeature.values()) {
            if (newMappings.get(feature) == null) {
            } else {
                assert feature == newMappings.get(feature).getVisualFeature();
            }
        }
        List<ScaffoldNode> vnodes = ImmutableList.copyOf(vtree.getVNodes());
        loaderAndApplier.addJob(new MappingJob(vnodes, newMappings));
    }

    /**
     * Disable the mapping to <code>feature</code>.
     * 
     * @param feature
     *            the visualFeature whose appearance is reset to default
     */
    public void disableMapping(VisualFeature feature) {
        logger.debug("disabling mapping for {}", feature);
        VisualMapping mapping = mappings.get(feature);
        if (mapping != null) {
            mapping.disable(vtree);
        }
    }

    /**
     * Helper class to apply some mapping to a vnode or a vtree
     */
    private abstract class VisualMapping {

        protected final ConfigMapping mapping;
        protected final PropertyDefinition propertyDefiniton;

        /**
         * Create a new VisualMapping as specified by <code>mapping</code>
         * 
         * @param mapping
         * @param dataset
         */
        VisualMapping(ConfigMapping mapping, Dataset dataset) {
            this.mapping = mapping;
            this.propertyDefiniton = mapping.getProperty(dataset);
        }

        /**
         * Obtain the num property value specified by the <code>Mapping</code>
         * which is backed by this visualMapping.
         * 
         * @param node
         *            the value will be obtained from the scaffold of this node
         * @return the value of the property specified by this mapping
         * @throws DatabaseException
         */
        protected double getPropertyValue(VNode node) {
            if (!(node instanceof ScaffoldNode)) {
                return Double.NaN;
            }
            ScaffoldNode scafNode = (ScaffoldNode)node;
            Double d;
            if (propertyDefiniton.isScaffoldProperty()) {
                if (mapping.isCumulative()) {
                    d = accumulatedValueCache.get(mapping.getVisualFeature()).get(scafNode.getScaffold());
                } else {
                    d = scafNode.getScaffold().getNumPropertyValue(propertyDefiniton);
                }
            } else {
                d = accumulatedValueCache.get(mapping.getVisualFeature()).get(scafNode.getScaffold());
            }
            return d == null ? Double.NaN : d;
        }

        /**
         * apply this mapping to a VNode.h
         * 
         * @param node
         *            the vnode to which this mapping is applied
         * @throws DatabaseException
         */
        abstract void apply(ScaffoldNode node);

        /**
         * Disable this mapping at <code>node</code>
         * 
         * @param node
         *            the vnode where this mapping is disabled.
         */
        abstract void disable(ScaffoldNode node);

        /**
         * Disable this mapping on the whole tree.
         * 
         * @param vtree
         *            the vtree for whose vnodes this mapping is disabled
         */
        protected void disable(VTree vtree) {
            for (ScaffoldNode node : vtree.getVNodes())
                disable(node);
        }

    }

    private abstract class NodeSize extends VisualMapping {

        NodeSize(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
        }

        @Override
        void disable(ScaffoldNode node) {
            node.setScale(1);
        }
    }

    private abstract class NodeBackgroundColor extends VisualMapping {

        NodeBackgroundColor(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
        }

        @Override
        void disable(ScaffoldNode node) {
            node.setColor(null);
        }

    }

    private abstract class EdgeBroadness extends VisualMapping {

        
        EdgeBroadness(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
        }

        @Override
        void disable(ScaffoldNode node) {
            VEdge e = node.getParentEdge();
            if (e != null)
                e.setEdgeWidth(null);
        }
        
        double difference(ScaffoldNode node) {
            if (node.getParent() == null) {
                return Double.NaN;
            }
            double parentValue = getPropertyValue(node.getTreeParent());
            double value = getPropertyValue(node);
            return Math.abs(parentValue - value);
        }
    }

    private abstract class EdgeColor extends VisualMapping {

        EdgeColor(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
        }

        @Override
        void disable(ScaffoldNode node) {
            node.getParentEdge().uncolor();
        }

    }

    private class NodeSizeContinuous extends NodeSize {

        private double min;
        private double factor;

        /**
         * @param mapping
         */
        NodeSizeContinuous(Subset subset, ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
            if (propertyDefiniton.isStringProperty()) {
                throw new IllegalArgumentException();
            }
            this.min = mapping.getMinimumPropertyValue();

            this.factor = (MAXIMUM_NODE_SCALE - MINIMUM_NODE_SCALE) / (mapping.getMaximumPropertyValue() - min);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#apply
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void apply(ScaffoldNode node) {
            if (!propertyDefiniton.isScaffoldProperty() && node.getScaffold().getMolecules().isEmpty())
                return;

            double value = getPropertyValue(node);
            if (Double.isNaN(value)) {
                node.scale(MINIMUM_NODE_SCALE / 2);
            } else {
                double scale = (value - min) * factor + MINIMUM_NODE_SCALE;
                if (!mapping.isGradientAscending()) {
                    scale = MAXIMUM_NODE_SCALE - scale + MINIMUM_NODE_SCALE;
                }
                node.setScale(scale);
            }
        }

    }

    private class NodeSizeIntervals extends NodeSize {

        private final double intervalSize;

        /**
         * @param mapping
         */
        NodeSizeIntervals(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
            intervalSize = MAXIMUM_NODE_SCALE / mapping.getIntervals().size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#apply
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void apply(ScaffoldNode node) {
            if (propertyDefiniton.isStringProperty())
                throw new IllegalArgumentException();

            double value = getPropertyValue(node);
            if (Double.isNaN(value)) {
                node.setScale(MINIMUM_NODE_SCALE / 2);
            } else {
                // KK This should always be consistent with the handling in
                // ConfigMapping getColor() and the Infobar mapping
                int i = 0;
                for (Interval interval : Lists.reverse(mapping.getIntervals())) {
                    if (interval.getLowerBound() <= value)
                        break;
                    ++i;
                }

                node.setScale(MAXIMUM_NODE_SCALE - i * intervalSize);
            }
        }

    }

    private class BackgroundColorContinuous extends NodeBackgroundColor {

        private double min;
        private double interval;

        /**
         * @param mapping
         */
        BackgroundColorContinuous(Subset subset, ConfigMapping mapping) {
            super(mapping, subset.getSession().getDataset());
            this.min = mapping.getMinimumPropertyValue();
            this.interval = mapping.getMaximumPropertyValue() - min;

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#apply
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void apply(ScaffoldNode node) {
            if (propertyDefiniton.isStringProperty())
                throw new IllegalArgumentException();

            double value = getPropertyValue(node);
            if (Double.isNaN(value)) {
                disable(node);
                return;
            }
            double index = (value - min) / interval;
            if (!mapping.isGradientAscending())
                index = 1 - index;

            node.setColor(mapping.getColor(index));

        }

    }

    private class BackgroundColorInterval extends NodeBackgroundColor {

        BackgroundColorInterval(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#apply
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void apply(ScaffoldNode node) {
            double value = getPropertyValue(node);
            if (Double.isNaN(value))
                return;
            node.setColor(mapping.getColor(value));
        }
    }

    private class EdgeBroadnessContinuous extends EdgeBroadness {

        final double factor;

        public EdgeBroadnessContinuous(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
            Preconditions.checkArgument(!propertyDefiniton.isStringProperty());
            double max = Math.abs(mapping.getMaximumPropertyValue() - mapping.getMinimumPropertyValue());
            factor = (MAXIMUM_EDGE_WIDTH - MINIMUM_EDGE_WIDTH) / max;
        }

        @Override
        void apply(ScaffoldNode node) {
            VEdge e = node.getParentEdge();
            if (e != null) {
                double difference = difference(node);
                if (Double.isNaN(difference)) {
                    e.setEdgeWidth(MINIMUM_EDGE_WIDTH / 2);
                } else {
                    double width = difference * factor + MINIMUM_EDGE_WIDTH;
                    if (!mapping.isGradientAscending()) {
                        width = MAXIMUM_EDGE_WIDTH - width + MINIMUM_EDGE_WIDTH;
                    }
                    e.setEdgeWidth(width);
                }
            }
        }
    }
    
    private class EdgeBroadnessInterval extends EdgeBroadness {
       
        private final double intervalWidth;
       
        EdgeBroadnessInterval(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
            Preconditions.checkArgument(!propertyDefiniton.isStringProperty());
            intervalWidth = (MAXIMUM_EDGE_WIDTH - MINIMUM_EDGE_WIDTH) / (mapping.getIntervals().size() - 1);
        }
        
        @Override
        void apply(ScaffoldNode node) {
            VEdge e = node.getParentEdge();
            if (e != null) {
                double difference = difference(node);
                if (Double.isNaN(difference)) {
                    e.setEdgeWidth(MINIMUM_EDGE_WIDTH / 2);
                } else {
                    int i = 0;
                    for (Interval interval : Lists.reverse(mapping.getIntervals())) {
                        if (difference >= interval.getLowerBound()) {
                            break;
                        }
                        i++;
                    }
                    e.setEdgeWidth(MAXIMUM_EDGE_WIDTH - i * intervalWidth);
                }
            }
        }
    }

    private class EdgeColorContinuous extends EdgeColor {

        private final double interval;

        /**
         * @param mapping
         */
        EdgeColorContinuous(Subset subset, ConfigMapping mapping) {
            super(mapping, subset.getSession().getDataset());

            this.interval = mapping.getMaximumPropertyValue() - mapping.getMinimumPropertyValue();

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#apply
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void apply(ScaffoldNode node) {
            if (propertyDefiniton.isStringProperty())
                throw new IllegalArgumentException();

            if (node.getParentEdge() == null)
                return;

            double min = mapping.getMinimumPropertyValue();
            double value = getPropertyValue(node);
            if (Double.isNaN(value)) {
                return;
            }
            double index = (value - min) / interval;
            if (mapping.isGradientAscending())
                index = 1 - index;
            Color childColor = mapping.getColor(index);

            value = getPropertyValue(node.getTreeParent());
            index = (value - min) / interval;
            if (mapping.isGradientAscending())
                index = 1 - index;
            node.getParentEdge().setColors(mapping.getColor(index), childColor);

        }

    }

    private class EdgeColorInterval extends EdgeColor {

        EdgeColorInterval(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#apply
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void apply(ScaffoldNode node) {
            if (node.getParentEdge() == null)
                return;

            double value = getPropertyValue(node);
            if (Double.isNaN(value)) {
                disable(node);
                return;
            }
            Color childColor = mapping.getColor(value);
            value = getPropertyValue(node.getTreeParent());
            node.getParentEdge().setColors(mapping.getColor(value), childColor);
        }
    }

    private class LabelMapping extends VisualMapping {

        /**
         * @param mapping
         */
        LabelMapping(ConfigMapping mapping, Dataset dataset) {
            super(mapping, dataset);
            if (propertyDefiniton.isStringProperty() && !propertyDefiniton.isScaffoldProperty())
                throw new IllegalArgumentException();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#apply
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void apply(ScaffoldNode node) {
            if (propertyDefiniton.isStringProperty())
                node.showLabel(node.getScaffold().getStringPropertyValue(propertyDefiniton));
            else {
                double value = getPropertyValue(node);
                if (Double.isNaN(value)) {
                    disable(node);
                    return;
                }
                value = Math.round(value * ROUND_LABEL_FACTOR);
                value /= ROUND_LABEL_FACTOR;
                node.showLabel(String.valueOf(value));
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#disable
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void disable(ScaffoldNode node) {
            node.removeLabel();
        }

    }

    private class InfobarMapping extends VisualMapping {

        private Map<Scaffold, List<Integer>> distributions = null;
        private Table<Scaffold, String, Integer> stringValueDistribution;

        InfobarMapping(ConfigMapping mapping, Dataset dataset, Table<Scaffold, String, Integer> stringValueDistribution) {
            super(mapping, dataset);
            this.stringValueDistribution = stringValueDistribution;
            if (propertyDefiniton.isScaffoldProperty())
                throw new IllegalArgumentException();
            if (propertyDefiniton.isStringProperty()) {
            } else {
                distributions = Scaffolds.getNumValueDistribution(vtree.getRoot().getScaffold(), mapping,
                        propertyDefiniton, mapping.isCumulative());
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#apply
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void apply(ScaffoldNode node) {
            List<PropertyBinValue> propBinVals;
            if (propertyDefiniton.isStringProperty()) {
                propBinVals = Lists.newArrayListWithCapacity(mapping.getIntervals().size());
                Map<String, Integer> row = stringValueDistribution.row(node.getScaffold());
                for (Interval interval : mapping.getIntervals()) {
                    Integer i = row.get(interval.getString());
                    if (i != null && i > 0)
                        propBinVals.add(new PropertyBinValue(i, interval.getColor()));
                }
            } else {
                List<Integer> dist = distributions.get(node.getScaffold());
                propBinVals = Lists.newArrayListWithCapacity(dist.size());
                int i = 0;
                for (Interval interval : mapping.getIntervals()) {
                    int value = dist.get(i++);
                    if (value > 0) {
                        propBinVals.add(new PropertyBinValue(value, interval.getColor()));
                    }
                }
            }
            if (!propBinVals.isEmpty()) {
                node.showInfoBar(propBinVals);
                vtree.setLayoutInvalid(true);
            }
            else {
                node.removeInfoBar();
                vtree.setLayoutInvalid(true);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.scaffoldtree.Mappings.VisualMapping#disable
         * (edu.udo.scaffoldhunter.view.scaffoldtree.VNode)
         */
        @Override
        void disable(ScaffoldNode node) {
            node.removeInfoBar();
            vtree.setLayoutInvalid(true);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        logger.trace("property change recieved");
        Map<VisualFeature, ConfigMapping> mappings = ((ScaffoldTreeViewConfig) evt.getNewValue()).getMappings();
        DBExceptionHandler.callDBManager(view.getDbManager(),
                new VoidUnaryDBFunction<Map<VisualFeature, ConfigMapping>>(mappings) {
            @Override
            public void call(Map<VisualFeature, ConfigMapping> mappings) throws DatabaseException {
                addMappings(mappings);
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.scaffoldtree.VNodeListener#vnodeAdded(edu
     * .udo.scaffoldhunter.view.scaffoldtree.VNode)
     */
    @Override
    public void vnodeAdded(ScaffoldNode vnode) {
        addedNodes.add(vnode);
        waitOnAddedVNode.restart();
    }

    private class NoMoreVNodes implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            loaderAndApplier.addJob(new MappingJob(addedNodes, view.getInstanceConfig().getMappings()));
            addedNodes.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.scaffoldtree.VNodeListener#vnodeRemoved(edu
     * .udo.scaffoldhunter.view.scaffoldtree.VNode)
     */
    @Override
    public void vnodeRemoved(ScaffoldNode vnode) {
        // do nothing
    }

    private class LoaderAndApplier extends SwingWorker<Void, MappingJob> {

        private final BlockingQueue<MappingJob> jobs = new LinkedBlockingQueue<MappingJob>();

        private Map<VisualFeature, VisualMapping> mappings = Maps.newEnumMap(VisualFeature.class);

        private final DbManager db;
        private final Dataset dataset;

        public LoaderAndApplier() {
            dataset = view.getSubset().getSession().getDataset();
            db = view.getDbManager();
        }

        public void addJob(MappingJob job) {
            jobs.add(job);
        }

        @Override
        protected Void doInBackground() throws Exception {
            try {
                List<PropertyDefinition> loadedProperties = Lists.newArrayList();
                List<PropertyDefinition> cumulativeScaffoldProperties = Lists.newArrayList();
                Map<VisualFeature, ConfigMapping> derivedValueMappings = Maps.newEnumMap(VisualFeature.class);
                for (;;) {
                    loadedProperties.clear();
                    cumulativeScaffoldProperties.clear();
                    derivedValueMappings.clear();
                    MappingJob job = jobs.take();
                    if (vtree.getRoot() == null)
                        continue;

                    for (ConfigMapping mapping : job.getMappings().values()) {
                        PropertyDefinition propDef = mapping.getProperty(dataset);

                        if (propDef.isScaffoldProperty()) {
                            if (mapping.isCumulative()) {
                                cumulativeScaffoldProperties.add(propDef);
                            } else {
                                loadedProperties.add(propDef);
                            }
                        } else {
                            if (propDef.isStringProperty()) {
                                assert mapping.getVisualFeature().equals(VisualFeature.InfoBar);
                                try {
                                    job.setStringDistribution(view.getDbManager().getStringDistribution(
                                            vtree.getRoot().getScaffold(), view.getSubset(), propDef));
                                } catch (DatabaseException e) {
                                    logger.warn("failed to load String Distribution", e);
                                    job.getMappings().remove(VisualFeature.InfoBar);
                                }
                            } else {
                                if (mapping.getVisualFeature().equals(VisualFeature.InfoBar)) {
                                    loadedProperties.add(propDef);
                                } else {
                                    derivedValueMappings.put(mapping.getVisualFeature(), mapping);
                                }
                            }
                        }
                    }

                    Iterable<Scaffold> allScaffolds = Scaffolds.getSubtreePreorderIterable(vtree.getRoot()
                            .getScaffold());
                    Iterable<Molecule> allMolecules = Scaffolds.getMolecules(allScaffolds);
                    try {
                        db.lockAndLoad(loadedProperties, Iterables.concat(allScaffolds, allMolecules));
                    } catch (DatabaseException e) {
                        logger.warn("failed to load scaffold properties", e);
                        for (VisualFeature f : VisualFeature.values()) {
                            if (loadedProperties.contains(job.getMappings().get(f).getProperty(dataset)))
                                job.getMappings().remove(f);
                        }
                    }
                    try {
                        db.lockAndLoad(cumulativeScaffoldProperties, allScaffolds);
                    } catch (DatabaseException e) {
                        logger.warn("failed to load scaffold properties", e);
                        for (ConfigMapping m : job.getMappings().values()) {
                            if (m.isCumulative() && cumulativeScaffoldProperties.contains(m.getProperty(dataset)))
                                job.getMappings().remove(m.getVisualFeature());
                        }
                    }
                    for (ConfigMapping m : job.getMappings().values()) {
                        if (m.isCumulative() && cumulativeScaffoldProperties.contains(m.getProperty(dataset))) {
                            job.getDerivedPropertyValues().put(
                                    m.getVisualFeature(),
                                    Scaffolds.getTreeCumulativeNumValues(vtree.getRoot().getScaffold(),
                                            m.getProperty(dataset), m.getFunction()));
                            db.unlockAndUnload(Collections.singleton(m.getProperty(dataset)), allScaffolds);
                        }
                    }
                    job.addAllToUnload(loadedProperties);
                    for (Entry<VisualFeature, ConfigMapping> e : derivedValueMappings.entrySet()) {
                        try {
                            job.getDerivedPropertyValues().put(
                                    e.getKey(),
                                    view.getDbManager().getAccNumProperties(e.getValue().getProperty(dataset),
                                            e.getValue().getFunction(), view.getSubset(),
                                            vtree.getRoot().getScaffold(), e.getValue().isCumulative()));
                        } catch (DatabaseException ex) {
                            logger.warn("failed to load scaffold properties", ex);
                            job.getMappings().remove(e.getKey());
                        }
                    }
                    publish(job);
                } // end for ever
            } catch (RuntimeException e) {
                logger.error("Exception occured", e);
                throw e;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.gui.util.SwingWorker#process(java.util.List)
         */
        @Override
        protected void process(List<MappingJob> chunks) {
            for (MappingJob job : chunks) {
                accumulatedValueCache = job.getDerivedPropertyValues();
                for (VisualFeature f : VisualFeature.values()) {
                    ConfigMapping mapping = job.getMappings().get(f);
                    if (mapping == null) {
                        VisualMapping v = mappings.get(f);
                        if (v != null) {
                            v.disable(vtree);
                            mappings.remove(f);
                        }
                        continue;
                    }
                    VisualMapping visualMapping = null;
                    Dataset dataset = subset.getSession().getDataset();
                    switch (mapping.getVisualFeature()) {
                    case NodeSize:
                        switch (mapping.getMappingType()) {
                        case Gradient:
                            visualMapping = new NodeSizeContinuous(subset, mapping, dataset);
                            break;
                        case Interval:
                            visualMapping = new NodeSizeIntervals(mapping, dataset);
                            break;
                        }
                        break;
                    case NodeBackgroundColor:
                        switch (mapping.getMappingType()) {
                        case Gradient:
                            visualMapping = new BackgroundColorContinuous(subset, mapping);
                            break;
                        case Interval:
                            visualMapping = new BackgroundColorInterval(mapping, dataset);
                            break;
                        }
                        break;
                    case EdgeThickness:
                        switch (mapping.getMappingType()) {
                        case Gradient:
                            visualMapping = new EdgeBroadnessContinuous(mapping, dataset);
                            break;
                        case Interval:
                            visualMapping = new EdgeBroadnessInterval(mapping, dataset);
                            break;
                        }
                        break;
                    case EdgeColor:
                        switch (mapping.getMappingType()) {
                        case Gradient:
                            visualMapping = new EdgeColorContinuous(subset, mapping);
                            break;
                        case Interval:
                            visualMapping = new EdgeColorInterval(mapping, dataset);
                            break;
                        }
                        break;
                    case Label:
                        visualMapping = new LabelMapping(mapping, dataset);
                        break;
                    case InfoBar:
                        if (mapping.getMappingType() != MappingType.Interval)
                            throw new IllegalArgumentException("Infobars require an interval mapping.");
                        visualMapping = new InfobarMapping(mapping, dataset, job.getStringDistribution());
                    }
                    mappings.put(mapping.getVisualFeature(), visualMapping);
                    for (ScaffoldNode n : job.getNodes()) {
                        visualMapping.apply(n);
                    }

                }
                Iterable<PropertyDefinition> scaffoldProps = Iterables.filter(job.getToUnload(),
                        SHPredicates.IS_SCAFFOLD_PROPDEF);
                Iterable<PropertyDefinition> moleculeProps = Iterables.filter(job.getToUnload(),
                        Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF));
                Iterable<Scaffold> allScaffolds = Scaffolds.getSubtreePreorderIterable(vtree.getRoot()
                        .getScaffold());
                db.unlockAndUnload(scaffoldProps, allScaffolds);
                db.unlockAndUnload(moleculeProps, Scaffolds.getMolecules(allScaffolds));
                accumulatedValueCache.clear();
            }
        }
    }

    private static class MappingJob {

        private final List<ScaffoldNode> nodes;
        private final Map<VisualFeature, ConfigMapping> mappings;
        private Table<Scaffold, String, Integer> stringDistribution;
        private final Map<VisualFeature, Map<Scaffold, Double>> derivedPropertyValues = Maps
                .newEnumMap(VisualFeature.class);
        private final List<PropertyDefinition> toUnload = Lists.newArrayList();

        public MappingJob(Iterable<ScaffoldNode> nodes, Map<VisualFeature, ConfigMapping> mappings) {
            this.nodes = ImmutableList.copyOf(nodes);
            this.mappings = Maps.newEnumMap(mappings);
        }

        /**
         * @return the stringDistribution
         */
        public Table<Scaffold, String, Integer> getStringDistribution() {
            return stringDistribution;
        }

        /**
         * @param stringDistribution
         *            the stringDistribution to set
         */
        public void setStringDistribution(Table<Scaffold, String, Integer> stringDistribution) {
            this.stringDistribution = stringDistribution;
        }

        /**
         * @return the nodes
         */
        public List<ScaffoldNode> getNodes() {
            return nodes;
        }

        /**
         * @return the mappings
         */
        public Map<VisualFeature, ConfigMapping> getMappings() {
            return mappings;
        }

        /**
         * @return the derivedPropertyValues
         */
        public Map<VisualFeature, Map<Scaffold, Double>> getDerivedPropertyValues() {
            return derivedPropertyValues;
        }

        /**
         * @return the toUnload
         */
        public List<PropertyDefinition> getToUnload() {
            return toUnload;
        }

        public void addAllToUnload(Iterable<PropertyDefinition> propDefs) {
            Iterables.addAll(toUnload, propDefs);
        }

    }
}
