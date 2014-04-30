/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ListIterator;

/**
 * JUnit Test for LinkedHashList
 * 
 * @author Till Schäfer
 * 
 */
public class LinkedHashListTest {

    /**
     * Inserts some elements and checks the size of the list
     */
    @org.junit.Test
    public void simpleInsertAndSize() {
        LinkedHashList<Integer> list = new LinkedHashList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        assertEquals(3, list.size());
    }

    /**
     * Inserts distinct elements and gets an iterator for one them. Than
     * iterates to the end and checks for the right elements.
     */
    @org.junit.Test
    public void IteratorByObject() {
        LinkedHashList<Integer> list = new LinkedHashList<Integer>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        ListIterator<Integer> it = list.listIterator(2);
        assertEquals((Integer) 3, it.next());
        assertEquals((Integer) 4, it.next());
        assertFalse(it.hasNext());
    }

    /**
     * Inserts some elements and removes one. Than it checks if the list still
     * contains the right elements.
     */
    @org.junit.Test
    public void simpleRemove() {
        LinkedHashList<String> list = new LinkedHashList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        
        list.remove("b");
        ListIterator<String> it = list.listIterator();
        assertEquals("a", it.next());
        assertEquals("c", it.next());
        assertEquals("d", it.next());
        assertEquals(3, list.size());
        assertFalse(it.hasNext());
    }

    /**
     * Insert more than one element of the same value and ckeck ...
     */
    @org.junit.Test
    public void insertRemoveMultiple() {
        LinkedHashList<String> list = new LinkedHashList<String>();
        list.add("a");
        list.add("b");
        list.add("b");
        list.add("b");
        list.add("c");
        list.add("d");
        
        assertEquals(6, list.size());
        list.remove("b");
        ListIterator<String> it = list.listIterator();
        assertEquals("a", it.next());
        assertEquals("c", it.next());
        assertEquals("d", it.next());
        assertEquals(3, list.size());
        assertFalse(it.hasNext());
        
    }
    
    /**
     * Replaces an element an checks consistency of the List
     */
    @org.junit.Test
    public void replaceTest() {
        LinkedHashList<String> list = new LinkedHashList<String>();
        list.add("a");
        list.add("b");
        list.add("c");
        list.add("d");
        
        list.listIterator("b").set("e");
        ListIterator<String> it = list.listIterator();
        assertEquals("a", it.next());
        assertEquals("e", it.next());
        assertEquals("c", it.next());
        assertEquals("d", it.next());
        assertEquals(4, list.size());
        assertFalse(it.hasNext());
        
        assertTrue(list.contains("a"));
        assertTrue(list.contains("e"));
        assertTrue(list.contains("c"));
        assertTrue(list.contains("d"));
        assertFalse(list.contains("b"));
    }
    
}
