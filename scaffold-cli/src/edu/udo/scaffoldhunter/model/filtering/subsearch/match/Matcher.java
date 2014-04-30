
package edu.udo.scaffoldhunter.model.filtering.subsearch.match;

import java.util.List;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPattern;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPlan;

/**
 * Base class for subgraph isomorphism algorithms.
 *
 * @author Nils Kriege
 */
public abstract class Matcher {

    /**
     * Used to enable debugging output.
     */
    public static final boolean DEBUG = false;

    protected SearchPattern searchPattern;
    protected SearchPlan searchPlan;
    protected Graph host;
    protected MatchDelegate matchDelegate;

    /**
     * Initializes the Matcher. Uses the SearchPlan of the specified pattern
     * graph.
     * 
     * @param searchPattern
     * @param hostGraph
     */
    public Matcher(SearchPattern searchPattern, Graph hostGraph) {
        this.searchPattern = searchPattern;
        this.searchPlan = searchPattern.getSearchPlan();
        this.host = hostGraph;
    }

    /**
     * Initializes the Matcher. Creates a SearchPlan for the specified pattern
     * graph.
     * 
     * @param patternGraph
     * @param hostGraph
     */
    public Matcher(Graph patternGraph, Graph hostGraph) {
        this(new SearchPattern(patternGraph, false), hostGraph);
    }

    /**
     * Sets a matching delegate which will be called for every matching found.
     * The default behavior is to abort the matching process and let the
     * function match() return true.
     * 
     * @param md a {@link MatchDelegate} to handle matchings 
     */
    public void setMatchDelegate(MatchDelegate md) {
        matchDelegate = md;
    }

    protected boolean handleMatch(PartialMapping pm) {
        if (matchDelegate != null)
            return matchDelegate.handleMatch(pm);
        else
            return true;
    }

    /**
     * Returns the current matching.
     * @return current matching
     */
    public abstract List<Pair> getMatch();

    /**
     * Invokes the matching process. By default the process will abort as soon
     * as the first match is found. This behaviour may be changed by using a
     * MatchDelegate.
     * 
     * @return true iff a mapping was found and was not delegated. When true
     *         getMatch() will return a correct mapping.
     * @see #getMatch()
     * @see #setMatchDelegate(MatchDelegate)
     */
    public abstract boolean match();

}
