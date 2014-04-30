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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.sdf;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import edu.udo.scaffoldhunter.gui.util.FileChooser;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Bernhard Dick
 * 
 */
// TODO make a better looking UI
public class SDFImportPluginSettingsPanel extends PluginSettingsPanel {
    private JTextField filenameTextField;
    private JLabel filenameLabel;
    private JButton fileButton;
    private JFileChooser myFileChooser;
    private SDFImportPluginArguments arguments;
    private SDFImportPluginSettings settings;

    /**
     * @param settings
     * @param arguments
     * 
     */
    public SDFImportPluginSettingsPanel(SDFImportPluginSettings settings, SDFImportPluginArguments arguments) {
        super();
        if (settings == null) {
            settings = new SDFImportPluginSettings();
            settings.setLastFilename("");
        }
        this.settings = settings;
        if (arguments == null) {
            arguments = new SDFImportPluginArguments(settings.getLastFilename());
        }

        this.arguments = arguments;

        filenameLabel = new JLabel("SDF File:");
        filenameTextField = new JTextField();
        filenameTextField.setText(arguments.getFilename());
        fileButton = new JButton("...");
        myFileChooser = new FileChooser();
        myFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("MOL (*.mol)", "mol"));
        myFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("SDF (*.sdf)", "sdf"));
        myFileChooser.setPreferredSize(new Dimension(800, (int) (800*0.618)));  // golden ratio

        fileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                int res = myFileChooser.showOpenDialog(null);
                if (res == JFileChooser.APPROVE_OPTION) {
                    SDFImportPluginSettingsPanel.this.arguments.setFilename(myFileChooser.getSelectedFile().getAbsolutePath());
                    filenameTextField.setText(SDFImportPluginSettingsPanel.this.arguments.getFilename());
                }
            }
        });

        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.weightx = 0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        this.add(filenameLabel, gbc);

        gbc.weightx = 1;
        gbc.gridx = 1;
        gbc.gridy = 0;
        this.add(filenameTextField, gbc);

        gbc.weightx = 0;
        gbc.gridx = 3;
        gbc.gridy = 0;
        this.add(fileButton, gbc);

        // workaround to be on top
        gbc.gridy = 1;
        gbc.weighty = 1;
        gbc.anchor = GridBagConstraints.PAGE_END;
        gbc.fill = GridBagConstraints.VERTICAL;
        this.add(new JPanel(), gbc);
    }

    /**
     * @return arguments for one plugin run
     */
    @Override
    public SDFImportPluginArguments getArguments() {
        arguments.setFilename(filenameTextField.getText());
        return arguments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.PluginSettingsComponent
     * #getSettings()
     */
    @Override
    public Serializable getSettings() {
        settings.setLastFilename(arguments.getFilename());
        return settings;
    }
}
