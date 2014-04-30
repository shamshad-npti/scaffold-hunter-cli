/*
 * ScaffoldHunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffoldhunter installation for details.
 *
 * This file is part of ScaffoldHunter.
 *
 * ScaffoldHunter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScaffoldHunter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.udo.scaffoldhunter.gui.datasetmanagement;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import edu.udo.scaffoldhunter.model.db.Rule;

/**
 * @author Philipp Lewe
 * 
 */
public class RulesetTableModel extends AbstractTableModel {

    private List<Rule> rules;

    /**
     * Creates and empty <code>RulesetTableModel</code>
     */
    public RulesetTableModel() {
        rules = new LinkedList<Rule>();
    }
    
    /**
     * Gets a shallow copy of the internal rules list of this model
     * @return the list of rules
     */
    public List<Rule> getRules() {
        List<Rule> rulesCopy = new LinkedList<Rule>();
        rulesCopy.addAll(rules);
        
        return rulesCopy;
    }

    /**
     * Returns the number of elements in the model
     * 
     * @return model size
     */
    public int getSize() {
        return rules.size();
    }

    /**
     * Returns the rule at index i in this model
     * 
     * @param i
     *            the index
     * @return the rule
     */
    public Rule get(int i) {
        return rules.get(i);
    }

    /**
     * Sets the rule at index i in this model
     * 
     * @param i
     *            the index
     * @param rule
     *            the rule
     */
    public void set(int i, Rule rule) {
        rules.set(i, rule);
        fireTableCellUpdated(i, 0);
        fireTableCellUpdated(i, 1);
    }

    /**
     * Removes all rules from the model
     */
    public void clear() {
        rules.clear();
        fireTableDataChanged();
    }

    /**
     * Adds a rule to the model
     * 
     * @param rule
     *            the rule to be added
     */
    public void addElement(Rule rule) {
        rules.add(rule);
        fireTableRowsInserted(rules.size() - 1, rules.size() - 1);
    }

    /**
     * Adds all rules in the collection to the model
     * 
     * @param collection
     *            the collection of rules to be added
     */
    public void addAllElements(Collection<Rule> collection) {
        for (Rule rule : collection) {
            addElement(rule);
        }
    }

    /**
     * Removes a rule from the model
     * 
     * @param rule
     *            the rule to be removed
     */
    public void removeElement(Rule rule) {
        int i = rules.indexOf(rule);
        rules.remove(rule);
        fireTableRowsDeleted(i, i);
    }
    
    /**
     * Removes all rules in the collection from the model
     * 
     * @param collection
     *            the collection of rules to be removed
     */
    public void removeAllElements(Collection<Rule> collection) {
        for (Rule rule : collection) {
            removeElement(rule);
        }
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return _("ManageRulesets.UsedRules.Rule");
        } else {
            return _("ManageRulesets.UsedRules.Ascending");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return rules.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return rules.get(rowIndex).getRule().name();
        } else {
            if (rules.get(rowIndex).isAscending()) {
                return _("ManageRulesets.UsedRules.Ascending.True");
            } else {
                return _("ManageRulesets.UsedRules.Ascending.False");
            }
        }
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col == 1);
    }

    @Override
    public void setValueAt(Object value, int row, int col) {
        if (col == 1 && value instanceof String) {
            boolean ascending;
            if (value == _("ManageRulesets.UsedRules.Ascending.True")) {
                ascending = true;
            } else {
                ascending = false;
            }
            rules.get(row).setAscending(ascending);
        }
        fireTableCellUpdated(row, col);
    }
}
