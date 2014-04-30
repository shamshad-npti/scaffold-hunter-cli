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

package edu.udo.scaffoldhunter.util;

import java.util.Iterator;
import java.util.List;

/**
 * @author Thomas Schmitz
 * 
 */
public class StringList {

    /**
     * Checks if the StringList contains the given string case insensitive
     * 
     * @param list
     *            The list to check
     * @param string
     *            The string to search for
     * @return True, if the list contains the string case insensitive
     */
    public static boolean containsIgnoreCase(List<String> list, String string) {
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            if (it.next().equalsIgnoreCase(string))
                return true;
        }
        return false;
    }
}
