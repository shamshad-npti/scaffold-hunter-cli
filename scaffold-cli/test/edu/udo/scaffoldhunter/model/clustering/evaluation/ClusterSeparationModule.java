/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.clustering.SymmetricDistanceMatrix;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.NumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * This calculates the cluster separation on each level of the dendrodram.<br>
 * <br>
 * 
 * The cluster separation for two clusters is defined as the single-linkage
 * distance between the clusters divided by the maximum inner cluster distance
 * of both clusters.<br>
 * 
 * For each level all cluster separation values for each par of clusters are
 * averaged, maximized and minimized to three single values.
 * 
 * @author Till Schäfer
 * 
 */
public class ClusterSeparationModule extends MonotheticEvaluationModule {
    private static Logger logger = LoggerFactory.getLogger(ClusterSeparationModule.class);
    
    private int steps;
    private boolean fixedStepWith;

    /**
     * Constructor
     * 
     * @param db
     *            the database connection
     * @param session
     *            the used session
     * @param clustering
     *            the clustering algorithm to measure
     * @param nnSearch
     *            the {@link NNSearch} strategy used by the clustering algorithm
     * @param linkage
     *            the linkage used by the clustering algorithm
     * @param distance
     *            the distance used by the clustering algorithm
     * @param parameters
     *            the {@link NNSearchParameters}
     * @param maxUsedSimultaniuousProperties
     *            the maximum number of used {@link NumProperty}s for one
     *            measurement
     * @param maxSubsetSize
     *            the maximum subset size for one measurement
     * @param steps
     *            If fixedStepWith=true each n-th level should be measured. i.e.
     *            stepWidth=1 means every level is measured. If
     *            fixedStepWith=false n levels are measured only which are
     *            uniformly distributed among the available levels
     * @param fixedStepWith
     *            see steps description
     */
    public ClusterSeparationModule(DbManager db, Session session, HierarchicalClusterings clustering,
            NNSearchs nnSearch, NNSearchParameters parameters, Linkages linkage, Distances distance,
            int maxUsedSimultaniuousProperties, int maxSubsetSize, int steps, boolean fixedStepWith) {
        super(db, session, clustering, nnSearch, parameters, linkage, distance, maxUsedSimultaniuousProperties,
                maxSubsetSize, 4);
        this.steps = steps;
        this.fixedStepWith = fixedStepWith;
    }

    @Override
    protected EvaluationResult singleMeasurement(Subset subset, Collection<PropertyDefinition> usedPropDefs) {
        logger.info("Running cluster separation measurement for session '{}' and Subset '{}' with size {}",
                new Object[] { session.getTitle(), subset.getTitle(), subset.size() });

        EvaluationResult result = new EvaluationResult(session.getTitle(), session.getDataset().getTitle(),
                "cluster separation", subset.getTitle(), subset.size());
        result.addClustering(clustering, nnSearch, nnSearchParameters, linkage, distance, usedPropDefs);

        HashMap<Molecule, HierarchicalClusterNode<Molecule>> mappedNodes = Maps.newHashMap();
        SymmetricDistanceMatrix<Molecule> distances = null;

        HashMap<HierarchicalClusterNode<Molecule>, Double> clusterWidths;

        HierarchicalClusterNode<Molecule> root;
        try {
            // run clustering
            root = clustering.generateClustering(subset.getMolecules(), usedPropDefs, nnSearch, nnSearchParameters,
                    linkage, distance).calc();

            // generate a distance matrix: precalculated distances
            Distance<Molecule> dist = distance.generateDistance(usedPropDefs);
            LinkedList<HierarchicalClusterNode<Molecule>> singletons = Lists.newLinkedList();
            for (Molecule mol : subset.getMolecules()) {
                HierarchicalClusterNode<Molecule> hcn = new HierarchicalClusterNode<Molecule>(mol);
                singletons.add(hcn);
                mappedNodes.put(mol, hcn);
            }
            distances = new SymmetricDistanceMatrix<Molecule>(dist, singletons);
        } catch (ClusteringException e) {
            throw new EvaluationException("Clustering or DistanceMatrix initialization failed", e);
        }

        // calc actual stepWidth and get the dendrogram levels
        int stepWith = fixedStepWith ? steps : subset.size() / steps;
        if (stepWith == 0) {
            stepWith = 1;
        }
        ArrayList<LinkedList<HierarchicalClusterNode<Molecule>>> allLevels = root.getAllLevels(stepWith);

        /*
         * run measurement for each level
         */
        for (LinkedList<HierarchicalClusterNode<Molecule>> level : allLevels) {
            clusterWidths = Maps.newHashMapWithExpectedSize(level.size());
            HierarchicalClusterNode.assignHcnIds(level);
            double avgSeparation = 0;
            double maxSeparation = Double.NEGATIVE_INFINITY;
            double minSeparation = Double.POSITIVE_INFINITY;
            int avgCount = 0;

            // for each pair of clusters (see also next for loop)
            for (HierarchicalClusterNode<Molecule> cluster1 : level) {
                List<Molecule> mols1 = cluster1.getStructuresInLeafs();
                double clusterWidth1 = getClusterWidth(mappedNodes, distances, clusterWidths, cluster1, mols1);

                for (HierarchicalClusterNode<Molecule> cluster2 : level) {
                    // avoid checking each pair twice and comparison with itself
                    if (cluster1.getExternalId() < cluster2.getExternalId()) {
                        List<Molecule> mols2 = cluster2.getStructuresInLeafs();
                        double clusterWidth2 = getClusterWidth(mappedNodes, distances, clusterWidths, cluster2, mols2);
                        double combinedWidth = Math.max(clusterWidth1, clusterWidth2);

                        // single linkage distance between cluster1 and cluster2
                        double slinkDist = Double.POSITIVE_INFINITY;
                        double dist = 0;
                        for (Molecule mol1 : mols1) {
                            for (Molecule mol2 : mols2) {
                                dist = distances.getDist(mappedNodes.get(mol1), mappedNodes.get(mol2));
                                slinkDist = Math.min(slinkDist, dist);
                            }
                        }

                        if(combinedWidth > 0) {
                            double separation = slinkDist / combinedWidth;
                            
                            maxSeparation = Math.max(maxSeparation, separation);
                            minSeparation = Math.min(minSeparation, separation);
                            
                            avgCount++;
                            avgSeparation = ((avgCount - 1) * avgSeparation + separation) / avgCount;
                        }
                    }
                }
            }

            String keyVal = ((Integer) level.size()).toString();
            result.addResult(keyVal + "_avg", avgCount == 0 ? "NA" : ((Double) avgSeparation).toString());
            result.addResult(keyVal + "_min", avgCount == 0 ? "NA" : ((Double) minSeparation).toString());
            result.addResult(keyVal + "_max", avgCount == 0 ? "NA" : ((Double) maxSeparation).toString());
        }

        return result;
    }

    /**
     * Calculates and caches the cluster width
     */
    private Double getClusterWidth(HashMap<Molecule, HierarchicalClusterNode<Molecule>> mappedNodes,
            SymmetricDistanceMatrix<Molecule> distances,
            HashMap<HierarchicalClusterNode<Molecule>, Double> clusterWidths,
            HierarchicalClusterNode<Molecule> cluster, List<Molecule> mols) {

        Double clusterWidth = clusterWidths.get(cluster);
        if (clusterWidth != null) {
            return clusterWidth;
        } else {
            double maxDist = 0;
            for (Molecule mol1 : mols) {
                for (Molecule mol2 : mols) {
                    if (mol1 != mol2) {
                        double currentDist = distances.getDist(mappedNodes.get(mol1), mappedNodes.get(mol2));
                        maxDist = Math.max(maxDist, currentDist);
                    }
                }
            }
            clusterWidths.put(cluster, maxDist);
            return maxDist;
        }
    }
}
