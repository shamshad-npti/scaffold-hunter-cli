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

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.table.TableCellRenderer;

import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

/**
 * A BeanInfo with automatic extraction of properties.
 * 
 * @author Thorsten Flügel
 */
public class PropertiesBeanInfo extends ExtendedBeanInfo {
    /**
     * Creates a BeanInfo containing descriptions of all properties of the bean
     * class. The properties are extracted by using reflection. For each public
     * member method named getX or isX a property X starting with a lower case
     * character is inserted, if the bean contains a field with the property 
     * name and that field is marked with a {@link ConfigProperty} annotation.
     * 
     * @param beanClass
     *            the class whose property information will be stored
     */
    public PropertiesBeanInfo(Class<?> beanClass) {
        super(beanClass);
        for (Method method : beanClass.getMethods()) {
            String property = null;
            if (method.getName().startsWith("get")) {
                property = method.getName().substring(3);
            } else if (method.getName().startsWith("is")) {
                property = method.getName().substring(2);
            }
            if (property != null) {
                Field field;
                try {
                    field = beanClass.getDeclaredField(Character.toLowerCase(property.charAt(0)) + property.substring(1));
                } catch (SecurityException e) {
                    field = null;
                } catch (NoSuchFieldException e) {
                    field = null;
                }

                boolean isConfigProperty = field != null ? field.isAnnotationPresent(ConfigProperty.class) : false;

                if (isConfigProperty) {
                    addProperty(property.substring(0, 1).toLowerCase() + property.substring(1));
                }
            }
        }
    }

    /**
     * Creates a BeanInfo only containing information about the passed
     * properties.
     * 
     * @param beanClass
     *            the class whose property information will be stored
     * @param properties
     *            each member can be an instance of {@link String} or
     *            {@link PropertyAssociatedClasses}. Strings state the name of
     *            the property. A PropertyEditorClass contains both the name and
     *            the editor class which will be used for this property.
     * @throws IllegalArgumentException
     *             a property was neither an instance of {@link String} nor of
     *             {@link PropertyAssociatedClasses}
     */
    public PropertiesBeanInfo(Class<?> beanClass, Object[] properties) throws IllegalArgumentException {
        super(beanClass);
        for (Object property : properties) {
            if (property instanceof String) {
                addProperty((String) property);
            } else if (property instanceof PropertyAssociatedClasses) {
                PropertyAssociatedClasses p = (PropertyAssociatedClasses) property;
                addProperty(p.getPropertyName());
                setPropertyAssociatedClasses(p);
            } else {
                throw new IllegalArgumentException(property.getClass() + " isn't supported");
            }
        }
    }

    /**
     * Sets an editor class and a renderer class for a property. A property with
     * the name stored in {@code propertyAssociation} must have been added
     * before. This method is best used in conjunction with
     * {@link PropertiesBeanInfo#PropertiesBeanInfo(Class)} to set the editor
     * class for a few properties.
     * 
     * @param propertyAssociation
     *            the editor and renderer classes associated with the property.
     *            if a class is null, the default class will be used
     */
    protected void setPropertyAssociatedClasses(PropertyAssociatedClasses propertyAssociation) {
        setPropertyAssociatedClasses(propertyAssociation.getPropertyName(), propertyAssociation.getEditorClass(),
                propertyAssociation.getRendererClass());
    }

    /**
     * Sets an editor class and a renderer class for a property. A property with
     * the name {@code propertyName} must have been added before. This method is
     * best used in conjunction with
     * {@link PropertiesBeanInfo#PropertiesBeanInfo(Class)} to set the editor
     * class for a few properties.
     * 
     * @param propertyName
     *            the name of the property
     * @param editorClass
     *            the editor class that will be used to edit the property. if
     *            null, the default editor will be used
     * @param rendererClass
     *            the renderer class that will be used to show the property. if
     *            null, the default renderer will be used
     */
    protected void setPropertyAssociatedClasses(String propertyName,
            Class<? extends AbstractPropertyEditor> editorClass, Class<? extends TableCellRenderer> rendererClass) {
        ExtendedPropertyDescriptor descriptor = (ExtendedPropertyDescriptor) getProperty(propertyName);
        if (descriptor != null) {
            descriptor.setPropertyEditorClass(editorClass);
            descriptor.setPropertyTableRendererClass(rendererClass);
        } else {
            throw new IllegalArgumentException("The property " + propertyName + " was not found in "
                    + getBeanDescriptor().getBeanClass());
        }
    }

    /**
     * Sets the editor and renderer classes for some properties.
     * 
     * @see PropertiesBeanInfo#setPropertyAssociatedClasses(PropertyAssociatedClasses)
     * 
     * @param propertyAssociations
     *            the editor and renderer classes associated with the properties
     */
    protected void setPropertiesAssociatedClasses(PropertyAssociatedClasses[] propertyAssociations) {
        for (PropertyAssociatedClasses p : propertyAssociations) {
            setPropertyAssociatedClasses(p);
        }
    }
}