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

import java.util.List;

import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Molecule;

/**
 * Coningency matrix is defined as n_ij = |clusterA_i \cap clusterB_j| where
 * clusterA_i is the i-th cluster from the fist clustering and clusterB_j the
 * j-th cluster form the second clustering.
 * 
 * @author Till Schäfer
 * 
 */
public class ContingencyMatrix {

    private final int[][] matrix;
    private final int[] sizesI;
    private final int[] sizesJ;

    /**
     * Constructor
     * 
     * @param clustersI
     * @param clustersJ
     */
    public ContingencyMatrix(List<HierarchicalClusterNode<Molecule>> clustersI,
            List<HierarchicalClusterNode<Molecule>> clustersJ) {
        matrix = new int[clustersI.size()][clustersJ.size()];
        sizesI = new int[clustersI.size()];
        sizesJ = new int[clustersJ.size()];

        int i = 0;
        for (HierarchicalClusterNode<Molecule> clusterI : clustersI) {
            List<Molecule> structures1 = clusterI.getStructuresInLeafs();
            sizesI[i] = clusterI.getClusterSize();

            int j = 0;
            for (HierarchicalClusterNode<Molecule> clusterJ : clustersJ) {
                List<Molecule> structures2 = clusterJ.getStructuresInLeafs();
                sizesJ[j] = clusterJ.getClusterSize();

                structures2.retainAll(structures1);
                matrix[i][j] = structures2.size();

                j++;
            }
            i++;
        }
    }

    /**
     * Returns the contingency value n_ij
     * 
     * @param i
     *            index i
     * @param j
     *            index j
     * @return n_ij
     */
    public int getContingencyValue(int i, int j) {
        return matrix[i][j];
    }

    /**
     * Return the size of the i-th cluster in clustering I
     * 
     * @param i
     *            index
     * @return the cluster size
     */
    public int getClusterSizeI(int i) {
        return sizesI[i];
    }

    /**
     * Return the size of the j-th cluster in clustering J
     * 
     * @param j
     *            index
     * @return the cluster size
     */
    public int getClusterSizeJ(int j) {
        return sizesJ[j];
    }

    /**
     * Return the number of clusters in clustering I
     * 
     * @return the number of clusters
     */
    public int sizeI() {
        return matrix.length;
    }

    /**
     * Return the number of clusters in clustering J
     * 
     * @return the number of clusters
     */
    public int sizeJ() {
        if (sizeI() > 0) {
            return matrix[0].length;
        } else {
            return 0;
        }
    }
}
