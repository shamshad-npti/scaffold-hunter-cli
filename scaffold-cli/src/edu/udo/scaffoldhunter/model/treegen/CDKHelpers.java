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

package edu.udo.scaffoldhunter.model.treegen;

import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.layout.StructureDiagramGenerator;
import org.openscience.cdk.silent.AtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class which provides static methods on
 * {@link IAtomContainer}s
 * 
 * @author Philipp Lewe
 * @author Nils Kriege
 * 
 */
public class CDKHelpers {
    private static Logger logger = LoggerFactory.getLogger(CDKHelpers.class);

    /**
     * Returns the largest fragment of a molecule or the input molecule if
     * there is just one fragment. Note that in the first case a new Molecule
     * object is returned and in the second case just a reference to the
     * input molecule. 
     * 
     * @param mol
     *            the {@link IAtomContainer}
     * @return {@link IAtomContainer} containing the largest
     *         fragment of the given molecule
     */
    public static IAtomContainer getLargestFragment(IAtomContainer mol) {
        // Check for multiple molecules in the entry
        // The largest one is selected
        if (!ConnectivityChecker.isConnected(mol)) {
            IAtomContainerSet fragmentSet = ConnectivityChecker.partitionIntoMolecules(mol);
            IAtomContainer largestFragment = new AtomContainer();
            for (int i = 0; i < fragmentSet.getAtomContainerCount(); i++) {
                IAtomContainer fragment = fragmentSet.getAtomContainer(i);
                if (fragment.getAtomCount() > largestFragment.getAtomCount()) {
                    largestFragment = fragment;
                }
            }
            largestFragment.setProperties(mol.getProperties());
            return largestFragment;
        } else {
            return mol;
        }
    }

    /**
     * Checks if a given molecule has valid 2D coordinates
     * 
     * @param mol
     *            the {@link IAtomContainer} to check
     * @return true if the molecules has valid 2D coordinates, false otherwise
     */
    public static boolean hasValid2Dcoordinates(IAtomContainer mol) {
        for (IAtom atom : mol.atoms()) {
            if (atom.getPoint2d() == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Calculates new 2D coordinates for a given molecules.
     * 
     * Note: The given molecule must be connected.
     * 
     * @param mol
     *            the {@link IAtomContainer}
     * @return {@link IAtomContainer} with calculated 2D
     *         coordinates
     */
    public static IAtomContainer calculate2Dcoordinates(IAtomContainer mol) {
        StructureDiagramGenerator sdg = new StructureDiagramGenerator();

        // set possible 2D and 3D coordinates to 0
        for (IAtom atom : mol.atoms()) {
            atom.setPoint3d(null);
        }

        // calculate new 2D coordinates
        sdg.setMolecule((IMolecule)mol);
        try {
            sdg.generateCoordinates();
        } catch (Exception e) {
            logger.warn("Calculation of 2D coordinates failed", e);
        }

        return sdg.getMolecule();
    }
        
}
