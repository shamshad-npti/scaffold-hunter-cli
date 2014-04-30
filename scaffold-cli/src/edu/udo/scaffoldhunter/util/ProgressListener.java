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

import java.util.EventListener;

/**
 * @author Thorsten Flügel
 * 
 * @param <T>
 *            the worker's return type
 */
public interface ProgressListener<T> extends EventListener {
    /**
     * Sets the absolute or relative (in indeterminate mode) progress.
     * 
     * @param progress
     *            the progress of the sender's work
     */
    public void setProgressValue(int progress);

    /**
     * Sets the minimum and maximum values. In indeterminate mode, these values
     * are the bounds of the relative progress values.
     * 
     * Will not be called in indeterminate mode if the bounds are unknown.
     * 
     * @param min
     *            minimum value
     * @param max
     *            maximum value
     */
    public void setProgressBounds(int min, int max);

    /**
     * Enables or disables the indeterminate mode.
     * 
     * In indeterminate mode, the listener should display a general
     * notice/animation which indicates that some work is being done if no
     * bounds have been set.
     * 
     * @param indeterminate
     *            true if the sender doesn't known how much work has to be done
     */
    public void setProgressIndeterminate(boolean indeterminate);

    /**
     * The worker has finished.
     * 
     * @param result 
     *            the worker's result 
     * @param cancelled
     *            true if the worker was cancelled while processing the tasks
     */
    public void finished(T result, boolean cancelled);
}
