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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.clustering.PropertyCount;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.NumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * Provides some convenience mehtods for {@link EvaluationModule}s that are only
 * using a single clustering setting/configuration
 * 
 * @author Till Schäfer
 * 
 */
public abstract class MonotheticEvaluationModule extends EvaluationModule {
    private static Logger logger = LoggerFactory.getLogger(MonotheticEvaluationModule.class);

    protected final DbManager db;
    protected final Session session;
    protected final HierarchicalClusterings clustering;
    protected final NNSearchs nnSearch;
    protected final NNSearchParameters nnSearchParameters;
    protected final Linkages linkage;
    protected final Distances distance;
    protected final int maxUsedSimultaniuousProperties;
    protected final int maxSubsetSize;
    protected final int maxParallelization;

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
     * @param maxUsedSimultaniousProperties
     *            the maximum number of used {@link NumProperty}s for one
     *            measurement
     * @param maxSubsetSize
     *            the maximum subset size for one measurement
     * @param maxParallelization
     *            The maximum number of parallel invoked singleMeasurements. If
     *            set to 0 this will automatically be set to the number of
     *            processors + 1.
     */
    public MonotheticEvaluationModule(DbManager db, Session session, HierarchicalClusterings clustering,
            NNSearchs nnSearch, NNSearchParameters parameters, Linkages linkage, Distances distance,
            int maxUsedSimultaniousProperties, int maxSubsetSize, int maxParallelization) {
        this.db = db;
        this.session = session;
        this.clustering = clustering;
        this.nnSearch = nnSearch;
        this.nnSearchParameters = parameters;
        this.linkage = linkage;
        this.distance = distance;
        this.maxUsedSimultaniuousProperties = maxUsedSimultaniousProperties;
        this.maxSubsetSize = maxSubsetSize;
        this.maxParallelization = maxParallelization;
    }

    /**
     * Runs the measurements for each combination of properties and subsets
     */
    @Override
    public final Collection<EvaluationResult> run() {
        Subset root = session.getSubset();
        List<PropertyDefinition> usedPropDefs = getMatchingPropDefs(session, distance);
        LinkedList<EvaluationResult> results = new LinkedList<EvaluationResult>();

        /*
         * use at most #processors +1 threads or #processors+1 threads if exact
         * number is not set
         */
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(maxParallelization == 0 ? processors + 1 : Math.min(
                maxParallelization, processors + 1));
        LinkedList<Callable<EvaluationResult>> jobs = Lists.newLinkedList();

        if (usedPropDefs.size() == 0 && distance.acceptedPropertyCount() != PropertyCount.NONE) {
            logger.debug("Leaving out Session \"{}\": No matching PropertyDefinition found", session.getTitle());
        } else {
            try {
                // load Properties
                db.lockAndLoad(usedPropDefs, root.getMolecules());
            } catch (DatabaseException e) {
                throw new EvaluationException("locking properties failed", e);
            }

            if (distance.acceptedPropertyCount() == PropertyCount.SINGLE) {
                for (PropertyDefinition propertyDefinition : usedPropDefs) {
                    logger.debug("Using singele PropertyDefinition for Fingerprint: {}", propertyDefinition.getTitle());

                    runAllSubsetMeasures(jobs, root, Collections.singleton(propertyDefinition));
                }
            } else if (distance.acceptedPropertyCount() == PropertyCount.MULTIPLE) {
                logger.debug("Using first {} PropertyDefinitions for NumDistance", maxUsedSimultaniuousProperties);

                runAllSubsetMeasures(jobs, root,
                        usedPropDefs.subList(0, Math.min(maxUsedSimultaniuousProperties, usedPropDefs.size())));
            } else if (distance.acceptedPropertyCount() == PropertyCount.NONE) {
                runAllSubsetMeasures(jobs, root, new LinkedList<PropertyDefinition>());
            } else {
                throw new UnsupportedOperationException("Unsupported PropertyCount");
            }

            List<Future<EvaluationResult>> futures;
            try {
                // run jobs
                futures = executor.invokeAll(jobs);
                // wait until all jobs are finished
                executor.shutdown();
                executor.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            } catch (InterruptedException e) {
                // this should never happen!
                throw new EvaluationException("This is impossible", e);
            }

            for (Future<EvaluationResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (InterruptedException e) {
                    // this should never happen!
                    throw new EvaluationException("This is impossible", e);
                } catch (ExecutionException e) {
                    throw new EvaluationException("Failure in singleMeasurement value", e);
                }
            }

            // unload Properties
            db.unlockAndUnload(usedPropDefs, root.getMolecules());
        }

        return results;
    }

    /**
     * Runs DFS like the measurement for all Subsets of the given root for a
     * fixes set of {@link PropertyDefinition}s.
     */
    private final void runAllSubsetMeasures(LinkedList<Callable<EvaluationResult>> jobs, Subset root,
            Collection<PropertyDefinition> usedPropDefs) {
        for (Subset subset : new SubsetIterable(root, maxSubsetSize)) {
            jobs.add(new SingleMeasurementCallable(subset, usedPropDefs));
        }
    }

    /**
     * Runs a single measurement. This method is invoked in parallel. If you
     * like a sequential behavior simply set the number of threads to 1.
     * 
     * @param subset
     *            the used {@link Subset}
     * @param usedPropDefs
     *            the used {@link PropertyDefinition}s
     * @return the {@link EvaluationResult}
     */
    protected abstract EvaluationResult singleMeasurement(Subset subset, Collection<PropertyDefinition> usedPropDefs);

    private class SingleMeasurementCallable implements Callable<EvaluationResult> {

        private Subset subset;
        private Collection<PropertyDefinition> usedPropDefs;

        SingleMeasurementCallable(Subset subset, Collection<PropertyDefinition> usedPropDefs) {
            this.subset = subset;
            this.usedPropDefs = usedPropDefs;
        }

        @Override
        public EvaluationResult call() throws Exception {
            return singleMeasurement(subset, usedPropDefs);
        }

    }

}
