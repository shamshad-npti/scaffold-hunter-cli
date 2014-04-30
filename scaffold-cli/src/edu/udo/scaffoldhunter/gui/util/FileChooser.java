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
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;

/**
 * A tiny wrapper around JFileChooser that works around the issue of a combo
 * box being rendered incorrectly by the GTK look and feel.
 * 
 * @author Dominic Sacré
 */
public class FileChooser extends JFileChooser {

    /**
     * Creates a new file chooser.
     */
    public FileChooser() {
        super();

        if (LookAndFeel.isGTKLookAndFeel()) {
            fixComboBoxHeight(this);
        }
    }

    /**
     * Recursively looks for the file type combo box, and changes its preferred
     * height to that of a default combobox.
     */
    private void fixComboBoxHeight(Component comp) {
        if (comp instanceof JComboBox && comp.getName() == null) {
            comp.setPreferredSize((new Dimension(
                    (int)comp.getPreferredSize().getWidth(),
                    (int)(new JComboBox()).getPreferredSize().getHeight())
            ));
        }
        if (comp instanceof Container) {
            for (Component c : ((Container)comp).getComponents()) {
                fixComboBoxHeight(c);
            }
        }
    }

}
