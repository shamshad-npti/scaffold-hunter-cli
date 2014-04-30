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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Dataset extends DbObject {
    private String title;
    private String comment;
    private Filterset filterset;
    /**
     * the Source Databases
     */
    private String databaseIdentifiers;
    private Date creationDate;
    private Profile createdBy;
    private Set<Tree> trees;
    /**
     * Mapping from PropertyDefinition.key to PropertyDefinition
     */
    private Map<String, PropertyDefinition> propertyDefinitions;


    /**
     * default constructor
     */
    public Dataset() {
        trees = new HashSet<Tree>();
        propertyDefinitions = new HashMap<String, PropertyDefinition>();
    }

    /**
     * @param title
     * @param comment
     * @param filterset
     * @param databaseIdentifiers
     * @param creationDate
     * @param createdBy
     * @param trees
     * @param propertyDefinitions
     */
    public Dataset(String title, String comment, Filterset filterset,
            String databaseIdentifiers, Date creationDate, Profile createdBy,
            Set<Tree> trees,
            Map<String, PropertyDefinition> propertyDefinitions) {
        this.title = title;
        this.comment = comment;
        this.filterset = filterset;
        this.databaseIdentifiers = databaseIdentifiers;
        this.creationDate = creationDate;
        this.createdBy = createdBy;
        this.trees = trees;
        this.propertyDefinitions = propertyDefinitions;
    }

    /**
     * @return the trees
     */
    public Set<Tree> getTrees() {
        return trees;
    }

    /**
     * @param trees
     *            the trees to set
     */
    public void setTrees(Set<Tree> trees) {
        this.trees = trees;
    }

    /**
     * Mapping from PropertyDefinition.key to PropertyDefinition
     * 
     * @return the propertyDefinitions mapping
     */
    public Map<String, PropertyDefinition> getPropertyDefinitions() {
        return propertyDefinitions;
    }

    /**
     * Mapping from PropertyDefinition.key to PropertyDefinition
     * 
     * @param propertyDefinitions
     *            the propertyDefinitions mapping to set
     */
    public void setPropertyDefinitions(
            Map<String, PropertyDefinition> propertyDefinitions) {
        this.propertyDefinitions = propertyDefinitions;
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
     * @return the filterset
     */
    public Filterset getFilterset() {
        return filterset;
    }

    /**
     * @param filterset
     *            the filterset to set
     */
    public void setFilterset(Filterset filterset) {
        this.filterset = filterset;
    }

    /**
     * Textual description of the Databases used to create the Dataset
     * 
     * @return the databaseIdentifiers
     */
    public String getDatabaseIdentifiers() {
        return databaseIdentifiers;
    }

    /**
     * Textual description of the Databases used to create the Dataset
     * 
     * @param databaseIdentifiers
     *            the databaseIdentifiers to set
     */
    public void setDatabaseIdentifiers(String databaseIdentifiers) {
        this.databaseIdentifiers = databaseIdentifiers;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param creationDate
     *            the creationDate to set
     */
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Attention: Lazy Property
     * 
     * @return the createdBy
     */
    Profile getCreatedBy() {
        return createdBy;
    }

    /**
     * @param createdBy
     *            the createdBy to set
     */
    public void setCreatedBy(Profile createdBy) {
        this.createdBy = createdBy;
    }


    @Override
    public String toString() {
        return getTitle();
    }
}
