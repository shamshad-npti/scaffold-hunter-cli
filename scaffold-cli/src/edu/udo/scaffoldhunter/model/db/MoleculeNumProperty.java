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

package edu.udo.scaffoldhunter.model.db;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class MoleculeNumProperty extends NumProperty {
    private Molecule molecule;
    
    /**
     * default constructor
     */
    public MoleculeNumProperty() {
    }

    /**
     * @param type
     * @param value
     */
    public MoleculeNumProperty(PropertyDefinition type, double value) {
        this.type = type;
        this.value = value;
    }

    /**
     * @return the molecule
     */
    public Molecule getMolecule() {
        return molecule;
    }

    /**
     * @param molecule the molecule to set
     */
    public void setMolecule(Molecule molecule) {
        this.molecule = molecule;
    }
}
