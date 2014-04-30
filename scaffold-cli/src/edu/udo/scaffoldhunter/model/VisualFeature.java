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

package edu.udo.scaffoldhunter.model;

import edu.udo.scaffoldhunter.util.I18n;


/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public enum VisualFeature {
    /**
     * The node size
     */
    NodeSize,
    /**
     * The background color of a node
     */
    NodeBackgroundColor,
    /**
     * The broadness of an edge
     */
    EdgeThickness,
    /**
     * The edge color
     */
    EdgeColor,
    /**
     * A text label
     */
    Label,
    /**
     * Basically a horizontal bar diagram
     */
    InfoBar;

    @Override
    public String toString() {
        return I18n.get("VisualFeature." + name());
    };
}
