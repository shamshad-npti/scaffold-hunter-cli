
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import java.util.Comparator;
import java.util.HashMap;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Node;

/**
 * Compares two nodes by their (assumed) frequency resulting in orderings with
 * ascending frequency.
 * 
 * Note: this comparator should only be used with molecule-graphs. Note: this
 * comparator imposes orderings that are inconsistent with equals.
 */
public class AtomFrequencyComparator implements Comparator<Node> {

    /*
    public final static int[] atomicNumberFrequency = { Integer.MAX_VALUE, 4238982, 0, 0, 0, 545, 3834810,
        489704, 610408, 60154, 0, 0, 0, 42, 3138, 3063, 87881, 49092, 0, 0, 0, 0, 4, 1, 8, 0,
	0, 0, 0, 0, 0, 3, 24, 30, 225, 13355, 0, 0, 0, 0, 2, 0, 3, 1, 4, 1, 0, 0, 0, 2, 147,
	13, 29, 2242, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 7, 0, 0, 0,
	0, 0, 30, 3, 10, 8, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
     */

    private static HashMap<Object, Integer> atomSymbolFrequency;
    /**
     * Stores the sum of all label occurrences.
     */
    public static int totalAtomCount;
    private int[] priorities;

    /**
     * Creates a default map for molecules mapping atom types to frequencies. 
     * @return a map of atom type frequencies
     */
    public static HashMap<Object, Integer> createLabelFrequencyMap() {
        HashMap<Object, Integer> result = new HashMap<Object, Integer>();
        // full dataset
        result.put("H", 4238982);
        result.put("C", 3834810);
        result.put("O", 610408);
        result.put("N", 489704);
        result.put("S", 87881);
        result.put("F", 60154);
        result.put("Cl", 49092);
        result.put("Br", 13355);
        result.put("Si", 3138);
        result.put("P", 3063);
        result.put("I", 2242);
        result.put("B", 545);
        result.put("R", 345);
        result.put("Se", 225);
        result.put("Sn", 147);
        result.put("Al", 42);
        result.put("As", 30);
        result.put("Hg", 30);
        result.put("Te", 29);
        result.put("Ge", 24);
        result.put("Sb", 13);
        result.put("Pb", 10);
        result.put("Cr", 8);
        result.put("Bi", 8);
        result.put("W", 7);
        result.put("Ru", 4);
        result.put("Ti", 4);
        result.put("Mo", 3);
        result.put("Ga", 3);
        result.put("Tl", 3);
        result.put("In", 2);
        result.put("Zr", 2);
        result.put("V", 1);
        result.put("Hf", 1);
        result.put("Rh", 1);
        result.put("Tc", 1);

        /*
	// 4000 random instances
	atomSymbolFrequency.put("H",112050);
	atomSymbolFrequency.put("C",101811);
	atomSymbolFrequency.put("O",16154);
	atomSymbolFrequency.put("N",12802);
	atomSymbolFrequency.put("S",2314);
	atomSymbolFrequency.put("F",1612);
	atomSymbolFrequency.put("Cl",1317);
	atomSymbolFrequency.put("Br",369);
	atomSymbolFrequency.put("Si",86);
	atomSymbolFrequency.put("P",71);
	atomSymbolFrequency.put("I",65);
	atomSymbolFrequency.put("B",7);
	atomSymbolFrequency.put("Se",4);
	atomSymbolFrequency.put("Sn",3);
	atomSymbolFrequency.put("Mo",3);
	atomSymbolFrequency.put("As",2);
	atomSymbolFrequency.put("Al",2);
	atomSymbolFrequency.put("Ge",1);
	atomSymbolFrequency.put("Ru",1);
	atomSymbolFrequency.put("Hg",1);
         */

        return result;
    }

    /**
     * Creates a Comparator for the nodes of the given graph.
     * @param g graph this comparator is created for
     */
    public AtomFrequencyComparator(Graph g) {
        atomSymbolFrequency = createLabelFrequencyMap();
        priorities = new int[g.getNodeCount()];
        for (Node n : g.nodes()) {
            priorities[n.getIndex()] = getFrequency(n);
        }
    }

    /**
     * Return -1 if the atom represented by u is less frequent than the atom
     * represented by v, 0 if they are of equal frequency, 1 otherwise.
     */
    @Override
    public int compare(Node u, Node v) {
        //int uFrq = atomicNumberFrequency[(Integer)u.getLabel()];
	//int vFrq = atomicNumberFrequency[(Integer)v.getLabel()];
	//int uFrq = getFrequency(u);
	//int vFrq = getFrequency(v);
        int uFrq = priorities[u.getIndex()];
        int vFrq = priorities[v.getIndex()];

        if (uFrq < vFrq)
            return -1;
        if (uFrq > vFrq)
            return 1;

        // TODO sort by degree

        return 0;
    }

    /**
     * Returns the frequency of the given node.
     * @param n a node
     * @return the associated frequency
     */
    public int getFrequency(Node n) {
        Integer frq = 0;
        Object label = n.getLabel();
        // handle query label:
        // all nodes match wildcard label: max value
        // list: sum single frequencies
        // notlist: totalAtomCount - sum single frequencies in list
        if (label instanceof GraphQueryLabel) {
            if (label instanceof GraphQueryLabel.Wildcard) {
                frq = totalAtomCount;
            } else if (label instanceof GraphQueryLabel.List) {
                frq = 0;
                GraphQueryLabel.List list = (GraphQueryLabel.List) label;
                for (Object o : list.getLabelList()) {
                    frq += getFrequency(o);
                }
            } else if (label instanceof GraphQueryLabel.NotList) {
                frq = totalAtomCount;
                GraphQueryLabel.NotList list = (GraphQueryLabel.NotList) label;
                for (Object o : list.getLabelList()) {
                    frq -= getFrequency(o);
                }
            }
        } else {
            frq = getFrequency(label);
        }
        return frq;
    }

    private int getFrequency(Object label) {
        Integer frq = atomSymbolFrequency.get(label);
        if (frq == null) {
            frq = 0;
        }
        return frq;
    }

    /**
     * Sets a map of label frequencies.
     * @param atomSymbolFrequency
     */
    public static void setAtomSymbolFrequency(HashMap<Object, Integer> atomSymbolFrequency) {
        AtomFrequencyComparator.atomSymbolFrequency = atomSymbolFrequency;
        totalAtomCount = 0;
        for (int c : atomSymbolFrequency.values()) {
            totalAtomCount += c;
        }
    }

}
