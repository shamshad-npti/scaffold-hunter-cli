/*
 * http://www.developer.com/java/other/article.php/3621276/Typed-and-Targeted-Property-Change-Events-in-Java.htm
 */

package edu.udo.scaffoldhunter.util;

import java.beans.PropertyChangeEvent;


/**
 * A property value change event is a Java Beans property change event
 * retrofitted to use generics to cast to proper value type.
 * 
 * @param <V>
 *            The type of property value.
 *            
 * @author Garret Wilson
 */
public class GenericPropertyChangeEvent<V> extends PropertyChangeEvent {
    /**
     * @param source
     * @param propertyName
     * @param oldValue
     * @param newValue
     */
    public GenericPropertyChangeEvent(final Object source, final String propertyName, final V oldValue, V newValue) {
        super(source, propertyName, oldValue, newValue);
    }

    /**
     * @param ev
     */
    @SuppressWarnings("unchecked")
    public GenericPropertyChangeEvent(final PropertyChangeEvent ev) {
        this(ev.getSource(), ev.getPropertyName(), (V)ev.getOldValue(), (V)ev.getNewValue());
        setPropagationId(ev.getPropagationId());
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getOldValue() {
        return (V) super.getOldValue();
    }

    @Override
    @SuppressWarnings("unchecked")
    public V getNewValue() {
        return (V) super.getNewValue();
    }
}