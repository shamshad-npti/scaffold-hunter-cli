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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.util.ColorFunctions;
import edu.udo.scaffoldhunter.util.I18n;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PImage;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.PFrame;

/**
 * A simple color legend that shows the range from low to high value
 * 
 * @author Lappie
 * 
 */
public class ColorLegend extends PCanvas {

    private static PImage legend;
    private static PCamera camera;
    
    private final int TITLE_OFFSET = 30; 
    private final int LEGEND_HEIGHT = 140; //height of the bar
    private final int LEGEND_WIDTH = 50; //width of the bar
    private final int OFFSET = 5;
    private final int CANVAS_WIDTH = 160;
    private final int CANVAS_HEIGHT = 160;
    
    private PText minText = new PText();
    private PText maxText = new PText();
    
    private PText title = new PText();
    
    private static float min = 0.0f;
    private static float max = 140.0f; //should match legend_height
    
    /**
     * The color for an item from which its property is not known.
     */
    public static final Color UNKNOWN_PROPERTY_COLOR = new Color(200,200,200); 

    /**
     * Creates a simple legend to show which colors we have
     */
    public ColorLegend() {
        super();
        camera = getCamera();
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));

        loadText();
        createImageLegend();
        getLayer().addChild(legend);
        getLayer().addChild(minText);
        getLayer().addChild(maxText);
        getLayer().addChild(title);

        // remove Pan and Zoom
        removeInputEventListener(getPanEventHandler());
        removeInputEventListener(getZoomEventHandler());
        
        getCamera().addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, new InitializationListener());
    }
    
    /**
     * For initializing and resetting the text of this legend
     */
    private void loadText() {
                
        title.setText(I18n.get("TreeMapView.Legend.Title"));
        title.setRotation(1.5 * Math.PI);
        title.setX(-100);
        title.setY(10);
        
        minText.setText(I18n.get("TreeMapView.Legend.Min"));
        maxText.setText(I18n.get("TreeMapView.Legend.Max"));
        
        minText.setBounds(TITLE_OFFSET+LEGEND_WIDTH+OFFSET, OFFSET-5, 20, 20);
        maxText.setBounds(TITLE_OFFSET+LEGEND_WIDTH+OFFSET, LEGEND_HEIGHT, 20, 20);
    }

    /**
     * Initialize this canvas. 
     * Necessary to zoom to the correct level. 
     */
    public static void innitialize() {
        camera.animateViewToCenterBounds(legend.getGlobalFullBounds(), true, 0);
    }

    private void createImageLegend() {
        LegendBar node = new LegendBar();
        legend = new PImage(node.toImage(CANVAS_WIDTH, CANVAS_HEIGHT, Color.WHITE)); //image so that we only have to draw this once
    }

    private Stroke getZeroStroke() {
        return new BasicStroke(0);
    }

    class LegendBar extends PNode {

        public LegendBar() {
            super();
            setX(0);
            setY(0);
            setWidth(CANVAS_WIDTH);
            setHeight(CANVAS_HEIGHT);
            setPaint(Color.BLACK);
        }

        private PBounds getBounds(int i) {
            return new PBounds(TITLE_OFFSET, OFFSET+i, LEGEND_WIDTH, 2);
        }
        
        @Override
        protected void paint(final PPaintContext paintContext) {
            Graphics2D g2 = paintContext.getGraphics();
            g2.setStroke(getZeroStroke());
            
            for (int i = 0; i < LEGEND_HEIGHT; i++) {
                g2.setPaint(getColor((100 * i) / LEGEND_HEIGHT));
                g2.fill(getBounds(i));
            }
            
            g2.setPaint(Color.BLACK);
        }
    }
    
    /**
     * Listen to property changes from this camera and updates the legend
     * This listener is required during initialization only because centering 
     * the camera can only be done after all other paint jobs are done
     */
    private class InitializationListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            innitialize();
        }
    }
    
    /**
     * Set the minimum value of the legend. Will update the title and affect the getRangeColor function
     * @param min
     */
    public void setMin(float min) {
        ColorLegend.min = min;
        minText.setText("" + min);
    }
    
    /**
     * Set the maximum value of the legend. Will update the title and affect the getRangeColor function
     * @param max
     */
    public void setMax(float max) {
        ColorLegend.max = max;
        maxText.setText("" + max);
    }
    
    /**
     * Set the title that is next the colorbar
     * @param title
     */
    public void setTitle(String title) {
        this.title.setText(title);
        this.title.setX(-80 - (title.length()/2*5.5));
    }

    /**
     * For a value between 0 and 100, what is the corresponding color according to this legend. 
     * @param value
     * @return Color matching the given value
     */
    private static Color getColor(int value) {
        Preconditions.checkArgument(value >= 0 && value <= 100);
        float hue = (100-value) / 100f / 3f + .333f; 
        // divide by 3 plus .33 so that we have colors
        // from blue to green
        return ColorFunctions.hsvToRgb(hue, .65f, 1f);
    }
    
    /**
     * Reset the text of the legend to default values
     */
    public void reset() {
        loadText();
    }
    
    /**
     * Get the color matching to this value according to this legend. With respect to the set min and max 
     * @param value
     * @return color corresponding to value
     */
    public static Color getRangeColor(float value) {
        if(value < min || value > max) {//TODO: something is wrong here, this shouldn't be possible. Add Logger
            return Color.PINK;
        }
        if(max == min)
            return getColor(50); //return the avg color;
        
        Preconditions.checkArgument(value >= min && value <= max, "Color value(" + value + ") should be between min(" + min + ") and max(" + max + ") to keep in range");
        
        float range = Math.abs(max - min);
        float correctedValue = value - min;
        int colorValue = (int) ((correctedValue/range) * 100.0);
        
        return getColor(Math.abs(colorValue));
    }
    
    
    /**
     * A simple test case
     * 
     * @param args
     *            : not applicable
     */
    public static void main(String args[]) {
        final ColorLegend canvas = new ColorLegend();
        PFrame frame = new PFrame("ColorLegend", false, canvas);
        frame.setSize(600, 400);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                innitialize();
            }
        });
        
    }
}
