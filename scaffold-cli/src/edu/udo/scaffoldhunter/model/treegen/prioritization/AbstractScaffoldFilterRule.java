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
 * Filters out scaffolds based on their descriptor values.
 * 
 * @author Nils Kriege
 */
public abstract class AbstractScaffoldFilterRule implements ScaffoldFilterRule {

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.treegen.prioritization.ScaffoldFilterRule#filter(java.util.Vector)
     */
    @Override
    public Vector<ScaffoldContainer> filter(Vector<ScaffoldContainer> scaffolds) {
        Vector<ScaffoldContainer> filteredScaffolds = new Vector<ScaffoldContainer>();
        for (ScaffoldContainer sc : scaffolds) {
            if (fulfills(sc)) {
                filteredScaffolds.add(sc);
            }
        }
        return filteredScaffolds;
    }
    
    /**
     * Checks if a given scaffold fulfills this rule.
     * @param sc the scaffold container
     * @return true iff sc fulfills this rule
     */
    public abstract boolean fulfills(ScaffoldContainer sc);

}
