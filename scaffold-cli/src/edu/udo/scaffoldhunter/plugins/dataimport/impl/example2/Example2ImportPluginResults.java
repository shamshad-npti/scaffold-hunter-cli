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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.example2;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.AtomContainer;

import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * @author Bernhard Dick
 * 
 */
public class Example2ImportPluginResults implements PluginResults {

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getSourceProperties
     * ()
     */
    @Override
    public Map<String, PropertyDefinition> getSourceProperties() {
        HashMap<String, PropertyDefinition> sourceProperties = new HashMap<String, PropertyDefinition>();

        sourceProperties.put("title", null);
        sourceProperties.put("number", null);

        return sourceProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getProbablyNumeric
     * ()
     */
    @Override
    public Set<String> getProbablyNumeric() {
        Set<String> probablyNumeric = Sets.newHashSet();
        probablyNumeric.add("number");
        return probablyNumeric;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getTitleMapping()
     */
    @Override
    public String getTitleMapping() {
        return "title";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getMolecules()
     */
    @Override
    public Iterable<IAtomContainer> getMolecules() {
        IAtomContainer molecule = new AtomContainer();
        molecule.setProperty("title", "Example Molecule");
        molecule.setProperty("number", 2342);

        LinkedList<IAtomContainer> molecules = new LinkedList<IAtomContainer>();
        molecules.add(molecule);

        return molecules;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getNumMolecules()
     */
    @Override
    public int getNumMolecules() {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#addMessageListener
     * (edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void addMessageListener(MessageListener listener) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#removeMessageListener
     * (edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void removeMessageListener(MessageListener listener) {

    }

}
