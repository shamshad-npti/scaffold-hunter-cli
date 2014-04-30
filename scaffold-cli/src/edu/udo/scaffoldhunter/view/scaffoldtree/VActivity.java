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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import edu.umd.cs.piccolo.PRoot;
import edu.umd.cs.piccolo.activities.PActivity;

/**
 * This activity is responsible for all code that should run <b>after</b>
 * all input events are processed in every UI cycle.
 * It will update the position of icons added to the camera and recompute the
 * layout if required.
 */
public class VActivity extends PActivity {
    private VCanvas vcanvas;
    private ArrayList<ScaffoldNode.ExpandReduceIcon> icons;
    private List<UnscaledNode> unscaledNodes = Lists.newArrayList();
    private boolean iconsHidden = false;

    /**
     * Creates a new <code>VActivity</code> for canvas.
     * 
     * @param canvas the vcanvas to which this activity belongs
     */
    public VActivity (VCanvas canvas) {
        // The activity will run for infinite time and is active in every step
        super(-1,0,0);
        this.vcanvas = canvas;
        icons = new ArrayList<ScaffoldNode.ExpandReduceIcon>();
    }

    @Override
    protected void activityStarted () {
        Iterator<ScaffoldNode.ExpandReduceIcon> i = icons.iterator();
        while (i.hasNext())
            relocateIcon(i.next());
    }

    @Override
    protected void activityStep(long elapsedTime) {
        super.activityStep(elapsedTime);

        // set visibleNodesInvalid if the camera moves or nodes are added
        // to the nodelayer
        vcanvas.invalidateVisibleNodes();

        /* update layout if needed */
        VTree vtree = vcanvas.getVTree();
        if (vtree.getLayoutInvalid() && (vtree.getRoot() != null)) {
            vtree.getLayout().doLayout();

            // the layout is updated if scaffolds where added
            // or removed. the bound property scaffoldCound will be
            // updated once here and not for every single node that
            // was added or removed.
            vcanvas.updateScaffoldCount();

            //			// TODO only change the cumulative values that have changed
            //			if (vcanvas.getBinAggregationDomain() != PropertyBin.SINGLE_SCAFFOLD) {
            //				vcanvas.updateCumulativePropertyBinValues(vtree.getRoot());
            //				vcanvas.updateInfoBars();
            //			}
        }

        /* relocate icons, hide/show them */
        boolean requireHide = SemanticZoomLevel.MEDIUM.scaleIsBelowThreshold(vcanvas.getCamera().getViewScale());
        if (iconsHidden && !requireHide && !vcanvas.getVAnimation().isAnimatingLayout())
            showIcons();
        else if (!iconsHidden && (requireHide || vcanvas.getVAnimation().isAnimatingLayout()))
            hideIcons();

        PRoot root = getActivityScheduler().getRoot();
        if (!iconsHidden && (root.getPaintInvalid() || root.getChildPaintInvalid())) {
            relocateAllIcons();
        }
        if (root.getPaintInvalid() || root.getChildPaintInvalid())
            for (UnscaledNode node : unscaledNodes)
                node.relocate(vcanvas.getCamera());
    }

    //****************************************************************
    // Handle Icons
    //****************************************************************

    /**
     * Adds a new icon to this activity.
     * <p>
     * Icons are not part of their associated scene graph entries, since
     * they are never scaled. So they have to be added to this activity to
     * ensure correct positioning.
     * 
     * @param icon the icon to be added
     */
    public void addIcon(ScaffoldNode.ExpandReduceIcon icon) {
        icons.add(icon);
        if (!iconsHidden) vcanvas.getCamera().addChild(icon);
    }
    
    /**
     * Adds an unscaled node to this activity. When the canvas changes 
     * <code>relocate</code> will be called for all these unscaled nodes.
     *  
     * @param node the unscalde node to be added
     */
    public void addUnscaledNode(UnscaledNode node) {
        unscaledNodes.add(node);
        vcanvas.getCamera().addChild(node);
    }

    /**
     * Removes an icon from this activity.
     * 
     * @param icon the icon to be removed.
     */
    public void removeIcon(ScaffoldNode.ExpandReduceIcon icon) {
        icons.remove(icon);
        if (!iconsHidden) vcanvas.getCamera().removeChild(icon);
    }
    
    /**
     * 
     * @param node the unscaled node to be removed from this activity.
     */
    public void removeUnscaledNode(UnscaledNode node) {
        unscaledNodes.remove(node);
        vcanvas.getCamera().removeChild(node);
    }

    private void relocateAllIcons() {
        for (ScaffoldNode.ExpandReduceIcon icon : icons)
            relocateIcon(icon);
    }

    private void relocateIcon(ScaffoldNode.ExpandReduceIcon icon) {
        Point2D target = new Point2D.Double(icon.getVNode().getX(), icon.getVNode().getY() + icon.getVNode().getHeight());
        icon.getVNode().localToGlobal(target);
        vcanvas.getCamera().globalToLocal(target);
        vcanvas.getCamera().viewToLocal(target);
        icon.centerFullBoundsOnPoint(target.getX(), target.getY());
    }

    private void hideIcons() {
        vcanvas.getCamera().removeChildren(icons);
        iconsHidden = true;
    }

    private void showIcons() {
        vcanvas.getCamera().addChildren(icons);
        iconsHidden = false;
    }


}
