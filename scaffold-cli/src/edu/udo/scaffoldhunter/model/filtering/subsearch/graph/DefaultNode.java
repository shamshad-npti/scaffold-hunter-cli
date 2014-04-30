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

import java.util.LinkedList;
import java.util.List;

/**
 * Default implementation of the {@link Node} interface.
 * 
 * @author Nils Kriege
 * @author Till Schäfer
 * 
 */
public class DefaultNode implements Node {

    private int index;
    protected Object label;
    private LinkedList<Edge> edges;

    /**
     * Constructor
     * 
     * Attention: only call this from the {@link Graph}
     * 
     * @param label
     *            the label
     * @param index
     *            the index in the {@link Graph}
     */
    DefaultNode(Object label, int index) {
        this.label = label;
        this.index = index;
        edges = new LinkedList<Edge>();
    }

    @Override
    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public int getDegree() {
        return edges.size();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Object getLabel() {
        return label;
    }

    @Override
    public void setLabel(Object label) {
        this.label = label;
    }

    @Override
    public void addEdge(Edge e) {
        edges.addLast(e);
    }

    @Override
    public boolean removeEdge(Edge e) {
        return edges.remove(e);
    }
}
