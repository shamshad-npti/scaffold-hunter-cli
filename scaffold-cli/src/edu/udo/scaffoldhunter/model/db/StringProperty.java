/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 Till Schäfer
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.BitSet;

import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.PropertyType;

/**
 * @author Till Schäfer
 * 
 */
public abstract class StringProperty extends Property {
    /**
     * The {@link StringProperty}s actual value
     */
    protected String value;
    /**
     * The number of blocks/bytes used for the length of the BitFingerprint
     */
    private static final int lengthbytes = 2;
    /**
     * The size of byte
     */
    private final short sizeofbyte = 8;
    /**
     * Caching the BitSet reduces the conversion from String to BitSets when
     * having multiple access
     */
    private BitSet bitsCache = null;
    /**
     * Caching the length of the fingerprint reduces the base64 decoding
     * overhead
     */
    private short lengthCache = 0;

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(String value) {
        invalidateBitCache();
        this.value = value;
    }

    /**
     * This will generate a {@link PropertyType}.BitFingerprint.
     * 
     * Precondition: Only for PropertyType BitFingerprint!
     * 
     * @param bits
     *            the bits in form of a {@link BitSet}
     * @param length
     *            the length as a {@link BitSet} does not save the exact length.
     */
    public void setBitFingerprint(BitSet bits, short length) {
        checkBitFingerprint();
        Preconditions.checkArgument(length < Math.pow(2, 16), "length exceeds range");
        Preconditions.checkArgument(length > 0, "A length of zero is not supported");

        // + 1 not full block + 2 size block
        byte[] bitFingerprint = new byte[length / sizeofbyte + ((length % sizeofbyte) > 0 ? 1 : 0) + lengthbytes];

        // convert short to two bytes
        lenghtToBitFingerprint(length, bitFingerprint);

        // bits.lenght is faster because its the position of the highest on bit
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                // + 1 because the first char contains the size
                bitFingerprint[i / sizeofbyte + lengthbytes] |= 1 << (i % sizeofbyte);
            }
        }

        value = new String(Base64.encodeBase64String(bitFingerprint));
        lengthCache = length;
        bitsCache = (BitSet) bits.clone();
    }

    /**
     * Returns the length of a BitFingerprint
     * 
     * @return the length
     */
    public short getBitFingerprintLength() {
        checkBitFingerprint();
        if (lengthCache == 0) {
            byte[] bitFingerprint = Base64.decodeBase64(value);
            short length = bitFingerprintToLength(bitFingerprint);
            lengthCache = length;
            return length;
        } else {
            return lengthCache;
        }

    }

    /**
     * Returns a new {@link BitSet} of the BitFingerprint
     * 
     * @return the fingerprint ({@link BitSet})
     */
    public BitSet getBitFingerprintBitSet() {
        checkBitFingerprint();

        if (bitsCache != null) {
            return (BitSet) bitsCache.clone();
        }

        BitSet bits = new BitSet();
        byte[] bitFingerprint;
        bitFingerprint = Base64.decodeBase64(value);

        short length = bitFingerprintToLength(bitFingerprint);

        for (int i = 0; i < length; i++) {
            if ((bitFingerprint[i / sizeofbyte + lengthbytes] & (1 << (i % sizeofbyte))) > 0) {
                bits.set(i);
            }
        }

        bitsCache = (BitSet) bits.clone();
        lengthCache = length;
        return bits;
    }

    /**
     * checks the consistency of the BitFingerprint
     */
    private void checkBitFingerprint() {
        Preconditions.checkArgument(type == null || type.getPropertyType() == PropertyType.BitFingerprint,
                "Invalid call for non BitFingerprint PropertyType");
    }

    /**
     * Extracts the BitFingerprint length from the first two bytes
     * 
     * @param bitFingerprint
     *            the bytes
     * @return the length
     */
    private short bitFingerprintToLength(byte[] bitFingerprint) {
        Preconditions.checkArgument(bitFingerprint.length >= 3);

        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.put(bitFingerprint[0]);
        bb.put(bitFingerprint[1]);
        short length = bb.getShort(0);

        return length;
    }

    /**
     * Set the length in the BitFingerprint (first two bytes)
     * 
     * @param length
     *            the length
     * @param bitFingerprint
     *            the fingerprint
     */
    private void lenghtToBitFingerprint(short length, byte[] bitFingerprint) {
        ByteBuffer bb = ByteBuffer.allocate(2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putShort(length);

        bitFingerprint[0] = bb.get(0);
        bitFingerprint[1] = bb.get(1);
    }

    /**
     * Invalidates the cache for BitFingerprint
     */
    private void invalidateBitCache() {
        bitsCache = null;
        lengthCache = 0;
    }
}
