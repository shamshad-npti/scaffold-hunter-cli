
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import java.util.HashMap;

import org.openscience.cdk.interfaces.IAtomContainer;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.DefaultGraph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.DefaultNode;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Edge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.MoleculeEdge;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.MoleculeGraph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.MoleculeNode;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Deletes all variable vertices and edges of a search pattern.
 */
public class NonVariableSubgraphBuilder {

    /**
     * Creates a new graph non containing any variable parts, i.e.,
     * nodes or edges with wildcard labels.
     * @param sp the search pattern
     * @return a subgraph of search pattern
     */
    public static Graph create(SearchPattern sp) {

        Graph graph = sp.getGraph();

        if (!sp.getUseWildcards()) {
            return graph;
        }

        // copies the molecule graph and the underlying atom container
        // deletes all variable elements.
        if (graph instanceof MoleculeGraph) {
            MoleculeGraph mg = ((MoleculeGraph) graph).clone();

            IAtomContainer ac = mg.getAtomContainer();
            for (Node n : mg.nodes()) {
                if (n.getLabel() instanceof GraphQueryLabel) {
                    ac.removeAtom(((MoleculeNode) n).getAtom());
                    for (Edge e : n.getEdges()) {
                        ac.removeBond(((MoleculeEdge) e).getBond());
                    }
                }
            }
            for (Edge e : mg.edges()) {
                if (e.getLabel() instanceof GraphQueryLabel) {
                    ac.removeBond(((MoleculeEdge) e).getBond());
                }
            }
            return new MoleculeGraph(ac);
        } else {
            DefaultGraph newGraph = new DefaultGraph();

            HashMap<Node, DefaultNode> nodeToNewNode = new HashMap<Node, DefaultNode>();

            for (Node n : graph.nodes()) {
                if (!(n.getLabel() instanceof GraphQueryLabel)) {
                    DefaultNode newNode = (DefaultNode)newGraph.addNode(n.getLabel());
                    nodeToNewNode.put(n, newNode);
                }
            }

            for (Edge e : graph.edges()) {
                if (!(e.getLabel() instanceof GraphQueryLabel)) {
                    DefaultNode u = nodeToNewNode.get(e.getFirstNode());
                    DefaultNode v = nodeToNewNode.get(e.getSecondNode());
                    if (u != null && v != null) {
                        newGraph.addEdge(u, v, e.getLabel());
                    }
                }
            }
            return newGraph;
        }
    }

}
