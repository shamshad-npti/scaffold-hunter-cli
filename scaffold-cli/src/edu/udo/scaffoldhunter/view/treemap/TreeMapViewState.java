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

package edu.udo.scaffoldhunter.view.treemap;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * Remember the state of the TreeMapView
 * Holds all user-parameters
 * 
 * @author Lappie
 *
 */
public class TreeMapViewState extends ViewState{
    
    // applied options
    private String sizeProperty = I18n.get("TreeMapView.Mappings.NrMolecules");    
    private String colorProperty = I18n.get("TreeMapView.Mappings.None");    
    private AccumulationFunction function = AccumulationFunction.Average;    
    private boolean cumulative = true;
    private boolean subsetInterval = true;
    private boolean scaffoldRadioButton = true;
    
    // selected but not yet applied options
    private String sizePropertySelected = I18n.get("TreeMapView.Mappings.NrMolecules");    
    private String colorPropertySelected = I18n.get("TreeMapView.Mappings.None");    
    private AccumulationFunction functionSelected = AccumulationFunction.Average;
    private boolean cumulativeSelected = true;
    private boolean subsetIntervalSelected = true;
    private boolean scaffoldRadioButtonSelected = true;
        
    /**
     * @return the sizeProperty
     */
    public String getSizeProperty() {
        return sizeProperty;
    }
    
    /**
     * @param sizeProperty the sizeProperty to set
     */
    public void setSizeProperty(String sizeProperty) {
        this.sizeProperty = sizeProperty;
    }
    
    /**
     * @return the colorProperty
     */
    public String getColorProperty() {
        return colorProperty;
    }
    
    /**
     * @param colorProperty the colorProperty to set
     */
    public void setColorProperty(String colorProperty) {
        this.colorProperty = colorProperty;
    }
    
    /**
     * @return the current accumulation function
     */
    public AccumulationFunction getFunction() {
        return function;
    }
    
    /**
     * @param function the new accumulation function
     */
    public void setFunction(AccumulationFunction function) {
        this.function = function;
    }
    
    /**
     * @return whether the properties are calculated cumulatively
     */
    public boolean isCumulative() {
        return cumulative;
    }
    
    /**
     * @param cumulative whether the properties are calculated cumulatively
     */
    public void setCumulative(boolean cumulative) {
        this.cumulative = cumulative;
    }
    
    /**
     * @return whether the color interval only refers to the maximum and minimum of the current subset (or the whole data set)
     */
    public boolean isSubsetInterval() {
        return subsetInterval;
    }
    
    /**
     * @param subsetInterval whether the color interval only refers to the maximum and minimum of the current subset (or the whole data set)
     */
    public void setSubsetInterval(boolean subsetInterval) {
        this.subsetInterval = subsetInterval;
    }
    
    /**
     * @return the scaffoldRadioButton
     */
    public boolean isScaffoldRadioButton() {
        return scaffoldRadioButton;
    }

    /**
     * @param scaffoldRadioButton the scaffoldRadioButton to set
     */
    public void setScaffoldRadioButton(boolean scaffoldRadioButton) {
        this.scaffoldRadioButton = scaffoldRadioButton;
    }

    /**
     * @return the sizePropertySelected
     */
    public String getSizePropertySelected() {
        return sizePropertySelected;
    }

    /**
     * @param sizePropertySelected the sizePropertySelected to set
     */
    public void setSizePropertySelected(String sizePropertySelected) {
        this.sizePropertySelected = sizePropertySelected;
    }

    /**
     * @return the colorPropertySelected
     */
    public String getColorPropertySelected() {
        return colorPropertySelected;
    }

    /**
     * @param colorPropertySelected the colorPropertySelected to set
     */
    public void setColorPropertySelected(String colorPropertySelected) {
        this.colorPropertySelected = colorPropertySelected;
    }
    
    /**
     * @return the currently selected accumulation function
     */
    public AccumulationFunction getFunctionSelected() {
        return functionSelected;
    }
    
    /**
     * @param function the new selected accumulation function
     */
    public void setFunctionSelected(AccumulationFunction function) {
        this.functionSelected = function;
    }
    
    /**
     * @return whether the properties are calculated cumulatively according to current selection
     */
    public boolean isCumulativeSelected() {
        return cumulativeSelected;
    }
    
    /**
     * @param cumulative whether the properties are calculated cumulatively  according to current selection
     */
    public void setCumulativeSelected(boolean cumulative) {
        this.cumulativeSelected = cumulative;
    }
    
    /**
     * @return whether the color interval only refers to the maximum and minimum of the current subset (or the whole data set)
     */
    public boolean isSubsetIntervalSelected() {
        return subsetIntervalSelected;
    }
    
    /**
     * @param subsetInterval whether the color interval only refers to the maximum and minimum of the current subset (or the whole data set)
     */
    public void setSubsetIntervalSelected(boolean subsetInterval) {
        this.subsetIntervalSelected = subsetInterval;
    }

    /**
     * @return the scaffoldRadioButtonSelected
     */
    public boolean isScaffoldRadioButtonSelected() {
        return scaffoldRadioButtonSelected;
    }

    /**
     * @param scaffoldRadioButtonSelected the scaffoldRadioButtonSelected to set
     */
    public void setScaffoldRadioButtonSelected(boolean scaffoldRadioButtonSelected) {
        this.scaffoldRadioButtonSelected = scaffoldRadioButtonSelected;
    }
}
