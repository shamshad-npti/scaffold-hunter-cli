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

import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * Represents a simple info/warning shown during data import / data calculation
 * 
 * @author Henning Garus
 * 
 */
public class Message {

    private final MessageType type;
    private final String moleculeTitle;
    private final PropertyDefinition propertyDefinition;
    private final Job job;

    /**
     * Create a new message.
     * 
     * @param type
     *            the type of the message
     * @param moleculeTitle
     *            the title of the molecule, which this message is about
     * @param propertyDefinition
     *            the property definition this message is about, may be null
     * @param job
     *            the job this message is about
     */
    public Message(MessageType type, String moleculeTitle, PropertyDefinition propertyDefinition, Job job) {
        this.type = type;
        this.moleculeTitle = moleculeTitle;
        this.propertyDefinition = propertyDefinition;
        this.job = job;
    }

    /**
     * @return the type
     */
    public MessageType getType() {
        return type;
    }

    /**
     * @return the moleculeTitle
     */
    public String getMoleculeTitle() {
        return moleculeTitle;
    }

    /**
     * @return the propertyDefinition
     */
    public PropertyDefinition getPropertyDefinition() {
        return propertyDefinition;
    }

    /**
     * @return the importJob
     */
    public Job getJob() {
        return job;
    }

}
