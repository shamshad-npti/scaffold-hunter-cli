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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginTransformOptionPanel;

/**
 * @author kriege
 *
 */
public class DaylightBitCalcPluginSettingsPanel extends PluginSettingsPanel implements PropertyChangeListener {
    DaylightBitCalcPluginArguments arguments;
    
    private JSpinner pathLength;
    private JSpinner fingerprintLength;

    /**
     * @param arguments
     */
    public DaylightBitCalcPluginSettingsPanel(DaylightBitCalcPluginArguments arguments) {
        super();
        this.arguments = arguments;

        JPanel optionsPanel = new JPanel();
        FormLayout layout = new FormLayout("right:pref, 4dlu, 50dlu, 4dlu", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, optionsPanel);        
        
        builder.appendSeparator(_("DataCalc.Plugins.PluginOptions"));
        SpinnerNumberModel model = new SpinnerNumberModel();
        model.setMinimum(0);
        model.setValue(arguments.getPathLength());
        pathLength = new JSpinner(model);
        builder.append(_("DataCalc.Plugins.DaylightFingerprint.PathLength")+":", pathLength, true);
        pathLength.addPropertyChangeListener(this);
        
        model = new SpinnerNumberModel();
        model.setMinimum(1);
        model.setValue(arguments.getFingerprintSize());
        fingerprintLength = new JSpinner(model);
        builder.append(_("DataCalc.Plugins.DaylightFingerprint.FingerprintSize")+":", fingerprintLength, true);
        fingerprintLength.addPropertyChangeListener(this);
        
        layout = new FormLayout("p", "");
        builder = new DefaultFormBuilder(layout, this);        
        builder.append(optionsPanel);        
        builder.append(new CalcPluginTransformOptionPanel(this.arguments, true, true, false));
    }
    

    @Override
    public Serializable getSettings() {
        return null;
    }

    @Override
    public Object getArguments() {
        return arguments;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (pathLength.getValue() instanceof Integer) {
            arguments.setPathLength((Integer)pathLength.getValue());
        }
        if (fingerprintLength.getValue() instanceof Integer) {
            arguments.setFingerprintSize((Integer)fingerprintLength.getValue());
        }
    }

}
