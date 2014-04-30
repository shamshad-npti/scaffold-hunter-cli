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

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.view.View;

/**
 * Displays a subset hierarchy as a tree.
 * 
 * @author Thorsten Flügel
 * @author Dominic Sacré
 */
public class SubsetTree extends JTree implements SubsetController.SubsetChangeListener {

    private final GUISession session;
    private final MainWindow window;
    private final SubsetController subsetManager;

    /**
     * @param session
     *          the GUI session
     * @param viewManager
     *          the view manager
     * @param window
     *          the main window
     */
    public SubsetTree(GUISession session, ViewManager viewManager, MainWindow window) {
        super(new SubsetTreeModel(session.getSubsetController(), window));

        this.session = session;
        this.window = window;
        this.subsetManager = session.getSubsetController();

        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

        setShowsRootHandles(true);

        // disable node expanding/closing on double click
        setToggleClickCount(0);

        setBorder(new EmptyBorder(2, 2, 2, 2));

        ToolTipManager.sharedInstance().registerComponent(this);

        setCellRenderer(new SubsetTreeCellRenderer(viewManager, window));

        setupListeners();

        // select the root subset. this must be done after the selection
        // listener has been registered to ensure that the actions are
        // updated appropriately
        setSelectionRow(0);
    }

    /**
     * Performs cleanup to ensure that the object can be garbage-collected.
     */
    public void destroy() {
        cleanupListeners();
    }

    private void setupListeners() {
        subsetManager.addSubsetChangeListener(this);

        addMouseListener(mouseListener);
    }

    private void cleanupListeners() {
        subsetManager.removeSubsetChangeListener(this);
    }

    private MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
            repaint();
            if (ev.getButton() == MouseEvent.BUTTON1 && ev.getClickCount() == 2) {
                View view = window.getActiveView();
                if (view != null) {
                    Subset subset = (Subset)getLastSelectedPathComponent();
    
                    if (subset != null) {
                        view.setSubset(subset);
                    }
                }
            }
            else if (ev.getButton() == MouseEvent.BUTTON3 && ev.getClickCount() == 1) {
                final TreePath path = getClosestPathForLocation(ev.getX(), ev.getY());
    
                if (path != null) {
                    addSelectionPath(path);

                    Subset subset = (Subset)path.getLastPathComponent();

                    List<Subset> selectedSubsets = Lists.newArrayList();
                    for (TreePath p : getSelectionPaths()) {
                        Subset s = (Subset)p.getLastPathComponent();
                        selectedSubsets.add(s);
                    }

                    Point pt = getRowBounds(getRowForPath(path)).getLocation();
                    pt.x = 0;
                    SwingUtilities.convertPointToScreen(pt, SubsetTree.this);

                    SubsetActions contextActions = new SubsetActions(session, session.getViewManager(), window,
                                                                     subset, selectedSubsets, pt, window.getActiveView());
                    JPopupMenu menu = new SubsetContextMenu(contextActions);
                    menu.addPopupMenuListener(new PopupMenuListener() {
                        @Override
                        public void popupMenuWillBecomeVisible(PopupMenuEvent e) {}
                        @Override
                        public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
                        @Override
                        public void popupMenuCanceled(PopupMenuEvent e) {
                            removeSelectionPath(path);
                        }
                    });
                    menu.show(ev.getComponent(), ev.getX(), ev.getY());
                }
            }
        }
    };

    @Override
    public String convertValueToText(Object value, boolean selected, boolean expanded,
                                     boolean leaf, int row, boolean hasFocus) {
        Subset s = (Subset)value;
        return s.getTitle();
    }

    @Override
    public void subsetAdded(Subset subset) {
        TreePath path = ((SubsetTreeModel)getModel()).getPathToSubset(subset);
        expandPath(path);
        scrollPathToVisible(path);

        ((SubsetTreeCellRenderer)getCellRenderer()).startHighlighting(this, subset);
    }
    
    @Override
    public void subsetsAdded(Iterable<Subset> subsets) {
        for (Subset s : subsets) {
            TreePath path = ((SubsetTreeModel)getModel()).getPathToSubset(s);
            expandPath(path);
            ((SubsetTreeCellRenderer)getCellRenderer()).startHighlighting(this, s);
        }
    }

    @Override
    public void subsetRemoved(Subset subset) {
    }

    @Override
    public void subsetsRemoved(Iterable<Subset> subsets) {
    }
    
    @Override
    public void subsetChanged(Subset subset) {
    }


}
