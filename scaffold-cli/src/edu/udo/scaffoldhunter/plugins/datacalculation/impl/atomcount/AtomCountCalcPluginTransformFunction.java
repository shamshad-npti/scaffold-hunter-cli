/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.atomcount;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;

import com.google.common.base.Function;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Till Schäfer
 * 
 */
public class AtomCountCalcPluginTransformFunction implements Function<IAtomContainer, IAtomContainer> {

    private PropertyDefinition propDefExplicit;
    private MessageListener msgListener;
    private PropertyDefinition propDefImplicit;

    /**
     * @param propDefExplicit
     *            the property to store the result
     * @param propDefImplicit 
     * @param msgListener
     */
    public AtomCountCalcPluginTransformFunction(PropertyDefinition propDefExplicit, PropertyDefinition propDefImplicit, MessageListener msgListener) {
        this.propDefExplicit = propDefExplicit;
        this.propDefImplicit = propDefImplicit;
        this.msgListener = msgListener;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public IAtomContainer apply(IAtomContainer molecule) {
        IAtomContainer molExplicit = new Molecule(molecule);
        AtomContainerManipulator.convertImplicitToExplicitHydrogens(molExplicit);
        molecule.getProperties().put(propDefExplicit, (double) molExplicit.getAtomCount());
        
        IAtomContainer molImplicit = AtomContainerManipulator.removeHydrogens(molecule);
        molecule.getProperties().put(propDefImplicit, (double) molImplicit.getAtomCount());
        
        return molecule;
    }

}
