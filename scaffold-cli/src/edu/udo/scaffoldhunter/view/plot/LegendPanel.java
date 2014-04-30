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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.util.ColorDistribution;

/**
 * @author Michael Hesse
 *
 */
public class LegendPanel extends JPanel implements ModelChangeListener {
    
    /**
     * 
     * @author Micha
     *
     */
    class SizeDistribution extends JPanel {
        int startSize, endSize;
        double startValue, endValue;
        
        /**
         * 
         */
        public SizeDistribution() {
            startSize = 1;
            endSize = 20;
            startValue = 0.0;
            endValue = 1.0;
            setOpaque(false);
        }

        /**
         * 
         * @param startSize
         */
        public void setStartSize(int startSize) {
            this.startSize = startSize;
        }
        
        /**
         * 
         * @param endSize
         */
        public void setEndSize(int endSize) {
            this.endSize = endSize;
        }

        /**
         * 
         * @param startValue
         */
        public void setStartValue(double startValue) {
            this.startValue = startValue;
        }
        
        /**
         * 
         * @param endValue
         */
        public void setEndValue(double endValue) {
            this.endValue = endValue;
        }

        @Override
        public void paint(Graphics g) {
            super.paintComponent(g);

            // determine number of Boxes
            int boxCount = endSize - startSize;
            if( boxCount > 10) {
                boxCount /= 2;
            }
            boxCount++;
            
            // determine size of boxes
            FontMetrics metrics = g.getFontMetrics(g.getFont());
            double boxHeight = getHeight() / 10.0;
            double boxWidth = 10.0;
            int fontHeight = metrics.getHeight();
            double valueStep = (endValue - startValue) / boxCount;
            for(int i=0; i<=boxCount; i++) {
                double value = valueStep * i + valueStep/2 + startValue;
                String text = " "+value;
                int width = metrics.stringWidth(text);
                if( width > boxWidth )
                    boxWidth = width;
            }

            // now draw the texts
            for(int i=0; i<boxCount; i++) {
                int j = boxCount - 1 - i;
                double value = valueStep * i + valueStep/2 + startValue;
                String text;
                try {
                    text = numberFormatter.valueToString(value);
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    text = ""+value;
                }
                double xPos = 40;
                double yPos = boxHeight*0.3 + j*boxHeight;
                g.drawString(text, (int) xPos, (int) (yPos)+fontHeight/2);
            }

            // draw dots
            double dotStep = ((double)(endSize-startSize+1)) / ((double)boxCount);
            for(int i=0; i<boxCount; i++) {
                int j = boxCount - 1 - i;
                int dotSize = (int)( dotStep*i + startSize );
                int xPos = (int) (5 + boxHeight/2 - dotSize/2);
                int yPos = (int) (boxHeight/2 + j*boxHeight - dotSize/2);
                g.setColor(Color.BLUE);
                g.fillOval(xPos, yPos, dotSize, dotSize);
            }
            
        }
    }


    
    
    
    
    
    JLabel xLabel, yLabel, zLabel, colorLabel, sizeLabel;
    Box colorPanel, sizePanel;
    ColorDistribution colorDistribution;
    SizeDistribution sizeDistribution;
    NumberFormatter numberFormatter = new NumberFormatter();
    
    
    /**
     * 
     */
    public LegendPanel() {
        super();
        
        { // create xPanel
            xLabel = new JLabel(" ");
            xLabel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
            xLabel.setVisible(false);
        }
        { // create yPanel
            yLabel = new JLabel(" ");
            yLabel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
            yLabel.setVisible(false);
        }
        { // create zPanel
            zLabel = new JLabel(" ");
            zLabel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
            zLabel.setVisible(false);
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
            colorPanel.setBorder( BorderFactory.createEmptyBorder(20, 0, 10, 0));
            colorDistribution.setSegments(10);
        }
        { // create size panel
            sizePanel = Box.createVerticalBox();
            sizeLabel = new JLabel(" ");
            sizeLabel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
            sizePanel.add(sizeLabel);
            sizeDistribution = new SizeDistribution();
            sizePanel.add(sizeDistribution);
            sizeDistribution.setPreferredSize( new Dimension( 100, 200 ));
            sizePanel.setVisible(false);
            sizePanel.setBorder( BorderFactory.createEmptyBorder(20, 0, 10, 0));
        }

        setOpaque(false);
        setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));
        Box box = Box.createVerticalBox();
        box.add( xLabel );
        box.add( yLabel );
        box.add( zLabel );
        box.add( colorPanel );
        box.add( sizePanel );
        setLayout(new GridLayout(1,1));
        add(box);
    }

    
    
    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.ModelChangeListener#modelChanged(edu.udo.scaffoldhunter.view.plot.Model, int, boolean)
     */
    @Override
    public void modelChanged(Model model, int channel, boolean moreToCome) {
        switch(channel) {
        case PlotPanel3D.X_CHANNEL:
            if( model.hasData(channel) ) {
                String text = "<html><b><u>"+I18n.get("PlotView.Mappings.XAxis")+"</u></b><br>" +
                                model.getChannelTitle(channel) + "<br>" +
                                I18n.get("PlotView.Legend.Domain")+": " + model.getDataMin(channel) + " - " +
                                model.getDataMax(channel) + "</html>";
                xLabel.setText(text);
                xLabel.setVisible(true);
            } else {
                xLabel.setText(" ");
                xLabel.setVisible(false);
            }
            break;
        case PlotPanel3D.Y_CHANNEL:
            if( model.hasData(channel) ) {
                String text = "<html><b><u>"+I18n.get("PlotView.Mappings.YAxis")+"</u></b><br>" +
                                model.getChannelTitle(channel) + "<br>" +
                                I18n.get("PlotView.Legend.Domain")+": " + model.getDataMin(channel) + " - " +
                                model.getDataMax(channel) + "</html>";
                yLabel.setText(text);
                yLabel.setVisible(true);
            } else {
                yLabel.setText(" ");
                yLabel.setVisible(false);
            }
            break;
        case PlotPanel3D.Z_CHANNEL:
            if( model.hasData(channel) ) {
                String text = "<html><b><u>"+I18n.get("PlotView.Mappings.ZAxis")+"</u></b><br>" +
                                model.getChannelTitle(channel) + "<br>" +
                                I18n.get("PlotView.Legend.Domain")+": " + model.getDataMin(channel) + " - " +
                                model.getDataMax(channel) + "</html>";
                zLabel.setText(text);
                zLabel.setVisible(true);
            } else {
                zLabel.setText(" ");
                zLabel.setVisible(false);
            }
            break;
        case PlotPanel3D.COLOR_CHANNEL:
            if( model.hasData(channel) ) {
                String text = "<html><b><u>"+I18n.get("PlotView.DotColor")+"</u></b><br>" +
                                model.getChannelTitle(channel) + "<br>" +
                                I18n.get("PlotView.Legend.Domain")+": " + model.getDataMin(channel) + " - " +
                                model.getDataMax(channel) + "</html>";
                colorLabel.setText(text);
                colorDistribution.setStartValue( model.getDataMin(channel), false );
                colorDistribution.setEndValue( model.getDataMax(channel), true );
                colorPanel.setVisible(true);
            } else {
                colorLabel.setText(" ");
                colorDistribution.setStartValue( 0.0, true );
                colorDistribution.setEndValue( 1.0, true );
                colorPanel.setVisible(false);
            }
            colorPanel.repaint();
            break;
        case PlotPanel3D.SIZE_CHANNEL:
            if( model.hasData(channel) ) {
                String text = "<html><b><u>"+I18n.get("PlotView.DotSize")+"</u></b><br>" +
                                model.getChannelTitle(channel) + "<br>" +
                                I18n.get("PlotView.Legend.Domain")+": " + model.getDataMin(channel) + " - " +
                                model.getDataMax(channel) + "</html>";
                sizeLabel.setText(text);
                sizeDistribution.setStartValue( model.getDataMin(channel) );
                sizeDistribution.setEndValue( model.getDataMax(channel) );
                sizePanel.setVisible(true);
            } else {
                sizeLabel.setText(" ");
                sizeDistribution.setStartValue( 0.0 );
                sizeDistribution.setEndValue( 1.0 );
                sizePanel.setVisible(false);
            }
            sizePanel.repaint();
            break;
        }
    }
    
    
    /**
     * 
     * @param startColor
     */
    public void setStartColor(Color startColor) {
        colorDistribution.setStartColor(startColor, true);
        colorPanel.repaint();
    }

    /**
     * 
     * @param endColor
     */
    public void setEndColor(Color endColor) {
        colorDistribution.setEndColor(endColor, true);
        colorPanel.repaint();
    }

    /**
     * 
     * @param startSize
     */
    public void setStartSize(int startSize) {
        sizeDistribution.setStartSize(startSize);
        sizePanel.repaint();
    }

    /**
     * 
     * @param endSize
     */
    public void setEndSize(int endSize) {
        sizeDistribution.setEndSize(endSize);
        sizePanel.repaint();
    }

}
