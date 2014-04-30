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

/**
 * The position of a view, consisting of split and tab indices. 
 *
 * @author Dominic Sacré
 */
public class ViewPosition {

    /**
     * The default split index.
     */
    public static final int DEFAULT_SPLIT = -1;

    /**
     * The default tab index.
     */
    public static final int DEFAULT_TAB = -1;

    /**
     * The default position when inserting a view.
     */
    public static final ViewPosition DEFAULT = new ViewPosition(DEFAULT_SPLIT, DEFAULT_TAB);


    /**
     * the split index
     */
    public int split;

    /**
     * the tab index
     */
    public int tab;


    /**
     * @param split
     * @param tab
     */
    public ViewPosition(int split, int tab) {
        this.split = split;
        this.tab = tab;
    }

    /**
     * @param pos
     */
    public ViewPosition(ViewPosition pos) {
        this.split = pos.getSplit();
        this.tab = pos.getTab();
    }

    /**
     * @return  the split index
     */
    public int getSplit() {
        return split;
    }

    /**
     * @param split
     *          the new split index
     */
    public void setSplit(int split) {
        this.split = split;
    }

    /**
     * @return  the tab index
     */
    public int getTab() {
        return tab;
    }

    /**
     * @param tab
     *          the new tab index
     */
    public void setTab(int tab) {
        this.tab = tab;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        ViewPosition pos = (ViewPosition)obj;

        return (split == pos.getSplit() && tab == pos.getTab());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + split;
        hash = 31 * hash + tab;
        return hash;
    }

    @Override
    public String toString() {
        return getClass().getName() + "[split=" + split + ",tab=" + tab + "]";
   }

}