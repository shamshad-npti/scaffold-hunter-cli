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

package edu.udo.scaffoldhunter.view.table;

import java.util.ArrayList;
import java.util.List;

import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Michael Hesse
 *
 */
public class DataPumpBlock {
    List <PropertyDefinition> propertyDefinitions;
    List <Molecule> molecules;
    
    /**
     * 
     */
    public DataPumpBlock() {
        propertyDefinitions = new ArrayList <PropertyDefinition> ();
        molecules = new ArrayList <Molecule> ();
    }
    
    /**
     * @param propertyDefinitions
     * @param molecules
     */
    public DataPumpBlock(List <PropertyDefinition> propertyDefinitions, List <Molecule> molecules) {
        this.propertyDefinitions = propertyDefinitions;
        this.molecules = molecules;
    }

    /**
     * 
     * @param propertyDefinitions
     * @param molecules
     */
    /*
    public void set( List <PropertyDefinition> propertyDefinitions, List <Molecule> molecules) {
        this.propertyDefinitions = propertyDefinitions;
        this.molecules = molecules;
    }
    */
    
    /**
     * 
     * @param propertyDefinitions
     * @param molecules
     */
    /*
    public void add( List <PropertyDefinition> propertyDefinitions, List <Molecule> molecules) {
        this.propertyDefinitions.addAll(propertyDefinitions);
        this.molecules.addAll(molecules);
    }
    */
    
    /**
     * @return
     *  the propertyDefinitions
     */
    public List <PropertyDefinition> getPropertyDefinitions() {
        return propertyDefinitions;
    }
    /**
     * @return
     *  the molecules
     */
    public List <Molecule> getMolecules() {
        return molecules;
    }
    
    /**
     * clears the lists
     */
    /*
    public void clear() {
        propertyDefinitions.clear();
        molecules.clear();
    }
    */

    /**
     * 
     * @param propertyDefinition
     * @param molecule
     * @return
     *  true if the specified value is covered by this block
     */
    public boolean contains(PropertyDefinition propertyDefinition, Molecule molecule) {
        return ( propertyDefinitions.contains(propertyDefinition) & molecules.contains(molecule) );
    }
    
    /**
     * @param cBlock
     * @return
     *  true if cBlock specifies the same propertyDefinitions and molecules as this block
     *  (and vice versa)
     */
    public boolean isEqual(DataPumpBlock cBlock) {
        boolean isEqual = true;
        
        if( cBlock.getPropertyDefinitions().size() != propertyDefinitions.size() )
            isEqual = false;
        else if( cBlock.getMolecules().size() != molecules.size() )
            isEqual = false;

        if( isEqual )
            for( PropertyDefinition pd : cBlock.getPropertyDefinitions() )
                if( ! propertyDefinitions.contains(pd) ) {
                    isEqual = false;
                    break;
                }
        
        if( isEqual )
            for( Molecule m : cBlock.getMolecules() )
                if( ! molecules.contains(m) ) {
                    isEqual = false;
                    break;
                }

        return isEqual;
    }
}
