
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import java.util.Comparator;
import java.util.Random;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Allows to impose random orderings.
 * 
 * Note: this comparator imposes orderings that are inconsistent with equals.
 */
public class RandomComparator implements Comparator<Node> {

    private static final long seed = 88686765;

    private int[] randomValues;

    /**
     * Initializes a new instance.
     * @param g the graph whose nodes should be compared
     */
    public RandomComparator(Graph g) {
        randomValues = new int[g.getNodeCount()];

        Random r = new Random(seed);
        for (int i = 0; i < randomValues.length; i++) {
            randomValues[i] = r.nextInt();
        }
    }

    @Override
    public int compare(Node u, Node v) {
        int uVal = randomValues[u.getIndex()];
        int vVal = randomValues[v.getIndex()];

        if (uVal < vVal)
            return -1;
        if (uVal > vVal)
            return 1;

        return 0;
    }

}
