
package edu.udo.scaffoldhunter.model.filtering.subsearch.match;

import java.util.List;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPattern;

/**
 * Extends PartialMapping by building the terminal sets (nodes adjacent to
 * mapped nodes) of the mapping as required by the VF2 algorithm.
 * 
 * Note: The terminal arrays include the nodes in the mapping to make
 * backtracking easy.
 */
public class PartialMappingTerminal extends PartialMapping {

    protected int[] termP;
    protected int[] termH;

    /**
     * Initializes the partial mapping 
     * @param searchPattern the search pattern
     * @param hostGraph the host graph
     */
    public PartialMappingTerminal(SearchPattern searchPattern, Graph hostGraph) {
        super(searchPattern, hostGraph);

        termP = new int[pattern.getNodeCount()];
        termH = new int[host.getNodeCount()];
    }

    /**
     * Adds the nodes to the mapping and extends the terminal sets.
     */
    @Override
    public void extend(Node patternNode, Node hostNode) {
        super.extend(patternNode, hostNode);

        // TODO: vflib implementation adds patternNode/hostNode
        // to terminal set (if not already included - arises only
        // with disconnected graphs). Why?
        List<Edge> edges = patternNode.getEdges();
        for (Edge e : edges) {
            Node neighbour = e.getOppositeNode(patternNode);
            if (termP[neighbour.getIndex()] == 0)
                termP[neighbour.getIndex()] = size;
        }

        edges = hostNode.getEdges();
        for (Edge e : edges) {
            Node neighbour = e.getOppositeNode(hostNode);
            if (termH[neighbour.getIndex()] == 0)
                termH[neighbour.getIndex()] = size;
        }
    }

    /**
     * Removes the nodes from the mapping and restores the terminal sets.
     * Note: Only call this function with pair of nodes added last!
     */
    @Override
    public void remove(Node patternNode, Node hostNode) {
        List<Edge> edges = patternNode.getEdges();
        for (Edge e : edges) {
            Node neighbour = e.getOppositeNode(patternNode);
            if (termP[neighbour.getIndex()] == size)
                termP[neighbour.getIndex()] = 0;
        }

        edges = hostNode.getEdges();
        for (Edge e : edges) {
            Node neighbour = e.getOppositeNode(hostNode);
            if (termH[neighbour.getIndex()] == size)
                termH[neighbour.getIndex()] = 0;
        }

        super.remove(patternNode, hostNode);
    }

    /**
     * A candidate pair is feasible iff
     * <ul>
     * <li>the label of the patternNode and the hostNode match</li>
     * <li>for each edge from patternNode to a mapped node there is an
     * corresponding edge in the host graph</li>
     * <li>the number of patternNodes neighbors in the terminal set is less or
     * equal to the number of hostNodes neighbors in the terminal set</li>
     * <li>the number of patternNodes neighbors not mapped is less or equal to
     * the number of hostNodes neighbors not mapped</li>
     * </ul>
     * @param patternNode the node of the pattern graph
     * @param hostNode the node of the host graph
     * @return true if mapping is feasible

     */
    public boolean isFeasibleCandidate(Node patternNode, Node hostNode) {

        // correct label
        if (!nodeMatcher.match(patternNode, hostNode)) {
            if (DEBUG)
                System.out.println("Not feasible: Wrong label");

            return false;
        }

        int cTermP = 0;
        int cTermH = 0;
        int cNewP = 0;
        int cNewH = 0;

        // check existence of pattern edges in host graph
        // and count terminal/new neigbours
        List<Edge> edges = patternNode.getEdges();
        for (Edge e : edges) {
            Node neighbour = e.getOppositeNode(patternNode);
            int iNeighbourMapped = mapPH[neighbour.getIndex()];
            if (iNeighbourMapped != -1) {
                Edge mappedEdge = host.getEdge(host.getNode(iNeighbourMapped), hostNode);

                // false iff host graph does not contain the edge or only a
                // wrong labeled edge
                if (mappedEdge == null || !edgeMatcher.match(e, mappedEdge))
                    return false;
            } else {
                if (termP[neighbour.getIndex()] != 0)
                    cTermP++;
                else
                    cNewP++;
            }
        }

        // count terminal/new neigbours in host graph
        // no edge existence check required due to monomorphism
        edges = hostNode.getEdges();
        for (Edge e : edges) {
            Node neighbour = e.getOppositeNode(hostNode);
            int iNeighbourMapped = mapHP[neighbour.getIndex()];
            if (iNeighbourMapped == -1) {
                if (termH[neighbour.getIndex()] != 0)
                    cTermH++;
                else
                    cNewH++;
            }
        }

        // terminal set pruning
        if (cTermP > cTermH || cTermP + cNewP > cTermH + cNewH) {
            return false;
        }

        return true;
    }

    /**
     * Used to iterate over the set of nodes in the terminal set of the
     * host graph.
     * @param pattern a pattern node the returned host node should be mapped to
     * @param lastHost the last node returned by this procedure, null to 
     * start with the first node
     * @return the next node in the terminal set
     */
    public Node nextTerminalHostNode(Node pattern, Node lastHost) {

        int i = (lastHost == null) ? 0 : lastHost.getIndex() + 1;

        if (termP[pattern.getIndex()] != 0) {
            while (i < host.getNodeCount() && (termH[i] == 0 || mapHP[i] != -1))
                i++;
        } else {
            while (i < host.getNodeCount() && mapHP[i] != -1)
                i++;
        }

        if (i < host.getNodeCount())
            return host.getNode(i);
        else
            return null;
    }

}
