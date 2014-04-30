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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.sdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
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
public class SDFImportPlugin extends AbstractImportPlugin {


    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "SDF";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getID()
     */
    @Override
    public String getID() {
        return "SDFImportPlugin_v0.1";
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
        return "Import Plugin for SDF";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#
     * getSettingsComponent
     * (edu.udo.scaffoldhunter.model.dataimport.plugins.PluginSettings)
     */
    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        return new SDFImportPluginSettingsPanel((SDFImportPluginSettings) settings, (SDFImportPluginArguments) arguments);
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
        return new SDFImportPluginResults((SDFImportPluginArguments) arguments);
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#checkArguments(java.lang.Object)
     */
    @Override
    public String checkArguments(Object args) {
        SDFImportPluginArguments arguments=(SDFImportPluginArguments) args;
        File sdFile = new File(arguments.getFilename());
        FileReader f = null;
        try {
            f = new FileReader(sdFile);
        } catch (FileNotFoundException e) {
            return e.getMessage();
        } finally {
            if (f != null) {
                try {
                    f.close();
                } catch (IOException e) {
                    return e.getMessage();
                }
            }
        }
        return null;
    }
}
