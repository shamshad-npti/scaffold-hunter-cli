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

import java.util.Date;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Comment extends DbObject {
    /**
     * is global if tree is NULL
     * global: the {@link Comment} is valid for all {@link Tree}s of the current {@link Dataset}
     */
    private Tree tree;
    private String smiles;
    private boolean molecule;
    /**
     * private=true: the comment is only visible for the user which created the {@link Comment} (see attribute createdBy)
     * private=false: the comment is visible for all users
     */
    private boolean priv;
    private Profile modifiedBy;
    private Date modificationDate;
    private String comment;
    private Dataset dataset;

    
    /**
     * default constructor
     */
    public Comment() { }

    /**
     * @param tree
     * @param smiles
     * @param molecule
     * @param priv
     * @param modifiedBy
     * @param modificationDate
     * @param comment
     * @param dataset 
     */
    public Comment(Tree tree, String smiles, boolean molecule, boolean priv,
            Profile modifiedBy, Date modificationDate, String comment, Dataset dataset) {
        this.tree = tree;
        this.smiles = smiles;
        this.molecule = molecule;
        this.priv = priv;
        this.modifiedBy = modifiedBy;
        this.modificationDate = modificationDate;
        this.comment = comment;
        this.dataset = dataset;
    }

    /**
     * @return the tree. Is null if global.
     */
    public Tree getTree() {
        return tree;
    }

    /**
     * @param tree
     *            the tree to set.
     */
    public void setTree(Tree tree) {
        this.tree = tree;
    }

    /**
     * @return the smiles
     */
    public String getSmiles() {
        return smiles;
    }

    /**
     * @param smiles
     *            the smiles to set
     */
    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    /**
     * @return if the Commend is bound to a Molecule or Scaffold
     */
    public boolean isMolecule() {
        return molecule;
    }

    /**
     * @param molecule
     *            sets if the Commend is bound to a Molecule or Scaffold
     */
    public void setMolecule(boolean molecule) {
        this.molecule = molecule;
    }

    /**
     * @return if the Commend is private or public
     */
    public boolean isPrivate() {
        return priv;
    }

    /**
     * @param priv
     *            sets if the Commend is private or public
     */
    public void setPrivate(boolean priv) {
        this.priv = priv;
    }

    /**
     * @return the profile
     */
    public Profile getModifiedBy() {
        return modifiedBy;
    }

    /**
     * @param profile
     *            the profile to set
     */
    public void setModifiedBy(Profile profile) {
        this.modifiedBy = profile;
    }

    /**
     * @return the creationDate
     */
    public Date getModificationDate() {
        return modificationDate;
    }

    /**
     * @param modificationDate
     *            the creationDate to set
     */
    public void setModificationDate(Date modificationDate) {
        this.modificationDate = modificationDate;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment
     *            the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return if the Comment is global
     */
    public boolean isGlobal() {
        return (tree == null);
    }

    /**
     * Set this as a global Comment 
     */
    public void setGlobal() {
        tree = null;
    }

    /**
     * @return the {@link Dataset}
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset the {@link Dataset} to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }
}
