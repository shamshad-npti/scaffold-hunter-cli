
package edu.udo.scaffoldhunter.model.filtering.subsearch.match;

import java.util.LinkedList;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.EdgeMatcher;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.NodeMatcher;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPattern;

/**
 * Represents a (partial) mapping.
 * 
 * @author Nils Kriege
 */
public class PartialMapping {

    /**
     * Number of mapping states visited. 
     */
    public static long stateCount = 0;
    /**
     * Number of partial mappings created.
     */
    public static long pmCount = 0;
    /**
     * Used to enable debugging output.
     */
    public static final boolean DEBUG = false;

    protected Graph pattern;
    protected Graph host;
    protected NodeMatcher nodeMatcher;
    protected EdgeMatcher edgeMatcher;

    protected int[] mapPH;
    protected int[] mapHP;

    protected int size;

    /**
     * Initializes the partial mapping 
     * @param searchPattern the search pattern
     * @param hostGraph the host graph
     */
    public PartialMapping(SearchPattern searchPattern, Graph hostGraph) {
        pmCount++;
        this.pattern = searchPattern.getGraph();
        this.host = hostGraph;
        this.nodeMatcher = searchPattern.getNodeMatcher();
        this.edgeMatcher = searchPattern.getEdgeMatcher();

        mapPH = new int[pattern.getNodeCount()];
        mapHP = new int[host.getNodeCount()];
        for (int i = 0; i < mapPH.length; i++)
            mapPH[i] = -1;
        for (int i = 0; i < mapHP.length; i++)
            mapHP[i] = -1;

        size = 0;
    }

    /**
     * Returns if the mapping is complete.
     * @return true iff every node of the pattern is mapped
     */
    public boolean isComplete() {
        return (size == pattern.getNodeCount());
    }

    /**
     * Returns the node of the host graph the given pattern 
     * node is mapped to.
     * @param patternNode the node of the pattern graph
     * @return the node of the host graph
     */
    public Node mapToHostNode(Node patternNode) {
        return host.getNode(mapPH[patternNode.getIndex()]);
    }

    /**
     * Returns the node of the pattern graph the given host 
     * node is mapped to.
     * @param hostNode the node of the host graph
     * @return the node of the pattern graph
     */
    public Node mapToPatternNode(Node hostNode) {
        return pattern.getNode(mapHP[hostNode.getIndex()]);
    }

    /**
     * Copies the current mapping to a convenient data structure.
     * @return a list of mapped pairs
     */
    public LinkedList<Pair> getMapping() {
        LinkedList<Pair> mapping = new LinkedList<Pair>();

        for (int i = 0; i < mapPH.length; i++)
            mapping.add(new Pair(pattern.getNode(i), host.getNode(mapPH[i])));

        return mapping;
    }

    /**
     * Returns the size of the current mapping.
     * @return number of mapped nodes
     */
    public int getSize() {
        return size;
    }

    /**
     * Extends the current mapping by the given pair.
     * @param patternNode the node of the pattern graph
     * @param hostNode the node of the host graph
     */
    public void extend(Node patternNode, Node hostNode) {
        stateCount++;
        if (DEBUG)
            System.out.println("added (" + patternNode.getIndex() + ", " + hostNode.getIndex() + ")");

        mapPH[patternNode.getIndex()] = hostNode.getIndex();
        mapHP[hostNode.getIndex()] = patternNode.getIndex();

        size++;
    }

    /**
     * Removes the given pair from the mapping. Note: This method does
     * not check if the given nodes indeed are mapped. Providing nodes
     * not mapped may result in consistent states!
     * @param patternNode the node of the pattern graph
     * @param hostNode the node of the host graph
     */
    public void remove(Node patternNode, Node hostNode) {
        if (DEBUG)
            System.out.println("removed (" + patternNode.getIndex() + ", " + hostNode.getIndex() + ")");

        mapPH[patternNode.getIndex()] = -1;
        mapHP[hostNode.getIndex()] = -1;

        size--;
    }

    /**
     * Checks if the given patternNode may be mapped to the given hostNode. A
     * node pair is feasible iff
     * <ul>
     * <li>neither patternNode nor hostNode are in the partial mapping</li>
     * <li>the label of the patternNode and the hostNode match</li>
     * <li>the degree of patternNode is less or equal to the degree of the
     * hostNode</li>
     * </ul>
     * @param patternNode the node of the pattern graph
     * @param hostNode the node of the host graph
     * @return true if mapping is feasible
     */
    public boolean isFeasibleNodePair(Node patternNode, Node hostNode) {
        // pattern or host node already mapped
        if (mapHP[hostNode.getIndex()] != -1 || mapPH[patternNode.getIndex()] != -1) {
            if (DEBUG)
                System.out.println("Not feasible: Node already in mapping");

            return false;
        }

        // correct label
        if (!nodeMatcher.match(patternNode, hostNode)) {
            if (DEBUG)
                System.out.println("Not feasible: Wrong label");

            return false;
        }

        // pruning: degree
        if (patternNode.getDegree() > hostNode.getDegree()) {
            if (DEBUG)
                System.out.println("Not feasible: Too many pattern edges");

            return false;
        }

        return true;
    }

    /**
     * Checks if the given patternEdges can be mapped to host graph edges in
     * case patternNode is mapped to hostNode. The patternEdges must be adjacent
     * to patternNode.
     * 
     * A node pair is feasible connected regarding patternEdges iff for each
     * edge from patternNode to a mapped node there is a corresponding edge in
     * the host graph.
     * @param patternNode the pattern node
     * @param hostNode the host node
     * @param patternEdges the pattern edges to nodes in the mapping 
     * @return true if mapping is feasible connected
     */
    public boolean isFeasibleConnectedPair(Node patternNode, Node hostNode, Iterable<Edge> patternEdges) {
        // check existence of pattern edges in host graph
        for (Edge e : patternEdges) {
            Node neighbour = e.getOppositeNode(patternNode);
            int iNeighbourMapped = mapPH[neighbour.getIndex()];
            if (iNeighbourMapped != -1) {
                Edge mappedEdge = host.getEdge(host.getNode(iNeighbourMapped), hostNode);

                // false iff host graph does not contain the edge or only a
                // wrong labeled edge
                if (mappedEdge == null || !edgeMatcher.match(e, mappedEdge)) {
                    if (DEBUG)
                        System.out.println("Not feasible: Missing/Wrong labeled edge");

                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuffer s = new StringBuffer();

        for (int i = 0; i < mapPH.length; i++)
            s.append(i + " -> " + mapPH[i] + "\n");

        return s.toString();
    }

}
