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

package edu.udo.scaffoldhunter.view.dendrogram;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.Resources;
import edu.umd.cs.piccolo.event.PDragSequenceEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.event.PInputEventFilter;
import edu.umd.cs.piccolo.event.PPanEventHandler;
import edu.umd.cs.piccolo.nodes.PPath;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Philipp Kopp
 * 
 */
public class DendrogramEventHandler extends PDragSequenceEventHandler {
    GlobalConfig config;

    // variables for editing selection
    private PPath selectionRectangle;
    private Point2D startSelectionPoint;
    private final Selection selection;

    private PPanEventHandler panhandler = null;

    // the canvas this eventHandler is associated with
    private DendrogramCanvas associatedcanvas;

    private boolean shiftdown = false;
    private boolean controldown = false;

    /**
     * @param canvas
     *            that will use this EventHandler
     * @param selection
     *            the selection which determines if molecules are selected
     * @param config
     *            the configuration which determines how several events are
     *            handled
     */
    public DendrogramEventHandler(DendrogramCanvas canvas, Selection selection,
            GlobalConfig config) {
        super();
        associatedcanvas = canvas;
        this.selection = selection;
        this.config = config;
        PInputEventFilter ef = new PInputEventFilter();
        ef.rejectAllEventTypes();
        ef.setAcceptsMousePressed(true);
        ef.setAcceptsMouseClicked(true);
        ef.setAcceptsMouseEntered(true);
        ef.setAcceptsMouseExited(true);
        ef.setAcceptsMouseMoved(true);
        ef.setAcceptsMouseDragged(true);
        ef.setAcceptsMouseReleased(true);
        ef.setAcceptsKeyPressed(true);
        ef.setAcceptsKeyReleased(true);
        setEventFilter(ef);
    }

    /**
     * Evaluates single click mouse button actions.
     * 
     * @param event
     *            the generated PInputEvent event
     * 
     */
    @Override
    public void mousePressed(PInputEvent event) {
        super.mousePressed(event);

        Point p = new Point();
        p.setLocation(event.getCanvasPosition());

        /* left mouse button */
        if (((event.getButton() == MouseEvent.BUTTON1) && !(event.isShiftDown() || event.isControlDown()))
                && (event.getPickedNode() instanceof DendrogramViewNode))
            ((DendrogramViewNode) event.getPickedNode()).invertSelection();

        /* middle mouse button */
        if ((event.getButton() == MouseEvent.BUTTON2) && (event.getPickedNode() instanceof DendrogramViewNode)) {

            DendrogramViewNode contextNode = (DendrogramViewNode) event.getPickedNode();
            Structure contextStructure = contextNode.getModel().getContent();

            if (contextNode.isLeaf()) {

                if (event.isShiftDown()) {
                    boolean privateBannerIsSet = associatedcanvas.hasBanner(true, contextStructure);
                    if (privateBannerIsSet) {
                        associatedcanvas.removeBanner(true, contextStructure);
                    } else {
                        associatedcanvas.addBanner(true, contextStructure, contextNode);
                    }
                    contextNode.repaint();
                } else {
                    boolean publicBannerIsSet = associatedcanvas.hasBanner(false, contextStructure);
                    if (publicBannerIsSet) {
                        associatedcanvas.removeBanner(false, contextStructure);
                    } else {
                        associatedcanvas.addBanner(false, contextStructure, contextNode);
                    }
                    contextNode.repaint();
                }
            }
        }

        /* right mouse button (on leaf node) */
        if ((event.getButton() == MouseEvent.BUTTON3) && (event.getPickedNode() instanceof DendrogramViewNode)) {
            final DendrogramViewNode contextNode = (DendrogramViewNode) event.getPickedNode();
            final Structure contextStructure = contextNode.getModel().getContent();

            if (contextNode.isLeaf()) {

                JPopupMenu contextMenu = new JPopupMenu();
                JMenuItem privateBannerItem;
                JMenuItem publicBannerItem;
                JMenuItem showStructureInTable;

                // add entry to add / remove private banner
                privateBannerItem = new JMenuItem(new AbstractAction(_("Banner.TogglePrivateBanner"),
                        Resources.getImageIcon("images/banner_private.png")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        boolean privateBannerIsSet = associatedcanvas.hasBanner(true, contextStructure);
                        if (privateBannerIsSet) {
                            associatedcanvas.removeBanner(true, contextStructure);
                        } else {
                            associatedcanvas.addBanner(true, contextStructure, contextNode);
                        }
                        contextNode.repaint();
                    }
                });

                contextMenu.add(privateBannerItem);

                // add entry to add / remove public banner
                publicBannerItem = new JMenuItem(new AbstractAction(_("Banner.TogglePublicBanner"),
                        Resources.getImageIcon("images/banner_public.png")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        boolean publicBannerIsSet = associatedcanvas.hasBanner(false, contextStructure);
                        if (publicBannerIsSet) {
                            associatedcanvas.removeBanner(false, contextStructure);
                        } else {
                            associatedcanvas.addBanner(false, contextStructure, contextNode);
                        }
                        contextNode.repaint();
                    }
                });
                contextMenu.add(publicBannerItem);

                // add entry for
                showStructureInTable = new JMenuItem(new AbstractAction(
                        _("DendrogramView.Contextmenu.ShowStructureInTable")) {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        associatedcanvas.getTable().ensureStructureIsVisible(contextStructure);
                    }
                });
                contextMenu.add(showStructureInTable);

                // show context menu
                contextMenu.show(associatedcanvas, (int) event.getPosition().getX(), (int) event.getPosition().getY());
            }
        }

    }

    /**
     * Start drag. Begin drawing selection rectangle.
     * 
     * @param event
     *            the generated PInputEvent event
     * @see DendrogramEventHandler#mouseClicked(PInputEvent)
     */
    @Override
    public void startDrag(PInputEvent event) {
        super.startDrag(event);
        if (event.isLeftMouseButton() && (event.isShiftDown() || event.isControlDown())) {
            // safe panhandler and stop panning
            panhandler = associatedcanvas.getPanEventHandler();
            associatedcanvas.setPanEventHandler(null);
            startSelectionPoint = associatedcanvas.getCamera().viewToLocal(event.getPosition());
            selectionRectangle = new PPath();
            // TODO make selected Node Color universal configureable
            selectionRectangle.setStrokePaint(Color.RED);
            associatedcanvas.getCamera().addChild(selectionRectangle);
            shiftdown = event.isShiftDown();
            controldown = event.isControlDown();
        }
    }

    /**
     * Drag. Continue drawing selection rectangle.
     * 
     * @param event
     *            the generated PInputEvent event
     * @see DendrogramEventHandler#mouseClicked(PInputEvent)
     */
    @Override
    public void drag(PInputEvent event) {
        super.drag(event);
        if (event.isLeftMouseButton() && (shiftdown || controldown)) {
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
     * @see DendrogramEventHandler#mouseClicked(PInputEvent)
     */
    @Override
    public void endDrag(PInputEvent event) {
        super.endDrag(event);
        if (event.isLeftMouseButton() && (shiftdown || controldown)) {

            Point2D p = associatedcanvas.getCamera().viewToLocal(event.getPosition());
            PBounds b = new PBounds();
            b.add(startSelectionPoint);
            b.add(p);
            selectionRectangle.setPathTo(b);

            associatedcanvas.getCamera().removeChild(selectionRectangle);
            Rectangle2D selectedArea = associatedcanvas.getCamera().localToView(selectionRectangle.getBounds());
            if ((selectedArea.getWidth() != 0) && (selectedArea.getHeight() != 0)) {
                Collection<Molecule> structures = associatedcanvas.findStructuresInArea(associatedcanvas.getCamera()
                        .localToGlobal(selectionRectangle.getBounds()));

                if (shiftdown) {
                    selection.addAll(structures);
                } else {
                    selection.removeAll(structures);
                }
            }
            // reactivate panning
            associatedcanvas.setPanEventHandler(panhandler);
            // otherwise stupid behaviour...
            // panhandler.mouseReleased(event);
            shiftdown = false;
            controldown = false;
        }
    }

    // /**
    // * @author Philipp Kopp
    // *
    // */
    // public class ContextMenuActionlistener implements ActionListener {
    //
    // @Override
    // public void actionPerformed(ActionEvent e) {
    // if (e.getSource().equals(privateBannerItem)) {
    // if (privateBannerIsSet) {
    // associatedcanvas.removeBanner(true, contextStructure);
    // } else {
    // associatedcanvas.addBanner(true, contextStructure, contextNode);
    // }
    // contextNode.repaint();
    // }
    // if (e.getSource().equals(publicBannerItem)) {
    // if (publicBannerIsSet) {
    // associatedcanvas.removeBanner(false, contextStructure);
    // } else {
    // associatedcanvas.addBanner(false, contextStructure, contextNode);
    // }
    // contextNode.repaint();
    // }
    //
    // }
    //
    // }
}
