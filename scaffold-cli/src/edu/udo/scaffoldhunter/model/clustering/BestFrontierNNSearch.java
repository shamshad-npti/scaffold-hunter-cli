/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * TODO
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete Structure
 * 
 */
public class BestFrontierNNSearch<S extends Structure> extends NNSearch<S> {
    private static Logger logger = LoggerFactory.getLogger(BestFrontierNNSearch.class);

    protected final HashSet<HierarchicalClusterNode<S>> currentLevelNodes;
    protected final PivotTree<S> pTree;
    protected final BestFrontierParameters parameters;

    /**
     * Constructor
     * 
     * @param distance
     *            the {@link Distance} measure
     * @param linkage
     *            the used {@link Linkage}
     * @param propertyVector
     *            the {@link Property}s used for clustering
     * @param singletons
     *            the singleton clusters
     * @param parameters
     *            the
     *            {@link edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters}
     * @throws ClusteringException
     *             if creation of {@link SymmetricDistanceMatrix} failed
     */
    public BestFrontierNNSearch(Distance<S> distance, Linkage<S> linkage,
            Collection<PropertyDefinition> propertyVector, Collection<HierarchicalClusterNode<S>> singletons,
            BestFrontierParameters parameters) throws ClusteringException {
        super(distance, linkage);
        this.parameters = parameters;
        Preconditions.checkArgument(singletons.size() > 1);

        currentLevelNodes = new HashSet<HierarchicalClusterNode<S>>(singletons);

        // XXX switch this to get a deterministic clustering for a fixed subset
        // / dataset
        boolean deterministicTree = false;
        pTree = new PivotTree<S>(new RandomSampler<S>(parameters.branchingFactor, deterministicTree ? (long) 0 : null),
                new MaxSizeLeafSelection<S>(), parameters.leafBound, singletons, distance, deterministicTree);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#getNN(edu.udo.scaffoldhunter
     * .model.clustering.HierarchicalClusterNode)
     */
    @Override
    public HierarchicalClusterNode<S> getNN(HierarchicalClusterNode<S> node) throws ClusteringException {
        Preconditions.checkNotNull(node);

        SimpleEntry<HierarchicalClusterNode<S>, Double> nnEntry = calcNNAndDist(node);
        if (nnEntry == null) {
            return null;
        } else {
            return nnEntry.getKey();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#getNNAndDist(edu.udo
     * .scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public SimpleEntry<HierarchicalClusterNode<S>, Double> getNNAndDist(HierarchicalClusterNode<S> node)
            throws ClusteringException {
        Preconditions.checkNotNull(node);

        return calcNNAndDist(node);
    }

    /**
     * Perform a BestFrontierSearch to find the NN and its estimated distance.
     * 
     * @param node
     *            the node to search the NN for
     * @return the NN and its distance
     */
    protected SimpleEntry<HierarchicalClusterNode<S>, Double> calcNNAndDist(HierarchicalClusterNode<S> node) {
        /*
         * the count of used pivots for each HierarchicalClusterNode
         */
        HashMap<HierarchicalClusterNode<S>, Integer> pivotCount = Maps.newHashMap();
        /*
         * all used pivots
         */
        LinkedList<Pivot<S>> pivots = Lists.newLinkedList();
        /*
         * the counting of HierarchicalClusterNodes in the BestFrontierSearch
         */
        HashMap<HierarchicalClusterNode<S>, Integer> nodeCount = Maps.newHashMap();

        /*
         * whether the frontier search exited by an exact match or by the
         * frontier bound
         */
        boolean exact = false;

        // there is no NN if we have only one or fewer nodes stored
        if (this.size() <= 1) {
            return null;
        }

        for (Collection<Pivot<S>> currentPivots : pTree.getPivots(node)) {
            // add currentPivots to Pivots
            pivots.addAll(currentPivots);
        }

        /*
         * Frontier (PriortityQueue) sorted by the distances to the pivot
         */
        PriorityQueue<FrontierElement> frontier = new PriorityQueue<FrontierElement>(pivots.size() * 2,
                new Comparator<FrontierElement>() {
                    @Override
                    public int compare(FrontierElement o1, FrontierElement o2) {
                        int retval = o1.dist.compareTo(o2.dist);
                        if (retval == 0) {
                            return o1.node.getUniqueID().compareTo(o2.node.getUniqueID());
                        } else {
                            return retval;
                        }
                    }
                });

        /*
         * fill frontier with initial values
         */
        for (Pivot<S> pivot : pivots) {
            Collection<SimpleEntry<HierarchicalClusterNode<S>, Double>> startFrontierEntries = pivot
                    .startNewFrontier(node);

            for (SimpleEntry<HierarchicalClusterNode<S>, Double> frontierEntry : startFrontierEntries) {
                FrontierElement frontierElement = new FrontierElement(pivot, frontierEntry.getKey(),
                        frontierEntry.getValue());
                frontier.add(frontierElement);
            }
        }

        logger.debug("frontier.size={}", frontier.size());

        /*
         * Best-Frontier-Search
         */
        int frontierDepth = 0;
        FrontierElement frontierElement = null;
        double oldDist = Double.NaN;
        while (frontierDepth < parameters.frontierBound) {
            assert frontier.size() > 0;

            frontierElement = frontier.poll();
            incrementCount(nodeCount, frontierElement.node);

            // distances must be monotonically increasing
            assert Double.isNaN(oldDist) || oldDist <= frontierElement.dist;
            // only update oldDist if assert is enabled
            assert (oldDist = frontierElement.dist) != Double.NaN;

            /*
             * We have found the NN if a HierarchicalClusterNode is found as
             * often as there are common pivots. There will be no smaller
             * distance with higher count in feature.
             */
            Integer currentCount = nodeCount.get(frontierElement.node);
            int commonPivotCount = commonPivotCount(pivotCount, node, frontierElement.node);
            if (currentCount == commonPivotCount) {
                exact = true;
                break;
            }

            // push frontier
            SimpleEntry<HierarchicalClusterNode<S>, Double> newFrontierEntry = frontierElement.pivot
                    .pushFrontier(frontierElement.node);
            if (newFrontierEntry != null) {
                frontier.add(new FrontierElement(frontierElement.pivot, newFrontierEntry.getKey(), newFrontierEntry
                        .getValue()));
            }

            frontierDepth++;
        }

        if (!exact) {
            /*
             * We found no exact NN. Guess entry with highest count in queue
             */
            FrontierElement maxEntry = null;
            int maxCount = Integer.MIN_VALUE;
            for (FrontierElement entry : frontier) {
                int count = nodeCount.containsKey(entry.node) ? nodeCount.get(entry.node) : 0;
                if (count > maxCount) {
                    maxEntry = entry;
                    maxCount = count;
                }
            }
            return new SimpleEntry<HierarchicalClusterNode<S>, Double>(maxEntry.node, maxEntry.dist);
        } else {
            return new SimpleEntry<HierarchicalClusterNode<S>, Double>(frontierElement.node, frontierElement.dist);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#size()
     */
    @Override
    public int size() {
        return currentLevelNodes.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#getCurrentLevelNodes()
     */
    @Override
    public Collection<HierarchicalClusterNode<S>> getCurrentLevelNodes() {
        return Collections.unmodifiableCollection(currentLevelNodes);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#currentLevelContains
     * (edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public boolean currentLevelContains(HierarchicalClusterNode<S> node) {
        return currentLevelNodes.contains(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#acceptedLinkages()
     */
    @Override
    public Collection<Linkages> acceptedLinkages() {
        return NNSearchs.BEST_FRONTIER.acceptedLinkages();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#merge(edu.udo.scaffoldhunter
     * .model.clustering.HierarchicalClusterNode,
     * edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public HierarchicalClusterNode<S> merge(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2)
            throws ClusteringException {
        Preconditions.checkArgument(currentLevelContains(node1));
        Preconditions.checkArgument(currentLevelContains(node2));

        HierarchicalClusterNode<S> mergedNode = pTree.merge(node1, node2, linkage.getUpdateFormula(),
                pTree.estimatedDistance(node1, node2));

        currentLevelNodes.remove(node1);
        currentLevelNodes.remove(node2);
        currentLevelNodes.add(mergedNode);

        return mergedNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#getDist(edu.udo.
     * scaffoldhunter.model.clustering.HierarchicalClusterNode,
     * edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public double getDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2)
            throws ClusteringException {
        return pTree.estimatedDistance(node1, node2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#accpetedDistances()
     */
    @Override
    public Collection<Distances> accpetedDistances() {
        return NNSearchs.BEST_FRONTIER.acceptedDistances();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#getDefaultConfig()
     */
    @Override
    public NNSearchParameters getDefaultParameters() {
        return NNSearchs.BEST_FRONTIER.getDefaultParameters();
    }

    /**
     * Wrapper around {@link PivotTree#commonPivotCount} to avoid
     * recalculations. Calculates the number of common {@link Pivot}s.
     * 
     * @param pivotCount
     *            the {@link HashMap} in which the previously calculated values
     *            are stored (like a cache)
     * @param node
     *            the first {@link HierarchicalClusterNode}
     * @param indexNode
     *            the second {@link HierarchicalClusterNode}, which is also the
     *            index node in pivotCount
     * @return the number of common Pivots
     */
    protected int commonPivotCount(HashMap<HierarchicalClusterNode<S>, Integer> pivotCount,
            HierarchicalClusterNode<S> node, HierarchicalClusterNode<S> indexNode) {
        Integer retVal = pivotCount.get(indexNode);
        if (retVal == null) {
            retVal = pTree.commonPivotCount(node, indexNode);
            pivotCount.put(indexNode, retVal);
        }
        return retVal;
    }

    /**
     * Increments the count of {@link HierarchicalClusterNode}.
     * 
     * @param map
     *            the count map
     * @param o
     *            the map key to count
     */
    protected static <E> void incrementCount(HashMap<E, Integer> map, E o) {
        Integer val = map.get(o);
        if (val != null) {
            map.put(o, val + 1);
        } else {
            map.put(o, 1);
        }
    }

    protected class FrontierElement {
        protected final Pivot<S> pivot;
        protected final HierarchicalClusterNode<S> node;
        protected final Double dist;

        public FrontierElement(Pivot<S> pivot, HierarchicalClusterNode<S> node, Double dist) {
            this.pivot = pivot;
            this.node = node;
            this.dist = dist;
        }
    }

    /**
     * The
     * {@link edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters}
     * 
     * @author Till Schäfer
     */
    public static class BestFrontierParameters implements NNSearchParameters, Cloneable {
        protected int branchingFactor;
        protected int frontierBound;
        protected int leafBound;

        /**
         * Default Constructor
         */
        public BestFrontierParameters() {
            branchingFactor = 20;
            frontierBound = Integer.MAX_VALUE;
            leafBound = 1;
        }

        /**
         * Constructor
         * 
         * @param branchingFactor
         * @param frontierBound
         * @param leafBound
         */
        public BestFrontierParameters(int branchingFactor, int frontierBound, int leafBound) {
            this.branchingFactor = branchingFactor;
            this.frontierBound = frontierBound;
            this.leafBound = leafBound;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("branching factor = ");
            builder.append(branchingFactor);
            builder.append("; frontier bound = ");
            builder.append(frontierBound);
            builder.append("; leaf bound = ");
            builder.append(leafBound);
            return builder.toString();
        }

        /**
         * same as toString() but formatted for filenames (no special characters
         * or whitespace)
         * 
         * @return descriptions of parameter values
         */
        public String toFileName() {
            StringBuilder builder = new StringBuilder();
            builder.append("branchingfactor=");
            builder.append(branchingFactor);
            builder.append("_frontierbound=");
            builder.append(frontierBound);
            builder.append("_leaf bound=");
            builder.append(leafBound);
            return builder.toString();
        }

        /**
         * @return the branchingFactor
         */
        public int getBranchingFactor() {
            return branchingFactor;
        }

        /**
         * @param branchingFactor
         *            the branchingFactor to set
         */
        public void setBranchingFactor(int branchingFactor) {
            this.branchingFactor = branchingFactor;
        }

        /**
         * @return the frontierBound
         */
        public int getFrontierBound() {
            return frontierBound;
        }

        /**
         * @param frontierBound
         *            the frontierBound to set
         */
        public void setFrontierBound(int frontierBound) {
            this.frontierBound = frontierBound;
        }

        /**
         * @return the leafBound
         */
        public int getLeafBound() {
            return leafBound;
        }

        /**
         * @param leafBound
         *            the leafBound to set
         */
        public void setLeafBound(int leafBound) {
            this.leafBound = leafBound;
        }

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}
