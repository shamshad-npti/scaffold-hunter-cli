/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.subsearchfingerprint;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Collections;
import java.util.Set;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessage;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessageTypes;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.filtering.subsearch.SubsearchConfig;
import edu.udo.scaffoldhunter.model.filtering.subsearch.fingerprint.FingerprintBuilder;
import edu.udo.scaffoldhunter.model.filtering.subsearch.graph.MoleculeGraph;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.datacalculation.AbstractCalcPlugin;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcHelpers;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginResults;

/**
 * Calculation Plugin for the Substructure Search Fingerprint
 * 
 * @author Till Schäfer
 */
@PluginImplementation
public class SubSearchFingerprintCalcPlugin extends AbstractCalcPlugin {
    Set<PropertyDefinition> reservedPropDefs = Sets.newHashSet();

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "Substructure Search Fingerprint";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getID()
     */
    @Override
    public String getID() {
        return "e27b5b04-f8dd-4282-a007-89d8b0d2fb60";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getDescription
     * ()
     */
    @Override
    public String getDescription() {
        return "Calculates a fingerprint to speed uo the substructure search";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#
     * setAvailableProperties(java.util.Set)
     */
    @Override
    public void setAvailableProperties(Set<PropertyDefinition> availableProperties) {
        this.reservedPropDefs = availableProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getSettingsPanel
     * (java.io.Serializable, java.lang.Object)
     */
    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        return new PluginSettingsPanel() {

            @Override
            public Serializable getSettings() {
                return null;
            }

            @Override
            public Object getArguments() {
                return null;
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getResults(
     * java.lang.Object, java.lang.Iterable,
     * edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public CalcPluginResults getResults(Object arguments, final Iterable<IAtomContainer> molecules,
            final MessageListener msgListener) {

        // TODO: support different lengths
        final String propKey = "subStructureSearchFingerprint1024";

        /*
         * TODO: use a diffent propertyType for this fingerprint as it should
         * not be identified by keys OR introduce a central naming convention
         * for keys
         */
        final PropertyDefinition propDef = new PropertyDefinition("SSF 1024",
                "Substructure Fingerprint with 1024 Bits", PropertyType.BitFingerprint, propKey, false, false);

        PropertyDefinition reservedProperty = getReservedProperty(propKey);
        if (reservedProperty != null) {
            msgListener.receiveMessage(new CalcMessage(CalcMessageTypes.PROPERTY_ALREADY_PRESENT, "ALL",
                    reservedProperty));
            return null;
        }

        return new CalcPluginResults() {
            @Override
            public Iterable<IAtomContainer> getMolecules() {
                return Iterables.transform(molecules, new SubSearchFPTransformFunction(propDef, msgListener));
            }

            @Override
            public Set<PropertyDefinition> getCalculatedProperties() {
                return Collections.singleton(propDef);
            }
        };
    }

    /**
     * Returns the property with
     * 
     * @param propKey
     *            the property key
     * @return if the key is already used
     */
    private PropertyDefinition getReservedProperty(String propKey) {
        for (PropertyDefinition propDef : reservedPropDefs) {
            if (propDef.getKey().equals(propKey)) {
                return propDef;
            }
        }
        return null;
    }

    private class SubSearchFPTransformFunction implements Function<IAtomContainer, IAtomContainer> {
        private final Logger logger = LoggerFactory.getLogger(SubSearchFPTransformFunction.class);
        private final PropertyDefinition propDef;
        private final MessageListener msgListener;

        public SubSearchFPTransformFunction(PropertyDefinition propDef, MessageListener msgListener) {
            this.propDef = propDef;
            this.msgListener = msgListener;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.google.common.base.Function#apply(java.lang.Object)
         */
        @Override
        public IAtomContainer apply(IAtomContainer molecule) {
            String string;
            BitSet bitset;

            FingerprintBuilder fpBuilder = SubsearchConfig.getFingerprintBuilder();

            try {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
                CDKHueckelAromaticityDetector.detectAromaticity(molecule);

                bitset = fpBuilder.getFingerprint(new MoleculeGraph(molecule));
                string = CalcHelpers.Bitset2BitFingerprint(bitset, (short) SubsearchConfig.FINGERPRINT_SIZE);

                molecule.getProperties().put(propDef, string);

            } catch (CDKException e) {
                logger.warn("substructure search bitfingerprint could not be generated", e);
                msgListener.receiveMessage(new CalcMessage(CalcMessageTypes.CALCULATION_ERROR, (String) molecule
                        .getProperty(CDKConstants.TITLE)));
            }
            return molecule;
        }
    }
}
