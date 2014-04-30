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

package edu.udo.scaffoldhunter.data;

/**
 * @author Thomas Schmitz
 *
 */
public enum ConnectionType {
    /** 
     * file based HyperSQL database 
     */
    HSQLDB("HSQLDB", "edu.udo.scaffoldhunter.model.db.HSQLDialectValid", "org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:"),
    /** 
     * MySQL database with InnoDB engine 
     */
    MYSQL("MySQL", "org.hibernate.dialect.MySQL5InnoDBDialect", "com.mysql.jdbc.Driver", "jdbc:mysql://");
    /** 
     * Some Oracle database, who knows? 
     */
//    ORACLE("Oracle", "OracleDialect", "OracleDriver", "Watweißich");
    
    private final String name;
    
    /**
     * @return the dialect
     */
    public String getHibernateDialect() {
        return hibernateDialect;
    }

    /**
     * @return the driver
     */
    public String getDriverClass() {
        return driverClass;
    }

    /**
     * @return the urlPrefix
     */
    public String getUrlPrefix() {
        return urlPrefix;
    }

    private final String hibernateDialect;
    private final  String driverClass;
    private final String urlPrefix;
    
    /**
     * Generates a new Connection Type
     * @param name Connectiontype name
     * @param hibernateDialect SQL Dialect
     * @param driverClass Driver class
     * @param urlPrefix Url Prefix
     */
    private ConnectionType(String name, String hibernateDialect, String driverClass, String urlPrefix) {
        this.name = name;
        this.hibernateDialect = hibernateDialect;
        this.driverClass = driverClass;
        this.urlPrefix = urlPrefix;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
