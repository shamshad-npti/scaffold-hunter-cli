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

package edu.udo.scaffoldhunter.model.clustering.evaluation;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashMap;
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
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.clustering.PropertyCount;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * Compares two {@link NNSearch} strategies by counting the number of different
 * nearest neighbors found.
 * 
 * @author Till Schäfer
 * 
 */
public class NNSearchComparisonModule extends EvaluationModule {
    private static Logger logger = LoggerFactory.getLogger(NNSearchComparisonModule.class);

    private final DbManager db;
    private final Session session;
    private final NNSearchs nnSearch1;
    private final NNSearchs nnSearch2;
    private final NNSearchParameters nnSearchParameters1;
    private final NNSearchParameters nnSearchParameters2;
    private final Linkages linkage;
    private final Distances distance;
    private int maxSubsetSize = Integer.MAX_VALUE;

    /**
     * Constructor
     * 
     * @param db
     * @param session
     * @param nnSearch1
     * @param nnSearch2
     * @param linkage
     * @param distance
     * @param nnSearchParameters1
     * @param nnSearchParameters2
     * @param maxSubsetSize
     */
    public NNSearchComparisonModule(DbManager db, Session session, NNSearchs nnSearch1, NNSearchs nnSearch2,
            NNSearchParameters nnSearchParameters1, NNSearchParameters nnSearchParameters2, Linkages linkage,
            Distances distance, int maxSubsetSize) {
        this.db = db;
        this.session = session;
        this.nnSearch1 = nnSearch1;
        this.nnSearch2 = nnSearch2;
        this.nnSearchParameters1 = nnSearchParameters1;
        this.nnSearchParameters2 = nnSearchParameters2;
        this.linkage = linkage;
        this.distance = distance;
        this.maxSubsetSize = maxSubsetSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.evaluation.EvaluationModule#run()
     */
    @Override
    public Collection<EvaluationResult> run() throws EvaluationException {
        Subset root = session.getSubset();
        Collection<PropertyDefinition> usedPropDefs = getMatchingPropDefs(session, distance);
        LinkedList<EvaluationResult> results = new LinkedList<EvaluationResult>();

        if (usedPropDefs.size() == 0 && distance.acceptedPropertyCount() != PropertyCount.NONE) {
            logger.debug("Leaving out Session \"{}\": No matching PropertyDefinition found", session.getTitle());
        } else {
            // load Properties
            try {
                db.lockAndLoad(usedPropDefs, root.getMolecules());
            } catch (DatabaseException e) {
                throw new EvaluationException("locking of properties failed", e);
            }

            /*
             * parallel execution of the calculations (maximal parallel threads
             * = #processors +1)
             */
            ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
            LinkedList<Callable<EvaluationResult>> jobs = Lists.newLinkedList();

            // generate jobs
            for (Collection<PropertyDefinition> propDefs : generatePropDefLists(distance, session)) {
                for (Subset subset : new SubsetIterable(root, maxSubsetSize)) {
                    jobs.add(new NNSearchCompCallable(propDefs, subset));
                }
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
                } catch (ExecutionException e) {
                    throw new EvaluationException("singleComparison failed", e);
                } catch (InterruptedException e) {
                    // this should never happen!
                    throw new EvaluationException("This is impossible", e);
                }
            }

            // unload Properties
            db.unlockAndUnload(usedPropDefs, root.getMolecules());
        }
        return results;
    }

    /**
     * @param propDefs
     * @param subset
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    private EvaluationResult singleComparison(Collection<PropertyDefinition> propDefs, Subset subset)
            throws InterruptedException, ExecutionException {

        /*
         * Maintain two lists of the same molecules, because otherwise
         * HierarchicalClusterNode#matrixId can be influenced by the other
         * instance of NNSearch
         */
        LinkedList<HierarchicalClusterNode<Molecule>> singletons1 = Lists.newLinkedList();
        LinkedList<HierarchicalClusterNode<Molecule>> singletons2 = Lists.newLinkedList();
        /*
         * HashMap to remain connection between singletons1 and singletons2
         */
        HashMap<HierarchicalClusterNode<Molecule>, HierarchicalClusterNode<Molecule>> singletonLink = Maps.newHashMap();

        EvaluationResult retVal = new EvaluationResult(session.getTitle(), session.getDataset().getTitle(),
                "NNSearchComparison", subset.getTitle(), subset.size());
        retVal.addClustering(null, nnSearch1, nnSearchParameters1, linkage, distance, propDefs);
        retVal.addClustering(null, nnSearch2, nnSearchParameters2, linkage, distance, propDefs);

        logger.info("Running NNSearchComparison: {}{}", System.getProperty("line.separator").toString(),
                retVal.toString());

        // generate HCNs
        for (Molecule structure : subset.getMolecules()) {
            HierarchicalClusterNode<Molecule> hcn1 = new HierarchicalClusterNode<Molecule>(structure);
            HierarchicalClusterNode<Molecule> hcn2 = new HierarchicalClusterNode<Molecule>(structure);

            singletons1.add(hcn1);
            singletons2.add(hcn2);

            singletonLink.put(hcn1, hcn2);
        }

        // executor for parallel calculation
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // generate NNSearch
        Future<NNSearch<Molecule>> fnns1 = executor.submit(new GenerateNNCallable(nnSearch1, propDefs, singletons1,
                nnSearchParameters1));
        Future<NNSearch<Molecule>> fnns2 = executor.submit(new GenerateNNCallable(nnSearch2, propDefs, singletons2,
                nnSearchParameters2));
        NNSearch<Molecule> nns1 = fnns1.get();
        NNSearch<Molecule> nns2 = fnns2.get();

        // statistical variables
        int notCorrect = 0;
        double avgIncorrectDifference = 0;
        double avgIncorrectPercentageDifference = 0;
        double minDist1 = Double.POSITIVE_INFINITY;
        double minDist2 = Double.POSITIVE_INFINITY;
        double maxDist1 = Double.NEGATIVE_INFINITY;
        double maxDist2 = Double.NEGATIVE_INFINITY;
        double avgDistDifference = 0;
        double avgPercentageDistDifference = 0;

        // run measurement
        for (HierarchicalClusterNode<Molecule> hcn : singletons1) {
            // run the nnSearchs in parallel
            Future<SimpleEntry<HierarchicalClusterNode<Molecule>, Double>> fNnAndDist1 = executor
                    .submit(new CalcNNCallable(nns1, hcn));
            Future<SimpleEntry<HierarchicalClusterNode<Molecule>, Double>> fNnAndDist2 = executor
                    .submit(new CalcNNCallable(nns2, singletonLink.get(hcn)));
            SimpleEntry<HierarchicalClusterNode<Molecule>, Double> nnAndDist1 = fNnAndDist1.get();
            SimpleEntry<HierarchicalClusterNode<Molecule>, Double> nnAndDist2 = fNnAndDist2.get();

            Double dist1 = nnAndDist1.getValue();
            Double dist2 = nnAndDist2.getValue();

            // calculate min and max distance values
            minDist1 = Math.min(dist1, minDist1);
            minDist2 = Math.min(dist2, minDist2);
            maxDist1 = Math.max(dist1, maxDist1);
            maxDist2 = Math.max(dist2, maxDist2);

            double distAbsDiff = Math.abs(dist1 - dist2);
            avgDistDifference += distAbsDiff / singletons1.size();
            avgPercentageDistDifference += distAbsDiff == 0 ? 0 : distAbsDiff / Math.max(dist1, dist2) * 100
                    / singletons1.size();

            // calculate notCorrect statistics
            if (nnAndDist1.getKey().getContent() != nnAndDist2.getKey().getContent()) {
                notCorrect++;

                avgIncorrectDifference = incremetalAvg(notCorrect, avgIncorrectDifference, distAbsDiff);

                double percentageDifference = distAbsDiff == 0 ? 0 : distAbsDiff / Math.max(dist1, dist2) * 100;
                avgIncorrectPercentageDifference = incremetalAvg(notCorrect, avgIncorrectPercentageDifference,
                        percentageDifference);
            }
        }

        if (executor.shutdownNow().size() != 0) {
            throw new IllegalStateException();
        }

        retVal.addResult("notCorrect", Integer.toString(notCorrect));
        retVal.addResult("notCorrectPercent",
                notCorrect == 0 ? "0" : Double.toString(((double) notCorrect / subset.size() * 100)));
        retVal.addResult("avgNotCorrectDistDifference", Double.toString(avgIncorrectDifference));
        retVal.addResult("avgNotCorrectPercentageDistDifference", Double.toString(avgIncorrectPercentageDifference));
        retVal.addResult("minDist0", Double.toString(minDist1));
        retVal.addResult("minDist2", Double.toString(minDist2));
        retVal.addResult("maxDist0", Double.toString(maxDist1));
        retVal.addResult("maxDist1", Double.toString(maxDist2));
        retVal.addResult("avgDistDifference", Double.toString(avgDistDifference));
        retVal.addResult("avgPercentageDistDifference", Double.toString(avgPercentageDistDifference));

        return retVal;
    }

    /**
     * Computes an incremental average
     * 
     * @param count
     *            the current count of all values (including the value which
     *            should be added by this call)
     * @param previousAvgValue
     *            the average value for the count-1 values
     * @param addValue
     *            the value which should be added incrementally
     * @return
     */
    private double incremetalAvg(int count, double previousAvgValue, double addValue) {
        return previousAvgValue * (count - 1) / count + addValue * 1.0 / count;
    }

    private class NNSearchCompCallable implements Callable<EvaluationResult> {
        private final Collection<PropertyDefinition> propDefs;
        private final Subset subset;

        public NNSearchCompCallable(Collection<PropertyDefinition> propDefs, Subset subset) {
            this.propDefs = propDefs;
            this.subset = subset;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public EvaluationResult call() throws Exception {
            return singleComparison(propDefs, subset);
        }

    }

    private class CalcNNCallable implements Callable<SimpleEntry<HierarchicalClusterNode<Molecule>, Double>> {

        private final NNSearch<Molecule> nns;
        private final HierarchicalClusterNode<Molecule> hcn;

        public CalcNNCallable(NNSearch<Molecule> nns, HierarchicalClusterNode<Molecule> hcn) {
            this.nns = nns;
            this.hcn = hcn;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public SimpleEntry<HierarchicalClusterNode<Molecule>, Double> call() throws Exception {
            return nns.getNNAndDist(hcn);
        }

    }

    private class GenerateNNCallable implements Callable<NNSearch<Molecule>> {

        private final Collection<PropertyDefinition> propDefs;
        private final LinkedList<HierarchicalClusterNode<Molecule>> singletons;
        private final NNSearchParameters nnSearchParameters;
        private final NNSearchs nnSearch;

        public GenerateNNCallable(NNSearchs nnSearch, Collection<PropertyDefinition> propDefs,
                LinkedList<HierarchicalClusterNode<Molecule>> singletons, NNSearchParameters nnSearchParameters) {
            this.nnSearch = nnSearch;
            this.propDefs = propDefs;
            this.singletons = singletons;
            this.nnSearchParameters = nnSearchParameters;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public NNSearch<Molecule> call() throws Exception {
            return nnSearch.generateNNSearch(linkage, distance, propDefs, singletons, nnSearchParameters);
        }

    }
}
