/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 Till Schäfer
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

import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public abstract class Structure extends DbObject {
    private String title;
    private String smiles;
    private int svgHeight;
    private int svgWidth;
    private Mol mol = new Mol();
    private Svg svg = new Svg();

    /**
     * Needed for internal locking mechanism (lazy loading) by the DbManager.
     * 
     * Key is the the ID of a {@link PropertyDefinition} Value is the number of
     * workers accessing the {@link Property}
     */
    Map<Integer, Integer> locks = Collections.synchronizedMap(new HashMap<Integer, Integer>());

    /**
     * default constructor
     */
    public Structure() {
    }

    /**
     * Constructor
     * 
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
    public Structure(String title, String smiles, String svgString, int svgHeight, int svgWidth, String strucMol) {
        this.title = title;
        this.smiles = smiles;
        this.svg.setString(svgString);
        this.svgHeight = svgHeight;
        this.svgWidth = svgWidth;
        mol.setString(strucMol);
    }

    /**
     * @return the database id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title
     *            the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the svgHeight
     */
    public int getSvgHeight() {
        return svgHeight;
    }

    /**
     * @param svgHeight
     *            the svgHeight to set
     */
    public void setSvgHeight(int svgHeight) {
        this.svgHeight = svgHeight;
    }

    /**
     * @return the svgWidth
     */
    public int getSvgWidth() {
        return svgWidth;
    }

    /**
     * @param svgWidth
     *            the svgWidth to set
     */
    public void setSvgWidth(int svgWidth) {
        this.svgWidth = svgWidth;
    }

    /**
     * @return the smiles
     */
    public String getSmiles() {
        return smiles;
    }

    /**
     * @param smiles
     *            the smiles to set
     */
    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    /**
     * Attention: Lazy Property
     * 
     * Please use DbManager.getSvgString instead
     * 
     * @return the svgImage
     */
    String getSvgString() {
        return svg.getString();
    }

    /**
     * @param svgString
     *            the svgImage to set
     */
    public void setSvgString(String svgString) {
        svg.setString(svgString);
    }

    /**
     * Attention: Lazy Property
     * 
     * Please use DbManager.getMolString instead
     * 
     * @return the strucMol
     */
    String getStrucMol() {
        return mol.getString();
    }

    /**
     * @param strucMol
     *            the strucMol to set
     */
    public void setStrucMol(String strucMol) {
        mol.setString(strucMol);
    }

    /**
     * @return the {@link NumProperty NumProperties}
     */
    public abstract Map<Integer, ? extends NumProperty> getNumProperties();

    /**
     * Convenience method to get the value of a numProperty by the
     * PropertyDefinition. If the property defined by <code>propDef</code> is
     * unknown <code>null</code> will be returned.<br>
     * Result is equivalent to:
     * <p>
     * <code>getNumProperties().get(propDef.getID()).getValue()</code>
     * 
     * @param propDef
     *            the {@link PropertyDefinition} which defines the property
     *            whose value is returned
     * @return the value of the property specified by <code>propDef</code> or
     *         <code>null</code> if that property is unknown.
     * 
     */
    public Double getNumPropertyValue(PropertyDefinition propDef) {
        NumProperty prop = getNumProperties().get(propDef.getId());
        Double d = prop == null ? null : prop.getValue();
        return d;
    }

    /**
     * @return the {@link StringProperty StringProperties}
     */
    public abstract Map<Integer, ? extends StringProperty> getStringProperties();

    /**
     * Convenience method to get the value of a stringProperty by the
     * PropertyDefinition. If the property defined by <code>propDef</code> is
     * unknown to this structure <code>null</code> will be returned. Result
     * is equivalent to:
     * <p>
     * <code>getNumProperties().get(propDef.getID()).getValue()</code>
     * 
     * @param propDef
     *            the {@link PropertyDefinition} which defines the property
     *            whose value is returned
     * @return the value of the property specified by <code>propDef</code>
     */
    public String getStringPropertyValue(PropertyDefinition propDef) {
        StringProperty prop = getStringProperties().get(propDef.getId());
        return prop == null ? null : prop.getValue();
    }

    /**
     * Convenience method to get the BitFingerprint length by the
     * PropertyDefinition. If the property defined by <code>propDef</code> is
     * unknown to this structure <code>null</code> will be returned. Result
     * is equivalent to:
     * <p>
     * <code>getNumProperties().get(propDef.getID()).getBitFingerprintLength()</code>
     * 
     * @param propDef
     *            the {@link PropertyDefinition} which defines the property
     *            whose value is returned
     * @return the length
     */
    public short getBitFingerprintLength(PropertyDefinition propDef) {
        StringProperty prop = getStringProperties().get(propDef.getId());
        return prop == null ? null : prop.getBitFingerprintLength();
    }
    
    /**
     * Convenience method to get the BitFingerprint {@link BitSet} by the
     * PropertyDefinition. If the property defined by <code>propDef</code> is
     * unknown to this structure <code>null</code> will be returned. Result
     * is equivalent to:
     * <p>
     * <code>getNumProperties().get(propDef.getID()).getBitFingerprintBitSet()</code>
     * 
     * @param propDef
     *            the {@link PropertyDefinition} which defines the property
     *            whose value is returned
     * @return the BitSet
     */
    public BitSet getBitFingerprintBitSet(PropertyDefinition propDef) {
        StringProperty prop = getStringProperties().get(propDef.getId());
        return prop == null ? null : prop.getBitFingerprintBitSet();
    }

    /**
     * @return a combined Map of all {@link NumProperty}s and all
     *         {@link StringProperty}s
     */
    public Map<Integer, ? extends Property> getProperties() {
        Map<Integer, Property> ret = Maps.newHashMap();
        ret.putAll(getStringProperties());
        ret.putAll(getNumProperties());
        return ret;
    }
}
