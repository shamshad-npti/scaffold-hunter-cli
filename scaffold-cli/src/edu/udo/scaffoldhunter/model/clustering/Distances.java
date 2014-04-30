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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.Collection;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Philipp Kopp
 * 
 */
public enum Distances {
    /**
     * @see Euclide
     */
    EUCLIDE {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Distance.Euclide");
        }

        @Override
        public String getName() {
            return "Euclide";
        }

        @Override
        public <S extends Structure> Distance<S> generateDistance(Collection<PropertyDefinition> propertyVector) {
            return new Euclide<S>(propertyVector);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyType
         * ()
         */
        @Override
        public PropertyType acceptedPropertyType() {
            return PropertyType.NumProperty;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyCount()
         */
        @Override
        public PropertyCount acceptedPropertyCount() {
            return PropertyCount.MULTIPLE;
        }
    },
    /**
     * @see Tanimoto
     */
    TANIMOTO {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Distance.Tanimoto");
        }

        @Override
        public String getName() {
            return "Tanimoto";
        }

        @Override
        public <S extends Structure> Distance<S> generateDistance(Collection<PropertyDefinition> propertyVector) {
            return new Tanimoto<S>(propertyVector);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyType
         * ()
         */
        @Override
        public PropertyType acceptedPropertyType() {
            return PropertyType.BitStringFingerprint;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyCount()
         */
        @Override
        public PropertyCount acceptedPropertyCount() {
            return PropertyCount.SINGLE;
        }
    },
    /**
     * @see TanimotoBit
     */
    TANIMOTOBIT {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Distance.TanimotoBit");
        }

        @Override
        public String getName() {
            return "Tanimoto Bit";
        }

        @Override
        public <S extends Structure> Distance<S> generateDistance(Collection<PropertyDefinition> propertyVector) {
            return new TanimotoBit<S>(propertyVector);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyType
         * ()
         */
        @Override
        public PropertyType acceptedPropertyType() {
            return PropertyType.BitFingerprint;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyCount()
         */
        @Override
        public PropertyCount acceptedPropertyCount() {
            return PropertyCount.SINGLE;
        }
    },
    /**
     * @see Jaccard
     */
    JACCARD {
        @Override
        public String getInfo() {
            return I18n.get("DendrogramView.Distance.Jaccard");
        }

        @Override
        public String getName() {
            return "Jaccard";
        }

        @Override
        public <S extends Structure> Distance<S> generateDistance(Collection<PropertyDefinition> propertyVector) {
            return new Jaccard<S>(propertyVector);
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyType
         * ()
         */
        @Override
        public PropertyType acceptedPropertyType() {
            return PropertyType.NumericalFingerprint;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.model.clustering.Distance#isSinglePropertyDistance
         * ()
         */
        @Override
        public PropertyCount acceptedPropertyCount() {
            return PropertyCount.SINGLE;
        }
    };
//    /**
//     * @see MCSTanimoto
//     */
//    MCS_TANIMOTO {
//        @Override
//        public String getInfo() {
//            return I18n.get("DendrogramView.Distance.MCSTanimoto");
//        }
//
//        @Override
//        public String getName() {
//            return "MCS Tanimoto";
//        }
//
//        @Override
//        public <S extends Structure> Distance<S> generateDistance(Collection<PropertyDefinition> propertyVector) {
//            return new MCSTanimoto<S>();
//        }
//        
//        /*
//         * (non-Javadoc)
//         * 
//         * @see
//         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyType()
//         */
//        @Override
//        public PropertyType acceptedPropertyType() {
//            return PropertyType.None;
//        }
//
//        /*
//         * (non-Javadoc)
//         * 
//         * @see
//         * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyCount()
//         */
//        @Override
//        public PropertyCount acceptedPropertyCount() {
//            return PropertyCount.NONE;
//        }
//        
//    };
    
    
    /**
     * @param <S>
     *            The type parameter for the Distance
     * @param propertyVector <code>null</code> if no properties are required
     * @return generates a matching distance object
     */
    public abstract <S extends Structure> Distance<S> generateDistance(Collection<PropertyDefinition> propertyVector);

    /**
     * @return a short description of this distanceMethod shown in the View
     */
    public abstract String getInfo();

    /**
     * @return the name of this method
     */
    public abstract String getName();

    /**
     * @return accepted propType
     */
    public abstract PropertyType acceptedPropertyType();

    /**
     * @return if only one property is allowed for calculation (mostly true for
     *         fingerprint distances)
     */
    public abstract PropertyCount acceptedPropertyCount();
}
