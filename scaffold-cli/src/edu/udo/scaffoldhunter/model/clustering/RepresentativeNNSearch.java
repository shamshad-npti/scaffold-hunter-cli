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
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * This implementation of the {@link NNSearch} interface performs a cluster
 * comparison by a representative {@link Structure} for each
 * {@link HierarchicalClusterNode}.
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class RepresentativeNNSearch<S extends Structure> extends NNSearch<S> {
    private static Logger logger = LoggerFactory.getLogger(RepresentativeNNSearch.class);

    private HashSet<HierarchicalClusterNode<S>> clusterNodes;

    /**
     * Constructor
     * 
     * @param distance
     *            the {@link Distance} measure
     * @param linkage
     *            the used {@link Linkage}
     * @param singletons
     *            the singleton clusters
     * @throws ClusteringException
     *             if creation of {@link SymmetricDistanceMatrix} failed
     */
    public RepresentativeNNSearch(Distance<S> distance, Linkage<S> linkage,
            Collection<HierarchicalClusterNode<S>> singletons) throws ClusteringException {
        super(distance, linkage);

        Preconditions.checkArgument(singletons.size() > 1);
        Preconditions.checkArgument(linkage.centreBasedLinkage());

        clusterNodes = new HashSet<HierarchicalClusterNode<S>>(singletons);
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
        S mergedContent = linkage.doContentMerge(node1.getContent(), node2.getContent(), node1.getClusterSize(),
                node2.getClusterSize());

        logger.debug("node1.title = {}, node2.title = {}", node1.getContent().getTitle(), node2.getContent().getTitle());
        logger.debug("size1 = {}, size2 = {}; real dist = {}",
                new Object[] { node1.getClusterSize(), node2.getClusterSize(), distance.calcDist(node1, node2) });

        // XXX: we can make this faster if we do not need to recalculate the
        // distance! Nevertheless the O(n) additional Distance calculations
        // compared to the O(n²) overall calculations should not have a
        // significant effect on runtime
        HierarchicalClusterNode<S> mergedNode = new HierarchicalClusterNode<S>(node1, node2,
                linkage.distancePostProcessing(distance.calcDist(node1, node2), node1.getClusterSize(),
                        node2.getClusterSize()), node1.getClusterSize() + node2.getClusterSize(), mergedContent);

        // clean content from children to avoid unnecessary memory consumption
        if (!node1.isLeaf()) {
            node1.setContent(null);
        }
        if (!node2.isLeaf()) {
            node2.setContent(null);
        }

        // update clusterNodes
        clusterNodes.add(mergedNode);
        clusterNodes.remove(node1);
        clusterNodes.remove(node2);

        return mergedNode;
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
        HierarchicalClusterNode<S> nn = null;

        for (HierarchicalClusterNode<S> currentNode : clusterNodes) {
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
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#size()
     */
    @Override
    public int size() {
        return clusterNodes.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#getCurrentLevelNodes()
     */
    @Override
    public Collection<HierarchicalClusterNode<S>> getCurrentLevelNodes() {
        return clusterNodes;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#acceptedLinkages()
     */
    @Override
    public Collection<Linkages> acceptedLinkages() {
        return NNSearchs.REPRESENTATIVE.acceptedLinkages();
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

        Double dist = linkage.distancePostProcessing(distance.calcDist(nn, node), nn.getClusterSize(),
                node.getClusterSize());

        return new SimpleEntry<HierarchicalClusterNode<S>, Double>(nn, dist);
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
        return clusterNodes.contains(node);
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
        return distance.calcDist(node1, node2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#accpetedDistances()
     */
    @Override
    public Collection<Distances> accpetedDistances() {
        return NNSearchs.REPRESENTATIVE.acceptedDistances();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#getDefaultConfig()
     */
    @Override
    public edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters getDefaultParameters() {
        return NNSearchs.REPRESENTATIVE.getDefaultParameters();
    }

    /**
     * Compares two nodes in a deterministic way (if the distances are the same,
     * the hashCode is used for comparison). If the distance to node1 is smaller
     * than to node2 it will return true.
     * 
     * @param node1
     *            the fist {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @param compareNode
     *            the {@link HierarchicalClusterNode} to compare with
     * @return true iff the distance to node1 is smaller than to node2
     * @throws ClusteringException
     */
    private boolean isSmaller(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2,
            HierarchicalClusterNode<S> compareNode) throws ClusteringException {
        // XXX: we can make this faster by storing the old minDist in getNN

        // XXX: we only need to apply post processing this if we do not have a
        // monotonic post processing, maybe some space for optimization
        double dist1 = linkage.distancePostProcessing(distance.calcDist(node1, compareNode), node1.getClusterSize(),
                compareNode.getClusterSize());
        double dist2 = linkage.distancePostProcessing(distance.calcDist(node2, compareNode), node2.getClusterSize(),
                compareNode.getClusterSize());

        if (dist1 == dist2) {
            return node1.hashCode() < node2.hashCode();
        } else {
            return dist1 < dist2;
        }
    }

    /**
     * no parameters
     * 
     * @author Till Schäfer
     */
    public static class RepresentativeParameters implements NNSearchParameters {
        @Override
        public String toString() {
            return "no parameter";
        }
    }
}
