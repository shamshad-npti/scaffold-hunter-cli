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

import java.util.HashSet;
import java.util.Set;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class BookmarkFolder extends DbObject {
    private BookmarkFolder parent;
    private Profile profile;
    private String name;
    private Set<Bookmark> bookmarks;

    
    /**
     * default constructor
     */
    public BookmarkFolder() {
        setBookmarks(new HashSet<Bookmark>());
    }

    /**
     * @param parent
     * @param profile
     * @param name
     * @param bookmarks 
     */
    public BookmarkFolder(BookmarkFolder parent, Profile profile, String name, Set<Bookmark> bookmarks) {
        this.parent = parent;
        this.profile = profile;
        this.name = name;
        this.setBookmarks(bookmarks);
    }

    /**
     * @return the parent
     */
    public BookmarkFolder getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(BookmarkFolder parent) {
        this.parent = parent;
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
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param bookmarks the bookmarks to set
     */
    public void setBookmarks(Set<Bookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    /**
     * @return the bookmarks
     */
    public Set<Bookmark> getBookmarks() {
        return bookmarks;
    }
}
