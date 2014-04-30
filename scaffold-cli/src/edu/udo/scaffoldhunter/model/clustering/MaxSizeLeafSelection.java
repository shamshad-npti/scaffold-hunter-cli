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

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.PriorityQueue;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.clustering.PivotTree.PTreeNode;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Leaf selection strategy: select the leaf with the maximal size
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete Structure
 * 
 */
public class MaxSizeLeafSelection<S extends Structure> extends PriorityQueue<PTreeNode<S>> implements LeafSelection<S> {

    /**
     * Constructor
     */
    public MaxSizeLeafSelection() {
        super(1, new MaxPTreeNodeComparator<S>());
    }

    /**
     * Constructor
     * 
     * @param initialCapacity
     *            the initial capacity
     */
    public MaxSizeLeafSelection(int initialCapacity) {
        super(initialCapacity, new MaxPTreeNodeComparator<S>());
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.LeafSelection#getAllLeafs()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Collection<PTreeNode<S>> getAllLeafs() {
        PTreeNode<S>[] temp = new PTreeNode[0]; 
        return Arrays.asList(toArray(temp));
    }

    /**
     * Comparator based on the size of {@link PTreeNode}s. If the size is equal
     * the hashCode will be compared.
     * 
     * @author Till Schäfer
     * 
     * @param <S>
     */
    private static class MaxPTreeNodeComparator<S extends Structure> implements Comparator<PTreeNode<S>> {
        /*
         * (non-Javadoc)
         * 
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(PTreeNode<S> o1, PTreeNode<S> o2) {
            Preconditions.checkNotNull(o1);
            Preconditions.checkNotNull(o2);

            if (o1.size() == o2.size()) {
                return ((Integer) o2.hashCode()).compareTo(o1.hashCode());
            } else {
                // descending order
                return ((Integer) o2.size()).compareTo(o1.size());
            }
        }
    }
}
