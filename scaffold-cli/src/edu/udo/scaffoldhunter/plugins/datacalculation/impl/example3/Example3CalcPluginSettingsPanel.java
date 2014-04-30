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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.example3;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JCheckBox;

import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Philipp Lewe
 * 
 */
public class Example3CalcPluginSettingsPanel extends PluginSettingsPanel {

    private Example3CalcPluginArguments arguments;
    private JCheckBox checkbox;

    Example3CalcPluginSettingsPanel(final Example3CalcPluginArguments arguments) {
        this.arguments = arguments;

        checkbox = new JCheckBox(
                "if selected the calculated property for all molecules is set to '1.0' otherwise to '-0.1'",
                arguments.isCheckboxChecked());
        checkbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                arguments.setCheckboxIsChecked(checkbox.isSelected());
            }
        });

        add(checkbox);
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
        return arguments;
    }

}
