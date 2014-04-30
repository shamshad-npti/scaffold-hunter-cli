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

import java.util.Set;

import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.umd.cs.piccolo.util.PAffineTransform;

/**
 *
 */
public class ScaffoldTreeViewState extends ViewState implements VNodeListener {

    private final Set<String> openVNodeSMILES = Sets.newHashSet();
    
    private PAffineTransform cameraTransform;
    
    private double radialWidthLayoutRadiusAdd = 0;
    
    private double radialWidthLayoutRadiusUserFactor = 1.0;
    
    /**
     * If this flag is <b>true</b> the radii are fixed and will not scale while
     * zooming.
     */
    private boolean fixedRadii = false;
    
    private boolean showDetailsNodes = false;
    
    /**
     * Returns the distance between the circles in the Scaffold Tree View (before {@code radialWidthLayoutRadiusAdd}).
     * @return the distance between the circles
     */
    public double getRadialWidthLayoutRadiusAdd() {
        return radialWidthLayoutRadiusAdd;
    }

    /**
     * Stores the distance between the circles in the Scaffold Tree View (before {@code radialWidthLayoutRadiusAdd}).
     * @param radialWidthLayoutRadiusAdd the new distance
     */
    public void setRadialWidthLayoutRadiusAdd(double radialWidthLayoutRadiusAdd) {
        this.radialWidthLayoutRadiusAdd = radialWidthLayoutRadiusAdd;
    }
    
    /**
     * Returns the factor, which is applied to the distance between the circles in the Scaffold Tree View.
     * @return the factor for circle distance
     */
    public double getRadialWidthLayoutRadiusUserFactor() {
        return radialWidthLayoutRadiusUserFactor;
    }

    /**
     * Stores the factor, which is applied to the distance between the circles in the Scaffold Tree View.
     * @param radialWidthLayoutRadiusUserFactor the new factor
     */
    public void setRadialWidthLayoutRadiusUserFactor(double radialWidthLayoutRadiusUserFactor) {
        this.radialWidthLayoutRadiusUserFactor = radialWidthLayoutRadiusUserFactor;
    }

    /**
     *
     * @return true if this state is "new" i.e it does not hold any state yet
     */
    public boolean isNewState() {
        // this is kind of a kludge, but the set should always contain the first
        // ring
        return openVNodeSMILES.isEmpty();
    }

    /**
     * 
     * @param scaffold
     * @return <code>true</code> if the vnode showing scaffold should is shown
     */
    public boolean isOpenScaffold(Scaffold scaffold) {
        return openVNodeSMILES.contains(scaffold.getSmiles());
    }

    @Override
    public void vnodeAdded(ScaffoldNode vnode) {
        if (!vnode.getScaffold().isImaginaryRoot()) {
            openVNodeSMILES.add(vnode.getScaffold().getSmiles());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.scaffoldtree.VNodeListener#vnodeRemoved(edu
     * .udo.scaffoldhunter.view.scaffoldtree.VNode)
     */
    @Override
    public void vnodeRemoved(ScaffoldNode vnode) {
        openVNodeSMILES.remove(vnode.getScaffold().getSmiles());
    }
    
    /**
     * @return the cameraTransform
     */
    public PAffineTransform getCameraTransform() {
        return cameraTransform;
    }

    /**
     * @param cameraTransform the cameraTransform to set
     */
    public void setCameraTransform(PAffineTransform cameraTransform) {
        this.cameraTransform = cameraTransform;
    }

    /**
     * @return the fixedRadii
     */
    public boolean isFixedRadii() {
        return fixedRadii;
    }

    /**
     * @param fixedRadii the fixedRadii to set
     */
    public void setFixedRadii(boolean fixedRadii) {
        this.fixedRadii = fixedRadii;
    }

    /**
     * @return the showDetailsNodes
     */
    public boolean isShowDetailsNodes() {
        return showDetailsNodes;
    }

    /**
     * @param showDetailsNodes the showDetailsNodes to set
     */
    public void setShowDetailsNodes(boolean showDetailsNodes) {
        this.showDetailsNodes = showDetailsNodes;
    }

}
