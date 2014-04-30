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

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Stores all features found in a hash map and counts the number of occurrences.
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 * @param <F>
 *            the type of the feature
 */
public class CountHashTable<F> implements FeatureStorage<F, HashMap<F, Integer>> {
    private HashMap<F, Integer> features;

    /**
     * Constructor
     */
    public CountHashTable() {
        features = new HashMap<F, Integer>();
    }

    /**
     * Note: The given object must have a reasonable implementation of
     * {@link Object#hashCode()} and {@link Object#equals(Object)}.
     * 
     * @see FeatureStorage#processFeature(Object)
     */
    @Override
    public void processFeature(F feature) {
        Integer value = features.get(feature);
        if (value == null)
            features.put(feature, 1);
        else
            features.put(feature, value + 1);
    }

    @Override
    public int getFeatureCount() {
        return features.size();
    }

    @Override
    public HashMap<F, Integer> getResult() {
        return features;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry<F, Integer> e : features.entrySet()) {
            sb.append(e.getKey());
            sb.append(" ");
            sb.append(e.getValue());
            sb.append("\n");
        }

        return sb.toString();
    }
}
