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

package edu.udo.scaffoldhunter.gui.util;

import edu.udo.scaffoldhunter.model.db.DatabaseException;

/**
 * Abstract implementation of {@link DBFunction}, which allows using an argument
 * when creating an anonymous inner class. This version is meant to be used when
 * you do not want to return anything, the return value forwarded by the
 * <code>CallDBManager</code> methods will always be <code>null</code>.
 * <p>
 * To use an argument use the argument type as type variable and call the
 * constructor with the argument. You can then access the argument from
 * <code>call</code> as <code>arg</code>.
 * 
 * @param <T>
 *            type of the function argument
 * 
 * @author Henning Garus
 */
public abstract class VoidUnaryDBFunction<T> implements DBFunction<Void> {

    private final T arg;

    /**
     * Creates a new db function, the constructor argument can be accessed as
     * <code>arg</code> in the <code>call</code> as <code>arg</code>
     * 
     * @param arg
     *            argument passed to <code>call(T)</code>
     */
    public VoidUnaryDBFunction(T arg) {
        this.arg = arg;
    }

    /**
     * 
     * @param arg
     *            argument given to the constructor of this db function
     * @throws DatabaseException
     * 
     * @see #call()
     */
    public abstract void call(T arg) throws DatabaseException;

    @Override
    public final Void call() throws DatabaseException {
        call(arg);
        return null;
    }
}
