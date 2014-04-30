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

import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * Interface for a hierarchical clustering algorithm
 * 
 * @author Till Schäfer
 * @param <S> the structure (Molecule or Scaffold)
 * 
 */
public interface HierarchicalClustering<S extends Structure> {
    /**
     * starts the clustering
     * 
     * @return the root cluster node
     * @throws ClusteringException 
     */
    public HierarchicalClusterNode<S> calc() throws ClusteringException;
    
    /**
     * adds a {@link ProgressListener}
     * @param listener the {@link ProgressListener}
     */
    public void addProgressListener(ProgressListener<HierarchicalClusterNode<S>> listener);
    
    /**
     * removes a {@link ProgressListener}
     * @param listener the {@link ProgressListener}
     */
    public void removeProgressListener(ProgressListener<HierarchicalClusterNode<S>> listener);
}
