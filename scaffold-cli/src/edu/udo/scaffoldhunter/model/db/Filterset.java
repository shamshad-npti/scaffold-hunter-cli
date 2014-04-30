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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Filterset extends Preset {
    private Set<Filter> filters;
    /**
     * true if the filters are linked by conjunction 
     * false if the filters are linked by disjunction
     */
    private boolean conjunctive = true;

    /**
     * default constructor
     */
    public Filterset() {
        super();
        
        filters = new HashSet<Filter>();
    }

    /**
     * @param profile
     * @param title
     * @param filters
     * @param conjunctive
     */
    public Filterset(Profile profile, String title, Set<Filter> filters, boolean conjunctive) {
        super(profile, title);

        this.filters = filters;
        this.setConjunctive(conjunctive);
    }
    
    /**
     * @return the filters
     */
    public Set<Filter> getFilters() {
        return filters;
    }

    /**
     * @param filters the filters to set
     */
    public void setFilters(Set<Filter> filters) {
        this.filters = filters;
    }

    /**
     * @param conjunctive
     *            sets whether the linkage between the filters are conjunctive
     *            or disjunctive
     */
    public void setConjunctive(boolean conjunctive) {
        this.conjunctive = conjunctive;
    }

    /**
     * @return the whether the linkage between the filters are conjunctive or
     *         disjunctive
     */
    public boolean isConjunctive() {
        return conjunctive;
    }
}
