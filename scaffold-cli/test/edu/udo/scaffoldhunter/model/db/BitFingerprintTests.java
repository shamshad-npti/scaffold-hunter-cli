/*
 * Scaffold Hunter
 * Copyright (C) 2012 Till Schäfer
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

package edu.udo.scaffoldhunter.model.db;

import java.util.BitSet;

import junit.framework.Assert;
import edu.udo.scaffoldhunter.model.PropertyType;

/**
 * Test the BitFingerprint methods
 * 
 * @author Till Schäfer
 * 
 */
public class BitFingerprintTests {
    
    /**
     * Tests if the length is correctly converted
     */
    @org.junit.Test
    public void lenghtTest() {
        PropertyDefinition propDef = new PropertyDefinition("Title", "desc", PropertyType.BitFingerprint, "TEST", true,
                false);
        MoleculeStringProperty prop = new MoleculeStringProperty(propDef, "");

        BitSet bits = new BitSet();

        prop.setBitFingerprint(bits, (short) 1);
        invalidateCache(prop);
        Assert.assertEquals(1, prop.getBitFingerprintLength());
        
        prop.setBitFingerprint(bits, (short) 4);
        invalidateCache(prop);
        Assert.assertEquals(4, prop.getBitFingerprintLength());
        
        prop.setBitFingerprint(bits, (short) 526);
        invalidateCache(prop);
        Assert.assertEquals(526, prop.getBitFingerprintLength());
        
        prop.setBitFingerprint(bits, (short) 31999);
        invalidateCache(prop);
        Assert.assertEquals(31999, prop.getBitFingerprintLength());
        
        prop.setBitFingerprint(bits, Short.MAX_VALUE);
        invalidateCache(prop);
        Assert.assertEquals(Short.MAX_VALUE, prop.getBitFingerprintLength());
    }

    /**
     * Tests if the bits are correctly converted
     */
    @org.junit.Test
    public void bitConversionTest() {
        PropertyDefinition propDef = new PropertyDefinition("Title", "desc", PropertyType.BitFingerprint, "TEST", true,
                false);
        MoleculeStringProperty prop = new MoleculeStringProperty(propDef, "");

        BitSet bits;

        bits = new BitSet();
        prop.setBitFingerprint(bits, (short) 1);
        invalidateCache(prop);
        Assert.assertEquals(bits, prop.getBitFingerprintBitSet());
        prop.setBitFingerprint(bits, Short.MAX_VALUE);
        invalidateCache(prop);
        Assert.assertEquals(bits, prop.getBitFingerprintBitSet());

        bits = new BitSet();
        bits.set(0);
        prop.setBitFingerprint(bits, (short) 1);
        invalidateCache(prop);
        Assert.assertEquals(bits, prop.getBitFingerprintBitSet());
        prop.setBitFingerprint(bits, Short.MAX_VALUE);
        invalidateCache(prop);
        Assert.assertEquals(bits, prop.getBitFingerprintBitSet());

        bits = new BitSet();
        bits.set(55);
        prop.setBitFingerprint(bits, (short) 56);
        invalidateCache(prop);
        Assert.assertEquals(bits, prop.getBitFingerprintBitSet());
        prop.setBitFingerprint(bits, Short.MAX_VALUE);
        invalidateCache(prop);
        Assert.assertEquals(bits, prop.getBitFingerprintBitSet());

        bits = new BitSet();
        bits.set(0, Short.MAX_VALUE);
        prop.setBitFingerprint(bits, Short.MAX_VALUE);
        invalidateCache(prop);
        Assert.assertEquals(bits, prop.getBitFingerprintBitSet());
    }

    /**
     * Invalidates the internal caches
     * 
     * @param prop
     */
    private void invalidateCache(MoleculeStringProperty prop) {
        prop.setValue(prop.getValue());
    }
}
