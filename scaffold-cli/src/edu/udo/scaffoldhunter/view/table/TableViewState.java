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

import java.util.Map;

import edu.udo.scaffoldhunter.model.ViewState;

/**
 *
 */
public class TableViewState extends ViewState {

    boolean valid = false;
    
    double rowLines;
    int scrollPosX, scrollPosY;
    Map <String, Integer> columnWidth = null;
    String[] stickyColumnOrder = null;
    String[] floatingColumnOrder = null;
    
    // moleculeOrder[] is not saved. it is only needed in cooperation with the
    // dendogram, which does not provide a ViewStateObject for the table.
    
    
    /**
     * @return
     *  true if this is a valid state
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * @param valid
     */
    public void setValid(boolean valid) {
        this.valid = valid;
    }
    
    /**
     * @return
     *  the row lines
     */
    public double getRowLines() {
        return rowLines;
    }
    
    /**
     * @param rowLines
     */
    public void setRowLines(double rowLines) {
        this.rowLines = rowLines;
    }

    /**
     * @return
     *  the x-scrollposition
     */
    public int getScrollPosX() {
        return scrollPosX;
    }
    
    /**
     * @param scrollPosX
     */
    public void setScrollPosX(int scrollPosX) {
        this.scrollPosX = scrollPosX;
    }
    
    /**
     * @return
     *  the y-scrollposition
     */
    public int getScrollPosY() {
        return scrollPosY;
    }
    
    /**
     * @param scrollPosY
     */
    public void setScrollPosY(int scrollPosY) {
        this.scrollPosY = scrollPosY;
    }


    
    /**
     * 
     * @param stickyColumnOrder
     */
    public void setStickyColumnOrder(String[] stickyColumnOrder ) {
        this.stickyColumnOrder = stickyColumnOrder;
    }
    
    /**
     * @return
     *  stickyColumnOrder
     */
    public String[] getStickyColumnOrder() {
        return stickyColumnOrder;
    }

    /**
     * 
     * @param floatingColumnOrder
     */
    public void setFloatingColumnOrder(String[] floatingColumnOrder ) {
        this.floatingColumnOrder = floatingColumnOrder;
    }

    /**
     * @return
     *  floatingColumnOrder
     */
    public String[] getFloatingColumnOrder() {
        return floatingColumnOrder;
    }

    
    /**
     * 
     * @param columnWidth
     */
    public void setColumnWidth(Map <String, Integer> columnWidth) {
        this.columnWidth= columnWidth;
    }

    /**
     * @return
     *  columnWidth
     */
    public Map <String, Integer> getColumnWidth() {
        return columnWidth;
    }

}
