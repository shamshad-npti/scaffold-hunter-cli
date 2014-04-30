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
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * This implementation of the {@link NNSearch} interface is based on a n x n
 * distance Matrix. It performs a minimum query over all distances for NN
 * search.
 * 
 * Precondition: the {@link Structure}s must not contain any missing value for
 * the propertyVector of the used {@link Linkages}
 * 
 * NN query complexity: O(n)
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class MatrixNNSearch<S extends Structure> extends NNSearch<S> {
    private static Logger logger = LoggerFactory.getLogger(MatrixNNSearch.class);

    protected SymmetricDistanceMatrix<S> matrix;

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
     * @throws ClusteringException
     *             if creation of {@link SymmetricDistanceMatrix} failed
     */
    public MatrixNNSearch(Distance<S> distance, Linkage<S> linkage, Collection<PropertyDefinition> propertyVector,
            Collection<HierarchicalClusterNode<S>> singletons) throws ClusteringException {
        super(distance, linkage);
        Preconditions.checkArgument(singletons.size() > 1);

        matrix = new SymmetricDistanceMatrix<S>(distance, singletons);

        if (linkage.needsProstProcessing()) {
            applyPostProcessing(singletons);
        }
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
    public HierarchicalClusterNode<S> merge(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) {
        logger.trace("Entering merge()");

        logger.debug("node1.title = {}, node2.title = {}", node1.getContent() == null ? null : node1.getContent()
                .getTitle(), node2.getContent() == null ? null : node2.getContent().getTitle());
        logger.debug("size1 = {}, size2 = {}; real dist = {}",
                new Object[] { node1.getClusterSize(), node2.getClusterSize(), matrix.getDist(node1, node2), });

        return matrix.mergeNodes(node1, node2, linkage.getUpdateFormula());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#getNN(edu.udo.scaffoldhunter
     * .model.clustering.HierarchicalClusterNode)
     */
    @Override
    public HierarchicalClusterNode<S> getNN(HierarchicalClusterNode<S> node) {
        Collection<HierarchicalClusterNode<S>> storedNodes = matrix.getStoredNodes();
        HierarchicalClusterNode<S> nn = null;

        for (HierarchicalClusterNode<S> currentNode : storedNodes) {
            // prevent comparison with the same node
            if (!node.equals(currentNode)) {
                if (nn == null || isSmaller(currentNode, nn, node)) {
                    // replace NN if the new one is closer
                    nn = currentNode;
                }
            }
        }

        return nn;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#getNNDist(edu.udo.
     * scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public SimpleEntry<HierarchicalClusterNode<S>, Double> getNNAndDist(HierarchicalClusterNode<S> node)
            throws ClusteringException {
        HierarchicalClusterNode<S> nn = getNN(node);
        if (nn == null) {
            return null;
        }
        return new SimpleEntry<HierarchicalClusterNode<S>, Double>(nn, matrix.getDist(node, nn));
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#size()
     */
    @Override
    public int size() {
        return matrix.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#getCurrentLevelNodes()
     */
    @Override
    public Collection<HierarchicalClusterNode<S>> getCurrentLevelNodes() {
        return matrix.getStoredNodes();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#acceptedLinkages()
     */
    @Override
    public Collection<Linkages> acceptedLinkages() {
        return NNSearchs.MATRIX.acceptedLinkages();
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
        return matrix.getStoredNodes().contains(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#getDist(edu.udo.
     * scaffoldhunter.model.clustering.HierarchicalClusterNode,
     * edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public double getDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) {
        return matrix.getDist(node1, node2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#accpetedDistances()
     */
    @Override
    public Collection<Distances> accpetedDistances() {
        return NNSearchs.MATRIX.acceptedDistances();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#getDefaultConfig()
     */
    @Override
    public NNSearchParameters getDefaultParameters() {
        return NNSearchs.MATRIX.getDefaultParameters();
    }

    /**
     * Compares the distances of two nodes to a third one and in a deterministic
     * way (if the distances are the same, the hashCode is used for comparison).
     * If the distance to node1 is smaller that to node2 it will return true.
     * 
     * @param node1
     *            the fist {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @param compareNode
     *            the {@link HierarchicalClusterNode} to compare with
     * @return if the distance to node1 is smaller that to node2
     */
    protected boolean isSmaller(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2,
            HierarchicalClusterNode<S> compareNode) {
        if (matrix.getDist(node1, compareNode) == matrix.getDist(node2, compareNode)) {
            return node1.hashCode() < node2.hashCode();
        } else {
            return matrix.getDist(node1, compareNode) < matrix.getDist(node2, compareNode);
        }
    }

    /**
     * Apply post processing.
     * 
     * @param singleClusters
     */
    private void applyPostProcessing(Collection<HierarchicalClusterNode<S>> singleClusters) {
        for (Iterator<HierarchicalClusterNode<S>> it1 = singleClusters.iterator(); it1.hasNext();) {
            HierarchicalClusterNode<S> node1 = it1.next();
            /*
             * We want to update (a,b) XOR (b,a) only because they are the same
             * in DistanceMatrix!
             * 
             * This is a terrible hack because Java does not support cloning of
             * iterators.
             */
            boolean beforeIt1 = true;
            for (Iterator<HierarchicalClusterNode<S>> it2 = singleClusters.iterator(); beforeIt1;) {
                HierarchicalClusterNode<S> node2 = it2.next();
                if (node1 != node2) {
                    double dist = matrix.getDist(node1, node2);
                    matrix.setDist(node1, node2, 0.5 * Math.pow(dist, 2));
                } else {
                    beforeIt1 = false;
                }
            }
        }
    }

    /**
     * No Parameters needed
     * 
     * @author Till Schäfer
     */
    public static class MatrixParameters implements NNSearchParameters {
        @Override
        public String toString() {
            return "no parameter";
        }
    }
}
