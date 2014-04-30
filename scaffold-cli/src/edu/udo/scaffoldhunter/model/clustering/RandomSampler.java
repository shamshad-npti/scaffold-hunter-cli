/*
 * Scaffold Hunter
 * Copyright (C) 2012 Till Schäfer
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Random sampling strategy
 * 
 * @author Till Schäfer
 * 
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class RandomSampler<S extends Structure> implements Sampler<S> {
    private static Logger logger = LoggerFactory.getLogger(RandomSampler.class);

    private final Random rand;
    private final int count;

    /**
     * Constructor
     * 
     * @param count
     *            count of samples that should be returned by getSample()
     * @param seed
     *            if the seed is not null, the Sample is deterministic
     */
    public RandomSampler(int count, Long seed) {
        this.count = count;

        if (seed != null) {
            rand = new Random(seed);
        } else {
            rand = new Random();
        }
    }

    /**
     * Derives a random sample from structures by using the Floyd’s algorithm.
     * If size of structures is smaller than the sample size, than the size of
     * structures is used as the sample size.
     * 
     * @param structures
     *            the {@link Structure}s
     * @return the sample
     */
    @Override
    public HashSet<HierarchicalClusterNode<S>> getSample(ArrayList<HierarchicalClusterNode<S>> structures) {
        if (structures.size() < count) {
            logger.debug(
                    "Basic set size is smaller than the sample size. Resulting samle has only size {} instead of sample size {}.",
                    structures.size(), count);
        }
        int count = Math.min(this.count, structures.size());

        HashSet<HierarchicalClusterNode<S>> retVal = new HashSet<HierarchicalClusterNode<S>>(count);
        int n = structures.size();
        for (int i = n - count; i < n; i++) {
            int pos = rand.nextInt(i + 1);
            HierarchicalClusterNode<S> item = structures.get(pos);
            if (retVal.contains(item))
                retVal.add(structures.get(i));
            else
                retVal.add(item);
        }
        return retVal;
    }

}
