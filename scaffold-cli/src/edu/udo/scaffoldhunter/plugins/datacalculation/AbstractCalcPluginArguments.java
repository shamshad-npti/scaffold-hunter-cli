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

package edu.udo.scaffoldhunter.plugins.datacalculation;

/**
 * @author Philipp Lewe
 *
 */
public abstract class AbstractCalcPluginArguments {

    private boolean useLargestFragments = false;
    private boolean deglycosilate = false;
    private boolean recalculate2Dcoords = false;
    
    /**
     * @param useLargestFragments the useLargestFragments to set
     */
    public void setUseLargestFragment(boolean useLargestFragments) {
        this.useLargestFragments = useLargestFragments;
    }

    /**
     * @return the useLargestFragments
     */
    public boolean isUseLargestFragments() {
        return useLargestFragments;
    }

    /**
     * @param deglycosilate the deglycosilate to set
     */
    public void setDeglycosilate(boolean deglycosilate) {
        this.deglycosilate = deglycosilate;
    }
    
    /**
     * @return the deglycosilate
     */
    public boolean isDeglycosilate() {
        return deglycosilate;
    }

    /**
     * @param recalculate2Dcoords the recalculate2Dcoords to set
     */
    public void setRecalculate2Dcoords(boolean recalculate2Dcoords) {
        this.recalculate2Dcoords = recalculate2Dcoords;
    }

    /**
     * @return the recalculate2Dcoords
     */
    public boolean isRecalculate2Dcoords() {
        return recalculate2Dcoords;
    }
    
}
