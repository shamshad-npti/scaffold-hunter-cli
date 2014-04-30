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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.example5;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.base.Function;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessage;
import edu.udo.scaffoldhunter.model.datacalculation.CalcMessageTypes;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Philipp Lewe
 *
 */
public class Example5CalcPluginTransformFunction implements Function<IAtomContainer, IAtomContainer> {
    
    private Example5CalcPluginArguments arguments;
    private PropertyDefinition propDef;
    private MessageListener msgListener;
    
    Example5CalcPluginTransformFunction(Example5CalcPluginArguments arguments, PropertyDefinition propDef, MessageListener msgListener) {
        this.arguments = arguments;
        this.propDef = propDef;
        this.msgListener = msgListener;
    }

    /* (non-Javadoc)
     * @see com.google.common.base.Function#apply(java.lang.Object)
     */
    @Override
    public IAtomContainer apply(IAtomContainer molecule) {
        
        if(molecule.getProperties().containsKey(arguments.getPropDef())) {
            double value = (Double) molecule.getProperties().get(arguments.getPropDef());
            
            if(arguments.isCheckboxChecked()) {
                value += 1.0;
            } else {
                value -=1.0;
            }
            
            molecule.getProperties().put(propDef, value);
        } else {
            String moleculeTitle = (String)molecule.getProperties().get(CDKConstants.TITLE);
            
            CalcMessage message = new CalcMessage(CalcMessageTypes.PROPERTY_NOT_PRESENT, moleculeTitle, arguments.getPropDef());
            
            msgListener.receiveMessage(message);
        }
                
        return molecule;
    }

}
