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

package edu.udo.scaffoldhunter.model.db;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public abstract class DbObject {
    /**
     * The Database identifier
     */
    protected int id;

    /**
     * Compares to DbObject for equality on database layer
     * 
     * Attention: Use this only in very rare cases. Usually all Database Objects
     * should be the same Java Objects. (Exceptions are: Scaffolds in different
     * Trees, Banner, Comments, Rulesets)
     * 
     * @param obj
     * @return true is equal
     */
    public boolean equalsDb(DbObject obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        @SuppressWarnings("cast")
        DbObject other = (DbObject) obj;
        if (id != other.id)
            return false;
        return true;
    }
}
