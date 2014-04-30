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

package edu.udo.scaffoldhunter.view.util;

import javax.swing.ToolTipManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.util.Copyable;

/**
 * Class to store the configuration for a property that should be shown in the
 * tooltip genererated by {@link ToolTipManager}.
 * 
 * @author Philipp Lewe
 * @author Henning Garus
 * 
 */
public class ToolTipPropertyConfigurationEntry extends Copyable {
    private static Logger logger = LoggerFactory.getLogger(ToolTipPropertyConfigurationEntry.class);

    private final String propertyDefinition;

    private AccumulationFunction accumulationFunction = AccumulationFunction.Average;

    private boolean accumulationWithSubtree = false;

    /**
     * 
     * @param propertyDefinitionKey
     */
    public ToolTipPropertyConfigurationEntry(String propertyDefinitionKey) {
        this.propertyDefinition = propertyDefinitionKey;
    }

    /**
     * @param propertyDefinition
     * @param accumulationFunction
     * @param accumulationWithSubtree
     */
    public ToolTipPropertyConfigurationEntry(PropertyDefinition propertyDefinition,
            AccumulationFunction accumulationFunction, boolean accumulationWithSubtree) {
        this(propertyDefinition.getKey());
        this.accumulationFunction = accumulationFunction;
        this.accumulationWithSubtree = accumulationWithSubtree;
    }

    /**
     * @param dataset
     *            the current dataset
     * @return the propertyDefinition
     * @throws IllegalArgumentException
     *             if the propertyDefinition used in this configuration entry is
     *             not defined for the given dataset
     */
    public PropertyDefinition getPropertyDefinition(Dataset dataset) throws IllegalArgumentException {
        if (dataset.getPropertyDefinitions().containsKey(propertyDefinition)) {
            return dataset.getPropertyDefinitions().get(propertyDefinition);
        } else {
            String errorMsg = String.format("The property definition with key \"%s\" is not defined for the dataset \"%s\"",
                    propertyDefinition, dataset.getTitle());
            logger.info(errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
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

    /**
     * @return the accumulationWithSubtree
     */
    public boolean isAccumulationWithSubtree() {
        return accumulationWithSubtree;
    }

    /**
     * @param accumulationWithSubtree
     *            the accumulationWithSubtree to set
     */
    public void setAccumulationWithSubtree(boolean accumulationWithSubtree) {
        this.accumulationWithSubtree = accumulationWithSubtree;
    }

    /**
     * @return the propertyDefinitionKey
     */
    public String getPropertyDefinitionKey() {
        return propertyDefinition;
    }
}
