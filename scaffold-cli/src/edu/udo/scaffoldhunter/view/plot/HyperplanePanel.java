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
import java.awt.GridLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

import edu.udo.scaffoldhunter.util.I18n;


/**
 * @author Michael Hesse
 *
 */
public class HyperplanePanel extends JPanel {

    class Hyperplane extends JPanel implements ChangeListener, PropertyChangeListener, ItemListener  {

        Box box;
        RangeSlider slider;
        JFormattedTextField minTF, maxTF;
        
        double minLimit, maxLimit, minValue, maxValue;
        boolean isAdjusting;
        JCheckBox applyToAxis;
        
        
        
        public Hyperplane(String title) {
            
            // layout
            setOpaque(false);
            setLayout(new GridLayout(1,1));
            setBorder( BorderFactory.createEmptyBorder(5, 0, 10, 0));
            box = Box.createHorizontalBox();
            box.setOpaque(false);
            JPanel p = new JPanel();
            p.setOpaque(false);
            p.setLayout( new BorderLayout() );
            JPanel w = new JPanel( new GridLayout(2,1) );
            w.add( new JLabel(title) );
            w.setOpaque(false);
            applyToAxis = new JCheckBox();
            applyToAxis.setSelected(false);
            applyToAxis.setText("");
            applyToAxis.setOpaque(false);
            applyToAxis.setBorder( BorderFactory.createEmptyBorder() );
            w.add(applyToAxis);
            p.add( w, BorderLayout.WEST);
            slider = new RangeSlider();
            slider.setMinimum(10);
            slider.setMaximum(20);
            slider.setOpaque(false);
            Box pp = Box.createVerticalBox();
            pp.setOpaque(false);
            pp.add(slider);
            Box ppp = Box.createHorizontalBox();
            ppp.setOpaque(false);
            minTF = new JFormattedTextField( new NumberFormatter() );
            maxTF = new JFormattedTextField( new NumberFormatter() );
            ppp.add( Box.createHorizontalStrut(5));
            ppp.add( minTF);
            ppp.add( Box.createHorizontalGlue() );
            ppp.add( new JLabel(" - "));
            ppp.add( Box.createHorizontalGlue() );
            ppp.add( maxTF );
            ppp.add( Box.createHorizontalStrut(5));
            pp.add(ppp);
            p.add(pp, BorderLayout.CENTER);
            box.add( p );
            add(box);
            
            // init values
            isAdjusting = true;
            setLimits( 1000.0, 3000.0);
            isAdjusting = false;
            
            // set additional parameters
            slider.setMajorTickSpacing(10);
            slider.setMinorTickSpacing(1);
            minTF.setActionCommand("value in TF changed");
            maxTF.setActionCommand("value in TF changed");
            
            
            // wire things
            slider.addChangeListener(this);
            minTF.addPropertyChangeListener(this);
            maxTF.addPropertyChangeListener(this);
            applyToAxis.addItemListener(this);
        }

        
        /**
         * @return true if the bounds should be applied to the axis
         */
        public boolean applyToAxis() {
            return applyToAxis.isSelected();
        }
        
        /**
         * @return the current limit
         */
        public double getMinLimit() {
            return minLimit;
        }

        /**
         * @return the current limit
         */
        public double getMaxLimit() {
            return maxLimit;
        }

        /**
         * @return the current value
         */
        public double getMinValue() {
            return minValue;
        }

        /**
         * @return the current value
         */
        public double getMaxValue() {
            return maxValue;
        }

        /**
         * 
         * @param minLimit
         * @param maxLimit
         */
        public void setLimits(double minLimit, double maxLimit) {
            this.minLimit = minLimit;
            this.maxLimit = maxLimit;
            minValue = minLimit;
            maxValue = maxLimit;
            slider.setMinimum(0);
            slider.setMaximum(100);
            slider.setValue(0);
            slider.setUpperValue(100);
            minTF.setValue( minLimit );
            maxTF.setValue( maxLimit );
            minTF.setCaretPosition(0);
            maxTF.setCaretPosition(0);
        }


        /* (non-Javadoc)
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        @Override
        public void stateChanged(ChangeEvent event) {
            if( event.getSource() == slider ) {
                // slider changed
                if( ! isAdjusting ) {
                    isAdjusting = true;
                    double delta = getMaxLimit() - getMinLimit();
                    double sliderMin = ((double) slider.getValue()) / 100;
                    double sliderMax = ((double) slider.getUpperValue()) / 100;
                    double newMinValue = getMinLimit() + delta*sliderMin;
                    double newMaxValue = getMinLimit() + delta*sliderMax;
                    minTF.setValue( newMinValue );
                    maxTF.setValue( newMaxValue );
                    minTF.setCaretPosition(0);
                    maxTF.setCaretPosition(0);
                    minValue = newMinValue;
                    maxValue = newMaxValue;
                    isAdjusting = false;
                    hyperplaneChanged(this);
                }
            }
        }



        /* (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if( (event.getSource() == minTF) | (event.getSource() == maxTF) ) {
                if( event.getPropertyName().equals("value")) {
                    if( ! isAdjusting ) {
                        // value in textfield changed
                        isAdjusting = true;
        
                        //double newMinValue = Double.parseDouble( minTF.getText() );
                        //double newMaxValue = Double.parseDouble( maxTF.getText() );
                        Double newMinValue = new Double(getMinLimit());
                        Double newMaxValue = new Double(getMaxLimit());
                        boolean valid = true;

                        if( minTF.getValue() instanceof Double)
                            newMinValue = (Double)minTF.getValue();
                        else if( minTF.getValue() instanceof Integer)
                            newMinValue = new Double((Integer)minTF.getValue());
                        else if( minTF.getValue() instanceof Long)
                            newMinValue = new Double((Long)minTF.getValue());
                        else
                            valid = false;

                        if( maxTF.getValue() instanceof Double)
                            newMaxValue = (Double)maxTF.getValue();
                        else if( maxTF.getValue() instanceof Integer)
                            newMaxValue = new Double((Integer)maxTF.getValue());
                        else if( maxTF.getValue() instanceof Long)
                            newMaxValue = new Double((Long)maxTF.getValue());
                        else
                            valid = false;
                            
                        if( (newMinValue < (long)getMinLimit())
                                | (newMaxValue > (long)getMaxLimit())
                                | (newMinValue > newMaxValue)
                                | (! valid) ) {
                            // revert
                            minTF.setValue( minValue );
                            maxTF.setValue( maxValue );
                        } else {
                            double delta = getMaxLimit() - getMinLimit();
                            double tick = delta/100.0;
                            double newSliderValue = (newMinValue-getMinLimit()) / tick;
                            double newSliderUpperValue = (newMaxValue-getMinLimit()) / tick;
                            slider.setValue((int)newSliderValue);
                            slider.setUpperValue((int)newSliderUpperValue);
                            minValue = newMinValue;
                            maxValue = newMaxValue;
                            hyperplaneChanged(this);
                        }
                        
                        minTF.setCaretPosition(0);
                        maxTF.setCaretPosition(0);
                        isAdjusting = false;
                    }
                }
            }
            
        }
    
        
        @Override
        public void setEnabled(boolean isEnabled) {
            super.setEnabled(isEnabled);
            slider.setEnabled(isEnabled);
            minTF.setEnabled(isEnabled);
            maxTF.setEnabled(isEnabled);
            applyToAxis.setEnabled(isEnabled);
        }


        /* (non-Javadoc)
         * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
         */
        @Override
        public void itemStateChanged(ItemEvent arg0) {
            hyperplaneChanged(this);
        }
    }
    
   
    
    /**
     * 
     */

    Box box;
    Hyperplane xHp, yHp, zHp, colorHp, sizeHp;
    Model model = null;
    
    
    /**
     * 
     */
    public HyperplanePanel() {
        super();
        setOpaque(false);
        setLayout( new BorderLayout() );
        setBorder( BorderFactory.createEmptyBorder(0, 10, 0, 5) );
        box = Box.createVerticalBox();
        
        // x hyperplane
        xHp = new Hyperplane(I18n.get("PlotView.Hyperplane.XAxisShortcut"));
        xHp.setEnabled(false);
        box.add( xHp );
        // y hyperplane
        yHp = new Hyperplane(I18n.get("PlotView.Hyperplane.YAxisShortcut"));
        yHp.setEnabled(false);
        box.add( yHp );
        // z hyperplane
        zHp = new Hyperplane(I18n.get("PlotView.Hyperplane.ZAxisShortcut"));
        zHp.setEnabled(false);
        box.add( zHp );
        // color hyperplane
        colorHp = new Hyperplane(I18n.get("PlotView.Hyperplane.ColorAxisShortcut"));
        colorHp.setEnabled(false);
        box.add( colorHp );
        // size hyperplane
        sizeHp = new Hyperplane(I18n.get("PlotView.Hyperplane.SizeAxisShortcut"));
        sizeHp.setEnabled(false);
        box.add( sizeHp );
        
        add(box, BorderLayout.NORTH);
    }
    
    
    /**
     * @param channel
     * @return true if there is a bound
     */
    public boolean hasMinHpBound(int channel) {
        return ( getHpByChannel(channel).slider.getValue() != 0 );
    }

    /**
     * @param channel
     * @return true if there is a bound
     */
    public boolean hasMaxHpBound(int channel) {
        return ( getHpByChannel(channel).slider.getUpperValue() != 100 );
    }
    
    /**
     * @param channel
     * @param isEnabled
     */
    public void setEnabled(int channel, boolean isEnabled) {
        getHpByChannel(channel).setEnabled(isEnabled);
    }

    /**
     * @param channel
     * @param minLimit
     * @param maxLimit
     */
    public void setLimits(int channel, double minLimit, double maxLimit) {
        getHpByChannel(channel).setLimits(minLimit, maxLimit);
    }
    
    /**
     * @param channel
     * @return the min-value which the user currently has set
     */
    public double getMinValue(int channel) {
        return getHpByChannel(channel).getMinValue();
    }

    /**
     * @param channel
     * @return the max-value which the user currently has set
     */
    public double getMaxValue(int channel) {
        return getHpByChannel(channel).getMaxValue();
    }

    /**
     * 
     * @param channel
     * @return true if the bounds should be applied to the axis
     */
    public boolean applyToAxis(int channel) {
        return getHpByChannel(channel).applyToAxis();
    }
    
    private Hyperplane getHpByChannel( int channel ) {
        Hyperplane hp = null;
        switch(channel) {
        case PlotPanel3D.X_CHANNEL:
            hp = xHp;
            break;
        case PlotPanel3D.Y_CHANNEL:
            hp = yHp;
            break;
        case PlotPanel3D.Z_CHANNEL:
            hp = zHp;
            break;
        case PlotPanel3D.COLOR_CHANNEL:
            hp = colorHp;
            break;
        case PlotPanel3D.SIZE_CHANNEL:
        default:
            hp = sizeHp;
            break;
        }
        return hp;
    }
    
    /**
     * @param model
     */
    public void setModel(Model model) {
        this.model = model;
    }

    /**
     * to inform the model that something changed
     * 
     * @param hyperplane
     */
    void hyperplaneChanged(Hyperplane hyperplane) {
        if( model != null) {
            if( hyperplane == xHp )
                model.fireModelChange(PlotPanel3D.X_CHANNEL, false);
            else if( hyperplane == yHp )
                model.fireModelChange(PlotPanel3D.Y_CHANNEL, false);
            else if( hyperplane == zHp )
                model.fireModelChange(PlotPanel3D.Z_CHANNEL, false);
            else if( hyperplane == colorHp )
                model.fireModelChange(PlotPanel3D.COLOR_CHANNEL, false);
            else if( hyperplane == sizeHp )
                model.fireModelChange(PlotPanel3D.SIZE_CHANNEL, false);
        }
    }
    
    
    /**
     * saves its state to the PlotViewState instance
     * 
     * @param pvs
     */
    public void saveState(PlotViewState pvs) {
        pvs.setHpXmin( xHp.getMinValue() );
        pvs.setHpXmax( xHp.getMaxValue() );
        pvs.setApplyHpXtoAxis( xHp.applyToAxis() );
        pvs.setHpYmin( yHp.getMinValue() );
        pvs.setHpYmax( yHp.getMaxValue() );
        pvs.setApplyHpYtoAxis( yHp.applyToAxis() );
        pvs.setHpZmin( zHp.getMinValue() );
        pvs.setHpZmax( zHp.getMaxValue() );
        pvs.setApplyHpZtoAxis( zHp.applyToAxis() );
        pvs.setHpColormin( colorHp.getMinValue() );
        pvs.setHpColormax( colorHp.getMaxValue() );
        pvs.setApplyHpColortoAxis( colorHp.applyToAxis() );
        pvs.setHpSizemin( sizeHp.getMinValue() );
        pvs.setHpSizemax( sizeHp.getMaxValue() );
        pvs.setApplyHpSizetoAxis( sizeHp.applyToAxis() );
    }

    /**
     * loads its state from a PlotViewState instance
     * 
     * @param pvs
     */
    public void loadState(PlotViewState pvs) {
        xHp.setLimits( pvs.getHpXmin(), pvs.getHpXmax() );
        xHp.applyToAxis.setSelected( pvs.applyHpXtoAxis() );
        yHp.setLimits( pvs.getHpYmin(), pvs.getHpYmax() );
        yHp.applyToAxis.setSelected( pvs.applyHpYtoAxis() );
        zHp.setLimits( pvs.getHpZmin(), pvs.getHpZmax() );
        zHp.applyToAxis.setSelected( pvs.applyHpZtoAxis() );
        colorHp.setLimits( pvs.getHpColormin(), pvs.getHpColormax() );
        colorHp.applyToAxis.setSelected( pvs.applyHpColortoAxis() );
        sizeHp.setLimits( pvs.getHpSizemin(), pvs.getHpSizemax() );
        sizeHp.applyToAxis.setSelected( pvs.applyHpSizetoAxis() );
    }
}
