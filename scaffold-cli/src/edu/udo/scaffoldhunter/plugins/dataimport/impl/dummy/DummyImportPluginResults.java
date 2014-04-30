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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.dummy;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.AtomContainer;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * @author Bernhard Dick
 * 
 */
public class DummyImportPluginResults implements PluginResults {
    HashMap<String, PropertyDefinition> sourceProperties;
    LinkedList<IAtomContainer> molecules;
    PropertyDefinition titleDef;
    PropertyDefinition smilesDef;

    /**
     * 
     */
    public DummyImportPluginResults() {
        titleDef = new PropertyDefinition("title", "Title for a molecule", PropertyType.StringProperty, "DUMMY_TITLE", true, false);
        smilesDef = new PropertyDefinition("smiles", "Smiles String", PropertyType.StringProperty, "DUMMY_SMILES", true, false);

        sourceProperties = new HashMap<String, PropertyDefinition>();
        sourceProperties.put("title", titleDef);
        sourceProperties.put("smiles", smilesDef);

        molecules = new LinkedList<IAtomContainer>();
        molecules.add(new AtomContainer());
        molecules.add(new AtomContainer());
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#
     * getSourceProperties()
     */
    @Override
    public Map<String, PropertyDefinition> getSourceProperties() {
        return sourceProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#getTitleMapping
     * ()
     */
    @Override
    public String getTitleMapping() {
        return "title";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#getMolecules
     * ()
     */
    @Override
    public Iterable<IAtomContainer> getMolecules() {
        return molecules;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#getNumMolecules
     * ()
     */
    @Override
    public int getNumMolecules() {
        return 2;
    }
    
    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#getProbablyNumeric()
     */
    @Override
    public Set<String> getProbablyNumeric() {
        return Collections.emptySet();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#addMessageListener(edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void addMessageListener(MessageListener listener) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#removeMessageListener(edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void removeMessageListener(MessageListener listener) {
        // TODO Auto-generated method stub
        
    }

}
