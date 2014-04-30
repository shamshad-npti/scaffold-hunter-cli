/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

package edu.udo.scaffoldhunter.model.treegen.net;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.openscience.cdk.interfaces.IMolecule;

import edu.udo.scaffoldhunter.model.db.Scaffold;

/**
 * Represents a scaffold network.
 * 
 * @author Nils Kriege
 */
public class ScaffoldNetwork {

    private Map<String, Node> scaffolds;
    
    /**
     * Creates a new scaffold network. 
     */
    public ScaffoldNetwork() {
        this.scaffolds = new HashMap<String, Node>();
    }
    
    /**
     * @return the nodes of this network
     */
    public Collection<Node> nodes() {
        return scaffolds.values();
    }
    
    /**
     * Adds a node to this network. 
     * @param scaffold the scaffold
     */
    public void addScaffold(Scaffold scaffold) {
        Node node = new Node(scaffold);
        scaffolds.put(scaffold.getSmiles(), node);
    }            
    
    /**
     * @param smiles smiles string of a scaffold
     * @return true iff a scaffold with the given smiles is
     * contained in this network
     */
    public boolean contains(String smiles) {
        return scaffolds.containsKey(smiles);
    }
    
    /**
     * @param smiles smiles string of a scaffold
     * @return the node belonging to the given scaffold
     */
    public Node getScaffoldNode(String smiles) {
        return scaffolds.get(smiles);
    }
    
    /**
     * @return the number of scaffolds in this network
     */
    public int size() {
        return scaffolds.size();
    }
    
    /**
     * Adds a virtual root to this network.
     */
    public void addRoot() {
        Scaffold scaf = new Scaffold();
        scaf.setSmiles("ROOT");
        Node root = new Node(scaf);
        for (Node n : nodes()) {
            if (n.getParents().isEmpty()) {
                root.addChild(n);
                n.addParent(root);
                n.setParentWeights(new int[1]);
            }
        }
        scaffolds.put("ROOT", root);
    }
    
    /**
     * Creates GML representation of this scaffold network.
     * @return string representation in GML format
     */
    public String createGmlGraph() {
        HashMap<ScaffoldNetwork.Node, Integer> map = new HashMap<ScaffoldNetwork.Node, Integer>();
        StringBuffer sb = new StringBuffer();
        sb.append("graph [\n");
        sb.append("\tdirected 0\n");
        int vID = 0;
        for (ScaffoldNetwork.Node n : nodes()) {
            int id = vID++;
            map.put(n, id);
            sb.append("\tnode [\n");
            sb.append("\t\tid "+id+"\n");
            sb.append("\t\tlabel \""+n.getScaffold().getSmiles()+"\"\n");
            sb.append("\t\tweight "+n.getScaffold().getHierarchyLevel()+"\n");
            sb.append("\t\tgraphics [\n");
            sb.append("\t\t\tw "+n.getScaffold().getSvgWidth()+".0\n");
            sb.append("\t\t\th "+n.getScaffold().getSvgHeight()+".0\n");
            sb.append("\t\t]\n");
            sb.append("\t]\n");
        }
        for (ScaffoldNetwork.Node n : nodes()) {
            for (int i=0; i<n.getParents().size(); i++) {
                ScaffoldNetwork.Node p = n.getParents().get(i);
                sb.append("\tedge [\n");
                sb.append("\t\tsource "+map.get(p)+"\n");
                sb.append("\t\ttarget "+map.get(n)+"\n");
                sb.append("\t\tlabel \""+n.getParentWeights()[i]+"\"\n");
                sb.append("\t]\n");
            }
        }
        sb.append("]\n");
        
        return sb.toString();
    }


    /**
     * A node in a scaffold network.
     */
    public class Node {
        
        private Scaffold scaffold;
        private ArrayList<Node> parents;
        private ArrayList<Node> children;
        private int[] parentWeights;
        private ArrayList<IMolecule> molecules;

        
        /**
         * @param scaffold
         */
        public Node(Scaffold scaffold) {
            this.scaffold = scaffold;
            this.parents = new ArrayList<Node>();
            this.children = new ArrayList<Node>();
            this.molecules = new ArrayList<IMolecule>();
        }
        
        /**
         * Adds a molecule to this scaffold.
         * @param mol the molecule
         */
        public void addMolecule(IMolecule mol) {
            molecules.add(mol);            
        }
        
        /**
         * Adds a parent of this node
         * @param parent the parent node
         */
        public void addParent(Node parent) {
            parents.add(parent);
        }
        
        /**
         * Adds a child of this node
         * @param child the child node
         */
        public void addChild(Node child) {
            children.add(child);
        }

        
        /**
         * @return the scaffold represented by this node
         */
        public Scaffold getScaffold() {
            return scaffold;
        }
        
        /**
         * @return the parent nodes
         */
        public ArrayList<Node> getParents() {
            return parents;
        }
        
        /**
         * @return the child nodes
         */
        public ArrayList<Node> getChildren() {
            return children;
        }
        
        /**
         * Sets the parent weights.
         * @param parentWeights
         */
        public void setParentWeights(int[] parentWeights) {
            this.parentWeights = parentWeights;
        }

        /**
         * @return weights of the parents
         */
        public int[] getParentWeights() {
            return parentWeights;
        }
        
        /**
         * @return level of the scaffold
         */
        public int getHierarchyLevel() {
            return scaffold.getHierarchyLevel();
        }
    }

}


