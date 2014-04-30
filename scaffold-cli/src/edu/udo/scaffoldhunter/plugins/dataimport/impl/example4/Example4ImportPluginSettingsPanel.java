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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.example4;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.Serializable;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Bernhard Dick
 * 
 */
public class Example4ImportPluginSettingsPanel extends PluginSettingsPanel {
    private JCheckBox errorCheckBox;
    private JTextField errorMessageTextField;
    private JLabel moleculeTitleLabel;
    private JTextField moleculeTitleTextField;
    private JCheckBox generateMessageCheckBox;

    /**
     * @param arguments
     * 
     */
    public Example4ImportPluginSettingsPanel(Example4ImportPluginArguments arguments) {
        // Check if we get Arguments, otherwise fill with default values
        if (arguments == null) {
            arguments = new Example4ImportPluginArguments();
            arguments.error = false;
            arguments.errorMessage = "Very bad Exception";
            arguments.moleculeTitle = "Title";
            arguments.generateMessage = false;
        }

        // Generate Elements
        errorCheckBox = new JCheckBox("Generate Error:");
        errorCheckBox.setSelected(arguments.error);

        errorMessageTextField = new JTextField(arguments.errorMessage);

        moleculeTitleLabel = new JLabel("Molecule Title:");

        moleculeTitleTextField = new JTextField(arguments.moleculeTitle);

        generateMessageCheckBox = new JCheckBox("Generate a Message");
        generateMessageCheckBox.setSelected(arguments.generateMessage);

        // Layout part
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(errorCheckBox, gbc);

        gbc.gridy += 1;
        this.add(moleculeTitleLabel, gbc);

        gbc.gridy += 1;
        gbc.gridwidth += 1;
        this.add(generateMessageCheckBox, gbc);
        gbc.gridwidth -= 1;

        gbc.gridy = 0;
        gbc.gridx += 1;
        this.add(errorMessageTextField, gbc);

        gbc.gridy += 1;
        this.add(moleculeTitleTextField, gbc);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginSettingsPanel#getSettings
     * ()
     */
    @Override
    public Serializable getSettings() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginSettingsPanel#getArguments
     * ()
     */
    @Override
    public Object getArguments() {
        Example4ImportPluginArguments arguments = new Example4ImportPluginArguments();

        arguments.error = errorCheckBox.isSelected();
        arguments.errorMessage = errorMessageTextField.getText();
        arguments.moleculeTitle = moleculeTitleTextField.getText();
        arguments.generateMessage = generateMessageCheckBox.isSelected();

        return arguments;
    }

}
