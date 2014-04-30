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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.example2;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.datacalculation.AbstractCalcPlugin;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginResults;

/**
 * @author Philipp Lewe
 *
 */
@PluginImplementation
public class Example2CalcPlugin extends AbstractCalcPlugin {
    
    private PropertyDefinition propDef;
    
    Example2CalcPlugin() {
        propDef = new PropertyDefinition();
        
        propDef.setPropertyType(PropertyType.NumProperty);
        propDef.setKey("EXAMPLE2CALCPLUGIN_PROPERTY");
        propDef.setTitle("example 2 property name");
        propDef.setDescription("This property was calculated by the Example2CalcPlugin. It is set to '1.0' for every molecule");
        propDef.setMappable(true);
        propDef.setScaffoldProperty(false);
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "Exampleplugin 2";
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getID()
     */
    @Override
    public String getID() {
        return "ExampleCalcPlugin_2.0";
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getDescription()
     */
    @Override
    public String getDescription() {
        return "This is just an example plugin. It will just create a numerical property with value '1.0' for each molecule.";
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#setAvailableProperties(java.util.Set)
     */
    @Override
    public void setAvailableProperties(Set<PropertyDefinition> availableProperties) {
        // do nothing here
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getSettingsPanel(java.io.Serializable, java.lang.Object)
     */
    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        return new PluginSettingsPanel() {
            
            @Override
            public Serializable getSettings() {
                return null;
            }
            
            @Override
            public Object getArguments() {
                return null;
            }
        };
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getResults(java.lang.Object, java.lang.Iterable, edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public CalcPluginResults getResults(Object arguments, final Iterable<IAtomContainer> molecules, MessageListener msgListener) {
        return new CalcPluginResults() {
            
            @Override
            public Iterable<IAtomContainer> getMolecules() {
                return Iterables.transform(molecules, new Example2CalcPluginTransformFunction(propDef));
            }
            
            @Override
            public Set<PropertyDefinition> getCalculatedProperties() {
                HashSet<PropertyDefinition> propDefs = Sets.newHashSet();
                propDefs.add(propDef);
                return propDefs;
            }
        };
    }

}
