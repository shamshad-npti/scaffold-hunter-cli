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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.util.WrapperException;

/**
 * A symmetric distance matrix. The distance for (a,b) is thus the same as for
 * (b,a).
 * 
 * @author Philipp Kopp
 * @author Till Schäfer
 * @author Dominic Sacré
 * @param <S>
 *            Structure: Molecule/Scaffold
 * 
 */
public class SymmetricDistanceMatrix<S extends Structure> {
    private static Logger logger = LoggerFactory.getLogger(SymmetricDistanceMatrix.class);

    private Distance<S> distance;
    /**
     * The mapping from a {@link HierarchicalClusterNode} to a index of the
     * matrix array
     */

    /**
     * The actual distance matrix.
     * 
     * @see #matrixIndex(int i, int j)
     */
    private double[] matrix;

    /**
     * The stored cluster nodes
     */
    private final HashSet<HierarchicalClusterNode<S>> nodes;

    /**
     * Constructor. Starts the initial calculation of the distances for each
     * pair of the specified nodes.
     * 
     * @param dist
     *            the used Distance calculation method
     * @param nodes
     *            the cluster nodes to store
     * @throws ClusteringException
     */
    public SymmetricDistanceMatrix(Distance<S> dist, Collection<HierarchicalClusterNode<S>> nodes)
            throws ClusteringException {
        this(dist, nodes, false);
    }

    /**
     * Constructor. Starts the initial calculation of the distances for each
     * pair of the specified nodes.
     * 
     * @param dist
     *            the used Distance calculation method
     * @param nodes
     *            the cluster nodes to store
     * @param parallelInit
     *            use a parallel distance computations for initialization
     * @throws ClusteringException
     */
    public SymmetricDistanceMatrix(Distance<S> dist, Collection<HierarchicalClusterNode<S>> nodes, boolean parallelInit)
            throws ClusteringException {
        distance = dist;
        this.nodes = Sets.newLinkedHashSet(nodes);

        matrix = new double[matrixSize(nodes.size())];

        HierarchicalClusterNode.assignHcnIds(nodes);

        if (parallelInit) {
            fillMatrixParallel(nodes);
        } else {
            fillMatrix(nodes);
        }
    }

    private void fillMatrixParallel(Collection<HierarchicalClusterNode<S>> nodes2) throws ClusteringException {
        /*
         * parallel execution of the calculation of each row (maximal parallel threads
         * = #processors +1)
         */
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        LinkedList<Callable<Void>> jobs = Lists.newLinkedList();
        for (HierarchicalClusterNode<S> node : nodes) {
            jobs.add(new InitCallable(node));
        }
        
        List<Future<Void>> futures = null;
        try {
            // run all jobs
            futures = executor.invokeAll(jobs);
            
            // wait until all jobs are finished
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            return;
        }
        
        // throw inner ClusteringExptions
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause.getClass() == WrapperException.class) {
                    throw (ClusteringException)((WrapperException) cause).unwrap();
                } else { 
                    throw new ClusteringException("Unkown Exception", cause);
                }
            }
            
        }
    }

    private class InitCallable implements Callable<Void>{
        private final HierarchicalClusterNode<S> node1; 
        
        public InitCallable (HierarchicalClusterNode<S> node) {
            this.node1 = node;
        }
        
        @Override
        public Void call() {
            for (HierarchicalClusterNode<S> node2 : nodes) {
                if (node1.getExternalId() < node2.getExternalId()) {
                    try {
                        matrix[matrixIndex(node1.getExternalId(), node2.getExternalId())] = distance.calcDist(node1, node2);
                    } catch (ClusteringException e) {
                        throw new WrapperException(e);
                    }

                    // Allow abortion
                    if (Thread.interrupted()) {
                        return null;
                    }
                }
            }
            return null;
        }

    }

    /**
     * Gets the distance between two different {@link HierarchicalClusterNode}s
     * 
     * @param node1
     *            the fist {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @return the stored distance between node1 and node2
     */
    public double getDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) {
        assert node1 != node2 : "node1 and node2 are not different";
        assert nodes.contains(node1) : "node1 is not stored in the matrix";
        assert nodes.contains(node2) : "node2 is not stored in the matrix";

        return matrix[matrixIndex(node1.getExternalId(), node2.getExternalId())];
    }

    /**
     * Updates the distance between two different
     * {@link HierarchicalClusterNode}s
     * 
     * @param node1
     *            the fist {@link HierarchicalClusterNode}
     * @param node2
     *            the second {@link HierarchicalClusterNode}
     * @param dist
     *            the new distance between node1 and node2
     */
    public void setDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2, double dist) {
        assert node1 != node2 : "node1 and node2 are not different";
        assert nodes.contains(node1) : "node1 is not stored in the matrix";
        assert nodes.contains(node2) : "node2 is not stored in the matrix";

        matrix[matrixIndex(node1.getExternalId(), node2.getExternalId())] = dist;
    }
    

    /**
     * Merges two nodes and update all Distances for the new Cluster using the
     * Lance Williams Update Formula
     * 
     * @param node_i
     *            the first {@link HierarchicalClusterNode}
     * @param node_j
     *            the second {@link HierarchicalClusterNode}
     * @param formula
     *            the update formula
     * @return the new formed {@link HierarchicalClusterNode}
     */
    public HierarchicalClusterNode<S> mergeNodes(HierarchicalClusterNode<S> node_i, HierarchicalClusterNode<S> node_j,
            LanceWilliamsUpdateFormula formula) {
        // TODO: junit testing
        double dist_ij = getDist(node_i, node_j);
        for (HierarchicalClusterNode<S> node_k : nodes) {
            if (node_k != node_i && node_k != node_j) {
                double newDist = formula.newDistance(getDist(node_k, node_i), getDist(node_k, node_j), dist_ij,
                        node_k.getClusterSize(), node_i.getClusterSize(), node_j.getClusterSize());
                setDist(node_k, node_i, newDist);
            }
        }
        
        HierarchicalClusterNode<S> newNode = new HierarchicalClusterNode<S>(node_i, node_j, dist_ij,
                node_i.getClusterSize() + node_j.getClusterSize());
        
        // replace node i by newNode
        newNode.setExternalId(node_i.getExternalId());
        nodes.add(newNode);
        nodes.remove(node_i);
        nodes.remove(node_j);
        
        return newNode;
    }

    /**
     * @return unmodifiable {@link Collection} of stored {@link HierarchicalClusterNode}s
     */
    public Collection<HierarchicalClusterNode<S>> getStoredNodes() {
        return Collections.unmodifiableCollection(nodes);
    }

    /**
     * Returns the size of all stored {@link HierarchicalClusterNode}s.
     * 
     * @return the size
     */
    public int size() {
        return nodes.size();
    }

    /**
     * @return the maximum value stored in the {@link SymmetricDistanceMatrix}
     */
    public double getMaxDistance() {
        Preconditions.checkState(nodes.size() > 0);
        
        double maxVal = Double.NEGATIVE_INFINITY; 
        for (HierarchicalClusterNode<S> node1 : nodes) {
            for (HierarchicalClusterNode<S> node2 : nodes) {
                if (node1.getExternalId() < node2.getExternalId()) {
                    maxVal = Math.max(getDist(node1, node2), maxVal);
                }
            }
        }
        
        return maxVal;
    }

    /**
     * Calculates the Distance between each pair of nodes
     * 
     * @param nodes
     *            cluster nodes to fill in the matrix
     * @throws ClusteringException
     */
    private void fillMatrix(Collection<HierarchicalClusterNode<S>> nodes) throws ClusteringException {
        for (HierarchicalClusterNode<S> node1 : nodes) {
            for (HierarchicalClusterNode<S> node2 : nodes) {
                if (node1.getExternalId() < node2.getExternalId()) {
                    matrix[matrixIndex(node1.getExternalId(), node2.getExternalId())] = distance.calcDist(node1, node2);

                    // Allow abortion
                    if (Thread.interrupted()) {
                        return;
                    }
                }
            }
        }
    }

    /**
     * Calculates the unidimensional array index of the distance between node i
     * and node j.
     * 
     * @param i
     *            the index of the first node
     * @param j
     *            the index of the second node
     * 
     * @return the array index of the distance
     */
    private int matrixIndex(int i, int j) {
        if (i < j) {
            // swap indices so that i > j
            int temp = i;
            i = j;
            j = temp;
        }

        /*
         * Mapping from node indices i,j to array index:
         *
         *        j-->
         *        0  1  2  3  4  5
         *     +-------------------
         * i 0 |  #  #  #  #  #  #
         * | 1 |  0  #  #  #  #  #
         * v 2 |  1  2  #  #  #  #
         *   3 |  3  4  5  #  #  #
         *   4 |  6  7  8  9  #  #
         *   5 | 10 11 12 13 14  # 
         * 
         * Triangular number:
         * t(n) := (n * (n + 1)) / 2
         * 
         * Index of first element of row i:
         * g(i) := t(i - 1)
         *
         * Index of element i,j:
         * f(i, j) := g(i) + j
         * 
         * Function f can then be simplified to:
         * f(i, j) := 1/2 (i² - i + 2j)
         */
        return (int) (((long) i * i - i + 2 * j) / 2);
    }

    /**
     * Calculates the number of elements required for a unidimensional array to
     * hold a symmetric n × n matrix (without main diagonal).
     * 
     * @param n
     *            the number of nodes
     * 
     * @return the required array size
     */
    private int matrixSize(int n) {
        /*
         * Total number of elements that must be stored for n nodes: s(n) := t(n
         * - 1)
         */
        long longsize = n * ((long) n - 1) / 2;
        if (longsize > Integer.MAX_VALUE) {
            throw new OutOfMemoryError("The matrix supports only 2G entries");
        }

        logger.debug("The size of the matrix for {} entries is: {}", n, longsize);
        return (int) longsize;
    }

}
