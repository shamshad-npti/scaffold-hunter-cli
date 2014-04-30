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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Michael Hesse
 *
 */
public class CellZoom extends JPanel implements CellPickListener {

    // infotexts that are shown in the panel
    private JLabel cellInformation, cellPosition;
    
    // the content is shown in this panel. the contentPanel contains
    // a cardlayout which displays only one of it's children at a time
    private JPanel contentPanel;
    private JEditorPane textContentPanel;
    private JPanel emptyContentPanel;
    private ComponentRenderPanel componentContentPanel;

    
    /**
     * constructs a new cell zoom panel
     */
    public CellZoom() {
        super();
        setBackground(Color.WHITE);
        
        // create components
        cellInformation = new JLabel(" ");
        cellInformation.setForeground(Color.BLACK);
        cellPosition = new JLabel(" ");
        cellPosition.setForeground(Color.BLACK);
        contentPanel = new JPanel( new CardLayout(), true );
        contentPanel.setBackground(getBackground());
        contentPanel.setBorder( BorderFactory.createEmptyBorder(10, 0, 0, 0));
        textContentPanel = new JEditorPane();
        textContentPanel.setEditable(false);
        textContentPanel.setBackground(getBackground());
        componentContentPanel = new ComponentRenderPanel();
        componentContentPanel.setOpaque(false);
        componentContentPanel.setBackground(getBackground());
        emptyContentPanel = new JPanel( new GridLayout(1,1), true );
        emptyContentPanel.setOpaque(true);
        emptyContentPanel.setBackground(getBackground());
        contentPanel.add(emptyContentPanel, "empty");
        contentPanel.add(textContentPanel, "text");
        contentPanel.add(componentContentPanel, "component");
        Box horizontalBox = Box.createHorizontalBox();
        
        // stack the components together to get a nice layout
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        horizontalBox.add(cellInformation);
        horizontalBox.add(Box.createHorizontalGlue());
        horizontalBox.add(cellPosition);
        horizontalBox.setBorder(BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.gray));
        add(horizontalBox, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    
    /**
     * 
     * used to render other cell contents than text
     *
     */
    static class ComponentRenderPanel extends JPanel {
        
        Component componentToRender = null;
        
        public ComponentRenderPanel() {
            super( new GridLayout(1,1), true );
        }
        
        public void setComponentToRender(Component component) {
            componentToRender = component;
        }
        
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            // do we have a component to render?
            if(componentToRender != null) {
                
                Dimension dimension = getSize();
                componentToRender.setSize( dimension );
                componentToRender.doLayout();
                componentToRender.setBackground(getBackground());

                if ( componentToRender instanceof SVGCellRenderer ) {
                    // SVG content - nothing to do :)
                } else if ( componentToRender instanceof JPanel ) {
                    // Banner content - nothing to do :)
                } else {
                    // other content
                    /*
                    // try to scale component to full size
                     * 
                     * note: this became unneccessary, as the SVG can resize itself now
                     * 
                    if( g instanceof Graphics2D ) {
                        Dimension preferredSize = componentToRender.getPreferredSize();
                        double scaleFactor = ( ((double)dimension.width) / ((double)preferredSize.width) ); 
                        if( preferredSize.height*scaleFactor > dimension.height )
                            scaleFactor = ( ((double)dimension.width) / ((double)preferredSize.width) ); 
                        ((Graphics2D)g).scale(scaleFactor, scaleFactor);
                    }
                    */
                }
                
                componentToRender.paint(g);
            }
        }
    }




    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.table.CellPickListener#CellPickChanged(edu.udo.scaffoldhunter.view.table.ViewComponent, int, int, java.lang.String, java.lang.String, java.awt.Component)
     */
    @Override
    public void CellPickChanged(ViewComponent viewComponent, int row, int column, String structureTitle,
            String columnTitle, Component cellContent) {
        if( row == -1) {
            
            // mousecursor has left the table, so clear all displays
            
            cellInformation.setText(" ");
            cellPosition.setText(" ");
            ((CardLayout)contentPanel.getLayout()).show(contentPanel, "empty");

        } else {
            
            // set the information to display
            
            // clear structureTitle, if there is none
            if( structureTitle == null )
                cellInformation.setText(columnTitle);
            else {
                if( structureTitle.isEmpty() )
                    cellInformation.setText(columnTitle);
                else
                    cellInformation.setText(columnTitle + " "+I18n.get("TableView.CellZoom.For")+" " + structureTitle);
            }
            
            // set cell position
            cellPosition.setText("["+column+", "+row+"]");
            
            // set up appropriate content panel
            if( cellContent instanceof JTextComponent ) {
                textContentPanel.setText( ((JTextComponent)cellContent).getText());
                ((CardLayout)contentPanel.getLayout()).show(contentPanel, "text");
            } else {
                componentContentPanel.setComponentToRender(cellContent);
                ((CardLayout)contentPanel.getLayout()).show(contentPanel, "component");
            }
            contentPanel.repaint();
        }
        
    }
    
    
    /**
     * 
     */
    public void destroy() {
    }
}
