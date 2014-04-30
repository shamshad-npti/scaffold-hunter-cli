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

import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.clustering.BestFrontierNNSearch.BestFrontierParameters;
import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.MatrixNNSearch.MatrixParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.DbManagerHibernate;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Session;

/**
 * Runs the evaluation of clustering algorithms
 * 
 * local Mysql database needed:
 * 
 * - schema: clustering_evaluation
 * 
 * - user: hibernate
 * 
 * - pass: temp
 * 
 * - SH profile: evaluation
 * 
 * @author Till Schäfer
 * 
 */
@SuppressWarnings("unused")
public class RunEvaluation {
    private static Logger logger = null;

    private DbManager db;
    private Profile profile;

    /**
     * @param args
     * @throws NotFoundException
     * @throws DatabaseException
     * @throws ClusteringException
     * @throws IOException
     */
    public static void main(String[] args) throws DatabaseException, NotFoundException, ClusteringException,
            IOException {
        // Initialize logging
        System.setProperty("java.util.logging.config.file", "logging.properties");
        logger = LoggerFactory.getLogger(RunEvaluation.class);

        boolean assertionEnabled = false;
        assert (assertionEnabled = true) == true;

        System.out.println("Running Evaluation. Assertion enabled=" + assertionEnabled);
        logger.info("Logger Info output enabled");

        RunEvaluation evaluation = new RunEvaluation();
        evaluation.loadDatabase();
    }

    private void loadDatabase() throws DatabaseException, NotFoundException, ClusteringException, IOException {
        // open Database
        try {
            db = new DbManagerHibernate("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQL5InnoDBDialect",
                    "jdbc:mysql://localhost/", "clustering_evaluation", "hibernate", "temp", true, false);
            EvaluationModule.setDbManager(db);
        } catch (DatabaseException e) {
            throw new RuntimeException("Database init failed", e);
        }

        // Load first Profile and Session
        profile = db.getProfile("clustering_evaluation");
        List<String> sessionTitles = db.getAllSessionTitles(profile);
        for (String title : sessionTitles) {
            Session session = db.getSession(profile, title);
            runModules(db, session);
        }

        // Session session = db.getSession(profile, "different_subset_sizes");
        // runModules(db, session);
    }

    /**
     * @param session
     * @throws DatabaseException
     * @throws ClusteringException
     * @throws IOException
     */
    private void runModules(DbManager db, Session session) throws DatabaseException, ClusteringException, IOException {
        logger.info("Running Modules for Session {}", session.getTitle());

        // testBestFrontierSearch(db, session);
//        testHSAHN(db, session);
        // testNNSearchEvalModule(db, session);
//        testClusterSeprationModule(db, session);
        testDistanceDistribution(db,session);
//        testTanimotoDistribution();
    }

    
    
    private void testTanimotoDistribution() {
        logger.info(System.getProperty("line.separator").toString() 
                + "##########################################" + System.getProperty("line.separator").toString()
                + "##Starting test: TanimotoDistribution   ##" + System.getProperty("line.separator").toString()
                + "##########################################");
        
        String homePath = System.getProperty("user.home");
        String resultsFolderPath = homePath + "/data/temp/eval_temp/tanimoto_distribution.txt";
        FileSaverMetaModule saver = new FileSaverMetaModule(resultsFolderPath, false, true, false);
//        saver.run(new TanimotoDistributionModule(73,100)); 
        saver.run(new TanimotoDistributionModule(1024,100, BigInteger.valueOf(10).pow(323))); 
    }
    
    /**
     * Calculate the distance distribution
     * 
     * @param db2
     * @param session
     */
    private void testDistanceDistribution(DbManager db, Session session) {
        logger.info(System.getProperty("line.separator").toString() 
                + "##########################################" + System.getProperty("line.separator").toString()
                + "##Starting test: DistanceDistribution   ##" + System.getProperty("line.separator").toString()
                + "##########################################");
        
        Distances distance = Distances.TANIMOTOBIT;
        int maxSubsetSize = 10000;
        String homePath = System.getProperty("user.home");
        String resultsFolderPath = homePath + "/data/temp/eval_temp/" + session.getTitle() + "/";
        
        FileSaverMetaModule saver = new FileSaverMetaModule(resultsFolderPath + "distance_distribution_" + distance.getName() + ".txt", false, true, false);
        saver.run(new DistanceDistributionModule(db, session, distance, 100, Integer.MAX_VALUE, 10000));
    }

    /**
     * Matrix vs Matrix Comparison to test if the output of
     * {@link NNSearchComparisonModule} is plausible
     * 
     * @param db2
     * @param session
     */
    private void testNNSearchEvalModule(DbManager db2, Session session) {
        FileSaverMetaModule saverNNSearchComp = new FileSaverMetaModule("/home/till/data/temp/testeval.txt", false,
                true, false);
        NNSearchComparisonModule module = new NNSearchComparisonModule(db, session, NNSearchs.MATRIX, NNSearchs.MATRIX,
                new MatrixParameters(), new MatrixParameters(), Linkages.CENTROID_LINKAGE, Distances.TANIMOTOBIT, 10000);
        saverNNSearchComp.run(module);
    }

    
    private void testClusterSeprationModule(DbManager db, Session session) {
        logger.info(System.getProperty("line.separator").toString() 
                + "##########################################" + System.getProperty("line.separator").toString()
                + "##Starting test: ClusterSeprationModule ##" + System.getProperty("line.separator").toString()
                + "##########################################");
        
        Distances distance = Distances.EUCLIDE;
        int maxSubsetSize = 10000;
        String homePath = System.getProperty("user.home");
        String resultsFolderPath = homePath + "/data/temp/eval_temp/" + session.getTitle() + "/";
//        NNSearchParameters parameters = new BestFrontierParameters(1000,1,Integer.MAX_VALUE);
//        NNSearchs searchStrat = NNSearchs.BEST_FRONTIER;
        NNSearchParameters parameters = new MatrixParameters();
        NNSearchs searchStrat = NNSearchs.MATRIX;
        
        FileSaverMetaModule saver = new FileSaverMetaModule(resultsFolderPath + "test.txt", false, true, false);
        ClusterSeparationModule module = new ClusterSeparationModule(db, session,
                HierarchicalClusterings.GENERIC_CLUSTERING_CORRECT, searchStrat, parameters, Linkages.CENTROID_LINKAGE,
                distance, Integer.MAX_VALUE, maxSubsetSize, 50, true);
        saver.run(module);
        
    }

    /**
     * @param db2
     * @param session
     */
    private void testBestFrontierSearch(DbManager db, Session session) {
        logger.info(System.getProperty("line.separator").toString() 
                + "##########################################" + System.getProperty("line.separator").toString()
                + "##  Starting test: BestFrontierSearch   ##" + System.getProperty("line.separator").toString()
                + "##########################################");

        Distances distance = Distances.EUCLIDE;
        int maxSubsetSize = 10000;
        String homePath = System.getProperty("user.home");
        String resultsFolderPath = homePath + "/data/temp/eval_bfs/" + session.getTitle() + "/";

        BestFrontierParameters[] parameters = { 
                new BestFrontierParameters(1, 1, 1),
                new BestFrontierParameters(20, 500, 1), 
                new BestFrontierParameters(20, 1000, 1),
                new BestFrontierParameters(20, 2000, 1), 
                new BestFrontierParameters(20, 4000, 1),
                new BestFrontierParameters(20, 8000, 1), 
                new BestFrontierParameters(20, 16000, 1),
                new BestFrontierParameters(20, Integer.MAX_VALUE, 1), 
                new BestFrontierParameters(50, 500, 1),
                new BestFrontierParameters(50, 1000, 1), 
                new BestFrontierParameters(50, 2000, 1),
                new BestFrontierParameters(50, 4000, 1), 
                new BestFrontierParameters(50, 8000, 1),
                new BestFrontierParameters(50, 16000, 1), 
                new BestFrontierParameters(50, Integer.MAX_VALUE, 1),
                new BestFrontierParameters(5, 500, 10), 
                new BestFrontierParameters(5, 1000, 10),
                new BestFrontierParameters(5, 2000, 10), 
                new BestFrontierParameters(5, 4000, 10),
                new BestFrontierParameters(5, 8000, 10), 
                new BestFrontierParameters(5, 16000, 10),
                new BestFrontierParameters(5, Integer.MAX_VALUE, 10), 
                new BestFrontierParameters(5, 500, 20),
                new BestFrontierParameters(5, 1000, 20), 
                new BestFrontierParameters(5, 2000, 20),
                new BestFrontierParameters(5, 4000, 20), 
                new BestFrontierParameters(5, 8000, 20),
                new BestFrontierParameters(5, 16000, 20), 
                new BestFrontierParameters(5, Integer.MAX_VALUE, 20),
                new BestFrontierParameters(5, 500, 50), 
                new BestFrontierParameters(5, 1000, 50),
                new BestFrontierParameters(5, 2000, 50), 
                new BestFrontierParameters(5, 4000, 50),
                new BestFrontierParameters(5, 8000, 50), 
                new BestFrontierParameters(5, 16000, 50),
                new BestFrontierParameters(5, Integer.MAX_VALUE, 50), 
                new BestFrontierParameters(10, 500, 50),
                new BestFrontierParameters(10, 1000, 50), 
                new BestFrontierParameters(10, 2000, 50),
                new BestFrontierParameters(10, 4000, 50), 
                new BestFrontierParameters(10, 8000, 50),
                new BestFrontierParameters(10, 16000, 50), 
                new BestFrontierParameters(10, Integer.MAX_VALUE, 50),
                new BestFrontierParameters(20, 500, 1000), 
                new BestFrontierParameters(20, 1000, 1000),
                new BestFrontierParameters(20, 2000, 1000), 
                new BestFrontierParameters(20, 4000, 1000),
                new BestFrontierParameters(20, 8000, 1000), 
                new BestFrontierParameters(20, 16000, 1000),
                new BestFrontierParameters(20, Integer.MAX_VALUE, 1000), 
                new BestFrontierParameters(100, 500, 1000),
                new BestFrontierParameters(100, 1000, 1000), 
                new BestFrontierParameters(100, 2000, 1000),
                new BestFrontierParameters(100, 4000, 1000), 
                new BestFrontierParameters(100, 8000, 1000),
                new BestFrontierParameters(100, 16000, 1000), 
                new BestFrontierParameters(100, Integer.MAX_VALUE, 1000)
        // new BestFrontierParameters(Integer.MAX_VALUE, Integer.MAX_VALUE, 1)
        };

        for (BestFrontierParameters parameter : parameters) {
            FileSaverMetaModule saverNNSearchComp = new FileSaverMetaModule(resultsFolderPath + parameter.toFileName()
                    + ".txt", false, true, false);
            NNSearchComparisonModule module = new NNSearchComparisonModule(db, session, NNSearchs.MATRIX,
                    NNSearchs.BEST_FRONTIER, new MatrixParameters(), parameter, Linkages.CENTROID_LINKAGE, distance,
                    maxSubsetSize);
            saverNNSearchComp.run(module);
        }
    }

    /**
     * @param db
     * @param session
     */
    private void testHSAHN(DbManager db, Session session) {
        logger.info(System.getProperty("line.separator").toString() 
                + "##########################################" + System.getProperty("line.separator").toString()
                + "##  Starting test: SAHN                 ##" + System.getProperty("line.separator").toString()
                + "##########################################");

        RepeaterMetaModule repeaterPerf = new RepeaterMetaModule(4, true);
        StatisticsMetaModule statistics = new StatisticsMetaModule();
        LinkedList<EvaluationMetaModule> statisticsPerfStack = Lists.newLinkedList();
        statisticsPerfStack.add(statistics);
        statisticsPerfStack.add(repeaterPerf);
        RepeaterMetaModule repeaterComp = new RepeaterMetaModule(3, false);
        LinkedList<EvaluationMetaModule> statisticsCompStack = Lists.newLinkedList();
        statisticsCompStack.add(statistics);
        statisticsCompStack.add(repeaterComp);
        // int[] maxDimensionalities = {2,5,10,20,50};
        int[] maxDimensionalities = { 5 };
        Distances distance = Distances.TANIMOTO;
        int maxSubsetSize = 10000;
        String homePath = System.getProperty("user.home");
        String resultsFolderPath = homePath + "/data/temp/eval_paper/" + session.getTitle() + "/";

        BestFrontierParameters[] parameters = { new BestFrontierParameters(1, 1, 1),
                new BestFrontierParameters(20, 500, 1), 
                new BestFrontierParameters(20, 1000, 1),
                new BestFrontierParameters(20, 2000, 1), 
                new BestFrontierParameters(20, 4000, 1),
                new BestFrontierParameters(20, 8000, 1), 
                new BestFrontierParameters(20, 16000, 1),
                new BestFrontierParameters(20, Integer.MAX_VALUE, 1),
                new BestFrontierParameters(50, 500, 1),
                new BestFrontierParameters(50, 1000, 1), 
                new BestFrontierParameters(50, 2000, 1),
                new BestFrontierParameters(50, 4000, 1), 
                new BestFrontierParameters(50, 8000, 1),
                new BestFrontierParameters(50, 16000, 1), 
                new BestFrontierParameters(50, Integer.MAX_VALUE, 1),
                new BestFrontierParameters(5, 500, 10), 
                new BestFrontierParameters(5, 1000, 10),
                new BestFrontierParameters(5, 2000, 10), 
                new BestFrontierParameters(5, 4000, 10),
                new BestFrontierParameters(5, 8000, 10), 
                new BestFrontierParameters(5, 16000, 10),
                new BestFrontierParameters(5, Integer.MAX_VALUE, 10),
                new BestFrontierParameters(5, 500, 20),
                new BestFrontierParameters(5, 1000, 20), 
                new BestFrontierParameters(5, 2000, 20),
                new BestFrontierParameters(5, 4000, 20),
                new BestFrontierParameters(5, 8000, 20),
                new BestFrontierParameters(5, 16000, 20), 
                new BestFrontierParameters(5, Integer.MAX_VALUE, 20),
                new BestFrontierParameters(5, 500, 50), 
                new BestFrontierParameters(5, 1000, 50),
                new BestFrontierParameters(5, 2000, 50),
                new BestFrontierParameters(5, 4000, 50),
                new BestFrontierParameters(5, 8000, 50), 
                new BestFrontierParameters(5, 16000, 50),
                new BestFrontierParameters(5, Integer.MAX_VALUE, 50), 
                new BestFrontierParameters(10, 500, 50),
                new BestFrontierParameters(10, 1000, 50), 
                new BestFrontierParameters(10, 2000, 50),
                new BestFrontierParameters(10, 4000, 50), 
                new BestFrontierParameters(10, 8000, 50),
                new BestFrontierParameters(10, 16000, 50), 
                new BestFrontierParameters(10, Integer.MAX_VALUE, 50),
                new BestFrontierParameters(20, 500, 1000), 
                new BestFrontierParameters(20, 1000, 1000),
                new BestFrontierParameters(20, 2000, 1000), 
                new BestFrontierParameters(20, 4000, 1000),
                new BestFrontierParameters(20, 8000, 1000), 
                new BestFrontierParameters(20, 16000, 1000),
                new BestFrontierParameters(20, Integer.MAX_VALUE, 1000), 
                new BestFrontierParameters(100, 500, 1000),
                new BestFrontierParameters(100, 1000, 1000), 
                new BestFrontierParameters(100, 2000, 1000),
                new BestFrontierParameters(100, 4000, 1000), 
                new BestFrontierParameters(100, 8000, 1000),
                new BestFrontierParameters(100, 16000, 1000), 
                new BestFrontierParameters(100, Integer.MAX_VALUE, 1000) };

        for (int maxDimensionality : maxDimensionalities) {
            // ------------

            FileSaverMetaModule saverPerfForward = new FileSaverMetaModule(resultsFolderPath + "forward_perf.txt", false,
                    true, true);
            FileSaverMetaModule saverSeparationForward = new FileSaverMetaModule(resultsFolderPath + "forward_separation.txt", false,
                    true, false);

            PerformanceModule perfForward = new PerformanceModule(db, session,
                    HierarchicalClusterings.GENERIC_CLUSTERING, NNSearchs.FORWARD, new MatrixParameters(),
                    Linkages.CENTROID_LINKAGE, distance, maxDimensionality, maxSubsetSize);
            ClusterSeparationModule separationForward = new ClusterSeparationModule(db, session,
                    HierarchicalClusterings.GENERIC_CLUSTERING, NNSearchs.FORWARD, new MatrixParameters(),
                    Linkages.CENTROID_LINKAGE, distance, maxDimensionality, maxSubsetSize, 50, false);
            
            saverPerfForward.run(statisticsPerfStack, perfForward);
            saverSeparationForward.run(statisticsCompStack, separationForward);
            
            // ---------------
            for (BestFrontierParameters parameter : parameters) {
                FileSaverMetaModule saverPerf = new FileSaverMetaModule(resultsFolderPath + parameter.toFileName()
                        + "_perf.txt", false, true, true);
                FileSaverMetaModule saverCompFM = new FileSaverMetaModule(resultsFolderPath + parameter.toFileName()
                        + "_quality-fm.txt", false, true, false);
                FileSaverMetaModule saverCompNVI = new FileSaverMetaModule(resultsFolderPath + parameter.toFileName()
                        + "_quality-nvi.txt", false, true, false);
                FileSaverMetaModule saverSeparation = new FileSaverMetaModule(resultsFolderPath + parameter.toFileName()
                        + "_separation.txt", false, true, false);

                PerformanceModule perf = new PerformanceModule(db, session,
                        HierarchicalClusterings.GENERIC_CLUSTERING_CORRECT, NNSearchs.BEST_FRONTIER, parameter,
                        Linkages.CENTROID_LINKAGE, distance, maxDimensionality, maxSubsetSize);

                ComparisonModule compFM = new ComparisonModule(db, session,
                        HierarchicalClusterings.GENERIC_CLUSTERING_CORRECT, HierarchicalClusterings.GENERIC_CLUSTERING,
                        NNSearchs.BEST_FRONTIER, NNSearchs.FORWARD, parameter, new MatrixParameters(),
                        Linkages.CENTROID_LINKAGE, distance, HierarchicalComparisons.FOWLKES_MALLOWS, 50, false,
                        maxDimensionality, maxSubsetSize);

                ComparisonModule compNVI = new ComparisonModule(db, session,
                        HierarchicalClusterings.GENERIC_CLUSTERING_CORRECT, HierarchicalClusterings.GENERIC_CLUSTERING,
                        NNSearchs.BEST_FRONTIER, NNSearchs.FORWARD, parameter, new MatrixParameters(),
                        Linkages.CENTROID_LINKAGE, distance, HierarchicalComparisons.NVI, 50, false, maxDimensionality,
                        maxSubsetSize);

                ClusterSeparationModule separation = new ClusterSeparationModule(db, session,
                        HierarchicalClusterings.GENERIC_CLUSTERING_CORRECT, NNSearchs.BEST_FRONTIER, parameter,
                        Linkages.CENTROID_LINKAGE, distance, maxDimensionality, maxSubsetSize, 50, false);

                saverPerf.run(statisticsPerfStack, perf);
                saverCompFM.run(statisticsCompStack, compFM);
                saverCompNVI.run(statisticsCompStack, compNVI);
                saverSeparation.run(statisticsCompStack, separation);
            }
        }
    }
}
