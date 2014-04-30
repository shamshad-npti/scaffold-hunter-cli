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

package edu.udo.scaffoldhunter.model.datacalculation;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;

import edu.udo.scaffoldhunter.model.db.MoleculeNumProperty;
import edu.udo.scaffoldhunter.model.db.MoleculeStringProperty;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * Helper class which constructs {@link Property}s
 * 
 * @author Philipp Lewe
 */
public class PluginPropertyIterator extends UnmodifiableIterator<Collection<Property>> {
    Iterator<IAtomContainer> cdkMoleculeIter;
    Iterator<edu.udo.scaffoldhunter.model.db.Molecule> dbMoleculeIter;
    Map<PropertyDefinition, PropertyDefinition> registeredPropertyDefinitions;

    /**
     * Creates a new PluginPropertyIterator
     * 
     * <br>
     * <b>Important: Both iterators must characterise the same molecules, thus
     * having the same order and length. Each entry in cdkMolecule will be
     * directly mapped with an entry in dbMolecule.</b>
     * 
     * @param cdkMoleculeIter
     *            an {@link Iterator} over {@link org.openscience.cdk.Molecule}
     *            with attached properties
     * @param dbMoleculeIter
     *            an {@link Iterator} over
     *            {@link edu.udo.scaffoldhunter.model.db.Molecule}
     * @param registeredPropertyDefinitions
     *            a mapping PluginPropertyDef -> RegisteredPropertyDef
     * 
     */
    public PluginPropertyIterator(Iterator<IAtomContainer> cdkMoleculeIter,
            Iterator<edu.udo.scaffoldhunter.model.db.Molecule> dbMoleculeIter,
            Map<PropertyDefinition, PropertyDefinition> registeredPropertyDefinitions) {
        Preconditions.checkNotNull(cdkMoleculeIter);
        Preconditions.checkNotNull(dbMoleculeIter);
        Preconditions.checkNotNull(registeredPropertyDefinitions);
        
        this.cdkMoleculeIter = cdkMoleculeIter;
        this.dbMoleculeIter = dbMoleculeIter;
        this.registeredPropertyDefinitions = registeredPropertyDefinitions;
    }

    @Override
    public boolean hasNext() {
        if ((cdkMoleculeIter.hasNext() && !dbMoleculeIter.hasNext())
                || (!cdkMoleculeIter.hasNext() && dbMoleculeIter.hasNext())) {
            throw new AssertionError("Working on iterators with different lenght");
        }

        return cdkMoleculeIter.hasNext() && dbMoleculeIter.hasNext();
    }

    @Override
    public Collection<Property> next() {
        if ((cdkMoleculeIter.hasNext() && !dbMoleculeIter.hasNext())
                || (!cdkMoleculeIter.hasNext() && dbMoleculeIter.hasNext())) {
            throw new AssertionError("Working on iterators with different lenght");
        }

        Collection<Property> collection = Lists.newLinkedList();

        IAtomContainer cdkMolecule = cdkMoleculeIter.next();
        edu.udo.scaffoldhunter.model.db.Molecule dbMolecule = dbMoleculeIter.next();

        for (Entry<PropertyDefinition, PropertyDefinition> entry : registeredPropertyDefinitions.entrySet()) {
            
            if (cdkMolecule.getProperties().containsKey(entry.getKey())) {
                // get RegisteredPropDef
                PropertyDefinition regPropDef = entry.getValue();

                Object propVal = cdkMolecule.getProperties().get(entry.getKey());
                
                // create property
                if (regPropDef.isStringProperty()) {
                    if (propVal instanceof String) {
                        String value = (String) propVal;
                        MoleculeStringProperty property = new MoleculeStringProperty(regPropDef, value);
                        property.setMolecule(dbMolecule);
                        
                        collection.add(property);
                    }
                } else {
                    if (propVal instanceof Double) {
                        Double value = (Double) propVal;
                        MoleculeNumProperty property = new MoleculeNumProperty(regPropDef, value);
                        property.setMolecule(dbMolecule);
                        
                        collection.add(property);
                    }
                }
            }
        }
        
        return collection;
    }
}
