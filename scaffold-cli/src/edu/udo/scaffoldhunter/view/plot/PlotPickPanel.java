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

package edu.udo.scaffoldhunter.view.plot;

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.Box;
import javax.swing.JLabel;

import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.util.AbstractPickPanel;

/**
 * @author Michael Hesse
 *
 */
public class PlotPickPanel extends AbstractPickPanel implements PlotPickChangeListener {

    private JLabel xLabel, yLabel, zLabel, colorLabel, sizeLabel;
    
    /**
     * @param db 
     * 
     */
    public PlotPickPanel(DbManager db) {
        super(db);

        xLabel = new JLabel(" ");
        yLabel = new JLabel(" ");
        zLabel = new JLabel(" ");
        colorLabel = new JLabel(" ");
        sizeLabel = new JLabel(" ");
        xLabel.setVisible(false);
        yLabel.setVisible(false);
        zLabel.setVisible(false);
        colorLabel.setVisible(false);
        sizeLabel.setVisible(false);
        xLabel.setForeground(Color.BLACK);
        yLabel.setForeground(Color.BLACK);
        zLabel.setForeground(Color.BLACK);
        colorLabel.setForeground(Color.BLACK);
        sizeLabel.setForeground(Color.BLACK);
        
        Box box = Box.createVerticalBox();
        box.add(xLabel);
        box.add(yLabel);
        box.add(zLabel);
        box.add(colorLabel);
        box.add(sizeLabel);
        contentPanel.add(box, BorderLayout.SOUTH);
    }
    
    @Override
    public void pickChanged(DataPack dataPack) {
        svgPanel.setStructure(dataPack.structure);
        if(dataPack.structure == null) {
            // clear everything
            cellInformation.setText(" ");
            xLabel.setText(" ");
            yLabel.setText(" ");
            zLabel.setText(" ");
            colorLabel.setText(" ");
            sizeLabel.setText(" ");
        } else {
            // fill in the information
            cellInformation.setText(dataPack.structure.getTitle());

            // x
            if(dataPack.xTitle != null) {
                xLabel.setText(I18n.get("PlotView.Hyperplane.XAxisShortcut")+": " + dataPack.xValue);
                xLabel.setVisible(true);
            }
            else
                xLabel.setVisible(false);
            
            // y
            if(dataPack.yTitle != null) {
                yLabel.setText(I18n.get("PlotView.Hyperplane.YAxisShortcut")+": " + dataPack.yValue);
                yLabel.setVisible(true);
            }
            else
                yLabel.setVisible(false);

            // z
            if(dataPack.zTitle != null) {
                zLabel.setText(I18n.get("PlotView.Hyperplane.ZAxisShortcut")+": " + dataPack.zValue);
                zLabel.setVisible(true);
            }
            else
                zLabel.setVisible(false);

            // color
            if(dataPack.colorTitle != null) {
                colorLabel.setText(I18n.get("PlotView.Hyperplane.ColorAxisShortcut")+": " + dataPack.colorValue);
                colorLabel.setVisible(true);
            }
            else
                colorLabel.setVisible(false);

            // size
            if(dataPack.sizeTitle != null) {
                sizeLabel.setText(I18n.get("PlotView.Hyperplane.SizeAxisShortcut")+": " + dataPack.sizeValue);
                sizeLabel.setVisible(true);
            }
            else
                sizeLabel.setVisible(false);
        }
    }

}
