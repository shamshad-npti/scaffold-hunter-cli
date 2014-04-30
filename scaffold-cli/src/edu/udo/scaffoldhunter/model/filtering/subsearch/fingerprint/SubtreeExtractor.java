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

import java.util.List;

import edu.udo.scaffoldhunter.model.filtering.subsearch.fingerprint.Subtree.IndexEdge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Finds all subtrees of a graph.
 */
public class SubtreeExtractor extends FeatureExtractor<String> {
    private int maxSize;
    @SuppressWarnings("unused")
    private int features = 0;

    /**
     * @param graph
     * @param featureStorage
     * @param maxSize
     *            the number of edges a subtree may contain
     */
    public SubtreeExtractor(Graph graph, FeatureStorage<? super String, ?> featureStorage, int maxSize) {
        super(graph, featureStorage);
        this.maxSize = maxSize;
    }

    @Override
    public void extractFeatures() {
        Subtree t = new Subtree(graph, maxSize);
        for (Node v : t.nodes()) {
            t.addActiveNode(v);
            featureStorage.processFeature(t.getCanonicalLabeling());
            features++;

            for (IndexEdge e : t.getExtensions()) {
                if (v.getIndex() > e.getOppositeNode(v).getIndex()) {
                    extendSubtreeByEdge(t, e);
                }
            }
            t.removeLastActiveNode();
        }
    }

    private void extendSubtreeByEdge(Subtree t, IndexEdge e) {
        t.addActiveEdge(e);
        featureStorage.processFeature(t.getCanonicalLabeling());
        features++;

        List<IndexEdge> selectableEdges = t.getExtensions();
        for (IndexEdge f : selectableEdges) {
            extendSubtreeByEdge(t, f);
        }

        t.allowEdges(selectableEdges);

        t.removeLastActiveEdge();
        t.forbidEdge(e);
    }

}
