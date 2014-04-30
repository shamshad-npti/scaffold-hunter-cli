/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;

import edu.udo.scaffoldhunter.view.scaffoldtree.Sorting.SortSettings;

/**
 * State for the current sorting of a scaffold tree. Contains a {@link SortSettings} object and a list of colors and double values,
 * which are used for representation in the sort legend sidebar item.
 * 
 * @author Sven Schrinner
 */
public class SortState implements Serializable {

    private SortSettings sortSettings = new SortSettings();
    private ArrayList<Color> colors = new ArrayList<Color>();
    private ArrayList<Double> values = new ArrayList<Double>();
    
    /**
     * Returns the stored {@link SortState} object.
     * @return the stored {@link SortState} object
     */
    public SortSettings getSortSettings() {
        return sortSettings;
    }

    /**
     * Stores a {@link SortState} object.
     * @param sortSettings
     *            the {@link SortState} object
     */
    public void setSortSettings(SortSettings sortSettings) {
        this.sortSettings = sortSettings;
    }

    /**
     * Returns the stored list of colors.
     * @return the list of colors
     */
    public ArrayList<Color> getColors() {
        return colors;
    }

    /**
     * Stores a list of colors, which are used by a {@link SortLegendPanel} for representation purposes.
     * @param colors list of colors
     */
    public void setColors(ArrayList<Color> colors) {
        this.colors = colors;
    }

    /**
     * Returns the stored list of numeric values.
     * @return the list of numeric values
     */
    public ArrayList<Double> getValues() {
        return values;
    }

    /**
     * Stores a list of numeric values, which are used by a {@link SortLegendPanel} for representation purposes.
     * @param values list of numeric values
     */
    public void setValues(ArrayList<Double> values) {
        this.values = values;
    }    
}