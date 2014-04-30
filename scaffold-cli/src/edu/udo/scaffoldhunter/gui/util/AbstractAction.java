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

import javax.swing.Icon;
import javax.swing.KeyStroke;

/**
 * An extension of the swing AbstractAction class that adds several
 * putValues() methods as a convenient alternative to the standard putValue().
 *  
 * @author Dominic Sacré
 */
public abstract class AbstractAction extends javax.swing.AbstractAction {

    /**
     *
     */
    public AbstractAction() {
    }

    /**
     * @param name
     */
    public AbstractAction(String name) {
        super(name);
    }
    
    /**
     * @param name
     * @param icon
     */
    public AbstractAction(String name, Icon icon) {
        super(name, icon);
    }
    
    protected void putValues(String name, Icon icon) {
        putValues(name, null, icon, null, null);
    }

    protected void putValues(String name) {
        putValues(name, null, null, null, null);
    }

    protected void putValues(String name, String shortDescription) {
        putValues(name, shortDescription, null, null, null);
    }

    protected void putValues(String name, String shortDescription, KeyStroke accelerator) {
        putValues(name, shortDescription, null, null, accelerator);
    }

    protected void putValues(String name, String shortDescription, Icon smallIcon, Icon largeIcon,
            KeyStroke accelerator) {
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, shortDescription);
        putValue(SMALL_ICON, smallIcon);
        putValue(LARGE_ICON_KEY, largeIcon);
        putValue(ACCELERATOR_KEY, accelerator);
    }
}
