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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.sql;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.dataimport.AbstractImportPlugin;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * @author Bernhard Dick
 * 
 */
@PluginImplementation
public class SQLImportPlugin extends AbstractImportPlugin {

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getTitle()
     */
    @Override
    public String getTitle() {
        return "SQL";
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#getID()
     */
    @Override
    public String getID() {
        return "SQLImportPlugin_v0.1";
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
        return "A very simple SQL Import Plugin";
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
        return new SQLImportPluginSettingsPanel(settings, (SQLImportPluginArguments) arguments);
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
        return new SQLImportPluginResults((SQLImportPluginArguments) arguments);
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.ImportPlugin#checkArguments(java.lang.Object)
     */
    @Override
    public String checkArguments(Object arguments) {
        SQLImportPluginArguments args=(SQLImportPluginArguments) arguments;
        Properties connProps = new Properties();
        connProps.put("user", args.getUser());
        connProps.put("password", args.getPass());
        try {
            Connection con = DriverManager.getConnection(args.getUrl(), connProps);
            Statement statement = con.createStatement();
            statement.setMaxRows(1);
            @SuppressWarnings("unused")
            ResultSet res = statement.executeQuery(args.getSelectClause());
            statement.close();
            con.close();
        } catch (SQLException e1) {
            return e1.getMessage();
        }
        return null;
    }

}
