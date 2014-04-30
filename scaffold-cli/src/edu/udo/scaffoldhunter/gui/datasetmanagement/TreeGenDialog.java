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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Ruleset;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.model.treegen.GeneratorOptions;

/**
 * Dialog to configure generation of a new tree and to edit existing trees
 * 
 * @author Philipp Lewe
 * 
 */
public class TreeGenDialog extends JDialog {

    DatasetManagement controller;

    /**
     * Indicates if we want generate a new tree or edit an existing one false =
     * new tree true = existing
     */
    private boolean editOnly;
    private boolean result = false;
    private GeneratorOptions genOptions;
    private Dataset dataset;
    private Tree tree;

    private JPanel container;
    private JPanel treeContainer;
    private JPanel optionsContainer;
    private JTextField title;
    private Border titleDefaultBorder;
    private JTextArea comment;
    private JScrollPane commentScrollPane;

    private JRadioButton defaultRules;
    private JRadioButton customRules;
    private ButtonGroup rulesGroup;
    private JComboBox rulesetsList;
    private Border rulesetsListDefaultBorder;
    private DefaultComboBoxModel rulesets;
    private JButton editRulesetsButton;
    private JCheckBox deglycosilate;

    private JLabel titleLabel;
    private JLabel commentLabel;

    private JButton okButton;
    private JButton cancelButton;

    private Insets insets;

    /**
     * Creates a dialog to create a new tree
     * 
     * @param owner
     *            the owner of this Dialog
     * @param dataset
     *            where the new should be generated in
     * @param controller
     *            the dataset management controller
     */
    public TreeGenDialog(Window owner, Dataset dataset, DatasetManagement controller) {
        super(owner);
        editOnly = false;
        this.dataset = dataset;
        tree = null;
        this.controller = controller;
        initGUI();
        loadCustomRulesets();

        // set default values
        title.setText(_("TreeGen.DefaultTitle", dataset.getTrees().size() + 1));
        title.selectAll();
        deglycosilate.setSelected(false);
    }

    /**
     * Creates a dialog to edit a tree
     * 
     * @param owner
     *            the owner of this Dialog
     * @param tree
     *            the tree which should be edited
     * @param controller
     *            the dataset management controller
     */
    public TreeGenDialog(Window owner, Tree tree, DatasetManagement controller) {
        super(owner);
        editOnly = true;
        dataset = tree.getDataset();
        this.tree = tree;
        this.controller = controller;
        initGUI();
        loadCustomRulesets();

        // set old values
        title.setText(tree.getTitle());
        comment.setText(tree.getComment());
        title.selectAll();
        if (tree.getRuleset() == null) {
            defaultRules.setSelected(true);
        } else {
            customRules.setSelected(true);
            rulesetsList.setSelectedItem(tree.getRuleset());
        }
        deglycosilate.setSelected(tree.isDeglycosilate());

        // deactivate options section
        defaultRules.setEnabled(false);
        customRules.setEnabled(false);
        rulesetsList.setEnabled(false);
        editRulesetsButton.setEnabled(false);
        deglycosilate.setEnabled(false);
    }

    /**
     * Initialize all components
     */
    private void initGUI() {
        genOptions = new GeneratorOptions();

        setTitle(_("TreeGen.WindowTitle"));
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setModal(true);

        setLayout(new BorderLayout());

        insets = new Insets(2, 2, 2, 2);

        container = new JPanel();
        container.setBorder(Borders.DIALOG_BORDER);
        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        add(container, BorderLayout.CENTER);

        titleLabel = new JLabel(_("TreeGen.Title") + ":");
        commentLabel = new JLabel(_("TreeGen.Comment") + ":");
        title = new JTextField();
        title.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                validConfig();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                validConfig();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                validConfig();
            }
        });
        titleDefaultBorder = title.getBorder();

        comment = new JTextArea();
        comment.setRows(4);
        comment.setLineWrap(true);
        commentScrollPane = new JScrollPane(comment);

        treeContainer = new JPanel();
        treeContainer.setBorder(BorderFactory.createTitledBorder(_("TreeGen.Tree")));
        treeContainer.setLayout(new GridBagLayout());
        layoutTreeContainer();
        container.add(treeContainer);

        defaultRules = new JRadioButton(new DefaultRulesAction());
        customRules = new JRadioButton(new CustomRulesAction());
        rulesGroup = new ButtonGroup();
        rulesGroup.add(defaultRules);
        rulesGroup.add(customRules);
        rulesGroup.setSelected(defaultRules.getModel(), true);
        rulesGroup.setSelected(defaultRules.getModel(), true);
        editRulesetsButton = new JButton(new EditRulesetsAction(this));
        editRulesetsButton.setPreferredSize(new Dimension(120, 30));
        rulesets = new DefaultComboBoxModel();
        rulesetsList = new JComboBox(rulesets);
        rulesetsList.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                genOptions.setRuleset((Ruleset) rulesetsList.getSelectedItem());
                validConfig();
            }
        });
        rulesetsList.setPreferredSize(editRulesetsButton.getPreferredSize());
        rulesetsListDefaultBorder = rulesetsList.getBorder();
        setChooseCustomRules(false);
        deglycosilate = new JCheckBox(_("TreeGen.Deglycosilate"));

        optionsContainer = new JPanel();
        optionsContainer.setBorder(BorderFactory.createTitledBorder(_("TreeGen.Options")));
        optionsContainer.setLayout(new GridBagLayout());
        layoutOptionsContainer();
        container.add(optionsContainer);

        okButton = new JButton(new OKAction());
        getRootPane().setDefaultButton(okButton);
        cancelButton = StandardButtonFactory.createCancelButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        container.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton));

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void layoutTreeContainer() {
        GridBagConstraints c = new GridBagConstraints();

        c.insets = insets;

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        treeContainer.add(titleLabel, c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        treeContainer.add(commentLabel, c);

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        treeContainer.add(title, c);
        c.weightx = 1.0;

        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;
        c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1.0;
        treeContainer.add(commentScrollPane, c);
    }

    private void layoutOptionsContainer() {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = insets;

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        optionsContainer.add(defaultRules, c);

        c.fill = GridBagConstraints.NONE;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        optionsContainer.add(customRules, c);

        c.fill = GridBagConstraints.WEST;
        c.gridx = 2;
        c.gridy = 1;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 1.0;
        optionsContainer.add(rulesetsList, c);

        c.fill = GridBagConstraints.WEST;
        c.gridx = 2;
        c.gridy = 2;
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 1.0;
        optionsContainer.add(editRulesetsButton, c);

        c.fill = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 3;
        c.anchor = GridBagConstraints.FIRST_LINE_START;
        c.weightx = 0;
        optionsContainer.add(deglycosilate, c);
    }

    /**
     * Set <code>Boolean</code> b = true to choose custom rules
     */
    private void setChooseCustomRules(boolean b) {
        rulesetsList.setEnabled(b);
        editRulesetsButton.setEnabled(b);
        genOptions.setCustomRules(b);
    }

    void loadCustomRulesets() {
        List<Ruleset> list = null;

        list = DBExceptionHandler.callDBManager(controller.getDbManager(), new DBFunction<List<Ruleset>>() {
            @Override
            public List<Ruleset> call() throws DatabaseException {
                return controller.getDbManager().getAllRulesets();
            }
        });

        rulesets.removeAllElements();
        for (Ruleset ruleset : list) {
            rulesets.addElement(ruleset);
        }
    }

    /**
     * Checks if the config is valid
     */
    private void validConfig() {
        if (isTitleValid() && isCustomRulesetValid()) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }

    /**
     * Checks if title is valid
     */
    private boolean isTitleValid() {
        String titleString = title.getText().trim();
        boolean valid = true;

        if (titleString.isEmpty() || titleString.length() > 100) {
            valid = false;
        }

        // title exists for another tree?
        for (Tree tree : dataset.getTrees()) {
            if (titleString.toLowerCase().equals(tree.getTitle().toLowerCase()) && tree != this.tree) {
                valid = false;
            }
        }

        if (valid) {
            title.setBorder(titleDefaultBorder);
            /**
             * The border created in the not valid case eats some space from the
             * title textarea. After setting the default border of the title
             * textarea, the area is to small. Thus we fix it by invoking layout
             * calculation of the parent component.
             */
            optionsContainer.revalidate();
        } else {
            title.setBorder(BorderFactory.createLineBorder(Color.RED));
        }

        return valid;
    }

    /**
     * Checks if custom ruleset is selected
     */
    private boolean isCustomRulesetValid() {
        boolean valid = true;

        // custom rules chosen, but combobox empty?
        if (customRules.isSelected() && rulesets.getSize() == 0) {
            valid = false;
            rulesetsList.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else {
            rulesetsList.setBorder(rulesetsListDefaultBorder);
        }

        return valid;
    }

    /**
     * Gets the status result of this dialog
     * 
     * @return true if the user clicked ok
     */
    public boolean getResult() {
        return result;
    }

    /**
     * @return true if dialog was constructed to edit a tree, false if it was
     *         constructed to create a new tree
     */
    public boolean isEditOnly() {
        return editOnly;
    }

    /**
     * Gets the generator options for the tree generation
     * 
     * @return the <code>GeneratatorOptions</code>
     */
    public GeneratorOptions getGeneratorOptions() {
        return genOptions;
    }

    private class DefaultRulesAction extends AbstractAction {

        DefaultRulesAction() {
            putValue(NAME, _("TreeGen.UseDefaultRules"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            setChooseCustomRules(false);
            validConfig();
        }
    }

    private class CustomRulesAction extends AbstractAction {

        CustomRulesAction() {
            putValue(NAME, _("TreeGen.UseCustomRules") + ":");
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            setChooseCustomRules(true);
            validConfig();
        }
    }

    private class EditRulesetsAction extends AbstractAction {
        Window window;

        EditRulesetsAction(Window window) {
            this.window = window;
            putValue(NAME, _("TreeGen.EditRulesets"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            controller.showRulesetManagementDialog(window);
            loadCustomRulesets();
            validConfig();
        }
    }

    private class OKAction extends AbstractAction {
        public OKAction() {
            super();
            if (editOnly) {
                putValue(NAME, _("TreeGen.OK.Edit"));
            } else {
                putValue(NAME, _("TreeGen.OK.New"));
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result = true;
            genOptions.setComment(comment.getText().trim());
            genOptions.setTitle(title.getText().trim());
            genOptions.setDeglycosilate(deglycosilate.isSelected());
            dispose();
        }
    }
}
