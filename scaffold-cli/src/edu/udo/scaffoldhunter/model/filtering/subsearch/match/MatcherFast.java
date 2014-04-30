
package edu.udo.scaffoldhunter.model.filtering.subsearch.match;

import java.util.List;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPattern;
import edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern.SearchPlan;

/**
 * The algorithm uses a sequence of pattern nodes with joinEdges (edges from a
 * node to previous nodes) for a backtracking search on the host graph to reduce
 * overhead.
 * 
 * The search-space is pruned by checking the degree of nodes only.
 * 
 * @author Nils Kriege
 */
public class MatcherFast extends Matcher {

    PartialMapping pm;

    /**
     * Constructs a new instance to match the given graphs.
     * @param patternGraph the pattern graph
     * @param hostGraph the host graph
     */
     public MatcherFast(Graph patternGraph, Graph hostGraph) {
        super(patternGraph, hostGraph);
        pm = new PartialMapping(searchPattern, host);
    }

    /**
     * Constructs a new instance to match the given graphs.
     * @param searchPattern the search pattern
     * @param hostGraph the host graph
     */
    public MatcherFast(SearchPattern searchPattern, Graph hostGraph) {
        super(searchPattern, hostGraph);
        pm = new PartialMapping(searchPattern, host);
    }

    @Override
    public List<Pair> getMatch() {
        if (!pm.isComplete())
            return null;

        return pm.getMapping();
    }

    private Node getJoinHostNode(SearchPlan.Extension ext, Node patternCandidate) {
        Edge joinEdge = ext.getJoinEdges().getFirst();
        Node joinHostNode = pm.mapToHostNode(joinEdge.getOppositeNode(patternCandidate));
        return joinHostNode;
    }

    /*
    private Node getMinDegreeJoinHostNode(SearchPlan.Extension ext, Node patternCandidate) {
	// TODO use number of unmapped nodes instead of degree
		
	Node minJoinHostNode = null;
	int minDegree = Integer.MAX_VALUE;
	for (Edge joinEdge : ext.getJoinEdges()) {
	    Node joinHostNode = pm.mapToHostNode(joinEdge.getOppositeNode(patternCandidate));
	    if (joinHostNode.getDegree() < minDegree) {
		minJoinHostNode = joinHostNode;
		minDegree = joinHostNode.getDegree();
	    }
	}

	return minJoinHostNode;
    }
    */

    @Override
    public boolean match() {

        if (pm.isComplete())
            return handleMatch(pm);

        SearchPlan.Extension ext = searchPlan.getExtension(pm.getSize());
        Node patternCandidate = ext.getNode();

        if (!ext.hasJoinEdges()) {
            // the host candidates can not be restricted to adjacent nodes,
            // all host nodes have to be checked
            for (Node hostCandidate : host.nodes()) {
                if (pm.isFeasibleNodePair(patternCandidate, hostCandidate)) {
                    pm.extend(patternCandidate, hostCandidate);
                    if (match())
                        return true;
                    pm.remove(patternCandidate, hostCandidate);
                }
            }
        } else {
            Node joinHostNode = getJoinHostNode(ext, patternCandidate);

            // only check adjacent nodes
            for (Edge edgeCandidate : joinHostNode.getEdges()) {
                Node hostCandidate = edgeCandidate.getOppositeNode(joinHostNode);
                if (pm.isFeasibleNodePair(patternCandidate, hostCandidate)
                        && pm.isFeasibleConnectedPair(patternCandidate, hostCandidate, ext.getJoinEdges())) {
                    pm.extend(patternCandidate, hostCandidate);
                    if (match())
                        return true;
                    pm.remove(patternCandidate, hostCandidate);
                }
            }
        }

        return false;
    }
}
