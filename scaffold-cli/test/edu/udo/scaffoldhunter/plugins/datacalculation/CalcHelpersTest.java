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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.BitSet;

import edu.udo.scaffoldhunter.plugins.datacalculation.CalcHelpers;

/**
 * @author Philipp Lewe
 * @author Nils Kriege
 */
public class CalcHelpersTest {

    /**
     * 
     */
    @org.junit.Test(expected = IllegalArgumentException.class)
    public void Bitset2StringTest1() {

        BitSet bitset = new BitSet(4);

        CalcHelpers.Bitset2BitString(bitset, bitset.size() + 1);
    }
    
    /**
     * 
     */
    @org.junit.Test
    public void Bitset2StringSmallRightBitsTest() {
        BitSet bitset = new BitSet(4);
        
        // 0101
        bitset.set(0);
        bitset.set(2);

        String string = CalcHelpers.Bitset2BitString(bitset, 4);
        
        // 0101
        assertTrue(string.charAt(0) == '0');
        assertTrue(string.charAt(1) == '1');
        assertTrue(string.charAt(2) == '0');
        assertTrue(string.charAt(3) == '1');
    }
    
    /**
     * 
     */
    @org.junit.Test
    public void Bitset2StringBigRightBitsTest() {

        BitSet bitset = new BitSet(100);
        
        bitset.set(0);
        bitset.set(2);
        bitset.set(45);
        bitset.set(72);
        bitset.set(99);

        String string = CalcHelpers.Bitset2BitString(bitset, 4);
        
        // reverse order
        string = new StringBuffer(string).reverse().toString();

        // check each position in string
        for (int i = 0; i < string.length(); i++) {
            char s = string.charAt(i);
            boolean b = false;
            if(s == '1') {
                b = true;
            } else if (s == '0'){
                b = false;
            } else {
                fail("string contains other characters than 0 and 1");
            }
            assertTrue(b == bitset.get(i));
        }
    }
    
    /**
     * 
     */
    @org.junit.Test
    public void Bitset2StringLenghtTest() {
        BitSet bitset = new BitSet(5);

        bitset.set(0);
        bitset.set(2);

        String string = CalcHelpers.Bitset2BitString(bitset, 4);
        
        assertTrue(string.length() == 4);
    }
    
    /**
     * Test BitString transformation
     */
    @org.junit.Test
    public void BitStringTransformationTest() {
        String fpString = "000101010010111111000010";
        BitSet bs = CalcHelpers.BitString2BitSet(fpString);
        String fpString2 = CalcHelpers.Bitset2BitString(bs, fpString.length());
        assertTrue(fpString2.equals(fpString));
    }

}
