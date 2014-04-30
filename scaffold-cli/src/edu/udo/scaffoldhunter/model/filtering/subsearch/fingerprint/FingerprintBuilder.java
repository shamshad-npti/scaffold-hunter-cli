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

import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;

import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.Graph;

/**
 * Allows common hash key fingerprint creation based on different features
 * classes: path, subtree, rings
 * 
 * @author Till Schäfer
 * 
 */
public class FingerprintBuilder {

    private boolean findPaths = false;
    private boolean findSubtrees = true;
    private boolean findRings = true;
    private int maxPathSize = 8;
    private int maxSubtreeSize = 5;
    private int maxRingSize = 8;

    private int fingerprintSize;

    /**
     * Constructor
     * 
     * @param fingerprintSize
     *            the fingerprint size (should be a power of 2)
     */
    public FingerprintBuilder(int fingerprintSize) {
        this.fingerprintSize = fingerprintSize;
    }

    /**
     * Constructor
     * 
     * @param fingerprintSize
     *            the fingerprint size (should be a power of 2)
     * @param findPaths
     *            enables the path fingerprint features
     * @param findSubtrees
     *            enables the subtree fingerprint features
     * @param findRings
     *            enables the ring fingerprint features
     * @param pathSize
     *            the maximum path size
     * @param subtreeSize
     *            the maximum subtree size
     * @param ringSize
     *            the maximum ring size
     */
    public FingerprintBuilder(int fingerprintSize, boolean findPaths, boolean findSubtrees, boolean findRings,
            int pathSize, int subtreeSize, int ringSize) {
        this.fingerprintSize = fingerprintSize;
        this.findPaths = findPaths;
        this.findSubtrees = findSubtrees;
        this.findRings = findRings;
        this.maxPathSize = pathSize;
        this.maxSubtreeSize = subtreeSize;
        this.maxRingSize = ringSize;
    }

    private void extractFeatures(FeatureStorage<? super String, ?> fs, Graph graph) {
        if (findPaths) {
            PathExtractor pe = new PathExtractor(graph, fs, maxPathSize, PathExtractor.SIMPLE_PATHS);
            pe.extractFeatures();
        }
        if (findSubtrees) {
            SubtreeExtractor se = new SubtreeExtractor(graph, fs, maxSubtreeSize);
            se.extractFeatures();
        }
        if (findRings) {
            RingExtractor re = new RingExtractor(graph, fs, maxRingSize);
            re.extractFeatures();
        }
    }

    /**
     * Calculates the fingerprint as {@link BitSet}
     * 
     * @param graph
     *            the graph to generate the fingerprint from
     * @return the fingerprint
     */
    public BitSet getFingerprint(Graph graph) {
        HashKeyFingerprint fp = new HashKeyFingerprint(fingerprintSize);
        extractFeatures(fp, graph);
        return fp.getResult();
    }

    /**
     * Return the size of the fingerprint
     * 
     * @return the size of the fingerprint
     */
    public int getFingerprintSize() {
        return fingerprintSize;
    }

    /**
     * Calculates the fingerprint as {@link HashSet}
     * 
     * @param graph
     *            the graph to generate the fingerprint from
     * @return the fingerprint
     */
    public HashSet<Integer> getIntHashSet(Graph graph) {
        IntHashSet ihs = new IntHashSet();
        extractFeatures(ihs, graph);
        return ihs.getResult();
    }

    /**
     * Calculates the fingerprint feature count as {@link HashMap}.
     * 
     * Feature -> count of occurences
     * 
     * @param graph
     *            the graph to generate the fingerprint feature count from
     * @return the fingerprint feature count
     */
    public HashMap<String, Integer> getCountHashTable(Graph graph) {
        CountHashTable<String> cht = new CountHashTable<String>();
        extractFeatures(cht, graph);
        return cht.getResult();
    }

    @Override
    public String toString() {
        return (findPaths ? "Paths " + maxPathSize + " " : "") + (findSubtrees ? "Subtrees " + maxSubtreeSize + " " : "")
                + (findRings ? "Rings " + maxRingSize + " " : "");
    }

}
