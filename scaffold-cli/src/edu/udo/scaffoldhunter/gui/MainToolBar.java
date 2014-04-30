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

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;


/**
 * The tool bar of the Scaffold Hunter main window.
 * 
 * @author Dominic Sacré
 */
public class MainToolBar extends JPanel {

    private JToolBar commonToolBar;
    private JToolBar viewToolBar;

    /**
     * @param actions
     *          the actions used to populate the tool bar
     */
    public MainToolBar(Actions actions) {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        commonToolBar = new JToolBar();
        commonToolBar.setFloatable(false);
        add(commonToolBar);

        JToggleButton b = new JToggleButton(actions.getToggleSideBar());
        b.setHideActionText(true);
        commonToolBar.add(b);

        b = new JToggleButton(actions.getToggleSubsetBar());
        b.setHideActionText(true);
        commonToolBar.add(b);

        commonToolBar.addSeparator(new Dimension(12, 32));

        // add a dummy tool bar to fill any empty space to the right of the
        // actual tool bars, and to push the tool bars all the way to the
        // left. this wouldn't be needed if BoxLayout didn't suck, and, as
        // usual, probably could be solved with a GridBagLayout...
        JToolBar dummyToolBar = new JToolBar();
        dummyToolBar.add(Box.createHorizontalStrut(10000));
        dummyToolBar.setFloatable(false);
        add(dummyToolBar);
    }
    
    /**
     * Changes the view-specific tool bar, replacing any previous one.
     * 
     * @param toolBar
     *          the new view-specific tool bar, or null to remove the current
     *          one
     */
    public void setViewSpecificToolBar(JToolBar toolBar) {
        // remove any view-specific tool bar
        if (viewToolBar != null) {
            remove(viewToolBar);
        }

        viewToolBar = toolBar;

        // add new view-specific tool bar, if available
        if (viewToolBar != null) {
            add(viewToolBar, 1);
        }

        validate();
        repaint();
    }
}
