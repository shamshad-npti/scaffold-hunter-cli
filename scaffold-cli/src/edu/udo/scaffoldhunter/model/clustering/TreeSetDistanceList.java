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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.clustering.PivotTree.PTreeNode;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * A {@link DistanceList} backed by a {@link TreeSet} for the sorting and a
 * {@link HashMap} for the distances.
 * 
 * @author Till Schäfer
 * 
 * @param <S>
 *            the concrete {@link Structure}
 */
public class TreeSetDistanceList<S extends Structure> implements DistanceList<S> {
    private static Logger logger = LoggerFactory.getLogger(TreeSetDistanceList.class);

    /**
     * {@link HierarchicalClusterNode} -> distance
     */
    private HashMap<HierarchicalClusterNode<S>, Double> distances = new HashMap<HierarchicalClusterNode<S>, Double>();
    /**
     * {@link HierarchicalClusterNode} sorted by distances
     */
    private TreeSet<HierarchicalClusterNode<S>> sortedNodes = new TreeSet<HierarchicalClusterNode<S>>(
            new DistanceComparator());;
    /**
     * The frontier start {@link HierarchicalClusterNode}
     */
    private HierarchicalClusterNode<S> frontierStart = null;
    /**
     * The Up Frontier
     */
    private Iterator<HierarchicalClusterNode<S>> upFrontier = null;
    /**
     * The Downs Frontier
     */
    private Iterator<HierarchicalClusterNode<S>> downFrontier = null;
    /**
     * The last {@link HierarchicalClusterNode} that was returned for the up
     * frontier
     */
    private HierarchicalClusterNode<S> lastUpNode = null;
    /**
     * The last {@link HierarchicalClusterNode} that was returned for the down
     * frontier
     */
    private HierarchicalClusterNode<S> lastDownNode = null;

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.DistanceList#add(edu.udo.
     * scaffoldhunter.model.clustering.HierarchicalClusterNode, double)
     */
    @Override
    public void add(HierarchicalClusterNode<S> node, double distance) {
        Preconditions.checkNotNull(node);
        checkNotContains(node);

        /*
         * Attention: Be careful with the order of the two commands below.
         * sortedNodes needs distances for comparator.
         */
        distances.put(node, distance);
        boolean inserted = sortedNodes.add(node);

        assert inserted;
        assert (distances.size() == sortedNodes.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.DistanceList#remove(edu.udo.
     * scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public void remove(HierarchicalClusterNode<S> node) {
        Preconditions.checkNotNull(node);
        checkContains(node);

        sortedNodes.remove(node);
        distances.remove(node);

        assert (distances.size() == sortedNodes.size());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.DistanceList#getDistance(edu.
     * udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public double getDistance(HierarchicalClusterNode<S> node) {
        Preconditions.checkNotNull(node);
        checkContains(node);

        return distances.get(node);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.DistanceList#startNewFrontier
     * (edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public Collection<HierarchicalClusterNode<S>> startNewFrontier(HierarchicalClusterNode<S> node) {
        Preconditions.checkNotNull(node);
        checkContains(node);

        frontierStart = node;
        NavigableSet<HierarchicalClusterNode<S>> upFrontierView = sortedNodes.headSet(node, false);
        upFrontier = upFrontierView.descendingIterator();
        NavigableSet<HierarchicalClusterNode<S>> downFrontierView = sortedNodes.tailSet(node, false);
        downFrontier = downFrontierView.iterator();

        Collection<HierarchicalClusterNode<S>> retVal = new ArrayList<HierarchicalClusterNode<S>>(2);
        if (upFrontier.hasNext()) {
            lastUpNode = upFrontier.next();
            retVal.add(lastUpNode);
        } else {
            lastUpNode = null;
        }
        if (downFrontier.hasNext()) {
            lastDownNode = downFrontier.next();
            retVal.add(lastDownNode);
        } else {
            lastDownNode = null;
        }

        assert upFrontierView.size() + downFrontierView.size() + 1 == size();

        logger.debug("frontierEntries size: {}, upFrontier.size={}, downFrontier.size=" + downFrontierView.size(),
                retVal.size(), upFrontierView.size());

        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.DistanceList#pushFrontier(edu
     * .udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public HierarchicalClusterNode<S> pushFrontier(HierarchicalClusterNode<S> node) {
        Preconditions.checkNotNull(node);
        checkContains(node);

        if (lastUpNode == node) {
            if (upFrontier.hasNext()) {
                // push up frontier
                HierarchicalClusterNode<S> nextUpNode = upFrontier.next();

                // distances must be monotonically decreasing
                assert distances.get(lastUpNode) >= distances.get(nextUpNode);
                assert nextUpNode != null;

                lastUpNode = nextUpNode;

                return nextUpNode;
            } else {
                // up frontier ended
                return null;
            }
        } else if (lastDownNode == node) {
            if (downFrontier.hasNext()) {
                // push down frontier
                HierarchicalClusterNode<S> nextDownNode = downFrontier.next();

                // distances must be monotonically increasing
                assert distances.get(lastDownNode) <= distances.get(nextDownNode);
                assert nextDownNode != null;

                lastDownNode = nextDownNode;

                return nextDownNode;
            } else {
                // down frontier ended
                return null;
            }
        } else {
            throw new IllegalStateException("node is no frontier node");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.DistanceList#getFrontierStartNode
     * ()
     */
    @Override
    public HierarchicalClusterNode<S> getFrontierStartNode() {
        if (frontierStart == null) {
            throw new IllegalStateException("frontier was never started");
        }
        return frontierStart;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.DistanceList#size()
     */
    @Override
    public int size() {
        assert (distances.size() == sortedNodes.size());

        return sortedNodes.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.DistanceList#getAllNodes()
     */
    @Override
    public Collection<HierarchicalClusterNode<S>> getAllNodes() {
        return Collections.unmodifiableCollection(distances.keySet());
    }

    /**
     * Checks if the node is stored in this data structure
     * 
     * @param node
     */
    private void checkContains(HierarchicalClusterNode<S> node) {
        if (!distances.containsKey(node)) {
            throw new NoSuchElementException();
        }
        // very slow assert
        // assert sortedNodes.contains(node) == distances.containsKey(node);
    }

    /**
     * Checks if the node is NOT stored in this data structure
     * 
     * @param node
     */
    private void checkNotContains(HierarchicalClusterNode<S> node) {
        if (distances.containsKey(node)) {
            throw new DoubleEntryException();
        }
    }

    /**
     * Comparator based on the size of {@link PTreeNode}s. If the size is equal
     * the hashCode will be compared.
     * 
     * @author Till Schäfer
     */
    private class DistanceComparator implements Comparator<HierarchicalClusterNode<S>> {
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(HierarchicalClusterNode<S> o1, HierarchicalClusterNode<S> o2) {
            /*
             * use assert instead on Preconditions because of performance
             * reasons the comparator is also not accessible outside of
             * TreeSEtDistanceList and therefore relies only on the correct
             * internal implementation
             */
            assert o1 != null;
            assert o2 != null;
            assert distances.containsKey(o1);
            assert distances.containsKey(o2);

            Double dist1 = distances.get(o1);
            Double dist2 = distances.get(o2);

            assert dist1 != null;
            assert dist2 != null;

            int retval = dist1.compareTo(dist2);
            if (retval == 0) {
                return o1.getUniqueID().compareTo(o2.getUniqueID());
            } else {
                return retval;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        for (HierarchicalClusterNode<S> node : sortedNodes) {
            builder.append(node.getContent() == null ? "null" : node.getContent().getTitle());
            builder.append(" - ");
            builder.append(distances.get(node));
            builder.append(System.getProperty("line.separator").toString());
        }

        return builder.toString();
    }
}
