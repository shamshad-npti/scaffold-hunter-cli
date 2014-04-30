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

import java.io.Serializable;
import java.util.Set;

import net.xeoh.plugins.base.annotations.PluginImplementation;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.base.Preconditions;

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
public class AdditionalSmilesCalcPlugin extends AbstractCalcPlugin {

    @Override
    public String getTitle() {
        return "Additional Smiles";
    }

    @Override
    public String getID() {
        return "SH.AdditionalSmiles_1.0";
    }

    @Override
    public String getDescription() {
        return "Calculates additional smiles for the largest fragment of the structure, largest fragment deglycosylated and original structure deglycosylated";
    }

    @Override
    public void setAvailableProperties(Set<PropertyDefinition> availableProperties) {
    }

    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        Preconditions.checkArgument(arguments instanceof AdditionalSmilesCalcPluginArguments || arguments == null);

        if (arguments == null) {
            arguments = new AdditionalSmilesCalcPluginArguments();
        }

        return new AdditionalSmilesCalcPluginSettingsPanel((AdditionalSmilesCalcPluginArguments) arguments);
    }

    @Override
    public CalcPluginResults getResults(Object arguments, Iterable<IAtomContainer> molecules, MessageListener msgListener) {
        Preconditions.checkArgument(arguments instanceof AdditionalSmilesCalcPluginArguments);
        Preconditions.checkNotNull(molecules);
        Preconditions.checkNotNull(msgListener);

        return new AdditionalSmilesCalcPluginResults((AdditionalSmilesCalcPluginArguments)arguments, molecules, msgListener);

    }
}
