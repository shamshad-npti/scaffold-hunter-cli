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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.MoleculeNumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Philipp Kopp
 * 
 */
public class EuclideTest {
    private Distance<Molecule> dist;
    private HierarchicalClusterNode<Molecule> node1, node2;
    private List<PropertyDefinition> oneElementVector, twoElementVector;

    /**
     * Prepare context
     */
    @org.junit.Before
    public void initEuclideTest() {
        PropertyDefinition propdef1 = new PropertyDefinition(null, null, PropertyType.NumProperty, "a", false, false);
        PropertyDefinition propdef2 = new PropertyDefinition(null, null, PropertyType.NumProperty, "b", false, false);

        oneElementVector = new ArrayList<PropertyDefinition>();
        twoElementVector = new ArrayList<PropertyDefinition>();

        oneElementVector.add(propdef1);
        twoElementVector.add(propdef1);
        twoElementVector.add(propdef2);

        MoleculeNumProperty prop1 = new MoleculeNumProperty(propdef1, 3);
        MoleculeNumProperty prop2 = new MoleculeNumProperty(propdef1, 5);
        MoleculeNumProperty prop3 = new MoleculeNumProperty(propdef2, 7);
        MoleculeNumProperty prop4 = new MoleculeNumProperty(propdef2, 9);

        Molecule mol1 = new Molecule();
        Molecule mol2 = new Molecule();

        mol1.getNumProperties().put(propdef1.getId(), prop1);
        mol2.getNumProperties().put(propdef1.getId(), prop2);
        mol1.getNumProperties().put(propdef2.getId(), prop3);
        mol2.getNumProperties().put(propdef2.getId(), prop4);
        

        node1 = new HierarchicalClusterNode<Molecule>(mol1);
        node2 = new HierarchicalClusterNode<Molecule>(mol2);

        ArrayList<HierarchicalClusterNode<Molecule>> cluster = new ArrayList<HierarchicalClusterNode<Molecule>>();
        cluster.add(node1);
        cluster.add(node2);
    }

    /**
     * Tests Euclidian Distance for a single {@link PropertyDefinition}
     * 
     * @throws ClusteringException
     */
    @org.junit.Test
    public void getDistanceSingleTest() throws ClusteringException {
        dist = new Euclide<Molecule>(oneElementVector);

        Assert.assertEquals(2.0, dist.calcDist(node1, node2));
    }
    
    /**
     * Tests Euclidian Distance for a multiple {@link PropertyDefinition}s
     * 
     * @throws ClusteringException
     */
    @org.junit.Test
    public void getDistanceMultipleTest() throws ClusteringException {
        dist = new Euclide<Molecule>(twoElementVector);
        
        Assert.assertEquals(Math.sqrt(8), dist.calcDist(node1, node2));
    }

}
