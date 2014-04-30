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

package edu.udo.scaffoldhunter.view.util;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;

import edu.udo.scaffoldhunter.gui.util.CustomComboBoxRenderer;

/**
 * a combobox item renderer with tooltips
 * 
 * @author Micha
 * 
 */
public class ToolboxComboBoxRenderer extends CustomComboBoxRenderer {

    @SuppressWarnings("rawtypes")
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (isSelected && -1 < index) {
            list.setToolTipText((String) value);
        }
        c.setFont(list.getFont());
        c.setText((value == null) ? "" : value.toString());
        return c;
    }
}
