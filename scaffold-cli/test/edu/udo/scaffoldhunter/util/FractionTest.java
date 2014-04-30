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

import org.junit.Assert;

/**
 * JUnit Testing for the {@link Fraction} class
 * 
 * @author Till Schäfer
 * 
 */
public class FractionTest {

    /**
     * Test the toDouble() method
     */
    @org.junit.Test
    public void testToDouble() {
        Fraction f1 = new Fraction();
        assertEquals(1.0, f1.doubleValue(), 0);

        Fraction f2 = new Fraction(1, 2);
        assertEquals(0.5, f2.doubleValue(), 0);

    }

    /**
     * Test the add() method
     */
    @org.junit.Test
    public void testAdd() {
        Fraction f1 = new Fraction();
        Fraction f2 = new Fraction(2, 3);

        assertEquals(new Fraction(5, 3), f1.add(f2));

        f1.set(2, 3);
        assertEquals(new Fraction(4, 3), f1.add(f2));
    }

    /**
     * Test the subtract() method
     */
    @org.junit.Test
    public void testSubstraction() {
        Fraction f1 = new Fraction();
        Fraction f2 = new Fraction(2, 3);

        assertEquals(new Fraction(1, 3), f1.substract(f2));
    }

    /**
     * Test the multiply() method
     */
    @org.junit.Test
    public void testMultiplication() {
        Fraction f1 = new Fraction(3, 5);
        Fraction f2 = new Fraction(2, 4);

        assertEquals(new Fraction(3, 10), f1.multiply(f2));
    }

    /**
     * Test the divideBy() method
     */
    @org.junit.Test
    public void testDivision() {
        Fraction f1 = new Fraction(3, 5);
        Fraction f2 = new Fraction(2, 4);

        assertEquals(new Fraction(6, 5), f1.divideBy(f2));
    }

    /**
     * Test the equals() method
     */
    @org.junit.Test
    public void testEquals() {
        Fraction f1 = new Fraction();
        Fraction f2 = new Fraction(1, -1);
        Fraction f3 = new Fraction(-2, 2);
        Fraction f4 = new Fraction(-1, 1);

        Assert.assertNotSame(f1,f2);
        assertEquals(f2, f3);
        assertEquals(f2, f4);
        assertEquals(f3, f4);
    }

}