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

import javax.swing.table.TableCellRenderer;

import com.l2fprod.common.beans.editor.AbstractPropertyEditor;

/**
 * Links a property to an editor and a renderer class.
 * 
 * @see PropertiesBeanInfo#PropertiesBeanInfo(Class, Object[])
 * @see PropertiesBeanInfo#setPropertyAssociatedClasses(PropertyAssociatedClasses)
 * 
 * @author Thorsten Flügel
 */
public class PropertyAssociatedClasses {
    private String propertyName;
    private Class<? extends AbstractPropertyEditor> editorClass;
    private Class<? extends TableCellRenderer> rendererClass;

    /**
     * @param propertyName
     *            the name of the property
     * @param editorClass
     *            the editor which shall be used to edit the property
     * @param rendererClass
     *            the renderer which shall be used to show the property
     */
    public PropertyAssociatedClasses(String propertyName, Class<? extends AbstractPropertyEditor> editorClass,
            Class<? extends TableCellRenderer> rendererClass) {
        this.propertyName = propertyName;
        this.editorClass = editorClass;
        this.rendererClass = rendererClass;
    }

    /**
     * @return the name of the property
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @return the editor class which shall be used to edit the property
     */
    public Class<? extends AbstractPropertyEditor> getEditorClass() {
        return editorClass;
    }

    /**
     * @return the renderer class which shall be used to show the property
     */
    public Class<? extends TableCellRenderer> getRendererClass() {
        return rendererClass;
    }
}