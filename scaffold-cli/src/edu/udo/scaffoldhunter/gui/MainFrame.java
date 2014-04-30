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

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.View;

/**
 * The main frame of Scaffold Hunter.
 * 
 * @author Dominic Sacré
 */
public class MainFrame extends JFrame {

    private final MainWindowState state;

    private final Container content;

    private final MainMenuBar menuBar;
    private final MainToolBar toolBar;

    private final JSplitPane sideBarSplit;
    private final JSplitPane subsetBarSplit;

    private final MainSideBar sideBar;
    private final JScrollPane sideBarScroll;

    private final JPanel subsetBar;
    private final SubsetTree subsetTree;
    private final JScrollPane subsetTreeScroll;
    private final SelectionPane statusPane;

    private final MainViewArea viewArea;

    // set to true while validating the side bar split panes, to avoid moving
    // split pane dividers to incorrect locations
    private boolean validating = false;

    /**
     * @param session
     *            the GUI session
     * @param viewManager
     *            the view manager
     * @param window
     *            the window this frame belongs to
     * @param actions
     *            the actions used to populate this frame's menu, toolbar, etc.
     * @param subsetActions
     *            the subset actions used to populate this frame's menu etc.
     * @param tabActions
     *            the tab actions used to populate this frame's menu etc.
     * @param selectionActions
     *            the selection actions used to populate this frame's menu etc.
     */
    public MainFrame(GUISession session, ViewManager viewManager, final MainWindow window,
                     Actions actions, SubsetActions subsetActions, TabActions tabActions,
                     SelectionActions selectionActions) {
        this.state = window.getState();

        setIconImage(Resources.getBufferedImage("images/scaffoldhunter-icon.png"));

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        content = getContentPane();

        // create menu bar
        menuBar = new MainMenuBar(viewManager, window, actions, subsetActions, tabActions, selectionActions);
        setJMenuBar(menuBar);

        // create tool bar
        toolBar = new MainToolBar(actions);
        content.add(toolBar, BorderLayout.NORTH);

        // create side bar
        sideBar = new MainSideBar(viewManager, window);
        sideBarScroll = new JScrollPane(sideBar);
        sideBarScroll.setMinimumSize(new Dimension(180, 0));

        // create subset bar
        subsetTree = new SubsetTree(session, viewManager, window);
        statusPane = new SelectionPane(session.getSelection(), window, selectionActions, session.getDbManager());
        subsetBar = new JPanel(new BorderLayout());

        subsetTreeScroll = new JScrollPane(subsetTree);
        subsetTreeScroll.setMinimumSize(new Dimension(120, 0));
        subsetTreeScroll.setPreferredSize(new Dimension(160, 0));
        
        statusPane.setBorder(BorderFactory.createTitledBorder(I18n.get("Main.Window.SelectionBrowse")));
        subsetTreeScroll.setBorder(BorderFactory.createTitledBorder(I18n.get("Main.Window.SubsetManagement")));

        subsetBar.add(subsetTreeScroll, BorderLayout.CENTER);
        subsetBar.add(statusPane, BorderLayout.SOUTH);

        // create main view area
        viewArea = new MainViewArea(viewManager, window);

        // create a split pane that separates the side bar from the main view
        sideBarSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, sideBarScroll, null);
        sideBarSplit.setBorder(null);
        sideBarSplit.setDividerSize(4);
        sideBarSplit.setResizeWeight(0.0);

        // create a split pane that separates the subset bar from the main view
        subsetBarSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, null, subsetBar);
        subsetBarSplit.setBorder(null);
        subsetBarSplit.setDividerSize(4);
        subsetBarSplit.setResizeWeight(1.0);
        
        // set subset bar width to at least its preferred size (or more if already saved in the session)
        state.setSubsetBarWidth(Math.max(state.getSubsetBarWidth(),(int)(subsetBar.getPreferredSize().getWidth())));
        state.setSideBarWidth(Math.max(state.getSideBarWidth(),(int)(sideBar.getPreferredSize().getWidth())));

        sideBarSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new GenericPropertyChangeListener<Integer>() {
                    @Override
                    public void propertyChange(GenericPropertyChangeEvent<Integer> ev) {
                        if (!validating) {
                            state.setSideBarWidth(ev.getNewValue());
                        }
                    }
                });

        subsetBarSplit.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY,
                new GenericPropertyChangeListener<Integer>() {
                    @Override
                    public void propertyChange(GenericPropertyChangeEvent<Integer> ev) {
                        if (!validating) {
                            state.setSubsetBarWidth(subsetBarSplit.getWidth() - subsetBarSplit.getDividerSize()
                                    - ev.getNewValue());
                        }
                    }
                });
        
        statusPane.addPropertyChangeListener(SelectionPane.SELECTION_SIZE, 
                new GenericPropertyChangeListener<Object>() {
                    @Override
                    public void propertyChange(GenericPropertyChangeEvent<Object> ev) {
                        // the subset bar must be extended, if the selection counter has not enough space
                        state.setSubsetBarWidth(Math.max(state.getSubsetBarWidth(),(int)(subsetBar.getMinimumSize().getWidth())));
                        subsetBarSplit.setDividerLocation(subsetBarSplit.getWidth() - subsetBarSplit.getDividerSize()
                                - state.getSubsetBarWidth());
                    }
                });


        // delay restoring split positions until this frame has been validated
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                rebuildSideBars();
                viewArea.restoreSplitPosition();
            }
        });
    }

    /**
     * Performs cleanup to ensure that the object can be garbage-collected.
     */
    public void destroy() {
        menuBar.destroy();
        subsetTree.destroy();
        statusPane.destroy();
        viewArea.destroy();
    }

    /**
     * @return the window's view area
     */
    public MainViewArea getViewArea() {
        return viewArea;
    }

    /**
     * Rebuilds menu, tool bar and side bar items for the given view.
     * 
     * @param view
     *            the view
     * @param viewState
     *            the view's external state
     */
    public void rebuildView(View view, ViewExternalState viewState) {
        if (view != null) {
            menuBar.setViewSpecificMenu(view.getMenu());
            toolBar.setViewSpecificToolBar(view.getToolBar());
            sideBar.setViewSideBarItems(view.getSideBarItems(), viewState);
        } else {
            menuBar.setViewSpecificMenu(null);
            toolBar.setViewSpecificToolBar(null);
            sideBar.setViewSideBarItems(null, null);
        }
    }

    /**
     * Rebuilds the layout of split panes depending on the visibility of the
     * side bars
     */
    public void rebuildSideBars() {
        // remove whatever is currently in the center of the content pane
        content.remove(sideBarSplit);
        content.remove(subsetBarSplit);
        content.remove(viewArea);

        boolean sideBarVisible = state.isSideBarVisible();
        boolean subsetBarVisible = state.isSubsetBarVisible();

        if (sideBarVisible && subsetBarVisible) {
            // subsetBarSplit nested in sideBarSplit
            subsetBarSplit.setLeftComponent(viewArea);
            sideBarSplit.setRightComponent(subsetBarSplit);
            content.add(sideBarSplit, BorderLayout.CENTER);
        } else if (sideBarVisible && !subsetBarVisible) {
            // sideBarSplit separates side bar from view area
            sideBarSplit.setRightComponent(viewArea);
            content.add(sideBarSplit, BorderLayout.CENTER);
        } else if (!sideBarVisible && subsetBarVisible) {
            // subsetBarSplit separates view area from subset bar
            subsetBarSplit.setLeftComponent(viewArea);
            content.add(subsetBarSplit, BorderLayout.CENTER);
        } else {
            // no splits, add tabs directly to content pane
            content.add(viewArea, BorderLayout.CENTER);
        }

        sneakyValidate();

        // restore split pane dividers to their previous locations
        sideBarSplit.setDividerLocation(state.getSideBarWidth());

        // now that the side bar divider location is set, revalidate so we can
        // subsequently set the correct subset bar divider location
        sneakyValidate();

        subsetBarSplit.setDividerLocation(subsetBarSplit.getWidth() - subsetBarSplit.getDividerSize()
                - state.getSubsetBarWidth());

        // and again...
        sneakyValidate();
    }

    /**
     * Validates without updating side bar widths in state.
     */
    private void sneakyValidate() {
        validating = true;
        validate();
        validating = false;
    }

}
