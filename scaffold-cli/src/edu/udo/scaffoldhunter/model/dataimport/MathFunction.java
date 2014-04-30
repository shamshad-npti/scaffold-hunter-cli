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

import de.congrace.exp4j.PostfixExpression;
import de.congrace.exp4j.UnknownFunctionException;
import de.congrace.exp4j.UnparseableExpressionException;

/**
 * Encodes a single argument mathematical function
 * 
 * @author Henning Garus
 *
 */
public class MathFunction {
    
    private final String expressionString;
    private final PostfixExpression expression;
    
    /**
     * Create a new mathematical function object from a mathematical expression 
     * 
     * @param mathExpression A mathematical expression containing:
     *          numbers, +, -, *,  /, ^, parentheses,
     *          functions: log, log10, exp, ceil, floor
     *          and the variable x which will be replaced with the argument
     *          on calls to <code>calculate</code>
     * @throws IllegalArgumentException if the expression cannot be parsed successfully 
     */
    public MathFunction(String mathExpression) throws IllegalArgumentException {
        try {
            this.expression = PostfixExpression.fromInfix("f(x)=" +mathExpression);
            // check if this expression was really valid
            expression.calculate(1);
        } catch (UnparseableExpressionException e) {
            throw new IllegalArgumentException("unparsable");
        } catch (UnknownFunctionException e) {
            throw new IllegalArgumentException("unknown function");
        // evil but necessary, since the parser is not as robust as I'd like
        // TODO maybe make the parser more robust
        } catch (RuntimeException e) {
            throw new IllegalArgumentException();
        }
        this.expressionString = mathExpression;
    }
    
    /**
     * Apply the mathematical function represented by this object.
     * 
     * @param value the value to which the function is applied
     * @return result of applying this mathematical function to value 
     */
    public double calculate(double value) {
        // we already checked this during construction
        assert(expression.getVariableNames().length == 1);
        return expression.calculate(value);
    }
    
    /**
     * Check if an expression can be parsed. If <code>true</code> is returned the constructor
     * will not throw an exception for the same argument.
     * 
     * @param mathExpression the mathematical expression to be checked
     * @return <code>true</code> if the expression can be parsed successfully,
     *  <code>false</code> otherwise
     */
    @SuppressWarnings("unused")
    public static boolean validate(String mathExpression) {
        // better than replicating the whole constructor
        try {
            // don't assign it to anything, just check for Exception
            new MathFunction(mathExpression);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return expressionString;
    }
    
    
}
