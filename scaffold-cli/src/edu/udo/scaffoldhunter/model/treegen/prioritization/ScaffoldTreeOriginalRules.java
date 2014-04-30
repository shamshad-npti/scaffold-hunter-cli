/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
 * See the file README.txt in the root directory of the Scaffold Hunter
 * source tree for details.
 *
 * Scaffold Hunter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Scaffold Hunter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package edu.udo.scaffoldhunter.model.treegen.prioritization;

import java.util.ArrayList;
import java.util.Vector;

import edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer;

/**
 * Implementation of the rules to determine a scaffold tree according
 * to the original publication:
 * 
 * Schuffenhauer, A.; Ertl, P.; Roggo, S.; Wetzel, S.; Koch, M. A. & Waldmann, H. 
 * The Scaffold Tree - Visualization of the Scaffold Universe by Hierarchical 
 * Scaffold Classification 
 * J. Chem. Inf. Model., 2007, 47, 47-58
 * 
 * @author Nils Kriege
 */
public class ScaffoldTreeOriginalRules {
    
    private static ArrayList<ScaffoldFilterRule> rules;
    
    /**
     * Returns a list of rules according to the original implementation.
     * @return the original list of rules
     */
    public static ArrayList<ScaffoldFilterRule> getRules() {
        if (rules != null) return rules;
        
        rules = new ArrayList<ScaffoldFilterRule>();
        rules.add(new Rule00());
        rules.add(new Rule01());
        rules.add(new Rule02());
        rules.add(new Rule03());
        rules.add(new Rule04());
        rules.add(new Rule05());
        rules.add(new Rule06());
        rules.add(new Rule07());
        rules.add(new Rule08());
        rules.add(new Rule09a());
        rules.add(new Rule09b());
        rules.add(new Rule09c());
        rules.add(new Rule10());
        rules.add(new Rule11());
        rules.add(new Rule12());
        rules.add(new Rule13());      

        return rules;
    }
    
    /**
     * This rule does not filter out any scaffolds, but exists for compatibility
     * reasons with the original implementation. When only a single scaffold
     * exists this rule triggers the termination of the iterative filter process.
     */
    public static class Rule00 implements ScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_00_onlyone"; }        
        @Override
        public Vector<ScaffoldContainer> filter(Vector<ScaffoldContainer> scaffolds) {
            return scaffolds;
        }
    }
    
    /**
     * Scaffold filter rule #01 from the original publication.
     */
    public static class Rule01 extends AbstractScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_01_het3first"; }
        @Override
        public boolean fulfills(ScaffoldContainer sc) {
            return (sc.getRRPringSize() == 3 && sc.getRRPnumHetAt() == 1);
        }
    }
    
    /**
     * Scaffold filter rule #02 from the original publication.
     */
    public static class Rule02 extends AbstractScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_02_not12ringAtoms"; }
        @Override
        public boolean fulfills(ScaffoldContainer sc) {
            return !(sc.getRRPringSize() > 11);
        }
    }
    
    /**
     * Scaffold filter rule #03 from the original publication.
     */
    public static class Rule03 extends AbstractMinScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_03_smallestacycbonds"; }
        @Override
        public int getProperty(ScaffoldContainer sc) {
            return sc.getSCPnumALB();
        }
    }
    
    /**
     * Scaffold filter rule #04 from the original publication.
     */
    public static class Rule04 extends AbstractMinScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_04_highabsdelta"; }
        @Override
        public int getProperty(ScaffoldContainer sc) {
            return -sc.getSCPabsDelta(); // used to determine max
        }
    }

    /**
     * Scaffold filter rule #05 from the original publication.
     */
    public static class Rule05 extends AbstractScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_05_highdelta"; }
        @Override
        public boolean fulfills(ScaffoldContainer sc) {
            return sc.getSCPdelta() >= 0;
        }
    }
    
    /**
     * Scaffold filter rule #06 from the original publication.
     */
    public static class Rule06 extends AbstractScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_06_356first"; }
        @Override
        public boolean fulfills(ScaffoldContainer sc) {
            int rrsize = sc.getRRPringSize();
            return (rrsize == 6 || rrsize == 5 || rrsize == 3);
        }
    }
    
    /**
     * Scaffold filter rule #07 from the original publication.
     */
    public static class Rule07 implements ScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_07_unimplemented"; }
        @Override
        public Vector<ScaffoldContainer> filter(Vector<ScaffoldContainer> scaffolds) {
            return scaffolds;
        }
    }
    
    /**
     * Scaffold filter rule #08 from the original publication.
     */
    public static class Rule08 extends AbstractMinScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_08_leasthetfirst"; }
        @Override
        public int getProperty(ScaffoldContainer sc) {
            return sc.getRRPnumHetAt();
        }
    }
    
    /**
     * Scaffold filter rule #09(a) from the original publication.
     */
    public static class Rule09a extends AbstractMinScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_09a_leastNfirst"; }
        @Override
        public int getProperty(ScaffoldContainer sc) {
            return sc.getRRPnumNAt();
        }
    }
    
    /**
     * Scaffold filter rule #09(b) from the original publication.
     */
    public static class Rule09b extends AbstractMinScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_09b_leastOfirst"; }
        @Override
        public int getProperty(ScaffoldContainer sc) {
            return sc.getRRPnumOAt();
        }
    }
    
    /**
     * Scaffold filter rule #09(c) from the original publication.
     */
    public static class Rule09c extends AbstractMinScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_09c_leastSfirst"; }
        @Override
        public int getProperty(ScaffoldContainer sc) {
            return sc.getRRPnumSAt();
        }
    }
    
    /**
     * Scaffold filter rule #10 from the original publication.
     */
    public static class Rule10 extends AbstractMinScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_10_smallerrings"; }
        @Override
        public int getProperty(ScaffoldContainer sc) {
            return sc.getRRPringSize();
        }
    }
    
    /**
     * Scaffold filter rule #11 from the original publication.
     */
    public static class Rule11 extends AbstractScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_11_arofirst"; }
        @Override
        public boolean fulfills(ScaffoldContainer sc) {
            return sc.getRRParomatic();
        }
    }
    
    /**
     * Scaffold filter rule #12 from the original publication.
     */
    public static class Rule12 extends AbstractScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_12_hetatch"; }
        @Override
        public boolean fulfills(ScaffoldContainer sc) {
            return sc.getRRPhetatlinked();
        }
    }
    
    /**
     * Scaffold filter rule #13 from the original publication for
     * breaking ties.
     */
    public static class Rule13 implements ScaffoldFilterRule {
        @Override
        public String getName() { return "Rule_13_tiebreak"; }
        @Override
        public Vector<ScaffoldContainer> filter(Vector<ScaffoldContainer> scaffolds) {
            Vector<ScaffoldContainer> filteredScaffolds = new Vector<ScaffoldContainer>();
            ScaffoldContainer min = scaffolds.firstElement();
            for (ScaffoldContainer sc : scaffolds) {
                if (min.getSMILES().compareTo(sc.getSMILES()) > 0) {
                    min = sc;
                }
            }
            filteredScaffolds.add(min);
            return filteredScaffolds;
        }
    }


}
