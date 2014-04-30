/*
 * Scaffold Hunter
 * Copyright (C) 2012 Till Schäfer
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
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClustering;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.NumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * Measures the performance (time) of a clustering algorithm
 * 
 * @author Till Schäfer
 * 
 */
public class PerformanceModule extends MonotheticEvaluationModule {
    private static Logger logger = LoggerFactory.getLogger(PerformanceModule.class);

    /**
     * Constructor with default {@link NNSearchParameters}
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
     * 
     */
    public PerformanceModule(DbManager db, Session session, HierarchicalClusterings clustering, NNSearchs nnSearch,
            Linkages linkage, Distances distance) {
        super(db, session, clustering, nnSearch, nnSearch.getDefaultParameters(), linkage, distance, Integer.MAX_VALUE,
                Integer.MAX_VALUE, 1);
    }

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
     * 
     */
    public PerformanceModule(DbManager db, Session session, HierarchicalClusterings clustering, NNSearchs nnSearch,
            NNSearchParameters parameters, Linkages linkage, Distances distance) {
        super(db, session, clustering, nnSearch, parameters, linkage, distance, Integer.MAX_VALUE, Integer.MAX_VALUE, 1);
    }

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
     */
    public PerformanceModule(DbManager db, Session session, HierarchicalClusterings clustering, NNSearchs nnSearch,
            NNSearchParameters parameters, Linkages linkage, Distances distance, int maxUsedSimultaniuousProperties,
            int maxSubsetSize) {
        super(db, session, clustering, nnSearch, parameters, linkage, distance, maxUsedSimultaniuousProperties,
                maxSubsetSize, 1);
    }

    /**
     * Runs a time measurement
     * 
     * @param subset
     *            the {@link Subset} which contains the {@link Molecule}s
     * @param propDefs
     *            the used Properties
     * @return the result of the measurement
     * 
     * @throws ClusteringException
     */
    @Override
    protected EvaluationResult singleMeasurement(Subset subset, Collection<PropertyDefinition> propDefs) {
        logger.info("Running time measurement for session '{}' and Subset '{}' with size {}",
                new Object[] { session.getTitle(), subset.getTitle(), subset.size() });

        EvaluationResult result = new EvaluationResult(session.getTitle(), session.getDataset().getTitle(),
                "time performance", subset.getTitle(), subset.size());
        result.addClustering(clustering, nnSearch, nnSearchParameters, linkage, distance, propDefs);

        try {
            /*
             * perform a garbage collection before measurement to avoid
             * different conditions for different runs
             */
            Runtime.getRuntime().gc();
            Date start = new Date();
            HierarchicalClustering<Molecule> clust;
            clust = clustering.generateClustering(subset.getMolecules(), propDefs, nnSearch, nnSearchParameters,
                    linkage, distance);
            Date afterInit = new Date();
            clust.calc();
            Date finished = new Date();
            result.addResult("time to initialise", afterInit.getTime() - start.getTime());
            result.addResult("time to run", finished.getTime() - afterInit.getTime());
            result.addResult("overall time", finished.getTime() - start.getTime());
        } catch (ClusteringException e) {
            throw new EvaluationException("Clustering failed", e);
        }

        return result;
    }
}
