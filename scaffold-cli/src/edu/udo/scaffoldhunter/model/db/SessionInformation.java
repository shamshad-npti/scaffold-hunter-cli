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

package edu.udo.scaffoldhunter.model.db;

/**
 * @author Till Schäfer
 * 
 */
public class SessionInformation {
    private int sessionId;
    private String title;
    private String treeName;
    private String datasetName;
    private int rootSubsetSize;

    /**
     * @return the {@link Session}.id
     */
    int getSessionId() {
        return sessionId;
    }

    /**
     * @param id
     *            the {@link Session}.id to set
     */
    void setSessionId(int id) {
        this.sessionId = id;
    }

    /**
     * @return the title of the {@link Session}
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title of the {@link Session} to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the treeName of {@link Session}.tree
     */
    public String getTreeName() {
        return treeName;
    }

    /**
     * @param treeName
     *            the treeName of {@link Session}.tree to set
     */
    void setTreeName(String treeName) {
        this.treeName = treeName;
    }

    /**
     * @return the datasetName of {@link Session}.tree.dataset
     */
    public String getDatasetName() {
        return datasetName;
    }

    /**
     * @param datasetName
     *            the datasetName of {@link Session}.tree.dataset to set
     */
    void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    /**
     * @return the size of the root {@link Subset}
     */
    public int getRootSubsetSize() {
        return rootSubsetSize;
    }

    /**
     * @param subsetSize the size of the root {@link Subset} to set
     */
    void setRootSubsetSize(int subsetSize) {
        this.rootSubsetSize = subsetSize;
    }
}
