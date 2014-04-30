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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import java.awt.Color;

/**
 * Holds a value and a Color which are shown in an {@link InfoBar}.
 * 
 * @author Henning Garus
 */
public class PropertyBinValue {

    private final int value;
    private final Color color;
    
    /**
     * @param value the number of structures in the bin
     * @param color the color of the info bar field
     */
    public PropertyBinValue(int value, Color color) {
        this.value = value;
        this.color = color;
    }


    /**
     * @return the number of structures in the bin for one scaffold
     */
    public int getValue() {
        return value;
    }


    /**
     * @return the color
     */
    public Color getColor() {
        return color;
    }

}
