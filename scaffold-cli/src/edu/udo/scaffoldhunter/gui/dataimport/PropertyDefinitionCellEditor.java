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

package edu.udo.scaffoldhunter.gui.dataimport;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractCellEditor;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.event.ListDataListener;
import javax.swing.table.TableCellEditor;

import edu.udo.scaffoldhunter.model.dataimport.PropertyDefinitionList;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * A Table Cell Editor for editing property Definitions. Provides a Combo Box,
 * which allows choosing from properties which are already defined and two
 * additional entries: "Do not map this property" and
 * "Create new PropertyDefinition". The latter will show a
 * {@link PropertyDefinitonCreationDialog}.
 * 
 * @author Henning Garus
 * @author Till Schäfer
 */
public class PropertyDefinitionCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private final JComboBox box = new JComboBox();
    private final Vector<PropertyDefinition> existingPropDefs;
    private final ComboBoxModel editPropDefs = new PropertyDefinitionComboBoxModel();
    private final PropertyDefinitionList propertyDefinitions;
    private final Set<String> probablyNumeric;
    private final MergeListener mergeListener = new MergeListener();
    private final PropertyDefinitionListCellRenderer propDefListCellRenderer = new PropertyDefinitionListCellRenderer();

    private Dialog owner;
    private String sourceProperty;

    /**
     * @param owner
     *            The owner which will be set as owner of the
     *            <code>PropertyDefinitonCreationDialog</code>
     * @param propertyDefinitions
     *            a property definition list which contains existing property
     *            definitions to chooses from. When a user creates a new
     *            property definition, it will be added to this list.
     * @param probablyNumeric
     *            A set of source properties which are probably numeric. These
     *            will default to numeric during PropertyDefinition creation.
     * @param existingPropDefs
     *            The existing properties which will be displayed when selecting
     *            the property definition on which to merge (for merge by
     *            property)
     */
    public PropertyDefinitionCellEditor(Dialog owner, PropertyDefinitionList propertyDefinitions,
            Set<String> probablyNumeric, Iterable<PropertyDefinition> existingPropDefs) {
        this.owner = owner;
        this.propertyDefinitions = propertyDefinitions;
        this.probablyNumeric = probablyNumeric;
        this.existingPropDefs = new Vector<PropertyDefinition>();
        for (PropertyDefinition propDef : existingPropDefs) {
            this.existingPropDefs.add(propDef);
            propertyDefinitions.add(propDef);
        }

        box.setModel(editPropDefs);
        box.setEditable(false);
        box.setRenderer(propDefListCellRenderer);
        box.addActionListener(this);
        box.addItemListener(itemlistener);
    }


    @Override
    public Object getCellEditorValue() {
        if (box.getSelectedIndex() <= 1)
            return null;
        return box.getSelectedItem();
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        sourceProperty = table.getValueAt(row, MappingTableModel.SOURCE_PROPERTY_COLUMN).toString();
        
        box.setModel(editPropDefs);
        box.removeActionListener(mergeListener);
        box.addActionListener(this);

        if (value != null) {
            PropertyDefinition propertyDef = (PropertyDefinition) value;
            box.setSelectedItem(propertyDef);
        } else
            box.setSelectedIndex(-1);

        return box;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // replaced by item listener, since this method often fires, even when the combo box has not been changed at all
//        if (box.getSelectedIndex() == 0) {
//            PropertyDefinitonCreationDialog d = new PropertyDefinitonCreationDialog(owner, sourceProperty,
//                    probablyNumeric.contains(sourceProperty));
//            d.setVisible(true);
//            d.requestFocusInWindow();
//
//            if (d.getPropertyDefinition() != null) {
//                propertyDefinitions.add(d.getPropertyDefinition());
//                box.setSelectedItem(d.getPropertyDefinition());
//                fireEditingStopped();
//            } else
//                fireEditingCanceled();
//
//        } else
//            fireEditingStopped();
    }
    
    private ItemListener itemlistener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (box.getSelectedIndex() == 0) {
                PropertyDefinitonCreationDialog d = new PropertyDefinitonCreationDialog(owner, sourceProperty,
                        probablyNumeric.contains(sourceProperty));
                d.setVisible(true);
                d.requestFocusInWindow();

                if (d.getPropertyDefinition() != null) {
                    propertyDefinitions.add(d.getPropertyDefinition());
                    box.setSelectedItem(d.getPropertyDefinition());
                    fireEditingStopped();
                } else
                    fireEditingCanceled();
            }
            else
                fireEditingStopped();
        }
    };

    private class PropertyDefinitionComboBoxModel implements ComboBoxModel {

        private Object selected;

        @Override
        public Object getElementAt(int index) {
            switch (index) {
            case 0:
                return I18n.get("ImportMappings.CreateNewPropertyDefiniton");
            case 1:
                return I18n.get("ImportMappings.DontMap");
            default:
                return propertyDefinitions.getElementAt(index - 2);
            }
        }

        @Override
        public int getSize() {
            return propertyDefinitions.getSize() + 2;
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = anItem;
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            propertyDefinitions.addListDataListener(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            propertyDefinitions.removeListDataListener(l);
        }

    }

    private class MergeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            editPropDefs.setSelectedItem(box.getSelectedItem());
            fireEditingStopped();
        }
    }

}