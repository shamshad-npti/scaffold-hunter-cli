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

import edu.udo.scaffoldhunter.model.db.NumProperty;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public enum NumComparisonFunction {
    /**
     * The {@link NumProperty} is defined
     */
    IsDefined,
    /**
     * The {@link NumProperty} is not defined
     */
    IsNotDefined,
    /**
     * The number is equal <br>
     * (includes that the {@link Property} is defined)
     */
    IsEqual,
    /**
     * The number is not equal <br>
     * (or the {@link Property} is <b>not</b> defined)
     */
    IsNotEqual,
    /**
     * The number is greater <br>
     * (includes that the {@link Property} is defined)
     */
    IsGreater,
    /**
     * The number is greater or equal <br>
     * (includes that the {@link Property} is defined)
     */
    IsGreaterOrEqual,
    /**
     * The number is less <br>
     * (includes that the {@link Property} is defined)
     */
    IsLess,
    /**
     * The number is less or equal <br>
     * (includes that the {@link Property} is defined)
     */
    IsLessOrEqual;
    
    @Override
    public String toString() {
        return I18n.get("NumComparisonFunction." + name() + ".toString");
    };
    
    /**
     * 
     * @return a textual description of the comparison function
     */
    public String getDescription() {
        return I18n.get("NumComparisonFunction." + name() + ".Description");
    }
}
