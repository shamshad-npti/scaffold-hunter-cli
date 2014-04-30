/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

import java.awt.Panel;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Hashtable;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.clustering.ClusteringController;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.VoidNullaryDBFunction;
import edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener;
import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkage;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.MatrixNNSearch.MatrixParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.clustering.PropertyCount;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.ProgressListener;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * This {@link Panel} contains all elements that are necessary to start a new
 * hierarchical clustering. Therefore you can select the {@link Linkage},
 * {@link Distance}, {@link NNSearch} strategy, and so on.
 * 
 * @author Till Schäfer
 * 
 */
public class StartClusteringPanel extends JPanel {

    private ApplyAction applyAction = new ApplyAction();
    private CancelAction cancelAction = new CancelAction();

    private JLabel clusteringTypeDescription = new JLabel("-----");
    private JLabel linkageDescription = new JLabel("-----");
    private JLabel distanceDescription = new JLabel("-----");

    private DefaultListModel propertyListModel = new DefaultListModel();
    private JList propertyList = new JList(propertyListModel);
    private JLabel propertyCountMessage = new JLabel("-----");
    private JLabel propertyDescription = new JLabel("<html><p align='justify'>"
            + _("DendrogramView.StartClusteringPanel.MouseNotOverPropertyDescription") + "</p></html>");

    private JSlider quality = new JSlider(JSlider.HORIZONTAL, 0, 99, 10);
    private JSlider dimensionality = new JSlider(JSlider.HORIZONTAL, 0, 2, 0);
    private JLabel qualityLabel = new JLabel(_("DendrogramView.StartClusteringPanel.Quality") + ":", JLabel.LEFT);
    private JLabel dimensionalityLabel = new JLabel(_("DendrogramView.StartClusteringPanel.Dimensionality") + ":",
            JLabel.LEFT);

    private JPanel linkagePanel = new JPanel();
    private JPanel distancePanel = new JPanel();

    private static FormLayout layout = new FormLayout("left:default, 20dlu, fill:180dlu, 20dlu:grow, 200dlu",
            "center:15dlu, top:10dlu, min, fill:pref, min, fill:pref, min, fill:pref, 0dlu:grow, 20dlu");
    private final CellConstraints cc = new CellConstraints();
    private final ClusteringController clusteringController;

    private Distances currentDistance = null;
    private Linkages currentLinkage = null;
    /**
     * true if exact clustering and false if heuristic
     */
    private boolean selectedExact = true;

    /**
     * Constructor
     * 
     * @param clusteringController
     *            the {@link ClusteringController}
     */
    public StartClusteringPanel(ClusteringController clusteringController) {
        super(layout);
        this.clusteringController = Preconditions.checkNotNull(clusteringController);

        init();
    }

    /**
     * Constructor for predefined State
     * 
     * @param clusteringController
     *            the {@link ClusteringController}
     * @param exact
     *            the default setting for the ClusteringTypePanel
     * @param linkage
     *            the default setting for the LinkagePanel
     * @param distance
     *            the default setting for the DistancePanel
     * @param propDefs
     *            the default selection for the PropertyPanel
     * @param quality
     *            If exact this need to be set to the used quality. Otherwise
     *            this value is ignored.
     * @param dimensionality
     *            If exact this need to be set to the used dimensionality.
     *            Otherwise this value is ignored.
     */
    public StartClusteringPanel(ClusteringController clusteringController, boolean exact, Linkages linkage,
            Distances distance, Collection<PropertyDefinition> propDefs, Integer quality, Integer dimensionality) {
        super(layout);
        // exact -> quality != null
        Preconditions.checkArgument(exact || quality != null);
        // exact -> dimensionality != null
        Preconditions.checkArgument(exact || dimensionality != null);
        this.clusteringController = Preconditions.checkNotNull(clusteringController);
        this.currentLinkage = linkage;
        this.currentDistance = distance;
        this.selectedExact = exact;

        init();

        /*
         * set the slider values if heuristic
         */
        if (!exact) {
            this.quality.setValue(quality);
            this.dimensionality.setValue(dimensionality);
        }

        /*
         * set the previous selected Properties
         */
        int selectedIndices[] = new int[propDefs.size()];
        int i = 0;
        for (PropertyDefinition propDef : propDefs) {
            selectedIndices[i] = propertyListModel.indexOf(propDef);
            i++;
        }
        propertyList.setSelectedIndices(selectedIndices);
    }

    /**
     * Initialises the panel (layout + some general logic).
     */
    private void init() {
        /*
         * Initial Layout
         */

        clusteringTypeDescription.setBorder(BorderFactory
                .createTitledBorder(_("DendrogramView.StartClusteringPanel.Description")));
        linkageDescription.setBorder(BorderFactory
                .createTitledBorder(_("DendrogramView.StartClusteringPanel.Description")));
        distanceDescription.setBorder(BorderFactory
                .createTitledBorder(_("DendrogramView.StartClusteringPanel.Description")));

        clusteringTypeDescription.setVerticalAlignment(SwingConstants.TOP);
        linkageDescription.setVerticalAlignment(SwingConstants.TOP);
        distanceDescription.setVerticalAlignment(SwingConstants.TOP);

        // heading
        add(new JLabel("<html><b><font size=\"+1\">" + _("Clustering.Title.Start") + "</font></b></html>"),
                cc.xyw(1, 1, 5, "center, center"));
        add(getTitledSeparator(""), cc.xyw(1, 2, 5));

        // clustering type (heuristic, exact)
        add(getTitledSeparator("Type of Clustering"), cc.xyw(1, 3, 4));
        add(getClusteringTypePanel(), cc.xy(1, 4));
        add(clusteringTypeDescription, cc.xy(3, 4));

        // linkage
        add(getTitledSeparator("Linkage"), cc.xyw(1, 5, 4));
        add(linkagePanel, cc.xy(1, 6));
        add(linkageDescription, cc.xy(3, 6));

        // distance
        add(getTitledSeparator("Distance"), cc.xyw(1, 7, 4));
        add(distancePanel, cc.xy(1, 8));
        add(distanceDescription, cc.xy(3, 8));

        // property
        add(getPropertyPanel(), cc.xywh(5, 3, 1, 7));

        // Apply - Cancel Buttons
        add(getButtonPanel(), cc.xyw(1, 10, 5, "right, bottom"));

        /*
         * Other init stuff
         */

        propertyList.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent me) {
                Point p = new Point(me.getX(), me.getY());
                int currentIndex = propertyList.locationToIndex(p);

                if (currentIndex == -1) {
                    propertyDescription.setText("<html><p align='justify'>"
                            + _("DendrogramView.StartClusteringPanel.MouseNotOverPropertyDescription") + "</p></html>");
                } else {
                    propertyDescription.setText(((PropertyDefinition) propertyListModel.get(currentIndex))
                            .getDescription());
                }
            }
        });

        propertyList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                propertySelectionChanged();
            }
        });

        propertyList.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
                propertyDescription.setText("<html><p align='justify'>"
                        + _("DendrogramView.StartClusteringPanel.MouseNotOverPropertyDescription") + "</p></html>");
            }
        });
    }

    /**
     * Constructs a titled {@link JSeparator}
     * 
     * @param label
     *            the title
     * @return the {@link JPanel} with the titled {@link JSeparator}
     */
    private JPanel getTitledSeparator(String label) {
        JPanel panel = new JPanel(new FormLayout("pref, fill:pref:grow", "bottom:pref"));
        panel.add(new JLabel(label), cc.xy(1, 1));
        panel.add(new JSeparator(), cc.xy(2, 1));

        return panel;
    }

    /**
     * Creates the panel that holds the Apply/Cancel buttons.
     * 
     * @return The <code>JPanel</code> with all buttons.
     */
    private JPanel getButtonPanel() {
        JButton profileOK = new JButton(applyAction);
        JButton profileCancel = new JButton(cancelAction);

        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(profileOK, profileCancel);
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        return buttonPanel;
    }

    /**
     * Constructs the ClusteringTypePanel, which contains {@link JRadioButton}s
     * to select exact or heuristic clustering and some parameters for the
     * heuristic Clustering
     * 
     * @return the ClusteringTypePanel
     * 
     */
    private JPanel getClusteringTypePanel() {
        JPanel panel = new JPanel(new FormLayout("left:pref", "pref, pref, pref"));

        JRadioButton exactRadio = new JRadioButton(_("DendrogramView.StartClusteringPanel.ExactClustering"));
        JRadioButton heuristicRadio = new JRadioButton(_("DendrogramView.StartClusteringPanel.HeuristicClustering"));

        exactRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClusteringType(true);
            }
        });
        heuristicRadio.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateClusteringType(false);
            }
        });

        if (selectedExact) {
            exactRadio.doClick();
        } else {
            heuristicRadio.doClick();
        }

        ButtonGroup group = new ButtonGroup();
        group.add(exactRadio);
        group.add(heuristicRadio);

        // Labels for quality
        Hashtable<Integer, JLabel> qualityLabels = new Hashtable<Integer, JLabel>();
        qualityLabels.put(new Integer(0), new JLabel("Low"));
        qualityLabels.put(new Integer(99), new JLabel("High"));
        quality.setLabelTable(qualityLabels);
        quality.setPaintLabels(true);
        quality.setMinorTickSpacing(1);
        quality.setMajorTickSpacing(10);

        // Labels for Dimensionality
        Hashtable<Integer, JLabel> dimLabels = new Hashtable<Integer, JLabel>();
        dimLabels.put(new Integer(0), new JLabel("Low"));
        dimLabels.put(new Integer(1), new JLabel("Mid"));
        dimLabels.put(new Integer(2), new JLabel("High"));
        dimensionality.setLabelTable(dimLabels);
        dimensionality.setPaintLabels(true);
        dimensionality.setMinorTickSpacing(1);
        dimensionality.setMajorTickSpacing(1);
        dimensionality.setSnapToTicks(true);

        JPanel heuristicSettingsPanel = new JPanel(new FormLayout("left:pref", "pref, pref, pref, pref"));
        heuristicSettingsPanel.setBorder(BorderFactory
                .createTitledBorder(_("DendrogramView.StartClusteringPanel.HeuristicSettings")));
        heuristicSettingsPanel.add(qualityLabel, cc.xy(1, 1, "center, center"));
        heuristicSettingsPanel.add(quality, cc.xy(1, 2));
        heuristicSettingsPanel.add(dimensionalityLabel, cc.xy(1, 3, "center, center"));
        heuristicSettingsPanel.add(dimensionality, cc.xy(1, 4));

        panel.add(exactRadio, cc.xy(1, 1));
        panel.add(heuristicRadio, cc.xy(1, 2));
        panel.add(heuristicSettingsPanel, cc.xy(1, 3));

        return panel;
    }

    /**
     * Constructs the LinkagePanel with one {@link JRadioButton} for each
     * available {@link Linkage}
     * 
     * Note: The currently selected linkage is preselected when it is 
     * supported by the chosen clustering algorithm and reset otherwise.
     * 
     * @param exact
     *            true -> exact clustering; false -> heuristic clustering
     * @return the LinakgePanel
     */
    private JPanel getLinkagePanel(boolean exact) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();
        Collection<Linkages> acceptedLinkages = ClusteringController.defaultNNSearchs(exact).acceptedLinkages();

        // currentLinkage is reset, if it does not fit the current Clustering Type
        if(!acceptedLinkages.contains(currentLinkage))
            currentLinkage = null;
        
        boolean first = true;
        for (final Linkages linkage : acceptedLinkages) {
            JRadioButton rButton = new JRadioButton(linkage.getName());
            rButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateLinkage(linkage);
                }
            });
            group.add(rButton);
            panel.add(rButton);

            // select first Button
            if ((currentLinkage == null && first) || currentLinkage == linkage) {
                rButton.doClick();
                first = false;
            }
        }
        return panel;
    }

    /**
     * Constructs the DistancePanel with one {@link JRadioButton} for each
     * available {@link Distance}
     * 
     * @param exact
     *            true -> exact clustering; false -> heuristic clustering
     * @return the DistancePanel
     */
    private JPanel getDistancePanel(boolean exact) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();
        Collection<Distances> acceptedDistances = ClusteringController.defaultNNSearchs(exact).acceptedDistances();

        boolean first = true;
        for (final Distances distance : acceptedDistances) {

            JRadioButton rButton = new JRadioButton(distance.getName());
            rButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    updateDistance(distance);
                }
            });

            group.add(rButton);
            panel.add(rButton);

            if ((currentDistance == null && first) || distance == currentDistance) {
                rButton.doClick();
                first = false;
            }
        }
        return panel;
    }

    /**
     * Constructs the PropertyPanel, which contains a JList with all matching
     * {@link Property}s and some additional informations
     * 
     * @return the PropertyPanel
     */
    private JPanel getPropertyPanel() {
        JPanel propertyPanel = new JPanel(new FormLayout("fill:default", "20dlu, fill:default:grow, fill:40dlu"));

        propertyDescription.setBorder(BorderFactory
                .createTitledBorder(_("DendrogramView.StartClusteringPanel.Description")));
        propertyDescription.setVerticalAlignment(SwingConstants.TOP);

        propertyList.setLayoutOrientation(JList.VERTICAL);

        JScrollPane listScroller = new JScrollPane(propertyList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        propertyPanel.setBorder(BorderFactory
                .createTitledBorder(_("DendrogramView.StartClusteringPanel.MatchProperties")));

        propertyPanel.add(propertyCountMessage, cc.xy(1, 1));
        propertyPanel.add(listScroller, cc.xy(1, 2));
        propertyPanel.add(propertyDescription, cc.xy(1, 3));

        return propertyPanel;
    }

    /**
     * Is called when the clustering type selection changes. Updates the Panel
     * accordingly.
     * 
     * @param exact
     *            true -> exact clustering; false -> heuristic clustering
     */
    private void updateClusteringType(boolean exact) {
        // set current selected clustering type
        selectedExact = exact;

        // enable or disable the heuristic options
        qualityLabel.setEnabled(!exact);
        quality.setEnabled(!exact);
        dimensionalityLabel.setEnabled(!exact);
        dimensionality.setEnabled(!exact);

        // set description
        if (exact) {
            clusteringTypeDescription.setText("<html><p align='justify'>" + _("DendrogramView.ClusteringType.Exact")
                    + "</p></html>");
        } else {
            clusteringTypeDescription.setText("<html><p align='justify'>"
                    + _("DendrogramView.ClusteringType.Heuristic") + "</p></html>");
        }

        /*
         * update linkagePanel
         */
        remove(linkagePanel);
        linkagePanel = getLinkagePanel(exact);
        add(linkagePanel, cc.xy(1, 6));

        /*
         * update distancePanel
         */
        remove(distancePanel);
        distancePanel = getDistancePanel(exact);
        add(distancePanel, cc.xy(1, 8));

        revalidate();
        repaint();
    }

    /**
     * Is called when the linkage selection changes. Updates the Panel
     * accordingly.
     * 
     * @param linkage
     *            the new selected {@link Linkage}
     */
    private void updateLinkage(Linkages linkage) {
        // set the current selected linkage
        currentLinkage = linkage;

        // update description
        linkageDescription.setText("<html><p align='justify'>" + _("DendrogramView.Linkage.Preamble") + "<br><br>"
                + linkage.getInfo() + "</p></html>");
    }

    /**
     * Is called when the linkage selection changes. Updates the Panel
     * accordingly.
     * 
     * @param linkage
     *            the new selected {@link Linkage}
     */
    private void updateDistance(Distances distance) {
        // set the current selected distance
        currentDistance = distance;

        // update description
        distanceDescription.setText("<html><p align='justify'>" + distance.getInfo() + "</p></html>");

        // update propertyList
        switch (distance.acceptedPropertyCount()) {
        case NONE:
            propertyList.setEnabled(false);
            propertyCountMessage.setText("<html><p align='justify'>"
                    + _("DendrogramView.StartClusteringPanel.NoPropertySelectable") + "</p></html>");
            updateShownProperties(clusteringController.getMatchingPropertyDefinitions(distance));
            break;
        case SINGLE:
            propertyList.setEnabled(true);
            propertyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            propertyCountMessage.setText("<html><p align='justify'>"
                    + _("DendrogramView.StartClusteringPanel.OnePropertySelectable") + "</p></html>");
            updateShownProperties(clusteringController.getMatchingPropertyDefinitions(distance));
            break;
        case MULTIPLE:
            propertyList.setEnabled(true);
            propertyList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
            propertyCountMessage.setText("<html><p align='justify'>"
                    + _("DendrogramView.StartClusteringPanel.MultiplePropertySelectable") + "</p></html>");
            updateShownProperties(clusteringController.getMatchingPropertyDefinitions(distance));
            break;
        default:
            throw new IllegalArgumentException("Illegal PropertyCount value");
        }

        propertySelectionChanged();
    }

    /**
     * Updates the JList Model to show propDfs.
     * 
     * @param propDefs
     */
    private void updateShownProperties(Collection<PropertyDefinition> propDefs) {
        propertyListModel.clear();

        for (PropertyDefinition propDef : propDefs) {
            propertyListModel.addElement(propDef);
        }
    }

    /**
     * Should be called when the property selection changed. enables or disables
     * the apply button if the selection is valid or not.
     */
    private void propertySelectionChanged() {
        if (currentDistance.acceptedPropertyCount() == PropertyCount.NONE) {
            // always valid to start clustering
            applyAction.setEnabled(true);
        } else {
            if (propertyList.getSelectedIndices().length == 0) {
                // no property selected where at least one must be selected
                applyAction.setEnabled(false);
            } else {
                // more than one property is selected
                applyAction.setEnabled(true);
            }
        }

    }

    /**
     * @return the selected PropertyDefinitions
     */
    private ArrayList<PropertyDefinition> getSelectedPropDefs() {
        Object[] selectedObjects = propertyList.getSelectedValues();
        ArrayList<PropertyDefinition> selectedPropDefs = Lists.newArrayList(Arrays.copyOf(selectedObjects,
                selectedObjects.length, PropertyDefinition[].class));
        return selectedPropDefs;
    }

    /**
     * Apply Button Events
     * 
     * @author Till Schäfer
     */
    private class ApplyAction extends AbstractAction {
        public ApplyAction() {
            super(_("Button.Apply"));
            putValue(Action.SMALL_ICON, Resources.getIcon("apply.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            StartClusteringPanel.this.setEnabled(false);
            /*
             * Before the actual clustering can be started, it must be validated
             * that all Properties are defined
             */
            clusteringController.validateParameter(getSelectedPropDefs(), new ValidationProgressListener(),
                    new ValidationExceptionListener());
        }

    }

    /**
     * Cancel Button Events
     * 
     * @author Till Schäfer
     */
    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super(_("Button.Cancel"));
            putValue(Action.SMALL_ICON, Resources.getIcon("cancel.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // dispose this panel
            clusteringController.getdView().disposeStartClusteringPanel();
        }
    }

    /**
     * When the validation of the parameters is finished, the clustering needs
     * to be started. If filtering is required, the user is informed and has to
     * choice to abort clustering.
     * 
     * @author Till Schäfer
     */
    private class ValidationProgressListener implements ProgressListener<SimpleEntry<Boolean, Subset>> {

        @Override
        public void setProgressValue(int progress) {
            // nothing to do here
        }

        @Override
        public void setProgressBounds(int min, int max) {
            // nothing to to here
        }

        @Override
        public void setProgressIndeterminate(boolean indeterminate) {
            // nothing to do here
        }

        @Override
        public void finished(final SimpleEntry<Boolean, Subset> result, boolean cancelled) {
            boolean startClustering = false;
            // if subset need to be filtered
            if (result.getKey()) {
                int answer = JOptionPane.showConfirmDialog(StartClusteringPanel.this,
                        I18n.get("Clustering.Start.Question"), I18n.get("Clustering.Title.Error"),
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (answer == JOptionPane.OK_OPTION) {
                    startClustering = true;
                }
            } else {
                startClustering = true;
            }

            if (startClustering) {
                // dispose this panel
                clusteringController.getdView().disposeStartClusteringPanel();

                final NNSearchs nnSearch = ClusteringController.defaultNNSearchs(selectedExact);
                final NNSearchParameters parameters;
                final HierarchicalClusterings clustering = ClusteringController
                        .defaultClusteringAlgorithm(selectedExact);

                if (selectedExact) {
                    // exact
                    parameters = new MatrixParameters();
                } else {
                    // heuristic
                    parameters = ClusteringController.bestFrontierParameterGeneration(quality.getValue() + 1,
                            dimensionality.getValue() + 1, result.getValue().size());
                }

                // start clustering
                DBExceptionHandler.callDBManager(null, new VoidNullaryDBFunction() {
                    @Override
                    public void voidCall() throws DatabaseException {
                        clusteringController.startClustering(result.getValue(), currentLinkage, currentDistance,
                                getSelectedPropDefs(), clustering, nnSearch, parameters);
                    }
                }, true);
            } else {
                /*
                 * we may want to select other parameters and start the
                 * clustering again
                 */
                StartClusteringPanel.this.setEnabled(true);
            }
        }
    }

    private class ValidationExceptionListener implements WorkerExceptionListener {

        @Override
        public ExceptionHandlerResult exceptionThrown(Throwable e) {
            if (e.getClass() == ClusteringException.class) {
                JOptionPane.showMessageDialog(StartClusteringPanel.this, e.getMessage(), _("Clustering.Title.Error"),
                        JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(StartClusteringPanel.this, e.getMessage(), _("Message.UnknownError"),
                        JOptionPane.ERROR_MESSAGE);
            }
            return ExceptionHandlerResult.STOP;
        }

    }
}
