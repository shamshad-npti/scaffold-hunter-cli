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

import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.gui.dialogs.DatabaseErrorDialog;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;

/**
 * Provides static methods, which can be used to wrap function calls which throw
 * {@link DatabaseException}s to handle all <code>DatabaseException</code> in
 * one place.
 * 
 * @author Henning Garus
 * 
 */
public class DBExceptionHandler {

    static Logger logger = LoggerFactory.getLogger(DBExceptionHandler.class);

    /**
     * Calls <code>func</code>, forwards the return value and handles occuring
     * {@link DatabaseException}s.
     * 
     * @param <T>
     *            the return type of the function
     * 
     * @param db
     *            the DB manager
     * @param func
     *            the dbFunctor to be called
     * 
     * @return the value returned by <code>func</code>
     * 
     * @see VoidUnaryDBFunction
     */
    public static <T> T callDBManager(DbManager db, DBFunction<T> func) {
        return callDBManager(db, func, false);
    }

    /**
     * Calls <code>func</code>, forwards the return value and handles occuring
     * {@link DatabaseException}s. Optionally the user can be allowed to ignore
     * exceptions, in which case this function returns <code>null</code>.
     * 
     * @param <T>
     *            the return type of the function
     * 
     * @param db
     *            the DB manager
     * @param func
     *            the dbFunctor to be called
     * @param allowIgnore
     *            whether the user should be allowed to ignore the
     *            {@link DatabaseException}
     * 
     * @return the value returned by <code>func</code>, or <code>null</code>
     * if an exception has been ignored
     * 
     * @see VoidUnaryDBFunction
     */
    public static <T> T callDBManager(DbManager db, DBFunction<T> func, final boolean allowIgnore) {
        boolean reinitialize = false;

        for (;;) {
            try {
                if (reinitialize) {
                    reinitialize = false;
                    logger.warn("reinitializing database connection");
                    db.initializeSessionFactory();
                }

                return func.call();
            }
            catch (final DatabaseException e) {
                logger.error("DatabaseException occured", e);

                DatabaseErrorDialog.Result result;

                if (SwingUtilities.isEventDispatchThread()) {
                    DatabaseErrorDialog dlg = new DatabaseErrorDialog(null, e.getMessage(), allowIgnore);
                    dlg.setVisible(true);
                    result = dlg.getResult();
                }
                else {
                    // use the ugliest way possible to return a result from
                    // invokeAndWait(): a one-element array
                    final DatabaseErrorDialog.Result[] r = new DatabaseErrorDialog.Result[1];
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                DatabaseErrorDialog dlg = new DatabaseErrorDialog(null, e.getMessage(), allowIgnore);
                                dlg.setVisible(true);
                                r[0] = dlg.getResult();
                            }
                        });
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    } catch (InvocationTargetException ex) {
                        ex.printStackTrace();
                    }
                    result = r[0];
                }

                switch (result) {
                case RETRY:
                    logger.warn("retrying after DatabaseException");
                    reinitialize = true;
                    continue;
                case IGNORE:
                    logger.warn("ignoring DatabaseException");
                    return null;
                case QUIT:
                    logger.warn("terminating the application after DatabaseException");
                    System.exit(1);
                }
            }
        }
    }

    /**
     * Calls <code>func</code> in a new thread and handles occuring
     * {@link DatabaseException}s.
     * 
     * @param db
     *            the DB manager
     * @param func
     *            the dbFunctor to be called
     * 
     * @see VoidUnaryDBFunction
     */
    public static void callDBManagerInThread(DbManager db, DBFunction<Void> func) {
        callDBManagerInThread(db, func, false);
    }

    /**
     * Calls <code>func</code> in a new thread and handles occuring
     * {@link DatabaseException}s. Optionally the user can be allowed to ignore
     * exceptions.
     * 
     * @param db
     *            the DB manager
     * @param func
     *            the dbFunctor to be called
     * @param allowIgnore
     *            whether the user should be allowed to ignore the
     *            {@link DatabaseException}
     * 
     * @see VoidUnaryDBFunction
     */
    public static void callDBManagerInThread(final DbManager db, final DBFunction<Void> func, final boolean allowIgnore) {
        new Thread() {
            @Override
            public void run() {
                callDBManager(db, func, allowIgnore);
            }
        }.start();
    }

}
