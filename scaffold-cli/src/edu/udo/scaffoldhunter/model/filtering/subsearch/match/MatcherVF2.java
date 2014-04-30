
package edu.udo.scaffoldhunter.model.filtering.subsearch.match;

import java.util.LinkedList;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPattern;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPlan.Extension;

/**
 * VF2 Implementation
 * 
 * @author Nils Kriege
 */
public class MatcherVF2 extends Matcher {

    PartialMappingTerminal pmt;

    /**
     * Constructs a new instance to match the given graphs.
     * @param patternGraph the pattern graph
     * @param hostGraph the host graph
     */
    public MatcherVF2(Graph patternGraph, Graph hostGraph) {
        super(patternGraph, hostGraph);
        pmt = new PartialMappingTerminal(searchPattern, host);
    }

    /**
     * Constructs a new instance to match the given graphs.
     * @param searchPattern the search pattern
     * @param hostGraph the host graph
     */
    public MatcherVF2(SearchPattern searchPattern, Graph hostGraph) {
        super(searchPattern, hostGraph);
        pmt = new PartialMappingTerminal(searchPattern, host);
    }

    @Override
    public LinkedList<Pair> getMatch() {
        if (!pmt.isComplete())
            return null;

        return pmt.getMapping();
    }

    private Node nextPatternCandidate() {
        Extension ext = searchPlan.getExtension(pmt.getSize());
        return ext.getNode();
    }

    @Override
    public boolean match() {

        if (pmt.isComplete())
            return handleMatch(pmt);

        Node patternCandidate = nextPatternCandidate();
        Node hostCandidate = pmt.nextTerminalHostNode(patternCandidate, null);
        while (hostCandidate != null) {

            if (DEBUG)
                System.out.println("Current Candidate: " + patternCandidate.getIndex() + " -> "
                        + hostCandidate.getIndex());

            if (pmt.isFeasibleCandidate(patternCandidate, hostCandidate)) {
                pmt.extend(patternCandidate, hostCandidate);
                if (match())
                    return true;
                pmt.remove(patternCandidate, hostCandidate);
            }

            hostCandidate = pmt.nextTerminalHostNode(patternCandidate, hostCandidate);
        }

        return false;
    }

}
