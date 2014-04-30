/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

package edu.udo.scaffoldhunter.gui.clustering;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClustering;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Starts a {@link HierarchicalClustering} algorithm in the background
 * 
 * @author Philipp Kopp
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 */
public class ClusteringWorker<S extends Structure> extends ProgressWorker<HierarchicalClusterNode<S>, Void> {
    private static Logger logger = LoggerFactory.getLogger(ClusteringWorker.class);

    private Collection<S> structures;
    private Collection<PropertyDefinition> propDefs;
    private Linkages linkageType;
    private Distances distanceType;
    private final HierarchicalClusterings hClustering;
    private final NNSearchs nnSearch;

    private final NNSearchParameters nnSearchParameters;

    /**
     * Constructor
     * 
     * @param structs
     *            the structures to be clustered
     * @param propDefs
     *            the {@link Property}s used for the clustering
     * @param hClustering
     *            the used {@link HierarchicalClustering} algorithm
     * @param nnSearch
     *            the used {@link NNSearch} strategy
     * @param nnSearchParameters
     *            the used {@link NNSearchParameters}
     * @param linkageType
     *            the {@link Linkages} used for the clustering
     * @param distType
     *            the {@link Distance} used for the clustering
     */
    public ClusteringWorker(Collection<S> structs, Collection<PropertyDefinition> propDefs,
            HierarchicalClusterings hClustering, NNSearchs nnSearch, NNSearchParameters nnSearchParameters,
            Linkages linkageType, Distances distType) {
        super();
        this.structures = structs;
        this.propDefs = propDefs;
        this.hClustering = hClustering;
        this.nnSearch = nnSearch;
        this.nnSearchParameters = nnSearchParameters;
        this.linkageType = linkageType;
        this.distanceType = distType;
    }

    @Override
    protected HierarchicalClusterNode<S> doInBackground() throws Exception {
        logger.trace("Entering doInBackground");

        HierarchicalClustering<S> clustering = hClustering.generateClustering(structures, propDefs, nnSearch,
                nnSearchParameters, linkageType, distanceType);

        clustering.addProgressListener(this);
        setProgressIndeterminate(false);

        HierarchicalClusterNode<S> root = clustering.calc();

        clustering.removeProgressListener(this);
        return root;
    }
}
