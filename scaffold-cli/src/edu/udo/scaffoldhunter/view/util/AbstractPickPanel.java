/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * A panel that is able to show the information that is under the mouse-pointer.
 * With support for a title (cellInformation) and a SVG.
 * 
 * Recommended use is extending this and create the missing GUI elements. Make a
 * listen-function and this as a listener to the canvas you want to observe.
 * 
 * @author Michael Hesse
 * 
 */
public abstract class AbstractPickPanel extends JPanel {

    protected JLabel cellInformation;
    protected JPanel contentPanel;
    protected SvgPanel svgPanel;

    protected SVGCache svgCache;

    static class SvgObserver implements SVGLoadObserver {

        SvgPanel panel;
        Structure structure;

        public SvgObserver(SvgPanel panel, Structure structure) {
            this.panel = panel;
            this.structure = structure;
        }

        @Override
        public void svgLoaded(SVG svg) {
            if (structure == panel.structureToLoad) {
                // apply
                panel.svg = svg;
                panel.currentStructure = structure;
                panel.repaint();
            }
        }

    }

    /**
     * the panel that shows the SVG
     */
    protected class SvgPanel extends JPanel {
        Structure currentStructure;
        Structure structureToLoad;
        SVG svg;

        public SvgPanel() {
            super();
            setBackground(Color.WHITE);
            currentStructure = null;
            structureToLoad = null;
        }

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (svg != null)
                svg.paint((Graphics2D) g, (double) g.getClipBounds().width, (double) g.getClipBounds().height);
        }

        public void setStructure(Structure structure) {
            if (structure == null) {
                currentStructure = null;
                structureToLoad = null;
                svg = null;
            } else if (structure == structureToLoad) {
                // no action required
            } else {
                // start a new thread that waits a little time and checks
                // if there was no further request for another structure,
                // before it tries to load the SVG

                structureToLoad = structure;

                new Thread() {
                    Structure stl;
                    SvgPanel panel;

                    public void kickstart(Structure s, SvgPanel p) {
                        stl = s;
                        panel = p;
                        start();
                    }

                    @Override
                    public void run() {
                        // wait a bit
                        try {
                            sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        // do the loading, if still neccessary
                        if (stl == structureToLoad) {
                            SvgObserver observer = new SvgObserver(panel, stl);
                            svg = svgCache.getSVG(stl, null, null, observer);
                            if (svg != null)
                                observer.svgLoaded(svg);
                        }
                    }

                }.kickstart(structure, this);
            }
            repaint();
        }
    }

    /**
     * Creates the cellInformation label and the contentPanel with the SvgPanel
     * in it. BorderLayout.SOUTH is free to place additional information.
     * 
     * @param db
     *            the databsemanager for loading the SVG's
     * 
     */
    public AbstractPickPanel(DbManager db) {
        super();
        setBackground(Color.WHITE);

        svgCache = new SVGCache(db);

        // create components
        cellInformation = new JLabel(" ");
        cellInformation.setForeground(Color.BLACK);
        contentPanel = new JPanel();
        contentPanel.setBackground(getBackground());
        contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        contentPanel.setLayout(new BorderLayout());

        svgPanel = new SvgPanel();
        contentPanel.add(svgPanel, BorderLayout.CENTER);

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // set up panel
        Box horizontalBox = Box.createHorizontalBox();
        horizontalBox.add(cellInformation);
        horizontalBox.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.gray));
        add(horizontalBox, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }
}
