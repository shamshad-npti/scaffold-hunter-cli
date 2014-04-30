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

package edu.udo.scaffoldhunter.model;

import com.l2fprod.common.propertysheet.Property;

import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Till Schäfer
 * 
 */
public enum PropertyType {
    /**
     * An ordinary numerical {@link Property}
     */
    NumProperty,
    /**
     * An ordinary string {@link Property}
     */
    StringProperty,
    /**
     * A bit fingerprint represented by a string of 1 and 0 (chars)
     */
    BitStringFingerprint,
    /**
     * A bit fingerprint that interprets every bit of a string as a bit. This is
     * logically identical to BitStringFingerprint but has less memory
     * consumption.
     * 
     * Format BitVectorContainer:
     * 
     * A BitVectorContainer is a simple Java {@link String} that has the
     * following format: "<lenght in bits><BitVector>". The <lenght in bits>
     * part is 16 bit (one char) long and is interpreted as a Integer. The
     * BitVector is filled with zeros if it does not align with char size
     */
    BitFingerprint,
    /**
     * A fingerprint that consists of many numerical values.
     * 
     * Format:
     * 
     * A NumericalFingerprint is a simple Java {@link String} with integer
     * values separated by a comma: int,int,...
     */
    NumericalFingerprint,
    /**
     * Void property
     */
    None;

    @Override
    public String toString() {
        return I18n.get("PropertyType." + name() + ".toString");
    };

    /**
     * 
     * @return a textual description of the property type
     */
    public String getDescription() {
        return I18n.get("PropertyType." + name() + ".Description");
    }

    /**
     * @return all types that should be available for import. Currently just
     *         PropertyType.None is filtered out.
     */
    public static PropertyType[] getImportableTypes() {
        PropertyType[] retVal = new PropertyType[values().length - 1];
        int j = 0;
        for (int i = 0; i < values().length; i++) {
            if (values()[i] != PropertyType.None) {
                retVal[j] = values()[i];
                j++;
            }
        }

        return retVal;
    }
}
