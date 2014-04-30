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
import java.awt.Graphics2D;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import edu.udo.scaffoldhunter.view.util.SVG;

/**
 * @author Michael Hesse
 *
 */
public class SVGCellRenderer extends DefaultTableCellRenderer {

    private SVG svg;    
        
    /* (non-Javadoc)
     * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
     */
    @Override
    public Component getTableCellRendererComponent(
            JTable jTable, Object value,
            boolean isSelected, boolean hasFocus, 
            int rowIndex, int columnIndex) {
        super.getTableCellRendererComponent(jTable, null, isSelected, hasFocus, rowIndex, columnIndex);
        setText("");
        if( ! isSelected )
            setBackground( (Color) ((rowIndex & 0x01) == 0x01 ? jTable.getClientProperty("zebracolor") : jTable.getBackground() ) );
        //svg = (Model.SVGWrapper) value;
        svg = (SVG) value;
        //calcScaleFactor ( jTable.getColumnModel().getColumn(columnIndex).getWidth(), jTable.getRowHeight(rowIndex) );
        return this;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);     // to paint the background color, selection and so on
        if(svg != null) {
            svg.paint( (Graphics2D)g, (double)getWidth(), (double)getHeight() );
            svg = null;         // unlink svg, to let the SVGCache do it's work
        }
    }

}
