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

package edu.udo.scaffoldhunter.model.util;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesGenerator;

/**
 * Wrapper class for generating canonical SMILES strings.
 * Should be used instead of the inner CDK smiles generator,
 * to ensure the same configuration is used to generate the
 * smiles strings.
 * 
 * @author Philipp Lewe
 *
 */
public class CanonicalSmilesGenerator {
    private static SmilesGenerator smigen = new SmilesGenerator();
    
    /**
     * @param mol the molecule of type <code>IAtomContainer</code>
     * @param chiral try to generate chiral SMILES
     * @return the canonical smiles string
     */
    public static String createSMILES(IAtomContainer mol, boolean chiral) {
        smigen.setUseAromaticityFlag(true);
        try {
            if (chiral) {
                try {
                    return smigen.createChiralSMILES(mol, new boolean[mol.getBondCount()]);
                } catch (CDKException e) {
                    return smigen.createSMILES(mol);
                }
            } else {
                return smigen.createSMILES(mol);
            }
        } catch (Exception e) {
            // We catch all Exceptions here to be on the save side, since to many things
            // have already gone wrong with SMILES generation
            return "";
        }
    }
}
