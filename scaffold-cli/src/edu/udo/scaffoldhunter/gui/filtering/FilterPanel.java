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

package edu.udo.scaffoldhunter.gui.filtering;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.CustomComboBoxRenderer;
import edu.udo.scaffoldhunter.model.NumComparisonFunction;
import edu.udo.scaffoldhunter.model.StringComparisonFunction;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Filter;
import edu.udo.scaffoldhunter.model.db.Filterset;
import edu.udo.scaffoldhunter.model.db.NumFilter;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.StringFilter;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Thomas Schmitz
 * 
 */
public class FilterPanel extends JPanel {
    private FilterDialog dialog;
    private Vector<PropertyDefinition> propertyDefinitions;
    private ComboBoxModel propertyDefinitionsModel;
    private Filter filter;
    private Dataset dataset;
    private Filterset filterset;

    private JComboBox propDefCombo;

    private JComboBox numComparison;
    private JSpinner numValue;

    private JComboBox stringComparison;
    private JTextField stringValue;

    private CellConstraints cc;

    /**
     * @param dialog
     * @param propertyDefinitionsWithChoose
     * @param propertyDefinitions
     * @param filter
     * @param dataset
     * @param filterset
     */
    public FilterPanel(FilterDialog dialog, Vector<PropertyDefinition> propertyDefinitionsWithChoose,
            Vector<PropertyDefinition> propertyDefinitions, Filter filter, Dataset dataset, Filterset filterset) {
        this.dialog = dialog;
        this.propertyDefinitions = propertyDefinitions;
        if (filter != null)
            this.propertyDefinitionsModel = new DefaultComboBoxModel(propertyDefinitions);
        else
            this.propertyDefinitionsModel = new DefaultComboBoxModel(propertyDefinitionsWithChoose);
        this.filter = filter;
        this.dataset = dataset;
        this.filterset = filterset;

        FormLayout layout = new FormLayout("l:d, 5dlu, l:d, 5dlu, f:d:grow, 5dlu, l:d", // 7
                // columns
                "f:d:grow"); // 1 row
        this.setLayout(layout);
        cc = new CellConstraints();

        showFilter();

        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        this.setPreferredSize(new Dimension(535, propDefCombo.getPreferredSize().height + 10));
        this.setMaximumSize(new Dimension(535, propDefCombo.getPreferredSize().height + 10));
    }

    private void showFilter() {
        this.removeAll();

        showPropertyDefinition();
        if (filter != null) {
            if (filter.getClass() == NumFilter.class) {
                showNumFilter();
            } else if (filter.getClass() == StringFilter.class) {
                showStringFilter();
            }
            showDeleteButton();
        }
        dialog.pack();
    }

    private void showDeleteButton() {
        JButton deleteButton = new JButton(new RemoveFilterAction(this));
        Dimension dim = deleteButton.getPreferredSize();
        dim.width = dim.height + 4;
        deleteButton.setPreferredSize(dim);
        this.add(deleteButton, cc.xy(7, 1));
    }

    private void showStringFilter() {
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        for (StringComparisonFunction f : StringComparisonFunction.values())
            m.addElement(f);
        stringComparison = new JComboBox(m);
        stringComparison.getModel().setSelectedItem(((StringFilter) filter).getComparisonFunction());
        stringComparison.setRenderer(new StringComparisonRenderer());
        stringComparison.addActionListener(new StringComparisonChanged());
        this.add(stringComparison, cc.xy(3, 1));

        StringComparisonFunction func = (StringComparisonFunction) stringComparison.getSelectedItem();
        if (func != StringComparisonFunction.IsDefined && func != StringComparisonFunction.IsNotDefined)
            showStringValue();
    }

    private static class StringComparisonRenderer extends CustomComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                StringComparisonFunction val = (StringComparisonFunction) value;
                c.setToolTipText(val.getDescription());
            }
            return c;
        }
    }

    private void showStringValue() {
        stringValue = new JTextField();
        Dimension dim = stringValue.getPreferredSize();
        dim.width = 80;
        stringValue.setPreferredSize(dim);
        stringValue.setText(((StringFilter) filter).getValue());
        stringValue.getDocument().addDocumentListener(new StringValueChanged());
        this.add(stringValue, cc.xy(5, 1));
    }

    private class StringValueChanged implements DocumentListener {
        @Override
        public void changedUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            update();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            update();
        }

        private void update() {
            ((StringFilter) filter).setValue(stringValue.getText());
            dialog.setFiltersetChanged(true, true);
        }
    }

    private class StringComparisonChanged implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ((StringFilter) filter).setComparisonFunction((StringComparisonFunction) stringComparison.getModel()
                    .getSelectedItem());
            dialog.setFiltersetChanged(true, true);
            showFilter();
        }
    }

    private void showPropertyDefinition() {
        propDefCombo = new JComboBox(propertyDefinitionsModel);
        if (filter != null) {
            propertyDefinitionsModel.setSelectedItem(filter.getPropDef(dataset));
        }
        propDefCombo.setRenderer(new PropertyDefinitionRenderer());
        propDefCombo.addActionListener(new PropertyDefinitionChanged());
        this.add(propDefCombo, cc.xy(1, 1));
    }

    private static class PropertyDefinitionRenderer extends CustomComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                PropertyDefinition val = (PropertyDefinition) value;
                c.setText(val.getTitle());
                c.setToolTipText(val.getDescription());
            }
            return c;
        }
    }

    private void showNumFilter() {
        DefaultComboBoxModel m = new DefaultComboBoxModel();
        for (NumComparisonFunction f : NumComparisonFunction.values())
            m.addElement(f);
        numComparison = new JComboBox(m);
        numComparison.getModel().setSelectedItem(((NumFilter) filter).getComparisonFunction());
        numComparison.setRenderer(new NumComparisonRenderer());
        numComparison.addActionListener(new NumComparisonChanged());
        this.add(numComparison, cc.xy(3, 1));

        NumComparisonFunction func = (NumComparisonFunction) numComparison.getSelectedItem();
        if (func != NumComparisonFunction.IsDefined && func != NumComparisonFunction.IsNotDefined)
            showNumValue();
    }

    private static class NumComparisonRenderer extends CustomComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                NumComparisonFunction val = (NumComparisonFunction) value;
                c.setToolTipText(val.getDescription());
            }
            return c;
        }
    }

    private void showNumValue() {
        numValue = new JSpinner();
        numValue.setModel(new SpinnerNumberModel(((NumFilter) filter).getValue(), null, null, 1));
        numValue.setValue(((NumFilter) filter).getValue());
        numValue.addChangeListener(new NumValueChanged());
        this.add(numValue, cc.xy(5, 1));
    }

    private class PropertyDefinitionChanged implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            PropertyDefinition pd = (PropertyDefinition) propertyDefinitionsModel.getSelectedItem();
            if (pd != null) {
                if (filter != null && filter.getPropDef(dataset).isStringProperty() == pd.isStringProperty()) {
                    filter.setPropDef(pd);
                } else {
                    if (filter == null) {
                        propertyDefinitionsModel = new DefaultComboBoxModel(propertyDefinitions);
                        propertyDefinitionsModel.setSelectedItem(pd);
                        dialog.addNewFilterPanel(filterset);
                    } else {
                        filterset.getFilters().remove(filter);
                    }

                    Filter f;
                    if (pd.isStringProperty()) {
                        f = new StringFilter();
                        ((StringFilter) f).setComparisonFunction(StringComparisonFunction.IsDefined);
                        ((StringFilter) f).setValue("");
                    } else {
                        f = new NumFilter();
                        ((NumFilter) f).setComparisonFunction(NumComparisonFunction.IsDefined);
                    }
                    f.setFilterset(filterset);
                    f.setPropDef(pd);
                    filterset.getFilters().add(f);
                    filter = f;
                }
                showFilter();
            }
            dialog.setFiltersetChanged(true, true);
        }
    }

    private class NumComparisonChanged implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            ((NumFilter) filter).setComparisonFunction((NumComparisonFunction) numComparison.getModel()
                    .getSelectedItem());
            dialog.setFiltersetChanged(true, true);
            showFilter();
        }
    }

    private class NumValueChanged implements ChangeListener {
        private void update() {
            ((NumFilter) filter).setValue((Double) (numValue.getValue()));
            dialog.setFiltersetChanged(true, true);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            update();
        }
    }

    private class RemoveFilterAction extends AbstractAction {
        private JPanel panel;

        public RemoveFilterAction(JPanel panel) {
            super();
            this.panel = panel;
            putValue(Action.SHORT_DESCRIPTION, _("Filtersets.RemoveFilterDescription"));
            putValue(Action.SMALL_ICON, Resources.getIcon("minus.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            filterset.getFilters().remove(filter);
            dialog.removeFilterPanel(panel);
            dialog.setFiltersetChanged(true, true);
        }
    }
}
