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

import java.util.LinkedList;
import java.util.List;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Ruleset extends DbObject {
    private String Title;
    /**
     * Ordered List of Rules
     * 
     * Attention: Cascade all -> do not save or delete manually 
     */
    private List<Rule> orderedRules;

    
    /**
     * default constructor
     */
    public Ruleset() {
        orderedRules = new LinkedList<Rule>();
    }

    /**
     * @param title
     * @param orderedRules
     */
    public Ruleset(String title, List<Rule> orderedRules) {
        Title = title;
        this.orderedRules = orderedRules;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return Title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        Title = title;
    }

    /**
     * @return the orderedRules
     * 
     * Attention: Cascade all -> do not save or delete manually 
     */
    public List<Rule> getOrderedRules() {
        return orderedRules;
    }

    /**
     * @param orderedRules
     *            the orderedRules to set
     *            
     * Attention: Cascade all -> do not save or delete manually 
     */
    public void setOrderedRules(List<Rule> orderedRules) {
        this.orderedRules = orderedRules;
        int orderNr = 0;
        for (Rule r: this.orderedRules)
            r.setOrder(orderNr++);
    }
    
    @Override
    public String toString() {
        return getTitle();
    }
}
