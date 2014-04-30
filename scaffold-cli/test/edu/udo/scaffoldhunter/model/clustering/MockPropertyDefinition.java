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

package edu.udo.scaffoldhunter.model.clustering;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * Mock for Testing. Allows to set the Database id.
 * 
 * @author Till Schäfer
 */
public class MockPropertyDefinition extends PropertyDefinition {
    
    /**
     * Constructor
     */
    public MockPropertyDefinition () {
        
    }
    /**
     * Constructor 
     * 
     * @param title
     * @param description
     * @param propertyType
     * @param key
     * @param mappable
     * @param scaffoldProperty
     */
    public MockPropertyDefinition(String title, String description, PropertyType propertyType, String key,
            boolean mappable, boolean scaffoldProperty) {
        super(title, description, propertyType, key, mappable, scaffoldProperty);
    }

        
    /**
     * @param id
     *            the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    @Override
    public int getId() {
        return id;
    }

}
