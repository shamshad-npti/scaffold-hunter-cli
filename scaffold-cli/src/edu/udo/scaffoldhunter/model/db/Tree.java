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
public class Tree extends DbObject {
    private String title;
    private String comment;
    private Dataset dataset;
    private Profile createdBy;
    private Date creationDate;
    private Ruleset ruleset;
    /**
     * whether the Tree is generated with deglycosilate option
     */
    private boolean deglycosilate;

    /**
     * default constructor
     */
    public Tree() {
    }

    /**
     * @param title
     * @param comment
     * @param dataset
     * @param createdBy
     * @param creationDate
     * @param ruleset
     */
    public Tree(String title, String comment, Dataset dataset, Profile createdBy, Date creationDate, Ruleset ruleset) {
        this.title = title;
        this.comment = comment;
        this.dataset = dataset;
        this.createdBy = createdBy;
        this.creationDate = creationDate;
        this.ruleset = ruleset;
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
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset
     *            the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
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
     * @return the ruleset
     */
    public Ruleset getRuleset() {
        return ruleset;
    }

    /**
     * @param ruleset
     *            the ruleset to set
     */
    public void setRuleset(Ruleset ruleset) {
        this.ruleset = ruleset;
    }

    /**
     * @return the whether the Tree is generated with deglycosilate option
     */
    public boolean isDeglycosilate() {
        return deglycosilate;
    }

    /**
     * @param deglycosilate
     *            whether the Tree is generated with deglycosilate option
     */
    public void setDeglycosilate(boolean deglycosilate) {
        this.deglycosilate = deglycosilate;
    }

    @Override
    public String toString() {
        return getTitle();
    }
}
