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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Finds the path of a graph.
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class PathExtractor extends FeatureExtractor<String> {

    /**
     * Used to find all paths (allowing cycles)
     */
    public static final int PATHS = 0;
    /**
     * Used to find all simple paths and paths with a single cycle
     */
    public static final int SINGLE_CYCLE_SIMPLE_PATHS = 1;
    /**
     * Used to find all simple paths
     */
    public static final int SIMPLE_PATHS = 2;

    private int maxSize;
    private int type;

    /**
     * Constructor
     * 
     * @param graph
     *            the {@link Graph} to extract the features from
     * @param featureStorage
     *            the {@link FeatureStorage}
     * @param maxSize
     *            the maximal path size
     * @param type
     *            the
     */
    public PathExtractor(Graph graph, FeatureStorage<? super String, ?> featureStorage, int maxSize, int type) {
        super(graph, featureStorage);
        this.maxSize = maxSize;
        this.type = type;
    }

    @Override
    public void extractFeatures() {
        switch (type) {
        case PATHS: {
            Path path = new Path();
            for (Node n : graph.nodes()) {
                pathSearch(n, maxSize, path);
            }
            break;
        }
        case SIMPLE_PATHS: {
            Path path = new Path();
            for (Node n : graph.nodes()) {
                simplePathSearch(n, maxSize, path);
            }
            break;
        }
        case SINGLE_CYCLE_SIMPLE_PATHS: {
            SingleCyclePath path = new SingleCyclePath();
            for (Node n : graph.nodes()) {
                singleCyclePathSearch(n, maxSize, path);
            }
            break;
        }
        }
    }

    private void simplePathSearch(Node u, int depth, Path path) {
        path.push(u);
        featureStorage.processFeature(path.getLexSmallerLabelPath());

        for (Edge e : u.getEdges()) {
            Node v = e.getOppositeNode(u);
            if (!path.contains(v) && depth > 0) {
                simplePathSearch(v, depth - 1, path);
            }
        }

        path.pop();
    }

    private void pathSearch(Node u, int depth, Path path) {
        Node parent = path.peekLast();

        path.push(u);
        featureStorage.processFeature(path.getLexSmallerLabelPath());

        for (Edge e : u.getEdges()) {
            Node v = e.getOppositeNode(u);
            if (v != parent && depth > 0) {
                pathSearch(v, depth - 1, path);
            }
        }

        path.pop();
    }

    private void singleCyclePathSearch(Node u, int depth, SingleCyclePath path) {
        Node parent = path.peekLast();

        path.push(u);
        featureStorage.processFeature(path.getLexSmallerLabelPath());

        for (Edge e : u.getEdges()) {
            Node v = e.getOppositeNode(u);
            if (v != parent && depth > 0 && (!path.contains(v) || (!path.containsCycle() && v != path.peekFirst()))) {
                singleCyclePathSearch(v, depth - 1, path);
            }
        }

        path.pop();

    }

    private class Path {
        LinkedList<Node> nodePath;
        LinkedList<Edge> edgePath;
        int[] pathMember; // stores the path size when the node became member

        public Path() {
            nodePath = new LinkedList<Node>();
            edgePath = new LinkedList<Edge>();
            pathMember = new int[graph.getNodeCount()];
        }

        public void push(Node n) {
            if (!nodePath.isEmpty()) {
                edgePath.addLast(graph.getEdge(nodePath.getLast(), n));
            }
            nodePath.addLast(n);

            if (pathMember[n.getIndex()] == 0)
                pathMember[n.getIndex()] = nodePath.size();
        }

        public Node pop() {
            Node n = nodePath.removeLast();
            if (!nodePath.isEmpty()) {
                edgePath.removeLast();
            }

            if (pathMember[n.getIndex()] > nodePath.size()) {
                pathMember[n.getIndex()] = 0;
            }

            return n;
        }

        public Node peekLast() {
            return nodePath.peekLast();
        }

        public Node peekFirst() {
            return nodePath.peekFirst();
        }

        public boolean contains(Node n) {
            return pathMember[n.getIndex()] != 0;
        }

        @SuppressWarnings("unused")
        public int indexOf(Node n) {
            return pathMember[n.getIndex()] - 1;
        }

        public boolean isEmpty() {
            return nodePath.isEmpty();
        }

        // Reversing the normal string does not work for node/edge labels
        // with more than one char (unless they are palindromes), e.g.
        // Cl-Ca / aC-lC -> aC-lC is stored
        // Ca-Cl / lC-aC -> Ca-Cl is stored
        // As a result the same path produced two different label paths,
        // depending
        // on the order.
        // This method does not have this problem.
        private String getLabelPath(LinkedList<Node> nodeList, LinkedList<Edge> edgeList, boolean reverse) {
            if (this.isEmpty())
                return "";

            StringBuilder sb = new StringBuilder();
            Iterator<Node> iNode;
            Iterator<Edge> iEdge;
            if (reverse) {
                iNode = nodeList.descendingIterator();
                iEdge = edgeList.descendingIterator();
            } else {
                iNode = nodeList.iterator();
                iEdge = edgeList.iterator();
            }

            sb.append(getLabel(iNode.next()));
            while (iNode.hasNext()) {
                sb.append(getLabel(iEdge.next()));
                sb.append(getLabel(iNode.next()));
            }

            return sb.toString();
        }

        public String getLabelPath() {
            return getLabelPath(nodePath, edgePath, false);
        }

        public String getReverseLabelPath() {
            return getLabelPath(nodePath, edgePath, true);
        }

        public String getLexSmallerLabelPath() {
            String string = this.getLabelPath();
            String revString = this.getReverseLabelPath();
            return string.compareTo(revString) < 0 ? string : revString;
        }

        @SuppressWarnings("unused")
        public Path subPath(int fromIndex, int toIndex) {
            Path result = new Path();
            ListIterator<Node> iNode = nodePath.listIterator(fromIndex);
            while (fromIndex++ < toIndex) {
                result.push(iNode.next());
            }
            return result;
        }

        @SuppressWarnings("unused")
        public int size() {
            return nodePath.size();
        }

        @SuppressWarnings("unused")
        public String getEdgeLabel(int index) {
            return getLabel(edgePath.get(index - 1));
        }
    }

    private class SingleCyclePath {
        Path part1;
        Path cycle;
        Path part2;
        Path currentPath;

        public SingleCyclePath() {
            part1 = new Path();
            cycle = new Path();
            part2 = new Path();
            currentPath = part1;
        }

        public void push(Node n) {
            if (part1.contains(n)) {
                cycle.push(n);
                Node u;
                do {
                    u = part1.pop();
                    cycle.push(u);
                } while (u != n);
                currentPath = cycle;
            } else {
                if (currentPath == cycle) {
                    currentPath = part2;
                }
                currentPath.push(n);
            }
        }

        public Node pop() {
            if (currentPath == cycle) {
                while (!cycle.isEmpty()) {
                    part1.push(cycle.pop());
                }
                currentPath = part1;
                return part1.pop();
            }
            Node n = currentPath.pop();
            if (currentPath.isEmpty() && currentPath == part2) {
                currentPath = cycle;
            }
            return n;
        }

        public Node peekLast() {
            return currentPath.peekLast();
        }

        public Node peekFirst() {
            return currentPath.peekFirst();
        }

        public boolean contains(Node n) {
            return part1.contains(n) || cycle.contains(n) || part2.contains(n);
        }

        public boolean containsCycle() {
            return !cycle.isEmpty();
        }

        @SuppressWarnings("unused")
        public boolean isEmpty() {
            return part1.isEmpty() && cycle.isEmpty();
        }

        private String getLabelPath(boolean reverse) {
            String cycleEdge = "", cycleMarker = "", part2Edge = "";
            if (containsCycle()) {
                if (!part1.isEmpty()) {
                    Edge e = graph.getEdge(part1.peekLast(), cycle.peekFirst());
                    cycleEdge = getLabel(e);
                }
                cycleMarker = "!";
            }
            if (!part2.isEmpty()) {
                Edge e = graph.getEdge(cycle.peekLast(), part2.peekFirst());
                part2Edge = getLabel(e);
            }

            if (reverse) {
                return part2.getReverseLabelPath() + part2Edge + cycleMarker + cycle.getLexSmallerLabelPath()
                        + cycleMarker + cycleEdge + part1.getReverseLabelPath();
            } else {
                return part1.getLabelPath() + cycleEdge + cycleMarker + cycle.getLexSmallerLabelPath() + cycleMarker
                        + part2Edge + part2.getLabelPath();
            }
        }

        public String getLabelPath() {
            return getLabelPath(false);
        }

        public String getReverseLabelPath() {
            return getLabelPath(true);
        }

        public String getLexSmallerLabelPath() {
            String string = this.getLabelPath();
            String revString = this.getReverseLabelPath();
            return string.compareTo(revString) < 0 ? string : revString;
        }

    }

}
