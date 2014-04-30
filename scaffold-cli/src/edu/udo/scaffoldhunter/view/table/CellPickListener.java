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

import java.awt.Component;

/**
 * @author Michael Hesse
 *
 */
public interface CellPickListener {

    /**
     * will be called whenever the mousecursor enters/leaves
     * a table cell in a TableViewComponent-table
     * 
     * @param viewComponent
     *  the table that generated the event
     * @param row
     *  the row of the entered tablecell, or -1 when the
     *  mousecursor has left the table
     * @param column
     *  the column of the entered tablecell, or -1 when the
     *  mousecursor has left the table
     * @param structureTitle 
     *  the title of the structure from which a cell was picked,
     *  or null when the mousecursor has left the table
     * @param columnTitle 
     *  the title of the column in which a cell was picked,
     *  or null when the mousecursor has left the table
     * @param cellContent
     *  the content of the entered tablecell, or null when the 
     *  mousecursor has left the table
     */
    public void CellPickChanged(
            ViewComponent viewComponent,
            int row,
            int column,
            String structureTitle,
            String columnTitle,
            Component cellContent
            );
    
}
