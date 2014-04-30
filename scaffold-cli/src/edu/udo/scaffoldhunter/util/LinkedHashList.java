/*
 * Scaffold Hunter
 * Copyright (C) 1997, 2011, Oracle and/or its affiliates.
 * Copyright (C) 2012 Till Schäfer
 * See the file README.txt in the root directory of the Scaffold Hunter
 * source tree for details.
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

package edu.udo.scaffoldhunter.util;

import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * The LinkedHashList Provides the same Operations as a {@link java.util.List},
 * but allows to access an element, remove an element, start an iterator at an
 * elements position and querying if the list contains an element in constant
 * time.
 * 
 * @author Till Schäfer
 * 
 * @param <E>
 *            the type of stored {@link Object}s
 * 
 */
public class LinkedHashList<E> implements List<E> {
    /**
     * pointer to the head of the {@link List}
     */
    transient Node<E> first;
    /**
     * pointer to the tail of the {@link List}
     */
    transient Node<E> last;
    /**
     * HashMap that contains all elements
     * 
     * stored elem -> set of nodes that contain the elem
     */
    HashMap<E, HashSet<Node<E>>> map;
    /**
     * Modification counter
     */
    transient int modCount;
    /**
     * Count of all objects in this list
     */
    transient int size;

    /**
     * Constructor
     */
    public LinkedHashList() {
        map = new HashMap<E, HashSet<Node<E>>>();
    }

    /**
     * Constructor
     * 
     * @param initialCapacity
     *            the initial capacity of the list
     */
    public LinkedHashList(int initialCapacity) {
        map = new HashMap<E, HashSet<Node<E>>>(initialCapacity);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#size()
     */
    @Override
    public int size() {
        return size;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * Returns an iterator over the elements in this list (in proper sequence).
     * <p>
     * 
     * This implementation merely returns a list iterator over the list.
     * 
     * @return an iterator over the elements in this list (in proper sequence)
     */
    @Override
    public Iterator<E> iterator() {
        return listIterator();
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element).
     * 
     * <p>
     * The returned array will be "safe" in that no references to it are
     * maintained by this list. (In other words, this method must allocate a new
     * array). The caller is thus free to modify the returned array.
     * 
     * <p>
     * This method acts as bridge between array-based and collection-based APIs.
     * 
     * @return an array containing all of the elements in this list in proper
     *         sequence
     */
    @Override
    public Object[] toArray() {
        Object[] result = new Object[size];
        int i = 0;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;
        return result;
    }

    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element); the runtime type of the returned
     * array is that of the specified array. If the list fits in the specified
     * array, it is returned therein. Otherwise, a new array is allocated with
     * the runtime type of the specified array and the size of this list.
     * 
     * <p>
     * If the list fits in the specified array with room to spare (i.e., the
     * array has more elements than the list), the element in the array
     * immediately following the end of the list is set to {@code null}. (This
     * is useful in determining the length of the list <i>only</i> if the caller
     * knows that the list does not contain any null elements.)
     * 
     * <p>
     * Like the {@link #toArray()} method, this method acts as bridge between
     * array-based and collection-based APIs. Further, this method allows
     * precise control over the runtime type of the output array, and may, under
     * certain circumstances, be used to save allocation costs.
     * 
     * <p>
     * Suppose {@code x} is a list known to contain only strings. The following
     * code can be used to dump the list into a newly allocated array of
     * {@code String}:
     * 
     * <pre>
     * String[] y = x.toArray(new String[0]);
     * </pre>
     * 
     * Note that {@code toArray(new Object[0])} is identical in function to
     * {@code toArray()}.
     * 
     * @param a
     *            the array into which the elements of the list are to be
     *            stored, if it is big enough; otherwise, a new array of the
     *            same runtime type is allocated for this purpose.
     * @return an array containing the elements of the list
     * @throws ArrayStoreException
     *             if the runtime type of the specified array is not a supertype
     *             of the runtime type of every element in this list
     * @throws NullPointerException
     *             if the specified array is null
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < size)
            a = (T[]) java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);
        int i = 0;
        Object[] result = a;
        for (Node<E> x = first; x != null; x = x.next)
            result[i++] = x.item;

        if (a.length > size)
            a[size] = null;

        return a;
    }

    /**
     * Appends the specified element to the end of this list.
     * 
     * @param e
     *            element to be appended to this list
     * @return {@code true} (as specified by {@link Collection#add})
     */
    @Override
    public boolean add(E e) {
        linkLast(e);
        return true;
    }

    /**
     * Removes all occurrences of the specified element from this list, if it is
     * present. If this list does not contain the element, it is unchanged.
     * Returns {@code true} if this list contained the specified element (or
     * equivalently, if this list changed as a result of the call).
     * 
     * This operation has O(1) complexity.
     * 
     * @param o
     *            element to be removed from this list, if present
     * @return {@code true} if this list contained the specified element
     */
    @Override
    public boolean remove(Object o) {
        HashSet<Node<E>> nodes = map.get(o);
        if (nodes == null) {
            return false;
        }
        LinkedList<Node<E>> removeList = new LinkedList<Node<E>>();
        for (Node<E> node : nodes) {
            if (node != null) {
                removeList.add(node);
            }
        }
        for (Node<E> node : removeList) {
            unlink(node);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This implementation iterates over the specified collection, checking each
     * element returned by the iterator in turn to see if it's contained in this
     * collection. If all elements are so contained <tt>true</tt> is returned,
     * otherwise <tt>false</tt>.
     * 
     * @throws ClassCastException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             {@inheritDoc}
     * @see #contains(Object)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        for (Object o : c) {
            if (!contains(o)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This implementation iterates over the specified collection, and adds each
     * object returned by the iterator to this collection, in turn.
     * 
     * @throws UnsupportedOperationException
     *             {@inheritDoc}
     * @throws ClassCastException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             {@inheritDoc}
     * @throws IllegalArgumentException
     *             {@inheritDoc}
     * @throws IllegalStateException
     *             {@inheritDoc}
     * 
     * @see #add(Object)
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean modified = false;
        for (E e : c)
            if (add(e))
                modified = true;
        return modified;
    }

    /**
     * Inserts all of the elements in the specified collection into this list,
     * starting at the specified position. Shifts the element currently at that
     * position (if any) and any subsequent elements to the right (increases
     * their indices). The new elements will appear in the list in the order
     * that they are returned by the specified collection's iterator.
     * 
     * @param index
     *            index at which to insert the first element from the specified
     *            collection
     * @param c
     *            collection containing elements to be added to this list
     * @return {@code true} if this list changed as a result of the call
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             if the specified collection is null
     */
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        checkPositionIndex(index);

        Node<E> pred, succ;
        if (index == size) {
            succ = null;
            pred = last;
        } else {
            succ = node(index);
            pred = succ.prev;
        }

        for (E e : c) {
            Node<E> newNode = new Node<E>(e, pred, null);
            if (pred == null) {
                first = newNode;
            } else {
                pred.next = newNode;
            }
            pred = newNode;

            mapAdd(newNode);
        }

        if (succ == null) {
            last = pred;
        } else {
            pred.next = succ;
            succ.prev = pred;
        }

        size += c.size();
        modCount++;
        return true;
    }

    /**
     * Removes every element in the list that is an element of c.
     * 
     * This operation has O(|c|) complexity.
     * 
     * @param c
     *            collection containing elements to be removed from this list
     * @return {@code true} if this list contained at least on of specified
     *         elements
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        boolean modified = false;

        for (Object o : c) {
            if (remove(o)) {
                modified = true;
            }
        }
        return modified;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This implementation iterates over this collection, checking each element
     * returned by the iterator in turn to see if it's contained in the
     * specified collection. If it's not so contained, it's removed from this
     * collection with the iterator's <tt>remove</tt> method.
     * 
     * Complexity: O(n)
     * 
     * <p>
     * Note that this implementation will throw an
     * <tt>UnsupportedOperationException</tt> if the iterator returned by the
     * <tt>iterator</tt> method does not implement the <tt>remove</tt> method
     * and this collection contains one or more elements not present in the
     * specified collection.
     * 
     * @throws UnsupportedOperationException
     *             {@inheritDoc}
     * @throws ClassCastException
     *             {@inheritDoc}
     * @throws NullPointerException
     *             {@inheritDoc}
     * 
     * @see #remove(Object)
     * @see #contains(Object)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        boolean modified = false;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                modified = true;
            }
        }
        return modified;
    }

    /**
     * Removes all of the elements from this list. The list will be empty after
     * this call returns.
     * 
     * Complexity: O(n)
     */
    @Override
    public void clear() {
        // Clearing all of the links between nodes is "unnecessary", but:
        // - helps a generational GC if the discarded nodes inhabit
        // more than one generation
        // - is sure to free memory even if there is a reachable Iterator
        for (Node<E> x = first; x != null;) {
            Node<E> next = x.next;
            x.item = null;
            x.next = null;
            x.prev = null;
            x = next;
        }
        first = last = null;
        size = 0;
        map.clear();
        modCount++;
    }

    /**
     * Returns the element at the specified position in this list.
     * 
     * Complexity: O(n)
     * 
     * @param index
     *            index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @Override
    public E get(int index) {
        checkElementIndex(index);
        return node(index).item;
    }

    /**
     * Returns an arbitrary element which the same hash code or null if the
     * element does not exist.
     * 
     * @param element
     *            the element to search for
     * @return the element from list with the same hash code
     */
    public E get(E element) {
        Node<E> node = map.get(element).iterator().next();
        return node == null ? null : node.item;
    }

    /**
     * Replaces the element at the specified position in this list with the
     * specified element.
     * 
     * Complexity: O(n)
     * 
     * @param index
     *            index of the element to replace
     * @param element
     *            element to be stored at the specified position
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @Override
    public E set(int index, E element) {
        checkElementIndex(index);
        Node<E> x = node(index);
        E oldVal = x.item;

        mapRemove(x);
        x.item = element;
        mapAdd(x);

        return oldVal;
    }

    /**
     * Inserts the specified element at the specified position in this list.
     * Shifts the element currently at that position (if any) and any subsequent
     * elements to the right (adds one to their indices).
     * 
     * Complexity: O(n)
     * 
     * @param index
     *            index at which the specified element is to be inserted
     * @param element
     *            element to be inserted
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @Override
    public void add(int index, E element) {
        checkPositionIndex(index);

        if (index == size) {
            linkLast(element);
        } else {
            linkBefore(element, node(index));
        }
    }

    /**
     * Removes the element at the specified position in this list. Shifts any
     * subsequent elements to the left (subtracts one from their indices).
     * Returns the element that was removed from the list.
     * 
     * @param index
     *            the index of the element to be removed
     * @return the element previously at the specified position
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     */
    @Override
    public E remove(int index) {
        checkElementIndex(index);
        return unlink(node(index));
    }

    /**
     * Returns the index of the first occurrence of the specified element in
     * this list, or -1 if this list does not contain the element. More
     * formally, returns the lowest index {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     * 
     * Complexity: O(n)
     * 
     * @param o
     *            element to search for
     * @return the index of the first occurrence of the specified element in
     *         this list, or -1 if this list does not contain the element
     */
    @Override
    public int indexOf(Object o) {
        int index = 0;
        if (o == null) {
            for (Node<E> x = first; x != null; x = x.next) {
                if (x.item == null) {
                    return index;
                }
                index++;
            }
        } else {
            for (Node<E> x = first; x != null; x = x.next) {
                if (o.equals(x.item)) {
                    return index;
                }
                index++;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the last occurrence of the specified element in this
     * list, or -1 if this list does not contain the element. More formally,
     * returns the highest index {@code i} such that
     * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
     * or -1 if there is no such index.
     * 
     * @param o
     *            element to search for
     * @return the index of the last occurrence of the specified element in this
     *         list, or -1 if this list does not contain the element
     */
    @Override
    public int lastIndexOf(Object o) {
        int index = size;
        if (o == null) {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (x.item == null) {
                    return index;
                }
            }
        } else {
            for (Node<E> x = last; x != null; x = x.prev) {
                index--;
                if (o.equals(x.item)) {
                    return index;
                }
            }
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * This implementation returns {@code listIterator(0)}.
     * 
     * @see #listIterator(int)
     */
    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    /**
     * Returns a list-iterator of the elements in this list (in proper
     * sequence), starting at the specified position in the list. Obeys the
     * general contract of {@code List.listIterator(int)}.
     * <p>
     * 
     * <br>
     * Complexity: O(n) (O(1) if start or end index)
     * 
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own {@code remove} or {@code add} methods,
     * the list-iterator will throw a {@code ConcurrentModificationException}.
     * Thus, in the face of concurrent modification, the iterator fails quickly
     * and cleanly, rather than risking arbitrary, non-deterministic behavior at
     * an undetermined time in the future.
     * 
     * @param index
     *            index of the first element to be returned from the
     *            list-iterator (by a call to {@code next})
     * @return a ListIterator of the elements in this list (in proper sequence),
     *         starting at the specified position in the list
     * @throws IndexOutOfBoundsException
     *             {@inheritDoc}
     * @see List#listIterator(int)
     */
    @Override
    public ListIterator<E> listIterator(int index) {
        checkPositionIndex(index);
        return new ListItr(index);
    }

    /**
     * Returns a ListIterator of the elements in this list (in proper sequence),
     * starting at the specified object in the list. If there is more than one
     * object an arbitrary object is chosen. Calling Iterator.next will return
     * the element after e. It is possible to call Iterator.set to replace e.
     * 
     * <br>
     * Complexity: O(1)
     * 
     * <br>
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own {@code remove} or {@code add} methods,
     * the list-iterator will throw a {@code ConcurrentModificationException}.
     * Thus, in the face of concurrent modification, the iterator fails quickly
     * and cleanly, rather than risking arbitrary, non-deterministic behavior at
     * an undetermined time in the future.
     * 
     * @param e
     *            object that should be the first element to be returned from
     *            the list-iterator (by a call to {@code next})
     * @return a ListIterator of the elements in this list (in proper sequence),
     *         starting at the specified position in the list
     * @see List#listIterator(int)
     */
    public ListIterator<E> listIterator(E e) {
        checkContains(e);
        Node<E> node = map.get(e).iterator().next();
        return new ListItr(node);
    }

    /**
     * Returns a list of all list-iterators of the elements in this list (in
     * proper sequence), starting at the specified object in the list. Calling
     * Iterator.next will return the element after e. It is possible to call
     * Iterator.set to replace e.
     * 
     * <br>
     * Complexity: O(|e|)
     * 
     * <br>
     * The list-iterator is <i>fail-fast</i>: if the list is structurally
     * modified at any time after the Iterator is created, in any way except
     * through the list-iterator's own {@code remove} or {@code add} methods,
     * the list-iterator will throw a {@code ConcurrentModificationException}.
     * Thus, in the face of concurrent modification, the iterator fails quickly
     * and cleanly, rather than risking arbitrary, non-deterministic behavior at
     * an undetermined time in the future.
     * 
     * @param e
     *            object that should be the first element to be returned from
     *            the list-iterator (by a call to {@code next})
     * @return a ListIterator of the elements in this list (in proper sequence),
     *         starting at the specified position in the list
     * @throws IndexOutOfBoundsException
     * @see List#listIterator(int)
     */
    public List<ListIterator<E>> listIterators(E e) {
        checkContains(e);
        HashSet<Node<E>> nodes = map.get(e);
        LinkedList<ListIterator<E>> iterators = new LinkedList<ListIterator<E>>();
        for (Node<E> node : nodes) {
            iterators.add(new ListItr(node));
        }
        return iterators;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.List#subList(int, int)
     */
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

    /**
     * Removes and returns the first element from this list.
     * 
     * @return the first element from this list
     * @throws NoSuchElementException
     *             if this list is empty
     */
    public E removeFirst() {
        final Node<E> f = first;
        if (f == null)
            throw new NoSuchElementException();
        return unlinkFirst(f);
    }

    /**
     * Removes and returns the last element from this list.
     * 
     * @return the last element from this list
     * @throws NoSuchElementException
     *             if this list is empty
     */
    public E removeLast() {
        final Node<E> l = last;
        if (l == null)
            throw new NoSuchElementException();
        return unlinkLast(l);
    }

    /**
     * Tells if the argument is the index of an existing element.
     */
    private boolean isElementIndex(int index) {
        return index >= 0 && index < size;
    }

    /**
     * Tells if the argument is the index of a valid position for an iterator or
     * an add operation.
     */
    private boolean isPositionIndex(int index) {
        return index >= 0 && index <= size;
    }

    /**
     * Constructs an IndexOutOfBoundsException detail message. Of the many
     * possible refactorings of the error handling code, this "outlining"
     * performs best with both server and client VMs.
     */
    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }

    private void checkElementIndex(int index) {
        if (!isElementIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void checkPositionIndex(int index) {
        if (!isPositionIndex(index))
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void checkContains(E obj) {
        if (!contains(obj))
            throw new NoSuchElementException();
    }

    /**
     * Returns the (non-null) Node at the specified element index.
     * 
     * Complexity O(n)
     */
    private Node<E> node(int index) {
        assert isElementIndex(index);

        if (index < (size >> 1)) {
            Node<E> x = first;
            for (int i = 0; i < index; i++)
                x = x.next;
            return x;
        } else {
            Node<E> x = last;
            for (int i = size - 1; i > index; i--)
                x = x.prev;
            return x;
        }
    }

    /**
     * Unlinks non-null node x.
     */
    private E unlink(Node<E> x) {
        assert x != null;

        final E element = x.item;
        final Node<E> next = x.next;
        final Node<E> prev = x.prev;

        if (prev == null) {
            first = next;
        } else {
            prev.next = next;
            x.prev = null;
        }

        if (next == null) {
            last = prev;
        } else {
            next.prev = prev;
            x.next = null;
        }

        mapRemove(x);
        x.item = null;

        size--;
        modCount++;

        return element;
    }

    /**
     * Unlinks non-null first node f.
     */
    private E unlinkFirst(Node<E> f) {
        assert f == first && f != null;

        final E element = f.item;
        final Node<E> next = f.next;
        mapRemove(f);
        f.item = null;
        f.next = null; // help GC
        first = next;
        if (next == null) {
            last = null;
        } else {
            next.prev = null;
        }

        size--;
        modCount++;

        return element;
    }

    /**
     * Unlinks non-null last node l.
     */
    private E unlinkLast(Node<E> l) {
        assert l == last && l != null;

        final E element = l.item;
        final Node<E> prev = l.prev;
        mapRemove(l);
        l.item = null;
        l.prev = null; // help GC
        last = prev;
        if (prev == null) {
            first = null;
        } else {
            prev.next = null;
        }

        size--;
        modCount++;

        return element;
    }

    /**
     * Links e as first element.
     */
    @SuppressWarnings("unused")
    private void linkFirst(E e) {
        final Node<E> f = first;
        final Node<E> newNode = new Node<E>(e, null, f);
        first = newNode;
        if (f == null) {
            last = newNode;
        } else {
            f.prev = newNode;
        }

        mapAdd(newNode);

        size++;
        modCount++;
    }

    /**
     * Links e as last element.
     */
    private void linkLast(E e) {
        final Node<E> l = last;
        final Node<E> newNode = new Node<E>(e, l, null);
        last = newNode;
        if (l == null) {
            first = newNode;
        } else {
            l.next = newNode;
        }

        mapAdd(newNode);

        size++;
        modCount++;
    }

    /**
     * Inserts element e before non-null Node succ.
     */
    private void linkBefore(E e, Node<E> succ) {
        assert succ != null;

        final Node<E> pred = succ.prev;
        final Node<E> newNode = new Node<E>(e, pred, succ);
        succ.prev = newNode;
        if (pred == null) {
            first = newNode;
        } else {
            pred.next = newNode;
        }

        mapAdd(newNode);

        size++;
        modCount++;
    }

    /**
     * Adds an Node to the map
     * 
     * @param node
     */
    private void mapAdd(Node<E> node) {
        if (map.containsKey(node.item)) {
            map.get(node.item).add(node);
        } else {
            HashSet<Node<E>> newSet = new HashSet<Node<E>>();
            newSet.add(node);
            map.put(node.item, newSet);
        }
    }

    /**
     * Removes a Node from the map
     * 
     * @param node
     */
    private void mapRemove(Node<E> node) {
        HashSet<Node<E>> nodes = map.get(node.item);
        nodes.remove(node);
        if (nodes.size() == 0) {
            map.remove(node.item);
        }
    }

    private class ListItr implements ListIterator<E> {
        private Node<E> lastReturned = null;
        private Node<E> next;
        private int expectedModCount = modCount;

        ListItr(int index) {
            assert isPositionIndex(index);

            next = (index == size) ? null : node(index);
        }

        ListItr(Node<E> node) {
            lastReturned = node;
            next = node.next;
        }

        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public E next() {
            checkForComodification();
            if (!hasNext())
                throw new NoSuchElementException();

            lastReturned = next;
            next = next.next;
            return lastReturned.item;
        }

        @Override
        public boolean hasPrevious() {
            return (next == null) ? size != 0 : next.prev != null;
        }

        @Override
        public E previous() {
            checkForComodification();
            if (!hasPrevious())
                throw new NoSuchElementException();

            lastReturned = next = (next == null) ? last : next.prev;
            return lastReturned.item;
        }

        @Override
        public int nextIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int previousIndex() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove() {
            checkForComodification();
            if (lastReturned == null) {
                throw new IllegalStateException();
            }

            Node<E> lastNext = lastReturned.next;
            unlink(lastReturned);
            if (next == lastReturned) {
                next = lastNext;
            }
            lastReturned = null;
            expectedModCount++;
        }

        @Override
        public void set(E e) {
            if (lastReturned == null) {
                throw new IllegalStateException();
            }
            checkForComodification();

            mapRemove(lastReturned);
            lastReturned.item = e;
            mapAdd(lastReturned);
        }

        @Override
        public void add(E e) {
            checkForComodification();
            lastReturned = null;
            if (next == null) {
                linkLast(e);
            } else {
                linkBefore(e, next);
            }
            expectedModCount++;
        }

        final void checkForComodification() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
        }
    }

    /**
     * The linked elements that are stored in the List
     * 
     * @author Till Schäfer
     * 
     */
    private class Node<T> {
        T item;
        Node<T> prev;
        Node<T> next;

        /**
         * Constructor
         * 
         * @param item
         *            the stored data
         * @param prev
         *            the previous node in the list
         * @param next
         *            the next node in the list
         */
        public Node(T item, Node<T> prev, Node<T> next) {
            this.item = item;
            this.prev = prev;
            this.next = next;
        }
    }
}
