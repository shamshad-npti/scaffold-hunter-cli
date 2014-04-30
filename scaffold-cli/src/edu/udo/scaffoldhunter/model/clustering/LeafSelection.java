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

import java.util.Collection;
import java.util.Queue;

import edu.udo.scaffoldhunter.model.clustering.PivotTree.PTreeNode;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Provides a strategy to select the next leaf for PivotTree construction (e.g.
 * leaf with most elements, minimum depth, ...)
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public interface LeafSelection<S extends Structure> extends Queue<PTreeNode<S>> {
    /**
     * Returns all current leafs
     * 
     * @return all current leafs
     */
    public Collection<PTreeNode<S>> getAllLeafs();
}
