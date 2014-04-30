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
import java.util.Map;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Molecule extends Structure {
    private Dataset dataset;
    /**
     * PropertyDefinition.Id -> {@link StringProperty}
     */
    private Map<Integer, MoleculeStringProperty> stringProperties;
    /**
     * PropertyDefinition.Id -> {@link NumProperty}
     */
    private Map<Integer, MoleculeNumProperty> numProperties;

    /**
     * default constructor
     */
    public Molecule() {
        super();

        stringProperties = Collections.synchronizedMap(new HashMap<Integer, MoleculeStringProperty>());
        numProperties = Collections.synchronizedMap(new HashMap<Integer, MoleculeNumProperty>());
    }

    /**
     * Constructor
     * 
     * @param dataset
     *            The {@link Dataset} to which the {@link Molecule} belongs
     * @param stringProperties
     *            Map with {@link StringProperty}s: PropertyDefinition.Id ->
     *            {@link StringProperty}
     * @param numProperties
     *            Map with {@link NumProperty}s: PropertyDefinition.Id ->
     *            {@link NumProperty}
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
    public Molecule(Dataset dataset, Map<Integer, MoleculeStringProperty> stringProperties,
            Map<Integer, MoleculeNumProperty> numProperties, String title, String smiles, String svgString,
            int svgHeight, int svgWidth, String strucMol) {
        super(title, smiles, svgString, svgHeight, svgWidth, strucMol);
        this.dataset = dataset;
        this.stringProperties = Collections.synchronizedMap(stringProperties);
        this.numProperties = Collections.synchronizedMap(numProperties);
    }

    /**
     * Attention: Lazy Property (therefore it is only package wide visible)
     * 
     * @return the {@link Dataset}
     */
    Dataset getDataset() {
        return dataset;
    }

    /**
     * @param dataset
     *            the {@link Dataset} to set
     */
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    /**
     * PropertyDefinition.Id -> {@link StringProperty}
     * 
     * @param stringProperties
     *            the {@link StringProperty}s to set
     */
    public void setStringProperties(Map<Integer, MoleculeStringProperty> stringProperties) {
        this.stringProperties = Collections.synchronizedMap(stringProperties);
    }

    /**
     * PropertyDefinition.Id -> {@link StringProperty}
     * 
     * @return the {@link StringProperty}s
     */
    @Override
    public Map<Integer, MoleculeStringProperty> getStringProperties() {
        return stringProperties;
    }

    /**
     * (PropertyDefinition.Id -> {@link NumProperty})
     * 
     * @param numProperties
     *            the {@link NumProperty}s to set
     */
    public void setNumProperties(Map<Integer, MoleculeNumProperty> numProperties) {
        this.numProperties = Collections.synchronizedMap(numProperties);
    }

    /**
     * (PropertyDefinition.Id -> {@link NumProperty})
     * 
     * @return the {@link NumProperty}s
     */
    @Override
    public Map<Integer, MoleculeNumProperty> getNumProperties() {
        return numProperties;
    }

}
