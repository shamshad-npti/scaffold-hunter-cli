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

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Calculates the features for the fingerprint
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 * @param <I>
 *            the type of feature description
 */
public abstract class FeatureExtractor<I> {

    protected FeatureStorage<? super I, ?> featureStorage;
    protected Graph graph;

    /**
     * Constructor
     */
    public FeatureExtractor() {
    }

    /**
     * Constructor
     * 
     * @param graph
     *            the {@link Graph} the extract the features from
     * @param featureStorage
     */
    public FeatureExtractor(Graph graph, FeatureStorage<? super I, ?> featureStorage) {
        this.graph = graph;
        this.featureStorage = featureStorage;
    }

    /**
     * Get the {@link FeatureStorage}
     * 
     * @return the {@link FeatureStorage}
     */
    public FeatureStorage<? super I, ?> getFeatureStorage() {
        return featureStorage;
    }

    /**
     * The actual calculation of the features
     */
    public abstract void extractFeatures();

    /**
     * Returns the label of the {@link Node} as {@link String}
     * 
     * @param n
     *            the {@link Node}
     * @return the label as {@link String}
     */
    public static String getLabel(Node n) {
        return n.getLabel().toString();
    }

    /**
     * Returns the label of the {@link Edge} as {@link String}
     * 
     * @param e
     *            the {@link Edge}
     * @return the label as {@link String}
     */
    public static String getLabel(Edge e) {
        return e.getLabel().toString();
    }

}
