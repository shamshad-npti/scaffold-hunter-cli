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

package edu.udo.scaffoldhunter.model.db;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Banner extends DbObject {
    /**
     * A {@link Banner} is always lokal (see {@link Comment}), so the Tree must
     * always be set
     */
    private Tree tree;
    /**
     * private per user or public for all users
     */
    private boolean priv;
    private Profile createdBy;
    private Structure structure;

    /**
     * default constructor
     */
    public Banner() {
    }

    /**
     * @param tree
     * @param priv
     * @param createdBy
     * @param structure
     */
    public Banner(Tree tree, boolean priv, Profile createdBy, Structure structure) {
        this.tree = tree;
        this.priv = priv;
        this.createdBy = createdBy;
        this.structure = structure;
    }

    /**
     * @return the tree
     */
    public Tree getTree() {
        return tree;
    }

    /**
     * @param tree
     *            the tree to set
     */
    public void setTree(Tree tree) {
        this.tree = tree;
    }

    /**
     * @return if the Banner is private or public
     */
    public boolean isPrivate() {
        return priv;
    }

    /**
     * @param priv
     *            sets if the Banner is private or public
     */
    public void setPrivate(boolean priv) {
        this.priv = priv;
    }

    /**
     * @return the profile
     */
    public Profile getCreatedBy() {
        return createdBy;
    }

    /**
     * @param profile
     *            the profile to set
     */
    public void setCreatedBy(Profile profile) {
        this.createdBy = profile;
    }

    /**
     * @return the scaffold
     */
    public Scaffold getScaffold() {
        if (structure instanceof Scaffold) {
            return (Scaffold) structure;
        } else {
            return null;
        }
    }

    /**
     * Sets the scaffold and erases the molecule
     * 
     * @param scaffold
     *            the scaffold to set
     */
    public void setScaffold(Scaffold scaffold) {
        this.structure = scaffold;
    }

    /**
     * @return the molecule
     */
    public Molecule getMolecule() {
        if (structure instanceof Molecule) {
            return (Molecule) structure;
        } else {
            return null;
        }
    }

    /**
     * Sets the molecule and erases the scaffold and tree
     * 
     * @param molecule
     *            the molecule to set
     */
    public void setMolecule(Molecule molecule) {
        this.structure = molecule;
    }

    /**
     * @return the structure
     */
    public Structure getStructure() {
        return structure;
    }

    /**
     * @param structure the structure to set
     */
    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    /**
     * @return if the Banner is bound to a Molecule or Scaffold
     */
    public boolean isMolecule() {
        return (structure instanceof Molecule);
    }
}
