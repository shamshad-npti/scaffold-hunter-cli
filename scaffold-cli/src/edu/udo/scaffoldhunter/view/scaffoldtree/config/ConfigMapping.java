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

package edu.udo.scaffoldhunter.view.scaffoldtree.config;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.MappingType;
import edu.udo.scaffoldhunter.model.VisualFeature;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Mapping;
import edu.udo.scaffoldhunter.model.db.MappingGradient;
import edu.udo.scaffoldhunter.model.db.MappingInterval;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.util.Copyable;

/**
 * Represents a mapping of a property to a visual feature. This is the
 * configuration equivalent of {@link Mapping}.
 * 
 * @author Henning Garus
 */
public class ConfigMapping extends Copyable {

    /**
     * the property maximum property value
     * 
     * @see #getMaximumPropertyValue()
     */
    public static final String MINIMUM_PROPERTY_VALUE = "MINIMUM";
    /**
     * the property minimum property value
     * 
     * @see #getMinimumPropertyValue()
     */
    public static final String MAXIMUM_PROPERTY_VALUE = "MAXIMUM";

    /**
     * the property number of distinct values
     * 
     * @see #getDistinctStringValues
     */
    public static final String DISTINCT_STRING_VALUES = "DISTINCT_STRING_VALUES";

    private List<String> distinctStringValues = null;
    private String property;
    private final VisualFeature visualFeature;
    private MappingType mappingType = MappingType.Gradient;
    private boolean cumulative;
    private boolean subsetForBounds;
    private AccumulationFunction function = AccumulationFunction.Average;
    private final List<Interval> intervals;
    private Color gradientColor1 = new Color(0, 0, 100);
    private Color gradientColor2 = new Color(100, 255, 100);
    private boolean gradientAscending = true;
    private double minimumPropertyValue = Double.NEGATIVE_INFINITY;
    private double maximumPropertyValue = Double.POSITIVE_INFINITY;

    private final PropertyChangeSupport listeners = new PropertyChangeSupport(this);

    /**
     * Create a new ConfigMapping
     * 
     * @param feature
     *            the visual feature mapped to by this mapping, cannot be
     *            <code>null</code>.
     */
    public ConfigMapping(VisualFeature feature) {
        Preconditions.checkNotNull(feature);
        this.visualFeature = feature;
        switch (feature) {
        case EdgeThickness:
        case EdgeColor:
        case NodeBackgroundColor:
        case NodeSize:
            mappingType = MappingType.Gradient;
            break;
        case InfoBar:
            mappingType = MappingType.Interval;
            break;
        case Label:
            // TODO maybe add a new Mapping Type
            mappingType = MappingType.Gradient;
            break;
        default:
            mappingType = MappingType.Gradient;
        }
        intervals = Lists.newArrayList();
    }

    /**
     * Create a new ConfigMapping based on the provided <code>Mapping</code>
     * 
     * @param mapping
     *            the mapping which is used to initialize this ConfigMapping
     */
    public ConfigMapping(Mapping mapping) {
        this.property = mapping.getPropertyDefinition().getKey();
        this.visualFeature = mapping.getVisualFeature();
        this.mappingType = mapping.getMappingType();
        this.cumulative = mapping.isCumulative();
        this.function = mapping.getFunction();
        switch (mappingType) {
        case Interval:
            intervals = Lists.newArrayListWithExpectedSize(mapping.getOrderedIntervals().size());
            for (MappingInterval interval : mapping.getOrderedIntervals()) {
                intervals.add(new Interval(interval));
            }
            break;
        case Gradient:
            gradientColor1 = mapping.getGradient().getColor1();
            gradientColor2 = mapping.getGradient().getColor2();
            gradientAscending = mapping.getGradient().isAscending();
            intervals = Lists.newArrayList();
        default:
            throw new AssertionError("Unhandled mappingType");
        }
    }

    /**
     * Create a new <code>Mapping</code> from this <code>ConfigMapping</code>
     * 
     * @param profile
     *            the profile for the new mapping
     * @param title
     *            the title of the new mapping
     * 
     * @return a new mapping based on this <code>ConfigMapping</code>
     */
    public Mapping newMapping(Profile profile, String title) {
        PropertyDefinition propDef = null;
        for (PropertyDefinition propertyDefinition : profile.getCurrentSession().getDataset().getPropertyDefinitions()
                .values()) {
            if (propertyDefinition.getKey().equals(property)) {
                propDef = propertyDefinition;
                break;
            }
        }
        assert (propDef != null);
        MappingGradient gradient = new MappingGradient(gradientAscending, gradientColor1, gradientColor2);
        Mapping mapping = new Mapping(propDef, visualFeature, cumulative, mappingType, new ArrayList<MappingInterval>(
                intervals.size()), gradient, function, profile, title);
        for (Interval i : intervals)
            mapping.getOrderedIntervals().add(i.newMappingInterval(mapping));
        return mapping;
    }

    /**
     * For Interval Mappings this can be used to query the color of the value's
     * interval.
     * <p>
     * For a Gradient Mapping a value between 0 and 1 can be used to index into
     * the gradient defined by both gradient Colors. For a GradientMapping
     * values larger 1 are treated as 1, values smaller than 0 are treated as 0.
     * 
     * @param value
     *            the value which is mapped to a color
     * @return a Color based on the mapping type, interval colors or gradient
     *         colors respectively and the provided value
     */
    public Color getColor(double value) {
        // Lower bound is not included except for the lowest interval.
        // As a result, intervals with lb=ub are not supported.
        switch (mappingType) {
        case Interval:
            for (Interval i : Lists.reverse(intervals)) {
                if (value > i.getLowerBound())
                    return i.getColor();
            }
            if (!intervals.isEmpty())
            {
                return intervals.get(0).getColor();
            }
            break;
        case Gradient:
            if (Double.isNaN(value))
                return null;
            if (value > 1)
                value = 1;
            else if (value < 0)
                value = 0;
            int r1 = gradientColor1.getRed();
            int r2 = gradientColor2.getRed();
            int g1 = gradientColor1.getGreen();
            int g2 = gradientColor2.getGreen();
            int b1 = gradientColor1.getBlue();
            int b2 = gradientColor2.getBlue();

            int r = (int) ((r2 - r1) * value) + r1;
            int g = (int) ((g2 - g1) * value) + g1;
            int b = (int) ((b2 - b1) * value) + b1;

            return new Color(r, g, b);
        }
        return null;
    }

    /**
     * @param dataset
     *            the dataset which is associated with this mapping the property
     *            definition will be retrieved from this dataset
     * @return the property
     */
    public PropertyDefinition getProperty(Dataset dataset) {
        if (property == null)
            return null;
        return dataset.getPropertyDefinitions().get(property);
    }

    /**
     * 
     * @return <code>true</code> if a property is set, <code>false</code> if the
     *         property is set to <code>null</code>.
     */
    public boolean hasNoProperty() {
        return property == null;
    }

    /**
     * @param propDef
     *            the propertyDefiniton to set
     */
    public void setProperty(PropertyDefinition propDef) {
        this.property = propDef == null ? null : propDef.getKey();
    }

    /**
     * @return the visualFeature
     */
    public VisualFeature getVisualFeature() {
        return visualFeature;
    }

    /**
     * @return the mappingType
     */
    public MappingType getMappingType() {
        return mappingType;
    }

    /**
     * @param mappingType
     *            the mappingType to set
     */
    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    /**
     * @return the function
     */
    public AccumulationFunction getFunction() {
        return function;
    }

    /**
     * @param function
     *            the function to set
     */
    public void setFunction(AccumulationFunction function) {
        this.function = function;
    }

    /**
     * @return the cumulative
     */
    public boolean isCumulative() {
        return cumulative;
    }

    /**
     * @param cumulative
     *            the cumulative to set
     */
    public void setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
    }

    /**
     * @return the subsetForBounds
     */
    public boolean isSubsetForBounds() {
        return subsetForBounds;
    }

    /**
     * @param subsetForBounds the subsetForBounds to set
     */
    public void setSubsetForBounds(boolean subsetForBounds) {
        this.subsetForBounds = subsetForBounds;
    }

    /**
     * @return the intervals
     */
    public List<Interval> getIntervals() {
        return intervals;
    }

    /**
     * @return the gradientColor1
     */
    public Color getGradientColor1() {
        return gradientColor1;
    }

    /**
     * @param gradientColor1
     *            the gradientColor1 to set
     */
    public void setGradientColor1(Color gradientColor1) {
        this.gradientColor1 = gradientColor1;
    }

    /**
     * @return the gradientColor2
     */
    public Color getGradientColor2() {
        return gradientColor2;
    }

    /**
     * @param gradientColor2
     *            the gradientColor2 to set
     */
    public void setGradientColor2(Color gradientColor2) {
        this.gradientColor2 = gradientColor2;
    }

    /**
     * @return the gradientAscending
     */
    public boolean isGradientAscending() {
        return gradientAscending;
    }

    /**
     * @param gradientAscending
     *            the gradientAscending to set
     */
    public void setGradientAscending(boolean gradientAscending) {
        this.gradientAscending = gradientAscending;
    }

    /**
     * @return the minimumPropertyValue
     */
    public double getMinimumPropertyValue() {
        return minimumPropertyValue;
    }

    /**
     * @param minimumPropertyValue
     *            the minimumPropertyValue to set
     */
    public void setMinimumPropertyValue(double minimumPropertyValue) {
        double oldvalue = this.minimumPropertyValue;
        this.minimumPropertyValue = minimumPropertyValue;
        listeners.firePropertyChange(new PropertyChangeEvent(this, MINIMUM_PROPERTY_VALUE, new Double(oldvalue),
                new Double(minimumPropertyValue)));
    }

    /**
     * @return the maximumPropertyValue
     */
    public double getMaximumPropertyValue() {
        return maximumPropertyValue;
    }

    /**
     * @param maximumPropertyValue
     *            the maximumPropertyValue to set
     */
    public void setMaximumPropertyValue(double maximumPropertyValue) {
        double oldvalue = this.maximumPropertyValue;
        this.maximumPropertyValue = maximumPropertyValue;
        listeners.firePropertyChange(MAXIMUM_PROPERTY_VALUE, Double.valueOf(oldvalue),
                Double.valueOf(maximumPropertyValue));
    }

    /**
     * @return the distinctStringValues
     */
    public List<String> getDistinctStringValues() {
        return distinctStringValues;
    }

    /**
     * @param distinctValues
     *            the distinctStringValues to set
     */
    public void setDistinctStringValues(List<String> distinctValues) {
        List<String> oldvalue = getDistinctStringValues();
        this.distinctStringValues = distinctValues;
        listeners.firePropertyChange(DISTINCT_STRING_VALUES, oldvalue, getDistinctStringValues());
    }

    /**
     * {@link PropertyChangeSupport#addPropertyChangeListener(String, PropertyChangeListener)}
     * 
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        listeners.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * {@link PropertyChangeSupport#removePropertyChangeListener(String, PropertyChangeListener)}
     * 
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListeners(String propertyName, PropertyChangeListener listener) {
        listeners.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Defines a single interval used for interval mappings
     * 
     */
    public static class Interval implements Serializable {

        private double lowerBound = Double.NaN;
        private Color color;
        private String string = null;

        /**
         * Create a new Interval, the upper bound of an interval is defined by
         * the lower bound of the next interval or is treated as infinity if
         * there is no next interval
         * 
         * @param lowerBound
         *            lower end of this interval
         * @param color
         *            color associated with this interval
         */
        public Interval(double lowerBound, Color color) {
            this.lowerBound = lowerBound;
            this.color = color;
        }

        /**
         * Create a new Interval which represents some String.
         * 
         * @param color
         *            color associated with this interval
         * @param string
         *            string associated with this interval
         */
        public Interval(String string, Color color) {
            this.color = color;
            this.string = string;
        }

        /**
         * @return the string
         */
        public String getString() {
            return string;
        }

        /**
         * @param string
         *            the string to set
         */
        public void setString(String string) {
            this.string = string;
        }

        private Interval(MappingInterval interval) {
            this.lowerBound = interval.getLowerBound();
            this.color = interval.getColor();
        }

        private MappingInterval newMappingInterval(Mapping mapping) {
            return new MappingInterval(mapping, (float) lowerBound, color, 0);
        }

        /**
         * @return the lowerBound
         */
        public double getLowerBound() {
            return lowerBound;
        }

        /**
         * @param lowerBound
         *            the lowerBound to set
         */
        public void setLowerBound(double lowerBound) {
            this.lowerBound = lowerBound;
        }

        /**
         * @return the color
         */
        public Color getColor() {
            return color;
        }

        /**
         * @param color
         *            the color to set
         */
        public void setColor(Color color) {
            this.color = color;
        }
    }

}
