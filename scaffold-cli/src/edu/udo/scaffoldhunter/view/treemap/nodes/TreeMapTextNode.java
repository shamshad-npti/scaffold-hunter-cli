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

package edu.udo.scaffoldhunter.view.treemap.nodes;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.view.treemap.TreeMapCanvas;
import edu.udo.scaffoldhunter.view.treemap.TreeMapView;
import edu.udo.scaffoldhunter.view.util.TooltipNode;
import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author schrins
 * 
 * <b>TreeMapTextNode</b> is used to visualize the SMILES string for every scaffold/molecule in the tree map. 
 * It has somewhat limited functionality compared to <b>PText</b>. Since the layout computation of <b>PText</b>
 * is quite time consuming, this class is just used to accelerate the tree map calculation.
 * 
 * The layout computation of <b>PText</b> would be done many times for every scaffold/molecule during one tree
 * map calculation. In this class we removed a recursive call of the layout computation and also simplified it
 * by dropping the support for multiple lines with automated line breaks.
 * 
 * It is discouraged to use this class in another context, since the reduced layout computation might not work
 * in other scenarios.
 * 
 * This node also implements the {@link TooltipNode} interface, because it lies above its parent {@link TreeMapNode}.
 * All tooltip method calls are just passed on to its parent.
 *
 */
public class TreeMapTextNode extends PText implements TooltipNode{
    
    /** Exactly one line of text. */
    private transient TextLayout lines;
    
    private static Logger logger = LoggerFactory.getLogger(TreeMapView.class);
    
    private FontMetrics fontMetrics;
    private String title;
    private TreeMapNode parent;
    private boolean drawText = false;
    
    /**
     * Creates a new text node with a given title and references to its parent and a canvas, where this
     * node will be drawn. The parent is mandatory, because this node has to be embedded in a {@link TreeMapNode}. 
     * @param title the title of this node
     * @param canvas the canvas, where this node will be drawn
     * @param parent this node's parent
     */
    public TreeMapTextNode(String title, TreeMapCanvas canvas, TreeMapNode parent) {
        super(title);
        fontMetrics = canvas.getFontMetrics(new Font("Tahoma", 0, 13)); // change fonts here  
        this.parent = parent;
    }
    
    /**
     * Reload the settings for the title.
     * 
     * Call this method once the size of this node is changed so that the title
     * is again placed at the correct position.
     */
    public void reloadTitle() {
        
        double x = parent.getInnerBounds().getX();
        double y = parent.getInnerBounds().getY();
        double width = parent.getInnerBounds().getWidth();
        double height = parent.getInnerBounds().getHeight();
        
        if(width == 0 || height == 0)
            return; // bounds have not been initialized yet
        
        if(Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(width) || Double.isNaN(height) || width < 0 || height < 0) {
            logger.error("TitleNode: Invalid values for node. Drawing impossible");
            return;
        }
            
        double xOffset = 0.01 * width; 
        if(width > 2 * height) //exception for really wide nodes. This offset needs to be smaller. 
            xOffset *= (height/width); 
        final double yOffset = 0;
    
        double fontHeight = fontMetrics.getHeight();
        double availableTextWidth = (width - 2*xOffset)*.85; //make it a bit smaller, necessary since titles will still be too long other wise
        double availableTextHeight = parent.getTitleBounds().getHeight() *.8; //keep some area clear.
        
        double scale = (availableTextHeight/fontHeight);
        while(fontMetrics.charWidth('w') * scale * 2 > availableTextWidth) { //if fontsize is bigger than width (this happens on very tall nodes)
            scale /= 1.5;
        }
        
        double xTitle = (x + xOffset) / scale;
        double yTitle = (y + yOffset) / scale;
        
        String title = this.title + ""; //copy
        //cut off too long titles
        if(fontMetrics.stringWidth(title) * scale > availableTextWidth) {
            while(fontMetrics.stringWidth(title) * scale > availableTextWidth) {
                if(title.length() == 0)
                    break;
                title = title.substring(0, title.length()-1);
            }
            if(title.length() > 1) { // if even shorter than this, don't bother..
                title = title.substring(0, title.length()-1);
                title += "..";
            }
        }
        
        setBounds(xTitle, yTitle, availableTextWidth, availableTextHeight);
        setText(title);
        setScale(scale);
        setOffset(xOffset, yOffset);
    }    
    
    @Override
    public void setText(String newText) {
        if (newText == null && getText() == null || newText != null && newText.equals(getText())) {
            /*
             * Layout computation is no longer invoked on bound/position updates.
             * Text is set, when a new tree map is calculated. To ensure, that recomputeLayout is 
             * called at least once (and preferably not more than once). It is called here if and 
             * only if it is not called by the setText method of PText
             */
            this.recomputeLayout();
        }
        else
            super.setText(newText);
        invalidatePaint();
    }

    @Override
    public void recomputeLayout() {
        double textWidth = 0;
        double textHeight = 0;

        if (getText() != null && getText().length() > 0) {
            final AttributedString atString = new AttributedString(getText());
            atString.addAttribute(TextAttribute.FONT, getFont());
            final AttributedCharacterIterator itr = atString.getIterator();
            
            lines = new TextLayout(itr,PPaintContext.RENDER_QUALITY_HIGH_FRC);
            textHeight += lines.getAscent();
            textHeight += lines.getDescent() + lines.getLeading();
            textWidth = Math.max(textWidth, lines.getAdvance());
        }

        if (isConstrainWidthToTextWidth() || isConstrainHeightToTextHeight()) {
            double newWidth = getWidth();
            double newHeight = getHeight();
            
            if (isConstrainWidthToTextWidth()) {
                newWidth = textWidth;
            }

            if (isConstrainHeightToTextHeight()) {
                newHeight = textHeight;
            }            
            
            if(getWidth() != newWidth || getHeight() != newHeight)
                super.setBounds(getX(), getY(), newWidth, newHeight);
        }
    }
    
    @Override
    protected void paintText(final PPaintContext paintContext) {
        if(!isDrawText()) {
            // do nothing when text must not be drawn
            return;
        }
                
        final float x = (float) getX();
        float y = (float) getY();
        final float bottomY = (float) getHeight() + y;

        final Graphics2D g2 = paintContext.getGraphics();
        g2.setStroke(new BasicStroke(0));

        if (lines == null) {
            recomputeLayout();
            repaint();
            return;
        }
    
        final TextLayout tl = lines;
        y += tl.getAscent();

        if (bottomY < y) {
            return;
        }

        final float offset = (float) (getWidth() - tl.getAdvance()) * getHorizontalAlignment();

        tl.draw(g2, x + offset, y);

        y += tl.getDescent() + tl.getLeading();
    }
    
    @Override
    protected void internalUpdateBounds(final double x, final double y, final double width, final double height) {
        // empty method, no recursion of {@code recomputeLayout()}
    }
    
    /**
     * @return the title of this node. 
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Set the title for this node
     * @param title
     */
    public void setTitle(String title) {
        this.title = title;
        reloadTitle();
    }

    /**
     * @return the drawText
     */
    public boolean isDrawText() {
        return drawText;
    }

    /**
     * @param drawText the drawText to set
     */
    public void setDrawText(boolean drawText) {
        this.drawText = drawText;
    }
    
    /**
     * @return the parent of this text node 
     */
    public TreeMapNode getTreeMapParent() {
        return parent;
    }
    
    /*
     * Tooltip methods are just passed to the parent, since this is the one, which handles
     * this functionality.
     */
    
    @Override
    public boolean hasTooltip() {
        return parent.hasTooltip();
    }
   
    @Override
    public Structure getStructure() {
        return parent.getStructure();
    }
}
