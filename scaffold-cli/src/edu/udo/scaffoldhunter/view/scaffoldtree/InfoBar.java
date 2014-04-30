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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import edu.udo.scaffoldhunter.view.util.ExportPaintNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;


/**
 * A bar containing several colored boxes, which can be displayed at a scaffold
 * to visualize the distribution of some property.
 * <p>
 * Each colored box represents a value range, the size of the box represents
 * the number of molecules whose property value falls into this range.
 * 
 * @author Henning Garus
 *
 */
public class InfoBar extends PNode implements ExportPaintNode {
    
    private static final int HEIGHT = 25;
    private static final int COMBINED_HEIGHT = 35;


    private static final float boxStrokeWidth = 1.5f;
    private static final float fillingLevelStrokeWidth = 4f;
    private static final Stroke boxStroke = new BasicStroke(boxStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
    private static final Stroke fillingLevelStroke = new BasicStroke(fillingLevelStrokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

    private List<Color> colors = Collections.emptyList();
    private List<Rectangle2D> slices = Collections.emptyList();
    private GeneralPath fillingLevelPath = null;

    /**
     * @param width
     */
    public InfoBar(double width) {
        setWidth(width);
    }

    /**
     * Draws one box for each value in <code>slices</code> in the color
     * given by the value. The size of the box is determined by the value of
     * <code>value</code> relative to the sum of all values.
     * 
     * @param slices
     */
    public void show(Collection<? extends PropertyBinValue> slices) {
        fillingLevelPath = null;
        setHeight(HEIGHT);
        update(slices);
        invalidatePaint();
    }      
    
    /**
     * Draws colored boxes the same way {@link #show} does. Additionally
     * depicts a line inside each box, showing the fraction by which the
     * shown cumulative values belong to the current node.
     * 
     * @param slices
     */
    public void showCombined(Collection<PropertyBinValueCombined> slices) {
        setHeight(COMBINED_HEIGHT);
        update(slices);
        updateFillingLevel(slices);
        invalidatePaint();
    }

    private void updateFillingLevel(Collection<PropertyBinValueCombined> slices) {
        int totalNumber = 0;
        for (PropertyBinValueCombined v : slices)
            totalNumber += v.getNonCumulativeValue();

        // reduce the inner area to make the filling level indicator visible
        double innerUpperBound = 0 + boxStrokeWidth/2 + fillingLevelStrokeWidth/3;
        double innerLowerBound = getHeight() - boxStrokeWidth/2 - fillingLevelStrokeWidth/3;
        double innerHeight = innerLowerBound - innerUpperBound;

        fillingLevelPath = new GeneralPath();
        //fillingLevelPath.moveTo(0, getHeight());

        double x = 0;
        double currentWidth;
        double currentHeight;
        for (PropertyBinValueCombined v : slices) {
            if (v.getNonCumulativeValue() == 0)
                continue;

            currentWidth = ((double)v.getNonCumulativeValue()/totalNumber) * getWidth();
            currentHeight = (1d-(double)v.getValue()/v.getNonCumulativeValue()) * innerHeight;
            //fillingLevelPath.lineTo(x, currentHeight);
            fillingLevelPath.moveTo(x, currentHeight+innerUpperBound);
            fillingLevelPath.lineTo(x+currentWidth, currentHeight+innerUpperBound);

            x += currentWidth;
        }
    }

    private void update(Collection<? extends PropertyBinValue> slices) {
        int totalNumber = 0;
        for (PropertyBinValue v : slices)
            totalNumber += v.getValue();

        ArrayList<Color> tmpColors = new ArrayList<Color>(slices.size());
        ArrayList<Rectangle2D> tmpSlices = new ArrayList<Rectangle2D>(slices.size());
        double x = 0;
        double currentWidth;
        for (PropertyBinValue v : slices) {
            if (v.getValue() == 0)
                continue;
            currentWidth = ((double)v.getValue()/totalNumber) * getWidth();
            tmpColors.add(v.getColor());
            tmpSlices.add(new Rectangle2D.Double(x, 0, currentWidth, getHeight()));
            x += currentWidth;
        }

        this.colors = tmpColors;
        this.slices = tmpSlices;
    }

 
//    private void updateCombined(Collection<PropertyBinValueCombined> slices) {
//        int totalNumber = 0;
//        for (PropertyBinValueCombined v : slices)
//            totalNumber += v.getNonCumulativeValue();
//
//        ArrayList<Color> tmpColors = new ArrayList<Color>(2*slices.size());
//        ArrayList<Rectangle2D> tmpSlices = new ArrayList<Rectangle2D>(2*slices.size());
//        double x = 0;
//        double currentWidth;
//        double currentHeight;
//        for (PropertyBinValueCombined v : slices) {
//            if (v.getNonCumulativeValue() == 0)
//                continue;
//            currentWidth = ((double)v.getNonCumulativeValue()/totalNumber) * getWidth();
//            currentHeight = (1d-(double)v.getValue()/v.getNonCumulativeValue()) * getHeight();
//            tmpColors.add(darker(v.getColor()));
//            tmpSlices.add(new Rectangle2D.Double(x, 0, currentWidth, currentHeight));
//            if (v.getValue() != 0) {
//                tmpColors.add(v.getColor());
//                tmpSlices.add(new Rectangle2D.Double(x, currentHeight, currentWidth, getHeight()-currentHeight));
//            }
//            x += currentWidth;
//        }
//
//        this.colors = tmpColors;
//        this.slices = tmpSlices;
//    }
//
//    private Color darker(Color c) {
//        //Color res = c.darker();
//        float hsb[] = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
//        return Color.getHSBColor(hsb[0], hsb[1], hsb[2] * 0.6f);
//    }

    @Override
    public void paint(PPaintContext paintContext) {
        Graphics2D g = paintContext.getGraphics();

        // semantic zoom
        if (SemanticZoomLevel.MEDIUM.scaleIsBelowThreshold(paintContext.getScale())) {
            return;
        }

        // resizes the infobars painting. the node (and its children) will be painted outside of this nodes bounds!
        if (SemanticZoomLevel.CLOSE.scaleIsBelowThreshold(paintContext.getScale())) {
            double factor = 2.3;
            g.translate(0, -factor*getHeight());
            g.scale(1.0, factor);
        }

        paintBasic(paintContext);
    }
    
    @Override
    public void exportPaint(PPaintContext paintContext) {
        paintBasic(paintContext);
    }
    
    /**
     * Paints this infobar. 
     * @param paintContext the context the infobar is painted to
     */
    public void paintBasic(PPaintContext paintContext) {
        Graphics2D g = paintContext.getGraphics();

        g.setStroke(new BasicStroke(0.2f));
        for (int i = 0; i < slices.size(); i++) {
            g.setPaint(colors.get(i));
            g.fill(slices.get(i));
            g.draw(slices.get(i));
        }

        if (fillingLevelPath != null) {
            g.setStroke(fillingLevelStroke);
            g.setPaint(Color.BLACK);
            g.draw(fillingLevelPath);
        }

        g.setStroke(boxStroke);
        g.setPaint(Color.BLACK);
        // FIXME this may paint outside the bounds of this node!
        g.draw(getBoundsReference());
    }

}
