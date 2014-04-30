/*
 * http://www.developer.com/java/other/article.php/3621276/Typed-and-Targeted-Property-Change-Events-in-Java.htm
 */

package edu.udo.scaffoldhunter.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * A Java Beans property change listener retrofitted to use generics to cast to
 * proper value type.
 * 
 * @param <V>
 *            The type of property value.
 *            
 * @author Garret Wilson
 */
interface GenericPropertyChangeListenerInterface<V> extends PropertyChangeListener {
    /**
     * @param ev
     */
    public void propertyChange(final GenericPropertyChangeEvent<V> ev);
}


/**
 * A Java Beans property change listener retrofitted to use generics to cast to
 * proper value type.
 * 
 * @param <V>
 *            The type of property value.
 *            
 * @author Garret Wilson
 */
public abstract class GenericPropertyChangeListener<V> implements GenericPropertyChangeListenerInterface<V> {

    /**
     * Called when a bound property is changed. This non-generics version calls
     * the generic version, creating a new event if necessary. No checks are
     * made at compile time to ensure the given event actually supports the
     * given generic type.
     * 
     * @param ev
     *            An event object describing the event source, the property that
     *            has changed, and its old and new values.
     * @see GenericPropertyChangeListenerInterface#propertyChange
     *      (GenericPropertyChangeEvent)
     */
    @Override
    @SuppressWarnings("unchecked")
    public final void propertyChange(final PropertyChangeEvent ev) {
        propertyChange((GenericPropertyChangeEvent<V>) getGenericPropertyChangeEvent(ev));
    }

    /**
     * Converts a property change event to a generics-aware property value
     * change event.
     * 
     * @param <T> 
     * 
     * @param ev
     *            An event object describing the event source, the property that
     *            has changed, and its old and new values.
     * @return A generics-aware property change event, either cast from the
     *         provided object or created from the provided object's values as
     *         appropriate.
     */
    @SuppressWarnings("unchecked")
    public static <T> GenericPropertyChangeEvent<T> getGenericPropertyChangeEvent(
            final PropertyChangeEvent ev) {
        if (ev instanceof GenericPropertyChangeEvent) {
            return (GenericPropertyChangeEvent<T>) ev;
        } else {
            return new GenericPropertyChangeEvent<T>(ev);
        }
    }
}