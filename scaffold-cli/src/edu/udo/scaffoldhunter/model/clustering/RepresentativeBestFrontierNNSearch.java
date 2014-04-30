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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * TODO
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete Structure
 * 
 */
public class RepresentativeBestFrontierNNSearch<S extends Structure> extends BestFrontierNNSearch<S> {
    private static Logger logger = LoggerFactory.getLogger(RepresentativeBestFrontierNNSearch.class);

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
     * @param parameters
     *            the
     *            {@link edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters}
     * @throws ClusteringException
     *             if creation of {@link SymmetricDistanceMatrix} failed
     */
    public RepresentativeBestFrontierNNSearch(Distance<S> distance, Linkage<S> linkage,
            Collection<PropertyDefinition> propertyVector, Collection<HierarchicalClusterNode<S>> singletons,
            BestFrontierParameters parameters) throws ClusteringException {
        super(distance, linkage, propertyVector, singletons, parameters);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NNSearch#getNNAndDist(edu.udo
     * .scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public SimpleEntry<HierarchicalClusterNode<S>, Double> getNNAndDist(HierarchicalClusterNode<S> node)
            throws ClusteringException {
        Preconditions.checkNotNull(node);
        
        SimpleEntry<HierarchicalClusterNode<S>, Double> nnEntry = calcNNAndDist(node);
        if (nnEntry == null) {
            return null;
        } else {
            return new SimpleEntry<HierarchicalClusterNode<S>, Double>(nnEntry.getKey(), distance.calcDist(node, nnEntry.getKey()));
        }
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
    public HierarchicalClusterNode<S> merge(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2)
            throws ClusteringException {
        Preconditions.checkArgument(currentLevelContains(node1));
        Preconditions.checkArgument(currentLevelContains(node2));

        logger.debug(
                "size1 = {}, size2 = {}; real dist = {}, estimated dist = {}",
                new Object[] { node1.getClusterSize(), node2.getClusterSize(), distance.calcDist(node1, node2),
                        pTree.estimatedDistance(node1, node2) });

        HierarchicalClusterNode<S> mergedNode = pTree.merge(node1, node2, linkage.getUpdateFormula(),
                distance.calcDist(node1, node2));

        S mergedContent = linkage.doContentMerge(node1.getContent(), node2.getContent(), node1.getClusterSize(),
                node2.getClusterSize());
        mergedNode.setContent(mergedContent);

        currentLevelNodes.remove(node1);
        currentLevelNodes.remove(node2);
        currentLevelNodes.add(mergedNode);
        
        // clean content from children to avoid unnecessary memory consumption
        if (!node1.isLeaf()) {
            node1.setContent(null);
        }
        if (!node2.isLeaf()) {
            node2.setContent(null);
        }

        return mergedNode;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#getDist(edu.udo.
     * scaffoldhunter.model.clustering.HierarchicalClusterNode,
     * edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public double getDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) throws ClusteringException {
        return distance.calcDist(node1, node2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#acceptedLinkages()
     */
    @Override
    public Collection<Linkages> acceptedLinkages() {
        return NNSearchs.REPRESENTATIVE_BEST_FRONTIER.acceptedLinkages();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#accpetedDistances()
     */
    @Override
    public Collection<Distances> accpetedDistances() {
        return NNSearchs.REPRESENTATIVE_BEST_FRONTIER.acceptedDistances();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.NNSearch#getDefaultConfig()
     */
    @Override
    public NNSearchParameters getDefaultParameters() {
        return NNSearchs.REPRESENTATIVE_BEST_FRONTIER.getDefaultParameters();
    }
}
