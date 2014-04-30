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

import java.util.Collection;
import java.util.LinkedList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.SymmetricDistanceMatrix;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.NumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * Creates a histogram of the distance distribution for a specific distance
 * function and dataset
 * 
 * @author Till Schäfer
 * 
 */
public class DistanceDistributionModule extends MonotheticEvaluationModule {
    private static Logger logger = LoggerFactory.getLogger(DistanceDistributionModule.class);

    private int granularity;

    /**
     * Constructor
     * 
     * @param db
     *            the database connection
     * @param session
     *            the used {@link Session}
     * @param distance
     *            the used {@link Distance}
     * @param granularity
     *            the number of distinct intervals
     * @param maxUsedSimultaniousProperties
     *            the maximum number of used {@link NumProperty}s for one
     *            measurement
     * @param maxSubsetSize
     *            the maximum subset size for one measurement
     */
    public DistanceDistributionModule(DbManager db, Session session, Distances distance, int granularity,
            int maxUsedSimultaniousProperties, int maxSubsetSize) {
        super(db, session, null, null, null, null, distance, maxUsedSimultaniousProperties, maxSubsetSize, 0);
        this.granularity = granularity;
    }

    @Override
    protected EvaluationResult singleMeasurement(Subset subset, Collection<PropertyDefinition> usedPropDefs) {
        logger.info("Running distance distribution measurement for session '{}' and Subset '{}' with size {}",
                new Object[] { session.getTitle(), subset.getTitle(), subset.size() });

        EvaluationResult result = new EvaluationResult(session.getTitle(), session.getDataset().getTitle(),
                "distance distribution", subset.getTitle(), subset.size());
        result.addClustering(clustering, nnSearch, nnSearchParameters, linkage, distance, usedPropDefs);

        // generate a distance matrix: precalculated distances
        Distance<Molecule> dist = distance.generateDistance(usedPropDefs);
        LinkedList<HierarchicalClusterNode<Molecule>> singletons = Lists.newLinkedList();
        for (Molecule mol : subset.getMolecules()) {
            HierarchicalClusterNode<Molecule> hcn = new HierarchicalClusterNode<Molecule>(mol);
            singletons.add(hcn);
        }
        SymmetricDistanceMatrix<Molecule> distances = null;
        try {
            distances = new SymmetricDistanceMatrix<Molecule>(dist, singletons);
        } catch (ClusteringException e) {
            throw new EvaluationException("DistanceMatrix initialization failed", e);
        }

        double maxStoredValue = distances.getMaxDistance();
        double intervalSize = maxStoredValue / granularity;
        int[] counts = new int[granularity];

        for (HierarchicalClusterNode<Molecule> node1 : singletons) {
            for (HierarchicalClusterNode<Molecule> node2 : singletons) {
                // ensure to count each distance only once (symmetry)
                if (node1.getExternalId() < node2.getExternalId()) {
                    int index = (int) Math.floor(distances.getDist(node1, node2) / intervalSize);
                    if (index == granularity) {
                        index--;
                    }
                    counts[index]++;
                }
            }
        }
        
        // excludes symmetric distances and distances to itself
        long numberOfDistances = singletons.size() * ((long) singletons.size() - 1) / 2;
        
        for (int i = 0; i < counts.length; i++) {
            String interval = String.format("%5.2f", i*intervalSize) + "-" + String.format("%5.2f", (i+1)*intervalSize);
            String frequency = String.format("%.4f", (double) counts[i] / numberOfDistances);
            result.addResult(interval, frequency); 
        }
        
        return result;
    }
}
