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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.DefaultEdge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.DefaultGraph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Represents a subtree of a graph and allows to add and remove vertices and 
 * edge of the underlying graph to the subtree. This class is used in the subtree
 * enumeration process implemented by {@link SubtreeExtractor} and provides 
 * tree canonization by the method {@link #getCanonicalLabeling()}.
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class Subtree extends DefaultGraph {

    private int maxSize = 5;
    private int n;
    private int m;

    private ArrayList<IndexEdge> edges;

    private LinkedList<Node> activeNodes;
    private BitSet iActiveNodes;

    private LinkedList<IndexEdge> activeEdges;
    private BitSet iActiveEdges;

    private int[] activeDegree;
    private final String[] canonicalLabelings;
    private final Comparator<Node> nodeLabelingComparator;

    private ArrayList<ArrayList<Node>> children;
    private ArrayList<Node> level;
    private ArrayList<Node> nextLevel;

    private boolean[] forbid;

    /**
     * Constructor
     * 
     * @param g
     *            the underlying {@link Graph}
     * @param maxSize
     *            the number of edges a subtree may contain
     */
    public Subtree(Graph g, int maxSize) {
        this.maxSize = maxSize;

        List<Node> gNodes = g.nodes();
        List<Edge> gEdges = g.edges();

        this.n = gNodes.size();
        this.m = gEdges.size();

        // construct graph copy
        edges = new ArrayList<IndexEdge>(gEdges.size());

        for (Node u : gNodes)
            this.addNode(u);

        for (Edge e : gEdges)
            this.addEdge(getNode(e.getFirstNode().getIndex()), getNode(e.getSecondNode().getIndex()), e);

        // init activation data structure
        activeNodes = new LinkedList<Node>();
        iActiveNodes = new BitSet(n);

        activeEdges = new LinkedList<IndexEdge>();
        iActiveEdges = new BitSet(m);

        activeDegree = new int[n];
        forbid = new boolean[m];

        canonicalLabelings = new String[n];
        nodeLabelingComparator = new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return canonicalLabelings[o1.getIndex()].compareTo(canonicalLabelings[o2.getIndex()]);
            }
        };
        children = new ArrayList<ArrayList<Node>>(n);
        for (int i = 0; i < n; i++) {
            children.add(new ArrayList<Node>());
        }
        level = new ArrayList<Node>();
        nextLevel = new ArrayList<Node>();

        clearLists();
    }

    @Override
    public IndexEdge addEdge(Node u, Node v, Object label) {
        IndexEdge edge = new IndexEdge(u, v, label, edges.size());
        u.addEdge(edge);
        v.addEdge(edge);
        edges.add(edge);
        super.edgeCount++;

        return edge;
    }

    /**
     * Extends the default edge by an index.
     */
    public class IndexEdge extends DefaultEdge {
        private int index;

        protected IndexEdge(Node u, Node v, Object label, int index) {
            super(u, v, label);
            this.index = index;
        }

        /**
         * Return the index of the {@link Edge}
         * 
         * @return the index
         */
        public int getIndex() {
            return index;
        }
    }

    /**
     * Adds a {@link Node} of the underlying graph to the subtree.
     * 
     * @param v
     *            the {@link Node} to activate
     */
    public void addActiveNode(Node v) {
        iActiveNodes.set(v.getIndex());
        activeNodes.push(v);
    }

    /**
     * Removes the {@link Node} from the subtree that was added last.
     */
    public void removeLastActiveNode() {
        Node v = activeNodes.pop();
        iActiveNodes.clear(v.getIndex());
    }

    /**
     * Adds an {@link Edge} of the underlying graph to the subtree.
     * Note that exactly one node of the edge must already be contained
     * in the subtree; the other node is added automatically.
     * 
     * @param e
     *            the {@link Edge} to activate
     */
    public void addActiveEdge(IndexEdge e) {
        iActiveEdges.set(e.getIndex());
        activeEdges.push(e);
        Node u = e.getFirstNode();
        Node v = e.getSecondNode();
        if (iActiveNodes.get(u.getIndex())) {
            addActiveNode(v);
        } else {
            addActiveNode(u);
        }
        activeDegree[u.getIndex()]++;
        activeDegree[v.getIndex()]++;
    }

    /**
     * Removes the {@link Edge} from the subtree that was added last. This 
     * will also remove the vertex that is no longer connected to the tree.
     */
    public void removeLastActiveEdge() {
        IndexEdge e = activeEdges.pop();
        iActiveEdges.clear(e.getIndex());
        removeLastActiveNode();
        activeDegree[e.getFirstNode().getIndex()]--;
        activeDegree[e.getSecondNode().getIndex()]--;
    }

    /**
     * Blocks an edge for extension.
     * 
     * @param e the edge that should be blocked
     * @see #getExtensions()
     */
    public void forbidEdge(IndexEdge e) {
        forbid[e.getIndex()] = true;
    }

    /**
     * Unblocks edges for extension.
     * 
     * @param edges the edges that should be unblocked
     * @see #getExtensions()
     */
    public void allowEdges(Collection<IndexEdge> edges) {
        for (IndexEdge e : edges) {
            forbid[e.getIndex()] = false;
        }
    }

    /**
     * Returns all edges of the underlying graph that can be used to extend
     * the current subtree. This is an empty set if the maximum subtree size is
     * reached. The set only contains edges that are not blocked and do not
     * form a cycle when added to the subtree.
     * 
     * @return list of edges allowed for extension
     * @see #forbidEdge(IndexEdge)
     * @see #allowEdges(Collection)
     */
    public List<IndexEdge> getExtensions() {
        LinkedList<IndexEdge> result = new LinkedList<IndexEdge>();

        if (activeNodes.size() > maxSize)
            return result;

        for (Node v : activeNodes) {
            for (Edge e : v.getEdges()) {
                int edgeIndex = ((IndexEdge) e).getIndex();
                if (!iActiveEdges.get(edgeIndex) && !iActiveNodes.get(e.getOppositeNode(v).getIndex())
                        && !forbid[edgeIndex]) {
                    result.add((IndexEdge) e);
                }
            }
        }

        return result;
    }

    private Iterable<IndexEdge> getActiveEdges(final Node u) {
        return new Iterable<IndexEdge>() {
            @Override
            public Iterator<IndexEdge> iterator() {
                return new Iterator<IndexEdge>() {
                    Iterator<Edge> i = u.getEdges().iterator();
                    IndexEdge current;

                    @Override
                    public boolean hasNext() {
                        while (i.hasNext()) {
                            if (iActiveEdges.get((current = (IndexEdge) i.next()).getIndex()))
                                return true;
                        }
                        return false;
                    }

                    @Override
                    public IndexEdge next() {
                        return current;
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /**
     * Computes a canonical labeling for the current subtree, i.e., a string
     * that uniquely identifies the structure and labels of the tree.
     * Two trees have the same canonical labeling iff they are isomorphic.
     *  
     * @return a canonical string encoding the current tree
     */
    public String getCanonicalLabeling() {
        // find center
        for (Node v : activeNodes) {
            // add leafs/the root of single node trees
            if (activeDegree[v.getIndex()] <= 1) {
                nextLevel.add(v);
            }
        }

        int[] activeDegreeClone = activeDegree.clone();
        while (!nextLevel.isEmpty()) {
            // swap
            ArrayList<Node> tmp = level;
            level = nextLevel;
            nextLevel = tmp;
            nextLevel.clear();
            for (Node u : level) {
                for (Edge e : getActiveEdges(u)) {
                    Node v = e.getOppositeNode(u);
                    // nodes with degree 1 have been processed before
                    // or there is a single node that is the root of this tree
                    if (activeDegreeClone[v.getIndex()] != 1 || (!nextLevel.isEmpty() && v == nextLevel.get(0))) {
                        int degree = --activeDegreeClone[v.getIndex()];
                        children.get(v.getIndex()).add(u);
                        if (degree == 1) {
                            nextLevel.add(v);
                        }
                    }
                }
            }
        }

        String label;
        ArrayList<Node> roots = level;
        if (roots.size() == 1) {
            label = getCanonicalLabeling(roots.get(0), children);
        } else { // bicentered
            String label1 = getCanonicalLabeling(roots.get(0), children);
            String label2 = getCanonicalLabeling(roots.get(1), children);

            String edgeLabel = FeatureExtractor.getLabel((Edge) getEdge(roots.get(0), roots.get(1)).getLabel());
            if (label1.compareTo(label2) < 0)
                label = label1 + edgeLabel + label2;
            else
                label = label2 + edgeLabel + label1;
        }

        clearLists();

        return label;
    }

    // T.root; T.T1, ..., T.Tn sorted by l(T.Ti); T1.edge, ..., Tn.edgd
    // build label: l(T)=l(T.root)l(T1.edge)l(T.1),...,l(Tn.edge)l(T.n)$
    // bottom-up
    private String getCanonicalLabeling(Node u, ArrayList<? extends List<Node>> children) {
        int childCount = children.get(u.getIndex()).size();

        if (childCount == 0) {
            return FeatureExtractor.getLabel((Node) u.getLabel()) + "$";
        } else if (childCount == 1) {
            Node child = children.get(u.getIndex()).get(0);
            Edge edge = getEdge(u, child);
            String edgeLabel = FeatureExtractor.getLabel((Edge) edge.getLabel());
            canonicalLabelings[child.getIndex()] = edgeLabel + getCanonicalLabeling(child, children);
            return FeatureExtractor.getLabel((Node) u.getLabel()) + canonicalLabelings[child.getIndex()] + "$";
        } else {
            List<Node> uChildren = children.get(u.getIndex());
            for (Node v : uChildren) {
                Edge edge = getEdge(u, v);
                String edgeLabel = FeatureExtractor.getLabel((Edge) edge.getLabel());
                canonicalLabelings[v.getIndex()] = edgeLabel + getCanonicalLabeling(v, children);
            }
            Collections.sort(uChildren, nodeLabelingComparator);
            StringBuilder bc = new StringBuilder(FeatureExtractor.getLabel((Node) u.getLabel()));
            for (Node c : uChildren)
                bc.append(canonicalLabelings[c.getIndex()]);
            bc.append("$");
            return bc.toString();
        }
    }

    private void clearLists() {
        for (int i = 0; i < n; i++) {
            children.get(i).clear();
        }
        level.clear();
        nextLevel.clear();
    }

    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        if (activeEdges.isEmpty()) {
            b.append(activeNodes.getFirst().getIndex());
        } else {
            for (IndexEdge e : activeEdges) {
                b.append(e.getFirstNode().getIndex() + " -(" + e.getIndex() + ")- " + e.getSecondNode().getIndex()
                        + "\n");
            }
        }
        return b.toString();
    }

}
