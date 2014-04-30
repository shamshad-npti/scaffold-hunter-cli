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

package edu.udo.scaffoldhunter.gui.dataimport;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;

import edu.udo.scaffoldhunter.gui.util.CustomComboBoxRenderer;

/**
 * A list cell renderer which renders <code>null</code> as a predefined string.
 * <p>
 * Additionally this renderer sets its tooltiptext to the value text, which is
 * useful when dealing with longer value strings.
 * 
 * @author Henning Garus
 * 
 */
public class CellRendererWithNullValue extends CustomComboBoxRenderer {

    private final String nullString;

    /**
     * Create a new Cell Renderer which will show a specified String for null
     * values.
     * 
     * @param nullString
     *            the string which will be shown when a rendered value is null
     */
    public CellRendererWithNullValue(String nullString) {
        super();
        this.nullString = nullString;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax
     * .swing.JList, java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        if (value == null) {
            value = nullString;
        }
        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
//        if (value == null) {
//            this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
//        } else {
//            this.setBorder(null);
//        }

        c.setToolTipText(value.toString());

        return c;

    }

}
