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

package edu.udo.scaffoldhunter.util;

import java.awt.Color;

/**
 * A util class that can hold several color functions. 
 * 
 * @author Lappie
 *
 */
public class ColorFunctions {

    /**
     * Transforms a HSV color to a RGB value stored in a Color-class. 
     * 
     * HSV gives a "more intuitive and perceptually relevant than the cartesian (cube) representation"
     * It is useful for finding more friendly colors or finding a series of colors that follow up, making 
     * a better gradient than rgb could
     * 
     * @param hue
     * @param saturation
     * @param value
     * @return A Color class with the Color given as HSV parameter
     */
    public static Color hsvToRgb(float hue, float saturation, float value) {

        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h) {
          case 0: return new Color(value, t, p);
          case 1: return new Color(q, value, p);
          case 2: return new Color(p, value, t);
          case 3: return new Color(p, q, value);
          case 4: return new Color(t, p, value);
          case 5: return new Color(value, p, q);
          default: throw new RuntimeException("Something went wrong when converting from HSV to RGB. Input was " + hue + ", " + saturation + ", " + value);
        }
    }
}
