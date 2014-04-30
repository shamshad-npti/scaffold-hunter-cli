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

package edu.udo.scaffoldhunter.view.dendrogram;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Dimension;
import java.util.Collection;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.udo.scaffoldhunter.gui.clustering.ClusteringController;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * @author Philipp Kopp
 * 
 */
public class ClusterMethodInfoPanel extends JPanel {

    /**
     * Shows the used PropertyDefinitions
     * 
     * @param propDefKeys
     * @param subset
     * @param linkage
     * @param distance
     * @param nnSearch
     * @param nnSearchParameters
     */
    public ClusterMethodInfoPanel(Collection<String> propDefKeys, Subset subset, Linkages linkage, Distances distance,
            NNSearchs nnSearch, NNSearchParameters nnSearchParameters) {
        super();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(new JLabel(_("DendrogramView.ClusterMethodInfoPanel.Subset", subset.getTitle())));
        add(new JLabel(_("DendrogramView.ClusterMethodInfoPanel.Linkage", linkage.getName())));
        add(new JLabel(_("DendrogramView.ClusterMethodInfoPanel.Distance", distance.getName())));
        add(new JLabel(_("DendrogramView.ClusterMethodInfoPanel.Heuristic", !ClusteringController.isExact(nnSearch))));
        if (!ClusteringController.isExact(nnSearch)) {
            add(new JLabel(_("DendrogramView.ClusterMethodInfoPanel.Quality",
                    ClusteringController.getQuality(nnSearchParameters))));
            add(new JLabel(_("DendrogramView.ClusterMethodInfoPanel.Dimensionality",
                    ClusteringController.getDimensionality(nnSearchParameters))));
        }

        add(new JLabel(_("DendrogramView.ClusterMethodInfoPanel.PropertyDefinitions")));
        for (String key : propDefKeys) {
            add((new JLabel(subset.getSession().getDataset().getPropertyDefinitions().get(key).getTitle())));
        }
        setPreferredSize(new Dimension(160, (int) getMinimumSize().getHeight()));
    }
}
