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

package edu.udo.scaffoldhunter.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.gui.SelectionBrowserPane;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;


/**
 * Representation for a selection of {@link Molecule Molecules}.
 * 
 * Note that whenever the selection is changed this may lead to
 * updates in all registered views. Therefore changing the
 * selection by multiple molecules at once should be performed 
 * with the methods {@link #addAll(Iterable)} and 
 * {@link #removeAll(Iterable)} instead of multiple calls of 
 * {@link #add(Molecule)} and {@link #remove(Object)}.
 * 
 * @author Thorsten Flügel
 * @author Dominic Sacré
 * @author Nils Kriege
 *
 */
public class Selection implements Set<Molecule> {

    private static final Logger logger = LoggerFactory.getLogger(Selection.class);
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    
    private Set<Molecule> selection = new LinkedHashSet<Molecule>();
    
    private int selectedIndex = 0;
    
    /**
     * The selection property name
     */
    public static final String SELECTION_PROPERTY = "selection";
    
    /**
     * Adds a property change listener to this selection
     *
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a property change listener from this selection
     *
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    protected final <T> void firePropertyChange(String propertyName, T oldValue, T newValue) {
        if (logger.isTraceEnabled()) {
            logger.trace("propertyName={}, oldValue={}, newValue={}", new Object[]{propertyName, oldValue, newValue});
            PropertyChangeListener[] listeners = propertyChangeSupport.getPropertyChangeListeners(propertyName);
            logger.trace("listeners={}", new Object[]{listeners});
        }

        GenericPropertyChangeEvent<T> ev = new GenericPropertyChangeEvent<T>(this, propertyName, oldValue, newValue);
        propertyChangeSupport.firePropertyChange(ev);
    }

    /**
     * Returns the index of the molecule, which is currently selected in the {@link SelectionBrowserPane}.
     * @return the selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }
    
    /**
     * Sets the index of the molecule, which is currently shown in the {@link SelectionBrowserPane}.
     * @param selectedIndex the new index
     */
    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex;
    }
       
    /**
     * Adds the molecule to this selection
     * @param s the molecule which will be inserted
     * @return true if this selection did not already contain the molecule
     */
    @Override
    public boolean add(Molecule s) {
        Set<Molecule> copy = Sets.newHashSet(selection);
        boolean modified = selection.add(s);
        if (modified) { firePropertyChange(SELECTION_PROPERTY, copy, selection); }
        return modified;
    }
    
    /**
     * Adds all molecules to this selection
     * @param c the collection of molecules which will be inserted
     * @return true if this selection did not already contain the molecules
     */
    @Override
    public boolean addAll(Collection<? extends Molecule> c) {
        Set<Molecule> copy = Sets.newHashSet(selection);
        boolean modified = selection.addAll(c);
        if (modified) { firePropertyChange(SELECTION_PROPERTY, copy, selection); }
        return modified;
    }
    
    /**
     * @param c the collection of molecules which will be inserted
     * @return true if this selection did not already contain the molecules
     * @see #addAll(Collection)
     */
    public boolean addAll(Iterable<? extends Molecule> c) {
        Set<Molecule> copy = Sets.newHashSet(selection);
        boolean modified = false;
        for (Molecule m : c) {
            modified = modified | selection.add(m);
        }
        if (modified) { firePropertyChange(SELECTION_PROPERTY, copy, selection); }
        return modified;
    }

    /**
     * Clears the entire selection
     */
    @Override
    public void clear() {
        Set<Molecule> copy = Sets.newHashSet(selection);
        selection.clear();
        firePropertyChange(SELECTION_PROPERTY, copy, selection);
    }

    /**
     * Removes the molecule from this selection
     * @param s the molecule which will be removed
     * @return true if this selection contained the Molecule
     */
    @Override
    public boolean remove(Object s) {
        Set<Molecule> copy = Sets.newHashSet(selection);
        boolean modified = selection.remove(s);
        if (modified) { firePropertyChange(SELECTION_PROPERTY, copy, selection); }
        return modified;
    }
    
    /**
     * Removes all molecules from this selection
     * @param c the collection of molecules which will be removed
     * @return true if this selection contained any of the molecules
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        Set<Molecule> copy = Sets.newHashSet(selection);
        boolean modified = selection.removeAll(c);
        if (modified) { firePropertyChange(SELECTION_PROPERTY, copy, selection); }
        return modified;
    }
    
    /**
     * @param c the collection of molecules which will be removed
     * @return true if this selection contained any of the molecules
     * @see #removeAll(Collection)
     */
    public boolean removeAll(Iterable<?> c) {
        Set<Molecule> copy = Sets.newHashSet(selection);
        boolean modified = false;
        for (Object o : c) {
            modified = modified | selection.remove(o);
        }
        if (modified) { firePropertyChange(SELECTION_PROPERTY, copy, selection); }
        return modified;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        Set<Molecule> copy = Sets.newHashSet(selection);
        boolean modified = selection.retainAll(c);
        if (modified) { firePropertyChange(SELECTION_PROPERTY, copy, selection); }
        return modified;
    }

    /**
     * @param s the molecule
     * @return true if the selection contains the molecule
     */
    @Override
    public boolean contains(Object s) {
        return selection.contains(s);
    }
    
    /**
     * @param c the collection of molecules
     * @return true if the selection contains all molecules
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return selection.containsAll(c);
    }
    
    /**
     * @return the number of molecules in this selection
     */
    @Override
    public int size() {
        return selection.size();
    }
    
    /**
     * @return an unmodifiable view of the set backing this selection
     */
    public Set<Molecule> getSet() {
        return Collections.unmodifiableSet(selection);
    }

    @Override
    public Iterator<Molecule> iterator() {
        return selection.iterator();
    }

    @Override
    public boolean isEmpty() {
        return selection.isEmpty();
    }

    @Override
    public Object[] toArray() {
        return selection.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return selection.toArray(a);
    }

}
