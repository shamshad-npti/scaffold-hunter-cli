/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.model.filtering.subsearch.fingerprint;

import java.util.HashSet;

/**
 * Maps each feature to a number in {0, ..., Integer.MAX_VALUE} and stores 
 * the numbers in a set.
 * 
 * Note: The given features must have a reasonable implementation of
 * {@link Object#hashCode()}.
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class IntHashSet implements FeatureStorage<Object, HashSet<Integer>> {

    private HashSet<Integer> features = new HashSet<Integer>();

    /**
     * Note: The given object must have a reasonable implementation of
     * {@link Object#hashCode()}.
     * 
     * @see FeatureStorage#processFeature(Object)
     */
    @Override
    public void processFeature(Object o) {
        int value = Hashing.hash(o.hashCode(), Integer.MAX_VALUE);
        features.add(value);
    }

    @Override
    public int getFeatureCount() {
        return features.size();
    }

    @Override
    public HashSet<Integer> getResult() {
        return features;
    }

}
