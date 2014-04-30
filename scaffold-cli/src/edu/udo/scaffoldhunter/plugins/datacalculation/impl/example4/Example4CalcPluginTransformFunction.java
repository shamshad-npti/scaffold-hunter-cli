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

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.base.Function;

import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Philipp Lewe
 * 
 */
public class Example4CalcPluginTransformFunction implements Function<IAtomContainer, IAtomContainer> {

    private Example4CalcPluginArguments arguments;
    private PropertyDefinition propDef;

    Example4CalcPluginTransformFunction(Example4CalcPluginArguments arguments, PropertyDefinition propDef) {
        this.arguments = arguments;
        this.propDef = propDef;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public IAtomContainer apply(IAtomContainer molecule) {

        if (molecule.getProperties().containsKey(arguments.getPropDef())) {
            double value = (Double) molecule.getProperties().get(arguments.getPropDef());

            if (arguments.isCheckboxChecked()) {
                value += 1.0;
            } else {
                value -= 1.0;
            }

            molecule.getProperties().put(propDef, value);
        }

        return molecule;
    }

}
