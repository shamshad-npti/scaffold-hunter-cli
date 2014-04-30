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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.csv;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;
import edu.udo.scaffoldhunter.gui.util.FileChooser;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Bernhard Dick
 * 
 */
public class CSVImportPluginSettingsPanel extends PluginSettingsPanel implements ActionListener {
    private static Logger logger = LoggerFactory.getLogger(CSVImportPluginSettingsPanel.class);
    
    private JLabel filenameLabel;
    private JLabel separatorLabel;
    private JLabel quotecharLabel;
    private JLabel optionsLabel;
    private JLabel smilesColumnIdLabel;

    private JTextField filenameTextField;
    private JComboBox separatorComboBox;
    private JComboBox quotecharComboBox;
    private JCheckBox strictQuotesCheckBox;
    private JCheckBox firstRowHeaderCheckBox;
    private JComboBox smilesColumnIdComboBox;

    private JButton fileButton;
    private JFileChooser myFileChooser;

    private JButton fillTableButton;
    private JTable dataTable;
    private JScrollPane dataTableScrollPane;

    private CSVImportPluginSettings settings;
    private CSVImportPluginArguments arguments;

    /**
     * @param settings
     * @param arguments
     */
    public CSVImportPluginSettingsPanel(CSVImportPluginSettings settings, CSVImportPluginArguments arguments) {
        super();
        if (settings == null) {
            settings = new CSVImportPluginSettings();
            settings.setLastFilename("");
        }
        this.settings = settings;
        if (arguments == null) {
            arguments = new CSVImportPluginArguments(settings.getLastFilename(), ',', '\"', false, true, 0);
        }

        this.arguments = arguments;

        filenameLabel = new JLabel("CSV File:");
        filenameTextField = new JTextField();
        filenameTextField.setText(arguments.getFilename());
        filenameTextField.addActionListener(this);
        fileButton = new JButton("...");
        myFileChooser = new FileChooser();
        myFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("CSV (*.csv)", "csv"));
        myFileChooser.setPreferredSize(new Dimension(800, (int) (800*0.618)));  // golden ratio
        
        fileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int res = myFileChooser.showOpenDialog(null);
                if (res == JFileChooser.APPROVE_OPTION) {
                    filenameTextField.setText(myFileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        fileButton.addActionListener(this);

        smilesColumnIdLabel = new JLabel("SMILES column");
        String[] columnNames = CSVImportPlugin.getDefaultColumnNames(Math.max(10, arguments.getSmilesColumnId() + 1));
        smilesColumnIdComboBox = new JComboBox(columnNames);
        smilesColumnIdComboBox.setSelectedIndex(arguments.getSmilesColumnId());
        smilesColumnIdComboBox.addActionListener(this);
        separatorLabel = new JLabel("Cell separator:");
        separatorComboBox = new JComboBox();
        separatorComboBox.addItem(",");
        separatorComboBox.addItem(";");
        separatorComboBox.addItem(":");
        separatorComboBox.addItem("{Tab}");
        separatorComboBox.addItem("{Space}");
        separatorComboBox.setEditable(true);
        if (arguments.getSeparator() == '\t') {
            separatorComboBox.setSelectedItem("{Tab}");
        } else if (arguments.getSeparator() == ' ') {
            separatorComboBox.setSelectedItem("{Space}");
        } else {
            separatorComboBox.setSelectedItem(arguments.getSeparator());
        }
        separatorComboBox.addActionListener(this);

        quotecharLabel = new JLabel("Quotation character:");
        quotecharComboBox = new JComboBox();
        quotecharComboBox.addItem('"');
        quotecharComboBox.addItem('\'');
        quotecharComboBox.setEditable(true);
        quotecharComboBox.setSelectedItem(arguments.getQuotechar());
        quotecharComboBox.addActionListener(this);

        optionsLabel = new JLabel("Further Options:");
        strictQuotesCheckBox = new JCheckBox("quotation required", arguments.isStrictQuotes());
        strictQuotesCheckBox.addActionListener(this);
        firstRowHeaderCheckBox = new JCheckBox("First row contains names", arguments.isFirstRowHeader());
        firstRowHeaderCheckBox.addActionListener(this);

        fillTableButton = new JButton("Preview with current settings");

        dataTable = new JTable(new DefaultTableModel(CSVImportPlugin.getDefaultColumnNames(10), 10));
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        dataTableScrollPane = new JScrollPane(dataTable);
        dataTableScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        dataTableScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(filenameLabel, gbc);

        gbc.gridy += 1;
        this.add(smilesColumnIdLabel, gbc);

        gbc.gridy += 1;
        this.add(separatorLabel, gbc);

        gbc.gridy += 1;
        this.add(quotecharLabel, gbc);

        gbc.gridy += 1;
        this.add(optionsLabel, gbc);

        gbc.weightx = 1;
        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(filenameTextField, gbc);

        gbc.gridwidth = 2;
        gbc.gridy += 1;
        this.add(smilesColumnIdComboBox, gbc);

        gbc.gridy += 1;
        this.add(separatorComboBox, gbc);

        gbc.gridy += 1;
        this.add(quotecharComboBox, gbc);

        gbc.gridy += 1;
        this.add(strictQuotesCheckBox, gbc);

        gbc.gridy += 1;
        this.add(firstRowHeaderCheckBox, gbc);

        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.gridy += 1;
        this.add(fillTableButton, gbc);

        gbc.gridy += 1;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(dataTableScrollPane, gbc);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridwidth = 1;
        gbc.weightx = 0;
        gbc.gridx = 2;
        gbc.gridy = 0;
        this.add(fileButton, gbc);

        fillTableButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                File csvFile = new File(filenameTextField.getText());
                CSVImportPluginArguments arguments = getArguments();
                CSVReader csvReader = null;
                try {
                    csvReader = new CSVReader(new FileReader(csvFile), arguments.getSeparator(), arguments
                            .getQuotechar(), arguments.isStrictQuotes());
                    String[] header;
                    String[] lastLine;
                    String[][] data = new String[10][];
                    lastLine = csvReader.readNext();
                    if (arguments.isFirstRowHeader()) {
                        header = lastLine;
                    } else {
                        header = CSVImportPlugin.getDefaultColumnNames(lastLine.length);
                        data[0] = lastLine;
                    }
                    for (int i = arguments.isFirstRowHeader() ? 0 : 1; i < 10; i++) {
                        if ((lastLine = csvReader.readNext()) != null) {
                            data[i] = lastLine;
                        }
                    }
                    dataTable.setModel(new DefaultTableModel(data, header));
                    int selectedIndex = smilesColumnIdComboBox.getSelectedIndex();
                    smilesColumnIdComboBox.removeAllItems();
                    for (String s : header) {
                        smilesColumnIdComboBox.addItem(s);
                    }
                    if (selectedIndex < smilesColumnIdComboBox.getItemCount()) {
                        smilesColumnIdComboBox.setSelectedIndex(selectedIndex);
                    }

                } catch (FileNotFoundException e1) {
                    JOptionPane.showMessageDialog((Component) e.getSource(),
                            "An error occured:\nFile ".concat(arguments.getFilename()).concat(" not found."), "Error",
                            JOptionPane.ERROR_MESSAGE);
                } catch (IOException e1) {
                    JOptionPane.showMessageDialog((Component) e.getSource(),
                            "An error occured:\n".concat(e1.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (csvReader != null) {
                        try {
                            csvReader.close();
                        } catch (IOException ex) {
                            logger.error("csvReader could not be closed: {}", ex.getMessage());
                        }
                    }
                }
            }
        });
    }

    private void updateArguments() {
        arguments.setFilename(filenameTextField.getText());
        String tmp = separatorComboBox.getSelectedItem().toString();
        if (tmp.equals("{Tab}")) {
            arguments.setSeparator('\t');
        } else if (tmp.equals("{Space}")) {
            arguments.setSeparator(' ');
        } else {
            arguments.setSeparator(tmp.charAt(0));
        }
        arguments.setQuotechar(quotecharComboBox.getSelectedItem().toString().charAt(0));
        arguments.setStrictQuotes(strictQuotesCheckBox.isSelected());
        arguments.setFirstRowHeader(firstRowHeaderCheckBox.isSelected());
        arguments.setSmilesColumnId(smilesColumnIdComboBox.getSelectedIndex());
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginSettingsPanel#
     * getSettings()
     */
    @Override
    public Serializable getSettings() {
        settings.setLastFilename(filenameTextField.getText());
        return settings;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginSettingsPanel#
     * getArguments()
     */
    @Override
    public CSVImportPluginArguments getArguments() {
        updateArguments();
        return arguments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        updateArguments();
    }

}
