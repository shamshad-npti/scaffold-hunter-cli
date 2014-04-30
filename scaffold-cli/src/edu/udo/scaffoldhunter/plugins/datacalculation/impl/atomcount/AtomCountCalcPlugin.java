/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.atomcount;

import java.io.Serializable;
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
 * This plugin calculates the number of atoms of the molecule. I calculates one
 * value with and one value without implicit hydrogens.
 * 
 * @author Till Schäfer
 * 
 */
@PluginImplementation
public class AtomCountCalcPlugin extends AbstractCalcPlugin {

    Set<PropertyDefinition> availableProperties;

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "Atom Count Calculator";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getID()
     */
    @Override
    public String getID() {
        return "758e4a0e-c795-4a3e-a90d-00bccfe9ed5e";
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
        return "This plugin calculates the number of atoms of the molecule. "
                + "I calculates one value with and one value without implicit hydrogens.";
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
            final MessageListener msgListener) {

        // use unique id for unique key of property
        final PropertyDefinition propDefExplicit = new PropertyDefinition("Explicit Atom Count",
                "Number of atoms (including implicit hydrogens)", PropertyType.NumProperty, "atom_count_explicit",
                true, false);
        final PropertyDefinition propDefImplicit = new PropertyDefinition("Implicit Atom Count",
                "Number of atoms (excluding implicit hydrogens)", PropertyType.NumProperty, "atom_count_implicit",
                true, false);

        return new CalcPluginResults() {

            @Override
            public Iterable<IAtomContainer> getMolecules() {
                return Iterables.transform(molecules, new AtomCountCalcPluginTransformFunction(propDefExplicit,
                        propDefImplicit, msgListener));
            }

            @Override
            public Set<PropertyDefinition> getCalculatedProperties() {
                return Sets.newHashSet(propDefExplicit, propDefImplicit);
            }
        };
    }
}
