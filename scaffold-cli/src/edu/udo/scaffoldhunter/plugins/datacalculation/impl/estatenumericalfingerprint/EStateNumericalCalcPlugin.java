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

package edu.udo.scaffoldhunter.plugins.datacalculation.impl.estatenumericalfingerprint;

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
 * @author Philipp Lewe
 * 
 */
@PluginImplementation
public class EStateNumericalCalcPlugin extends AbstractCalcPlugin {

    @Override
    public String getTitle() {
        return "EStateNumericalFingerprinter";
    }

    @Override
    public String getID() {
        return "CDK.EStateNumericalFingerprinter_1.0";
    }

    @Override
    public String getDescription() {
        String s = "This fingerprinter generates a fragment count descriptor using the E-State fragments.\n"
                + "Traditionally the e-state descriptors identify the relevant fragments and then evaluate the actual "
                + "e-state value. However it has been shown in [Butina, D. , Performance of Kier-Hall E-state Descriptors "
                + "in Quantitative Structure Activity Relationship (QSAR) Studies of Multifunctional Molecules , Molecules, 2004, 9:1004-1009] "
                + "that simply using the counts of the e-state fragments can lead to QSAR models that exhibit similar performance to "
                + "those built using the actual e-state indices."
                + "The descriptor returns an numerical fingerprint of 79 values."
                + "Needed for clustering with Jaccard distance";
        return s;
    }

    @Override
    public void setAvailableProperties(Set<PropertyDefinition> availableProperties) {
    }

    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        Preconditions.checkArgument(arguments instanceof EStateNumericalCalcPluginArguments || arguments == null);

        if (arguments == null) {
            arguments = new EStateNumericalCalcPluginArguments();
        }

        return new EStateNumericalCalcPluginSettingsPanel((EStateNumericalCalcPluginArguments) arguments);
    }

    @Override
    public CalcPluginResults getResults(Object arguments, Iterable<IAtomContainer> molecules, MessageListener msgListener) {
        Preconditions.checkArgument(arguments instanceof EStateNumericalCalcPluginArguments);
        Preconditions.checkNotNull(molecules);
        Preconditions.checkNotNull(msgListener);

        return new EStateNumericalCalcPluginResults(Iterables.transform(molecules, new CalcPluginMoleculeTransformFunction(
                (EStateNumericalCalcPluginArguments) arguments, msgListener)), msgListener);
    }
}
