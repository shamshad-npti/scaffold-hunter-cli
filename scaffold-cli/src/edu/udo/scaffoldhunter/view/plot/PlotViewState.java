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

package edu.udo.scaffoldhunter.view.plot;

import java.awt.Color;

import edu.udo.scaffoldhunter.model.ViewState;

/**
 *
 */
public class PlotViewState extends ViewState {

    boolean valid = false;
    
    boolean showTicks;
    boolean showGrid;
    int jitter;
    
    // indexes of comboboxes
    int highlightIndex;
    int xMappingIndex;
    int yMappingIndex; 
    int zMappingIndex; 
    int colorMappingIndex;
    int sizeMappingIndex;
    
    // scrolling
    int scrollPosX;
    int scrollPosY;

    // dotcolors
    Color defaultDotcolor;
    Color minDotcolor;
    Color maxDotcolor;
    
    // dotsizes
    int defaultDotsizeIndex;
    int minDotsizeIndex;
    int maxDotsizeIndex;
    
    // zoom factors
    double metaScale;
    double xAxisScale; 
    double yAxisScale;
    double zAxisScale;
    
    // rotation
    double metaRotationAlpha;
    double metaRotationBeta;
    
    // hyperplanes
    double hpXmin, hpXmax;
    double hpYmin, hpYmax;
    double hpZmin, hpZmax;
    double hpColormin, hpColormax;
    double hpSizemin, hpSizemax;
    boolean applyHpXtoAxis, applyHpYtoAxis, applyHpZtoAxis, applyHpColorToAxis, applyHpSizeToAxis;
    
    transient boolean _applied = false;
    
    
    /**
     * @return
     *  true, when this is a valid state
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * @param valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    
    /**
     * @return
     *  true if the grid should be shown
     */
    public boolean isShowGrid() {
        return showGrid;
    }
    
    /**
     * @param showGrid
     */
    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }


    /**
     * @return
     *  true if the ticks should be shown
     */
    public boolean isShowTicks() {
        return showTicks;
    }
    
    /**
     * @param showTicks
     */
    public void setShowTicks(boolean showTicks) {
        this.showTicks = showTicks;
    }

    
    /**
     * @return
     *  the jitter value
     */
    public int getJitter() {
        return jitter;
    }
    
    /**
     * @param jitter
     */
    public void setJitter( int jitter ) {
        this.jitter = jitter;
    }
    
    
    /**
     * @return
     *  the index of the combobox for the mapping of the highlighting
     */
    public int getHighlightIndex() {
        return highlightIndex;
    }

    /**
     * @param highlightIndex
     */
    public void setHighlightIndex( int highlightIndex) {
        this.highlightIndex = highlightIndex ;
    }

    
    /**
     * @return
     *  the index of the combobox for the mapping of the x-axis
     */
    public int getXMappingIndex() {
        return xMappingIndex;
    }

    /**
     * @param xMappingIndex
     */
    public void setXMappingIndex( int xMappingIndex) {
        this.xMappingIndex= xMappingIndex ;
    }


    /**
     * @return
     *  the index of the combobox for the mapping of the y-axis
     */
    public int getYMappingIndex() {
        return yMappingIndex;
    }

    /**
     * @param yMappingIndex
     */
    public void setYMappingIndex( int yMappingIndex) {
        this.yMappingIndex= yMappingIndex ;
    }


    /**
     * @return
     *  the index of the combobox for the mapping of the z-axis
     */
    public int getZMappingIndex() {
        return zMappingIndex;
    }

    /**
     * @param zMappingIndex
     */
    public void setZMappingIndex( int zMappingIndex) {
        this.zMappingIndex= zMappingIndex ;
    }

    
    /**
     * @return
     *  the index of the combobox for the mapping of the color-axis
     */
    public int getColorMappingIndex() {
        return colorMappingIndex;
    }

    /**
     * @param colorMappingIndex
     */
    public void setColorMappingIndex( int colorMappingIndex) {
        this.colorMappingIndex = colorMappingIndex ;
    }
    

    /**
     * @return
     *  the index of the combobox for the mapping of the size-axis
     */
    public int getSizeMappingIndex() {
        return sizeMappingIndex;
    }

    /**
     * @param sizeMappingIndex
     */
    public void setSizeMappingIndex( int sizeMappingIndex) {
        this.sizeMappingIndex = sizeMappingIndex ;
    }
    
    
    /**
     * @return
     *  the x scrollpos
     */
    public int getScrollPosX() {
        return scrollPosX;
    }
    
    /**
     * @param scrollPosX
     */
    public void setScrollPosX(int scrollPosX) {
        this.scrollPosX = scrollPosX;
    }

    /**
     * @return
     *  the y scrollpos
     */
    public int getScrollPosY() {
        return scrollPosY;
    }
    
    /**
     * @param scrollPosY
     */
    public void setScrollPosY(int scrollPosY) {
        this.scrollPosY = scrollPosY;
    }

    
    /**
     * @return
     *  the default dotcolor
     */
    public Color getDefaultDotcolor() {
        return defaultDotcolor;
    }
    
    /**
     * 
     * @param defaultDotcolor
     */
    public void setDefaultDotcolor(Color defaultDotcolor) {
        this.defaultDotcolor = defaultDotcolor;
    }
    
    
    /**
     * @return
     *  the min dotcolor
     */
    public Color getMinDotcolor() {
        return minDotcolor;
    }
    
    /**
     * 
     * @param minDotcolor
     */
    public void setMinDotcolor(Color minDotcolor) {
        this.minDotcolor = minDotcolor;
    }
    
    
    /**
     * @return
     *  the max dotcolor
     */
    public Color getMaxDotcolor() {
        return maxDotcolor;
    }
    
    /**
     * 
     * @param maxDotcolor
     */
    public void setMaxDotcolor(Color maxDotcolor) {
        this.maxDotcolor = maxDotcolor;
    }
    
    
    /**
     * @return
     *  the default dotsize
     */
    public int getDefaultDotsizeIndex() {
        return defaultDotsizeIndex;
    }
    
    /**
     * @param defaultDotsizeIndex
     */
    public void setDefaultDotsizeIndex(int defaultDotsizeIndex) {
        this.defaultDotsizeIndex = defaultDotsizeIndex;
    }

    /**
     * @return
     *  the min dotsize
     */
    public int getMinDotsizeIndex() {
        return minDotsizeIndex;
    }
    
    /**
     * @param minDotsizeIndex
     */
    public void setMinDotsizeIndex(int minDotsizeIndex) {
        this.minDotsizeIndex = minDotsizeIndex;
    }

    /**
     * @return
     *  the max dotsize
     */
    public int getMaxDotsizeIndex() {
        return maxDotsizeIndex;
    }
    
    /**
     * @param maxDotsizeIndex
     */
    public void setMaxDotsizeIndex(int maxDotsizeIndex) {
        this.maxDotsizeIndex = maxDotsizeIndex;
    }
    
    
    /**
     * @return
     *  the metascale
     */
    public double getMetaScale() {
        return metaScale;
    }
    
    /**
     * @param metaScale
     */
    public void setMetaScale(double metaScale) {
        this.metaScale = metaScale;
    }

    /**
     * @return
     *  the xAxisScale
     */
    public double getXAxisScale() {
        return xAxisScale;
    }
    
    /**
     * @param xAxisScale
     */
    public void setXAxisScaleScale(double xAxisScale) {
        this.xAxisScale = xAxisScale;
    }


    /**
     * @return
     *  the yAxisScale
     */
    public double getYAxisScale() {
        return yAxisScale;
    }
    
    /**
     * @param yAxisScale
     */
    public void setYAxisScaleScale(double yAxisScale) {
        this.yAxisScale = yAxisScale;
    }


    /**
     * @return
     *  the zAxisScale
     */
    public double getZAxisScale() {
        return zAxisScale;
    }
    
    /**
     * @param zAxisScale
     */
    public void setZAxisScaleScale(double zAxisScale) {
        this.zAxisScale = zAxisScale;
    }

    
    /**
     * @return
     *  alpha
     */
    public double getMetaRotationAlpha() {
        return metaRotationAlpha;
    }
    
    /**
     * @param metaRotationAlpha
     */
    public void setMetaRotationAlpha( double metaRotationAlpha ) {
        this.metaRotationAlpha = metaRotationAlpha;
    }
    

    /**
     * @return
     *  beta
     */
    public double getMetaRotationBeta() {
        return metaRotationBeta;
    }
    
    /**
     * @param metaRotationBeta
     */
    public void setMetaRotationBeta( double metaRotationBeta ) {
        this.metaRotationBeta = metaRotationBeta;
    }


    /**
     * @return
     *  the minimum limit
     */
    public double getHpXmin() {
        return hpXmin;
    }

    /**
     * @param min
     */
    public void setHpXmin(double min) {
        hpXmin = min;
    }
    
    /**
     * @return
     *  the maximum limit
     */
    public double getHpXmax() {
        return hpXmax;
    }

    /**
     * @param max
     */
    public void setHpXmax(double max) {
        hpXmax = max;
    }

    /**
     * @return
     *  true if the axis should follow the hyperplane
     */
    public boolean applyHpXtoAxis() {
        return applyHpXtoAxis;
    }

    /**
     * @param a
     */
    public void setApplyHpXtoAxis(boolean a) {
        applyHpXtoAxis = a;
    }
    

    /**
     * @return
     *  the minimum limit
     */
    public double getHpYmin() {
        return hpYmin;
    }

    /**
     * @param min
     */
    public void setHpYmin(double min) {
        hpYmin = min;
    }
    
    /**
     * @return
     *  the maximum limit
     */
    public double getHpYmax() {
        return hpYmax;
    }

    /**
     * @param max
     */
    public void setHpYmax(double max) {
        hpYmax = max;
    }

    /**
     * @return
     *  true if the axis should follow the hyperplane
     */
    public boolean applyHpYtoAxis() {
        return applyHpYtoAxis;
    }

    /**
     * @param a
     */
    public void setApplyHpYtoAxis(boolean a) {
        applyHpYtoAxis = a;
    }
    
    
    /**
     * @return
     *  the minimum limit
     */
    public double getHpZmin() {
        return hpZmin;
    }

    /**
     * @param min
     */
    public void setHpZmin(double min) {
        hpZmin = min;
    }
    
    /**
     * @return
     *  the maximum limit
     */
    public double getHpZmax() {
        return hpZmax;
    }

    /**
     * @param max
     */
    public void setHpZmax(double max) {
        hpZmax = max;
    }

    /**
     * @return
     *  true if the axis should follow the hyperplane
     */
    public boolean applyHpZtoAxis() {
        return applyHpZtoAxis;
    }

    /**
     * @param a
     */
    public void setApplyHpZtoAxis(boolean a) {
        applyHpZtoAxis = a;
    }

    
    /**
     * @return
     *  the minimum limit
     */
    public double getHpColormin() {
        return hpColormin;
    }

    /**
     * @param min
     */
    public void setHpColormin(double min) {
        hpColormin = min;
    }
    
    /**
     * @return
     *  the maximum limit
     */
    public double getHpColormax() {
        return hpColormax;
    }

    /**
     * @param max
     */
    public void setHpColormax(double max) {
        hpColormax = max;
    }

    /**
     * @return
     *  true if the axis should follow the hyperplane
     */
    public boolean applyHpColortoAxis() {
        return applyHpColorToAxis;
    }

    /**
     * @param a
     */
    public void setApplyHpColortoAxis(boolean a) {
        applyHpColorToAxis = a;
    }

    
    /**
     * @return
     *  the minimum limit
     */
    public double getHpSizemin() {
        return hpSizemin;
    }

    /**
     * @param min
     */
    public void setHpSizemin(double min) {
        hpSizemin = min;
    }
    
    /**
     * @return
     *  the maximum limit
     */
    public double getHpSizemax() {
        return hpSizemax;
    }

    /**
     * @param max
     */
    public void setHpSizemax(double max) {
        hpSizemax = max;
    }

    /**
     * @return
     *  true if the axis should follow the hyperplane
     */
    public boolean applyHpSizetoAxis() {
        return applyHpSizeToAxis;
    }

    /**
     * @param a
     */
    public void setApplyHpSizetoAxis(boolean a) {
        applyHpSizeToAxis = a;
    }

    
    /**
     * @return
     *  true if applying this state is completed
     */
    public boolean isApplied() {
        return _applied;
    }
    
    /**
     * @param applied
     */
    public void setApplied(boolean applied) {
        this._applied = applied;
    }
    
}
