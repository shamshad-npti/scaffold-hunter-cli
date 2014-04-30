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

package edu.udo.scaffoldhunter.model.filtering.subsearch.graph;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IPseudoAtom;

/**
 * A {@link Graph} {@link DefaultNode} that has additional linkage to the
 * corresponding {@link IAtom}
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class MoleculeNode extends DefaultNode {

    IAtom atom;

    /**
     * Constructor
     * 
     * @param atom
     *            the linked {@link IAtom}
     * @param index
     *            the index in the {@link Graph}
     */
    MoleculeNode(IAtom atom, int index) {
        super(atom instanceof IPseudoAtom ? ((IPseudoAtom) atom).getLabel() : atom.getSymbol(), index);
        this.atom = atom;
    }

    /**
     * Returns the linked {@link IAtom}
     * 
     * @return the linked {@link IAtom}
     */
    public IAtom getAtom() {
        return atom;
    }
}
