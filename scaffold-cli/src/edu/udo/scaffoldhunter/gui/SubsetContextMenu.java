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

import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * The context menu for the subset bar.
 * 
 * @author Dominic Sacré
 */
public class SubsetContextMenu extends JPopupMenu {

    /**
     * @param subsetActions
     *          the available subset actions
     */
    public SubsetContextMenu(SubsetActions subsetActions) {
        add(subsetActions.getShowInCurrentView());

        JMenu submenu = new JMenu(_("Subset.ShowInNewView"));
        add(submenu);
        for (Class<? extends View> klass : ViewClassRegistry.getClasses()) {
            submenu.add(subsetActions.getShowInNewView(klass, 0));
        }

//        submenu = new JMenu(_("Subset.ShowInNewView2"));
//        add(submenu);
//        for (Class<? extends View> klass : ViewClassRegistry.getClasses()) {
//            submenu.add(subsetActions.getShowInNewView(klass, 1));
//        }

        submenu = new JMenu(_("Subset.ShowInNewWindow"));
        add(submenu);
        for (Class<? extends View> klass : ViewClassRegistry.getClasses()) {
            submenu.add(subsetActions.getShowInNewWindow(klass));
        }


        /*
         * selection actions
         */
        addSeparator();
        add(subsetActions.getAddToSelection());
        add(subsetActions.getRemoveFromSelection());
        add(subsetActions.getReplaceSelection());

        /*
         * multiple subsets comparison actions
         */
        addSeparator();
        add(subsetActions.getMakeUnion());
        add(subsetActions.getMakeIntersection());
        add(subsetActions.getMakeDifference());
        
        /*
         * single subset filters
         */
        addSeparator();
        add(subsetActions.getFilter());
        add(subsetActions.getGenerateRandomSubset());
        add(subsetActions.getSubsetFromRing());

        /*
         * subset modifications
         */
        addSeparator();
        add(subsetActions.getRename());
        add(subsetActions.getEditComment());
        add(subsetActions.getDelete());

        /*
         * export
         */
        addSeparator();
        add(subsetActions.getExport());
    }

}
