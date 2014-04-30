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

package edu.udo.scaffoldhunter.model.filtering.subsearch.graph;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class DefaultGraph implements Graph {

    ArrayList<Node> nodes;
    protected int edgeCount;

    /**
     * Constructor
     */
    public DefaultGraph() {
        nodes = new ArrayList<Node>();
        edgeCount = 0;
    }

    /**
     * Constructor
     * 
     * @param initialNodeCapacity
     *            the initial capacity of the underlying {@link Node} data
     *            structure
     */
    public DefaultGraph(int initialNodeCapacity) {
        nodes = new ArrayList<Node>(initialNodeCapacity);
    }

    /**
     * Copy Constructor
     * 
     * @param g
     *            the {@link Graph} to copy
     */
    public DefaultGraph(Graph g) {
        this(g.getNodeCount());

        addGraph(g);
    }

    @Override
    public DefaultGraph clone() {
        return new DefaultGraph(this);
    }

    @Override
    public List<Node> nodes() {
        return nodes;
    }

    @Override
    public List<Edge> edges() {
        LinkedList<Edge> edges = new LinkedList<Edge>();
        for (Node u : nodes)
            for (Edge e : u.getEdges())
                if (u.getIndex() < e.getOppositeNode(u).getIndex())
                    edges.add(e);

        return edges;
    }

    @Override
    public DefaultNode getNode(int index) {
        return (DefaultNode) nodes.get(index);
    }

    @Override
    public int getNodeCount() {
        return nodes.size();
    }

    @Override
    public int getEdgeCount() {
        return edgeCount;
    }

    @Override
    public boolean hasEdge(Node u, Node v) {
        return getEdge(u, v) != null;
    }

    @Override
    public DefaultEdge getEdge(Node u, Node v) {
        List<? extends Edge> edges = u.getEdges();
        for (Edge e : edges)
            if (e.getOppositeNode(u) == v)
                return (DefaultEdge) e;
        return null;
    }

    @Override
    public Node addNode(Object label) {
        DefaultNode node = new DefaultNode(label, nodes.size());
        nodes.add(node);
        return node;
    }

    @Override
    public Edge addEdge(Node u, Node v, Object label) {
        Edge edge = new DefaultEdge(u, v, label);
        u.addEdge(edge);
        v.addEdge(edge);
        edgeCount++;

        return edge;
    }

    @Override
    public void addGraph(Graph g) {
        int offset = this.getNodeCount();

        for (int i = 0; i < g.getNodeCount(); i++)
            this.addNode(g.getNode(i).getLabel());

        for (Edge e : g.edges()) {
            DefaultNode u = this.getNode(e.getFirstNode().getIndex() + offset);
            DefaultNode v = this.getNode(e.getSecondNode().getIndex() + offset);
            this.addEdge(u, v, e.getLabel());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("V={");
        for (Node n : nodes()) {
            sb.append("(");
            sb.append(n.getIndex());
            sb.append(",");
            sb.append(n.getLabel());
            sb.append(") ");
        }
        if (!nodes().isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}\n");
        sb.append("E={");
        for (Edge e : edges()) {
            sb.append("(");
            sb.append(e.getFirstNode().getIndex());
            sb.append(",");
            sb.append(e.getSecondNode().getIndex());
            sb.append(",");
            sb.append(e.getLabel());
            sb.append(") ");
        }
        if (!edges().isEmpty()) {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append("}");

        return sb.toString();
    }
}
