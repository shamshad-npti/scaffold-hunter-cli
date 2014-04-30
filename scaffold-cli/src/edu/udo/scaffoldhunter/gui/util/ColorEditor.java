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

package edu.udo.scaffoldhunter.gui.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JColorChooser;

/**
 * A simple Color Editor. Basically a JButton which shows the currently selected
 * color and opens a JColorChooser on click.
 * 
 * @author Henning Garus
 * 
 */
public class ColorEditor extends JButton implements ActionListener {

    /**
     * The color held by this ColorEditor.
     */
    public static final String COLOR_PROPERTY = "COLOR";

    private static final int ICON_WIDTH = 16;
    private static final int ICON_HEIGHT = 16;

    private Color color;

    /**
     * Create a new color editor and initialize selection to the specified
     * color.
     * 
     * @param color the color initially selected by the editor
     */
    public ColorEditor(Color color) {
        super();
        if (color == null)
            this.color = Color.BLACK;
        else
            this.color = color;
        addActionListener(this);

        setColorIcon();
    }

    private void setColorIcon() {
        Image img = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, ICON_WIDTH, ICON_HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, ICON_WIDTH - 1, ICON_HEIGHT - 1);
        setIcon(new ImageIcon(img));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Color oldColor = color;
        Color newColor = JColorChooser.showDialog(this, "", color);
        if (newColor != null && !newColor.equals(oldColor)) {
            color = newColor;
            setColorIcon();
            firePropertyChange(COLOR_PROPERTY, oldColor, newColor);
        }
    }

}
