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

package edu.udo.scaffoldhunter.model.filtering.subsearch;

import edu.udo.scaffoldhunter.model.filtering.subsearch.fingerprint.FingerprintBuilder;

/**
 * Substructure default config. Nothing than static settings.
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 */
public class SubsearchConfig {
    /**
     * The fingerprint size
     */
    public static final int FINGERPRINT_SIZE = 2048;
    /**
     * The max path size
     * 
     * 0 = disabled
     */
    public static final int MAX_PATH_SIZE = 0;
    /**
     * The max subtree size
     * 
     * 0 = disabled
     */
    public static final int MAX_SUBTREE_SIZE = 5;
    /**
     * The max ring size
     * 
     * 0 = disabled
     */
    public static final int MAX_RING_SIZE = 8;

    /**
     * Returns a {@link FingerprintBuilder} with the default settings
     * 
     * @return the default {@link FingerprintBuilder}
     */
    public static FingerprintBuilder getFingerprintBuilder() {
        return new FingerprintBuilder(FINGERPRINT_SIZE, (MAX_PATH_SIZE > 0), (MAX_SUBTREE_SIZE > 0),
                (MAX_RING_SIZE > 0), MAX_PATH_SIZE, MAX_SUBTREE_SIZE, MAX_RING_SIZE);
    }
}
