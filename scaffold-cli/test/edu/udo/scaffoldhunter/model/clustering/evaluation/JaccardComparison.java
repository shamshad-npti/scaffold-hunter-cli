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
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Molecule;

/**
 * @author Till Schäfer
 * 
 */
public class JaccardComparison extends HierarchicalComparison {
    private static Logger logger = LoggerFactory.getLogger(JaccardComparison.class);

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
    public JaccardComparison(HierarchicalClusterNode<Molecule> root1, HierarchicalClusterNode<Molecule> root2,
            EvaluationResult result, int stepWith) {
        super(root1, root2, result, stepWith);
    }
    
    /**
     * Worker class for parallel execution of the Jaccard calculations
     */
    protected class JaccardWorker extends Worker {
        public JaccardWorker(ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels1,
                ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels2, int level) {
            super(levels1, levels2, level);
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public SimpleEntry<String, String> call() throws Exception {
            // the flat clusterings on level x
            LinkedList<HierarchicalClusterNode<Molecule>> flatClustering1 = levels1.get(level);
            LinkedList<HierarchicalClusterNode<Molecule>> flatClustering2 = levels2.get(level);

            logger.debug("Cluster on level {} : {}", level, flatClustering1.size());

            BitSet bits1 = getPairsBitVector(flatClustering1);
            BitSet bits2 = getPairsBitVector(flatClustering2);
            BitSet intersection = (BitSet) bits1.clone();
            BitSet union = (BitSet) bits1.clone();
            intersection.and(bits2);
            union.or(bits2);

            double value = (double) intersection.cardinality() / union.cardinality();
            if (union.cardinality() == 0) {
                value = 1;
            }

            logger.debug("measurements for level {}: {}", level, value);
            // k = i+1
            return new SimpleEntry<String, String>(Integer.toString(level * stepWith + 1), Double.toString(value));
        }
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.clustering.evaluation.HierarchicalComparison#getNewWorker(java.util.ArrayList, java.util.ArrayList, int)
     */
    @Override
    protected Worker getNewWorker(ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels1,
            ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> levels2, int level) {
        return new JaccardWorker(levels1, levels2, level);
    }
}
