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

package edu.udo.scaffoldhunter.view.treemap.sidebar;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.SwingWorker;
import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.treemap.TreeMapCanvas;
import edu.udo.scaffoldhunter.view.treemap.TreeMapView;
import edu.udo.scaffoldhunter.view.treemap.TreeMapViewState;
import edu.udo.scaffoldhunter.view.treemap.loading.TreeMapDbsLoader;
import edu.udo.scaffoldhunter.view.util.ToolboxComboBoxRenderer;

/**
 * Panel that holds the buttons to choose which properties one wants to see on
 * the TreeMapView.
 * 
 * @author Lappie
 * 
 */
public class PropertySelector extends JPanel implements ActionListener {

    private static Logger logger = LoggerFactory.getLogger(TreeMapView.class);
    
    private Map<String, PropertyDefinition> numProperties = new HashMap<String, PropertyDefinition>();

    private JRadioButton scaffoldRadioButton = new JRadioButton(I18n.get("TreeMapView.Mappings.Scaffolds"));
    private JRadioButton moleculeRadioButton = new JRadioButton(I18n.get("TreeMapView.Mappings.Molecules"));
    
    private JComboBox<String> sizeBox = new JComboBox<String>();
    private JComboBox<String> colorBox = new JComboBox<String>();
    private JComboBox<AccumulationFunction> accumulationBox = new JComboBox<AccumulationFunction>();
    
    private JCheckBox cumulativeBox = new JCheckBox();
    private JCheckBox subsetIntervalBox = new JCheckBox();
    
    private JProgressBar progressBar;
    private JButton plotButton = new JButton(I18n.get("TreeMapView.Mappings.Plot"));
    private JLabel syncLabel;

    @SuppressWarnings("unchecked")
    private List<? extends JComponent> components = Arrays.asList(scaffoldRadioButton, moleculeRadioButton,
            sizeBox, colorBox, plotButton);
    
    
    private TreeMapCanvas canvas;
    private TreeMapDbsLoader loader;
    private TreeMapViewState state;
    
    private PropertyChangeSupport propertyListeners = new PropertyChangeSupport(this);
    
    private final static String LOAD_PROPERTY = "load-property";
    
    private PropertyDataLoader pdl = null; //Saved to make sure no two copies exist 
    
    // these indices indicate, where the molecule and scaffold properties start in the size and color combo box
    private int moleculeStartIndexSize = 0;
    private int scaffoldStartIndexSize = 0;
    private int moleculeStartIndexColor = 0;
    private int scaffoldStartIndexColor = 0;
    
    private boolean disablePlot = false;

    /**
     * Create Size and combo color box.
     * @param canvas 
     *            The canvas necessary to update any loading-screens
     * @param subset
     *            The currently loaded subset to define the properties
     * @param loader
     *            The loader for the treemap to handle the buttons actions
     * @param state
     *            The state of the last session, restore last used (saved) user settings 
     */
    public PropertySelector(TreeMapCanvas canvas, Subset subset, TreeMapDbsLoader loader, TreeMapViewState state) {
        super(new FormLayout("4dlu, left:min, fill:pref:grow, 4dlu", "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, f:14dlu, 3dlu, p:g"));
        this.canvas = canvas;
        this.loader = loader;
        this.state = state;

        createOptionsPanel();
        loadSubset(subset);
        loadState();
        addActionListeners();
        checkSync();
    }
    
    private void setComponentsEnabled(boolean enabled) {
        for(JComponent component : components) 
            component.setEnabled(enabled);
    }
    
    @SuppressWarnings("unchecked")
    private void createOptionsPanel() {
        
        JPanel plotTypePanel = new JPanel(new FormLayout("4dlu, l:d:g(.5), 4dlu, l:d:g(.5), 4dlu", "p:g"));
        JPanel sizePanel = new JPanel(new FormLayout("0dlu, left:min, 4dlu, 4dlu, f:d:g, 0dlu", "p:g"));
        JPanel colorPanel = new JPanel(new FormLayout("0dlu, left:min, 4dlu, 4dlu, f:d:g, 0dlu", "p, 3dlu, p, 3dlu, p, 1dlu, p:g"));
        
        plotTypePanel.setBorder(BorderFactory.createTitledBorder(I18n.get("TreeMapView.Mappings.NodeType")));
        sizePanel.setBorder(BorderFactory.createTitledBorder(I18n.get("TreeMapView.Mappings.NodeSize")));
        colorPanel.setBorder(BorderFactory.createTitledBorder(I18n.get("TreeMapView.Mappings.NodeColor")));
        
        ToolboxComboBoxRenderer tcb = new ToolboxComboBoxRenderer();
        sizeBox.setRenderer(tcb);
        colorBox.setRenderer(tcb);
        
        // type panel
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(scaffoldRadioButton); //couple them together
        buttonGroup.add(moleculeRadioButton); //if one is selected, the other is unselected
      
        plotTypePanel.add(scaffoldRadioButton, CC.xyw(2, 1, 2));
        plotTypePanel.add(moleculeRadioButton, CC.xyw(4, 1, 2));
        
        // size panel
        
        sizePanel.add(new JLabel(I18n.get("TreeMapView.Mappings.Size")), CC.xyw(2, 1, 1));
        sizePanel.add(sizeBox, CC.xyw(4, 1, 2));
        
        // color panel
        
        JLabel accumulationLabel = new JLabel(I18n.get("TreeMapView.Mappings.Accumulation"));
        accumulationLabel.setToolTipText(I18n.get("Tooltip.ScaffoldTreeView.Sort.AcculumulationFunction"));
        
        accumulationBox.addItem(AccumulationFunction.Average);
        accumulationBox.addItem(AccumulationFunction.Sum);
        accumulationBox.addItem(AccumulationFunction.Minimum);
        accumulationBox.addItem(AccumulationFunction.Maximum);
        
        cumulativeBox.setText(I18n.get("TreeMapView.Mappings.Cumulative"));
        cumulativeBox.setToolTipText(I18n.get("Tooltip.ScaffoldTreeView.Sort.Cumulative"));
        
        subsetIntervalBox.setText(I18n.get("TreeMapView.Mappings.SubsetInterval"));
        subsetIntervalBox.setToolTipText(I18n.get("Tooltip.TreeMapView.SubsetInterval"));
        
        colorPanel.add(new JLabel(I18n.get("TreeMapView.Mappings.Color")), CC.xyw(2, 1, 1));
        colorPanel.add(accumulationLabel, CC.xyw(2, 3, 1));
        
        colorPanel.add(colorBox, CC.xyw(4, 1, 2));
        colorPanel.add(accumulationBox, CC.xyw(4, 3, 2));
        
        colorPanel.add(cumulativeBox, CC.xyw(2, 5, 4));
        
        colorPanel.add(subsetIntervalBox, CC.xyw(2, 7, 4));
        
        // rest
        
        progressBar = new JProgressBar();
        progressBar.setString("Loaded");
        progressBar.setStringPainted(true);
        
        syncLabel = new JLabel();
        syncLabel.setForeground(Color.RED);
        syncLabel.setText(I18n.get("TreeMapView.Mappings.MappingChanged"));
        
        add(plotTypePanel, CC.xyw(1, 1, 4));
        add(sizePanel, CC.xyw(1, 3, 4));
        add(colorPanel, CC.xyw(1, 5, 4));
        
        add(plotButton, CC.xyw(2, 7, 2));
        add(progressBar, CC.xyw(2, 9, 2));
        add(syncLabel, CC.xyw(2, 11, 2));
    }
    
    private void startLoading() {
        progressBar.setIndeterminate(true);
        progressBar.setString(I18n.get("TreeMapView.Properties.Loading"));
        
        setComponentsEnabled(false);
        canvas.startLoading();
    }
    
    private void stopLoading() {
        progressBar.setIndeterminate(false);
        progressBar.setString(I18n.get("TreeMapView.Properties.Loaded"));
        
        setComponentsEnabled(true);
        checkConsistency();
        canvas.stopLoading();
        
        propertyListeners.firePropertyChange(LOAD_PROPERTY, 0, 1);
    }
    
    private void checkSync() {
        if(disablePlot) {
            //if plot button is disabled, then do not show change message
            syncLabel.setVisible(false);
            plotButton.setForeground(Color.BLACK);
            return;
        }
        
        boolean isSync = true;
        if(state.getColorPropertySelected() != null &&
                !state.getColorPropertySelected().equals(I18n.get("TreeMapView.Mappings.None")) && 
                !state.getColorPropertySelected().equals(state.getColorProperty())) {
            isSync = false;
        }
        if(state.getSizePropertySelected() != null &&
                !state.getSizePropertySelected().equals(I18n.get("TreeMapView.Mappings.None"))&&
                !state.getSizePropertySelected().equals(state.getSizeProperty())) {
            isSync = false;
        }
        if(!state.isScaffoldRadioButtonSelected() == state.isScaffoldRadioButton())
            isSync = false;
        if(state.getFunction() != state.getFunctionSelected())
            isSync = false;
        if(!state.isCumulativeSelected() == state.isCumulative())
            isSync = false;
        if(!state.isSubsetIntervalSelected() == state.isSubsetInterval())
            isSync = false;
        syncLabel.setVisible(!isSync);
        if(isSync)
            plotButton.setForeground(Color.BLACK);
        else
            plotButton.setForeground(Color.RED);
    }
    
    private void checkAccumulation() {
        boolean showAcc = !state.getColorPropertySelected().equals(I18n.get("TreeMapView.Mappings.None"));
        if(colorBox.getSelectedIndex() > scaffoldStartIndexColor && !cumulativeBox.isSelected()) {
            // using scaffold property for color and no accumulation active -> no point in defining some accumulation function
            showAcc = false;
        }
        accumulationBox.setEnabled(showAcc);
    }
    
    private void checkConsistency() {
        if(sizeBox.getSelectedIndex() == moleculeStartIndexSize
                || sizeBox.getSelectedIndex() == scaffoldStartIndexSize
                || colorBox.getSelectedIndex() == moleculeStartIndexColor
                || colorBox.getSelectedIndex() == scaffoldStartIndexColor) {
            // these items are separators, therefore the plot button must be disabled
            disablePlot = true;
        }
        else {
            disablePlot = false;
        }
        
        if(sizeBox.getSelectedIndex() > scaffoldStartIndexSize) {
            // using scaffold property for size mapping -> not possible to draw molecule nodes
            // currently not possible to map scaffold property to size
            moleculeRadioButton.setSelected(false);
            scaffoldRadioButton.setSelected(true);
            moleculeRadioButton.setEnabled(false);
            scaffoldRadioButton.setEnabled(false);
        }
        else {
            moleculeRadioButton.setEnabled(true);
            scaffoldRadioButton.setEnabled(true);
        }
        
        plotButton.setEnabled(!disablePlot);
    }
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if(propertyListeners == null) //if this is called before creation of listeners do nothing
            return; 
        propertyListeners.addPropertyChangeListener(LOAD_PROPERTY, listener);
    }
    
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if(propertyListeners == null) //if this is called before creation of listeners do nothing
            return;
        propertyListeners.removePropertyChangeListener(LOAD_PROPERTY, listener);
    }

    /**
     * sets the subset that this model should serve to update its properties.
     */
    private void loadSubset(Subset subset) {
        // clear comboboxes
        sizeBox.removeAllItems();
        sizeBox.addItem(I18n.get("TreeMapView.Mappings.NrMolecules"));
        colorBox.removeAllItems();
        colorBox.addItem(I18n.get("TreeMapView.Mappings.None"));

        // detect and store new num properties
        if (subset != null) {
            List<PropertyDefinition> propertyDefList = new ArrayList<PropertyDefinition>();
            propertyDefList.addAll(subset.getSession().getDataset().getPropertyDefinitions().values());
            Collections.sort(propertyDefList, new Comparator<PropertyDefinition>() {
                @Override
                public int compare(PropertyDefinition a, PropertyDefinition b) {
                    return a.getTitle().compareTo(b.getTitle());
                }
            });
            
            ArrayList<PropertyDefinition> moleculeProperties = Lists.newArrayList();
            ArrayList<PropertyDefinition> scaffoldProperties = Lists.newArrayList();
            
            for (PropertyDefinition def : propertyDefList) {
                if (!def.isScaffoldProperty())
                    moleculeProperties.add(def);
                else
                    scaffoldProperties.add(def);
            }
            
            //TODO: Use prettier separators
            moleculeStartIndexSize = sizeBox.getItemCount();
            moleculeStartIndexColor = colorBox.getItemCount();
            sizeBox.addItem("----- "+I18n.get("ScaffoldTreeView.Sort.MoleculeProperties")+" -----");
            colorBox.addItem("----- "+I18n.get("ScaffoldTreeView.Sort.MoleculeProperties")+" -----");
            
            
            for (PropertyDefinition def : moleculeProperties) {
                if (!def.isStringProperty()) {
                    numProperties.put(def.getTitle(), def);
                    sizeBox.addItem(def.getTitle());
                    colorBox.addItem(def.getTitle());
                }
            }
            
            //TODO: Use prettier separators
            scaffoldStartIndexSize = sizeBox.getItemCount();
            scaffoldStartIndexColor = colorBox.getItemCount();
            colorBox.addItem("----- "+I18n.get("ScaffoldTreeView.Sort.ScaffoldProperties")+" -----");
            
            for (PropertyDefinition def : scaffoldProperties) {
                if (!def.isStringProperty()) {
                    numProperties.put(def.getTitle(), def);
                    colorBox.addItem(def.getTitle());
                }
            }
        }
    }
    
    private void loadState() {
        if(state.isScaffoldRadioButtonSelected()) {
            scaffoldRadioButton.setSelected(true);
        } else {
            moleculeRadioButton.setSelected(true);
        }
        if(state.getSizePropertySelected() != null) {
            sizeBox.setSelectedItem(state.getSizePropertySelected());
        }
        if(state.getColorPropertySelected() != null) {
            colorBox.setSelectedItem(state.getColorPropertySelected());
        }
        if(state.getFunctionSelected() != null) {
            accumulationBox.setSelectedItem(state.getFunctionSelected());
        }
        if(state.isCumulativeSelected()) {
            cumulativeBox.setSelected(true);
        } else {
            cumulativeBox.setSelected(false);
        }
        if(state.isSubsetIntervalSelected()) {
            subsetIntervalBox.setSelected(true);
        } else {
            subsetIntervalBox.setSelected(false);
        }
    }
    
    private void addActionListeners() {
        scaffoldRadioButton.addActionListener(this);
        moleculeRadioButton.addActionListener(this);
        sizeBox.addActionListener(this);
        colorBox.addActionListener(this);
        accumulationBox.addActionListener(this);
        cumulativeBox.addActionListener(this);
        subsetIntervalBox.addActionListener(this);
        plotButton.addActionListener(this);
    }
    
    private void saveState() {
        state.setScaffoldRadioButtonSelected(scaffoldRadioButton.isSelected());
        state.setSizePropertySelected(sizeBox.getItemAt(sizeBox.getSelectedIndex()));
        state.setColorPropertySelected(colorBox.getItemAt(colorBox.getSelectedIndex()));
        state.setFunctionSelected(accumulationBox.getItemAt(accumulationBox.getSelectedIndex()));
        state.setCumulativeSelected(cumulativeBox.isSelected());
        state.setSubsetIntervalSelected(subsetIntervalBox.isSelected());
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        checkConsistency();
        saveState();        
        if(event.getSource() == plotButton) { 
            //only for plot button 
            state.setScaffoldRadioButton(scaffoldRadioButton.isSelected());
            state.setSizeProperty(sizeBox.getItemAt(sizeBox.getSelectedIndex()));
            state.setColorProperty(colorBox.getItemAt(colorBox.getSelectedIndex()));
            state.setFunction(accumulationBox.getItemAt(accumulationBox.getSelectedIndex()));
            state.setCumulative(cumulativeBox.isSelected());
            state.setSubsetInterval(subsetIntervalBox.isSelected());
            plot();
        }
        checkSync();
        checkAccumulation();
    }
    
    /**
     * Performs a new plot of the tree map
     */
    public void plot() {
      //If size property is null, then set it to "None"
      if(state.getSizeProperty() == null)
            state.setSizeProperty(I18n.get("TreeMapView.Mappings.None"));     
      
      //Two threads loading at the same time is not allowed!
        if(pdl != null && !pdl.isDone()) {
            logger.error("Two Threads are about to load at the same time.");
            return;
        }
        pdl = new PropertyDataLoader();
        pdl.execute();
    }
    
    /**
     * Load the selected properties in a seperate SwingWorker-Thread. 
     * @author Lappie
     */
    private class PropertyDataLoader extends SwingWorker<Void, Void> {
        
        //////////////////////////////////////////////////
        // If you ever have problems where the progress bar keeps going
        // but the loading doesn't seem to finish. 
        // Copy this code and place it in actionPerformed. That will remove 
        // the Thread, and possible exception will now be displayed in the
        // console. 
        //
        @Override
        protected Void doInBackground() throws Exception {
            startLoading();
            boolean success = true;
            canvas.setDisplayMoleculeLeafs(!state.isScaffoldRadioButton());
            
            if(state.getSizeProperty().equals(I18n.get("TreeMapView.Mappings.NrMolecules"))) {
                loader.loadMoleculeSizes();
            }
            else {
                success = loader.loadSize(getSizeProperty());
            }
            
            try{
                success &= loader.loadColor(getColorProperty(), 
                        !state.isScaffoldRadioButton(), 
                        state.getFunction(), 
                        state.isCumulative(), 
                        state.isSubsetInterval());
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            if(success) { //If successful then reposition
                canvas.reposition();
            }
            return null;            
        }
        
        @Override
        protected void done() {
            stopLoading();
            canvas.repaint();
        }
    }

    /**
     * Load the given new subset. Load both the properties and let the loader know which data to load.
     * 
     * @param subset : the new subset 
     */
    public void loadNewSubset(Subset subset) {
        
        //Two threads loading at the same time is not allowed!
        if(pdl != null && !pdl.isDone()) {
            logger.error("Two Threads are about to load at the same time.");
            return;
        }
        pdl = new PropertyDataLoader();
        pdl.execute();
    }

    /**
     * @return the property that corresponds to the selected name in the
     *         size-combobox. null in case "none" is selected in the box.
     */
    public PropertyDefinition getSizeProperty() {
        return numProperties.get(state.getSizeProperty());
    }

    /**
     * @return the property that corresponds to the selected name in the
     *         color-combobox. null in case "none" is selected in the box.
     */
    public PropertyDefinition getColorProperty() {
        return numProperties.get(state.getColorProperty());
    }
}
