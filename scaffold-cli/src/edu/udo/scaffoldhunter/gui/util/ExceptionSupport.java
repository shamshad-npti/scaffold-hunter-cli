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

import java.util.LinkedList;
import java.util.List;

/**
 * @author Thorsten Flügel
 *
 */
public class ExceptionSupport implements WorkerExceptionListener {
    private List<WorkerExceptionListener> listeners = new LinkedList<WorkerExceptionListener>();    

    /**
     * @param listener
     */
    public void addExceptionListener(WorkerExceptionListener listener) {
        listeners.add(listener);
    }

    /**
     * @param listener
     */
    public void removeExceptionListener(WorkerExceptionListener listener) {
        listeners.remove(listener);
    }

    @Override
    public ExceptionHandlerResult exceptionThrown(Throwable e) {
        for (WorkerExceptionListener listener: listeners) {
            ExceptionHandlerResult r = listener.exceptionThrown(e);
            if (r != ExceptionHandlerResult.NOT_HANDLED) {
                return r;
            }
        }
        return ExceptionHandlerResult.NOT_HANDLED;
    }
}
