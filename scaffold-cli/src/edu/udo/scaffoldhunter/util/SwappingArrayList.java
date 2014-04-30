package edu.udo.scaffoldhunter.util;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This is basically an simple Array List, but it adds a Method to remove
 * Elements in O(1) if the ordering is not required.
 * 
 * @author Till Schäfer
 * @param <E>
 *            the type of elements
 */
public class SwappingArrayList<E> extends ArrayList<E> {
    private static final long serialVersionUID = -3859463217479134778L;

    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public SwappingArrayList() {
        super();
    }
    
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public SwappingArrayList(int initialCapacity) {
        super(initialCapacity);
    }
    
    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public SwappingArrayList(Collection<? extends E> c){
        super(c);
    }
    
    /**
     * Removes the element at index, by swapping it with the last element in the
     * List and removing the last element afterwards.
     * 
     * Requires onyl O(1) instead of a O(n) for a direct remove.
     * 
     * Changes the ordering of the elements!
     * 
     * @param index
     *            the index of the element to remove
     */
    public void swapAndRemove(int index) {
        assert size() > index; 
        
        int lastIndex = size() - 1;
        set(index, get(lastIndex));
        remove(lastIndex);
    }
}
