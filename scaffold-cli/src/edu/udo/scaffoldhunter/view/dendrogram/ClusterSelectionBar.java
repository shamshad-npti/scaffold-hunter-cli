/*
 * ScaffoldHunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffoldhunter installation for details.
 *
 * This file is part of ScaffoldHunter.
 *
 * ScaffoldHunter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * ScaffoldHunter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package edu.udo.scaffoldhunter.view.dendrogram;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * This class represents the cluster selection bar for the scene graph of the
 * dendrogram
 * 
 * @author Philipp Lewe
 * 
 */
public class ClusterSelectionBar extends PNode {
    private static Logger logger = LoggerFactory.getLogger(ClusterSelectionBar.class);

    int height;
    int width;
    double treeHeight;
    double spaceLeftOfRoot;
    double position = 0;
    ClusterSelectionDragEventHandler handler;

    /**
     * Creates a ClusterSelectionBar with the given width and height
     * 
     * @param width
     *            the width of the ClusterSelectionBar
     * @param spaceLeftOfRoot
     *            the amount of shifting of the bar to the left
     * @param height
     *            the height of the ClusterSelectionBar
     * @param treeHeight
     *            the height of the tree in which the ClusterSelectionBar is
     *            installed. This is used to limit the position of the
     *            ClusterSelectionBar's to the interval between the root node of
     *            the DendrogramTree and the leaf nodes of the DendrogramTree
     */
    public ClusterSelectionBar(int width, int height, double treeHeight, double spaceLeftOfRoot) {
        this.width = width;
        this.height = height;
        this.treeHeight = treeHeight;
        this.spaceLeftOfRoot = spaceLeftOfRoot;
        this.height *= 2;
        setBounds(-spaceLeftOfRoot,0, this.width, this.height);
        handler = new ClusterSelectionDragEventHandler(treeHeight, this);
        this.addInputEventListener(handler);
    }
    /**
     * @param position
     */
    public void setBarPosition(double position) {
        this.position = position;
        setBounds(getBounds().x, position, getBounds().width, getBounds().height);
        handler.setPosition(position);     
        fireClusterSelectionBarDragStarted();
        fireClusterSelectionBarDragActive();
        fireClusterSelectionBarDragReleased();
    }
    /**
     * @return the actual position
     */
    public double getBarPosition() {
        return handler.getSelectionbarPosition();
    }

    @Override
    public void paint(PPaintContext aPaintContext) {

        Graphics2D g2 = aPaintContext.getGraphics();

        logger.trace("cluster selection bar fullbounds: {}", getBoundsReference());

        g2.setStroke(new BasicStroke(1));
        g2.setPaint(Color.red);
        g2.drawLine((int) getBounds().getX(), (int) ((getBounds().getHeight()) / 2 + position),
                (int) (getBounds().getX() + getBounds().getWidth()),
                (int) (getBounds().getHeight() / 2 + position));
    }

    /**
     * Adds the specified ClusterSelectionBarChangedEventListener to receive
     * events from this node.
     * 
     * @param listener
     *            the new ClusterSelectionBarChangedEventListener
     */
    public void addClusterSelectionBarChangedEventListener(ClusterSelectionBarChangedEventListener listener) {
        getListenerList().add(ClusterSelectionBarChangedEventListener.class, listener);
    }

    /**
     * Removes the specified ClusterSelectionBarChangedEventListener to receive
     * events from this node.
     * 
     * @param listener
     *            the new ClusterSelectionBarChangedEventListener
     */
    public void removeClusterSelectionBarChangedEventListener(ClusterSelectionBarChangedEventListener listener) {
        getListenerList().remove(ClusterSelectionBarChangedEventListener.class, listener);
    }

    synchronized void fireClusterSelectionBarDragStarted() {
        ClusterSelectionBarChangedEvent event = new ClusterSelectionBarChangedEvent(this);
        ClusterSelectionBarChangedEventListener[] listeners = getListenerList().getListeners(
                ClusterSelectionBarChangedEventListener.class);

        for (ClusterSelectionBarChangedEventListener listener : listeners) {
            listener.ClusterSelectionBarDragStarted(event);
        }
    }

    synchronized void fireClusterSelectionBarDragActive() {
        ClusterSelectionBarChangedEvent event = new ClusterSelectionBarChangedEvent(this);
        ClusterSelectionBarChangedEventListener[] listeners = getListenerList().getListeners(
                ClusterSelectionBarChangedEventListener.class);

        for (ClusterSelectionBarChangedEventListener listener : listeners) {
            listener.ClusterSelectionBarDragActive(event);
        }
    }

    synchronized void fireClusterSelectionBarDragReleased() {
        ClusterSelectionBarChangedEvent event = new ClusterSelectionBarChangedEvent(this);
        ClusterSelectionBarChangedEventListener[] listeners = getListenerList().getListeners(
                ClusterSelectionBarChangedEventListener.class);

        for (ClusterSelectionBarChangedEventListener listener : listeners) {
            listener.ClusterSelectionBarDragReleased(event);
        }
    }

}
