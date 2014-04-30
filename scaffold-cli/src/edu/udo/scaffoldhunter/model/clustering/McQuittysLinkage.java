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

import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * The McQuitty's Linkage
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class McQuittysLinkage<S extends Structure> extends Linkage<S> {

    /**
     * Constructor
     * 
     * @param propertyVector
     *            the used {@link Property}s for distance calculation
     */
    public McQuittysLinkage(Collection<PropertyDefinition> propertyVector) {
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
                return 0.5 * ki + 0.5 * kj;
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
    @Override
    public S doContentMerge(S centre1, S centre2, int size1, int size2) {
        throw new UnsupportedOperationException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NewLinkage#centreBasedLinkage()
     */
    @Override
    public boolean centreBasedLinkage() {
        return Linkages.MCQUITTYS_LINKAGE.centreBasedLinkage();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.NewLinkage#needsProstProcessing()
     */
    @Override
    public boolean needsProstProcessing() {
        return Linkages.MCQUITTYS_LINKAGE.needsProstProcessing();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.clustering.Linkage#isMetric()
     */
    @Override
    public boolean isMetric() {
        return Linkages.MCQUITTYS_LINKAGE.isMetric();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.clustering.Linkage#fulfilReproducibility()
     */
    @Override
    public boolean fulfilReducibility() {
        return Linkages.MCQUITTYS_LINKAGE.fulfilReproducibility();
    }

}
