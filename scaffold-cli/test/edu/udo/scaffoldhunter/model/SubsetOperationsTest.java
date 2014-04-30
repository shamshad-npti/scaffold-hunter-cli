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

package edu.udo.scaffoldhunter.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.MockMolecule;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Subsets;

/**
 * @author Dominic Sacré
 *
 */
public class SubsetOperationsTest {

    private Session session;
    private Map<String, Molecule> molecules;
    private Subset root;
    private List<Subset> subsets;

    /**
     * Setup for each test case: create the subset manager, along with a
     * session, a few molecules and a basic subset tree.
     */
    @Before
    public void setup() {
        session = new Session();

        molecules = Maps.newHashMap();
        molecules.put("A", new MockMolecule("A"));
        molecules.put("B", new MockMolecule("B"));
        molecules.put("C", new MockMolecule("C"));
        molecules.put("D", new MockMolecule("D"));
        molecules.put("E", new MockMolecule("E"));
        molecules.put("F", new MockMolecule("F"));
        molecules.put("G", new MockMolecule("G"));
        molecules.put("H", new MockMolecule("H"));

        subsets = Lists.newLinkedList();

        root = new Subset(null, "root", null, session, molecules.values(), null);
        subsets.add(root);
        session.setSubset(root);

        subsets.addAll(Arrays.asList(
            new Subset(root, "Subset 1", null, session, Sets.newHashSet(
                    molecules.get("A"),
                    molecules.get("B"),
                    molecules.get("C"),
                    molecules.get("D"),
                    molecules.get("E"),
                    molecules.get("F")
                ), null),
            new Subset(root, "Subset 2", null, session, Sets.newHashSet(
                    molecules.get("A"),
                    molecules.get("B"),
                    molecules.get("C")
                ), null),
            new Subset(root, "Subset 3", null, session, Sets.newHashSet(
                    molecules.get("E"),
                    molecules.get("F")
                ), null),
            new Subset(root, "Subset 4", null, session, Sets.newHashSet(
                    molecules.get("F"),
                    molecules.get("G"),
                    molecules.get("H")
                ), null)
        ));

        // FIXME: should the Subset ctor do this?
        for (Subset s : Iterables.skip(subsets, 1)) {
            root.addChild(s);
        }
    }

    /**
     * Tests subset creation.
     * 
     * @throws DatabaseException
     */
//    @Test
//    public void testCreateSubset() throws DatabaseException {
//        assertEquals(4, root.getChildren().size());
//
//        Subset newSubset = subsetManager.createSubset(null, "Test", Sets.newHashSet(
//            molecules.get("D"),
//            molecules.get("F")
//        ));
//        subsetManager.addSubset(newSubset);
//
//        assertEquals(5, root.getChildren().size());
//        assertEquals(true, root.getChildren().contains(newSubset));
//        assertSame(root, newSubset.getParent());
//        assertSame(session, newSubset.getSession());
//
//        assertEquals(Sets.newHashSet(
//            molecules.get("D"),
//            molecules.get("F")
//        ), newSubset);
//    }

    /**
     * Tests subset deletion.
     * 
     * @throws DatabaseException
     */
//    @Test
//    public void testDeleteSubset() throws DatabaseException {
//        assertEquals(4, root.getChildren().size());
//
//        subsetManager.removeSubset(subsets.get(3));
//
//        assertEquals(3, root.getChildren().size());
//        assertEquals(false, root.getChildren().contains(subsets.get(3)));
//    }

    /**
     * Test union subset creation.
     * 
     * @throws DatabaseException
     */
    @Test
    public void testUnion() throws DatabaseException {
        Subset newSubset = Subsets.union(null, Arrays.asList(
            subsets.get(2),     // {A, B, C}
            subsets.get(3)      // {E, F}
        ));

        assertSame(root, newSubset.getParent());

        assertEquals(5, newSubset.size());
        assertEquals(Sets.newHashSet(
            molecules.get("A"),
            molecules.get("B"),
            molecules.get("C"),
            molecules.get("E"),
            molecules.get("F")
        ), newSubset);
    }

    /**
     * Test union subset creation with more than two operands.
     * 
     * @throws DatabaseException
     */
    @Test
    public void testCreateUnionMultiple() throws DatabaseException {
        Subset newSubset = Subsets.union(null, Arrays.asList(
            subsets.get(2),     // {A, B, C}
            subsets.get(3),     // {E, F}
            subsets.get(4)      // {F, G, H}
        ));

        assertSame(root, newSubset.getParent());

        assertEquals(7, newSubset.size());
        assertEquals(Sets.newHashSet(
            molecules.get("A"),
            molecules.get("B"),
            molecules.get("C"),
            molecules.get("E"),
            molecules.get("F"),
            molecules.get("G"),
            molecules.get("H")
        ), newSubset);
    }

    /**
     * Test intersection subset creation.
     * 
     * @throws DatabaseException
     */
    @Test
    public void testIntersection() throws DatabaseException {
        Subset newSubset = Subsets.intersection(null, Arrays.asList(
            subsets.get(1),     // {A, B, C, D, E, F}
            subsets.get(4)      // {F, G, H}
        ));

        assertSame(subsets.get(1), newSubset.getParent());

        assertEquals(1, newSubset.size());
        assertEquals(Sets.newHashSet(
            molecules.get("F")
        ), newSubset);
    }

    /**
     * Test intersection subset creation with more than two operands.
     * 
     * @throws DatabaseException
     */
    @Test
    public void testIntersectionMultiple() throws DatabaseException {
        Subset newSubset = Subsets.intersection(null, Arrays.asList(
            subsets.get(1),     // {A, B, C, D, E, F}
            subsets.get(3),     // {E, F}
            subsets.get(4)      // {F, G, H}
        ));

        assertSame(subsets.get(1), newSubset.getParent());

        assertEquals(1, newSubset.size());
        assertEquals(Sets.newHashSet(
            molecules.get("F")
        ), newSubset);
    }

    /**
     * Test difference subset creation.
     * 
     * @throws DatabaseException
     */
    @Test
    public void testDifference() throws DatabaseException {
        Subset newSubset = Subsets.difference(null,
            subsets.get(1),     // {A, B, C, D, E, F}
            Arrays.asList(
                subsets.get(2)  // {A, B, C}
            )
        );

        assertSame(subsets.get(1), newSubset.getParent());

        assertEquals(3, newSubset.size());
        assertEquals(Sets.newHashSet(
            molecules.get("D"),
            molecules.get("E"),
            molecules.get("F")
        ), newSubset);
    }

    /**
     * Test difference subset creation with more than two operands.
     * 
     * @throws DatabaseException
     */
    @Test
    public void testDifferenceMultiple() throws DatabaseException {
        Subset newSubset = Subsets.difference(null,
            subsets.get(1),     // {A, B, C, D, E, F}
            Arrays.asList(
                subsets.get(2), // {A, B, C}
                subsets.get(4)  // {F, G, H}
            )
        );

        assertSame(subsets.get(1), newSubset.getParent());

        assertEquals(2, newSubset.size());
        assertEquals(Sets.newHashSet(
            molecules.get("D"),
            molecules.get("E")
        ), newSubset);
    }

}
