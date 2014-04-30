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

package edu.udo.scaffoldhunter.plugins.dataimport;

import java.util.Map;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Bernhard Dick
 * 
 */
public interface PluginResults {
    /**
     * @return suggested names for the PropertyDefinitions
     */
    public Map<String, PropertyDefinition> getSourceProperties();
    
    /**
     * @return properties which are probably NumProperties
     */
    public Set<String> getProbablyNumeric();

    /**
     * @return the name of the as molecule title suggested PropertyDefinition
     */
    public String getTitleMapping();

    /**
     * @return the molecules from a plugin run
     */
    public Iterable<IAtomContainer> getMolecules();
    
    /**
     * @return the number of Molecules which can be imported
     */
    public int getNumMolecules();
    
    /**
     * add a message listener
     * 
     * @param listener
     */
    public void addMessageListener(MessageListener listener);
    
    /**
     * remove a message listener
     * 
     * @param listener
     */
    public void removeMessageListener(MessageListener listener);
}
