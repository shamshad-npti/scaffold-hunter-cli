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

package edu.udo.scaffoldhunter.model.db;

/**
 * @author Till Schäfer
 * 
 * Database URL: ./testdb/hsqldb
 *
 */
public class DatabaseTestsHsql extends DatabaseTestsMysql {
    
    /**
     * Constructor
     */
    public DatabaseTestsHsql() {
        this.dbDriver = "org.hsqldb.jdbcDriver";
        this.hibernateDialect = "edu.udo.scaffoldhunter.model.db.HSQLDialectValid";
        this.url = "jdbc:hsqldb:file:./testdb/hsqldb";
    }
}
