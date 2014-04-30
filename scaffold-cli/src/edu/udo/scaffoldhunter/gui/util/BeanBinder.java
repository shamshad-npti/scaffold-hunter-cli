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

import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.EventListener;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

/**
 * A BeanBinder links a PropertySheetPanel to a bean, so that the bean is
 * updated everytime a property in the sheet changes.
 * 
 * The bean may dismiss the change by throwing a PropertyVetoException.
 * 
 * Listeners can be registered to be informed about the successful changes.
 * 
 * @author Thorsten Flügel
 */
public class BeanBinder {

    private final Object bean;
    private final PropertySheetPanel sheet;
    private final PropertyChangeListener listener;
    private final Object id;

    /**
     * @return the id associated with the bean
     */
    public Object getId() {
        return id;
    }

    private EventListenerList beanChangeListeners;

    /**
     * The listener class for the bean changed event.
     * 
     * @author Thorsten Flügel
     * 
     */
    public interface BeanChangeListener extends EventListener {
        /**
         * Called when the bean has changed.
         * 
         * @param bean
         *            the changed bean
         * @param id
         *            Can be used to identify the object the bean belongs to.
         *            May be {@code null}.
         */
        void processEvent(Object bean, Object id);
    }

    /**
     * Create the BeanBinder and build the property sheet page according to the
     * default BeanInfo class for the bean.
     * 
     * A BeanInfo class XBeanInfo must exist for each bean class X.
     * 
     * @param bean
     *            will be linked to the sheet
     * @param id
     *            Can be used to identify the object the bean belongs to. May be
     *            {@code null}.
     * @param sheet
     *            will be filled with the beans properties
     */
    public BeanBinder(Object bean, Object id, PropertySheetPanel sheet) {
        this(bean, id, sheet, new BeanInfoResolver().getBeanInfo(bean));
    }

    /**
     * Create the BeanBinder and build the property sheet page according to the
     * passed BeanInfo object.
     * 
     * @param bean
     *            will be linked to the sheet
     * @param id
     *            Can be used to identify the object the bean belongs to. May be
     *            {@code null}.
     * @param sheet
     *            will be filled with the beans properties
     * @param beanInfo
     *            contains informations about the properties of the bean
     */
    public BeanBinder(Object bean, Object id, PropertySheetPanel sheet, BeanInfo beanInfo) {
        this.bean = bean;
        this.id = id;
        this.sheet = sheet;
        beanChangeListeners = new EventListenerList();

        if (beanInfo != null) {
            sheet.setProperties(beanInfo.getPropertyDescriptors());
            sheet.readFromObject(bean);

            listener = new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    Property property = (Property) evt.getSource();
                    try {
                        property.writeToObject(BeanBinder.this.bean);
                        for (BeanChangeListener listener : BeanBinder.this.beanChangeListeners
                                .getListeners(BeanChangeListener.class)) {
                            listener.processEvent(BeanBinder.this.bean, BeanBinder.this.id);
                        }
                    } catch (RuntimeException e) {
                        // handle PropertyVetoException and restore previous
                        // value
                        if (e.getCause() instanceof PropertyVetoException) {
                            PropertyVetoException ex = (PropertyVetoException) e.getCause();
                            // inform user about this error
                            UIManager.getLookAndFeel().provideErrorFeedback(BeanBinder.this.sheet);
                            JOptionPane
                                    .showMessageDialog(
                                            BeanBinder.this.sheet,
                                            "<html>The entered value was not accepted by the property "
                                                    + ex.getPropertyChangeEvent().getPropertyName() + ":<br>"
                                                    + ex.getMessage());
                            property.setValue(evt.getOldValue());
                        }
                    }
                }
            };
            sheet.addPropertySheetChangeListener(listener);
        } else {
            listener = null;
        }
    }

    /**
     * Adds an external listener for successful changes of the bean.
     * 
     * @param listener
     *            will be notified about the changes
     */
    public void addBeanChangeListener(BeanChangeListener listener) {
        beanChangeListeners.add(BeanChangeListener.class, listener);
    }

    /**
     * Removes an external listener for successful changes of the bean.
     * 
     * @param listener
     *            will no longer be notified about the changes
     */
    public void removeBeanChangeListener(BeanChangeListener listener) {
        beanChangeListeners.remove(BeanChangeListener.class, listener);
    }

}
