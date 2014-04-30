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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.example4;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

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
public class Example4CalcPlugin extends AbstractCalcPlugin {

    private PropertyDefinition propDef;
    Set<PropertyDefinition> availableProperties;

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "Exampleplugin 4";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getID()
     */
    @Override
    public String getID() {
        return "ExampleCalcPlugin_4.0";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getDescription
     * ()
     */
    @Override
    public String getDescription() {
        return "This is just an example plugin. It creates a new property, while adding '1' to or subtracting '1' from  to the value of the chosen property";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#
     * setAvailableProperties(java.util.Set)
     */
    @Override
    public void setAvailableProperties(Set<PropertyDefinition> availableProperties) {
        this.availableProperties = availableProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getSettingsPanel
     * (java.io.Serializable, java.lang.Object)
     */
    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        if (arguments == null) {
            arguments = new Example4CalcPluginArguments();
        }

        return new Example4CalcPluginSettingsPanel((Example4CalcPluginArguments) arguments, availableProperties);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getResults(
     * java.lang.Object, java.lang.Iterable,
     * edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public CalcPluginResults getResults(final Object arguments, final Iterable<IAtomContainer> molecules,
            MessageListener msgListener) {
        final Example4CalcPluginArguments args = (Example4CalcPluginArguments) arguments;
        PropertyDefinition inputProperty = args.getPropDef();

        propDef = new PropertyDefinition(inputProperty);
        if (args.isCheckboxChecked()) {
            propDef.setKey(inputProperty.getKey() + "_ADDED_1.0");
            propDef.setTitle(inputProperty.getTitle() + "(added 1.0)");
            propDef.setDescription("This property is the original value of '" + inputProperty.getTitle() + "'plus 1.0");
        } else {
            propDef.setKey(inputProperty.getKey() + "_SUBTRACTED_1.0");
            propDef.setTitle(inputProperty.getTitle() + "(subtracted 1.0)");
            propDef.setDescription("This property is the original value of '" + inputProperty.getTitle() + "'minus 1.0");
        }

        return new CalcPluginResults() {

            @Override
            public Iterable<IAtomContainer> getMolecules() {
                return Iterables.transform(molecules, new Example4CalcPluginTransformFunction(args, propDef));
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
