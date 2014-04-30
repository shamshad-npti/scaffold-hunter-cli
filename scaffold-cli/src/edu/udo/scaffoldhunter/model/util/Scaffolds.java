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

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.MappingType;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.util.Orderings;
import edu.udo.scaffoldhunter.view.scaffoldtree.config.ConfigMapping;
import edu.udo.scaffoldhunter.view.scaffoldtree.config.ConfigMapping.Interval;

/**
 * @author Henning Garus
 * 
 */
public class Scaffolds {

    /**
     * Returns an Iterable which can be used to perform a preorder iteration
     * over the scaffold subtree rooted at <code>scaffold</code>.
     * 
     * @param scaffold
     *            the root of the iterated subtree
     * @return an iterable which provides an iterator which performs a preorder
     *         iteration of the subtree rooted at <code>scaffold</code>
     * 
     */
    public static Iterable<Scaffold> getSubtreePreorderIterable(Scaffold scaffold) {
        return new SubtreePreorderIterable(scaffold);
    }

    private static class SubtreePreorderIterable implements Iterable<Scaffold> {

        private Scaffold root;

        private SubtreePreorderIterable(Scaffold root) {
            this.root = root;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<Scaffold> iterator() {

            return new Iterator<Scaffold>() {

                Scaffold next = root;
                private Deque<Iterator<Scaffold>> level = next == null ? null : new LinkedList<Iterator<Scaffold>>();

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Scaffold next() {
                    if (next == null)
                        throw new NoSuchElementException();
                    Scaffold ret = next;
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
     * Computes the distribution of the values specified by the property
     * definition of <code>mapping</code>, according to the mapping intervals
     * for each scaffold in the subtree rooted at <code>scaffold</code> and
     * returns them in a map.
     * 
     * @param scaffold
     *            the root of the subtree for which the distributions are
     *            computed
     * @param mapping
     *            the mapping specifying the distribution by its
     *            <code>MappingIntervals</code> and the property on which the
     *            distributions are based by its <code>PropertyDefinition</code>
     * @param propertyDefinition
     *            the property definition defining the property for which the
     *            distribution is computed.
     * @param cumulative
     *            <code>true</code> iff the distribution for a scaffold should
     *            take the scaffold's children into account.
     * @return a map containing the value distribution for each scaffold in the
     *         subtree rooted at <code>scaffold</code>
     */
    public static Map<Scaffold, List<Integer>> getNumValueDistribution(Scaffold scaffold, ConfigMapping mapping,
            PropertyDefinition propertyDefinition, boolean cumulative) {
        if ((propertyDefinition.isStringProperty() || mapping.getMappingType() != MappingType.Interval))
            return null;
        if (propertyDefinition.isScaffoldProperty())
            return null;
        Map<Scaffold, List<Integer>> map = Maps.newHashMap();
        fillMap(map, scaffold, mapping, propertyDefinition, cumulative);
        return map;
    }

    private static int[] fillMap(Map<Scaffold, List<Integer>> map, Scaffold scaffold, ConfigMapping mapping,
            final PropertyDefinition propertyDefinition, boolean cumulative) {
        int[] dist = new int[mapping.getIntervals().size()];
        { // determine Distribution for current Scaffold
            Predicate<Molecule> propertyNotNull = new Predicate<Molecule>() {
                @Override
                public boolean apply(Molecule input) {
                    return input.getNumPropertyValue(propertyDefinition) != null;
                }
            };
            Set<Molecule> current = Sets.filter(scaffold.getMolecules(), propertyNotNull);
            int i = mapping.getIntervals().size() - 1;
            for (Interval interval : Iterables.limit(Lists.reverse(mapping.getIntervals()), mapping.getIntervals()
                    .size() - 1)) {
                Set<Molecule> filtered = Sets.filter(current,
                        new LesserOrEqual(propertyDefinition, interval.getLowerBound()));
                dist[i--] = current.size() - filtered.size();
                current = filtered;
            }
            dist[0] = current.size();
        }
        /*
         * determine distributions for children recursively and add them in the
         * cumulative case
         */
        for (Scaffold child : scaffold.getChildren()) {
            int[] childDistribution = fillMap(map, child, mapping, propertyDefinition, cumulative);
            if (cumulative) {
                for (int i = 0; i < dist.length; ++i)
                    dist[i] += childDistribution[i];
            }
        }
        map.put(scaffold, Ints.asList(dist));
        return dist;
    }

    /**
     * tests x >= y where x is the value of some NumProperty
     */
    private static class LesserOrEqual implements Predicate<Molecule> {

        private final PropertyDefinition propDef;
        private final double y;

        LesserOrEqual(PropertyDefinition propDef, double y) {
            this.propDef = propDef;
            this.y = y;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        @Override
        public boolean apply(Molecule input) {
            Double i = input.getNumPropertyValue(propDef);
            return i <= y;
        }

    }

    /**
     * 
     * @param scaffolds
     *            the scaffolds for which the associated molecules should be
     *            returned
     * @return An iterable over all molecules associated with the specified
     *         scaffolds.
     */
    public static Iterable<Molecule> getMolecules(Iterable<Scaffold> scaffolds) {
        Iterable<Iterable<Molecule>> molecules = Iterables.transform(scaffolds,
                new Function<Scaffold, Iterable<Molecule>>() {
                    @Override
                    public Iterable<Molecule> apply(Scaffold input) {
                        return input.getMolecules();
                    }
                });
        return Iterables.concat(molecules);
    }

    /**
     * Sort the children of each scaffold in the subtree rooted at
     * <code>scaffold</code> according to <code>scaffoldComparator</code>.
     * <p>
     * The tree is traversed bottom-up during sorting.
     * 
     * @param scaffold
     *            the root of the subtree, which will be sorted
     * @param scaffoldComparator
     *            the comparator used for sorting
     */
    public static void sort(Scaffold scaffold, Comparator<? super Scaffold> scaffoldComparator) {
        for (Scaffold s : scaffold.getChildren()) {
            sort(s, scaffoldComparator);
        }
        Collections.sort(scaffold.getChildren(), scaffoldComparator);
    }

    /**
     * 
     * @param scaffold
     *            a scaffold
     * @return the root of <code>scaffold</code>'s Scaffold Tree
     */
    public static Scaffold getRoot(Scaffold scaffold) {
        Preconditions.checkNotNull(scaffold);
        Scaffold root = scaffold;
        while (root.getParent() != null)
            root = root.getParent();
        return root;
    }

    /**
     * Return the tree cumulative values for some scaffold property and tree.
     * <p>
     * Tree cumulative means a scaffold is associated with the cumulative value of
     * the subtree rooted at that scaffold.
     * <p>
     * Callers of this method are responsible for loading the necessary
     * properties beforehand.
     * 
     * @param root
     *            the root of the tree for which cumulative values are computed
     * @param propDef
     *            the property definition of the value which should be computed
     * @param accumulation
     *            the accumulation function used to compute cumulative values
     * @return a map containing the cumulative value for each scaffold. If the
     *         cumulative value of a scaffold is not defined (when the property
     *         is not defined for a scaffold and is not defined for any scaffold
     *         in the subtree rooted at that scaffold), then the scaffold will
     *         not be contained in the map.
     */
    public static Map<Scaffold, Double> getTreeCumulativeNumValues(Scaffold root, PropertyDefinition propDef,
            AccumulationFunction accumulation) {
        Map<Scaffold, Double> accumulatedValues = Maps.newHashMap();
        List<Scaffold> scaffolds = Orderings.SCAFFOLDS_BY_HIERARCHY_LEVEL.reverse().sortedCopy(
                getSubtreePreorderIterable(root));
        for (Scaffold scaf : scaffolds) {
            if (scaf.getChildren().isEmpty()) {
                Double val = scaf.getNumPropertyValue(propDef);
                if (val != null) {
                    accumulatedValues.put(scaf, val);
                }
            } else {
                List<Double> values = Lists.newArrayListWithCapacity(scaf.getChildren().size() + 1);
                for (Scaffold c : scaf.getChildren()) {
                    Double val = accumulatedValues.get(c);
                    if (val != null) {
                        values.add(val);
                    }
                }
                Double val = scaf.getNumPropertyValue(propDef);
                if (val != null) {
                    values.add(val);
                }
                if (!values.isEmpty()) {

                    switch (accumulation) {
                    case Average: {
                        double sum = 0;
                        for (Double v : values) {
                            sum += v;
                        }
                        double avg = sum / values.size();
                        accumulatedValues.put(scaf, avg);
                        break;
                    }
                    case Maximum: {
                        double max = Ordering.natural().max(values);
                        accumulatedValues.put(scaf, max);
                        break;
                    }
                    case Minimum: {
                        double min = Ordering.natural().min(values);
                        accumulatedValues.put(scaf, min);
                        break;
                    }
//                    case Median: {
//                        Collections.sort(values);
//                        double median = values.get((values.size() / 2) - 1);
//                        accumulatedValues.put(scaf, median);
//                        break;
//                    }
                    case Sum: {
                        double sum = 0;
                        for (Double v : values) {
                            sum += v;
                        }
                        accumulatedValues.put(scaf, sum);
                        break;
                    }
                    }
                }
            }
        }
        return accumulatedValues;
    }
}
