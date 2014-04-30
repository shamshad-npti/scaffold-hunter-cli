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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.clustering.BestFrontierNNSearch.BestFrontierParameters;
import edu.udo.scaffoldhunter.model.clustering.MatrixNNSearch.MatrixParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.RepresentativeNNSearch.RepresentativeParameters;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Specify and generate a {@link NNSearch} strategy (as a replacement for
 * function pointers)
 * 
 * @author Till Schäfer
 * 
 */
public enum NNSearchs {
    /**
     * @see MatrixNNSearch
     */
    MATRIX {

        @Override
        public <S extends Structure> NNSearch<S> generateNNSearch(Linkages linkage, Distances distance,
                Collection<PropertyDefinition> propDefs, Collection<HierarchicalClusterNode<S>> singletons,
                NNSearchParameters parameters) throws ClusteringException {
            Distance<S> dist = distance.generateDistance(propDefs);
            Linkage<S> link = linkage.genereateLinkage(propDefs);

            return new MatrixNNSearch<S>(dist, link, propDefs, singletons);
        }

        @Override
        public String getName() {
            // TODO internationalisation
            return "Matrix NN search";
        }

        @Override
        public Collection<Linkages> acceptedLinkages() {
            return Arrays.asList(Linkages.values());
        }

        @Override
        public Collection<Distances> acceptedDistances() {
            return Arrays.asList(Distances.values());
        }

        @Override
        public NNSearchParameters getDefaultParameters() {
            return new MatrixParameters();
        }
    },
    /**
     * @see RepresentativeNNSearch
     */
    REPRESENTATIVE {

        @Override
        public <S extends Structure> NNSearch<S> generateNNSearch(Linkages linkage, Distances distance,
                Collection<PropertyDefinition> propDefs, Collection<HierarchicalClusterNode<S>> singletons,
                NNSearchParameters parameters) throws ClusteringException {
            Distance<S> dist = distance.generateDistance(propDefs);
            Linkage<S> link = linkage.genereateLinkage(propDefs);

            return new RepresentativeNNSearch<S>(dist, link, singletons);
        }

        @Override
        public String getName() {
            return "Representative NN search";
        }

        @Override
        public Collection<Linkages> acceptedLinkages() {
            LinkedList<Linkages> retVal = new LinkedList<Linkages>();

            for (Linkages linkage : Linkages.values()) {
                if (linkage.centreBasedLinkage()) {
                    retVal.add(linkage);
                }
            }

            return retVal;
        }

        @Override
        public Collection<Distances> acceptedDistances() {
            return Collections.singleton(Distances.EUCLIDE);
        }

        @Override
        public NNSearchParameters getDefaultParameters() {
            return new RepresentativeParameters();
        }

    },
    /**
     * @see ForwardNNSearch
     */
    FORWARD {

        @Override
        public <S extends Structure> NNSearch<S> generateNNSearch(Linkages linkage, Distances distance,
                Collection<PropertyDefinition> propDefs, Collection<HierarchicalClusterNode<S>> singletons,
                NNSearchParameters parameters) throws ClusteringException {
            Distance<S> dist = distance.generateDistance(propDefs);
            Linkage<S> link = linkage.genereateLinkage(propDefs);

            return new ForwardNNSearch<S>(dist, link, propDefs, singletons);
        }

        @Override
        public String getName() {
            return "Forward NN search";
        }

        @Override
        public Collection<Linkages> acceptedLinkages() {
            /*
             * ForwardNNSearch is a subclass of MatrixNNSearch and does not
             * override the convenience Method
             */
            return MATRIX.acceptedLinkages();
        }

        @Override
        public Collection<Distances> acceptedDistances() {
            /*
             * ForwardNNSearch is a subclass of MatrixNNSearch and does not
             * override the convenience Method
             */
            return MATRIX.acceptedDistances();
        }

        @Override
        public NNSearchParameters getDefaultParameters() {
            return new MatrixParameters();
        }
    },
    /**
     * @see BestFrontierNNSearch
     */
    BEST_FRONTIER {

        @Override
        public <S extends Structure> NNSearch<S> generateNNSearch(Linkages linkage, Distances distance,
                Collection<PropertyDefinition> propDefs, Collection<HierarchicalClusterNode<S>> singletons,
                NNSearchParameters parameters) throws ClusteringException {
            Preconditions.checkArgument(parameters.getClass() == BestFrontierParameters.class,
                    "parameters does not match BestFrontierParameters");
            BestFrontierParameters bfParameters = (BestFrontierParameters) parameters;

            /*
             * Check if the lefBound is larger than twice the number of elements
             * to cluster. If not reduce the leafBound, because otherwise the
             * clustering may not terminate.
             */
            if (singletons.size() * 2 <= bfParameters.leafBound) {
                try {
                    bfParameters = (BestFrontierParameters) bfParameters.clone();
                    bfParameters.setLeafBound(singletons.size() / 2);
                } catch (CloneNotSupportedException e) {
                    throw new ClusteringException(e);
                }
            }

            Distance<S> dist = distance.generateDistance(propDefs);
            Linkage<S> link = linkage.genereateLinkage(propDefs);

            return new BestFrontierNNSearch<S>(dist, link, propDefs, singletons, bfParameters);
        }

        @Override
        public String getName() {
            return "Best-Frontier-Search / Pivot tree NN search";
        }

        @Override
        public Collection<Linkages> acceptedLinkages() {
            LinkedList<Linkages> retVal = new LinkedList<Linkages>();

            for (Linkages linkage : Linkages.values()) {
                if (linkage.isMetric()) {
                    retVal.add(linkage);
                }
            }

            return retVal;
        }

        @Override
        public Collection<Distances> acceptedDistances() {
            return Arrays.asList(Distances.values());
        }

        @Override
        public NNSearchParameters getDefaultParameters() {
            return new BestFrontierParameters();
        }
    },
    /**
     * @see BestFrontierNNSearch
     */
    REPRESENTATIVE_BEST_FRONTIER {

        @Override
        public <S extends Structure> NNSearch<S> generateNNSearch(Linkages linkage, Distances distance,
                Collection<PropertyDefinition> propDefs, Collection<HierarchicalClusterNode<S>> singletons,
                NNSearchParameters parameters) throws ClusteringException {
            Preconditions.checkArgument(parameters.getClass() == BestFrontierParameters.class,
                    "parameters does not match RepresentativeBestFrontierParameters");
            BestFrontierParameters bfParameters = (BestFrontierParameters) parameters;

            /*
             * Check if the lefBound is larger than twice the number of elements
             * to cluster. If not reduce the leafBound, because otherwise the
             * clustering may not terminate.
             */
            if (singletons.size() * 2 <= bfParameters.leafBound) {
                try {
                    bfParameters = (BestFrontierParameters) bfParameters.clone();
                    bfParameters.setLeafBound(singletons.size() / 2);
                } catch (CloneNotSupportedException e) {
                    throw new ClusteringException(e);
                }
            }
            
            Distance<S> dist = distance.generateDistance(propDefs);
            Linkage<S> link = linkage.genereateLinkage(propDefs);

            return new RepresentativeBestFrontierNNSearch<S>(dist, link, propDefs, singletons,
                    bfParameters);
        }

        @Override
        public String getName() {
            return "Representative Best-Frontier-Search / Pivot tree NN search";
        }

        @Override
        public Collection<Linkages> acceptedLinkages() {
            LinkedList<Linkages> retVal = new LinkedList<Linkages>();

            for (Linkages linkage : Linkages.values()) {
                if (linkage.isMetric() && linkage.centreBasedLinkage()) {
                    retVal.add(linkage);
                }
            }

            return retVal;
        }

        @Override
        public Collection<Distances> acceptedDistances() {
            return Collections.singleton(Distances.EUCLIDE);
        }

        @Override
        public NNSearchParameters getDefaultParameters() {
            return new BestFrontierParameters();
        }
    };

    /**
     * Generates the {@link NNSearch} object
     * 
     * @param linkage
     *            the used {@link Linkage}
     * @param distance
     *            the used {@link Distance}
     * @param propDefs
     *            the used {@link PropertyDefinition}(s)
     * @param singletons
     *            the singleton {@link HierarchicalClusterNode}s
     * @param parameters
     *            the {@link NNSearchParameters}
     * @return the {@link NNSearch} object
     * @throws ClusteringException
     */
    public abstract <S extends Structure> NNSearch<S> generateNNSearch(Linkages linkage, Distances distance,
            Collection<PropertyDefinition> propDefs, Collection<HierarchicalClusterNode<S>> singletons,
            NNSearchParameters parameters) throws ClusteringException;

    /**
     * The description of the {@link NNSearch}
     * 
     * @return the description
     */
    public abstract String getName();

    /**
     * Returns the accepted {@link Linkage} strategies.
     * 
     * @return the accepted {@link Linkages}
     */
    public abstract Collection<Linkages> acceptedLinkages();

    /**
     * Returns the accepted {@link Distance} measures.
     * 
     * @return the accepted {@link Distances}
     */
    public abstract Collection<Distances> acceptedDistances();

    /**
     * Returns a default {@link NNSearchParameters} object
     * 
     * @return default {@link NNSearchParameters}
     */
    public abstract NNSearchParameters getDefaultParameters();
}