/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.additionalsmiles;

import edu.udo.scaffoldhunter.plugins.datacalculation.AbstractCalcPluginArguments;

/**
 * @author Philipp Lewe
 *
 */
public class AdditionalSmilesCalcPluginArguments extends AbstractCalcPluginArguments {
    
    private boolean calcLargestFragmentSmiles = true;
    private boolean calcLargestFragmentDeglycosilatedSmiles = true;
    private boolean calcOriginalStructureDeglycosilatedSmiles = true;

    /**
     * 
     */
    public AdditionalSmilesCalcPluginArguments() {
        super();
    }

    /**
     * @return the calcLargestFragmentSmiles
     */
    public boolean isCalcLargestFragmentSmiles() {
        return calcLargestFragmentSmiles;
    }

    /**
     * @param calcLargestFragmentSmiles the calcLargestFragmentSmiles to set
     */
    public void setCalcLargestFragmentSmiles(boolean calcLargestFragmentSmiles) {
        this.calcLargestFragmentSmiles = calcLargestFragmentSmiles;
    }

    /**
     * @return the calcLargestFragmentDeglycosilatedSmiles
     */
    public boolean isCalcLargestFragmentDeglycosilatedSmiles() {
        return calcLargestFragmentDeglycosilatedSmiles;
    }

    /**
     * @param calcLargestFragmentDeglycosilatedSmiles the calcLargestFragmentDeglycosilatedSmiles to set
     */
    public void setCalcLargestFragmentDeglycosilatedSmiles(boolean calcLargestFragmentDeglycosilatedSmiles) {
        this.calcLargestFragmentDeglycosilatedSmiles = calcLargestFragmentDeglycosilatedSmiles;
    }

    /**
     * @return the calcOriginalStructureDeglycosilatedSmiles
     */
    public boolean isCalcOriginalStructureDeglycosilatedSmiles() {
        return calcOriginalStructureDeglycosilatedSmiles;
    }

    /**
     * @param calcOriginalStructureDeglycosilatedSmiles the calcOriginalStructureDeglycosilatedSmiles to set
     */
    public void setCalcOriginalStructureDeglycosilatedSmiles(boolean calcOriginalStructureDeglycosilatedSmiles) {
        this.calcOriginalStructureDeglycosilatedSmiles = calcOriginalStructureDeglycosilatedSmiles;
    }

}
