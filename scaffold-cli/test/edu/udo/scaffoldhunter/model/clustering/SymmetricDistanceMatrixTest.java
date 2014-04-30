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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.MoleculeNumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * @author Philipp Kopp
 * @author Till Schäfer
 * 
 */
public class SymmetricDistanceMatrixTest {
    private SymmetricDistanceMatrix<Molecule> matrix;
    private Distance<Molecule> dist;
    private HierarchicalClusterNode<Molecule> node1, node2;
    private List<PropertyDefinition> propertyVector;

    /**
     * Initializes Data for the Test
     * 
     * @throws ClusteringException
     */
    @org.junit.Before
    public void initSymDistMatTest() throws ClusteringException {
        MockPropertyDefinition propdef1 = new MockPropertyDefinition();
        MockPropertyDefinition propdef2 = new MockPropertyDefinition();
        propdef1.setId(1);
        propdef1.setPropertyType(PropertyType.NumProperty);
        propdef2.setId(2);
        propdef2.setPropertyType(PropertyType.NumProperty);

        propertyVector = new ArrayList<PropertyDefinition>();

        propertyVector.add(propdef1);
        propertyVector.add(propdef2);
        dist = new Euclide<Molecule>(propertyVector);

        node1 = generateHCN(propdef1, propdef2);
        node2 = generateHCN(propdef1, propdef2);

        ArrayList<HierarchicalClusterNode<Molecule>> cluster = new ArrayList<HierarchicalClusterNode<Molecule>>();
        cluster.add(node1);
        cluster.add(node2);

        for (int i = 0; i < 100; i++) {
            cluster.add(generateHCN(propdef1, propdef2));
        }

        matrix = new SymmetricDistanceMatrix<Molecule>(dist, cluster);
    }

    /**
     * Generate a random HierarchicalClusterNode with two euclidian properties
     * 
     * @param propdef1
     *            the first {@link PropertyDefinition}
     * @param propdef2
     *            the second {@link PropertyDefinition}
     */
    private HierarchicalClusterNode<Molecule> generateHCN(MockPropertyDefinition propdef1,
            MockPropertyDefinition propdef2) {
        MoleculeNumProperty prop1 = new MoleculeNumProperty(propdef1, Math.random());
        MoleculeNumProperty prop2 = new MoleculeNumProperty(propdef2, Math.random());

        Map<Integer, MoleculeNumProperty> numProp = new HashMap<Integer, MoleculeNumProperty>();
        numProp.put(propdef1.getId(), prop1);
        numProp.put(propdef2.getId(), prop2);

        Molecule mol = new Molecule();
        mol.setNumProperties(numProp);
        return new HierarchicalClusterNode<Molecule>(mol);
    }

    /**
     * Tests if the DistanceCalculation of the matrix is working
     * 
     * @throws ClusteringException
     */
    @org.junit.Test
    public void getDistanceTestDistanceCalculation() throws ClusteringException {
        if (matrix.getDist(node1, node2) != dist.calcDist(node1, node2)) {
            Assert.fail();
        }
    }

    /**
     * Tests if the symmetry of the matrix is working
     * 
     * @throws ClusteringException
     */
    @org.junit.Test
    public void getDistanceTestReversal() throws ClusteringException {
        if (matrix.getDist(node1, node2) != matrix.getDist(node2, node1)) {
            Assert.fail();
        }
    }

    /**
     * tests the set of a new distance
     * 
     * @throws ClusteringException
     */
    @org.junit.Test
    public void setDistTest() throws ClusteringException {
        matrix.setDist(node1, node2, 10);
        if (matrix.getDist(node1, node2) != 10) {
            Assert.fail();
        }
        if (matrix.getDist(node2, node1) != 10) {
            Assert.fail();
        }
    }

    /**
     * Tests if the merging is done correctly (in terms of containing
     * {@link Structure}s only)
     */
    @org.junit.Test
    public void mergeContainsTest() {
        Collection<HierarchicalClusterNode<Molecule>> nodes = Lists.newLinkedList(matrix.getStoredNodes());
        Linkage<Molecule> centroid = new CentroidLinkage<Molecule>(propertyVector);

        int initialSize = nodes.size();

        Iterator<HierarchicalClusterNode<Molecule>> iterator;
        while (nodes.size() > 1) {
            iterator = nodes.iterator();
            HierarchicalClusterNode<Molecule> first = iterator.next();
            HierarchicalClusterNode<Molecule> second = iterator.next();

            assertTrue(matrix.getStoredNodes().contains(first));
            assertTrue(matrix.getStoredNodes().contains(second));
            assertEquals(initialSize, matrix.getStoredNodes().size());

            HierarchicalClusterNode<Molecule> merged = matrix.mergeNodes(first, second, centroid.getUpdateFormula());

            nodes.remove(first);
            nodes.remove(second);
            nodes.add(merged);

            assertTrue(matrix.getStoredNodes().contains(merged));
            assertFalse(matrix.getStoredNodes().contains(first));
            assertFalse(matrix.getStoredNodes().contains(second));
            assertEquals(--initialSize, matrix.getStoredNodes().size());

        }
    }

}
