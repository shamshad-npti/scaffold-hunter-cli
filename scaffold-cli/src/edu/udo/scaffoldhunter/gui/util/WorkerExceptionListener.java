/*
 * ScaffoldHunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffoldhunter installation for details.
 *
 * This file is part of ScaffoldHunter.
 *
 * ScaffoldHunter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScaffoldHunter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package edu.udo.scaffoldhunter.gui.util;

/**
 * @author Thorsten Flügel
 */
public interface WorkerExceptionListener {
    /**
     * result values that tell whether the exception has been handled and if the
     * worker has to do something to handle the exception in the right way
     * 
     * @author Thorsten Flügel
     */
    public enum ExceptionHandlerResult {
        /**
         * the exception wasn't handled by the listener
         */
        NOT_HANDLED,
        /**
         * the exception was handled by the listener and the worker has to stop
         */
        STOP,
        /**
         * the exception was handled by the listener and the worker should
         * continue if possible
         */
        CONTINUE,
        /**
         * the exception was handled by the listener and the worker should be
         * started again
         */
        RESTART
    }

    /**
     * method that is called if an Exception occurred
     * 
     * @param e
     *            the exception that has to be handled
     * @return the result of the exception handling
     */
    public abstract ExceptionHandlerResult exceptionThrown(Throwable e);
}