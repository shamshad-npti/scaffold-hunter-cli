
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.MoleculeGraph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Stores a pattern graph together with its search plan and the its node/edge
 * matcher.
 */
public class SearchPattern {

    private Graph graph;
    private NodeMatcher nodeMatcher;
    private EdgeMatcher edgeMatcher;
    private SearchPlan searchPlan;
    private boolean useWildcards;

    /**
     * Initializes a new search pattern.
     * 
     * @param graph the pattern graph
     * @param useWildcards account for wildcard labels 
     * @param priorityMode mode to determine the node order
     */
    public SearchPattern(Graph graph, boolean useWildcards, int priorityMode) {
        this.graph = graph;

        configure(useWildcards);

        this.searchPlan = new SearchPlan(graph, priorityMode);
    }

    /**
     * Note: If useWildcards is true, wildcards label of the graphs will be
     * changed to GraphQueryLabel
     * 
     * @param graph
     * @param useWildcards
     */
    public SearchPattern(Graph graph, boolean useWildcards) {
        this(graph, useWildcards, 
                (graph instanceof MoleculeGraph) ? 
                        SearchPlan.ATOM_FREQUENCY_PRIORITY : 
                        SearchPlan.NO_PRIORITY);
    }

    private void configure(boolean useWildcards) {
        if (useWildcards && compileQueryLabel()) {
            this.nodeMatcher = new NodeMatcher.Wildcards();
            this.edgeMatcher = new EdgeMatcher.Wildcards();
            this.useWildcards = true;
        } else {
            this.nodeMatcher = new NodeMatcher.Label();
            this.edgeMatcher = new EdgeMatcher.Label();
            this.useWildcards = false;
        }
    }

    private boolean compileQueryLabel() {
        boolean hasQueryLabel = false;
        for (Node n : graph.nodes()) {
            if (n.getLabel() instanceof String) {
                String s = (String) n.getLabel();
                if (s.equals("*") || s.equals("R")) {
                    n.setLabel(new GraphQueryLabel.Wildcard());
                    hasQueryLabel = true;
                } else if (s.contains("[")) {
                    // format must be [symbol,symbol,...] or ![symbol,symbol,...]
                    GraphQueryLabel.AbstractList queryList;
                    if (s.startsWith("!")) {
                        queryList = new GraphQueryLabel.NotList();
                        s = s.substring(1); // remove '!'
                    } else {
                        queryList = new GraphQueryLabel.List();
                    }
                    s = s.substring(1, s.length() - 1);
                    String[] list = s.split(",");
                    queryList.setLabelList(list);
                    n.setLabel(queryList);
                    hasQueryLabel = true;
                }
            } else if (n.getLabel() instanceof GraphQueryLabel) {
                hasQueryLabel = true;
            }
        }
        for (Edge e : graph.edges()) {
            if (e.getLabel() instanceof String) {
                String s = (String) e.getLabel();
                if (s.equals("*")) {
                    e.setLabel(new GraphQueryLabel.Wildcard());
                    hasQueryLabel = true;
                }
            } else if (e.getLabel() instanceof GraphQueryLabel) {
                hasQueryLabel = true;
            }
        }
        return hasQueryLabel;
    }

    /**
     * @return the pattern graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * @return the node matcher
     */
    public NodeMatcher getNodeMatcher() {
        return nodeMatcher;
    }

    /**
     * @return the edge matcher
     */
    public EdgeMatcher getEdgeMatcher() {
        return edgeMatcher;
    }

    /**
     * @return the search plan
     */
    public SearchPlan getSearchPlan() {
        return searchPlan;
    }

    /**
     * @return true if wildcards are considered
     */
    public boolean getUseWildcards() {
        return useWildcards;
    }

}
