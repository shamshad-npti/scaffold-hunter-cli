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

import java.util.ListResourceBundle;

import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Philipp Lewe
 * 
 */
public abstract class I18nResourceBundleWrapper extends ListResourceBundle {
    /*
     * (non-Javadoc)
     * 
     * @see java.util.ListResourceBundle#getContents()
     */
    @Override
    protected Object[][] getContents() {
        String[] keys = getKeyArray();
        Object[][] contents = new Object[keys.length][2];

        // cuts of all except the classname out of the qualified name
        String className = getClass().getName();
        int lastDotPosition = className.lastIndexOf(".");
        if (lastDotPosition != -1) {
            className = className.substring(lastDotPosition + 1, className.length());
        }

        // fetch internationalisation
        for (int i = 0; i < contents.length; i++) {
            contents[i][0] = keys[i];
            contents[i][1] = I18n.get("Config." + className + "." + keys[i]);
        }
        return contents;
    }

    /**
     * Gets the keys used by this configuration object
     * 
     * @return an array of keys
     */
    abstract protected String[] getKeyArray();
}
