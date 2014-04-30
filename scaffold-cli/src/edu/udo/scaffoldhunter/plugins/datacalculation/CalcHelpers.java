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

package edu.udo.scaffoldhunter.plugins.datacalculation;

import java.util.BitSet;

import edu.udo.scaffoldhunter.model.db.MoleculeStringProperty;

/**
 * @author Philipp Lewe
 * 
 */

public class CalcHelpers {

    /**
     * Converts the given {@link BitSet} to a {0,1}-String of the given length.
     * 
     * @param bitset
     *            the {@link BitSet} to convert
     * @param length
     *            the length of the resulting String
     * @return {0,1}-String representation of bitset
     * @throws IllegalArgumentException
     *             if {@link BitSet#size()} smaller than the given length
     */
    public static String Bitset2BitString(BitSet bitset, int length) {
        if (bitset.size() < length) {
            throw new IllegalArgumentException("length if longer than the bitset lenght");
        }

        StringBuilder builder = new StringBuilder(length);

        for (int i = length - 1; i >= 0; i--) {
            if (bitset.get(i)) {
                builder.append("1");
            } else {
                builder.append("0");
            }
        }
        return builder.toString();
    }
    
    /**
     * Converts the given {0,1}-String to a {@link BitSet}.
     * 
     * @param string
     *            the {0,1}-String to convert
     * @return {@link BitSet} representation of the string
     */
    public static BitSet BitString2BitSet(String string) {
        
        char[] chStr = string.toCharArray();
        BitSet bitset = new BitSet(chStr.length);
        
        for (int i=0; i<chStr.length; i++) {
            if (chStr[chStr.length-i-1] == '1') {
                bitset.set(i);
            }
        }
        return bitset;
    }

    /**
     * Converts the given {@link BitSet} to a BitFingerprint of the given length
     * (see PropertyType.BitFingerprint).
     * 
     * @param bitset
     *            the {@link BitSet} to convert
     * @param length
     *            the length of the resulting BitFingerprint (not the same as
     *            the resulting String!)
     * @return the BitFingerprint
     */
    public static String Bitset2BitFingerprint(BitSet bitset, short length) {
        MoleculeStringProperty prop = new MoleculeStringProperty();
        prop.setBitFingerprint(bitset, length);

        return prop.getValue();
    }

}
