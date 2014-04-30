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

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.NumComparisonFunction;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 *
 */
public class NumFilter extends Filter {
    private double value;
    private NumComparisonFunction comparisonFunction;
    
    
    /**
     * default Constructor
     */
    public NumFilter() {
        super();
    }

    /**
     * @param filterset 
     * @param propertyDefinition 
     * @param accumulationFunction 
     * @param value
     * @param comparisonFunction
     */
    public NumFilter(Filterset filterset, PropertyDefinition propertyDefinition,
            AccumulationFunction accumulationFunction, double value, NumComparisonFunction comparisonFunction) {
        super(filterset, propertyDefinition, accumulationFunction);
        this.value = value;
        this.comparisonFunction = comparisonFunction;
    }

    /**
     * @return the value
     */
    public double getValue() {
        return value;
    }
    
    /**
     * @param value the value to set
     */
    public void setValue(double value) {
        this.value = value;
    }
    
    /**
     * @return the comparisonFunction
     */
    public NumComparisonFunction getComparisonFunction() {
        return comparisonFunction;
    }
    
    /**
     * @param comparisonFunction the comparisonFunction to set
     */
    public void setComparisonFunction(NumComparisonFunction comparisonFunction) {
        this.comparisonFunction = comparisonFunction;
    }
}
