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

package edu.udo.scaffoldhunter.gui.util;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.google.common.base.Objects;

/**
 * A helper class which can display an arbitrary JPanel as a Tooltip, which has
 * "Eclipselike" behavior.
 * <p>
 * With "Eclipselike" being defined as follows:
 * <ul>
 * <li>on creation the JPanel is shown like a usual tooltip without a border or
 * titlebar
 * <li>on mouseover it is shown as an unfocused dialog including border and
 * titlebar
 * <li>as long as the tooltip is not focused it will disappear once the mouse
 * leaves its region of interest (an arbitrary region defined by the user, to
 * which the bounds of the tooltip are added)
 * <li>once it has been focused, it will only disappear when it loses its focus
 * <ul>
 * <p>
 * After creation
 * 
 * @author Henning Garus
 * 
 */
public class EclipseTooltip {

    /**
     * The tooltip background color specified by the <code>UIManager</code>
     */
    public static final Color BACKGROUND = Objects.firstNonNull(UIManager.getColor("ToolTip.background"),
            Color.LIGHT_GRAY);

    private final ToolTipImpl tooltip;

    /**
     * Create an "Eclipselike" tooltip and show it immidiately.
     * 
     * @param position
     *            point where the tooltip will be shown
     * @param owner
     *            the component above which the tooltip will be shown
     * @param region
     *            a rectangle defining this tooltip's region of interest, the
     *            tooltip will only be shown as long as the mouse stays inside
     *            that area or on the tooltip
     * @param content
     *            The jPanel which contains the actual content to be displayed.
     *            The tooltip's size will be derived from the preferred size of
     *            this panel.
     */
    public EclipseTooltip(Point position, JComponent owner, Rectangle2D region, JPanel content) {
        Window parent = (Window) owner.getTopLevelAncestor();
        tooltip = new ToolTipImpl(position, parent, region, content);
    }

    /**
     * hide the tooltip forever
     */
    public void destroy() {
        tooltip.destroy();
    }

    /**
     * call {@link JDialog#pack()} on the tooltip
     */
    public void pack() {
        tooltip.pack();
    }

    /**
     * Check if the tooltip is currently being displayed
     * 
     * @return <code>true</code> if the tooltip is currently being displayed
     *         otherwise <code>false</code>
     */
    public boolean isVisible() {
        return tooltip.isVisible();
    }

    // Use an inner class to hide that this is actually a JDialog. A lot of
    // things I don't want to think about could go wrong if EclipseTooltip
    // directly extends JDialog. Yes, this is poor man's private derivation.
    private static class ToolTipImpl extends JDialog implements WindowFocusListener, AWTEventListener,
            ComponentListener, ActionListener {

        // Delay in ms until the region of interest is shrunk to the dialog
        // bounds after the mouse entered the dialog;
        private static final int UPDATE_DELAY = 500;

        private final Rectangle region;
        private boolean hasEntered = false;
        private Rectangle regionOfInterest;
        private final Timer timer = new Timer(UPDATE_DELAY, this);

        private ToolTipImpl(Point position, Window owner, Rectangle2D region, JPanel content) {
            super(owner);
            timer.setRepeats(false);
            this.region = new Rectangle((int) region.getX(), (int) region.getY(), (int) region.getWidth(),
                    (int) region.getHeight());

            this.setLocation(position.x, position.y);
            Toolkit.getDefaultToolkit().addAWTEventListener(this,
                    MouseEvent.MOUSE_PRESSED | MouseEvent.MOUSE_MOVED | MouseEvent.MOUSE_EXITED);

            addComponentListener(this);
            addWindowFocusListener(this);
            // use the glassPane to catch mouse entered events for the whole
            // dialog
            getGlassPane().setVisible(true);
            getGlassPane().addMouseListener(new MouseHandler());
            add(content, BorderLayout.CENTER);
            setUndecorated(true);
            setFocusableWindowState(false);
            setAlwaysOnTop(true);
            setModal(false);
            pack();
            
            setVisible(true);
            updateRoi();

        }
        
        @Override
        public void pack() {
            super.pack();
                Point position = getLocation();
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

                /*
                 * add small offsets if the window is undecorated, otherwise 
                 * the window may jump to the other position when decoration
                 * is enabled.
                 */
                int undecoratedOffset = isUndecorated() ? 10 : 0;
                int newY = position.y;
                if (position.y + getHeight() + undecoratedOffset > screenSize.getHeight()) {
                    newY -= position.y + getHeight() - screenSize.getHeight() + 20;
                }
                if (position.getX() + getWidth() + undecoratedOffset < screenSize.width)
                    setLocation(position.x, newY);
                else
                    setLocation(position.x - getWidth() - 20, newY);
        }

        private void destroy() {
            timer.stop();
            Toolkit.getDefaultToolkit().removeAWTEventListener(this);
            dispose();
        }

        private void updateRoi() {
            if (hasEntered) {
                regionOfInterest = new Rectangle(getContentPane().getLocationOnScreen(), getContentPane().getSize());
            } else {
                Rectangle bounds = new Rectangle(getLocationOnScreen(), getSize());
                regionOfInterest = bounds.union(region);
            }
        }

        @Override
        public void eventDispatched(AWTEvent e) {
            switch (e.getID()) {
            case MouseEvent.MOUSE_MOVED:
            case MouseEvent.MOUSE_EXITED:
                if (!isFocused()) {
                    if (!regionOfInterest.contains(MouseInfo.getPointerInfo().getLocation())) {
                        destroy();
                    }
                }
                break;
            case MouseEvent.MOUSE_PRESSED:
                if (getBounds().contains(((MouseEvent) e).getLocationOnScreen()) && !isFocused()) {
                    setFocusableWindowState(true);
                    requestFocus();
                    Component c = ((MouseEvent) e).getComponent();
                    if (c != null && c.isFocusable()) {
                        c.requestFocus();
                    }
                }
            }
        }

        private class MouseHandler extends MouseAdapter {

            /*
             * (non-Javadoc)
             * 
             * @see
             * java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent
             * )
             */
            @Override
            public void mouseEntered(MouseEvent arg0) {
                if (isUndecorated()) {
                    timer.start();
                    dispose();
                    getGlassPane().setVisible(false);
                    setUndecorated(false);
                    pack();
                    setVisible(true);
                }
            }

        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.WindowFocusListener#windowGainedFocus(java.awt.event
         * .WindowEvent)
         */
        @Override
        public void windowGainedFocus(WindowEvent e) {
            // Do nothing
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.WindowFocusListener#windowLostFocus(java.awt.event
         * .WindowEvent)
         */
        @Override
        public void windowLostFocus(WindowEvent e) {
            if (e.getOppositeWindow() != e.getSource()) {
                destroy();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ComponentListener#componentResized(java.awt.event.
         * ComponentEvent)
         */
        @Override
        public void componentResized(ComponentEvent e) {
            updateRoi();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ComponentListener#componentMoved(java.awt.event.
         * ComponentEvent)
         */
        @Override
        public void componentMoved(ComponentEvent e) {
            updateRoi();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ComponentListener#componentShown(java.awt.event.
         * ComponentEvent)
         */
        @Override
        public void componentShown(ComponentEvent e) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.awt.event.ComponentListener#componentHidden(java.awt.event.
         * ComponentEvent)
         */
        @Override
        public void componentHidden(ComponentEvent e) {
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent
         * )
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            hasEntered = true;
            updateRoi();
        }
    }

}
