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

import java.util.List;

import javax.swing.DefaultButtonModel;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * The menu bar of a the Scaffold Hunter main window.
 * 
 * @author Dominic Sacré
 */
public class MainMenuBar extends JMenuBar implements ViewManager.WindowsChangeListener {

    private final ViewManager viewManager;
    private final MainWindow window;
    private final Actions actions;
    private final SubsetActions subsetActions;
    private final TabActions tabActions;
    private final SelectionActions selectionActions;

    private JMenu viewSpecificMenu;
    private JMenu windowMenu;

    /**
     * @param viewManager
     *            the view manager
     * @param window
     *            the main window this menu bar belongs to
     * @param actions
     *            the actions used to populate the menu
     * @param subsetActions
     *            the subset actions used to populate the menu
     * @param tabActions
     *            the tab actions used to populate the menu
     * @param selectionActions
     *            the selection actions used to populate the menu
     */
    public MainMenuBar(ViewManager viewManager, MainWindow window, Actions actions,
                       SubsetActions subsetActions, TabActions tabActions, SelectionActions selectionActions) {
        this.viewManager = viewManager;
        this.window = window;
        this.actions = actions;
        this.subsetActions = subsetActions;
        this.tabActions = tabActions;
        this.selectionActions = selectionActions;

        add(createSessionMenu());
        add(createSelectionMenu());
        add(createSubsetMenu());
        add(windowMenu = createWindowMenu(null));
        add(createHelpMenu());

        viewManager.addWindowsChangeListener(this);
    }

    /**
     * Performs cleanup to ensure that the object can be garbage-collected.
     */
    public void destroy() {
        viewManager.removeWindowsChangeListener(this);
    }

    private JMenu createSessionMenu() {
        JMenu menu = new JMenu(_("Main.Session"));

        menu.add(actions.getShowSessionDialog());
        menu.add(actions.getSaveSession());
        menu.addSeparator();
        menu.add(actions.getShowGlobalOptionsDialog());
        menu.add(actions.getShowOptionsDialog());
        menu.add(actions.getShowTooltipDialog());
        menu.addSeparator();
        menu.add(actions.getQuit());

        return menu;
    }

    private JMenu createSelectionMenu() {
        JMenu menu = new JMenu(_("Main.Selection"));

        menu.add(selectionActions.getSelectAll());
        menu.add(selectionActions.getDeselectAll());
        menu.add(selectionActions.getInvert());
        menu.add(selectionActions.getConfineToView());

        menu.addSeparator();

        menu.add(selectionActions.getMakeSubset());
        menu.add(selectionActions.getMakeViewSubset());

        menu.addSeparator();

        menu.add(selectionActions.getAddPublicBanners());
        menu.add(selectionActions.getAddPrivateBanners());
        menu.add(selectionActions.getRemovePublicBanners());
        menu.add(selectionActions.getRemovePrivateBanners());

        return menu;
    }

    private JMenu createSubsetMenu() {
        JMenu menu = new JMenu(_("Main.Subset"));

//        menu.add(subsetActions.getShowInCurrentView());

        JMenu submenu = new JMenu(_("Subset.ShowInNewView"));
        menu.add(submenu);
        for (Class<? extends View> klass : ViewClassRegistry.getClasses()) {
            submenu.add(subsetActions.getShowInNewView(klass, 0));
        }

//        submenu = new JMenu(_("Subset.ShowInNewView2"));
//        menu.add(submenu);
//        for (Class<? extends View> klass : ViewClassRegistry.getClasses()) {
//            submenu.add(subsetActions.getShowInNewView(klass, 1));
//        }

        submenu = new JMenu(_("Subset.ShowInNewWindow"));
        menu.add(submenu);
        for (Class<? extends View> klass : ViewClassRegistry.getClasses()) {
            submenu.add(subsetActions.getShowInNewWindow(klass));
        }

        /*
         * selection actions
         */
        menu.addSeparator();
        menu.add(subsetActions.getAddToSelection());
        menu.add(subsetActions.getRemoveFromSelection());
        menu.add(subsetActions.getReplaceSelection());

        /*
         * These actions work only on multiple subsets, but the subset menu is
         * linked to the subset that is active in the current view. This is only
         * a single subset and therefore this actions will be never enabled.
         */
        /*
         * multiple subsets comparison actions
         */
//        menu.addSeparator();
//        menu.add(subsetActions.getMakeUnion());
//        menu.add(subsetActions.getMakeIntersection());
//        menu.add(subsetActions.getMakeDifference());
        
        /*
         * single subset filters
         */
        menu.addSeparator();
        menu.add(subsetActions.getFilter());
        menu.add(subsetActions.getGenerateRandomSubset());
        menu.add(subsetActions.getSubsetFromRing());

        /*
         * subset modifications
         */
        menu.addSeparator();
        menu.add(subsetActions.getRename());
        menu.add(subsetActions.getEditComment());
        menu.add(subsetActions.getDelete());

        /*
         * export
         */
        menu.addSeparator();
        menu.add(subsetActions.getExport());

        return menu;
    }

    private JMenu createWindowMenu(List<Window> windows) {
        JMenu menu = new JMenu(_("Main.Window"));

        menu.add(new JCheckBoxMenuItem(actions.getToggleSideBar()));
        menu.add(new JCheckBoxMenuItem(actions.getToggleSubsetBar()));
        menu.addSeparator();

        menu.add(actions.getNewWindow());
        menu.add(actions.getCloseWindow());
        menu.addSeparator();

        JMenu submenu = new JMenu(_("Main.Window.AddView"));
        menu.add(submenu);
        // submenu.setIcon(Resources.getIcon("tab-new.png"));
        for (Class<? extends View> klass : ViewClassRegistry.getClasses()) {
            submenu.add(actions.getAddView(klass, 0));
        }


        menu.add(tabActions.getRename());

        submenu = new JMenu(_("Main.Window.MoveTabToWindow"));

        for (Window w : viewManager.getWindows()) {
            AbstractAction a = tabActions.getMoveToWindow(w);
            a.setEnabled(w != window);
            submenu.add(a);
        }

        submenu.addSeparator();

        submenu.add(tabActions.getMoveToNewWindow());

        menu.add(submenu);

        submenu = new JMenu(_("Main.Window.MoveTabToTabPane"));

        for (int n = 0; n < 2; ++n) {
            AbstractAction a = tabActions.getMoveToTabPane(n);
            submenu.add(a);
        }

        menu.add(submenu);

        menu.add(tabActions.getDuplicate());


        menu.add(actions.getCloseView());
        menu.addSeparator();

//        submenu = new JMenu(_("Main.Window.SplitWindow"));
//        menu.add(submenu);
        // submenu.setIcon(Resources.getIcon("view-right-new.png"));
        menu.add(new JRadioButtonMenuItem(actions.getSplitWindowNone()));
        menu.add(new JRadioButtonMenuItem(actions.getSplitWindowHorizontally()));
        menu.add(new JRadioButtonMenuItem(actions.getSplitWindowVertically()));
        menu.addSeparator();

        menu.add(actions.getSelectNextView());
        menu.add(actions.getSelectPrevView());

        // show a list of all frames if there's more than one
        if (windows != null && windows.size() > 1) {
            menu.addSeparator();

            for (Window w : windows) {
                JRadioButtonMenuItem item = new JRadioButtonMenuItem(w.getTitle());

                final boolean selected = (w == window);

                // these menu items always retain their initial selection state
                item.setModel(new DefaultButtonModel() {
                    @Override
                    public boolean isSelected() {
                        return selected;
                    }
                });

                item.setAction(actions.getRaiseWindow(w));
                menu.add(item);
            }
        }

        return menu;
    }

    private JMenu createHelpMenu() {
        JMenu menu = new JMenu(_("Main.Help"));

        menu.add(actions.getShowAboutDialog());

        return menu;
    }

    /**
     * Changes the view-specific menu, replacing any previous one.
     * 
     * @param menu
     *            the view-specific menu
     */
    public void setViewSpecificMenu(JMenu menu) {
        replaceMenu(menu, viewSpecificMenu, 3);
        viewSpecificMenu = menu;
    }

    /**
     * @param windows
     *            the list of main windows currently open
     */
    public void updateWindowMenu(List<Window> windows) {
        JMenu menu = createWindowMenu(windows);
        replaceMenu(menu, windowMenu, -1);
        windowMenu = menu;
    }

    /**
     * Replaces oldMenu with newMenu, inserting the new menu at the same
     * position where the old one used to be. If oldMenu is null, the new menu
     * is inserted at the given index instead.
     * 
     * @param newMenu
     * @param oldMenu
     * @param index
     */
    private void replaceMenu(JMenu newMenu, JMenu oldMenu, int index) {
        if (oldMenu != null) {
            index = getComponentIndex(oldMenu);
            remove(oldMenu);
        }

        if (newMenu != null) {
            add(newMenu, index);
        }

        validate();
        repaint();
    }

    @Override
    public void windowsChanged(List<Window> windows) {
        updateWindowMenu(windows);
    }

}
