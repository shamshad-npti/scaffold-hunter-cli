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

package edu.udo.scaffoldhunter.plugins.datacalculation;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.AtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessage;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessageTypes;
import edu.udo.scaffoldhunter.model.treegen.CDKHelpers;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer;

/**
 * Transform function, which transforms {@link IAtomContainer}s based on the options
 * stored in the given {@link AbstractCalcPluginArguments}. Currently extracting
 * the largest fragment of a molecule and deglycosylation is supported.
 * 
 * @see CalcPluginTransformOptionPanel
 * 
 * @author Philipp Lewe
 * 
 */
public class CalcPluginMoleculeTransformFunction implements Function<IAtomContainer, IAtomContainer> {
    private static Logger logger = LoggerFactory.getLogger(CalcPluginMoleculeTransformFunction.class);
    AbstractCalcPluginArguments arguments;
    MessageListener msgListener;

    /**
     * 
     * @param arguments
     *            the plugin arguments
     * @param msgListener
     *            the message listener used for reporting of errors
     */
    public CalcPluginMoleculeTransformFunction(AbstractCalcPluginArguments arguments, MessageListener msgListener) {
        this.arguments = arguments;
        this.msgListener = msgListener;
    }

    @Override
    public IAtomContainer apply(IAtomContainer input) {
        IAtomContainer mol;
        try {
            mol = input.clone();
        } catch (CloneNotSupportedException e) {
            logger.warn("could not clone molecule. using new and empty molecule instead", e);
            mol = new AtomContainer();
        }

        if (arguments.isUseLargestFragments()) {

            // notify user
            if (!ConnectivityChecker.isConnected(mol)) {
                logger.debug("using largest fragment of structure");
                msgListener.receiveMessage(new CalcMessage(CalcMessageTypes.LARGEST_FRAGMENT_USED, (String) mol
                        .getProperty(CDKConstants.TITLE)));
            }

            mol = CDKHelpers.getLargestFragment(mol);
        }

        if (arguments.isDeglycosilate()) {
            logger.debug("using deglycosilated structure");
            ScaffoldContainer sc = new ScaffoldContainer(mol, false, true);
            mol = sc.getScaffoldMolecule();
        }

        if (arguments.isRecalculate2Dcoords()) {
            logger.debug("recalculating 2d coordinates");
            mol = CDKHelpers.calculate2Dcoordinates(mol);
        }

        return mol;
    }
}
