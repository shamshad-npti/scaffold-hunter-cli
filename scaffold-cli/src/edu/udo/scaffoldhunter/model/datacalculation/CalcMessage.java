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

package edu.udo.scaffoldhunter.model.datacalculation;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageType;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Philipp Lewe
 * 
 */
public class CalcMessage extends Message {

    /**
     * @param type
     *            the type of message
     * @param moleculeTitle
     *            the title of the molecule the message is related to (use
     *            <code>molecule.getProperties().get(CDKConstants.TITLE)</code>
     *            to get the title of a CDK molecule)
     * @param propertyDefinition
     *            the property definition the message is related to
     */
    public CalcMessage(MessageType type, String moleculeTitle, PropertyDefinition propertyDefinition) {
        super(type, moleculeTitle, propertyDefinition, null);
    }

    /**
     * @param type
     *            the type of message
     * @param moleculeTitle
     *            the title of the molecule the message is related to
     */
    public CalcMessage(MessageType type, String moleculeTitle) {
        super(type, moleculeTitle, null, null);
    }

}
