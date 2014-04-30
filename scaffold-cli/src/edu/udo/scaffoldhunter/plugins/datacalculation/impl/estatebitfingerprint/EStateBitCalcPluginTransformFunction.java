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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.estatebitfingerprint;

import java.util.BitSet;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.fingerprint.EStateFingerprinter;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessage;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessageTypes;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcHelpers;

/**
 * @author Philipp Lewe
 * 
 */
public class EStateBitCalcPluginTransformFunction implements Function<IAtomContainer, IAtomContainer> {
    private static Logger logger = LoggerFactory.getLogger(EStateBitCalcPluginTransformFunction.class);

    private PropertyDefinition propDef;
    MessageListener msgListener;
    private EStateFingerprinter fingerprinter;

    /**
     * @param propDef
     *            the {@link PropertyDefinition} for the property calculated and
     *            stored in the molecule
     * @param msgListener
     *            the message listener used for reporting of errors
     */
    public EStateBitCalcPluginTransformFunction(PropertyDefinition propDef, MessageListener msgListener) {
        this.propDef = propDef;
        this.msgListener = msgListener;
        fingerprinter = new EStateFingerprinter();
    }

    @Override
    public IAtomContainer apply(IAtomContainer molecule) {

        String string;
        BitSet bitset;

        try {
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(molecule);
            CDKHueckelAromaticityDetector.detectAromaticity(molecule);

            bitset = fingerprinter.getFingerprint(molecule);
            string = CalcHelpers.Bitset2BitFingerprint(bitset, (short) 79);

            logger.trace("bitset on bit positions: {}", bitset.toString());
            logger.trace("string fingerprint: {}", string);

            molecule.getProperties().put(propDef, string);

        } catch (CDKException e) {
            logger.warn("estate bitfingerprint could not be generated", e);
            msgListener.receiveMessage(new CalcMessage(CalcMessageTypes.CALCULATION_ERROR, (String) molecule
                    .getProperty(CDKConstants.TITLE)));
        }
        return molecule;
    }
}
