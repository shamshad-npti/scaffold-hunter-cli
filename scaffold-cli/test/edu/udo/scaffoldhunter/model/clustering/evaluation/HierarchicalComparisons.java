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

package edu.udo.scaffoldhunter.model.clustering.evaluation;

import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Session;

/**
 * Specify and generate a {@link HierarchicalComparison} (as a replacement for
 * function pointers)
 * 
 * @author Till Schäfer
 * 
 */
public enum HierarchicalComparisons {
    // TODO: FILL WITH CONTENT
    /**
     * @see FowlkesMallowsComparison
     */
    FOWLKES_MALLOWS {
        @Override
        public HierarchicalComparison generateComparison(HierarchicalClusterNode<Molecule> root1,
                HierarchicalClusterNode<Molecule> root2, Session session, EvaluationResult result, int stepWith) {
            return new FowlkesMallowsComparison(root1, root2, result, stepWith);
        }

        @Override
        public String getDescription() {
            return "Fowlkes Mallow criterion";
        }
    },
    /**
     * @see JaccardComparison
     */
    JACCARD {
        @Override
        public HierarchicalComparison generateComparison(HierarchicalClusterNode<Molecule> root1,
                HierarchicalClusterNode<Molecule> root2, Session session, EvaluationResult result, int stepWith) {
            return new JaccardComparison(root1, root2, result, stepWith);
        }

        @Override
        public String getDescription() {
            return "Jaccard criterion";
        }
    }, 
    /**
     * @see NVIComparison
     */
    NVI {
        @Override
        public HierarchicalComparison generateComparison(HierarchicalClusterNode<Molecule> root1,
                HierarchicalClusterNode<Molecule> root2, Session session, EvaluationResult result, int stepWith) {
            return new NVIComparison(root1, root2, result, stepWith);
        }

        @Override
        public String getDescription() {
            return "Normalized Variation of Information criterion";
        }
        
    };

    /**
     * Returns a {@link HierarchicalComparison} if the specified type
     * 
     * @param root1
     *            root cluster node of the first clustering result
     * @param root2
     *            root cluster node of the second clustering result
     * @param session
     *            the {@link Session} of the clustering
     * @param result
     *            the result with the informations about the clusterings
     * @param stepWith
     *            Each n-th level should be measured. i.e. stepWith=1 means
     *            every level is measured.
     * @return a {@link HierarchicalComparison} if the specified type
     */
    public abstract HierarchicalComparison generateComparison(HierarchicalClusterNode<Molecule> root1,
            HierarchicalClusterNode<Molecule> root2, Session session, EvaluationResult result, int stepWith);

    /**
     * Returns the a description for the comparison method
     * 
     * @return description
     */
    public abstract String getDescription();
}
