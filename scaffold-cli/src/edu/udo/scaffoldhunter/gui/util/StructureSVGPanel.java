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

package edu.udo.scaffoldhunter.gui.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.view.util.SVG;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGLoadObserver;

/**
 * JPanel depicting the structural formula of a molecule, the
 * SVG is loaded asynchronously using an SVGCache.
 * 
 * @author Nils Kriege
 * @author Philipp Lewe
 */
public class StructureSVGPanel extends JPanel implements SVGLoadObserver {
    

    private SVG svg = null;
    private SVGCache svgCache;

    /**
     * @param svgCache
     *            the {@link SVGCache} where new svgs are retrieved from
     */
    public StructureSVGPanel(SVGCache svgCache) {
        this.svgCache = svgCache;
        setOpaque(false);
        setBackground(Color.WHITE);
    }

    /**
     * Updates the zoom panel to show a new {@link Structure}
     * 
     * @param structure
     *            the {@link Structure} to be shown or null to show nothing
     */
    public void updateSVG(Structure structure) {
        if (structure != null) {
            svg = svgCache.getSVG(structure, null, getBackground(), this);
        } else {
            svg = null;
        }
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        
        // get drawing area
        Rectangle clipRect = g.getClipBounds();
        int clipX;
        int clipY;
        int clipW;
        int clipH;
        if (clipRect == null) {
            clipX = clipY = 0;
            clipW = getWidth();
            clipH = getHeight();
        } else {
            clipX = clipRect.x;
            clipY = clipRect.y;
            clipW = clipRect.width;
            clipH = clipRect.height;
        }
        if(clipW > getWidth()) {
            clipW = getWidth();
        }
        if(clipH > getHeight()) {
            clipH = getHeight();
        }
        
        // paint
        g.setColor(getBackground());
        g.fillRect(clipX, clipY, clipW, clipH);
        super.paintBorder(g);
        if (svg != null) {
            svg.paint((Graphics2D) g, clipX, clipY, clipW, clipH, true);
        }
    }

    @Override
    public void svgLoaded(SVG svg) {
        repaint();
    }

}
