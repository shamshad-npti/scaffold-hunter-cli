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

import java.text.DecimalFormat;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.udo.scaffoldhunter.util.I18n;

/**
 * This class is intended to be shown in the sidebar of a
 * <code>DendrogramView</code> to show statistics about the actually chosen
 * clusters. This class implements a
 * <code>ClusterSelectionBarChangedEventListener</code> to get notified about
 * actually chosen clusters and to refresh the information provided by its
 * components.
 * 
 * @author Philipp Lewe
 * 
 */
public class DendrogramClusterInfoPanel extends JPanel implements ClusterSelectionBarChangedEventListener {
    private DendrogramCanvas canvas;

    private List<Integer> clusterSizes = null;
    private int min;
    private int max;
    private double avg;
    private int size;
    private int total;

    private JLabel numClusters = new JLabel();
    private JLabel minClusterSize = new JLabel();
    private JLabel maxClusterSize = new JLabel();
    private JLabel avgClusterSize = new JLabel();

    /**
     * Default constructor
     * 
     * @param canvas
     *            the related canvas
     */
    public DendrogramClusterInfoPanel(DendrogramCanvas canvas) {
        super();

        this.canvas = canvas;
        canvas.addClusterSelectionBarChangedEventListener(this);

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(numClusters);
        add(new JLabel(I18n.get("DendrogramView.ClusterInfoPanel.ClusterSize")));
        add(minClusterSize);
        add(maxClusterSize);
        add(avgClusterSize);

        updateComponents();
    }

    /**
     * Calculate new values for all components
     */
    private void updateComponents() {
        size = 0;
        min = Integer.MAX_VALUE;
        max = Integer.MIN_VALUE;
        avg = 0;
        total = 0;

        clusterSizes = canvas.getClusterSizes();

        if (clusterSizes != null && !clusterSizes.isEmpty()) {
            size = clusterSizes.size();

            for (Integer i : clusterSizes) {
                total += i;
                if (i < min) {
                    min = i;
                }
                if (i > max) {
                    max = i;
                }
            }
            avg = total / size;
        }

        numClusters.setText(I18n.get("DendrogramView.ClusterInfoPanel.NumClusters") + ": " + size);
        minClusterSize.setText(" min: " + min);
        maxClusterSize.setText(" max: " + max);
        DecimalFormat df = new DecimalFormat("#####0.00");
        avgClusterSize.setText(" avg: " + df.format(avg));
    }

    @Override
    public void ClusterSelectionBarDragStarted(ClusterSelectionBarChangedEvent event) {
        // nothing to do here
    }

    @Override
    public void ClusterSelectionBarDragActive(ClusterSelectionBarChangedEvent event) {
        updateComponents();
    }

    @Override
    public void ClusterSelectionBarDragReleased(ClusterSelectionBarChangedEvent event) {
        updateComponents();
    }
}
