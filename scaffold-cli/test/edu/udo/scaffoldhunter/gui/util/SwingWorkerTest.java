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

/* 
 * $Id: SwingWorkerTest.java,v 1.4 2008-07-25 19:32:28 idk Exp $
 * 
 * Copyright � 2005 Sun Microsystems, Inc. All rights
 * reserved. Use is subject to license terms.
 */
package edu.udo.scaffoldhunter.gui.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import junit.framework.TestCase;
import edu.udo.scaffoldhunter.gui.util.SwingWorker.StateValue;

/**
 * copy of the original swingworker test, slightly adjusted to match the swingworker modifications
 * @author Thorsten Flügel
 */
public class SwingWorkerTest extends TestCase {

    private final static int TIME_OUT = 30;
    private final static TimeUnit TIME_OUT_UNIT = TimeUnit.SECONDS;
    
    /**
     * is to be run on a worker thread.
     * @throws Exception
     */
    public final void testdoInBackground() throws Exception {
        SwingWorker<Thread,?> test = new SwingWorker<Thread, Object>() {
            @Override
            protected Thread doInBackground() throws Exception {
                return Thread.currentThread();
            }
        };
        test.execute();
        Thread result = test.get(TIME_OUT, TIME_OUT_UNIT);
        assertNotNull(result);
        assertNotSame(Thread.currentThread(), result);
    }

    /**
     * {@code process} gets everything from {@code publish}
     * should be executed on the EDT
     * @throws Exception
     */
    public final void testPublishAndProcess() throws Exception {
        final Exchanger<List<Integer>> listExchanger = 
            new Exchanger<List<Integer>>();
        final Exchanger<Boolean> boolExchanger = 
            new Exchanger<Boolean>();
        SwingWorker<List<Integer>,Integer> test = 
            new SwingWorker<List<Integer>, Integer>() {
                List<Integer> receivedArgs = 
                    Collections.synchronizedList(new ArrayList<Integer>());
                Boolean isOnEDT = Boolean.TRUE;
                final int NUMBERS = 100;
                @Override
                protected List<Integer> doInBackground() throws Exception {
                    List<Integer> ret = 
                        Collections.synchronizedList(
                            new ArrayList<Integer>(NUMBERS));
                    for (int i = 0; i < NUMBERS; i++) {
                        publish(i);
                        ret.add(i);
                    }
                    return ret;
                }
                @Override
                protected void process(List<Integer> args) {
                    for(Integer i : args) {
                        receivedArgs.add(i);
                    }
                    isOnEDT = isOnEDT && SwingUtilities.isEventDispatchThread();
                    if (receivedArgs.size() == NUMBERS) {
                        try {
                            boolExchanger.exchange(isOnEDT);
                            listExchanger.exchange(receivedArgs);
                        } catch (InterruptedException ignore) {
                            ignore.printStackTrace();
                        }
                    }
                }
        };
        test.execute();
        assertTrue(boolExchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
        assertEquals(test.get(TIME_OUT, TIME_OUT_UNIT), 
            listExchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
    }

    /**
     * done is executed on the EDT
     * receives the return value from doInBackground using get()
     * @throws Exception
     */
    public final void testDone() throws Exception {
        final String testString  = "test"; 
        final Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
        SwingWorker<?,?> test = new SwingWorker<String, Object>() {
            @Override
            protected String doInBackground() throws Exception {
                return testString;
            }
            @Override
            protected void done() {
                try {
                    exchanger.exchange(
                        testString == get()
                        && SwingUtilities.isEventDispatchThread());
                } catch (Exception ignore) {
                }
            }
        };
        test.execute();
        assertTrue(exchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
    }

    /**
     * PropertyChangeListener should be notified on the EDT only
     * @throws Exception
     */
    public final void testPropertyChange() throws Exception {
        final Exchanger<Boolean> boolExchanger = 
            new Exchanger<Boolean>();
        final SwingWorker<?,?> test = 
            new SwingWorker<Object, Object>() {
                @Override
                protected Object doInBackground() throws Exception {
                    firePropertyChange("test", null, "test");
                    return null;
                }
            };
        test.addPropertyChangeListener(
            new PropertyChangeListener() {
                boolean isOnEDT = true;

                @Override
                public  void propertyChange(PropertyChangeEvent evt) {
                    isOnEDT &= SwingUtilities.isEventDispatchThread();
                    if ("state".equals(evt.getPropertyName())
                        && StateValue.DONE == evt.getNewValue()) {
                        try {
                            boolExchanger.exchange(isOnEDT);
                        } catch (Exception ignore) {
                            ignore.printStackTrace();
                        }
                    }
                }
            });
        test.execute();
        assertTrue(boolExchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
    }
    
    /**
     * the sequence should be
     * StateValue.STARTED, done, StateValue.DONE
     * @throws Exception
     */
    public final void testWorkFlow() throws Exception {
        final List<Object> goldenSequence = 
            Arrays.asList(new Object[]{StateValue.STARTED, "done", 
                                       StateValue.DONE});
        final List<Object> sequence = 
                    Collections.synchronizedList(new ArrayList<Object>());

        final Exchanger<List<Object>> listExchanger = new Exchanger<List<Object>>();
        
        final SwingWorker<?,?> test = 
            new SwingWorker<Object,Object>() {
                @Override
                protected Object doInBackground() throws Exception {
                    return null;
                }
                @Override
                protected void done() {
                    sequence.add("done");
                }
            };
        test.addPropertyChangeListener(
            new PropertyChangeListener() {
                @Override
                public  void propertyChange(PropertyChangeEvent evt) {
                    if ("state".equals(evt.getPropertyName())) {
                        sequence.add(evt.getNewValue());
                        if (StateValue.DONE == evt.getNewValue()) {
                            try {
                                listExchanger.exchange(sequence);
                            } catch (Exception ignore) {
                                ignore.printStackTrace();
                            }
                        }
                    }
                }
            });
        test.execute();
        assertEquals(goldenSequence, 
                     listExchanger.exchange(null, TIME_OUT, TIME_OUT_UNIT));
    }
    
    /**
     * regression test for 6557137
     * [SwingWorker does not change the state to DONE on exception in done]
     * (issue 5)
     * @throws Exception 
     */
    public final void test6557137() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        SwingWorker<Void, Void> testWorker =
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    return null;
                }
                @Override 
                protected void done() {
                    Thread.currentThread().setUncaughtExceptionHandler(
                        new Thread.UncaughtExceptionHandler() {
                            @Override
                            public void uncaughtException(Thread t, 
                                                          Throwable e) {
                                // do nothing
                            }
                        });
                    throw new RuntimeException("Test exception. Please ignore");
                }
            };
        testWorker.addPropertyChangeListener(
            new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if ("state" == evt.getPropertyName()) {
                        if (SwingWorker.StateValue.DONE == evt.getNewValue()) {
                            latch.countDown();
                        }
                    }
                }
            });

        testWorker.execute();
        if (! latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("failed");
        }
    }
    
    /**
     * test the added restart capability
     * @throws Exception
     */
    public final void testRestart() throws Exception {
        class Int {
            public int n = 0;
        }
        final Int value = new Int();
        final SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                for (int i=0; i<10; ++i) {
                    value.n++;
                }
                return null;
            }
        };
        worker.execute();
        worker.get();
        assertEquals(10, value.n);
        worker.restart();
        worker.get();
        assertEquals(20, value.n);
        worker.restart();
        worker.get();
        assertEquals(30, value.n);
    }
}
