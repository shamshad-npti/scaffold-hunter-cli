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

package edu.udo.scaffoldhunter.util;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 *
 * Utility class to hold various <code>Ordering</code>s
 * 
 * @see Ordering
 */
public class Orderings {

    /**
     * Orders property definitions by their title attribute.
     */
    public static final Ordering<PropertyDefinition> PROPERTY_DEFINITION_BY_TITLE = new Ordering<PropertyDefinition>() {
        @Override
        public int compare(PropertyDefinition left, PropertyDefinition right) {
            if (left == null && right == null) {
                return 0;
            } else if (left == null) {
                return -1;
            } else if (right == null) {
                return 1;
            }
            return left.getTitle().compareTo(right.getTitle());
        }
    };
    
    /**
     * Orders property definitions by type: first molecule properties, then scaffold properties
     */
    public static final Ordering<PropertyDefinition> PROPERTY_DEFINITION_BY_STRUCTURE_TYPE = new Ordering<PropertyDefinition>() {
        @Override
        public int compare(PropertyDefinition left, PropertyDefinition right) {
            if (left.isScaffoldProperty() && !right.isScaffoldProperty())
                return 1;
            else if (!left.isScaffoldProperty() && right.isScaffoldProperty())
                return -1;
            else
                return 0;
        }
    };
    
    /**
     * Orders property definitions by property type: first numeric properties, then string properties
     */
    public static final Ordering<PropertyDefinition> PROPERTY_DEFINITION_BY_PROPERTY_TYPE = new Ordering<PropertyDefinition>() {
        @Override
        public int compare(PropertyDefinition left, PropertyDefinition right) {
            if (left.isStringProperty() && !right.isStringProperty())
                return 1;
            else if (!left.isStringProperty() && right.isStringProperty())
                return -1;
            else 
                return 0;
        }
    };
    
   /**
    * Orders Scaffolds by their hierarchy level
    */
    public static final Ordering<Scaffold> SCAFFOLDS_BY_HIERARCHY_LEVEL = new Ordering<Scaffold>() {
        @Override
        public int compare(Scaffold left, Scaffold right) {
            return Ints.compare(left.getHierarchyLevel(), right.getHierarchyLevel());
        }
    };
    
    /**
     * Orders Structures by their database id
     */
     public static final Ordering<Structure> STRUCTURE_BY_ID = new Ordering<Structure>() {
         @Override
         public int compare(Structure left, Structure right) {
             return Ints.compare(left.getId(), right.getId());
         }
     };
     
     /**
      * Orders Structures by their database id
      */
      public static final Ordering<Structure> STRUCTURE_BY_SMILES = new Ordering<Structure>() {
          @Override
          public int compare(Structure left, Structure right) {
              return left.getSmiles().compareTo(right.getSmiles());
          }
      };
    
     /**
      * Orders molecules by some property.
      * 
      * @author Henning Garus
      */
     public static class DBOrdering extends Ordering<Molecule> {

         private Map<Integer, Integer> sortOrder = null;

         /**
          * Create a new Ordering
          * 
          * @param db
          *            the db manager
          * @param propertyDefiniton
          *            the property defintion which should be used to order the
          *            molecules
          * @param subset
          *            a subset which contains all molecules to be ordered
          * @throws DatabaseException
          */
         public DBOrdering(DbManager db, PropertyDefinition propertyDefiniton, Subset subset) throws DatabaseException {
             Preconditions.checkArgument(!propertyDefiniton.isScaffoldProperty());
             sortOrder = db.getSortOrder(subset, propertyDefiniton, true);
         }
         
         @Override
         public int compare(Molecule left, Molecule right) {
             Preconditions.checkState(sortOrder != null, "load has to be called first");
             int l = Objects.firstNonNull(sortOrder.get(left.getId()), Integer.MAX_VALUE);
             int r = Objects.firstNonNull(sortOrder.get(right.getId()), Integer.MAX_VALUE);
             return Ints.compare(l, r);
         }

     }
}
