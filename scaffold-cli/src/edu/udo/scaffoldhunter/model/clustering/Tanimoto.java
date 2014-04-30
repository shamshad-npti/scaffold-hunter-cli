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

package edu.udo.scaffoldhunter.model.clustering;

import java.util.Collection;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Implementation of the Tanimoto coefficient
 * 
 * @author Philipp Kopp
 * @author Till Schäfer
 * @param <S>
 *            molecule / scaffold
 * 
 */
public class Tanimoto<S extends Structure> extends Distance<S> {

    /**
     * Constructor
     * 
     * @param propertyVector
     *            The {@link Property}s which should be used for the calculation
     *            of the distance
     */
    public Tanimoto(Collection<PropertyDefinition> propertyVector) {
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
        return Distances.TANIMOTO.acceptedPropertyCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.clustering.Distance#calcDist(edu.udo.
     * scaffoldhunter.model.clustering.DendrogramModelNode,
     * edu.udo.scaffoldhunter.model.clustering.DendrogramModelNode)
     */
    @Override
    public double calcDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2) {
        PropertyDefinition propDef = propertyVector.iterator().next();
        String string1 = node1.getContent().getStringPropertyValue(propDef);
        String string2 = node2.getContent().getStringPropertyValue(propDef);

        Preconditions.checkNotNull(string1, "string1 is null: Undefined Property");
        Preconditions.checkNotNull(string2, "string2 is null: Undefined Property");
        Preconditions.checkArgument(string1.length() == string2.length(),
                "StringProperties of two nodes must have the same length");

        int intersection = 0;
        int union = 0;
        for (int i = 0; i < string1.length(); i++) {
            if ((string1.charAt(i) == '1') && (string2.charAt(i) == '1')) {
                // if both arguments are equal 1 then the numerator and the
                // denominator are raised by one
                union++;
                intersection++;
            } else if ((string1.charAt(i) == '1') || (string2.charAt(i) == '1')) {
                // if only one of them is equal one, then only the denominator
                // is raised
                union++;
            }
        }

        /*
         * if string1 and string2 have no on bit they are equal
         * 
         * prevents devision by zero
         */
        if (union == 0) {
            return 1;
        }
        return 1 - (intersection / (double) union);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.Distance#acceptedPropertyType()
     */
    @Override
    public PropertyType acceptedPropertyType() {
        return Distances.TANIMOTO.acceptedPropertyType();
    }
}
