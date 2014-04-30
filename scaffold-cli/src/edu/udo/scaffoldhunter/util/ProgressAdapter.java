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

package edu.udo.scaffoldhunter.util;


/**
 * Helper class to stop cluttering up the code with unused methods when the
 * implementer is only interested in some progress events.
 * 
 * @see ProgressListener
 * @param <T> return type of the progress worker
 * 
 * @author Henning Garus
 * 
 */
public class ProgressAdapter<T> implements ProgressListener<T> {

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressValue(int)
     */
    @Override
    public void setProgressValue(int progress) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressBounds(int,
     * int)
     */
    @Override
    public void setProgressBounds(int min, int max) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressIndeterminate
     * (boolean)
     */
    @Override
    public void setProgressIndeterminate(boolean indeterminate) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#finished(java.lang.Object
     * , boolean)
     */
    @Override
    public void finished(T result, boolean cancelled) {

    }

}
