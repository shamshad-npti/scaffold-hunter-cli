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

package edu.udo.scaffoldhunter.model;

import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.StringProperty;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 *
 */
public enum StringComparisonFunction {
    /**
     * The {@link StringProperty} is defined
     */
    IsDefined,
    /**
     * The {@link StringProperty} is not defined
     */
    IsNotDefined,
    /**
     * The String is equal <br>
     * (includes that the {@link Property} is defined)
     */
    IsEqual,
    /**
     * The String is not equal <br>
     * (or the {@link Property} is <b>not</b> defined)
     */
    IsNotEqual,
    /**
     * The specified String is part of another String <br>
     * (includes that the {@link Property} is defined)
     */
    Contains,
    /**
     * Another String is part of the specified String <br>
     * (includes that the {@link Property} is defined)
     */
    ContainsInverse,
    /**
     * The specified String is not part of another String <br>
     * (or the {@link Property} is <b>not</b> defined)
     */
    ContainsNot,
    /**
     * Another String is not part of the specified String <br>
     * (or the {@link Property} is <b>not</b> defined)
     */
    ContainsNotInverse,
    /**
     * The specified String is the beginning of another String <br>
     * (includes that the {@link Property} is defined)
     */
    Begins,
    /**
     * Another String is the beginning of the specified String <br>
     * (includes that the {@link Property} is defined)
     */
    BeginsInverse, 
    /**
     * The specified String is not the beginning of another String <br>
     * (or the {@link Property} is <b>not</b> defined)
     */
    BeginsNot,
    /**
     * Another String is not the beginning of the specified String <br>
     * (or the {@link Property} is <b>not</b> defined)
     */
    BeginsNotInverse,
    /**
     * The specified String is the end of another String <br>
     * (includes that the {@link Property} is defined)
     */
    Ends, 
    /**
     * Another String is the end of the specified String <br>
     * (includes that the {@link Property} is defined)
     */
    EndsInverse, 
    /**
     * The specified String is not the end of another String <br>
     * (or the {@link Property} is <b>not</b> defined)
     */
    EndsNot,
    /**
     * Another String is not the end of the specified String <br>
     * (or the {@link Property} is <b>not</b> defined)
     */
    EndsNotInverse;
    
    @Override
    public String toString() {
        return I18n.get("StringComparisonFunction." + name() + ".toString");
    };
    
    /**
     * 
     * @return a textual description of the comparison function
     */
    public String getDescription() {
        return I18n.get("StringComparisonFunction." + name() + ".Description");
    }
}
