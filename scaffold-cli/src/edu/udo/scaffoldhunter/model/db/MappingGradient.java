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
public class MappingGradient extends DbObject {
    private boolean ascending;
    private Color color1;
    private Color color2;

    
    /**
     * default constructor
     */
    public MappingGradient() { }

    /**
     * @param ascending
     * @param color1
     * @param color2
     */
    public MappingGradient(boolean ascending, Color color1, Color color2) {
        this.ascending = ascending;
        this.color1 = color1;
        this.color2 = color2;
    }

    /**
     * @return if the MappingGradient is ascending or descending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * @param ascending
     *            set if the MappingGradient is ascending or descending
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * @return the color1
     */
    public Color getColor1() {
        return color1;
    }

    /**
     * @param color1
     *            the color1 to set
     */
    public void setColor1(Color color1) {
        this.color1 = color1;
    }

    /**
     * @return the color2
     */
    public Color getColor2() {
        return color2;
    }

    /**
     * @param color2
     *            the color2 to set
     */
    public void setColor2(Color color2) {
        this.color2 = color2;
    }
}
