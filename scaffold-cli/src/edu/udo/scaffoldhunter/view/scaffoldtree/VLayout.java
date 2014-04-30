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

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import edu.umd.cs.piccolo.PLayer;

/**
 * All layout classes should extend this basic layout class.
 * This class provides options to enable/disable layout
 * animation while subclasses only have to overwrite the method
 * drawLayout() where the position of all nodes is calculated
 * and set via <code>centerNodeOn(VNode node, Point2D
 * position)</code>.
 * 
 * @author Wiesniewski
 * @author Kriege
 */
public abstract class VLayout {

    protected VTree vtree;
    protected ScaffoldTreeViewState state;

    /**
     * Create a new layout for <code>vtree</code>.
     * @param vtree
     * @param state 
     */
    public VLayout (VTree vtree, ScaffoldTreeViewState state) {
        this.vtree = vtree;
        this.state = state;
    }

    /**
     * @return the <code>VTree</code> associated with this layout.
     */
    public VTree getVTree() {
        return vtree;
    }

    /**
     * A layout may return a special background layer that underlines the
     * alignment of nodes. Subclasses should overwrite this method if a layer
     * should be displayed. This layer also may underline the order of
     * nodes.
     * @return a layer that will be displayed  under all nodes and edges;
     * null may be returned if no layer is desired.
     * @see VLayout#setSeparators(ArrayList, ArrayList, ArrayList)
     * @see VLayout#clearSeparators()
     */
    public PLayer getBackgroundLayer () {
        return null;
    }

    /**
     * This method is called to (re)compute the layout.
     */
    public void doLayout() {
        doLayout(false);
    }

    /**
     * This method is used to disable animation for a single layout calculation
     * independent of the configuration in VISControl.
     * @param disableAnimation if true the layout will be updated without
     * animating changes
     */
    public void doLayout(boolean disableAnimation) {
        vtree.getVCanvas().getVAnimation().startLayoutAnimationList(disableAnimation);
        drawLayout();
        vtree.getVCanvas().getVAnimation().stopLayoutAnimationList();
        vtree.setLayoutInvalid(false);
    }

    /**
     * This method should only be called within the layout class.
     * This method must be implemented and should calculate the layout.
     * The new positions of nodes should be set by calling centerNodeOn().
     * @see VLayout#doLayout()
     */
    protected abstract void drawLayout();


    /**
     * Centers the given node on the specified position.
     */
    protected void centerNodeOn(VNode node, Point2D position) {
        vtree.getVCanvas().getVAnimation().centerNodeOn(node, position);
    }

    /**
     * The layout may graphically divide the first ring in sectors
     * according to the current order of nodes.
     * @param separators a list containing the first node of all sectors
     * in the same order as in the graph
     * @param caption labels that should be displayed in the sectors
     * @param colors the colors of the segments
     * @see VLayout#getBackgroundLayer()
     */
    public void setSeparators(ArrayList<VNode> separators, ArrayList<String> caption, ArrayList<Color> colors) {}

    /**
     * Is called when the displayed sectors should be removed.
     */
    public void clearSeparators() {};

    /**
     * This methods can be used for general updates in the layout algorithm.
     */
    public void updateLayout() {};

    /**
     * This can be used for update the radii
     * @param delta
     */
    public void updateRadii(double delta) {};
    
    /**
     * This can be used to reset the radii
     */
    public void resetRadii() {};

    /**
     * This method can be used for fix the layout
     * @param enable
     */
    public void setFixedLayout(boolean enable) {};

    /**
     * @return <code>true</code> iff the layout is fixed.
     */
    public boolean getFixedLayout() {return true;};

    /**
     * This method will be called after the layout animation
     * has completed. Override this method to upgrade
     * the background layer after all positions of nodes
     * have changed.
     */
    public void layoutAnimationFinished() {};

}
