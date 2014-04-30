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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.dummy;

import java.io.Serializable;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Bernhard Dick
 * 
 */
public class DummyImportPluginSettingsPanel extends PluginSettingsPanel {
    private JLabel dummyLabel;
    private JCheckBox generateErrorCheckBox;
    private JTextArea errorMessageTextArea;

    /**
     * 
     */
    public DummyImportPluginSettingsPanel() {
        dummyLabel = new JLabel("Hello World!");
        generateErrorCheckBox = new JCheckBox("Generate Error");
        errorMessageTextArea = new JTextArea("Errormessage");
        this.add(dummyLabel);
        this.add(generateErrorCheckBox);
        this.add(errorMessageTextArea);
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
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.PluginSettingsComponent
     * #getArguments()
     */
    @Override
    public Object getArguments() {
        return new DummyImportPluginArguments(generateErrorCheckBox.isSelected(), errorMessageTextArea.getText());
    }
}
