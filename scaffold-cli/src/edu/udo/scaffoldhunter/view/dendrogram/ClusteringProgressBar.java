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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import edu.udo.scaffoldhunter.gui.clustering.ClusteringWorker;
import edu.udo.scaffoldhunter.gui.util.ProgressPanel;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * Holds the information about the clustering progress, and provides a panel to show it
 * 
 * @author Philipp Lewe
 * @author Till Schäfer
 * @param <S>
 * 
 */
public class ClusteringProgressBar<S extends Structure> implements ActionListener{
    private JPanel visualComponent = new JPanel();
    private JButton cancelClusteringButton;
    private JPanel progressDialog;
    private ClusteringWorker<S> worker;
    private boolean hidden;
    protected boolean blinkenBool;

    /**
     * Constructor
     */
    ClusteringProgressBar() {
        hidden = true;
    }

    /**
     * @return the visualComponent which shows the clustering progress or an
     *         information about the subset state
     */
    public JPanel getComponent() {
        return visualComponent;
    }

    /**
     * calculates the height of the JPanel so it can be as small as possible
     * 
     * @param maxWidth
     * @return the optimal needed height of the component
     */
    public int getOptimumHeight(int maxWidth) {
        int height;
        if (hidden) {
            height = 1;
        } else {
            // if the with is to small it brakes the line
            if (maxWidth > progressDialog.getWidth() + cancelClusteringButton.getWidth() + 10) {
                height = Math.max(progressDialog.getHeight(), cancelClusteringButton.getHeight()) + 10;
            } else {
                height = progressDialog.getHeight() + cancelClusteringButton.getHeight() + 15;
            }
        }
        return height;
    }

    /**
     * deletes the "a new clustering must be started" and adds the clustering
     * progress to the Panel
     * 
     * @param progressDialog
     * @param worker
     */
    public void showProgress(ProgressPanel<HierarchicalClusterNode<S>> progressDialog, ClusteringWorker<S> worker) {
        hidden = false;
        this.progressDialog = progressDialog;
        this.worker = worker;
        visualComponent.add(progressDialog);
        cancelClusteringButton = new JButton("Cancel");
        visualComponent.add(cancelClusteringButton);
        cancelClusteringButton.addActionListener(this);
        visualComponent.setPreferredSize(visualComponent.getMinimumSize());
        visualComponent.revalidate();

    }

    /**
     * removes the progress
     */
    public void hideProgress() {
        hidden = true;
        visualComponent.remove(progressDialog);
        visualComponent.remove(cancelClusteringButton);
        visualComponent.setPreferredSize(visualComponent.getMinimumSize());
        visualComponent.revalidate();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource().equals(cancelClusteringButton)) {
            worker.cancel(true);
        }
    };
}
