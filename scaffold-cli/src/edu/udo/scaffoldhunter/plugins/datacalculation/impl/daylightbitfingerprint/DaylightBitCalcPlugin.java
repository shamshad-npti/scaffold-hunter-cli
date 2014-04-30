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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.daylightbitfingerprint;

import java.io.Serializable;
import java.util.Set;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.datacalculation.AbstractCalcPlugin;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginMoleculeTransformFunction;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginResults;

/**
 * @author kriege
 *
 */
@PluginImplementation
public class DaylightBitCalcPlugin extends AbstractCalcPlugin {

    @Override
    public String getTitle() {
        return "DaylightBitFingerprinter";
    }

    @Override
    public String getID() {
        return "CDK.DaylightBitFingerprinter_1.0";
    }

    @Override
    public String getDescription() {
        String s = "This fingerprinter generates a hash-key fingerprint based on the labeled paths "
                 + "contained in a molecule, similar to the classical Daylight fingerprint.";
        return s;
    }

    @Override
    public void setAvailableProperties(Set<PropertyDefinition> availableProperties) {
    }

    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        Preconditions.checkArgument(arguments instanceof DaylightBitCalcPluginArguments || arguments == null);

        if (arguments == null) {
            arguments = new DaylightBitCalcPluginArguments();
        }

        return new DaylightBitCalcPluginSettingsPanel((DaylightBitCalcPluginArguments) arguments);
    }

    @Override
    public CalcPluginResults getResults(Object arguments, Iterable<IAtomContainer> molecules, MessageListener msgListener) {
        Preconditions.checkArgument(arguments instanceof DaylightBitCalcPluginArguments);
        Preconditions.checkNotNull(molecules);
        Preconditions.checkNotNull(msgListener);
        
        final DaylightBitCalcPluginArguments args = (DaylightBitCalcPluginArguments)arguments;

        Iterable<IAtomContainer> transfomedMols = Iterables.transform(molecules, 
                new CalcPluginMoleculeTransformFunction(args, msgListener));
        return new DaylightBitCalcPluginResults(transfomedMols, args, msgListener);
    }

}
