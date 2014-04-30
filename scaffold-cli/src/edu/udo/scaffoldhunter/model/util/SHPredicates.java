/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

package edu.udo.scaffoldhunter.model.util;

import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.not;

import com.google.common.base.Predicate;

import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * A collection of useful {@link Predicate}s.
 * 
 * @author Henning Garus
 * 
 */
public class SHPredicates {

    /**
     * Checks if a PropertyDefinition describes a string property.
     */
    public static final Predicate<PropertyDefinition> IS_STRING_PROPDEF = new Predicate<PropertyDefinition>() {
        @Override
        public boolean apply(PropertyDefinition input) {
            return input.isStringProperty();
        };
    };

    /**
     * Checks if a PropertyDefinition describes a scaffold property.
     */
    public static final Predicate<PropertyDefinition> IS_SCAFFOLD_PROPDEF = new Predicate<PropertyDefinition>() {
        @Override
        public boolean apply(PropertyDefinition input) {
            return input.isScaffoldProperty();
        };
    };

    /**
     * Checks if a PropertyDefinition describes a scaffold string property.
     */
    public static final Predicate<PropertyDefinition> IS_STRSCAF_PROPDEF = and(IS_STRING_PROPDEF, IS_SCAFFOLD_PROPDEF);

    /**
     * Checks if a PropertyDefinition describes a numerical scaffold property.
     */
    public static final Predicate<PropertyDefinition> IS_NUMSCAF_PROPDEF = and(not(IS_STRING_PROPDEF),
            IS_SCAFFOLD_PROPDEF);

    /**
     * Checks if a PropertyDefinition describes a molecule string property.
     */
    public static final Predicate<PropertyDefinition> IS_STRMOL_PROPDEF = and(IS_STRING_PROPDEF,
            not(IS_SCAFFOLD_PROPDEF));

    /**
     * Checks if a PropertyDefinition describes a numerical molecule property.
     */
    public static final Predicate<PropertyDefinition> IS_NUMMOL_PROPDEF = and(not(IS_STRING_PROPDEF),
            not(IS_SCAFFOLD_PROPDEF));

}
