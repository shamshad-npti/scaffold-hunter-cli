package edu.udo.scaffoldhunter.model.clustering.evaluation;

import java.util.Collection;
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

import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.PropertyCount;
import edu.udo.scaffoldhunter.model.clustering.SymmetricDistanceMatrix;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * Calculates the intrinsic dimensionality
 * 
 * @author Till Schäfer
 */
public class IntrinsicDimModule extends EvaluationModule {
    private static Logger logger = LoggerFactory.getLogger(NNSearchComparisonModule.class);

    private final DbManager db;
    private final Session session;
    private final Distances distance;
    private final int maxSubsetSize;

    /**
     * Constructor
     * 
     * @param db
     * @param session
     * @param distance
     * @param maxSubsetSize
     */
    public IntrinsicDimModule(DbManager db, Session session, Distances distance, int maxSubsetSize) {
        this.db = db;
        this.session = session;
        this.distance = distance;
        this.maxSubsetSize = maxSubsetSize;
    }

    @Override
    public Collection<EvaluationResult> run() throws EvaluationException {
        LinkedList<EvaluationResult> results = new LinkedList<EvaluationResult>();
        Subset root = session.getSubset();
        Collection<PropertyDefinition> usedPropDefs = getMatchingPropDefs(session, distance);

        if (usedPropDefs.size() == 0 && distance.acceptedPropertyCount() != PropertyCount.NONE) {
            logger.debug("Leaving out Session \"{}\": No matching PropertyDefinition found", session.getTitle());
        } else {
            // lock properties
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

            for (Subset subset : new SubsetIterable(root, maxSubsetSize)) {
                // generate HCNs
                LinkedList<HierarchicalClusterNode<Molecule>> singletons = Lists.newLinkedList();
                for (Molecule structure : subset.getMolecules()) {
                    HierarchicalClusterNode<Molecule> hcn = new HierarchicalClusterNode<Molecule>(structure);

                    singletons.add(hcn);
                }

                // generate jobs
                for (Collection<PropertyDefinition> propDefs : generatePropDefLists(distance, session)) {
                    jobs.add(new CalcCallable(propDefs, subset, singletons));
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

            // unload properties
            db.unlockAndUnload(usedPropDefs, root.getMolecules());
        }
        return results;
    }

    private class CalcCallable implements Callable<EvaluationResult> {
        private final Collection<PropertyDefinition> propDefs;
        private final Subset subset;
        private final Collection<HierarchicalClusterNode<Molecule>> nodes;

        public CalcCallable(Collection<PropertyDefinition> propDefs, Subset subset,
                Collection<HierarchicalClusterNode<Molecule>> nodes) {
            this.propDefs = propDefs;
            this.subset = subset;
            this.nodes = nodes;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.concurrent.Callable#call()
         */
        @Override
        public EvaluationResult call() throws Exception {
            EvaluationResult result = new EvaluationResult(session.getTitle(), session.getDataset().getTitle(),
                    "IntrinsicDimensionality", subset.getTitle(), subset.size());
            SymmetricDistanceMatrix<Molecule> matrix = null;
            try {
                Distance<Molecule> dist = distance.generateDistance(propDefs);
                matrix = new SymmetricDistanceMatrix<Molecule>(dist, nodes, true);
            } catch (ClusteringException e) {
                e.printStackTrace();
            }

            long count = 0;
            double avg = 0;
            for (HierarchicalClusterNode<Molecule> node1 : nodes) {
                for (HierarchicalClusterNode<Molecule> node2 : nodes) {
                    if (node1 != node2) {
                        avg = incremetalAvg(++count, avg, matrix.getDist(node1, node2));
                    }
                }
            }
            count = 0;
            double variance = 0;
            for (HierarchicalClusterNode<Molecule> node1 : nodes) {
                for (HierarchicalClusterNode<Molecule> node2 : nodes) {
                    if (node1 != node2) {
                        variance = incremetalAvg(++count, variance, Math.pow(matrix.getDist(node1, node2) - avg, 2));
                    }
                }
            }

            result.addResult("average distance", Double.toString(avg));
            result.addResult("variance", Double.toString(variance));
            result.addResult("intrinsic dimensionality", Double.toString(Math.pow(avg,2) / (2 * variance)));

            return result;
        }

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
    private double incremetalAvg(long count, double previousAvgValue, double addValue) {
        return previousAvgValue * (count - 1) / count + addValue * 1.0 / count;
    }

}
