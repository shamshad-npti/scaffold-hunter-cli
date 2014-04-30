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

package edu.udo.scaffoldhunter.gui.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * @author Thorsten Flügel
 * 
 */
public class ProgressWorkerTest {
    static final int TIMEOUT = 10;
    static final TimeUnit TIMEOUT_UNIT = TimeUnit.SECONDS;

    /**
     * test the progress system
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testProgress() throws InterruptedException, ExecutionException, TimeoutException {
        final Exchanger<?> exchanger = new Exchanger<Object>();
        final int n = 10;
        ProgressWorker<Integer, Void> worker = new ProgressWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                setProgressIndeterminate(false);
                setProgressBounds(1, n);
                Integer k = 0;
                for (int i = 0; i < n; ++i) {
                    setProgressValue(++k);
                }
                return k;
            }
        };
        class Progress {
            public int value = 0;
            public boolean indeterminate = true;
            public int min = Integer.MIN_VALUE;
            public int max = Integer.MIN_VALUE;
            public int result = Integer.MIN_VALUE;
        }
        final Progress lastProgress = new Progress();
        worker.addProgressListener(new ProgressListener<Integer>() {
            @Override
            public void setProgressValue(int progress) {
                lastProgress.value = progress;
            }

            @Override
            public void setProgressIndeterminate(boolean indeterminate) {
                lastProgress.indeterminate = indeterminate;
            }

            @Override
            public void setProgressBounds(int min, int max) {
                lastProgress.min = min;
                lastProgress.max = max;
            }

            @Override
            public void finished(Integer result, boolean cancelled) {
                lastProgress.result = result;
                try {
                    exchanger.exchange(null);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });
        worker.execute();
        // if get is called too early, finished isn't called, because the
        // thread running the worker's done method is stopped by the futuretask.
        // invoking the worker on the Event dispatcher thread solves the
        // problem.
        // most of the times...
        // here we explicitly wait (up to 1 second) for completion.
        exchanger.exchange(null, TIMEOUT, TimeUnit.SECONDS);
        Integer result = worker.get();
        assertEquals(false, lastProgress.indeterminate);
        assertEquals(1, lastProgress.min);
        assertEquals(n, lastProgress.max);
        assertEquals(n, lastProgress.value);
        assertEquals(n, lastProgress.result);
        assertEquals(n, result.intValue());
    }

    /**
     * tests the order of progress listener invocations
     * 
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testProgressListenerOrder() throws InterruptedException, TimeoutException {
        final List<Object> goldenSequence = Arrays.asList(new Object[] { "indeterminate true", "indeterminate false",
                "bounds 0 10", "progress 0", "progress 5", "progress 10", "finished null false" });
        final List<Object> sequence = Collections.synchronizedList(new ArrayList<Object>());
        final Exchanger<List<Object>> listExchanger = new Exchanger<List<Object>>();

        final ProgressWorker<Void, Void> test = new ProgressWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                setProgressIndeterminate(false);
                setProgressBounds(0, 10);
                setProgressValue(0);
                setProgressValue(5);
                setProgressValue(10);
                return null;
            }
        };

        test.addProgressListener(new ProgressListener<Void>() {
            @Override
            public void setProgressValue(int progress) {
                sequence.add("progress " + progress);
            }

            @Override
            public void setProgressIndeterminate(boolean indeterminate) {
                sequence.add("indeterminate " + indeterminate);
            }

            @Override
            public void setProgressBounds(int min, int max) {
                sequence.add("bounds " + min + " " + max);
            }

            @Override
            public void finished(Void result, boolean cancelled) {
                sequence.add("finished " + result + " " + cancelled);
                try {
                    listExchanger.exchange(sequence);
                } catch (Exception ignore) {
                    ignore.printStackTrace();
                }
            }
        });
        test.execute();
        assertEquals(goldenSequence, listExchanger.exchange(null, TIMEOUT, TIMEOUT_UNIT));
    }

    /**
     * tests the exception handling
     * 
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Test
    public void testExceptions() throws InterruptedException, ExecutionException, TimeoutException {
        final Exchanger<?> exchanger = new Exchanger<Object>();
        class Int {
            public int value = 0;
        }
        final int n = 3;
        final Int count = new Int();
        ProgressWorker<Integer, Void> worker = new ProgressWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                Integer k = 0;
                for (int i = 0; i < n; ++i) {
                    try {
                        ++k;
                        ++count.value;
                        throw new Exception();
                    } catch (Exception e) {
                        handleException(e);
                    }
                }
                return k;
            }
        };
        worker.addProgressListener(new ProgressListener<Integer>() {
            @Override
            public void setProgressValue(int progress) {
            }

            @Override
            public void setProgressIndeterminate(boolean indeterminate) {
            }

            @Override
            public void setProgressBounds(int min, int max) {
            }

            @Override
            public void finished(Integer result, boolean cancelled) {
                try {
                    exchanger.exchange(null);
                } catch (InterruptedException ignore) {
                    ignore.printStackTrace();
                }
            }
        });
        WorkerExceptionListener continueHandler = new WorkerExceptionListener() {
            @Override
            public ExceptionHandlerResult exceptionThrown( Throwable e) {
                return ExceptionHandlerResult.CONTINUE;
            }
        };
        WorkerExceptionListener stopHandler = new WorkerExceptionListener() {
            @Override
            public ExceptionHandlerResult exceptionThrown(Throwable e) {
                return ExceptionHandlerResult.STOP;
            }
        };
        WorkerExceptionListener restartHandler = new WorkerExceptionListener() {
            boolean b = false;
            
            @Override
            public ExceptionHandlerResult exceptionThrown(Throwable e) {
                if (b) {
                    return ExceptionHandlerResult.NOT_HANDLED;
                }
                b = true;
                return ExceptionHandlerResult.RESTART;
            }
        };
        Integer result = 0;
        worker.addExceptionListener(continueHandler);
        worker.execute();
        exchanger.exchange(null, TIMEOUT, TimeUnit.SECONDS);
        result = worker.get();
        assertEquals(n, result.intValue());
        worker.removeExceptionListener(continueHandler);

        worker.addExceptionListener(stopHandler);
        worker.restart();
        exchanger.exchange(null, TIMEOUT, TimeUnit.SECONDS);
        try {
            result = worker.get();
        } catch (CancellationException e) {
        } catch (Exception e) {
            fail("Expected an CancellationException");
        }
        assertEquals(n + 1, count.value);
        worker.removeExceptionListener(stopHandler);

        worker.addExceptionListener(restartHandler);
        // replace the standard error output with a dummy, so that the confusing
        // expected exceptions aren't printed. the correct behaviour is tested
        // by checking the counter value
        PrintStream err = System.err;
        System.setErr(new PrintStream(new OutputStream() {
            @Override
            public void write(int b) throws IOException {
            }
        }));
        worker.restart();
        exchanger.exchange(null, TIMEOUT, TimeUnit.SECONDS);
        // restore standard error stream
        System.setErr(err);
        result = worker.get();
        // n+1 is old value, 1 for first exception which causes a restart, then
        // n unhandled exceptions that each cause a (silent) printStackTrace,
        // while the worker continues
        assertEquals(n + 1 + 1 + n, count.value);
        worker.removeExceptionListener(restartHandler);
    }

    /**
     * regression test for 6493680 [SwingWorker notifications might be out of
     * order.] Was part of the original {@link SwingWorkerTest}, moved here
     * since {@link SwingWorker} doesn't have the required progress support
     * 
     * @throws Exception
     */
    @Test
    public final void test6493680() throws Exception {
        class Test {
            private final AtomicInteger lastProgressValue = new AtomicInteger(-1);
            private final Exchanger<Boolean> exchanger = new Exchanger<Boolean>();

            boolean test() throws Exception {
                TestSwingWorker swingWorker = new TestSwingWorker();
                swingWorker.addProgressListener(new ProgressListener<Void>() {
                    @Override
                    public void setProgressValue(int progress) {
                        lastProgressValue.set(progress);
                    }

                    @Override
                    public void setProgressIndeterminate(boolean indeterminate) {
                    }

                    @Override
                    public void setProgressBounds(int min, int max) {
                    }

                    @Override
                    public void finished(Void result, boolean cancelled) {
                    }
                });

                swingWorker.execute();
                return exchanger.exchange(true);
            }

            class TestSwingWorker extends ProgressWorker<Void, Void> {
                @Override
                protected Void doInBackground() throws Exception {
                    for (int i = 0; i <= 100; i++) {
                        Thread.sleep(1);
                        setProgressValue(i);
                    }
                    return null;
                }

                @Override
                protected void done() {
                    boolean isPassed = (lastProgressValue.get() == 100);
                    try {
                        exchanger.exchange(isPassed);
                    } catch (Exception ingore) {
                    }
                }
            }
        }
        /*
         * because timing is involved in this bug we will run the test
         * NUMBER_OF_TRIES times. the tes`t passes if it does not fail once.
         */
        final int NUMBER_OF_TRIES = 50;
        for (int i = 0; i < NUMBER_OF_TRIES; i++) {
            assertTrue((new Test()).test());
        }
    }
}
