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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.AbstractToggleAction;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * A group of AbstractAction objects which can be inserted into menus, tool
 * bars, etc.
 * The main purpose of the Actions class is to bind the individual actions
 * (inner classes) to the GUI controller and a specific main window.
 * 
 * @author Dominic Sacré
 */
public class Actions {

    private final GUIController ctrl;
    private final GUISession session;
    private final ViewManager viewManager;
    private final MainWindow window;
    private final MainWindowState state;

    /**
     * @param ctrl
     *          the GUI controller
     * @param session
     *          the GUI session
     * @param viewManager
     *          the view manager
     * @param window
     *          the main window
     */
    public Actions(GUIController ctrl, GUISession session, ViewManager viewManager, MainWindow window) {
        this.ctrl = ctrl;
        this.session = session;
        this.window = window;
        this.viewManager = viewManager;
        this.state = window.getState();

        updateState();
    }


    /**
     * Updates the actions' enabled/selected states.
     */
    public void updateState() {
        toggleSideBar.setSelected(state.isSideBarVisible());
        toggleSubsetBar.setSelected(state.isSubsetBarVisible());

        updateSplitState();
    }


    /**
     * @return  an action that terminates the application
     */
    public AbstractAction getQuit() {
        return quit;
    }

    private AbstractAction quit = new AbstractAction() {
        {
            putValues(_("Main.Session.Quit"), _("Main.Session.Quit.Description"),
                      Resources.getIcon("exit.png"), null,
                      KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.exit(window);
        }
    };
    
    /**
     * @return  an action that opens the session dialog
     */
    public AbstractAction getShowSessionDialog() {
        return showSessionDialog;
    }
    
    private AbstractAction showSessionDialog = new AbstractAction() {
        {
            putValues(_("Main.Session.SessionDialog"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.showSessionDialog(session.getDbSession().getProfile(), window.getFrame());
        }
    };
    
    /**
     * @return  an action that saves the current session
     */
    public AbstractAction getSaveSession() {
        return saveSession;
    }
    
    private AbstractAction saveSession = new AbstractAction() {
        {
            putValues(_("Main.Session.SaveSession"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.saveSession(window.getFrame());
        }
    };

    /**
     * @return  an action that opens the options dialog
     */
    public AbstractAction getShowGlobalOptionsDialog() {
        return showGlobalOptionsDialog;
    }

    private AbstractAction showGlobalOptionsDialog = new AbstractAction() {
        {
            putValues(_("Main.Session.GlobalOptions"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.showGlobalOptionsDialog(window);
        }
    };

    /**
     * @return  an action that opens the options dialog
     */
    public AbstractAction getShowOptionsDialog() {
        return showOptionsDialog;
    }

    private AbstractAction showOptionsDialog = new AbstractAction() {
        {
            putValues(_("Main.Session.Options"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.showOptionsDialog(window);
        }
    };
    
    /**
     * @return an action that opens the tooltip configuration dialog
     */
    public AbstractAction getShowTooltipDialog() {
        return showTooltipDialog;
    }
    
    private AbstractAction showTooltipDialog = new AbstractAction() {
        {
            putValues(_("Main.Session.TooltipOptions"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.showTooltipDialog(window);
        }
    };
    
    /**
     * @return  an action that toggles the visibility of the side bar
     */
    public AbstractToggleAction getToggleSideBar() {
        return toggleSideBar;
    }

    private AbstractToggleAction toggleSideBar = new AbstractToggleAction() {
        {
            putValues(_("Main.Window.ToggleSideBar"), _("Main.Window.ToggleSideBar.Description"),
                    Resources.getIcon("sidebar.png"), Resources.getLargeIcon("sidebar.png"),
                    KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, ActionEvent.ALT_MASK));
        }
        @Override
        public void toggleActionPerformed(ActionEvent e) {
            state.setSideBarVisible(isSelected());
        }
    };

    /**
     * @return  an action that toggles the visibility of the subset bar
     */
    public AbstractToggleAction getToggleSubsetBar() {
        return toggleSubsetBar;
    }
    
    private AbstractToggleAction toggleSubsetBar = new AbstractToggleAction() {
        {
            putValues(_("Main.Window.ToggleSubsetBar"), _("Main.Window.ToggleSubsetBar.Description"),
                      Resources.getIcon("subsetbar.png"), Resources.getLargeIcon("subsetbar.png"),
                      KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, ActionEvent.ALT_MASK));
        }
        @Override
        public void toggleActionPerformed(ActionEvent e) {
            state.setSubsetBarVisible(isSelected());
        }
    };
    
    /**
     * @return  an action that opens a new main window
     */
    public AbstractAction getNewWindow() {
        return newWindow;
    }
    
    private AbstractAction newWindow = new AbstractAction() {
        {
            putValues(_("Main.Window.NewWindow"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.createWindow(null);
        }
    };

    /**
     * @return  an action that closes the current main window
     */
    public AbstractAction getCloseWindow() {
        return closeWindow;
    }

    private AbstractAction closeWindow = new AbstractAction() {
        {
            putValues(_("Main.Window.CloseWindow"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.closeWindow(window);
        }
    };
    
    /**
     * @return  an action than navigates to the next view in this window
     */
    public AbstractAction getSelectNextView() {
        return selectNextView;
    }
    
    private AbstractAction selectNextView = new AbstractAction() {
        {
            putValues(_("Main.Window.NextView"), null,
                      KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, ActionEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            window.selectNextView();
        }
    };

    /**
     * @return  an action than navigates to the previous view in this window
     */
    public AbstractAction getSelectPrevView() {
        return selectPrevView;
    }

    private AbstractAction selectPrevView = new AbstractAction() {
        {
            putValues(_("Main.Window.PreviousView"), null,
                      KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, ActionEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            window.selectPrevView();
        }
    };

    /**
     * FIXME: this is kind of neat, but should go away once proper
     * view/subset management is implemented
     * 
     * @param klass
     * @param split 
     *
     * @return  an action that adds a new view of the specified class to
     *          the current window
     */
    public AbstractAction getAddView(Class<? extends View> klass, int split) {
        return new AddView(klass, split);
    }

    private class AddView extends AbstractAction {
        private Class<? extends View> klass;
        private int split;

        private AddView(Class<? extends View> klass, int split) {
            super(ViewClassRegistry.getClassName(klass),
                  ViewClassRegistry.getClassIcon(klass));
            this.klass = klass;
            this.split = split;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            View view = viewManager.createView(klass, session.getDbSession().getSubset());
            
            if (split == 1 && state.getSplitOrientation() == MainWindowState.SplitOrientation.NONE) {
                state.setSplitOrientation(MainWindowState.SplitOrientation.HORIZONTAL);
            }
            window.addView(view, split);
            window.selectView(view);
        }
    }
    
    /**
     * @return  an action that closes the current view
     */
    public AbstractAction getCloseView() {
        return closeView;
    }

    private AbstractAction closeView = new AbstractAction() {
        {
            putValues(_("Main.Window.CloseView"), null,
                      KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            View view = window.getActiveView();
            if (view != null) {
                window.closeView(view);
            }
        }
    };

    /**
     * @return  an action that joins the split window
     */
    public AbstractAction getSplitWindowNone() {
        return splitWindowNone;
    }

    private AbstractAction splitWindowNone = new AbstractAction() {
        {
            putValues(_("Main.Window.SplitWindowNone"), null, Resources.getIcon("split-none.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean b = state.getSplitOrientation() != MainWindowState.SplitOrientation.NONE;
            state.setSplitOrientation(MainWindowState.SplitOrientation.NONE);
            if (b) {
                window.removeSplit();
            }

            updateSplitState();
        }
    };

    /**
     * @return  an action that splits the window horizontally
     */
    public AbstractAction getSplitWindowHorizontally() {
        return splitWindowHorizontally;
    }

    private AbstractAction splitWindowHorizontally = new AbstractAction() {
        {
            putValues(_("Main.Window.SplitWindowHorizontally"), null,
                    Resources.getIcon("split-horizontal.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean b = state.getSplitOrientation() == MainWindowState.SplitOrientation.NONE;
            state.setSplitOrientation(MainWindowState.SplitOrientation.HORIZONTAL);
            if (b) {
                window.addSplit();
            }

            updateSplitState();
        }
    };

    /**
     * @return  an action that splits the window vertically
     */
    public AbstractAction getSplitWindowVertically() {
        return splitWindowVertically;
    }

    private AbstractAction splitWindowVertically = new AbstractAction() {
        {
            putValues(_("Main.Window.SplitWindowVertically"), null,
                    Resources.getIcon("split-vertical.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            boolean b = state.getSplitOrientation() == MainWindowState.SplitOrientation.NONE;
            state.setSplitOrientation(MainWindowState.SplitOrientation.VERTICAL);
            if (b) {
                window.addSplit();
            }

            updateSplitState();
        }
    };

    private void updateSplitState() {
        splitWindowNone.putValue(AbstractAction.SELECTED_KEY,
                state.getSplitOrientation() == MainWindowState.SplitOrientation.NONE);
        splitWindowHorizontally.putValue(AbstractAction.SELECTED_KEY,
                state.getSplitOrientation() == MainWindowState.SplitOrientation.HORIZONTAL);
        splitWindowVertically.putValue(AbstractAction.SELECTED_KEY,
                state.getSplitOrientation() == MainWindowState.SplitOrientation.VERTICAL);
    }


    /**
     * @param window
     * 
     * @return  an action that raises the specified main window
     */
    public AbstractAction getRaiseWindow(Window window) {
        return new RaiseWindow(window);
    }
    
    private class RaiseWindow extends AbstractAction {
        private Window window;

        private RaiseWindow(Window window) {
            super(window.getTitle());
            this.window = window;
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            this.window.raise();
        }
    }

    /**
     * @return  an action that shows the about dialog
     */
    public AbstractAction getShowAboutDialog() {
        return showAboutDialog;
    }

    private AbstractAction showAboutDialog = new AbstractAction() {
        {
            putValues(_("Main.Help.About"));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.showAboutDialog(window);
        }
    };
    
}
