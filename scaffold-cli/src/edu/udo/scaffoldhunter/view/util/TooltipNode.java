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

package edu.udo.scaffoldhunter.view.util;

import java.awt.geom.Rectangle2D;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * A view that wants to implement the generic TooltipEventHandler should make
 * sure that their nodes implement this interface if they want to have a tooltip
 * 
 * @author Lappie
 * 
 */
public interface TooltipNode {

    /**
     * Wether or not this node should display a tooltip. 
     * @return true when a tooltip should be displayed
     */
    public boolean hasTooltip();

    /**
     * Return the bounds of this node so that the position of the tooltip can be calculated. 
     * 
     * Should most likely be implemented by PNode
     * 
     * @return bounds of this node
     */
    public Rectangle2D getGlobalBounds();

    /**
     * Return the Structure that this tooltip should display
     * @return the structure represented by this node
     */
    public Structure getStructure();
}
