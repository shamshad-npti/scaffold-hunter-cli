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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.SortOrder;
import javax.swing.table.TableRowSorter;

import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.model.db.DatabaseException;

/**
 * @author Michael Hesse
 * 
 */
public class TableViewRowSorter extends TableRowSorter<Model> {

    /**
     * 
     */
    public enum SortieState {
        /**
         * 
         */
        CONST, /**
         * 
         */
        UNAVAILABLE, /**
         * 
         */
        LOADING, /**
         * 
         */
        AVAILABLE
    };

    Model myModel;

    class Sortie {
        int column;
        SortieState state;
        Map<Integer, Integer> mapping;
    }
    Sortie[] sorties;

    @SuppressWarnings("rawtypes")
    private static class MyComparator implements Comparator {
        @SuppressWarnings("unchecked")
        @Override
        public int compare(Object  o1, Object  o2) {
            return ((Comparable )o1).compareTo(o2);
        }
    }
    @SuppressWarnings("rawtypes")
    private static final Comparator MY_COMPARATOR = new MyComparator();
    
    
    @Override
    public void setModel(Model model) {

        super.setModel(model);
        if(model == null)
            return;
        myModel = model;

        sorties = new Sortie[model.getColumnCount()];
        for (int i = 0; i < model.getColumnCount(); i++) {
            sorties[i] = new Sortie();
            sorties[i].column = i;
            if (model.getColumnKey(i).startsWith("key_pd_")) {
                sorties[i].state = SortieState.UNAVAILABLE;
            } else {
                sorties[i].state = SortieState.CONST;
            }
            sorties[i].mapping = null;
        }

        final ModelWrapper<Model, Integer> wrapper = getModelWrapper();
        setModelWrapper(new ModelWrapper<Model, Integer>() {

            @Override
            public Model getModel() {
                return wrapper.getModel();
            }

            @Override
            public int getColumnCount() {
                return wrapper.getColumnCount();
            }

            @Override
            public int getRowCount() {
                return wrapper.getRowCount();
            }

            @Override
            public Object getValueAt(int row, int column) {
                Object value = null;
                switch( sorties[column].state) {
                case CONST:
                    // return the value from the model
                    value = wrapper.getValueAt(row, column);
                    break;
                case UNAVAILABLE:
                    // start loading; in the meantime return row to leave the table unchanged
                    load();
                    value = row;
                    break;
                case LOADING:
                    // return row, to leave the table unchanged
                    value = row;
                    break;
                case AVAILABLE:
                    // return the mapped row number
                    if(sorties[column].mapping.isEmpty())
                        value = row;
                    else 
                        value = sorties[column].mapping.get( myModel.getMoleculeAtRow(row).getId() );
                    break;
                }
                return value;
            }

            @Override
            public String getStringValueAt(int row, int column) {
                return wrapper.getStringValueAt(row, column);
                //return getValueAt(row, column).toString();
            }

            @Override
            public Integer getIdentifier(int index) {
                return wrapper.getIdentifier(index);
            }
            
            
            private void load() {
                List <? extends SortKey> sortkeys = getSortKeys();
                List <Integer> keysToKeep = new ArrayList <Integer> ();
                Map <Integer, SortOrder> keysToLoad = new HashMap <Integer, SortOrder> ();

                // determine which keys we should keep and which we should load
                for(SortKey key : sortkeys) {
                    int column = key.getColumn();
                    keysToKeep.add( column );
                    if(sorties[column].state == SortieState.UNAVAILABLE)
                        keysToLoad.put(column, key.getSortOrder());
                }
                
                // clear mappings that are not longer needed
                for(int i=0; i<sorties.length; i++) {
                    if( ! (sorties[i].state == SortieState.CONST) ){
                        if( ! keysToKeep.contains(i) ) {
                            sorties[i].state = SortieState.UNAVAILABLE;
                            sorties[i].mapping = null;
                        }
                    }
                }
                // for each mapping that should be loaded start an own thread
                for(int column : keysToLoad.keySet()) {
                    sorties[column].state = SortieState.LOADING;
                    
                    TablesortWorker worker = new TablesortWorker();
                    worker.setParameters(column, keysToLoad.get(column));
                    worker.execute();
                }
            }
        });

    }
    
    
    @Override
    public Comparator<?> getComparator(int column) {
        if( sorties[column].state != SortieState.CONST)
            return MY_COMPARATOR;
        else return super.getComparator(column);
    }


    
    
    class TablesortWorker extends ProgressWorker<Integer, Void> {
        
        int col;
        SortOrder order;
        
        public void setParameters(int column, SortOrder order) {
            col = column;
            this.order = order;
            setProgressBounds(0, 1);
            setProgressIndeterminate(true);
        }

        @Override
        protected Integer doInBackground() {
            if(myModel == null)
                return 0;
            
            try {
                Map <Integer, Integer> mapping = 
                    myModel.getDbManager().getSortOrder(
                            myModel.getSubset(), 
                            myModel.getPropertyDefinitionAtColumn(col), 
                            ! (order == SortOrder.DESCENDING) );
                sorties[col].mapping = mapping;
                sorties[col].state = SortieState.AVAILABLE;

                //myModel.fireTableDataChanged();
                //myModel.reapplySelectionFromExtern();
                //if(myModel.getRowCount() != 0)
                //    myModel.fireTableRowsUpdated(0, myModel.getRowCount()-1);
                if(myModel == null)
                    return 0;
                myModel.vc.selectionProcess = ViewComponent.SelectionProcess.STOP;
                sort();
                //myModel.fireTableDataChanged();
                //myModel.repaintTable();
            } catch (DatabaseException e) {
                //FIXME: use dbExceptionHandler?, use logger
                e.printStackTrace();
            }
            return 0;
        }
    
    }

    
    /**
     * 
     */
    public void destroy() {
        myModel = null;
    }
}
