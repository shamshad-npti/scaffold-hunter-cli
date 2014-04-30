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

package edu.udo.scaffoldhunter.gui.dialogs;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.GlobalConfigRB;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.util.SHPredicates;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Orderings;
import edu.udo.scaffoldhunter.view.util.ToolTipPropertyConfigurationEntry;

/**
 * @author Henning Garus
 * 
 */
public class TooltipConfigurationDialog extends JDialog {

    private final Dataset dataset;

    private final List<ToolTipPropertyConfigurationEntry> currentProperties;
    private final Set<String> selectedProperties = Sets.newHashSet();
    private final Map<String, ToolTipPropertyConfigurationEntry> newProperties = Maps.newHashMap();

    private final List<JCheckBox> showCheckBoxes = Lists.newArrayList();

    private final ShowListener showListener = new ShowListener();
    private final AccumulationListener accumulationListener = new AccumulationListener();
    private final CumulativeListener cumulativeListener = new CumulativeListener();

    private JCheckBox showTooltipCheckBox;
    private JCheckBox tooltipShowUndefinedPropertiesCheckBox;

    private GlobalConfig config;

    /**
     * 
     * @param owner
     *            the parent window
     * @param dataset
     *            the current dataset
     * @param config the global config object
     */
    public TooltipConfigurationDialog(Window owner, Dataset dataset, GlobalConfig config) {
        super(owner, I18n.get("Tooltip.Configuration.Title"), ModalityType.APPLICATION_MODAL);
        JPanel pane = new JPanel(new FormLayout("p, l:p:g, d", "p, 3dlu, min(d;400dlu):g, 3dlu, p"));
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(pane);

        this.dataset = dataset;
        this.currentProperties = config.getTooltipProperties(dataset);
        for (ToolTipPropertyConfigurationEntry e : currentProperties) {
            this.newProperties.put(e.getPropertyDefinitionKey(), (ToolTipPropertyConfigurationEntry) e.copy());
            this.selectedProperties.add(e.getPropertyDefinitionKey());
        }
        for (PropertyDefinition propDef : dataset.getPropertyDefinitions().values()) {
            if (!newProperties.containsKey(propDef.getKey())) {
                newProperties.put(propDef.getKey(), new ToolTipPropertyConfigurationEntry(propDef.getKey()));
            }
        }
        this.config = config;

        add(ButtonBarFactory.buildLeftAlignedBar(new JButton(new SelectAll()), new JButton(new SelectNone())),
                CC.xy(1, 1));

        add(buildOptionPanel(), CC.xyw(2, 1, 2));

        JPanel propertyPanel = buildPropertyPanel();
        JScrollPane scrollPane = new JScrollPane(propertyPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        add(scrollPane, CC.xyw(1, 3, 2));

        JButton okButton = StandardButtonFactory.createOKButton(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                apply();
                dispose();
            }
        });
        add(ButtonBarFactory.buildOKCancelApplyBar(okButton,
                StandardButtonFactory.createCancelButton(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dispose();
                    }
                }), StandardButtonFactory.createApplyButton(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
                        apply();
                    }
                })), CC.xyw(1, 5, 2));

        getRootPane().setDefaultButton(okButton);
        pack();
        setLocationRelativeTo(owner);
    }
    
    private void apply() {
        currentProperties.clear();
        for (String key : selectedProperties) {
            currentProperties.add(newProperties.get(key));
        }
        
        config.setShowTooltip(showTooltipCheckBox.isSelected());
        config.setTooltipShowUndefinedProperties(tooltipShowUndefinedPropertiesCheckBox.isSelected());
    }

    
    private JPanel buildOptionPanel() {
        GlobalConfigRB bundle = new GlobalConfigRB();
        
        JPanel panel = new JPanel(new FormLayout("r:d:g, r:d", "p"));
        showTooltipCheckBox = new JCheckBox(bundle.getString("showTooltip"), config.isShowTooltip());
        panel.add(showTooltipCheckBox, CC.xy(1, 1));

        tooltipShowUndefinedPropertiesCheckBox = new JCheckBox(bundle.getString("tooltipShowUndefinedProperties"), config.isTooltipShowUndefinedProperties());
        panel.add(tooltipShowUndefinedPropertiesCheckBox, CC.xy(2, 1));

        return panel;
    }

    private void buildTitle(DefaultFormBuilder builder, boolean mol) {
        String bold = "<html><b>%s</b></html>";
        JLabel l = new JLabel(String.format(bold, I18n.get("Model.PropertyDefinition")), JLabel.CENTER);
        builder.append(l);
        if (mol) {
            l = new JLabel(String.format(bold, I18n.get("Model.AccumulationFunction")), JLabel.CENTER);
            builder.append(l);
            l = new JLabel(String.format(bold, I18n.get("Model.SubtreeCumulative")), JLabel.CENTER);
            builder.append(l);
        }
        builder.nextLine();
    }

    private JPanel buildPropertyPanel() {
        FormLayout layout = new FormLayout("p, 3dlu, c:p:g(.5), 3dlu, c:p:g(.5)");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();

        List<PropertyDefinition> propDefs = Orderings.PROPERTY_DEFINITION_BY_TITLE.sortedCopy(dataset
                .getPropertyDefinitions().values());

        Collections.sort(propDefs, Orderings.PROPERTY_DEFINITION_BY_PROPERTY_TYPE);
        Iterable<PropertyDefinition> molPropDefs = Iterables.filter(propDefs,
                Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF));
        Iterable<PropertyDefinition> scaffoldPropDefs = Iterables.filter(propDefs, SHPredicates.IS_SCAFFOLD_PROPDEF);
        if (!Iterables.isEmpty(molPropDefs)) {
            builder.appendSeparator(I18n.get("Tooltip.PropertyTitles.Molecule"));
            builder.nextLine();
            buildTitle(builder, true);
            for (PropertyDefinition propDef : molPropDefs) {
                addProperty(builder, propDef);
                builder.nextLine();
            }
        }

        if (!Iterables.isEmpty(scaffoldPropDefs)) {
            builder.appendSeparator(I18n.get("Tooltip.PropertyTitles.Scaffold"));
            buildTitle(builder, false);
            for (PropertyDefinition propDef : scaffoldPropDefs) {
                addProperty(builder, propDef);
                builder.nextLine();
            }
        }

        return builder.getPanel();
    }

    private void addProperty(DefaultFormBuilder builder, PropertyDefinition propDef) {
        JCheckBox show = new JCheckBox();
        show.setText(propDef.getTitle());
        if (!propDef.getDescription().isEmpty()) {
            show.setToolTipText(propDef.getDescription());
        }
        show.setActionCommand(propDef.getKey());
        show.addActionListener(showListener);
        show.setSelected(selectedProperties.contains(propDef.getKey()));
        builder.append(show);
        this.showCheckBoxes.add(show);

        if (SHPredicates.IS_NUMMOL_PROPDEF.apply(propDef)) {

            JComboBox accumulationFunction = new JComboBox(AccumulationFunction.values());
            accumulationFunction.setActionCommand(propDef.getKey());
            accumulationFunction.addActionListener(accumulationListener);
            accumulationFunction.setSelectedItem(newProperties.get(propDef.getKey()).getAccumulationFunction());
            builder.append(accumulationFunction);

            JCheckBox cumulative = new JCheckBox();
            cumulative.setActionCommand(propDef.getKey());
            cumulative.addActionListener(cumulativeListener);
            cumulative.setSelected(newProperties.get(propDef.getKey()).isAccumulationWithSubtree());
            builder.append(cumulative);
        }
    }

    private class ShowListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox b = (JCheckBox) e.getSource();
            if (b.isSelected()) {
                selectedProperties.add(e.getActionCommand());
            } else {
                selectedProperties.remove(e.getActionCommand());
            }
        }

    }

    private class AccumulationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JComboBox b = (JComboBox) e.getSource();
            newProperties.get(e.getActionCommand()).setAccumulationFunction((AccumulationFunction) b.getSelectedItem());
        }
    }

    private class CumulativeListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox b = (JCheckBox) e.getSource();
            newProperties.get(e.getActionCommand()).setAccumulationWithSubtree(b.isSelected());
        }
    }

    private class SelectAll extends AbstractAction {

        SelectAll() {
            super(I18n.get("Tooltip.Configuration.SelectAll"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (JCheckBox b : showCheckBoxes) {
                // because using doClick is slow as hell...
                b.setSelected(true);
            }
            selectedProperties.addAll(newProperties.keySet());
        }
    }

    private class SelectNone extends AbstractAction {

        SelectNone() {
            super(I18n.get("Tooltip.Configuration.SelectNone"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // because using doClick is slow as hell...
            for (JCheckBox b : showCheckBoxes) {
                b.setSelected(false);
            }
            selectedProperties.clear();
        }
    }
}
