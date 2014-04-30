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

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.udo.scaffoldhunter.model.dataimport.ImportJob;
import edu.udo.scaffoldhunter.model.dataimport.ImportProcess;
import edu.udo.scaffoldhunter.model.dataimport.MergeStrategy;

/**
 * Renders a merge strategy in a JTable. Shows a red border, if a merge strategy
 * should be set according to
 * {@link ImportProcess#isUndefinedMergeStrategy(ImportJob, int)}
 * 
 * @author Henning Garus
 * 
 */
public class MergeStrategyRenderer extends DefaultTableCellRenderer {

    private ImportProcess sources;
    private ImportJob source;
    private JComboBox mergeByComboBox;

    /**
     * Create a new Merge strategy renderer.
     * 
     * @param sources
     *            the import sources object this renderer will query on wether a
     *            merge strategy for this entry is needed.
     * @param source
     *            the import source to which the merge strategies shown by this
     *            renderer belong.
     * @param mergeByComboBox
     *            the {@link JComboBox} which indicates which property is
     *            selected for merging
     */
    public MergeStrategyRenderer(ImportProcess sources, ImportJob source, JComboBox mergeByComboBox) {
        super();
        this.sources = sources;
        this.source = source;
        this.mergeByComboBox = mergeByComboBox;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent
     * (javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        // don't show anything for NONE
        if ((MergeStrategy) value == MergeStrategy.NONE)
            value = null;
        JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (sources.isUndefinedMergeStrategy(source, row)) {
            label.setBorder(BorderFactory.createLineBorder(Color.RED));
        } else
            label.setBorder(null);

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
