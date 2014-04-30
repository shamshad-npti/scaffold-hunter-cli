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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;

/**
 * @author Dominic Sacré
 *
 */
public class DbObjectWithProperties extends DbObject {

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    /**
     * Adds a change listener for all properties.
     * 
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Adds a change listener for the given property.
     * 
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a change listener for the given property.
     * 
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a change listener for all properties.
     * 
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    /**
     * Fires a property change event.
     * 
     * @param <T>
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected <T> void firePropertyChange(String propertyName, T oldValue, T newValue) {
        GenericPropertyChangeEvent<T> ev = new GenericPropertyChangeEvent<T>(this, propertyName, oldValue, newValue);
        propertyChangeSupport.firePropertyChange(ev);
    }

}
