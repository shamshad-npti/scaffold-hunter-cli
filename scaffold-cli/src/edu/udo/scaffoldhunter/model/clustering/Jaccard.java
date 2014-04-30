/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Implementation of the Jaccard coefficient<br>
 * <br>
 * see Paper <br>
 * Aysha Al Khalifa, Maciej Haranczy and John Holliday<br>
 * J. Chem. Inf. Model. 2009, 49, 1193-1201<br>
 * Comparison of Nonbinary Similarity Coefficents for Similarity Searching,
 * Clustering and Compound Selection
 * 
 * @author Till Schäfer
 * @param <S>
 *            molecule / scaffold
 * 
 */
public class Jaccard<S extends Structure> extends Distance<S> {
    private static Logger logger = LoggerFactory.getLogger(Jaccard.class);

    /**
     * Constructor
     * 
     * @param propertyVector
     */
    public Jaccard(Collection<PropertyDefinition> propertyVector) {
        super(propertyVector);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.Distance#isFingerprintDistance()
     */
    @Override
    public PropertyCount acceptedPropertyCount() {
        return Distances.JACCARD.acceptedPropertyCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyType()
     */
    @Override
    public PropertyType acceptedPropertyType() {
        return Distances.JACCARD.acceptedPropertyType();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.Distance#calcDist(edu.udo.
     * scaffoldhunter.model.clustering.HierarchicalClusterNode,
     * edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode)
     */
    @Override
    public double calcDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2)
            throws ClusteringException {
        List<Integer> values1 = getNumericalFingerprintList(node1);
        List<Integer> values2 = getNumericalFingerprintList(node2);

        double sumOfProducts = 0;
        double sumOfSquared1 = 0;
        double sumOfSquared2 = 0;

        Iterator<Integer> it1 = values1.iterator();
        Iterator<Integer> it2 = values2.iterator();

        double similarity;

        /*
         * sum(x_j1*x_j2) / [sum((x_j1)² + sum((x_j2))² - sum(x_j1*x_j2)]
         * 
         * where x_jn is the j-th element of valuesn (n in {1;2})
         */
        while (it1.hasNext() && it2.hasNext()) {
            int value1 = it1.next();
            int value2 = it2.next();

            sumOfProducts += (value1 * value2);
            sumOfSquared1 += Math.pow(value1, 2);
            sumOfSquared2 += Math.pow(value2, 2);
        }

        similarity = sumOfProducts / (sumOfSquared1 + sumOfSquared2 - sumOfProducts);

        logger.debug("Similarity: {}", similarity);
        return 1 - similarity;
    }
}
