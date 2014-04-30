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

package edu.udo.scaffoldhunter.view;

import javax.swing.Icon;
import javax.swing.JComponent;

/**
 * An item to be included in the side bar.
 * 
 * @author Dominic Sacré
 */
public class SideBarItem {

    private final String name;
    private final Icon icon;
    private JComponent component;
    private final boolean expandedByDefault;

    /**
     * @param name
     *          a string describing the item
     * @param icon
     *          an icon to be shown above the component
     * @param component
     *          the actual component
     */
    public SideBarItem(String name, Icon icon, JComponent component) {
        this(name, icon, component, true);
    }

    /**
     * @param name
     *          a string describing the item
     * @param icon
     *          an icon to be shown above the component
     * @param component
     *          the component. if this is null the side bar item will be hidden
     * @param expandedByDefault
     *          if this sidebar item is expanded by default
     */
    public SideBarItem(String name, Icon icon, JComponent component, boolean expandedByDefault) {
        this.name = name;
        this.icon = icon;
        this.component = component;
        this.expandedByDefault = expandedByDefault;
    }

    /**
     * @param component
     *          the component. if this is null the side bar item will be hidden
     */
    public void setComponent(JComponent component) {
        this.component = component;
    }

    /**
     * @return  a string describing the item
     */
    public String getName() {
        return name;
    }
    
    /**
     * @return  an icon to be shown above the component
     */
    public Icon getIcon() {
        return icon;
    }
    
    /**
     * @return  the actual component
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * @return  if this sidebar item is expanded by default
     */
    public boolean getExpandedByDefault() {
        return expandedByDefault;
    }

}