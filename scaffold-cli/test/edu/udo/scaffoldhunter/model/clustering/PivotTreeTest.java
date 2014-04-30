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

import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import junit.framework.Assert;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.MoleculeNumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * JUnit Tests for the {@link PivotTree}
 * 
 * @author Till Schäfer
 */
public class PivotTreeTest {
    /**
     * Tests if the {@link Distance} between two each {@link Molecule} is
     * estimated exactly if each {@link Molecule} is also a pivot
     */
    @org.junit.Test
    public void ExactDistanceEstimationTest() {
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

        Distance<Molecule> dist = Distances.EUCLIDE.generateDistance(propDefs);
        PivotTree<Molecule> pTree;
        try {
            pTree = new PivotTree<Molecule>(new RandomSampler<Molecule>(Integer.MAX_VALUE, null),
                    new MaxSizeLeafSelection<Molecule>(), 1, nodes, dist, false);
        } catch (ClusteringException e) {
            AssertionError err = new AssertionError("creating pivot tree failed");
            err.initCause(e);
            throw err;
        }

        for (HierarchicalClusterNode<Molecule> node1 : nodes) {
            for (HierarchicalClusterNode<Molecule> node2 : nodes) {
                try {
                    Assert.assertEquals(pTree.estimatedDistance(node1, node2), dist.calcDist(node1, node2));
                } catch (ClusteringException e) {
                    AssertionError err = new AssertionError("calculate exact distance failed");
                    err.initCause(e);
                    throw err;
                }
            }
        }
    }
}
