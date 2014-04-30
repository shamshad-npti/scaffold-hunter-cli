/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffold Hunter source tree
 * for details.
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

package edu.udo.scaffoldhunter.model.db;

import edu.udo.scaffoldhunter.model.RuleType;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Rule extends DbObject implements Comparable<Rule> {
    private RuleType rule;
    private boolean ascending;
    private int order;
    private Ruleset ruleset;

    /**
     * default constructor
     */
    public Rule() {
    }

    /**
     * @param rule
     * @param ascending
     * @param order
     */
    public Rule(RuleType rule, boolean ascending, int order) {
        this.rule = rule;
        this.ascending = ascending;
        this.order = order;
    }

    @Override
    public int compareTo(Rule o) {
        return order - o.order;
    }
    
    /**
     * @return the rule
     */
    public RuleType getRule() {
        return rule;
    }

    /**
     * @param rule
     *            the rule to set
     */
    public void setRule(RuleType rule) {
        this.rule = rule;
    }

    /**
     * @return the ascending
     */
    public boolean isAscending() {
        return ascending;
    }

    /**
     * @param ascending
     *            the ascending to set
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * @return the order
     */
    int getOrder() {
        return order;
    }

    /**
     * @param order
     *            the order to set
     */
    void setOrder(int order) {
        this.order = order;
    }

    /**
     * @param ruleset the ruleset to set
     */
    public void setRuleset(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    /**
     * @return the ruleset
     */
    public Ruleset getRuleset() {
        return ruleset;
    }
    
    @Override
    public String toString() {
        return (getRule().toString());
    }
}
