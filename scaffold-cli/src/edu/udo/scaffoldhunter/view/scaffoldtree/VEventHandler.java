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
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import javax.swing.Timer;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.gui.SubsetController;
import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Scaffolds;
import edu.udo.scaffoldhunter.view.View;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * The <b>VEventHandler</b> manages the user interactions
 * <ul>
 * <li>pan</li>
 * <li>node select</li>
 * <li>context window</li>
 * </ul>
 * 
 * @author Gebhard Schrader
 * @author Henning Garus
 * @see VZoomHandler
 */
public class VEventHandler extends PDragSequenceEventHandler implements PropertyChangeListener {
    private final BannerPool bannerPool;
    private PPath selectionRectangle;
    private Point2D startSelectionPoint;
    private boolean addToSelection;

    private Timer tooltipTimer;

    private GlobalConfig config;
    private Subset currentSubset;
    private final SubsetController subsetManager;
    private final Selection selection;
    private static final PInputEventFilter SUPPORTED_EVENTS = new PInputEventFilter();
    private static final PInputEventFilter NO_EVENTS = new PInputEventFilter();
    static {
        SUPPORTED_EVENTS.rejectAllEventTypes();
        SUPPORTED_EVENTS.setAcceptsMousePressed(true);
        SUPPORTED_EVENTS.setAcceptsMouseClicked(true);
        SUPPORTED_EVENTS.setAcceptsMouseEntered(true);
        SUPPORTED_EVENTS.setAcceptsMouseExited(true);
        SUPPORTED_EVENTS.setAcceptsMouseMoved(true);
        SUPPORTED_EVENTS.setAcceptsMouseDragged(true);
        SUPPORTED_EVENTS.setAcceptsMouseReleased(true);
        SUPPORTED_EVENTS.setAcceptsKeyPressed(true);
        SUPPORTED_EVENTS.setAcceptsKeyReleased(true);
        SUPPORTED_EVENTS.setAcceptsMouseWheelRotated(true);

        NO_EVENTS.rejectAllEventTypes();
    }

    // the canvas this eventHandler is associated with
    private VCanvas associatedcanvas;

    // temporaly safe of the panhandler when making a selection
    private PInputEventFilter panEventFilter;

    /**
     * Creates a new VEventHandler for the <code>canvas</code>.
     * 
     * @param canvas
     *            that will use this EventHandler
     * @param tooltipTimer
     *            the timer that manages when a tooltip pops-up. Give this so
     *            that this eventhandler can prevent other pops-up from opening.
     * @param selection
     *            the selection which determines if molecules associated with
     *            scaffolds shown in the vcanvas belonging to this event handler
     *            are selected
     * @param config
     *            the configuration which determines how several events are
     *            handled
     * @param bannerPool
     * @param subsetManager
     * @param currentSubset
     */
    public VEventHandler(VCanvas canvas, Timer tooltipTimer, Selection selection, GlobalConfig config,
            BannerPool bannerPool, SubsetController subsetManager, Subset currentSubset) {
        super();

        this.config = config;
        this.tooltipTimer = tooltipTimer;
        this.selection = selection;
        this.associatedcanvas = canvas;
        this.bannerPool = bannerPool;
        this.subsetManager = subsetManager;
        this.currentSubset = currentSubset;

        setEventFilter(SUPPORTED_EVENTS);
    }

    /**
     * block and unblock the event handler, the event handler will not receive
     * any events, while it is blocked.
     * 
     * @param blocked
     */
    public void setBlocked(boolean blocked) {
        if (blocked)
            setEventFilter(NO_EVENTS);
        else
            setEventFilter(SUPPORTED_EVENTS);
    }

    /**
     * 
     * @return <code>true</code> iff the event handler is blocked
     */
    public boolean isBlocked() {
        return getEventFilter().equals(NO_EVENTS);
    }

    private void showPopup(PInputEvent event) {
        Preconditions.checkArgument(event.isPopupTrigger());
        tooltipTimer.stop();
        if (event.getPickedNode() instanceof ScaffoldNode) {
            ContextMenu menu = new ContextMenu(associatedcanvas, (ScaffoldNode) event.getPickedNode(), bannerPool,
                    subsetManager, currentSubset, selection);
            Point2D pos = event.getCanvasPosition();
            menu.show(associatedcanvas, (int) pos.getX(), (int) pos.getY());
        }
    }

    // ****************************************************************
    // Eventhandling
    // ***************************************************************

    /**
     * Start drag. Begin drawing selection rectangle.
     * 
     * @param event
     *            the generated PInputEvent event
     * @see VEventHandler#mouseClicked(PInputEvent)
     */
    @Override
    public void startDrag(PInputEvent event) {
        super.startDrag(event);
        if (event.isLeftMouseButton() && (event.isShiftDown() || event.isControlDown())) {
            // safe panhandler event filter and stop panning
            panEventFilter = associatedcanvas.getPanEventHandler().getEventFilter();
            PInputEventFilter f = new PInputEventFilter();
            f.rejectAllEventTypes();
            associatedcanvas.getPanEventHandler().setEventFilter(f);
            startSelectionPoint = associatedcanvas.getCamera().viewToLocal(event.getPosition());
            selectionRectangle = new PPath();
            selectionRectangle.setStrokePaint(config.getSelectedColor());
            associatedcanvas.getCamera().addChild(selectionRectangle);
            addToSelection = event.isShiftDown();
        }
    }

    /**
     * Drag. Continue drawing selection rectangle.
     * 
     * @param event
     *            the generated PInputEvent event
     * @see VEventHandler#mouseClicked(PInputEvent)
     */
    @Override
    public void drag(PInputEvent event) {
        super.drag(event);
        if (event.isLeftMouseButton() && startSelectionPoint != null) {
            Point2D p = associatedcanvas.getCamera().viewToLocal(event.getPosition());
            PBounds b = new PBounds();
            b.add(startSelectionPoint);
            b.add(p);
            selectionRectangle.setPathTo(b);
        }
    }

    /**
     * End drag. Select all nodes among startpoint and endpoint of selection.
     * 
     * @param event
     *            the generated PInputEvent event
     * @see VEventHandler#mouseClicked(PInputEvent)
     */
    @Override
    public void endDrag(PInputEvent event) {
        super.endDrag(event);
        if (event.isLeftMouseButton() && startSelectionPoint != null) {
            associatedcanvas.getCamera().removeChild(selectionRectangle);
            Rectangle2D selectedArea = associatedcanvas.getCamera().localToView(selectionRectangle.getBounds());
            if ((selectedArea.getWidth() != 0) && (selectedArea.getHeight() != 0)) {
                Collection<Scaffold> scaffolds = associatedcanvas.findScaffoldsInArea(startSelectionPoint,
                        associatedcanvas.getCamera().viewToLocal(event.getPosition()));
                List<Molecule> molecules = Lists.newArrayList(Scaffolds.getMolecules(scaffolds));

                if (addToSelection) {
                    selection.addAll(molecules);
                } else {
                    selection.removeAll(molecules);
                }
            }
            // reactivate panning
            associatedcanvas.getPanEventHandler().setEventFilter(panEventFilter);

            startSelectionPoint = null;
        }
    }

    /**
     * Evaluates single right click mouse button action. click on right mouse
     * button shows a context window.</p>
     * 
     * @param event
     *            the generated PInputEvent event
     * @see VEventHandler#mouseClicked(PInputEvent)
     */
    @Override
    public void mouseReleased(PInputEvent event) {
        super.mouseReleased(event);
        if (event.isPopupTrigger()) {
            showPopup(event);
        }
    }

    /**
     * Evaluates single click mouse button actions.
     * <p>
     * Click on left mouse button selects a node and shows scaffold details,<br>
     * </br>
     * <p>
     * Click on middle mouse button sets the cursor on this node<br>
     * </br>
     * 
     * @param event
     *            the generated PInputEvent event
     * @see VEventHandler#mouseClicked(PInputEvent)
     */
    @Override
    public void mousePressed(PInputEvent event) {
        super.mousePressed(event);

        if (event.isPopupTrigger()) {
            showPopup(event);
        }

    }

    /**
     * Evaluates mouse clicks
     * 
     * @param event
     *            the generated PInputEvent event
     * @see VEventHandler#mousePressed(PInputEvent)
     */
    @Override
    public void mouseClicked(PInputEvent event) {
        if (event.getPickedNode() instanceof ScaffoldNode) {
            Scaffold pickedScaffold = ((ScaffoldNode) event.getPickedNode()).getScaffold();
            if (event.isLeftMouseButton()) {
                ScaffoldNode node = (ScaffoldNode) event.getPickedNode();
                if (selection.containsAll(node.getScaffold().getMolecules()))
                    selection.removeAll(node.getScaffold().getMolecules());
                else
                    selection.addAll(node.getScaffold().getMolecules());
            } else if (event.isMiddleMouseButton()) {
                if (event.isShiftDown()) {
                    if (bannerPool.hasBanner(pickedScaffold, true)) {
                        bannerPool.removeBanner(pickedScaffold, true);
                    } else {
                        bannerPool.addBanner(pickedScaffold, true);
                    }
                } else {
                    if (bannerPool.hasBanner(pickedScaffold, false)) {
                        bannerPool.removeBanner(pickedScaffold, false);
                    } else {
                        bannerPool.addBanner(pickedScaffold, false);
                    }
                }
            }
        } else if (event.getPickedNode() instanceof ScaffoldDetailsNode) {
            ScaffoldDetailsNode s = (ScaffoldDetailsNode) event.getPickedNode();
            s.clicked(event.getPosition());
        } else if (event.getPickedNode() instanceof ScaffoldDetailsNode.MoleculeNode) {
            ScaffoldDetailsNode.MoleculeNode n = (ScaffoldDetailsNode.MoleculeNode) event.getPickedNode();
            n.mousePressed();
            /* interaction with the expand/reduce button */
        } else if (event.getPickedNode() instanceof ScaffoldNode.ExpandReduceIcon) {
            ScaffoldNode.ExpandReduceIcon icon = (ScaffoldNode.ExpandReduceIcon) event.getPickedNode();

            if (icon.getVNode().isExpandable()) {
                associatedcanvas.expand(icon.getVNode());
            } else {
                // if cursor is in subtree give it a new position
                for (VNode v : icon.getVNode().getTreeChildren()) {
                    if (associatedcanvas.getCursorVNode().equals(v))
                        associatedcanvas.setNewCursor(icon.getVNode());
                }
                associatedcanvas.reduce(icon.getVNode());
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.
     * PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(View.SUBSET_PROPERTY)) {
            currentSubset = (Subset) evt.getNewValue();
        }
    }

}