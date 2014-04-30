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
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teneighty.heap.AbstractHeap;
import org.teneighty.heap.BinaryHeap;
import org.teneighty.heap.Heap.Entry;

import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.ProgressListener;
import edu.udo.scaffoldhunter.util.ProgressSupport;

/**
 * Generic Clustering Algorithm introduced by Daniel Müllner in
 * "Modern hierarchical, agglomerative clustering algorithms". It is able to
 * handle Linkages that do not fulfil the reducibility property!
 * 
 * Stat.ML: arXiv:1109.2378v1
 * 
 * @author Till Schäfer
 * @param <S>
 * 
 */
public class GenericClustering<S extends Structure> implements HierarchicalClustering<S> {
    private static Logger logger = LoggerFactory.getLogger(GenericClustering.class);

    private NNSearch<S> nnSearch;

    /**
     * All listeners that will be informed about the progress of the clustering.
     */
    private ProgressSupport<HierarchicalClusterNode<S>> progressListeners = new ProgressSupport<HierarchicalClusterNode<S>>();

    /**
     * Constructor.
     * 
     * Precondition: the {@link Structure}s must not contain any missing Value
     * for the propertyVector of the used {@link Linkages}
     * 
     * @param nnSearch
     *            the nearest neighbour search strategy
     */
    public GenericClustering(NNSearch<S> nnSearch) {
        logger.trace("Entering Constructor");

        this.nnSearch = nnSearch;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.HierarchicalClustering#calc(java
     * .util.Collection)
     */
    @Override
    public HierarchicalClusterNode<S> calc() throws ClusteringException {
        logger.trace("Entering calc");

        // this is useful if the calc() is called twice
        if (nnSearch.size() == 1) {
            return nnSearch.getCurrentLevelNodes().iterator().next();
        }

        /*
         * count of singleton clusters
         */
        int initialSize = nnSearch.size();

        /*
         * Node -> NN(node)
         */
        HashMap<HierarchicalClusterNode<S>, HierarchicalClusterNode<S>> nearestNeighbour = new HashMap<HierarchicalClusterNode<S>, HierarchicalClusterNode<S>>(
                initialSize);

        /*
         * The queue nodes that are ordered by their minDist
         */
        AbstractHeap<Double, HierarchicalClusterNode<S>> Q = new BinaryHeap<Double, HierarchicalClusterNode<S>>();

        /*
         * how much progress in percent is done by one agglomeration step
         */
        float progressStep = 100 / ((float) initialSize);

        // Init Listeners
        progressListeners.setProgressBounds(0, 100);
        progressListeners.setProgressValue(0);

        // Initialise NN relationship for all Nodes and insert into Q
        for (HierarchicalClusterNode<S> node : nnSearch.getCurrentLevelNodes()) {
            initNode(nearestNeighbour, Q, node);
        }

        logger.trace("Finished initialisation");

        // iterate until only one cluster remains
        while (nnSearch.size() > 1) {
            // get data of current min NN
            Entry<Double, HierarchicalClusterNode<S>> minEntry = Q.extractMinimum();
            HierarchicalClusterNode<S> minNode = minEntry.getValue();
            HierarchicalClusterNode<S> nn = nearestNeighbour.get(minNode);

            /*
             * Update Distance if real distance != dist and choose new NN. This
             * is because the recalculation of the distance is delayed until it
             * is really needed and thus saving some distance updates /
             * recalculations
             * 
             * clusternodes.contains(minNode) is checked because removal of nn
             * would require linear time below
             * 
             * clusterNodes.contains(nn) is checked because we can save some NN
             * update operations for all nodes before newMergedNode
             */
            while (!nnSearch.currentLevelContains(nn) || !nnSearch.currentLevelContains(minNode)) {
                logger.trace("recalculate NN and find new minNode");

                if (nnSearch.currentLevelContains(minNode)) {
                    SimpleEntry<HierarchicalClusterNode<S>, Double> newNnAndDist = nnSearch.getNNAndDist(minNode);
                    /*
                     * If we use ForwardNNSearch, the last element has no NN
                     */
                    if (newNnAndDist != null) {
                        HierarchicalClusterNode<S> newNn = newNnAndDist.getKey();
                        assert (newNn != null) : "newNN == null";
                        Double newDist = newNnAndDist.getValue();
                        
                        nearestNeighbour.put(minNode, newNn);
                        Q.insert(newDist, minNode);
                    }
                }

                minEntry = Q.extractMinimum();
                minNode = minEntry.getValue();
                nn = nearestNeighbour.get(minNode);
            }

            // we have found the node with a NN of minimal distance at this
            // point

            // Merge Clusters
            logger.debug("Merge Nodes: {} ---- {}", minNode, nn);
            HierarchicalClusterNode<S> newMergedCluster = nnSearch.merge(minNode, nn);
            logger.debug("New formed cluster: {}", newMergedCluster);

            /*
             * Remove merged nodes from nearestNeighbour
             */
            nearestNeighbour.remove(minNode);
            nearestNeighbour.remove(nn);

            /*
             * Init new Cluster
             */
            initNode(nearestNeighbour, Q, newMergedCluster);

            // Update Progress in %
            float progress = (initialSize - nnSearch.size()) * progressStep;
            progressListeners.setProgressValue((int) progress);
            logger.debug("Clustering Progress: {}", progress);
        }

        logger.trace("Finished calc");
        return nnSearch.getCurrentLevelNodes().iterator().next();
    }

    /**
     * Calculates the NN and dist for node and add it to the Q
     * 
     * @param nearestNeighbour
     *            the nearest neighbour relationship data structure
     * @param Q
     *            the priority queue
     * @param node
     *            the node
     * 
     * @throws ClusteringException
     */
    private void initNode(HashMap<HierarchicalClusterNode<S>, HierarchicalClusterNode<S>> nearestNeighbour,
            AbstractHeap<Double, HierarchicalClusterNode<S>> Q, HierarchicalClusterNode<S> node)
            throws ClusteringException {
        SimpleEntry<HierarchicalClusterNode<S>,Double> nnAndDist = nnSearch.getNNAndDist(node);
        /*
         * If we use ForwardNNSearch, the last element has no NN
         */
        if (nnAndDist != null) {
            HierarchicalClusterNode<S> nn = nnAndDist.getKey();
            nearestNeighbour.put(node, nn);
            Q.insert(nnAndDist.getValue(), node);
        } else {
            logger.debug("nnSearch.getNNAndDist returned null");
        }
    }

    /**
     * Adds a {@link ProgressListener}
     * 
     * @param listener
     *            The {@link ProgressListener}
     */
    @Override
    public void addProgressListener(ProgressListener<HierarchicalClusterNode<S>> listener) {
        logger.trace("Entering addProgressListener");
        progressListeners.addProgressListener(listener);
    }

    /**
     * Remove a {@link ProgressListener}
     * 
     * @param listener
     */
    @Override
    public void removeProgressListener(ProgressListener<HierarchicalClusterNode<S>> listener) {
        logger.trace("Entering removeProgressListener");
        progressListeners.removeProgressListener(listener);
    }
}
