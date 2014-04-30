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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener.ExceptionHandlerResult;
import edu.udo.scaffoldhunter.util.ProgressListener;
import edu.udo.scaffoldhunter.util.ProgressSupport;

/**
 * A worker with added support for progress notifications and exception
 * handling.
 * 
 * Child classes must implement doInBackground and should call setProgressBounds
 * and setIndeterminate before doing the work. finished will be called
 * automatically.
 * 
 * @param <T>
 *            The type of the return value of {@link #doInBackground()} and
 *            {@link #get()}, as in {@link SwingWorker}
 * @param <V>
 *            The type of the intermediate results, used by
 *            {@link #publish(Object...)} and {@link #process(java.util.List)}
 * 
 * @author Thorsten Flügel
 */
public abstract class ProgressWorker<T, V> extends SwingWorker<T, V> implements ProgressListener<T> {
    private static Logger logger = LoggerFactory.getLogger(ProgressWorker.class);

    private ProgressSupport<T> progressListeners = new ProgressSupport<T>();
    private ExceptionSupport exceptionListeners = new ExceptionSupport();

    /**
     * Constructs a progress worker that automatically sets indeterminate mode
     * to true when starting the {@link #doInBackground()} method.
     */
    public ProgressWorker() {
        addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("state".equals(evt.getPropertyName()) && evt.getNewValue() == StateValue.STARTED) {
                    setProgressIndeterminate(true);
                }
            }
        });
    }

    /*
     * TODO: use {@link AccumulativeRunnable} as in {@link
     * SwingWorker#setProgress} since workers might update the progress quite
     * often and the listeners could do some expensive GUI-updates.
     * 
     * @param progress
     */
    @Override
    public void setProgressValue(final int progress) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                progressListeners.setProgressValue(progress);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            submit(r);
        }
    }

    @Override
    public void setProgressBounds(final int min, final int max) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                progressListeners.setProgressBounds(min, max);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            submit(r);
        }
    }

    @Override
    public void setProgressIndeterminate(final boolean indeterminate) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                progressListeners.setProgressIndeterminate(indeterminate);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            submit(r);
        }
    }

    @Override
    public void finished(final T result, final boolean cancelled) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                progressListeners.finished(result, cancelled);
            }
        };
        if (SwingUtilities.isEventDispatchThread()) {
            r.run();
        } else {
            submit(r);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.SwingWorker#done()
     */
    @Override
    protected void done() {
        if (!isRestartingWorker()) {
            super.done();
            boolean finished = true;
            T result = null;
            
            // get the worker's result
            try {
                result = get();
            } catch (InterruptedException e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                logger.debug("Interrupted: {}", sw.toString());
            } catch (CancellationException e) {
                /*
                 * this exception is thrown if the worker is being stopped after
                 * handling an exception inside the worker by calling
                 * handleException
                 */
            } catch (ExecutionException e) {
                // there was an exception inside of the worker, but it wasn't
                // handled by the worker itself.
                ExceptionHandlerResult r = exceptionListeners.exceptionThrown(e.getCause());
                switch (r) {
                case RESTART:
                    // worker has already stopped, but don't tell the listeners
                    // about it ;)
                    finished = false;
                    reset();
                    execute();
                    break;
                case CONTINUE:
                    // nothing to do: worker has finished, can't resume
                    break;
                case STOP:
                    // nothing to do: worker has already stopped
                    break;
                case NOT_HANDLED:
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    logger.error("Unhandled Exception {}", sw.toString());
                    break;
                }
            }
            // inform the listener about the worker's result
            if (finished) {
                finished(result, isCancelled());
            }
        }
    }

    /**
     * Handles an exception. If the exception handlers want the worker to stop
     * or restart, this is done in this method.
     * 
     * This method is intended to be called in the worker's doInBackground
     * method. The worker should continue after catching the exception and
     * calling this method, so that the worker can continue its work if the
     * exception handler returns {@link ExceptionHandlerResult#CONTINUE}.
     * 
     * @param e
     *            the exception that has to be handled
     */
    protected void handleException(Exception e) {
        ExceptionHandlerResult r = exceptionListeners.exceptionThrown(e);
        switch (r) {
        case RESTART:
            restart();
            throw new SwingWorker.StopException();
        case CONTINUE:
            // nothing to do: the worker simply continues after returning from
            // this method
            break;
        case STOP:
            cancel(true);
            // notify listeners before stopping the worker
            finished(null, true);
            throw new SwingWorker.StopException();
        case NOT_HANDLED:
            e.printStackTrace();
            break;
        }
    }

    /**
     * @param listener
     *            will be informed about the progress of the worker.
     */
    public void addProgressListener(ProgressListener<T> listener) {
        progressListeners.addProgressListener(listener);
    }

    /**
     * @param listener
     *            will be no longer be informed about the progress of the
     *            worker.
     */
    public void removeProgressListener(ProgressListener<T> listener) {
        progressListeners.removeProgressListener(listener);
    }

    /**
     * @param listener
     *            will be informed about exceptions that occur in this worker
     */
    public void addExceptionListener(WorkerExceptionListener listener) {
        exceptionListeners.addExceptionListener(listener);
    }

    /**
     * @param listener
     *            will be no longer be informed about exceptions that occur in
     *            this worker
     */
    public void removeExceptionListener(WorkerExceptionListener listener) {
        exceptionListeners.removeExceptionListener(listener);
    }
}
