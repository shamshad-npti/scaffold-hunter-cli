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
import java.awt.Graphics;

import javax.swing.JEditorPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 * @author Michael Hesse
 *
 */
public class TextCellRenderer extends DefaultTableCellRenderer implements TableCellRenderer {

    JEditorPane textField;
    
    /**
     * 
     */
    public TextCellRenderer() {
        super();
        textField = new JEditorPane() {
            
            int height, asc, desc, preferredHeight;
            int xpos, ypos;
            int dotsWidth = -1;
            int dotsHeight = -1;
            Color textColor;
            
            @Override
            public void paintComponent(Graphics g) {
                if(dotsWidth == -1) {
                    dotsWidth = (int)getFont().getStringBounds("\u2026", getFontMetrics( getFont() ).getFontRenderContext() ).getWidth();
                    dotsHeight = (int)getFont().getStringBounds("\u2026", getFontMetrics( getFont() ).getFontRenderContext() ).getHeight();
                    desc = getFontMetrics(getFont()).getMaxDescent();
                    asc = getFontMetrics( getFont() ).getMaxAscent();
                }
                height = getSize().height;
                preferredHeight = getPreferredSize().height - desc;
                super.paintComponent(g);
                if( preferredHeight > height ) {
                    // paint points
                    xpos = getSize().width - dotsWidth;
                    ypos = height - dotsHeight;
                    textColor = g.getColor();
                    g.setColor( getBackground() );
                    g.translate(xpos, ypos);
                    g.fillRect(-2, 0, dotsWidth+2, dotsHeight);
                    g.setColor(textColor);
                    g.drawString("\u2026", 0, asc);
                }
            }
        };
        textField.setEditable(false);
        textField.setFont(getFont());
        textField.setOpaque(true);      // does this fix bug #156?
        this.setOpaque(true);          // does this fix bug #156?
    }

    
    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(
            JTable jTable, Object value,
            boolean isSelected, boolean hasFocus, 
            int rowIndex, int columnIndex) {
        super.getTableCellRendererComponent ( jTable, value, isSelected, hasFocus, rowIndex, columnIndex);

        textField.setBorder(getBorder());

        // In some cases getBackground() returns an instance of ColorUIResource
        // that's subsequently modified behind the scenes. On systems with GTK
        // look and feel, this may cause selected table rows to be rendered
        // incorrectly. Copying the color value to a new Color instance works
        // around this rather obscure bug (see #156).
        textField.setBackground(new Color(getBackground().getRGB()));

        if( ! isSelected )
            textField.setBackground( (Color) ((rowIndex & 0x01) == 0x01 ? jTable.getClientProperty("zebracolor") : getBackground() ) );
        textField.setForeground(getForeground());
        if(getText().equals("NaN"))
            textField.setText("...");
        else
            textField.setText(getText());
        
        int columnWidth = jTable.getColumnModel().getColumn(columnIndex).getWidth();
        textField.setSize(columnWidth, Integer.MAX_VALUE);
        return textField;
    }

}
