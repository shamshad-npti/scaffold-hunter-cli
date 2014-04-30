
package edu.udo.scaffoldhunter.model.filtering.subsearch.match;

import java.util.LinkedList;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPattern;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPlan.Extension;

/**
 * Matching algorithm with simple pruning.
 */
public class MatcherSimple extends Matcher {

    PartialMapping pm;

    /**
     * Constructs a new instance to match the given graphs.
     * @param patternGraph the pattern graph
     * @param hostGraph the host graph
     */
    public MatcherSimple(Graph patternGraph, Graph hostGraph) {
        super(patternGraph, hostGraph);
        pm = new PartialMapping(searchPattern, host);
    }

    /**
     * Constructs a new instance to match the given graphs.
     * @param searchPattern the search pattern
     * @param hostGraph the host graph
     */
    public MatcherSimple(SearchPattern searchPattern, Graph hostGraph) {
        super(searchPattern, hostGraph);
        pm = new PartialMapping(searchPattern, host);
    }

    @Override
    public LinkedList<Pair> getMatch() {
        if (!pm.isComplete())
            return null;

        return pm.getMapping();
    }

    private boolean hasNextHostCandidate(Node lastHostCandidate) {
        if (lastHostCandidate == null)
            return true;

        if (lastHostCandidate.getIndex() + 1 >= host.getNodeCount())
            return false;

        return true;
    }

    private Node nextHostCandidate(Node lastHostCandidate) {
        if (lastHostCandidate == null)
            return host.getNode(0);

        int i = lastHostCandidate.getIndex() + 1;
        return host.getNode(i);
    }

    private Node nextPatternCandidate() {
        Extension ext = searchPlan.getExtension(pm.getSize());
        return ext.getNode();
    }

    @Override
    public boolean match() {

        if (pm.isComplete())
            return handleMatch(pm);

        Node patternCandidate = nextPatternCandidate();
        Node hostCandidate = null;
        while (hasNextHostCandidate(hostCandidate)) {
            hostCandidate = nextHostCandidate(hostCandidate);

            if (DEBUG)
                System.out.println("Current Candidate: " + patternCandidate.getIndex() + " -> "
                        + hostCandidate.getIndex());

            if (pm.isFeasibleNodePair(patternCandidate, hostCandidate)
                    && pm.isFeasibleConnectedPair(patternCandidate, hostCandidate, patternCandidate.getEdges())) {
                pm.extend(patternCandidate, hostCandidate);
                if (match())
                    return true;
                pm.remove(patternCandidate, hostCandidate);
            }
        }

        return false;
    }
}
