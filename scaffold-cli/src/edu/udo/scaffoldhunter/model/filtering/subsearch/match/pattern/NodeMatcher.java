
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Interface for classes to determine the compatibility of nodes.
 *  
 * @author Nils Kriege
 */
public interface NodeMatcher {

    /**
     * Determines the compatibility of the two nodes.
     * @param patternNode the node of the pattern graph
     * @param hostNode the node of the host graph
     * @return the compatibility
     */    
    public boolean match(Node patternNode, Node hostNode);

    /**
     * Ignores node label.
     */
    public class IgnoreLabel implements NodeMatcher {
        @Override
        public boolean match(Node patternNode, Node hostNode) {
            return true;
        }
    }

    /**
     * Verifies node label.
     */
    public class Label implements NodeMatcher {
        @Override
        public boolean match(Node patternNode, Node hostNode) {
            return patternNode.getLabel().equals(hostNode.getLabel());
        }
    }

    /**
     * Respects Wildcard Labels.
     */
    public class Wildcards implements NodeMatcher {
        @Override
        public boolean match(Node patternNode, Node hostNode) {
            Object patternLabel = patternNode.getLabel();
            if (patternLabel instanceof GraphQueryLabel) {
                return ((GraphQueryLabel) patternLabel).matches(hostNode.getLabel());
            } else {
                return patternLabel.equals(hostNode.getLabel());
            }
        }
    }
}
