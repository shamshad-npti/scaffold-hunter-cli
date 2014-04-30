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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Michael Hesse
 *
 * note: this class is deprecated and will be removed in the near future
 * 
 */
public class DataCacheMatrix {
    
    
    private Map <Integer, Map<Integer, Object> > matrixRows;
    
    
    /**
     * 
     */
    public DataCacheMatrix() {
        matrixRows = new TreeMap <Integer, Map<Integer, Object> > ();
    }

    
    
    /**
     * @param column
     * @param row
     * @return
     *  the value 
     */
    public synchronized Object getValue(int column, int row) {
        Object value = null;
        Map <Integer, Object> matrixColumns = matrixRows.get(row);
        if( matrixColumns != null )
            value = matrixColumns.get(column);
        return value;
    }
    
    
    /**
     * @param column
     * @param row
     * @return
     *  true if the specified value is currently stored in the matrix
     */
    public synchronized boolean contains(int column, int row) {
        boolean isPresent = false;
        Map <Integer, Object> matrixColumns = matrixRows.get(row);
        if( matrixColumns != null)
            isPresent = matrixColumns.containsKey(column);
        return isPresent;
    }
    
    
    /**
     * stores a value in the matrix
     * @param column
     * @param row
     * @param value
     */
    public synchronized void setValue(int column, int row, Object value) {

        Map <Integer, Object> matrixColumns = matrixRows.get(row);
        if( matrixColumns == null) {
            matrixColumns = new TreeMap <Integer, Object> ();
            matrixRows.put(row, matrixColumns);
        }
        
        matrixColumns.put(column, value);
        
    }

    
    
    /**
     * removes matrix cells that are no longer needed and creates a list
     * of cells that should be loaded. the list will be sorted by 
     * loading priority
     * 
     * @param leftPreloadColumns
     * @param centerColumns
     * @param rightPreloadColumns
     * @param topPreloadRows
     * @param centerRows
     * @param bottomPreloadRows
     * @return 
     *  a list of cells that should be loaded, sorted by priority.
     *  the format of a list-entry:
     *     bit 62-32 : row
     *     bit    30 : = 1 if this is a center cell, = 0 otherwise.
     *                 this flag can be used to visualize that the currently
     *                 visible table cells may change their values
     *     bit  29-0 : column
     */
    public synchronized List <Long> synchronizeMatrix(
            List <Integer> leftPreloadColumns,
            List <Integer> centerColumns,
            List <Integer> rightPreloadColumns,
            List <Integer> topPreloadRows,
            List <Integer> centerRows,
            List <Integer> bottomPreloadRows) {

        List <Long> cellsToLoad;
        
        { // first remove all unused cells
            List <Integer> allWantedColumns = new ArrayList<Integer> ();
            allWantedColumns.addAll( leftPreloadColumns );
            allWantedColumns.addAll( centerColumns );
            allWantedColumns.addAll( rightPreloadColumns );

            List <Integer> allWantedRows = new ArrayList<Integer> ();
            allWantedRows.addAll( topPreloadRows );
            allWantedRows.addAll( centerRows );
            allWantedRows.addAll( bottomPreloadRows );

            // find rows that are not longer needed
            List <Integer> rowKeysToDelete = new ArrayList <Integer> ();
            for( int r : matrixRows.keySet()) {
                if( allWantedRows.contains(r) ) {
                    
                    // row should be kept; just remove unused columns
                    Map <Integer, Object> matrixColumns = matrixRows.get(r);
                    List <Integer> columnKeysToDelete = new ArrayList <Integer> ();
                    for( int c : matrixColumns.keySet() ) {
                        if( ! allWantedColumns.contains(c) ) {
                            // column is no longer needed, remove it
                            columnKeysToDelete.add(c);
                            //matrixColumns.remove(c);
                        }
                    }
                    // perform the deletion
                    for( int c : columnKeysToDelete)
                        matrixColumns.remove(c);
    
                } else {
                    // row is not longer needed, remove it
                    rowKeysToDelete.add(r);
                    //matrixRows.remove(r);
                }
            }
            // perform the deletion
            for( int r : rowKeysToDelete)
                matrixRows.remove(r);

        }
        
        // create a list of cells that should be loaded, ordered by the
        // loading priority
        cellsToLoad = new ArrayList <Long> ();
        cellsToLoad.addAll( processCellBlock(centerColumns, centerRows, true) );
        cellsToLoad.addAll( processCellBlock(centerColumns, bottomPreloadRows, false) );
        cellsToLoad.addAll( processCellBlock(centerColumns, topPreloadRows, false) );
        cellsToLoad.addAll( processCellBlock(rightPreloadColumns, centerRows, false) );
        cellsToLoad.addAll( processCellBlock(leftPreloadColumns, centerRows, false) );
        cellsToLoad.addAll( processCellBlock(rightPreloadColumns, bottomPreloadRows, false) );
        cellsToLoad.addAll( processCellBlock(rightPreloadColumns, topPreloadRows, false) );
        cellsToLoad.addAll( processCellBlock(leftPreloadColumns, bottomPreloadRows, false) );
        cellsToLoad.addAll( processCellBlock(leftPreloadColumns, topPreloadRows, false) );
        
        return cellsToLoad;
    }

    
    /**
     * @param columns
     * @param rows
     * @param isCenterCell
     * @return
     *  and array specifying which cells should be loaded
     */
    private List <Long> processCellBlock( List <Integer> columns, List <Integer> rows, boolean isCenterCell) {
        List <Long> cellsToLoad = new ArrayList <Long> ();
        long prio = (isCenterCell ? 0x40000000 : 0);
        
        for(long r : rows) {
            if( matrixRows.containsKey((int)r) ) {
                Map <Integer, Object> matrixColumns = matrixRows.get((int)r);
                // check if the column is present
                for( long c : columns ) {
                    if( ! matrixColumns.containsKey((int)c) ) {
                        // this cell is missing. add it to cellsToLoad
                        //System.out.println("DataCacheMatrix.processCellBlock: " + r+", "+prio+", "+c);
                        cellsToLoad.add( (r<<32) | prio | c) ;
                    }
                }
            } else {
                // the whole row is missing, so just add all cells to the cellsToLoad list
                for( long c: columns) {
                    //System.out.println("DataCacheMatrix.processCellBlock b: " + r+", "+prio+", "+c);
                    cellsToLoad.add( (r<<32) | prio | c);
                }
            }
        }
        return cellsToLoad;
    }

    

}
