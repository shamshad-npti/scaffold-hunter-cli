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

import com.google.common.base.Preconditions;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * @author Dominic Sacré
 * 
 */
public class Session extends DbObjectWithProperties {
    private Profile profile;
    private String title;
    private Date lastUsed;
    private Tree tree;
    private String sessionData;

    /**
     * The root Subset
     */
    private Subset subset;

    /**
     * The title property name
     */
    public static final String TITLE_PROPERTY = "title";


    /**
     * default constructor
     */
    public Session() {
    }

    /**
     * @param profile
     * @param title
     * @param lastUsed
     * @param tree
     * @param subset
     * @param sessionData 
     */
    public Session(Profile profile, String title, Date lastUsed, Tree tree, Subset subset, String sessionData) {
        this.profile = Preconditions.checkNotNull(profile);
        this.title = Preconditions.checkNotNull(title);
        this.lastUsed = Preconditions.checkNotNull(lastUsed);
        this.tree = Preconditions.checkNotNull(tree);
        this.subset = subset;
        this.sessionData = sessionData;
    }

    /**
     * @return the profile
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * @param profile
     *            the profile to set
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
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
        String oldTitle = this.title;
        this.title = Preconditions.checkNotNull(title);
        firePropertyChange(TITLE_PROPERTY, oldTitle, title);
    }

    /**
     * @return the lastUsed
     */
    public Date getLastUsed() {
        return lastUsed;
    }

    /**
     * @param lastUsed
     *            the lastUsed to set
     */
    public void setLastUsed(Date lastUsed) {
        this.lastUsed = lastUsed;
    }

    /**
     * @return the subset
     */
    public Subset getSubset() {
        return subset;
    }

    /**
     * @param subset
     *            the subset to set
     */
    public void setSubset(Subset subset) {
        this.subset = subset;
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
     * @return the {@link Dataset} that belongs to this Session
     */
    public Dataset getDataset() {
        return getTree().getDataset();
    }

    /**
     * @param sessionData the sessionData to set
     */
    public void setSessionData(String sessionData) {
        this.sessionData = sessionData;
    }

    /**
     * @return the sessionData
     */
    public String getSessionData() {
        return sessionData;
    }

}
