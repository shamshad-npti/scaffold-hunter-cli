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

import javax.swing.JPanel;

import com.l2fprod.common.propertysheet.PropertySheet;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.l2fprod.common.swing.LookAndFeelTweaks;

import edu.udo.scaffoldhunter.gui.util.BeanBinder.BeanChangeListener;


/**
 * A panel containing a {@link PropertySheetPanel} used to edit a bean.
 * 
 * @author Thorsten Flügel
 */
public class PropertySheetPage extends JPanel {
    private final PropertySheetPanel sheet;
    private final BeanBinder beanBinder;

    /**
     * Creates the {@link PropertySheetPanel}, enables sorting and categories of
     * properties and creates the {@link BeanBinder} which links the bean to the
     * sheet.
     * 
     * @param data
     *            will be editable via a sheet
     * @param id
     *            Can be used to identify the object the bean belongs to. May be
     *            {@code null}.
     */
    public PropertySheetPage(Object data, Object id) {
        setLayout(LookAndFeelTweaks.createVerticalPercentLayout());

        sheet = new PropertySheetPanel();
        sheet.setMode(PropertySheet.VIEW_AS_CATEGORIES);
        sheet.setDescriptionVisible(true);
        sheet.setSortingCategories(true);
        sheet.setSortingProperties(true);
        sheet.setRestoreToggleStates(true);
        sheet.setToolBarVisible(false);
        sheet.setBorder(null);
        add(sheet, "*");

        beanBinder = new BeanBinder(data, id, sheet);
    }
    
    /**
     * @return the id associated with the bean
     */
    public Object getId() {
        return beanBinder.getId();
    }

    /**
     * @param listener
     *            will be informed about changes of the bean
     */
    public void addBeanChangedListener(BeanChangeListener listener) {
        beanBinder.addBeanChangeListener(listener);
    }

    /**
     * @param listener
     *            will no longer be informed about changes of the bean
     */
    public void removePropertyChangeListener(BeanChangeListener listener) {
        beanBinder.removeBeanChangeListener(listener);
    }

}
