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

package edu.udo.scaffoldhunter.model.db;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.RuleType;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeView;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Scaffold extends Structure {
    private Tree tree;
    /**
     * for the imaginary Root parent is null
     */
    private Scaffold parent;
    private List<Scaffold> children;
    private int hierarchyLevel;
    /**
     * Rule that was applied to generate the parent of this Scaffold
     */
    private RuleType deletionRuleParent;
    /**
     * All {@link Molecule}s that belong to the {@link Scaffold} in the whole
     * {@link Dataset}. They are only needed for {@link Tree} generation an must
     * not be accessed when using the {@link Scaffold}.
     */
    private Set<Molecule> generationMolecules;
    /**
     * Molecules that belong to the Scaffold in the current Subset.
     */
    private Set<Molecule> molecules;
    /**
     * PropertyDefinition.Id -> StringProperty
     */
    private Map<Integer, ScaffoldStringProperty> stringProperties;
    /**
     * PropertyDefinition.Id -> NumProperty
     */
    private Map<Integer, ScaffoldNumProperty> numProperties;

    /**
     * default constructor
     * <p>
     * Care should be taken regarding equality when this constructor is used.
     * @see Scaffold#equals
     */
    public Scaffold() {
        super();

        children = Lists.newArrayList();
        generationMolecules = new HashSet<Molecule>();
        molecules = new HashSet<Molecule>();
        stringProperties = Collections.synchronizedMap(new HashMap<Integer, ScaffoldStringProperty>());
        numProperties = Collections.synchronizedMap(new HashMap<Integer, ScaffoldNumProperty>());
    }

    /**
     * Constructor
     * 
     * @param tree
     *            the {@link Tree} to which the {@link Scaffold} belongs
     * @param parent
     *            the parent {@link Scaffold} in {@link Tree}
     * @param children
     *            the children in the {@link Tree}
     * @param hierarchyLevel
     *            the tree level to display the Scaffold in
     *            {@link ScaffoldTreeView}
     * @param deletionRuleParent
     *            the deletion rule which was applied to create the
     *            {@link Scaffold}
     * @param generationMolecules
     *            all {@link Molecule}s which belong to this {@link Scaffold}
     *            (Dataset wide)
     * @param molecules
     *            all {@link Molecule}s which directly belong to this
     *            {@link Scaffold}
     * @param stringProperties
     *            Map with {@link StringProperty}s: PropertyDefinition.Id ->
     *            StringProperty
     * @param numProperties
     *            Map with {@link NumProperty}s: PropertyDefinition.Id ->
     *            NumProperty
     * @param title
     *            the title
     * @param smiles
     *            the canonical smiles string
     * @param svgString
     *            the svg image in text form
     * @param svgHeight
     *            the svg image height
     * @param svgWidth
     *            the svg image width
     * @param strucMol
     *            mol format structure information
     */
    public Scaffold(Tree tree, Scaffold parent, List<Scaffold> children, int hierarchyLevel,
            RuleType deletionRuleParent, Set<Molecule> generationMolecules, Set<Molecule> molecules,
            Map<Integer, ScaffoldStringProperty> stringProperties, Map<Integer, ScaffoldNumProperty> numProperties,
            String title, String smiles, String svgString, int svgHeight, int svgWidth, String strucMol) {
        super(title, smiles, svgString, svgHeight, svgWidth, strucMol);
        this.tree = tree;
        this.parent = parent;
        this.children = children;
        this.hierarchyLevel = hierarchyLevel;
        this.deletionRuleParent = deletionRuleParent;
        this.generationMolecules = generationMolecules;
        this.molecules = molecules;
        this.stringProperties = Collections.synchronizedMap(stringProperties);
        this.numProperties = Collections.synchronizedMap(numProperties);
    }

    /**
     * @return the tree
     */
    public Tree getTree() {
        return tree;
    }

    /**
     * @param tree
     *            the tree to set
     */
    public void setTree(Tree tree) {
        this.tree = tree;
    }

    /**
     * @return the parent
     */
    public Scaffold getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    public void setParent(Scaffold parent) {
        this.parent = parent;
    }

    /**
     * @return the children
     */
    public List<Scaffold> getChildren() {
        return children;
    }

    /**
     * @param children
     *            the children to set
     */
    public void setChildren(List<Scaffold> children) {
        this.children = children;
    }

    /**
     * @return the hierarchyLevel
     */
    public int getHierarchyLevel() {
        return hierarchyLevel;
    }

    /**
     * @param hierarchyLevel
     *            the hierarchyLevel to set
     */
    public void setHierarchyLevel(int hierarchyLevel) {
        this.hierarchyLevel = hierarchyLevel;
    }

    /**
     * @return the deletionRuleParent
     */
    public RuleType getDeletionRuleParent() {
        return deletionRuleParent;
    }

    /**
     * @param deletionRuleParent
     *            the deletionRuleParent to set
     */
    public void setDeletionRuleParent(RuleType deletionRuleParent) {
        this.deletionRuleParent = deletionRuleParent;
    }

    /**
     * @return the molecules
     */
    public Set<Molecule> getMolecules() {
        return molecules;
    }

    /**
     * @param molecules
     *            the molecules to set
     */
    public void setMolecules(Set<Molecule> molecules) {
        this.molecules = molecules;
    }

    /**
     * Checks if the scaffold is the imaginary root, the parent of all scaffolds
     * which consist of a single ring.
     * 
     * @return <code>true</code> if this is imaginary root, else
     *         <code>false</code>
     */
    public boolean isImaginaryRoot() {
        return getHierarchyLevel() == 0;
    }

    /**
     * @param stringProperties
     *            the stringProperties to set
     */
    public void setStringProperties(Map<Integer, ScaffoldStringProperty> stringProperties) {
        this.stringProperties = Collections.synchronizedMap(stringProperties);
    }

    /**
     * @return the stringProperties
     */
    @Override
    public Map<Integer, ScaffoldStringProperty> getStringProperties() {
        return stringProperties;
    }

    /**
     * @param numProperties
     *            the numProperties to set
     */
    public void setNumProperties(Map<Integer, ScaffoldNumProperty> numProperties) {
        this.numProperties = Collections.synchronizedMap(numProperties);
    }

    /**
     * @return the numProperties
     */
    @Override
    public Map<Integer, ScaffoldNumProperty> getNumProperties() {
        return numProperties;
    }

    /**
     * @return the generationMolecules
     */
    @SuppressWarnings("unused")
    private Set<Molecule> getGenerationMolecules() {
        return generationMolecules;
    }

    /**
     * @param generationMolecules
     *            the generationMolecules to set
     */
    public void setGenerationMolecules(Set<Molecule> generationMolecules) {
        this.generationMolecules = generationMolecules;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getSmiles() == null) ? 0 : getSmiles().hashCode());
        result = prime * result + ((tree == null) ? 0 : tree.hashCode());
        return result;
    }

    /**
     * Two scaffolds are considered equal if their trees and their smile strings
     * are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Scaffold other = (Scaffold) obj;
        if (getSmiles() == null) {
            if (other.getSmiles() != null)
                return false;
        } else if (!getSmiles().equals(other.getSmiles()))
            return false;
        else if (tree == null) {
            if (other.tree != null)
                return false;
        } else if (!tree.equals(other.tree))
            return false;
        return true;
    }
}
