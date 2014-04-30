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
import java.util.ListIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.LinkedHashList;

/**
 * This implementation of the {@link NNSearch} interface is based on a n x n
 * distance Matrix. It maintains a ordering in the current level
 * {@link HierarchicalClusterNode}s an performs the NN query only to descending
 * {@link HierarchicalClusterNode}s. Therefore it should save about the half of
 * the Runtime compared to {@link MatrixNNSearch}.
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class ForwardNNSearch<S extends Structure> extends MatrixNNSearch<S> {
    private static Logger logger = LoggerFactory.getLogger(ForwardNNSearch.class);

    /**
     * The ordering of all {@link HierarchicalClustering} (which is needed to
     * perform the forward search)
     */
    private LinkedHashList<HierarchicalClusterNode<S>> clusterNodes = new LinkedHashList<HierarchicalClusterNode<S>>();

    /**
     * Constructor
     * 
     * @param distance
     *            the {@link Distance} measure
     * @param linkage
     *            the used {@link Linkage}
     * @param propertyVector
     *            the {@link Property}s used for clustering
     * @param singletons
     *            the singleton clusters
     * @throws ClusteringException
     *             if creation of {@link SymmetricDistanceMatrix} failed
     */
    public ForwardNNSearch(Distance<S> distance, Linkage<S> linkage, Collection<PropertyDefinition> propertyVector,
            Collection<HierarchicalClusterNode<S>> singletons) throws ClusteringException {
        super(distance, linkage, propertyVector, singletons);

        clusterNodes.addAll(singletons);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#merge(edu.udo.scaffoldhunter
     * .model.clustering.HierarchicalClusterNode,
     * edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public HierarchicalClusterNode<S> merge(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) {
        logger.trace("Entering merge()");

        logger.debug("size1 = {}, size2 = {}; real dist = {}",
                new Object[] { node1.getClusterSize(), node2.getClusterSize(), matrix.getDist(node1, node2), });

        HierarchicalClusterNode<S> mergedNode = matrix.mergeNodes(node1, node2, linkage.getUpdateFormula());

        /*
         * If the reducibility property is not fulfilled we must insert the
         * mergedNode at the fist position to ensure the correctness for
         * GenericClustering. Otherwise it is possible to replace node1 or node2
         * (saves some more computations in getNN)
         */
        if (linkage.fulfilReducibility()) {
            clusterNodes.listIterator(node2).set(mergedNode);
            clusterNodes.remove(node1);
        } else {
            clusterNodes.remove(node1);
            clusterNodes.remove(node2);
            clusterNodes.add(0, mergedNode);
        }

        return mergedNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#getNN(edu.udo.scaffoldhunter
     * .model.clustering.HierarchicalClusterNode)
     */
    @Override
    public HierarchicalClusterNode<S> getNN(HierarchicalClusterNode<S> node) {
        HierarchicalClusterNode<S> nn = null;

        /*
         * Iterate over clusterNodes. Start with the position of node. (forward
         * search only)
         */
        ListIterator<HierarchicalClusterNode<S>> it = clusterNodes.listIterator(node);
        while (it.hasNext()) {
            HierarchicalClusterNode<S> currentNode = it.next();

            assert currentNode != null;

            // prevent comparison with the same node
            if (!node.equals(currentNode)) {
                if (nn == null || isSmaller(currentNode, nn, node)) {
                    // replace NN if the new one is closer
                    nn = currentNode;
                }
            }
        }

        return nn;
    }
}
