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

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginTransformOptionPanel;

/**
 * @author Philipp Lewe
 *
 */
class EStateNumericalCalcPluginSettingsPanel extends PluginSettingsPanel {
    EStateNumericalCalcPluginArguments arguments;

    public EStateNumericalCalcPluginSettingsPanel(EStateNumericalCalcPluginArguments arguments) {
        super();
        this.arguments = arguments;
        
        FormLayout layout = new FormLayout("p", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, this);
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

}
