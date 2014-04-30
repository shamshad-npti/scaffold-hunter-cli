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

import java.util.List;

import com.google.common.collect.ImmutableList;

import edu.udo.scaffoldhunter.util.ObjectWithProperties;

/**
 * @author Dominic Sacré
 */
public class ViewExternalState extends ObjectWithProperties {

    /**
     * The side bar items expanded property name
     */
    public static final String SIDE_BAR_ITEMS_EXPANDED_PROPERTY = "sideBarItemsExpanded";
    /**
     * The tab title property name
     */
    public static final String TAB_TITLE_PROPERTY = "tabTitle";
    

    private ImmutableList<Boolean> sideBarItemsExpanded = null;
    private String tabTitle = null;

    /**
     * @return  a list of boolean values reflecting the visibility of each
     *          side bar item
     */
    public ImmutableList<Boolean> getSideBarItemsExpanded() {
        return sideBarItemsExpanded;
    }

    /**
     * @param expanded
     *          a list of boolean values reflecting the visibility of each
     *          side bar item
     */
    public void setSideBarItemsExpanded(List<Boolean> expanded) {
        List<Boolean> oldSideBarItemsExpanded = sideBarItemsExpanded;
        sideBarItemsExpanded = ImmutableList.copyOf(expanded);
        firePropertyChange(SIDE_BAR_ITEMS_EXPANDED_PROPERTY, oldSideBarItemsExpanded, sideBarItemsExpanded);
    }

    /**
     * @return  the tab name
     */
    public String getTabTitle() {
        return tabTitle;
    }

    /**
     * @param title
     *          the new tab name
     */
    public void setTabTitle(String title) {
        String oldTabTitle = tabTitle;
        tabTitle = title;
        firePropertyChange(TAB_TITLE_PROPERTY, oldTabTitle, tabTitle);
    }

}
