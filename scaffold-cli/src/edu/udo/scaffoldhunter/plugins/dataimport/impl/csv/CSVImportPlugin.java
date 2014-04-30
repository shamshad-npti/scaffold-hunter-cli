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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.csv;

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
public class CSVImportPlugin extends AbstractImportPlugin {

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "CSV";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getID()
     */
    @Override
    public String getID() {
        return "CSVImportPlugin_v0.1";
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
        return "An Import Plugin that reads CSV Files";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getSettingsPanel
     * (java.io.Serializable, java.lang.Object)
     */
    @Override
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments) {
        return new CSVImportPluginSettingsPanel((CSVImportPluginSettings) settings, (CSVImportPluginArguments) arguments);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getResults
     * (java.lang.Object)
     */
    @Override
    public PluginResults getResults(Object arguments) {
        return new CSVImportPluginResults((CSVImportPluginArguments) arguments);
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#checkArguments(java.lang.Object)
     */
    @Override
    public String checkArguments(Object args) {
        CSVImportPluginArguments arguments=(CSVImportPluginArguments) args;
        File csvFile = new File(arguments.getFilename());
        FileReader f = null;
        try {
            f = new FileReader(csvFile);
        } catch (FileNotFoundException e) {
            return "File ".concat(arguments.getFilename()).concat(" not found.");
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
    
    /**
     * @param columnCount
     *          number of columns
     * @return default column names
     */
    public static String[] getDefaultColumnNames(int columnCount) {
        String[] columnNames = new String[columnCount];
        for (int i=0; i<columnCount; i++) {
            columnNames[i] = "Column " + (i + 1);
        }
        return columnNames;
    }

}
