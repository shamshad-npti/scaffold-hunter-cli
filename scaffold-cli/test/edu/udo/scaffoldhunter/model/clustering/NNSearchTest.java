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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import junit.framework.Assert;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.clustering.BestFrontierNNSearch.BestFrontierParameters;
import edu.udo.scaffoldhunter.model.clustering.MatrixNNSearch.MatrixParameters;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.MoleculeNumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * JUnit test class for {@link NNSearch} strategies
 * 
 * @author Till Schäfer
 */
public class NNSearchTest {
    /**
     * Compare the NNSearch results of the MAtrix NNSearch with an exact Best
     * Frontier Search (each Molecules is also a {@link Pivot} + no frontier
     * bound)
     */
    @org.junit.Test
    public void matrixVsExactBestFrontier() {
        Random rand = new Random();
        MockPropertyDefinition propDef1 = new MockPropertyDefinition("Test Property 1", "", PropertyType.NumProperty,
                "test_1", false, false);
        MockPropertyDefinition propDef2 = new MockPropertyDefinition("Test Property 2", "", PropertyType.NumProperty,
                "test_2", false, false);
        Collection<PropertyDefinition> propDefs = Lists.newLinkedList();
        propDefs.add(propDef1);
        propDefs.add(propDef2);

        propDef1.setId(1);
        propDef2.setId(2);

        LinkedList<HierarchicalClusterNode<Molecule>> nodes = Lists.newLinkedList();

        for (int i = 0; i < 100; i++) {
            Molecule mol = new Molecule();
            MoleculeNumProperty prop1 = new MoleculeNumProperty(propDef1, rand.nextDouble());
            mol.getNumProperties().put(1, prop1);
            MoleculeNumProperty prop2 = new MoleculeNumProperty(propDef2, rand.nextDouble());
            mol.getNumProperties().put(2, prop2);
            nodes.add(new HierarchicalClusterNode<Molecule>(mol));
        }

        NNSearch<Molecule> matrixNNSearch;
        NNSearch<Molecule> bestFrontierNNSearch;
        try {
            matrixNNSearch = NNSearchs.MATRIX.generateNNSearch(Linkages.CENTROID_LINKAGE, Distances.EUCLIDE, propDefs,
                    nodes, new MatrixParameters());
            bestFrontierNNSearch = NNSearchs.BEST_FRONTIER.generateNNSearch(Linkages.CENTROID_LINKAGE,
                    Distances.EUCLIDE, propDefs, nodes, new BestFrontierParameters(Integer.MAX_VALUE,
                            Integer.MAX_VALUE, 1));
        } catch (ClusteringException e) {
            AssertionError err = new AssertionError("creating NNSearch failed");
            err.initCause(e);
            throw err;
        }

        for (HierarchicalClusterNode<Molecule> node1 : nodes) {
            try {
                SimpleEntry<HierarchicalClusterNode<Molecule>, Double> matrixNN = matrixNNSearch.getNNAndDist(node1);
                SimpleEntry<HierarchicalClusterNode<Molecule>, Double> bestFrontierNN = bestFrontierNNSearch
                        .getNNAndDist(node1);

                // both NN must have the same distance
                Assert.assertEquals(matrixNN.getValue(), bestFrontierNN.getValue());

                /*
                 * if the NNs are not the same: dist(node1, nn1) must be the
                 * same as dist(node1, nn2)
                 */
                if (matrixNN.getKey().getContent() != bestFrontierNN.getKey().getContent()) {
                    Assert.assertEquals(matrixNNSearch.getDist(node1, bestFrontierNN.getKey()),
                            bestFrontierNN.getValue());
                }
            } catch (ClusteringException e) {
                AssertionError err = new AssertionError("calculating NN and dist failed");
                err.initCause(e);
                throw err;
            }
        }
    }
}
