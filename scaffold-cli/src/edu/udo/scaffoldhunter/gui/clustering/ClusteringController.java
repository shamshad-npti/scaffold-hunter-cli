/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

package edu.udo.scaffoldhunter.gui.clustering;

import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener;
import edu.udo.scaffoldhunter.model.NumComparisonFunction;
import edu.udo.scaffoldhunter.model.StringComparisonFunction;
import edu.udo.scaffoldhunter.model.clustering.BestFrontierNNSearch.BestFrontierParameters;
import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkage;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Filter;
import edu.udo.scaffoldhunter.model.db.Filterset;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.NumFilter;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.StringFilter;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Orderings;
import edu.udo.scaffoldhunter.util.ProgressListener;
import edu.udo.scaffoldhunter.view.dendrogram.DendrogramView;

/**
 * This Class starts/ends the clustering and takes care updating the progress.
 * It also loads and unloads the needed properties from the database. It also
 * provides some higher level helper functions for the GUI.
 * 
 * @author Philipp Kopp
 * @author Till Schäfer
 * 
 */
public class ClusteringController {
    private static Logger logger = LoggerFactory.getLogger(ClusteringController.class);

    private DbManager db;
    private DendrogramView dView;
    private Collection<PropertyDefinition> clusteringPropDefs;
    private Collection<Molecule> structs;
    private Subset clusteringSubset;
    private Linkages clusteringLinkage;
    private Distances clusteringDistance;
    private final Session session;
    private NNSearchs nnSearchStrategy;
    private NNSearchParameters nnSearchParameters;

    /**
     * Constructor
     * 
     * @param db
     *            the {@link DbManager}
     * @param session
     *            the currently used Session
     * @param dView
     *            the associated {@link DendrogramView}
     * 
     */
    public ClusteringController(DbManager db, Session session, DendrogramView dView) {
        this.session = session;
        Preconditions.checkNotNull(db);
        Preconditions.checkNotNull(dView);

        this.db = db;
        this.dView = dView;
    }

    /**
     * @return the associated {@link DendrogramView}
     */
    public DendrogramView getdView() {
        return dView;
    }

    /**
     * Starts the Clustering in Background with a {@link ClusteringWorker} and
     * informs the {@link DendrogramView} about this.
     * 
     * @param subset
     *            the {@link Subset} to be clustered
     * @param linkage
     *            the {@link Linkage} that should be used for clustering
     * @param distance
     *            the {@link Distance}-Type that should be used for clustering
     * @param propDefs
     *            the {@link PropertyDefinition}s that are used by the
     *            {@link Distance}
     * @param clustering
     * @param nnSearchStrategy
     *            the {@link NNSearch} strategy to be used by the clustering
     * @param nnSearchParameters
     *            the {@link NNSearchParameters} to be used by the clustering
     * @throws DatabaseException
     */
    public void startClustering(Subset subset, Linkages linkage, Distances distance,
            Collection<PropertyDefinition> propDefs, HierarchicalClusterings clustering, NNSearchs nnSearchStrategy,
            NNSearchParameters nnSearchParameters) throws DatabaseException {
        Preconditions.checkArgument(clusteringPropDefs == null, "You can only run one clustering at each time.");
        Preconditions.checkArgument(structs == null, "Clustering coltroller inconsistency");
        Preconditions.checkArgument(clusteringSubset == null, "Clustering coltroller inconsistency");
        Preconditions.checkArgument(clusteringLinkage == null, "Clustering coltroller inconsistency");
        Preconditions.checkArgument(clusteringDistance == null, "Clustering coltroller inconsistency");

        this.clusteringDistance = Preconditions.checkNotNull(distance);
        this.clusteringLinkage = Preconditions.checkNotNull(linkage);
        this.clusteringSubset = Preconditions.checkNotNull(subset);
        this.clusteringPropDefs = Preconditions.checkNotNull(propDefs);
        this.nnSearchStrategy = Preconditions.checkNotNull(nnSearchStrategy);
        this.nnSearchParameters = Preconditions.checkNotNull(nnSearchParameters);

        // lazy loading of properties
        structs = Lists.newArrayList(clusteringSubset.getMolecules());
        try {
            db.lockAndLoad(propDefs, structs);
        } catch (DatabaseException e) {
            Writer stacktrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stacktrace));
            logger.error(e.getMessage() + stacktrace.toString());

            throw e;
        }

        // create the ClusteringWorker
        ClusteringWorker<Molecule> worker = new ClusteringWorker<Molecule>(structs, propDefs, clustering,
                nnSearchStrategy, nnSearchParameters, linkage, distance);
        worker.addExceptionListener(new ClusteringExceptionListener());
        worker.addProgressListener(new ClusteringProgressHandler());

        // inform the view that a clustering is started
        dView.clusteringStarted(worker);

        // start the clustering in background
        worker.execute();
    }

    /**
     * Validates that the all {@link Property}s are defined.
     * 
     * @param propDefs
     *            chosen {@link PropertyDefinition}s
     * @param progressListener
     *            The listener that should be informed about the progress and
     *            the result of the validation. The result contains of the
     *            filtered subset and a boolean value that is true if and only
     *            if the filtered subset is smaller than the original subset. It
     *            is not necessary to unregister the listeners manually.
     * @param exceptionListener
     *            The listener that should be informed about exceptions that
     *            occur during the validation. Exceptions will result in
     *            stopping the execution. It is not necessary to unregister the
     *            listener manually.
     */
    public void validateParameter(Collection<PropertyDefinition> propDefs,
            ProgressListener<SimpleEntry<Boolean, Subset>> progressListener, WorkerExceptionListener exceptionListener) {
        Preconditions.checkNotNull(propDefs);
        Preconditions.checkNotNull(progressListener);

        // create on "isDefined" filter per property
        HashSet<Filter> filters = Sets.newHashSet();
        for (PropertyDefinition propDef : propDefs) {
            if (propDef.isStringProperty()) {
                filters.add(new StringFilter(null, propDef, null, null, StringComparisonFunction.IsDefined));
            } else {
                filters.add(new NumFilter(null, propDef, null, 0, NumComparisonFunction.IsDefined));
            }
        }

        // create a Filterset with all the filters constructed above
        Profile profile = dView.getSubset().getSession().getProfile();
        Filterset filterset = new Filterset(profile, "Clusterinf filtered Subset", filters, true);
        filterset.setFilters(filters);
        for (Filter filter : filters) {
            filter.setFilterset(filterset);
        }

        // create and execute the ValidationWorker
        FilterDefinedWorker filterWorker = new FilterDefinedWorker(dView.getSubset(), filterset, db);
        /*
         * We do not need to remove the listeners as filterWorker gets destroyed
         * anyway after the filtering process has finished.
         */
        filterWorker.addExceptionListener(exceptionListener);
        filterWorker.addProgressListener(progressListener);
        filterWorker.execute();
    }

    /**
     * Calculates all accepted PropertyDefinitions for a given {@link Distance}
     * type
     * 
     * @param distance
     *            the {@link Distances}
     * @return all matching {@link PropertyDefinition}s
     */
    public Collection<PropertyDefinition> getMatchingPropertyDefinitions(Distances distance) {
        LinkedList<PropertyDefinition> retVal = Lists.newLinkedList();

        List<PropertyDefinition> propDefs = Orderings.PROPERTY_DEFINITION_BY_TITLE.immutableSortedCopy(session
                .getDataset().getPropertyDefinitions().values());
        for (PropertyDefinition propDef : propDefs) {
            if (propDef.getPropertyType() == distance.acceptedPropertyType() && !propDef.isScaffoldProperty()) {
                retVal.add(propDef);
            }
        }

        return retVal;
    }

    /**
     * Generates BestFrontierParameters based on two more intuitive parameters
     * (quality and dimensionality)
     * 
     * @param quality
     *            the quality should be given in percent [1,100]
     * @param dimensionalatiy
     *            the (intrinsic) dimensionality in three steps [1,3] (low <= 7,
     *            8 <= mid <= 12, 13 <= high).
     * @param size
     *            the size of the dataset
     * 
     * @return estimation of good {@link BestFrontierParameters}
     */
    public static BestFrontierParameters bestFrontierParameterGeneration(int quality, int dimensionalatiy, int size) {
        Preconditions.checkArgument(quality <= 100 && quality > 0, "quality is out of range");
        Preconditions.checkArgument(dimensionalatiy > 0 && dimensionalatiy <= 3, "dimensionality out of range");

        /*
         * For a size 10000 the range of Depth is between approximately 100 and
         * 10000
         */
        int frontierBound = (int) (Math.log(size) * 10 * quality);

        /*
         * 50 <= leafBound <= 100
         */
        int leafBound = 50 + quality / 2;

        /*
         * {5,10,15}
         */
        int branchingFactor;

        switch (dimensionalatiy) {
        case 1:
            branchingFactor = 5;
            break;
        case 2:
            branchingFactor = 10;
            break;
        case 3:
            branchingFactor = 15;
            break;
        default:
            throw new IllegalStateException();
        }
        return new BestFrontierParameters(branchingFactor, frontierBound, leafBound);
    }

    /**
     * reverse of @see ClusteringController#bestFrontierParameterGeneration
     * 
     * @param nnSearch
     * @return whether the used NNSearch is exact (or heuristic)
     */
    public static boolean isExact(NNSearchs nnSearch) {
        if (nnSearch == defaultNNSearchs(false)) {
            return false;
        } else if (nnSearch == defaultNNSearchs(true)) {
            return true;
        } else {
            throw new IllegalStateException("Illegal NNSearch strategy");
        }
    }

    /**
     * reverse off @see ClusteringController#bestFrontierParameterGeneration
     * 
     * @param nnSearchParameters
     *            {@link BestFrontierParameters}. If other type -> return null
     * @return the simplified quality parameter for heuristic clustering or null
     *         if {@link NNSearchParameters} are not of type
     *         {@link BestFrontierParameters}
     */
    public static Integer getQuality(NNSearchParameters nnSearchParameters) {
        if (nnSearchParameters != null && nnSearchParameters.getClass() == BestFrontierParameters.class) {
            BestFrontierParameters parameters = (BestFrontierParameters) nnSearchParameters;

            return parameters.getLeafBound() * 2 - 100;
        } else {
            return null;
        }
    }

    /**
     * reverse off @see ClusteringController#bestFrontierParameterGeneration
     * 
     * @param nnSearchParameters
     *            {@link BestFrontierParameters}. If other type -> return null
     * @return the simplified dimensionality parameter for heuristic clustering
     *         or null if {@link NNSearchParameters} are not of type
     *         {@link BestFrontierParameters}
     */
    public static Integer getDimensionality(NNSearchParameters nnSearchParameters) {
        if (nnSearchParameters != null && nnSearchParameters.getClass() == BestFrontierParameters.class) {
            BestFrontierParameters parameters = (BestFrontierParameters) nnSearchParameters;

            switch (parameters.getBranchingFactor()) {
            case 5:
                return 1;
            case 10:
                return 2;
            case 15:
                return 3;
            default:
                throw new IllegalArgumentException();
            }
        } else {
            return null;
        }
    }

    /**
     * Note: this must be consistent with defaultClusteringAlgorithm()!
     * 
     * @param exact
     *            exact or heuristic
     * @return the {@link NNSearchs} that is used for the exact or heuristic
     *         clustering
     */
    public static NNSearchs defaultNNSearchs(boolean exact) {
        if (exact) {
            return NNSearchs.FORWARD;
        } else {
            return NNSearchs.BEST_FRONTIER;
        }
    }

    /**
     * Note: this must be consistent with defaultNNSearchs()!
     * 
     * @param exact
     *            exact or heuristic
     * @return the {@link HierarchicalClusterings} that is used for the exact or
     *         heuristic clustering
     */
    public static HierarchicalClusterings defaultClusteringAlgorithm(boolean exact) {
        if (exact) {
            return HierarchicalClusterings.GENERIC_CLUSTERING;
        } else {
            return HierarchicalClusterings.GENERIC_CLUSTERING_CORRECT;
        }
    }

    /**
     * Reset clustering state. This ensures only one clustering can be done at
     * each time.
     */
    private void resetClusteringState() {
        clusteringPropDefs = null;
        structs = null;
        clusteringSubset = null;
        clusteringLinkage = null;
        clusteringDistance = null;
    }

    /**
     * Handles unlocking of properties and setting the view to the new
     * clustering when the {@link ClusteringWorker} is finished.
     * 
     * @author Till Schäfer
     */
    private class ClusteringProgressHandler implements ProgressListener<HierarchicalClusterNode<Molecule>> {
        @Override
        public void setProgressValue(int progress) {
            /*
             * Nothing to do here. the progress is handled directly by the
             * DendrogramView
             */
        }

        @Override
        public void setProgressBounds(int min, int max) {
            /*
             * Nothing to do here. the progress is handled directly by the
             * DendrogramView
             */
        }

        @Override
        public void setProgressIndeterminate(boolean indeterminate) {
            /*
             * Nothing to do here. the progress is handled directly by the
             * DendrogramView
             */
        }

        /**
         * Clustering is finished
         * 
         * -> unlock Properties, set results in DView, ...
         */
        @Override
        public void finished(HierarchicalClusterNode<Molecule> result, boolean cancelled) {
            // unlock Properties
            db.unlockAndUnload(clusteringPropDefs, structs);

            if (cancelled || result == null) {
                JOptionPane.showConfirmDialog(dView.getComponent(), I18n.get("Clustering.Cancel"),
                        I18n.get("Clustering.Title.Cancel"), JOptionPane.DEFAULT_OPTION,
                        JOptionPane.INFORMATION_MESSAGE);
                dView.clusteringFinished();
            } else {
                // if a filtered subset was used
                if (dView.getSubset().size() != clusteringSubset.size()) {
                    /*
                     * add a hint to the title, that this is a automatically
                     * filtered subset by clustering
                     */
                    clusteringSubset.setTitle("clustering_" + clusteringSubset.getTitle());
                    // add the filtered subset to the SubsetManager
                    dView.getSubsetManager().addSubset(clusteringSubset);
                }

                // inform the dView about the new clustering results
                dView.changeModel(result, clusteringSubset, clusteringPropDefs, clusteringLinkage, clusteringDistance,
                        nnSearchStrategy, nnSearchParameters);
                dView.clusteringFinished();
            }

            resetClusteringState();
        }
    }

    /**
     * Do some cleanup and shows an error message if a
     * {@link ClusteringException} occurs.
     * 
     * @author Philipp Kopp
     */
    private class ClusteringExceptionListener implements WorkerExceptionListener {
        @Override
        public ExceptionHandlerResult exceptionThrown(Throwable e) {
            // unlock Properties
            db.unlockAndUnload(clusteringPropDefs, structs);

            resetClusteringState();

            // show error dialog with stack trace
            JDialog clusteringError = new JDialog();

            clusteringError.setModal(true);
            clusteringError.setTitle("Clustering Error");
            clusteringError.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.add(new JLabel(I18n.get("Clustering.Error")));
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            String stacktrace = sw.toString();
            logger.error("Error while clustering:");
            logger.error(stacktrace);
            logger.error("End Clustering error");
            JTextArea text = new JTextArea(stacktrace);
            text.setEditable(false);
            JScrollPane scroll = new JScrollPane(text);
            panel.add(scroll);
            int w = Math.min(800, scroll.getMaximumSize().width);
            int h = Math.min(400, scroll.getMaximumSize().height);
            scroll.setPreferredSize(new Dimension(w, h));
            clusteringError.add(panel);
            clusteringError.pack();
            clusteringError.setVisible(true);

            // stop clustering worker
            return ExceptionHandlerResult.STOP;
        }

    }

}
