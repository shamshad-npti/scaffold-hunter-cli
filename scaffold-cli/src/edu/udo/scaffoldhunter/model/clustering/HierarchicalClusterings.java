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

package edu.udo.scaffoldhunter.model.clustering;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Specify and generate a clustering algorithm (as a replacement for function
 * pointers)
 * 
 * @author Till Schäfer
 * 
 */
public enum HierarchicalClusterings {
    /**
     * @see NNChain
     */
    NNCHAIN {
        @Override
        public <S extends Structure> HierarchicalClustering<S> generateClustering(Collection<S> structures,
                Collection<PropertyDefinition> propDefs, NNSearchs nnSearchStrategy,
                NNSearchParameters nnSearchParameters, Linkages linkageType, Distances distType)
                throws ClusteringException {
            try {
                LinkedList<HierarchicalClusterNode<S>> singletons = generateSingletons(structures);

                return new NNChain<S>(nnSearchStrategy.generateNNSearch(linkageType, distType, propDefs, singletons,
                        nnSearchParameters));
            } catch (ClusteringException e) {
                Writer stacktrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stacktrace));
                logger.error(e.getMessage() + stacktrace.toString());
                throw e;
            }
        }

        @Override
        public String getName() {
            return "NNChain";
        }

        @Override
        public Collection<NNSearchs> acceptedNNSearchs() {
            LinkedList<NNSearchs> retVal = Lists.newLinkedList();

            retVal.add(NNSearchs.MATRIX);
            retVal.add(NNSearchs.REPRESENTATIVE);

            return retVal;
        }
    },
    /**
     * @see GenericClustering
     */
    GENERIC_CLUSTERING {
        @Override
        public <S extends Structure> HierarchicalClustering<S> generateClustering(Collection<S> structures,
                Collection<PropertyDefinition> propDefs, NNSearchs nnSearchStrategy,
                NNSearchParameters nnSearchParameters, Linkages linkageType, Distances distType)
                throws ClusteringException {
            try {
                LinkedList<HierarchicalClusterNode<S>> singletons = generateSingletons(structures);

                return new GenericClustering<S>(nnSearchStrategy.generateNNSearch(linkageType, distType, propDefs,
                        singletons, nnSearchParameters));
            } catch (ClusteringException e) {
                Writer stacktrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stacktrace));
                logger.error(e.getMessage() + stacktrace.toString());
                throw e;
            }
        }

        @Override
        public String getName() {
            return "Generic";
        }

        @Override
        public Collection<NNSearchs> acceptedNNSearchs() {
            LinkedList<NNSearchs> retVal = Lists.newLinkedList();

            retVal.add(NNSearchs.MATRIX);
            retVal.add(NNSearchs.REPRESENTATIVE);
            retVal.add(NNSearchs.FORWARD);
            retVal.add(NNSearchs.BEST_FRONTIER);

            return retVal;
        }
    },
    /**
     * @see GenericClusteringCorrections
     */
    GENERIC_CLUSTERING_CORRECT {
        @Override
        public <S extends Structure> HierarchicalClustering<S> generateClustering(Collection<S> structures,
                Collection<PropertyDefinition> propDefs, NNSearchs nnSearchStrategy,
                NNSearchParameters nnSearchParameters, Linkages linkageType, Distances distType)
                throws ClusteringException {
            try {
                LinkedList<HierarchicalClusterNode<S>> singletons = generateSingletons(structures);

                return new GenericClusteringCorrections<S>(nnSearchStrategy.generateNNSearch(linkageType, distType,
                        propDefs, singletons, nnSearchParameters));
            } catch (ClusteringException e) {
                Writer stacktrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stacktrace));
                logger.error(e.getMessage() + stacktrace.toString());
                throw e;
            }
        }

        @Override
        public String getName() {
            return "Generic Corrected";
        }

        @Override
        public Collection<NNSearchs> acceptedNNSearchs() {
            LinkedList<NNSearchs> retVal = Lists.newLinkedList();

            retVal.add(NNSearchs.MATRIX);
            retVal.add(NNSearchs.REPRESENTATIVE);
            retVal.add(NNSearchs.FORWARD);
            retVal.add(NNSearchs.BEST_FRONTIER);

            return retVal;
        }
    };

    private static Logger logger = LoggerFactory.getLogger(HierarchicalClusterings.class);

    /**
     * Returns a Clustering of the specified type
     * 
     * @param structures
     *            the structures to be clustered
     * @param propDefs
     *            the {@link Property}s used for the clustering
     * @param nnSearchStrategy
     *            the used {@link NNSearch} strategy
     * @param nnSearchParameters
     *            the {@link NNSearchParameters}
     * @param linkageType
     *            the {@link Linkages} used for the clustering
     * @param distType
     *            the {@link Distance} used for the clustering
     * @return the {@link HierarchicalClustering} object
     * @throws ClusteringException
     */
    public abstract <S extends Structure> HierarchicalClustering<S> generateClustering(Collection<S> structures,
            Collection<PropertyDefinition> propDefs, NNSearchs nnSearchStrategy, NNSearchParameters nnSearchParameters,
            Linkages linkageType, Distances distType) throws ClusteringException;

    /**
     * Returns a Clustering of the specified type with default
     * {@link NNSearchParameters}
     * 
     * @param structures
     *            the structures to be clustered
     * @param propDefs
     *            the {@link Property}s used for the clustering
     * @param nnSearchStrategy
     *            the used {@link NNSearch} strategy
     * @param linkageType
     *            the {@link Linkages} used for the clustering
     * @param distType
     *            the {@link Distance} used for the clustering
     * @return the {@link HierarchicalClustering} object
     * @throws ClusteringException
     */
    public <S extends Structure> HierarchicalClustering<S> generateClustering(Collection<S> structures,
            Collection<PropertyDefinition> propDefs, NNSearchs nnSearchStrategy, Linkages linkageType,
            Distances distType) throws ClusteringException {
        return generateClustering(structures, propDefs, nnSearchStrategy, nnSearchStrategy.getDefaultParameters(),
                linkageType, distType);
    }

    /**
     * Returns the name of the clustering algorithm
     * 
     * @return the algorithm name
     */
    public abstract String getName();

    /**
     * Returns a {@link Collection} of the possible {@link NNSearch} strategies
     * 
     * @return the accepted {@link NNSearch} strategies
     */
    public abstract Collection<NNSearchs> acceptedNNSearchs();

    /**
     * Generates a {@link List} of singleton {@link HierarchicalClusterNode}s
     * based on the structures
     * 
     * @param structures
     *            the used {@link Structure}s
     * @return the singleton {@link List}
     */
    private static <S extends Structure> LinkedList<HierarchicalClusterNode<S>> generateSingletons(
            Collection<S> structures) {
        LinkedList<HierarchicalClusterNode<S>> singletons = new LinkedList<HierarchicalClusterNode<S>>();

        for (S structure : structures) {
            singletons.add(new HierarchicalClusterNode<S>(structure));
        }

        return singletons;
    }
}
