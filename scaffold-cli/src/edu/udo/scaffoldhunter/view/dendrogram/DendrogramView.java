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

package edu.udo.scaffoldhunter.view.dendrogram;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.gui.clustering.ClusteringController;
import edu.udo.scaffoldhunter.gui.clustering.ClusteringWorker;
import edu.udo.scaffoldhunter.gui.util.ProgressPanel;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.Linkage;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.GenericView;
import edu.udo.scaffoldhunter.view.SideBarItem;
import edu.udo.scaffoldhunter.view.table.CellZoom;
import edu.udo.scaffoldhunter.view.table.ViewComponent;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.TooltipEventHandler;
import edu.udo.scaffoldhunter.view.util.TooltipManager;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * A view that supports clustering of <code>Molecules</code> and showing the
 * result in a dendrogram tree.<br>
 * <br>
 * 
 * 
 * Note that this view is implemented against <code>Structure</code>, so
 * theoretically it can cluster both <code>Molecule</code>s and
 * <code>Scaffold</code>s. If you want to add support to cluster
 * <code>Scaffold</code>s you have to rewrite the code which manages the
 * selection, because a the moment scaffolds are not added to the selection.
 * Here are the critical functions:
 * 
 * @see DendrogramCanvas#updateSelectionSelect()
 * @see DendrogramCanvas#updateSelectionDeselect()
 * @see DendrogramViewNode#isSelected()
 * @see DendrogramViewNode#invalidateSelection()
 * 
 * @author Philipp Lewe
 * @author Philipp Kopp
 * @author Till Schäfer
 */
public class DendrogramView extends
        GenericView<DendrogramViewInstanceConfig, DendrogramViewClassConfig, DendrogramViewState> {
    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(DendrogramView.class);

    private SVGCache svgCache;

    /**
     * the main panel thats hold all visible elements of this view
     */
    private JPanel viewContainer = new JPanel(new BorderLayout());

    private JSplitPane splitView = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private JPanel dendrogramCanvasAndClusteringSettings = new JPanel(new BorderLayout());
    JScrollPane scrollPane = new JScrollPane();

    private SideBarItem magnifymapItem;
    private SideBarItem infoPanelItem;
    private SideBarItem methodInfoPanelItem;
    private SideBarItem startClusteringItem;
    private SideBarItem tableDetailView;

    private JToolBar toolbar = new JToolBar();

    private DendrogramCanvas canvas;
    private TooltipEventHandler tooltipEventHandler;
    private DendrogramEventHandler eventHandler;
    private TooltipManager tooltipManager;
    private HierarchicalClusterNode<Molecule> model;
    private ViewComponent table;
    private JPanel tablePanel;

    private boolean tableShown = false;
    private int dividerLocation = 0;

    private JMenu menu;
    private JMenu tableMenu;
    private Action switchTableAction;
    private Action zoomInVerticalAction;
    private Action zoomOutVerticalAction;
    private Action zoomInHorizontalAction;
    private Action zoomOutHorizontalAction;
    private Action zoomToFitAction;
    private Action zoomToSelectionAction;

    private ComponentListener canvasSizeListener;

    private Collection<String> chosenPropDefs = new ArrayList<String>();
    private Subset subset;
    private Linkages linkage = null;
    private Distances distance = null;
    private NNSearchs nnSearch = ClusteringController.defaultNNSearchs(true);
    private NNSearchParameters nnSearchParameters;

    private boolean clusteringInProgress = false;
    private final ClusteringController clusteringController;
    private DendrogramClusteringSettingsSave settingSave = new DendrogramClusteringSettingsSave();

    private ClusteringProgressBar<Molecule> clusteringProgressBar = new ClusteringProgressBar<Molecule>();

    /*
     * StartClustering SideBarItem parts
     */
    private AbstractAction startClusteringAction = new StartClusteringAction();
    private JButton startClusteringButton = new JButton(startClusteringAction);
    private JPanel startClusteringSidebarPanel = new JPanel();
    JLabel subsetLabel = new JLabel("<html>" + _("DendrogramView.ClusteringChooser.SubsetChanged") + "</html>");

    /**
     * Creates a new Dendrogram View
     * 
     * @param session
     * @param subset
     * @param instanceConfig
     * @param classConfig
     * @param globalConfig
     * @param viewState
     */
    public DendrogramView(GUISession session, Subset subset, ViewInstanceConfig instanceConfig,
            ViewClassConfig classConfig, GlobalConfig globalConfig, ViewState viewState) {
        super(session, subset, instanceConfig, classConfig, globalConfig, viewState);
        Preconditions.checkArgument(viewState.getClass() == DendrogramViewState.class);

        this.subset = subset;

        svgCache = new SVGCache(getDbManager());
        clusteringController = new ClusteringController(getDbManager(), subset.getSession(), this);

        initView((DendrogramViewState) viewState);
    }

    /**
     * Changes the used clustering to model
     * 
     * @param model
     *            the root {@link HierarchicalClusterNode} from the clustering
     * @param subset
     *            the used {@link Subset}
     */
    public void changeModel(HierarchicalClusterNode<Molecule> model, Subset subset) {
        setSubset(subset);

        // cleanup
        model.sortById();
        if (eventHandler != null) {
            removePropertyChangeListener(GLOBAL_CONFIG_PROPERTY, tooltipEventHandler);
            canvas.removeInputEventListener(tooltipEventHandler);
            canvas.removeInputEventListener(eventHandler);
        }
        if (canvas != null) {
            getBannerPool().removeBannerChangeListener(canvas);
        }
        if (table != null) {
            table.destroy();
        }
        if (tableMenu != null) {
            menu.remove(tableMenu);
        }
        if (canvasSizeListener != null) {
            dendrogramCanvasAndClusteringSettings.removeComponentListener(canvasSizeListener);
        }

        // end cleanup
        this.model = model;

        List<Molecule> list = getModelList(model);
        table = new ViewComponent(getDbManager(), subset, list, getBannerPool(), null);
        table.setSelection(getSelection());
        table.setPreferredSize(new Dimension(500, 450));
        // TODO share SVG Cache with table

        canvas = new DendrogramCanvas(svgCache, model, getSelection(), table, getGlobalConfig(), getBannerPool(),
                subset);
        tooltipManager = new TooltipManager(getDbManager(), svgCache, getSubset().getSession().getProfile(),
                getGlobalConfig());
        addPropertyChangeListener(GLOBAL_CONFIG_PROPERTY, tooltipManager);

        addPropertyChangeListener(GLOBAL_CONFIG_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                getGlobalConfig().getRenderingQuality().setQuality(canvas);
                canvas.setGlobalConfig(getGlobalConfig());
            }
        });
        canvasSizeListener = new ComponentListener() {

            @Override
            public void componentResized(ComponentEvent e) {
                canvas.adaptToParentSize();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentShown(ComponentEvent e) {
            }

        };
        dendrogramCanvasAndClusteringSettings.addComponentListener(canvasSizeListener);
        
        tooltipEventHandler = new TooltipEventHandler(canvas, tooltipManager, getGlobalConfig());
        addPropertyChangeListener(GLOBAL_CONFIG_PROPERTY, tooltipEventHandler);
        canvas.addInputEventListener(tooltipEventHandler);
        
        eventHandler = new DendrogramEventHandler(canvas, getSelection(), getGlobalConfig());
        canvas.addInputEventListener(eventHandler);

        scrollPane.setViewportView(canvas);

        tablePanel = new JPanel();
        tablePanel.setLayout(new BorderLayout());
        tablePanel.add(table, BorderLayout.CENTER);
        tablePanel.add(table.getToolBar(), BorderLayout.PAGE_START);

        splitView.setBottomComponent(tablePanel);
        switchTable();
        switchTable();
        dividerLocation = getComponent().getHeight() / 2;
        switchTableAction.setEnabled(true);

        tableMenu = table.getMenu();
        menu.add(tableMenu);
        setZoomActionsEnabled(true);

        installSidebarItems(canvas);

        firePropertyChange(CONTENT_PROPERTY, null, null);

        canvas.zoomToOverview();
    }

    /**
     * Changes the used clustering to model and calls setUsedClusterSettings
     * 
     * @param result
     *            the result of the hierarchical clustering (root
     *            {@link HierarchicalClusterNode} of the dendrogramm)
     * @param subset
     *            the used subset
     * @param propDefs
     *            the used {@link PropertyDefinition}s
     * @param linkage
     *            the used {@link Linkage}
     * @param distance
     *            the used {@link Distance}
     * @param nnSearch
     *            the used {@link NNSearch} strategy
     * @param nnSearchParameters
     *            the used {@link NNSearchParameters}
     */
    public void changeModel(HierarchicalClusterNode<Molecule> result, Subset subset,
            Collection<PropertyDefinition> propDefs, Linkages linkage, Distances distance, NNSearchs nnSearch,
            NNSearchParameters nnSearchParameters) {
        setClusterSettings(propDefs, linkage, distance, nnSearch, nnSearchParameters, subset);
        changeModel(result, subset);
    }

    /**
     * May only be used on NumValues
     * 
     * @param type
     *            the NumProperty type
     * @param root
     *            the sub-/tree to sort
     * @return the lowest value of chosen type
     */
    public double sortTree(PropertyDefinition type, HierarchicalClusterNode<Molecule> root) {
        if (type.isStringProperty()) {
            throw new IllegalArgumentException(this.getClass().toString() + ": only Num Properties supported");
        }
        if (root.isLeaf()) {
            return root.getContent().getNumPropertyValue(type);
        } else {
            double rightProp = sortTree(type, root.getRightChild());
            double leftProp = sortTree(type, root.getLeftChild());
            if (rightProp > leftProp) {
                return rightProp;
            } else {
                root.switchChildren();
                return leftProp;
            }
        }

    }

    /**
     * @return return true if the table is shown, false otherwise
     */
    public boolean isTableShown() {
        return tableShown;
    }

    /**
     * Informs the {@link DendrogramView} that a new clustering is started
     * 
     * @param worker
     *            the dView will add itself as a ProgressListener to the worker
     */
    public void clusteringStarted(ClusteringWorker<Molecule> worker) {
        Preconditions.checkArgument(!clusteringInProgress);

        clusteringInProgress = true;
        startClusteringAction.setEnabled(false);

        // create the progress panel and show it
        ProgressPanel<HierarchicalClusterNode<Molecule>> panel = new ProgressPanel<HierarchicalClusterNode<Molecule>>(
                "Clustering Progress");
        worker.addProgressListener(panel);
        showProgress(panel, worker);
    }

    /**
     * Informs the {@link DendrogramView} that the Clustering has finished
     */
    public void clusteringFinished() {
        Preconditions.checkArgument(clusteringInProgress);

        clusteringInProgress = false;
        startClusteringAction.setEnabled(true);

        hideProgress();
        disposeStartClusteringPanel();
        startClusteringSidebarPanelState(false);
    }

    /**
     * @return clustering in progress?
     */
    public boolean isClusteringInProgress() {
        return clusteringInProgress;
    }

    /**
     * @return the clusteringChooser
     */
    public ClusteringProgressBar<Molecule> getClusteringProgressBar() {
        return clusteringProgressBar;
    }

    @Override
    public JMenu getMenu() {
        return menu;
    }

    @Override
    public JComponent getComponent() {
        return viewContainer;
    }

    @Override
    public List<SideBarItem> getSideBarItems() {
        return Arrays.asList(startClusteringItem, magnifymapItem, infoPanelItem, methodInfoPanelItem, tableDetailView);
    }

    @Override
    public JToolBar getToolBar() {
        return toolbar;
    }

    @Override
    public void destroy() {
        if (canvas != null) {
            canvas.destroy();
        }
        if (table != null) {
            table.destroy();
        }
    }

    @Override
    public DendrogramViewState getState() {
        DendrogramViewState state = new DendrogramViewState();
        if (canvas != null) {
            state.setValid(false);
            state.setTree(model);
            state.setSelectionbarPosition(canvas.getClusterBarPosition());
            state.setVerticalZoomFactor(canvas.getYZoom());
            state.setHorizontalZoomFactor(canvas.getXZoom());
            state.setScrollPosition(canvas.getVisibleRect());
            state.setTableExpanded(tableShown);
            state.setTablePosition(splitView.getDividerLocation());
            state.setPropDefs(chosenPropDefs);
            state.setLinkage(linkage);
            state.setDistance(distance);
            state.setNnSearch(nnSearch);
            state.setNnSearchParameters(nnSearchParameters);
            state.setCanvasSize(canvas.getLayer().getFullBoundsReference());
            state.setSettingSave(settingSave);
            state.setValid(true);
        }
        return state;
    }

    /**
     * Shows the StartClusteringPanel which allows to set the parameters for a
     * new clustering and start it.
     */
    void showStartClusteringPanel() {
        // clear view
        viewContainer.removeAll();

        /*
         * add the startClusteringPanel to the view
         */
        // map saved propertyDefinition keys to real Objects
        LinkedList<PropertyDefinition> selectedPropDefs = Lists.newLinkedList();
        for (String key : chosenPropDefs) {
            selectedPropDefs.add(subset.getSession().getDataset().getPropertyDefinitions().get(key));
        }
        StartClusteringPanel startPanel = new StartClusteringPanel(clusteringController,
                ClusteringController.isExact(nnSearch), linkage, distance, selectedPropDefs,
                ClusteringController.getQuality(nnSearchParameters),
                ClusteringController.getDimensionality(nnSearchParameters));
        JScrollPane scrollPane = new JScrollPane(startPanel);
        scrollPane.setPreferredSize(new Dimension(20, 20));
        viewContainer.add(scrollPane);

        actionsEnabled(false);

        viewContainer.repaint();
    }

    /**
     * Disposes the StartClusteringPanel and shows the
     * dendrogramCanvasAndClusteringSettings again
     */
    void disposeStartClusteringPanel() {
        viewContainer.removeAll();
        viewContainer.add(dendrogramCanvasAndClusteringSettings);

        viewContainer.repaint();

        // if table was shown restore it
        if (isTableShown()) {
            setTableShown(false);
            switchTable();
        }

        actionsEnabled(true);
    }

    /**
     * Enables or disables all actions (i.e. menu entries and toolbar).
     * 
     * @param enable
     */
    private void actionsEnabled(boolean enable) {
        switchTableAction.setEnabled(enable);
        zoomInVerticalAction.setEnabled(enable);
        zoomOutVerticalAction.setEnabled(enable);
        zoomInHorizontalAction.setEnabled(enable);
        zoomOutHorizontalAction.setEnabled(enable);
        zoomToFitAction.setEnabled(enable);
        zoomToSelectionAction.setEnabled(enable);
    }

    /**
     * Sets the used clustering settings to show them in the sidebar.
     * 
     * @param propDefs
     * @param linkageName
     * @param distance
     * @param subset
     */
    private void setClusterSettings(Collection<PropertyDefinition> propDefs, Linkages linkage, Distances distance,
            NNSearchs nnSearch, NNSearchParameters nnSearchParameters, Subset subset) {
        chosenPropDefs = new ArrayList<String>();
        for (PropertyDefinition propertyDefinition : propDefs) {
            chosenPropDefs.add(propertyDefinition.getKey());
        }
        this.subset = subset;
        this.linkage = linkage;
        this.distance = distance;
        this.nnSearchParameters = nnSearchParameters;
        this.nnSearch = nnSearch;
    }

    /**
     * Initialisation of GUI
     * 
     * @param state
     */
    private void initView(DendrogramViewState state) {

        /*
         * Add Listener to ourself. No need to unregister them
         */
        addPropertyChangeListener(GenericView.SUBSET_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                startClusteringSidebarPanelState(true);
            }
        });

        scrollPane.setVerticalScrollBarPolicy(PScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setHorizontalScrollBarPolicy(PScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(400, 400));

        dendrogramCanvasAndClusteringSettings.add(scrollPane, BorderLayout.CENTER);
        dendrogramCanvasAndClusteringSettings.setPreferredSize(new Dimension(500, 450));
        dendrogramCanvasAndClusteringSettings.add(clusteringProgressBar.getComponent(), BorderLayout.NORTH);
        dendrogramCanvasAndClusteringSettings.addComponentListener(new windowSizeListener(clusteringProgressBar
                .getComponent()));
        viewContainer.add(dendrogramCanvasAndClusteringSettings, BorderLayout.CENTER);
        viewContainer.validate();

        splitView.setContinuousLayout(true);
        splitView.setBorder(null);

        setupToolbar();

        // Construct StartClustering SideBarItem
        startClusteringSidebarPanel.setLayout(new BoxLayout(startClusteringSidebarPanel, BoxLayout.Y_AXIS));
        startClusteringSidebarPanel.add(startClusteringButton);
        startClusteringSidebarPanel.add(subsetLabel);
        startClusteringItem = new SideBarItem(I18n.get("DendrogramView.Sidebar.Clustering"),
                Resources.getIcon("gears.png"), startClusteringSidebarPanel, true);
        startClusteringAction.setEnabled(true);

        // other SideBarItems
        methodInfoPanelItem = new SideBarItem(I18n.get("DendrogramView.Sidebar.Method"), null, null);
        infoPanelItem = new SideBarItem(I18n.get("DendrogramView.Sidebar.Info"), null, null);
        magnifymapItem = new SideBarItem(I18n.get("DendrogramView.Sidebar.Zoom"), Resources.getIcon("zoom.png"), null);
        tableDetailView = new SideBarItem(I18n.get("DendrogramView.Sidebar.Tabledetail"),
                Resources.getIcon("zoom.png"), null);

        if (state.isValid()) {
            setState(state);
        }
        canvasSizeListener = null;
    }

    private void installSidebarItems(DendrogramCanvas canvas) {

        ClusterMethodInfoPanel methodInfoPanel;
        if (!chosenPropDefs.isEmpty() && subset != null && linkage != null && distance != null && nnSearch != null
                && nnSearchParameters != null) {
            methodInfoPanel = new ClusterMethodInfoPanel(chosenPropDefs, subset, linkage, distance, nnSearch,
                    nnSearchParameters);
            methodInfoPanelItem.setComponent(methodInfoPanel);
        }

        DendrogramClusterInfoPanel infoPanel = new DendrogramClusterInfoPanel(canvas);
        infoPanel.setPreferredSize(new Dimension(160, 160));
        infoPanelItem.setComponent(infoPanel);

        JPanel nodeZoom = new DendrogramNodeZoomPanel(canvas, svgCache);
        nodeZoom.setPreferredSize(new Dimension(160, 160));
        magnifymapItem.setComponent(nodeZoom);

    }

    /**
     * Sets the {@link DendrogramView} to a {@link DendrogramViewState} (i.e.
     * restoring session)
     * 
     * @param state
     */
    private void setState(DendrogramViewState state) {
        changeModel(state.getTree(subset), subset);
        splitView.setDividerLocation(state.getTablePosition());
        dividerLocation = state.getTablePosition();
        if (state.isTableExpanded()) {
            tableShown = false;
            switchTable();
        }
        canvas.setSelectionbarPosition(state.getSelectionbarPosition());

        canvas.setXZoom(state.getHorizontalZoomFactor());
        canvas.setYZoom(state.getVerticalZoomFactor());

        final Rectangle visRect = state.getScrollPosition();

        canvas.scrollRectToVisible(new Rectangle(500, 0, 10, 10));

        chosenPropDefs = state.getPropDefs();
        linkage = state.getLinkage();
        distance = state.getDistance();
        nnSearch = state.getNnSearch();
        nnSearchParameters = state.getNnSearchParameters();

        ClusterMethodInfoPanel methodInfoPanel;
        methodInfoPanel = new ClusterMethodInfoPanel(chosenPropDefs, subset, linkage, distance, nnSearch,
                nnSearchParameters);
        methodInfoPanelItem.setComponent(methodInfoPanel);
        settingSave = state.getSettingSave();

        // TODO: Find another way (event based) to restore the scroll position,
        // as this one is just a stopgap solution

        Timer t = new Timer(1000, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.scrollRectToVisible(visRect);
            }
        });

        t.setRepeats(false);
        t.start();
    }

    private void setupToolbar() {
        switchTableAction = new SwitchTableAction();
        zoomInVerticalAction = new ZoomInVerticalAction();
        zoomOutVerticalAction = new ZoomOutVerticalAction();
        zoomInHorizontalAction = new ZoomInHorizontalAction();
        zoomOutHorizontalAction = new ZoomOutHorizontalAction();
        zoomToFitAction = new ZoomToFitAction();
        zoomToSelectionAction = new ZoomToSelectionAction();
        setZoomActionsEnabled(false);

        toolbar.add(switchTableAction);
        toolbar.add(zoomInVerticalAction);
        toolbar.add(zoomOutVerticalAction);
        toolbar.add(zoomInHorizontalAction);
        toolbar.add(zoomOutHorizontalAction);
        toolbar.add(zoomToFitAction);
        toolbar.add(zoomToSelectionAction);

        menu = new JMenu(I18n.get("DendrogramView.Menu"));
        menu.add(startClusteringAction);
        menu.addSeparator();
        menu.add(switchTableAction);
        menu.addSeparator();
        menu.add(zoomInVerticalAction);
        menu.add(zoomOutVerticalAction);
        menu.add(zoomInHorizontalAction);
        menu.add(zoomOutHorizontalAction);
        menu.add(zoomToFitAction);
        menu.add(zoomToSelectionAction);
        menu.addSeparator();
    }

    private void setZoomActionsEnabled(boolean b) {
        zoomInVerticalAction.setEnabled(b);
        zoomOutVerticalAction.setEnabled(b);
        zoomInHorizontalAction.setEnabled(b);
        zoomOutHorizontalAction.setEnabled(b);
        zoomToFitAction.setEnabled(b);
        zoomToSelectionAction.setEnabled(b);
    }

    /**
     * @return model leafs in list form
     */
    private <S extends Structure> ArrayList<Molecule> getModelList(HierarchicalClusterNode<S> root) {
        ArrayList<Molecule> list = new ArrayList<Molecule>();

        if (root.isLeaf()) {
            list.add((Molecule) root.getContent());
            return list;
        } else {
            list.addAll(getModelList(root.getLeftChild()));
            list.addAll(getModelList(root.getRightChild()));
        }
        return list;
    }

    /**
     * @param isTableShown
     */
    private void setTableShown(boolean isTableShown) {
        tableShown = isTableShown;
    }

    /**
     * Toggle the visibility of the table
     */
    private void switchTable() {
        if (isTableShown()) {
            // note old location
            dividerLocation = splitView.getDividerLocation();

            viewContainer.remove(splitView);
            splitView.remove(dendrogramCanvasAndClusteringSettings);
            viewContainer.add(dendrogramCanvasAndClusteringSettings, BorderLayout.CENTER);
            splitView.validate();
            viewContainer.validate();
            setTableShown(false);
            tableDetailView.setComponent(null);
            firePropertyChange(CONTENT_PROPERTY, null, null);
        } else {
            viewContainer.remove(dendrogramCanvasAndClusteringSettings);
            viewContainer.add(splitView, BorderLayout.CENTER);
            splitView.setTopComponent(dendrogramCanvasAndClusteringSettings);
            splitView.setDividerLocation(dividerLocation);
            splitView.validate();
            viewContainer.validate();
            canvas.zoomToOverview();
            setTableShown(true);
            List<SideBarItem> tableSidebar = table.getSideBar();
            for (SideBarItem sideBarItem : tableSidebar) {

                if (sideBarItem.getComponent() instanceof CellZoom) {
                    tableDetailView.setComponent(sideBarItem.getComponent());
                }
            }
            firePropertyChange(CONTENT_PROPERTY, null, null);
        }
    }

    /**
     * hide the progressbar
     */
    private void hideProgress() {
        clusteringProgressBar.hideProgress();
    }

    /**
     * show the progress bar
     * 
     * @param progressPanel
     * @param worker
     */
    private void showProgress(ProgressPanel<HierarchicalClusterNode<Molecule>> progressPanel,
            ClusteringWorker<Molecule> worker) {
        clusteringProgressBar.showProgress(progressPanel, worker);
    }

    /**
     * If a subset has changed, the startClusteringButton becomes highlighted
     * and there is a label in the side bar indicating that one may want to
     * start a new clustering
     * 
     * @param subsetChanged
     *            set this to true if the subset has changed and
     */
    private void startClusteringSidebarPanelState(boolean subsetChanged) {
        if (subsetChanged) {
            // Button and menu item becomes red
            startClusteringAction.putValue(AbstractAction.NAME,
                    "<html><b><font color=\"red\">" + I18n.get("DendrogramView.ClusteringChooser.StartClustering")
                            + "</font></b></html>");
            subsetLabel.setVisible(true);
        } else {
            // Button and menu item becomes black
            startClusteringAction.putValue(AbstractAction.NAME,
                    "<html>" + I18n.get("DendrogramView.ClusteringChooser.StartClustering"));
            subsetLabel.setVisible(false);
        }
    }

    // Actions
    private class ZoomInVerticalAction extends AbstractAction {
        public ZoomInVerticalAction() {
            super(I18n.get("Menu.Edit.Zoomin.Vertical"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomin.Vertical"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-in-vertical.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PLUS, InputEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (canvas != null)
                canvas.zoomInVertical(canvas.getViewportCenter());
        }
    }

    private class ZoomOutVerticalAction extends AbstractAction {
        public ZoomOutVerticalAction() {
            super(I18n.get("Menu.Edit.Zoomout.Vertical"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomout.Vertical"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-out-vertical.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (canvas != null)
                canvas.zoomOutVertical(canvas.getViewportCenter());
        }
    }

    private class ZoomInHorizontalAction extends AbstractAction {
        public ZoomInHorizontalAction() {
            super(I18n.get("Menu.Edit.Zoomin.Horizontal"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomin.Horizontal"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-in-horizontal.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (canvas != null)
                canvas.zoomInHorizontal(canvas.getViewportCenter());
        }
    }

    private class ZoomOutHorizontalAction extends AbstractAction {
        public ZoomOutHorizontalAction() {
            super(I18n.get("Menu.Edit.Zoomout.Horizontal"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomout.Horizontal"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-out-horizontal.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('-'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (canvas != null)
                canvas.zoomOutHorizontal(canvas.getViewportCenter());
        }
    }

    private class ZoomToFitAction extends AbstractAction {
        public ZoomToFitAction() {
            super(I18n.get("Menu.Edit.ZoomToFit"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.ZoomToFit"));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-best-fit.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-best-fit.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('0'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (canvas != null)
                canvas.zoomToOverview();
        }
    }

    private class ZoomToSelectionAction extends AbstractAction {
        public ZoomToSelectionAction() {
            super(I18n.get("Menu.Edit.ZoomToSelection"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.ZoomToSelection"));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-fit-selection.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-fit-selection.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('s'));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (canvas != null) {
                canvas.zoomAndfocusOnSelectedNodes();

                if (isTableShown()) {
                    table.zoomToSelection();
                }
            }
        }
    }

    private class SwitchTableAction extends AbstractAction {
        public SwitchTableAction() {
            super(I18n.get("DendrogramView.Menu.SwitchTable"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("DendrogramView.Menu.SwitchTable"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("table.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (canvas != null)
                switchTable();
        }
    }

    private class windowSizeListener implements ComponentListener {

        private JPanel component;

        /**
         * @param component
         */
        public windowSizeListener(JPanel component) {
            this.component = component;
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            int w = (int) component.getPreferredSize().getWidth();
            int h = clusteringProgressBar.getOptimumHeight(e.getComponent().getWidth());
            component.setPreferredSize(new Dimension(w, h));
            if (h == 0) {
                component.setVisible(false);
            } else {
                component.setVisible(true);
            }
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

    }

    private class StartClusteringAction extends AbstractAction {
        StartClusteringAction() {
            super("<html><b><font color=\"red\">" + I18n.get("DendrogramView.ClusteringChooser.StartClustering")
                    + "</font></b></html>");
            putValue(Action.SMALL_ICON, Resources.getIcon("gears.png"));
            putValue(Action.SHORT_DESCRIPTION, "<html>" + I18n.get("DendrogramView.ClusteringChooser.StartClustering")
                    + "</html>");
            setEnabled(false);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            startClusteringSidebarPanelState(false);

            showStartClusteringPanel();
        }
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.View#focusMolecule(edu.udo.scaffoldhunter.model.db.Molecule)
     */
    @Override
    public void focusMolecule(Molecule molecule) {
        if (!subset.contains(molecule)) {
            return;
        }
        if (canvas != null) {
            canvas.focusMolecule(molecule);

            if (isTableShown()) {
                table.focusMolecule(molecule);
            }
        }
    }

}
