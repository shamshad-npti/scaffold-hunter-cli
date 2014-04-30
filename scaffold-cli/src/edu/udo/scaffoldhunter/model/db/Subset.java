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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * @author Dominic Sacré
 * 
 */
public class Subset extends DbObjectWithProperties implements Set<Molecule> {
    private Subset parent;
    private String title;
    private String comment;
    private Session session;
    private Set<Molecule> molecules;
    private Set<Subset> children;
    private Date creationDate;

    /**
     * The title property name
     */
    public static final String TITLE_PROPERTY = "title";
    /**
     * The comment property name
     */
    public static final String COMMENT_PROPERTY = "comment";

    /**
     * default constructor
     */
    /* package */Subset() {
    }

    /**
     * Constructor. The creation date will be the current date.
     * 
     * @param parent
     * @param title
     * @param comment
     * @param session
     * @param molecules
     * @param children
     */
    public Subset(Subset parent, String title, String comment, Session session, Iterable<Molecule> molecules,
            Iterable<Subset> children) {
        Preconditions.checkNotNull(title);
        Preconditions.checkNotNull(session);

        this.parent = parent;
        this.title = title;
        this.comment = comment;
        this.session = session;

        if (molecules == null) {
            this.molecules = Sets.newHashSet();
        } else {
            this.molecules = Sets.newHashSet(molecules);
        }

        if (children == null) {
            this.children = Sets.newHashSet();
        } else {
            this.children = Sets.newHashSet(children);
        }
        
        creationDate = new Date();
    }

    /**
     * @return the database id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the parent
     */
    public Subset getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(Subset parent) {
        this.parent = parent;
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
        String oldComment = this.comment;
        this.comment = comment;
        firePropertyChange(COMMENT_PROPERTY, oldComment, comment);
    }

    /**
     * @return the session
     */
    public Session getSession() {
        return session;
    }

    /**
     * @param session
     *            the session to set
     */
    /* package */void setSession(Session session) {
        this.session = session;
    }

    /**
     * @return the molecules
     */
    public Set<Molecule> getMolecules() {
        // return Collections.unmodifiableSet(molecules);
        return molecules;
    }

    /**
     * @param molecules
     *            the molecules to set
     */
    public void setMolecules(Set<Molecule> molecules) {
        this.molecules = Preconditions.checkNotNull(molecules);
    }

    /**
     * @return the children
     */
    public Set<Subset> getChildren() {
        // return Collections.unmodifiableSet(children);
        return children;
    }

    /**
     * @param children
     *            the children to set
     */
    /* package */void setChildren(Set<Subset> children) {
        this.children = children;
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
     * Adds a child subset.
     * 
     * @param child
     */
    public void addChild(Subset child) {
        Preconditions.checkNotNull(child);
        Preconditions.checkArgument(!children.contains(child));
        children.add(child);
    }

    /**
     * Removes a child subset.
     * 
     * @param child
     */
    public void removeChild(Subset child) {
        Preconditions.checkArgument(children.contains(child));
        children.remove(child);
    }

    @Override
    public Iterator<Molecule> iterator() {
        return molecules.iterator();
    }

    @Override
    public int size() {
        return molecules.size();
    }

    @Override
    public boolean isEmpty() {
        return molecules.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return molecules.contains(o);
    }

    @Override
    public boolean add(Molecule e) {
        throw new UnsupportedOperationException("can't add molecules to an existing subset");
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("can't remove molecules from an existing subset");
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return molecules.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends Molecule> c) {
        throw new UnsupportedOperationException("can't add molecules to an existing subset");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("can't remove molecules from an existing subset");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("can't remove molecules from an existing subset");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("can't remove molecules from an existing subset");
    }

    @Override
    public Object[] toArray() {
        return molecules.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return molecules.toArray(a);
    }

}
