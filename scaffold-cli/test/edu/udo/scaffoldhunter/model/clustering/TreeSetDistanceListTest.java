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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import junit.framework.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.db.Molecule;

/**
 * Tests the {@link TreeSetDistanceList}
 * 
 * @author Till Schäfer
 */
public class TreeSetDistanceListTest {

    /**
     * Tests if the distances by calling PushFrontier are monotonically
     * increasing and if all stored nodes are returned
     */
    @org.junit.Test
    public void testPushFrontierOrdering() {
        TreeSetDistanceList<Molecule> distanceList = new TreeSetDistanceList<Molecule>();
        ArrayList<HierarchicalClusterNode<Molecule>> hcns = Lists.newArrayList();
        HashMap<HierarchicalClusterNode<Molecule>, Integer> distances = Maps.newHashMap();
        
        /*
         * Number of HCNs exclusive the pivot HCN (must be even!)
         */
        int numberOfNodes = 100;
        for (int i = 0; i <= numberOfNodes; i++) {
            HierarchicalClusterNode<Molecule> hcn = new MockHierarchicalClusterNode();
            hcns.add(hcn);
            /*
             * distances are in [-count/2;count/2]
             */
            distanceList.add(hcn, i - numberOfNodes / 2);
            distances.put(hcn, i - numberOfNodes / 2);
        }

        // start the frontier with the pivot (distance 0)
        Collection<HierarchicalClusterNode<Molecule>> frontierNodes = distanceList.startNewFrontier(hcns.get(numberOfNodes / 2));
        
        int count = 2;
        for (HierarchicalClusterNode<Molecule> hcn : frontierNodes) {
            HierarchicalClusterNode<Molecule> oldNode = hcn;
            HierarchicalClusterNode<Molecule> nextNode = distanceList.pushFrontier(oldNode);;
            
            while (nextNode != null) {
                count ++;
                Integer oldVal = distances.get(oldNode);
                Integer nextVal = distances.get(nextNode);
                Assert.assertTrue("sorting wrong", Math.abs(oldVal) < Math.abs(nextVal));
                
                oldNode = nextNode;
                nextNode = distanceList.pushFrontier(oldNode);
            } 
        }
        Assert.assertEquals(100, count);
    }

}
