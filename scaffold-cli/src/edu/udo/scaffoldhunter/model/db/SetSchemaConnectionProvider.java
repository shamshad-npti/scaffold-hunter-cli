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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.DriverManagerConnectionProvider;
import org.hibernate.dialect.MySQL5InnoDBDialect;

/**
 * A connection Provider which sets the hibernate.default_schema for a HSQLDB
 * connection.
 * 
 * @author Henning Garus
 * 
 */
public class SetSchemaConnectionProvider extends DriverManagerConnectionProvider {

    private String schema;
    private String dialect;
    
    @Override
    public void configure(Properties props) throws HibernateException {
        super.configure(props);
        schema = props.getProperty(Environment.DEFAULT_SCHEMA);
        dialect = props.getProperty(Environment.DIALECT);
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection c = super.getConnection();
        if (dialect.equals(MySQL5InnoDBDialect.class.getCanonicalName())) {
            c.setCatalog(schema);
        } else if (dialect.equals(HSQLDialectValid.class.getCanonicalName())) {
            Statement s = c.createStatement();
            s.execute("SET SCHEMA " + schema + ";");
            s.close();
        } else {
            throw new AssertionError("Unknown dialect");
        }
        return c;

    }

}
