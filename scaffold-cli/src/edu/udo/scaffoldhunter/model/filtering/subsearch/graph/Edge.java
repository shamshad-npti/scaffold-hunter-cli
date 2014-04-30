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

/**
 * The edge of a {@link Graph}
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 */
public interface Edge {

    /**
     * Get the label
     * 
     * @return the label
     */
    public Object getLabel();

    /**
     * Set the label
     * 
     * @param o
     *            the label
     */
    public void setLabel(Object o);

    /**
     * Get the first adjacent {@link Node}
     * 
     * @return the fist adjacent {@link Node}
     */
    public Node getFirstNode();

    /**
     * Get the second adjacent {@link Node}
     * 
     * @return the second adjacent {@link Node}
     */
    public Node getSecondNode();

    /**
     * Get the opposite adjacent {@link Node} of v. I.e. the fist node if v is
     * the second and vice versa.
     * 
     * @param v the adjacent {@link Node}
     * @return the opposite adjacent {@link Node}
     */
    public Node getOppositeNode(Node v);

}
