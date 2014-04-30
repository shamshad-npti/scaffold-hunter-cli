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

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.view.View;

/**
 * The context menu for view tabs.
 * 
 * @author Dominic Sacré
 */
public class TabContextMenu extends JPopupMenu {

    /**
     * @param session
     * @param viewManager
     * @param window
     * @param view
     * @param tabLocation
     */
    public TabContextMenu(GUISession session, ViewManager viewManager, MainWindow window, View view, Point tabLocation) {
        TabActions actions = new TabActions(session, viewManager, window, view, tabLocation);

        JMenuItem item = new JMenuItem(actions.getRename());
        item.setText(_("Tab.Rename"));
        add(item);

        JMenu submenu = new JMenu(_("Tab.MoveToWindow"));

        for (Window w : viewManager.getWindows()) {
            AbstractAction a = actions.getMoveToWindow(w);
            a.setEnabled(w != window);
            submenu.add(a);
        }

        submenu.addSeparator();

        submenu.add(actions.getMoveToNewWindow());

        add(submenu);

        submenu = new JMenu(_("Tab.MoveToTabPane"));

        for (int n = 0; n < 2; ++n) {
            AbstractAction a = actions.getMoveToTabPane(n);
            submenu.add(a);
        }

        add(submenu);

        item = new JMenuItem(actions.getDuplicate());
        item.setText(_("Tab.Duplicate"));
        add(item);

        add(actions.getClose());
    }
}
