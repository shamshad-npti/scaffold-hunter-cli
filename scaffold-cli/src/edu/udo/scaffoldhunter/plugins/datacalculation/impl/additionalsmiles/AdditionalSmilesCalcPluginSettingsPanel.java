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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.event.ActionEvent;
import java.io.Serializable;

import javax.swing.JCheckBox;

import com.google.common.base.Preconditions;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Philipp Lewe
 * 
 */
public class AdditionalSmilesCalcPluginSettingsPanel extends PluginSettingsPanel {

    AdditionalSmilesCalcPluginArguments arguments;

    private JCheckBox calcLargestFragmentSmiles = new JCheckBox(new LFSAction());
    private JCheckBox calcLargestFragmentDeglycosilatedSmiles = new JCheckBox(new LFDSAction());
    private JCheckBox calcOriginalStructureDeglycosilatedSmiles = new JCheckBox(new OSDSAction());

    /**
     * @param arguments the plugin arguments
     */
    public AdditionalSmilesCalcPluginSettingsPanel(AdditionalSmilesCalcPluginArguments arguments) {
        super();
        Preconditions.checkNotNull(arguments);
        this.arguments = arguments;

        FormLayout layout = new FormLayout("p", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);

        builder.appendSeparator(_("DataCalc.Plugins.PluginOptions"));

        calcLargestFragmentSmiles.setSelected(arguments.isCalcLargestFragmentSmiles());
        calcLargestFragmentDeglycosilatedSmiles.setSelected(arguments.isCalcLargestFragmentDeglycosilatedSmiles());
        calcOriginalStructureDeglycosilatedSmiles.setSelected(arguments.isCalcOriginalStructureDeglycosilatedSmiles());

        builder.append(calcLargestFragmentSmiles);
        builder.append(calcLargestFragmentDeglycosilatedSmiles);
        builder.append(calcOriginalStructureDeglycosilatedSmiles);
    }

    @Override
    public Serializable getSettings() {
        return null;
    }

    @Override
    public Object getArguments() {
        return arguments;
    }

    private class LFSAction extends AbstractAction {
        
        public LFSAction () {
            putValues(_("DataCalc.Plugins.AdditionalSmiles.LargestFragmentSmiles"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            arguments.setCalcLargestFragmentSmiles(calcLargestFragmentSmiles.isSelected());
        }
    }

    private class LFDSAction extends AbstractAction {
        
        public LFDSAction() {
            putValues(_("DataCalc.Plugins.AdditionalSmiles.LargestFragmentDeglycosilatedSmiles"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            arguments.setCalcLargestFragmentDeglycosilatedSmiles(calcLargestFragmentDeglycosilatedSmiles.isSelected());
        }
    }

    private class OSDSAction extends AbstractAction {
        
        public OSDSAction() {
            putValues(_("DataCalc.Plugins.AdditionalSmiles.OriginalStructureDeglycosilatedSmiles"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            arguments.setCalcOriginalStructureDeglycosilatedSmiles(calcOriginalStructureDeglycosilatedSmiles
                    .isSelected());
        }
    }
}
