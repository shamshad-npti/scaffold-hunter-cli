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

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.nodes.PText;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * Label used to add some text to a scaffold.
 * 
 * @author Henning Garus
 *
 */
public class ScaffoldLabel extends UnscaledNode {
    
    private final PText text;
    
    /**
     * Creates a new scaffold label without any text.
     * 
     * @param node vnode this label is attached to
     */
    public ScaffoldLabel(VNode node) {
        this(node, "");
    }
    
   /**
    * Creates a new scaffold label with text
    * 
    * @param node vnode this label is attached to
    * @param text text which will be initially displayed by this label
    */
    public ScaffoldLabel(VNode node, String text) {
        super(node, 
                new Point2D.Double(node.getBoundsReference().getCenterX(), 
                        node.getBoundsReference().getMaxY() + 15));
        this.text = new PText(text);
        this.setBounds(this.text.getBoundsReference());
        this.addChild(this.text);
        this.text.centerFullBoundsOnPoint(getBoundsReference().getCenterX(), getBoundsReference().getCenterY());
        this.text.setPickable(false);
        this.setPickable(false);
    }
   
    /**
     * @param text the text displayed by this label
     */
    public void setText(String text) {
        this.text.setText(text);
        this.setBounds(this.text.getBoundsReference());
        this.text.centerFullBoundsOnPoint(getBoundsReference().getCenterX(), getBoundsReference().getCenterY());
    }


    /* (non-Javadoc)
     * @see edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext)
     */
    @Override
    public void fullPaint(PPaintContext paintContext) {
        if (SemanticZoomLevel.MEDIUM.scaleIsBelowThreshold(paintContext.getCamera().getViewScale()))
            return;
        super.fullPaint(paintContext);
    }
   
}
