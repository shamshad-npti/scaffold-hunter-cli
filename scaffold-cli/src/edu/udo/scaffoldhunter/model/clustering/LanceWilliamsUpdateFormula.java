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

package edu.udo.scaffoldhunter.model.clustering;


/**
 * Interface as a replacement for a function pointer for a Lance Williams Update
 * Formula
 * 
 * @author Till Schäfer
 * 
 */
public interface LanceWilliamsUpdateFormula {

    /**
     * The Lance Williams Update Formula
     * 
     * d[k,(i.j)] = ai*d[k,i] + aj*d[k,j] + b*d[i,j] + c*|d[k,i] - d[k,j]|
     * 
     * @param ki
     *            distance between cluster k and i: d[k,i]
     * @param kj
     *            distance between cluster k and j: d[k,j]
     * @param ij
     *            distance between cluster i and j: d[i,j]
     * @param k
     *            size if cluster k
     * @param i
     *            size if cluster i
     * @param j
     *            size if cluster j
     * @return distance between cluster k and merged cluster (i,j): d[k,(i,j)]
     */
    public double newDistance(double ki, double kj, double ij, int k, int i, int j);
}
