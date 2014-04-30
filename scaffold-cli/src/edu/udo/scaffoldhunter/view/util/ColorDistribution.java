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

package edu.udo.scaffoldhunter.view.util;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.text.NumberFormatter;

/**
 * @author Michael Hesse
 * @author Sven Schrinner
 *
 */
public class ColorDistribution extends JPanel {
    private NumberFormatter numberFormatter;    
    private ArrayList<ColorInterval> colorIntervals;
    private int segments;
    
    /**
     * Creates a new object, with white start color, black end color, 0.0 as start value and 1.0 as end value. Intervals is set to false by default.
     */
    public ColorDistribution() {        
        numberFormatter = new NumberFormatter();        
        colorIntervals = new ArrayList<ColorInterval>();
        colorIntervals.add(new ColorInterval(0.0, 1.0, Color.white, Color.black));
    }    

    @Override
    public void paint(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        
        assert(colorIntervals != null);
        
        // determine height of boxes
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        double boxHeight = getHeight() / (colorIntervals.size() + 1);
        double boxWidth = 10.0;
        int fontHeight = metrics.getHeight();
        for(ColorInterval c : colorIntervals) {
            String text = " "+c.getStartValue();
            int width = metrics.stringWidth(text);
            if( width > boxWidth)
                boxWidth = width;
            
            text = " "+c.getEndValue();
            width = metrics.stringWidth(text);
            if( width > boxWidth)
                boxWidth = width;
        }

        // outer texts
        if(colorIntervals.size() > 0) {
            String text = toString(colorIntervals.get(0).getStartValue());        
            double xPos = 50;
            double yPos = boxHeight* (0.5);
            g.drawString(text, (int) xPos, (int) ((yPos)+fontHeight*0.3));
            g.drawLine( 35, (int) yPos, 42, (int) yPos);
        
            text = toString(colorIntervals.get(colorIntervals.size() - 1).getEndValue());            
            xPos = 50;
            yPos = boxHeight* (colorIntervals.size() + 0.5);
            g.drawString(text, (int) xPos, (int) ((yPos)+fontHeight*0.3));
            g.drawLine( 35, (int) yPos, 42, (int) yPos);
            
            // inner texts
            for(int i = 1; i < colorIntervals.size(); i++) { 
                xPos = 50;
                yPos = boxHeight* (0.5 + i);
                if(colorIntervals.get(i).getStartValue() == colorIntervals.get(i-1).getEndValue() || 
                        colorIntervals.get(i).getStartValue() != colorIntervals.get(i).getStartValue() ||
                                colorIntervals.get(i-1).getEndValue() != colorIntervals.get(i-1).getEndValue()) {
                    // draws one number when adjacent borders are equal
                    text = toString(colorIntervals.get(i).getStartValue());                                
                    g.drawString(text, (int) xPos, (int) ((yPos)+fontHeight*0.3));
                    g.drawLine( 35, (int) yPos, 42, (int) yPos);
                }
                else {
                    // draws two numbers when intervals are not continuous
                    String textLow = toString(colorIntervals.get(i-1).getEndValue());
                    String textUp = toString(colorIntervals.get(i).getStartValue());                
                    g.drawString(textUp, (int) xPos, (int) ((yPos)+fontHeight*0.80));
                    g.drawString(textLow, (int) xPos, (int) ((yPos)-fontHeight*0.1));
                    g.drawLine( 5, (int) yPos, 82, (int) yPos);
                }
                
            }
        }
        
        // draw gradient rectangles
        for(int i = 0; i < colorIntervals.size(); i++) { 
            Rectangle2D rect = new Rectangle2D.Double( 5, (0.5 + i)*boxHeight, 30, boxHeight);
            GradientPaint gp;
            gp = new GradientPaint (
                    0, (float)((0.5 + i)*boxHeight),  colorIntervals.get(i).getStartColor(),
                    0, (float)((1.5 + i)*boxHeight),  colorIntervals.get(i).getEndColor());
            g2d.setPaint(gp);
            g2d.fill(rect);
        }
        Rectangle2D outerRect = new Rectangle2D.Double( 5, boxHeight*(0.5), 30, colorIntervals.size()*boxHeight);
        g2d.setColor(Color.BLACK);
        g2d.draw(outerRect);
    }   
    
    private String toString(double d) {
        String text;
        if(d != d)
            text = "NaN";
        else {
            try {
                text = numberFormatter.valueToString(d);
            } catch (ParseException e) {
                e.printStackTrace();
                text = ""+d;
            }
        }
        return text;
    }
    
    private Color getInterpolatedColor(Color color1, Color color2, double weight) {
        return new Color(
                (int)(weight * color1.getRed() + (1-weight) * color2.getRed()),
                (int)(weight * color1.getGreen() + (1-weight) * color2.getGreen()),
                (int)(weight * color1.getBlue() + (1-weight) * color2.getBlue())
        );
    }
    
    /**
     * Returns a list of color intervals, which are used for rendering this element.
     * @return the list of color intervals
     */
    public ArrayList<ColorInterval> getColorIntervals() {
        return colorIntervals;
    }
    
    /**
     * Sets the internally used list of color intervals.
     * @param intervals the new list
     */
    public void setColorIntervals(ArrayList<ColorInterval> intervals) {
        this.colorIntervals = intervals;
    }
    
    
    /**
     * Returns the number of segments of the {@link ColorDistribution}. Each segment consists of a single {@link ColorInterval}.
     * @return the number of segments
     */
    public int getSegments() {
        return segments;
    }

    /**
     * Sets the number of segments of the {@link ColorDistribution}. This method will delete all {@link ColorInterval} objects from the colorIntervals list
     * and create new ColorIntervals. The previous start color and value will be used for the first ColorInterval, while the previous end color and value
     * will be used for the last ColorInterval. All interval borders in between will be calculated by linear interpolation.
     * This method should only be used to initially specify the number of segments of the ColorDistribution.
     * @param segments the number of segments to be created
     */
    public void setSegments(int segments) {        
        this.segments = segments;
        double startValue = colorIntervals.get(0).getStartValue();
        double endValue = colorIntervals.get(colorIntervals.size() - 1).getEndValue();
        Color startColor = colorIntervals.get(0).getStartColor();
        Color endColor = colorIntervals.get(colorIntervals.size() - 1).getEndColor();
        colorIntervals.clear();
        for(int i = 0; i < segments; i++)
            colorIntervals.add(new ColorInterval(startValue, endValue, startColor, endColor));
        setStartValue(startValue, false);
        setEndValue(startValue, true);
        setStartColor(startColor, false);
        setEndColor(endColor, true);
    }

    /**
     * Changes the start value of the first {@link ColorInterval}. If distributeIntervals is true, the borders of the ColorIntervals are redistributed
     * in the following way: The first ColorInterval keeps its start value and the last ColorInterval keeps its end value. All interval borders 
     * in between are calculated by linear interpolation.
     * @param startValue the start value for the first interval
     * @param distributeIntervals if true, then the interval borders are rearranged
     */
    public void setStartValue(double startValue, boolean distributeIntervals) {
        if(distributeIntervals) {
            double endValue = colorIntervals.get(colorIntervals.size() - 1).getEndValue();
            for(int i = 0; i < colorIntervals.size(); i++) {
                colorIntervals.get(i).setStartValue(startValue + i * (endValue - startValue) / colorIntervals.size());
                colorIntervals.get(i).setEndValue(startValue + (i+1) * (endValue - startValue) / colorIntervals.size());
            }
        }
        else
            colorIntervals.get(0).setStartValue(startValue);
    }
    
    /**
     * Changes the end value of the last {@link ColorInterval}. If distributeIntervals is true, the borders of the ColorIntervals are redistributed
     * in the following way: The first ColorInterval keeps its start value and the last ColorInterval keeps its end value. All interval borders 
     * in between are calculated by linear interpolation.
     * @param endValue the end value for the last interval
     * @param distributeIntervals if true, then the interval borders are rearranged
     */
    public void setEndValue(double endValue, boolean distributeIntervals) {
        if(distributeIntervals) {
            double startValue = colorIntervals.get(0).getStartValue();
            for(int i = 0; i < colorIntervals.size(); i++) {
                colorIntervals.get(i).setStartValue(startValue + i * (endValue - startValue) / colorIntervals.size());
                colorIntervals.get(i).setEndValue(startValue + (i+1) * (endValue - startValue) / colorIntervals.size());
            }
        }
        else
            colorIntervals.get(colorIntervals.size() - 1).setEndValue(endValue);
    }
    
    /**
     * Changes the start color of the first {@link ColorInterval}. If distributeIntervals is true, the colors of the ColorIntervals are recalculated
     * in the following way: The first ColorInterval keeps its start color and the last ColorInterval keeps its end color. All colors
     * in between are calculated by linear interpolation.
     * @param startColor the start color for the first interval
     * @param distributeIntervals if true, then the interval colors are rearranged
     */
    public void setStartColor(Color startColor, boolean distributeIntervals) {
        if(distributeIntervals) {
            Color endColor = colorIntervals.get(colorIntervals.size() - 1).getEndColor();
            for(int i = 0; i < colorIntervals.size(); i++) {
                colorIntervals.get(i).setStartColor(getInterpolatedColor(startColor, endColor, (double)(colorIntervals.size() - i) / colorIntervals.size()));                
                colorIntervals.get(i).setEndColor(getInterpolatedColor(startColor, endColor, (double)(colorIntervals.size() - i - 1) / colorIntervals.size()));
            }
        }
        else
            colorIntervals.get(0).setStartColor(startColor);
    }
    
    /**
     * Changes the end color of the last {@link ColorInterval}. If distributeIntervals is true, the colors of the ColorIntervals are recalculated
     * in the following way: The first ColorInterval keeps its start color and the last ColorInterval keeps its end color. All colors
     * in between are calculated by linear interpolation.
     * @param endColor the end color for the last interval
     * @param distributeIntervals if true, then the interval colors are rearranged
     */
    public void setEndColor(Color endColor, boolean distributeIntervals) {
        if(distributeIntervals) {
            Color startColor = colorIntervals.get(0).getStartColor();
            for(int i = 0; i < colorIntervals.size(); i++) {
                colorIntervals.get(i).setStartColor(getInterpolatedColor(startColor, endColor, (double)(colorIntervals.size() - i) / colorIntervals.size()));
                colorIntervals.get(i).setEndColor(getInterpolatedColor(startColor, endColor, (double)(colorIntervals.size() - i - 1) / colorIntervals.size()));
            }
        }
        else
            colorIntervals.get(colorIntervals.size() - 1).setEndColor(endColor);
    }
    
    /**
     * Sets the list of intervals by generating {@link ColorInterval} objects from the given lists. The i-th element of the given numeric value list will 
     * become the start value of the i-th ColorInterval and the end value of the (i-1)-th ColorInterval. The colors are applied analogously.
     * Note that the resulting list of ColorIntervals has one element less than the parameter lists.
     * If the parameter list do not have equal size, the longer list will be cut.
     * @param values the numeric values for interval borders
     * @param colors the colors for the created ColorIntervals
     */
    public void setColorIntervalsAsGradients(ArrayList<Double> values, ArrayList<Color> colors) {
        if(values == null || colors == null)
            return;
        assert(values.size() == colors.size());
        
        colorIntervals.clear();        
        int size = Math.min(colors.size() - 1, colors.size() - 1);            
        for(int i = 0; i < size; i++)
            colorIntervals.add(new ColorInterval(values.get(i) == null?Double.NaN:values.get(i), 
                                            values.get(i+1) == null?Double.NaN:values.get(i+1),
                                            colors.get(i), 
                                            colors.get(i+1)));
    }
    
    /**
     * Sets the list of intervals by generating {@link ColorInterval} objects from the given lists. The i-th element of the given numeric value list will 
     * become the start value of the i-th ColorInterval and the end value of the (i-1)-th ColorInterval. The i-th color of the parameter list will both
     * become start and end color for the i-th ColorInterval. The list of numeric values must have one element more than the list of colors, otherwise
     * the longer list will be cut.
     * @param values the numeric values for interval borders
     * @param colors the colors for the created ColorIntervals
     */
    public void setColorIntervalsAsIntervals(ArrayList<Double> values, ArrayList<Color> colors) {
        if(values == null || colors == null)
            return;            
        assert(values.size() == colors.size() + 1);
        
        colorIntervals.clear();
        int size = Math.min(colors.size(), colors.size() - 1);            
        for(int i = 0; i < size; i++)
            colorIntervals.add(new ColorInterval(values.get(i) == null?Double.NaN:values.get(i), 
                                            values.get(i) == null?Double.NaN:values.get(i), 
                                            colors.get(i), 
                                            colors.get(i)));
    }
    
    /**
     * @author Sven Schrinner
     *
     */
    public static class ColorInterval {
        
        private double startValue;
        private double endValue;
        private Color startColor;
        private Color endColor;
        
        /**
         * Creates a new ColorInterval with the startValue as lower bound and endValue as upper bound. 
         * Also assigns the startColor to the lower bound and the endColor to the upper bound.
         * @param startValue the lower bound of the interval
         * @param endValue the upper bound of the interval
         * @param startColor the color for the lower bound
         * @param endColor the color for the upper bound
         */
        public ColorInterval(double startValue, double endValue, Color startColor, Color endColor) {
            this.startValue = startValue;
            this.endValue = endValue;
            this.startColor = startColor;
            this.endColor = endColor;
        }

        /**
         * Returns the start value of this ColorInterval.
         * @return the start value
         */
        public double getStartValue() {
            return startValue;
        }

        /**
         * Sets the start value of this ColorInterval.
         * @param startValue the new start value
         */
        public void setStartValue(double startValue) {
            this.startValue = startValue;
        }

        /**
         * Returns the end value of this ColorInterval.
         * @return the end value
         */
        public double getEndValue() {
            return endValue;
        }

        /**
         * Sets the end value of this ColorInterval.
         * @param endValue the new end value
         */
        public void setEndValue(double endValue) {
            this.endValue = endValue;
        }

        /**
         * Returns the start color of this ColorInterval.
         * @return the start color
         */
        public Color getStartColor() {
            return startColor;
        }

        /**
         * Sets the start color of this ColorInterval.
         * @param startColor the new start color
         */
        public void setStartColor(Color startColor) {
            this.startColor = startColor;
        }

        /**
         * Returns the end color of this ColorInterval.
         * @return the end color
         */
        public Color getEndColor() {
            return endColor;
        }

        /**
         * Sets the end color of this ColorInterval.
         * @param endColor the new end color
         */
        public void setEndColor(Color endColor) {
            this.endColor = endColor;
        }
    }    
}