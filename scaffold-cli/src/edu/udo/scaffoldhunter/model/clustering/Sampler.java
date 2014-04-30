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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * This is the interface for a sampler. A sampler selects some data objects from
 * set with a cetain strategy.
 * 
 * @author Till Schäfer
 * 
 * @param <S>
 *            the concrete Structure
 * 
 */
public interface Sampler<S extends Structure> {
    /**
     * Derives a sample from structures. Using concrete implementations of
     * {@link List} and {@link Set} because of performance reasons.
     * 
     * @param structures
     *            the {@link HierarchicalClusterNode}s from which should be
     *            sampled
     * @return the sample
     */
    public HashSet<HierarchicalClusterNode<S>> getSample(ArrayList<HierarchicalClusterNode<S>> structures);
}
