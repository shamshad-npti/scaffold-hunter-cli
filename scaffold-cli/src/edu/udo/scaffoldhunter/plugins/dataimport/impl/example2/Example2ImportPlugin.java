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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.example2;

import java.io.Serializable;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.dataimport.AbstractImportPlugin;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * @author Bernhard Dick
 * 
 */
@PluginImplementation
public class Example2ImportPlugin extends AbstractImportPlugin {

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "Example 2";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin#getID()
     */
    @Override
    public String getID() {
        return "Example2ImportPlugin_v1";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin#getDescription()
     */
    @Override
    public String getDescription() {
        return "The second example in the manual";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin#getSettingsPanel
     * (java.io.Serializable, java.lang.Object)
     */
    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        return new PluginSettingsPanel() {

            @Override
            public Serializable getSettings() {
                return null;
            }

            @Override
            public Object getArguments() {
                return null;
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin#getResults(java
     * .lang.Object)
     */
    @Override
    public PluginResults getResults(Object arguments) {
        return new Example2ImportPluginResults();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin#checkArguments
     * (java.lang.Object)
     */
    @Override
    public String checkArguments(Object arguments) {
        return null;
    }

}
