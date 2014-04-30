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
 * Interface defining a parent scaffold filter rule, i.e., a class
 * filtering out some possible parent scaffolds.
 * 
 * @author Nils Kriege
 */
public interface ScaffoldFilterRule {
    
    /**
     * Returns the unique name of this rule.
     * @return the name of this rule
     */
    public String getName();
    
    /**
     * Returns the scaffolds fulfilling this rule.
     * @param scaffolds list of input scaffolds
     * @return list of scaffolds fulfilling the rule
     */
    public Vector<ScaffoldContainer> filter(Vector<ScaffoldContainer> scaffolds);

}
