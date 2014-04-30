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

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.hibernate.envers.tools.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * This class keeps track of all open {@link Window Windows} as well as
 * {@link View Views} and their positions in the window, split and tab
 * hierarchy.
 * 
 * @author Dominic Sacré
 */
public class ViewManager {

    private static final Logger logger = LoggerFactory.getLogger(ViewManager.class);

    private final GUIController ctrl;
    private final GUISession session;

    private final List<Window> windows = Lists.newArrayList();
    private final Map<Window, List<List<View>>> views = Maps.newHashMap();
    
    private final Map<View, ViewExternalState> viewStates = Maps.newHashMap();
    private final Map<View, Window> viewWindows = Maps.newHashMap();

    private final EventListenerList listeners = new EventListenerList();
    private final Map<Window, EventListenerList> windowListeners = Maps.newHashMap();

    private int windowCounter = 0; 


    /**
     * @param ctrl
     *          the GUI controller
     * @param session
     *          the GUI session
     */
    public ViewManager(GUIController ctrl, GUISession session) {
        this.ctrl = ctrl;
        this.session = session;
    }


    /**
     * Creates a new {@link MainWindow}. The window must subsequenty be added
     * using {@link #addWindow}.
     * 
     * @param session
     *          the GUI session
     *          
     * @return  the newly created window
     */
    public MainWindow createMainWindow(GUISession session) {
        return createMainWindow(session, null);
    }

    /**
     * Creates a new {@link MainWindow}. The window must subsequenty be added
     * using {@link #addWindow}.
     * 
     * @param session
     *          the GUI session
     * @param state
     *          the new window's state, or null to create a new state object
     * 
     * @return  the newly created window
     */
    public MainWindow createMainWindow(GUISession session, MainWindowState state) {
        logger.trace("state={}", state);

        if (state == null) {
            state = new MainWindowState();
        }
        MainWindow window = new MainWindow(ctrl, session, this, ++windowCounter, state);

        // this will leak memory if the window is not added
        createPerWindowCollections(window);

        return window;
    }

    /**
     * Creates a new {@link View} of the given type. The view must subsequently
     * be added using {@link #addView}.
     * 
     * @param klass
     *          the class of the view to be created
     * @param subset
     *          the subset to be shown in the new view
     *          
     * @return  the newly created view
     */
    public View createView(Class<? extends View> klass, Subset subset) {
        logger.trace("klass={}, subset={}", klass, subset);
        Preconditions.checkArgument(ViewClassRegistry.getClasses().contains(klass));

        GlobalConfig globalConfig = ctrl.getConfigManager().getGlobalConfig();

        ViewClassConfig classConfig = ctrl.getConfigManager().getViewClassConfig(klass);

        // FIXME
        ViewInstanceConfig instanceConfig = classConfig.getDefaultConfig();
        if (instanceConfig == null) {
            instanceConfig = ViewClassRegistry.instantiateInstanceConfig(klass);
        }

        ViewState state = ViewClassRegistry.instantiateState(klass);

        return ViewClassRegistry.instantiate(klass, session, subset,
                                             instanceConfig, classConfig, globalConfig, state);
    }

    /**
     * Duplicates an existing view.
     * 
     * @param original
     *          the view to be duplicated
     *          
     * @return  the newly created view
     */
    public View duplicateView(View original) {
        logger.trace("original={}", original);

        Class<? extends View> klass = original.getClass();

        GlobalConfig globalConfig = ctrl.getConfigManager().getGlobalConfig();

        ViewClassConfig classConfig = ctrl.getConfigManager().getViewClassConfig(klass);

        ViewInstanceConfig instanceConfig = (ViewInstanceConfig)original.getInstanceConfig().copy();
        
        ViewState state = (ViewState)original.getState().copy();

        return ViewClassRegistry.instantiate(klass, session, original.getSubset(),
                                             instanceConfig, classConfig, globalConfig, state);
    }

    /**
     * Adds a new window.
     * 
     * @param window
     *          the window to be added
     */
    public void addWindow(Window window) {
        addWindow(window, -1);
    }

    /**
     * Adds a new window at the given index.
     * 
     * @param window
     *          the window to be added
     * @param index
     *          the index in the list of windows at which the window is to be
     *          inserted, or -1 to append to the window list
     */
    public void addWindow(Window window, int index) {
        logger.trace("window={}, index={}", window, index);
        Preconditions.checkState(!windows.contains(window));
        Preconditions.checkArgument(index <= windows.size());
        
        if (index == -1) {
            index = windows.size();
        }

        windows.add(index, window);

        // currently only needed for unit testing, otherwise createMainWindow()
        // (or registering listeners) takes care of this
        createPerWindowCollections(window);

        fireWindowsChangeEvent();
    }

    /**
     * Removes a window.
     * 
     * @param window
     *          the window to be removed
     */
    public void removeWindow(Window window) {
        logger.trace("window={}", window);
        Preconditions.checkState(windows.contains(window));

        window.destroy();

        windows.remove(window);
        views.remove(window);
        windowListeners.remove(window);

        fireWindowsChangeEvent();
    }

    private void createPerWindowCollections(Window window) {
        if (!views.containsKey(window)) {
            List<List<View>> l = new ArrayList<List<View>>();
            l.add(new ArrayList<View>());
            views.put(window, l);
        }
        if (!windowListeners.containsKey(window)) {
            windowListeners.put(window, new EventListenerList());
        }
    }


    /**
     * Splits a window.
     * 
     * @param window
     *          the window to be split
     */
    public void addSplit(Window window) {
        logger.trace("window={}", window);
        Preconditions.checkState(windows.contains(window));
        
        List<List<View>> l = views.get(window);
        Preconditions.checkState(l.size() == 1);

        l.add(1, new ArrayList<View>());
        
        fireWindowSplitChangeEvent(window, true);
    }

    /**
     * Joins a split window. Views from both halves of the window are combined
     * in the new, single tab pane.
     * 
     * @param window
     *          the window to be un-split
     */
    public void removeSplit(Window window) {
        logger.trace("window={}", window);
        Preconditions.checkState(windows.contains(window));

        List<List<View>> l = views.get(window);
        Preconditions.checkState(l.size() == 2);

        List<View> move = l.get(1);

        // append all views from the second tab pane to the first one
        l.get(0).addAll(move);
        l.remove(1);

        fireWindowSplitChangeEvent(window, false);

        for (View v : move) {
            ViewPosition prevPos = new ViewPosition(1, move.indexOf(v));
            ViewPosition newPos = new ViewPosition(0, l.get(0).indexOf(v));
            
            fireViewRemovedEvent(v, window, prevPos);
            fireViewAddedEvent(v, window, newPos);
        }
    }

    /**
     * Adds a new view to the given window.
     *
     * @param view
     *          the view to be added
     * @param window
     *          the window in which the view is to be inserted
     */
    public void addView(View view, Window window) {
        addView(view, window, null, null);
    }

    /**
     * Adds a new view to the given window at the given split/tab index.
     * 
     * @param view
     *          the view to be added
     * @param window
     *          the window in which the view is to be inserted
     * @param pos
     *          the view position, or null for default
     */
    public void addView(View view, Window window, ViewPosition pos) {
        addView(view, window, pos, null);
    }

    /**
     * Adds a new view to the given window at the given split/tab index.
     * 
     * @param view
     *          the view to be added
     * @param window
     *          the window in which the view is to be inserted
     * @param pos
     *          the view position, or null for default
     * @param state
     *          the view's external state
     */
    public void addView(View view, Window window, ViewPosition pos, ViewExternalState state) {
        logger.trace("view={}, window={}, pos={}, state={}", new Object[]{view, window, pos, state});
        pos = addViewImpl(view, window, pos);

        if (state == null) {
            state = new ViewExternalState();
            state.setTabTitle(ViewClassRegistry.getClassName(view.getClass()));
        }

        viewStates.put(view, state);

        fireViewAddedEvent(view, window, pos);
    }

    /**
     * Inserts a view, but does not create a new view state or fire any
     * events.
     *  
     * @param view
     * @param window
     * @param pos
     * 
     * @return  the actual position at which the view was inserted
     */
    private ViewPosition addViewImpl(View view, Window window, ViewPosition pos) {
        Preconditions.checkState(windows.contains(window));

        if (pos == null) {
            pos = ViewPosition.DEFAULT;
        }

        if (pos.getSplit() == ViewPosition.DEFAULT_SPLIT) {
            pos = new ViewPosition(0, pos.getTab());
        }
        Preconditions.checkArgument(pos.getSplit() >= 0 && pos.getSplit() < 2);

        // split the window if necessary
        if (pos.getSplit() >= views.get(window).size()) {
            addSplit(window);
        }

        List<View> vs = views.get(window).get(pos.getSplit());

        if (pos.getTab() == ViewPosition.DEFAULT_TAB) {
            pos = new ViewPosition(pos.getSplit(), vs.size());
        }
        Preconditions.checkArgument(pos.getTab() >= 0 && pos.getTab() <= vs.size());

        viewWindows.put(view, window);
        vs.add(pos.getTab(), view);
        
        return pos;
    }

    /**
     * Removes a view.
     * 
     * @param view
     *          the view to be removed
     */
    public void removeView(View view) {
        logger.trace("view={}", view);

        Pair<Window, ViewPosition> r = removeViewImpl(view);
        Window window = r.getFirst();
        ViewPosition pos = r.getSecond();

        fireViewRemovedEvent(view, window, pos);

        viewStates.remove(view);
        view.destroy();
    }

    /**
     * Removes a view, but does not remove the view state, fire any events
     * or call the view's destroy() method.
     * 
     * @param view
     * 
     * @return  the window that contained the view, and the position the
     *          view used to occupy
     */
    private Pair<Window, ViewPosition> removeViewImpl(View view) {
        Preconditions.checkArgument(viewStates.containsKey(view));

        Window window = getViewWindow(view);
        ViewPosition pos = getViewPosition(view);

        List<View> vs = views.get(window).get(pos.getSplit());
        vs.remove(view);

        viewWindows.remove(view);

        return Pair.make(window, pos);
    }

    /**
     * Moves a view to a different window/split/tab.
     * 
     * @param view
     *          the view to be moved
     * @param window
     *          the window to move the view to
     * @param pos
     *          the new view position
     */
    public void moveView(View view, Window window, ViewPosition pos) {
        logger.trace("view={}, window={}, pos={}", new Object[]{view, window, pos});

        Pair<Window, ViewPosition> r = removeViewImpl(view);
        Window prevWindow = r.getFirst();
        ViewPosition prevPos = r.getSecond();
        
        fireViewRemovedEvent(view, prevWindow, prevPos);

        pos = addViewImpl(view, window, pos);

        fireViewAddedEvent(view, window, pos);
    }

    
    /**
     * @return  a list of all windows
     */
    public List<Window> getWindows() {
        return windows;
    }

    /**
     * @param window
     * 
     * @return  the number of splits in the given window (one or two)
     */
    public int getWindowNumSplits(Window window) {
        return views.get(window).size();
    }

    /**
     * @return  all views in all windows (in unspecified order)
     */
    public Iterable<View> getAllViews() {
        return viewStates.keySet();
    }

    /**
     * @param <T>
     * @param klass
     *          the class of the views requested
     *
     * @return  all views of the given class (in unspecified order)
     */
    public <T extends View> Iterable<View> getAllViewsOfClass(Class<T> klass) {
        return Lists.<View>newArrayList(Iterables.filter(getAllViews(), klass));
    }
    
    /**
     * @param window
     * 
     * @return  a list of all views in the given window
     */
    public List<View> getViews(Window window) {
        return Lists.newArrayList(Iterables.concat(views.get(window)));
    }

    /**
     * @param window
     * @param split
     * 
     * @return  a list of all views in the given window
     */
    public List<View> getViews(Window window, int split) {
        return views.get(window).get(split);
    }

    /**
     * @param window
     * @param pos
     * 
     * @return  the view in the given window at the given position
     */
    public View getView(Window window, ViewPosition pos) {
        return views.get(window).get(pos.getSplit()).get(pos.getTab());
    
    }

    /**
     * @param view
     * 
     * @return  the window that holds the given view
     */
    public Window getViewWindow(View view) {
        return viewWindows.get(view);
    }

    /**
     * @param view
     * 
     * @return  the split/tab position occupied by the given view
     */
    public ViewPosition getViewPosition(View view) {
        Window window = getViewWindow(view);
        List<List<View>> l = views.get(window);

        int split = 0;
        for (List<View> vs : l) {
            int i = vs.indexOf(view);
            if (i != -1) {
                return new ViewPosition(split, i);
            }
            ++split;
        }
        // make compiler happy
        return null;
    }

    /**
     * @param view
     * 
     * @return  the current state of the given view
     */
    public ViewExternalState getViewState(View view) {
        return viewStates.get(view);
    }


    /**
     * Listener interface for changes in the list of windows.
     */
    public interface WindowsChangeListener extends EventListener {
        /**
         * @param windows
         *          the new list of windows
         */
        public void windowsChanged(List<Window> windows);
    }

    /**
     * @param listener
     */
    public void addWindowsChangeListener(WindowsChangeListener listener) {
        logger.trace("listener={}", listener);
        listeners.add(WindowsChangeListener.class, listener);
    }

    /**
     * @param listener
     */
    public void removeWindowsChangeListener(WindowsChangeListener listener) {
        logger.trace("listener={}", listener);
        listeners.remove(WindowsChangeListener.class, listener);
    }

    private void fireWindowsChangeEvent() {
        logger.trace(null);
        for (WindowsChangeListener listener : listeners.getListeners(WindowsChangeListener.class)) {
            listener.windowsChanged(windows);
        }
    }


    /**
     * Listener interface for changes in a window's split configuration.
     */
    public interface WindowSplitChangeListener extends EventListener {
        /**
         * @param b
         *          whether or not the window is to be split
         */
        public void windowSplitChanged(boolean b);
    }

    /**
     * @param window
     * @param listener
     */
    public void addWindowSplitChangeListener(Window window, WindowSplitChangeListener listener) {
        logger.trace("window={}, listener={}", window, listener);
        // XXX: this allows adding listeners before the window has been
        // added to the ViewManager. we probably shouldn't do this
        createPerWindowCollections(window);
        windowListeners.get(window).add(WindowSplitChangeListener.class, listener);
    }

    /**
     * @param window
     * @param listener
     */
    public void removeWindowSplitChangeListener(Window window, WindowSplitChangeListener listener) {
        logger.trace("window={}, listener={}", window, listener);
        windowListeners.get(window).remove(WindowSplitChangeListener.class, listener);
    }

    private void fireWindowSplitChangeEvent(Window window, boolean b) {
        logger.trace("window={}, b={}", window, b);
        for (WindowSplitChangeListener listener :
            windowListeners.get(window).getListeners(WindowSplitChangeListener.class)) {
                listener.windowSplitChanged(b);
        }
    }


    /**
     * Listener interface for changes in the list of views.
     */
    public interface ViewsChangeListener extends EventListener {
        /**
         * @param view
         *          the view that was added
         * @param pos
         *          the position at which the view was added
         */
        public void viewAdded(View view, ViewPosition pos);
        /**
         * @param view
         *          the view that was removed
         * @param pos
         *          the position the view used to occupy
         */
        public void viewRemoved(View view, ViewPosition pos);
    }

    /**
     * @param window
     * @param listener
     */
    public void addViewsChangeListener(Window window, ViewsChangeListener listener) {
        logger.trace("window={}, listener={}", window, listener);
        // XXX: this allows adding listeners before the window has been
        // added to the ViewManager. we probably shouldn't do this
        createPerWindowCollections(window);
        windowListeners.get(window).add(ViewsChangeListener.class, listener);
    }

    /**
     * @param window
     * @param listener
     */
    public void removeViewsChangeListener(Window window, ViewsChangeListener listener) {
        logger.trace("window={}, listener={}", window, listener);
        windowListeners.get(window).remove(ViewsChangeListener.class, listener);
    }
    
    private void fireViewAddedEvent(View view, Window window, ViewPosition pos) {
        logger.trace("view={}, window={}, pos={}", new Object[]{view, window, pos});
        for (ViewsChangeListener listener :
                windowListeners.get(window).getListeners(ViewsChangeListener.class)) {
            listener.viewAdded(view, pos);
        }
    }

    private void fireViewRemovedEvent(View view, Window window, ViewPosition pos) {
        logger.trace("view={}, window={}, pos={}", new Object[]{view, window, pos});
        for (ViewsChangeListener listener :
                windowListeners.get(window).getListeners(ViewsChangeListener.class)) {
            listener.viewRemoved(view, pos);
        }
    }

}
