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

package edu.udo.scaffoldhunter.model.treegen.prioritization;

import java.util.Vector;

import edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer;

/**
 * Determines the scaffolds with minimum descriptor value among all
 * possible parent scaffolds. To determine scaffolds with maximum
 * descriptor values the negative property values can be used.
 * 
 * @see #getProperty(ScaffoldContainer)
 * 
 * @author Nils Kriege
 */
public abstract class AbstractMinScaffoldFilterRule extends AbstractScaffoldFilterRule {

    int min;

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.treegen.prioritization.AbstractScaffoldFilterRule#fulfills(edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer)
     */
    @Override
    public Vector<ScaffoldContainer> filter(Vector<ScaffoldContainer> scaffolds) {
        min = Integer.MAX_VALUE;
        for (ScaffoldContainer sc : scaffolds) {
            min = Math.min(min, getProperty(sc));
        }
        return super.filter(scaffolds);
    }
    
    /**
     * @param sc the scaffold
     * @return true iff the property value of sc is minimum among all possible
     * parent scaffolds
     */
    @Override
    public boolean fulfills(ScaffoldContainer sc) {
        return getProperty(sc) == min;
    }
    
    /**
     * Returns the property value this rule is based on.
     * @param sc the scaffold
     * @return the property value
     */
    public abstract int getProperty(ScaffoldContainer sc);

}
