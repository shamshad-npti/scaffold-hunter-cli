/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.model.util;

import java.util.Iterator;
import java.util.ListIterator;

import junit.framework.Assert;
import edu.udo.scaffoldhunter.util.LinkedHashList;

/**
 * JUnit Test cases for the {@link LinkedHashList}
 * 
 * @author Till Schäfer
 * 
 */
public class LinkedHashListTest {

    LinkedHashList<Double> list;

    /**
     * Initialization of new List
     */
    @org.junit.Before
    public void initList() {
        list = new LinkedHashList<Double>();
    }

    /**
     * Tests the ordering of the List when inserting at the fist position
     */
    @org.junit.Test
    public void addFirstIndexTest() {
        Double i;
        for (i = 1.0; i <= 10; i++) {
            list.add(0, i);
        }
        i--;
        for (Double currentVal : list) {
            Assert.assertEquals(i, currentVal);
            i--;
        }
    }

    /**
     * Tests the ordering of the List when inserting at the last position
     */
    @org.junit.Test
    public void addLastTest() {
        Double i;
        for (i = 1.0; i <= 10; i++) {
            list.add(i);
        }
        i = 1.0;
        for (Double currentVal : list) {
            Assert.assertEquals(i, currentVal);
            i++;
        }
    }

    /**
     * Tests if inserted elements are reflected by contains
     */
    @org.junit.Test
    public void containsTest() {
        for (Double i = 1.0; i <= 5; i++) {
            list.add(0, i);
        }
        for (Double i = 6.0; i <= 10; i++) {
            list.add(i);
        }
        for (Double i = 1.0; i <= 10; i++) {
            Assert.assertTrue(list.contains(i));
        }

        Assert.assertFalse(list.contains(123));
    }

    /**
     * Tests the size method for insertions
     */
    @org.junit.Test
    public void sizeAfterInsertionTest() {
        int expectedSize = 0;

        Assert.assertEquals(expectedSize, list.size());

        for (Double i = 1.0; i <= 5; i++) {
            list.add(0, i);
            Assert.assertEquals(++expectedSize, list.size());
        }
        for (Double i = 6.0; i <= 10; i++) {
            list.add(i);
            Assert.assertEquals(++expectedSize, list.size());
        }
    }

    /**
     * Tests the removal of an element by specifying the object
     */
    @org.junit.Test
    public void removeByObjectTest() {
        Double i;
        for (i = 1.0; i <= 10; i++) {
            list.add(i);
        }

        // position in middle
        Assert.assertTrue(list.contains(6.0));
        list.remove(6.0);
        Assert.assertFalse(list.contains(6.0));
        // position at beginning
        Assert.assertTrue(list.contains(1.0));
        list.remove(1.0);
        Assert.assertFalse(list.contains(1.0));
        // position at end
        Assert.assertTrue(list.contains(10.0));
        list.remove(10.0);
        Assert.assertFalse(list.contains(10.0));

        for (i = 1.0; i <= 10; i++) {
            if (i != 1.0 && i != 6.0 && i != 10.0) {
                Assert.assertTrue(list.contains(i));
            }
        }

        Assert.assertEquals(7, list.size());
    }

    /**
     * Tests the removal of an element by specifying the Index
     */
    @org.junit.Test
    public void removeByIndexTest() {
        Double i;
        for (i = 1.0; i <= 10; i++) {
            list.add(i);
        }

        // position at end
        Assert.assertTrue(list.contains(10.0));
        list.remove(9);
        Assert.assertFalse(list.contains(10.0));
        // position in middle
        Assert.assertTrue(list.contains(6.0));
        list.remove(5);
        Assert.assertFalse(list.contains(6.0));
        // position at beginning
        Assert.assertTrue(list.contains(1.0));
        list.remove(0);
        Assert.assertFalse(list.contains(1.0));

        for (i = 1.0; i <= 10; i++) {
            if (i != 1.0 && i != 6.0 && i != 10.0) {
                Assert.assertTrue(list.contains(i));
            }
        }

        Assert.assertEquals(7, list.size());
    }

    /**
     * Tests if the multiple occurrences of the same element are handled
     * correctly
     */
    @org.junit.Test
    public void addMultiple() {
        list.add(1.0);
        list.add(1.0);
        list.add(2.0);
        list.add(2.0);
        list.add(1.0);

        Iterator<Double> it = list.iterator();
        Assert.assertEquals(1.0, it.next());
        Assert.assertEquals(1.0, it.next());
        Assert.assertEquals(2.0, it.next());
        Assert.assertEquals(2.0, it.next());
        Assert.assertEquals(1.0, it.next());
        Assert.assertFalse(it.hasNext());

        Assert.assertEquals(5, list.size());
    }

    /**
     * Tests if the multiple occurrences of the same element are handled
     * correctly
     */
    @org.junit.Test
    public void removeMultiple() {
        list.add(1.0);
        list.add(1.0);
        list.add(2.0);
        list.add(2.0);
        list.add(1.0);

        list.remove(2.0);

        Iterator<Double> it = list.iterator();
        Assert.assertEquals(1.0, it.next());
        Assert.assertEquals(1.0, it.next());
        Assert.assertEquals(1.0, it.next());
        Assert.assertFalse(it.hasNext());

        Assert.assertEquals(3, list.size());
    }

    /**
     * Test if an exception is thrown when trying to remove an element that is
     * not in the list
     */
    @org.junit.Test
    public void removeNotInListObjectTest() {
        Assert.assertFalse(list.remove(5.0));

        list.add(1.0);
        list.add(1.0);
        list.add(2.0);
        list.add(2.0);
        list.add(1.0);

        Assert.assertTrue(list.remove(2.0));
        Assert.assertFalse(list.remove(4.0));
    }

    /**
     * Tests if setting the correct iterator element works correctly
     */
    @org.junit.Test
    public void iteratorSetTest() {
        list.add(1.0);
        list.add(1.0);
        list.add(2.0);
        list.add(3.0);
        list.add(1.0);

        ListIterator<Double> it = list.listIterator(2.0);

        it.set(4.0);

        Iterator<Double> it2 = list.iterator();
        Assert.assertEquals(1.0, it2.next());
        Assert.assertEquals(1.0, it2.next());
        Assert.assertEquals(4.0, it2.next());
        Assert.assertEquals(3.0, it2.next());
        Assert.assertEquals(1.0, it2.next());
    }

}
