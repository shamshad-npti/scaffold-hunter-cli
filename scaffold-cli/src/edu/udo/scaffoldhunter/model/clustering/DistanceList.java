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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.Collection;
import java.util.NoSuchElementException;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * This Interface describes a data structure that allows to store distances and
 * for a set of {@link HierarchicalClusterNode}s to one other
 * {@link HierarchicalClusterNode}. It provides the needed methods to do a
 * Best-Frontier-Search (i.e. the possibility to iterate from a given element in
 * both directions of the list).
 * 
 * Best-Frontier-Search description:
 * 
 * Jianjun Zhou - Efficiently searching and mining biological sequence and
 * structure data, Chapter 3.4.3 (PhD. Thesis, University of Alberta)
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public interface DistanceList<S extends Structure> {
    /**
     * Adds a new {@link HierarchicalClusterNode}.
     * 
     * Note that it is allowed to store each element only once!
     * 
     * @param node
     *            the {@link HierarchicalClusterNode}
     * @param distance
     *            the distance
     * @throws DoubleEntryException
     *             if the node is already stored
     */
    public void add(HierarchicalClusterNode<S> node, double distance);

    /**
     * Removes a {@link HierarchicalClusterNode}.
     * 
     * @param node
     *            the {@link HierarchicalClusterNode}
     * @throws NoSuchElementException
     *             if node is not stored
     */
    public void remove(HierarchicalClusterNode<S> node);

    /**
     * Returns the distance of the {@link HierarchicalClusterNode}
     * 
     * @param node
     *            the {@link HierarchicalClusterNode}
     * @return the distance
     * @throws NoSuchElementException
     *             if node is not stored
     */
    public double getDistance(HierarchicalClusterNode<S> node);

    /**
     * Starts a frontier in both directions if the distances list.
     * 
     * @param node
     *            the node to start the frontier from
     * @return all frontier {@link HierarchicalClusterNode}s
     * @throws NoSuchElementException
     *             if node is not stored
     */
    public Collection<HierarchicalClusterNode<S>> startNewFrontier(HierarchicalClusterNode<S> node);

    /**
     * Pushes the Frontier on further away from the node where it started.
     * 
     * @param node
     *            the last Frontier {@link HierarchicalClusterNode} (to
     *            determine the direction in which the Frontier should be
     *            pushed)
     * @return the new Frontier {@link HierarchicalClusterNode} or null if the
     *         frontier is at its end.
     * @throws IllegalStateException
     *             if the node is no frontier {@link HierarchicalClusterNode}
     * @throws NoSuchElementException
     *             if node is not stored
     */
    public HierarchicalClusterNode<S> pushFrontier(HierarchicalClusterNode<S> node);

    /**
     * Returns the node from which the frontier was started.
     * 
     * Attention: Calling this method is only allowed if startNewFrontier() was
     * called before!
     * 
     * @return the frontier start node
     * @throws IllegalStateException
     *             if the frontier was never started
     */
    public HierarchicalClusterNode<S> getFrontierStartNode();

    /**
     * The size of the {@link DistanceList}.
     * 
     * @return the size
     */
    public int size();

    /**
     * Returns an <b>unmodifiable</b> {@link Collection} of all stored
     * {@link HierarchicalClusterNode}s
     * 
     * @return all stored {@link HierarchicalClusterNode}s
     */
    public Collection<HierarchicalClusterNode<S>> getAllNodes();
}
