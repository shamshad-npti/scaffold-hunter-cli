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

import java.util.LinkedList;
import java.util.List;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.MappingType;
import edu.udo.scaffoldhunter.model.VisualFeature;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 *         Defines the mapping from Scaffold or Molecule value to a visual
 *         feature in Tree
 */
public class Mapping extends Preset {
    private PropertyDefinition propertyDefinition;
    /**
     * Which Visual Feature is effected e.g. NodeSize, NodeColor, ...
     */
    private VisualFeature visualFeature;
    /**
     * If the mapping should be cumulative over the subtree or not
     */
    private boolean cumulative;
    /**
     * The kind of mapping e.g. Gradient, Interval
     */
    private MappingType mappingType;
    private List<MappingInterval> orderedIntervals;
    private MappingGradient gradient;
    private AccumulationFunction function;

    
    /**
     * default constructor
     */
    public Mapping() {
        super();
        
        orderedIntervals = new LinkedList<MappingInterval>();
    }

    /**
     * @param propertyDefinition
     * @param visualFeature
     * @param cumulative
     * @param mappingType
     * @param orderedIntervals 
     * @param gradient
     * @param function
     * @param profile 
     * @param title 
     */
    public Mapping(PropertyDefinition propertyDefinition,
            VisualFeature visualFeature, boolean cumulative,
            MappingType mappingType, List<MappingInterval> orderedIntervals,
            MappingGradient gradient, AccumulationFunction function,
            Profile profile, String title) {
        super(profile, title);
        this.propertyDefinition = propertyDefinition;
        this.visualFeature = visualFeature;
        this.cumulative = cumulative;
        this.mappingType = mappingType;
        this.orderedIntervals = orderedIntervals;
        this.gradient = gradient;
        this.function = function;
    }

    /**
     * @return the propertyDefinition
     */
    public PropertyDefinition getPropertyDefinition() {
        return propertyDefinition;
    }

    /**
     * @param propertyDefinition
     *            the propertyDefinition to set
     */
    public void setPropertyDefinition(PropertyDefinition propertyDefinition) {
        this.propertyDefinition = propertyDefinition;
    }

    /**
     * @return the visualFeature
     */
    public VisualFeature getVisualFeature() {
        return visualFeature;
    }

    /**
     * @param visualFeature
     *            the visualFeature to set
     */
    public void setVisualFeature(VisualFeature visualFeature) {
        this.visualFeature = visualFeature;
    }

    /**
     * @return if the Mapping is cumulative over the subtree
     */
    public boolean isCumulative() {
        return cumulative;
    }

    /**
     * @param cumulative
     *            sets if the Mapping is cumulative over the subtree
     */
    public void setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
    }

    /**
     * @return the mappingType
     */
    public MappingType getMappingType() {
        return mappingType;
    }

    /**
     * Sets the mappingType. Deletes other MappingType classes. e.g. The
     * intervals for this mapping are deleted if the new mappingType is a
     * gradient
     * 
     * @param mappingType
     *            the mappingType to set
     */
    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
        if (mappingType != MappingType.Interval) {
            orderedIntervals = null;
        }
        if (mappingType != MappingType.Gradient) {
            gradient = null;
        }
    }

    /**
     * @return the intervals. Is empty if the MappingType != interval
     */
    public List<MappingInterval> getOrderedIntervals() {
        return orderedIntervals;
    }

    /**
     * @param orderedIntervals 
     *            the intervals to set. Only works if mappingType = interval
     */
    public void setOrderedIntervals(List<MappingInterval> orderedIntervals) {
        if (mappingType == MappingType.Interval) {
            this.orderedIntervals = orderedIntervals;
        }
    }

    /**
     * @return the gradient
     */
    public MappingGradient getGradient() {
        return gradient;
    }

    /**
     * @param gradient
     *            the gradient to set
     */
    public void setGradient(MappingGradient gradient) {
        this.gradient = gradient;
    }

    /**
     * @param function
     *            the function to set
     */
    public void setFunction(AccumulationFunction function) {
        this.function = function;
    }

    /**
     * @return the function
     */
    public AccumulationFunction getFunction() {
        return function;
    }
}
