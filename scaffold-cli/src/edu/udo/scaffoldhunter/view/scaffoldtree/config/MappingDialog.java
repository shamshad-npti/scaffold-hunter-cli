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

package edu.udo.scaffoldhunter.view.scaffoldtree.config;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.CardLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Map;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.collect.Maps;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.model.VisualFeature;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeViewConfig;

/**
 * A Dialog to configure mappings of properties to visual features.
 * 
 * @author Henning Garus
 * 
 */
public class MappingDialog extends JDialog implements ListSelectionListener {

    private final JList<VisualFeature> visualFeatures;

    private final ScaffoldTreeViewConfig config;
    private Map<VisualFeature, ConfigMapping> mappings = Maps.newEnumMap(VisualFeature.class);

    private final CardLayout featurePanelLayout = new CardLayout();
    private final JPanel featurePanelContainer = new JPanel(featurePanelLayout);

    private final Profile profile;

    /**
     * Create a new MappingDialog
     * 
     * @param owner
     *            the dialog's owner
     * @param config
     *            the configuration object which holds the mapping configuration
     * @param profile
     *            the profile of the current user
     * @param db 
     */
    public MappingDialog(Window owner, ScaffoldTreeViewConfig config, Profile profile, DbManager db, Subset subset) {
        super(owner, _("VisualMappings.Title"), ModalityType.DOCUMENT_MODAL);

        this.config = config;
        this.profile = profile;

        for (Map.Entry<VisualFeature, ConfigMapping> e : config.getMappings().entrySet()) {
            if (e.getValue() != null)
                this.mappings.put(e.getKey(), (ConfigMapping) e.getValue().copy());
        }
        for (VisualFeature f : VisualFeature.values())
            if (mappings.get(f) == null)
                mappings.put(f, new ConfigMapping(f));

        PanelBuilder builder = new PanelBuilder(new FormLayout("max(p;100dlu), 3dlu, p, 3dlu, p:g", "p:g, 3dlu, p"));
        builder.setDefaultDialogBorder();

        // remove non working VisualFeatures for beta
        Vector<VisualFeature> features = new Vector<VisualFeature>(Arrays.asList(VisualFeature.values()));
        features.remove(VisualFeature.NodeSize);
        visualFeatures = new JList<VisualFeature>(features);
        visualFeatures.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        visualFeatures.setSelectedIndex(0);
        visualFeatures.getSelectionModel().addListSelectionListener(this);

        builder.add(visualFeatures, CC.xy(1, 1));
        builder.add(new JSeparator(JSeparator.VERTICAL), CC.xywh(3, 1, 1, 2));
        builder.add(featurePanelContainer, CC.xy(5, 1));
        JButton okButton = buildOKButton();
        builder.add(ButtonBarFactory.buildOKCancelApplyBar(okButton, buildCancelButton(), buildApplyButton()),
                CC.xyw(4, 3, 2));

        for (VisualFeature f : VisualFeature.values()) {
            if (SinglePropertyPanel.supported(f))
                featurePanelContainer.add(new SinglePropertyPanel(mappings.get(f), profile, db, subset), f.name());
        }

        VisualFeature selected = visualFeatures.getSelectedValue();
        featurePanelLayout.show(featurePanelContainer, selected.name());

        setContentPane(builder.getPanel());
        getRootPane().setDefaultButton(okButton);
        pack();
        setLocationRelativeTo(owner);
        setMinimumSize(getSize());
        setResizable(false);
    }

    private JButton buildApplyButton() {
        return new JButton(new AbstractAction(_("Button.Apply")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                setConfig();
                mappings = Maps.newEnumMap(VisualFeature.class);
                for (Map.Entry<VisualFeature, ConfigMapping> entry : config.getMappings().entrySet())
                    mappings.put(entry.getKey(), (ConfigMapping) entry.getValue().copy());
            }
        });
    }

    private JButton buildOKButton() {
        return new JButton(new AbstractAction(_("Button.OK")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                setConfig();
                dispose();
            }
        });
    }

    private JButton buildCancelButton() {
        return new JButton(new AbstractAction(_("Button.Cancel")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
     * .ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        VisualFeature selected = visualFeatures.getSelectedValue();
        featurePanelLayout.show(featurePanelContainer, selected.name());
    }

    private void setConfig() {
        Map<VisualFeature, ConfigMapping> ms = Maps.newEnumMap(VisualFeature.class);
        for (ConfigMapping m : mappings.values()) {
            if (m.getProperty(profile.getCurrentSession().getDataset()) != null)
                ms.put(m.getVisualFeature(), m);
        }
        config.setMappings(ms);
    }
    
}
