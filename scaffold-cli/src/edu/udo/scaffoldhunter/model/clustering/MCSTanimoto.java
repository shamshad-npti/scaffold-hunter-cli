/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.model.clustering;

import java.io.IOException;

import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.smsd.Isomorphism;
import org.openscience.cdk.smsd.interfaces.Algorithm;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Tanimoto coefficient based on the maximum common substructure of two
 * molecules.
 * 
 * @author Nils Kriege
 * @param <S> molecule / scaffold
 *
 */
public class MCSTanimoto<S extends Structure> extends Distance<S> {

    /**
     * Constructor 
     */
    public MCSTanimoto() {
        super(null);
    }

    @Override
    public double calcDist(HierarchicalClusterNode<S> node1, HierarchicalClusterNode<S> node2)
        throws ClusteringException {
        try {
            SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
            
            IAtomContainer A1 = sp.parseSmiles(node1.getContent().getSmiles());
            IAtomContainer A2 = sp.parseSmiles(node2.getContent().getSmiles());
            //Bond Sensitive is set true
            Isomorphism comparison = new Isomorphism(Algorithm.DEFAULT, true);
            // set molecules, remove hydrogens, clean and configure molecule
            comparison.init(A1, A2, true, true);
            // set chemical filter true
            comparison.setChemFilters(true, true, true);
    
            //Get similarity score
            double dist = 1-comparison.getTanimotoSimilarity();
        
            return dist;
        } catch (CDKException e) {
            throw new ClusteringException(e);
        } catch (IOException e) {
            throw new ClusteringException(e);
        }
    }

    @Override
    public PropertyType acceptedPropertyType() {
        return null;
//        return Distances.MCS_TANIMOTO.acceptedPropertyType();
    }
    
    @Override
    public PropertyCount acceptedPropertyCount() {
        return null;
//        return Distances.MCS_TANIMOTO.acceptedPropertyCount();
    }
    
}
