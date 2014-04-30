
package edu.udo.scaffoldhunter.model.filtering.subsearch.match;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

// TODO remove, use more generic datastructures.Pair
/**
 * A pair of nodes in a matching.
 * @author Nils Kriege
 */
public class Pair {
    /**
     * The pattern node.
     */
    public Node patternNode;
    /**
     * The host node.
     */
    public Node hostNode;

    /**
     * Creates a new pair.
     * @param patternNode the node in the pattern graph
     * @param hostNode the node in the host graph
     */
    public Pair(Node patternNode, Node hostNode) {
        this.patternNode = patternNode;
        this.hostNode = hostNode;
    }
}
