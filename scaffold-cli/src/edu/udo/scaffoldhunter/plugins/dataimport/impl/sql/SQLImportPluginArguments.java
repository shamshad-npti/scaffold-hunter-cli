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

/**
 * @author Bernhard Dick
 * 
 */
public class SQLImportPluginArguments {
    private String db;
    private String hostname;
    private String schema;
    private String user;
    private String pass;
    private String table;
    private String selectClause;
    private String smilesColumn;
    private String molColumn;

    /**
     * @param db 
     * @param hostname 
     * @param schema 
     * @param user
     *            The Database User
     * @param pass
     *            The Database users password
     * @param table 
     * @param selectClause
     *            The SELECT Statement
     * @param smilesColumn
     * @param molColumn
     * 
     */
    public SQLImportPluginArguments(String db, String hostname, String schema, String user, String pass, String table,
            String selectClause, String smilesColumn, String molColumn) {
        this.db = db;
        this.hostname = hostname;
        this.schema = schema;
        this.user = user;
        this.pass = pass;
        this.table = table;
        this.selectClause = selectClause;
        this.smilesColumn = smilesColumn;
        this.molColumn = molColumn;
    }

    /**
     * @param schema
     *            the schema to set
     */
    public void setSchema(String schema) {
        this.schema = schema;
    }

    /**
     * @return the schema
     */
    public String getSchema() {
        return schema;
    }

    /**
     * @param hostname
     *            the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param db
     *            the db to set
     */
    public void setDb(String db) {
        this.db = db;
    }

    /**
     * @return the db
     */
    public String getDb() {
        return db;
    }

    /**
     * @param selectClause
     *            the selectClause to set
     */
    public void setSelectClause(String selectClause) {
        this.selectClause = selectClause;
    }

    /**
     * @return the selectClause
     */
    public String getSelectClause() {
        return selectClause;
    }

    /**
     * @param user
     *            the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param pass
     *            the pass to set
     */
    public void setPass(String pass) {
        this.pass = pass;
    }

    /**
     * @return the pass
     */
    public String getPass() {
        return pass;
    }

    /**
     * @param table
     *            the table to set
     */
    public void setTable(String table) {
        this.table = table;
    }

    /**
     * @return the table
     */
    public String getTable() {
        return table;
    }

    /**
     * @param molColumn
     *            the molColumn to set
     */
    public void setMolColumn(String molColumn) {
        this.molColumn = molColumn;
    }

    /**
     * @return the molColumn
     */
    public String getMolColumn() {
        return molColumn;
    }

    /**
     * @param smilesColumn
     *            the smilesColumn to set
     */
    public void setSmilesColumn(String smilesColumn) {
        this.smilesColumn = smilesColumn;
    }

    /**
     * @return the smilesColumn
     */
    public String getSmilesColumn() {
        return smilesColumn;
    }

    /**
     * @return a generated JDBC Connection URL
     */
    public String getUrl() {
        return "jdbc:".concat(db).concat("://").concat(hostname).concat("/").concat(schema);
    }
}
