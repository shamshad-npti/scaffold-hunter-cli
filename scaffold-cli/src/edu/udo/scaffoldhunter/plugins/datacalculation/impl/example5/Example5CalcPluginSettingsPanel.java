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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.example5;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Philipp Lewe
 * 
 */
public class Example5CalcPluginSettingsPanel extends PluginSettingsPanel {

    private Example5CalcPluginArguments arguments;
    private JCheckBox checkbox;
    private JList list;

    Example5CalcPluginSettingsPanel(final Example5CalcPluginArguments arguments,
            Set<PropertyDefinition> availableProperties) {
        this.arguments = arguments;

        checkbox = new JCheckBox(
                "if selected, 'new property = old value + 1.0', otherwise 'new property = old value - 0.1'",
                arguments.isCheckboxChecked());
        checkbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                arguments.setCheckboxIsChecked(checkbox.isSelected());
            }
        });
        add(checkbox);

        DefaultListModel model = new DefaultListModel();
        list = new JList(model);
        for (PropertyDefinition propertyDefinition : availableProperties) {
            if (propertyDefinition.getPropertyType() == PropertyType.NumProperty) {
                model.addElement(propertyDefinition);
            }
        }
        list.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting() && !list.isSelectionEmpty()) {
                    PropertyDefinition chosenProperty = (PropertyDefinition) list.getSelectedValue();
                    arguments.setPropDef(chosenProperty);
                } else {
                    arguments.setPropDef(null);
                }
            }
        });
        if (arguments.getPropDef() != null) {
            list.setSelectedValue(arguments.getPropDef(), true);
        } else {
            list.setSelectedIndex(0);
        }

        add(list);

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
