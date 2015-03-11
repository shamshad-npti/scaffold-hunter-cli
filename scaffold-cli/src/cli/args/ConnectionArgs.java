/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.cli.args;

import java.util.List;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;
import com.google.common.collect.Lists;

/**
 * Represents argument required to manipulate 
 * connection data. Manipulation of connection 
 * data includes saving, deleting and listing
 * 
 * @author Shamshad Alam
 *
 */
@Parameters(commandNames = "connection", commandDescriptionKey = "CLI.CommandHelp.Connection")
public class ConnectionArgs extends AbstractArgs {
    /**
     * name of the connection
     */
    @Parameter(names = {"-c", "--connection-name"}, description = "A unique name of the connection", descriptionKey = "")
    public String connectionName;
    
    /**
     * Database type (mySql, hsqldb)
     */
    @Parameter(names={"-t", "--database-type"}, description = "Type of database [mysql, hsqldb]", descriptionKey = "")
    public String dbType;
    
    /**
     * URL of the database
     */
    @Parameter(names={"-u", "--url"}, description = "Url of the database or location of database", descriptionKey = "")
    public String dbUrl;
    
    /**
     * Name of the database
     */
    @Parameter(names={"-n", "--database-name"}, description = "Name of the database", descriptionKey = "")
    public String dbName;
    
    /**
     * Database username
     */
    @Parameter(names = {"-un", "--user-name"}, description = "Database username for login", descriptionKey = "")
    public String dbUserName;
    
    /**
     * 
     */
    @Parameter(names = {"-p", "--password"}, description="Password for login", descriptionKey = "", password = true)
    public String dbPassword;
    
    /**
     * Database password
     */
    @Parameter(description = "", descriptionKey = "", converter = ConnectionDataActionConverter.class)    
    public List<ConnectionDataAction> action;
    
    /**
     * Enum constant to indicate action
     * @author Shamshad Alam
     *
     */
    public static enum ConnectionDataAction {
        /**
         * Save Action
         */
        SAVE,
        
        /**
         * List Action
         */
        LIST,
        
        /**
         * Delete Action
         */
        DELETE,
    }
    
    /**
     * Convert String to Enum
     * @author Shamshad Alam
     *
     */
    public static class ConnectionDataActionConverter implements IStringConverter<ConnectionDataAction> {
        private static final List<String> actions = Lists.newArrayList("SAVE", "LIST", "DELETE");
        /**
         * Converts a command line string to ConnectionDataAction object
         */
        @Override
        public ConnectionDataAction convert(String value) {
            if(value != null && actions.contains(value.toUpperCase())) {
                return ConnectionDataAction.valueOf(value.toUpperCase());                
            } else {
                if(value == null)
                    return ConnectionDataAction.LIST;
                else {
                    return null;
                }
            }
        }
    }
}
