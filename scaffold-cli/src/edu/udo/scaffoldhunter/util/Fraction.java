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

import java.math.BigInteger;

/**
 * This class represents a fraction and provides some precise operations which
 * are lossy with floating point data types.
 * 
 * @author Till Schäfer
 * 
 */
public class Fraction extends Number implements Comparable<Fraction>, Cloneable {
    int denominator = 1;
    int numerator = 1;

    /**
     * Default Constructor
     */
    public Fraction() {
    }

    /**
     * Constructor
     * 
     * @param value
     *            an integer value
     */
    public Fraction(int value) {
        numerator = value;
    }

    /**
     * Constructor
     * 
     * @param numerator
     *            the numerator
     * @param denominator
     *            the denominator
     */
    public Fraction(int numerator, int denominator) {
        this.denominator = denominator;
        this.numerator = numerator;
    }

    /**
     * Set the value
     * 
     * @param val
     *            the other Fraction
     */
    public void set(Fraction val) {
        denominator = val.denominator;
        numerator = val.numerator;
    }

    /**
     * Set the value
     * 
     * @param numerator
     *            the numerator
     * @param denominator
     *            the denominator
     */
    public void set(int numerator, int denominator) {
        this.denominator = denominator;
        this.numerator = numerator;
    }

    /**
     * @return the numerator
     */
    public int getNumerator() {
        return numerator;
    }

    /**
     * @param numerator
     *            the numerator to set
     */
    public void setNumerator(int numerator) {
        this.numerator = numerator;
    }

    /**
     * @return the denominator
     */
    public int getDenominator() {
        return denominator;
    }

    /**
     * Set the denominator
     * 
     * @param denominator
     *            the denominator
     */
    public void setDenominator(int denominator) {
        this.denominator = denominator;
    }

    /**
     * Set the numerator
     * 
     * @param numerator
     *            the numerator
     */
    public void set(int numerator) {
        this.numerator = numerator;
    }

    /**
     * @return the irreducible fraction of this fraction
     */
    public Fraction getIrreducibleFraction() {
        checkDenominator();

        BigInteger a = BigInteger.valueOf(denominator);
        BigInteger b = BigInteger.valueOf(numerator);
        BigInteger gcd = a.gcd(b);

        return new Fraction(numerator / gcd.intValue(), denominator / gcd.intValue());
    }

    /**
     * Calculates the sum of this and val.<br>
     * 
     * Note that the Fraction will be made irreducible in the case that
     * this.denominator != val.denominator. This allows some additions where
     * integer operations would cause an overflow.
     * 
     * @param val
     *            the other {@link Fraction}
     * @return the summed up fraction
     */
    public Fraction add(Fraction val) {
        checkDenominator();

        if (denominator == val.denominator) {
            return new Fraction(numerator + val.numerator, denominator);
        } else {
            // this is a little bit slow but prevents integer overflows
            long lDenominator = (long) denominator * val.denominator;
            long lNumerator = (long) denominator * val.numerator + (long) val.denominator * numerator;
            BigInteger a = BigInteger.valueOf(lDenominator);
            BigInteger b = BigInteger.valueOf(lNumerator);
            BigInteger gcd = a.gcd(b);

            return new Fraction((int) (lNumerator / gcd.longValue()), (int) (lDenominator / gcd.longValue()));
        }
    }

    /**
     * add(val *-1)
     * 
     * @param val
     *            the other {@link Fraction}
     * @return the subtracted fraction
     */
    public Fraction substract(Fraction val) {
        return add(val.multiply(new Fraction(-1)));
    }

    /**
     * Calculates the multiplication of this and val
     * 
     * @param val
     *            the other {@link Fraction}
     * @return the multiplied fraction
     */
    public Fraction multiply(Fraction val) {
        checkDenominator();

        return new Fraction(numerator * val.numerator, denominator * val.denominator);
    }

    /**
     * Calculates the division of this and val
     * 
     * @param val
     *            the other {@link Fraction}
     * @return the divided fraction
     */
    public Fraction divideBy(Fraction val) {
        return this.multiply(val.reciprocal());
    }

    /**
     * @return the reciprocal of this fraction
     */
    public Fraction reciprocal() {
        return new Fraction(denominator, numerator);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Fraction) {

            Fraction other = (Fraction) o;
            if (this.denominator == 0 || other.denominator == 0) {
                return denominator == other.denominator && numerator == other.numerator;
            } else {
                Fraction irrThis = this.getIrreducibleFraction();
                Fraction irrO = other.getIrreducibleFraction();

                irrThis.stardardRepresentation();
                irrO.stardardRepresentation();

                return irrThis.denominator == irrO.denominator && irrThis.numerator == irrO.numerator;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        try {
            Fraction irrThis = (denominator != 0 ? this.getIrreducibleFraction() : (Fraction) this.clone());
            irrThis.stardardRepresentation();
            return 997 * (irrThis.denominator) ^ 991 * (irrThis.numerator);
        } catch (CloneNotSupportedException e) {
            // not possible
            throw new AssertionError();
        }
    }

    @Override
    public int intValue() {
        return numerator / denominator;
    }

    @Override
    public long longValue() {
        return numerator / denominator;
    }

    @Override
    public float floatValue() {
        return (float) numerator / denominator;
    }

    @Override
    public double doubleValue() {
        return (double) numerator / denominator;
    }

    @Override
    public int compareTo(Fraction anotherFraction) {
        return Double.compare(doubleValue(), anotherFraction.doubleValue());
    }

    /**
     * Use the numerator as the sign variable only
     */
    protected void stardardRepresentation() {
        if (denominator < 0) {
            denominator *= -1;
            numerator *= -1;
        }
    }

    /**
     * denominator != 0
     */
    private void checkDenominator() {
        if (denominator == 0) {
            throw new IllegalArgumentException("denominator must not be zero" + "");
        }
    }
}
