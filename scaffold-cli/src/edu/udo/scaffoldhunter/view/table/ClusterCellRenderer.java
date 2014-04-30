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

package edu.udo.scaffoldhunter.view.table;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

import edu.udo.scaffoldhunter.util.DefaultColors;

/**
 * @author Michael Hesse
 *
 */
public class ClusterCellRenderer extends DefaultTableCellRenderer {

    /**
     * 
     */
    public ClusterCellRenderer() {
        super();
        this.setHorizontalAlignment(SwingConstants.CENTER);
    }

    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(
            JTable jTable, Object value,
            boolean isSelected, boolean hasFocus, 
            int rowIndex, int columnIndex) {
        super.getTableCellRendererComponent ( jTable, ((Integer)value)+1, isSelected, hasFocus, rowIndex, columnIndex);
        setBackground( DefaultColors.getColor((Integer)value));
        setForeground( Color.white );
        return this;
    }

}
