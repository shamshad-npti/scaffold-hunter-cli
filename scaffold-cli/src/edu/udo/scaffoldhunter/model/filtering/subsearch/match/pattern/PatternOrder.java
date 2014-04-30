
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import java.util.LinkedList;
import java.util.Queue;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Class to generate node orderings of pattern graphs.
 * 
 * @author Nils Kriege
 */
public abstract class PatternOrder {

    /**
     * Creates a node order.
     * @param pattern the pattern graph
     * @return an array of node indices
     */
    public abstract int[] createOrder(Graph pattern);

    /**
     * Creates a node order by BFS.
     * @param g the graph
     * @return an array of node indices
     */
    public static int[] createBFSOrder(Graph g) {

        int[] order = new int[g.getNodeCount()];

        boolean[] found = new boolean[g.getNodeCount()];
        int c = 0;
        Queue<Node> q = new LinkedList<Node>();

        for (Node u : g.nodes()) {
            if (!found[u.getIndex()]) {
                q.add(u);
                found[u.getIndex()] = true;
                while (!q.isEmpty()) {
                    Node v = q.poll();
                    order[c++] = v.getIndex();
                    for (Edge e : v.getEdges()) {
                        Node w = e.getOppositeNode(v);
                        if (!found[w.getIndex()]) {
                            q.add(w);
                            found[w.getIndex()] = true;
                        }
                    }
                }
            }
        }

        return order;
    }

    /**
     * Creates a node order by DFS.
     */
    public static class DFSPatternOrder extends PatternOrder {
        private int[] order;
        private boolean[] found;
        int c;

        @Override
        public int[] createOrder(Graph g) {

            order = new int[g.getNodeCount()];
            found = new boolean[g.getNodeCount()];

            for (Node u : g.nodes()) {
                if (!found[u.getIndex()])
                    doDFS(u);
            }

            return order;
        }

        private void doDFS(Node v) {
            found[v.getIndex()] = true;
            order[c++] = v.getIndex();
            for (Edge e : v.getEdges()) {
                Node w = e.getOppositeNode(v);
                if (!found[w.getIndex()])
                    doDFS(w);
            }

        }
    }
}
