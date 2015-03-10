/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.cli;

/**
 * Displays some changing message to console. It replaces the previously 
 * displayed message with the new message when you call <code>replace()</code> 
 * method. For example if previously you have printed 'generating...' and you 
 * are calling this method now with 'generated...' then 'generating...' is 
 * replaced with 'generated...'
 * 
 * WARNING! This will not work if between two successive call of this method
 * some output is printed on console using any of console output methods such 
 * as System.out.print()
 * @author Shamshad Alam
 *
 */
public class ProgressUtil {
    private int length = 0;
    
    /**
     * @param message to be printed on console and this will
     * replaces the previous message
     */
    public void replace(String message) {
        StringBuilder builder = new StringBuilder();
        while(length > 0) {
            builder.append("\b \b");
            length--;
        }
        System.out.print(builder.append(message));
        length = message != null ? message.length() : 0;
    }
    
    /**
     * Reset last message length to zero
     */
    public void reset() {
        length = 0;
    }
}
