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

package edu.udo.scaffoldhunter.model.data;

import javax.swing.Icon;

import edu.udo.scaffoldhunter.model.dataimport.MergeMessageTypes;

/**
 * Defines the type of a message.
 * 
 * @author Henning Garus
 *
 * @see MergeMessageTypes
 */
public interface MessageType {

    /**
     * build a string which can be displayed for a message
     * 
     * @param message the message which the string should represent
     * @return a string which can be displayed in place of a message
     */
    public String getMessageString(Message message);
    
    /**
     * 
     * @return an icon representing this message type
     */
    public Icon getIcon();
}
