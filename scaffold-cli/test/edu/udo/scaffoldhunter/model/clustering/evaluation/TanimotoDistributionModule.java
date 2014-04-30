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

package edu.udo.scaffoldhunter.model.clustering.evaluation;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.clustering.Tanimoto;
import edu.udo.scaffoldhunter.util.Fraction;
import edu.udo.scaffoldhunter.util.SHMath;

/**
 * Actually, this module does not test anything. It calculated the distribution
 * of the {@link Tanimoto} distance for a fixed number of Bits.
 * 
 * @author Till Schäfer
 * 
 */
public class TanimotoDistributionModule extends EvaluationModule {

    private int numberOfBits;
    private int granularity;
    private BigInteger divideBy;

    /**
     * Constructor
     * 
     * @param numberOfBits
     *            the number of bits
     */
    public TanimotoDistributionModule(int numberOfBits) {
        this.numberOfBits = numberOfBits;
        granularity = 0;
        divideBy = BigInteger.ONE;
    }

    /**
     * Constructor for a result which uses a histogram-like accumulation
     * 
     * @param numberOfBits
     *            the number of bits
     * @param granularity
     *            The number of distinct intervals. All values are summed up for
     *            each interval.
     */
    public TanimotoDistributionModule(int numberOfBits, int granularity) {
        Preconditions.checkArgument(granularity >= 1, "granularity must be greater than one");

        this.granularity = granularity;
        this.numberOfBits = numberOfBits;
        divideBy = BigInteger.ONE;
    }

    /**
     * Constructor for a result which uses a histogram-like accumulation
     * 
     * @param numberOfBits
     *            the number of bits
     * @param granularity
     *            The number of distinct intervals. All values are summed up for
     *            each interval.
     * @param divideBy
     *            divide the result values by this factor. This is useful as the
     *            values can be so large, that other programs are not able to
     *            interpret them
     */
    public TanimotoDistributionModule(int numberOfBits, int granularity, BigInteger divideBy) {
        Preconditions.checkArgument(granularity >= 1, "granularity must be greater than one");

        this.granularity = granularity;
        this.numberOfBits = numberOfBits;
        this.divideBy = divideBy;
    }

    @Override
    public Collection<EvaluationResult> run() throws EvaluationException {
        EvaluationResult result = new EvaluationResult(null, null, "Tanimoto Distribution for " + numberOfBits
                + " bits" + (divideBy.equals(BigInteger.ONE) ? "" : ("values div by " + divideBy.toString())), null, 0);
        HashMap<Fraction, BigInteger> distribution = Maps.newHashMap();

        for (Fraction fraction : possibleFractions()) {
            /*
             * Sum all Fractions with the same value (i.e. 1/3 and 2/6) to a
             * single value. This is implicitly done by Fraction#equals and
             * Fraction#hashCode
             */
            if (distribution.containsKey(fraction)) {
                BigInteger oldVal = distribution.get(fraction);
                distribution.put(fraction, oldVal.add(numberOfCombinations(fraction)));
            } else {
                distribution.put(fraction, numberOfCombinations(fraction));
            }
        }

        // Tanimoto similarity counts fraction 0/0 as 0
        distribution.remove(new Fraction(0, 0));
        distribution.put(new Fraction(1), distribution.get(new Fraction(1)).add(BigInteger.ONE));

        /*
         * copy fractions and counts to a list and sort them according their
         * fraction value sorting
         */
        List<Entry<Fraction, BigInteger>> sortedDistribution = Lists.newLinkedList(distribution.entrySet());
        Collections.sort(sortedDistribution, new Comparator<Entry<Fraction, BigInteger>>() {
            @Override
            public int compare(Entry<Fraction, BigInteger> o1, Entry<Fraction, BigInteger> o2) {
                return o1.getKey().compareTo(o2.getKey());
            }
        });

        // assert: count overall pairs and compare with expected number
        boolean assertionsEnabled = false;
        assert (assertionsEnabled = true) == true;
        if (assertionsEnabled) {
            BigInteger count = BigInteger.ZERO;
            for (Entry<Fraction, BigInteger> entry : sortedDistribution) {
                count = count.add(entry.getValue());
            }
            assert count.equals(BigInteger.valueOf(2).pow(2 * numberOfBits));
        }

        if (granularity != 0) {
            double maxStoredValue = sortedDistribution.get(sortedDistribution.size() - 1).getKey().doubleValue();
            double intervalSize = maxStoredValue / granularity;
            BigInteger[] counts = new BigInteger[granularity];
            for (int i = 0; i < counts.length; i++) {
                counts[i] = BigInteger.ZERO;
            }

            for (Entry<Fraction, BigInteger> entry : sortedDistribution) {
                int index = (int) Math.floor(entry.getKey().doubleValue() / intervalSize);
                if (index == granularity) {
                    index--;
                }
                counts[index] = counts[index].add(entry.getValue());
            }

            for (int i = 0; i < counts.length; i++) {
                String interval = String.format("%5.2f", i * intervalSize) + "-"
                        + String.format("%5.2f", (i + 1) * intervalSize);
                String summedCounts = counts[i].divide(divideBy).toString();
                result.addResult(interval, summedCounts);
            }
        } else {
            /*
             * convert similarity to distance and format as string for the
             * results
             */
            for (Entry<Fraction, BigInteger> entry : sortedDistribution) {
                result.addResult(String.valueOf(1 - entry.getKey().doubleValue()), entry.getValue().divide(divideBy)
                        .toString());
            }
        }

        return Collections.singleton(result);
    }

    /**
     * @return all Fractions a/b with 0 <= a <= b <= numberOfBits
     */
    private List<Fraction> possibleFractions() {
        List<Fraction> retVal = Lists.newLinkedList();
        for (int i = 0; i <= numberOfBits; i++) {
            for (int j = 0; j <= i; j++) {
                retVal.add(new Fraction(j, i));
            }
        }

        return retVal;
    }

    private BigInteger numberOfCombinations(Fraction fraction) {
        BigInteger intersectionPositions = SHMath.bigBinomial(numberOfBits, fraction.getNumerator());
        int diff = fraction.getDenominator() - fraction.getNumerator();
        BigInteger diffPositions = SHMath.bigBinomial(numberOfBits - fraction.getNumerator(), diff);

        return intersectionPositions.multiply(BigInteger.valueOf(2).pow(diff).multiply(diffPositions));
    }
}
