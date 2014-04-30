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

package edu.udo.scaffoldhunter.gui;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Point;
import java.awt.event.ActionEvent;

import edu.udo.scaffoldhunter.gui.dialogs.RenameDialog;
import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.View;

/**
 * @author Dominic Sacré
 *
 */
public class TabActions {

    private final GUISession session;
    private final ViewManager viewManager;
    private final MainWindow window;

    private View view;
    private Point tabLocation;

    /**
     * @param session
     * @param viewManager
     * @param window
     * @param view
     * @param tabLocation
     */
    public TabActions(GUISession session, ViewManager viewManager, MainWindow window, View view, Point tabLocation) {
        this.session = session;
        this.viewManager = viewManager;
        this.window = window;
        this.tabLocation = tabLocation;

        updateContext(view);
    }

    /**
     * Updates the tab actions' context.
     * 
     * @param view
     */
    public void updateContext(View view) {
        this.view = view;

        rename.setEnabled(view != null);
        moveToNewWindow.setEnabled(view != null);
        duplicate.setEnabled(view != null);
        close.setEnabled(view != null);

        for (int n = 0; n < 2; ++n) {
            moveToTabPane[n].setEnabled(view != null && viewManager.getViewPosition(view).getSplit() != n);
        }
    }

    private Point dialogLocation(Point tabLocation) {
        if (tabLocation != null) {
            Point location = (Point)tabLocation.clone();
            location.translate(0, 45);
            return location;
        } else {
            return null;
        }
    }
    

    /**
     * @return  an action that renames the given tab.
     */
    public AbstractAction getRename() {
        return rename;
    }

    private AbstractAction rename = new AbstractAction() {
        {
            putValues(_("Main.Window.RenameTab"), null, Resources.getIcon("edit.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            ViewExternalState state = viewManager.getViewState(view);

            RenameDialog dlg = new RenameDialog(window.getFrame(), _("RenameView.Title"), state.getTabTitle(),
                                                dialogLocation(tabLocation), RenameDialog.Anchor.TOP_LEFT);
            dlg.setVisible(true);

            String newTitle = dlg.getNewText();

            if (newTitle != null) {
                state.setTabTitle(newTitle);
            }
        }
    };

    /**
     * @param window
     * 
     * @return  an action that moves the view to the given window
     */
    public AbstractAction getMoveToWindow(Window window) {
        return new MoveToWindow(window);
    }

    private class MoveToWindow extends AbstractAction {
        private final Window window;

        private MoveToWindow(Window window) {
            super(window.getTitle());
            this.window = window;

            setEnabled(view != null);
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            viewManager.moveView(view, window, ViewPosition.DEFAULT);
        }
    }

    /**
     * @return  an action that moves the view to a new window
     */
    public AbstractAction getMoveToNewWindow() {
        return moveToNewWindow;
    }

    private AbstractAction moveToNewWindow = new AbstractAction() {
        {
            putValues(_("Tab.MoveToNewWindow"));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            MainWindow w = session.getGUIController().createWindow();

            viewManager.moveView(view, w, ViewPosition.DEFAULT);

            w.setVisible(true);
        }
    };

    /**
     * @param split
     * 
     * @return  an action that moves the view to the given tab pane
     */
    public AbstractAction getMoveToTabPane(int split) {
        return moveToTabPane[split];
    }

    private AbstractAction[] moveToTabPane = new AbstractAction[]{
            new MoveToTabPane(0),
            new MoveToTabPane(1),
    };

    private class MoveToTabPane extends AbstractAction {
        private final int split;

        private MoveToTabPane(int split) {
            super((split == 0) ? _("Tab.TabPaneFirst") : _("Tab.TabPaneSecond"));
            this.split = split;
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            MainWindowState state = window.getState();
            if (split == 1 && state.getSplitOrientation() == MainWindowState.SplitOrientation.NONE) {
                state.setSplitOrientation(MainWindowState.SplitOrientation.HORIZONTAL);
            }
            viewManager.moveView(view, window, new ViewPosition(split, ViewPosition.DEFAULT_TAB));
        }
    }

    /**
     * @return  an action that duplicates the given view.
     */
    public AbstractAction getDuplicate() {
        return duplicate;
    }

    private AbstractAction duplicate = new AbstractAction() {
        {
            putValues(_("Main.Window.DuplicateTab"), null, Resources.getIcon("copy-view.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            View newView = viewManager.duplicateView(view);

            ViewPosition pos = viewManager.getViewPosition(view);
            ViewPosition newPos = new ViewPosition(pos.getSplit(), pos.getTab() + 1);

            viewManager.addView(newView, window, newPos);

            ViewExternalState oldState = viewManager.getViewState(view);
            ViewExternalState newState = viewManager.getViewState(newView);
            newState.setTabTitle(oldState.getTabTitle() + " (copy)");
        }
    };

    /**
     * @return  an action that closes the given tab.
     */
    public AbstractAction getClose() {
        return close;
    }

    private AbstractAction close = new AbstractAction() {
        {
            putValues(_("Tab.Close"));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            window.closeView(view);
        }
    };

}
