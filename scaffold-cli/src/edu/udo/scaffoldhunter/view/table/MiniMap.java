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
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
import javax.swing.JViewport;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author Michael Hesse
 *
 */
public class MiniMap extends JPanel 
implements ChangeListener, ComponentListener, MouseListener, MouseMotionListener {

    private JPanel tableRepresentation;
    private JPanel floatingTableRepresentation;
    private JPanel viewportRepresentation;
    private ViewComponent tableViewComponent;
    
    /**
     * @param tableViewComponent 
     */
    public MiniMap(ViewComponent tableViewComponent) {
        
        super();
        this.tableViewComponent = tableViewComponent;
        setLayout(null);
        addComponentListener(this);
        tableViewComponent.getViewport().addChangeListener(this);
        setBackground(Color.WHITE);

        
        // set table representation
        tableRepresentation = new JPanel();
        tableRepresentation.setBorder( new LineBorder(Color.BLACK, 1));
        tableRepresentation.setBackground(Color.LIGHT_GRAY);
        tableRepresentation.setLayout( null );
        add(tableRepresentation);

        // set floating table representation
        floatingTableRepresentation = new JPanel();
        floatingTableRepresentation.setBorder( new LineBorder(Color.BLACK, 1));
        floatingTableRepresentation.setLayout(null);
        floatingTableRepresentation.addMouseListener(this);
        floatingTableRepresentation.addMouseMotionListener(this);
        tableRepresentation.add(floatingTableRepresentation);

        // set viewport representation
        viewportRepresentation = new JPanel();
        viewportRepresentation.setBackground(Color.PINK);
        viewportRepresentation.setBorder( new LineBorder(Color.BLACK, 1));
        floatingTableRepresentation.add(viewportRepresentation);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentResized(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentResized(ComponentEvent e) {
        stateChanged(new ChangeEvent(tableViewComponent.getViewport()));
    }
    
    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e) {
        if( e.getSource() == tableViewComponent.getViewport() ) {
            
            // fix the non-displaying-bug when this panel ist shown for the first time
            if(getWidth() <= 0)
                setSize(getPreferredSize());
            
            // get some values
            JViewport stickyViewport = tableViewComponent.getRowHeader();
            Dimension stickyViewSize = stickyViewport.getView().getSize();
            JViewport floatingViewport = tableViewComponent.getViewport();
            Dimension floatingViewSize = floatingViewport.getView().getSize();
            Dimension floatingVisibleSize = floatingViewport.getExtentSize();
            Point floatingVisiblePosition = floatingViewport.getViewPosition();
            Dimension tableRepresentationSize = new Dimension (
                    (stickyViewSize.width+floatingViewSize.width),
                    floatingViewSize.height);
            
            // calculate some values
            double maxWidth = getWidth() - 20;
            double maxHeight = getHeight() - 20;
            double scalefactor = maxWidth/tableRepresentationSize.width;
            if (tableRepresentationSize.height*scalefactor > maxHeight)
                scalefactor = maxHeight/tableRepresentationSize.height;
            int newHeight = (int)(floatingViewSize.height*scalefactor);
            int newFloatingWidth = (int)(floatingViewSize.width*scalefactor);
            int newTableWidth = newFloatingWidth + (int)(stickyViewSize.width*scalefactor);
            
            // set table size
            tableRepresentation.setSize(newTableWidth, newHeight);
            tableRepresentation.setLocation( (getWidth()-tableRepresentation.getWidth())/2, (getHeight()-newHeight)/2 );
            
            // set floating table size
            floatingTableRepresentation.setSize(newFloatingWidth, newHeight);
            floatingTableRepresentation.setLocation( newTableWidth-newFloatingWidth, 0 );
            
            // set viewportRepresentation size and position
            Dimension vprSize = new Dimension(
                    (int)(floatingVisibleSize.width*scalefactor),
                    (int)(floatingVisibleSize.height*scalefactor) );
            
            // avoid vanishing of the viewportRepresentation if it becomes too small
            viewportRepresentation.setBackground(Color.PINK);
            if ( vprSize.width < 3 ) {
                vprSize.width = 3;
                viewportRepresentation.setBackground(Color.RED);
            }
            if ( vprSize.height < 3 ) {
                vprSize.height = 3;
                viewportRepresentation.setBackground(Color.RED);
            }
            viewportRepresentation.setSize( vprSize );
            
            // set new viewportRepresentation location
            Point vprLocation = new Point( (int)(floatingVisiblePosition.x*scalefactor), (int)(floatingVisiblePosition.y*scalefactor) );
            if ( vprLocation.x+vprSize.width > floatingTableRepresentation.getWidth() )
                vprLocation.x = floatingTableRepresentation.getWidth()-vprSize.width;
            if ( vprLocation.y+vprSize.height> floatingTableRepresentation.getHeight() )
                vprLocation.y = floatingTableRepresentation.getHeight()-vprSize.height;
            
            viewportRepresentation.setLocation( vprLocation );
        }
    }

    /* (non-Javadoc)
     * catches mouseclicks in the tableRepresentation
     * and sets the viewport representation to the
     * clicked position
     * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
     */
    @Override
    public void mousePressed(MouseEvent e) {
        dragViewport(e.getPoint());
    }

    /* (non-Javadoc)
     * catches mousedraggs from the viewportRepresentation
     * and sets it to the new position
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e) {
        dragViewport(e.getPoint());
    }

    /**
     * @param position 
     */
    private void dragViewport(Point position) {
        JViewport viewport = tableViewComponent.getViewport();
        Dimension viewSize = viewport.getView().getSize();
        int tableWidth = floatingTableRepresentation.getWidth();
        int tableHeight = floatingTableRepresentation.getHeight();
        int viewportWidth = viewportRepresentation.getWidth();
        int viewportHeight = viewportRepresentation.getHeight();
        position.x -= (viewportWidth/2);
        position.y -= (viewportHeight/2);
        // set viewport position in real table
        position.x = (int)(viewSize.getWidth() * position.x / tableWidth);
        position.y = (int)(viewSize.getHeight() * position.y / tableHeight);
        if ( (position.x+viewport.getWidth()) > viewSize.width )
            position.x = viewSize.width - viewport.getWidth();
        if ( (position.y+viewport.getHeight()) > viewSize.height )
            position.y = viewSize.height - viewport.getHeight();
        position.x = (position.x<0 ? 0 : position.x);
        position.y = (position.y<0 ? 0 : position.y);
        viewport.setViewPosition(position);
    }

    /**
     * not implemented; overwritten as private to avoid the use of this constructor
     */
    @SuppressWarnings("unused")
    private MiniMap() {
        super();
    }

    /**
     * not implemented; overwritten as private to avoid the use of this constructor
     * @param arg0
     */
    @SuppressWarnings("unused")
    private MiniMap(LayoutManager arg0) {
        // currently not used
        super(arg0);
    }

    /**
     * not implemented; overwritten as private to avoid the use of this constructor
     * @param arg0
     */
    @SuppressWarnings("unused")
    private MiniMap(boolean arg0) {
        // currently not used
        super(arg0);
    }

    /**
     * not implemented; overwritten as private to avoid the use of this constructor
     * @param arg0
     * @param arg1
     */
    @SuppressWarnings("unused")
    private MiniMap(LayoutManager arg0, boolean arg1) {
        // currently not used
        super(arg0, arg1);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentHidden(ComponentEvent arg0) {
        // currently not used
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentMoved(ComponentEvent arg0) {
        // currently not used
    }

    /* (non-Javadoc)
     * @see java.awt.event.ComponentListener#componentShown(java.awt.event.ComponentEvent)
     */
    @Override
    public void componentShown(ComponentEvent arg0) {
        // currently not used
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseClicked(MouseEvent e) {
        // currently not used
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseEntered(MouseEvent e) {
        // currently not used
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseExited(MouseEvent e) {
        // currently not used
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseReleased(MouseEvent arg0) {
        // currently not used
    }

    /* (non-Javadoc)
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent arg0) {
        // currently not used
    }

    /**
     * 
     */
    public void destroy() {
        removeComponentListener(this);
        if(tableViewComponent != null)
            tableViewComponent.getViewport().removeChangeListener(this);
        tableViewComponent = null;
    }
}
