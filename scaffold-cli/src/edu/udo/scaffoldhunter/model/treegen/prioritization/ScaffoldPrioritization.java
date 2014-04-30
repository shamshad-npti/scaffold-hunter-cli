package edu.udo.scaffoldhunter.model.treegen.prioritization;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import org.apache.batik.dom.util.HashTable;

import edu.udo.scaffoldhunter.model.RuleType;
import edu.udo.scaffoldhunter.model.db.Rule;
import edu.udo.scaffoldhunter.model.db.Ruleset;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer;

/**
 * Class for the selection of parent scaffolds based on a set of rules.
 * 
 * @author Steffen Renner <steffen.renner@mpi-dortmund.mpg.de>
 *         Max-Planck-Institut für Molekulare Physiologie Otto-Hahn-Strasse 11
 *         D-44227 Dortmund Germany
 * @author Philipp Lewe
 * @author Nils Kriege
 * 
 */
public class ScaffoldPrioritization {
    
    ArrayList<ScaffoldFilterRule> customRules;

    /**
     * Sets custom rules from a ruleset.
     * @param ruleset the <code>Ruleset</code>
     */
    public void setCustomRules(Ruleset ruleset) {
        customRules = new ArrayList<ScaffoldFilterRule>();
        for (Rule rule : ruleset.getOrderedRules()) {
            customRules.add(new CustomScaffoldFilterRule(rule));
        }
        // tie-breaking rule
        customRules.add(new ScaffoldTreeOriginalRules.Rule13());
    }

    
    /**
     * Reads in custom rules from a file
     * 
     * @param filename
     *            the path to the rules file
     * @return the <code>Ruleset</code> constructed out of the file
     * @throws IOException 
     */
    public static Ruleset readRulesFile(String filename) throws IOException {
        Ruleset return_val = new Ruleset();
        HashTable rulesHash = new HashTable();
        
        // fill lookup values
        for (RuleType rule : RuleType.values()) {
            rulesHash.put(rule.name(), rule.ordinal());
        }

        BufferedReader reader;
        String line;
    
        reader = new BufferedReader(new FileReader(filename));

        while ((line = reader.readLine()) != null) {

            String[] sline = line.trim().split(" ");
            // reading in the rules
            if (sline.length == 2 && !sline[0].equals("#")) {
                int selection = Integer.MAX_VALUE;

                try {
                    RuleType ruleType = RuleType.valueOf(RuleType.class, sline[0]);
                    selection = Integer.parseInt(sline[1]);
                    if(selection < Integer.MAX_VALUE) {
                        Rule rule = new Rule();
                        rule.setRuleset(return_val);
                        rule.setRule(ruleType);
                        if(selection > 0) {
                            rule.setAscending(true);
                        } else {
                            rule.setAscending(false);
                        }
                        return_val.getOrderedRules().add(rule);
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("The following was not recognized as rule by the parser \"" + sline[0] + " " + sline[1] + "\"");
                }
            }
        }        
        reader.close();
        return return_val;
    }
    
    /**
     * Select a parent scaffold from a set of scaffolds.
     * 
     * @param scaffolds
     *            Vector of scaffolds from which the best is selected as parent
     * @return a <code>ScaffoldContainer</code> representing the parent scaffold
     */
    public ScaffoldContainer selectParentScaffoldCustomRules(Vector<ScaffoldContainer> scaffolds) {
        if (customRules == null) throw new IllegalStateException("No custom rules set");
        return selectParentScaffold(customRules, scaffolds);
    }

    /**
     * Select a parent scaffold from a set of scaffolds according the original set of rules.
     * 
     * @param scaffolds
     *            Vector of scaffolds from which the best is selected as parent
     * @return a <code>ScaffoldContainer</code> representing the parent scaffold
     */
    public ScaffoldContainer selectParentScaffoldOriginalRules(Vector<ScaffoldContainer> scaffolds) {
        ArrayList<ScaffoldFilterRule> rules = ScaffoldTreeOriginalRules.getRules();
        return selectParentScaffold(rules, scaffolds);
    }
        
    /**
     * Select a parent scaffold from a set of scaffolds based on the given rules.
     * 
     * @param rules rules used for parent scaffold selection
     * @param scaffolds
     *            Vector of scaffolds from which the best is selected as parent
     * @return a <code>ScaffoldContainer</code> representing the parent scaffold
     */
    public ScaffoldContainer selectParentScaffold(ArrayList<ScaffoldFilterRule> rules, Vector<ScaffoldContainer> scaffolds) {
        
        if (scaffolds.isEmpty()) return null;
                
        Vector<ScaffoldContainer> remaining = scaffolds;
        for (ScaffoldFilterRule rule : rules) {
            Vector<ScaffoldContainer> stillRemaining = rule.filter(remaining);
            if (!stillRemaining.isEmpty()) {
                remaining = stillRemaining;
            }
            if (remaining.size() == 1) {
                remaining.firstElement().setDeletionRule(rule.getName());
                break;
            }
        }
        return remaining.firstElement();
    }

    /**
     * Returns weights of parents
     * @param scaffolds list of scaffolds
     * @return weights of parents
     */
    public int[] getParentWeights(Vector<ScaffoldContainer> scaffolds) {
        int[] weights = new int[scaffolds.size()];
        if (scaffolds.isEmpty()) return weights;

        ArrayList<ScaffoldFilterRule> rules = ScaffoldTreeOriginalRules.getRules();
        Vector<ScaffoldContainer> remaining = scaffolds;
        for (int i=0; i<rules.size(); i++) {
            ScaffoldFilterRule rule = rules.get(i);
            Vector<ScaffoldContainer> stillRemaining = rule.filter(remaining);
            for (ScaffoldContainer sc : stillRemaining) {
                weights[scaffolds.indexOf(sc)] += rules.size() - i; // minus 1 even for tiebreak rule
            }
        }
        return weights;
    }

        
}

	
