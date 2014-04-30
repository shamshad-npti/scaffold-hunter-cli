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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.ColorEditor;
import edu.udo.scaffoldhunter.util.DefaultColors;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.scaffoldtree.config.ConfigMapping.Interval;

/**
 * A panel which is used to configure the intervals of an interval mapping.
 * 
 * @see SinglePropertyPanel
 * @see ConfigMapping
 * 
 * @author Henning Garus
 */
public class IntervalPanel extends JPanel {

    private static final Logger logger = LoggerFactory.getLogger(IntervalPanel.class);

    private static final Icon plusIcon = Resources.getIcon("plus.png");
    private static final Icon deleteIcon = Resources.getIcon("minus.png");
    private static final Icon distributeIcon = Resources.getIcon("distribute-intervals.png");
    
    private final JScrollPane scrollPane;
    private final JPanel intervalsPanel;
    
    private final JButton distributeIntervalsButton;
    private final JPanel distributeIntervalsPanel;

    private final List<Interval> intervals;
    private final boolean colors;
    
    private boolean numIntervals;
    private List<String> distinctStringValues;
    
    private double minimum = Double.NEGATIVE_INFINITY;
    private double maximum = Double.POSITIVE_INFINITY;

    private SpinnerNumberModel maximumModel;

    /**
     * Create a new IntervalPanel.
     * 
     * @param colors
     *            select if the mapping should provide a color selector for each
     *            interval
     * @param mapping
     *            the mapping whose intervals are edited using this panel
     */
    public IntervalPanel(boolean colors, ConfigMapping mapping) {
        super(new BorderLayout());
        
        numIntervals = mapping.getDistinctStringValues() == null;
        distinctStringValues = mapping.getDistinctStringValues();
        minimum = mapping.getMinimumPropertyValue();
        maximum = mapping.getMaximumPropertyValue();
        
        intervalsPanel = new JPanel();
        intervalsPanel.setLayout(new BoxLayout(intervalsPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(intervalsPanel);
        add(scrollPane, BorderLayout.CENTER);
        
        distributeIntervalsButton = new JButton(distributeIcon);
        distributeIntervalsPanel = new JPanel();
        distributeIntervalsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
               
        this.intervals = mapping.getIntervals();
        this.colors = colors;
        mapping.addPropertyChangeListener(ConfigMapping.MINIMUM_PROPERTY_VALUE, new GenericPropertyChangeListener<Double>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<Double> ev) {
                setMinimum(ev.getNewValue());
            }
        });
        mapping.addPropertyChangeListener(ConfigMapping.MAXIMUM_PROPERTY_VALUE, new GenericPropertyChangeListener<Double>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<Double> ev) {
                setMaximum(ev.getNewValue());
            }
        });
        mapping.addPropertyChangeListener(ConfigMapping.DISTINCT_STRING_VALUES, new GenericPropertyChangeListener<List<String>>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<List<String>> ev) {
                logger.trace("Distinct String values changed to: {}", ev.getNewValue());
                distinctStringValues = ev.getNewValue();
                if (ev.getNewValue() == null) {
                    setNumIntervals(true, null);
                } else {
                    setNumIntervals(false, ev.getNewValue());
                    Collections.sort(distinctStringValues);
                }
            }
        });
        initialize();
    }
    
    private void initialize() {
        if (numIntervals) {
            maximumModel = new SpinnerNumberModel(maximum, maximum, maximum, 0);
            if (intervals.isEmpty())
                intervals.add(new Interval(minimum, DefaultColors.getMutedColor(0)));
            if (intervalsPanel.getComponentCount() == 0) {
                for (Interval i : intervals) {
                    logger.trace("adding panel");
                    SingleNumIntervalPanel panel = new SingleNumIntervalPanel(i);
                    intervalsPanel.add(panel);
                    panel.attach();
                }
            }
        } else {
            if (intervals.isEmpty()) {
                int i = 0;
                for (String s : distinctStringValues) {
                    Interval interval = new Interval(s, DefaultColors.getMutedColor(i++));
                    intervals.add(interval);
                }
            }
            for (Interval interval : intervals) {
                SingleStringIntervalPanel panel = new SingleStringIntervalPanel(interval);
                intervalsPanel.add(panel);
                panel.setAddButtonEnabled(false);
            }
        }
        
        intervalsPanel.add(distributeIntervalsPanel);
        distributeIntervalsPanel.add(distributeIntervalsButton);
        distributeIntervalsButton.addActionListener(new DistributeIntervalListener(IntervalPanel.this));
        distributeIntervalsButton.setToolTipText(I18n.get("Tooltip.ScaffoldTreeViewConfig.DistributeIntervals"));
        if(!numIntervals)
            distributeIntervalsButton.setVisible(false);
        else
            distributeIntervalsButton.setVisible(true);
        
        intervalsPanel.revalidate();

        
        Dimension d = intervalsPanel.getComponent(0).getPreferredSize();
        d.height = d.height * 5;
        //add space for the scroll bar m(
        d.width += 35;
        scrollPane.setPreferredSize(d);
        scrollPane.setBorder(BorderFactory.createTitledBorder(I18n.get("VisualMappings.Intervals")));
        revalidate();
    }

    private void removeInterval(Interval interval) {
        int index = intervals.indexOf(interval);
        SingleNumIntervalPanel toRemove = (SingleNumIntervalPanel) intervalsPanel.getComponent(index);
        if (toRemove.getBelow() == null) {
            toRemove.getAbove().setUpperBoundModel(maximumModel);
        } else {
            toRemove.getAbove().setUpperBoundModel(toRemove.getBelow().getLowerBoundModel());
        }
        intervalsPanel.remove(toRemove);
        logger.trace("removing {}", index);
        intervals.remove(index);
        intervalsPanel.validate();
        intervalsPanel.repaint();
    }
    
    private void setNumIntervals(boolean numIntervals, List<String> newDistinctStringValues) {
        logger.trace("setNumIntervals {}, {}", numIntervals, newDistinctStringValues);
        if (this.numIntervals == numIntervals)
            return;
        intervalsPanel.removeAll();
        intervals.clear();
        this.numIntervals = numIntervals;
        initialize();
    }

    private void setMinimum(double minimum) {
        logger.trace("setMinimum( {} )", minimum);
        this.minimum = minimum;
        if (numIntervals) {
            boolean first = true;
            for (Component c : intervalsPanel.getComponents()) {
                // if c is the distributeInterval button then skip this component
                if(!(c instanceof SingleNumIntervalPanel) && !(c instanceof SingleStringIntervalPanel)) continue;
                SingleNumIntervalPanel i = (SingleNumIntervalPanel)c;
                if (first) {
                    first = false;
                    SpinnerNumberModel m = (SpinnerNumberModel)i.getLowerBoundModel();
                    m.setMinimum(minimum);
                    m.setValue(minimum);
                    m.setMaximum(minimum);
                }
                i.updateLowerBound();
            }
        }
        if(minimum == Double.NEGATIVE_INFINITY)
            distributeIntervalsButton.setEnabled(false);
        else
            distributeIntervalsButton.setEnabled(true);
    }
    
    private void setMaximum(double maximum) {
        logger.trace("setMaximum( {} )", maximum);
        this.maximum = maximum;
        if (numIntervals) {
            for (Component c : intervalsPanel.getComponents()) {
                // if c is the distributeInterval button then skip this component
                if(!(c instanceof SingleNumIntervalPanel) && !(c instanceof SingleStringIntervalPanel)) continue;
                SingleNumIntervalPanel i = (SingleNumIntervalPanel)c;
                i.updateUpperBound();
            }
            maximumModel.setMaximum(maximum);
            maximumModel.setMinimum(maximum);
            maximumModel.setValue(maximum);
        }
    }

    /**
     * A panel representing a single interval. Holds two spinners which can be
     * used to set upper and lower bound of the interval (where upper bound is
     * the lower bound of the next interval) buttons to add and remove an
     * interval and optionally a color editor.
     * <p>
     * It is generally assumed that the backing <code>Interval</code> is
     * inserted into the interval list first. Then the new
     * <code>SingleIntervalPanel</code> should be created and inserted into the
     * <code>IntervalPanel</code> at the correct position afterwards
     * <code>attach()</code> should be called to link a panel with its upper and
     * lower neighbor.
     */
    // TODO I am pretty sure there is a better way to do this.
    private class SingleNumIntervalPanel extends JPanel implements ChangeListener {

        private final Interval interval;

        private final JSpinner lowerBound;
        private final JSpinner upperBound;
        private final ColorEditor colorEditor;

        public SingleNumIntervalPanel(Interval interval) {
            super();
            FormLayout layout = new FormLayout("3dlu, 60dlu, 3dlu, 60dlu, 3dlu, 24dlu, 3dlu, 24dlu, 3dlu, 24dlu, 3dlu",
                    "24dlu, 5dlu");
            layout.setColumnGroups(new int[][] { { 2, 4 } });
            setLayout(layout);

            
            this.interval = interval;

            lowerBound = new JSpinner(new SpinnerNumberModel(interval.getLowerBound(), minimum, maximum, 0.1));

            add(lowerBound, CC.xy(2, 1));
            upperBound = new JSpinner(maximumModel);
            upperBound.setEnabled(false);
            add(upperBound, CC.xy(4, 1));

            if (colors) {
                colorEditor = new ColorEditor(interval.getColor());
                add(colorEditor, CC.xy(6, 1));
                colorEditor.addPropertyChangeListener(ColorEditor.COLOR_PROPERTY, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        SingleNumIntervalPanel.this.interval.setColor((Color) evt.getNewValue());
                    }
                });
            } else
                colorEditor = null;

            JButton addInterval = new JButton(plusIcon);
            add(addInterval, CC.xy(10, 1));
            addInterval.addActionListener(new AddIntervalListener(SingleNumIntervalPanel.this));

            // increase the upper bound if the lower bound gets larger than the
            // upper bound
            lowerBound.getModel().addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    Double v = (Double) ((SpinnerNumberModel) e.getSource()).getValue();
                    SingleNumIntervalPanel.this.interval.setLowerBound(v);
                    if (v.compareTo((Double) upperBound.getValue()) > 0)
                        upperBound.setValue(v);
                }
            });
            
            //so boxlayout lays the out directly beneath each other
            setMaximumSize(getPreferredSize());
        }

        public void setUpperBoundModel(SpinnerModel model) {
            upperBound.getModel().removeChangeListener(this);
            upperBound.setModel(model);
            upperBound.setEnabled(true);
            model.addChangeListener(this);
            if (model == maximumModel)
                upperBound.setEnabled(false);
        }
        
        public void updateLowerBound() {
            SpinnerNumberModel m = (SpinnerNumberModel)lowerBound.getModel();
            m.setMinimum(minimum);
            if ((Double)m.getValue() < minimum)
                m.setValue(minimum);
        }
        
        public void updateUpperBound() {
            SpinnerNumberModel m = (SpinnerNumberModel)lowerBound.getModel();
            m.setMaximum(maximum);
            if ((Double)m.getValue() > maximum)
                m.setValue(maximum);
        }
        
        public SingleNumIntervalPanel getAbove() {
            int index = intervals.indexOf(interval) - 1;
            if (index < 0 || index >= intervalsPanel.getComponentCount() - 1)
                return null;
            return (SingleNumIntervalPanel) intervalsPanel.getComponent(index);
        }

        public Interval getInterval() {
            return interval;
        }

        public SpinnerModel getLowerBoundModel() {
            return lowerBound.getModel();
        }

        /**
         * @return the below
         */
        public SingleNumIntervalPanel getBelow() {
            int index = intervals.indexOf(interval) + 1;
            if (index < 0 || index >= intervalsPanel.getComponentCount() - 1)
                return null;
            return (SingleNumIntervalPanel) intervalsPanel.getComponent(index);
        }

        // decrease the lower bound if the upper bound gets smaller than the
        // lower bound
        @Override
        public void stateChanged(ChangeEvent e) {
            Double v = (Double) ((SpinnerNumberModel) e.getSource()).getValue();
            if (v.compareTo((Double) lowerBound.getValue()) < 0)
                lowerBound.setValue(v);
        }

        public void attach() {
            SingleNumIntervalPanel above = getAbove();
            if (above != null) {
                logger.trace("above found");
                JButton deleteInterval = new JButton(deleteIcon);
                add(deleteInterval, CC.xy(8, 1));
                deleteInterval.addActionListener(new DeleteListener(interval));
                above.setUpperBoundModel(lowerBound.getModel());
            } else { // this is the uppermost panel
                lowerBound.setEnabled(false);
            }
            SingleNumIntervalPanel below = getBelow();
            if (below != null) {
                logger.trace("below found");
                setUpperBoundModel(below.getLowerBoundModel());
            }
        }
    }
    
    private class SingleStringIntervalPanel extends JPanel {
        
        private final Interval interval;
        private final ColorEditor colorEditor;
        private final JButton addButton;
        
        SingleStringIntervalPanel(Interval interval) {
            super(new FormLayout("3dlu, p, 3dlu, 24dlu, 3dlu, 24dlu, 3dlu, 24dlu, 3dlu", "p"));
            this.interval = interval;
            
            add(buildStringChooser(), CC.xy(2, 1));
            
            
            
            if (colors) {
                colorEditor = new ColorEditor(interval.getColor());
                add(colorEditor, CC.xy(4, 1));
                colorEditor.addPropertyChangeListener(ColorEditor.COLOR_PROPERTY, new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        SingleStringIntervalPanel.this.interval.setColor((Color) evt.getNewValue());
                    }
                });
            } else
                colorEditor = null;

            addButton = new JButton(plusIcon);
            add(addButton, CC.xy(8, 1));
            addButton.addActionListener(new AddStringIntervalListener(SingleStringIntervalPanel.this));

            if (intervals.indexOf(interval) != 0) {
                JButton removeButton = new JButton(deleteIcon);
                add(removeButton, CC.xy(6,1));
                removeButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        intervals.remove(SingleStringIntervalPanel.this.interval);
                        intervalsPanel.remove(SingleStringIntervalPanel.this);
                        intervalsPanel.revalidate();
                        for (Component c : intervalsPanel.getComponents()) {
                            // if c is the distributeInterval button then skip this component
                            if(!(c instanceof SingleNumIntervalPanel) && !(c instanceof SingleStringIntervalPanel)) continue;
                            SingleStringIntervalPanel p = (SingleStringIntervalPanel)c;
                            p.setAddButtonEnabled(true);
                        }
                    }
                });
            }
            
            
            
        }
        
        private JComboBox<String> buildStringChooser() {
            JComboBox<String> box = new JComboBox<String>((String[]) distinctStringValues.toArray());
            box.addActionListener(new ActionListener() {
                @SuppressWarnings("unchecked")
                @Override
                public void actionPerformed(ActionEvent e) {
                    interval.setString(((JComboBox<String>)e.getSource()).getSelectedItem().toString());
                }
            });
            box.setSelectedItem(interval.getString());
            return box;
        }
        
        Interval getInterval() {
            return interval;
        }
        
        void setAddButtonEnabled(boolean disabled) {
            addButton.setEnabled(disabled);
        }
    }

    private class DeleteListener implements ActionListener {

        private final Interval interval;

        public DeleteListener(Interval panel) {
            this.interval = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            removeInterval(interval);
        }
    }

    private class AddIntervalListener implements ActionListener {

        private final SingleNumIntervalPanel panel;

        public AddIntervalListener(SingleNumIntervalPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            double lowerBound = panel.getBelow() != null ? panel.getBelow().getInterval().getLowerBound() : maximum;
            Interval interval = new Interval(lowerBound, DefaultColors.getMutedColor(intervals.size()));
            int index = intervals.indexOf(panel.getInterval()) + 1;
            logger.trace("adding interval at {}, lb: {}", index, lowerBound);
            intervals.add(index, interval);
            SingleNumIntervalPanel panel = new SingleNumIntervalPanel(interval);
            intervalsPanel.add(panel, index);
            panel.attach();
            intervalsPanel.validate();
        }
    }
    
    private class AddStringIntervalListener implements ActionListener {
        
        private final SingleStringIntervalPanel panel;

        public AddStringIntervalListener(SingleStringIntervalPanel panel) {
            this.panel = panel;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            Set<String> mappedStrings = Sets.newHashSetWithExpectedSize(intervals.size());
            for (Interval i : intervals) {
                mappedStrings.add(i.getString());
            }
            Iterable<String> unmappedStrings = Iterables.filter(distinctStringValues, Predicates.not(Predicates.in(mappedStrings)));
            assert !Iterables.isEmpty(unmappedStrings);
            Interval interval = new Interval(unmappedStrings.iterator().next(), DefaultColors.getMutedColor(intervals.size()));
            int index = intervals.indexOf(panel.getInterval()) + 1;
            intervals.add(index, interval);
            SingleStringIntervalPanel panel = new SingleStringIntervalPanel(interval);
            intervalsPanel.add(panel, index);
            intervalsPanel.revalidate();
            
            if (intervals.size() == distinctStringValues.size()) {
                for (Component c : intervalsPanel.getComponents()) {
                    // if c is the distributeInterval button then skip this component
                    if(!(c instanceof SingleNumIntervalPanel) && !(c instanceof SingleStringIntervalPanel)) continue;
                    SingleStringIntervalPanel p = (SingleStringIntervalPanel)c;
                    p.setAddButtonEnabled(false);
                }
            }
        }
    }
    
    private class DistributeIntervalListener implements ActionListener {

        private final IntervalPanel panel;

        public DistributeIntervalListener(IntervalPanel panel) {
            this.panel = panel;
        }

        @Override
        public void actionPerformed(ActionEvent e) {        
            assert(numIntervals);            
            int countIntervals = intervals.size();
            double stepSize = (maximum - minimum)/(countIntervals);
            for(int i = 0; i < countIntervals; i++) {
                ((SingleNumIntervalPanel)intervalsPanel.getComponent(i)).getLowerBoundModel().setValue(minimum + (i) * stepSize);
            }           
            intervalsPanel.validate();
        }
    }
  
}