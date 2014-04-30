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

package edu.udo.scaffoldhunter.model.filtering.subsearch.graph;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IBond;

/**
 * A {@link Graph} {@link DefaultEdge} that has additional linkage to the
 * corresponding {@link IBond}
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class MoleculeEdge extends DefaultEdge {
    IBond bond;

    /**
     * Constructor
     * 
     * @param u
     *            the fist adjacent {@link Edge}
     * @param v
     *            the second adjacent {@link Edge}
     * @param bond
     *            the linked bond
     */
    public MoleculeEdge(MoleculeNode u, MoleculeNode v, IBond bond) {
        super(u, v, null);
        label = getLabel(bond);
        this.bond = bond;
    }

    /**
     * Get the linked {@link IBond}
     * 
     * @return the linked {@link IBond}
     */
    public IBond getBond() {
        return bond;
    }

    private String getLabel(IBond bond) {
        // TODO dont misuse stereo bonds for wildcards
        if (bond.getStereo() == IBond.Stereo.E_OR_Z) {
            return "*";
        } else if (bond.getFlag(CDKConstants.ISAROMATIC)) {
            return ":";
        } else if (bond.getOrder() == IBond.Order.SINGLE) {
            return "-";
        } else if (bond.getOrder() == IBond.Order.DOUBLE) {
            return "=";
        } else if (bond.getOrder() == IBond.Order.TRIPLE) {
            return "#";
        } else {
            throw new IllegalArgumentException("Unknown bond type");
        }

    }
}
