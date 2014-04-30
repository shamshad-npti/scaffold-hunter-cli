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

package edu.udo.scaffoldhunter.model.clustering.evaluation;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
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

import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Molecule;

/**
 * The Subclasses of this Class implement a Comparison between two hierarchical
 * cluster results
 * 
 * @author Till Schäfer
 * 
 */
public abstract class HierarchicalComparison {
    private static Logger logger = LoggerFactory.getLogger(HierarchicalComparison.class);

    /*
     * result of clustering 1
     */
    protected HierarchicalClusterNode<Molecule> root1;
    /*
     * result of clustering 2
     */
    protected HierarchicalClusterNode<Molecule> root2;
    protected EvaluationResult result;
    /*
     * Each n-th level should be measured. i.e. stepWith=1 means every level is
     * measured, stepWith=2 every second, etc.
     */
    protected int stepWith = 1;

    /**
     * Constructor
     * 
     * Precondition: both clusterings should be from the same set of elements
     * 
     * @param root1
     *            the root of the first hierarchical clustering result
     * @param root2
     *            the root of the second hierarchical clustering result
     * @param result
     *            the {@link EvaluationResult} that should be filled with the
     *            measurement results (should already contain the used
     *            clusterings)
     * @param stepWith
     *            Each n-th level should be measured. i.e. stepWith=1 means
     *            every level is measured.
     */
    public HierarchicalComparison(HierarchicalClusterNode<Molecule> root1, HierarchicalClusterNode<Molecule> root2,
            EvaluationResult result, int stepWith) {
        Preconditions.checkArgument(stepWith > 0);
        Preconditions.checkArgument(root1.getClusterSize() == root2.getClusterSize());

        this.root1 = root1;
        this.root2 = root2;
        this.result = result;
        this.stepWith = stepWith;
    }

    /**
     * Runs the measurement (in parallel)
     * 
     * @return the {@link EvaluationResult}
     */
    public EvaluationResult run() {
        logger.trace("Running HierarchicalComparison");
        ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels1 = getAllLevels(root1);
        ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels2 = getAllLevels(root2);

        assert levels1.size() == levels2.size();

        /*
         * parallel execution of the calculations per level (maximal parallel
         * threads = #processors +1)
         */
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
        LinkedList<Callable<SimpleEntry<String, String>>> jobs = Lists.newLinkedList();

        // generate jobs
        for (int index = 0; index < levels1.size(); index++) {
            jobs.add(getNewWorker(levels1, levels2, index));
        }

        List<Future<SimpleEntry<String, String>>> futures;

        try {
            // run jobs
            futures = executor.invokeAll(jobs);
            // wait until all jobs are finished
            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            // this should never happen!
            throw new EvaluationException("This is impossible", e);
        }

        for (Future<SimpleEntry<String, String>> future : futures) {
            SimpleEntry<String, String> entry;
            try {
                entry = future.get();
            } catch (InterruptedException e) {
                // this should never happen!
                throw new EvaluationException("This is impossible", e);
            } catch (ExecutionException e) {
                throw new EvaluationException("Failure in calculating comparison value", e);
            }
            result.addResult(entry.getKey(), entry.getValue());
        }

        return result;
    }

    /**
     * Returns for each level a {@link List} with the
     * {@link HierarchicalClusterNode}s of this level.
     * 
     * Level 0 contains only the root node. Level (subset.getMolecules().size()
     * - 1) contains all singleton clusters.
     * 
     * @param root
     *            the root {@link HierarchicalClusterNode} i.e. the clustering
     *            result
     * @return the levels
     */
    protected ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> getAllLevels(
            HierarchicalClusterNode<Molecule> root) {
        Preconditions.checkNotNull(root);

        return root.getAllLevels(stepWith);
    }

    /**
     * Returns the bit vector of all pairs of Objects in flat clustering. The
     * bit is one if the pair is located in the same cluster. This is useful for
     * comparison methods which rely on the counting of pairs (e.g. Rand).
     * 
     * @param molecules
     *            all {@link Molecule}s (the ordering is important if the
     *            comparison of two bitVecorts should make sense)
     * @param clusters
     *            all {@link HierarchicalClusterNode}s of the flat clustering.
     *            Note that clusters must be disjunct!
     * @return the bit vector
     */
    protected BitSet getPairsBitVector(Collection<HierarchicalClusterNode<Molecule>> clusters) {
        List<Molecule> molecules = getMolecules(clusters);
        BitSet bitVector = new BitSet((int) Math.pow(molecules.size(), 2));
        HashMap<Molecule, HierarchicalClusterNode<Molecule>> belongsToNode = new HashMap<Molecule, HierarchicalClusterNode<Molecule>>();

        for (HierarchicalClusterNode<Molecule> node : clusters) {
            for (Molecule mol : node.getStructuresInLeafs()) {
                belongsToNode.put(mol, node);
            }
        }

        int index = 0;
        for (Molecule mol : molecules) {
            for (Molecule mol2 : molecules) {
                if (belongsToNode.get(mol) == belongsToNode.get(mol2)) {
                    bitVector.set(index);
                    index++;
                }
            }
        }

        return bitVector;
    }

    /**
     * Returns the molecules of the leafs below each cluster root
     * 
     * @param clusters
     *            the cluster roots
     * @return the molecules
     */
    protected List<Molecule> getMolecules(Collection<HierarchicalClusterNode<Molecule>> clusters) {
        List<Molecule> molecules = new LinkedList<Molecule>();
        for (HierarchicalClusterNode<Molecule> node : clusters) {
            molecules.addAll(node.getStructuresInLeafs());
        }
        return molecules;
    }

    /**
     * Instantiates a new Worker Object of the implementing Subclass
     * 
     * @param levels1
     *            the levels of the first clustering
     * @param levels2
     *            the levels of the second clustering
     * @param level
     *            the level used (0 to n-1)
     * @return the Worker Object
     */
    protected abstract Worker getNewWorker(ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels1,
            ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels2, int level);

    /**
     * Abstract Callable that must be returned by getNewWorker. Runs one single
     * level comparison.
     * 
     * @author Till Schäfer
     * 
     */
    protected abstract class Worker implements Callable<SimpleEntry<String, String>> {
        protected ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels1;
        protected ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels2;
        /**
         * the level of the dendrogram
         */
        protected int level;

        public Worker(ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels1,
                ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels2, int level) {
            super();

            this.levels1 = levels1;
            this.levels2 = levels2;
            this.level = level;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public abstract SimpleEntry<String, String> call() throws Exception;
    }
}
