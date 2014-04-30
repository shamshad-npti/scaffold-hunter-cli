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

package edu.udo.scaffoldhunter.view.plot;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JButton;

/**
 * just a button showing a color
 * 
 * @author Michael Hesse
 *
 */
public class ColorButton extends JButton {

    
    
    static class ColorIcon implements Icon {

        private Color color = Color.blue;

        public ColorIcon() {
        }
        
        /* (non-Javadoc)
         * @see javax.swing.Icon#paintIcon(java.awt.Component, java.awt.Graphics, int, int)
         */
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            y += 2;
            g.setColor(color);
            g.fillRect(x, y, c.getWidth()-2*x, c.getHeight()-2*y-1);
            g.setColor(Color.black);
            g.drawRect(x, y, c.getWidth()-2*x, c.getHeight()-2*y-1);
        }

        /* (non-Javadoc)
         * @see javax.swing.Icon#getIconWidth()
         */
        @Override
        public int getIconWidth() {
            return 25;
        }

        /* (non-Javadoc)
         * @see javax.swing.Icon#getIconHeight()
         */
        @Override
        public int getIconHeight() {
            return 16;
        }
        
        public void setColor(Color color) {
            this.color = color;
        }
        
        public Color getColor() {
            return color;
        }
    }
    

    
    
    private ColorIcon icon;

    /**
     * the simple constructor, just calls the JButton() constructor
     */
    public ColorButton() {
        super();
        icon = new ColorIcon();
        setIcon( icon );
    }
    
    /**
     * @return
     *  the currently shown color
     */
    public Color getColor() {
        return icon.getColor();
    }
    
    /**
     * @param color
     *  the color that this button should show
     */
    public void setColor(Color color) {
        if(color != null) {
            icon.setColor(color);
            repaint();
        }
    }
    
}
