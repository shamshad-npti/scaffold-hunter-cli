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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.view.View;

/**
 * The controller part of the Scaffold Hunter main window, responsible for
 * setting up listeners and for handling user input.
 * 
 * @author Dominic Sacré
 */
public class MainWindow implements Window, MainViewArea.ViewEventListener {

    private final GUIController ctrl;
    private final GUISession session;
    private final ViewManager viewManager;
    private final Actions actions;
    private final SubsetActions subsetActions;
    private final TabActions tabActions;
    private final MainWindowState state;

    private final MainFrame frame;

    private final int number;

    /**
     * The active view property name.
     */
    public static final String ACTIVE_VIEW_PROPERTY = "activeView";

    /**
     * The active view property name.
     */
    public static final String ACTIVE_SUBSET_PROPERTY = "activeSubset";

    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);


    /**
     * @param ctrl
     *          the GUI controller
     * @param session
     *          the GUI session
     * @param viewManager
     *          the view manager
     * @param number
     *          the number of the window
     * @param state
     *          the window's state
     */
    public MainWindow(GUIController ctrl, GUISession session, ViewManager viewManager, int number, MainWindowState state) {
        this.ctrl = ctrl;
        this.session = session;
        this.viewManager = viewManager;
        this.number = number;
        this.state = state;

        actions = new Actions(ctrl, session, viewManager, this);

        subsetActions = new SubsetActions(session, viewManager, this, null, null, null, null);
        tabActions = new TabActions(session, viewManager, this, null, null);
        SelectionActions selectionActions = new SelectionActions(session, this);

        frame = new MainFrame(session, viewManager, this, actions, subsetActions, tabActions, selectionActions);
        session.getSubsetController().setParentFrame(frame);

        String title = session.getDbSession().getTitle() + " - " + _("Main.Title");
        if (number > 1) {
            title += " (" + Integer.toString(number) + ")";
        }
        frame.setTitle(title);

        setupListeners();

        if (state.getFrameBounds() == null) {
            frame.setSize(960, 720);
        } else {
            frame.setBounds(state.getFrameBounds());
            frame.setExtendedState(state.getFrameExtendedState());
        }
    }

    /**
     * Closes this window and performs cleanup to ensure that it can be
     * garbage-collected.
     */
    @Override
    public void destroy() {
        cleanupListeners();
        frame.destroy();
        frame.dispose();
    }

    private void setupListeners() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ctrl.closeWindow(MainWindow.this);
            }
        });

        viewManager.addViewsChangeListener(this, viewsChangeListener);

        state.addPropertyChangeListener(MainWindowState.ACTIVE_VIEW_POSITION_PROPERTY, activeViewPositionChangeListener);
        state.addPropertyChangeListener(MainWindowState.SIDE_BAR_VISIBLE_PROPERTY, sideBarsVisibleChangeListener);
        state.addPropertyChangeListener(MainWindowState.SUBSET_BAR_VISIBLE_PROPERTY, sideBarsVisibleChangeListener);
        state.addPropertyChangeListener(MainWindowState.SPLIT_ORIENTATION_PROPERTY, splitOrientationChangeListener);

        addPropertyChangeListener(ACTIVE_VIEW_PROPERTY, activeViewChangeListener);
        addPropertyChangeListener(ACTIVE_SUBSET_PROPERTY, activeSubsetChangeListener);

        frame.getViewArea().addViewEventListener(this);
    }

    private void cleanupListeners() {
        viewManager.removeViewsChangeListener(this, viewsChangeListener);
    }


    /**
     * Makes this window visible, raising it above other windows.
     */
    @Override
    public void raise() {
        frame.toFront();
    }

    /**
     * Shows or hides this window
     * 
     * @param visible
     */
    @Override
    public void setVisible(boolean visible) {
        frame.setVisible(visible);
    }

    /**
     * @return  the title of this window
     */
    @Override
    public String getTitle() {
        return frame.getTitle();
    }

    /**
     * @return  the number of this window
     */
    @Override
    public int getNumber() {
        return number;
    }

    /**
     * @return  the JFrame belonging to this window
     */
    public MainFrame getFrame() {
        return frame;
    }

    /**
     * @return  the state of this window
     */
    public MainWindowState getState() {
        if (frame != null) {
            state.setFrameBounds(frame.getBounds());
            state.setFrameExtendedState(frame.getExtendedState());
        }

        return state;
    }

    /**
     * Adds a new view to this window
     * 
     * @param view
     *          the view to be added
     * @param split 
     */
    public void addView(View view, int split) {
        viewManager.addView(view, this, new ViewPosition(split, ViewPosition.DEFAULT_TAB));
    }

    /**
     * Closes a view
     * 
     * @param view
     *          the view to be closed
     */
    public void closeView(View view) {
        viewManager.removeView(view);
    }

    /**
     * Switches to the specified view tab
     * 
     * @param view
     *          the view to be selected
     */
    public void selectView(View view) {
        selectView(viewManager.getViewPosition(view));
    }

    /**
     * Validates the given view position and then selects the corresponding
     * view.
     *  
     * @param pos
     *          the position of the view to be selected
     */
    public void selectView(ViewPosition pos) {
        selectView(pos, false);
    }

    /**
     * Validates the given view position and then selects the corresponding
     * view.
     *  
     * @param pos
     *          the position of the view to be selected
     * @param forceListeners
     *          if true, view/subset change listeners will be called regardless
     *          of the previous view/subset
     */
    public void selectView(ViewPosition pos, boolean forceListeners) {
        int nsplits = state.getSplitOrientation() == MainWindowState.SplitOrientation.NONE ? 1 : 2;

        if (pos.getSplit() >= nsplits) {
            // FIXME: use active tab instead of first one
            pos = new ViewPosition(0, 0);
        }

        View oldView = getActiveView();
        if (oldView != null) {
            oldView.removePropertyChangeListener(View.CONTENT_PROPERTY, viewUpdateListener);
        }

        state.setActiveViewPosition(pos);

        View newView = getActiveView();
        if (newView != null) {
            newView.addPropertyChangeListener(View.CONTENT_PROPERTY, viewUpdateListener);
        }

        firePropertyChange(ACTIVE_VIEW_PROPERTY, !forceListeners ? oldView : null, newView);

        Subset oldSubset = (oldView != null && !forceListeners) ? oldView.getSubset() : null;
        Subset newSubset = newView != null ? newView.getSubset() : null;

        firePropertyChange(ACTIVE_SUBSET_PROPERTY, oldSubset, newSubset);
    }

    /**
     * Switches to the next view tab (to the right)
     */
    public void selectNextView() {
        List<View> views = getViews(state.getActiveViewPosition().getSplit());
        if (views.size() == 0) return;
        int i = state.getActiveViewPosition().getTab() + 1;
        selectView(new ViewPosition(state.getActiveViewPosition().getSplit(), i % views.size()));
    }
    
    /**
     * Switches to the previous view tab (to the left)
     */
    public void selectPrevView() {
        List<View> views = getViews(state.getActiveViewPosition().getSplit());
        if (getViews().size() == 0) return;
        int i = state.getActiveViewPosition().getTab() - 1 + views.size();
        selectView(new ViewPosition(state.getActiveViewPosition().getSplit(), i % views.size()));
    }
    
    /**
     * @return  a list of all views contained in this window
     */
    public List<View> getViews() {
        return viewManager.getViews(this);
    }

    /**
     * @param split 
     * @return  a list of all views contained within a split area in this
     *          window
     */
    public List<View> getViews(int split) {
        return viewManager.getViews(this, split);
    }

    private View getView(ViewPosition pos) {
        return viewManager.getView(this, pos);
    }

    /**
     * @return  the view that's currently active in this window
     */
    public View getActiveView() {
        ViewPosition activeView = state.getActiveViewPosition();
        if (activeView.getSplit() != ViewPosition.DEFAULT_SPLIT && activeView.getTab() != ViewPosition.DEFAULT_TAB) {
            // FIXME
            try {
                return getViews(activeView.getSplit()).get(activeView.getTab());
            } catch (IndexOutOfBoundsException ex) {
                return null;
            } catch (NullPointerException ex) {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * Updates this view-dependent parts of this window.
     */
    public void updateView() {
        View activeView = getActiveView();

        // FIXME: at least do this somewhere else
        for (View v : getViews()) {
            if (v == activeView) {
                v.getComponent().setBorder(new LineBorder(UIManager.getColor("textHighlight"), 2));
            } else {
                v.getComponent().setBorder(new EmptyBorder(2, 2, 2, 2));
            }
        }
        
        ViewExternalState viewState = viewManager.getViewState(activeView);
        frame.rebuildView(activeView, viewState);
    }

    /**
     * Splits this window.
     */
    public void addSplit() {
        viewManager.addSplit(this);
    }

    /**
     * Un-splits this window.
     */
    public void removeSplit() {
        viewManager.removeSplit(this);
    }


    @Override
    public void activeTabChange(ViewPosition pos) {
        selectView(pos);
    }

    @Override
    public void tabClosed(ViewPosition pos) {
        closeView(getView(pos));
    }

    @Override
    public void tabDragNDrop(ViewPosition prevPos, ViewPosition newPos) {
        View v = getView(prevPos);
        viewManager.moveView(v, this, newPos);
        selectView(v);
    }

    @Override
    public void tabClicked(ViewPosition pos, MouseEvent ev) {
        if (ev.getButton() == MouseEvent.BUTTON1) {
            if (ev.getClickCount() == 1) {
                selectView(pos);
            }
            else if (ev.getClickCount() == 2) {
                // left mouse button double click
                boolean b = !(state.isSideBarVisible() || state.isSubsetBarVisible());
                state.setSideBarVisible(b);
                state.setSubsetBarVisible(b);
            }
        }
        else if (ev.getButton() == MouseEvent.BUTTON3) {
            MainTabbedPane tabbedPane = (MainTabbedPane)ev.getComponent();
            int split = tabbedPane.getSplitIndex();
            int tab = tabbedPane.indexAtLocation(ev.getX(), ev.getY());

            // get the position of the tab
            Point pt = tabbedPane.getUI().getTabBounds(tabbedPane, tab).getLocation();
            SwingUtilities.convertPointToScreen(pt, tabbedPane);

            View view = getView(new ViewPosition(split, tab));

            JPopupMenu menu = new TabContextMenu(session, viewManager, this, view, pt);
            menu.show(ev.getComponent(), ev.getX(), ev.getY());
        }
    }

    @Override
    public void viewClicked(ViewPosition pos, MouseEvent ev) {
        selectView(pos);
    }

    private PropertyChangeListener activeViewPositionChangeListener =
            new GenericPropertyChangeListener<ViewPosition>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<ViewPosition> ev) {
            updateView();
        }
    };

    private PropertyChangeListener sideBarsVisibleChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            actions.updateState();
            frame.rebuildSideBars();
        }
    };

    private PropertyChangeListener splitOrientationChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            actions.updateState();
        }
    };

    private PropertyChangeListener viewUpdateListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateView();
        }
    };

    private ViewManager.ViewsChangeListener viewsChangeListener = new ViewManager.ViewsChangeListener() {
        @Override
        public void viewAdded(View view, ViewPosition pos) {
            updateView();
        }

        @Override
        public void viewRemoved(View view, ViewPosition pos) {
            updateView();
        }
    };

    private PropertyChangeListener activeViewChangeListener = new GenericPropertyChangeListener<View>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<View> ev) {
            View oldView = ev.getOldValue();
            View newView = ev.getNewValue();

            if (oldView != null) {
                oldView.removePropertyChangeListener(View.SUBSET_PROPERTY, subsetChangeListener);
            }

            if (newView != null) {
                newView.addPropertyChangeListener(View.SUBSET_PROPERTY, subsetChangeListener);
            }

            tabActions.updateContext(ev.getNewValue());
            subsetActions.updateContext(newView != null ? newView.getSubset() : null, null, newView);
        }
    };

    private PropertyChangeListener activeSubsetChangeListener = new GenericPropertyChangeListener<Subset>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Subset> ev) {
            subsetActions.updateContext(ev.getNewValue(), null, getActiveView());
        }
    };

    private PropertyChangeListener subsetChangeListener = new GenericPropertyChangeListener<Subset>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Subset> ev) {
            firePropertyChange(ACTIVE_SUBSET_PROPERTY, ev.getOldValue(), ev.getNewValue());
        }
    };


    /**
     * Adds a change listener for the given property.
     * 
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a change listener for the given property.
     * 
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Fires a property change event.
     * 
     * @param <T>
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    protected <T> void firePropertyChange(String propertyName, T oldValue, T newValue) {
        GenericPropertyChangeEvent<T> ev = new GenericPropertyChangeEvent<T>(this, propertyName, oldValue, newValue);
        propertyChangeSupport.firePropertyChange(ev);
    }

}
