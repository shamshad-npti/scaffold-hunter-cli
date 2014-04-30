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

package edu.udo.scaffoldhunter.gui.util;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

/**
 * A base class for custom ComboBox cell renderers that should be used instead
 * of deriving from DefaultListCellRenderer. This works around Java bug 6505565
 * which causes ComboBoxes with custom cell renderers to be drawn incorrectly
 * by the GTK look and feel.
 * <p>
 * The return value of getListCellRendererComponent() can be assumed to be a
 * JLabel.
 * 
 * @author Dominic Sacré
 */
public class CustomComboBoxRenderer implements ListCellRenderer {

    private final ListCellRenderer defaultRenderer;

    /**
     * Creates a new custom cell renderer.
     */
    public CustomComboBoxRenderer() {
        if (LookAndFeel.isGTKLookAndFeel()) {
            // this seems to be the only way to instantiate the correct default
            // cell renderer
            defaultRenderer = (new JComboBox()).getRenderer();
        } else {
            defaultRenderer = new DefaultListCellRenderer();
        }
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {
        return defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }

}
