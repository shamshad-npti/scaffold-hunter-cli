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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.l2fprod.common.swing.JTaskPane;
import com.l2fprod.common.swing.JTaskPaneGroup;
import com.l2fprod.common.swing.PercentLayout;

import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.view.SideBarItem;
import edu.udo.scaffoldhunter.view.View;

/**
 * The side bar of the Scaffold Hunter main window.
 * 
 * @author Dominic Sacré
 */
public class MainSideBar extends JTaskPane {

    private final ViewManager viewManager;
    private final MainWindow window;

    /**
     * @param viewManager
     *          the view manager
     * @param window
     *          the window this side bar belongs to
     */
    public MainSideBar(ViewManager viewManager, MainWindow window) {
        this.viewManager = viewManager;
        this.window = window;

        // remove borders and reduce size of gaps between items
        setBorder(BorderFactory.createEmptyBorder());
        ((PercentLayout)getLayout()).setGap(8);
    }

    /**
     * Sets the side bar items, replacing any previous items.
     * 
     * @param items
     *          the items to be included in the side bar
     * @param viewState
     *          the state of the current view
     */
    public void setViewSideBarItems(List<SideBarItem> items, ViewExternalState viewState) {
        // remove any previous items
        removeAll();
        
        // add new items
        if (items != null) {
            List<Boolean> expanded = viewState.getSideBarItemsExpanded();

            // FIXME: this is not the right place to do this
            if (expanded == null) {
                expanded = Lists.transform(items, new Function<SideBarItem, Boolean>() {
                    @Override
                    public Boolean apply(SideBarItem item) {
                        return item.getExpandedByDefault();
                    }
                });
                viewState.setSideBarItemsExpanded(expanded);
            }

            int count = 0;
            for (SideBarItem item : items) {
                if (item.getComponent() != null) {
                    add(makeTaskPaneGroup(item, expanded.get(count)));
                }
                ++count;
            }
        }

        // XXX bug in JTaskPane: non-expanded side bar items briefly pop up
        // before being hidden. we delay repainting of the task pane in an
        // attempt to prevent this
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                revalidate();
                // XXX and another apparent JTaskPane bug: something goes
                // horribly awry if newly added JTaskPaneGroups are not
                // expanded, leaving visual artifacts all over the main window.
                // as a workaround, we just repaint the whole frame...
                window.getFrame().repaint();
            }
        });
    }

    private JTaskPaneGroup makeTaskPaneGroup(SideBarItem item, boolean expanded) {
        final JTaskPaneGroup group = new JTaskPaneGroup();

        group.setExpanded(expanded);

        // remove border around component
        ((JPanel)group.getContentPane()).setBorder(BorderFactory.createEmptyBorder());

        group.setTitle(item.getName());
        if (item.getIcon() != null) {
            group.setIcon(item.getIcon());
        }
        group.add(item.getComponent());

        // the index of this newly created task pane group
        int index = getComponentCount();

        // listen for changes to the "expanded" property of each task pane
        // group, and update the view state when this property changes 
        group.addPropertyChangeListener(JTaskPaneGroup.EXPANDED_CHANGED_KEY, new ExpandedChangeListener(index));

        return group;
    }


    private class ExpandedChangeListener extends GenericPropertyChangeListener<Boolean> {
        private int index;

        public ExpandedChangeListener(int index) {
            this.index = index;
        }

        @Override
        public void propertyChange(GenericPropertyChangeEvent<Boolean> ev) {
            View view = window.getActiveView();
            ViewExternalState viewState = viewManager.getViewState(view);

            // make a mutable copy
            List<Boolean> expanded = Lists.newArrayList(viewState.getSideBarItemsExpanded());

            expanded.set(index, ev.getNewValue());
            viewState.setSideBarItemsExpanded(expanded);
        }
    }
    
}
