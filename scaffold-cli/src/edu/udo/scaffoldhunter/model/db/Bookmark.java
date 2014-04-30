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
public class Bookmark extends DbObject {
    private String title;
    private String smiles;
    private boolean molecule;
    /**
     * A Bookmark is always in a BookmarkFolder
     */
    private BookmarkFolder folder;
    
    
    /**
     * default constructor
     */
    public Bookmark() { }

    /**
     * @param title
     * @param smiles
     * @param molecule
     * @param folder
     */
    public Bookmark(String title, String smiles, boolean molecule,
            BookmarkFolder folder) {
        this.title = title;
        this.smiles = smiles;
        this.molecule = molecule;
        this.folder = folder;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
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
     * @return the molecule
     */
    public boolean isMolecule() {
        return molecule;
    }

    /**
     * @param molecule
     *            set if it is bound to a molecule or scaffold
     */
    public void setMolecule(boolean molecule) {
        this.molecule = molecule;
    }

    /**
     * @return the folder. null if on top level
     */
    public BookmarkFolder getFolder() {
        return folder;
    }

    /**
     * @param folder
     *            the folder to set. null if on top level
     */
    public void setFolder(BookmarkFolder folder) {
        this.folder = folder;
    }
}
