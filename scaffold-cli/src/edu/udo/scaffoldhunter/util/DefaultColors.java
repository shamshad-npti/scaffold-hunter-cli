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

package edu.udo.scaffoldhunter.util;

import java.awt.Color;

/**
 * @author Philipp Kopp
 * @author Nils Kriege
 */
public class DefaultColors {
    /**
     * contains some easily distinguishable colors
     */
    public static final Color[] COLORS = {
        new Color(255, 0, 0),
        new Color(0, 255, 0),
        new Color(0, 0, 255),
        new Color(255, 255, 0),
        new Color(255, 0, 255),
        new Color(0, 255, 255),
        new Color(100, 0, 0),
        new Color(0, 100, 0),
        new Color(0, 0, 100),
        new Color(100, 100, 0),
        new Color(100, 0, 100),
        new Color(0, 100, 100),
        new Color(200, 0, 0),
        new Color(0, 200, 0),
        new Color(0, 0, 200),
        new Color(200, 200, 0),
        new Color(200, 0, 200),
        new Color(0, 200, 200),
        new Color(100, 100, 100),
        new Color(200, 200, 200)
    };
    
    private static final Color[] MUTED_COLORS = {
        new Color(228,26,28),
        new Color(55,126,184),
        new Color(77,175,74),
        new Color(152,78,163),
        new Color(255,127,0),
        new Color(255,255,51),
        new Color(166,86,40),
        new Color(247,129,191),
        new Color(153,153,153)
    };
    
    /**
     * Returns the color at position <code>i % 20</code> in a list of 
     * 20 easily distinguishable default colors.
     * @param i index of the color
     * @return a color
     */
    public static Color getColor(int i) {
        return COLORS[i % COLORS.length];
    }

    /**
     * Returns the i-th muted color. Note that the first 9 colors are unique
     * and the subsequent colors are darker versions of the previous colors. 
     * @param i index of the color
     * @return a muted color
     */
    public static Color getMutedColor(int i) {
        return getDarkerColors(MUTED_COLORS, i);        
    }
    
    /**
     * Returns the color at position i in the given array. If i exceeds the bounds
     * of the array, a darker version of the color at <code>i % colors.length</code>
     * is returned.
     */
    private static Color getDarkerColors(Color[] colors, int i) {
        Color c = colors[i % colors.length];
        while (i >= colors.length) {
            c = c.darker();
            i -= colors.length;
        }
        return c;
    }
}
