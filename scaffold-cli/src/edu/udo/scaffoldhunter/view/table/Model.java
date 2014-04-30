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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.table.BannerManager.BannerState;
import edu.udo.scaffoldhunter.view.util.SVG;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGLoadObserver;

/**
 * @author Michael Hesse
 *
 */
public class Model extends AbstractTableModel implements BannerManagerListener {

    private final DbManager db;
    private final BannerPool bannerPool;

    private BiMap <Molecule, Integer> molecules;
    private BiMap <Integer, Molecule> moleculesInverse;
    private boolean hasClusters = false;
    List <ColumnInfo> columnInfo;
    int svgColumnNumber;        // just a shortcut, so we don't have to search it all the time
    Subset subset;
    List <Integer> clusterNumberList = new ArrayList <Integer> ();

    SVGCache svgCache;
    SVGObserver[] svgObserver;

    BannerManager bm;
    //DataCache cache;
    DataPump dataPump;
    
    // number of lines before and after the viewport that should be preloaded
    static final int ROW_PRELOAD = 150;
    static final int COLUMN_PRELOAD = 20;
    
    ViewComponent vc = null;
    
    static private enum BannerChangeState { READY, STOP };
    BannerChangeState bannerChangeState;
    

    /** current state of the sorting process */
    public enum SortState { 
        /** user requested sorting                 */ START, 
        /** loading values for the column(s)       */ LOADING, 
        /** actual sorting of the loaded values    */ SORTING,
        /** no sorting related process running atm */ READY };
    private SortState sortState;
    
    
    class ColumnInfo implements Comparable <ColumnInfo> {

        public String title;            // can be language dependent
        public String key;              // must be constant and unique
        public Class<?> type;
        public PropertyDefinition propertyDefinition;
        
        public ColumnInfo() {
        }
        
        public ColumnInfo( String title, String key, Class<?> type, PropertyDefinition propertyDefinition) {
            this.title = title;
            this.key = key;
            this.type = type;
            this.propertyDefinition = propertyDefinition;
        }
        
        @Override
        public int compareTo(ColumnInfo b) {
            if ( (title==null) && (b.title==null) )
                return 0;
            if ( title == null )
                return 1;
            if ( b.title == null )
                return -1;
            return title.compareTo(b.title);            
        }
    }

    
    /**
     * internal class to assign the notification from the SVGCache
     * to a tablecell
     * @author Michael Hesse
     */
    class SVGObserver implements SVGLoadObserver {
        /* (non-Javadoc)
         * @see edu.udo.scaffoldhunter.view.util.SVGLoadObserver#svgLoaded(edu.udo.scaffoldhunter.view.util.SVG)
         */
        private int row, column;
        private Subset subset;
        
        public SVGObserver(int row, int column) {
            this.row = row;
            this.column = column;
            this.subset = getSubset();
        }
        @Override
        public void svgLoaded(SVG svg) {
            svg.removeObserver(this);
            // ignore the event when the subset has changed while loading
            if( getSubset() == subset )
                fireTableCellUpdated(row, column);
        }
    }

    
    class MoleculeComparator <T> implements Comparator <T> {

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(T a, T b) {
            // TODO Auto-generated method stub
            Molecule x = (Molecule) a;
            Molecule y = (Molecule) b;
            return x.getTitle().toLowerCase().compareTo(y.getTitle().toLowerCase());
        }
        
    }
    
    
    


    
    /**
     * the constructor
     * 
     * @param db
     *  the DB manager
     * @param hasClusters 
     *  true if there should be a column for clusternumbers
     * @param bannerPool 
     */
    public Model(DbManager db, boolean hasClusters, BannerPool bannerPool) {
        this.db = db;
        this.bannerPool = bannerPool;
        molecules = HashBiMap.create();
        moleculesInverse = molecules.inverse();
        columnInfo = new ArrayList <ColumnInfo> ();
        this.hasClusters = hasClusters;
        dataPump = null;
        
        svgCache = new SVGCache(db);
        svgColumnNumber = -1;

        // strange, but true...
        clusterNumberList.add(999999999);
        
        sortState = SortState.READY;
        setSubset(null, null);
        
        bannerChangeState = BannerChangeState.READY;
    }

    
    /**
     * @return  the DB manager
     */
    public DbManager getDbManager() {
        return db;
    }
    
    /**
     * sets the subset that this model should serve
     * @param subset
     * @param moleculeOrder 
     */
    public void setSubset(Subset subset, List <Molecule> moleculeOrder) {

        molecules.clear();
        columnInfo.clear();
        sortState = SortState.READY;
        
        this.subset = subset;
        
        if(subset != null) {

            if(dataPump != null)
                dataPump.destroy();
            dataPump = new DataPump(this);

            int moleculeIndex = 0;
            // rebuild list of molecules
            if( moleculeOrder == null ) {
                List <Molecule> mo = new ArrayList <Molecule> ();
                mo.addAll( subset.getMolecules() );
                Collections.sort( mo, new MoleculeComparator <Molecule> () );
                for( Molecule m : mo ) {
                    molecules.put(m, moleculeIndex);
                    moleculeIndex++;
                }
            } else {
                for (Molecule m : moleculeOrder ) {
                    molecules.put(m, moleculeIndex);
                    moleculeIndex++;
                }
            }

            { // rebuild columnInfos
            
                // lets get the properties first
                for (PropertyDefinition pd : subset.getSession().getDataset().getPropertyDefinitions().values()) {
                    if (!pd.isScaffoldProperty()) {
                        columnInfo.add(new ColumnInfo(pd.getTitle(), "key_pd_" + pd.getKey(),
                                (pd.isStringProperty() ? String.class : Double.class), pd));
                    }
                }
                Collections.sort(columnInfo);
                
                // SMILES
                columnInfo.add( 0, new ColumnInfo("SMILES", "key_smiles", String.class, null) );
                
                // SVG
                columnInfo.add( 0, new ColumnInfo("SVG", "key_svg", SVG.class, null) );

                // banners
                columnInfo.add( 0, new ColumnInfo(I18n.get("TableView.Model.PublicBanner"), "key_publicbanner", BannerManager.BannerState.class, null) );
                columnInfo.add( 1, new ColumnInfo(I18n.get("TableView.Model.PrivateBanner"), "key_privatebanner", BannerManager.BannerState.class, null) );
                
                // title
                columnInfo.add(0, new ColumnInfo(
                        I18n.get("TableView.Model.Title"),
                        "key_title",
                        String.class,
                        null
                        )
                );
                
                // clusters
                if(hasClusters) {
                    columnInfo.add(0, new ColumnInfo(
                            I18n.get("TableView.Model.Cluster"),
                            "key_cluster",
                            Integer.class,
                            null
                            )
                    );
                }
            }
            
            { // set up svgObservers
                svgColumnNumber = -1;
                // find the column where the SVGs should be displayed
                for (int i=0; i<columnInfo.size(); i++)
                    if(columnInfo.get(i).key.equals("key_svg")) {
                        svgColumnNumber = i;
                        break;
                    }
                // initializes the observers
                svgObserver = new SVGObserver[ molecules.size() ];
                for(int i=0; i<svgObserver.length; i++)
                    svgObserver[i] = new SVGObserver( i, svgColumnNumber );
            }

        }

        // initilize new bannermanager
        bm = new BannerManager(subset, bannerPool);
        bm.addListener(this);
        
        this.fireTableStructureChanged();
    }
    
    /**
     * @return
     *  the subset that this model currently serves
     */
    public Subset getSubset() {
        return subset;
    }
    
    
    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return columnInfo.size();
    }

    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getRowCount()
     */
    @Override
    public int getRowCount() {
        return molecules.size();
    }

    
    /* (non-Javadoc)
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {

        // the value to return;
        Object value = null;

        // do we have so many rows? (we should always have)
        if( rowIndex < this.getRowCount() ) {

            // determine key of this column
            String key = columnInfo.get(columnIndex).key;
            
            if( key.equals("key_cluster") ) {
                // the cluster number
                // do a binary search
                if(rowIndex < clusterNumberList.get(0))
                    value = 0;
                else {
                    int start = 0;
                    int end = clusterNumberList.size()-1; // last element
                    while ( rowIndex<clusterNumberList.get(end-1) ) {
                        if( rowIndex < clusterNumberList.get( (start+end)/2 ) )
                            end = (start+end)/2;
                        else
                            start = (start+end)/2;
                    }
                    value = end;
                }                

            } else if ( key.equals("key_title") ) {
                // the title
                value = getMoleculeAtRow(rowIndex).getTitle();
            
            } else if ( key.equals("key_svg") ) {
                // the SVG
                value = svgCache.getSVG(getMoleculeAtRow(rowIndex), null, null, svgObserver[rowIndex]);
                
            } else if ( key.equals("key_smiles") ) {
                // the SMILES
                value = getMoleculeAtRow(rowIndex).getSmiles();

            } else if ( key.equals("key_publicbanner") ) {
                value = bm.getPublicBanner(getMoleculeAtRow(rowIndex));

            } else if ( key.equals("key_privatebanner") ) {
                value = bm.getPrivateBanner(getMoleculeAtRow(rowIndex));

            } else if ( key.startsWith("key_pd_") ) {
                Molecule m;
                Molecule mm = getMoleculeAtRow(rowIndex);
                PropertyDefinition pd = columnInfo.get(columnIndex).propertyDefinition;
                switch( getSortState() ) {
                    case READY:
                        //# m = dataLoader.getMolecule(molecules.get(rowIndex));
                        //if(cache.contains(columnIndex, rowIndex))
                        if(dataPump.contains(pd, mm))
                            m = mm;
                        else
                            m = null;
                        break;
                    case SORTING:
                        m = mm;
                        break;
                    default:
                        m = null;
                }
                
                if( columnInfo.get(columnIndex).type == String.class) {
                    if( m == null ) {
                        value = "...";
                    }
                    else {
                        value = m.getStringPropertyValue( pd );
                        //value = cache.getValue(columnIndex, rowIndex);
                    }
                } else {
                    if( m == null )
                        value = Double.NaN;
                    else
                        value = m.getNumPropertyValue( pd );
                        //value = cache.getValue(columnIndex, rowIndex);
                }
                
            }
            
        }
        
        return value;
    }
    
    /**
     * @param row
     * @return molecule
     *  the molecule in the specified row
     */
    public Molecule getMoleculeAtRow(int row) {
        return moleculesInverse.get(row);
    }

    /**
     * @param column
     * @return
     *  the propertydefinition for the specified column
     */
    public PropertyDefinition getPropertyDefinitionAtColumn(int column) {
        return columnInfo.get(column).propertyDefinition;
    }
    
    /**
     * find the position of a molecule in the tablemodel. needed
     * for automatic selection of molecules
     * 
     * @param molecule
     *  the molecule for which the tablerow should be returned
     * @return
     *  the row in which the given molecule is stored in the model.
     *  -1 if the molecule isn't found in the model
     */
    public int getRowOfMolecule(Molecule molecule) {
        Integer row = molecules.get(molecule); 
        return row != null? row: -1;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return columnInfo.get(column).type;
    }
    
    @Override
    public String getColumnName(int column) {
        return columnInfo.get(column).title;
    }

    /**
     * returns the description of the moleculeproperty for this column
     * 
     * @param column
     * @return
     *  the propertydescription or null (if the column shows something else than a property)
     */
    public String getColumnDescription(int column) {
        if( columnInfo.get(column).propertyDefinition == null) 
            return null;
        return columnInfo.get(column).propertyDefinition.getDescription();
    }
    
    
    /**
     * @return
     *  the data loader of this model
     */
    //# public DataLoader getDataLoader() {
    //#     return dataLoader;
    //# }
    
    //# /**
    //#  * called from the tables to indicate which molecules
    //#  * are visible (and should be loaded)
    //#  * @param wantedMolecules
    //#  */
    //# public void setWantedMolecules(List <Molecule> wantedMolecules ) {
    //#    if( getSortState() == SortState.READY )
    //#        dataLoader.setWantedMolecules(wantedMolecules);
    //#}
    
    
    /**
     * @param rows
     * @param columns
     */
    public void loadPropertyValues( List <Integer> rows, List <Integer> columns) {
        List <PropertyDefinition> propertyDefinitions = new ArrayList <PropertyDefinition> ();
        List <Molecule> molecules = new ArrayList <Molecule> ();
        
        for(Integer column : columns) {
            PropertyDefinition pd = getPropertyDefinitionAtColumn(column);
            if( pd != null)
                propertyDefinitions.add( pd );
        }
        for(Integer row : rows)
            molecules.add( getMoleculeAtRow(row) );
        
        if( (! propertyDefinitions.isEmpty()) & (! molecules.isEmpty()) ) {
            DataPumpBlock block = new DataPumpBlock( propertyDefinitions, molecules );
            dataPump.load(block);
        }
    }
    
    
    
    /**
     * sets the number of clusters and the number of elements in each cluster
     * @param clusterList
     * @return 
     *   true if the clusterlist is adopted, false if an error occured and the
     *   list is not adopted
     */
    public boolean setClusters(List <Integer> clusterList) {
        
        if( hasClusters == false )
            return false;
        
        { // test if the partitioning is valid
            int length = 0;
            for (int clusterSize: clusterList) {
                length += clusterSize;
            }
            if (length != molecules.size())
                return false;
        }
        
        {
            clusterNumberList.clear();
            int sum=0;
            for (int clusterSize : clusterList) {
                sum += clusterSize;
                clusterNumberList.add(sum);
            }
            
        }
        
        //fireTableDataChanged(); 
        if(getRowCount() != 0)
            fireTableRowsUpdated(0, getRowCount()-1);
        return true;
    }


    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.DataLoaderListener#loadingStartedEvent()
     */
    /*
    @Override
    public void loadingStartedEvent() {
        // nothing to do here atm
    }
*/

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.DataLoaderListener#loadingFinishedEvent()
     */
    /*
    @Override
    public void loadingFinishedEvent() {
        repaintTable();
    }
*/
    
    /**
     * 
     */
    public void repaintTable() {
        // repaint. a quick fix for the slow system
        TableModelListener[] tml =  this.getTableModelListeners();
        for(int i=0; i<tml.length; i++) {
            if( tml[i] instanceof ViewTable ) {
                ((ViewTable)tml[i]).repaint();
            }
        }
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.DataLoaderListener#moleculeLoaded(edu.udo.scaffoldhunter.model.db.Molecule)
     */
    /*
    @Override
    public void moleculeLoaded(Molecule molecule) {
        int row = getRowOfMolecule(molecule);
        fireTableRowsUpdated(row, row);
        
        // quick'n dirty fix, because the system may react
        // too slow to the repaint requests from the jTable.
        // when the user scrolls like a madman it may happen
        // that the row in question isn't at it's former
        // position anymore by the time the system fulfills
        // the repaint-request. in this case the wrong tablerow
        // becomes repainted.
        //
        // Note: quickfix changed, see loadingFinishedEvent()
        //
        //int startRow = ( row-1 >= 0 ? row-1 : row);
        //int endRow = ( row+1 < getRowCount() ? getRowCount()-1 : row);
        //fireTableRowsUpdated(startRow, endRow);
    }
*/ 
    
    
    /**
     * invoked before the table should become sorted, so the
     * model gets the chance to gather all the data for the
     * columns (specified by the sortkeys)
     * 
     * @param rowSorter 
     * @param viewComponent 
     */
    /*
    public void doSorting( TableRowSorter<?> rowSorter, ViewComponent viewComponent ) {
        if( getSortState() != SortState.READY )
            return;

        sortState = SortState.START;
        //# dataLoader.destroy();   // stop it and unload all locked properties
       
        List <? extends SortKey> sortKeys = rowSorter.getSortKeys();

        
        new Thread() {
            List <? extends SortKey> sList;
            ViewComponent viewComponent;
            Subset subset;
            
            public void kickStart(List <? extends SortKey> sList, TableRowSorter<?> rowSorter, ViewComponent viewComponent) {
                sortState = SortState.LOADING;
                this.sList = sList;
                this.viewComponent = viewComponent;
                subset = getSubset();
                start();
            }
            
            @Override
            public void run() {
                
                // use a copy of the molecule list to prevent concurrency related modification trouble
                Map<Molecule, Integer> moleculeListCopy = new HashMap<Molecule, Integer> (molecules);
                List <PropertyDefinition> pdList = new ArrayList <PropertyDefinition> ();
                
                for(SortKey sk : sList) {
                    int column = sk.getColumn();
                    if(columnInfo.get(column).key.startsWith("key_pd_")) {
                        pdList.add( columnInfo.get(column).propertyDefinition );
                    }
                }
                
                if( ! pdList.isEmpty() ) {
                    try {
                        db.lockAndLoad(pdList, moleculeListCopy.keySet());
                    } catch (DatabaseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                } 

                if( getSubset() == subset ) {
                    sortState = SortState.SORTING;
                    fireTableDataChanged();
                
                    if( ! pdList.isEmpty() ) {
                        db.unlockAndUnload(pdList, moleculeListCopy.keySet());
                    } 
                    sortState = SortState.READY;
                    
                    // revalidate tables
                    TableModelListener[] tml =  getTableModelListeners();
                    for(int i=0; i<tml.length; i++) {
                        if( tml[i] instanceof ViewTable ) {
                            ((ViewTable)tml[i]).revalidateAfterChange();
                        }
                    }
                    viewComponent.applySelectionFromExtern();
                } else {
                    if( ! pdList.isEmpty() ) {
                        db.unlockAndUnload(pdList, moleculeListCopy.keySet());
                    } 
                }
            }
            
        }.kickStart(sortKeys, rowSorter, viewComponent);

    }
*/
    
    /**
     * @return
     *  the state of the sorting process
     */
    public SortState getSortState() {
        return sortState;
    }
    
    
    /**
     * needed by the table to make this column unsortable.
     * and by the svg loading trigger.
     * @return
     *  index of the svg-column
     */
    
    public int getIndexOfSvgColumn() {
        return svgColumnNumber;
        /*
        int index = -1;
        for(int i=0; i<this.columnInfo.size(); i++) {
            if(columnInfo.get(i).key.equals("key_svg")) {
                index = i;
                break;
            }
        }
        return index;
        */
    }
    

    /**
     * @return
     *  the bannermanager
     */
    public BannerManager getBannerManager() {
        return bm;
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.table.BannerManagerListener#BannerStateChanged(edu.udo.scaffoldhunter.model.db.Structure, boolean, edu.udo.scaffoldhunter.view.table.BannerManager.BannerState)
     */
    @Override
    public void BannerStateChanged(Molecule molecule, boolean privateBanner, BannerState state) {
        int row = getRowOfMolecule(molecule);
        int column = -1;
        
        for(int i=0; i < columnInfo.size(); i++) {
            if( (!privateBanner) & columnInfo.get(i).key.equals("key_publicbanner")) {
                column = i;
                break;
            }
            if( privateBanner & columnInfo.get(i).key.equals("key_privatebanner")) {
                column = i;
                break;
            }
        }
        if(column != -1) {
            this.fireTableCellUpdated(row, column);
        }

    }


    /**
     * @param c
     * @return
     *  the key of the tablecolumn
     */
    public String getColumnKey(int c) {
        if(c < columnInfo.size())
            return columnInfo.get(c).key;
        else
            return "";
    }
 
    
    /**
     * @param vc
     */
    public void setViewComponent( ViewComponent vc ) {
        this.vc = vc;
    }
    
    /**
     * 
     */
    public void reapplySelectionFromExtern() {
        if(vc != null)
            vc.applySelectionFromExtern();
    }
    
    /**
     * loads (and paints) the svg at the specified rowIndex
     * @param rowIndex
     */
    public void loadAnSvg(int rowIndex) {
        // the adjacent tablecell will be updated by the svgObserver
        svgCache.getSVG(getMoleculeAtRow(rowIndex), 
                null, null, 
                svgObserver[rowIndex]);
    }
    

    /**
     * 
     */
    public void destroy() {
        bm.destroy();
        
        columnInfo.clear();
        columnInfo = null;
        clusterNumberList = null;
        subset = null;
        svgCache = null;
        for(int i=0; i<svgObserver.length; i++) {
            svgObserver[i] = null;
        }
        if(dataPump != null)
            dataPump.destroy();
        dataPump = null;
        //if(vc != null)                // this is just a reference to an object that is destroy by Tableview
        //    vc.destroy();
        vc = null;
        
        molecules.clear();
        molecules = null;
        moleculesInverse.clear();
        moleculesInverse = null;
    }
}
