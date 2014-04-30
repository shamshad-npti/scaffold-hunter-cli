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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.util.CommentComponent;

// TODO: translate texts


/**
 * @author Michael Hesse
 *
 */
public class ContextMenuMouseAdapter extends MouseAdapter {

    private final DbManager db;

    Component owner;
    JPopupMenu contextMenu;
    
    JMenuItem commentMenuItem;
    JCheckBoxMenuItem togglePrivateBannerMenuItem, togglePublicBannerMenuItem;
    JMenuItem setPublicBannerForSelectionMenuItem, clearPublicBannerForSelectionMenuItem;
    JMenuItem setPrivateBannerForSelectionMenuItem, clearPrivateBannerForSelectionMenuItem;
    JMenuItem scrollToSelectionMenuItem;
    
    Molecule molecule;
    Subset subset;
    
    int tableRow, tableColumn;
    
    
    /**
     * @param db 
     * @param owner
     *   the component on which the contextmenu should appear
     */
    public ContextMenuMouseAdapter( DbManager db, Component owner ) {
        this.db = db;
        this.owner = owner;
        contextMenu = new JPopupMenu();
        
        // dealing with banners
        togglePublicBannerMenuItem = new JCheckBoxMenuItem("...");
        togglePrivateBannerMenuItem = new JCheckBoxMenuItem("...");
        togglePublicBannerMenuItem.setAction( new togglePublicBannerAction() );
        togglePrivateBannerMenuItem.setAction( new togglePrivateBannerAction() );
        contextMenu.add( togglePublicBannerMenuItem);
        contextMenu.add( togglePrivateBannerMenuItem);
        setPublicBannerForSelectionMenuItem = new JMenuItem(I18n.get("Selection.SetPublicBannersForSelection"));
        clearPublicBannerForSelectionMenuItem = new JMenuItem(I18n.get("Selection.ClearPublicBannersForSelection"));
        setPrivateBannerForSelectionMenuItem = new JMenuItem(I18n.get("Selection.SetPrivateBannersForSelection"));
        clearPrivateBannerForSelectionMenuItem = new JMenuItem(I18n.get("Selection.ClearPrivateBannersForSelection"));
        //   contextMenu.add( setPublicBannerForSelectionMenuItem );
        //   contextMenu.add( clearPublicBannerForSelectionMenuItem );
        //   contextMenu.add( setPrivateBannerForSelectionMenuItem );
        //   contextMenu.add( clearPrivateBannerForSelectionMenuItem );
  //      contextMenu.addSeparator();
  //      scrollToSelectionMenuItem = new JMenuItem(I18n.get("TableView.ToolBar.ScrollToSelection"));
  //      scrollToSelectionMenuItem.setAction( new ScrollToSelectionAction() );
  //      contextMenu.add( scrollToSelectionMenuItem );
     //   contextMenu.add( setPublicBannerForSelectionMenuItem );
     //   contextMenu.add( clearPublicBannerForSelectionMenuItem );
     //   contextMenu.add( setPrivateBannerForSelectionMenuItem );
     //   contextMenu.add( clearPrivateBannerForSelectionMenuItem );
        
        contextMenu.addSeparator();

        // create menu item for writing comments
        commentMenuItem = new JMenuItem("...");
        commentMenuItem.setAction( new EditCommentAction() );

        contextMenu.add( commentMenuItem );
    }
    
    
    
    
    private void showPopup(MouseEvent e) {
        if (e.isPopupTrigger() && owner.isEnabled()) {
            if( shouldShow(e.getX(), e.getY()) ) {
                Molecule molecule = getRelatedMolecule(e.getX(), e.getY());
                Subset subset = getRelatedSubset();
                if( (molecule  != null) & (subset != null) ) {
                    this.molecule = molecule;
                    this.subset = subset;
                    boolean privateBanner = false;
                    boolean publicBanner = false;
                    BannerManager bm = ((Model)((ViewTable)owner).getModel()).getBannerManager();
                    if( bm.getPublicBanner(molecule) == BannerManager.BannerState.PUBLIC )
                        publicBanner = true;
                    if( bm.getPrivateBanner(molecule) == BannerManager.BannerState.PRIVATE )
                        privateBanner = true;
                    togglePublicBannerMenuItem.setText(
                            I18n.get("Banner.TogglePublicBanner")+" ("+
                            I18n.get("TableView.CellZoom.For")+" "+
                            molecule.getTitle()+
                            ")");
                    togglePublicBannerMenuItem.setSelected(publicBanner);
                    togglePrivateBannerMenuItem.setText(
                            I18n.get("Banner.TogglePrivateBanner")+" ("+
                            I18n.get("TableView.CellZoom.For")+" "+
                            molecule.getTitle()+
                            ")");
                    togglePrivateBannerMenuItem.setSelected(privateBanner);
                    commentMenuItem.setText(
                            I18n.get("TableView.Menu.EditComment")+" ("+
                            I18n.get("TableView.CellZoom.For")+" "+
                            molecule.getTitle()+
                            ")");
                    contextMenu.show(owner, e.getX(), e.getY());
                }
            }
        }
    }

    
    
    // =========================================================



    class EditCommentAction extends AbstractAction {

        public EditCommentAction() {
            super("edit comment");
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            JFrame f = new JFrame(
                    I18n.get("TableView.Menu.EditComment")+" "+
                    I18n.get("TableView.CellZoom.For")+" "+
                    molecule.getTitle());
            Component cc = new CommentComponent(db, molecule, subset.getSession().getProfile());
            JScrollPane spc = new JScrollPane(cc);
            f.setLayout(new BorderLayout());
            f.add(spc, BorderLayout.CENTER);
            spc.setPreferredSize( cc.getPreferredSize() );
            f.setMinimumSize( new Dimension(10, 400));
            f.pack();
            f.setVisible(true);
        }
    }

    
    /**
     * 
     * @author Michael Hesse
     *
     */
    class togglePublicBannerAction extends AbstractAction {

        public togglePublicBannerAction() {
            super(I18n.get("Banner.TogglePublicBanner"));
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            BannerManager bm = ((Model)((ViewTable)owner).getModel()).getBannerManager();
            BannerManager.BannerState state = bm.getPublicBanner(molecule);
            if( state == BannerManager.BannerState.PUBLIC ) {
                bm.setPublicBanner(molecule, BannerManager.BannerState.CLEARED);
            } else if( state == BannerManager.BannerState.CLEARED ) {
                bm.setPublicBanner(molecule, BannerManager.BannerState.PUBLIC);
            }
            //((Model)((ViewTable)owner).getModel()).fireTableRowsUpdated(tableRow, tableRow);
        }
    }

    /**
     * 
     * @author Michael Hesse
     *
     */
    class togglePrivateBannerAction extends AbstractAction {

        public togglePrivateBannerAction() {
            super(I18n.get("Banner.TogglePublicBanner"));
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            BannerManager bm = ((Model)((ViewTable)owner).getModel()).getBannerManager();
            BannerManager.BannerState state = bm.getPrivateBanner(molecule);
            if( state == BannerManager.BannerState.PRIVATE ) {
                bm.setPrivateBanner(molecule, BannerManager.BannerState.CLEARED);
            } else if( state == BannerManager.BannerState.CLEARED ) {
                bm.setPrivateBanner(molecule, BannerManager.BannerState.PRIVATE);
            }
            //((Model)((ViewTable)owner).getModel()).fireTableRowsUpdated(tableRow, tableRow);
        }
    }

    /**
     * 
     * @author Michael Hesse
     *
     */
    class ScrollToSelectionAction extends AbstractAction {

        public ScrollToSelectionAction() {
            super(I18n.get("TableView.ToolBar.ScrollToSelection"));
        }
        
        /* (non-Javadoc)
         * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            int row = ((ViewTable)owner).getSelectedRow();
            if(row != -1) {
                row = ((ViewTable)owner).convertRowIndexToModel(row);
                Molecule m = ((Model)((ViewTable)owner).getModel()).getMoleculeAtRow(row);
                ((ViewTable)owner).ensureStructureIsVisible(m);
            }
        }
    }
    
    
    // ==============================================
    
    
    @Override
    public void mousePressed(MouseEvent e) {
        showPopup(e);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        showPopup(e);
    }
    

    /**
     * decides if the contextMenu should be shown at this position
     * 
     * @param xPos
     *   position of the mouseclick, taken from the MouseEvent
     * @param yPos
     *   position of the mouseclick, taken from the MouseEvent
     * @return 
     *   true if the contextmenu should be shown, false otherwise
     */
    public boolean shouldShow(int xPos, int yPos) {
        boolean shouldShow = false;
        ViewTable table = (ViewTable) owner;
        Point p = new Point(xPos, yPos);
        int column = table.convertColumnIndexToModel(table.columnAtPoint(p) );
        int row = table.convertRowIndexToModel(table.rowAtPoint(p));

        if ( (row >= 0 && row < table.getRowCount())
                & (column >= 0 && column < table.getColumnCount()) ) {
            if (contextMenu != null
                && contextMenu.getComponentCount() > 0)
                shouldShow = true;
        }
        return shouldShow;
    }
    
    
    /**
     * this method should deliver a structure, for which the context
     * menu adapts.
     *  
     * @param xPos 
     *   xPos from the MouseEvent
     * @param yPos 
     *   yPos from the MouseEvent
     * @return
     *   the structure, for which the contextmenu adapts. if the return
     *   value is null then no contextmenu will be shown
     */
    public Molecule getRelatedMolecule(int xPos, int yPos) {
        
        ViewTable table = (ViewTable) owner;
        Point p = new Point(xPos, yPos);
        tableColumn = table.convertColumnIndexToModel(table.columnAtPoint(p) );
        tableRow = table.convertRowIndexToModel(table.rowAtPoint(p));
        Molecule m = null;
        
        if ( (tableRow >= 0 && tableRow < table.getRowCount())
                & (tableColumn >= 0 && tableColumn < table.getColumnCount()) ) {
            if (contextMenu != null
                && contextMenu.getComponentCount() > 0) {
                m = ((Model)table.getModel()).getMoleculeAtRow(tableRow);
            }
        }

        return m;
    }

    
    /**
     * @return
     *  the subset that the owner-component of this subset uses.
     *  neccessary for querieing the banners. the whole shebang 
     *  will most probably result in a NullPointerException if 
     *  this method returns null.
     */
    public Subset getRelatedSubset() {
        ViewTable table = (ViewTable) owner;
        return ((Model)table.getModel()).getSubset();
    }

}
