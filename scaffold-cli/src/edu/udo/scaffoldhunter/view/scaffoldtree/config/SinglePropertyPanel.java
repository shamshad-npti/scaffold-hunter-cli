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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.dataimport.PropertyDefinitionListCellRenderer;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.MappingType;
import edu.udo.scaffoldhunter.model.VisualFeature;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.util.Orderings;
import edu.udo.scaffoldhunter.util.ProgressAdapter;

/**
 * A variable panel which can be used to map single properties to various visual
 * features. The Panel's model is a <code>ConfigMapping</code> changes made in
 * the panel are directly applied to the backing model.
 * <p>
 * The panel will change in a <i>hopefully</i> sensible manner based on the
 * visual feature:
 * <ul>
 * <li>Some visual features allow mapping of values to colors i.e Background
 * Color
 * <li>Others allow gradient or intervals, but no colors (Node Size)
 * <li>Again other features allow only the selection of a property (Label)
 * <ul>
 * 
 * @author Henning Garus
 * 
 */
public class SinglePropertyPanel extends JPanel implements ActionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(SinglePropertyPanel.class);

    private static final Set<VisualFeature> SUPPORTED = EnumSet.of(VisualFeature.EdgeThickness,
            VisualFeature.EdgeColor, VisualFeature.InfoBar, VisualFeature.Label, VisualFeature.NodeBackgroundColor,
            VisualFeature.NodeSize);

    private static final String GRADIENT_TOGGLE_COMMAND = "GRADIENT";
    private static final String INTERVAL_TOGGLE_COMMAND = "INTERVAL";
    private static final String CUMULATIVE_CHECK_COMMAND = "CUMULATIVE";
    private static final String PROPERTY_SELECTION_COMMAND = "PROPERTY";
    private static final String FUNCTION_SELECTION_COMMAND = "FUNCTION";
    private static final String BOUNDS_CHECK_COMMAND = "BOUNDS";

    private static final int MAX_DISTINCT_STRING_COUNT = 10;

    private final DbManager db;

    private final ConfigMapping mapping;
    private final Profile profile;
    private final Set<PanelType> type;
    private final Subset subset;

    private final JComboBox propertySelection;
    private final JLabel functionSelectionLabel;
    private final JComboBox functionSelection;
    private final JCheckBox cumulative;
    private final JCheckBox subsetBounds;
    private final CardLayout intervalGradientLayout = new CardLayout();
    private final JPanel intervalGradientContainer = new JPanel(intervalGradientLayout);

    private JRadioButton gradient = null;
    private JRadioButton interval = null;

    private IntervalPanel intervalPanel;

    /**
     * Create a new SinglePropertyPanel with the specified mapping as a model.
     * The profile is used to obtain database values where necessary.
     * <p>
     * In general the profile should be the profile of the currently active
     * user.
     * 
     * @param mapping
     *            this panel's model
     * @param profile
     *            the profile which <i>owns</code> the mapping
     * @param db
     */
    public SinglePropertyPanel(ConfigMapping mapping, Profile profile, DbManager db, Subset subset) {
        super(new FormLayout("max(100dlu;p), 3dlu, p, p:g", "p, 3dlu, p, 3dlu, p, 3dlu, p, 5dlu, p, 3dlu, p, 5dlu, f:p:g"));
        if (!SUPPORTED.contains(mapping.getVisualFeature()))
            throw new IllegalArgumentException(mapping.getVisualFeature().name() + " not supported");

        this.mapping = mapping;
        this.profile = profile;
        this.type = determineType(mapping.getVisualFeature());
        this.db = db;
        this.subset = subset;

        // build and add comboBoxes for property selection
        this.propertySelection = buildPropertySelection();
        this.functionSelection = buildFunctionSelection();
        this.cumulative = buildCumulativeCB();

        add(new JLabel(_("Model.PropertyDefinition") + ":"), CC.xy(1, 1));
        add(propertySelection, CC.xy(1, 3));
        functionSelectionLabel = new JLabel(_("Model.AccumulationFunction") + ":");
        add(functionSelectionLabel, CC.xy(3, 1));
        add(functionSelection, CC.xy(3, 3));

        add(cumulative, CC.xy(1, 5));
        
        // build checkBox to differentiate between global bounds and subset bounds
        subsetBounds = new JCheckBox(_("Model.SubsetForBorders"));
        subsetBounds.setSelected(mapping.isSubsetForBounds());
        subsetBounds.setActionCommand(BOUNDS_CHECK_COMMAND);
        subsetBounds.addActionListener(this);
        add(subsetBounds, CC.xy(1, 7));

        // build and add Radio buttons for interval/gradient toggle if needed
        if (type.containsAll(EnumSet.of(PanelType.INTERVAL, PanelType.GRADIENT))) {
            ButtonGroup g = new ButtonGroup();
            gradient = buildGradientToggle(g);
            interval = buildIntervalToggle(g);

            add(gradient, CC.xy(1, 9));
            add(interval, CC.xy(1, 11));
        }

        add(intervalGradientContainer, CC.xyw(1, 13, 4, "c, f"));
        // build interval/gradient panel if needed
        if (type.contains(PanelType.INTERVAL)) {
            intervalPanel = new IntervalPanel(type.contains(PanelType.COLOR), mapping);
            intervalGradientContainer.add(intervalPanel, MappingType.Interval.name());
        } else {
            intervalPanel = null;
        }
        if (type.contains(PanelType.GRADIENT)) {
            GradientPanel gradientPanel = new GradientPanel(type.contains(PanelType.COLOR), mapping);
            intervalGradientContainer.add(gradientPanel, MappingType.Gradient.name());
        }

        // set selection and show
        switch (mapping.getMappingType()) {
        case Gradient:
            if (gradient != null)
                gradient.setSelected(true);
            break;
        case Interval:
            if (interval != null)
                interval.setSelected(true);
        default:
        }
        intervalGradientLayout.show(intervalGradientContainer, mapping.getMappingType().name());

        // set preferred size based on the accumulation
        // chooser being visible
        functionSelection.setVisible(true);
        setPreferredSize(getPreferredSize());
        checkAccumulationFunctionVisible();
    }

    private void checkAccumulationFunctionVisible() {
        PropertyDefinition propdef = (PropertyDefinition)propertySelection.getSelectedItem();
        boolean cumulative = this.cumulative.isSelected();
        boolean infobar = mapping.getVisualFeature().equals(VisualFeature.InfoBar);

        boolean visible = propdef != null && ((!propdef.isScaffoldProperty() || cumulative) && !infobar);
        functionSelection.setVisible(visible);
        functionSelectionLabel.setVisible(visible);
    }

    private Set<PanelType> determineType(VisualFeature feature) {
        switch (feature) {
        case NodeSize:
        case EdgeThickness:
            return Sets.immutableEnumSet(PanelType.INTERVAL, PanelType.GRADIENT, 
                    PanelType.MOLECULE_NUM_VALUES, PanelType.SCAFFOLD_NUM_VALUES);
        case EdgeColor:
        case NodeBackgroundColor:
            return Sets.immutableEnumSet(PanelType.INTERVAL, PanelType.GRADIENT, PanelType.COLOR, PanelType.MOLECULE_NUM_VALUES,
                    PanelType.SCAFFOLD_STRING_VALUES, PanelType.SCAFFOLD_NUM_VALUES);
        case InfoBar:
            return Sets.immutableEnumSet(PanelType.INTERVAL, PanelType.COLOR, PanelType.MOLECULE_NUM_VALUES, PanelType.MOLECULE_STRING_VALUES);
        case Label:
            return Sets.immutableEnumSet(PanelType.MOLECULE_NUM_VALUES, PanelType.SCAFFOLD_NUM_VALUES, PanelType.SCAFFOLD_STRING_VALUES);
        }
        return null;
    }

    private JComboBox buildPropertySelection() {
        Dataset dataset = profile.getCurrentSession().getDataset();
        Vector<PropertyDefinition> defs = new Vector<PropertyDefinition>();
        for (PropertyDefinition p : dataset.getPropertyDefinitions().values()) {
            // panel allows scaffold properties && is scaffoldProperty
            if (p.isMappable() && (
                    (type.contains(PanelType.SCAFFOLD_NUM_VALUES) && p.isScaffoldProperty() && !p.isStringProperty())
                    || (type.contains(PanelType.SCAFFOLD_STRING_VALUES) && p.isScaffoldProperty() && p.isStringProperty())
                    || (type.contains(PanelType.MOLECULE_NUM_VALUES) && !p.isScaffoldProperty() && !p.isStringProperty())
                    || (type.contains(PanelType.MOLECULE_STRING_VALUES) && !p.isScaffoldProperty() && p.isStringProperty()) )) {
                defs.add(p);
            }
        }
        Collections.sort(defs, Orderings.PROPERTY_DEFINITION_BY_TITLE);
        JComboBox propertySelection = new JComboBox(defs);
        propertySelection.insertItemAt(_("VisualMappings.ClearMapping"), 0);
        propertySelection.setSelectedItem(mapping.getProperty(dataset));
        propertySelection.setRenderer(new PropertyDefinitionListCellRenderer());
        propertySelection.setActionCommand(PROPERTY_SELECTION_COMMAND);
        propertySelection.addActionListener(this);
        return propertySelection;
    }

    private JComboBox buildFunctionSelection() {
        JComboBox functionSelection = new JComboBox(AccumulationFunction.values());
        PropertyDefinition propDef = mapping.getProperty(profile.getCurrentSession().getDataset());
        functionSelection.setVisible((propDef == null || !propDef.isScaffoldProperty()) && !mapping.getVisualFeature().equals(VisualFeature.InfoBar));
        functionSelection.setSelectedItem(mapping.getFunction());
        functionSelection.setActionCommand(FUNCTION_SELECTION_COMMAND);
        functionSelection.addActionListener(this);
        return functionSelection;
    }

    private JCheckBox buildCumulativeCB() {
        JCheckBox cumulative = new JCheckBox(_("Model.SubtreeCumulative"));
        cumulative.setActionCommand(CUMULATIVE_CHECK_COMMAND);
        cumulative.addActionListener(this);
        cumulative.setSelected(mapping.isCumulative());
        return cumulative;
    }

    private JRadioButton buildGradientToggle(ButtonGroup g) {
        JRadioButton b = new JRadioButton(_("VisualMappings.Gradient"));
        b.setActionCommand(GRADIENT_TOGGLE_COMMAND);
        b.addActionListener(this);
        g.add(b);
        return b;
    }

    private JRadioButton buildIntervalToggle(ButtonGroup g) {
        JRadioButton b = new JRadioButton(_("VisualMappings.Interval"));
        b.setActionCommand(INTERVAL_TOGGLE_COMMAND);
        b.addActionListener(this);
        g.add(b);
        return b;
    }

    private static enum PanelType {
        INTERVAL, GRADIENT, COLOR, MOLECULE_STRING_VALUES, MOLECULE_NUM_VALUES, SCAFFOLD_STRING_VALUES, SCAFFOLD_NUM_VALUES
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(GRADIENT_TOGGLE_COMMAND)) {
            mapping.setMappingType(MappingType.Gradient);
            intervalGradientLayout.show(intervalGradientContainer, MappingType.Gradient.name());
        } else if (e.getActionCommand().equals(INTERVAL_TOGGLE_COMMAND)) {
            mapping.setMappingType(MappingType.Interval);
            intervalGradientLayout.show(intervalGradientContainer, MappingType.Interval.name());
        } else if (e.getActionCommand().equals(CUMULATIVE_CHECK_COMMAND)) {
            mapping.setCumulative(cumulative.isSelected());
            checkAccumulationFunctionVisible();
            if (mapping.getFunction() == null)
                mapping.setFunction(AccumulationFunction.Average);
            if((PropertyDefinition) propertySelection.getSelectedItem() != null) {
                PreparePropertyChangeWorker worker = new PreparePropertyChangeWorker(db,
                        (PropertyDefinition) propertySelection.getSelectedItem(), mapping.getFunction(), 
                        subsetBounds.isSelected(), mapping.isCumulative(), profile.getCurrentSession(), subset);
                worker.addProgressListener(new PreparePropertyProgressListener());
                worker.execute();
            }
        } else if (e.getActionCommand().equals(BOUNDS_CHECK_COMMAND)) {
            mapping.setSubsetForBounds(subsetBounds.isSelected());
            if((PropertyDefinition) propertySelection.getSelectedItem() != null) {
                PreparePropertyChangeWorker worker = new PreparePropertyChangeWorker(db,
                        (PropertyDefinition) propertySelection.getSelectedItem(), mapping.getFunction(), 
                        subsetBounds.isSelected(), mapping.isCumulative(), profile.getCurrentSession(), subset);
                worker.addProgressListener(new PreparePropertyProgressListener());
                worker.execute();
            }
        } else if (e.getActionCommand().equals(FUNCTION_SELECTION_COMMAND)) {
            mapping.setFunction((AccumulationFunction) functionSelection.getSelectedItem());
            if((PropertyDefinition) propertySelection.getSelectedItem() != null) {
                PreparePropertyChangeWorker worker = new PreparePropertyChangeWorker(db,
                        (PropertyDefinition) propertySelection.getSelectedItem(), mapping.getFunction(), 
                        subsetBounds.isSelected(), mapping.isCumulative(), profile.getCurrentSession(), subset);
                worker.addProgressListener(new PreparePropertyProgressListener());
                worker.execute();
            }
        } else if (e.getActionCommand().equals(PROPERTY_SELECTION_COMMAND)) {
            if (propertySelection.getSelectedItem() instanceof String) {
                // do not map to this feature selected
                mapping.setProperty(null);
                propertySelection.setSelectedItem(null);
            } else {
                PropertyDefinition p = (PropertyDefinition) propertySelection.getSelectedItem();
                if (!(p.isScaffoldProperty() || p.isStringProperty() || 
                        mapping.getVisualFeature().equals(VisualFeature.InfoBar)) || mapping.isCumulative()) {
                    if (mapping.getFunction() == null)
                        mapping.setFunction(AccumulationFunction.Average);
                } else {
                    mapping.setFunction(null);
                }
                if (p.isStringProperty() && !p.isScaffoldProperty()) {
                    assert intervalPanel != null;
                    setIntervalGradientRBsEnabled(false);
                    intervalGradientLayout.show(intervalGradientContainer, MappingType.Interval.name());
                } else {
                    setIntervalGradientRBsEnabled(true);
                }
                mapping.setProperty(p);
                if (mapping.getFunction() != null || mapping.getVisualFeature().equals(VisualFeature.InfoBar) || p.isScaffoldProperty()) {
                    PreparePropertyChangeWorker worker = new PreparePropertyChangeWorker(db,
                            (PropertyDefinition) propertySelection.getSelectedItem(), mapping.getFunction(), 
                            subsetBounds.isSelected(), mapping.isCumulative(), profile.getCurrentSession(), subset);
                    worker.addProgressListener(new PreparePropertyProgressListener());
                    worker.execute();
                }

            }
            checkAccumulationFunctionVisible();
        } else {
            throw new AssertionError("unhandled ActionCommand");
        }

    }

    private void setIntervalGradientRBsEnabled(boolean enabled) {
        if (interval != null)
            interval.setEnabled(enabled);
        if (gradient != null)
            gradient.setEnabled(enabled);
    }

    /**
     * Can be used to query the supported VisualFeatures of
     * <code>SinglePropertyPanel</code>. <code>SinglePropertyPanels</code> can
     * only be instantiated for ConfigMappings which map to a supported visual
     * feature.
     * 
     * @param feature
     *            the visual feature
     * @return <code>true</code> if <code>feature</code> is supported
     *         </code>false</code> otherwise
     */
    public static boolean supported(VisualFeature feature) {
        return SUPPORTED.contains(feature);
    }

    private static class PreparePropertyChangeWorker extends ProgressWorker<WorkerResult, Void> {

        private final DbManager db;
        private final PropertyDefinition propDef;
        private final AccumulationFunction function;
        private final Tree tree;
        private final Session session;
        private final Subset subset;
        private final boolean subsetForBounds;
        private final boolean subtreeCumulative;

        private PreparePropertyChangeWorker(DbManager db, PropertyDefinition propDef, AccumulationFunction function, 
                boolean subsetForBounds, boolean subtreeCumulative, Session session, Subset subset) {
            this.db = db;
            this.propDef = propDef;
            this.function = function;
            this.tree = session.getTree();
            this.session = session;
            this.subset = subset;
            this.subsetForBounds = subsetForBounds;
            this.subtreeCumulative = subtreeCumulative;
        }

        @Override
        protected WorkerResult doInBackground() throws Exception {
            logger.trace("preparing property change");
            try {
                double[] minMax = null;
                List<String> distinctValues = null;
                if (propDef.isStringProperty()) {
                    long distinctStringCount = db.getDistinctValueCount(propDef);
                    if (distinctStringCount <= MAX_DISTINCT_STRING_COUNT) {
                        distinctValues = db.getDistinctStrings(propDef);
                    }
                }
                else {
                    minMax = DBExceptionHandler.callDBManager(db, new DBFunction<double[]>() {
                        @Override
                        public double[] call() throws DatabaseException {
                            return db.getAccPropertyMinMax(subset.getSession().getTree(), propDef, function, 
                                    subsetForBounds ? subset : session.getSubset(), subtreeCumulative, false, false); 
                        }
                    });
                }
                WorkerResult result = new WorkerResult(minMax, function, distinctValues, propDef);
                return result;
            } catch (RuntimeException e) {
                logger.error("runtime exception occured", e);
                throw e;
            }
        }
    }

    private class PreparePropertyProgressListener extends ProgressAdapter<WorkerResult> {
        @Override
        public void setProgressIndeterminate(boolean indeterminate) {
            if (intervalPanel != null) {
                intervalPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            }
        }

        @Override
        public void finished(WorkerResult result, boolean cancelled) {
            /*
             * if multiple workers are executed one after another we might get
             * results for the wrong property definition/ accumulation function
             * in that case we just ignore them
             */
            if (result.propDef == (PropertyDefinition) propertySelection.getSelectedItem()
                    && ((result.function == null && result.propDef.isStringProperty()) || (result.function == mapping
                            .getFunction()))) {
                if (result.propDef.isStringProperty()) {
                    mapping.setDistinctStringValues(result.distinctValues);
                } else {
                    
                    mapping.setDistinctStringValues(null);
                    // Edge Width is a special case since we use absolute differences
                    if (mapping.getVisualFeature().equals(VisualFeature.EdgeThickness)) {
                        double max = Math.abs(result.maximum - result.minimum);
                        mapping.setMinimumPropertyValue(0);
                        mapping.setMaximumPropertyValue(max);
                    } else {
                        mapping.setMinimumPropertyValue(result.minimum);
                        mapping.setMaximumPropertyValue(result.maximum);
                    }
                }
                if (intervalPanel != null) {
                    intervalPanel.setCursor(Cursor.getDefaultCursor());
                }
            }

        }
    }

    private static class WorkerResult {
        final double minimum;
        final double maximum;
        final AccumulationFunction function;
        final List<String> distinctValues;
        final PropertyDefinition propDef;

        private WorkerResult(double[] minMax, AccumulationFunction function, List<String> distinctValues,
                PropertyDefinition propDef) {

            if (minMax != null) {
                if(Double.isNaN(minMax[0]) || Double.isNaN(minMax[1])) {
                    // one of the entries is NaN
                    this.minimum = 0;
                    this.maximum = 0;
                } else {
                    this.minimum = minMax[0];
                    this.maximum = minMax[1];
                }
            } else {
                this.minimum = 0;
                this.maximum = 0;
            }
            this.function = function;
            this.distinctValues = distinctValues;
            this.propDef = propDef;
        }

    }

}
