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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.additionalsmiles;

import java.util.List;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginResults;

/**
 * @author Philipp Lewe
 * 
 */
public class AdditionalSmilesCalcPluginResults implements CalcPluginResults {

    AdditionalSmilesCalcPluginArguments arguments;
    Iterable<IAtomContainer> molecules;
    MessageListener msgListener;

    List<PropertyDefinition> propDefs = Lists.newArrayList();

    /**
     * @param arguments
     *            the plugin arguments
     * @param molecules
     *            the input molecule {@link Iterable}
     * @param msgListener
     *            the message listener used for reporting of errors
     */
    public AdditionalSmilesCalcPluginResults(AdditionalSmilesCalcPluginArguments arguments,
            Iterable<IAtomContainer> molecules, MessageListener msgListener) {
        this.arguments = arguments;
        this.molecules = molecules;
        this.msgListener = msgListener;

        createPropertyDefinitions();
    }

    private void createPropertyDefinitions() {
        PropertyDefinition propDef;

        propDef = new PropertyDefinition();
        propDef.setKey("LargestFragmentSmiles");
        propDef.setTitle("LargestFragmentSmiles");
        propDef.setDescription("Smiles for largest fragment of the structure");
        propDef.setScaffoldProperty(false);
        propDef.setPropertyType(PropertyType.StringProperty);
        propDefs.add(propDef);

        propDef = new PropertyDefinition();
        propDef.setKey("LargestFragmentDeglycosilatedSmiles");
        propDef.setTitle("LargestFragmentDeglycosilatedSmiles");
        propDef.setDescription("Smiles for deglycosilised largest fragment of the structure");
        propDef.setScaffoldProperty(false);
        propDef.setPropertyType(PropertyType.StringProperty);
        propDefs.add(propDef);

        propDef = new PropertyDefinition();
        propDef.setKey("OriginalStructureDeglycosilatedSmiles");
        propDef.setTitle("OriginalStructureDeglycosilatedSmiles");
        propDef.setDescription("Smiles for deglycosilised structure");
        propDef.setScaffoldProperty(false);
        propDef.setPropertyType(PropertyType.StringProperty);
        propDefs.add(propDef);
    }

    @Override
    public Set<PropertyDefinition> getCalculatedProperties() {
        Set<PropertyDefinition> s = Sets.newLinkedHashSet();
        
        if(arguments.isCalcLargestFragmentSmiles()) {
            s.add(propDefs.get(0));
        }
        
        if(arguments.isCalcLargestFragmentDeglycosilatedSmiles()) {
            s.add(propDefs.get(1));
        }
        
        if(arguments.isCalcOriginalStructureDeglycosilatedSmiles()) {
            s.add(propDefs.get(2));
        }
        
        return s;
    }

    @Override
    public Iterable<IAtomContainer> getMolecules() {
        return Iterables.transform(molecules, new AdditionalSmilesCalcPluginTransformFunction(arguments, propDefs, msgListener));
    }

}
