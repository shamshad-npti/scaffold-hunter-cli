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

package edu.udo.scaffoldhunter.model.clustering;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Simple struct that holds a {@link HierarchicalClusterNode} and a distance.
 * Mainly used for storing a nearest neighbor and its distance.
 * 
 * @author Till Schäfer
 * @param <S>
 *            the concrete {@link Structure}
 * 
 */
public class NodeAndDist<S extends Structure> {
    /**
     * the node
     */
    public HierarchicalClusterNode<S> node;
    /**
     * the distance
     */
    public double dist;

    /**
     * Constructor
     * 
     * @param node
     *            the node
     * @param dist
     *            the distance
     */
    public NodeAndDist(HierarchicalClusterNode<S> node, double dist) {
        this.node = node;
        this.dist = dist;

    }
}
