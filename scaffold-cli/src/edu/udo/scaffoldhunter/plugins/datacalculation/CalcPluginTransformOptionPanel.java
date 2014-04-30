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

package edu.udo.scaffoldhunter.plugins.datacalculation;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;

/**
 * Panel to configure transform options needed for almost every
 * {@link CalcPlugin}.
 * 
 * @author Philipp Lewe
 * 
 * @see CalcPluginMoleculeTransformFunction
 * 
 */
public class CalcPluginTransformOptionPanel extends JPanel {
    AbstractCalcPluginArguments arguments;

    private JCheckBox largestFragment;
    private JCheckBox deglycosilate;
    private JCheckBox recalculate2Dcoords;

    /**
     * Creates a new {@link CalcPluginTransformOptionPanel}, all available
     * transform options are enabled. 
     * 
     * @param arguments
     *            the {@link AbstractCalcPluginArguments} that should be
     *            configured by this panel
     */
    public CalcPluginTransformOptionPanel(AbstractCalcPluginArguments arguments) {
        super();
        this.arguments = arguments;
        initGUI();
        largestFragment.setSelected(arguments.isUseLargestFragments());
        deglycosilate.setSelected(arguments.isDeglycosilate());
        recalculate2Dcoords.setSelected(arguments.isRecalculate2Dcoords());
    }
    
    /**
     * Creates a new {@link CalcPluginTransformOptionPanel}, allows to specify
     * which transform options are enabled. 
     * 
     * @param arguments
     *            the {@link AbstractCalcPluginArguments} that should be
     *            configured by this panel
     * @param enableLargestFragment
     *            enables largest fragment checkbox
     * @param enableDeglycosilate
     *            enables deglycosilate checkbox 
     * @param enableRecalculate2Dcoords
     *            enables recalculate 2D-coordinates checkbox
     */
    public CalcPluginTransformOptionPanel(AbstractCalcPluginArguments arguments, 
            boolean enableLargestFragment, boolean enableDeglycosilate, 
            boolean enableRecalculate2Dcoords) {
        this(arguments);
        largestFragment.setEnabled(enableLargestFragment);
        deglycosilate.setEnabled(enableDeglycosilate);
        recalculate2Dcoords.setEnabled(enableRecalculate2Dcoords);
    }

    private void initGUI() {
        FormLayout layout = new FormLayout("p", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);

        builder.appendSeparator(_("DataCalc.TransformOptionsPanel.TransformOptions"));

        largestFragment = new JCheckBox(new LargestFragmentAction());
        builder.append(largestFragment);

        deglycosilate = new JCheckBox(new DeglycosilateAction());
        builder.append(deglycosilate);
        
        recalculate2Dcoords = new JCheckBox(new Recalculate2DCoordsAction());
        builder.append(recalculate2Dcoords);
    }

    private class LargestFragmentAction extends AbstractAction {

        public LargestFragmentAction() {
            putValue(NAME, _("DataCalc.TransformOptionsPanel.LargestFragment"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            arguments.setUseLargestFragment(largestFragment.isSelected());
        }
    }

    private class DeglycosilateAction extends AbstractAction {

        DeglycosilateAction() {
            putValue(NAME, _("DataCalc.TransformOptionsPanel.Deglycosilate"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            arguments.setDeglycosilate(deglycosilate.isSelected());
        }
    }
    
    private class Recalculate2DCoordsAction extends AbstractAction {

        Recalculate2DCoordsAction() {
            putValue(NAME, _("DataCalc.TransformOptionsPanel.Recalculate2DCoords"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            arguments.setRecalculate2Dcoords(recalculate2Dcoords.isSelected());
        }
    }
}
