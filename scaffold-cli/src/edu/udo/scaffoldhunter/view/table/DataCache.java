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

package edu.udo.scaffoldhunter.view.table;

import java.util.List;

import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Michael Hesse
 * 
 * note: this class is deprecated and will be removed in the near future
 *
 */
public class DataCache {

    Model model;
    DataCacheMatrix matrix;
    
    List <Long> cellsToLoad;
    boolean threadIsRunning;

    
    /**
     * @param model
     */
    public DataCache(Model model) {
        this.model = model;
        matrix = new DataCacheMatrix();
        threadIsRunning = false;
    }
    
    /**
     * @param leftPreloadColumns
     * @param centerColumns
     * @param rightPreloadColumns
     * @param topPreloadRows
     * @param centerRows
     * @param bottomPreloadRows
     */
    public synchronized void loadPropertyValues(
            List <Integer> leftPreloadColumns,
            List <Integer> centerColumns,
            List <Integer> rightPreloadColumns,
            List <Integer> topPreloadRows,
            List <Integer> centerRows,
            List <Integer> bottomPreloadRows) {
        
        cellsToLoad = matrix.synchronizeMatrix(
                leftPreloadColumns, centerColumns, rightPreloadColumns, 
                topPreloadRows, centerRows, bottomPreloadRows);
        
        if( ! threadIsRunning ) {
            threadIsRunning = true;
            // start a new thread to load the stuff
            new Thread() {
                List <Long> ctl;
                
                @Override
                public void run() {
                    int previousRow = -1;
                    
                    do {
                        ctl = cellsToLoad;
                        boolean centerCellsProcessed = false;
                        
                        // process list of cells to load
                        for( long cell : ctl ) {
    
                            // stop if there was a change in the 
                            // cellsToLoad list in the meantime
                            if( ctl != cellsToLoad )
                                break;
    
                            // determine which value to load next
                            int column = (int) (cell & 0x3fffffff);
                            int row    = (int) ((cell >>> 32) & 0x3fffffff);
                            boolean centerFlag = ((cell & 0x40000000) != 0);
                            if( (! centerFlag) & (! centerCellsProcessed) ) {
                                // trigger a complete repaint of the table,
                                // to compensate for all the lost updates
                                model.repaintTable();
                                centerCellsProcessed = true;
                            }
                            // update complete row when all cells are loaded
                            if( row != previousRow) {
                                if( (previousRow > 0) & (previousRow < model.getRowCount()) & centerFlag )
                                    model.fireTableRowsUpdated(previousRow, previousRow);
                                previousRow = row;
                            }
                            Molecule molecule = model.getMoleculeAtRow(row);
                            PropertyDefinition propertyDefinition = model.getPropertyDefinitionAtColumn(column);
                            Object value = null;
                            
                            if(propertyDefinition != null) {
                                // load the value and store it in the matrix
                                try {
                                    model.getDbManager().lockAndLoad(propertyDefinition, molecule);
                                    if(propertyDefinition.isStringProperty())
                                        value = molecule.getStringPropertyValue(propertyDefinition);
                                    else
                                        value = molecule.getNumPropertyValue(propertyDefinition);
                                    model.getDbManager().unlockAndUnload(propertyDefinition, molecule);
                                    matrix.setValue(column, row, value);
                                    //model.fireTableCellUpdated(row, column);
                                } catch (DatabaseException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                            }
                            
                        } // end of processing the current list

                        
                    } while (ctl != cellsToLoad ); // start over of the list has changed
                    
                    threadIsRunning = false;
                }


            }.start();
        }
    }

    
    /**
     * @param column
     * @param row
     * @return
     *  the value
     */
    public Object getValue(int column, int row) {
        return matrix.getValue(column, row);
    }
    
    
    /**
     * @param column
     * @param row
     * @return
     *  true if the specified value is currently stored in the matrix
     */
    public boolean contains(int column, int row) {
        return matrix.contains(column, row);
    }

}
