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

import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.ProgressListener;
import edu.udo.scaffoldhunter.util.ProgressSupport;

/**
 * 
 * Implementation of the NNChain Algorithm described in Murtagh,
 * Multidimensional Clustering Algorithms (chapter 3)
 * 
 * This algorithms solves SAHN clustering in O(n²) time and O(n) or O(n²) space
 * (depending on the used {@link NNSearch})
 * 
 * @author Till Schäfer
 * 
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class NNChain<S extends Structure> implements HierarchicalClustering<S> {
    private static Logger logger = LoggerFactory.getLogger(NNChain.class);

    NNSearch<S> nnSearch;

    /**
     * All listeners that will be informed about the progress of the clustering.
     */
    private ProgressSupport<HierarchicalClusterNode<S>> progressListeners = new ProgressSupport<HierarchicalClusterNode<S>>();

    /**
     * Constructor.
     * 
     * @param nnSearch
     *            the {@link NNSearch} strategy
     * 
     */
    public NNChain(NNSearch<S> nnSearch) {
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
         * The Chain of Nearest Neighbours
         */
        LinkedList<HierarchicalClusterNode<S>> nnChain = new LinkedList<HierarchicalClusterNode<S>>();

        /*
         * The next NNChain element
         * 
         * Candidate for a new nnChain Member. It is not added to nnChain if it
         * is reciprocal with the last NNChain element. In this case it is
         * directly merged.
         */
        HierarchicalClusterNode<S> nnChainCandidate = null;

        /*
         * count of singleton clusters
         */
        int initialSize = nnSearch.size();

        /*
         * how much progress in percent is done by one agglomeration step
         */
        float progressStep = 100 / ((float) initialSize);

        // Init Listeners
        progressListeners.setProgressBounds(0, 100);
        progressListeners.setProgressValue(0);

        /*
         * clustering is complete if only one Element remains
         * 
         * INVARIANT: (valid nnChainCandidate && nnChain.size >= 1) ||
         * nnChain.isEmpty
         * 
         * valid nnChain: nnChain[i+1] is nearest neighbour of nnChain[i] for i
         * in [0, nnChain.size() - 1]
         */
        while (nnSearch.size() > 1) {

            logger.debug("nnSearch.size: {}", nnSearch.size());
            logger.debug("nnChain.size: {}", nnChain.size());
            // if the chain is empty, insert an arbitrary cluster node
            if (nnChain.isEmpty()) {
                nnChain.add(nnSearch.getCurrentLevelNodes().iterator().next());
                nnChainCandidate = nnSearch.getNN(nnChain.getLast());
            } else {
                assert nnSearch.currentLevelContains(nnChainCandidate);
                // Calc NN of Candidate
                HierarchicalClusterNode<S> nn = nnSearch.getNN(nnChainCandidate);
                assert nnSearch.currentLevelContains(nn);

                /*
                 * found a pair of reciprocal NNs -> merge them to a new cluster
                 * 
                 * we cant get loops because we do resolve similar distance with hashCode comparison in MatrixNNSearch
                 */
                if (nn == nnChain.getLast()) {
                    // merge NN and remove the old cluster from chain
                    nnSearch.merge(nnChain.pollLast(), nnChainCandidate);

                    if (nnChain.size() >= 2) {
                        // the last nnChain element is the new candidate
                        nnChainCandidate = nnChain.pollLast();
                    } else if (!nnChain.isEmpty()) {
                        /*
                         * if only one element is in the nnChain we calc a new
                         * NN (see invariant). There must be a cluster that is
                         * in clusterNodes and not in nnChain because we formed
                         * newMergedCluster before
                         */
                        nnChainCandidate = nnSearch.getNN(nnChain.getLast());
                    }
                } else { // grow the chain
                    assert !nnChain.contains(nnChainCandidate);
                    nnChain.addLast(nnChainCandidate);
                    nnChainCandidate = nn;
                }
            }
            // Update Progress in %
            progressListeners.setProgressValue((int) ((initialSize - nnSearch.size()) * progressStep));
            logger.debug("Progress: {}%", (initialSize - nnSearch.size()) * progressStep);

            if (Thread.interrupted()) {
                logger.trace("Thread interrupted");
                return null;
            }
        }

        logger.trace("finished clac");
        return nnSearch.getCurrentLevelNodes().iterator().next();
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
