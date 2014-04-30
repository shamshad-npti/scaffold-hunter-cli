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

import edu.umd.cs.piccolo.event.PDragEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.util.PDimension;

/**
 * This class defines the dragging behaviour of the cluster selection bar
 * 
 * @author Philipp Lewe
 * 
 */
public class ClusterSelectionDragEventHandler extends PDragEventHandler {

    private double height;
    private double actHeight;
    private ClusterSelectionBar bar;

    ClusterSelectionDragEventHandler(double height, ClusterSelectionBar bar) {
        super();
        this.height = height;
        this.bar = bar;
        actHeight = 0;
        
        // react on mouse dragging
        PInputEventFilter ef = new PInputEventFilter();
        ef.setAcceptsMouseDragged(true);
        setEventFilter(ef);
    }

    /**
     * @param position
     */
    public void setPosition(double position) {
        actHeight = position;
        
    }

    /**
     * Moves the dragged node in proportion to the drag distance
     * 
     * @param event
     *            event representing the drag
     */
    @Override
    protected void drag(final PInputEvent event) {
        final PDimension d = event.getDeltaRelativeTo(super.getDraggedNode());
        super.getDraggedNode().localToParent(d);

        // if drag position is not outside the dendrogram tree (interval from
        // zero to height)
        if (!(actHeight + d.getHeight() < -5 || actHeight + d.getHeight() > height)) {

            // move node to new position
            super.getDraggedNode().offset(0, d.getHeight());
            actHeight += d.getHeight();
        }
        bar.fireClusterSelectionBarDragActive();
    }

    /**
     * @return the selectionbar Position;
     */
    public double getSelectionbarPosition() {
        return actHeight;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.umd.cs.piccolo.event.PDragSequenceEventHandler#dragActivityFirstStep
     * (edu.umd.cs.piccolo.event.PInputEvent)
     */
    @Override
    protected void dragActivityFirstStep(PInputEvent event) {
        bar.fireClusterSelectionBarDragStarted();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.umd.cs.piccolo.event.PDragSequenceEventHandler#dragActivityFinalStep
     * (edu.umd.cs.piccolo.event.PInputEvent)
     */
    @Override
    protected void dragActivityFinalStep(PInputEvent aEvent) {
        bar.fireClusterSelectionBarDragReleased();
    }
}
