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

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.event.EventListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Banner;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.DefaultColors;
import edu.udo.scaffoldhunter.view.table.ViewComponent;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * 
 * @author Philipp Lewe
 * @author Philipp Kopp
 */
public class DendrogramCanvas extends PCanvas implements ClusterSelectionBarChangedEventListener, BannerChangeListener {
    private static Logger logger = LoggerFactory.getLogger(DendrogramCanvas.class);

    private static final double ZOOM_SENSITIVITY = 0.095;

    private PLayer layer;
    // a special node which hold all leafnodes with the SVGs
    // necessary because of the zooming behavior
    private PNode leafNodes;
    // the tree above the leafes
    private DendrogramViewNode dendrogramViewRoot;

    private ClusterSelectionBar selectionBar;
    private DendrogramZoomHandler zoomhandler;
    private PCamera camera;
    EventListenerList listenerList = new EventListenerList();

    // maps from all modelNodes to the PNodes which built the tree
    private Hashtable<HierarchicalClusterNode<Molecule>, DendrogramViewNode> dictionary = new Hashtable<HierarchicalClusterNode<Molecule>, DendrogramViewNode>();
    // holds the clusters managed by the selectionBar
    List<DendrogramViewNode> chosenClusterTrees;
    // holds the size of the clusters above
    List<Integer> clusterSizes = new LinkedList<Integer>();

    private double old_threshold = 0;
    private double spaceLeftOfRoot;

    private double xScale = 1.0;
    private double yScale = 1.0;
    private double xZoom = 1.0;
    private double yZoom = 1.0;
    private double initialTreeHeight;
    private double initialLeafHeight;
    private PBounds initialBounds;

    private ViewComponent table;

    private List<Structure> modifiedSelection = new ArrayList<Structure>();
    private Selection selection;
    private SelectionPropertyChangeListener selectionChangeListener = new SelectionPropertyChangeListener();

    private GlobalConfig globalConfig;

    private BannerPool bannerPool;
    // a local storage to find the right ViewNodes fast
    private HashMap<Structure, DendrogramViewNode> bannerMap = new HashMap<Structure, DendrogramViewNode>();

    private DendrogramCanvasResizeListener canvasResizeListener;

    private int minSVGWidth;

    private static final int MINSVGWIDTHBORDER = 500;

    /**
     * Create and set up basic scene graph, install event handlers
     * 
     * 
     * @param svgcache
     *            the shared cache
     * @param dendrogramModel
     *            cluster model
     * @param selection
     *            the global selection object
     * @param table
     *            corresponding tableView
     * @param globalConfig
     * @param bannerPool
     * @param subset
     */
    public DendrogramCanvas(SVGCache svgcache, HierarchicalClusterNode<Molecule> dendrogramModel, Selection selection,
            ViewComponent table, GlobalConfig globalConfig, BannerPool bannerPool, Subset subset) {
        super();
        this.bannerPool = bannerPool;
        bannerPool.addBannerChangeListener(this);
        this.table = table;
        this.setGlobalConfig(globalConfig);
        camera = getCamera();
        layer = camera.getLayer(0);

        leafNodes = new PNode();
        layer.addChild(leafNodes);

        zoomhandler = new DendrogramZoomHandler(this); // zoom

        camera.addInputEventListener(zoomhandler);
        canvasResizeListener = new DendrogramCanvasResizeListener(this);
        addComponentListener(canvasResizeListener);
        setZoomEventHandler(null);
        setPanEventHandler(null);
        // setPanEventHandler(new DendrogramPanEventHandler(this));

        this.selection = selection;

        // doesn't change the correlation between the nodes
        // without it the inner tree nodes might get do big/small
        double normalizingFactor = getMaxDist(dendrogramModel);
        if (normalizingFactor == 0) {
            normalizingFactor = 1;
        }
        normalizingFactor = 1 / normalizingFactor * 300;
        // at this moment the constructor of DendrogramViewNode builds the tree
        // recursive
        minSVGWidth = Math.max(calcMinSVGWidth(dendrogramModel), MINSVGWIDTHBORDER);
        dictionary.put(dendrogramModel, new DendrogramViewNode(dendrogramModel, dictionary, 0, leafNodes, svgcache,
                this, normalizingFactor));

        selection.addPropertyChangeListener(Selection.SELECTION_PROPERTY, selectionChangeListener);

        // the root of the tree
        dendrogramViewRoot = dictionary.get(dendrogramModel);
        // sets the right positions of the nodes
        adjustSceneGraph(dendrogramViewRoot);
        spaceLeftOfRoot = calcSpaceLeftOfRoot(dendrogramViewRoot);

        selectionBar = new ClusterSelectionBar(dendrogramViewRoot.getTreeWidth(), 1, dendrogramViewRoot.getPosY(),
                getSpaceLeftOfRoot());
        selectionBar.addClusterSelectionBarChangedEventListener(this);

        layer.addChild(dendrogramViewRoot);
        dendrogramViewRoot.addChild(selectionBar);

        leafNodes.translate(0, dendrogramViewRoot.getPosY());
        dendrogramViewRoot.translate(spaceLeftOfRoot, 0);

        initialBounds = layer.getFullBounds();
        initialLeafHeight = leafNodes.getFullBounds().getHeight();
        initialTreeHeight = dendrogramViewRoot.getFullBounds().getHeight();
        logger.trace("initialBounds: {}", initialBounds);
        logger.trace("initialLeafHeight: {}", initialLeafHeight);
        logger.trace("initialTreeHeight: {}", initialTreeHeight);

        /* init chosen clusters */
        chosenClusterTrees = dendrogramViewRoot.getChosenClusterRoots(Double.MAX_VALUE);

        clusterSizes.clear();
        for (DendrogramViewNode dendrogramViewNode : chosenClusterTrees) {
            clusterSizes.add(dendrogramViewNode.getTreeSize());
        }
        dendrogramViewRoot.initialBannerPlacing(bannerPool.getAllBanners(), bannerMap);

        /* end init chosen clusters */

        invalidateSelection();

        // debug command to show the bounding boxes
        // PDebug.debugBounds = true;
        // PDebug.debugPaintCalls = true;

    }

    /**
     * 
     * @param root
     * @return the maximum dissimilarity
     */
    private double getMaxDist(HierarchicalClusterNode<Molecule> root) {
        double max, left, right;

        if (root.isLeaf()) {
            max = 0;
        } else {
            left = getMaxDist(root.getLeftChild());
            right = getMaxDist(root.getRightChild());
            max = root.getDissimilarity();
            if (left > max) {
                max = left;
            }
            if (right > max) {
                max = right;
            }
        }
        return max;
    }

    /**
     * 
     * @param root
     * @return the minimal width of the SVGs in the leafs
     */
    private int calcMinSVGWidth(HierarchicalClusterNode<Molecule> root) {
        if (root.isLeaf()) {
            return root.getContent().getSvgWidth();
        } else {
            return Math.min(calcMinSVGWidth(root.getLeftChild()), calcMinSVGWidth(root.getRightChild()));
        }
    }

    /**
     * Deregister all listeners on other objects
     */
    public void destroy() {
        selection.removePropertyChangeListener(Selection.SELECTION_PROPERTY, selectionChangeListener);
    }

    /**
     * Notifies the canvas to repaint all nodes based on the current selection
     */
    public void invalidateSelection() {
        for (DendrogramViewNode node : dictionary.values()) {
            node.invalidateSelection();
        }
    }

    /**
     * @return the dendrogramTree sceneGraph
     */
    public DendrogramViewNode getViewNodes() {
        return dendrogramViewRoot;
    }

    /**
     * @return the top layer where all contents of this canvas are stored
     */
    public PLayer getNodeLayer() {
        return layer;
    }

    private void updateTable() {
        table.setClusters(getClusterSizes());
    }

    /**
     * @return the {@link ViewComponent} of the connected table view
     */
    public ViewComponent getTable() {
        return table;
    }

    /**
     * Returns a list of Integers. Each list entry represents a cluster. The
     * size of the cluster corresponds the integer value.
     * 
     * @return a list of cluster sizes
     */
    public List<Integer> getClusterSizes() {
        return clusterSizes;
    }

    /**
     * Colorizes choosen clusters based on the colors stored in TwentyColors
     */
    public void colorizeChoosenClusters() {
        for (int i = 0; i < chosenClusterTrees.size(); i++) {
            chosenClusterTrees.get(i).setClusterColorRecursively(DefaultColors.getColor(i));
        }
    }

    /**
     * adjusts the calculated positions to the scene graph
     * 
     * @param root
     *            the element to translate
     */
    private void adjustSceneGraph(DendrogramViewNode root) {

        // only translate child nodes to the calculated positions
        if (dendrogramViewRoot != root) {
            double x;
            double y;
            if (root.isLeaf()) {
                x = root.getPosX();
                y = 0;
            } else {
                x = root.getRelativePos().x;
                y = root.getRelativePos().y;
            }

            root.translate(x, y);
        }

        // recursive translation for all non leaf tree elements
        if (!root.isLeaf()) {
            adjustSceneGraph(root.getLeftChild());
            adjustSceneGraph(root.getRightChild());

            // as the translations for the nodes are made, the edges can be
            // calculated

            root.getLeftEdge().calcLine();
            root.getRightEdge().calcLine();
        } else {
            root.translate(-root.getBounds().getWidth() / 2, 0);
        }
    }

    /**
     * 
     * @param root
     * @return the amount of pixel left from the rootNode
     */
    private double calcSpaceLeftOfRoot(DendrogramViewNode root) {
        double spaceLeftOfRoot;
        if (!root.isLeaf()) {
            spaceLeftOfRoot = calcSpaceLeftOfRoot(root.getLeftChild());
            spaceLeftOfRoot -= root.getLeftChild().getRelativePos().getX();
        } else {
            spaceLeftOfRoot = root.getTreeWidth() / (double) 2;
        }
        return spaceLeftOfRoot;
    }

    /**
     * @return space left of root
     */
    public double getSpaceLeftOfRoot() {
        return spaceLeftOfRoot;
    }

    /**
     * @return the SVG sceneGraph
     */
    public PNode getLeafNodes() {
        return leafNodes;
    }

    // ****************************************************************
    // EventListener - Incoming
    // ****************************************************************

    @Override
    public void ClusterSelectionBarDragStarted(ClusterSelectionBarChangedEvent event) {
        fireClusterSelectionBarDragStarted();
    }

    @Override
    public void ClusterSelectionBarDragActive(ClusterSelectionBarChangedEvent event) {
        double halfNodeHeight = dendrogramViewRoot.getBounds().height / 2;
        double halfSelectionHeight = selectionBar.getBounds().height / 2;

        // threshold = difference of the middle y coordinates between
        // dendrogramViewRoot and selectionBar
        double threshold = (dendrogramViewRoot.getPosY() + halfNodeHeight)
                - (selectionBar.getFullBounds().getY() + halfSelectionHeight);

        if (threshold != old_threshold) {
            old_threshold = threshold;

            chosenClusterTrees = dendrogramViewRoot.getChosenClusterRoots(threshold);

            clusterSizes.clear();
            for (DendrogramViewNode dendrogramViewNode : chosenClusterTrees) {
                clusterSizes.add(dendrogramViewNode.getTreeSize());
            }
        }
        fireClusterSelectionBarDragActive();
    }

    @Override
    public void ClusterSelectionBarDragReleased(ClusterSelectionBarChangedEvent event) {
        updateTable();
        colorizeChoosenClusters();
        fireClusterSelectionBarDragReleased();
    }

    // ****************************************************************
    // EventListener - Outgoing
    // ****************************************************************

    /**
     * Return the list of event listeners associated with this canvas.
     * 
     * @return event listener list or null
     */
    public EventListenerList getListenerList() {
        return listenerList;
    }

    /**
     * Adds the specified ClusterSelectionBarChangedEventListener to receive
     * events from this canvas.
     * 
     * @param listener
     *            the new ClusterSelectionBarChangedEventListener
     */
    public void addClusterSelectionBarChangedEventListener(ClusterSelectionBarChangedEventListener listener) {
        getListenerList().add(ClusterSelectionBarChangedEventListener.class, listener);
    }

    /**
     * Removes the specified ClusterSelectionBarChangedEventListener to receive
     * events from this canvas.
     * 
     * @param listener
     *            the new ClusterSelectionBarChangedEventListener
     */
    public void removeClusterSelectionBarChangedEventListener(ClusterSelectionBarChangedEventListener listener) {
        getListenerList().remove(ClusterSelectionBarChangedEventListener.class, listener);
    }

    synchronized void fireClusterSelectionBarDragStarted() {
        ClusterSelectionBarChangedEvent event = new ClusterSelectionBarChangedEvent(this);
        ClusterSelectionBarChangedEventListener[] listeners = getListenerList().getListeners(
                ClusterSelectionBarChangedEventListener.class);

        for (ClusterSelectionBarChangedEventListener listener : listeners) {
            listener.ClusterSelectionBarDragStarted(event);
        }
    }

    synchronized void fireClusterSelectionBarDragActive() {
        ClusterSelectionBarChangedEvent event = new ClusterSelectionBarChangedEvent(this);
        ClusterSelectionBarChangedEventListener[] listeners = getListenerList().getListeners(
                ClusterSelectionBarChangedEventListener.class);

        for (ClusterSelectionBarChangedEventListener listener : listeners) {
            listener.ClusterSelectionBarDragActive(event);
        }
    }

    synchronized void fireClusterSelectionBarDragReleased() {
        ClusterSelectionBarChangedEvent event = new ClusterSelectionBarChangedEvent(this);
        ClusterSelectionBarChangedEventListener[] listeners = getListenerList().getListeners(
                ClusterSelectionBarChangedEventListener.class);

        for (ClusterSelectionBarChangedEventListener listener : listeners) {
            listener.ClusterSelectionBarDragReleased(event);
        }
    }

    // ****************************************************************
    // Zooming
    // ****************************************************************

    /**
     * Zooms to an overview of the whole dendrogram.
     */
    public void zoomToOverview() {
        logger.trace("----------------------------------------------------------------------------- begin zoom to overview");
        setXZoom(1.0);
        setYZoom(1.0);
        logger.trace("----------------------------------------------------------------------------- end zoom to overview");
    }

    /**
     * Zoom in one step (on the horizontal axis)
     * 
     * @param p
     *            the position the view should focus on
     */
    public void zoomInHorizontal(Point2D p) {
        Rectangle oldBounds = layer.getFullBounds().getBounds();
        setXZoom(getXZoom() * (1.0 + ZOOM_SENSITIVITY));
        focusOn(oldBounds, p);
    }

    /**
     * Zoom out one step (on the horizontal axis)
     * 
     * @param p
     *            the position the view should focus on
     */
    public void zoomOutHorizontal(Point2D p) {
        Rectangle oldBounds = layer.getFullBounds().getBounds();
        setXZoom(getXZoom() / (1.0 + ZOOM_SENSITIVITY));
        focusOn(oldBounds, p);
    }

    /**
     * Zoom in one step (on the vertical axis)
     * 
     * @param p
     *            the position the view should focus on
     */
    public void zoomInVertical(Point2D p) {
        Rectangle oldBounds = layer.getFullBounds().getBounds();
        setYZoom(getYZoom() * (1.0 + ZOOM_SENSITIVITY));
        focusOn(oldBounds, p);
    }

    /**
     * Zoom out one step (on the vertical axis)
     * 
     * @param p
     *            the position the view should focus on
     */
    public void zoomOutVertical(Point2D p) {
        Rectangle oldBounds = layer.getFullBounds().getBounds();
        setYZoom(getYZoom() / (1.0 + ZOOM_SENSITIVITY));
        focusOn(oldBounds, p);
    }

    /**
     * Focus the viewport on the given point
     * 
     * @param oldBounds
     *            the bounds of the dendrogram layer BEFORE scaling
     * @param pos
     *            the position the view should focus on
     */
    public void focusOn(Rectangle oldBounds, Point2D pos) {
        Rectangle newBounds = layer.getFullBounds().getBounds();
        Rectangle r = getVisibleRect();

        double oldLeftDist = pos.getX() - r.getMinX();
        double oldRightDist = r.getMaxX() - pos.getX();
        double oldAboveDist = pos.getY() - r.getMinY();
        double oldBelowDist = r.getMaxY() - pos.getY();

        double hScale = newBounds.getWidth() / oldBounds.getWidth();
        double vScale = newBounds.getHeight() / oldBounds.getHeight();

        double leftBorder = (hScale * pos.getX() - oldLeftDist);
        double rightBorder = (hScale * pos.getX() + oldRightDist);
        double aboveBorder = (vScale * pos.getY() - oldAboveDist);
        double belowBorder = (vScale * pos.getY() + oldBelowDist);

        Rectangle rectNew = new Rectangle((int) (leftBorder), (int) (aboveBorder), (int) (rightBorder - leftBorder),
                (int) (belowBorder - aboveBorder));

        logger.trace("focus On: {}", rectNew);
        scrollRectToVisible(rectNew);
    }

    /**
     * Zooms and focus on the given set of molecules
     * @param molecules 
     */
    public void zoomAndfocusOnMoleculeSet(Set<Molecule> molecules) {

        Rectangle2D mostLeftNodeRect = new Rectangle(Integer.MAX_VALUE, Integer.MAX_VALUE, 0, 0);
        Rectangle2D mostRightNodeRect = new Rectangle(Integer.MIN_VALUE, Integer.MIN_VALUE, 0, 0);

        DendrogramViewNode mostLeftNode = null;
        DendrogramViewNode mostRightNode = null;

        // calculate minimum / maximum positions for all selected leaf nodes
        for (DendrogramViewNode node : dendrogramViewRoot.getLeafs()) {

            // is node selected?
            if (molecules.contains(node.getModel().getContent())) {
                Rectangle2D rect = node.localToGlobal(node.getBounds());

                // calc minimum
                if (mostLeftNodeRect.getMinX() > rect.getMinX()) {
                    mostLeftNode = node;
                    mostLeftNodeRect = rect;
                }

                // calc maximum
                if (mostRightNodeRect.getMaxX() < rect.getMaxX()) {
                    mostRightNode = node;
                    mostRightNodeRect = rect;
                }
            }
        }
        logger.debug("mostLeftNode: {}, mostRightNode: {}", mostLeftNode, mostRightNode);

        double selectionWidth = mostRightNodeRect.getMaxX() - mostLeftNodeRect.getMinX();
        double newXZoom = (getParent().getWidth() / selectionWidth) * getXZoom();
        setXZoom(newXZoom);

        // calculate new global positions after setting new zoom
        mostLeftNodeRect = mostLeftNode.localToGlobal(mostLeftNode.getBounds());
        mostRightNodeRect = mostRightNode.localToGlobal(mostRightNode.getBounds());
        selectionWidth = mostRightNodeRect.getMaxX() - mostLeftNodeRect.getMinX();

        Rectangle scrollRect = new Rectangle((int) mostLeftNodeRect.getMinX(), Math.max(0,
                (int) mostLeftNodeRect.getMaxY() - getParent().getHeight()), (int) selectionWidth, getParent()
                .getHeight());

        scrollRectToVisible(scrollRect);
    }
    
    /**
     * Zooms and focus on the selected nodes
     */
    public void zoomAndfocusOnSelectedNodes() {
        zoomAndfocusOnMoleculeSet(getSelection());
    }
    
    /**
     * Zooms and focuses the specified node.
     * @param molecule 
     */
    public void focusMolecule(Molecule molecule) {
        HashSet<Molecule> singleton = new HashSet<Molecule>();
        singleton.add(molecule);
        zoomAndfocusOnMoleculeSet(singleton);
    }

    /**
     * Returns the current viewports center point. Used to zoom in by key events
     * (where no mouse position is known/meaningful).
     * 
     * @return the center coordinates
     */
    public Point2D getViewportCenter() {
        return new Point2D.Double(getVisibleRect().getCenterX(), getVisibleRect().getCenterY());
    }

    /**
     * @param xZoom
     *            the xZoom to set
     */
    public void setXZoom(double xZoom) {
        logger.trace("------------------------------------------------------------------ begin setXZoom");
        if (xZoom <= 1.0) {
            this.xZoom = 1.0;
        } else if (xZoom > calcMaxXZoom()) {
            this.xZoom = calcMaxXZoom();
        } else {
            this.xZoom = xZoom;
        }
        logger.trace("xZoom: {}", this.xZoom);

        // DO NOT CHANGE ORDER
        setXScale(calcXScale());
        setYScale(calcYScale());
        // END DO NOT CHANGE ORDER

        logger.trace("------------------------------------------------------------------ end setXZoom");
    }

    /**
     * @return the xZoom
     */
    public double getXZoom() {
        return xZoom;
    }

    /**
     * @param yZoom
     *            the yZoom to set
     */
    public void setYZoom(double yZoom) {
        logger.trace("------------------------------------------------------------------ begin setYZoom");

        if (yZoom <= 1.0) {
            this.yZoom = 1.0;
        } else if (yZoom > calcMaxYZoom()) {
            this.yZoom = calcMaxYZoom();
        } else {
            this.yZoom = yZoom;
        }

        logger.trace("yZoom: {}", this.yZoom);

        setAxisScale(getXScale(), calcYScale());
        logger.trace("------------------------------------------------------------------ end setYZoom");
    }

    /**
     * @return the yZoom
     */
    public double getYZoom() {
        return yZoom;
    }

    /**
     * @return the maximum zoom factor for the x-axis
     */
    protected double calcMaxXZoom() {
        return (Math.max(1.0, (initialBounds.getWidth() / getParent().getWidth())));
    }

    /**
     * @return the maximum zoom factor for the y-axis
     */
    protected double calcMaxYZoom() {
        double remaining_height = getParent().getHeight() - (getXScale() * initialLeafHeight);
        double MAX_FACTOR = 75;
        return (Math.max(1.0, (initialTreeHeight * MAX_FACTOR / remaining_height)));
    }

    /**
     * @return the absolute scaling factor for the x-axis, so that the canvas
     *         fits in the parent component multiplied with the x-zoom factor
     */
    protected double calcXScale() {
        double scale;
        if (getParent() == null) {
            scale = 1.0;
        } else {
            /*
             * With a x-zoom factor of 1.0 we want the width of the initial
             * dendrogram to fit in the space given by the parent components
             * width (the scrollpane), thus we build the quotient of both. If
             * the x-zoom factor is higher or less than 1.0, the want to scale
             * this initial scale value by the x-zoom factor.
             */
            scale = getXZoom() * getParent().getWidth() / initialBounds.getWidth();
        }
        logger.trace("XScale: {}", scale);
        return scale;
    }

    /**
     * @return the absolute scaling factor for the y-axis, so that the canvas
     *         fits in the parent component multiplied with the y-zoom factor
     */
    protected double calcYScale() {
        double scale;
        if (getParent() == null) {
            scale = 1.0;
        } else {
            /*
             * With a y-zoom factor of 1.0 we want the height of the initial
             * dendrogram to fit in the space given by the parent components
             * height. Unfortunately, if we scaled the width of the initial
             * dendrogram to fit in the parents width before (@see
             * calcXScale()), then we also scaled the leaf nodes height to avoid
             * stretching of the SVGs. This means there is less space available
             * for scaling the height of the full dendrogram (tree nodes + leaf
             * nodes). Thus we want to fit the initial tree height (the
             * dendrogram without the leaf nodes) in the remaining height.
             * 
             * The remaining height is calculated like follows: At a y-zoom
             * factor of 1.0 the height we can use to draw the full dendrogram
             * is the height of the parent component. If the y-zoom factor is
             * higher or less than 1.0, we scale this height by the y-zoom. As
             * we adapted the x-scaling before, we have to subtract the height
             * needed to draw the leaf nodes a the corresponding x-scale.
             */

            double remaining_height = (getYZoom() * getParent().getHeight()) - (getXScale() * initialLeafHeight);

            /*
             * if the remaining height is to low, then the dendrogram tree will
             * look very ugly because it collapes to one single line. So we set
             * the remaining heights minimum value relatively to the parent
             * height, which has the effect of moving the leaf nodes out of the
             * visual area and creating a vertical scrollbar.
             */
            remaining_height = Math.max(remaining_height, getParent().getHeight() / (double) 3);

            scale = remaining_height / initialTreeHeight;

            logger.trace("remaining_height: {}", remaining_height);
            logger.trace("YScale: {}", scale);
        }

        return scale;
    }

    /**
     * Sets the absolute scaling factor for both dimensions
     * 
     * @param xFactor
     *            the absolute scaling factor for the x-axis
     * @param yFactor
     *            the absolute scaling factor for the y-axis
     */
    protected void setAxisScale(double xFactor, double yFactor) {
        xScale = xFactor;
        yScale = yFactor;

        dendrogramViewRoot.setTransform(new AffineTransform(xScale, 0, 0, yScale, xScale * getSpaceLeftOfRoot(), 0));
        /*
         * scale leaf nodes in both dimensions the same and translate them below
         * the dendrogram
         */
        leafNodes.setTransform(new AffineTransform(xScale, 0, 0, xScale, 0, dendrogramViewRoot.getFullBounds()
                .getHeight()));

        setPreferredSize(layer.getFullBounds().getBounds().getSize());
        revalidate();

        layer.invalidatePaint();
        selectionBar.invalidatePaint();
    }

    /**
     * Returns the absolute scaling factor of the x-axis
     * 
     * @return the xScale
     */
    protected double getXScale() {
        return xScale;
    }

    /**
     * Returns the absolute scaling factor of the y-axis
     * 
     * @return the yScale
     */
    protected double getYScale() {
        return yScale;
    }

    /**
     * Sets the absolute scaling factor of the x-axis
     * 
     * @param scale
     *            the absolute scaling factor for the x-axis
     */
    protected void setXScale(double scale) {
        setAxisScale(scale, getYScale());
    }

    /**
     * Sets the absolute scaling factor of the y-axis
     * 
     * @param scale
     *            the absolute scaling factor for the y-axis
     */
    protected void setYScale(double scale) {
        setAxisScale(getXScale(), scale);
    }

    /**
     * Adapts the internal scaling to fit in the space defined by the parent
     * component of the canvas. Note that the zoom factors stay the same, but
     * after calling this method there is more (or less space) "allocated" for
     * the dendrogram.
     */
    public void adaptToParentSize() {
        logger.trace("----------------------------------------------------------------------------- begin adaptToParentSize");
        logger.trace("parentWidth: {}, parentHeight: {}", getParent().getWidth(), getParent().getHeight());
        setXZoom(getXZoom());
        logger.trace("----------------------------------------------------------------------------- end adaptToParentSize");

    }

    /**
     * Returns the modified selection list
     * 
     * @return the modifiedSelection
     */
    List<Structure> getModifiedSelection() {
        return modifiedSelection;
    }

    /**
     * Adds all selected molecules to global selection
     */
    void updateSelectionSelect() {
        List<Molecule> molecules = new ArrayList<Molecule>();

        for (Structure s : getModifiedSelection()) {
            if (s instanceof Molecule) {
                molecules.add((Molecule) s);
            }
        }
        selection.addAll(molecules);

        getModifiedSelection().clear();
    }

    /**
     * Removes all selected molecules from global selection
     */
    void updateSelectionDeselect() {
        List<Molecule> molecules = new ArrayList<Molecule>();

        for (Structure s : getModifiedSelection()) {
            if (s instanceof Molecule) {
                molecules.add((Molecule) s);
            }
        }
        selection.removeAll(molecules);
        getModifiedSelection().clear();
    }

    /**
     * @return the global selection object
     */
    public Selection getSelection() {
        return selection;
    }

    class SelectionPropertyChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            invalidateSelection();
        }
    }

    /**
     * Returns all structures in the given area
     * 
     * @param selectedArea
     *            a {@link Rectangle2D} referencing the selected area in global
     *            the coordinate system
     * @return {@link Collection} of {@link Structure}s
     */
    public Collection<Molecule> findStructuresInArea(Rectangle2D selectedArea) {
        List<Molecule> selectionList = Lists.newLinkedList();
        Point2D nodeCenter;

        for (DendrogramViewNode node : dendrogramViewRoot.getLeafs()) {

            nodeCenter = node.localToGlobal(node.getBounds().getCenter2D());

            if (selectedArea.contains(nodeCenter)) {
                selectionList.add(node.getModel().getContent());
            }
        }
        return selectionList;
    }

    /**
     * @param globalConfig
     *            the globalConfig to set
     */
    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = globalConfig;
    }

    /**
     * @return the globalConfig
     */
    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    /**
     * the view local Method to set a banner
     * 
     * @param isPrivate
     * @param structure
     * @param node
     */
    public void addBanner(boolean isPrivate, Structure structure, DendrogramViewNode node) {
        boolean exists = false;
        // security check not to add already existant Banner
        if (bannerMap.containsKey(structure)) {
            if (isPrivate) {
                exists = bannerMap.get(structure).hasPrivateBanner();
            } else {
                exists = bannerMap.get(structure).hasPublicBanner();
            }
        }

        if (!exists) {
            bannerMap.put(structure, node);
            bannerPool.addBanner(structure, isPrivate);
            if (isPrivate) {
                node.setHasPrivateBanner(true);
            } else {
                node.setHasPublicBanner(true);
            }
        }

    }

    /**
     * the view local method to check if a banner is present
     * 
     * @param isPrivate
     * @param structure
     * @return if the structure has the chosen banner
     */
    public boolean hasBanner(boolean isPrivate, Structure structure) {
        if (bannerMap.containsKey(structure)) {
            if (isPrivate) {
                return bannerMap.get(structure).hasPrivateBanner();
            } else {
                return bannerMap.get(structure).hasPublicBanner();
            }
        }
        return false;
    }

    /**
     * the view local Method to remove a banner
     * 
     * @param removePrivate
     * @param structure
     */
    public void removeBanner(boolean removePrivate, Structure structure) {
        if (bannerMap.containsKey(structure)) {
            if (removePrivate) {
                bannerMap.get(structure).setHasPrivateBanner(false);
            } else {
                bannerMap.get(structure).setHasPublicBanner(false);
            }
            if ((!bannerMap.get(structure).hasPrivateBanner()) && (!bannerMap.get(structure).hasPublicBanner())) {
                bannerMap.remove(structure);
            }
            bannerPool.removeBanner(structure, removePrivate);

        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener#bannerAdded
     * (edu.udo.scaffoldhunter.model.db.Banner)
     */
    @Override
    public void bannerAdded(Banner banner) {
        boolean exists = false;
        if (bannerMap.containsKey(banner.getStructure())) {
            if (banner.isPrivate()) {
                exists = bannerMap.get(banner.getStructure()).hasPrivateBanner();
            } else {
                exists = bannerMap.get(banner.getStructure()).hasPublicBanner();
            }
        }
        if (!exists) {
            // searches the whole tree for the structure with the banner
            DendrogramViewNode node = dendrogramViewRoot.searchForStructure(banner.getStructure());
            if (node != null) {
                bannerMap.put(banner.getStructure(), node);
                if (banner.isPrivate()) {
                    node.setHasPrivateBanner(true);
                } else {
                    node.setHasPublicBanner(true);
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener#bannerRemoved
     * (edu.udo.scaffoldhunter.model.db.Banner)
     */
    @Override
    public void bannerRemoved(Banner banner) {
        if (bannerMap.containsKey(banner.getStructure())) {
            if (banner.isPrivate()) {
                bannerMap.get(banner.getStructure()).setHasPrivateBanner(false);
            } else {
                bannerMap.get(banner.getStructure()).setHasPublicBanner(false);
            }
            if ((!bannerMap.get(banner.getStructure()).hasPrivateBanner())
                    && (!bannerMap.get(banner.getStructure()).hasPublicBanner())) {
                bannerMap.remove(banner.getStructure());
            }
        }
    }

    /**
     * @return the actual bar position
     */
    public double getClusterBarPosition() {
        return selectionBar.getBarPosition();
    }

    /**
     * should be called if the vieState wants to set a saved Zoomlevel
     */
    // public void setStartZoomSet() {
    // canvasResizeListener.setIsStartZoomSet(true);
    // }

    /**
     * @param selectionbarPosition
     */
    public void setSelectionbarPosition(double selectionbarPosition) {
        selectionBar.setBarPosition(selectionbarPosition);

    }

    /**
     * @return the minimal SVG width of the tree
     */
    public int getMinSVGWidth() {
        return minSVGWidth;
    }
}
