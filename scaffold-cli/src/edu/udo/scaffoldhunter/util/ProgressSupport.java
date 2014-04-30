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

package edu.udo.scaffoldhunter.util;

import javax.swing.event.EventListenerList;


/**
 * @author Thorsten Flügel
 * @param <T>
 */
public class ProgressSupport<T> implements ProgressListener<T> {
    private EventListenerList listeners = new EventListenerList();
    
    @SuppressWarnings("unchecked")
    @Override
    public void setProgressValue(int progress) {
        for (ProgressListener<T> l: listeners.getListeners(ProgressListener.class)) {
            l.setProgressValue(progress);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setProgressBounds(int min, int max) {
        for (ProgressListener<T> l: listeners.getListeners(ProgressListener.class)) {
            l.setProgressBounds(min, max);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void setProgressIndeterminate(boolean indeterminate) {
        for (ProgressListener<T> l: listeners.getListeners(ProgressListener.class)) {
            l.setProgressIndeterminate(indeterminate);
        }
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void finished(T result, boolean cancelled) {
        for (ProgressListener<T> l: listeners.getListeners(ProgressListener.class)) {
            l.finished(result, cancelled);
        }
    }
    
    /**
     * @param l
     */
    public void addProgressListener(ProgressListener<T> l) {
        listeners.add(ProgressListener.class, l);
    }

    /**
     * @param l
     */
    public void removeProgressListener(ProgressListener<T> l) {
        listeners.remove(ProgressListener.class, l);
    }
}
