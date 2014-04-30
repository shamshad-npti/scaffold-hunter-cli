/*
 * Scaffold Hunter
 * Copyright (C) 2012 Till Schäfer
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

import java.io.Serializable;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Search strategy for nearest neighbour queries.
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete Structure
 * 
 */
public abstract class NNSearch<S extends Structure> {
    protected Linkage<S> linkage;
    protected Distance<S> distance;

    /**
     * Constructor
     * 
     * @param distance
     *            the {@link Distance} measure
     * @param linkage
     *            the used {@link Linkage}
     * @throws ClusteringException
     *             if creation of {@link SymmetricDistanceMatrix} failed
     */
    public NNSearch(Distance<S> distance, Linkage<S> linkage) throws ClusteringException {
        this.distance = distance;
        this.linkage = linkage;
    }

    /**
     * Merges two {@link HierarchicalClusterNode} to a new
     * {@link HierarchicalClusterNode}.
     * 
     * @param node1
     *            the first {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @return the new formed {@link HierarchicalClusterNode}
     * @throws ClusteringException
     */
    public abstract HierarchicalClusterNode<S> merge(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2)
            throws ClusteringException;

    /**
     * Returns the nearest neighbour of node.
     * 
     * @param node
     *            the {@link HierarchicalClusterNode}
     * @return the nearest neighbour or null if no NN found
     * @throws ClusteringException
     */
    public abstract HierarchicalClusterNode<S> getNN(HierarchicalClusterNode<S> node) throws ClusteringException;

    /**
     * Returns the nearest neighbour of node and its distance value.
     * 
     * @param node
     *            the {@link HierarchicalClusterNode}
     * @return the nearest neighbour and its distance or null if no NN found
     * @throws ClusteringException
     */
    public abstract SimpleEntry<HierarchicalClusterNode<S>, Double> getNNAndDist(HierarchicalClusterNode<S> node)
            throws ClusteringException;

    /**
     * Returns the size of the current merge level
     * {@link HierarchicalClusterNode}s.
     * 
     * @return the size
     */
    public abstract int size();

    /**
     * Returns the current merge level {@link HierarchicalClusterNode}s
     * 
     * Complexity is O(1) or should be mentioned in subclasses otherwise
     * 
     * @return the current merge level {@link HierarchicalClusterNode}s
     */
    public abstract Collection<HierarchicalClusterNode<S>> getCurrentLevelNodes();

    /**
     * Returns if the current merge level contains the node
     * 
     * @param node
     *            the {@link HierarchicalClusterNode}
     * @return if it contains the node
     */
    public abstract boolean currentLevelContains(HierarchicalClusterNode<S> node);

    /**
     * Returns a {@link Collection} of the accepted {@link Linkages}
     * 
     * Complexity is O(1) or should be mentioned in subclasses otherwise
     * 
     * @return the accepted {@link Linkages}
     */
    public abstract Collection<Linkages> acceptedLinkages();

    /**
     * Returns the accepted {@link Distance} measures.
     * 
     * @return the accepted {@link Distances}
     */
    public abstract Collection<Distances> accpetedDistances();

    /**
     * Returns a default {@link NNSearchParameters} object
     * 
     * @return default {@link NNSearchParameters}
     */
    public abstract NNSearchParameters getDefaultParameters();

    /**
     * Calculate the distance betwenn node1 and node2
     * 
     * @param node1
     *            the first {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @return the distance
     * @throws ClusteringException
     */
    public abstract double getDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) throws ClusteringException;

    /**
     * Interface for additional parameters uniquely required by concrete
     * {@link NNSearch} strategies.
     * 
     * The implementing class must be a java bean class
     * 
     * @author Till Schäfer
     */
    public interface NNSearchParameters extends Serializable {
    }
}
