/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

package edu.udo.scaffoldhunter.view.treemap.sidebar;

import java.awt.BorderLayout;

import javax.swing.Box;
import javax.swing.JLabel;

import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.treemap.nodes.TreeMapNode;
import edu.udo.scaffoldhunter.view.util.AbstractPickPanel;

/**
 * Panel that shows what is under the mouse-pointer, created for TreeMap.
 * 
 * Adds a color and sizelabel and makes this a listener.
 * 
 * @author Lappie
 * 
 */
public class TreeMapPickPanel extends AbstractPickPanel implements TreeMapPickChangeListener {

    private JLabel sizeValue, colorMeanValue, colorPropertyValue;

    /**
     * Default settings, a title, SVG and size and color labels underneath.
     * 
     * @param db
     *            The databasemanager for loading the SVG's
     */
    public TreeMapPickPanel(DbManager db) {
        super(db);

        Box box = Box.createVerticalBox();
        sizeValue = new JLabel();
        colorMeanValue = new JLabel();
        colorPropertyValue = new JLabel();
        box.add(sizeValue);
        box.add(colorMeanValue);
        box.add(colorPropertyValue);
        contentPanel.add(box, BorderLayout.SOUTH);
    }
    
    private String formatValue(Double value) {
        if(value == null)
            return "-";
        if(value > 1 || value < -1)
            return String.format("%.2f", value);
        if(value > 0.01 || value < 0.01)
            return String.format("%.4f", value);
        return value + "";// if really small number, show it all. 
    }

    @Override
    public void pickChanged(TreeMapNode node) {
        svgPanel.setStructure(node == null || node.isVirtual() ? null : node.getStructure()); // root has no SVG

        if (node == null) {
            // clear everything
            cellInformation.setText("");
            sizeValue.setText("");
            colorMeanValue.setText("");
            colorPropertyValue.setText("");
        } else {
            cellInformation.setText(node.getTitleShort()); // use shorter title (only affects root nodes)
            sizeValue.setText(I18n.get("TreeMapView.Mappings.Size") + ": " + formatValue(node.getSizeValue()));
            colorMeanValue.setText(I18n.get("TreeMapView.Mappings.Color.Mean") + ": " + formatValue(node.getColorValue()));
            colorPropertyValue.setText(I18n.get("TreeMapView.Mappings.Color.Value") + ": " + formatValue(node.getColorPropertyValue()));
        }
    }
}
