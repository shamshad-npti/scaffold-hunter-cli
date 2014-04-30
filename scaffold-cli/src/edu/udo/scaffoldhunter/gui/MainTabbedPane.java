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

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.gui.util.CloseableTabbedPane;
import edu.udo.scaffoldhunter.gui.util.DnDCloseableTabbedPane;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.util.StringEscapeUtils;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * The tab part of the Scaffold Hunter main window, containing the main
 * components of each view.
 * 
 * @author Dominic Sacré
 */
public class MainTabbedPane extends DnDCloseableTabbedPane implements ViewManager.ViewsChangeListener {

    private final ViewManager viewManager;
    private final MainViewArea viewArea;
    private final int split;

    private final Map<View, PropertyChangeListener> viewTitleListeners = Maps.newHashMap();
    private final Map<View, PropertyChangeListener> viewSubsetListeners = Maps.newHashMap();
    private final Map<View, PropertyChangeListener> subsetPropertyListeners = Maps.newHashMap();

    /**
     * @param viewManager
     *          the view manager
     * @param viewArea 
     *          the window's view area
     * @param split
     *          the split index of this tabbed pane
     */
    public MainTabbedPane(ViewManager viewManager, MainViewArea viewArea, int split) {
        this.viewManager = viewManager;
        this.viewArea = viewArea;
        this.split = split;

        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        setupListeners();
    }

    private void setupListeners() {
        // listen for changes of the active tab
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int tab = getSelectedIndex();
//                if (tab == -1) tab = 0; //FIXME: Why was the position manually set to 0? Causes errors and program works fine with -1
                viewArea.fireActiveTabChangeEvent(new ViewPosition(split, tab));
            }
        });

        // listen for tabs being closed
        setCloseListener(new CloseableTabbedPane.CloseListener() {
            @Override
            public void closeRequested(Component component) {
                int tab = ((MainTabbedPane)component.getParent()).indexOfComponent(component);
                viewArea.fireTabClosedEvent(new ViewPosition(split, tab));
            }
        });

        // listen for tabs being dagged/dropped
        setDnDListener(new DnDCloseableTabbedPane.DnDListener() {
            @Override
            public void dndFinished(int prevIndex, int newIndex) {
                viewArea.fireTabDragNDropEvent(new ViewPosition(split, prevIndex), new ViewPosition(split, newIndex));
            }
        });

        // listen for mouse clicks on tabs
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent ev) {
                int tab = indexAtLocation(ev.getX(), ev.getY());
                if (tab != -1) {
                    viewArea.fireTabClickedEvent(new ViewPosition(split, tab), ev);
                }
            }
        });
    }

    @Override
    protected void processMouseEvent(MouseEvent ev) {
        // intercept right button mouse clicks in order to be able to show
        // the context menu without switching tabs
        if (ev.getID() == MouseEvent.MOUSE_PRESSED && ev.getButton() == MouseEvent.BUTTON3) {
            return;
        }
        super.processMouseEvent(ev);
    }

    /**
     * @return  the split index of this tabbed pane
     */
    public int getSplitIndex() {
        return split;
    }

    @Override
    public void viewAdded(final View view, final ViewPosition pos) {
        // return if the view is not being added to this tab pane
        if (pos.getSplit() != split) return;

        ViewExternalState state = viewManager.getViewState(view);

        String title = state.getTabTitle();
        Icon icon = ViewClassRegistry.getClassIcon(view.getClass());

        // insert a new tab for the view
        insertTab(title, icon, view.getComponent(), makeToolTipText(view), pos.getTab());

        viewTitleListeners.put(view, new GenericPropertyChangeListener<String>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<String> ev) {
                // title changed, update tab title and tooltip
                setTitleAt(indexOfComponent(view.getComponent()), ev.getNewValue());
                updateToolTip(view);
            }
        });

        viewSubsetListeners.put(view, new GenericPropertyChangeListener<Subset>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<Subset> ev) {
                // view's subset changed, update tooltip
                updateToolTip(view);

                // detach listeners from old subset and re-attach to new subset
                ev.getOldValue().removePropertyChangeListener(subsetPropertyListeners.get(view));
                ev.getNewValue().addPropertyChangeListener(subsetPropertyListeners.get(view));
            }
        });

        subsetPropertyListeners.put(view, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent ev) {
                // subset properties (title, comment, ...) changed, update
                // tooltip
                updateToolTip(view);
            }
        });

        // listen for changes of the tab tile
        state.addPropertyChangeListener(ViewExternalState.TAB_TITLE_PROPERTY, viewTitleListeners.get(view));

        // listen for changes of the view's subset
        view.addPropertyChangeListener(View.SUBSET_PROPERTY, viewSubsetListeners.get(view));

        // listen changes of the subset's properties
        view.getSubset().addPropertyChangeListener(subsetPropertyListeners.get(view));
    }

    @Override
    public void viewRemoved(View view, ViewPosition pos) {
        // return if the view being removed is not in this tab pane
        if (pos.getSplit() != split) return;

        ViewExternalState state = viewManager.getViewState(view);

        // remove the view's tab
        remove(indexOfComponent(view.getComponent()));

        // remove listeners
        state.removePropertyChangeListener(ViewExternalState.TAB_TITLE_PROPERTY, viewTitleListeners.get(view));
        view.removePropertyChangeListener(View.SUBSET_PROPERTY, viewSubsetListeners.get(view));
        view.getSubset().removePropertyChangeListener(subsetPropertyListeners.get(view));

        viewTitleListeners.remove(view);
        viewSubsetListeners.remove(view);
        subsetPropertyListeners.remove(view);
    }

    private void updateToolTip(View view) {
        setToolTipTextAt(indexOfComponent(view.getComponent()), makeToolTipText(view));
    }

    private String makeToolTipText(View view) {
        Subset subset = view.getSubset();

        boolean hasComment = (subset.getComment() != null && subset.getComment() != "");

        return _("Tab.Tooltip",
                StringEscapeUtils.escapeHTML(viewManager.getViewState(view).getTabTitle()),
                ViewClassRegistry.getClassName(view.getClass()),
                StringEscapeUtils.escapeHTML(subset.getTitle()),
                subset.size(),
                subset.getCreationDate(),
                hasComment ? StringEscapeUtils.escapeHTML(subset.getComment()) : _("Subset.Tooltip.EmptyComment")
        );
    }
}
