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

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.NoSuchElementException;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * A {@link Pivot} is a {@link Structure} which is used to estimate the
 * {@link Distance} between two other {@link Structure}s
 * 
 * @author Till Schäfer
 * 
 * @param <S>
 *            the concrete {@link Structure}
 */
public class Pivot<S extends Structure> {
    private final HierarchicalClusterNode<S> pivot;
    private final Distance<S> distance;
    private DistanceList<S> distances = new TreeSetDistanceList<S>();

    /**
     * Constructor
     * 
     * @param pivot
     *            the {@link HierarchicalClusterNode} used as pivot element
     * @param distance
     *            the used {@link Distance} measure
     * @param nodes
     *            the nodes that belong to the pivot
     * @throws ClusteringException
     *             if the distance calculation fails
     */
    public Pivot(HierarchicalClusterNode<S> pivot, Distance<S> distance, Collection<HierarchicalClusterNode<S>> nodes)
            throws ClusteringException {
        Preconditions.checkNotNull(pivot);
        Preconditions.checkNotNull(distance);
        Preconditions.checkNotNull(nodes);

        this.pivot = pivot;
        this.distance = distance;

        // calculate the distances to the pivot
        for (HierarchicalClusterNode<S> node : nodes) {
            add(node);
        }
    }

    /**
     * Returns the exact {@link Distance} to the {@link Pivot}
     * 
     * @param node
     *            the {@link HierarchicalClusterNode} to calculate the
     *            {@link Distance} to
     * @return the {@link Distance}
     * @throws NoSuchElementException
     *             if node is not stored
     */
    public Double getDist(HierarchicalClusterNode<S> node) {
        Preconditions.checkNotNull(node);

        return distances.getDistance(node);
    }

    /**
     * Returns a lower bound of the {@link Distance} between node1 and node2 by
     * using the triangle inequality for metric {@link Distance}s.
     * 
     * d(node1, node2) >= |d(node1, pivot) - d(node2, pivot)|
     * 
     * @param node1
     *            the first node
     * @param node2
     *            the second node
     * @return a lower bound for the Distance
     * @throws NoSuchElementException
     *             if node1 or node2 is not stored
     */
    public Double getLowerDistanceBound(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) {
        Preconditions.checkNotNull(node1);
        Preconditions.checkNotNull(node2);

        return Math.abs(distances.getDistance(node1) - distances.getDistance(node2));
    }

    /**
     * Starts a frontier in both directions if the distances list.
     * 
     * @param node
     *            the node to start the frontier from
     * 
     * @return all {@link SimpleEntry}s which are neighbours of node in the
     *         ordering. The key of the {@link SimpleEntry} is the frontier
     *         {@link HierarchicalClusterNode} and the value is the lower bound
     *         of the distance to the start node (parameter) by triangle
     *         inequality. triangle lower bound))
     * @throws NoSuchElementException
     *             if node is not stored
     */
    public Collection<SimpleEntry<HierarchicalClusterNode<S>, Double>> startNewFrontier(HierarchicalClusterNode<S> node) {
        Preconditions.checkNotNull(node);

        Collection<SimpleEntry<HierarchicalClusterNode<S>, Double>> retVal = new ArrayList<SimpleEntry<HierarchicalClusterNode<S>, Double>>(
                2) {
        };

        for (HierarchicalClusterNode<S> frontierNode : distances.startNewFrontier(node)) {
            retVal.add(new SimpleEntry<HierarchicalClusterNode<S>, Double>(frontierNode, getLowerDistanceBound(
                    distances.getFrontierStartNode(), frontierNode)));
        }

        return retVal;
    }

    /**
     * Pushes the Frontier on further away from the node where it started.
     * 
     * @param node
     *            the last Frontier {@link HierarchicalClusterNode} (to
     *            determine the direction in which the Frontier should be
     *            pushed)
     * @return the new Frontier {@link HierarchicalClusterNode} with its
     *         distance to the start node (see startNewFrontier()) or null if
     *         the frontier reached its end.
     * @throws IllegalStateException
     *             if the node is no frontier {@link HierarchicalClusterNode}
     * @throws NoSuchElementException
     *             if node is not stored
     */
    public SimpleEntry<HierarchicalClusterNode<S>, Double> pushFrontier(HierarchicalClusterNode<S> node) {
        HierarchicalClusterNode<S> frontierNode = distances.pushFrontier(node);

        if (frontierNode == null) {
            return null;
        } else {
            return new SimpleEntry<HierarchicalClusterNode<S>, Double>(frontierNode, getLowerDistanceBound(
                    distances.getFrontierStartNode(), frontierNode));
        }
    }

    /**
     * Number of {@link HierarchicalClusterNode}s that belong to the
     * {@link Pivot}
     * 
     * @return the size
     */
    public int size() {
        return distances.size();
    }

    /**
     * Removes a {@link HierarchicalClusterNode} from the {@link Pivot}.
     * 
     * @param node
     *            the {@link HierarchicalClusterNode} to remove
     */
    public void remove(HierarchicalClusterNode<S> node) {
        Preconditions.checkNotNull(node);

        distances.remove(node);
    }

    /**
     * Add a new {@link HierarchicalClusterNode} to the Pivot (calculates the
     * distance based on the the contend of {@link HierarchicalClusterNode})
     * 
     * @param node
     *            the {@link HierarchicalClusterNode} to add
     * @throws ClusteringException
     *             if the distance calculation fails
     */
    public void add(HierarchicalClusterNode<S> node) throws ClusteringException {
        Preconditions.checkNotNull(node);

        double dist = distance.calcDist(node, pivot);
        assert !Double.isNaN(dist);
        distances.add(node, dist);
    }

    /**
     * Add a new {@link HierarchicalClusterNode} to the Pivot
     * 
     * @param node
     *            the {@link HierarchicalClusterNode} to add
     * @param dist
     *            the distance to the {@link Pivot}
     */
    public void add(HierarchicalClusterNode<S> node, double dist) {
        Preconditions.checkNotNull(node);

        distances.add(node, dist);
    }

    /**
     * Returns an <b>unmodifiable</b> {@link Collection} of all stored
     * {@link HierarchicalClusterNode}s.
     * 
     * @return <b>unmodifiable</b> {@link Collection} of all stored
     *         {@link HierarchicalClusterNode}s
     */
    public Collection<HierarchicalClusterNode<S>> getAllNodes() {
        return distances.getAllNodes();
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Pivot: ");
        builder.append(pivot.getContent().getTitle());
        builder.append(System.getProperty("line.separator").toString());
        builder.append("-----");
        builder.append(System.getProperty("line.separator").toString());
        
        builder.append(distances.toString());
        
        return builder.toString(); 
    }
}
