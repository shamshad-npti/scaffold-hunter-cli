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

import java.awt.Color;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class MappingInterval extends DbObject {
    private Mapping mapping;
    /**
     * lower bound of the interval the upper bound is the next MappingInterval
     */
    private float lowerBound;
    private Color color;
    /**
     * Value to which the interval is mapped to e.g. Node size
     */
    private float value;

    
    /**
     * default constructor
     */
    public MappingInterval() {
    }

    /**
     * @param mapping
     * @param lowerBound
     * @param color
     * @param value
     */
    public MappingInterval(Mapping mapping, float lowerBound, Color color,
            float value) {
        this.mapping = mapping;
        this.lowerBound = lowerBound;
        this.color = color;
        this.value = value;
    }

    /**
     * @return the lowerBound
     */
    public float getLowerBound() {
        return lowerBound;
    }

    /**
     * @param lowerBound
     *            the lowerBound to set
     */
    public void setLowerBound(float lowerBound) {
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

    /**
     * @return the value
     */
    public float getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(float value) {
        this.value = value;
    }

    /**
     * @return the mapping
     */
    public Mapping getMapping() {
        return mapping;
    }

    /**
     * @param mapping
     *            the mapping to set
     */
    public void setMapping(Mapping mapping) {
        this.mapping = mapping;
    }
}
