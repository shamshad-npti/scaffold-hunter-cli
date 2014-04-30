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

import java.util.List;

/**
 * Simple graph structure used to represent a molecule.
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public interface Graph {

    /**
     * Get all {@link Node}s
     * 
     * @return all {@link Node}s
     */
    public List<Node> nodes();

    /**
     * Get all {@link Edge}s
     * 
     * @return all {@link Edge}s
     */
    public List<Edge> edges();

    /**
     * Get the {@link Node} of the Graph with a defined index
     * 
     * @param index
     *            the index of the {@link Node}
     * @return the {@link Node} for the index
     */
    public Node getNode(int index);

    /**
     * Get the count of all {@link Node}s
     * 
     * @return count of all {@link Node}s
     */
    public int getNodeCount();

    /**
     * Get the count of all {@link Edge}s
     * 
     * @return count of all {@link Edge}s
     */
    public int getEdgeCount();

    /**
     * Returns if an edge between {@link Node} u and {@link Node} v exists.
     * 
     * @param u
     *            the fist {@link Node}
     * @param v
     *            the second {@link Node}
     * @return if the {@link Edge} between {@link Node} v and u exists
     */
    public boolean hasEdge(Node u, Node v);

    /**
     * Return the Edge between {@link Node} u and {@link Node} v.
     * 
     * @param u
     *            the fist {@link Node}
     * @param v
     *            the second {@link Node}
     * @return the {@link Edge} between {@link Node} v and u OR null if the edge
     *         does not exist
     */
    public Edge getEdge(Node u, Node v);

    /**
     * Adds a new {@link Node} to the {@link Graph}
     * 
     * @param label
     *            the label or annotation
     * @return the new {@link Node}
     */
    Node addNode(Object label);

    /**
     * Adds a new {@link Edge} to the {@link Graph}
     * 
     * @param u
     *            the first {@link Node}
     * @param v
     *            the second {@link Node}
     * @param label
     *            the label or annotation
     * @return the new {@link Edge}
     */
    Edge addEdge(Node u, Node v, Object label);

    /**
     * Merges another {@link Graph} into this one
     * 
     * @param g
     *            the other {@link Graph}
     */
    void addGraph(Graph g);
}
