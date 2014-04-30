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
 * when creating an anonymous inner class. The value you return in the call
 * function will be forwarded by <code>CallDBManager</code> methods.
 * <p>
 * To use an argument use the argument type as type variable and call the
 * constructor with the argument. You can then access the argument from
 * <code>call</code> as <code>arg</code>.
 * 
 * @param <T>
 *            type of the return value
 * @param <U>
 *            type of the function argument
 * @author Henning Garus
 */
public abstract class UnaryDBFunction<T, U> implements DBFunction<T> {

    private final U arg;

    /**
     * Creates a new db function, the constructor argument can be accessed as
     * <code>arg</code> in the <code>call</code> as <code>arg</code>
     * 
     * @param arg
     *            argument passed to <code>call(T)</code>
     */
    public UnaryDBFunction(U arg) {
        this.arg = arg;
    }

    /**
     * 
     * @param arg
     *            argument given to the constructor of this db function
     * @return a value which is forwarded by the <code>DBExceptionHandler</code>
     *         methods
     * @throws DatabaseException
     * 
     * @see #call()
     */
    public abstract T call(U arg) throws DatabaseException;

    @Override
    public final T call() throws DatabaseException {
        return call(arg);
    }
}
