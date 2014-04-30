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

import javax.swing.Icon;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageType;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Henning Garus
 * @author Philipp Lewe
 * 
 */
public enum CalcMessageTypes implements MessageType {

    /**
     * A source property needed for calculation was not present
     */
    PROPERTY_NOT_PRESENT("dialog-information.png"),

    /**
     * The Property is already calculated and can be calculated only once
     */
    PROPERTY_ALREADY_PRESENT("dialog-information.png"),

    /**
     * A structure contained no valid 2D coordinates. New coordinates are
     * calculated
     */
    NO_VALID_2D_COORDS("dialog-information.png"),

    /**
     * Multiple fragments in structure, largest one used
     */
    LARGEST_FRAGMENT_USED("dialog-information.png"),

    /**
     * Internal plugin error, no value calculated
     */
    CALCULATION_ERROR("dialog-warning.png");

    private final String msgKey;
    private final String string;
    private final Icon icon;

    private CalcMessageTypes(String icon) {
        this.msgKey = "DataCalc.Messages.MessageString." + this.name();
        this.string = I18n.get("DataCalc.Messages.ToString." + this.name());
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
