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
import java.util.Stack;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.DefaultGraph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.MoleculeGraph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Finds the rings in a {@link Graph}
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class RingExtractor extends FeatureExtractor<String> {
    private int maxSize;

    /**
     * Constructor Note: Each ring will be extracted and stored several times!
     * 
     * @param graph
     *            the {@link Graph} to extract the feaures from
     * @param featureStorage
     *            the {@link FeatureStorage}
     * @param maxSize
     *            the max ring size
     */
    public RingExtractor(Graph graph, FeatureStorage<? super String, ?> featureStorage, int maxSize) {
        super(graph, featureStorage);

        Preconditions.checkArgument(graph instanceof MoleculeGraph);

        this.maxSize = maxSize;
    }

    @Override
    public void extractFeatures() {
        boolean[] visited = new boolean[graph.getNodeCount()];
        Stack<Node> path = new Stack<Node>();
        for (Node n : graph.nodes()) {
            search(n, visited, path, n, 1);
        }
    }

    private void search(Node startNode, boolean[] visited, Stack<Node> path, Node u, int depth) {
        path.push(u);
        visited[u.getIndex()] = true;

        for (Edge e : u.getEdges()) {
            Node v = e.getOppositeNode(u);
            /*
             * Avoids enumerating duplicates by adding only nodes with index
             * higher than the start vertex. Each circle will be generated once
             * starting from the node with the lowest index. Furthermore each
             * circle can be build in two direction. Only one of them will be
             * processed.
             */
            if (!visited[v.getIndex()] && depth < maxSize && v.getIndex() > startNode.getIndex()) {
                search(startNode, visited, path, v, depth + 1);
            } else if (v == startNode && path.size() > 2 && u.getIndex() > path.get(1).getIndex()) {
                processCircle(path);
            }
        }

        path.pop();
        visited[u.getIndex()] = false;
    }

    private void processCircle(Stack<Node> circle) {
        DefaultGraph g = new DefaultGraph(circle.size());
        for (Node n : circle) {
            g.addNode(n.getLabel());
        }
        for (int i = 0; i < circle.size(); i++) {
            int iU = i;
            int iV = i + 1;
            if (iV == circle.size())
                iV = 0;

            Edge sEdge = graph.getEdge(circle.get(iU), circle.get(iV));

            g.addEdge(g.getNode(iU), g.getNode(iV), sEdge.getLabel());
        }

        String label = "$" + getLabel(g) + "$";
        featureStorage.processFeature(label);
        // add a second bit
        StringBuilder sb = new StringBuilder(label);
        featureStorage.processFeature(sb.reverse().toString());
    }

    /**
     * @param ring
     *            a graph that must be a ring
     */
    private String getLabel(Graph ring) {
        String distinctLabel = "";

        for (Node u : ring.nodes()) {
            for (Edge e : u.getEdges()) {
                StringBuilder sb = new StringBuilder();
                traverseRing(u, u, e, sb);
                String label = sb.toString();
                if (label.compareTo(distinctLabel) > 0) {
                    distinctLabel = label;
                }
            }
        }

        return distinctLabel;
    }

    /**
     * @param u
     * @param e
     *            = (u,*)
     * @param sb
     */
    private void traverseRing(Node startNode, Node u, Edge e, StringBuilder sb) {
        sb.append(getLabel(u));
        sb.append(getLabel(e));

        Node v = e.getOppositeNode(u);
        if (v == startNode)
            return; // ring closed

        List<Edge> edges = v.getEdges();
        Edge f = edges.get(0);
        if (f == e)
            f = edges.get(1);

        traverseRing(startNode, v, f, sb);
    }

}
