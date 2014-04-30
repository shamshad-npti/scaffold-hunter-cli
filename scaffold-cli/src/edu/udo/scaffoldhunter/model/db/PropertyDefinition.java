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

package edu.udo.scaffoldhunter.model.db;

import edu.udo.scaffoldhunter.model.PropertyType;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class PropertyDefinition extends DbObject implements Comparable<PropertyDefinition> {
    private Dataset dataset;
    private String title;
    private String description;
    private PropertyType propertyType;
    private boolean scaffoldProperty;

    /**
     * unique identifier for some Property - needed for generator
     */
    private String key;
    /**
     * if this Property is selectable for a mapping to Visualization in GUI
     */
    private boolean mappable;

    /**
     * default constructor
     */
    public PropertyDefinition() {
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
    public PropertyDefinition(String title, String description, PropertyType propertyType, String key,
            boolean mappable, boolean scaffoldProperty) {
        this.title = title;
        this.description = description;
        this.propertyType = propertyType;
        this.key = key;
        this.mappable = mappable;
        this.scaffoldProperty = scaffoldProperty;
    }

    /**
     * Copy Constructor. Creates a new {@link PropertyDefinition} as a copy of
     * the given {@link PropertyDefinition}
     * 
     * @param propDef
     *            the {@link PropertyDefinition} that should be copied
     */
    public PropertyDefinition(PropertyDefinition propDef) {
        this.title = propDef.getTitle();
        this.description = propDef.getDescription();
        this.propertyType = propDef.getPropertyType();
        this.key = propDef.getKey();
        this.mappable = propDef.isMappable();
        this.scaffoldProperty = propDef.isScaffoldProperty();
        this.dataset = propDef.getDataset();
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Whether a {@link Property} is saved on database level as
     * {@link StringProperty} or {@link NumProperty}
     * 
     * Attention: This Function has a new meaning
     * 
     * Legacy meaning: Use getPropertyType instead. false is interpreted as
     * PropertyType.NumProperty. Everything else is true. Until now the new
     * meaning is compatible with the old usage.
     * 
     * @return if the property is a string property
     */
    public boolean isStringProperty() {
        return (propertyType != PropertyType.NumProperty);
    }

    /**
     * Legacy: Use setPropertyType instead. true is interpreted as
     * PropertyType.StringProperty and false is interpreted as
     * PropertyType.NumProperty
     * 
     * @param stringProperty
     *            set if the property is a string property
     */
    @Deprecated
    public void setStringProperty(boolean stringProperty) {
        propertyType = stringProperty ? PropertyType.StringProperty : PropertyType.NumProperty;
    }

    /**
     * @return the propertyType
     */
    public PropertyType getPropertyType() {
        return propertyType;
    }

    /**
     * @param propertyType
     *            the propertyType to set
     */
    public void setPropertyType(PropertyType propertyType) {
        this.propertyType = propertyType;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the mappable
     */
    public boolean isMappable() {
        return mappable;
    }

    /**
     * @param mappable
     *            the mappable to set
     */
    public void setMappable(boolean mappable) {
        this.mappable = mappable;
    }

    /**
     * @param dataset
     *            the dataset to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * @return the dataset
     */
    public Dataset getDataset() {
        return dataset;
    }

    /**
     * @return the scaffoldProperty
     */
    public boolean isScaffoldProperty() {
        return scaffoldProperty;
    }

    /**
     * @param scaffoldProperty
     *            the scaffoldProperty to set
     */
    public void setScaffoldProperty(boolean scaffoldProperty) {
        this.scaffoldProperty = scaffoldProperty;
    }

    @Override
    public int compareTo(PropertyDefinition o) {
        return getKey().compareTo(o.getKey());
    }
    
    @Override
    public String toString() {
        return getTitle();
    }
}
