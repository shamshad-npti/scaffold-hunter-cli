
package edu.udo.scaffoldhunter.model.filtering.subsearch.match;

import java.util.LinkedList;

/**
 * Implementing classes specify how to process a found matching.
 * 
 * @see Matcher#setMatchDelegate(MatchDelegate)
 * @author Nils Kriege
 */
public interface MatchDelegate {

    /**
     * This method specifies how to process a found mapping. If false is
     * returned the matching process will not abort when the first matching is
     * found. Note that in this case the function Matcher.match() will always
     * return false.
     * 
     * @param pm
     *            the mapping found
     * @return true if the search should be aborted after the function call,
     *         return false for an exhaustive search
     */
    public boolean handleMatch(PartialMapping pm);

    /**
     * Prints all matches.
     */
    public class PrintAll implements MatchDelegate {
        @Override
        public boolean handleMatch(PartialMapping pm) {
            for (Pair p : pm.getMapping())
                System.out.println(p.patternNode.getIndex() + " -> " + p.hostNode.getIndex());
            System.out.println("-----------------------");
            return false;
        }
    }

    /**
     * Stores a list of all matches.
     */
    public class CollectAll implements MatchDelegate {
        private LinkedList<LinkedList<Pair>> matches;

        /**
         * Constructor.
         */
        public CollectAll() {
            matches = new LinkedList<LinkedList<Pair>>();
        }

        @Override
        public boolean handleMatch(PartialMapping pm) {
            matches.add(pm.getMapping());
            return false;
        }

        /**
         * Returns a list of all matching.
         * @return list of matchings
         */
        public LinkedList<LinkedList<Pair>> getMatches() {
            return matches;
        }
    }
}
