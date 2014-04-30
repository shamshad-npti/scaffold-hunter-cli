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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.sql;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Bernhard Dick
 * 
 */
public class SQLImportPluginSettingsPanel extends PluginSettingsPanel {

    private JLabel dbLabel;
    private JLabel hostnameLabel;
    private JLabel schemaLabel;
    private JLabel userLabel;
    private JLabel passLabel;
    private JLabel tableLabel;
    private JLabel selectClauseLabel;
    private JLabel smilesLabel;
    private JLabel molLabel;

    private JComboBox dbComboBox;
    private JTextField hostnameTextField;
    private JTextField schemaTextField;
    private JTextField userTextField;
    private JPasswordField passPasswordField;
    private JButton getTablesButton;
    private JComboBox tableComboBox;
    private JTextArea selectClauseTextArea;
    private JScrollPane selectClauseScrollPane;
    private JButton executeQueryButton;
    private JComboBox smilesComboBox;
    private JComboBox molComboBox;

    private SQLImportPluginArguments arguments;

    /**
     * @param settings
     * @param arguments
     */
    public SQLImportPluginSettingsPanel(Serializable settings, SQLImportPluginArguments arguments) {
        super();
        if (arguments == null) {
            arguments = new SQLImportPluginArguments("mysql", "localhost", "", "", "", "", "", " - none - ",
                    " - none - ");
        }
        this.arguments = arguments;

        dbLabel = new JLabel("DB Type: ");
        dbComboBox = new JComboBox();
        dbComboBox.setEditable(true);
        dbComboBox.addItem("mysql");
        dbComboBox.setSelectedItem(arguments.getDb());

        hostnameLabel = new JLabel("DB Host: ");
        hostnameTextField = new JTextField(arguments.getHostname());

        schemaLabel = new JLabel("DB schema: ");
        schemaTextField = new JTextField(arguments.getSchema());

        userLabel = new JLabel("DB User: ");
        userTextField = new JTextField(arguments.getUser());

        passLabel = new JLabel("DB Password: ");
        passPasswordField = new JPasswordField(arguments.getPass());

        getTablesButton = new JButton("Get tables from database");

        tableLabel = new JLabel("DB Table: ");
        tableComboBox = new JComboBox();
        tableComboBox.addItem(arguments.getTable());

        selectClauseLabel = new JLabel("SQL Statement: ");
        selectClauseTextArea = new JTextArea(arguments.getSelectClause());
        selectClauseScrollPane = new JScrollPane(selectClauseTextArea);
        selectClauseScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        selectClauseScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        executeQueryButton = new JButton("Execute SQL Query");

        smilesLabel = new JLabel("SMILES: ");
        smilesComboBox = new JComboBox();
        smilesComboBox.addItem(arguments.getSmilesColumn());

        molLabel = new JLabel("MOL: ");
        molComboBox = new JComboBox();
        molComboBox.addItem(arguments.getMolColumn());

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(dbLabel, gbc);

        gbc.gridy += 1;
        this.add(hostnameLabel, gbc);

        gbc.gridy += 1;
        this.add(schemaLabel, gbc);

        gbc.gridy += 1;
        this.add(userLabel, gbc);

        gbc.gridy += 1;
        this.add(passLabel, gbc);

        gbc.gridy += 1;
        gbc.gridwidth = 2;
        this.add(getTablesButton, gbc);
        gbc.gridwidth = 1;

        gbc.gridy += 1;
        this.add(tableLabel, gbc);

        gbc.gridy += 1;
        this.add(selectClauseLabel, gbc);

        gbc.gridy += 2;
        gbc.gridwidth = 2;
        this.add(executeQueryButton, gbc);

        gbc.gridy += 1;
        gbc.gridwidth = 1;
        this.add(smilesLabel, gbc);

        gbc.gridy += 1;
        this.add(molLabel, gbc);

        gbc.weightx = 1;
        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(dbComboBox, gbc);

        gbc.gridy += 1;
        this.add(hostnameTextField, gbc);

        gbc.gridy += 1;
        this.add(schemaTextField, gbc);

        gbc.gridy += 1;
        this.add(userTextField, gbc);

        gbc.gridy += 1;
        this.add(passPasswordField, gbc);

        gbc.gridy += 2;
        this.add(tableComboBox, gbc);

        gbc.gridy += 1;
        gbc.weighty = 1;
        gbc.gridheight = 2;
        // gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.fill = GridBagConstraints.BOTH;
        this.add(selectClauseScrollPane, gbc);
        gbc.weighty = 0;
        gbc.gridheight = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy += 3;
        this.add(smilesComboBox, gbc);

        gbc.gridy += 1;
        this.add(molComboBox, gbc);

        getTablesButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                tableComboBox.removeAllItems();
                Properties connProps = new Properties();
                connProps.put("user", userTextField.getText());
                connProps.put("password", new String(passPasswordField.getPassword()));
                try {
                    Connection con = DriverManager.getConnection(generateDbURL(), connProps);
                    ResultSet res = con.getMetaData().getTables(null, null, "%", null);
                    while (res.next()) {
                        tableComboBox.addItem(res.getString(3));
                    }

                    con.close();
                } catch (SQLException e1) {
                    JOptionPane.showMessageDialog((Component) e.getSource(),
                            "An error occured:\n".concat(e1.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        tableComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (tableComboBox.getSelectedItem() != null) {
                    selectClauseTextArea.setText("SELECT * FROM ".concat((String) tableComboBox.getSelectedItem())
                            .concat(";"));
                }
            }
        });

        executeQueryButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                smilesComboBox.removeAllItems();
                smilesComboBox.addItem(" - none - ");

                molComboBox.removeAllItems();
                molComboBox.addItem(" - none - ");

                Properties connProps = new Properties();
                connProps.put("user", userTextField.getText());
                connProps.put("password", new String(passPasswordField.getPassword()));
                try {
                    Connection con = DriverManager.getConnection(generateDbURL(), connProps);
                    Statement statement = con.createStatement();
                    statement.setMaxRows(1);
                    ResultSet res = statement.executeQuery(selectClauseTextArea.getText());
                    ResultSetMetaData meta = res.getMetaData();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        smilesComboBox.addItem(meta.getColumnName(i));
                        molComboBox.addItem(meta.getColumnName(i));
                    }
                    statement.close();
                    con.close();
                } catch (SQLException e1) {
                    JOptionPane.showMessageDialog((Component) e.getSource(),
                            "An error occured:\n".concat(e1.getMessage()), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private String generateDbURL() {
        return this.getArguments().getUrl();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginSettingsPanel#
     * getSettings()
     */
    @Override
    public Serializable getSettings() {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginSettingsPanel#
     * getArguments()
     */
    @Override
    public SQLImportPluginArguments getArguments() {
        arguments.setDb(dbComboBox.getSelectedItem().toString());
        arguments.setHostname(hostnameTextField.getText());
        arguments.setSchema(schemaTextField.getText());
        arguments.setUser(userTextField.getText());
        arguments.setPass(new String(passPasswordField.getPassword()));
        arguments.setSelectClause(selectClauseTextArea.getText());
        arguments.setSmilesColumn((String) smilesComboBox.getSelectedItem());
        arguments.setMolColumn((String) molComboBox.getSelectedItem());
        return arguments;
    }

}
