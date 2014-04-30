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

package edu.udo.scaffoldhunter.view.treemap;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;

import org.apache.batik.transcoder.TranscoderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.gui.util.ProgressWorkerUtil;
import edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.FileType;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.GenericView;
import edu.udo.scaffoldhunter.view.SideBarItem;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.treemap.loading.TreeMapDbsLoader;
import edu.udo.scaffoldhunter.view.treemap.sidebar.ColorLegend;
import edu.udo.scaffoldhunter.view.treemap.sidebar.PropertySelector;
import edu.udo.scaffoldhunter.view.treemap.sidebar.TreeMapPickPanel;
import edu.udo.scaffoldhunter.view.util.ExportDialog;
import edu.udo.scaffoldhunter.view.util.MiniMap;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGGenerator;
import edu.udo.scaffoldhunter.view.util.TooltipEventHandler;
import edu.udo.scaffoldhunter.view.util.TooltipManager;

/**
 * The View that creates the famous TreeMap:
 * "Treemaps display hierarchical (tree-structured) data as a set of nested rectangles"
 * (Wiki)
 * 
 * @author Lappie
 * 
 */
public class TreeMapView extends GenericView<TreeMapViewInstanceConfig, TreeMapViewClassConfig, TreeMapViewState>
        implements PropertyChangeListener {

    private static Logger logger = LoggerFactory.getLogger(TreeMapView.class);
    private JPanel viewContainer = new JPanel(new BorderLayout());
    private JScrollPane scrollPane = new JScrollPane();
    private TreeMapCanvas canvas;

    private JMenu menu;
    private JToolBar toolbar;

    private TooltipEventHandler tooltipEventHandler;

    // Sidebar
    private Vector<SideBarItem> sidebarComponents = new Vector<SideBarItem>();
    private MiniMap minimap;
    private PropertySelector propertySelector;
    private ColorLegend legend;

    private TreeMapDbsLoader loader;
    
    // Actions
    private Action zoomInAction;
    private Action zoomOutAction;
    private Action zoomToOverview;
    private Action zoomToSelection;    
    private Action exportAction;
    /**
     * Setup the TreeMap with default settings.
     * 
     * @param session
     * @param subset
     * @param instanceConfig
     * @param classConfig
     * @param globalConfig
     * @param state
     */
    public TreeMapView(GUISession session, Subset subset, ViewInstanceConfig instanceConfig,
            ViewClassConfig classConfig, GlobalConfig globalConfig, ViewState state) {
        super(session, subset, instanceConfig, classConfig, globalConfig, state);
        logger.debug("TreeMapView loaded");
        
        SVGCache svgCache = new SVGCache(getDbManager(),1500);

        loader = new TreeMapDbsLoader(getDbManager(), subset);
        Scaffold scaffold = loader.getRootScaffold();
        canvas = new TreeMapCanvas(scaffold, getSelection(), globalConfig, svgCache, subset);
        loader.setCanvas(canvas);

        setupActions();
        setupSidebar();
        setupToolBar();
        setupMenu();

        scrollPane.setViewportView(canvas);

        viewContainer.add(scrollPane);
        viewContainer.addComponentListener(new TreeMapCanvasResizeListener(viewContainer, canvas));

        // Set up Tooltips:
        TooltipManager tooltipManager = new TooltipManager(getDbManager(), svgCache, getSubset().getSession()
                .getProfile(), getGlobalConfig());
        addPropertyChangeListener(View.GLOBAL_CONFIG_PROPERTY, tooltipManager);

        addPropertyChangeListener(SUBSET_PROPERTY, this); //if other subset selected
        
        tooltipEventHandler = new TooltipEventHandler(canvas, tooltipManager, getGlobalConfig());
        addPropertyChangeListener(GLOBAL_CONFIG_PROPERTY, tooltipEventHandler);
        canvas.addInputEventListener(tooltipEventHandler);
    }
    
    private void setupActions() {
        zoomInAction = new ZoomInAction(canvas);
        zoomOutAction = new ZoomOutAction(canvas);
        zoomToOverview = new ZoomToOverview(canvas);
        zoomToSelection = new ZoomToSelection(canvas);
        exportAction = new ExportCanvasAction(canvas);
    }

    private void setupSidebar() {
        // minimap:
        minimap = new MiniMap();
        minimap.setPreferredSize(new Dimension(160, 160));
        minimap.connect(canvas);
        sidebarComponents.add(new SideBarItem(I18n.get("TreeMapView.Minimap.Minimap"),
                Resources.getIcon("minimap.png"), minimap));

        // Pick color/size:
        propertySelector = new PropertySelector(canvas, getSubset(), loader, getState());
        sidebarComponents.add(new SideBarItem(I18n.get("TreeMapView.Mappings.AxisPropertyMapping"), Resources
                .getIcon("axes_property_mapping.png"), propertySelector));
        propertySelector.addPropertyChangeListener(minimap);

        // Legend:
        legend = new ColorLegend();
        legend.setPreferredSize(new Dimension(160, 160));
        sidebarComponents.add(new SideBarItem(I18n.get("TreeMapView.Legend.Legend"), Resources.getIcon("legend.png"),
                legend));
        loader.setLegend(legend);
        
        //Detail view: 
        TreeMapPickPanel pickPanel = new TreeMapPickPanel(getDbManager());
        pickPanel.setPreferredSize(new Dimension(160, 250));
        canvas.addPickChangeListener(pickPanel);
        sidebarComponents.add(new SideBarItem(I18n.get("PlotView.Mappings.Detailview"), Resources.getIcon("zoom.png"), pickPanel));
        
        // Plot last settings
        propertySelector.plot();
    }
    
    private void setupToolBar() {
        toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(zoomInAction);
        toolbar.add(zoomOutAction);
        toolbar.add(zoomToOverview);
        toolbar.add(zoomToSelection);
        toolbar.add(exportAction);
    }
    
    private void setupMenu() {
        menu = new JMenu(I18n.get("TreeMapView.Menu.TreeMap"));
        menu.add(zoomInAction);
        menu.add(zoomOutAction);
        menu.add(zoomToOverview);
        menu.add(zoomToSelection);
        menu.addSeparator();
        menu.add(exportAction);
    }

    @Override
    public List<SideBarItem> getSideBarItems() {
        return sidebarComponents;
    }

    @Override
    public JComponent getComponent() {
        return viewContainer;
    }
    
    @Override
    public JToolBar getToolBar() {
        return toolbar;
    }

    @Override
    public void destroy() {
        canvas.removeInputEventListener(tooltipEventHandler);
        removePropertyChangeListener(SUBSET_PROPERTY, this);
        removePropertyChangeListener(GLOBAL_CONFIG_PROPERTY, tooltipEventHandler);
        propertySelector.removePropertyChangeListener(minimap);
        
        if (canvas != null)
            canvas.destroy();
    }

    /**
     * listens to changes of the subset. If this happenes, give everybody the
     * new subset and load the new canvas.
     */
    @Override
    public void propertyChange(PropertyChangeEvent event) {
        Subset subset = (Subset) event.getNewValue();
        loader.setSubset(subset);
        
        canvas.refreshSubset(subset);
        canvas.loadNewScaffold(loader.getRootScaffold());
        
        propertySelector.loadNewSubset(subset);
    }

    @Override
    public void focusMolecule(Molecule molecule) {
        canvas.focusOnMolecule(molecule);
    }
    
    @Override
    public JMenu getMenu() {
        return menu;
    }
    
    private class ZoomInAction extends AbstractAction {
        private final TreeMapCanvas canvas;

        public ZoomInAction(TreeMapCanvas canvas) {
            super(I18n.get("Menu.Edit.Zoomin"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomin"));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-in.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-in.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int x = canvas.getHeight() / 2;
            int y = canvas.getWidth() / 2;
            Point2D p = canvas.getCamera().localToView(new Point(x, y));
            canvas.zoomIn(p);
        }
    }
    
    private class ZoomOutAction extends AbstractAction {
        private final TreeMapCanvas canvas;

        public ZoomOutAction(TreeMapCanvas canvas) {
            super(I18n.get("Menu.Edit.Zoomout"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomout"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('-'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-out.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-out.png"));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int x = canvas.getHeight() / 2;
            int y = canvas.getWidth() / 2;
            Point2D p = canvas.getCamera().localToView(new Point(x, y));
            canvas.zoomOut(p);
        }
    }
    
    private class ZoomToOverview extends AbstractAction {
        private final TreeMapCanvas canvas;

        public ZoomToOverview(TreeMapCanvas canvas) {
            super(I18n.get("Menu.Edit.ZoomToFit"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.ZoomToFit"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('0'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-best-fit.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-best-fit.png"));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.zoomToOverview();
        }
    }
    
    private class ZoomToSelection extends AbstractAction {
        private final TreeMapCanvas canvas;

        public ZoomToSelection(TreeMapCanvas canvas) {
            super(I18n.get("ScaffoldTreeView.Menu.ZoomToSelection"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("ScaffoldTreeView.Menu.ZoomToSelection"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('s'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-fit-selection.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-fit-selection.png"));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.zoomToSelectionOverview();
        }
    }
    static class ExportCanvasAction extends AbstractAction {
        private final TreeMapCanvas canvas;

        public ExportCanvasAction(TreeMapCanvas canvas) {
            super(I18n.get("Export.ImageDescription"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Export.ImageDescription"));
            putValue(Action.SMALL_ICON, Resources.getIcon("save.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("save.png"));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Window parent = (Window) canvas.getTopLevelAncestor();
            final ExportDialog d = new ExportDialog(parent, canvas.getExportScreenDimension(),
                    canvas.getExportAllDimension());
            int ret = d.showExportDialog();
            if (ret == JFileChooser.CANCEL_OPTION)
                return;
            ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Dimension dim = d.getDimension();
                    FileType type = d.getFileType();
                    File file = d.getFile();
                    SVGGenerator gen = canvas.exportSVG(dim, d.isExportAll());

                    try {
                        gen.transcode(file, type);
                    } catch (TranscoderException e) {
                        if (file.exists())
                            file.delete();
                        throw e;
                    }
                    return null;
                }
            };
            worker.addExceptionListener(new WorkerExceptionListener() {
                @Override
                public ExceptionHandlerResult exceptionThrown(Throwable e) {
                    if (e instanceof TranscoderException) {
                        JOptionPane.showMessageDialog(canvas, I18n.get("Export.Error"));
                        return ExceptionHandlerResult.STOP;
                    } else {
                        return ExceptionHandlerResult.NOT_HANDLED;
                    }
                }
            });
            ProgressWorkerUtil.executeWithProgressDialog(parent, I18n.get("Export.Title"), I18n.get("Export.Progress"),
                    ModalityType.APPLICATION_MODAL, worker);
        }
    }
}
