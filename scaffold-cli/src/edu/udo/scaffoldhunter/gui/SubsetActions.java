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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.List;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;

import com.google.common.base.Joiner;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.gui.dialogs.ExportDialog;
import edu.udo.scaffoldhunter.gui.dialogs.RandomSubsetDialog;
import edu.udo.scaffoldhunter.gui.dialogs.RenameDialog;
import edu.udo.scaffoldhunter.gui.dialogs.SubsetFromRingDialog;
import edu.udo.scaffoldhunter.gui.filtering.FilterDialog;
import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Subsets;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * All {@link Action}s that are related to {@link Subsets}
 *  
 * @author Dominic Sacré
 * @author Andrey Zhylka
 * @author Shamshad Alam
 */
public class SubsetActions {

    private final GUISession session;
    private final ViewManager viewManager;
    private final SubsetController subsetController;
    private final Selection selection;
    private final DbManager db;
    private final MainWindow window;

    private Subset subset;
    private Iterable<Subset> selectedSubsets;
    private Point subsetLocation;

    /**
     * @param session
     *            the GUI session
     * @param viewManager
     *            the view manager
     * @param window
     *            the main window
     * @param subset
     *            the subset the actions refer to
     * @param selectedSubsets
     *            all selected subsets, may be null
     * @param subsetLocation
     *            the coordinates of the subset in the subset tree, may be null
     * @param view
     *            the active view, may be null
     */
    public SubsetActions(GUISession session, ViewManager viewManager, MainWindow window,
                         Subset subset, Iterable<Subset> selectedSubsets, Point subsetLocation,
                         View view) {
        this.session = session;
        this.viewManager = viewManager;
        this.subsetController = session.getSubsetController();
        this.selection = session.getSelection();
        this.db = session.getDbManager();
        this.window = window;

        this.subsetLocation = subsetLocation;

        updateContext(subset, selectedSubsets, view);
    }

    /**
     * Updates the subset actions' context.
     * 
     * @param subset
     *            the subset the actions refer to
     * @param selectedSubsets
     *            all selected subsets, may be null
     * @param view
     *            the active view
     */
    public void updateContext(Subset subset, Iterable<Subset> selectedSubsets, View view) {
        if (selectedSubsets == null) {
            List<Subset> l = Lists.newArrayList();
            l.add(subset);
            selectedSubsets = l;
        }

        this.subset = subset;
        this.selectedSubsets = selectedSubsets;

        showInCurrentView.setEnabled(subset != null && view != null);

        Icon icon = view != null ? ViewClassRegistry.getClassIcon(view.getClass()) : null;
        showInCurrentView.putValue(Action.SMALL_ICON, icon);

        addToSelection.setEnabled(subset != null);
        removeFromSelection.setEnabled(subset != null);
        replaceSelection.setEnabled(subset != null);
        filter.setEnabled(subset != null);
        rename.setEnabled(subset != null);
        editComment.setEnabled(subset != null);
        export.setEnabled(subset != null);
        subsetFromRing.setEnabled(subset != null);
        
        // can't delete the root subset
        delete.setEnabled(!Iterables.contains(selectedSubsets, session.getRootSubset()));

        // need at least two selected subsets for set operation
        boolean enableSetOps = (Iterables.size(selectedSubsets) > 1);

        makeUnion.setEnabled(enableSetOps);
        makeIntersection.setEnabled(enableSetOps);
        makeDifference.setEnabled(enableSetOps);
    }

    private Point dialogLocation(Point subsetLocation) {
        if (subsetLocation != null) {
            Point location = (Point) subsetLocation.clone();
            location.translate(-25, 40);
            return location;
        } else {
            return null;
        }
    }


    /**
     * @return an action that changes the subset shown in the currently selected
     *         view
     */
    public AbstractAction getShowInCurrentView() {
        return showInCurrentView;
    }

    private AbstractAction showInCurrentView = new AbstractAction() {
        {
            putValues(_("Subset.ShowInCurrentView"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            View view = window.getActiveView();
            if (view != null) {
                view.setSubset(subset);
            }
        }
    };

    /**
     * @param klass
     * @param split
     * @return an action that adds a new view showing the selected subset
     */
    public AbstractAction getShowInNewView(Class<? extends View> klass, int split) {
        return new ShowInNewView(klass, split);
    }

    private class ShowInNewView extends AbstractAction {
        private Class<? extends View> klass;
        private int split;

        private ShowInNewView(Class<? extends View> klass, int split) {
            super(ViewClassRegistry.getClassName(klass), ViewClassRegistry.getClassIcon(klass));
            this.klass = klass;
            this.split = split;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            View view = viewManager.createView(klass, subset);

            if (split == 1 && window.getState().getSplitOrientation() == MainWindowState.SplitOrientation.NONE) {
                window.getState().setSplitOrientation(MainWindowState.SplitOrientation.HORIZONTAL);
            }
            window.addView(view, split);
            window.selectView(view);
        }
    }

    /**
     * @param klass
     * @return an action that opens a new window and adds a new view showing the
     *         selected subset,
     */
    public AbstractAction getShowInNewWindow(Class<? extends View> klass) {
        return new ShowInNewWindow(klass);
    }

    private class ShowInNewWindow extends AbstractAction {
        private Class<? extends View> klass;

        private ShowInNewWindow(Class<? extends View> klass) {
            super(ViewClassRegistry.getClassName(klass), ViewClassRegistry.getClassIcon(klass));
            this.klass = klass;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            MainWindow w = session.getGUIController().createWindow();

            View view = viewManager.createView(klass, subset);

            viewManager.addView(view, w);

            w.setVisible(true);
        }
    }

    /**
     * @return an action that generates random subset with user defined size
     */
    public AbstractAction getGenerateRandomSubset() {
        return generateRandomSubset;
    }

    private AbstractAction generateRandomSubset = new AbstractAction() {
        {
            putValues(_("Subset.GenerateRandomSubset.Short"), _("Subset.GenerateRandomSubset.Description"),
                    KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            RandomSubsetDialog sizeDlg = new RandomSubsetDialog(window.getFrame(), 
                    subset.size()/*, location, SizeDialog.Anchor.TOP_RIGHT*/);

            sizeDlg.setVisible(true);

            final Integer newSize = sizeDlg.getNewSize();

            if (newSize == null) {
                return;
            }

            Subset newSubset = subsetController.createRandomSubset(subset, newSize);
            
            subsetController.addSubset(newSubset);
        }
    };

    /**
     * @return an action that adds all molecules in the subset to the selection
     */
    public AbstractAction getAddToSelection() {
        return addToSelection;
    }

    private AbstractAction addToSelection = new AbstractAction() {
        {
            putValues(_("Subset.AddToSelection"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (Subset s : selectedSubsets) {
                selection.addAll(s);
            }
        }
    };

    /**
     * @return an action that removes all molecules in the subset from the
     *         selection
     */
    public AbstractAction getRemoveFromSelection() {
        return removeFromSelection;
    }

    private AbstractAction removeFromSelection = new AbstractAction() {
        {
            putValues(_("Subset.RemoveFromSelection"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (Subset s : selectedSubsets) {
                selection.removeAll(s);
            }
        }
    };

    /**
     * @return an action that replaces the selection with the given subset
     */
    public AbstractAction getReplaceSelection() {
        return replaceSelection;
    }

    private AbstractAction replaceSelection = new AbstractAction() {
        {
            putValues(_("Subset.ReplaceSelection"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            selection.clear();
            for (Subset s : selectedSubsets) {
                selection.addAll(s);
            }
        }
    };

    /**
     * @return an action that creates the union of the selected subsets.
     */
    public AbstractAction getMakeUnion() {
        return makeUnion;
    }

    private AbstractAction makeUnion = new AbstractAction() {
        {
            putValues(_("Subset.MakeUnion"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RenameDialog dlg = new RenameDialog(window.getFrame(), _("Subset.NewSubsetName"),
                    subsetController.defaultUnionName(selectedSubsets), dialogLocation(subsetLocation),
                    RenameDialog.Anchor.TOP_RIGHT);
            dlg.setVisible(true);

            String newTitle = dlg.getNewText();

            if (newTitle != null) {
                Subset newSubset = subsetController.createUnion(null, newTitle, selectedSubsets);
                subsetController.addSubset(newSubset);
            }
        }
    };

    /**
     * @return an action that creates the intersection of the selected subsets.
     */
    public AbstractAction getMakeIntersection() {
        return makeIntersection;
    }

    private AbstractAction makeIntersection = new AbstractAction() {
        {
            putValues(_("Subset.MakeIntersection"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Subset newSubset = subsetController.createIntersection(null, null, selectedSubsets);

            if (newSubset.size() == 0) {
                JOptionPane.showMessageDialog(window.getFrame(), _("Subset.EmptyErrorMessage"),
                        _("Subset.EmptyErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            RenameDialog dlg = new RenameDialog(window.getFrame(), _("Subset.NewSubsetName"),
                    subsetController.defaultIntersectionName(selectedSubsets), dialogLocation(subsetLocation),
                    RenameDialog.Anchor.TOP_RIGHT);
            dlg.setVisible(true);

            String newTitle = dlg.getNewText();

            if (newTitle != null) {
                newSubset.setTitle(newTitle);
                subsetController.addSubset(newSubset);
            }
        }
    };

    /**
     * @return an action that creates the difference of the selected subsets.
     */
    public AbstractAction getMakeDifference() {
        return makeDifference;
    }

    private AbstractAction makeDifference = new AbstractAction() {
        {
            putValues(_("Subset.MakeDifference"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Iterable<Subset> otherSubsets = Iterables.filter(selectedSubsets,
                    Predicates.not(Predicates.equalTo(subset)));

            Subset newSubset = subsetController.createDifference(null, null, subset, otherSubsets);

            if (newSubset.size() == 0) {
                JOptionPane.showMessageDialog(window.getFrame(), _("Subset.EmptyErrorMessage"),
                        _("Subset.EmptyErrorTitle"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            RenameDialog dlg = new RenameDialog(window.getFrame(), _("Subset.NewSubsetName"),
                    subsetController.defaultDifferenceName(subset, otherSubsets), dialogLocation(subsetLocation),
                    RenameDialog.Anchor.TOP_RIGHT);
            dlg.setVisible(true);

            String newTitle = dlg.getNewText();

            if (newTitle != null) {
                newSubset.setTitle(newTitle);
                subsetController.addSubset(newSubset);
            }
        }
    };

    /**
     * @return an action that opens a filter dialog and filters the given
     *         subset.
     */
    public AbstractAction getFilter() {
        return filter;
    }

    private AbstractAction filter = new AbstractAction() {
        {
            putValues(_("Subset.Filter"), null, Resources.getIcon("filter.png"), null, null);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            FilterDialog fdlg = new FilterDialog(window.getFrame(), db, session.getDbSession().getProfile(),
                    session.getDbSession().getDataset(), subset);

            fdlg.setModal(true);
            fdlg.setVisible(true);
            if (fdlg.getResult()) {
                Subset newSubset = subsetController.createFilteredSubset(subset, fdlg.getSelectedFilterset());

                RenameDialog dlg = new RenameDialog(window.getFrame(), _("Subset.NewSubsetName"),
                        subsetController.defaultFilterName(subset, fdlg.getSelectedFilterset()),
                        dialogLocation(subsetLocation), RenameDialog.Anchor.TOP_RIGHT);
                dlg.setVisible(true);

                String newTitle = dlg.getNewText();

                if (newTitle != null) {
                    newSubset.setTitle(newTitle);
                    subsetController.addSubset(newSubset);
                }
            }
        }
    };

    /**
     * @return an action that renames the given subset.
     */
    public AbstractAction getRename() {
        return rename;
    }

    private AbstractAction rename = new AbstractAction() {
        {
            putValues(_("Subset.Rename"), null, Resources.getIcon("edit.png"), null, null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RenameDialog dlg = new RenameDialog(window.getFrame(), _("RenameSubset.Title"), subset.getTitle(),
                    dialogLocation(subsetLocation), RenameDialog.Anchor.TOP_RIGHT);
            dlg.setVisible(true);

            String newTitle = dlg.getNewText();

            if (newTitle != null) {
                subsetController.renameSubset(subset, newTitle);
            }
        }
    };

    /**
     * @return an action that changes the subset's comment.
     */
    public AbstractAction getEditComment() {
        return editComment;
    }

    private AbstractAction editComment = new AbstractAction() {
        {
            putValues(_("Subset.EditComment"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RenameDialog dlg = new RenameDialog(window.getFrame(), _("EditSubsetComment.Title"), subset.getComment(),
                    dialogLocation(subsetLocation), RenameDialog.Anchor.TOP_RIGHT);
            dlg.setAllowEmpty(true);
            dlg.setVisible(true);

            String newComment = dlg.getNewText();

            if (newComment != null) {
                String comment = newComment.equals("") ? null : newComment;
                subsetController.changeSubsetComment(subset, comment);
            }
        }
    };

    /**
     * @return an action that deletes the given subset.
     */
    public AbstractAction getDelete() {
        return delete;
    }

    private AbstractAction delete = new AbstractAction() {
        {
            putValues(_("Subset.Delete"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // make a list of all views currently showing one of the subsets
            // to be deleted
            List<View> viewsToBeClosed = Lists.newArrayList();

            for (Window w : viewManager.getWindows()) {
                for (View v : viewManager.getViews(w)) {
                    if (Iterables.contains(selectedSubsets, v.getSubset())) {
                        viewsToBeClosed.add(v);
                    }
                }
            }

            // build a string containing descriptions of all the views that
            // would need to be closed
            StringBuilder sb = new StringBuilder();

            for (View v : viewsToBeClosed) {
                sb.append(_("Subset.ConfirmDeleteViewDescription",
                        viewManager.getViewWindow(v).getNumber(),
                        viewManager.getViewState(v).getTabTitle(),
                        ViewClassRegistry.getClassName(v.getClass())
                ));
            }
            String viewDescriptions = sb.toString();

            // build a sting containing the names of all subsets to be deleted
            String subsetNames = Joiner.on("\n").join(
                    Iterables.transform(selectedSubsets, Subsets.getSubsetTitleFunction)
            );

            // format the dialog's message string
            String message;
            if (viewsToBeClosed.isEmpty()) {
                message = _("Subset.ConfirmDeleteMessage", subsetNames);            
            } else {
                message = _("Subset.ConfirmDeleteCloseViewsMessage", subsetNames, viewDescriptions);            
            }

            // ask the user
            JTextArea textArea = new JTextArea (message);
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setPreferredSize(new Dimension(400, 400));
            int result = JOptionPane.showConfirmDialog(window.getFrame(), scrollPane, _("Subset.ConfirmDeleteTitle"),
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            
            if (result == JOptionPane.OK_OPTION) {
                // delete subsets
                subsetController.removeSubsets(selectedSubsets);

                // close views
                for (View v : viewsToBeClosed) {
                    viewManager.removeView(v);
                }
            }
        }
    };

    /**
     * @return starts Export of subset
     */
    public AbstractAction getExport() {
        return export;
    }

    private AbstractAction export = new AbstractAction() {
        {
            putValues(_("Subset.Export"), null, Resources.getIcon("save.png"), null, null);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ExportDialog exDiag = new ExportDialog(subset, db, window);
            exDiag.setVisible(true);

        }
    };
    
    /**
     * Action that allow to create a subset at specified scaffold ring level.
     * All the molecules at that ring and from entire subtree of the scaffold 
     * will be saved in a new subset.
     * @return Action for subset from ring
     */
    public AbstractAction getSubsetFromRing() {
        return subsetFromRing;
    }
    
    private AbstractAction subsetFromRing = new AbstractAction() {
        {
            putValue(AbstractAction.NAME, I18n.get("ScaffoldTreeView.SubsetFromRing"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("ScaffoldTreeView.SubsetFromRing"));
            putValue(Action.SMALL_ICON, Resources.getIcon("make_subset_arrow.png"));
        }

        @Override
        public void actionPerformed(ActionEvent evt) {
            // Get scaffold tree of current subset
            final Frame parent = window.getFrame();

            Scaffold scaffold = DBExceptionHandler.callDBManager(db, new DBFunction<Scaffold>() {
                @Override
                public Scaffold call() throws DatabaseException {
                    return db.getScaffolds(subset, false);
                }
            });

            int firstRingCount = scaffold.getHierarchyLevel() + 1;
            final List<List<Scaffold>> ringList = Lists.newArrayList();
            
            // find first level appropriate for splitting
            List<Scaffold> children = scaffold.getChildren();
            while (children.size() == 1) {
                firstRingCount++;
                children = getChildren(children);
            }            
            while( !children.isEmpty() ) {
                ringList.add(children);
                children = getChildren(children);
            }

            final int minRingCount = firstRingCount;
            int maxRingCount =  minRingCount + ringList.size() - 1;

            final SubsetFromRingDialog subsetDialog = 
                    new SubsetFromRingDialog(parent, minRingCount, maxRingCount);
            
            int ret = subsetDialog.showDialog();
            
            if(ret == JOptionPane.OK_OPTION) {
                final int ringLevel = subsetDialog.getRingLevel();
                final String name = subsetDialog.getNewText() + ' '
                        + String.format(_("ScaffoldTreeView.SubsetFromRing.Level"), ringLevel);

                Iterable<Subset> subsets = subsetController.subsetsFromSubtrees(subset, name, ringList.get(ringLevel - minRingCount));
                Subset union = subsetController.createUnion(subset, name, subsets);

                for (Subset s : subsets) {
                    s.setParent(union);
                }
                subsetController.addSubsets(Iterables.concat(subsets, Collections.singleton(union)));
            }
        }
                
        /**
         * return a list that contains all the first level children of the 
         * scaffolds and return empty list if none of scaffold has children
         * @param scaffolds
         * @return List of first level children in scaffold
         */
        private List<Scaffold> getChildren(List<Scaffold> scaffolds) {
            List<Scaffold> list = Lists.newArrayList();
            for (Scaffold scaffold : scaffolds) {
                list.addAll(scaffold.getChildren());
            }
            return list;
        }
    };
}
