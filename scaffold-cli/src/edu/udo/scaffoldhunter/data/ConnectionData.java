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

package edu.udo.scaffoldhunter.data;

/**
 * Data necessary to connect to a database
 * 
 * @author Thorsten Flügel
 */
public class ConnectionData {
    private String connectionName;
    private String dbUrl;
    private String dbName;
    private String dbusername;
    private String dbpassword;
    private ConnectionType dbType = ConnectionType.values()[0];
    
    /**
     * @return the dbType
     */
    public ConnectionType getDbType() {
        return dbType;
    }

    /**
     * @param dbType the dbType to set
     */
    public void setDbType(ConnectionType dbType) {
        this.dbType = dbType;
    }

    /**
     * Creates an empty connection data object
     */
    public ConnectionData() {
    }
    
    /**
     * Creates a connection data object with the passed values
     * @param connectionName
     * @param dbUrl
     * @param dbName
     * @param dbusername
     * @param dbpassword
     * @param dbType 
     */
    public ConnectionData(String connectionName, String dbUrl, String dbName, String dbusername, String dbpassword, ConnectionType dbType) {
        this.connectionName = connectionName;
        this.dbUrl = dbUrl;
        this.dbName = dbName;
        this.dbusername = dbusername;
        this.dbpassword = dbpassword;
        this.dbType = dbType;
    }
    
    /**
     * @param connectionName
     *            human readable name of the connection
     */
    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    /**
     * @return human readable name of the connection
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
     * @param dbUrl
     *            URL of the database
     */
    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    /**
     * @return URL of the database
     */
    public String getDbUrl() {
        if (dbUrl != null && !dbUrl.startsWith(getDbType().getUrlPrefix())) {
            return getDbType().getUrlPrefix() + dbUrl;
        } else {
            return dbUrl;
        }
    }

    /**
     * @param dbName
     *            name of the database
     */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * @return name of the database
     */
    public String getDbName() {
        return dbName;
    }

    /**
     * @param username
     */
    public void setDbUsername(String username) {
        this.dbusername = username;
    }

    /**
     * @return name of the user
     */
    public String getDbUsername() {
        return dbusername;
    }

    /**
     * @param password
     */
    public void setDbPassword(String password) {
        this.dbpassword = password;
    }

    /**
     * @return the password
     */
    public String getDbPassword() {
        return dbpassword;
    }
    
    @Override
    public String toString() {
        return connectionName;
    }
}
