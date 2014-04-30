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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.Ordering;

import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.VisualFeature;
import edu.udo.scaffoldhunter.model.db.Banner;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Orderings;
import edu.udo.scaffoldhunter.util.ProgressAdapter;
import edu.udo.scaffoldhunter.util.ProgressListener;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.GenericView;
import edu.udo.scaffoldhunter.view.SideBarItem;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.BalloonLayoutAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.DecreaseRadiusAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.ExpandAllNodesAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.ExpandToDefaultLevelAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.ExportCanvasAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.FixRadiiAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.IncreaseRadiusAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.LinearLayoutAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.MaximizeNodeAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.MaximizeSelectedNodesAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.MinimizeNodeAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.MinimizeSelectedNodesAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.NormalizeAllNodesAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.NormalizeNodeAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.NormalizeSelectedNodesAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.RadialLayoutAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.ResetRadiusAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.ToggleDetailsNodesAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.ZoomComboAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.ZoomToFitAction;
import edu.udo.scaffoldhunter.view.scaffoldtree.TreeViewActions.ZoomToSelection;
import edu.udo.scaffoldhunter.view.scaffoldtree.config.MappingDialog;
import edu.udo.scaffoldhunter.view.scaffoldtree.config.SortChooser;
import edu.udo.scaffoldhunter.view.util.MiniMap;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.TooltipEventHandler;
import edu.udo.scaffoldhunter.view.util.TooltipManager;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolox.swing.PScrollPane;

/**
 * A view showing the scaffold tree.
 * 
 * @author Bernhard Dick
 * @author Henning Garus
 * 
 */
public class ScaffoldTreeView extends
        GenericView<ScaffoldTreeViewConfig, ScaffoldTreeViewClassConfig, ScaffoldTreeViewState> {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory.getLogger(ScaffoldTreeView.class);

    private final VCanvas vcanvas;
    private final PScrollPane scrollPane;

    // Toolbar, Menu and Elements
    private JToolBar toolbar;
    private JMenu menu;
    private JMenu layoutMenu;
    private Action exportAction;
    //
    private Action radialLayoutAction;
    private Action balloonLayoutAction;
    private Action linearLayoutAction;
    private Action zoomInAction;
    private JComboBox<String> zoomCombo;
    private Action zoomToFitAction;
    private Action zoomToSelection;
    private Action zoomOutAction;
    private Action expandAllNodesAction;
    private Action expandToDefaultLevelAction;
    //
    private Action increaseRadiusAction;
    private Action decreaseRadiusAction;
    private Action fixRadiiAction;
    private Action resetRadiusAction;
    private JToggleButton fixRadiiToolbarButton;
    private JCheckBoxMenuItem fixRadiiMenuItem;
    //
    private Action maximizeNodeAction;
    private Action minimizeNodeAction;
    private Action normalizeNodeAction;
    //
    private Action maximizeSelectedNodesAction;
    private Action minimizeSelectedNodesAction;
    private Action normalizeSelectedNodesAction;
    private Action normalizeAllNodesAction;

    private Action setMappingsAction;
    private Action toggleDetailsAction;

    // Sidebar
    private Vector<SideBarItem> sidebarComponents = new Vector<SideBarItem>();
    private MiniMap minimap;
    private VMagnifyingMap magnifymap;

    private ZoomListener zoomlistener;
    private final BannerListener bannerListener;

    private final Mappings mappings;
    private final Sorting sorting;
    
    private VEventHandler eventhandler;
    private TooltipEventHandler tooltipEventHandler;

    /**
     * Create a new view showing a scaffold tree.
     * 
     * @param session
     *            the GUI session
     * @param subset
     *            the subset of molecules to be associated with the trees
     *            scaffolds
     * @param instanceConfig
     *            the configuration object for this tree view instance
     * @param classConfig
     *            the configuration object shared by all instances of tree view
     * @param globalConfig
     *            the global config object
     * @param viewState
     *            the object which holds the state of this tree view, used for
     *            saving and restoring sessions
     */
    public ScaffoldTreeView(GUISession session, Subset subset, ViewInstanceConfig instanceConfig,
            ViewClassConfig classConfig, GlobalConfig globalConfig, ViewState viewState) {
        super(session, subset, instanceConfig, classConfig, globalConfig, viewState);

        SVGCache svgCache = new SVGCache(getDbManager());
        vcanvas = new VCanvas(svgCache, getSelection(), getBannerPool(), getGlobalConfig(), getState());
        scrollPane = new PScrollPane(vcanvas);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        
        scrollPane.addComponentListener(new VCanvasResizeListener(vcanvas));
        
        // TODO: Used for possible hotkey fix
//        scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_DOWN_MASK), "none");
//        scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_DOWN_MASK), "none");
        
        vcanvas.getVTree().addVNodeListener(getState());
        vcanvas.getVTree().setShowScaffoldDetailsNodes(getState().isShowDetailsNodes());
        sortMoleculesBy(getInstanceConfig().getMoleculeOrderProperty(getSubset().getSession().getDataset()));
        bannerListener = new BannerListener(vcanvas);
        getBannerPool().addBannerChangeListener(bannerListener);
                        
        sorting = new Sorting(getDbManager(), getSubset().getSession().getDataset(), vcanvas, getInstanceConfig()
                .getSortSettings(), getInstanceConfig().getSortState());
        getInstanceConfig().setSortState(sorting.getSortState());
        vcanvas.setSorting(sorting);
        setupSidebar();
        minimap.connect(vcanvas);
        magnifymap.connect(vcanvas);

        mappings = new Mappings(this, vcanvas, subset);
        addPropertyChangeListener(INSTANCE_CONFIG_PROPERTY, mappings);
        
        TooltipManager tooltipManager = new TooltipManager(getDbManager(), svgCache, subset.getSession().getProfile(),
                getGlobalConfig());
        addPropertyChangeListener(View.GLOBAL_CONFIG_PROPERTY, tooltipManager);
        
        tooltipEventHandler = new TooltipEventHandler(vcanvas, tooltipManager, getGlobalConfig());
        addPropertyChangeListener(GLOBAL_CONFIG_PROPERTY, tooltipEventHandler);
        
        
        eventhandler = new VEventHandler(vcanvas, tooltipEventHandler.getTooltipTimer(), getSelection(), getGlobalConfig(), getBannerPool(),
                getSubsetManager(), getSubset());
        // vcanvas, tooltipManager, getSelection(), getGlobalConfig(),
        // getBannerPool(), getSubsetManager(), getSubset());
        addPropertyChangeListener(SUBSET_PROPERTY, eventhandler);
        addPropertyChangeListener(SUBSET_PROPERTY, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                rebuildTree();
            }
        });
        initialize();

        showTree();
    }

    /**
     * asynchronously shows the tree of the current subset in the vcanvas
     */
    private void showTree() {
        final ProgressWorker<Scaffold, Void> rootWorker = new ProgressWorker<Scaffold, Void>() {
            @Override
            protected Scaffold doInBackground() throws Exception {
                setProgressIndeterminate(true);

                return DBExceptionHandler.callDBManager(getDbManager(), new DBFunction<Scaffold>() {
                    @Override
                    public Scaffold call() throws DatabaseException {
                        return getDbManager().getScaffolds(getSubset(), getClassConfig().isCutTreeStem());
                    }
                });
            }
        };
        rootWorker.addProgressListener(new ProgressListener<Scaffold>() {
            @Override
            public void setProgressValue(int progress) {
            }

            @Override
            public void setProgressIndeterminate(boolean indeterminate) {
                eventhandler.setBlocked(true);
                vcanvas.enableZoom(false);
                vcanvas.setCursor(new Cursor(Cursor.WAIT_CURSOR));
            }

            @Override
            public void setProgressBounds(int min, int max) {
            }

            @Override
            public void finished(Scaffold result, boolean cancelled) {

                if (result != null) {
                    for (VisualFeature f : VisualFeature.values())
                        mappings.disableMapping(f);
                    ScaffoldTreeViewState state = getState();
                    if (state.isNewState()) {
                        vcanvas.showTree(getSubset().getSession().getTree(), result, 
                                getClassConfig().getInitiallyOpenRings());
                    } else {
                        vcanvas.showTree(getSubset().getSession().getTree(), result, state);
                    }
                    mappings.addMappings(getInstanceConfig().getMappings());
                    if (state.getCameraTransform() != null) {
                        vcanvas.getCamera().setViewTransform(state.getCameraTransform());
                        vcanvas.updateLayout();
                    } else {
                        // zoom to overview
                        // invoke later to allow the gui to initialize first
                        SwingUtilities.invokeLater(new Runnable() { 
                            @Override
                            public void run() {
                                vcanvas.zoomToOverview(true);
                            }
                        });
                    }
                    state.setCameraTransform(vcanvas.getCamera().getViewTransformReference());
                }
                sorting.sortTree(getSubset(), new ProgressAdapter<Void>());
                eventhandler.setBlocked(false);
                vcanvas.enableZoom(true);
                vcanvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        rootWorker.execute();
    }

    private void initialize() {
        // Actions for toolbar and menu
        exportAction = new ExportCanvasAction(vcanvas);
        radialLayoutAction = new RadialLayoutAction(this);
        balloonLayoutAction = new BalloonLayoutAction(this);
        linearLayoutAction = new LinearLayoutAction(this);
        zoomInAction = new TreeViewActions.ZoomInAction(vcanvas);
        String[] zoomFactor = { "10%", "20%", "30%", "40%", "50%", "60%", "70%", "80%", "90%", "100%" };
        zoomCombo = new JComboBox<String>(zoomFactor);
        zoomCombo.setEditable(true);
        zoomCombo.setAction(new ZoomComboAction(vcanvas));
        zoomCombo.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                switch (e.getKeyChar()) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '%':
                    break;
                default:
                    e.consume();
                }
            }
        });
        zoomCombo.getEditor().setItem(vcanvas.getZoomFactor() + "%");
        zoomOutAction = new TreeViewActions.ZoomOutAction(vcanvas);
        zoomToFitAction = new ZoomToFitAction(vcanvas);
        zoomToSelection = new ZoomToSelection(vcanvas);
        expandAllNodesAction = new ExpandAllNodesAction(vcanvas);
        expandToDefaultLevelAction = new ExpandToDefaultLevelAction(vcanvas, getClassConfig());
        increaseRadiusAction = new IncreaseRadiusAction(vcanvas);
        decreaseRadiusAction = new DecreaseRadiusAction(vcanvas);
        resetRadiusAction = new ResetRadiusAction(vcanvas);
        fixRadiiAction = new FixRadiiAction(this);
        fixRadiiToolbarButton = new JToggleButton(fixRadiiAction);
        fixRadiiToolbarButton.setText("");
        fixRadiiMenuItem = new JCheckBoxMenuItem(fixRadiiAction);
        maximizeNodeAction = new MaximizeNodeAction(vcanvas);
        minimizeNodeAction = new MinimizeNodeAction(vcanvas);
        normalizeNodeAction = new NormalizeNodeAction(vcanvas);
        maximizeSelectedNodesAction = new MaximizeSelectedNodesAction(vcanvas);
        minimizeSelectedNodesAction = new MinimizeSelectedNodesAction(vcanvas);
        normalizeSelectedNodesAction = new NormalizeSelectedNodesAction(vcanvas);
        normalizeAllNodesAction = new NormalizeAllNodesAction(vcanvas);
        setMappingsAction = new SetMappingsAction();
        toggleDetailsAction = new ToggleDetailsNodesAction(vcanvas, getState());

        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(zoomInAction);
        toolbar.add(zoomCombo);
        toolbar.add(zoomOutAction);
        toolbar.add(zoomToFitAction);
        toolbar.add(zoomToSelection);
        toolbar.addSeparator();
        toolbar.add(expandAllNodesAction);
        toolbar.add(expandToDefaultLevelAction);
        toolbar.addSeparator();
        toolbar.add(increaseRadiusAction);
        toolbar.add(decreaseRadiusAction);
        toolbar.add(fixRadiiToolbarButton);
        toolbar.add(resetRadiusAction);
        toolbar.addSeparator();
        toolbar.add(maximizeNodeAction);
        toolbar.add(minimizeNodeAction);
        toolbar.add(normalizeNodeAction);
        toolbar.addSeparator();
        toolbar.add(maximizeSelectedNodesAction);
        toolbar.add(minimizeSelectedNodesAction);
        toolbar.add(normalizeSelectedNodesAction);
        toolbar.add(normalizeAllNodesAction);
        toolbar.addSeparator();
        toolbar.add(setMappingsAction);
        JToggleButton toggleDetailsButton = new JToggleButton(toggleDetailsAction);
        toggleDetailsButton.setText("");
        toolbar.add(toggleDetailsButton);
        toolbar.add(exportAction);

        menu = new JMenu(I18n.get("ScaffoldTreeView.Menu.Tree"));
        layoutMenu = new JMenu(I18n._("Layout.Layout"));
        layoutMenu.add(radialLayoutAction);
        layoutMenu.add(balloonLayoutAction);
        layoutMenu.add(linearLayoutAction);
        menu.add(layoutMenu);
        JCheckBoxMenuItem toggleDetailsItem = new JCheckBoxMenuItem(toggleDetailsAction);
        toggleDetailsItem.setModel(toggleDetailsButton.getModel());
        menu.add(toggleDetailsItem);
        menu.addSeparator();
        menu.add(setMappingsAction);
        menu.addSeparator();
        menu.add(zoomInAction);
        menu.add(zoomOutAction);
        menu.add(zoomToFitAction);
        menu.add(zoomToSelection);
        menu.addSeparator();
        menu.add(expandAllNodesAction);
        menu.add(expandToDefaultLevelAction);
        menu.addSeparator();
        menu.add(increaseRadiusAction);
        menu.add(decreaseRadiusAction);
        menu.add(fixRadiiMenuItem);
        menu.add(resetRadiusAction);
        menu.addSeparator();
        menu.add(maximizeNodeAction);
        menu.add(minimizeNodeAction);
        menu.add(normalizeNodeAction);
        menu.addSeparator();
        menu.add(maximizeSelectedNodesAction);
        menu.add(minimizeSelectedNodesAction);
        menu.add(normalizeSelectedNodesAction);
        menu.add(normalizeAllNodesAction);
        menu.addSeparator();
        menu.add(exportAction);

        zoomlistener = new ZoomListener();
        vcanvas.getCamera().addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, zoomlistener);
        vcanvas.getCamera().addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, zoomlistener);

        // select, mouseover, context
        vcanvas.addInputEventListener(tooltipEventHandler);
        vcanvas.addInputEventListener(eventhandler);

        // TODO better place
        // TODO divide global/local/universalconfig
        addPropertyChangeListener(INSTANCE_CONFIG_PROPERTY,
                new GenericPropertyChangeListener<ScaffoldTreeViewConfig>() {
                    @Override
                    public void propertyChange(GenericPropertyChangeEvent<ScaffoldTreeViewConfig> ev) {
                        ScaffoldTreeViewConfig data = ev.getNewValue();
                        if (ev.getOldValue() == null ||
                                !ev.getOldValue().getLayout().equals(data.getLayout())) {
                            updateLayout(data.getLayout());
                        }
                        Dataset currentDataset = getSubset().getSession().getDataset();
                        PropertyDefinition orderProperty = 
                            ev.getNewValue().getMoleculeOrderProperty(currentDataset);
                        if (ev.getOldValue() == null || 
                                !Objects.equal(ev.getOldValue().getMoleculeOrderProperty(currentDataset), 
                                        orderProperty)) {
                            sortMoleculesBy(orderProperty);
                        } if (ev.getOldValue() == null || 
                                ev.getOldValue().isHideSubtreeEdges() != ev.getNewValue().isHideSubtreeEdges()) {
                            vcanvas.setHideSubtreeEdges(ev.getNewValue().isHideSubtreeEdges());
                            if (vcanvas.getScaffolds().size() != 0)
                            vcanvas.getVTree().getLayout().updateLayout();
                        }
                    }
                });
        addPropertyChangeListener(CLASS_CONFIG_PROPERTY, new GenericPropertyChangeListener<ScaffoldTreeViewClassConfig>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<ScaffoldTreeViewClassConfig> ev) {
                ScaffoldTreeViewClassConfig data = ev.getNewValue();
                vcanvas.getVAnimation().setLayoutAnimation(data.isLayoutAnimation());
                vcanvas.setCameraAnimation(data.isCameraAnimation());
                vcanvas.setCursorAnimation(data.isCursorAnimation());
                vcanvas.setFocusAfterAction(data.isFocusAfterAction());
                vcanvas.setAnimationSpeed(data.getAnimationSpeed());
                vcanvas.setCursorSiblingWraparound(data.isCursorSiblingWraparound());
                if (ev.getOldValue() == null ||  ev.getOldValue().isCutTreeStem() != ev.getNewValue().isCutTreeStem()) {
                    rebuildTree();
                }
            }
        });
        firePropertyChange(INSTANCE_CONFIG_PROPERTY, null, getInstanceConfig());
        addPropertyChangeListener(GLOBAL_CONFIG_PROPERTY, new GenericPropertyChangeListener<GlobalConfig>() {
            @Override
            public void propertyChange(GenericPropertyChangeEvent<GlobalConfig> ev) {
                GlobalConfig data = getGlobalConfig();
                vcanvas.setRenderingQuality(data.getRenderingQuality());
                minimap.setRenderingQuality(data.getRenderingQuality());
                magnifymap.setRenderingQuality(data.getRenderingQuality());
            }
        });
        firePropertyChange(GLOBAL_CONFIG_PROPERTY, null, getGlobalConfig());
    }

    private void setupSidebar() {
        minimap = new MiniMap();
        minimap.setPreferredSize(new Dimension(160, 160));
        sidebarComponents.add(new SideBarItem("Minimap", Resources.getIcon("minimap.png"), minimap));

        magnifymap = new VMagnifyingMap();
        magnifymap.setPreferredSize(new Dimension(160, 160));
        sidebarComponents.add(new SideBarItem("Zoom", Resources.getIcon("zoom.png"), magnifymap));
        
        SortChooser sortChooser = new SortChooser(sorting, this, getSubset());
        addPropertyChangeListener(SUBSET_PROPERTY, sortChooser);
        sidebarComponents.add(sortChooser);    
        
        SortLegendPanel sortLegendPanel = new SortLegendPanel(sorting);
        sidebarComponents.add(new SideBarItem(I18n.get("ScaffoldTreeView.SortLegend"), null, sortLegendPanel, false));
        sorting.setSortLegendPanel(sortLegendPanel);
    }    
   
    /**
     * apply a new Layout to the tree
     * 
     * @param layout the layout to apply
     */
    void updateLayout(VLayoutsEnum layout) {
        getInstanceConfig().setLayout(layout);
        VTree tree = vcanvas.getVTree();
        if (tree.getLayout() != null)
            tree.getLayout().clearSeparators();
        switch (layout) {
        case BALLOON_LAYOUT:
            tree.setLayout(new VBalloonLayout(tree, getState()));
            increaseRadiusAction.setEnabled(false);
            decreaseRadiusAction.setEnabled(false);
            fixRadiiAction.setEnabled(false);
            fixRadiiMenuItem.setSelected(false);
            resetRadiusAction.setEnabled(false);
            break;
        case RADIAL_LAYOUT:
            tree.setLayout(new VRadialWidthLayout(tree, getState()));
            increaseRadiusAction.setEnabled(true);
            decreaseRadiusAction.setEnabled(true);
            fixRadiiAction.setEnabled(true);
            fixRadiiMenuItem.setSelected(vcanvas.getVTree().getLayout().getFixedLayout());
            resetRadiusAction.setEnabled(true);
            break;
        case LINEAR_LAYOUT:
            tree.setLayout(new VLinearLayout(tree, getState()));
            increaseRadiusAction.setEnabled(false);
            decreaseRadiusAction.setEnabled(false);
            fixRadiiAction.setEnabled(false);
            fixRadiiMenuItem.setSelected(false);
            resetRadiusAction.setEnabled(false);
        }
        tree.setLayoutInvalid(true);
        /*
         *  reapply sorting: this is not strictly necessary since the
         *  underlying vtree will still be sorted correctly, but this
         *  is the easiest way to readd the colored background.  
         */
        sorting.sortTree(getSubset(), new ProgressAdapter<Void>());
    }

    @Override
    public JComponent getComponent() {
        return scrollPane;
    }

    @Override
    public JToolBar getToolBar() {
        return toolbar;
    }

    @Override
    public List<SideBarItem> getSideBarItems() {
        return sidebarComponents;
    }

    @Override
    public void destroy() {
        for (ScaffoldNode n : vcanvas.getVTree().getVNodes()) {
            getSelection().removePropertyChangeListener(Selection.SELECTION_PROPERTY, n);
        }
        getBannerPool().removeBannerChangeListener(bannerListener);
    }
    
    public void resetTree() {
        vcanvas.getVTree().removeVNodeListener(getState());
        vcanvas.getVTree().clear();
        vcanvas.getVTree().addVNodeListener(getState());
        showTree();
    }

    
    private void rebuildTree() {
        getState().setCameraTransform(null);
        // destroying the tree here should not influence the state
        vcanvas.getVTree().removeVNodeListener(getState());
        vcanvas.getVTree().clear();
        vcanvas.getVTree().addVNodeListener(getState());
        showTree();
    }
    
    @Override
    public JMenu getMenu() {
        return menu;
    }

    private class SetMappingsAction extends AbstractAction {
        public SetMappingsAction() {
            super(I18n.get("Menu.View.SetMappings"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.SetMappings"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("property-mappings.png"));
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_DOWN_MASK));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ScaffoldTreeViewConfig oldConfig = (ScaffoldTreeViewConfig) getInstanceConfig().copy();
            MappingDialog dialog = new MappingDialog((Window) vcanvas.getTopLevelAncestor(), getInstanceConfig(),
                    getSubset().getSession().getProfile(), getDbManager(), getSubset());
            dialog.setVisible(true);
            ScaffoldTreeView.this.firePropertyChange(INSTANCE_CONFIG_PROPERTY, oldConfig, getInstanceConfig());
        }
    }
    

    private class ZoomListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            zoomCombo.getEditor().setItem(vcanvas.getZoomFactor() + "%");
            
            // only do this for camera changes, not for bounds
            if(evt.getPropertyName().equals(PCamera.PROPERTY_VIEW_TRANSFORM)) {
                vcanvas.setZoomOnResize(false);
            }
        }
    }

    private static class BannerListener implements BannerChangeListener {

        final VCanvas canvas;

        BannerListener(VCanvas canvas) {
            this.canvas = canvas;
        }

        private void bannerChanged(Banner banner) {
            if (!banner.isMolecule()) {
                canvas.repaint();
            }
        }

        @Override
        public void bannerAdded(Banner banner) {
            bannerChanged(banner);
        }

        @Override
        public void bannerRemoved(Banner banner) {
            bannerChanged(banner);
        }
    }
    
    /**
     * Fix the distance between the tree layers
     * 
     * @param fixed
     */
    public void setFixedRadii(boolean fixed) {
        fixRadiiMenuItem.setSelected(fixed);
        fixRadiiToolbarButton.setSelected(fixed);
        getState().setFixedRadii(fixed);
    }
    
    /**
     * 
     * @param propertyDefinition
     */
    public void sortMoleculesBy(final PropertyDefinition propertyDefinition) {
        getInstanceConfig().setMoleculeOrderProperty(propertyDefinition);
        if (propertyDefinition == null) {
            vcanvas.getVTree().setMoleculeComparator(null);
        } else {
            ProgressWorker<Ordering<? super Molecule>, Void> w = 
                new ProgressWorker<Ordering<? super Molecule>, Void>() {

                @Override
                protected Ordering<? super Molecule> doInBackground() throws Exception {
                    DBFunction<Ordering<? super Molecule>> f = new DBFunction<Ordering<? super Molecule>>() {
                        @Override
                        public Ordering<? super Molecule> call() throws DatabaseException {
                            return new Orderings.DBOrdering(getDbManager(), propertyDefinition, getSubset());
                        }
                    };
                    
                    Ordering<? super Molecule> o = DBExceptionHandler.callDBManager(getDbManager(),f, true);
                    
                    return o;
                }
            };
            w.addProgressListener(new ProgressAdapter<Ordering<? super Molecule>>() {
                
                @Override
                public void finished(Ordering<? super Molecule> result, boolean cancelled) {
                    if (cancelled) {
                        result = null;
                    }
                    vcanvas.getVTree().setMoleculeComparator(result);
                }
            });
            w.execute();
        }
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.View#focusMolecule(edu.udo.scaffoldhunter.model.db.Molecule)
     */
    @Override
    public void focusMolecule(Molecule molecule) {
        // find scaffold
        LinkedList<Scaffold> q = new LinkedList<Scaffold>();
        Scaffold scaffold = vcanvas.getVTree().getRoot().getScaffold();
        q.add(scaffold);    
        boolean found = false;
        while (!q.isEmpty()) {
            scaffold = q.pop();
            if (scaffold.getMolecules().contains(molecule)) {
                found = true;
                break;
            }
            q.addAll(scaffold.getChildren());
        }

        // do panning
        if (found) {
            vcanvas.panTo(scaffold);
        }
    }

}