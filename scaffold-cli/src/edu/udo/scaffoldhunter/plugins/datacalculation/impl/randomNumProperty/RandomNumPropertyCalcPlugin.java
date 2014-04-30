/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.randomNumProperty;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.NumProperty;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.datacalculation.AbstractCalcPlugin;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginResults;

/**
 * Generates a {@link NumProperty} that has random values. Intended for testing
 * purpose only.
 * 
 * @author Till Schäfer
 */
@PluginImplementation
public class RandomNumPropertyCalcPlugin extends AbstractCalcPlugin {
    Set<PropertyDefinition> reservedPropDefs = Sets.newHashSet();
    private Integer numProperties = 1;

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "Radom NumProperty Generator";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#getID()
     */
    @Override
    public String getID() {
        return "de0c5676-ce39-4ba5-8b53-26cb0d2225b9";
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
        return "Calculates Random Numerical Values in [0,1]";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin#
     * setAvailableProperties(java.util.Set)
     */
    @Override
    public void setAvailableProperties(Set<PropertyDefinition> availableProperties) {
        this.reservedPropDefs = availableProperties;
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
            private JSpinner spinner;
            SpinnerModel model;

            {
                JPanel spinnerPanel = new JPanel();
                model = new SpinnerNumberModel(1, 1, Integer.MAX_VALUE, 1);
                model.addChangeListener(new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        numProperties = (Integer) model.getValue();
                    }
                });
                spinner = new JSpinner(model);
                JLabel label = new JLabel("Number of Properties: ");
                spinnerPanel.add(label);
                spinnerPanel.add(spinner);
                this.add(spinnerPanel);
            }

            @Override
            public Serializable getSettings() {
                return numProperties;
            }

            @Override
            public Object getArguments() {
                return numProperties;
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
    public CalcPluginResults getResults(Object arguments, final Iterable<IAtomContainer> molecules,
            MessageListener msgListener) {
        final LinkedList<PropertyDefinition> propDefs = Lists.newLinkedList();

        String propKey = "random";
        int count = 0;

        for (int i = 0; i < numProperties; i++) {
            while (propKeyReserved(propKey)) {
                propKey = "random_" + count;
                count++;
            }
            propDefs.add(new PropertyDefinition(propKey, "random values in [0,1]", PropertyType.NumProperty, propKey,
                    true, false));
        }

        return new CalcPluginResults() {

            @Override
            public Iterable<IAtomContainer> getMolecules() {
                return Iterables.transform(molecules, new RandomTransformFunction(propDefs));
            }

            @Override
            public Set<PropertyDefinition> getCalculatedProperties() {
                return new HashSet<PropertyDefinition>(propDefs);
            }
        };
    }

    private boolean propKeyReserved(String propKey) {
        for (PropertyDefinition propDef : reservedPropDefs) {
            if (propDef.getKey().equals(propKey)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Puts a random value in the {@link Property}s map of the {@link IAtomContainer}
     * 
     * @author Till Schäfer
     */
    public class RandomTransformFunction implements Function<IAtomContainer, IAtomContainer> {
        private final Collection<PropertyDefinition> propDefs;
        private final Random rand = new Random();

        RandomTransformFunction(Collection<PropertyDefinition> propDefs) {
            this.propDefs = propDefs;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.google.common.base.Function#apply(java.lang.Object)
         */
        @Override
        public IAtomContainer apply(IAtomContainer molecule) {

            for (PropertyDefinition propDef : propDefs) {
                molecule.getProperties().put(propDef, rand.nextDouble());
            }

            return molecule;
        }
    }
}
