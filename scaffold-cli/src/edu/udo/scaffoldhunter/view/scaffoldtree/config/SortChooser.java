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

package edu.udo.scaffoldhunter.view.scaffoldtree.config;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.dataimport.PropertyDefinitionListCellRenderer;
import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.ColorEditor;
import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.SHPredicates;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Orderings;
import edu.udo.scaffoldhunter.util.ProgressAdapter;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.SideBarItem;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeView;
import edu.udo.scaffoldhunter.view.scaffoldtree.Sorting;
import edu.udo.scaffoldhunter.view.scaffoldtree.Sorting.SortSettings;

/**
 * A side bar item which allows initiating a sort of the scaffold tree. It is
 * used as well to set options related to sorting.
 * <p>
 * To initiate a new sort a SortChooser needs to know the current subset, this
 * can be achieved by adding it as a listener for subset changes.
 * 
 * @author Henning Garus
 */
public class SortChooser extends SideBarItem implements PropertyChangeListener {
    
    // These PropertyDefinitions are only used locally. They act as separators in the JComboBox for selecting a property for scaffold sorting
    private static final PropertyDefinition scaffoldProperties = new PropertyDefinition("----- "+I18n.get("ScaffoldTreeView.Sort.ScaffoldProperties")+" -----", "", PropertyType.None, "", false, false);
    private static final PropertyDefinition moleculeProperties = new PropertyDefinition("----- "+I18n.get("ScaffoldTreeView.Sort.MoleculeProperties")+" -----", "", PropertyType.None, "", false, false);
        
    /**
     * @param sorting
     *            the sorting which is backing this sort chooser
     * @param treeView 
     *            the treeView using this sort chooser
     * @param subset
     *            the subset which is currently shown by the tree
     */
    public SortChooser(Sorting sorting, ScaffoldTreeView treeView, Subset subset) {
        super(I18n.get("ScaffoldTreeView.Sort"), null, null);
        JPanel sortPanel = new JPanel();
        sortPanel.setLayout(new BoxLayout(sortPanel, BoxLayout.Y_AXIS));        
        
        sortPanel.add(Box.createRigidArea(new Dimension(10, 4)));
        sortPanel.add(new SortMoleculesPanel(sorting, treeView, subset));
        sortPanel.add(Box.createRigidArea(new Dimension(10, 8)));
        sortPanel.add(new SortScaffoldsPanel(sorting, treeView, subset));
                
        setComponent(sortPanel);       
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(ScaffoldTreeView.SUBSET_PROPERTY))
            ((SortScaffoldsPanel) getComponent().getComponent(3)).subset = (Subset) evt.getNewValue();
    }

    private static class SortScaffoldsPanel extends JPanel {

        private Subset subset;
        private final Sorting sorting;
        private final ScaffoldTreeView scaffoldTreeView;

        private final ColorEditor coloredit;
        private final JComboBox sortByChooser;
        private final JComboBox accumulationChooser;
        private final JCheckBox cumulative;
        private final JCheckBox addCaption;
        private final JCheckBox descending;
        private final JCheckBox colorSegments;
        private final JButton sortButton;
        private final JButton resetButton;
        private final JLabel syncLabel;

        SortScaffoldsPanel(Sorting sorting, ScaffoldTreeView treeView, Subset subset) {
            super(new FormLayout("fill:pref, right:pref:grow, right:15dlu", "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 15dlu, 3dlu, p, 3dlu, p:g"));
            this.sorting = sorting;
            this.scaffoldTreeView = treeView;
            this.subset = subset;
            
            setBorder(BorderFactory.createTitledBorder(I18n.get("ScaffoldTreeView.SortScaffolds")));            
          
            add(new JLabel(I18n.get("ScaffoldTreeView.Sort.SortScaffoldsBy")), CC.xyw(1, 1, 3));
            sortByChooser = buildSortByChooser(sorting.getDataset());
            add(sortByChooser, CC.xyw(1, 3, 3));
            sortByChooser.setToolTipText(I18n.get("Tooltip.ScaffoldTreeView.Sort.SortScaffoldsBy"));

            accumulationChooser = buildAccumulationChooser();
            add(accumulationChooser, CC.xy(1, 5));
            accumulationChooser.setToolTipText(I18n.get("Tooltip.ScaffoldTreeView.Sort.AcculumulationFunction"));

            cumulative = new JCheckBox(new CumulativeAction());
            cumulative.setSelected(sorting.getSelectedSortSettings().isCumulative());
            add(cumulative, CC.xyw(1, 7, 3));
            cumulative.setToolTipText(I18n.get("Tooltip.ScaffoldTreeView.Sort.Cumulative"));

            descending = new JCheckBox(new DescendingAction());
            descending.setSelected(sorting.getSelectedSortSettings().isDescending());
            add(descending, CC.xy(1, 9));

            colorSegments = new JCheckBox(new ColorSegmentsAction());
            colorSegments.setSelected(sorting.getSelectedSortSettings().isColorSegments());
            add(colorSegments, CC.xy(1, 11));

            coloredit = new ColorEditor(sorting.getSelectedSortSettings().getBackground());
            coloredit.setEnabled(sorting.getSelectedSortSettings().isColorSegments());
            coloredit.addPropertyChangeListener(ColorEditor.COLOR_PROPERTY, new ColorListener());
            add(coloredit, CC.xyw(2, 11, 2));

            addCaption = new JCheckBox(new AddCaptionAction());
            addCaption.setSelected(sorting.getSelectedSortSettings().isAddCaption());
            add(addCaption, CC.xy(1, 13));

            sortButton = new JButton(new SortAction());
            add(sortButton, CC.xyw(1, 15, 2));
            if (sorting.getSelectedSortSettings().getPropDef(subset.getSession().getDataset()) == null) {
                sortButton.setEnabled(false);
            }
            sortButton.setToolTipText(I18n.get("Tooltip.ScaffoldTreeView.Sort.SortApply"));
            
            resetButton = new JButton(new ResetAction());
            add(resetButton, CC.xyw(3, 15, 1));
            if (sorting.getSortState().getSortSettings().getPropDef(subset.getSession().getDataset()) == null) {
                resetButton.setEnabled(false);
            }
            resetButton.setToolTipText(I18n.get("Tooltip.ScaffoldTreeView.Sort.SortReset"));
            
            syncLabel = new JLabel();
            add(syncLabel, CC.xyw(1, 17, 3));
            syncLabel.setForeground(Color.RED);
            syncLabel.setText(I18n.get("ScaffoldTreeView.Sort.SortingChanged"));
                        
            checkAccumulationFunction();
            checkSortSync();
        }    

        private JComboBox buildSortByChooser(Dataset dataset) {
            Predicate<PropertyDefinition> filter = Predicates.or(SHPredicates.IS_SCAFFOLD_PROPDEF,
                    SHPredicates.IS_NUMMOL_PROPDEF);
            Iterable<PropertyDefinition> propDefs = Iterables.filter(dataset.getPropertyDefinitions().values(), filter);
            Vector<PropertyDefinition> propDefVector = new Vector<PropertyDefinition>();
            Iterables.addAll(propDefVector, propDefs);
            
            // sorts the properties first by type, then by name
            Collections.sort(propDefVector, new Ordering<PropertyDefinition>() {
                @Override
                public int compare(PropertyDefinition left, PropertyDefinition right) {                    
                    int result = Orderings.PROPERTY_DEFINITION_BY_STRUCTURE_TYPE.compare(left, right);
                    if(result == 0)
                        return Orderings.PROPERTY_DEFINITION_BY_TITLE.compare(left, right);
                    else
                        return result; 
                }
            });
            
            // adds two separators to the combo box
            Predicate<PropertyDefinition> onlyNummol = SHPredicates.IS_NUMMOL_PROPDEF;
            int numberOfNummolProps = Iterables.size(Iterables.filter(dataset.getPropertyDefinitions().values(), onlyNummol));
                        
            propDefVector.add(numberOfNummolProps, scaffoldProperties);
            propDefVector.add(0, moleculeProperties);
            
            
            
            JComboBox box = new JComboBox(propDefVector);
            box.setSelectedItem(sorting.getSelectedSortSettings().getPropDef(dataset));            
            
            box.setRenderer(new PropertyDefinitionListCellRenderer());
            box.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {                    
                    JComboBox box = (JComboBox) e.getSource();
                    
                    // catch separator properties and disable sort button
                    if(box.getSelectedItem() == scaffoldProperties || box.getSelectedItem() == moleculeProperties) {
                        checkAccumulationFunction();
                        sortButton.setEnabled(false);
                        sorting.getSelectedSortSettings().setPropDef(null);
                    }
                    else {
                        checkAccumulationFunction();
                        sortButton.setEnabled(true);
                        sorting.getSelectedSortSettings().setPropDef((PropertyDefinition) box.getSelectedItem());
                    }
                    checkSortSync(); 
                }
            });
            return box;
        }

        private JComboBox buildAccumulationChooser() {
            Vector<AccumulationFunction> accumulations = new Vector<AccumulationFunction>(
                    Arrays.asList(AccumulationFunction.values()));
            JComboBox box = new JComboBox(accumulations);
            box.setSelectedItem(sorting.getSelectedSortSettings().getFunction());
            box.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JComboBox box = (JComboBox) e.getSource();
                    sorting.getSelectedSortSettings().setFunction((AccumulationFunction) box.getSelectedItem());
                    checkSortSync();
                }
            });
            return box;
        }        
        
        private void checkAccumulationFunction() {
            PropertyDefinition propDef = (PropertyDefinition) sortByChooser.getSelectedItem();
            boolean accumulatedPropDef = propDef != null && !propDef.isScaffoldProperty();
            accumulationChooser.setEnabled(accumulatedPropDef || cumulative.isSelected());
        }
        
        /**
         * Returns the {@link SortSetting} object, which contains the current selected sort settings.
         * @return the SortSetting
         */        
        private SortSettings getCurrentSortSettings() {
            return sorting.getSelectedSortSettings();
        }
        
        /**
         * Returns the {@link SortSetting} object, which contains the current visible sort settings.
         * @return the SortSetting
         */        
        private SortSettings getAppliedSortSettings() {
            return sorting.getSortState().getSortSettings();
        }
        
        /**
         * Checks whether the visible sorting is in sync with the selected options and updated the SortChooser concerning sort sync.
         */  
        private void checkSortSync() {
            boolean isSync = true;
            try{
                if(!getCurrentSortSettings().getPropDefTitle().equals(getAppliedSortSettings().getPropDefTitle()))
                    isSync = false;
                if(!getCurrentSortSettings().getFunction().equals(getAppliedSortSettings().getFunction()) && accumulationChooser.isEnabled())
                    isSync = false;
                if(!getCurrentSortSettings().isCumulative() == getAppliedSortSettings().isCumulative())
                    isSync = false;
                if(!getCurrentSortSettings().isDescending() == getAppliedSortSettings().isDescending())
                    isSync = false;
                if(!getCurrentSortSettings().isColorSegments() == getAppliedSortSettings().isColorSegments())
                    isSync = false;
                if(!getCurrentSortSettings().getBackground().equals(getAppliedSortSettings().getBackground()))
                    isSync = false;
                if(!getCurrentSortSettings().isAddCaption() == getAppliedSortSettings().isAddCaption() && coloredit.isEnabled())
                    isSync = false;
            }
            catch(NullPointerException e) {}
            syncLabel.setVisible(!isSync);
            if(isSync)
                sortButton.setForeground(Color.BLACK);
            else
                sortButton.setForeground(Color.RED);
        }

        private class CumulativeAction extends AbstractAction {

            CumulativeAction() {
                super(I18n.get("ScaffoldTreeView.Sort.Cumulative"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                sorting.getSelectedSortSettings().setCumulative(box.isSelected());
                checkAccumulationFunction();
                checkSortSync();
            }

        }

        private class DescendingAction extends AbstractAction {

            DescendingAction() {
                super(I18n.get("VisualMappings.Gradient.Descending"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                sorting.getSelectedSortSettings().setDescending(box.isSelected());
                checkSortSync();              
            }
        }

        private class ColorSegmentsAction extends AbstractAction {

            ColorSegmentsAction() {
                super(I18n.get("ScaffoldTreeView.Sort.ColorSegments"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                sorting.getSelectedSortSettings().setColorSegments(box.isSelected());
                coloredit.setEnabled(box.isSelected());
                addCaption.setEnabled(box.isSelected());
                checkSortSync();      
            }
        }

        private class ColorListener extends GenericPropertyChangeListener<Color> {

            @Override
            public void propertyChange(GenericPropertyChangeEvent<Color> ev) {
                sorting.getSelectedSortSettings().setBackground(ev.getNewValue());
                checkSortSync(); 
            }

        }

        private class AddCaptionAction extends AbstractAction {

            AddCaptionAction() {
                super(I18n.get("ScaffoldTreeView.Sort.AddCaption"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                JCheckBox box = (JCheckBox) e.getSource();
                sorting.getSelectedSortSettings().setAddCaption(box.isSelected());
                checkSortSync(); 
            }
        }

        private class SortAction extends AbstractAction {

            SortAction() {
                super(I18n.get("ScaffoldTreeView.Sort"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                sortButton.setEnabled(false);
                sorting.apply();
                sorting.sortTree(subset, new ProgressAdapter<Void>() {

                    @Override
                    public void finished(Void result, boolean cancelled) {
                        sortButton.setEnabled(true);
                        resetButton.setEnabled(true);
                        checkSortSync();
                    }
                });
            }
        }
        
        private class ResetAction extends AbstractAction {
            
            public ResetAction() {
                super();
                putValue(Action.SMALL_ICON, Resources.getIcon("cancel.png"));
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                sortByChooser.setSelectedIndex(0);
                resetButton.setEnabled(false);
                sorting.apply();
                scaffoldTreeView.resetTree();
            }
        }
    }

    private static class SortMoleculesPanel extends JPanel {

        private Subset subset;
        private final Sorting sorting;
        private final ScaffoldTreeView scaffoldTreeView;

        private final JComboBox sortMoleculeChooser;

        SortMoleculesPanel(Sorting sorting, ScaffoldTreeView treeView, Subset subset) {
            super(new FormLayout("fill:50px:grow", "p, 3dlu, p"));
            this.sorting = sorting;
            this.scaffoldTreeView = treeView;
            this.subset = subset;
            setBorder(BorderFactory.createTitledBorder(I18n.get("ScaffoldTreeView.SortMolecules")));
            
            add(new JLabel(I18n.get("ScaffoldTreeView.Sort.SortMoleculesBy")), CC.xyw(1, 1, 1));
            sortMoleculeChooser = buildSortMoleculeChooser();
            add(sortMoleculeChooser, CC.xyw(1, 3, 1));    
            sortMoleculeChooser.setToolTipText(I18n.get("Tooltip.ScaffoldTreeView.Sort.SortMoleculesBy"));
        }
               
        private JComboBox buildSortMoleculeChooser() {
            List<PropertyDefinition> propdefs = Lists.newArrayList(
                    Iterables.filter(subset.getSession().getDataset().getPropertyDefinitions().values(), 
                            Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF)));
            Collections.sort(propdefs, Orderings.PROPERTY_DEFINITION_BY_TITLE);
            JComboBox box = new JComboBox(propdefs.toArray());
            box.setSelectedItem(scaffoldTreeView.getInstanceConfig().getMoleculeOrderProperty(
                    subset.getSession().getDataset()));
            box.setRenderer(new PropertyDefinitionListCellRenderer());
            box.addActionListener(new ActionListener() {
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    scaffoldTreeView.sortMoleculesBy((PropertyDefinition)sortMoleculeChooser.getSelectedItem());
                }
            });
            return box;
        }        
    }
}
