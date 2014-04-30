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

package edu.udo.scaffoldhunter.model.util;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomType;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nils Kriege
 */
public class MoleculeConfigurator {
    
    private static Logger logger = LoggerFactory.getLogger(MoleculeConfigurator.class);
    
    /**
     * Prepare molecule, perceive atom types, add implicit hydrogens.
     * @param mol molecule for modification
     * @param resetHybridization force reset of hybridization for reconfiguration
     */
    public static void prepare(IAtomContainer mol, boolean resetHybridization) {
        // reset some properties for reconfiguration
        if (resetHybridization) {
            for (IAtom atom : mol.atoms()) {
                // Hybridization must be reset when molecules are modified, e.g., when 
                // scaffolds are pruned.
                // Removing rings may change hybridization, this affects the number of
                // implicit hydrogens assigned to atoms. The correct number of implicit
                // hydrogens is necessary for correct smiles canonization (see bug #3535292).
                atom.setHybridization((IAtomType.Hybridization) CDKConstants.UNSET);
            }
        }
        // atom types
        try {
            AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(mol);
        } catch (CDKException ex) {
            logger.warn("Error configuring atoms!");
        }
        // add implicit hydrogens
        try {
            CDKHydrogenAdder.getInstance(mol.getBuilder()).addImplicitHydrogens(mol);
        } catch (CDKException ex) {
            logger.warn("Error adding implicit hydrogens!\n\tCDK error message: "+ex.getMessage());
        }
    }


}
