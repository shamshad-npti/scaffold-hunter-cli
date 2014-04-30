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

package edu.udo.scaffoldhunter.model.dataimport;

import java.util.Collections;
import java.util.List;

import javax.swing.AbstractListModel;

import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.util.Orderings;

/**
 * @author Henning Garus
 * 
 */
public class PropertyDefinitionList extends AbstractListModel {

    private final List<PropertyDefinition> propertyDefinitions;

    /**
     * Create a new property definition list from a list of property definitions
     * 
     * @param propertyDefinitions
     *            a list of property definitions which are put into the newly
     *            created property definition list
     */
    public PropertyDefinitionList(List<PropertyDefinition> propertyDefinitions) {
        this.propertyDefinitions = Orderings.PROPERTY_DEFINITION_BY_TITLE.sortedCopy(propertyDefinitions);
    }

    @Override
    public int getSize() {
        return propertyDefinitions.size();
    }

    @Override
    public Object getElementAt(int index) {
        return propertyDefinitions.get(index);
    }

    /**
     * add a new property definition to this list
     * 
     * @param propertyDefinition
     *            the property definition to add
     */
    public void add(PropertyDefinition propertyDefinition) {
        propertyDefinitions.add(propertyDefinition);
        Collections.sort(propertyDefinitions, Orderings.PROPERTY_DEFINITION_BY_TITLE);
        fireContentsChanged(this, 0, propertyDefinitions.size());
    }

    /**
     * Retrieves the property definition with the specified title. If there are
     * multiple property definitions with that tile in the list, the first one
     * will be retrieved.
     * 
     * @param title
     *            the searched property definition title
     * 
     * @return the first property definition with that title or
     *         <code>null</code> if ther is none.
     */
    public PropertyDefinition getByTitle(String title) {
        for (PropertyDefinition propDef : propertyDefinitions) {
            if (propDef.getTitle().equals(title))
                return propDef;
        }
        return null;
    }
}
