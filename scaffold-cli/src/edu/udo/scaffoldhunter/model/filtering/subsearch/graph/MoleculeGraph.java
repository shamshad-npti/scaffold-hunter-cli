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

import java.util.HashMap;

import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * A Graph that is constructed form an {@link IAtomContainer}. Using the Atoms
 * as {@link Node}s and the bonds as Edges.
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class MoleculeGraph extends DefaultGraph {
    private static Logger logger = LoggerFactory.getLogger(MoleculeGraph.class);

    IAtomContainer atomContainer;
    HashMap<IAtom, MoleculeNode> atomToNode;

    /**
     * Constructor.
     * 
     * @param atomContainer
     *            the {@link IAtomContainer} from which the {@link Graph} should
     *            be created.
     */
    public MoleculeGraph(IAtomContainer atomContainer) {
        this(atomContainer, false);
    }

    /**
     * Constructor.
     * 
     * @param atomContainer
     *            the {@link IAtomContainer} from which the {@link Graph} should
     *            be created.
     * @param configure
     *            if a the configure (percieveAtomTypesAndConfigureAtoms and
     *            detectAromaticity) should be applied as pre-processing
     */
    public MoleculeGraph(IAtomContainer atomContainer, boolean configure) {
        super(atomContainer.getAtomCount());

        if (configure) {
            try {
                AtomContainerManipulator.percieveAtomTypesAndConfigureAtoms(atomContainer);
                CDKHueckelAromaticityDetector.detectAromaticity(atomContainer);
            } catch (CDKException ex) {
                logger.warn("Configure of atom failed: {}", ex.getMessage());
            }
        }

        atomToNode = new HashMap<IAtom, MoleculeNode>();
        this.atomContainer = atomContainer;

        for (IAtom atom : atomContainer.atoms()) {
            MoleculeNode node = new MoleculeNode(atom, nodes.size());
            nodes.add(node);
            atomToNode.put(atom, node);
        }

        for (IBond bond : atomContainer.bonds()) {
            assert (bond.getAtomCount() == 2);
            MoleculeNode u = atomToNode.get(bond.getAtom(0));
            MoleculeNode v = atomToNode.get(bond.getAtom(1));

            addEdge(u, v, bond);
        }
    }

    @Override
    public MoleculeGraph clone() {
        try {
            IAtomContainer ac = getAtomContainer();
            IAtomContainer acClone = ac.clone();
            MoleculeGraph mg = new MoleculeGraph(acClone);
            for (Node n : nodes()) {
                mg.getNode(n.getIndex()).setLabel(n.getLabel());
            }
            for (Edge e : edges()) {
                Node u = e.getFirstNode();
                Node v = e.getSecondNode();
                Edge e2 = mg.getEdge(mg.getNode(u.getIndex()), mg.getNode(v.getIndex()));
                e2.setLabel(e.getLabel());
            }
            return mg;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the underlying {@link IAtomContainer} of this graph
     * 
     * @return the underlying {@link IAtomContainer}
     */
    public IAtomContainer getAtomContainer() {
        return atomContainer;
    }

    /**
     * Returns the node representing the given {@link IAtom}.
     * @param atom atom of the underlying {@link IAtomContainer}
     * @return the representing {@link IAtom}
     */
    public MoleculeNode getNode(IAtom atom) {
        return atomToNode.get(atom);
    }

    @Override
    public MoleculeEdge addEdge(Node u, Node v, Object label) {
        Preconditions.checkArgument(u instanceof MoleculeNode, "Node type of u must be MoleculeNode");
        Preconditions.checkArgument(v instanceof MoleculeNode, "Node type of v must be MoleculeNode");
        Preconditions.checkArgument(label instanceof IBond, "Label type must be IBond");

        MoleculeEdge edge = new MoleculeEdge((MoleculeNode) u, (MoleculeNode) v, (IBond) label);
        u.addEdge(edge);
        v.addEdge(edge);
        edgeCount++;

        return edge;
    }

    @Override
    public String toString() {
        return super.toString();// +"\nM="+getAtomContainer();
    }

}
