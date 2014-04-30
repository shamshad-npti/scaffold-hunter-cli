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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.util.ColorDistribution;

/**
 * @author Sven Schrinner
 *
 */
public class SortLegendPanel extends JPanel {

    private JLabel propertyLabel, cumulationLabel, optionsLabel, colorLabel;
    private Box colorPanel;
    private ColorDistribution colorDistribution;
    private final SortState sortState;
    
    /**
     * Creates a new panel used for the sorting legend in the sidebar. This panel requires a {@link Sorting} object, 
     * which contains the information about the current sorting.
     * @param sorting the {@link Sorting} object
     */
    public SortLegendPanel(Sorting sorting) {
        super();
        sortState = sorting.getSortState();
        
        { // create propertyPanel
            propertyLabel = new JLabel(" ");
            propertyLabel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
            propertyLabel.setVisible(false);
        }
        { // create cumulationPanel
            cumulationLabel = new JLabel(" ");
            cumulationLabel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
            cumulationLabel.setVisible(false);
        }
        { // create optionsPanel
            optionsLabel = new JLabel(" ");
            optionsLabel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
            optionsLabel.setVisible(false);
        }
        { // create color panel
            colorPanel = Box.createVerticalBox();
            colorLabel = new JLabel(" ");
            colorLabel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
            colorPanel.add(colorLabel);
            colorDistribution = new ColorDistribution();
            colorPanel.add(colorDistribution);
            colorDistribution.setPreferredSize( new Dimension( 100, 200 ));
            colorPanel.setVisible(false);
        }
        
        setOpaque(false);
        setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Box box = Box.createVerticalBox();
        box.add( propertyLabel );
        box.add( cumulationLabel );
        box.add( optionsLabel );
        box.add( colorPanel );
        setLayout(new GridLayout(1,1));
        add(box);
    }

    
    /**
     * Updates the displayed information about the current sorting. Repaints the color legend.
     */
    public void refresh() {
        if( sortState.getSortSettings().getPropDefTitle() != null ) {
            String text = "<html><b><u>"+I18n.get("ScaffoldTreeView.Sort.SortedBy")+"</u></b><br>" +
                    sortState.getSortSettings().getPropDefTitle() +
                    " ("+(sortState.getSortSettings().isDescending()?I18n.get("VisualMappings.Gradient.Descending"):I18n.get("VisualMappings.Gradient.Ascending"))+")" +
                    " "+"<br>" + "</html>";
            propertyLabel.setText(text);
            propertyLabel.setVisible(true);
        } else {
            propertyLabel.setText(I18n.get("ScaffoldTreeView.Sort.NoSorting"));
            propertyLabel.setVisible(true);
        }
        
        if(sortState.getSortSettings().getPropDefTitle() == null) {
            cumulationLabel.setText(" ");
            cumulationLabel.setVisible(false);
        }
        else if( sortState.getSortSettings().isCumulative() ) {
            String text = "<html><b><u>"+I18n.get("ScaffoldTreeView.Sort.Accumulation")+"</u></b><br>" +
                    sortState.getSortSettings().getFunction().toString() +
                    " (" + I18n.get("ScaffoldTreeView.Sort.Cumulative") + ") "+"</html>";
            cumulationLabel.setText(text);
            cumulationLabel.setVisible(true);
        } else {
            String text = "<html><b><u>"+I18n.get("ScaffoldTreeView.Sort.Accumulation")+"</u></b><br>" +
                    sortState.getSortSettings().getFunction().toString() +"</html>";
            cumulationLabel.setText(text);
            cumulationLabel.setVisible(true);
        }
                
        if(sortState.getSortSettings().getPropDefTitle() != null && sortState.getSortSettings().isColorSegments()) {
            if(sortState.getSortSettings().isAddCaption())
                optionsLabel.setText("<html><b><u>"+I18n.get("ScaffoldTreeView.Sort.Options")+"</u></b><br>" + 
                        I18n.get("ScaffoldTreeView.Sort.ColorSegments")+", "+I18n.get("ScaffoldTreeView.Sort.AddCaption") + "</html>");
            else
                optionsLabel.setText("<html><b><u>"+I18n.get("ScaffoldTreeView.Sort.Options")+"</u></b><br>" +
                        I18n.get("ScaffoldTreeView.Sort.ColorSegments") + "</html>");
            optionsLabel.setVisible(true);
        }
        else {
            optionsLabel.setText(" ");
            optionsLabel.setVisible(false);
        }
        
        if(sortState.getSortSettings().getPropDefTitle() != null && sortState.getSortSettings().isColorSegments()) {
            colorDistribution.setColorIntervalsAsGradients(sortState.getValues(), sortState.getColors());
            if(sortState.getValues() != null && sortState.getColors() != null) {
                colorPanel.setVisible(true);
                colorPanel.repaint();
            }
            else
                colorPanel.setVisible(false);
        }
        else {
            colorPanel.setVisible(false);
        }
    }
}
