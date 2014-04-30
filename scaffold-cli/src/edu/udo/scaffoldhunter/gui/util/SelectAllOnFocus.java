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

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.text.JTextComponent;

/**
 * Focus Listener which can be added to a <code>JTextComponent</code> to
 * automatically select the component's text when the component recieves the
 * focus.
 * 
 * To ensure that this Listener is only added to text components the constructor
 * is private. The static <code>addTo</code> method should be used instead.
 * 
 * @author Henning Garus
 * 
 */
public class SelectAllOnFocus extends FocusAdapter {

    private static SelectAllOnFocus instance = new SelectAllOnFocus();

    private SelectAllOnFocus() {
    }

    /**
     * Add the default instance of this listener to a textComponent.
     * 
     * @param textComponent
     *            the component to which this listener will be added
     */
    public static void addTo(JTextComponent textComponent) {
        textComponent.addFocusListener(instance);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.awt.event.FocusAdapter#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(FocusEvent e) {
        if (e.getComponent() instanceof JTextComponent)
            ((JTextComponent) e.getComponent()).selectAll();
    }
}
