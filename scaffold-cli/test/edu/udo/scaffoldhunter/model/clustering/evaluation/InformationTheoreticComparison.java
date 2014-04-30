/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

import java.util.Collection;

import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Molecule;

/**
 * Provides some methods for information theoretic comparisons
 * 
 * @author Till Schäfer
 */
public abstract class InformationTheoreticComparison extends HierarchicalComparison {

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
    public InformationTheoreticComparison(HierarchicalClusterNode<Molecule> root1,
            HierarchicalClusterNode<Molecule> root2, EvaluationResult result, int stepWith) {
        super(root1, root2, result, stepWith);
    }

    /**
     * Calculates the entropy of the clusters
     * 
     * @param clusters
     *            the clusters (must be from the same clustering as root1 or
     *            root2)
     * @return the entropy
     */
    protected double entropy(Collection<HierarchicalClusterNode<Molecule>> clusters) {
        int overallSize = root1.getClusterSize();
        double retVal = 0;

        for (HierarchicalClusterNode<Molecule> node : clusters) {
            int ai = node.getStructuresInLeafs().size();
            double normalisedAi = (double) ai / overallSize;

            retVal += normalisedAi * Math.log(normalisedAi);
        }

        return -retVal;
    }

    /**
     * Calculates the entropy of the clusters
     * 
     * @param matrix
     *            the {@link ContingencyMatrix} (must be from the same
     *            clustering as root1 or root2)
     * @return the joint entropy
     */
    protected double jointEntropy(ContingencyMatrix matrix) {
        int overallSize = root1.getClusterSize();
        double retVal = 0;

        for (int i = 0; i < matrix.sizeI(); i++) {
            for (int j = 0; j < matrix.sizeJ(); j++) {
                double normalizedNij = (double) matrix.getContingencyValue(i, j) / overallSize;
                if (normalizedNij != 0) {
                    retVal += normalizedNij * Math.log(normalizedNij);
                }
            }
        }

        return -retVal;
    }

    /**
     * Calculates the conditional entropy H
     * 
     * @param matrix
     *            the {@link ContingencyMatrix} (must be from the same
     *            clustering as root1 or root2)
     * @param invertCondition
     *            false=H(I|J) and true=H(J|I)
     * @return the conditional entropy
     */
    protected double conditionalEntropy(ContingencyMatrix matrix, boolean invertCondition) {
        double overallSize = root1.getClusterSize();
        double retVal = 0;

        for (int i = 0; i < matrix.sizeI(); i++) {
            for (int j = 0; j < matrix.sizeJ(); j++) {
                double nij = matrix.getContingencyValue(i, j);
                double clusterSize = invertCondition ? matrix.getClusterSizeI(i) : matrix.getClusterSizeJ(j);

                assert clusterSize != 0;
                if (nij != 0) {
                    retVal += nij / overallSize * Math.log((nij / overallSize) / (clusterSize / overallSize));
                }
            }
        }

        return -retVal;
    }

    /**
     * Calculates the mutual information
     * 
     * @param matrix
     *            the {@link ContingencyMatrix} (must be from the same
     *            clustering as root1 or root2)
     * @return the mutual information
     */
    protected double mutualInformation(ContingencyMatrix matrix) {
        double overallSize = root1.getClusterSize();
        double retVal = 0;

        for (int i = 0; i < matrix.sizeI(); i++) {
            for (int j = 0; j < matrix.sizeJ(); j++) {
                double nij = matrix.getContingencyValue(i, j);
                double multClusterSizes = matrix.getClusterSizeI(i) * matrix.getClusterSizeJ(j);

                assert multClusterSizes != 0;
                if (nij != 0) {
                    retVal += nij / overallSize
                            * Math.log((nij / overallSize) / (multClusterSizes / Math.pow(overallSize, 2)));
                }
            }
        }

        return retVal;
    }
}
