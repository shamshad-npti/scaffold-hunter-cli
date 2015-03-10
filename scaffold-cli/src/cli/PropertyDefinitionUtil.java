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

import java.util.Map;

import com.beust.jcommander.internal.Maps;

import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * Utility class for property definition to deep copy the property definition
 * 
 * @author Shamshad Alam
 * 
 */
public class PropertyDefinitionUtil {

    /**
     * 
     * @param propertyDefinitions
     *            {@code Iterable<PropertyDefinition>} to deep copy
     * @return copy of properties
     */
    public static Map<PropertyDefinition, PropertyDefinition> deepCopy(Iterable<PropertyDefinition> propertyDefinitions) {
        Map<PropertyDefinition, PropertyDefinition> copiedMap = Maps.newHashMap();

        for (PropertyDefinition propDef : propertyDefinitions) {
            PropertyDefinition copy = new PropertyDefinition(propDef);
            // remove reference on existing dataset
            copy.setDataset(null);
            copiedMap.put(propDef, copy);
        }
        return copiedMap;
    }

}
