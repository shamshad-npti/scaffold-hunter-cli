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

package edu.udo.scaffoldhunter.view.dendrogram;

import java.util.Iterator;
import java.util.Vector;

import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * @author Philipp Lewe
 * 
 */
public class DendrogramDebug {

    /**
     * Generates a random dendrogram based on the molecules in the given subset.
     * Useful for debugging zoom functions
     * 
     * @param subset
     *            the subset to cluster
     * @return the root cluster node
     */
    public static HierarchicalClusterNode<Structure> GenerateTestDendrogram(Subset subset) {

        assert (subset.getMolecules().size() == 0) : "empty subset";

        Vector<Structure> structures = new Vector<Structure>();

        structures.ensureCapacity(subset.getMolecules().size());
        structures.addAll(subset.getMolecules());

        Vector<HierarchicalClusterNode<Structure>> result = (recursiveTreeGen(createLeafNodes(structures)));
        assert (result.size() == 1) : "recursiveTreeGen should return exactly one DendrogramModelNode";

        return (result.firstElement());
    }

    private static Vector<HierarchicalClusterNode<Structure>> createLeafNodes(Vector<Structure> structures) {
        Vector<HierarchicalClusterNode<Structure>> leafNodes = new Vector<HierarchicalClusterNode<Structure>>();

        for (Structure structure : structures) {
            HierarchicalClusterNode<Structure> node = new HierarchicalClusterNode<Structure>(structure);
            leafNodes.add(node);
        }
        return leafNodes;
    }

    private static Vector<HierarchicalClusterNode<Structure>> recursiveTreeGen(
            Vector<HierarchicalClusterNode<Structure>> structures) {
        Vector<HierarchicalClusterNode<Structure>> nodes = new Vector<HierarchicalClusterNode<Structure>>();
        nodes.ensureCapacity(structures.size() / 2 + 1);

        // skip first element if odd number of elements
        if (((structures.size() % 2) == 1)) {
            nodes.add(structures.firstElement());
            structures.remove(structures.firstElement());
        }

        // melt two nodes to one
        Iterator<HierarchicalClusterNode<Structure>> nodeIter = structures.iterator();
        while (nodeIter.hasNext()) {
            HierarchicalClusterNode<Structure> node1 = nodeIter.next();
            nodeIter.remove();
            HierarchicalClusterNode<Structure> node2 = nodeIter.next();
            nodeIter.remove();

            HierarchicalClusterNode<Structure> modelNode = new HierarchicalClusterNode<Structure>(node1, node2,
                    (20 + 100 * Math.random()));
            nodes.add(modelNode);
        }

        // if more than 1 element exists, melt recursively
        if (nodes.size() > 1) {
            return recursiveTreeGen(nodes);
        } else {
            return nodes;
        }
    }

}
