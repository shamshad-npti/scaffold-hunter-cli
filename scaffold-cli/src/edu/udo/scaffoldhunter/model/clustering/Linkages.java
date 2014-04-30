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

import java.util.Collection;

import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Till Schäfer
 * 
 */
public enum Linkages {
    /**
     * @see CompleteLinkage
     */
    COMPLETE_LINKAGE {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Linkage.Complete");
        }

        @Override
        public String getName() {
            return "Complete Linkage";
        }

        @Override
        public <S extends Structure> Linkage<S> genereateLinkage(Collection<PropertyDefinition> propertyVector) {
            return new CompleteLinkage<S>(propertyVector);
        }

        @Override
        public boolean centreBasedLinkage() {
            return false;
        }

        @Override
        public boolean needsProstProcessing() {
            return false;
        }

        @Override
        public boolean isMetric() {
            return false;
        }

        @Override
        public boolean fulfilReproducibility() {
            return true;
        }
    },
    /**
     * @see GroupAverageLinkage
     */
    GROUP_AVERAGE_LINKAGE {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Linkage.Group");
        }

        @Override
        public String getName() {
            return "Group Average Linkage";
        }

        @Override
        public <S extends Structure> Linkage<S> genereateLinkage(Collection<PropertyDefinition> propertyVector) {
            return new GroupAverageLinkage<S>(propertyVector);
        }

        @Override
        public boolean centreBasedLinkage() {
            return false;
        }

        @Override
        public boolean needsProstProcessing() {
            return false;
        }

        @Override
        public boolean isMetric() {
            return false;
        }

        @Override
        public boolean fulfilReproducibility() {
            return true;
        }
    },
    /**
     * @see McQuittysLinkage
     */
    MCQUITTYS_LINKAGE {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Linkage.McQuitty");
        }

        @Override
        public String getName() {
            return "McQuittys Linkage";
        }

        @Override
        public <S extends Structure> Linkage<S> genereateLinkage(Collection<PropertyDefinition> propertyVector) {
            return new McQuittysLinkage<S>(propertyVector);
        }

        @Override
        public boolean centreBasedLinkage() {
            return false;
        }

        @Override
        public boolean needsProstProcessing() {
            return false;
        }

        @Override
        public boolean isMetric() {
            return false;
        }

        @Override
        public boolean fulfilReproducibility() {
            return true;
        }
    },
    /**
     * @see SingleLinkage
     */
    SINGLE_LINKAGE {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Linkage.Single");
        }

        @Override
        public String getName() {
            return "Single Linkage";
        }

        @Override
        public <S extends Structure> Linkage<S> genereateLinkage(Collection<PropertyDefinition> propertyVector) {
            return new SingleLinkage<S>(propertyVector);
        }

        @Override
        public boolean centreBasedLinkage() {
            return false;
        }

        @Override
        public boolean needsProstProcessing() {
            return false;
        }

        @Override
        public boolean isMetric() {
            return false;
        }

        @Override
        public boolean fulfilReproducibility() {
            return true;
        }
    },
    /**
     * @see WardLinkage
     */
    WARD_LINKAGE {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Linkage.Ward");
        }

        @Override
        public String getName() {
            return "Ward Linkage";
        }

        @Override
        public <S extends Structure> Linkage<S> genereateLinkage(Collection<PropertyDefinition> propertyVector) {
            return new WardLinkage<S>(propertyVector);
        }

        @Override
        public boolean centreBasedLinkage() {
            return true;
        }

        @Override
        public boolean needsProstProcessing() {
            return true;
        }

        @Override
        public boolean isMetric() {
            return false;
        }

        @Override
        public boolean fulfilReproducibility() {
            return true;
        }
    },
    /**
     * @see CentroidLinkage
     */
    CENTROID_LINKAGE {
        @Override
        public <S extends Structure> Linkage<S> genereateLinkage(Collection<PropertyDefinition> propertyVector) {
            return new CentroidLinkage<S>(propertyVector);
        }

        @Override
        public boolean centreBasedLinkage() {
            return true;
        }

        @Override
        public boolean needsProstProcessing() {
            return false;
        }

        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Linkage.Centroid");
        }

        @Override
        public String getName() {
            return "Centroid Linkage";
        }

        @Override
        public boolean isMetric() {
            return true;
        }

        @Override
        public boolean fulfilReproducibility() {
            return false;
        }
    },
    /**
     * @see MedianLinkage
     */
    MEDIAN_LINKAGE {
        @Override
        public <S extends Structure> Linkage<S> genereateLinkage(Collection<PropertyDefinition> propertyVector) {
            return new MedianLinkage<S>(propertyVector);
        }

        @Override
        public boolean centreBasedLinkage() {
            return true;
        }

        @Override
        public boolean needsProstProcessing() {
            return false;
        }

        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Linkage.Median");
        }

        @Override
        public String getName() {
            return "Median Linkage";
        }

        @Override
        public boolean isMetric() {
            return true;
        }

        @Override
        public boolean fulfilReproducibility() {
            return false;
        }
    };
    /**
     * @param <S>
     *            The type parameter for the Linkage
     * @param propertyVector
     * @return generates the matching {@link Linkage}
     */
    public abstract <S extends Structure> Linkage<S> genereateLinkage(Collection<PropertyDefinition> propertyVector);

    /**
     * Determines if the Linkage allows centre based distance calculations for
     * clusters. I.e. if the doContendMerge methods is valid.
     * 
     * @return if the linkage allows centre base distance calculations
     */
    public abstract boolean centreBasedLinkage();

    /**
     * Returns if a distancePostProcessing needs to be called (for performance
     * reasons).
     * 
     * @return if post processing is needed
     */
    public abstract boolean needsProstProcessing();

    /**
     * @return short description of the used linkage to be shown in the view
     */
    public abstract String getInfo();

    /**
     * @return the name of this linkage
     */
    public abstract String getName();

    /**
     * Returns if the {@link Linkage} fulfils the metric conditions
     * 
     * @return if the {@link Linkage} is metric
     */
    public abstract boolean isMetric();

    /**
     * Returns if the Linkage fulfils the reproducibility property
     * 
     * @return if the {@link Linkage} fulfils the reproducibility property
     */
    public abstract boolean fulfilReproducibility();
}
