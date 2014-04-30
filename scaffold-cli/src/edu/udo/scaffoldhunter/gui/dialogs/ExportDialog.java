/*
 * ScaffoldHunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffoldhunter installation for details.
 *
 * This file is part of ScaffoldHunter.
 *
 * ScaffoldHunter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScaffoldHunter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package edu.udo.scaffoldhunter.gui.dialogs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu.Separator;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.udo.scaffoldhunter.gui.MainWindow;
import edu.udo.scaffoldhunter.gui.util.FileChooser;
import edu.udo.scaffoldhunter.model.dataexport.ExportInterface;
import edu.udo.scaffoldhunter.model.dataexport.ExportIterable;
import edu.udo.scaffoldhunter.model.dataexport.csv.CSVExport;
import edu.udo.scaffoldhunter.model.dataexport.sdf.SDFExport;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Philipp Kopp
 * 
 */
public class ExportDialog extends JDialog implements ActionListener {
    private JPanel panel;
    private JPanel properties;
    private JPanel method;
    private JButton exportButton;
    private JButton cancelButton;
    private JRadioButton csv;
    private JRadioButton sdf;
    private HashMap<JCheckBox, PropertyDefinition> shownPropDefs;
    private Collection<JCheckBox> propertyBoxes;
    private JButton markAll;
    private JButton markNone;
    private JScrollPane propertyScroll;
    private JFileChooser chooser;
    private Subset subset;
    private DbManager db;
    private JCheckBox smiles;
    private JCheckBox exportDescriptionsCheckBox;
    private SDFExport sdfExport;
    private CSVExport csvExport;
    private ExportInterface export;
    private JPanel exportConfigPanel;

    /**
     * @param subset
     * @param db
     * @param window
     */
    public ExportDialog(Subset subset, DbManager db, MainWindow window) {
        setAlwaysOnTop(true);

        // TODO built a better UI Layout
        this.db = db;
        this.subset = subset;
        this.sdfExport = new SDFExport();
        this.csvExport = new CSVExport();
        this.export = csvExport;
        this.exportConfigPanel = new JPanel();
        exportConfigPanel.add(export.getConfigurationPanel());
        method = new JPanel();
        method.setLayout(new BoxLayout(method, BoxLayout.Y_AXIS));
        ButtonGroup methodGroup = new ButtonGroup();
        csv = new JRadioButton("CSV");
        csv.setActionCommand("CSV");
        sdf = new JRadioButton("SDF");
        sdf.setActionCommand("SDF");
        methodGroup.add(csv);
        method.add(csv);
        csv.doClick();
        methodGroup.add(sdf);
        method.add(sdf);

        csv.addActionListener(this);
        sdf.addActionListener(this);

        properties = new JPanel();
        properties.setLayout(new BoxLayout(properties, BoxLayout.Y_AXIS));

        shownPropDefs = new HashMap<JCheckBox, PropertyDefinition>();
        propertyBoxes = new ArrayList<JCheckBox>();
        exportDescriptionsCheckBox = new JCheckBox("Property Descriptions");
        properties.add(exportDescriptionsCheckBox);
        smiles = new JCheckBox("SMILES");
        properties.add(smiles);

        for (PropertyDefinition propDef : subset.getSession().getDataset().getPropertyDefinitions().values()) {

            if (!propDef.isScaffoldProperty()) {
                JCheckBox cb = new JCheckBox(propDef.getTitle());
                propertyBoxes.add(cb);
                shownPropDefs.put(cb, propDef);

                properties.add(cb);
            }
        }

        properties.setPreferredSize(properties.getMinimumSize());

        propertyScroll = new JScrollPane(properties);
        propertyScroll.setPreferredSize(new Dimension(100, 200));
        Dimension dim = new Dimension(properties.getMinimumSize().width + 20, 500);
        propertyScroll.setPreferredSize(dim);

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        // Buttons
        exportButton = new JButton("Export");
        exportButton.addActionListener(this);

        cancelButton = new JButton(I18n.get("Button.Cancel"));
        cancelButton.addActionListener(this);
        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(exportButton, cancelButton);

        markAll = new JButton("Select all");
        markAll.addActionListener(this);
        markNone = new JButton("Deselect all");
        markNone.addActionListener(this);
        JButton[] bar = { markAll, markNone };
        JPanel selectionButtons = ButtonBarFactory.buildCenteredBar(bar);

        panel.add(new JLabel("Choose the export method:"));
        panel.add(method);
        panel.add(new Separator());
        panel.add(new JLabel("Export Configuration:"));
        exportConfigPanel.setSize(200, 200);
        panel.add(exportConfigPanel);
        panel.add(new Separator());
        panel.add(new JLabel("Which properties should be exported"));
        panel.add(selectionButtons);

        panel.add(propertyScroll);
        panel.add(buttonPanel);

        setTitle(I18n.get("Export.Dialog.Title"));
        getContentPane().add(panel);
        Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();

        setModal(true);
        pack();
        setBounds((screensize.width - getWidth()) / 2, (screensize.height - getHeight()) / 2, getWidth(), getHeight());
    }

    /**
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == markAll) {
            smiles.setSelected(true);
            for (JCheckBox box : propertyBoxes) {
                box.setSelected(true);
            }
        }
        if (e.getSource() == markNone) {
            smiles.setSelected(false);
            for (JCheckBox box : propertyBoxes) {
                box.setSelected(false);
            }

        }
        if (e.getSource() == exportButton) {
            String fileType = "";
            if (csv.isSelected()) {
                fileType = "csv";
            } else {
                if (sdf.isSelected()) {
                    fileType = "sdf";
                }
            }
            FileFilter filter = new FileNameExtensionFilter(fileType.toUpperCase(), fileType);
            chooser = new FileChooser();
            chooser.addChoosableFileFilter(filter);
            int a = chooser.showSaveDialog(this);
            if (a == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                String path = file.getAbsolutePath();
                if (path.endsWith(fileType)) {
                    // TODO start export module

                } else {
                    path = path + "." + fileType;
                    // TODO start export module

                }
                ArrayList<PropertyDefinition> chosenProps = new ArrayList<PropertyDefinition>();

                for (JCheckBox cb : shownPropDefs.keySet()) {
                    if (cb.isSelected()) {
                        chosenProps.add(shownPropDefs.get(cb));
                    }
                }
                String[] propNames;
                // TODO find a better solution for this
                if (smiles.isSelected()) {
                    propNames = new String[chosenProps.size() + 1];
                    propNames[0] = "SMILES";
                    for (int i = 1; i < propNames.length; i++) {
                        propNames[i] = chosenProps.get(i - 1).getTitle();
                    }
                } else {
                    propNames = new String[chosenProps.size()];
                    String title;
                    for (int i = 0; i < propNames.length; i++) {
                        title = chosenProps.get(i).getTitle();
                        if (exportDescriptionsCheckBox.isSelected()) {
                            title += " (".concat(chosenProps.get(i).getDescription()).concat(")");
                        }
                        propNames[i] = title;
                    }
                }
                ExportIterable iterable = new ExportIterable(subset, chosenProps, db,
                        exportDescriptionsCheckBox.isSelected(), smiles.isSelected());

                export.writeData(iterable, propNames, path);
                this.dispose();
            }
        }
        if (e.getSource() == cancelButton) {
            dispose();
        }

        if (e.getActionCommand().equals("SDF")) {
            export = sdfExport;
            exportConfigPanel.removeAll();
            exportConfigPanel.add(export.getConfigurationPanel());
            this.validate();
        }

        if (e.getActionCommand().equals("CSV")) {
            export = csvExport;
            exportConfigPanel.removeAll();
            exportConfigPanel.add(export.getConfigurationPanel());
            this.validate();
        }
    }
}
