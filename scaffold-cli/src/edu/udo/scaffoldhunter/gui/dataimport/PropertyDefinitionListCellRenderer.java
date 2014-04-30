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
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;

import edu.udo.scaffoldhunter.gui.util.CustomComboBoxRenderer;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Henning Garus
 *
 */
public class PropertyDefinitionListCellRenderer extends CustomComboBoxRenderer {

    /* (non-Javadoc)
     * @see javax.swing.DefaultListCellRenderer#getListCellRendererComponent(javax.swing.JList, java.lang.Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        if (value == null)
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        /*
         *  give non property definitions a different look
         *  
         *  this is used to differentiate options such as 
         *  "new property definition..." from selectable property definitions
         */
        if (!(value instanceof PropertyDefinition)) {
            c.setFont(c.getFont().deriveFont(Font.ITALIC));
            return c;
        }
        c.setFont(c.getFont().deriveFont(Font.PLAIN));
        PropertyDefinition propDef = (PropertyDefinition)value;
        c.setText(propDef.getTitle());
        String tt = propDef.getDescription();
        if (tt == null || tt.equals("")) {
            tt = propDef.getTitle();
        }
        c.setToolTipText(tt);
        
        return c;
    }
        
}