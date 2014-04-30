/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

import java.awt.BasicStroke;
import java.awt.Stroke;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.view.scaffoldtree.UnscaledNode;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * A camera which can be used to render a scene graph in a different way for
 * export.
 * 
 * @author Henning Garus
 * @author Nils Kriege
 */
public class ExportCamera extends PCamera {
    
    /**
     * Factor to adjust the size of exported {@link UnscaledNode}s.
     */
    public static double UNSCALED_NODE_FACTOR = 3d;

    /**
     * Paints every node in the layers of this camera. For nodes which implement
     * {@link ExportPaintNode} <code>exportPaint</code> is called instead of
     * <code>paint</code>.
     * 
     * @param paintContext
     *            the paintContext
     * @param unscaledNodes
     *            list of unscaled nodes not affected by view transform 
     */
    public void exportPaint(PPaintContext paintContext, Collection<UnscaledNode> unscaledNodes) {
        paintContext.pushCamera(this);
        paintContext.pushTransform(getViewTransformReference());
        paintContext.pushTransparency(getTransparency());
        @SuppressWarnings("unchecked")
        List<PLayer> layersReference = getLayersReference();
        for (PLayer layer : layersReference) {
            paintNode(layer, paintContext);
        }
        paintContext.popTransparency(getTransparency());
        paintContext.popTransform(getViewTransformReference());
        for (UnscaledNode node : unscaledNodes) {
            paintUnscaledNode(node, paintContext);
        }
        paintContext.popCamera();
    }
    
    /**
     * Paints {@link UnscaledNode}s that are not affected by the view transform, but 
     * require individual relocation.
     *  
     * @param node
     *            the node to be painted
     * @param paintContext
     *            the paint context
     */
    public void paintUnscaledNode(UnscaledNode node, PPaintContext paintContext) {
        node.scale(UNSCALED_NODE_FACTOR);
        node.relocate(this);
        paintNode(node, paintContext);
        node.scale(1d/UNSCALED_NODE_FACTOR);
    }
    
    /**
     * Walks down through the scene graph calling either <code>fullPaint</code>
     * or <code>exportPaint</code>. Before <code>fullPaint</code> is called the
     * node's children are removed since otherwise they would be painted
     * recursively. Afterwards the children are added again.
     * <p>
     * This should basically yield the same result as
     * <code>PNode.fullPaint</code> unless a node has overridden fullPaint.
     * 
     * @param node
     *            the node to be painted
     * @param paintContext
     *            the paint context
     */
    @SuppressWarnings("unchecked")
    private void paintNode(PNode node, PPaintContext paintContext) {
        // check visibility
        if (!node.getVisible() || node.getOccluded()) return;
        // paint node and children
        if (node instanceof ExportPaintNode) {
            // apply state change
            paintContext.pushTransform(node.getTransformReference(true));
            paintContext.pushTransparency(node.getTransparency());
            // paint
            ((ExportPaintNode)node).exportPaint(paintContext);
            // undo state change
            paintContext.popTransparency(node.getTransparency());
            paintContext.popTransform(node.getTransform());        
        } else if (node instanceof PPath) {
            PPath p = (PPath)node;
            Stroke stroke = p.getStroke();
            if (stroke instanceof PFixedWidthStroke) {
                p.setStroke(new BasicStroke(2 * ((PFixedWidthStroke)stroke).getLineWidth()));
            }
            p.fullPaint(paintContext);
            p.setStroke(stroke);
        } else {
            List<PNode> childrenList = Lists.newArrayList(node.getChildrenReference());
            node.removeAllChildren();
            node.fullPaint(paintContext);
            node.addChildren(childrenList);
        }
        
        // apply state change for children
        paintContext.pushTransform(node.getTransformReference(true));
        paintContext.pushTransparency(node.getTransparency());
        // paint children
        for (PNode child : (List<PNode>)node.getChildrenReference()) {
            paintNode(child, paintContext);
        }
        // undo state change
        paintContext.popTransparency(node.getTransparency());
        paintContext.popTransform(node.getTransform());   
    }  
}
