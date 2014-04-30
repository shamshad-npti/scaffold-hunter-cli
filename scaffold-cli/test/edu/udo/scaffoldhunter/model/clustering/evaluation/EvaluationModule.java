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

package edu.udo.scaffoldhunter.model.clustering.evaluation;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.NumComparisonFunction;
import edu.udo.scaffoldhunter.model.StringComparisonFunction;
import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.PropertyCount;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Filter;
import edu.udo.scaffoldhunter.model.db.Filterset;
import edu.udo.scaffoldhunter.model.db.NumFilter;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.StringFilter;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * A {@link EvaluationModule} runs tests of clusterings and returns a
 * {@link EvaluationResult}
 * 
 * @author Till Schäfer
 */
public abstract class EvaluationModule {
    private static Logger logger = LoggerFactory.getLogger(EvaluationModule.class);
    private static DbManager db;

    /**
     * Returns the matching {@link PropertyDefinition}s for the used
     * {@link Distance}
     * 
     * @param session
     *            the session to determine the {@link Dataset} which should be
     *            used
     * @param distance
     *            the {@link Distance} measure
     * @return the filtered PropertyDefinition Collection
     */
    protected List<PropertyDefinition> getMatchingPropDefs(Session session, Distances distance) {
        Collection<PropertyDefinition> allPropDefs = session.getDataset().getPropertyDefinitions().values();
        LinkedList<PropertyDefinition> usedPropDefs = new LinkedList<PropertyDefinition>();

        for (PropertyDefinition propertyDefinition : allPropDefs) {
            if (!propertyDefinition.isScaffoldProperty()
                    && propertyDefinition.getPropertyType() == distance.acceptedPropertyType()) {
                Filterset filterset = new Filterset(session.getProfile(), "temp", null, true);
                Filter filter;
                if (propertyDefinition.isStringProperty()) {
                    filter = new StringFilter(filterset, propertyDefinition, null, null,
                            StringComparisonFunction.IsDefined);
                } else {
                    filter = new NumFilter(filterset, propertyDefinition, null, 0, NumComparisonFunction.IsDefined);
                }
                filterset.setFilters(Collections.singleton(filter));

                try {
                    if (db.getFilteredSubsetSize(session.getSubset(), filterset) == session.getSubset().size()) {
                        usedPropDefs.add(propertyDefinition);
                    } else {
                        logger.debug("Leaving out propDef {} as it is not defined for all Properties",
                                propertyDefinition.getKey());
                    }
                } catch (DatabaseException e) {
                    throw new EvaluationException("Checking for defined properties failed", e);
                }
                logger.debug("Using PropertyDefinition {}", propertyDefinition.getTitle());
            }
        }

        return usedPropDefs;
    }

    /**
     * Sets the current {@link DbManager}
     * 
     * @param db
     *            the {@link DbManager} to set
     */
    public static void setDbManager(DbManager db) {
        EvaluationModule.db = db;
    }

    /**
     * Runs the measurement for each {@link Subset} in the activated
     * {@link Session}s
     * 
     * @return a {@link EvaluationResult}
     * @throws EvaluationException
     */
    public abstract Collection<EvaluationResult> run() throws EvaluationException;

    /**
     * Generates a List of Lists with PropertyDefinitions. If the distance has
     * PropertyCount.MULTIPLE the return list will only contain one list with
     * all properties. If the distance has PropertyCount.SINGLE it will contain
     * n singleton lists. If the distance has PropertyCount.NONE it will contain
     * a single empty list.
     * 
     * @param distance
     *            the used distance (to determine if it is a
     *            fingerprintDistance)
     * @param session
     *            the session to use
     * @return a List of Lists with PropertyDefinitions (see method description)
     */
    protected List<Collection<PropertyDefinition>> generatePropDefLists(Distances distance, Session session) {
        return generatePropDefLists(distance, session, Integer.MAX_VALUE);
    }

    /**
     * Generates a List of Lists with PropertyDefinitions. If the distance has
     * PropertyCount.MULTIPLE the return list will only contain one list with
     * all properties. If the distance has PropertyCount.SINGLE it will contain
     * n singleton lists. If the distance has PropertyCount.NONE it will contain
     * a single empty list.
     * 
     * @param distance
     *            the used distance (to determine if it is a
     *            fingerprintDistance)
     * @param session
     *            the session to use
     * @param maxUsedSimultaniuousProperties
     *            use only the first maxUsedSimultaniuousProperties if
     *            PropertyCount=MULTIPLE.
     * @return a List of Lists with PropertyDefinitions (see method description)
     */
    protected List<Collection<PropertyDefinition>> generatePropDefLists(Distances distance, Session session,
            int maxUsedSimultaniuousProperties) {
        LinkedList<Collection<PropertyDefinition>> propDefLists = new LinkedList<Collection<PropertyDefinition>>();
        List<PropertyDefinition> usedPropDefs = getMatchingPropDefs(session, distance);
        boolean isSinglePropertyDist = (distance.acceptedPropertyCount() == PropertyCount.SINGLE);
        boolean isMultiplePropertyDist = (distance.acceptedPropertyCount() == PropertyCount.MULTIPLE);
        boolean isNoPropertyDist = (distance.acceptedPropertyCount() == PropertyCount.NONE);

        if (isSinglePropertyDist) {
            for (PropertyDefinition propertyDefinition : usedPropDefs) {
                propDefLists.add(Collections.singleton(propertyDefinition));
            }
        } else if (isMultiplePropertyDist) {
            // use only the first maxUsedSimultaniuousProperties properties
            if (maxUsedSimultaniuousProperties < usedPropDefs.size()) {
                propDefLists
                        .add(usedPropDefs.subList(0, Math.min(maxUsedSimultaniuousProperties, usedPropDefs.size())));
            } else {
                propDefLists.add(usedPropDefs);
            }
        } else if (isNoPropertyDist) {
            // return just one empty list
            propDefLists.add(new LinkedList<PropertyDefinition>());
        } else {
            throw new UnsupportedOperationException("Unsupported PropertyCount");
        }

        return propDefLists;
    }

    /**
     * {@link Iterable} that returns all {@link Subset}s of a root
     * {@link Subset} sorted by {@link Subset#size()} on every level. The root
     * {@link Subset} is included.
     * 
     * @author Till Schäfer
     * 
     */
    protected class SubsetIterable implements Iterable<Subset> {
        private final Subset root;
        private final int maxSubsetSize;

        /**
         * Constructor
         * 
         * @param root
         *            the root {@link Subset}
         * @param maxSubsetSize
         *            the maximum {@link Subset#size()}
         */
        public SubsetIterable(Subset root, int maxSubsetSize) {
            this.root = root;
            this.maxSubsetSize = maxSubsetSize;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Iterable#iterator()
         */
        @Override
        public Iterator<Subset> iterator() {
            return new it(root, maxSubsetSize);
        }

        private class it implements Iterator<Subset> {
            private LinkedList<Subset> orderedSubsets = Lists.newLinkedList();

            /**
             * @param root
             * @param maxSubsetSize
             */
            public it(Subset root, int maxSubsetSize) {
                initOrder(root, maxSubsetSize);
            }

            /**
             * @param root
             * @param maxSubsetSize
             */
            private void initOrder(Subset root, int maxSubsetSize) {
                if (root.size() <= maxSubsetSize) {
                    orderedSubsets.add(root);
                }

                // sort the children by subset size
                LinkedList<Subset> children = Lists.newLinkedList(root.getChildren());
                Collections.sort(children, new Comparator<Subset>() {
                    @Override
                    public int compare(Subset o1, Subset o2) {
                        return o1.size() - o2.size();
                    }
                });

                for (Subset childSubset : children) {
                    initOrder(childSubset, maxSubsetSize);
                }
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#hasNext()
             */
            @Override
            public boolean hasNext() {
                return !orderedSubsets.isEmpty();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#next()
             */
            @Override
            public Subset next() {
                if (orderedSubsets.isEmpty()) {
                    throw new NoSuchElementException();
                }
                return orderedSubsets.removeFirst();
            }

            /*
             * (non-Javadoc)
             * 
             * @see java.util.Iterator#remove()
             */
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

        }
    }
}
