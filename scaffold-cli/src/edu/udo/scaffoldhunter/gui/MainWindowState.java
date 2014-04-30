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

package edu.udo.scaffoldhunter.gui;

import java.awt.Frame;
import java.awt.Rectangle;

import edu.udo.scaffoldhunter.util.ObjectWithProperties;


/**
 * The state of the main window. All properties can be observed using
 * PropertyChangeListeners.
 * 
 * @author Dominic Sacré
 */
public class MainWindowState extends ObjectWithProperties {

    /**
     * The orientation of the window's split, if any. 
     */
    public enum SplitOrientation {
        /**
         * No split
         */
        NONE,
        /**
         * Views side by side
         */
        HORIZONTAL,
        /**
         * Views on top of each other
         */
        VERTICAL,
    }

    /**
     * the frame bounds property
     */
    public static final String FRAME_BOUNDS_PROPERTY = "frameBounds";
    /**
     * the frame extended state property
     */
    public static final String FRAME_EXTENDED_STATE_PROPERTY = "frameExtendedState";
    /**
     * The active view property name
     */
    public static final String ACTIVE_VIEW_POSITION_PROPERTY = "activeViewPosition";
    /**
     * The split orientation property name
     */
    public static final String SPLIT_ORIENTATION_PROPERTY = "splitOrientation";
    /**
     * The split position property name
     */
    public static final String SPLIT_POSITION_PROPERTY = "splitPosition";
    /**
     * The side bar visible property name
     */
    public static final String SIDE_BAR_VISIBLE_PROPERTY = "sideBarVisible";
    /**
     * The subset bar visible property name
     */
    public static final String SUBSET_BAR_VISIBLE_PROPERTY = "subsetBarVisible";
    /**
     * The side bar width property name
     */
    public static final String SIDE_BAR_WIDTH_PROPERTY = "sideBarWidth";
    /**
     * The subset bar width property name
     */
    public static final String SUBSET_BAR_WIDTH_PROPERTY = "subsetBarWidth";


    private Rectangle frameBounds = null;
    private int frameExtendedState = Frame.NORMAL;
    private ViewPosition activeViewPosition = ViewPosition.DEFAULT;
    private SplitOrientation splitOrientation = SplitOrientation.NONE;
    private int splitPosition = 0;
    private boolean sideBarVisible = true;
    private boolean subsetBarVisible = true;
    private int sideBarWidth = 180;
    private int subsetBarWidth = 160;


    /**
     * @return  the bounds of the frame
     */
    public Rectangle getFrameBounds() {
        return frameBounds;
    }

    /**
     * Changes the bounds of the frame.
     * 
     * @param bounds
     *          the new bounds
     */
    public void setFrameBounds(Rectangle bounds) {
        Rectangle oldFrameBounds = frameBounds;
        frameBounds = bounds;
        firePropertyChange(FRAME_BOUNDS_PROPERTY, oldFrameBounds, frameBounds);
    }

    /**
     * @return  the extended state of the frame
     */
    public int getFrameExtendedState() {
        return frameExtendedState;
    }

    /**
     * Changes the extended state of the frame.
     * 
     * @param state
     *          the new extended state
     */
    public void setFrameExtendedState(int state) {
        int oldFrameExtendedState = frameExtendedState;
        frameExtendedState = state;
        firePropertyChange(FRAME_EXTENDED_STATE_PROPERTY, oldFrameExtendedState, frameExtendedState);
    }

    /**
     * @return the position of the active view
     */
    public ViewPosition getActiveViewPosition() {
        return activeViewPosition;
    }

    /**
     * Changes the active view
     * 
     * @param active
     *          the position of the view to be activated
     */
    public void setActiveViewPosition(ViewPosition active) {
        ViewPosition oldActiveViewPosition = activeViewPosition;
        activeViewPosition = active;
        firePropertyChange(ACTIVE_VIEW_POSITION_PROPERTY, oldActiveViewPosition, activeViewPosition);
    }

    /**
     * @return  the orientation of the window's split pane
     */
    public SplitOrientation getSplitOrientation() {
        return splitOrientation;
    }

    /**
     * Changes the orientation of the window's split pane
     * 
     * @param orientation
     *          the new orientation
     */
    public void setSplitOrientation(SplitOrientation orientation) {
        SplitOrientation oldSplitOrientation = splitOrientation;
        splitOrientation = orientation;
        firePropertyChange(SPLIT_ORIENTATION_PROPERTY, oldSplitOrientation, splitOrientation);
    }

    /**
     * @return  the position of the window's split pane
     */
    public int getSplitPosition() {
        return splitPosition;
    }

    /**
     * Changes the position of the window's split pane
     * 
     * @param position
     *          the new position
     */
    public void setSplitPosition(int position) {
        int oldSplitPosition = splitPosition;
        splitPosition = position;
        firePropertyChange(SPLIT_POSITION_PROPERTY, oldSplitPosition, splitPosition);
    }

    /**
     * @return  whether the side bar on the left hand side of the frame is
     *          visible
     */
    public boolean isSideBarVisible() {
        return sideBarVisible;
    }
    
    /**
     * Shows or hides the side bar on the left hand side of the frame
     * 
     * @param visible
     *          whether the side bar should be visible
     */
    public void setSideBarVisible(boolean visible) {
        boolean oldSideBarVisible = sideBarVisible;
        sideBarVisible = visible;
        firePropertyChange(SIDE_BAR_VISIBLE_PROPERTY, oldSideBarVisible, sideBarVisible);
    }

    /**
     * @return  whether the subset bar on the right hand side of the frame is
     *          visible
     */
    public boolean isSubsetBarVisible() {
        return subsetBarVisible;
    }

    /**
     * Shows or hides the subset bar on the right hand side of the frame
     * 
     * @param visible
     *          whether the subset bar should be visible
     */
    public void setSubsetBarVisible(boolean visible) {
        boolean oldSubsetBarVisible = subsetBarVisible;
        subsetBarVisible = visible;
        firePropertyChange(SUBSET_BAR_VISIBLE_PROPERTY, oldSubsetBarVisible, subsetBarVisible);
    }

    /**
     * @return  the width of the side bar
     */
    public int getSideBarWidth() {
        return sideBarWidth;
    }

    /**
     * Sets the width of the side bar
     * 
     * @param width
     */
    public void setSideBarWidth(int width) {
        int oldSideBarWidth = sideBarWidth;
        sideBarWidth = width;
        firePropertyChange(SIDE_BAR_WIDTH_PROPERTY, oldSideBarWidth, sideBarWidth);
    }

    /**
     * @return  the width of the subset bar
     */
    public int getSubsetBarWidth() {
        return subsetBarWidth;
    }

    /**
     * Sets the width of the subset bar
     * 
     * @param width
     */
    public void setSubsetBarWidth(int width) {
        int oldSubsetBarWidth = subsetBarWidth;
        subsetBarWidth = width;
        firePropertyChange(SUBSET_BAR_WIDTH_PROPERTY, oldSubsetBarWidth, subsetBarWidth);
    }

}
