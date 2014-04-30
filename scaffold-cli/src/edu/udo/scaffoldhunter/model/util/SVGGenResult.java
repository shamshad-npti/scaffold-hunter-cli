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

package edu.udo.scaffoldhunter.model.util;

/**
 * Wrapper class to store the return values of the
 * <code>calcSVGString()</code> method in the SVGGen class.
 * 
 * @author Philipp Lewe
 * 
 */
public class SVGGenResult {
    
    private String svgString = "";
    private int width = 1;
    private int height = 1;
    
    /**
     * 
     */
    public SVGGenResult() {}
    
    /**
     * Creates and SVG
     * @param svgString
     * @param width
     * @param height
     */
    public SVGGenResult(String svgString, int width, int height) {
        this.svgString = svgString;
        this.width = width;
        this.height = height;
    }

    /**
     * @param svgString the svgString to set
     */
    public void setSvgString(String svgString) {
        this.svgString = svgString;
    }

    /**
     * @return the svgString
     */
    public String getSvgString() {
        return svgString;
    }

    /**
     * @param width the width to set
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * @return the width
     */
    public int getWidth() {
        return width;
    }

    /**
     * @param height the height to set
     */
    public void setHeight(int height) {
        this.height = height;
    }

    /**
     * @return the height
     */
    public int getHeight() {
        return height;
    }

}
