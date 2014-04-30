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

package edu.udo.scaffoldhunter.view.plot;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.VoidNullaryDBFunction;
import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Banner;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.view.util.ToolboxComboBoxRenderer;

/**
 * @author Michael Hesse
 *
 */
public class DbModel extends JPanel implements Model, ActionListener, ChangeListener, PropertyChangeListener, BannerChangeListener {

    private BannerPool bannerPool = null;

    Subset subset;
    JComboBox<String>[] axisMappingComboBox = new JComboBox[5];
    int[] selectedMappings = new int[5]; // stores the selected mappings in an array for performance improvements
    
    // the property definitions that are available in the current subset
    List <PropertyDefinition> numPropertyDefinitions;
    
    // all molecules of the subset in an invariable order
    List <Molecule> molecules;
    
    // cached properties
    Map <PropertyDefinition, List <Double> > propertyValues;
    
    // caches; a value is invalid when it's set to Double.NaN
    double[] minValue;
    double[] maxValue;
    
    // jittervectors for each dot, will be filled with random values when
    // selecting a new channel into x, y, or z
    List <Double> jitterX = new ArrayList <Double> ();
    List <Double> jitterY = new ArrayList <Double> ();
    List <Double> jitterZ = new ArrayList <Double> ();
    JSlider jitterSlider;
    
    // hyperplanes
    HyperplanePanel hyperplanePanel = null;
    
    // listeners
    List <ModelChangeListener> modelChangeListenerList = new ArrayList <ModelChangeListener> ();
    
    DbManager db;
    Selection selection;
    
    
    /**
     * @param db
     * @param selection 
     */
    public DbModel(DbManager db, Selection selection) {
        
        // layout this panel
        setLayout( new BorderLayout() );
        JPanel vBox1 = new JPanel( new GridLayout(5+1,1));
        vBox1.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 5) );
        JPanel vBox2 = new JPanel( new GridLayout(5+1,1));
        vBox2.setBorder( BorderFactory.createEmptyBorder(10, 5, 10, 10) );
        JLabel[] label = new JLabel[axisMappingComboBox.length];
        ToolboxComboBoxRenderer tcbr = new ToolboxComboBoxRenderer();
        for(int i=0; i<label.length; i++) {
            label[i] = new JLabel();
            axisMappingComboBox[i] = new JComboBox();
            axisMappingComboBox[i].setRenderer(tcbr);
            axisMappingComboBox[i].addActionListener(this);
            vBox1.add(label[i]);
            vBox2.add(axisMappingComboBox[i]);
            selectedMappings[i] = axisMappingComboBox[i].getSelectedIndex();
        }
        
        label[0].setText(I18n.get("PlotView.Mappings.XAxis"));
        label[1].setText(I18n.get("PlotView.Mappings.YAxis"));
        label[2].setText(I18n.get("PlotView.Mappings.ZAxis"));
        label[3].setText(I18n.get("PlotView.Color"));
        label[4].setText(I18n.get("PlotView.Size"));
        { // jitter
            vBox1.add( new JLabel(I18n.get("PlotView.Mappings.Jitter")));
            jitterSlider = new JSlider(0, 50, 0);
            jitterSlider.setOpaque(false);
            jitterSlider.addChangeListener(this);
            vBox2.add(jitterSlider);
        }
        
        add(vBox1, BorderLayout.WEST);
        add(vBox2, BorderLayout.CENTER);
        this.setMaximumSize( new Dimension( 100000, vBox2.getHeight()) );
        setOpaque(false);
        vBox1.setOpaque(false);
        vBox2.setOpaque(false);
        
        // initialize
        this.db = db;
        this.selection = selection;
        molecules = new ArrayList <Molecule> ();
        numPropertyDefinitions = new ArrayList <PropertyDefinition> ();
        propertyValues = new HashMap <PropertyDefinition, List <Double> > ();
        setSubset(null, null);
    }
    
    /**
     * sets the subset that this model should serve
     * @param subset
     * @param bp 
     */
    public void setSubset(Subset subset, BannerPool bp) {
        
        // dealing with the banner pool
        if(bannerPool != null)
            bannerPool.removeBannerChangeListener(this);
        bannerPool = bp;
        if(bannerPool != null)
            bannerPool.addBannerChangeListener(this);
        
        
        // store selected mappings (to restore them later)
        int[] selectedMappingsLocale = new int[axisMappingComboBox.length];
        for(int i=0; i<axisMappingComboBox.length; i++) {
            selectedMappingsLocale[i] = axisMappingComboBox[i].getSelectedIndex();
        }
        
        this.subset = subset;
        
        // clear comboboxes
        for(int i=0; i<axisMappingComboBox.length; i++) {
            axisMappingComboBox[i].removeAllItems();
            axisMappingComboBox[i].addItem(I18n.get("PlotView.Mappings.None"));
        }
        
        // release all currently locked property definitions
        propertyValues.clear();
        numPropertyDefinitions.clear();

        // reset Hyperplanes
        if(hyperplanePanel != null) {
            hyperplanePanel.setLimits(PlotPanel3D.X_CHANNEL, 0.0, 1.0);
            hyperplanePanel.setLimits(PlotPanel3D.Y_CHANNEL, 0.0, 1.0);
            hyperplanePanel.setLimits(PlotPanel3D.Z_CHANNEL, 0.0, 1.0);
            hyperplanePanel.setLimits(PlotPanel3D.COLOR_CHANNEL, 0.0, 1.0);
            hyperplanePanel.setLimits(PlotPanel3D.SIZE_CHANNEL, 0.0, 1.0);
            hyperplanePanel.setEnabled(PlotPanel3D.X_CHANNEL, false);
            hyperplanePanel.setEnabled(PlotPanel3D.Y_CHANNEL, false);
            hyperplanePanel.setEnabled(PlotPanel3D.Z_CHANNEL, false);
            hyperplanePanel.setEnabled(PlotPanel3D.COLOR_CHANNEL, false);
            hyperplanePanel.setEnabled(PlotPanel3D.SIZE_CHANNEL, false);
        }
        
        // get new molecules
        molecules.clear();
        if(subset != null)
            for( Molecule m : subset.getMolecules() )
                molecules.add(m);
        
        // detect and store new num properties
        if (subset != null) {
            List <PropertyDefinition> propertyDefList = new ArrayList <PropertyDefinition> ();
            propertyDefList.addAll( subset.getSession().getDataset().getPropertyDefinitions().values() );
            Collections.sort(propertyDefList,
                    new Comparator <PropertyDefinition> () {
                        @Override
                        public int compare(PropertyDefinition a, PropertyDefinition b) {
                            return a.getTitle().compareTo( b.getTitle() );
                        }
            } );
            
            for (PropertyDefinition def : propertyDefList) {
                if (!def.isScaffoldProperty()) {
                    if (!def.isStringProperty()) {
                        // store this num property
                        numPropertyDefinitions.add(def);
                        propertyValues.put(def, new ArrayList <Double> ());
                        // and show it in the comboboxes
                        for (int i = 0; i < axisMappingComboBox.length; i++) {
                            axisMappingComboBox[i].addItem(def.getTitle());
                        }
                    }
                }
            }
        }
        
        // invalidate all cached min- and max-values
        minValue = new double[ getNumberOfChannels() ];
        maxValue = new double[ getNumberOfChannels() ];
        for(int i=0; i<getNumberOfChannels(); i++) {
            minValue[i] = Double.NaN;
            maxValue[i] = Double.NaN;
        }

        // create new Jitter vectors
        jitterX.clear();
        jitterY.clear();
        jitterZ.clear();
        for(int i=0; i<getDataLength(); i++) {
            jitterX.add( (Math.random() * 2 - 1.0)/250.0 );
            jitterY.add( (Math.random() * 2 - 1.0)/250.0 );
            jitterZ.add( (Math.random() * 2 - 1.0)/250.0 );
        }

        for(int i=0; i<axisMappingComboBox.length; i++) {
            if(selectedMappingsLocale[i] != -1)
                axisMappingComboBox[i].setSelectedIndex(selectedMappingsLocale[i]);
        }
        
        // fire change event
        for( int i=1; i<axisMappingComboBox.length; i++)
            fireModelChange(i, true);
        fireModelChange(0, false);
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#getTitle()
     * 
     * returns the title of the current subset
     */
    @Override
    public String getTitle() {
        if(subset != null)
            return subset.getTitle();
        else
            return "";
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#getNumberOfChannels()
     * 
     * returns the number of available channels (aka numProperties in the current subset)
     */
    @Override
    public int getNumberOfChannels() {
        return numPropertyDefinitions.size();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#getChannelTitle(int)
     * 
     * returns the title of the channel (aka the key of the numProperty)
     */
    @Override
    public String getChannelTitle(int channel) {
        PropertyDefinition pd = this.getPropertyDefinitionForLogicalChannel(channel);
        if(pd == null)
            return "";
        else
            return pd.getTitle();
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#hasData(int)
     * 
     * returns true if the specified channel is mapped to a property
     */
    @Override
    public boolean hasData(int channel) {
        return ( getPropertyDefinitionForLogicalChannel(channel) != null );
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#getData(int, int)
     * 
     * returns the specified value, or Double.NaN if the value
     * does not exist or if the property is not loaded
     */
    @Override
    public double getData(int channel, int index) {
        Double value = Double.NaN;
        PropertyDefinition pd = getPropertyDefinitionForLogicalChannel(channel);
        if( (pd != null) & (index>=0) & (index<=getDataLength()) ) {
            List <Double> values = propertyValues.get(pd);
            if( ! values.isEmpty() ) {
                double v = values.get(index);
                if(hyperplanePanel != null) {
                    if( (v >= hyperplanePanel.getMinValue(channel))
                            & (v <= hyperplanePanel.getMaxValue(channel))) {
                        value = v;
                    }
                } else {
                    value = v;
                }

                if(jitterSlider.getValue() != 0) {
                    double max = getDataMax(channel);
                    double min = getDataMin(channel);
                    switch( channel ) {
                    case PlotPanel3D.X_CHANNEL:
                        value += jitterSlider.getValue() * jitterX.get(index) * (max-min);
                        break;
                    case PlotPanel3D.Y_CHANNEL:
                        value += jitterSlider.getValue() * jitterY.get(index) * (max-min);
                        break;
                    case PlotPanel3D.Z_CHANNEL:
                        value += jitterSlider.getValue() * jitterZ.get(index) * (max-min);
                        break;
                    }
                    if(value < min) value = min;
                    else if (value > max) value = max;
                }

            }
        }
        return value;
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#getDataMin(int)
     * 
     * returns the minimum of the data on this channel
     *  - returns Double.NaN if the channel has no data
     *  - returns the current minimum if lazy loading of the data for
     *    this channel is still in progress
     */
    @Override
    public double getDataMin(int channel) {
        int physicalChannel = convertLogicalToPhysicalChannel(channel);
        if( (physicalChannel<0) | (physicalChannel>minValue.length) )
            return Double.NaN;

        double min = minValue[physicalChannel];
        if( maxValue[physicalChannel] == min ) {
            if(min == 0) {
                min -= 0.000005;
            } else {
                min *= 0.999;
            }
        }

        double value = min;
        if(hyperplanePanel != null)
            if( hyperplanePanel.applyToAxis(channel) ) {
                value = hyperplanePanel.getMinValue(channel);
            }

        return value;
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#getDataMax(int)
     * 
     * returns the maximum of the data on this channel
     *  - returns Double.NaN if the channel has no data
     *  - returns the current maximum if lazy loading of the data for
     *    this channel is still in progress
     */
    @Override
    public double getDataMax(int channel) {
        int physicalChannel = convertLogicalToPhysicalChannel(channel);
        if( (physicalChannel<0) | (physicalChannel>maxValue.length) )
            return Double.NaN;

        double max = maxValue[physicalChannel];
        if( minValue[physicalChannel] == max ) {
            if(max == 0) {
                max += 0.000005;
            } else {
                max *= 0.999;
            }
        }

        double value = max;
        if(hyperplanePanel != null)
            if( hyperplanePanel.applyToAxis(channel) ) {
                value = hyperplanePanel.getMaxValue(channel);
            }
        return value;
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#getDataLength()
     * 
     * returns the number of elements
     */
    @Override
    public int getDataLength() {
        return molecules.size();
    }
    
    
    /**
     * 
     * @param channel
     *  the channel, for which the property definition should be
     *  detected (with respect to the mapping)
     * @return
     *  the property definition that is currently mapped to this channel
     */
    private PropertyDefinition getPropertyDefinitionForLogicalChannel(int channel) {
        int physicalChannel = convertLogicalToPhysicalChannel(channel);
        if( (physicalChannel >= 0) & (physicalChannel < numPropertyDefinitions.size()) )
            return numPropertyDefinitions.get(physicalChannel);
        return null;
    }
    
    /**
     * 
     * @param channel
     *  the logical channel
     * @return
     *  the physical channel
     */
    private int convertLogicalToPhysicalChannel(int channel) {
        int physicalChannel = -1;
        if( (channel>=0) & (channel<axisMappingComboBox.length) ) {
            int selectedIndex = selectedMappings[channel];
            physicalChannel = selectedIndex-1;
        }
        return physicalChannel;
    }
    

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#addModelChangeListener(edu.udo.scaffoldhunter.view.dendrogram.ModelChangeListener)
     */
    @Override
    public void addModelChangeListener(ModelChangeListener modelChangeListener) {
        if( modelChangeListener == null )
            return;
        if( ! modelChangeListenerList.contains( modelChangeListener) ) {
            modelChangeListenerList.add( modelChangeListener );
        }        
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#removeModelChangeListener(edu.udo.scaffoldhunter.view.dendrogram.ModelChangeListener)
     */
    @Override
    public void removeModelChangeListener(ModelChangeListener modelChangeListener) {
        if( modelChangeListener == null )
            return;
        if( modelChangeListenerList.contains( modelChangeListener) ) {
            modelChangeListenerList.remove( modelChangeListener );
        }
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#fireModelChange(int, boolean)
     */
    @Override
    public void fireModelChange(int channel, boolean moreToCome) {
        for( ModelChangeListener listener : modelChangeListenerList ) {
            listener.modelChanged( this, channel, moreToCome );
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     * 
     * will be called when a selection in a combobox was made
     * 
     */
    @Override
    public void actionPerformed(ActionEvent event) {
        int channel = -1;
        
        // get the channel that has changed
        for(int i=0; i<axisMappingComboBox.length; i++)
            if(event.getSource() == axisMappingComboBox[i] ) {
                channel = i;
                break;
            }
        
        selectedMappings[channel] = axisMappingComboBox[channel].getSelectedIndex();

        // invalidate dereferenced data
        for(int i=0; i<numPropertyDefinitions.size(); i++) {
            boolean isReferenced = false;
            for(int j=0; j<axisMappingComboBox.length; j++) {
                if(axisMappingComboBox[j].getSelectedIndex() == i+1) {
                    isReferenced = true;
                    break;
                }
            }
            if( ! isReferenced) {
                minValue[i] = Double.NaN;
                maxValue[i] = Double.NaN;
                propertyValues.put(numPropertyDefinitions.get(i), new ArrayList <Double> ());
            }
        }

        PropertyDefinition pd = this.getPropertyDefinitionForLogicalChannel(channel);
        if(pd != null) {
            // load new data
            propertyValues.put(pd, new ArrayList <Double> ());
            loadData(pd, channel);
        } else {
            // disable hyperplanes
            if(hyperplanePanel != null) {
                hyperplanePanel.setLimits(channel, 0.0, 1.0);
                hyperplanePanel.setEnabled(channel, false);
            }
        }
        
        // fire change event
        fireModelChange(channel, false);
    }

    
    /**
     * loads the values of the specified property definition
     * @param prop
     */
    void loadData(PropertyDefinition prop, int channel) {

        new Thread () {
            
            PropertyDefinition pd;
            int channel;
            
            public void kickstart(PropertyDefinition pd, int channel) {
                this.pd = pd;
                this.channel = channel;
                start();
            }

            
            @Override
            public void run() {
                List <Double> values = new ArrayList <Double> ();
                List <Molecule> m = new ArrayList <Molecule> ();
                List <Molecule> oldMoleculeList = molecules;

                // make a local copy of the list
                for(int i=0; i<molecules.size(); i++) {
                    m.add(molecules.get(i));
                }
                
                // load the stuff
                List <PropertyDefinition> propDefs = new ArrayList <PropertyDefinition> ();
                propDefs.add(pd);
                
                DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction(){
                    
                    private List<PropertyDefinition> propDefs;
                    private List <Molecule> m;
                    
                    public VoidNullaryDBFunction init(List<PropertyDefinition> propDefs, List <Molecule> m) {
                        this.propDefs = propDefs;
                        this.m = m;
                        return this;
                    }
                    
                    @Override
                    public void voidCall() throws DatabaseException {
                        db.lockAndLoad(propDefs, m);
                    }
                }.init(propDefs, m));

                // has something changed in the meantime?
                if( oldMoleculeList == molecules ) {
                    // no, so copy values into our local list
                    double min = Double.NaN;
                    double max = Double.NaN;
                    for(int i=0; i<m.size(); i++) {
                        if( m.get(i).getNumPropertyValue(pd) == null) {
                            values.add(i, Double.NaN);
                        } else {
                            if( Double.isNaN(m.get(i).getNumPropertyValue(pd))) {
                                values.add(i, Double.NaN);
                            }
                            else {
                                double v = m.get(i).getNumPropertyValue(pd);
                                values.add(i, v);
                                if ( (m.get(i).getNumPropertyValue(pd) < min) | Double.isNaN(min) )
                                    min = m.get(i).getNumPropertyValue(pd);
                                if ( (m.get(i).getNumPropertyValue(pd) > max) | Double.isNaN(max) )
                                    max = m.get(i).getNumPropertyValue(pd);
                            }
                        }
                    }
                    propertyValues.put(pd, values);
                    minValue[convertLogicalToPhysicalChannel(channel)] = min;
                    maxValue[convertLogicalToPhysicalChannel(channel)] = max;
                    
                    // unlock the data
                    db.unlockAndUnload(propDefs, m);
                    
                    // set hyperplanes
                    if(hyperplanePanel != null) {
                        double myMin = getDataMin(channel);
                        double myMax = getDataMax(channel);
                        hyperplanePanel.setLimits(channel, myMin, myMax);
                        hyperplanePanel.setEnabled(channel, true);
                    }
                    
                    // fire a change event
                    fireModelChange(channel, false);

                }
            }
        }.kickstart(prop, channel);
    }


    /* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    @Override
    public void stateChanged(ChangeEvent e) {

        // jitter?
        if(e.getSource() == jitterSlider) {
            fireModelChange( PlotPanel3D.X_CHANNEL, true);
            fireModelChange( PlotPanel3D.Y_CHANNEL, true);
            fireModelChange( PlotPanel3D.Z_CHANNEL, false);
        }
    }
    
    
    @Override
    public boolean isSelected(int index) {
        return selection.contains( molecules.get(index));
    }

    /**
     * @param index
     * @param isSelected
     */
    @Override
    public void setSelected(int index, boolean isSelected) {
        Molecule m = molecules.get(index);
        if( isSelected ) {
            if( ! selection.contains(m) )
                selection.add(m);
        } else {
            if( selection.contains(m) )
                selection.remove(m);
        }
    }

    /**
     * for bulk selecting
     * 
     * @param index
     * @param isSelected
     */
    @Override
    public void setSelected(List <Integer> index, boolean isSelected) {
        List <Molecule> selectedMolecules = new ArrayList<Molecule> ();
        for(int i : index)
            selectedMolecules.add( molecules.get(i) );
        if( isSelected )
            selection.addAll(selectedMolecules);
        else
            selection.removeAll(selectedMolecules);
    }
    
    /**
     * 
     * @param index
     * @return
     *  true, if a public banner for this molecule is set
     */
    @Override
    public boolean hasPublicBanner(int index) {
        Molecule molecule = molecules.get(index);
        if(molecule == null)
            return false;
        return ( bannerPool.hasBanner(molecule, false) );
    }

    /**
     * 
     * @param index
     */
    @Override
    public void togglePublicBanner(int index) {
        Molecule molecule = molecules.get(index);
        if(molecule == null)
            return;
        if( bannerPool.hasBanner(molecule, false) )
            bannerPool.removeBanner(molecule, false);
        else
            bannerPool.addBanner(molecule, false);
        // just cause a repaint
        //fireModelChange(0, false);
    }
    
    /**
     * 
     * @param index
     * @return
     *  true, if a private banner for this molecule is set
     */
    @Override
    public boolean hasPrivateBanner(int index) {
        Molecule molecule = molecules.get(index);
        if(molecule == null)
            return false;
        return ( bannerPool.hasBanner(molecule, true) );
    }

    /**
     * 
     * @param index
     */
    @Override
    public void togglePrivateBanner(int index) {
        Molecule molecule = molecules.get(index);
        if(molecule == null)
            return;
        if( bannerPool.hasBanner(molecule, true) )
            bannerPool.removeBanner(molecule, true);
        else 
            bannerPool.addBanner(molecule, true);
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(Selection.SELECTION_PROPERTY)) {
            // just cause a repaint
            fireModelChange(0, false);
        }
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#getMolecule(int)
     */
    @Override
    public Molecule getMolecule(int index) {
        if( subset == null )
            return null;
        if(molecules.isEmpty())
            return null;
        if( (index < 0) | (index >= molecules.size()) )
            return null;
        return molecules.get(index);
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.plot.Model#setHyperplanePanel(edu.udo.scaffoldhunter.view.plot.HyperplanePanel)
     */
    @Override
    public void setHyperplanePanel(HyperplanePanel hyperplanePanel) {
        this.hyperplanePanel = hyperplanePanel;
    }
    
    @Override
    public HyperplanePanel getHyperplanePanel() {
        return hyperplanePanel;
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener#bannerAdded(edu.udo.scaffoldhunter.model.db.Banner)
     */
    @Override
    public void bannerAdded(Banner banner) {
        // just cause a repaint
        fireModelChange(0, false);
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener#bannerRemoved(edu.udo.scaffoldhunter.model.db.Banner)
     */
    @Override
    public void bannerRemoved(Banner banner) {
        // just cause a repaint
        fireModelChange(0, false);
    }

    /**
     * saves the current state to the plotViewState object
     * @param pvs 
     */
    public void saveState(PlotViewState pvs) {
        if(pvs.isApplied()) {
            pvs.setXMappingIndex( axisMappingComboBox[0].getSelectedIndex() );
            pvs.setYMappingIndex( axisMappingComboBox[1].getSelectedIndex() );
            pvs.setZMappingIndex( axisMappingComboBox[2].getSelectedIndex() );
            pvs.setColorMappingIndex( axisMappingComboBox[3].getSelectedIndex() );
            pvs.setSizeMappingIndex( axisMappingComboBox[4].getSelectedIndex() );
            pvs.setJitter( jitterSlider.getValue() );
            //hyperplanePanel.saveState(pvs);
        }
    }
    
    /**
     * loads the current state from the plotViewState object
     * @param pvs
     */
    public void loadState(PlotViewState pvs) {
        axisMappingComboBox[0].setSelectedIndex( pvs.getXMappingIndex() );
        axisMappingComboBox[1].setSelectedIndex( pvs.getYMappingIndex() );
        axisMappingComboBox[2].setSelectedIndex( pvs.getZMappingIndex() );
        axisMappingComboBox[3].setSelectedIndex( pvs.getColorMappingIndex() );
        axisMappingComboBox[4].setSelectedIndex( pvs.getSizeMappingIndex() );
        jitterSlider.setValue( pvs.getJitter() );
        //hyperplanePanel.loadState(pvs);
    }
}

