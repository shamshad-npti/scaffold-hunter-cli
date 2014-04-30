/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.model.filtering.subsearch.fingerprint;

import java.util.Random;

/**
 * Utility Class for differrent hashing functions
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 */
public class Hashing {

    /**
     * Multiplicative hashing.
     * 
     * Inefficient implementation due to lack of unsigned integer!
     * 
     * @param str
     *            the {@link String} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int multHash(String str, int m) {

        final double A = (Math.sqrt(5) - 1) / 2;
        int k = str.hashCode();
        double hash = ((A * k) % 1) * m;

        return (int) Math.floor(Math.abs(hash));
    }

    /**
     * Java HashMap hashing function.
     * 
     * Note: Works well only if m is power of 2!
     * 
     * @param o
     *            the {@link Object} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int hash(Object o, int m) {
        int k = o.hashCode();
        k ^= (k >>> 20) ^ (k >>> 12);
        k ^= (k >>> 7) ^ (k >>> 4);
        return k & (m - 1);
    }

    /**
     * Hashing function used by CDK.
     * 
     * @param o
     *            the {@link Object} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int cdkHash(Object o, int m) {
        return new java.util.Random(o.hashCode()).nextInt(m);
    }

    /**
     * CRC32 based hashing function.
     * 
     * @param s
     *            the {@link String} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int crc32Hash(String s, int m) {
        long crc32 = crc32(s);
        return Math.abs((int) (crc32 % m));
    }

    /**
     * CRC32+RNG based hashing function.
     * 
     * @param s
     *            the {@link String} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int crc32RandomHash(String s, int m) {
        long crc32 = crc32(s);
        return new Random(crc32).nextInt(m);
    }

    /**
     * djb2 hashing function.
     * 
     * @param s
     *            the {@link String} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int djb2Hash(String s, int m) {
        int hash = 5381;

        for (int i = 0; i < s.length(); i++) {
            hash = ((hash << 5) + hash) + s.charAt(i);
        }

        return Math.abs(hash % m);
    }

    /**
     * crc32 hashing function.
     * 
     * @param s
     *            the {@link String} to hash
     * @return the hash
     */
    public static long crc32(String s) {
        java.util.zip.CRC32 crc32 = new java.util.zip.CRC32();
        crc32.update(s.getBytes());
        return crc32.getValue();
    }

    /**
     * DEK hashing function.
     * 
     * @param s
     *            the {@link String} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int DEKHash(String s, int m) {
        int hash = s.length();

        for (int i = 0; i < s.length(); i++) {
            hash = ((hash << 5) ^ (hash >> 27)) ^ s.charAt(i);
        }

        return Math.abs(hash % m);
    }

    /**
     * ELF hashing function.
     * 
     * @param s
     *            the {@link String} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int ELFHash(String s, int m) {
        long hash = 0;
        long x = 0;

        for (int i = 0; i < s.length(); i++) {
            hash = (hash << 4) + s.charAt(i);

            if ((x = hash & 0xF0000000L) != 0) {
                hash ^= (x >> 24);
            }
            hash &= ~x;
        }

        return Math.abs((int) (hash % m));
    }

    /**
     * SDBM hashing function.
     * 
     * @param s
     *            the {@link String} to hash
     * @param m
     *            the maximal hash value
     * @return the hash
     */
    public static int SDBMHash(String s, int m) {
        int hash = 0;

        for (int i = 0; i < s.length(); i++) {
            hash = s.charAt(i) + (hash << 6) + (hash << 16) - hash;
        }

        return Math.abs(hash % m);
    }

}
