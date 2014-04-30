/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffold Hunter source tree
 * for details.
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

package edu.udo.scaffoldhunter.model.db;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.AccumulationFunction;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public abstract class Filter extends DbObject {
    private Filterset filterset;
    private String propDefKey;
    private AccumulationFunction accumulationFunction;

    /**
     * default Constructor
     */
    public Filter() {
    }

    /**
     * @param filterset
     * @param propDef
     * @param accumulationFunction
     */
    public Filter(Filterset filterset, PropertyDefinition propDef, AccumulationFunction accumulationFunction) {
        this.filterset = filterset;
        if (propDef != null)
            this.propDefKey = propDef.getKey();
        this.accumulationFunction = accumulationFunction;
    }

    /**
     * @return the filterset
     */
    public Filterset getFilterset() {
        return filterset;
    }

    /**
     * @param filterset
     *            the filterset to set
     */
    public void setFilterset(Filterset filterset) {
        this.filterset = filterset;
    }

    /**
     * Attention Profile.currentSession must be not null for this operation
     * 
     * @return the {@link PropertyDefinition} of the current {@link Session}s
     *         {@link Dataset} or null if a PropertyDefinition with the key
     *         could not be found
     */
    public PropertyDefinition getPropDef() {
        Preconditions.checkNotNull(getFilterset().getProfile().getCurrentSession());

        return getPropDef(getFilterset().getProfile().getCurrentSession().getDataset());
    }

    /**
     * @param dataset
     *            The dataset
     * 
     * @return the {@link PropertyDefinition} of the given {@link Dataset} or
     *         null if a PropertyDefinition with the key could not be found
     */
    public PropertyDefinition getPropDef(Dataset dataset) {
        Preconditions.checkNotNull(dataset);

        return dataset.getPropertyDefinitions().get(propDefKey);
    }

    /**
     * @param propDef
     *            the propDef to set
     */
    public void setPropDef(PropertyDefinition propDef) {
        if (this.getClass() == NumFilter.class) {
            Preconditions.checkArgument(!propDef.isStringProperty());
        } else {
            Preconditions.checkArgument(propDef.isStringProperty());
        }

        this.propDefKey = propDef.getKey();
    }

    /**
     * @return the accumulationFunction
     */
    public AccumulationFunction getAccumulationFunction() {
        return accumulationFunction;
    }

    /**
     * @param accumulationFunction
     *            the accumulationFunction to set
     */
    public void setAccumulationFunction(AccumulationFunction accumulationFunction) {
        this.accumulationFunction = accumulationFunction;
    }
}
