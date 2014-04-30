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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultRowSorter;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterListener;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumn;

import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.SideBarItem;
import edu.udo.scaffoldhunter.view.table.Model.SortState;

/**
 * @author Michael Hesse
 *
 */
public class ViewComponent 
extends JScrollPane 
implements ListSelectionListener, ChangeListener, MouseListener, MouseMotionListener, PropertyChangeListener, RowSorterListener, AdjustmentListener, TableColumnModelListener {
    
    // the model that provides access to the molecule data
    private Model tableModel;

    // the visible 'table' is composed of these two separate tables
    private ViewTable stickyTable;
    private ViewTable floatingTable;

    // the listeners for the cell picker
    List <CellPickListener> cellPickListeners = new ArrayList <CellPickListener> ();

    // avoid firing multiple cell pick events for the same cell
    // by storing the last picked cell
    private int lastPickedRow = -1;
    private int lastPickedColumn = -1;
    
    // needed to determine when the table(s) become scrolled/resized
    private Point lastViewportPosition = new Point(-1, -1);
    private Rectangle lastViewportSize = new Rectangle( -1, -1);
    
    
    // the selection
    private Selection selection;
    enum SelectionProcess { READY, APPLY_FROM_EXTERN, PROPAGATE_TO_EXTERN, STOP };
    SelectionProcess selectionProcess = SelectionProcess.STOP;
    
    // this comes from the dendogram
    List <Molecule> moleculeOrder = null;
    
    JToolBar toolbar;
    private JMenu viewMenu;
    
    ViewState viewState;
    private boolean viewStateInitialized = false;
    
    TableViewRowSorter rowSorter = null;
    
    Vector<SideBarItem> sidebarComponents;
    
    /**
     * initializes the table with a tablemodel, 
     * sets cellrenderers and sets up the the 
     * component
     * 
     * @param db
     *  the DB manager
     * @param subset 
     *  the subset (of molecules) that should be displayed in the table
     * @param bannerPool 
     *  the banner pool
     * @param viewState 
     */
    public ViewComponent(DbManager db, Subset subset, BannerPool bannerPool, ViewState viewState) {
        this(db, subset, null, bannerPool, viewState);
    }

    
    /**
     * initializes the table with a tablemodel, 
     * sets cellrenderers and sets up the the 
     * component.
     * furthermore the molecules of the subset are stored in
     * the specified order. this constructor is neccessary
     * for cooperation with the dendogram view.
     * 
     * @param db
     *  the DB manager
     * @param subset 
     *  the subset (of molecules) that should be displayed in the table
     * @param moleculeOrder 
     *  the molecules of the subset in a specified order
     * @param bannerPool
     *  the banner pool
     * @param viewState 
     */
    public ViewComponent(DbManager db, Subset subset, List <Molecule> moleculeOrder, BannerPool bannerPool, ViewState viewState ) {
        
        this.moleculeOrder = moleculeOrder;
        this.viewState = viewState;
        
        // create a new tableModel for the given subset (with or without column  for clusters)
        tableModel = new Model( db, (moleculeOrder == null ? false : true), bannerPool );
        tableModel.setViewComponent(this);
        tableModel.setSubset(subset, moleculeOrder);
        
        // create floating table
        floatingTable = new ViewTable(db, tableModel, false, this);
        setViewportView(floatingTable);
        floatingTable.setAutoCreateRowSorter(true);
        ((DefaultRowSorter<?, ?>) floatingTable.getRowSorter()).setMaxSortKeys(3);
        floatingTable.getRowSorter().addRowSorterListener(this);
        
        // create sticky table and place it in the component
        stickyTable = new ViewTable(db, tableModel, true, this);
        JPanel stickyPanel = new JPanel( new FlowLayout(FlowLayout.CENTER, 0, 0));
        stickyPanel.add(stickyTable);
        setCorner( ScrollPaneConstants.UPPER_LEFT_CORNER, stickyTable.getTableHeader());
        setRowHeaderView(stickyPanel);
        
        // connect the two tables (selection and sorting)
        stickyTable.setSelectionModel( floatingTable.getSelectionModel() );
        stickyTable.setRowSorter( floatingTable.getRowSorter() );
    
        // listens to changes in the selection
        floatingTable.getSelectionModel().addListSelectionListener(this);

        // listens to mouse events from the table to be able
        // to fire TableCellPick events
        stickyTable.addMouseListener(this);
        floatingTable.addMouseListener(this);
        stickyTable.addMouseMotionListener(this);
        floatingTable.addMouseMotionListener(this);

        // listen to scrollevents for this scrollpane
        getHorizontalScrollBar().addAdjustmentListener(this);
        getVerticalScrollBar().addAdjustmentListener(this);
        
        // listen to column-move events
        stickyTable.getColumnModel().addColumnModelListener(this);
        floatingTable.getColumnModel().addColumnModelListener(this);
        
        // apply current selections, if any
        selectionProcess = SelectionProcess.READY;
        applySelectionFromExtern();
     
        setSubset(subset);
        
        { // set up toolbar and menu
            MakeColumnFloatingAction makeColumnFloatingAction = new MakeColumnFloatingAction();
            MakeColumnStickyAction makeColumnStickyAction = new MakeColumnStickyAction();
            EnlargeRowHeightAction enlargeRowHeightAction = new EnlargeRowHeightAction();
            NormalizeRowHeightAction normalizeRowHeightAction = new NormalizeRowHeightAction();
            ShrinkRowHeightAction shrinkRowHeightAction = new ShrinkRowHeightAction();
            ZoomToSelectionAction zoomToSelectionAction = new ZoomToSelectionAction();
            
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.add(makeColumnFloatingAction);
            toolbar.add(makeColumnStickyAction);
            toolbar.addSeparator();
            toolbar.add(enlargeRowHeightAction);
            toolbar.add(normalizeRowHeightAction);
            toolbar.add(shrinkRowHeightAction);
            toolbar.add( zoomToSelectionAction );
            
            viewMenu = new JMenu(I18n.get("TableView.Title"));
            viewMenu.add(makeColumnFloatingAction);
            viewMenu.add(makeColumnStickyAction);
            viewMenu.addSeparator();
            viewMenu.add(enlargeRowHeightAction);
            viewMenu.add(normalizeRowHeightAction);
            viewMenu.add(shrinkRowHeightAction);
            viewMenu.add(zoomToSelectionAction);
        }
        
        { // set up sidebar
            sidebarComponents = new Vector<SideBarItem>();
            
            // set up minimap
            MiniMap miniMap = new MiniMap(this);
            miniMap.setPreferredSize(new Dimension(160, 160));
            sidebarComponents.add(new SideBarItem(I18n.get("TableView.MiniMap.Title"), Resources.getIcon("minimap.png"), miniMap));
        
            // set up cell zoom
            CellZoom cellZoom = new CellZoom();
            cellZoom.setPreferredSize(new Dimension(160, 250));
            this.addCellPickListener(cellZoom);
            sidebarComponents.add(new SideBarItem(I18n.get("TableView.Detailview"), Resources.getIcon("zoom.png"), cellZoom));
            
        }
        // restore viewstate
        if(viewState != null) {
            if( viewState instanceof TableViewState ) {
                TableViewState tvs = (TableViewState) viewState;
                if(tvs.isValid()) {
                    // restore state
                    for(int i=1; i<tvs.getRowLines(); i++)
                        rowHeightEnlarge();
                } else {
                    // initialize viewstate
                    tvs.setScrollPosX(0);
                    tvs.setScrollPosY(0);
                    tvs.setRowLines(1.0);
                    tvs.setValid(true);
                }
            }
        }

    }

    /**
     * performs cleanup when the view is being destroyed
     */
    public void destroy() {
        destroySideBar();
        removeAll();
        selection.removePropertyChangeListener(Selection.SELECTION_PROPERTY, this);
        if(rowSorter != null) {
            rowSorter.removeRowSorterListener(this);
            rowSorter.destroy();
        }
        rowSorter = null;
        stickyTable.destroy();
        floatingTable.destroy();
        cellPickListeners.clear();
        cellPickListeners = null;
        selection = null;
        if(moleculeOrder != null)
            moleculeOrder.clear();
        moleculeOrder = null;
    }
    
    
    /**
     * makes the first floating column in the table sticky
     * 
     * for problems with this method see comment to the method
     * public void setClusters(List <List <Molecule>> clusterList)
     */
    public void makeColumnSticky() {
        if(floatingTable.getColumnCount() > 1) {
            TableColumn column = floatingTable.getColumn( floatingTable.getColumnName(0) );
            if(column.getWidth() < getViewport().getWidth()) {
                floatingTable.removeColumn(column);
                stickyTable.addColumn(column);
                saveTableState();
            }
        }
    }

    /**
     * makes the last sticky column in the table floating
     * 
     * for problems with this method see comment to the method
     * public void setClusters(List <List <Molecule>> clusterList)
     */
    public void makeColumnFloating() {
        if(stickyTable.getColumnCount() > 0) {
            TableColumn column = stickyTable.getColumn(stickyTable.getColumnName(stickyTable.getColumnCount()-1));
            stickyTable.removeColumn(column);
            floatingTable.addColumn(column);
            // move newly added column to the leftmost position of the floating table
            floatingTable.moveColumn(floatingTable.getColumnCount()-1, 0);
            saveTableState();
        }
    }
    

    /**
     * 
     */
    public void rowHeightNormalize() {
        Point floatingPoint = getViewport().getViewPosition();
        Point stickyPoint = getRowHeader().getViewPosition();
        int floatingRow = floatingTable.rowAtPoint( floatingPoint );
        int floatingColumn = floatingTable.columnAtPoint( floatingPoint );
        int stickyRow = stickyTable.rowAtPoint( stickyPoint );
        int stickyColumn = stickyTable.columnAtPoint( stickyPoint );
        floatingTable.rowHeightNormalize();
        stickyTable.rowHeightNormalize();
        getViewport().setViewPosition( calculateNewViewPosition(floatingTable, floatingRow, floatingColumn) );
        getRowHeader().setViewPosition( calculateNewViewPosition(stickyTable, stickyRow, stickyColumn) );
        // save view state
        if(viewState != null) {
            if( viewState instanceof TableViewState ) {
                if(viewStateInitialized) {
                    TableViewState tvs = (TableViewState) viewState;
                    tvs.setRowLines( floatingTable.getCurrentRowLines() );
                }
            }
        }
    }
    
    /**
     * 
     */
    public void rowHeightEnlarge() {
        Point floatingPoint = getViewport().getViewPosition();
        Point stickyPoint = getRowHeader().getViewPosition();
        int floatingRow = floatingTable.rowAtPoint( floatingPoint );
        int floatingColumn = floatingTable.columnAtPoint( floatingPoint );
        int stickyRow = stickyTable.rowAtPoint( stickyPoint );
        int stickyColumn = stickyTable.columnAtPoint( stickyPoint );
        floatingTable.rowHeightEnlarge();
        stickyTable.rowHeightEnlarge();
        getViewport().setViewPosition( calculateNewViewPosition(floatingTable, floatingRow, floatingColumn) );
        getRowHeader().setViewPosition( calculateNewViewPosition(stickyTable, stickyRow, stickyColumn) );
        // save view state
        if(viewState != null) {
            if( viewState instanceof TableViewState ) {
                if(viewStateInitialized) {
                    TableViewState tvs = (TableViewState) viewState;
                    tvs.setRowLines( floatingTable.getCurrentRowLines() );
                }
            }
        }
    }
    
    /**
     * 
     */
    public void rowHeightShrink() {
        Point floatingPoint = getViewport().getViewPosition();
        Point stickyPoint = getRowHeader().getViewPosition();
        int floatingRow = floatingTable.rowAtPoint( floatingPoint );
        int floatingColumn = floatingTable.columnAtPoint( floatingPoint );
        int stickyRow = stickyTable.rowAtPoint( stickyPoint );
        int stickyColumn = stickyTable.columnAtPoint( stickyPoint );
        floatingTable.rowHeightShrink();
        stickyTable.rowHeightShrink();
        getViewport().setViewPosition( calculateNewViewPosition(floatingTable, floatingRow, floatingColumn) );
        getRowHeader().setViewPosition( calculateNewViewPosition(stickyTable, stickyRow, stickyColumn) );
        // save view state
        if(viewState != null) {
            if( viewState instanceof TableViewState ) {
                if(viewStateInitialized) {
                    TableViewState tvs = (TableViewState) viewState;
                    tvs.setRowLines( floatingTable.getCurrentRowLines() );
                }
            }
        }
    }
    
    private Point calculateNewViewPosition(ViewTable table, int row, int column) {
        int y = row * table.getRowHeight();
        int x = 0;
        for(int i=0; i<table.getColumnCount(); i++) {
            if(i >= column)
                break;
            x += table.getColumnModel().getColumn(i).getPreferredWidth();
        }
        return new Point( x, y );
    }

    /* (non-Javadoc)
     * @see java.awt.event.AdjustmentListener#adjustmentValueChanged(java.awt.event.AdjustmentEvent)
     */
    @Override
    public void adjustmentValueChanged(AdjustmentEvent arg0) {
        // called when the table(s) are scrolled
        // save view state
        if(viewState != null) {
            if( viewState instanceof TableViewState ) {
                if(viewStateInitialized) {
                    TableViewState tvs = (TableViewState) viewState;
                    tvs.setScrollPosX (getViewport().getViewPosition().x);
                    tvs.setScrollPosY (getViewport().getViewPosition().y);
                }
            }
        }
    }
    
    /**
     * adds a listener to the list of listeners for cell pick events.
     * if the listener is already in the list then nothing happends.
     * 
     * @param listener
     *  the object that should be informed of table cell pick events
     */
    public void addCellPickListener(CellPickListener listener) {
        if( listener == null )
            return;
        if( ! cellPickListeners.contains(listener) ) {
            cellPickListeners.add(listener);
        }
    }
    
    /**
     * removes a listener to the list of listeners for cell pick events.
     * if the listener was not in the list then nothing happends.
     * 
     * @param listener
     *  the object that should no longer be informed of table cell pick events
     */
    public void removeCellPickListener(CellPickListener listener) {
        if( listener == null )
            return;
        if( cellPickListeners.contains(listener) ) {
            cellPickListeners.remove(listener);
        }
    }

    /**
     * fires a table cell pick event for this TableViewComponent.
     * 
     * @param row
     *  the row of the entered tablecell, or -1 when the
     *  mousecursor has left the table
     * @param column
     *  the column of the entered tablecell, or -1 when the
     *  mousecursor has left the table
     * @param structureTitle 
     *  the title of the picked structure
     * @param columnTitle 
     *  the title of the picked column
     * @param cellContent
     *  the content of the entered tablecell, or null when the 
     *  mousecursor has left the table
     */
    public void fireCellPickEvent(int row, int column, String structureTitle, String columnTitle, Component cellContent) {
        for( CellPickListener listener: cellPickListeners) {
            if(listener != null)
                listener.CellPickChanged(this, row, column, structureTitle, columnTitle, cellContent);
        }
    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent event) {
        
        // get source cell of the event
        JTable table = (JTable)event.getSource();
        int row = table.rowAtPoint( event.getPoint() );
        int realColumn = table.columnAtPoint( event.getPoint() );
        // make the two tables look like one big table
        int column = realColumn + (table == stickyTable ? 0 : stickyTable.getColumnCount());
        
        // fire only when it is not the same cell as last time
        if( ( row != lastPickedRow) | ( column != lastPickedColumn) ) {

            String structureTitle;
            String columnTitle;
            Component cellContent;

            if( (row == -1) | (column == -1) ) {
                structureTitle = null;
                columnTitle = null;
                cellContent = null;
                row = -1;
                column = -1;
            } else {
                structureTitle = 
                    tableModel.getMoleculeAtRow(table.convertRowIndexToModel(row)).getTitle();
                columnTitle = 
                    table.getColumnName(realColumn);
                // get the rendered component from the tablecell
                cellContent =
                    table.getCellRenderer(row, realColumn).getTableCellRendererComponent(table, table.getValueAt(row, realColumn), false, false, row, realColumn);
            }

            lastPickedRow = row;
            lastPickedColumn = column;
            fireCellPickEvent( row, column, structureTitle, columnTitle, cellContent );
        }
    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     * 
     * will be called if the mouse exits the tablearea and informs
     * the TableCellPickListeners
     */
    @Override
    public void mouseExited(MouseEvent arg0) {
        lastPickedRow = -1;
        lastPickedColumn = -1;
        fireCellPickEvent(-1, -1, null, null, null );
    }
    
    
    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     * 
     * not needed, but has to be here to implement the interface
     */
    @Override
    public void mouseDragged(MouseEvent arg0) {
    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     * 
     * not needed, but has to be here to implement the interface
     */
    @Override
    public void mouseClicked(MouseEvent arg0) {
    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     * 
     * not needed, but has to be here to implement the interface
     */
    @Override
    public void mouseEntered(MouseEvent arg0) {
    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     * 
     * not needed, but has to be here to implement the interface
     */
    @Override
    public void mousePressed(MouseEvent arg0) {
    }


    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     * 
     * not needed, but has to be here to implement the interface
     */
    @Override
    public void mouseReleased(MouseEvent arg0) {
    }

    
    /**
     * just used to register the BusyLED to the data loader
     * 
     * @return
     *  the model
     */
    public Model getTableModel() {
        return tableModel;
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     *
     * listenes to selection change events
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if(event.getPropertyName().equals("selection")) {
            applySelectionFromExtern();
        }
        else {
            saveTableState();
        }
    }

    

    /* (non-Javadoc)
     * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
     * 
     * listens to changes in the selection. please note that always
     * two events will be received for one selectionchange, one event
     * from each table.
     */
    @Override
    public void valueChanged(ListSelectionEvent event) {
        this.PropagateSelectionToExtern(event);
    }

    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        System.out.println("stateChanged: " + e.toString());
        // TODO Auto-generated method stub
    }

    /**
     * sets the selection object. if no selection object is set
     * (or selection is null) then the table will not cooperate
     * with the other views
     * 
     * @param selection
     */
    public void setSelection( Selection selection ) {
        // unsubscribe from current publisher
        if(this.selection != null) {
            selectionProcess = SelectionProcess.STOP;
            this.selection.removePropertyChangeListener(Selection.SELECTION_PROPERTY, this);
            selectionProcess = SelectionProcess.READY;
        }
        // subscribe to new publisher
        if(selection != null) {
            selectionProcess = SelectionProcess.STOP;
            this.selection = selection;
            selection.addPropertyChangeListener(Selection.SELECTION_PROPERTY, this);
            selectionProcess = SelectionProcess.READY;
            this.applySelectionFromExtern();
        }
    }
    
    /**
     * @param subset
     */
    public void setSubset( Subset subset ) {
        selectionProcess = SelectionProcess.STOP;
        
        floatingTable.getSelectionModel().clearSelection();

        // make all columns unsticky
        while(stickyTable.getColumnCount() > 0) {
            makeColumnFloating();
        }
        
        lastViewportPosition.x = -1; lastViewportPosition.y = -1;      // to retrigger the loading of the cellvalues
        tableModel.setSubset(subset, moleculeOrder);
        if(rowSorter != null) {
            rowSorter.removeRowSorterListener(this);
            rowSorter.destroy();
        }
        rowSorter = new TableViewRowSorter();
        rowSorter.setModel(tableModel);
        rowSorter.setSortable(tableModel.getIndexOfSvgColumn(), false);
        rowSorter.addRowSorterListener(this);
        floatingTable.setRowSorter( rowSorter );
        stickyTable.setRowSorter( rowSorter );
        //((DefaultRowSorter<?, ?>)floatingTable.getRowSorter()).setSortable(tableModel.getIndexOfSvgColumn(), false);
        lastPickedRow = -1;
        lastPickedColumn = -1;
        selectionProcess = SelectionProcess.READY;
        
        this.applySelectionFromExtern();
        
    }

    
    
    /**
     * sets the clusterlist that should be displayed in the table.
     * only neccessary in cooperation with the dendogram.
     * 
     * @param clusterList
     *   list of clustersizes
     * @return true if clustering fits to the given subset, 
     *   false otherwise
     */
    public boolean setClusters(List <Integer> clusterList) {
        return tableModel.setClusters(clusterList);
    }


    
    // =================================================
    // handle selections
    
    void applySelectionFromExtern() {
        if( selection == null )
            return;

        if( selectionProcess == SelectionProcess.READY ) {
            selectionProcess = SelectionProcess.APPLY_FROM_EXTERN;

            // needed for the correct behaviour
            floatingTable.setApplyExternalSelections(true);
            stickyTable.setApplyExternalSelections(true);

            // both tables share the same selection model, so it doesn't
            // matter which table we ask to return it
            ListSelectionModel selectionModel = floatingTable.getSelectionModel();

            // we have probably more than just one change, so
            // whoever is listening - he should wait till we're done
            selectionModel.setValueIsAdjusting(true);

            // ignore oldValue and clear complete selection
            selectionModel.clearSelection();
            
            // set selection to newValue
            for( Structure selectedItem : selection ) {
                // skip scaffolds
                if( ! (selectedItem instanceof Molecule) )
                    continue;
                int row = tableModel.getRowOfMolecule((Molecule) selectedItem);
                // the tables should only highlight those molecules
                // that are inside the current model
                if(row != -1) {
                    row = floatingTable.convertRowIndexToView(row);
                    selectionModel.addSelectionInterval(row, row);
                }
            }
            
            // we are done
            selectionModel.setValueIsAdjusting(false);
            selectionProcess = SelectionProcess.READY;
            floatingTable.setApplyExternalSelections(false);
            stickyTable.setApplyExternalSelections(false);

        }
    }
    
    
    
    private void PropagateSelectionToExtern( ListSelectionEvent event ) {
        if( selection == null )
            return;
        
        if( tableModel.getSortState()!=SortState.READY )
            return;

        // wait till selection is complete
        if( event.getValueIsAdjusting() )
            return;

        // now that the selection is finally done: propagate it to the system
        if( selectionProcess == SelectionProcess.READY ) {
            selectionProcess = SelectionProcess.PROPAGATE_TO_EXTERN;

            ListSelectionModel selectionModel = floatingTable.getSelectionModel();
            List<Molecule> addMolecules = new LinkedList<Molecule>();
            List<Molecule> removeMolecules = new LinkedList<Molecule>();
            
            for (int i = event.getFirstIndex(); i <= event.getLastIndex(); i++) {
                if ((i < 0) | (i >= tableModel.getRowCount()))
                    continue;

                Molecule molecule = tableModel.getMoleculeAtRow(floatingTable.convertRowIndexToModel(i));

                if (selectionModel.isSelectedIndex(i)) {
                    if (!selection.contains(molecule)) {
                        addMolecules.add(molecule);
                    }
                } else {
                    if (selection.contains(molecule)) {
                        removeMolecules.add(molecule);
                    }
                }
            }
            
            selection.addAll(addMolecules);
            selection.removeAll(removeMolecules);

            // we're done
            selectionProcess = SelectionProcess.READY;
        }
    }


    /* (non-Javadoc)
     * @see javax.swing.event.RowSorterListener#sorterChanged(javax.swing.event.RowSorterEvent)
     */
    @Override
    public void sorterChanged(RowSorterEvent event) {
        if(event.getType() == RowSorterEvent.Type.SORT_ORDER_CHANGED) {
            selectionProcess = SelectionProcess.STOP;
        }
        if(event.getType() == RowSorterEvent.Type.SORTED) {
            selectionProcess = SelectionProcess.READY;
            applySelectionFromExtern();
            lastViewportPosition.x = -1;
            lastViewportPosition.y = -1;
            lastViewportSize.width = -1;
            lastViewportSize.height= -1;
            initiateCellLoading();
        }
    }


    /**
     * @return
     *  a list of currently visible rows (in the view of the model)
     */
    public List <Integer> getCurrentlyVisibleModelRows() {
        List <Integer> rowList = new ArrayList <Integer> ();

        JViewport vp = getViewport();
        Point point = vp.getViewPosition();
        
        // test if the column with the svgs is visible
        int svgColumn = tableModel.getIndexOfSvgColumn();
        boolean isSvgVisible = false;
        int startColumn = floatingTable.columnAtPoint( point );
        point.x += vp.getWidth();
        int endColumn = floatingTable.columnAtPoint( point )+1;
        if( endColumn == 0)
            endColumn = floatingTable.getColumnCount();
        endColumn = (endColumn > floatingTable.getColumnCount() ? floatingTable.getColumnCount() : endColumn);
        point.x -= vp.getWidth();
        for(int i=startColumn; i<endColumn; i++) {
            if( floatingTable.convertColumnIndexToModel(i) == svgColumn ) {
                isSvgVisible = true;
                break;
            }
        }
        if( ! isSvgVisible ) {
            // test the sticky table as well
            for(int i=0; i<stickyTable.getColumnCount(); i++) {
                if( stickyTable.convertColumnIndexToModel(i) == svgColumn ) {
                    isSvgVisible = true;
                    break;
                }
            }
        }

        // return an empty list to the svg loading trigger when the svgs aren't visible
        if( ! isSvgVisible ) {
            return rowList;
        }
        
        
        // fill list with visible row numbers
        int startRow = floatingTable.rowAtPoint( point );
        point.y += vp.getHeight();
        int endRow = floatingTable.rowAtPoint( point ) +1;
        if( endRow == 0)
            endRow = tableModel.getRowCount();
        endRow = (endRow > tableModel.getRowCount() ? tableModel.getRowCount() : endRow);

        for( int i=startRow; i<endRow; i++) {
            rowList.add( floatingTable.convertRowIndexToModel(i) );
        }
        
        return rowList;
    }
    
    /**
     * determines which table rows/columns are visible and initiates
     * the loading of the cellvalues
     */
    public void initiateCellLoading() {
        JViewport vp = getViewport();
        Point point = vp.getViewPosition();

        // test if the whole shebang is neccessary
        if( (point.x == lastViewportPosition.x) & (point.y == lastViewportPosition.y) )
            if( (getWidth() == lastViewportSize.width) & (getHeight() == lastViewportSize.height) ) {
                return;
            }
        lastViewportPosition.x = point.x;
        lastViewportPosition.y = point.y;
        lastViewportSize.width = getWidth();
        lastViewportSize.height= getHeight();

        List <Integer> columns = new ArrayList <Integer> ();
        List <Integer> rows = new ArrayList <Integer> ();

        int startRow = -1;
        int endRow = -1;
        int startColumn = -1;
        int endColumn = -1;

        
        // get start row
        startRow = floatingTable.rowAtPoint( point );
        startRow -= Model.ROW_PRELOAD;
        startRow = (startRow < 0 ? 0 : startRow );
        startRow &= 0xffffffc0;
        
        // get end row
        point.y += vp.getHeight();
        endRow = floatingTable.rowAtPoint( point ) +1;
        if( endRow == 0)
            endRow = tableModel.getRowCount();
        endRow += Model.ROW_PRELOAD;
        endRow |= 0x003f;
        endRow = (endRow > tableModel.getRowCount() ? tableModel.getRowCount() : endRow);
        
        // fill row list
        for(int i = startRow; i<endRow; i++)
            rows.add( floatingTable.convertRowIndexToModel(i));

        
        // get start column
        point.y -= vp.getHeight();
        startColumn = floatingTable.columnAtPoint( point );
        startColumn -= Model.COLUMN_PRELOAD;
        startColumn = (startColumn < 0 ? 0 : startColumn);
        startColumn &= 0xfffffff8;

        // get end column
        point.x += vp.getWidth();
        endColumn = floatingTable.columnAtPoint( point )+1;
        if( endColumn == 0)
            endColumn = floatingTable.getColumnCount();
        endColumn += Model.COLUMN_PRELOAD;
        endColumn |= 0x0007;
        endColumn = (endColumn > floatingTable.getColumnCount() ? floatingTable.getColumnCount() : endColumn);

        // fill column list
        for(int i=0; i<stickyTable.getColumnCount(); i++)
            columns.add(stickyTable.convertColumnIndexToModel(i));
        for(int i = startColumn; i<endColumn; i++)
            columns.add(floatingTable.convertColumnIndexToModel(i));
        
        
        // now start the loading of the molecule values
        tableModel.loadPropertyValues(rows, columns);
    }
    
    
    /**
     * Zooms to the first selected molecule
     */
    public void zoomToSelection() {
        int row = floatingTable.getSelectedRow();
        if(row != -1) {
            row = floatingTable.convertRowIndexToModel(row);
            Molecule m = tableModel.getMoleculeAtRow(row);
            floatingTable.ensureStructureIsVisible(m);
        }
    }
    
    /**
     * Focuses the given molecule
     * 
     * @param molecule
     */
    public void focusMolecule(Molecule molecule) {
        floatingTable.ensureStructureIsVisible(molecule);
    }    
    
    // ==================================================================
    //
    // actions
    //
    // ==================================================================


    /**
     * @return
     *  a toolbar with actions for the tableview
     */
    public JToolBar getToolBar() {
        return toolbar;
    }
    
    /**
     * @return
     *  a menu with actions for the tableview
     */
    public JMenu getMenu() {
        return viewMenu;
    }


    /**
     * @return
     *  the sidebarcomponents
     */
    public List<SideBarItem> getSideBar() {
        /*
        Vector<SideBarItem> sidebarComponents = new Vector<SideBarItem>();
        
        // set up minimap
        MiniMap miniMap = new MiniMap(this);
        miniMap.setPreferredSize(new Dimension(160, 160));
        sidebarComponents.add(new SideBarItem(I18n.get("TableView.MiniMap.Title"), Resources.getIcon("minimap.png"), miniMap));
    
        // set up cell zoom
        CellZoom cellZoom = new CellZoom();
        cellZoom.setPreferredSize(new Dimension(160, 250));
        this.addCellPickListener(cellZoom);
        sidebarComponents.add(new SideBarItem(I18n.get("TableView.Detailview"), Resources.getIcon("zoom.png"), cellZoom));
        */
        return sidebarComponents;
    }
    
    
    /*
     * Action: make a column floating, used in menu and toolbar
     */
    private class MakeColumnFloatingAction extends AbstractAction {
        public MakeColumnFloatingAction() {
            super(I18n.get("TableView.Menu.MakeColumnFloating"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("TableView.Menu.MakeColumnFloatingToolTip"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("table-sticky-remove.png"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            makeColumnFloating();
        }
    }

    /*
     * Action: make a column sticky, used in menu and toolbar
     */
    private class MakeColumnStickyAction extends AbstractAction {
        public MakeColumnStickyAction() {
            super(I18n.get("TableView.Menu.MakeColumnSticky"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("TableView.Menu.MakeColumnStickyToolTip"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("table-sticky-add.png"));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            makeColumnSticky();
        }
    }


    /*
     * Action: enlarges the rows
     */
    private class EnlargeRowHeightAction extends AbstractAction {
        public EnlargeRowHeightAction () {
            super(I18n.get("TableView.Menu.EnlargeRows"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("TableView.Menu.EnlargeRowsToolTip"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("table-lines-enlarge.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            rowHeightEnlarge();
        }
    }

    /*
     * Action: shrinks the rows
     */
    private class ShrinkRowHeightAction extends AbstractAction {
        public ShrinkRowHeightAction () {
            super(I18n.get("TableView.Menu.ShrinkRows"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("TableView.Menu.ShrinkRowsToolTip"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("table-lines-shrink.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('-'));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            rowHeightShrink();
        }
    }

    /*
     * Action: sets row heights back to normal size
     */
    private class NormalizeRowHeightAction extends AbstractAction {
        public NormalizeRowHeightAction () {
            super(I18n.get("TableView.Menu.NormalizeRows"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("TableView.Menu.NormalizeRowsToolTip"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("table-lines-normalize.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('0'));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            rowHeightNormalize();
        }
    }
 

    /**
     * 
     */
    private class ZoomToSelectionAction extends AbstractAction {
        public ZoomToSelectionAction() {
            super(I18n.get("TableView.ToolBar.ScrollToSelection"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("TableView.ToolBar.ScrollToSelection"));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-fit-selection.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-fit-selection.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('s'));
        }        
        @Override
        public void actionPerformed(ActionEvent e) {
            zoomToSelection();
        }
    }

    
    /**
     * this method ensures that the table scrolls to the row
     * which is associated with the specified structure. the
     * effect is, that the structure will become visible (if
     * it was not visible before). 
     * If the structure is not part of the subset which is 
     * shown in the table, or if the structure is null, then 
     * nothing happends.
     * 
     * @param structure
     */
    public void ensureStructureIsVisible(Structure structure) {
        if( ! (structure instanceof Molecule) )
            return;
        int row = tableModel.getRowOfMolecule((Molecule)structure);
        if( row != -1 ) {
            row = floatingTable.convertRowIndexToView(row);
            Rectangle r = new Rectangle(0, 0, getViewport().getWidth(), getViewport().getHeight());
            r.y = floatingTable.getRowHeight()*row;
            floatingTable.scrollRectToVisible(r);
        }
    }

    /**
     * this method is overwritten just to be able to restore the viewstate.
     * some properties of the tables seem to be only available after the
     * table is painted for the first time
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if(!viewStateInitialized) {

            // add THIS to some listeners, to be able to save the current state of the table
            for(int i=0; i<floatingTable.getColumnCount(); i++) {
                floatingTable.getColumnModel().getColumn(i).addPropertyChangeListener(this);
            }

            // restore viewstate
            if(viewState != null) {
                if( viewState instanceof TableViewState ) {
                    TableViewState tvs = (TableViewState) viewState;
                    if(tvs.isValid()) {
                        // restore state
                        if( tvs.getStickyColumnOrder() != null ) {

                            Map <String, Integer> columnWidth = tvs.getColumnWidth();
                            for(String columnName : columnWidth.keySet()) {
                                int index = floatingTable.getColumnModel().getColumnIndex(columnName);
                                TableColumn column = floatingTable.getColumnModel().getColumn(index);
                                column.setPreferredWidth( columnWidth.get(columnName) );
                            }
                            
                            String[] stickyColumnOrder = tvs.getStickyColumnOrder();
                            for(int i=0; i<stickyColumnOrder.length; i++) {
                                int index = floatingTable.getColumnModel().getColumnIndex(stickyColumnOrder[i]);
                                TableColumn column = floatingTable.getColumnModel().getColumn(index);
                                floatingTable.removeColumn(column);
                                stickyTable.addColumn(column);
                            }
                            
                            String[] floatingColumnOrder = tvs.getFloatingColumnOrder();
                            for(int i=0; i<floatingColumnOrder.length; i++) {
                                int index = floatingTable.getColumnModel().getColumnIndex(floatingColumnOrder[i]);
                                floatingTable.moveColumn(index, i);
                            }
                        }
                        int scrollPosX = tvs.getScrollPosX();
                        int scrollPosY = tvs.getScrollPosY();
                        Point scrollPoint = new Point( scrollPosX, scrollPosY);
                        this.getViewport().setViewPosition( scrollPoint );
                    }
                }
            }

            viewStateInitialized = true;
        }
        
    }


    /* (non-Javadoc)
     * @see javax.swing.event.TableColumnModelListener#columnAdded(javax.swing.event.TableColumnModelEvent)
     */
    @Override
    public void columnAdded(TableColumnModelEvent arg0) {
        // not needed
    }


    /* (non-Javadoc)
     * @see javax.swing.event.TableColumnModelListener#columnMarginChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void columnMarginChanged(ChangeEvent arg0) {
        // not needed
    }


    /* (non-Javadoc)
     * @see javax.swing.event.TableColumnModelListener#columnMoved(javax.swing.event.TableColumnModelEvent)
     */
    @Override
    public void columnMoved(TableColumnModelEvent arg0) {
        saveTableState();
    }


    /* (non-Javadoc)
     * @see javax.swing.event.TableColumnModelListener#columnRemoved(javax.swing.event.TableColumnModelEvent)
     */
    @Override
    public void columnRemoved(TableColumnModelEvent arg0) {
        // not needed
    }


    /* (non-Javadoc)
     * @see javax.swing.event.TableColumnModelListener#columnSelectionChanged(javax.swing.event.ListSelectionEvent)
     */
    @Override
    public void columnSelectionChanged(ListSelectionEvent arg0) {
        // not needed
    }
    
    /**
     * 
     */
    private void saveTableState() {
        if(viewStateInitialized)
            if(viewState != null) {
                if( viewState instanceof TableViewState ) {
                    TableViewState tvs = (TableViewState) viewState;

                    Map <String, Integer> columnWidth = new HashMap <String, Integer> ();
                    for(int index = 0; index<stickyTable.getColumnModel().getColumnCount(); index++) {
                        String columnName = stickyTable.getColumnModel().getColumn(index).getIdentifier().toString();
                        int width = stickyTable.getColumnModel().getColumn(index).getWidth();
                        columnWidth.put(columnName, width);
                    }
                    for(int index = 0; index<floatingTable.getColumnModel().getColumnCount(); index++) {
                        String columnName = floatingTable.getColumnModel().getColumn(index).getIdentifier().toString();
                        int width = floatingTable.getColumnModel().getColumn(index).getWidth();
                        columnWidth.put(columnName, width);
                    }

                    String[] stickyColumnOrder = new String[stickyTable.getColumnCount()];
                    for(int i=0; i<stickyColumnOrder.length; i++) {
                        stickyColumnOrder[i] = stickyTable.getColumnModel().getColumn(i).getIdentifier().toString();
                    }

                    String[] floatingColumnOrder = new String[floatingTable.getColumnCount()];
                    for(int i=0; i<floatingColumnOrder.length; i++) {
                        floatingColumnOrder[i] = floatingTable.getColumnModel().getColumn(i).getIdentifier().toString();
                    }

                    tvs.setColumnWidth(columnWidth);
                    tvs.setStickyColumnOrder(stickyColumnOrder);
                    tvs.setFloatingColumnOrder(floatingColumnOrder);
                    
                }
        }
    }

    /**
     * 
     */
    public void destroySideBar() {
        if(sidebarComponents != null) {
            for(SideBarItem sbItem : sidebarComponents) {
                Component component = sbItem.getComponent();
                if(component instanceof MiniMap)
                    ((MiniMap)component).destroy();
                if(component instanceof CellZoom)
                    ((CellZoom)component).destroy();
            }
        }
    }
}
