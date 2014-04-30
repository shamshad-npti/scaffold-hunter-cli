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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Interface for a clustering distance function
 * 
 * @author Philipp Kopp und Till Schäfer
 * @param <S>
 *            The Type of Structure ({@link Molecule} or {@link Scaffold})
 */
public abstract class Distance<S extends Structure> {
    protected static Logger logger = LoggerFactory.getLogger(Distance.class);

    protected Collection<PropertyDefinition> propertyVector;

    /**
     * Constructor
     * 
     * @param propertyVector
     *            The {@link Property}s which should be used for the calculation
     *            of the distance, <code>null</code> if no properties are
     *            required
     */
    public Distance(Collection<PropertyDefinition> propertyVector) {
        logger.trace("Entering Constructor");

        /*
         * PropertyCount.NONE
         * 
         * -> propertyVector == null OR propertyVector.size() == 0
         */
        Preconditions.checkArgument(acceptedPropertyCount() != PropertyCount.NONE || propertyVector == null
                || propertyVector.size() == 0, "The propertyVector for PropertyCount.NONE must be null or empty()");
        
        /*
         * PropertyCount.SINGLE
         * 
         * -> propertyVector != null AND propertyVector.size() == 1
         */
        Preconditions.checkArgument(acceptedPropertyCount() != PropertyCount.SINGLE
                || (propertyVector != null && propertyVector.size() == 1),
                "The propertyVector for PropertyCount.SINGLE must have size 1 and cannot be null");
        
        /*
         * PropertyCount.MULTIPLE
         * 
         * -> propertyVector != null AND propertyVector.size() >= 1
         */
        Preconditions.checkArgument(acceptedPropertyCount() != PropertyCount.MULTIPLE
                || (propertyVector != null && propertyVector.size() >= 1),
                "The propertyVector for PropertyCount.SINGLE must have size >= 1 and cannot be null");

        /*
         * check if all propertyDefinitions match the acceptedPropertyType 
         */
        if (propertyVector != null) {
            for (PropertyDefinition propertyDefinition : propertyVector) {
                if (propertyDefinition.getPropertyType() != acceptedPropertyType()) {
                    throw new IllegalArgumentException("The PropertyType is not compatible with the current Distance");
                }
            }
        }
        
        /*
         *  member assignments
         */
        this.propertyVector = propertyVector;
    }

    /**
     * @return The number of accepted properties
     */
    public abstract PropertyCount acceptedPropertyCount();

    /**
     * @return The accepted PropertyType
     */
    public abstract PropertyType acceptedPropertyType();

    /**
     * Calculates the Distance between two {@link HierarchicalClusterNode}s
     * 
     * @param node1
     *            The first node for the distance calculation
     * @param node2
     *            The second node for the distance calculation
     * @return The distance between node1 and node2
     * @throws ClusteringException
     */
    public abstract double calcDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2)
            throws ClusteringException;

    /**
     * Extracts the BitVector from a BitVectorContainer.
     * 
     * @param node
     *            The node witch contains the BitVectorContainer
     * @return The BitVector
     * @throws ClusteringException
     */
    protected BitSet getBitVector(HierarchicalClusterNode<S> node) throws ClusteringException {
        logger.trace("Entering getBitVector");
        assert acceptedPropertyCount() == PropertyCount.SINGLE;

        PropertyDefinition propDef = propertyVector.iterator().next();

        return node.getContent().getBitFingerprintBitSet(propDef);
    }

    /**
     * Extracts the BitVector length from a BitVectorContainer. The
     * BitVectorContainer will be extracted from the DendrogramModelNode
     * regarding to the propertyVector.
     * 
     * @param node
     *            The node which contains the BitVectorContainer
     * @return The Length of the BitVector
     * @throws ClusteringException
     */
    protected int getBitVectorLength(HierarchicalClusterNode<S> node) throws ClusteringException {
        logger.trace("Entering getBitVectorLength");
         assert acceptedPropertyCount() == PropertyCount.SINGLE;

        PropertyDefinition propDef = propertyVector.iterator().next();

        return node.getContent().getBitFingerprintLength(propDef);
    }

    /**
     * Converts a NumericalFingerprint into a {@link List} of {@link Integer}s
     * 
     * @param node
     *            The node which contains the NumericalFingerprint
     * @return The List with all Integer Values
     */
    protected List<Integer> getNumericalFingerprintList(HierarchicalClusterNode<S> node) {
        assert acceptedPropertyType() == PropertyType.NumericalFingerprint;
        assert acceptedPropertyCount() == PropertyCount.SINGLE;

        List<Integer> retVal = new LinkedList<Integer>();

        PropertyDefinition propDef = propertyVector.iterator().next();
        String values = node.getContent().getStringPropertyValue(propDef);

        for (String singleValue : values.split(",")) {
            try {
                retVal.add(Integer.parseInt(singleValue));
            } catch (NumberFormatException e) {
                Writer stacktrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stacktrace));
                logger.error("Conversion of NumericalFingerprint value to Integer failed: {} \n {}", e.getMessage(),
                        stacktrace);
                throw new IllegalArgumentException("Conversion of NumericalFingerprint value to Integer failed", e);
            }
        }
        return retVal;
    }

}
