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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.ButtonStackBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.gui.util.UrlOpener;
import edu.udo.scaffoldhunter.model.RuleType;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Rule;
import edu.udo.scaffoldhunter.model.db.Ruleset;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * Dialog to edit/create tree generation rulesets
 * 
 * @author Philipp Lewe
 * 
 */
public class ManageRulesetsDialog extends JDialog {

    private DatasetManagement controller;

    private JPanel container;
    private JPanel leftContainer;
    private JPanel rightContainer;

    // Rulesets Section
    private JList rulesetsList;
    private DefaultListModel rulesets;
    private JScrollPane rulesetsListScroll;

    private JButton newRulesetButton;
    private JButton deleteRulesetButton;
    
    // TODO: Changes
    private Ruleset createdNotSaved;

    // Ruleset Edit Section
    private JTextField rulesetTitle;
    private Border defaultRulesetTitleBorder;
    private RulesetTitleDocumentListener rulesetTitleListener;
    private JList existingRulesList;
    private DefaultListModel existingRules;
    private JScrollPane existingRulesListScroll;
    private JTable usedRulesTable;
    private RulesetTableModel usedRules;
    private JScrollPane usedRulesTableScroll;
    private ListSelectionListener ruleSelection;

    private JButton addRuleButton;
    private JButton removeRuleButton;
    private JButton ruleUpButton;
    private JButton ruleDownButton;

    private JButton saveButton;
    private JButton okButton;

    private JEditorPane infoLabel;

    private List<Ruleset> immutableRulesets;
    private Ruleset currentRulesetEditing = null;
    private boolean unsavedChanges = false;

    private boolean isNewRuleset = false;

    /**
     * Creates new dialog for management of rulesets
     * 
     * @param owner
     *            the owner of this dialog
     * @param controller
     *            the dataset management controller
     * @throws DatabaseException
     *             when the immutable rulesets list could not be loaded
     */
    public ManageRulesetsDialog(Window owner, DatasetManagement controller) throws DatabaseException {
        super(owner);
        this.controller = controller;
        initGUI();
        loadImmutableRulesets();
        loadRulesets();
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Initialises all GUI components
     */
    private void initGUI() {
        FormLayout containerLayout;
        PanelBuilder containerBuilder;

        FormLayout leftLayout;
        PanelBuilder leftBuilder;

        FormLayout rightLayout;
        PanelBuilder rightBuilder;

        ButtonStackBuilder rulesetListButtons;
        ButtonStackBuilder ruleAddRemoveButtons;
        ButtonStackBuilder ruleUpDownButtons;

        CellConstraints cc = new CellConstraints();

        String listPrototypeElement = "____________________";
        int listsNumRows = 15;
        JComboBox ascendingChooser;

        setTitle(_("ManageRulesets.WindowTitle"));
        setLayout(new BorderLayout());
        setModal(true);

        container = new JPanel();
        add(container, BorderLayout.CENTER);

        containerLayout = new FormLayout("p, 2dlu, p:g(1.0)", "f:p:g(1.0), p");
        containerBuilder = new PanelBuilder(containerLayout, container);
        containerBuilder.setDefaultDialogBorder();

        leftContainer = new JPanel();
        leftContainer.setBorder(BorderFactory.createTitledBorder(_("ManageRulesets.Rulesets")));
        containerBuilder.add(leftContainer, cc.rc(1, 1));

        leftLayout = new FormLayout("p, 2dlu, p:g(1.0)", "f:p:g(1.0)");
        leftBuilder = new PanelBuilder(leftLayout, leftContainer);

        rulesets = new DefaultListModel();
        rulesetsList = new JList(rulesets);
        rulesetsList.setVisibleRowCount(listsNumRows);
        rulesetsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        rulesetsList.setPrototypeCellValue(listPrototypeElement);
        rulesetsList.addListSelectionListener(new RulesetsListSelectionListener());
        rulesetsListScroll = new JScrollPane(rulesetsList);
        leftBuilder.add(rulesetsListScroll, cc.rc(1, 1));

        newRulesetButton = new JButton(new NewRulesetAction());
        deleteRulesetButton = new JButton(new DeleteRulesetAction(this));
        rulesetListButtons = new ButtonStackBuilder();
        rulesetListButtons.addGridded(newRulesetButton);
        rulesetListButtons.addRelatedGap();
        rulesetListButtons.addGridded(deleteRulesetButton);
        leftBuilder.add(rulesetListButtons.getPanel(), cc.rc(1, 3));

        rightContainer = new JPanel();
        rightContainer.setBorder(BorderFactory.createTitledBorder(_("ManageRulesets.Ruleset")));
        containerBuilder.add(rightContainer, cc.rc(1, 3));

        rulesetTitle = new JTextField();
        rulesetTitleListener = new RulesetTitleDocumentListener();
        rulesetTitle.getDocument().addDocumentListener(rulesetTitleListener);
        defaultRulesetTitleBorder = rulesetTitle.getBorder();

        ruleSelection = new RuleSelectionListener();

        existingRules = new DefaultListModel();
        existingRulesList = new JList(existingRules);
        existingRulesList.setVisibleRowCount(listsNumRows);
        existingRulesList.setMinimumSize(new Dimension(120, 20));
        existingRulesList.addListSelectionListener(ruleSelection);
        existingRulesListScroll = new JScrollPane(existingRulesList);
        existingRulesListScroll.setMinimumSize(new Dimension(160, 20));
        existingRulesListScroll.setPreferredSize(new Dimension(160, 20));

        usedRules = new RulesetTableModel();
        usedRulesTable = new JTable(usedRules);
        usedRulesTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        usedRulesTable.getSelectionModel().addListSelectionListener(ruleSelection);
        usedRulesTableScroll = new JScrollPane(usedRulesTable);
        usedRulesTableScroll.setPreferredSize(new Dimension(300, 200));

        ascendingChooser = new JComboBox();
        ascendingChooser.addItem(_("ManageRulesets.UsedRules.Ascending.True"));
        ascendingChooser.addItem(_("ManageRulesets.UsedRules.Ascending.False"));
        usedRulesTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        usedRulesTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(ascendingChooser));
        usedRulesTable.getColumnModel().getColumn(1).getCellEditor().addCellEditorListener(new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                rulesetChanged();
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
                // do nothing
            }
        });

        usedRulesTable.getColumnModel().getColumn(1).setPreferredWidth(170);
        usedRulesTable.setRowHeight(ascendingChooser.getPreferredSize().height);
        usedRulesTable.getTableHeader().setReorderingAllowed(false);

        addRuleButton = new JButton(new AddRuleAction());
        removeRuleButton = new JButton(new RemoveRuleAction());
        ruleAddRemoveButtons = new ButtonStackBuilder();
        ruleAddRemoveButtons.addButton(addRuleButton, removeRuleButton);

        ruleUpButton = new JButton(new RuleUpAction());
        ruleDownButton = new JButton(new RuleDownAction());
        ruleUpDownButtons = new ButtonStackBuilder();
        ruleUpDownButtons.addButton(ruleUpButton, ruleDownButton);

        infoLabel = new JEditorPane();
        infoLabel.setContentType("text/html");
        infoLabel.setText(_("ManageRulesets.HelpNoSelection"));
        infoLabel.setEditable(false);
        infoLabel.setBackground(rightContainer.getBackground());
        infoLabel.setPreferredSize(new Dimension(30, 40));
        infoLabel.setMaximumSize(new Dimension(30, 40));
        infoLabel.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                // if clicked, open url in browser
                if (e.getEventType() == EventType.ACTIVATED) {
                    UrlOpener.browse(e.getURL());
                }
            }
        });
        saveButton = new JButton(new SaveAction());
        saveButton.setEnabled(false);

        rightLayout = new FormLayout("p, 2dlu, l:m, d, p:g, d, f:m", // 7 colums
                "t:p, 2dlu, p, d, f:p:g, d, p, d, f:p"); // 9 rows
        rightLayout.setColumnGroups(new int[][] { /* { 1, 5 }, */{ 2, 4, 6 } });
        rightLayout.setRowGroups(new int[][] { { 2, 4, 6, 8 } });

        rightBuilder = new PanelBuilder(rightLayout, rightContainer);

        rightBuilder.addLabel(_("ManageRulesets.RulesetLabel"), cc.rc(1, 1));
        rightBuilder.add(rulesetTitle, cc.rcw(1, 3, 5));

        rightBuilder.addLabel(_("ManageRulesets.ExistingRules"), cc.rc(3, 1));
        rightBuilder.add(existingRulesListScroll, cc.rc(5, 1));
        rightBuilder.add(ruleAddRemoveButtons.getPanel(), cc.rc(5, 3));

        rightBuilder.addLabel(_("ManageRulesets.UsedRules"), cc.rc(3, 5));
        rightBuilder.add(usedRulesTableScroll, cc.rc(5, 5));
        rightBuilder.add(ruleUpDownButtons.getPanel(), cc.rc(5, 7));

        rightBuilder.add(saveButton, cc.rchw(7, 7, 3, 1));

        rightBuilder.addLabel(_("ManageRulesets.Help"), cc.rc(7, 1));
        rightBuilder.add(infoLabel, cc.rcw(9, 1, 5));

        okButton = StandardButtonFactory.createCloseButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmIfUnsavedChanges();
                dispose();
            }
        });
        getRootPane().setDefaultButton(okButton);
        containerBuilder.add(ButtonBarFactory.buildOKBar(okButton), cc.rcw(2, 1, 3));

        setRulesetEditingEnable(false, false);
    }

    /**
     * Loads all rulesets used by any tree in any dataset. Needed to ensure a
     * used ruleset is not editable
     * 
     * @throws DatabaseException
     */
    private void loadImmutableRulesets() throws DatabaseException {
        List<Dataset> datasets;
        Ruleset ruleset;
        immutableRulesets = new LinkedList<Ruleset>();

        datasets = controller.getDbManager().getAllDatasets();

        // loads all rulesets in any tree in any dataset
        for (Dataset dataset : datasets) {
            for (Tree tree : dataset.getTrees()) {
                ruleset = tree.getRuleset();
                if (ruleset != null) {
                    immutableRulesets.add(ruleset);
                }
            }
        }
    }

    private void loadRules(Ruleset ruleset) {
        Set<RuleType> usedRuleTypes = new HashSet<RuleType>();
        Rule newRule;

        // load used rules
        usedRules.clear();
        usedRules.addAllElements(ruleset.getOrderedRules());

        // load existing rules

        existingRules.clear();

        // collect all used ruletypes from ruleset
        for (Rule rule : ruleset.getOrderedRules()) {
            usedRuleTypes.add(rule.getRule());
        }

        // create a rule for all ruletypes which are not present in the ruleset
        for (RuleType ruleType : RuleType.values()) {

            if (!usedRuleTypes.contains(ruleType)) {
                newRule = new Rule();
                newRule.setRule(ruleType);
                newRule.setRuleset(ruleset);
                existingRules.addElement(newRule);
            }
        }
    }

    private void loadRulesets() {
        List<Ruleset> list;

        list = DBExceptionHandler.callDBManager(controller.getDbManager(), new DBFunction<List<Ruleset>>() {
            @Override
            public List<Ruleset> call() throws DatabaseException {
                return controller.getDbManager().getAllRulesets();
            }
        });

        rulesets.clear();

        for (Ruleset ruleset : list) {
            rulesets.addElement(ruleset);
        }

        if (!rulesets.isEmpty()) {
            rulesetsList.setSelectedIndex(0);
        }
    }

    private void rulesetChanged() {
        unsavedChanges = true;

        if (validTitle()) {
            saveButton.setEnabled(true);
            rulesetTitle.setBorder(defaultRulesetTitleBorder);
        } else {
            saveButton.setEnabled(false);
            rulesetTitle.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
    }

    /**
     * Checks if the title entered by the user is valid
     * 
     * @return <code>true</code> if the title is valid, <code>false</code> if
     *         not.
     */
    private boolean validTitle() {
        String title = rulesetTitle.getText().trim();
        Ruleset ruleset;

        // empty title is not allowed
        if (title.isEmpty()) {
            return false;
        }

        // same titles for rulesets are not allowed
        for (Enumeration<?> e = rulesets.elements(); e.hasMoreElements();) {
            ruleset = (Ruleset) e.nextElement();

            if (ruleset != currentRulesetEditing
                    && ruleset.getTitle().toLowerCase().equals(rulesetTitle.getText().toLowerCase())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Loads the given ruleset to the GUI
     * 
     * @param ruleset
     *            the <code>Ruleset</code> to load
     */
    private void editRuleset(Ruleset ruleset) {
        unsavedChanges = false;
        currentRulesetEditing = ruleset;
        
        if(ruleset == null) {
         // if this is the case, the edited ruleset has been deleted
            return;
        }
        
        loadRules(ruleset);

        rulesetTitle.getDocument().removeDocumentListener(rulesetTitleListener);
        rulesetTitle.setText(currentRulesetEditing.getTitle());
        rulesetTitle.selectAll();
        rulesetTitle.getDocument().addDocumentListener(rulesetTitleListener);

        // if the ruleset is immutable, don't allow editing of rules
        if (isRulesetImmutable(currentRulesetEditing)) {
            setRulesetEditingEnable(false, true);
            infoLabel.setText(String.format("<html><font color=\"red\"><i>%s</i></font></html>",
                    _("ManageRulesets.HelpRulesetNotModifiable")));
        } else {
            setRulesetEditingEnable(true, true);
            infoLabel.setText(_("ManageRulesets.HelpNoSelection"));
        }

        if (isNewRuleset) {
            isNewRuleset = false;
            rulesetChanged();
        }
    }

    private void setRulesetEditingEnable(boolean b, boolean enableTitleEditing) {
        usedRulesTable.setEnabled(b);
        existingRulesList.setEnabled(b);
        ruleUpButton.setEnabled(b);
        ruleDownButton.setEnabled(b);
        addRuleButton.setEnabled(b);
        removeRuleButton.setEnabled(b);
        rulesetTitle.setEnabled(enableTitleEditing);
    }

    /**
     * Checks if the given ruleset is immutable
     * 
     * @return <code>true</code> if the ruleset is immutable, <code>false</code>
     *         otherwise
     */
    private boolean isRulesetImmutable(Ruleset ruleset) {

        for (Ruleset immRuleset : immutableRulesets) {
            if (ruleset.equalsDb(immRuleset)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Saves the currently loaded ruleset
     */
    private void save() {
        assert (!(currentRulesetEditing == null)) : "saving without editing should not be possible";
        
        currentRulesetEditing.setTitle(rulesetTitle.getText().trim());

        // if editing of this ruleset is allowed update the rules list of this
        // ruleset
        if (!isRulesetImmutable(currentRulesetEditing)) {
            currentRulesetEditing.setOrderedRules(usedRules.getRules());
        }

        controller.saveRuleset(currentRulesetEditing);
       
        createdNotSaved = null;
        
        unsavedChanges = false;
        saveButton.setEnabled(false);
        deleteRulesetButton.setEnabled(true);
        rulesetsList.updateUI();
    }

    /**
     * Shows information about the <code>Ruletype</code> of the given Rule
     * 
     * @param rule
     *            the rule the info should be displayed (or <code>null</code> if
     *            there is no selection)
     */
    private void showInfo(Rule rule) {
        String hyperlinkedDescription;
        String text;

        if (rule != null) {
            hyperlinkedDescription = rule.getRule().getDescription()
                    .replaceAll("http://[^ ]*", "<a href=\"$0\">$0</a>");
            text = String.format("<html>%s: <i>%s</i><br><br></html>", rule.getRule().name(), hyperlinkedDescription);
        } else {
            text = String.format("<html>%s</html>", _("ManageRulesets.HelpNoSelection"));
        }

        infoLabel.setText(text);
    }

    /**
     * Shows a confirmation dialog if unsaved changes exist and saves the
     * ruleset if the user confirms saving
     */
    public void confirmIfUnsavedChanges() {
        if (unsavedChanges) {
                        
            int returnValue;                
            
            returnValue = JOptionPane.showConfirmDialog(this,
                    _("ManageRulesets.ConfirmUnsavedMessage", currentRulesetEditing.getTitle()),
                    _("ManageRulesets.ConfirmUnsavedTitle"), JOptionPane.YES_NO_OPTION);

            if (returnValue == JOptionPane.YES_OPTION) {
                save();
            }
            else {
                // if there is an unsaved ruleset it is deleted here                
                if(createdNotSaved != null) {                    
                    Ruleset tempRuleset = createdNotSaved;   
                    createdNotSaved = null;
                    isNewRuleset = false;
                    rulesets.removeElement(tempRuleset);
                }
            }
        }
    }

    /**
     * Used to fix errors when an cell with active cell editor is removed from
     * the list. Invoked by all button action listeners before removing /
     * switching an element from the table.
     */
    private void stopCellEditors() {
        usedRulesTable.getColumnModel().getColumn(1).getCellEditor().stopCellEditing();
    }
    
    /**
     * Internal method, used for generating new Ruleset names. 
     * Searches for the smallest positive integer, for which the default Ruleset name is not in use.
     * @return Smallest positive integer, which can be used for a generic Ruleset name.
     */
    private int smallestNameIndexNotUsed() {
        
        int size = rulesets.getSize();

        // TODO: Could be implemented more efficiently
        for(int i = 1; i <= size; i++) {
            boolean used = false;
            for(int j = 0; j < size; j++) {
                String name = ((Ruleset)rulesets.get(j)).getTitle();
                if(name.equals(_("ManageRulesets.NewRulesetDefaulTitle", i)))
                    used = true;
            }
            if(!used) {
                return i;
            }
        }
        return size + 1;
    }

    private class RulesetTitleDocumentListener implements DocumentListener {
        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            if (!currentRulesetEditing.getTitle().equals(rulesetTitle.getText())) {
                rulesetChanged();
            }
        }
    }

    private class RulesetsListSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {            
            if (!e.getValueIsAdjusting()) {
                if (!rulesetsList.isSelectionEmpty()) {
                    Ruleset ruleset = (Ruleset) rulesetsList.getSelectedValue();
                    
                    confirmIfUnsavedChanges();

                    editRuleset(ruleset);

                    if (isRulesetImmutable(ruleset)) {
                        deleteRulesetButton.setEnabled(false);
                    } else {
                        deleteRulesetButton.setEnabled(true);
                    }
                } else {
                    deleteRulesetButton.setEnabled(false);
                    setRulesetEditingEnable(false, false);
                }
            }
        }
    }

    private class RuleSelectionListener implements ListSelectionListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.
         * event.ListSelectionEvent)
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel model = null;
            Rule rule = null;

            if (!e.getValueIsAdjusting()) {
                if (e.getSource() instanceof ListSelectionModel) {
                    model = (ListSelectionModel) e.getSource();
                    if (!model.isSelectionEmpty()) {
                        rule = usedRules.get(model.getLeadSelectionIndex());
                    }
                } else if (e.getSource() instanceof JList) {
                    model = ((JList) e.getSource()).getSelectionModel();
                    if (!model.isSelectionEmpty()) {
                        rule = (Rule) existingRules.get(model.getLeadSelectionIndex());
                    }

                }

                showInfo(rule);
            }

        }

    }

    private class SaveAction extends AbstractAction {
        public SaveAction() {
            super();
            putValue(NAME, _("ManageRulesets.Save"));
            putValue(Action.SMALL_ICON, Resources.getIcon("save.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {            
            save();
        }
    }

    private class NewRulesetAction extends AbstractAction {

        NewRulesetAction() {
            super(_("ManageRulesets.New"));
            putValue(Action.SMALL_ICON, Resources.getIcon("new.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            Ruleset newRuleset = new Ruleset();       
            
            String newTitle = _("ManageRulesets.NewRulesetDefaulTitle", smallestNameIndexNotUsed());
            
            // if there is still an old unsaved ruleset, the user is asked to do so (or remove the old one)
            if(createdNotSaved != null) {
                confirmIfUnsavedChanges();
                editRuleset(createdNotSaved);
            }
            
            newRuleset.setTitle(newTitle);
            rulesets.addElement(newRuleset);
            isNewRuleset = true;
            rulesetsList.setSelectedValue(newRuleset, true);
            deleteRulesetButton.setEnabled(false);            
                      
            // this should be set at last, otherwise the new created ruleset might be deleted immediately
            createdNotSaved = newRuleset;
        }
    }

    private class DeleteRulesetAction extends AbstractAction {
        Window window;

        DeleteRulesetAction(Window window) {
            super(_("ManageRulesets.Delete"));
            this.window = window;
            putValue(Action.SMALL_ICON, Resources.getIcon("delete.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!rulesetsList.isSelectionEmpty()) {
                Ruleset ruleset = (Ruleset) rulesetsList.getSelectedValue();
                if (controller.showDeleteRulesetDialog(window, ruleset)) {
                    rulesets.removeElement(ruleset);
                }
            }
        }
    }

    private class AddRuleAction extends AbstractAction {

        AddRuleAction() {
            super();
            putValue(Action.SMALL_ICON, Resources.getIcon("right.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (!existingRulesList.isSelectionEmpty()) {
                for (Object selected : existingRulesList.getSelectedValuesList()) {
                    assert (selected instanceof Rule) : "wrong use";
                    usedRules.addElement((Rule) selected);
                    existingRules.removeElement(selected);
                }

                rulesetChanged();
            }
        }
    }

    private class RemoveRuleAction extends AbstractAction {

        RemoveRuleAction() {
            super();
            putValue(Action.SMALL_ICON, Resources.getIcon("left.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            Rule rule;

            if (!usedRulesTable.getSelectionModel().isSelectionEmpty()) {
                stopCellEditors();

                for (int i : usedRulesTable.getSelectedRows()) {
                    rule = usedRules.get(i);
                    existingRules.addElement(rule);
                    usedRules.removeElement(rule);
                }

                rulesetChanged();
            }
        }
    }

    private class RuleUpAction extends AbstractAction {

        RuleUpAction() {
            super();
            putValue(Action.SMALL_ICON, Resources.getIcon("up.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            int minSelIndex = usedRulesTable.getSelectionModel().getMinSelectionIndex();

            if (!usedRulesTable.getSelectionModel().isSelectionEmpty() && (minSelIndex > 0)) {
                int indices[] = usedRulesTable.getSelectedRows();
                stopCellEditors();

                for (int i : indices) {
                    Rule value1 = usedRules.get(i - 1);
                    Rule value2 = usedRules.get(i);

                    // move entry i to position i-1
                    usedRules.set(i, value1);
                    usedRules.set(i - 1, value2);

                    // remove selection for i
                    usedRulesTable.removeRowSelectionInterval(i, i);

                }

                for (int i : indices) {
                    // add selection index for i-1
                    usedRulesTable.addRowSelectionInterval(i - 1, i - 1);
                }

                rulesetChanged();
            }
        }
    }

    private class RuleDownAction extends AbstractAction {

        RuleDownAction() {
            super();
            putValue(Action.SMALL_ICON, Resources.getIcon("down.png"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {

            int maxSelIndex = usedRulesTable.getSelectionModel().getMaxSelectionIndex();

            if (!usedRulesTable.getSelectionModel().isSelectionEmpty() && (maxSelIndex < usedRules.getSize() - 1)) {
                int indices[] = usedRulesTable.getSelectedRows();
                stopCellEditors();

                for (int i : indices) {
                    Rule value1 = usedRules.get(i + 1);
                    Rule value2 = usedRules.get(i);

                    // move entry i to position i+1
                    usedRules.set(i, value1);
                    usedRules.set(i + 1, value2);

                    // remove selection for i
                    usedRulesTable.removeRowSelectionInterval(i, i);
                }

                for (int i : indices) {
                    // add selection index for i+1
                    usedRulesTable.addRowSelectionInterval(i + 1, i + 1);
                }

                rulesetChanged();
            }
        }
    }
}
