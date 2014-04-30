/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2014 PG552
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
package edu.udo.scaffoldhunter.model.util;

/**
 * Wraps an {@link Exception} in a {@link RuntimeException} to allow throwing it
 * in some cases where throwing {@link Exception}s is impossible (e.g. throwing
 * inside {@link Runnable}).
 * 
 * @author Till Schäfer
 * 
 */
public class WrapperException extends RuntimeException {
    private static final long serialVersionUID = 4255904499122917390L;

    //FIXME: Use Throwable.cause for this.
    private Exception e;

    /**
     * Constructor
     * 
     * @param e
     *            the wrapped {@link RuntimeException}
     */
    public WrapperException(Exception e) {
        this.e = e;
    }

    /**
     * @return the wrapped {@link RuntimeException}
     */
    public Exception unwrap() {
        return e;
    }
}
