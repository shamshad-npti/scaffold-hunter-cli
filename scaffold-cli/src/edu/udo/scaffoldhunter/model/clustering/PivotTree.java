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
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * This is the implementation of an dynamic pivot tree. It is based on:
 * 
 * Jianjun Zhou; Sander, J.; , "Speedup Clustering with Hierarchical Ranking,"
 * Data Mining, 2006. ICDM '06. Sixth International Conference on , vol., no.,
 * pp.1205-1210, 18-22 Dec. 2006 doi: 10.1109/ICDM.2006.151
 * 
 * URL:
 * http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=4053180&isnumber=
 * 4053013
 * 
 * It is modified to support distance updates for SAHN Clustering.
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class PivotTree<S extends Structure> {
    private static Logger logger = LoggerFactory.getLogger(PivotTree.class);
    /**
     * the sampling strategy
     */
    private final Sampler<S> sampler;
    /**
     * if this number is exceeded the construction of the tree is stopped. Note
     * that the real leaf count can be higher depending on the sampling
     * strategy.
     */
    private final int leafBound;
    /**
     * which PTreeNode is the leaf of {@link HierarchicalClusterNode} x
     * 
     * {@link HierarchicalClusterNode} -> leaf node
     */
    private HashMap<HierarchicalClusterNode<S>, PTreeNode<S>> leafStructures = Maps.newHashMap();
    /**
     * the root node
     */
    private PTreeNode<S> root;
    /**
     * the leaf selection strategy
     */
    private LeafSelection<S> leafSelection;
    /**
     * the {@link Distance} between single elements
     */
    private Distance<S> distance;

    /**
     * Constructor
     * 
     * @param sampler
     *            the sampling strategy
     * @param leafSelection
     *            the {@link LeafSelection} strategy
     * @param leafBound
     *            if the this number of leafs is exceeded the construction of
     *            the tree is stopped. Note that the real leaf count can be
     *            higher depending on the sampling strategy.
     * @param nodes
     *            the initial nodes
     * @param distance
     *            the used {@link Distance}
     * @param deterministic
     *            if set to true, the {@link PivotTree} will be generated in a
     *            deterministic way for a specific {@link Dataset}. This is
     *            useful for testing and evaluation purposes.
     * @throws ClusteringException
     */
    public PivotTree(Sampler<S> sampler, LeafSelection<S> leafSelection, int leafBound,
            Collection<HierarchicalClusterNode<S>> nodes, Distance<S> distance, boolean deterministic)
            throws ClusteringException {
        Preconditions.checkArgument(leafBound > 0);

        // TODO: add leaf selection and sampler generator enums
        this.sampler = sampler;
        this.leafSelection = leafSelection;
        this.leafBound = leafBound;
        this.distance = distance;

        ArrayList<HierarchicalClusterNode<S>> nodeArray = Lists.newArrayList(nodes);

        if (deterministic) {
            Collections.sort(nodeArray, new Comparator<HierarchicalClusterNode<S>>() {

                @Override
                public int compare(HierarchicalClusterNode<S> o1, HierarchicalClusterNode<S> o2) {
                    return o2.getContentDbId() - o1.getContentDbId();
                }

            });
        }
        construct(nodeArray);

    }

    /**
     * Returns all {@link Pivot}s that belong to node (i.e. all parent pivots of
     * the leaf which contains node). The {@link Pivot}s are split by the
     * {@link PTreeNode} they belong to. This is useful because all
     * {@link Pivot}s of one {@link PTreeNode} have the same
     * {@link HierarchicalClusterNode}s assigned.
     * 
     * @param node
     *            the {@link HierarchicalClusterNode}
     * @return all {@link Pivot}s that belong to node
     */
    public Collection<Collection<Pivot<S>>> getPivots(HierarchicalClusterNode<S> node) {
        LinkedList<Collection<Pivot<S>>> retVal = new LinkedList<Collection<Pivot<S>>>();

        for (PTreeNode<S> treeNode : new UpwardIterable(node)) {
            retVal.add(treeNode.pivots);
        }

        return retVal;
    }

    /**
     * Deletes a {@link HierarchicalClusterNode} from the {@link PivotTree}.
     * 
     * @param node
     *            the {@link HierarchicalClusterNode} to delete
     */
    public void remove(HierarchicalClusterNode<S> node) {
        for (PTreeNode<S> treeNode : new UpwardIterable(node)) {
            treeNode.remove(node);
        }

        leafStructures.remove(node);
    }

    /**
     * Merges two {@link HierarchicalClusterNode}s to one new Node and removes
     * node1 and node2.
     * 
     * @param node1
     *            the first {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @param formula
     *            the {@link LanceWilliamsUpdateFormula} used to update the
     *            distances to the pivot
     * @param dist12
     *            the distance between node1 and node2
     * @return the new merged {@link HierarchicalClusterNode}
     */
    public HierarchicalClusterNode<S> merge(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2,
            LanceWilliamsUpdateFormula formula, double dist12) {
        HierarchicalClusterNode<S> mergedNode = new HierarchicalClusterNode<S>(node1, node2, dist12,
                node1.getClusterSize() + node2.getClusterSize());

        /*
         * Start adding mergedNode with LCA. The leaf node for the new merged
         * cluster will be the LCA.
         */
        PTreeNode<S> lca = lowestCommonAncestor(leafStructures.get(node1), leafStructures.get(node2));
        leafStructures.put(mergedNode, lca);
        for (PTreeNode<S> node : new UpwardIterable(lca)) {
            node.addMergedNode(mergedNode, formula);
        }

        // remove merged cluster nodes
        remove(node1);
        remove(node2);

        return mergedNode;
    }

    /**
     * Estimates the distance between two nodes based on triangular inequality
     * 
     * @param node1
     *            the first {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @return the estimated distance
     */
    public double estimatedDistance(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) {
        PTreeNode<S> lca = lowestCommonAncestor(leafStructures.get(node1), leafStructures.get(node2));

        double maxDist = Double.NEGATIVE_INFINITY;
        for (PTreeNode<S> node : new UpwardIterable(lca)) {
            for (Pivot<S> pivot : node.pivots) {
                maxDist = Math.max(maxDist, pivot.getLowerDistanceBound(node1, node2));
            }
        }

        return maxDist;
    }

    /**
     * Returns the number of common {@link Pivot}s between node1 and node2
     * 
     * @param node1
     *            the first {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @return the number of common {@link Pivot}s
     */
    public int commonPivotCount(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) {
        int retVal = 0;
        PTreeNode<S> lca = lowestCommonAncestor(leafStructures.get(node1), leafStructures.get(node2));

        for (PTreeNode<S> node : new UpwardIterable(lca)) {
            retVal += node.pivots.size();
        }
        return retVal;
    }

    /**
     * Calculates the lowest common ancestor (LCA) from two {@link PTreeNode}s.
     * 
     * @param node1
     *            the first {@link PTreeNode}
     * @param node2
     *            the second {@link PTreeNode}
     * @return the LCA
     */
    private PTreeNode<S> lowestCommonAncestor(PTreeNode<S> node1, PTreeNode<S> node2) {
        assert node1 != null;
        assert node2 != null;

        PTreeNode<S> deeperNode = node1.level < node2.level ? node2 : node1;
        PTreeNode<S> higherNode = node1 == deeperNode ? node2 : node1;

        // align level
        while (deeperNode.level > Math.min(node1.level, node2.level)) {
            deeperNode = deeperNode.parent;
        }

        // go up level by level and check if we reached the LCA
        while (deeperNode != higherNode) {
            deeperNode = deeperNode.parent;
            higherNode = higherNode.parent;
        }

        return deeperNode;
    }

    /**
     * Constructs the {@link PivotTree} from the initial
     * {@link HierarchicalClusterNode}s
     * 
     * @throws ClusteringException
     */
    private void construct(ArrayList<HierarchicalClusterNode<S>> nodes) throws ClusteringException {
        /*
         * PTreeNode -> according Structures
         */
        HashMap<PTreeNode<S>, ArrayList<HierarchicalClusterNode<S>>> elements = Maps.newHashMap();

        // create root PTreeNode
        root = new PTreeNode<S>(null, 0);
        elements.put(root, new ArrayList<HierarchicalClusterNode<S>>(nodes));
        leafSelection.add(root);
        generatePivots(root, elements.get(root));

        /*
         * Invariant: leafSelection contains the current leaf PTreeNodes
         */
        SPLITLOOP: while (leafSelection.size() < leafBound) {
            HashMap<Pivot<S>, PTreeNode<S>> pivotTreeNodes = Maps.newHashMap();

            /*
             * get next leaf to split
             */
            PTreeNode<S> currentLeaf = leafSelection.remove();

            /*
             * generate PTreeNodes for each Pivot of currentLeaf
             */
            for (Pivot<S> pivot : currentLeaf.pivots) {
                PTreeNode<S> pTreeNode = new PTreeNode<S>(currentLeaf, currentLeaf.level + 1);
                pivotTreeNodes.put(pivot, pTreeNode);
            }

            /*
             * assign each node to the closest PTreeNode / sample point
             */
            ArrayList<HierarchicalClusterNode<S>> currentElements = elements.get(currentLeaf);
            if (currentElements != null) {
                for (HierarchicalClusterNode<S> node : currentElements) {
                    double minDist = Double.POSITIVE_INFINITY;
                    PTreeNode<S> minPTreeNode = null;

                    // find minimal distance PTreeNode
                    for (Pivot<S> simplePivot : currentLeaf.pivots) {
                        if (simplePivot.getDist(node) < minDist) {
                            minDist = simplePivot.getDist(node);
                            minPTreeNode = pivotTreeNodes.get(simplePivot);
                        }
                    }

                    // add node to elements for the minimal distance PTreeNode
                    if (elements.containsKey(minPTreeNode)) {
                        assert !elements.get(minPTreeNode).contains(node);

                        elements.get(minPTreeNode).add(node);
                    } else {
                        elements.put(minPTreeNode,
                                new ArrayList<HierarchicalClusterNode<S>>(Collections.singleton(node)));
                    }
                }
            }

            /*
             * generate Pivots for each new PTreeNode
             */
            for (PTreeNode<S> pTreeNode : pivotTreeNodes.values()) {
                ArrayList<HierarchicalClusterNode<S>> pivotElements = elements.get(pTreeNode);
                // do not create empty TreeNodes
                if (pivotElements != null) {
                    /*
                     * If the newly generated PTreeNode has the same size as its
                     * parent, no further splitting is possible and this is the
                     * only Sibling. Normally this should happen when the size
                     * is 1, but this is also the case if all pairwise distances
                     * in a set of HCNs are 0 and therefore all HCNs are always
                     * assigned to the same PTreeNode.
                     */
                    if (pivotElements.size() == currentLeaf.size()) {
                        // reAdd currentLead as it stays a leaf
                        leafSelection.add(currentLeaf);
                        break SPLITLOOP;
                    } else {
                        generatePivots(pTreeNode, pivotElements);
                        leafSelection.add(pTreeNode);
                    }
                }
            }
        }
        logger.debug("Real leaf count: {}", leafSelection.size());

        /*
         * Add all HierarchicalClusterNodes to leafStructures
         */
        for (PTreeNode<S> leaf : leafSelection.getAllLeafs()) {
            ArrayList<HierarchicalClusterNode<S>> leafElements = elements.get(leaf);
            // some leafs may not contain any elements
            if (leafElements != null) {
                for (HierarchicalClusterNode<S> node : leafElements) {
                    leafStructures.put(node, leaf);
                }
            }
        }
    }

    /**
     * Checks if a {@link Collection} contains each element only once.
     * 
     * Runs the check only if assertions are enabled!
     * 
     * @param collection
     *            the {@link Collection} to check.
     */
    private void assertUniqueEntries(@SuppressWarnings("rawtypes") Collection collection) {
        boolean assertsAreEnabled = false;
        assert (assertsAreEnabled = true) == true;

        if (assertsAreEnabled) {
            for (Object node : collection) {
                int count = 0;
                for (Object compareNode : collection) {
                    if (node.equals(compareNode)) {
                        count++;
                    }
                }
                assert count == 1;
            }
        }
    }

    /**
     * Samples the {@link HierarchicalClusterNode}s from pTreeNode and creates a
     * {@link Pivot} for each sample. Adds the {@link Pivot} to the pTreeNode.
     * 
     * @param pTreeNode
     *            the {@link PTreeNode} to generate the Pivots for
     * @param elements
     *            the elements which are assigned to the {@link PTreeNode}s
     * @throws ClusteringException
     */
    private void generatePivots(PTreeNode<S> pTreeNode, ArrayList<HierarchicalClusterNode<S>> pivotElements)
            throws ClusteringException {
        assertUniqueEntries(pivotElements);

        HashSet<HierarchicalClusterNode<S>> sample = sampler.getSample(pivotElements);
        for (HierarchicalClusterNode<S> pivotNode : sample) {
            Pivot<S> pivot = new Pivot<S>(pivotNode, distance, pivotElements);
            pTreeNode.pivots.add(pivot);
        }
    }

    /**
     * The TreeNode for a {@link PivotTree}
     * 
     * @author Till Schäfer
     * 
     */
    protected static class PTreeNode<E extends Structure> {
        private PTreeNode<E> parent;
        /**
         * (Pivot, associated child {@link PTreeNode})
         */
        private LinkedList<Pivot<E>> pivots = Lists.newLinkedList();
        /**
         * the nodes level or depth
         */
        private int level;

        /**
         * Constructor
         * 
         * only used for Array initialisation
         */
        public PTreeNode() {

        }

        /**
         * Constructor
         * 
         * @param parent
         *            the nodes parent
         * @param level
         *            the nodes level
         */
        public PTreeNode(PTreeNode<E> parent, int level) {
            this.parent = parent;
            this.level = level;
        }

        public int size() {
            if (pivots.size() == 0) {
                return 0;
            } else {
                /*
                 * all pivots have the same size -> choose an arbitrary pivot
                 */
                return pivots.get(0).size();
            }
        }

        /**
         * Removes a {@link HierarchicalClusterNode} from all {@link Pivot}s.
         * 
         * @param node
         *            the {@link HierarchicalClusterNode} to remove
         */
        public void remove(HierarchicalClusterNode<E> node) {
            for (Pivot<E> pivot : pivots) {
                pivot.remove(node);
            }
        }

        /**
         * Adds a {@link HierarchicalClusterNode} to all {@link Pivot}s. The new
         * distance to the Pivot is calculated by the
         * {@link LanceWilliamsUpdateFormula} with respect to the children
         * distances, the children clusterSizes and the dissimilarity of node.
         * 
         * @param node
         *            the {@link HierarchicalClusterNode} to add
         * @param formula
         *            the {@link LanceWilliamsUpdateFormula}
         */
        public void addMergedNode(HierarchicalClusterNode<E> node, LanceWilliamsUpdateFormula formula) {
            int leftClusterSize = node.getLeftChild().getClusterSize();
            int rightClusterSize = node.getRightChild().getClusterSize();

            for (Pivot<E> pivot : pivots) {
                double distToLeftChild = pivot.getDist(node.getLeftChild());
                double distToRightChild = pivot.getDist(node.getRightChild());

                double newDist = formula.newDistance(distToLeftChild, distToRightChild, node.getDissimilarity(), 1,
                        leftClusterSize, rightClusterSize);

                pivot.add(node, newDist);
            }
        }
    }

    /**
     * {@link Iterable} that moves upward the PivotTree until the root.
     * 
     * @author Till Schäfer
     * 
     */
    private class UpwardIterable implements Iterable<PTreeNode<S>> {
        private final PTreeNode<S> pTreeNode;

        /**
         * Constructor
         * 
         * @param pTreeNode
         *            the start {@link PTreeNode}
         */
        public UpwardIterable(PTreeNode<S> pTreeNode) {
            this.pTreeNode = pTreeNode;
        }

        /**
         * Constructor (starting upward iteration with leafStructure)
         * 
         * @param node
         *            the {@link HierarchicalClusterNode}
         */
        public UpwardIterable(HierarchicalClusterNode<S> node) {
            pTreeNode = leafStructures.get(node);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<PTreeNode<S>> iterator() {
            return new it();
        }

        private class it implements Iterator<PTreeNode<S>> {
            private PTreeNode<S> currentNode = null;

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#hasNext()
             */
            @Override
            public boolean hasNext() {
                return currentNode == null || currentNode.parent != null;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#next()
             */
            @Override
            public PTreeNode<S> next() {
                if (currentNode == null) {
                    currentNode = pTreeNode;
                } else {
                    if (currentNode.parent == null) {
                        throw new NoSuchElementException();
                    }
                    currentNode = currentNode.parent;
                }
                return currentNode;
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#remove()
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }
    }
}
