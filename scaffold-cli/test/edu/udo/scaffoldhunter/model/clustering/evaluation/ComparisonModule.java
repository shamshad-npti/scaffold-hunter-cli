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
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClustering;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.clustering.PropertyCount;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.NumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * Compares two {@link HierarchicalClustering} results
 * 
 * @author Till Schäfer
 * 
 */
public class ComparisonModule extends EvaluationModule {
    private static Logger logger = LoggerFactory.getLogger(ComparisonModule.class);

    private final DbManager db;
    private final Session session;
    private final HierarchicalClusterings clustering1;
    private final HierarchicalClusterings clustering2;
    private final NNSearchs nnSearch1;
    private final NNSearchs nnSearch2;
    private final NNSearchParameters nnSearchParameters1;
    private final NNSearchParameters nnSearchParameters2;
    private final Linkages linkage1;
    private final Linkages linkage2;
    private final Distances distance1;
    private final Distances distance2;
    private final HierarchicalComparisons comparisonMethod;
    private final int steps;
    private final boolean singleDistance;
    private final boolean fixedStepWith;
    private final int maxUsedSimultaniuousProperties;
    private int maxSubsetSize = Integer.MAX_VALUE;

    /**
     * Constructor
     * 
     * The two clusterings use the same linkage and distance. Using default
     * {@link NNSearchParameters}.
     * 
     * If the distance is a fingerprint distance it runs one measurement for
     * each defined matching fingerprint.
     * 
     * @param db
     *            the database connection
     * @param session
     *            the used session
     * @param clustering1
     *            the first clustering algorithm
     * @param clustering2
     *            the second clustering algorithm
     * @param nnSearch1
     *            the {@link NNSearch} strategy used by the first clustering
     *            algorithm
     * @param nnSearch2
     *            the {@link NNSearch} strategy used by the second clustering
     *            algorithm
     * @param linkage
     *            the linkage used by the clustering algorithm
     * @param distance
     *            the distance used by the clustering algorithm
     * @param comparisonMethod
     *            the used {@link HierarchicalComparison} method
     * @param steps
     *            If fixedStepWith=true each n-th level should be measured. i.e.
     *            stepWidth=1 means every level is measured. If
     *            fixedStepWith=false n levels are measured only which are
     *            uniformly distributed among the available levels
     * @param fixedStepWith
     *            see steps description
     */
    public ComparisonModule(DbManager db, Session session, HierarchicalClusterings clustering1,
            HierarchicalClusterings clustering2, NNSearchs nnSearch1, NNSearchs nnSearch2, Linkages linkage,
            Distances distance, HierarchicalComparisons comparisonMethod, int steps, boolean fixedStepWith) {
        this.db = db;
        this.session = session;
        this.clustering1 = clustering1;
        this.clustering2 = clustering2;
        this.nnSearch1 = nnSearch1;
        this.nnSearch2 = nnSearch2;
        this.linkage1 = linkage;
        this.linkage2 = linkage;
        this.distance1 = distance;
        this.distance2 = distance;
        this.comparisonMethod = comparisonMethod;
        this.steps = steps;
        this.fixedStepWith = fixedStepWith;

        nnSearchParameters1 = nnSearch1.getDefaultParameters();
        nnSearchParameters2 = nnSearch2.getDefaultParameters();
        maxUsedSimultaniuousProperties = Integer.MAX_VALUE;
        singleDistance = true;
    }

    /**
     * Constructor
     * 
     * The two clusterings use the same linkage and distance.
     * 
     * If the distance is a fingerprint distance it runs one measurement for
     * each defined matching fingerprint.
     * 
     * @param db
     *            the database connection
     * @param session
     *            the used session
     * @param clustering1
     *            the first clustering algorithm
     * @param clustering2
     *            the second clustering algorithm
     * @param nnSearch1
     *            the {@link NNSearch} strategy used by the first clustering
     *            algorithm
     * @param nnSearch2
     *            the {@link NNSearch} strategy used by the second clustering
     *            algorithm
     * @param nnSearchParameters1
     *            the {@link NNSearchParameters} used by the fist clustering
     * @param nnSearchParameters2
     *            the {@link NNSearchParameters} used by the second clustering
     * @param linkage
     *            the linkage used by the clustering algorithm
     * @param distance
     *            the distance used by the clustering algorithm
     * @param comparisonMethod
     *            the used {@link HierarchicalComparison} method
     * @param steps
     *            If fixedStepWith=true each n-th level should be measured. i.e.
     *            stepWidth=1 means every level is measured. If
     *            fixedStepWith=false n levels are measured only which are
     *            uniformly distributed among the available levels
     * @param fixedStepWith
     *            see steps description
     */
    public ComparisonModule(DbManager db, Session session, HierarchicalClusterings clustering1,
            HierarchicalClusterings clustering2, NNSearchs nnSearch1, NNSearchs nnSearch2,
            NNSearchParameters nnSearchParameters1, NNSearchParameters nnSearchParameters2, Linkages linkage,
            Distances distance, HierarchicalComparisons comparisonMethod, int steps, boolean fixedStepWith) {
        this.db = db;
        this.session = session;
        this.clustering1 = clustering1;
        this.clustering2 = clustering2;
        this.nnSearch1 = nnSearch1;
        this.nnSearch2 = nnSearch2;
        this.nnSearchParameters1 = nnSearchParameters1;
        this.nnSearchParameters2 = nnSearchParameters2;
        this.linkage1 = linkage;
        this.linkage2 = linkage;
        this.distance1 = distance;
        this.distance2 = distance;
        this.comparisonMethod = comparisonMethod;
        this.steps = steps;
        this.fixedStepWith = fixedStepWith;

        maxUsedSimultaniuousProperties = Integer.MAX_VALUE;
        singleDistance = true;
    }

    /**
     * Constructor
     * 
     * The two clusterings use the same linkage and distance.
     * 
     * If the distance is a fingerprint distance it runs one measurement for
     * each defined matching fingerprint.
     * 
     * @param db
     *            the database connection
     * @param session
     *            the used session
     * @param clustering1
     *            the first clustering algorithm
     * @param clustering2
     *            the second clustering algorithm
     * @param nnSearch1
     *            the {@link NNSearch} strategy used by the first clustering
     *            algorithm
     * @param nnSearch2
     *            the {@link NNSearch} strategy used by the second clustering
     *            algorithm
     * @param nnSearchParameters1
     *            the {@link NNSearchParameters} used by the fist clustering
     * @param nnSearchParameters2
     *            the {@link NNSearchParameters} used by the second clustering
     * @param linkage
     *            the linkage used by the clustering algorithm
     * @param distance
     *            the distance used by the clustering algorithm
     * @param comparisonMethod
     *            the used {@link HierarchicalComparison} method
     * @param steps
     *            If fixedStepWith=true each n-th level should be measured. i.e.
     *            stepWidth=1 means every level is measured. If
     *            fixedStepWith=false n levels are measured only which are
     *            uniformly distributed among the available levels
     * @param fixedStepWith
     *            see steps description
     * @param maxUsedSimultaniuousProperties
     *            the maximum number of used {@link NumProperty}s for one
     *            measurement
     * @param maxSubsetSize
     *            the maximum used subset size
     */
    public ComparisonModule(DbManager db, Session session, HierarchicalClusterings clustering1,
            HierarchicalClusterings clustering2, NNSearchs nnSearch1, NNSearchs nnSearch2,
            NNSearchParameters nnSearchParameters1, NNSearchParameters nnSearchParameters2, Linkages linkage,
            Distances distance, HierarchicalComparisons comparisonMethod, int steps, boolean fixedStepWith,
            int maxUsedSimultaniuousProperties, int maxSubsetSize) {
        this.db = db;
        this.session = session;
        this.clustering1 = clustering1;
        this.clustering2 = clustering2;
        this.nnSearch1 = nnSearch1;
        this.nnSearch2 = nnSearch2;
        this.nnSearchParameters1 = nnSearchParameters1;
        this.nnSearchParameters2 = nnSearchParameters2;
        this.linkage1 = linkage;
        this.linkage2 = linkage;
        this.distance1 = distance;
        this.distance2 = distance;
        this.comparisonMethod = comparisonMethod;
        this.steps = steps;
        this.fixedStepWith = fixedStepWith;
        this.maxUsedSimultaniuousProperties = maxUsedSimultaniuousProperties;
        this.maxSubsetSize = maxSubsetSize;

        singleDistance = true;
    }

    /**
     * Constructor
     * 
     * The two clusterings use different linkage and distance.Using default
     * {@link NNSearchParameters}.
     * 
     * If the distance is a fingerprint distance it runs the measurement for
     * each defined fingerprint.
     * 
     * This constructor influences the run() method in the following way. It
     * assumes that the distances are different and therefore we have 3 cases:
     * 
     * 1. both are no fingerprints: run one measurement between the two
     * clustering
     * 
     * 2. both are fingerprints: run a measurement for all combinations of the
     * fingerprints
     * 
     * 3. mixed: run a measurement for each fingerprint compared to 1 non
     * fingerprint distance clustering
     * 
     * @param db
     *            the database connection
     * @param session
     *            the used session
     * @param clustering1
     *            the first clustering algorithm
     * @param clustering2
     *            the second clustering algorithm
     * @param nnSearch1
     *            the {@link NNSearch} strategy used by the first clustering
     *            algorithm
     * @param nnSearch2
     *            the {@link NNSearch} strategy used by the second clustering
     *            algorithm
     * @param linkage1
     *            the linkage used by the first clustering algorithm
     * @param linkage2
     *            the linkage used by the second clustering algorithm
     * @param distance1
     *            the distance used by the first clustering algorithm
     * @param distance2
     *            the distance used by the second clustering algorithm
     * @param comparisonMethod
     *            the used {@link HierarchicalComparison} method
     * @param steps
     *            If fixedStepWith=true each n-th level should be measured. i.e.
     *            stepWidth=1 means every level is measured. If
     *            fixedStepWith=false n levels are measured only which are
     *            uniformly distributed among the available levels
     * @param fixedStepWith
     *            see steps description
     */
    public ComparisonModule(DbManager db, Session session, HierarchicalClusterings clustering1,
            HierarchicalClusterings clustering2, NNSearchs nnSearch1, NNSearchs nnSearch2, Linkages linkage1,
            Linkages linkage2, Distances distance1, Distances distance2, HierarchicalComparisons comparisonMethod,
            int steps, boolean fixedStepWith) {
        this.db = db;
        this.session = session;
        this.clustering1 = clustering1;
        this.clustering2 = clustering2;
        this.nnSearch1 = nnSearch1;
        this.nnSearch2 = nnSearch2;
        this.linkage1 = linkage1;
        this.linkage2 = linkage2;
        this.distance1 = distance1;
        this.distance2 = distance2;
        this.comparisonMethod = comparisonMethod;
        this.steps = steps;
        this.fixedStepWith = fixedStepWith;

        nnSearchParameters1 = nnSearch1.getDefaultParameters();
        nnSearchParameters2 = nnSearch2.getDefaultParameters();
        maxUsedSimultaniuousProperties = Integer.MAX_VALUE;
        singleDistance = false;
    }

    /**
     * Constructor
     * 
     * The two clusterings use different linkage and distance.Using default
     * {@link NNSearchParameters}.
     * 
     * If the distance is a fingerprint distance it runs the measurement for
     * each defined fingerprint.
     * 
     * This constructor influences the run() method in the following way. It
     * assumes that the distances are different and therefore we have 3 cases:
     * 
     * 1. both are no fingerprints: run one measurement between the two
     * clustering
     * 
     * 2. both are fingerprints: run a measurement for all combinations of the
     * fingerprints
     * 
     * 3. mixed: run a measurement for each fingerprint compared to 1 non
     * fingerprint distance clustering
     * 
     * @param db
     *            the database connection
     * @param session
     *            the used session
     * @param clustering1
     *            the first clustering algorithm
     * @param clustering2
     *            the second clustering algorithm
     * @param nnSearch1
     *            the {@link NNSearch} strategy used by the first clustering
     *            algorithm
     * @param nnSearch2
     *            the {@link NNSearch} strategy used by the second clustering
     *            algorithm
     * @param nnSearchParameters1
     *            the {@link NNSearchParameters} used by the fist clustering
     * @param nnSearchParameters2
     *            the {@link NNSearchParameters} used by the second clustering
     * @param linkage1
     *            the linkage used by the first clustering algorithm
     * @param linkage2
     *            the linkage used by the second clustering algorithm
     * @param distance1
     *            the distance used by the first clustering algorithm
     * @param distance2
     *            the distance used by the second clustering algorithm
     * @param comparisonMethod
     *            the used {@link HierarchicalComparison} method
     * @param steps
     *            If fixedStepWith=true each n-th level should be measured. i.e.
     *            stepWidth=1 means every level is measured. If
     *            fixedStepWith=false n levels are measured only which are
     *            uniformly distributed among the available levels
     * @param fixedStepWith
     *            see steps description
     */
    public ComparisonModule(DbManager db, Session session, HierarchicalClusterings clustering1,
            HierarchicalClusterings clustering2, NNSearchs nnSearch1, NNSearchs nnSearch2,
            NNSearchParameters nnSearchParameters1, NNSearchParameters nnSearchParameters2, Linkages linkage1,
            Linkages linkage2, Distances distance1, Distances distance2, HierarchicalComparisons comparisonMethod,
            int steps, boolean fixedStepWith) {
        this.db = db;
        this.session = session;
        this.clustering1 = clustering1;
        this.clustering2 = clustering2;
        this.nnSearch1 = nnSearch1;
        this.nnSearch2 = nnSearch2;
        this.nnSearchParameters1 = nnSearchParameters1;
        this.nnSearchParameters2 = nnSearchParameters2;
        this.linkage1 = linkage1;
        this.linkage2 = linkage2;
        this.distance1 = distance1;
        this.distance2 = distance2;
        this.comparisonMethod = comparisonMethod;
        this.steps = steps;
        this.fixedStepWith = fixedStepWith;

        maxUsedSimultaniuousProperties = Integer.MAX_VALUE;
        singleDistance = false;
    }

    /**
     * Constructor
     * 
     * The two clusterings use different linkage and distance.Using default
     * {@link NNSearchParameters}.
     * 
     * If the distance is a fingerprint distance it runs the measurement for
     * each defined fingerprint.
     * 
     * This constructor influences the run() method in the following way. It
     * assumes that the distances are different and therefore we have 3 cases:
     * 
     * 1. both are no fingerprints: run one measurement between the two
     * clustering
     * 
     * 2. both are fingerprints: run a measurement for all combinations of the
     * fingerprints
     * 
     * 3. mixed: run a measurement for each fingerprint compared to 1 non
     * fingerprint distance clustering
     * 
     * @param db
     *            the database connection
     * @param session
     *            the used session
     * @param clustering1
     *            the first clustering algorithm
     * @param clustering2
     *            the second clustering algorithm
     * @param nnSearch1
     *            the {@link NNSearch} strategy used by the first clustering
     *            algorithm
     * @param nnSearch2
     *            the {@link NNSearch} strategy used by the second clustering
     *            algorithm
     * @param nnSearchParameters1
     *            the {@link NNSearchParameters} used by the fist clustering
     * @param nnSearchParameters2
     *            the {@link NNSearchParameters} used by the second clustering
     * @param linkage1
     *            the linkage used by the first clustering algorithm
     * @param linkage2
     *            the linkage used by the second clustering algorithm
     * @param distance1
     *            the distance used by the first clustering algorithm
     * @param distance2
     *            the distance used by the second clustering algorithm
     * @param comparisonMethod
     *            the used {@link HierarchicalComparison} method
     * @param steps
     *            If fixedStepWith=true each n-th level should be measured. i.e.
     *            stepWidth=1 means every level is measured. If
     *            fixedStepWith=false n levels are measured only which are
     *            uniformly distributed among the available levels
     * @param fixedStepWith
     *            see steps description
     * @param maxUsedSimultaniuousProperties
     *            the maximum number of used {@link NumProperty}s for one
     *            measurement
     */
    public ComparisonModule(DbManager db, Session session, HierarchicalClusterings clustering1,
            HierarchicalClusterings clustering2, NNSearchs nnSearch1, NNSearchs nnSearch2,
            NNSearchParameters nnSearchParameters1, NNSearchParameters nnSearchParameters2, Linkages linkage1,
            Linkages linkage2, Distances distance1, Distances distance2, HierarchicalComparisons comparisonMethod,
            int steps, boolean fixedStepWith, int maxUsedSimultaniuousProperties) {
        this.db = db;
        this.session = session;
        this.clustering1 = clustering1;
        this.clustering2 = clustering2;
        this.nnSearch1 = nnSearch1;
        this.nnSearch2 = nnSearch2;
        this.nnSearchParameters1 = nnSearchParameters1;
        this.nnSearchParameters2 = nnSearchParameters2;
        this.linkage1 = linkage1;
        this.linkage2 = linkage2;
        this.distance1 = distance1;
        this.distance2 = distance2;
        this.comparisonMethod = comparisonMethod;
        this.steps = steps;
        this.fixedStepWith = fixedStepWith;
        this.maxUsedSimultaniuousProperties = maxUsedSimultaniuousProperties;

        singleDistance = false;
    }

    /**
     * Runs the comparison of two {@link HierarchicalClustering} algorithms.
     */
    @Override
    public Collection<EvaluationResult> run() {
        Subset root = session.getSubset();
        Collection<PropertyDefinition> usedPropDefs1 = getMatchingPropDefs(session, distance1);
        Collection<PropertyDefinition> usedPropDefs2 = getMatchingPropDefs(session, distance2);
        LinkedList<EvaluationResult> results = new LinkedList<EvaluationResult>();

        if ((usedPropDefs1.size() == 0 && distance1.acceptedPropertyCount() != PropertyCount.NONE)
                || (usedPropDefs2.size() == 0 && distance2.acceptedPropertyCount() != PropertyCount.NONE)) {
            logger.debug("Leaving out Session \"{}\": No matching PropertyDefinition found", session.getTitle());
        } else {
            // load Properties
            try {
                db.lockAndLoad(usedPropDefs1, root.getMolecules());
                db.lockAndLoad(usedPropDefs2, root.getMolecules());
            } catch (DatabaseException e) {
                throw new EvaluationException("locking of properties failed", e);
            }

            if (singleDistance) {
                for (Collection<PropertyDefinition> propDefs1 : generatePropDefLists(distance1, session, maxUsedSimultaniuousProperties)) {
                    runAllSubsetMeasures(results, root, propDefs1, propDefs1);
                }
            } else {
                for (Collection<PropertyDefinition> propDefs1 : generatePropDefLists(distance1, session, maxUsedSimultaniuousProperties)) {
                    for (Collection<PropertyDefinition> propDefs2 : generatePropDefLists(distance2, session, maxUsedSimultaniuousProperties)) {
                        runAllSubsetMeasures(results, root, propDefs1, propDefs2);
                    }
                }
            }

            // unload Properties
            db.unlockAndUnload(usedPropDefs1, root.getMolecules());
            db.unlockAndUnload(usedPropDefs2, root.getMolecules());
        }

        return results;
    }

    /**
     * Runs DFS like the measurement for all Subsets of the given root.
     * 
     * @param results
     * @param root
     * @param singleton
     * @throws ClusteringException
     */
    private void runAllSubsetMeasures(LinkedList<EvaluationResult> results, Subset root,
            Collection<PropertyDefinition> propDefs1, Collection<PropertyDefinition> propDefs2) {
        for (Subset subset : new SubsetIterable(root, maxSubsetSize)) {
            results.add(singleComparison(subset, propDefs1, propDefs2));
        }
    }

    private EvaluationResult singleComparison(Subset subset, Collection<PropertyDefinition> propDefs1,
            Collection<PropertyDefinition> propDefs2) {
        EvaluationResult result = new EvaluationResult(session.getTitle(), session.getDataset().getTitle(),
                comparisonMethod.getDescription(), subset.getTitle(), subset.size());
        result.addClustering(clustering1, nnSearch1, nnSearchParameters1, linkage1, distance1, propDefs1);
        result.addClustering(clustering2, nnSearch2, nnSearchParameters2, linkage2, distance2, propDefs2);

        logger.info("Running clustering: {}{}", System.getProperty("line.separator").toString(), result.toString());

        try {
            // run clusterings in parallel
            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<HierarchicalClusterNode<Molecule>> future1 = executor.submit(new ClusteringCallable(clustering1,
                    subset.getMolecules(), propDefs1, nnSearch1, nnSearchParameters1, linkage1, distance1));
            Future<HierarchicalClusterNode<Molecule>> future2 = executor.submit(new ClusteringCallable(clustering2,
                    subset.getMolecules(), propDefs2, nnSearch2, nnSearchParameters2, linkage2, distance2));

            executor.shutdown();
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);

            // calculate stepWith
            int stepWith = fixedStepWith ? steps : subset.size() / steps;
            if (stepWith == 0) {
                stepWith = 1;
            }

            // run comparison
            logger.info("Running comparison: {}{}", System.getProperty("line.separator").toString(), result.toString());
            comparisonMethod.generateComparison(future1.get(), future2.get(), session, result, stepWith).run();

        } catch (ExecutionException e) {
            throw new EvaluationException("clustering failed", e);
        } catch (InterruptedException e) {
            // this should never happen!
            throw new EvaluationException("This is impossible", e);
        }

        return result;
    }

    private class ClusteringCallable implements Callable<HierarchicalClusterNode<Molecule>> {

        private final HierarchicalClusterings clustering;
        private final Collection<Molecule> structures;
        private final Collection<PropertyDefinition> propDefs;
        private final NNSearchs nnSearchStrategy;
        private final NNSearchParameters nnSearchParameters;
        private final Linkages linkageType;
        private final Distances distType;

        public ClusteringCallable(HierarchicalClusterings clustering, Collection<Molecule> structures,
                Collection<PropertyDefinition> propDefs, NNSearchs nnSearchStrategy,
                NNSearchParameters nnSearchParameters, Linkages linkageType, Distances distType) {
            this.structures = structures;
            this.propDefs = propDefs;
            this.nnSearchStrategy = nnSearchStrategy;
            this.nnSearchParameters = nnSearchParameters;
            this.linkageType = linkageType;
            this.distType = distType;
            this.clustering = clustering;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public HierarchicalClusterNode<Molecule> call() throws Exception {
            return clustering.generateClustering(structures, propDefs, nnSearchStrategy, nnSearchParameters,
                    linkageType, distType).calc();
        }

    }
}
