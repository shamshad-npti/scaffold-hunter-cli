/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 Till Schäfer
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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.BitSet;
import java.util.Collection;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * The Tanimoto Distance. Implemented using a BitVectorContainer (see
 * {@link PropertyType}) which is more memory efficient that saving a 1 or 0 per
 * char.
 * 
 * @author Philipp Kopp
 * @author Till Schäfer
 * @param <S>
 *            The Type of Structure ({@link Molecule} or {@link Scaffold})
 */
public class TanimotoBit<S extends Structure> extends Distance<S> {

    /**
     * Constructor 
     * 
     * @param propertyVector
     *            The {@link Property}s which should be used for the calculation
     *            of the distance
     */
    public TanimotoBit(Collection<PropertyDefinition> propertyVector) {
        super(propertyVector);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.Distance#isBitVectorDistance()
     */
    @Override
    public PropertyCount acceptedPropertyCount() {
        return Distances.TANIMOTOBIT.acceptedPropertyCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.Distance#calcDist(edu.udo.
     * scaffoldhunter.model.clustering.DendrogramModelNode,
     * edu.udo.scaffoldhunter.model.clustering.DendrogramModelNode)
     */
    @Override
    public double calcDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2)
            throws ClusteringException {
        BitSet bits1 = getBitVector(node1);
        BitSet bits2 = getBitVector(node2);
        
        int cardinality1 = bits1.cardinality();
        int cardinality2 = bits2.cardinality();
        
        bits1.and(bits2);
        
        int intersectionCardinality = bits1.cardinality(); 
        int unionCardinality = cardinality1 + cardinality2 - intersectionCardinality;

        if (unionCardinality == 0) {
            // avoid devision by zero
            return 1;
        } else {
            double val = 1.0 - (double) intersectionCardinality / unionCardinality;
            assert !Double.isNaN(val);
            return val;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyType()
     */
    @Override
    public PropertyType acceptedPropertyType() {
        return Distances.TANIMOTOBIT.acceptedPropertyType();
    }
}
