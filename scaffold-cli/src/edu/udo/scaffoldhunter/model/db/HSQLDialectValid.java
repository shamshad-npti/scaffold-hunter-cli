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

import java.sql.Types;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.HSQLDialect;

/**
 * A {@link Dialect} for HSQLDB which fixes some issues regarding validation.
 * 
 * @author Henning Garus
 */
public class HSQLDialectValid extends HSQLDialect {

    /**
     * Default Constructor
     */
    public HSQLDialectValid() {
        super();
        /*
         * HSQLDB maps longvarchar to varchar with a default length of 1M
         * (1024*1024). We set this here directly, otherwise the database will
         * return varchar as columntype, while validation expects longvarchar.
         */
        registerColumnType(Types.LONGVARCHAR, "varchar(1048576)");
        /*
         * HSQLDB maps float to double. We set this here directly, otherwise the
         * database will return double as columntype, where validation expects
         * float.
         */
        registerColumnType(Types.FLOAT, "double");
    }
}
