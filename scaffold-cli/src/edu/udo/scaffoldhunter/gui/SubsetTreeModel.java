/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See the file README.txt in the root directory of the Scaffold Hunter
 * source tree for details.
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

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Subsets;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;

/**
 * The model behind the subset tree.
 * 
 * @author Dominic Sacré
 */
public class SubsetTreeModel implements TreeModel, SubsetController.SubsetChangeListener {

    private final SubsetController subsetManager;

    private final EventListenerList listeners = new EventListenerList();

    /**
     * @param subsetManager
     *            the subset manager
     * @param window
     *            the main window
     */
    public SubsetTreeModel(SubsetController subsetManager, MainWindow window) {
        this.subsetManager = subsetManager;

        subsetManager.addSubsetChangeListener(this);

        window.addPropertyChangeListener(MainWindow.ACTIVE_SUBSET_PROPERTY, activeSubsetChangeListener);
    }

    private PropertyChangeListener activeSubsetChangeListener = new GenericPropertyChangeListener<Subset>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Subset> ev) {
            Subset oldSubset = ev.getOldValue();
            Subset newSubset = ev.getNewValue();

            if (oldSubset != null) {
                fireTreeNodesChanged(oldSubset);
            }

            if (newSubset != null) {
                fireTreeNodesChanged(newSubset);
            }
        }
    };

    @Override
    public void subsetAdded(Subset subset) {
        fireTreeStrucureChanged(subset.getParent());
    }

    @Override
    public void subsetsAdded(Iterable<Subset> subsets) {
        HashSet<Subset> parents = Sets.newHashSet();
        for (Subset s : subsets) {
            parents.add(s.getParent());
        }
        for (Subset p : parents) {
            fireTreeStrucureChanged(p);
        }
    }

    @Override
    public void subsetRemoved(Subset subset) {
        fireTreeStrucureChanged(subset.getParent());
    }

    @Override
    public void subsetsRemoved(Iterable<Subset> subsets) {
        HashSet<Subset> removedSubsets = Sets.newHashSet(subsets);
        HashSet<Subset> parents = Sets.newHashSet();
        for (Subset s : subsets) {
            Subset parent = s.getParent();
            // we do not need to update parents that are also removed
            if (!removedSubsets.contains(parent)) {
                parents.add(parent);
            }
        }
        for (Subset p : parents) {
            fireTreeStrucureChanged(p);
        }
    }

    @Override
    public void subsetChanged(Subset subset) {
        fireTreeNodesChanged(subset);
    }

    protected void fireTreeStrucureChanged(Subset subset) {
        TreePath path = getPathToSubset(subset);
        TreeModelEvent e = new TreeModelEvent(this, path);

        for (TreeModelListener tml : listeners.getListeners(TreeModelListener.class)) {
            tml.treeStructureChanged(e);
        }
    }

    protected void fireTreeNodesChanged(Subset subset) {
        TreePath path = getPathToSubset(subset);
        TreeModelEvent e = new TreeModelEvent(this, path);

        for (TreeModelListener tml : listeners.getListeners(TreeModelListener.class)) {
            tml.treeNodesChanged(e);
        }
    }

    @Override
    public Object getRoot() {
        return subsetManager.getRootSubset();
    }

    @Override
    public Object getChild(Object parent, int index) {
        return sortedChildren((Subset) parent).get(index);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        return sortedChildren((Subset) parent).indexOf(child);
    }

    @Override
    public int getChildCount(Object parent) {
        return ((Subset) parent).getChildren().size();
    }

    @Override
    public boolean isLeaf(Object node) {
        return ((Subset) node).getChildren().isEmpty();
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(TreeModelListener.class, l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(TreeModelListener.class, l);
    }

    /**
     * @param subset
     * 
     * @return the TreePath leading to the given subset
     */
    public TreePath getPathToSubset(Subset subset) {
        Object[] objs = Lists.reverse(Subsets.getAncestors(subset)).toArray();
        return new TreePath(objs);
    }

    /**
     * @param subset
     * 
     * @return a list of the subset's children, ordered by creation date
     */
    private List<Subset> sortedChildren(Subset subset) {
        List<Subset> list = Lists.newArrayList(subset.getChildren());
        Collections.sort(list, new Comparator<Subset>() {
            @Override
            public int compare(Subset o1, Subset o2) {
                return o1.getCreationDate().compareTo(o2.getCreationDate());
            }
        });
        return list;
    }

}
