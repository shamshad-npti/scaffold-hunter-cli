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

package edu.udo.scaffoldhunter.model.dataimport;

import javax.swing.Icon;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageType;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Henning Garus
 * 
 */
public enum MergeMessageTypes implements MessageType {

    /** An error occured during double parsing */
    DOUBLE_CONVERSION_ERROR("dialog-warning.png"),
    /** An error occured while writing the molecular structure */
    STRUCTURE_WRITE_ERROR("dialog-warning.png"),
    /** No structure found */
    NO_STRUCTURE_ERROR("dialog-warning.png"),
    /** SMILES generation failed */
    SMILES_ERROR("dialog-warning.png"),
    /** A source contains the same structure multiple times */
    SAME_MOLECULE("dialog-warning.png"),
    /** A property was not present */
    PROPERTY_NOT_PRESENT("dialog-information.png"),
    /** No title string was found for the title property */
    NO_TITLE("dialog-information.png"),
    /** An existing property was not overwritten by a new one*/
    DONT_OVERWRITE("dialog-information.png"),
    /** An existing property was overwritten by a new one */
    OVERWRITE("dialog-information.png"),
    /** A property was set to the minimum of the old and the new one */
    MINIMUM("dialog-information.png"),
    /** A property was set to the maximum of the old and the new one */
    MAXIMUM("dialog-information.png"),
    /** A new property was appended to the existing property */
    CONCATENATE("dialog-information.png"),
    /** Can't build Molecule on base of SMILES */
    MOLECULE_BY_SMILES_FAILED("dialog-warning.png"),
    /** Can't build Molecule on base of MOL */
    MOLECULE_BY_MOL_FAILED("dialog-warning.png");
    

    private final String msgKey;
    private final String string;
    private final Icon icon;

    private MergeMessageTypes(String icon) {
        this.msgKey = "DataImport.Messages.MessageString." + this.name();
        this.string = I18n.get("DataImport.Messages.ToString." + this.name());
        this.icon = Resources.getIcon(icon);
    }
    
    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public String getMessageString(Message message) {
        String propDefTitle = null;
        if (message.getPropertyDefinition() != null)
            propDefTitle = message.getPropertyDefinition().getTitle();
        return I18n.get(msgKey, message.getMoleculeTitle(), propDefTitle);
    }
    
    @Override
    public String toString() {
        return string;
    }

}
