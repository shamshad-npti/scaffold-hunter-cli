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

package edu.udo.scaffoldhunter.model.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * @author Philipp Lewe
 *
 */
public class CanonicalSmilesGeneratorTest {
    
    SmilesParser sp;
    IAtomContainer m1;
    IAtomContainer m2;
    IAtomContainer m3;
    IAtomContainer m4;
    
    String s1;
    String s2;
    String s3;
    String s4;
    
    /**
     * @throws InvalidSmilesException
     */
    @org.junit.Before
    public void setup() throws InvalidSmilesException {
        sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
        m1 = sp.parseSmiles("CN2C(=O)N(C)C(=O)C1=C2N=CN1C"); //same
        m2 = sp.parseSmiles("CN1C=NC2=C1C(=O)N(C)C(=O)N2C"); //same
        m3 = sp.parseSmiles("CCO"); //ethanol
        m4 = sp.parseSmiles("C(C)O"); //ethanol
        s1 = CanonicalSmilesGenerator.createSMILES(m1, true);
        s2 = CanonicalSmilesGenerator.createSMILES(m2, true);
        s3 = CanonicalSmilesGenerator.createSMILES(m3, true);
        s4 = CanonicalSmilesGenerator.createSMILES(m4, true);
    }
    
    /**
     * 
     */
    @org.junit.Test
    public void compareSmiles1(){
            assertTrue("Cannonical smiles strings does NOT match", s1.equals(s2));
    }
    
    /**
     * 
     */
    @org.junit.Test
    public void compareSmiles2(){
            assertTrue("Cannonical smiles strings does NOT match", s3.equals(s4));
    }
    
    /**
     * 
     */
    @org.junit.Test
    public void compareSmiles3(){
            assertFalse("Cannonical smiles strings DOES match", s1.equals(s3));
    }
    
    /*
     *  TODO: Testcase:
     *  1. generate molecule from smiles parser (memorise smiles string)
     *  2. write molecule to sd-file
     *  3. read molecule from sd-file (memorise smiles string)
     *  4. compare strings
     */

}
