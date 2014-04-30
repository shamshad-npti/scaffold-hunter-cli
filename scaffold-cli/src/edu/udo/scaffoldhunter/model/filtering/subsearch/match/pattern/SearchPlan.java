
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Stores a sequence of extension operations, each consisting of a node together
 * with its join edges to nodes previous in the sequence. The order can be
 * optimized by atom frequencies. The consequence of which is that atoms assumed
 * to be rare are contained in the first operation. The next atom is chosen from
 * all possible atoms greedily.
 */
public class SearchPlan {

    /**
     * Used to enable debugging output.
     */
    public static final boolean DEBUG = false;

    /**
     * The order will not be modified and depends on the graph.
     */
    public static final int NO_PRIORITY = 0;

    /**
     * The order will be randomized.
     */
    public static final int RANDOM_PRIORITY = 1;

    /**
     * Nodes representing atoms assumed to be rare are preferred. This option is
     * only intended for molecule graphs.
     */
    public static final int ATOM_FREQUENCY_PRIORITY = 2;

    Extension[] order;
    /*
    Extension[] nodeToExtension;
    int[] nDFS;
    int c;
     */

    /**
     * Generates a search plan for the specified graph. The order is determined
     * by the priorityMode.
     * 
     * @param g
     *            the graph
     * @param priorityMode
     *            a value of NO_PRIORITY/RANDOM_PRIORITY/ATOM_FREQUENCY_PRIORITY
     */
    public SearchPlan(Graph g, int priorityMode) {

        Queue<Node> q;
        List<Node> nodes;

        switch (priorityMode) {
        case RANDOM_PRIORITY: {
            Comparator<Node> comp = new RandomComparator(g);
            q = new PriorityQueue<Node>(g.getNodeCount(), comp);

            // sort all nodes by frequency
            nodes = new LinkedList<Node>(g.nodes());
            Collections.sort(nodes, comp);
            break;
        }
        case ATOM_FREQUENCY_PRIORITY: {
            Comparator<Node> comp = new AtomFrequencyComparator(g);
            q = new PriorityQueue<Node>(g.getNodeCount(), comp);

            // sort all nodes by frequency
            nodes = new LinkedList<Node>(g.nodes());
            Collections.sort(nodes, comp);
            break;
        }
        default: {
            q = new LinkedList<Node>();
            nodes = g.nodes();
            break;
        }
        }

        order = new Extension[g.getNodeCount()];

	Extension[] nodeToExtension = new Extension[g.getNodeCount()]; // node retrieved from queue
        boolean[] found = new boolean[g.getNodeCount()]; // node added to queue
        int c = 0;

        for (Node u : nodes) {
            if (!found[u.getIndex()]) {
                q.add(u);
                found[u.getIndex()] = true;
                while (!q.isEmpty()) {
                    Node v = q.poll();
                    Extension ext = new Extension(v);
                    nodeToExtension[v.getIndex()] = ext;
                    order[c++] = ext;
                    for (Edge e : v.getEdges()) {
                        Node w = e.getOppositeNode(v);
                        if (!found[w.getIndex()]) {
                            q.add(w);
                            found[w.getIndex()] = true;
                        } else {
                            if (nodeToExtension[w.getIndex()] != null)
                                nodeToExtension[v.getIndex()].addEdge(e);
                        }
                    }
                }
            }
        }

        if (DEBUG) {
            for (int i = 0; i < g.getNodeCount(); i++) {
                Extension ext = getExtension(i);
                AtomFrequencyComparator afc = new AtomFrequencyComparator(g);
                System.out.print(ext.getNode().getLabel() + " " + afc.getFrequency(ext.getNode()));
                System.out.print(" " + ext.getNode().getIndex());
                for (Edge e : ext.getJoinEdges())
                    System.out.print(" " + e.getOppositeNode(ext.getNode()).getIndex());
                System.out.println();
            }
        }
    }

    /**
     * Creates a search plan for g without priority based optimization.
     * @param g the pattern graph
     */
    public SearchPlan(Graph g) {
        this(g, 0);

	// DFS
	/*
	order = new Extension[g.getNodeCount()];
	nodeToExtension = new Extension[g.getNodeCount()];
	nDFS = new int[pattern.getNodeCount()];
	c = 0;
	
	for (Node v : g.nodes()) {
	    if (nodeToExtension[v.getIndex()] == null)
		doDFS(v);
	    }

	nodeToExtension = null;
	nDFS = null;
	*/
    }

    /*
    private void doDFS(Node v) {
        Extension ext = new Extension(v);
	nodeToExtension[v.getIndex()] = ext;
	nDFS[v.getIndex()] = c;
	order[c] = ext;
	c++;
	for (Edge e : v.getEdges()) {
	    Node w = e.getOppositeNode(v);
	    if (nodeToExtension[w.getIndex()] == null)
		doDFS(w);
	    else {
		if (nDFS[w.getIndex()] < nDFS[v.getIndex()]) {
		    // (v,w) is back edge
		    // each edge is found twice: As tree- or forward-edge
		    // or as back-edge. To simplify matters only backedges are
		    // used to generate the joinEdges.
		    ext = nodeToExtension[v.getIndex()];
		    ext.addEdge(e);
		}
	    }
	}
    }
    */

    /**
     * Returns the i-th extension
     * @param i index of the extemsion
     * @return the i-th extension
     */
    public Extension getExtension(int i) {
        return order[i];
    }

    /**
     * Contains a node of the pattern graph and a list of all edges 
     * connecting the node to nodes of previous extensions (join edges).
     */
    public class Extension {
        Node node;
        LinkedList<Edge> joinEdges;

        /**
         * Initialize extension.
         * @param node the pattern node
         */
        public Extension(Node node) {
            this.node = node;
            joinEdges = new LinkedList<Edge>();
        }

        /**
         * Adds an edge, which should be a join edge.
         * @param e edge
         */
        public void addEdge(Edge e) {
            joinEdges.add(e);
        }

        /**
         * @return the node
         */
        public Node getNode() {
            return node;
        }

        /**
         * @return true iff join edges exist
         */
        public boolean hasJoinEdges() {
            return !joinEdges.isEmpty();
        }

        /**
         * @return a list of join edges
         */
        public LinkedList<Edge> getJoinEdges() {
            return joinEdges;
        }
    }

}
