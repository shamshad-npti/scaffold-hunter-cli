/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffold Hunter source tree
 * for details.
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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.dummy;

import java.io.Serializable;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.dataimport.AbstractImportPlugin;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * A dummy Plugin giving sample Molecules back
 * 
 * @author Bernhard Dick
 * 
 */
@PluginImplementation
public class DummyImportPlugin extends AbstractImportPlugin {

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "Dummy";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getID()
     */
    @Override
    public String getID() {
        return "DummyImportPlugin_v0.1";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getDescription
     * ()
     */
    @Override
    public String getDescription() {
        return "Very simple Plugin";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#
     * getSettingsComponent
     * (edu.udo.scaffoldhunter.model.dataimport.plugins.PluginSettings)
     */
    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object o) {
        return new DummyImportPluginSettingsPanel();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getResults
     * (edu.udo.scaffoldhunter.model.dataimport.plugins.PluginArguments)
     */
    @Override
    public PluginResults getResults(Object arguments) {
        return new DummyImportPluginResults();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#checkArguments(java.lang.Object)
     */
    @Override
    public String checkArguments(Object arguments) {
        if(((DummyImportPluginArguments)arguments).isGenerateError()) {
            return ((DummyImportPluginArguments)arguments).getErrorMessage();
        } else {
            return null;
        }
    }

}
