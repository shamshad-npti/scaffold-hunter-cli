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

package edu.udo.scaffoldhunter.gui.filtering;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Filter;
import edu.udo.scaffoldhunter.model.db.Filterset;
import edu.udo.scaffoldhunter.model.db.NumFilter;
import edu.udo.scaffoldhunter.model.db.Preset;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.StringFilter;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Thomas Schmitz
 * 
 */
public class FilterDialog extends JDialog {

    private DbManager dbManager;

    private Profile profile;

    private Dataset dataset;

    private Subset subset;

    private DefaultListModel filtersets;

    private JList filtersetsList;

    private JTextField filtersetTitle;

    private JPanel filtersPanel;

    private JComboBox filtersetConjunctive;

    private JButton addFiltersetButton;

    private JButton deleteFiltersetButton;

    private JButton saveFiltersetButton;

    private JLabel moleculeCountLabel;

    private boolean unsavedChanges = false;

    private CopiedFilterset currentFilterset;

    private Vector<PropertyDefinition> propertyDefinitions;
    private Vector<PropertyDefinition> propertyDefinitionsWithChoose;

    private boolean updateMoleculeCount = true;

    private boolean result = false;

    /**
     * @return the result
     */
    public boolean getResult() {
        return result;
    }

    /**
     * @return the selected Filterset
     */
    public Filterset getSelectedFilterset() {
        // Filterset f = (Filterset) filtersetsList.getSelectedValue();
        if (currentFilterset.getTitle().equals(_("Filtersets.NoFilterset")))
            return null;

        if (!isFiltersetUsable(currentFilterset))
            return null;

        return currentFilterset;
    }

    /**
     * Constructs the Dialog to manage and use filters
     * 
     * @param parent
     *            The parent window
     * @param dbManager
     *            The DbManager
     * @param profile
     *            The current profile
     * @param dataset
     *            The current dataset
     * @param subset
     *            The subset to be filtered. If null, the complete dataset will
     *            be filtered to create a new root subset.
     */
    public FilterDialog(Frame parent, DbManager dbManager, Profile profile, Dataset dataset, Subset subset) {
        super(null, ModalityType.TOOLKIT_MODAL);

        setIconImage(Resources.getBufferedImage("images/scaffoldhunter-icon.png"));

        this.dbManager = dbManager;
        this.profile = profile;
        this.dataset = dataset;
        this.subset = subset;

        initModels();
        initGUI(parent);
        loadFiltersets();
    }

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    private void initModels() {
        propertyDefinitions = new Vector<PropertyDefinition>(dataset.getPropertyDefinitions().values());
        Collections.sort(propertyDefinitions);

        propertyDefinitionsWithChoose = (Vector<PropertyDefinition>) propertyDefinitions.clone();
        propertyDefinitionsWithChoose.insertElementAt(new PropertyDefinition(_("Filtersets.ChoosePropertyDefinition"),
                "", PropertyType.NumProperty, "ChoosePropertyDefinition", false, false), 0);
    }

    /**
     * 
     */
    private void loadFiltersets() {
        filtersets.clear();
        CopiedFilterset noFilterset = new CopiedFilterset();
        noFilterset.setTitle(_("Filtersets.NoFilterset"));
        filtersets.addElement(noFilterset);

        Set<Preset> presets = profile.getPresets();
        for (Preset ps : presets) {
            if (ps.getClass() == Filterset.class) {
                Filterset fs = (Filterset) ps;
                filtersets.addElement(copyFilterset(fs));
            }
        }
        filtersetsList.setSelectedIndex(0);
    }

    private CopiedFilterset copyFilterset(Filterset filterset) {
        CopiedFilterset result = new CopiedFilterset();
        result.setOriginal(filterset);
        result.setTitle(filterset.getTitle());
        result.setProfile(filterset.getProfile());
        result.setConjunctive(filterset.isConjunctive());
        for (Filter f : filterset.getFilters()) {
            Filter newF;
            if (f.getClass() == NumFilter.class) {
                NumFilter fNum = (NumFilter) f;
                newF = new NumFilter(result, fNum.getPropDef(dataset), fNum.getAccumulationFunction(), fNum.getValue(),
                        fNum.getComparisonFunction());
            } else {
                StringFilter fString = (StringFilter) f;
                newF = new StringFilter(result, fString.getPropDef(dataset), fString.getAccumulationFunction(),
                        fString.getValue(), fString.getComparisonFunction());
            }
            result.getFilters().add(newF);
        }

        return result;
    }

    /**
     * Adjusts the GUI elements to represent the settings of the
     * specified filterset.
     */
    private void loadFilterset(CopiedFilterset filterset) {
        updateMoleculeCount = false;
        currentFilterset = filterset;
        filtersetTitle.setText(filterset.getTitle());
        if (filterset.isConjunctive())
            filtersetConjunctive.setSelectedItem(_("Filtersets.Conjunctive"));
        else
            filtersetConjunctive.setSelectedItem(_("Filtersets.Disconjunctive"));

        boolean enabled = !filterset.getTitle().equals(_("Filtersets.NoFilterset"));
        boolean usable = isFiltersetUsable(filterset);
        filtersetTitle.setEnabled(enabled && usable);
        deleteFiltersetButton.setEnabled(enabled);
        filtersetConjunctive.setEnabled(enabled && usable);

        filtersPanel.removeAll();
        if (enabled && usable) {
            for (Filter f : filterset.getFilters()) {
                filtersPanel.add(new FilterPanel(this, propertyDefinitionsWithChoose, propertyDefinitions, f, dataset,
                        filterset));
            }
            addNewFilterPanel(filterset);
        } else if (!usable) {
            filtersPanel.add(new JLabel(_("Filtersets.NotUsable")));
        }
        pack();
        filtersPanel.repaint();

        updateMoleculeCount = true;
        setFiltersetChanged(false, true);
    }

    private boolean isFiltersetUsable(Filterset filterset) {
        for (Filter f : filterset.getFilters()) {
            PropertyDefinition def = f.getPropDef(dataset);
            if (def == null)
                return false;
            if (def.isStringProperty() && f.getClass() == NumFilter.class)
                return false;

        }
        return true;
    }

    /**
     * @param filterset
     * 
     */
    public void addNewFilterPanel(Filterset filterset) {
        filtersPanel.add(new FilterPanel(this, propertyDefinitionsWithChoose, propertyDefinitions, null, dataset,
                filterset));
    }

    private void initGUI(Frame parent) {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setTitle(_("Filtersets.Title"));
        setResizable(false);
        addWindowListener(new CloseAction());

        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(getPanel(), BorderLayout.CENTER);

        pack();

        setLocationRelativeTo(parent);
    }

    private JPanel getPanel() {
        FormLayout layout = new FormLayout("f:d, 5dlu, f:d:grow", // 3 columns
                "f:d:grow, 5dlu, f:d"); // 3 rows
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();

        pb.add(getFiltersetsPanel(), cc.xy(1, 1));
        pb.add(getFiltersetsButtonsPanel(), cc.xy(1, 3));
        pb.add(getEditFiltersetPanel(), cc.xy(3, 1));
        pb.add(getButtonPanel(), cc.xy(3, 3));

        return pb.getPanel();
    }

    private Component getFiltersetsPanel() {
        FormLayout layout = new FormLayout("f:d:grow", // 1 column
                "f:d:grow"); // 1 row
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);

        filtersets = new DefaultListModel();
        filtersetsList = new JList(filtersets);
        Dimension dim = filtersetsList.getPreferredSize();
        dim.width = 90;
        filtersetsList.setPreferredSize(dim);
        filtersetsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        filtersetsList.addListSelectionListener(new FiltersetChanged(this));
        JScrollPane filtersetsScroll = new JScrollPane(filtersetsList);
        pb.add(filtersetsScroll, cc.xy(1, 1));

        return pb.getPanel();
    }

    private CopiedFilterset lastFilterset;
    private boolean dirty = true;

    private class FiltersetChanged implements ListSelectionListener {
        private JDialog dialog;

        public FiltersetChanged(JDialog dialog) {
            super();
            this.dialog = dialog;
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (dirty) {
                if (unsavedChanges) {
                    if (updateAllowSave()) {
                        int choice = JOptionPane.showConfirmDialog(dialog,
                                _("Filtersets.ConfirmUnsavedMessage", filtersetTitle.getText()),
                                _("Filtersets.ConfirmUnsavedTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (choice == JOptionPane.YES_OPTION) {
                            saveFilterset();
                            loadFilterset((CopiedFilterset) filtersetsList.getSelectedValue());
                        } else if (choice == JOptionPane.NO_OPTION) {
                            // reset a saved filter set to its original state; 
                            // remove in case the filter set was not saved before
                            if (lastFilterset.getOriginal() != null) {
                                reloadFilterset(lastFilterset);
                            } else {
                                dirty = false;
                                filtersets.removeElement(currentFilterset);
                            }
                            loadFilterset((CopiedFilterset) filtersetsList.getSelectedValue());
                        } else { // cancel was selected
                            dirty = false;
                            CopiedFilterset newSelection = (CopiedFilterset) filtersetsList.getSelectedValue();
                            // if this change was caused by creating a new filter set, remove the 
                            // new filter set before reselecting the element that was selected before
                            if (newSelection.getOriginal() == null && filtersetsList.getSelectedIndex() != 0) {
                                filtersets.removeElement(filtersetsList.getSelectedValue());
                            }
                            filtersetsList.setSelectedValue(lastFilterset, true);
                        }
                    } else {
                        int choice = JOptionPane.showConfirmDialog(dialog,
                                _("Filtersets.CannotBeSavedMessage", filtersetTitle.getText()),
                                _("Filtersets.ConfirmUnsavedTitle"), JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE);
                        if (choice == JOptionPane.YES_OPTION) {
                            reloadFilterset(lastFilterset);
                            loadFilterset((CopiedFilterset) filtersetsList.getSelectedValue());
                        } else {
                            dirty = false;
                            filtersetsList.setSelectedValue(lastFilterset, true);
                        }
                    }
                } else {
                    loadFilterset((CopiedFilterset) filtersetsList.getSelectedValue());
                }

                lastFilterset = (CopiedFilterset) filtersetsList.getSelectedValue();
                dirty = true;
            }
        }
    }

    /**
     * Resets a modified CopiedFilterset to its original state.
     */
    private void reloadFilterset(CopiedFilterset filterset) {
        Filterset original = filterset.getOriginal();
        filterset.setTitle(original.getTitle());
        filterset.setProfile(original.getProfile());
        filterset.setConjunctive(original.isConjunctive());
        filterset.getFilters().clear();

        for (Filter f : original.getFilters()) {
            Filter newF;
            if (f.getClass() == NumFilter.class) {
                NumFilter fNum = (NumFilter) f;
                newF = new NumFilter(filterset, fNum.getPropDef(dataset), fNum.getAccumulationFunction(),
                        fNum.getValue(), fNum.getComparisonFunction());
            } else {
                StringFilter fString = (StringFilter) f;
                newF = new StringFilter(filterset, fString.getPropDef(dataset), fString.getAccumulationFunction(),
                        fString.getValue(), fString.getComparisonFunction());
            }
            filterset.getFilters().add(newF);
        }
    }

    private Component getFiltersetsButtonsPanel() {
        FormLayout layout = new FormLayout("l:d, 5dlu, f:d:grow", // 3 columns
                "f:d:grow"); // 1 row
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);

        addFiltersetButton = new JButton(new AddFiltersetAction());
        pb.add(addFiltersetButton, cc.xy(1, 1));

        deleteFiltersetButton = new JButton(new RemoveFiltersetAction());
        pb.add(deleteFiltersetButton, cc.xy(3, 1));

        return pb.getPanel();
    }

    private Component getEditFiltersetPanel() {
        FormLayout layout = new FormLayout("l:d, 5dlu, l:d:grow, 5dlu, r:d", // 5
                                                                             // columns
                "f:d, 5dlu, f:d:grow, 5dlu, c:d"); // 5 rows
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);

        JLabel titleLabel = new JLabel(_("Filtersets.FiltersetTitle"));
        pb.add(titleLabel, cc.xy(1, 1));

        filtersetTitle = new JTextField();
        Dimension dim = filtersetTitle.getPreferredSize();
        dim.width = 400;
        filtersetTitle.setPreferredSize(dim);
        filtersetTitle.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                setFiltersetChanged(true, false);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                setFiltersetChanged(true, false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setFiltersetChanged(true, false);
            }
        });
        pb.add(filtersetTitle, cc.xyw(3, 1, 3));

        filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));
        JScrollPane filtersScroll = new JScrollPane(filtersPanel);
        Dimension filtersDim = filtersScroll.getPreferredSize();
        filtersDim.height = 200;
        filtersDim.width = 555;
        filtersScroll.setPreferredSize(filtersDim);
        pb.add(filtersScroll, cc.xyw(1, 3, 5));

        filtersetConjunctive = new JComboBox();
        filtersetConjunctive.addItem(_("Filtersets.Conjunctive"));
        filtersetConjunctive.addItem(_("Filtersets.Disconjunctive"));
        filtersetConjunctive.addActionListener(new ConjunctiveChanged());
        pb.add(filtersetConjunctive, cc.xy(3, 5));

        saveFiltersetButton = new JButton(new SaveFiltersetAction());
        pb.add(saveFiltersetButton, cc.xy(5, 5));

        JPanel panel = pb.getPanel();
        panel.setBorder(BorderFactory.createTitledBorder(_("Filtersets.EditFiltersetBorder")));

        panel.setEnabled(false);

        return panel;
    }

    private class ConjunctiveChanged implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            currentFilterset.setConjunctive(filtersetConjunctive.getSelectedItem().equals(_("Filtersets.Conjunctive")));
            setFiltersetChanged(true, true);
        }
    }

    /**
     * @param changed
     *            If true, the Save button is enabled and a save question
     *            appears, if the user selects a different Filterset.
     * @param updateCount
     *            If true, the molecule count is updated with a db call.
     */
    public void setFiltersetChanged(boolean changed, boolean updateCount) {
        unsavedChanges = changed;
        updateAllowSave();

        if (updateCount && updateMoleculeCount)
            updateMoleculeCount();
    }

    private boolean updateAllowSave() {
        boolean e = unsavedChanges && !filtersetTitle.getText().equals(_("Filtersets.NoFilterset"));
        saveFiltersetButton.setEnabled(e);
        return e;
    }

    private int moleculeCount = 0;
    
    private boolean runAgain = false;
    
    Thread countUpdateThread;

    private void updateMoleculeCount() {
        runAgain = true;
        if (countUpdateThread == null || !countUpdateThread.isAlive()) {
            countUpdateThread = new Thread() {
                @Override
                public void run() {
                    runAgain = true;
                    while (runAgain) {
                        runAgain = false;
                        moleculeCountLabel.setText(_("Filtersets.MoleculeCountUpdating"));
                        try {
                            if (isFiltersetUsable(currentFilterset)) {
                                if (subset == null)
                                    moleculeCount = dbManager.getRootSubsetSize(dataset, currentFilterset);
                                else
                                    moleculeCount = dbManager.getFilteredSubsetSize(subset, currentFilterset);
                            } else {
                                if (subset == null)
                                    moleculeCount = dbManager.getRootSubsetSize(dataset, null);
                                else
                                    moleculeCount = dbManager.getFilteredSubsetSize(subset, null);
                            }
                            moleculeCountLabel.setText(_("Filtersets.MoleculeCount") + ": " + moleculeCount);
                        } catch (DatabaseException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }
            };
            countUpdateThread.start();
        }
    }

    private void saveFilterset() {
        currentFilterset.setTitle(filtersetTitle.getText());

        Filterset original = currentFilterset.getOriginal();
        if (original == null) {
            original = new Filterset();
            currentFilterset.setOriginal(original);
        }
        original.setTitle(currentFilterset.getTitle());
        original.setProfile(currentFilterset.getProfile());
        original.setConjunctive(currentFilterset.isConjunctive());

        original.getFilters().clear();

        for (Filter f : currentFilterset.getFilters()) {
            Filter newF;
            if (f.getClass() == NumFilter.class) {
                NumFilter fNum = (NumFilter) f;
                newF = new NumFilter(original, fNum.getPropDef(dataset), fNum.getAccumulationFunction(),
                        fNum.getValue(), fNum.getComparisonFunction());
            } else {
                StringFilter fString = (StringFilter) f;
                newF = new StringFilter(original, fString.getPropDef(dataset), fString.getAccumulationFunction(),
                        fString.getValue(), fString.getComparisonFunction());
            }
            original.getFilters().add(newF);
        }

        try {
            dbManager.saveOrUpdate(original);
            for (Filter f : original.getFilters())
                dbManager.saveOrUpdate(f);
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        profile.getPresets().add(original);

        setFiltersetChanged(false, false);
        filtersetsList.repaint();
    }

    private void addFilterset() {
        CopiedFilterset fs = new CopiedFilterset();
        fs.setProfile(profile);
        fs.setTitle(getFreeFiltersetTitle());
        filtersets.addElement(fs);
        filtersetsList.setSelectedValue(fs, true);
        setFiltersetChanged(true, true);
    }

    @SuppressWarnings("rawtypes")
    private String getFreeFiltersetTitle() {
        String filtersetTitleStart = _("Filtersets.NewFiltersetTitle");
        String filtersetTitle = filtersetTitleStart;
        int nr = 1;
        Set<String> filtersetNames = new HashSet<String>();
        Filterset fs;

        for (Enumeration e = filtersets.elements(); e.hasMoreElements();) {
            fs = (Filterset) e.nextElement();

            filtersetNames.add(fs.getTitle());
        }

        while (filtersetNames.contains(filtersetTitle)) {
            nr++;
            filtersetTitle = filtersetTitleStart + " (" + nr + ")";
        }

        return filtersetTitle;
    }

    private Component getButtonPanel() {
        FormLayout layout = new FormLayout("r:d:grow, 5dlu, r:d", // 3 columns
                "f:d:grow"); // 1 row
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);

        moleculeCountLabel = new JLabel(_("Filtersets.MoleculeCount") + ": " + moleculeCount);
        pb.add(moleculeCountLabel, cc.xy(1, 1));

        JButton okButton = new JButton(new OkAction());
        JButton cancelButton = new JButton(new CancelAction());
        pb.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), cc.xy(3, 1));

        getRootPane().setDefaultButton(okButton);

        return pb.getPanel();
    }

    private class SaveFiltersetAction extends AbstractAction {
        public SaveFiltersetAction() {
            super();
            putValue(NAME, _("Filtersets.Save"));
            putValue(Action.SHORT_DESCRIPTION, _("Filtersets.SaveDescription"));
            putValue(Action.SMALL_ICON, Resources.getIcon("save.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            saveFilterset();
        }
    }

    private class AddFiltersetAction extends AbstractAction {
        public AddFiltersetAction() {
            super(); // _("Filtersets.Add")
            putValue(Action.SHORT_DESCRIPTION, _("Filtersets.AddDescription"));
            putValue(Action.SMALL_ICON, Resources.getIcon("new.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addFilterset();
        }
    }

    private class RemoveFiltersetAction extends AbstractAction {
        public RemoveFiltersetAction() {
            super(); // _("Filtersets.Remove")
            putValue(Action.SHORT_DESCRIPTION, _("Filtersets.RemoveDescription"));
            putValue(Action.SMALL_ICON, Resources.getIcon("delete.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int result = JOptionPane.showConfirmDialog(null,
                        _("Filtersets.ConfirmDeleteMessage", currentFilterset.getTitle()),
                        _("Filtersets.ConfirmDeleteTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    Filterset original = ((CopiedFilterset) filtersetsList.getSelectedValue()).getOriginal();
                    if (original != null) {
                        dbManager.delete(original);
                        profile.getPresets().remove(original);
                    }
                    boolean dirt = dirty;
                    dirty = false;
                    int index = filtersetsList.getSelectedIndex();
                    filtersetsList.setSelectedIndex(0);
                    filtersets.remove(index);
                    dirty = dirt;
                    loadFilterset((CopiedFilterset) filtersetsList.getSelectedValue());
                }
            } catch (DatabaseException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    private class OkAction extends AbstractAction {
        public OkAction() {
            super();
            putValue(NAME, _("Filtersets.OK"));
            putValue(Action.SMALL_ICON, Resources.getIcon("apply.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            closeWindow(true);
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super();
            putValue(NAME, _("Filtersets.Cancel"));
            putValue(Action.SMALL_ICON, Resources.getIcon("cancel.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            closeWindow(false);
        }
    }

    /**
     * @param panel
     */
    public void removeFilterPanel(JPanel panel) {
        filtersPanel.remove(panel);
        pack();
        filtersPanel.repaint();
    }

    private class CloseAction extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            super.windowClosing(e);
            closeWindow(false);
        }
    }

    private void closeWindow(boolean result) {
        if (unsavedChanges) {
            if (updateAllowSave()) {
                int choice = JOptionPane.showConfirmDialog(this,
                        _("Filtersets.ConfirmUnsavedMessage", filtersetTitle.getText()),
                        _("Filtersets.ConfirmUnsavedTitle"), JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    saveFilterset();
                    checkMoleculeCountAndClose(result);
                } else if (choice == JOptionPane.NO_OPTION) {
                    checkMoleculeCountAndClose(result);
                }
            } else {
                int choice = JOptionPane.showConfirmDialog(this,
                        _("Filtersets.CannotBeSavedMessage", filtersetTitle.getText()),
                        _("Filtersets.ConfirmUnsavedTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (choice == JOptionPane.YES_OPTION) {
                    checkMoleculeCountAndClose(result);
                }
            }
        } else {
            checkMoleculeCountAndClose(result);
        }
    }

    private void checkMoleculeCountAndClose(boolean check) {
        if (check) {
            if (moleculeCount != 0) {
                result = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, _("Filtersets.NoMoleculesError"), _("Filtersets.Error"),
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            dispose();
        }
    }
}
