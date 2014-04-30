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

import java.util.Collection;

import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.MoleculeNumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.ScaffoldNumProperty;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * The Ward Linkage which agglomerates the clusters which cause the minimum
 * change of the overall inner cluster variance
 * 
 * @author Till Schäfer
 * 
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class WardLinkage<S extends Structure> extends Linkage<S> {

    /**
     * Constructor 
     * @param propertyVector
     */
    public WardLinkage(Collection<PropertyDefinition> propertyVector) {
        super(propertyVector);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NewLinkage#getUpdateFormula()
     */
    @Override
    public LanceWilliamsUpdateFormula getUpdateFormula() {
        return new LanceWilliamsUpdateFormula() {

            /*
             * (non-Javadoc)
             * 
             * @see
             * edu.udo.scaffoldhunter.model.clustering.LanceWilliamsUpdateFormula
             * #newDistance(double, double, double, int, int, int)
             */
            @Override
            public double newDistance(double ki, double kj, double ij, int k, int i, int j) {
                double ai = (i + k) / (double) (i + j + k);
                double aj = (j + k) / (double) (i + j + k);
                double b = -k / (double) (i + j + k);
                return ai * ki + aj * kj + b * ij;
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NewLinkage#doContentMerge(edu
     * .udo.scaffoldhunter.model.db.Structure,
     * edu.udo.scaffoldhunter.model.db.Structure, int, int)
     */
    @SuppressWarnings("unchecked")
    @Override
    public S doContentMerge(S centre1, S centre2, int size1, int size2) {
        S mergedCentre;
        if (centre1.getClass() == Molecule.class) {
            mergedCentre = (S) new Molecule();
        } else {
            mergedCentre = (S) new Scaffold();
        }

        // calculation of the new cluster centre
        for (PropertyDefinition propDef : propertyVector) {
            double prop1 = centre1.getNumPropertyValue(propDef);
            double prop2 = centre2.getNumPropertyValue(propDef);

            // the new centre of the clusters for each dimension
            double centreVal = (size1 * prop1 + size2 * prop2) / (size1 + size2);
            if (centre1.getClass() == Molecule.class) {
                MoleculeNumProperty numProp = new MoleculeNumProperty(propDef, centreVal);
                ((Molecule) mergedCentre).getNumProperties().put(propDef.getId(), numProp);
            } else {
                ScaffoldNumProperty numProp = new ScaffoldNumProperty(propDef, centreVal);
                ((Scaffold) mergedCentre).getNumProperties().put(propDef.getId(), numProp);
            }
        }

        return mergedCentre;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NewLinkage#distancePostProcessing
     * (double, int, int)
     */
    @Override
    public double distancePostProcessing(double dist, int size1, int size2) {
        return (size1 * size2) / (size1 + size2) * Math.pow(dist, 2);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NewLinkage#needsProstProcessing()
     */
    @Override
    public boolean needsProstProcessing() {
        return Linkages.WARD_LINKAGE.needsProstProcessing();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.clustering.NewLinkage#centreBasedLinkage()
     */
    @Override
    public boolean centreBasedLinkage() {
        return Linkages.WARD_LINKAGE.centreBasedLinkage();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.clustering.Linkage#isMetric()
     */
    @Override
    public boolean isMetric() {
        return Linkages.WARD_LINKAGE.isMetric();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.clustering.Linkage#fulfilReproducibility()
     */
    @Override
    public boolean fulfilReducibility() {
        return Linkages.WARD_LINKAGE.fulfilReproducibility();
    }

}
