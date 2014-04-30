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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import edu.udo.scaffoldhunter.model.dataimport.MergeStrategy;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * A table cell editor which allows choosing a merge strategy in a combobox.
 * 
 * @author Henning Garus
 *
 */
public class MergeStrategyEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private final JComboBox box = new JComboBox();
    private final DefaultCellEditor defaultEditor = new DefaultCellEditor(box);
   
    /**
     * Create a new merge strategy editor.
     */
    public MergeStrategyEditor() {
        box.addActionListener(this);
    }

    /* (non-Javadoc)
     * @see javax.swing.CellEditor#getCellEditorValue()
     */
    @Override
    public Object getCellEditorValue() {
        return box.getSelectedItem();
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellEditor#getTableCellEditorComponent(javax.swing.JTable, java.lang.Object, boolean, int, int)
     */
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        if (table.getValueAt(row, 1) != null && ((PropertyDefinition)table.getValueAt(row, 1)).isStringProperty())
            box.setModel(new DefaultComboBoxModel(MergeStrategy.getStringStrategies().toArray()));
        else
            box.setModel(new DefaultComboBoxModel(MergeStrategy.getNumStrategies().toArray()));
        return defaultEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        fireEditingStopped();
    }
    
 
}
