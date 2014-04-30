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

import java.awt.geom.Point2D;

import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;

/**
 * A piccolo node, which is not scaled when the canvas is zoomed. This is
 * achieved by adding the unscaled nodes to a Piccolo camera instead of a
 * canvas and relocating them "by hand" in a <code>PActivity</code>. To 
 * perform relocation unscaled nodes are associated with another vnode and
 * are then placed at a specified point in that node's coordinate system.
 * 
 * For this to work, {@link #relocate} has to be called from some
 * <code>PActivity</code> for every unscaled node.
 * 
 * @author Henning Garus
 *
 */
public class UnscaledNode extends PNode {
    
    private final PNode associatedNode;
    private final Point2D position;
    
    /**
     * Create a new unscaled node, associate it with another <code>PNode</code>
     * and specify its position in that <code>PNode</code>'s coordinate system.
     * 
     * @param associatedNode the pnode this unscaled node is "glued" to.
     * @param position the position in the associated nodes coordinate system
     *  where this unscaled node will be placed.
     */
    public UnscaledNode(PNode associatedNode, Point2D position) {
        super();
        this.associatedNode = associatedNode;
        this.position = position;
    }


    /**
     * @return the pnode this unscaled node is "glued to"
     */
    public PNode getAssociatedNode() {
        return associatedNode;
    }


    /**
     * @return the position where this unscaled  node is placed in its
     *  associated nodes coordinate system 
     */
    public Point2D getPosition() {
        return position;
    }


    /**
     * Should be called by a <code>PActivity</code> to relocate this node,
     * after the canvas has changed.
     * 
     * @param camera the camera to which the unscaled node has been added 
     */
    public void relocate(PCamera camera) {
        Point2D target = (Point2D)position.clone();
        associatedNode.localToGlobal(target);
        camera.globalToLocal(target);
        camera.viewToLocal(target);
        this.centerFullBoundsOnPoint(target.getX(), target.getY());
    }
    
    

}
