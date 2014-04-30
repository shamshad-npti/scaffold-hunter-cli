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

package edu.udo.scaffoldhunter.model.dataimport;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Henning Garus
 * 
 */
public class MathFunctionTest {

    private MathFunction func;

    private String[] validExps = { "x", "x + 10", "x^10", "x^(5*2)", "x + x", "((x/10))", "sqrt(x)", "exp(x)",
            "log(exp(x))", "exp(x+5)/10", "+x--5" };
    private String[] invalidExps = { "", " ", "x^^x", "a", "((x+5)", ")x", "x++", "x +y" };

    /**
     * Test if some valid Expressions are validated correctly
     */
    @org.junit.Test
    public void validatePositive() {
        for (String s : validExps) {
            assertTrue(MathFunction.validate(s));
        }
    }

    /**
     * Test if some invalid Expressions are not validated
     */
    @org.junit.Test
    public void validateNegative() {
        for (String s : invalidExps) {
            assertFalse(MathFunction.validate(s));
        }
    }

    /**
     * Try some Math functions
     */
    @org.junit.Test
    public void constructPositive() {
        for (String s : validExps)
            func = new MathFunction(s);
    }

    /**
     * Try some invalid Math functions (they should fail)
     */
    @org.junit.Test
    public void constructNegative() {
        try {
            for (String s : invalidExps)
                func = new MathFunction(s);
        } catch (IllegalArgumentException e) {
            // everything went as expected
            return;
        }
        Assert.fail();
    }

    /**
     * Test some results
     */
    @Test
    public void calculate() {
        double arg = 10;
        double results[] = { arg, arg + 10, Math.pow(arg, 10), Math.pow(arg, 10), arg * 2, arg / 10, Math.sqrt(arg),
                Math.exp(arg), arg, Math.exp(arg + 5) / 10, +arg + 5 };
        assert( results.length == validExps.length);
        for( int i = 0; i < validExps.length; i++) {
            func = new MathFunction(validExps[i]);
            Assert.assertEquals(func.calculate(arg), results[i]);
        }
    }
}
