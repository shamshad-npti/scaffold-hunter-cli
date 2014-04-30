/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

package edu.udo.scaffoldhunter.gui.dataimport;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * CellRenderer that allows to visually disable a cell
 * 
 * @author Till Schäfer
 * 
 */
public class ActivatableTableCellRenderer extends DefaultTableCellRenderer {
    private JComboBox mergeByComboBox;

    /**
     * Constructor
     * 
     * @param mergeByComboBox
     *            the {@link JComboBox} which indicates which property is
     *            selected for merging
     */
    public ActivatableTableCellRenderer(JComboBox mergeByComboBox) {
        this.mergeByComboBox = mergeByComboBox;
        // TODO Auto-generated constructor stub
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {

        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

        if (mergeByComboBox != null
                && table.getModel().getValueAt(row, MappingTableModel.SOURCE_PROPERTY_COLUMN)
                        .equals(mergeByComboBox.getSelectedItem())) {
            label.setBackground(Color.GRAY);
            if (column != MappingTableModel.SOURCE_PROPERTY_COLUMN) {
                label.setText("");
            }
        } else {
            if (!isSelected) {
                label.setBackground(Color.WHITE);
            }
            if (column != MappingTableModel.SOURCE_PROPERTY_COLUMN) {
                label.setText(value == null ? "" : value.toString());
            }
        }

        return label;

    }
}
