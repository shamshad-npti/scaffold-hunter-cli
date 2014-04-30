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

import static org.junit.Assert.assertEquals;

/**
 * JUnit testing for SHMath functions
 * 
 * @author Till Schäfer
 */
public class SHMathTest {
    /**
     * Test the binomial calculations
     */
    @org.junit.Test
    public void testBinomial() {
        assertEquals(1, SHMath.binomial(0, 0));
        assertEquals(1, SHMath.binomial(1, 0));
        assertEquals(1, SHMath.binomial(20, 0));
        assertEquals(0, SHMath.binomial(0, 1));
        assertEquals(0, SHMath.binomial(0, 20));
        assertEquals(10, SHMath.binomial(10, 1));
        assertEquals(45, SHMath.binomial(10, 2));
        assertEquals(207288004, SHMath.binomial(122, 5));
    }
}
