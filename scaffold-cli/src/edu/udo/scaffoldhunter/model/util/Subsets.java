/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

package edu.udo.scaffoldhunter.model.util;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.SwappingArrayList;

/**
 * @author Dominic Sacré
 * 
 */
public class Subsets {

    /**
     * Returns an Iterable which can be used to perform a preorder iteration
     * over the subset tree rooted at <code>subset</code>.
     * 
     * @param subset
     *            the root of the iterated subtree
     * @return an iterable which provides an iterator which performs a preorder
     *         iteration of the subtree rooted at <code>subset</code>
     * 
     */
    public static Iterable<Subset> getSubsetTreeIterable(Subset subset) {
        return new SubsetTreeIterable(subset);
    }

    private static class SubsetTreeIterable implements Iterable<Subset> {

        private final Subset subset;

        private SubsetTreeIterable(Subset root) {
            this.subset = root;
        }

        @Override
        public Iterator<Subset> iterator() {

            return new Iterator<Subset>() {

                Subset next = subset;
                private Deque<Iterator<Subset>> level = next == null ? null : new LinkedList<Iterator<Subset>>();

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Subset next() {
                    if (next == null)
                        throw new NoSuchElementException();
                    Subset ret = next;
                    if (!next.getChildren().isEmpty()) {
                        level.addFirst(next.getChildren().iterator());
                        next = level.peekFirst().next();
                    } else {
                        while (!level.isEmpty() && !level.peekFirst().hasNext()) {
                            level.pollFirst();
                        }
                        if (level.isEmpty())
                            next = null;
                        else
                            next = level.peekFirst().next();
                    }
                    return ret;
                }

                @Override
                public boolean hasNext() {
                    return next != null;
                }
            };
        }
    }

    /**
     * A functor that returns a subset's title.
     */
    public static Function<Subset, String> getSubsetTitleFunction = new Function<Subset, String>() {
        @Override
        public String apply(Subset input) {
            return input.getTitle();
        }
    };

    /**
     * Creates a random {@link Subset} from a parent {@link Subset}. The
     * {@link Session} is the same as the parents {@link Session}.
     * 
     * @param parent
     *            the {@link Subset} from which the random {@link Subset} should
     *            be created.
     * @param size
     *            the size of the random {@link Subset}
     * 
     * @return the random {@link Subset}
     */
    public static Subset random(Subset parent, int size) {
        Preconditions.checkNotNull(parent);
        Preconditions.checkArgument(parent.size() >= size);
        Preconditions.checkArgument(size >= 0);

        SwappingArrayList<Molecule> randomSelection = new SwappingArrayList<Molecule>();
        randomSelection.addAll(parent.getMolecules());
        Random randomGenerator = new Random();

        int parentSize = parent.size();
        for (int i = 0; i < parentSize - size; i++) {
            randomSelection.swapAndRemove(randomGenerator.nextInt(parentSize - i));
        }
        return new Subset(parent, "random subset", null, parent.getSession(), randomSelection, null);
    }

    /**
     * Creates the union of an arbitrary number of subsets. The {@link Session}
     * is the same as the parents {@link Session}.
     * 
     * @param parent
     *            the parent subset, or null to use the lowest common ancestor
     * @param subsets
     *            the input subsets
     * 
     * @return the newly created subset
     */
    public static Subset union(Subset parent, Iterable<Subset> subsets) {
        Preconditions.checkNotNull(subsets);

        Set<Molecule> molecules = Sets.newHashSet(Iterables.concat(subsets));

        if (parent == null) {
            parent = getLowestCommonAncestor(subsets);
        }

        return new Subset(parent, "union subset", null, parent.getSession(), molecules, null);
    }

    /**
     * Creates the intersection of an arbitrary number of {@link Subsets}. The
     * {@link Session} is the same as the parents {@link Session}.
     * 
     * @param parent
     *            the parent {@link Subset}, or null to use the first input
     *            {@link Subset}
     * @param subsets
     *            the input {@link Subset}s
     * 
     * @return the intersection {@link Subset}
     */
    public static Subset intersection(Subset parent, Iterable<Subset> subsets) {
        Preconditions.checkNotNull(subsets);

        Set<Molecule> molecules = Sets.newHashSet(Iterables.get(subsets, 0));
        for (Subset s : Iterables.skip(subsets, 1)) {
            Set<Molecule> set = Sets.newHashSet(s);
            molecules.retainAll(set);
        }

        if (parent == null) {
            parent = Iterables.get(subsets, 0);
        }

        return new Subset(parent, "intersection subset", null, parent.getSession(), molecules, null);
    }

    /**
     * Creates the difference of one subset and an arbitrary number of other
     * subsets.The {@link Session} is the same as the parents {@link Session}.
     * 
     * @param parent
     *            the parent subset, or null to use the first input subset
     * @param source
     *            the first input subset (from which the others are subtracted)
     * @param subtract
     *            the other input subsets
     * 
     * @return the newly created subset
     */
    public static Subset difference(Subset parent, Subset source, Iterable<Subset> subtract) {
        Preconditions.checkNotNull(source);
        Preconditions.checkNotNull(subtract);

        Set<Molecule> molecules = Sets.newHashSet(source);
        for (Subset s : subtract) {
            Set<Molecule> set = Sets.newHashSet(s);
            molecules.removeAll(set);
        }

        if (parent == null) {
            parent = source;
        }

        return new Subset(parent, "difference subset", null, parent.getSession(), molecules, null);
    }

    /**
     * Creates a subset for each of the given scaffolds. The subset for a
     * scaffold contains the molecules associated with the scaffold and any of
     * its descendants in the scaffold tree. 
     * 
     * @param parent
     *            the parent subset
     * @param title
     *            prefix of the title of the subsets. We add a unique number as a suffix.
     * @param scaffolds
     *            Scaffolds to create a subtree subset for
     * @return the newly created subsets
     */
    public static Iterable<Subset> subsetsFromSubtrees(Subset parent, String title, Iterable<Scaffold> scaffolds) {
        Preconditions.checkNotNull(parent);
        Preconditions.checkNotNull(scaffolds);
        Preconditions.checkNotNull(title);

        List<Subset> subsets = Lists.newLinkedList();
        int counter = 1;
        for (Scaffold scaffold : scaffolds) {
            Iterable<Molecule> molecules = Scaffolds.getMolecules(Scaffolds.getSubtreePreorderIterable(scaffold));
            subsets.add(new Subset(parent, title + " - " + (counter++), null, parent.getSession(), molecules, null));
        }
        
        return subsets;
    }
    
    /**
     * Calculated the lowest common ancestor of all given subsets.
     * 
     * @param subsets
     *            the given subsets.
     * 
     * @return the lowest common ancestor of all the given subsets.
     */
    public static Subset getLowestCommonAncestor(Iterable<Subset> subsets) {
        List<Subset> ancestors = Lists.newArrayList(getAncestors(Iterables.get(subsets, 0)));

        for (Subset s : Iterables.skip(subsets, 1)) {
            ancestors.retainAll(getAncestors(s));
        }

        if (ancestors.size() == 0) {
            throw new IllegalArgumentException("The subsets do not have a common ancestor. "
                    + "They are not in a common subset tree.");
        }

        return ancestors.get(0);
    }

    /**
     * @param subset
     * 
     * @return the list of ancestors of the given subset, starting with (and
     *         including) the subset itself and ending with the root subset.
     */
    public static List<Subset> getAncestors(Subset subset) {
        List<Subset> ancestors = Lists.newArrayList();

        do {
            ancestors.add(subset);
            subset = subset.getParent();
        } while (subset != null);

        return ancestors;
    }
    
}
