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

package edu.udo.scaffoldhunter.view.scaffoldtree;

/**
 * A listener who is notified when a <code>VNode</code> is added to or removed
 * from a <code>VTree</code>
 * 
 * @author Henning Garus
 */
public interface VNodeListener {

    /**
     * Called when a <code>VNode</code> has been added.
     * 
     * @param vnode the new node
     */
    public void vnodeAdded(ScaffoldNode vnode);

    /**
     * Called when a <code>VNode</code> has been removed.
     * 
     * @param vnode the removed node
     */
    public void vnodeRemoved(ScaffoldNode vnode);
}
