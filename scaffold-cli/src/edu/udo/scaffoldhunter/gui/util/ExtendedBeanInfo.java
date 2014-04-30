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

package edu.udo.scaffoldhunter.gui.util;

import java.beans.PropertyDescriptor;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;

/**
 * A BeanInfo which supports name, description and categories in property
 * descriptions.
 * 
 * @author Thorsten Flügel
 */
public class ExtendedBeanInfo extends BaseBeanInfo {
    /**
     * @param beanClass
     *            the class whose property information will be stored
     */
    public ExtendedBeanInfo(Class<?> beanClass) {
        super(beanClass);
    }

    /**
     * Adds the property to the bean description and extracts the property
     * category as well as the human readable name and description from the
     * resource bundle of the bean class.
     * 
     * The resource bundle of a bean class X is either a class named XRB or a
     * file XRB.properties and must exist in the same package as the bean class.
     * Localisation is also possible by appending a locale string as in
     * {@link ResourceBundle#getBundle(String)}.
     * 
     * @param propertyName
     *            name of the property whose description will be extracted from
     *            the resource bundle
     */
    @Override
    public ExtendedPropertyDescriptor addProperty(String propertyName) {
        ExtendedPropertyDescriptor descriptor = super.addProperty(propertyName);
        try {
            descriptor.setCategory(getResources().getString(
                    propertyName + ".category"));
        } catch (MissingResourceException e) {
            // ignore, the resource may not be provided
        }
        return descriptor;
    }

    /**
     * Retrieves a property description to allow modification in subclasses.
     * 
     * @param propertyName
     *            the name of the property which shall be retrieved
     * @return the property descriptor. null if there is no property with the
     *         passed name
     */
    protected PropertyDescriptor getProperty(String propertyName) {
        for (PropertyDescriptor property : getPropertyDescriptors()) {
            if (propertyName.equals(property.getName())) {
                return property;
            }
        }
        return null;
    }

}