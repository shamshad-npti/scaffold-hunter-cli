/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.LinkedList;

import org.junit.Assert;

import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * JUnit test classes for the {@link LanceWilliamsUpdateFormula}s
 * 
 * @author Till Schäfer
 */
public class LanceWilliamsFormulaTest {
    /**
     * Tests the centroid linkage LW-formula
     */
    @org.junit.Test
    public void centroidTest () {
        Linkage<Structure> linkage = Linkages.CENTROID_LINKAGE.genereateLinkage(new LinkedList<PropertyDefinition> ());
        LanceWilliamsUpdateFormula updateFormula = linkage.getUpdateFormula();
        
        double val = updateFormula.newDistance(Math.sqrt(2), Math.sqrt(2), 2, 1, 1, 1);
        Assert.assertEquals(1, val, 0.0001);
        
        val = updateFormula.newDistance(Math.sqrt(5), Math.sqrt(2), 3, 1, 1, 2);
        Assert.assertEquals(1, val, 0.0001);

        val = updateFormula.newDistance(Math.sqrt(8), Math.sqrt(5), 3, 1, 1, 2);
        Assert.assertEquals(2, val, 0.0001);
        
        val = updateFormula.newDistance(Math.sqrt(8), Math.sqrt(5), 3, 10, 1, 2);
        Assert.assertEquals(2, val, 0.0001);
    }
}
