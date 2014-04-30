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

import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Interface for a Linkage Strategy (e.g. Ward, Sinlge-Link)
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public abstract class Linkage<S extends Structure> {
    protected Collection<PropertyDefinition> propertyVector;

    /**
     * Constructor
     * 
     * @param propertyVector
     *            the used {@link Property}s for distance calculation
     */
    public Linkage(Collection<PropertyDefinition> propertyVector) {
        this.propertyVector = propertyVector;
    }

    /**
     * Returns a {@link LanceWilliamsUpdateFormula} for distance updates.
     * 
     * @return the {@link LanceWilliamsUpdateFormula}
     */
    public abstract LanceWilliamsUpdateFormula getUpdateFormula();

    /**
     * Some {@link Linkage} support centre based distance calculations for
     * clusters. This method creates the new centre content for cluster
     * representative based on the two merged centres and the cluster sizes.
     * 
     * Attention: it is only valid to call this method if vectorSpaceLinkage()
     * is true!
     * 
     * @param centre1
     *            the centre of the first cluster
     * @param centre2
     *            the centre of the second cluster
     * @param size1
     *            the size of the first cluster
     * @param size2
     *            the size of the second cluster
     * @return the centre of the new formed cluster
     * @throws UnsupportedOperationException
     *             if centreBasedLinkage() is false.
     */
    public abstract S doContentMerge(S centre1, S centre2, int size1, int size2);

    /**
     * Determines if the Linkage allows centre based distance calculations for
     * clusters.
     * 
     * @return if the linkage allows centre base distance calculations
     */
    public abstract boolean centreBasedLinkage();

    /**
     * Some {@link Linkage}s (e.g. Ward) need a post processing of the
     * {@link Distance}s calculated by cluster centres or singletons.
     * 
     * @param dist
     *            the {@link Distance} between cluster centres
     * @param size1
     *            the size of the first cluster
     * @param size2
     *            the size of the second cluster
     * @return the adjusted distance
     */
    public double distancePostProcessing(double dist, int size1, int size2) {
        return dist;
    }

    /**
     * Returns if a distancePostProcessing needs to be called (for performance
     * reasons).
     * 
     * @return if post processing is needed
     */
    public abstract boolean needsProstProcessing();

    /**
     * Returns if the {@link Linkage} fulfils the metric conditions
     * 
     * @return if the {@link Linkage} is metric
     */
    public abstract boolean isMetric();
    
    /**
     * Returns if the Linkage fulfils the reducibility property
     * 
     * @return if the {@link Linkage} fulfils the reducibility property
     */
    public abstract boolean fulfilReducibility();
}
