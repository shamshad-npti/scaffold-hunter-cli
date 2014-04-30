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
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.table.BannerManager.BannerState;
import edu.udo.scaffoldhunter.view.table.Model.SortState;
import edu.udo.scaffoldhunter.view.util.SVG;

/**
 * @author Michael Hesse
 *
 */
public class ViewTable extends JTable {
    
    private double normalRowHeight; 
    private double currentRowLines; // needed to avoid rounding errors by int values
    
    private boolean applyExternalSelections = false;

    private boolean isSticky;
    private ViewComponent viewComponent;
    
    
    /**
     * @param db
     * the DB manager
     * @param dm
     * a TableViewTableModel
     * @param sticky 
     * true: this is the sticky table
     * false: this is the floating table
     * @param vc 
     */
    public ViewTable(DbManager db, TableModel dm, boolean sticky, ViewComponent vc) {
        super();
        
        // init
        setAutoCreateColumnsFromModel(!sticky);
        setModel(dm);
        setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        currentRowLines = 1.0;
        isSticky = sticky;
        viewComponent = vc;
        
        // does this fix for nonstandard fontsizes work?
        normalRowHeight = getFontMetrics( getFont() ).getHeight();
        rowHeightNormalize();
        //normalRowHeight = getRowHeight();    // <- the original line
        
        
        // customize
        setFillsViewportHeight(true);
        setShowHorizontalLines(false);
        setShowVerticalLines(false);
        setGridColor(Color.lightGray);
        //setRowHeight((int)normalRowHeight);
        setFocusable(false);
        
        // darken the header of the sticky table
        if(sticky) {
            TableCellRenderer originalRenderer = getTableHeader().getDefaultRenderer();
            DarkHeaderRenderer newRenderer = new DarkHeaderRenderer( originalRenderer );
            getTableHeader().setDefaultRenderer( newRenderer );
        }
        
        // set table cell renderers
        setDefaultRenderer(BannerState.class, new BannerCellRenderer());
        setDefaultRenderer(SVG.class, new SVGCellRenderer());
        setDefaultRenderer(String.class, new TextCellRenderer());       // remove this line to go back to normale JLabel renderers
        setDefaultRenderer(Double.class, new TextCellRenderer());       // remove this line to go back to normale JLabel renderers
        setDefaultRenderer(Integer.class, new ClusterCellRenderer());   // registering this to integers is surely not optimal...
        
        // set foreground, background and zebra colors
        setForeground(Color.BLACK);
        setBackground(Color.WHITE);
        if ( sticky ) {
            Color background = new Color( 
                    (int) (getBackground().getRed()*0.95), 
                    (int) (getBackground().getGreen()*0.95), 
                    (int) (getBackground().getBlue()*0.95) );
            setBackground( background );
        }
        Color zebra = new Color( 
                    (int)(getBackground().getRed() * 0.92), 
                    (int)(getBackground().getGreen() * 0.92), 
                    (int)(getBackground().getBlue() * 0.92));
        putClientProperty("zebracolor", zebra);
        
        // add context menu
        addMouseListener( new ContextMenuMouseAdapter(db, this) );
    }

    
    /**
     * @param model
     */
    public void setModel(Model model) {
        super.setModel(model);
    }
    
    
    
    /**
     * 
     */
    public void rowHeightNormalize() {
        currentRowLines = 1;
        // the +2 may fix the cutting of descents (or however it's spelled)
        setRowHeight( (int)(normalRowHeight) +2 );              
    }
    
    /**
     * 
     */
    public void rowHeightEnlarge() {
        currentRowLines++;
        // the +2 may fix the cutting of descents (or however it's spelled)
        setRowHeight( (int)(normalRowHeight*currentRowLines) +2 );
    }

    /**
     * 
     */
    public void rowHeightShrink() {
        if(currentRowLines > 1) {
            currentRowLines--;
            // the +2 may fix the cutting of descents (or however it's spelled)
            setRowHeight( (int)(normalRowHeight*currentRowLines) +2 );
        }
    }
    
    
    @Override
    /**
     * overridden to change the selection behaviour of the tables
     */
    public void changeSelection (int rowIndex, int columnIndex, boolean toggle, boolean extend) {
        if( ((Model)getModel()).getSortState() == SortState.READY ) {
                if(applyExternalSelections)
                    super.changeSelection(rowIndex, columnIndex, toggle, extend);
                else
                    super.changeSelection(rowIndex, columnIndex, true, extend);
        } else {
            // while the table becomes sorted changes in the selections will just be ignored
        }
    }

    
    @Override
    public void paintComponent (Graphics g) {
        if( ! isSticky) {
            viewComponent.initiateCellLoading();
        }
        super.paintComponent(g);
    }
 
    
    /**
     * called after sorting to invalidate the cached visible rows
     */
    public void revalidateAfterChange() {
        repaint();
    }
    
    
    /**
     * needed for the correct behaviour
     * @param b
     */
    public void setApplyExternalSelections (boolean b) {
        applyExternalSelections = b;
    }

    /**
     * @param structure
     */
    public void ensureStructureIsVisible(Structure structure) {
        viewComponent.ensureStructureIsVisible(structure);
    }
        
    // create tooltiprenderer for table headers
    @Override
    protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            @Override
            public String getToolTipText(MouseEvent e) {
                String tip = null;
                String description = null;
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = 
                        columnModel.getColumn(index).getModelIndex();
                tip = getModel().getColumnName(realIndex);
                description = ((Model)getModel()).getColumnDescription(realIndex);
                if(description != null) {
                    if( ! description.isEmpty() ) {
                    tip = "<html>"+tip
                    +"<br><br><b>"+I18n.get("PropertyDefinition.Description")+":</b> "
                    +description+"</html>";
                    }
                }
                return tip;
            }
        };
    }

    /**
     * create a tooltip for a table cell
     */
    /*
    @Override
    public String getToolTipText(MouseEvent event) {
        TooltipManager tooltipManager = ((Model) getModel()).getTooltipManager();
        Point location = event.getPoint();
        
        //if( (location.x != lastTooltipLocation.x) | (location.y != lastTooltipLocation.y) ) { 
            lastTooltipLocation = location;
            int row = rowAtPoint(location);
            int column = columnAtPoint(location);
            int width = getColumnModel().getColumn(column).getWidth();
            int height = getRowHeight(row);
            location.x += 10;
            Rectangle region = new Rectangle(width, height);
            Molecule molecule = ((Model)getModel()).getMoleculeAtRow(row);
            
            //tooltipManager.showTooltip(event.getLocationOnScreen(), this, region, molecule);
        //}
        return null;
    }
    */
    
    
    
    /**
     * decorator for the tableCellRenderer which is used to render the
     * column headers. this decorator just darkens the background of the
     * header.
     */
    class DarkHeaderRenderer implements TableCellRenderer {
        TableCellRenderer originalRenderer;
        
        public DarkHeaderRenderer( TableCellRenderer r ) {
            originalRenderer = r;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = originalRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            c.setBackground( c.getBackground().darker() );
            return c;
        }
    }
    
    /**
     * 
     * @return
     *  the current row lines
     */
    public double getCurrentRowLines() {
        return currentRowLines;
    }
    
    /**
     * 
     */
    public void destroy() {
        viewComponent = null;
    }
}
