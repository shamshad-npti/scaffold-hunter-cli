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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.additionalsmiles;

import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessage;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessageTypes;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.treegen.CDKHelpers;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer;
import edu.udo.scaffoldhunter.model.util.CanonicalSmilesGenerator;

/**
 * @author Philipp Lewe
 * 
 */
public class AdditionalSmilesCalcPluginTransformFunction implements Function<IAtomContainer, IAtomContainer> {
    private static Logger logger = LoggerFactory.getLogger(AdditionalSmilesCalcPluginTransformFunction.class);

    AdditionalSmilesCalcPluginArguments arguments;
    List<PropertyDefinition> propDefs;
    MessageListener msgListener;

    /**
     * @param arguments
     *            the plugin arguments
     * @param propDefs
     *            the a order list of property definitions used to save the
     *            calculated properties
     * @param msgListener
     *            the message listener used for reporting of errors
     */
    public AdditionalSmilesCalcPluginTransformFunction(AdditionalSmilesCalcPluginArguments arguments,
            List<PropertyDefinition> propDefs, MessageListener msgListener) {
        this.arguments = arguments;
        this.propDefs = propDefs;
        this.msgListener = msgListener;
    }

    @Override
    public IAtomContainer apply(IAtomContainer input) {

        if (arguments.isCalcLargestFragmentSmiles()) {
            try {
                input.setProperty(propDefs.get(0), getLargestFragmentSmiles(input));
            } catch (CloneNotSupportedException e) {
                logger.warn("clone of molecule failed", e);
                msgListener.receiveMessage(new CalcMessage(CalcMessageTypes.CALCULATION_ERROR, (String) input
                        .getProperty(CDKConstants.TITLE)));
            }
        }

        if (arguments.isCalcLargestFragmentDeglycosilatedSmiles()) {
            try {
                input.setProperty(propDefs.get(1), getLargestFragmentDeglycosilatedSmiles(input));
            } catch (CloneNotSupportedException e) {
                logger.warn("clone of molecule failed", e);
                msgListener.receiveMessage(new CalcMessage(CalcMessageTypes.CALCULATION_ERROR, (String) input
                        .getProperty(CDKConstants.TITLE)));
            }
        }

        if (arguments.isCalcOriginalStructureDeglycosilatedSmiles()) {
            try {
                input.setProperty(propDefs.get(2), getOriginalStructureDeglycosilatedSmiles(input));
            } catch (CloneNotSupportedException e) {
                logger.warn("clone of molecule failed", e);
                msgListener.receiveMessage(new CalcMessage(CalcMessageTypes.CALCULATION_ERROR, (String) input
                        .getProperty(CDKConstants.TITLE)));
            }
        }

        return input;
    }

    private String getLargestFragmentSmiles(IAtomContainer input) throws CloneNotSupportedException {
        IAtomContainer mol = input.clone();
        mol = CDKHelpers.getLargestFragment(mol);

        return CanonicalSmilesGenerator.createSMILES(mol, true);
    }

    private String getLargestFragmentDeglycosilatedSmiles(IAtomContainer input) throws CloneNotSupportedException {
        IAtomContainer mol = input.clone();
        mol = CDKHelpers.getLargestFragment(mol);

        // deglycosilation
        ScaffoldContainer sc = new ScaffoldContainer(mol, false, true);
        return sc.getSMILES();
    }

    private String getOriginalStructureDeglycosilatedSmiles(IAtomContainer input) throws CloneNotSupportedException {
        IAtomContainer mol = input.clone();

        // deglycosilation
        ScaffoldContainer sc = new ScaffoldContainer(mol, false, true);
        return sc.getSMILES();
    }
}
