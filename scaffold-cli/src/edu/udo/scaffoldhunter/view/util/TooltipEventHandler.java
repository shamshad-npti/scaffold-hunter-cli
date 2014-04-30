/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Timer;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.view.dendrogram.DendrogramEventHandler;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;

/**
 * A generic TooltipEventHandler, that can create tooltips for all kind of
 * Canvasses.
 * 
 * To use this, add this eventHandler to your canvas and make sure the nodes
 * that should have tooltip implement tooltipNode
 * 
 * @author Philipp Kopp
 * @author Lappie
 */
public class TooltipEventHandler extends PBasicInputEventHandler implements ActionListener, PropertyChangeListener {

    protected PCanvas associatedCanvas;
    private GlobalConfig config;

    // variables for tooltip manager
    private TooltipManager tooltipManager;
    private Timer tooltipTimer;
    private Point currentPosition = new Point();
    private Structure currentStructure = null;
    private Rectangle2D currentBounds = null;

    /**
     * @param canvas
     *            that will use this EventHandler
     * @param tooltipManager
     *            the manage used to show scaffold tooltips
     * @param config
     *            the configuration which determines how several events are
     *            handled
     */
    public TooltipEventHandler(PCanvas canvas, TooltipManager tooltipManager, GlobalConfig config) {
        associatedCanvas = canvas;
        this.tooltipManager = tooltipManager;
        this.config = config;

        // timer for waiting until creating a tooltip
        tooltipTimer = new Timer(config.getTooltipDelay(), this);
        tooltipTimer.setRepeats(false);
    }
    
    /**
     * This method checks if the mouse is over a structure and shows a tooltip
     * after a certain time.
     * 
     * @param event
     *            the generated PInputEvent
     * @see DendrogramEventHandler#mouseExited(PInputEvent)
     */
    @Override
    public void mouseEntered(PInputEvent event) {
        if (config.isShowTooltip()) {
            // ignore anything but ToolTipNode...
            if (!(event.getPickedNode() instanceof TooltipNode)) {
                currentStructure = null;
                return;
            }

            TooltipNode node = (TooltipNode) event.getPickedNode();
            // ignore non leaf nodes
            if (!node.hasTooltip()) {
                currentStructure = null;
                return;
            }

            currentStructure = node.getStructure();

            PCamera c = event.getTopCamera();

            // calculate the actual position in screen coordinates:
            // first calculate the position relative to the canvas
            currentBounds = c.viewToLocal(c.globalToLocal(node.getGlobalBounds()));
            // than add the canvas' screen location
            Point p = new Point(
                    (int) Math.ceil(currentBounds.getMaxX() + associatedCanvas.getLocationOnScreen().x + 5),
                    (int) Math.floor(currentBounds.getMinY() + associatedCanvas.getLocationOnScreen().y));
            currentBounds = new Rectangle2D.Double(currentBounds.getX() + associatedCanvas.getLocationOnScreen().x,
                    currentBounds.getY() + associatedCanvas.getLocationOnScreen().y, currentBounds.getWidth(),
                    currentBounds.getHeight());

            currentPosition.setLocation(p);
            if (!tooltipTimer.isRunning())
                tooltipTimer.start();
        }
    }

    /**
     * This method checks if the mouse leaves a structure an hides possible
     * tooltips.
     * 
     * @param event
     *            the generated PInputEvent
     * @see DendrogramEventHandler#mouseEntered(PInputEvent)
     */
    @Override
    public void mouseExited(PInputEvent event) {
        if (tooltipTimer.isRunning())
            tooltipTimer.stop();
    }

    @Override
    public void mouseMoved(PInputEvent event) {
        currentPosition.setLocation(event.getPosition());
        currentPosition.setLocation(((MouseEvent) event.getSourceSwingEvent()).getXOnScreen(),
                ((MouseEvent) event.getSourceSwingEvent()).getYOnScreen());
    }

    @Override
    public void mouseWheelRotated(PInputEvent event) {
        tooltipManager.hideTooltip();
        if (currentStructure != null)
            tooltipTimer.restart();
    }

    @Override
    public void actionPerformed(ActionEvent event) {

        if (currentStructure != null) {
            currentPosition.x += 10;
            currentPosition.y -= 15;
            tooltipManager.showTooltip(currentPosition, associatedCanvas, currentBounds, currentStructure);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

        config = (GlobalConfig) evt.getNewValue();
        tooltipManager.setShowUndefinedProperties(config.isTooltipShowUndefinedProperties());
        tooltipManager.setMaxSVGSize(config.getTooltipMaxSVGSize());
        // TODO uncomment to react on changes of global config
        // tooltipManager.setPropertyConfigurations(config.getTooltipProperties());
        tooltipTimer.setInitialDelay(config.getTooltipDelay());

    }
    
    /**
     * Get the timer of the tooltip. A user can use this to control when a tooltip should be displayed. 
     * Put it on stop when a tooltip should not be displayed
     * @return the timer that controls when a tooltip should be displayed
     */
    public Timer getTooltipTimer() {
        return tooltipTimer;
    }

}
