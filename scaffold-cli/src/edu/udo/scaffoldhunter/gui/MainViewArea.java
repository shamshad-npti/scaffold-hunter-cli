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

package edu.udo.scaffoldhunter.gui;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseEvent;
import java.util.EventListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.view.View;

/**
 * The view area of the Scaffold Hunter main window, containing one or
 * two tabbed panes.
 * 
 * @author Dominic Sacré
 */
public class MainViewArea extends JPanel implements ViewManager.WindowSplitChangeListener {

    private final ViewManager viewManager;
    private final MainWindow window;

    private JSplitPane splitPane = null;
    private final List<MainTabbedPane> tabbedPanes = Lists.newArrayList();

    private final EventListenerList listeners = new EventListenerList();

    /**
     * @param viewManager
     *          the view manager
     * @param window 
     *          the main window
     */
    public MainViewArea(ViewManager viewManager, MainWindow window) {
        super(new BorderLayout());

        this.viewManager = viewManager;
        this.window = window;

        setupListeners();

        // create the first tabbed pane
        tabbedPanes.add(createTabbedPane(0));

        add(tabbedPanes.get(0));
    }

    /**
     * Performs cleanup to ensure that the object can be garbage-collected.
     */
    public void destroy() {
        cleanupListeners();
    }

    private void setupListeners() {
        viewManager.addWindowSplitChangeListener(window, this);

        window.getState().addPropertyChangeListener(MainWindowState.SPLIT_ORIENTATION_PROPERTY,
                new GenericPropertyChangeListener<MainWindowState.SplitOrientation>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<MainWindowState.SplitOrientation> ev) {
                if (ev.getOldValue() != MainWindowState.SplitOrientation.NONE &&
                    ev.getNewValue() != MainWindowState.SplitOrientation.NONE) {
                    windowSplitChanged(true);
                }
            }
        });

        window.getState().addPropertyChangeListener(MainWindowState.ACTIVE_VIEW_POSITION_PROPERTY,
                new GenericPropertyChangeListener<ViewPosition>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<ViewPosition> ev) {
                tabbedPanes.get(ev.getNewValue().getSplit()).setSelectedIndex(ev.getNewValue().getTab());
            }
        });

        // listen for _all_ AWT mouse events. this seems to be the only way to
        // capture events originating from any of the views' child components.
        Toolkit.getDefaultToolkit().addAWTEventListener(awtMouseEventListener, AWTEvent.MOUSE_EVENT_MASK);
    }

    private void cleanupListeners() {
        viewManager.removeWindowSplitChangeListener(window, this);

        Toolkit.getDefaultToolkit().removeAWTEventListener(awtMouseEventListener);
    }


    private AWTEventListener awtMouseEventListener = new AWTEventListener() {
        @Override
        public void eventDispatched(AWTEvent ev) {
            // check if this is a mouse clicked event originating
            // somewhere in this view area
            if (ev instanceof MouseEvent &&
                ev.getID() == MouseEvent.MOUSE_CLICKED &&
                SwingUtilities.isDescendingFrom((Component)ev.getSource(), MainViewArea.this))
            {
                // XXX: this is not exactly efficient
                for (View v : viewManager.getViews(window)) {
                    if (SwingUtilities.isDescendingFrom((Component)ev.getSource(), v.getComponent())) {
                        fireViewClickedEvent(viewManager.getViewPosition(v), (MouseEvent)ev);
                    }
                }
            }
        }
    };


    @Override
    public void windowSplitChanged(boolean b) {
        remove(0);

        if (b) {
            if (tabbedPanes.size() == 1) {
                // create a second tabbed pane
                tabbedPanes.add(createTabbedPane(1));
            }
            int orientation = window.getState().getSplitOrientation() == MainWindowState.SplitOrientation.HORIZONTAL ?
                               JSplitPane.HORIZONTAL_SPLIT : JSplitPane.VERTICAL_SPLIT;
            splitPane = new JSplitPane(orientation, true, tabbedPanes.get(0), tabbedPanes.get(1));
            splitPane.setBorder(null);
            splitPane.setDividerSize(2);
            splitPane.setResizeWeight(0.5);

            splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                    new GenericPropertyChangeListener<Integer>() {
                        @Override
                        public void propertyChange(GenericPropertyChangeEvent<Integer> ev) {
                            window.getState().setSplitPosition(ev.getNewValue());
                        }
                    });

            add(splitPane);

            validate();
            splitPane.setDividerLocation(0.5f);
        }
        else {
            if (tabbedPanes.size() == 2) {
                // destroy the second tabbed pane
                destroyTabbedPane(tabbedPanes.remove(1));
            }
            splitPane = null;
            add(tabbedPanes.get(0));
        }

        validate();
    }

    /**
     * Restores the split position to that saved in the state.
     */
    public void restoreSplitPosition() {
        if (splitPane != null) {
            if (window.getState().getSplitPosition() == 0) {
                splitPane.setDividerLocation(0.5f);
            } else {
                splitPane.setDividerLocation(window.getState().getSplitPosition());
            }
        }
    }

    private MainTabbedPane createTabbedPane(int split) {
        MainTabbedPane tabbedPane = new MainTabbedPane(viewManager, this, split);
        viewManager.addViewsChangeListener(window, tabbedPane);
        return tabbedPane;
    }

    private void destroyTabbedPane(MainTabbedPane tabbedPane) {
        viewManager.removeViewsChangeListener(window, tabbedPane);
    }


    /**
     * Listener interface for view tab interactions.
     */
    public interface ViewEventListener extends EventListener {
        /**
         * Fired when the active tab changes.
         *
         * @param pos
         *          the position of the new active tab
         */
        public void activeTabChange(ViewPosition pos);
        /**
         * Fired when a tab is being closed.
         *
         * @param pos
         *          the position of the tab being closed
         */
        public void tabClosed(ViewPosition pos);
        /**
         * Fired when a tab has been dragged and dropped.
         *
         * @param prevPos
         *          the previous position 
         * @param newPos
         *          the new position
         */
        public void tabDragNDrop(ViewPosition prevPos, ViewPosition newPos);
        /**
         * Fired when a tab has been clicked.
         *
         * @param pos
         *          the position of the tab
         * @param ev
         *          the MouseEvent that triggered the click
         */
        public void tabClicked(ViewPosition pos, MouseEvent ev);
        
        /**
         * Fired when a mouse click occurs anywhere within a view component
         * 
         * @param pos
         *          the position of the view 
         * @param ev
         *          the MouseEvent that triggered the click
         */
        public void viewClicked(ViewPosition pos, MouseEvent ev);
    }

    /**
     * Adds a view event listener.
     *
     * @param listener
     */
    public void addViewEventListener(ViewEventListener listener) {
        listeners.add(ViewEventListener.class, listener); 
    }

    /**
     * Removes a view event listener.
     *
     * @param listener
     */
    public void removeViewEventListener(ViewEventListener listener) {
        listeners.remove(ViewEventListener.class, listener);
    }

    /*package*/ void fireActiveTabChangeEvent(ViewPosition pos) {
        for (ViewEventListener listener : listeners.getListeners(ViewEventListener.class)) {
            listener.activeTabChange(pos);
        }
    }

    /*package*/ void fireTabClosedEvent(ViewPosition pos) {
        for (ViewEventListener listener : listeners.getListeners(ViewEventListener.class)) {
            listener.tabClosed(pos);
        }
    }

    /*package*/ void fireTabDragNDropEvent(ViewPosition prevPos, ViewPosition newPos) {
        for (ViewEventListener listener : listeners.getListeners(ViewEventListener.class)) {
            listener.tabDragNDrop(prevPos, newPos);
        }
    }

    /*package*/ void fireTabClickedEvent(ViewPosition pos, MouseEvent ev) {
        for (ViewEventListener listener : listeners.getListeners(ViewEventListener.class)) {
            listener.tabClicked(pos, ev);
        }
    }

    /*package*/ void fireViewClickedEvent(ViewPosition pos, MouseEvent ev) {
        for (ViewEventListener listener : listeners.getListeners(ViewEventListener.class)) {
            listener.viewClicked(pos, ev);
        }
    }
}
