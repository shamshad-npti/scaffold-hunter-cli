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

package edu.udo.scaffoldhunter.gui.datasetmanagement;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DateFormat;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.collect.Lists;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.gui.util.UnaryDBFunction;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.util.ProgressListener;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * Gui Dialog which supports management of datasets and trees
 * 
 * @author Philipp Lewe
 * @author Till Schäfer
 * 
 */
public class DatasetAndTreeManagerDialog extends JDialog {

    private DatasetManagement controller;
    private JDialog window;

    JButton okButton = StandardButtonFactory.createCloseButton(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    });

    /*
     * Dataset-Section
     */
    private DefaultListModel datasets;

    private JList datasetList;

    private JScrollPane datasetListScrollPane;

    private JPanel datasetSection = new JPanel();

    private JButton newDataset = new JButton(new NewDatasetAction(this));

    private JButton editDataset = new JButton(new EditDatasetAction());

    private JButton calcProperties = new JButton(new CalcPropertiesAction());

    private JButton renameDataset = new JButton(new RenameDatasetAction());

    private JButton deleteDataset = new JButton(new DeleteDatasetAction());

    private JEditorPane datasetInfo = new JEditorPane("text/html",
            _("DatasetAndTreeManager.DatasetSection.DatasetInfo.EmptyText"));

    private JScrollPane datasetInfoScrollPane = new JScrollPane(datasetInfo);

    /*
     * Tree-Section
     */
    private DefaultListModel trees;

    private JList treeList;

    private JScrollPane treeListScrollPane;

    private JPanel treeSection = new JPanel();

    private JButton newTree = new JButton(new NewTreeAction());

    private JButton editTree = new JButton(new EditTreeAction());

    private JButton deleteTree = new JButton(new DeleteTreeAction());

    private JEditorPane treeInfo = new JEditorPane("text/html",
            _("DatasetAndTreeManager.TreeSection.TreeInfo.EmptyText"));

    private JScrollPane treeInfoScrollPane = new JScrollPane(treeInfo);

    /*
     * Prefered Sizes
     */

    private final Dimension prefSizeInfoLabels = new Dimension(0, 0);
    private final Dimension prefSizeLists = new Dimension(0, 0);
    private final Dimension prefSizeButtons = new Dimension(200, 100);
    private final Dimension prefWindowSize = new Dimension(800, 494); // golden ratio

    private final Insets insets = new Insets(2, 2, 2, 2);

    /**
     * Creates a new dialog DatasetAndTreeManager dialog with specified owner
     * dialog
     * 
     * @param owner
     *            the owner of this dialog
     * @param controller
     *            the DatasetManagement controller
     * 
     */
    public DatasetAndTreeManagerDialog(Window owner, DatasetManagement controller) {
        super(owner);
        this.controller = controller;
        window = this;

        initDatasetList();
        initTreeList();
        initGUI();
        loadDatasets();
    }

    private void initGUI() {
        JPanel container = new JPanel();

        setResizable(true);
        setPreferredSize(prefWindowSize);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(true);

        setLayout(new BorderLayout());
        setTitle(_("DatasetAndTreeManager.Title"));

        treeSection.setBorder(BorderFactory.createTitledBorder(_("DatasetAndTreeManager.TreeSection.Title")));
        datasetSection.setBorder(BorderFactory.createTitledBorder(_("DatasetAndTreeManager.DatasetSection.Title")));

        container.setLayout(new GridBagLayout());
        container.setBorder(Borders.DIALOG_BORDER);

        GridBagConstraints c = new GridBagConstraints();

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 1.0;
        c.weighty = 0.5;
        container.add(datasetSection, c);

        c.gridy = 1;
        container.add(treeSection, c);

        c.gridy = 2;
        c.weighty = 0.0;
        getRootPane().setDefaultButton(okButton);
        container.add(ButtonBarFactory.buildOKBar(okButton), c);

        add(container, BorderLayout.CENTER);

        layoutDatasetSection();
        pack();
        layoutTreeSection();

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void initDatasetList() {
        datasets = new DefaultListModel();
        datasetList = new JList(datasets);
        datasetList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        datasetList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (!datasetList.isSelectionEmpty()) {
                        datasetList.ensureIndexIsVisible(datasetList.getSelectedIndex());
                        loadTrees();
                        editDataset.setEnabled(true);
                        calcProperties.setEnabled(true);
                        deleteDataset.setEnabled(true);
                        renameDataset.setEnabled(true);
                        newTree.setEnabled(true);
                    } else {
                        trees.clear();
                        updateDatasetInfo(null);
                        editDataset.setEnabled(false);
                        calcProperties.setEnabled(false);
                        deleteDataset.setEnabled(false);
                        renameDataset.setEnabled(false);
                        newTree.setEnabled(false);
                    }

                    updateDatasetInfo((Dataset) datasetList.getSelectedValue());
                }
            }
        });

        datasetListScrollPane = new JScrollPane(datasetList);
    }

    private void layoutDatasetSection() {
        datasetInfoScrollPane.setPreferredSize(prefSizeInfoLabels);
        datasetInfo.setEditable(false);
        // ensure fixed with of button row
        renameDataset.setMinimumSize(new Dimension(prefSizeButtons.width, deleteTree.getPreferredSize().height));
        renameDataset.setMaximumSize(prefSizeButtons);
        
        datasetListScrollPane.setPreferredSize(prefSizeLists);
        newDataset.setPreferredSize(prefSizeButtons);
        calcProperties.setPreferredSize(prefSizeButtons);
        editDataset.setPreferredSize(prefSizeButtons);
        deleteDataset.setPreferredSize(prefSizeButtons);
        renameDataset.setPreferredSize(prefSizeButtons);

        datasetSection.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();

        c.insets = insets;

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 5;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0.4;
        c.weighty = 1.0;
        datasetSection.add(datasetListScrollPane, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0;
        c.weighty = 0;
        datasetSection.add(newDataset, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        datasetSection.add(editDataset, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        datasetSection.add(calcProperties, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 3;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        datasetSection.add(renameDataset, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 4;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        datasetSection.add(deleteDataset, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 5;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0.6;
        c.weighty = 1.0;
        datasetSection.add(datasetInfoScrollPane, c);
    }

    private void updateDatasetInfo(Dataset dataset) {
        String text = _("DatasetAndTreeManager.DatasetSection.DatasetInfo.EmptyText");

        if (dataset != null) {
            String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(
                    dataset.getCreationDate());

            String createdBy = DBExceptionHandler.callDBManager(controller.getDbManager(),
                    new UnaryDBFunction<String, Dataset>(dataset) {
                        @Override
                        public String call(Dataset dataset) throws DatabaseException {
                            return controller.getDbManager().getCreationUserName(dataset);
                        }
                    });

            text = String.format(
                    "<html>%1$s: <i>%2$s</i><p> %3$s: <i>%4$s</i><p> %5$s: <i>%6$s</i><p> %7$s: <i>%8$s</i></html>",
                    _("DatasetAndTreeManager.DatasetSection.DatasetInfo.Title"), dataset.getTitle(),
                    _("DatasetAndTreeManager.DatasetSection.DatasetInfo.CreatedBy"), createdBy,
                    _("DatasetAndTreeManager.DatasetSection.DatasetInfo.CreationDate"), date,
                    _("DatasetAndTreeManager.DatasetSection.DatasetInfo.Comment"), dataset.getComment());
        }

        datasetInfo.setText(text);
    }

    private void loadDatasets() {
        List<Dataset> d = DBExceptionHandler.callDBManager(controller.getDbManager(), new GetAllDatasets());

        datasets.clear();
        for (Dataset dataset : d) {
            datasets.addElement(dataset);
        }

        if (!datasets.isEmpty()) {
            datasetList.setSelectedIndex(0);
        } else {
            updateDatasetInfo(null);
        }

        datasetList.repaint();
    }

    private void initTreeList() {
        trees = new DefaultListModel();
        treeList = new JList(trees);
        treeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        treeList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (!treeList.isSelectionEmpty()) {
                        treeList.ensureIndexIsVisible(treeList.getSelectedIndex());
                        updateTreeInfo((Tree) treeList.getSelectedValue());
                        editTree.setEnabled(true);
                        deleteTree.setEnabled(true);
                    } else {
                        updateTreeInfo(null);
                        editTree.setEnabled(false);
                        deleteTree.setEnabled(false);
                    }
                }
            }
        });
        treeListScrollPane = new JScrollPane(treeList);
    }

    private void layoutTreeSection() {
        treeListScrollPane.setPreferredSize(prefSizeLists);
        treeInfo.setEditable(false);
        // ensure fixed with of button row
        deleteTree.setMinimumSize(new Dimension(prefSizeButtons.width, deleteTree.getPreferredSize().height));
        deleteTree.setMaximumSize(prefSizeButtons);
        
        treeInfoScrollPane.setPreferredSize(prefSizeInfoLabels);
        newTree.setPreferredSize(prefSizeButtons);
        editTree.setPreferredSize(prefSizeButtons);
        deleteTree.setPreferredSize(prefSizeButtons);

        GridBagLayout layoutMgr = new GridBagLayout();
        treeSection.setLayout(layoutMgr);

        GridBagConstraints c = new GridBagConstraints();

        c.insets = insets;

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 0;
        c.gridy = 0;
        c.gridheight = 3;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0.4;
        c.weighty = 1.0;
        treeSection.add(treeListScrollPane, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0;
        c.weighty = 0;
        treeSection.add(newTree, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        treeSection.add(editTree, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 2;
        c.gridheight = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        treeSection.add(deleteTree, c);

        c.fill = GridBagConstraints.BOTH;
        c.gridx = 2;
        c.gridy = 0;
        c.gridheight = 3;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0.6;
        c.weighty = 1.0;
        treeSection.add(treeInfoScrollPane, c);
    }

    private void updateTreeInfo(Tree tree) {
        String text = _("DatasetAndTreeManager.TreeSection.TreeInfo.EmptyText");
        String ruleset;

        if (tree != null) {
            String date = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.MEDIUM).format(
                    tree.getCreationDate());
            ruleset = (tree.getRuleset() != null) ? tree.getRuleset().toString()
                    : _("DatasetAndTreeManager.TreeSection.TreeInfo.DefaultRuleset");

            String createdBy = DBExceptionHandler.callDBManager(controller.getDbManager(),
                    new UnaryDBFunction<String, Tree>(tree) {
                        @Override
                        public String call(Tree tree) throws DatabaseException {
                            return controller.getDbManager().getCreationUserName(tree);
                        }
                    });

            text = String
                    .format("<html>%1$s: <i>%2$s</i><p> %3$s: <i>%4$s</i><p> %5$s: <i>%6$s</i><p> %7$s: <i>%8$s</i><p> %9$s: <i>%10$s</i></html>",
                            _("DatasetAndTreeManager.TreeSection.TreeInfo.Title"), tree.getTitle(),
                            _("DatasetAndTreeManager.TreeSection.TreeInfo.CreatedBy"), createdBy,
                            _("DatasetAndTreeManager.TreeSection.TreeInfo.CreationDate"), date,
                            _("DatasetAndTreeManager.TreeSection.TreeInfo.Comment"), tree.getComment(),
                            _("DatasetAndTreeManager.TreeSection.TreeInfo.Ruleset"), ruleset);
        }
        treeInfo.setText(text);
    }

    private void loadTrees() {
        trees.clear();
        if (!datasetList.isSelectionEmpty()) {
            Dataset dataset = (Dataset) datasetList.getSelectedValue();
            Set<Tree> t = dataset.getTrees();
            for (Tree tree : t) {
                trees.addElement(tree);
            }
        }

        if (!trees.isEmpty()) {
            treeList.setSelectedIndex(0);
        }

        treeList.repaint();
    }

    private class GetAllDatasets implements DBFunction<List<Dataset>> {
        @Override
        public List<Dataset> call() throws DatabaseException {
            return controller.getDbManager().getAllDatasets();
        }
    }

    private class NewDatasetAction extends AbstractAction {
        Window window;

        NewDatasetAction(Window window) {
            super(_("DatasetAndTreeManager.DatasetSection.Buttons.New"));
            putValue(Action.SMALL_ICON, Resources.getIcon("new.png"));
            putValue(Action.SHORT_DESCRIPTION, _("DatasetAndTreeManager.DatasetSection.Buttons.New"));
            this.window = window;
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            Dataset dataset = controller.newDatasetDialog(window);

            if (dataset != null) {
                datasets.addElement(dataset);
                datasetList.setSelectedValue(dataset, true);
            }
        }
    }

    private class EditDatasetAction extends AbstractAction {
        EditDatasetAction() {
            super(_("DatasetAndTreeManager.DatasetSection.Buttons.Edit"));
            putValue(Action.SMALL_ICON, Resources.getIcon("edit.png"));
            putValue(Action.SHORT_DESCRIPTION, _("DatasetAndTreeManager.DatasetSection.Buttons.Edit"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!datasetList.isSelectionEmpty()) {
                Dataset dataset = (Dataset) datasetList.getSelectedValue();
                controller.editDatasetDialog(window, dataset);
                datasetList.repaint();
            }
        }
    }

    private class CalcPropertiesAction extends AbstractAction {
        CalcPropertiesAction() {
            super(_("DatasetAndTreeManager.DatasetSection.Buttons.CalcProperties"));
            putValue(Action.SMALL_ICON, Resources.getIcon("calculate.png"));
            putValue(Action.SHORT_DESCRIPTION, _("DatasetAndTreeManager.DatasetSection.Buttons.CalcProperties"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!datasetList.isSelectionEmpty()) {
                Dataset dataset = (Dataset) datasetList.getSelectedValue();
                controller.calculatePropertiesDialog(window, dataset);
            }
        }
    }

    private class RenameDatasetAction extends AbstractAction {
        RenameDatasetAction() {
            super(_("DatasetAndTreeManager.DatasetSection.Buttons.Rename"));
            putValue(Action.SMALL_ICON, Resources.getIcon("edit.png"));
            putValue(Action.SHORT_DESCRIPTION, _("DatasetAndTreeManager.DatasetSection.Buttons.Rename"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!datasetList.isSelectionEmpty()) {
                Dataset dataset = (Dataset) datasetList.getSelectedValue();

                Collection<Dataset> sets = Lists.newLinkedList();
                for (int i = 0; i < (datasets.size()); i++) {
                    sets.add((Dataset) datasets.get(i));
                }

                controller.renameDatasetDialog(window, sets, dataset);
                datasetList.repaint();
                updateDatasetInfo(dataset);
            }
        }
    }

    private class DeleteDatasetAction extends AbstractAction {
        DeleteDatasetAction() {
            super(_("DatasetAndTreeManager.DatasetSection.Buttons.Delete"));
            putValue(Action.SMALL_ICON, Resources.getIcon("delete.png"));
            putValue(Action.SHORT_DESCRIPTION, _("DatasetAndTreeManager.DatasetSection.Buttons.Delete"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!datasetList.isSelectionEmpty()) {
                Dataset dataset = (Dataset) datasetList.getSelectedValue();
                if (controller.deleteDatasetDialog(window, dataset)) {
                    datasets.removeElement(dataset);
                    if (!datasets.isEmpty()) {
                        datasetList.setSelectedIndex(0);
                    }
                }
            }
        }
    }

    private class NewTreeAction extends AbstractAction implements ProgressListener<Tree> {
        NewTreeAction() {
            super(_("DatasetAndTreeManager.TreeSection.Buttons.New"));
            putValue(Action.SMALL_ICON, Resources.getIcon("new.png"));
            putValue(Action.SHORT_DESCRIPTION, _("DatasetAndTreeManager.TreeSection.Buttons.New"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!datasetList.isSelectionEmpty()) {
                Dataset dataset = (Dataset) datasetList.getSelectedValue();
                controller.showNewTreeDialog(window, dataset, this);
            }
        }

        @Override
        public void setProgressValue(int progress) {// ignore
        }

        @Override
        public void setProgressBounds(int min, int max) {// ignore
        }

        @Override
        public void setProgressIndeterminate(boolean indeterminate) {// ignore
        }

        @Override
        public void finished(Tree result, boolean cancelled) {
            if (!cancelled && result != null) {
                trees.addElement(result);
                treeList.setSelectedValue(result, true);
                result.getDataset().getTrees().add(result);
            }
        }
    }

    private class EditTreeAction extends AbstractAction {
        EditTreeAction() {
            super(_("DatasetAndTreeManager.TreeSection.Buttons.Edit"));
            putValue(Action.SMALL_ICON, Resources.getIcon("edit.png"));
            putValue(Action.SHORT_DESCRIPTION, _("DatasetAndTreeManager.TreeSection.Buttons.Edit"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!treeList.isSelectionEmpty()) {
                Tree tree = (Tree) treeList.getSelectedValue();
                controller.showEditTreeDialog(window, tree);
                treeList.repaint();
                updateTreeInfo(tree);
            }
        }
    }

    private class DeleteTreeAction extends AbstractAction {
        DeleteTreeAction() {
            super(_("DatasetAndTreeManager.TreeSection.Buttons.Delete"));
            putValue(Action.SMALL_ICON, Resources.getIcon("delete.png"));
            putValue(Action.SHORT_DESCRIPTION, _("DatasetAndTreeManager.TreeSection.Buttons.Delete"));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!treeList.isSelectionEmpty()) {
                Tree tree = (Tree) treeList.getSelectedValue();
                Dataset dataset = tree.getDataset();
                if (controller.showDeleteTreeDialog(window, tree)) {
                    trees.removeElement(tree);
                    dataset.getTrees().remove(tree);
                    if (!trees.isEmpty()) {
                        treeList.setSelectedIndex(0);
                    }
                }
            }
        }
    }
}
