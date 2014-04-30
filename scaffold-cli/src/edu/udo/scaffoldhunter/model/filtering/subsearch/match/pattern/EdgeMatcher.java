
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;

/**
 * Interface for classes to determine the compatibility of edges.
 *  
 * @author Nils Kriege
 */
public interface EdgeMatcher {

    /**
     * Determines the compatibility of the two edges.
     * @param patternEdge the edge of the pattern graph
     * @param hosteEdge the edge of the host graph
     * @return the compatibility
     */
    public boolean match(Edge patternEdge, Edge hosteEdge);

    /**
     * Ignores edge label.
     */
    public class IgnoreLabel implements EdgeMatcher {
        @Override
        public boolean match(Edge patternEdge, Edge hostEdge) {
            return true;
        }
    }

    /**
     * Verifies edge label.
     */
    public class Label implements EdgeMatcher {
        @Override
        public boolean match(Edge patternEdge, Edge hostEdge) {
            return patternEdge.getLabel().equals(hostEdge.getLabel());
        }
    }

    /**
     * Respects Wildcard Labels.
     */
    public class Wildcards implements EdgeMatcher {
        @Override
        public boolean match(Edge patternEdge, Edge hostEdge) {
            Object patternLabel = patternEdge.getLabel();
            if (patternLabel instanceof GraphQueryLabel) {
                return ((GraphQueryLabel) patternLabel).matches(hostEdge.getLabel());
            } else {
                return patternLabel.equals(hostEdge.getLabel());
            }
        }
    }
}
