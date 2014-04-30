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
 * A collection of Math functions
 * 
 * @author Till Schäfer
 */
public class SHMath {
    /**
     * Binomial coefficient (n choose k). Uses the following recursive formula
     * to avoid integer overflows and precision errors:<br>
     * <br>
     * 
     * (n choose k+1) = [(n - k)(n choose k)] \ (k + 1)<br>
     * <br>
     * 
     * where "\" is an integer division.
     * 
     * @param n
     * @param k
     * @return the binomial coefficient
     */
    public static int binomial(int n, int k) {
        if (k == 0) {
            return 1;
        }

        int bin = n;
        for (int k_iter = 1; k_iter < k; k_iter++) {
            bin = ((n - k_iter) * bin) / (k_iter + 1);
        }

        return bin;
    }

    /**
     * Binomial coefficient (n choose k) <b>BigInteger version</b>. Uses the
     * following recursive formula to avoid integer overflows and precision
     * errors:<br>
     * <br>
     * 
     * (n choose k+1) = [(n - k)(n choose k)] \ (k + 1)<br>
     * <br>
     * 
     * where "\" is an integer division.
     * 
     * @param n
     * @param k
     * @return the binomial coefficient
     */
    public static BigInteger bigBinomial(int n, int k) {
        if (k == 0) {
            return BigInteger.ONE;
        }

        
        BigInteger bn = BigInteger.valueOf(n);
        BigInteger bin = BigInteger.valueOf(n);
        for (int k_iter = 1; k_iter < k; k_iter++) {
            BigInteger k_biter = BigInteger.valueOf(k_iter);
            bin = (bn.subtract(k_biter)).multiply(bin).divide((k_biter.add(BigInteger.ONE)));
        }

        return bin;
    }
}
