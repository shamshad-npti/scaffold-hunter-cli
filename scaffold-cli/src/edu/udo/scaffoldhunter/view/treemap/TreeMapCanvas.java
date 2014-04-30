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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.view.scaffoldtree.UnscaledNode;
import edu.udo.scaffoldhunter.view.treemap.nodes.TreeMapNode;
import edu.udo.scaffoldhunter.view.treemap.nodes.TreeMapRootNode;
import edu.udo.scaffoldhunter.view.treemap.nodes.TreeMapTextNode;
import edu.udo.scaffoldhunter.view.treemap.sidebar.TreeMapPickChangeListener;
import edu.udo.scaffoldhunter.view.util.AbstractIndependentCanvas;
import edu.udo.scaffoldhunter.view.util.ExportCamera;
import edu.udo.scaffoldhunter.view.util.MiniMap;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGGenerator;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.event.PBasicInputEventHandler;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Lappie
 * 
 */
public class TreeMapCanvas extends AbstractIndependentCanvas {

    /**
     * The width of the TREEMAP in pixels
     */
    public final static int TREEMAP_WIDTH = 1600;

    /**
     * The height of the TREEMAP in pixels
     */
    public final static int TREEMAP_HEIGHT = 1000;
    
    /**
     * The free space around a node in percentage when centering view on it. 
     */
    private final static double FREE_SPACE_AROUND_NODE = 0.05;
    
    /**
     * The animation speed in milliseconds
     */
    private final int SPEED = 1000;

    private PCamera camera;
    private TreeMapRootNode rootNode;
    private TreeMapLayouter layouter;
    
    /**
     * The possible animation currently in progress
     */
    private PTransformActivity currentAnimation;
    
    private PropertyChangeListener innitializationListener = new InitializationListener();
    
    List <TreeMapPickChangeListener> pickChangeListenerList = new ArrayList <TreeMapPickChangeListener> ();
    
    /**
     * if interaction took place, the innitializationListener doesn't have to center anymore.  
     * this flag will make sure of that. 
     */
    private boolean interactionTookPlace = false;
    
    private boolean loading = false; //if loading
    private boolean noSizes = false; //if children shouldn't be painted
    
    /**
     * If true, display molecule-nodes in the scaffold-leafs. 
     * If not, display a scaffold SVG in the scaffold leafs. 
     */
    private boolean displayMolecules = false; 
    
    /**
     * Create and set up basic data for the tree map
     * 
     * NOTE: Make sure to add the TreeMapCanvasResizeListener, since it is necessary for some of
     * the functionality of this TreeMap (especially the semantic zoom)
     * @param selection the selected molecules
     * @param root the root scaffold
     * @param config The Global View settings
     * @param svgCache to load the scaffold svg's
     * @param subset the view's current subset
     */
    public TreeMapCanvas(Scaffold root, Selection selection, GlobalConfig config, SVGCache svgCache, Subset subset) {
        super();
        
        Dimension dimension = new Dimension(1,1); //some default value, this will be updated by the resize listener soon enough
        camera = getCamera();
        
        rootNode = new TreeMapRootNode(root, selection, config, dimension, svgCache, subset, this);
        getLayer().addChild(rootNode);
        
        layouter = new TreeMapLayouterSquarify();

        removeInputEventListener(getZoomEventHandler());
        createListeners();
    }
    
    /**
     * Tell this TreeMap to load a new rootNode. 
     * e.g. a different subset is loaded
     * @param root
     */
    public void loadNewScaffold(Scaffold root) {
        rootNode.loadScaffold(root);
        rootNode.validateSelectionOfAllNodes();
    }
    
    /**
     * Resets the subset reference for the root node.
     * @param subset the new subset
     */
    public void refreshSubset(Subset subset) {
        rootNode.setSubset(subset);
    }
    
    /**
     * Reposition the nodes of this canvas. 
     * Call this when the node properties have been changed. 
     */
    public void reposition() {
        layouter.layoutNodes(rootNode);
    }
    
    /**
     * For the loaded tree load the molecule leafs in it. 
     * @param displayMolecules true if leaf-molecules should be displayed
     */
    public void setDisplayMoleculeLeafs(boolean displayMolecules) {
        this.displayMolecules = displayMolecules;  
    }
    
    /**
     * Wether or not to display the leaf-molecules. 
     * @return true if leaf molecules should be displayed
     */
    public boolean getDisplayMolecules() {
        return displayMolecules;
    }
    
    /**
     * After the scaffolds are other data has changed call this function to recalculate and redraw
     * Note that this will position all elements again and this is a timefull operation. 
     * Use repaint if you just want to redraw;
     */
    public void stopLoading() {
        loading = false;
        
        if(!noSizes)
            rootNode.onlyDrawRootNode(false);
        repaint();
    }
    
    /**
     * Don't display all the treemapnodes, just the empty scaffold rootNode. 
     * @param noSizes
     */
    public void noSizes(boolean noSizes) {
        this.noSizes = noSizes;
        if(noSizes)
            rootNode.onlyDrawRootNode(true);
        else if (!loading)
            rootNode.onlyDrawRootNode(false);
    }
    
    /**
     * Load an empty canvas, for in case there is nothing to show. 
     * Reset by calling reload
     */
    public void startLoading() {
        loading = true;
        rootNode.onlyDrawRootNode(true);
        repaint();
    }
    
    /**
     * @return the root of this Treemap
     */
    public TreeMapRootNode getRootNode() {
        return rootNode;
    }
    
    private void createListeners() {
        camera.addInputEventListener(new PBasicInputEventHandler() {
            @Override
            public void mouseWheelRotated(final PInputEvent event) {
                interactionTookPlace = true;
                if(currentAnimation != null) {
                    currentAnimation.terminate(PActivity.TERMINATE_WITHOUT_FINISHING);
                    currentAnimation = null;
                }
                Point2D point = event.getPosition();
                if (event.getWheelRotation() == 1)
                    zoomOut(point);
                else
                    zoomIn(point);
            }
            
            @Override
            public void mousePressed(final PInputEvent event) {
                interactionTookPlace = true;
            }
            
            @Override
            public void mouseReleased(final PInputEvent event) {
                interactionTookPlace = true;
            }

            @Override
            public void mouseClicked(final PInputEvent event) {
                interactionTookPlace = true;
                if(event.isLeftMouseButton()) {
                    PNode node = event.getPickedNode();
                    if(node instanceof TreeMapTextNode) {
                     // if text node has been clicked, then pretend as if it was its surrounding TreeMapNode
                        node = node.getParent();
                    }
                    if(node instanceof TreeMapNode) {
                        TreeMapNode clickedNode = findClickedNode((TreeMapNode) node);
                        clickedNode.toggleSelection();
                    }
                } else if (event.isRightMouseButton())
                    handleRightMouseButton(event);
            }
            
            @Override
            public void mouseMoved(final PInputEvent event) {
                PNode node = event.getPickedNode();
                if(node instanceof TreeMapTextNode) {
                 // if text node has been moved over, then pretend as if it was its surrounding TreeMapNode
                    node = node.getParent();
                }
                if(node instanceof TreeMapNode) {
                    TreeMapNode pickedNode = findClickedNode((TreeMapNode) node); 
                    
                    if(!pickedNode.getInnerBounds().contains(event.getPosition())
                            /*|| (pickedNode.isMoleculeNode() && !displayMolecules)*/ ) {
                        pickedNode = pickedNode.getTreeMapParent();
                    }
                    firePickChange(pickedNode);
                }
                else
                    firePickChange(null);
            }
        });
        
        //load the camera to the right point
        getCamera().addPropertyChangeListener(PCamera.PROPERTY_BOUNDS, innitializationListener);
    }
    
    /**
    *
    * @param pickChangeListener
    */
   public void addPickChangeListener(TreeMapPickChangeListener pickChangeListener ) {
       if( pickChangeListener == null )
           return;
       if(!pickChangeListenerList.contains(pickChangeListener)) {
           pickChangeListenerList.add( pickChangeListener );
       }
   }

   /**
    *
    * @param pickChangeListener
    */
   public void removePickChangeListener(TreeMapPickChangeListener pickChangeListener ) {
       if( pickChangeListener == null )
           return;
       if( pickChangeListenerList.contains(pickChangeListener)) {
           pickChangeListenerList.remove( pickChangeListener );
       }
   }
    
    private void firePickChange(TreeMapNode node) {
        // call listeners
        for( TreeMapPickChangeListener listener : pickChangeListenerList ) {
            listener.pickChanged(node);
        }
    }
    
    /**
     * This function will find of a range of parents which TreeMapNode is currently being drawn, 
     * and hence has to be the one the user meant by clicking. 
     * @param treeMapNode the deepest TreeMapNode, the one clicked
     * @return the first treeMapNode or one of it's parent that is being drawn. If all else fails, mainNode is returned
     */
    private TreeMapNode findClickedNode(TreeMapNode treeMapNode) {
        if(treeMapNode.hasBeenDrawn()) {
            return treeMapNode;
        }
        TreeMapNode parent = treeMapNode.getTreeMapParent();
        while(parent != null) {
            if(parent.hasBeenDrawn()) {
                return parent;
            }
            parent = parent.getTreeMapParent();
        }
        
        return rootNode;
    }
    
    /**
     * This function will find of a range of parents which TreeMapNode is currently being drawn, 
     * and hence has to be the one the user meant by clicking. 
     * @param treeMapNode the deepest TreeMapNode, the one clicked
     * @return the first treeMapNode or one of it's parent that is being drawn. If all else fails, mainNode is returned
     */
    private TreeMapNode findClickedNodeMinimap(TreeMapNode treeMapNode, PCamera camera) {
        if(treeMapNode.isDrawn(camera))
            return treeMapNode;
        TreeMapNode parent = treeMapNode.getTreeMapParent();
        while(parent != null) {
            if(parent.isDrawn(camera))
                return parent;
            parent = parent.getTreeMapParent();
        }
        return rootNode;
    }

    /**
     * Center the view around the given node
     * 
     * @param node
     *            the node to view
     */
    private void centerView(PNode node, boolean animate) {
        double width = node.getBounds().width;
        double height = node.getBounds().height;
        Rectangle2D centerView = new Rectangle2D.Double(
                node.getBounds().x - (width * FREE_SPACE_AROUND_NODE), 
                node.getBounds().y - (height * FREE_SPACE_AROUND_NODE), 
                node.getBounds().width + (width * 2*FREE_SPACE_AROUND_NODE),
                node.getBounds().height + (height * 2*FREE_SPACE_AROUND_NODE));
        currentAnimation = camera.animateViewToCenterBounds(centerView, true, animate ? SPEED : 0);
    }
    
    /**
     * Initialize this canvas.
     * Necessary to zoom to the correct level. 
     */
    public void innitialize() {
        centerView(rootNode, false);
    }
    
    /**
     * Update the dimension through which this canvas is being viewed at. 
     * @param dimension through which is viewed
     */
    public void updateDimension(Dimension dimension) {
        rootNode.updateViewerDimension(dimension);
    }

    /**
     * Destroy all data
     */
    public void destroy() {
        rootNode.destroy();
    }
    
    @Override
    public PLayer getNodeLayer() {
        return getLayer(); // we only use one layer
    }

    @Override
    public void zoomIn(Point2D position) {
        camera.scaleViewAboutPoint(1.1, position.getX(), position.getY());
    }

    @Override
    public void zoomOut(Point2D position) {
        camera.scaleViewAboutPoint(.9, position.getX(), position.getY());
    }
    
    /**
     * Zoom to an overview on the rootNode
     */
    public void zoomToOverview() {
        centerView(rootNode, true);
    }
    
    /**
     * Zoom and pan to the level where all selected nodes are visible. 
     */
    public void zoomToSelectionOverview() {
        PBounds overviewBounds = new PBounds();
        boolean found = false;
        for (TreeMapNode node : rootNode.getAllTreeMapChildrenNodes()) {
            if (node.isSelected()) {
                overviewBounds.add(node.getGlobalFullBounds());
                found = true;
            }
        }
        if(!found) // don't zoom if nothing selected
            return; 
        overviewBounds.x -= overviewBounds.width * FREE_SPACE_AROUND_NODE;
        overviewBounds.y -= overviewBounds.height * FREE_SPACE_AROUND_NODE;
        overviewBounds.width += overviewBounds.width * 2*FREE_SPACE_AROUND_NODE;
        overviewBounds.height += overviewBounds.height * 2*FREE_SPACE_AROUND_NODE;
        currentAnimation = camera.animateViewToCenterBounds(overviewBounds, true, SPEED);
    }

    @Override
    public void handleRightMouseButton(final PInputEvent event) {
        PNode node = event.getPickedNode();
        
        if (node instanceof PCamera) {
            // if clicked outside the main node
            centerView(rootNode, true);
        } else {
            if(node instanceof TreeMapTextNode) {
                // if text node has been clicked, then pretend as if its surrounding TreeMapNode has been clicked
                node = node.getParent();
            }
            if (node instanceof TreeMapNode) {
                // we want to focus on the node we can see. (I.e. Don't 
                //zoom to nodes you can't see yet due to semantic zoom). Find that one
                
                TreeMapNode clickedNode = null;
                String cameraName = event.getCamera().getName();
                if(cameraName != null && cameraName.equals(MiniMap.CAMERA_NAME))
                    clickedNode = findClickedNodeMinimap((TreeMapNode) node, event.getCamera());
                else
                    clickedNode = findClickedNode((TreeMapNode) node);
                if(!clickedNode.getInnerBounds().contains(event.getPosition())) {
                    clickedNode = clickedNode.getTreeMapParent();
                }
                centerView(clickedNode, true);
            }
        }
    }
    
    /**
     * Listen to property changes from this camera and updates the center of the treemap
     * This listener is required during initialization only because centering 
     * the camera can only be done after all other paint jobs are done
     */
    private class InitializationListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if(!interactionTookPlace) {
                innitialize();
                getCamera().removePropertyChangeListener(innitializationListener); //all is done
            }
        }
    }
    
    /**
     * Focus on the given molecule
     * @param molecule
     */
    public void focusOnMolecule(Molecule molecule) {
        TreeMapNode node = rootNode.getNodeWith(molecule);
        if(node != null)
            centerView(node, true);
    }
    //****************************************************************
    // Export
    //****************************************************************

    /**
     * Paints the specified area to the given <code>Graphics</code>
     * context. +/- Symbols will not be painted and semantic zoom is
     * disabled.
     * @param graphArea the area that will be painted in the coordinate system
     * of the layers
     * @param g Graphics context on which to paint
     * @param paintArea the area that may be painted on. the specified
     * <code>graphArea</code> will be scaled to fit. The aspect ratio will not
     * be altered. If <code>null</code> no scaling and no translation will be
     * done.
     * @return the area really used for painting. This rectangle lies within
     * <code>paintArea</code> and shares two sides with it while the others
     * depend on on the ratio of <code>graphArea</code>.
     */
    public Rectangle2D exportPaint(Rectangle2D graphArea, Graphics2D g, Rectangle2D paintArea) {

        // set up graphics context (translate, scale)
        PAffineTransform at = new PAffineTransform();
        
        if (paintArea != null) {
            at.translate(paintArea.getX(), paintArea.getY());
            double scale = Math.min(paintArea.getWidth()/graphArea.getWidth(),
                    paintArea.getHeight()/graphArea.getHeight());
            at.scale(scale, scale);
            g.transform(at);
        }
        
        Rectangle2D outline = new Rectangle2D.Double(0, 0, graphArea.getWidth(), graphArea.getHeight());
        g.setClip(outline);
        PPaintContext paintContext = new PPaintContext(g);

        ExportCamera exportCam = new ExportCamera();
        @SuppressWarnings("unchecked")
        List<PLayer> layersReference = getCamera().getLayersReference();
        for (PLayer layer : layersReference) {
            exportCam.addLayer(layer);
        }
        
        ArrayList<UnscaledNode> unscaledNodes = new ArrayList<UnscaledNode>();
        
        exportCam.setBounds(outline);
        exportCam.setViewBounds(graphArea);
        
        g.setPaint(Color.white);
        g.fill(outline);
        exportCam.exportPaint(paintContext, unscaledNodes);

        while (exportCam.getLayerCount() > 0) {
            exportCam.removeLayer(exportCam.getLayerCount() - 1);
        }

        return at.transform(outline, null);
    }

    /**
     * Creates a SVG document displaying tree.
     * 
     * @param dim 
     *            dimensions of the SVG root node
     * @param exportAll 
     *            if <code>true</code> the SVG will contain the whole tree,
     *            otherwise it contains the current view 
     * @return a SVG generator whose document displays the tree
     *
     */
    public SVGGenerator exportSVG(Dimension dim, boolean exportAll) {
        
        SVGGenerator svgGen = new SVGGenerator();
        Rectangle2D.Double paintArea = new Rectangle2D.Double(0, 0, dim.getWidth(), dim.getHeight());
        Rectangle2D area;
        if (exportAll) {
            area = exportPaint(getCamera().getUnionOfLayerFullBounds(), svgGen.getGraphics(), paintArea);
        } else {
            area = exportPaint(getCamera().getViewBounds(), svgGen.getGraphics(), paintArea);
        }
        svgGen.setSVGCanvasSize((int)area.getWidth(), (int)area.getHeight());
        return svgGen;
    }


    /**
     * @return the dimension of the whole graph.
     */
    public Dimension getExportAllDimension() {
        int x = (int) (getCamera().getUnionOfLayerFullBounds().getWidth() * getCamera().getViewScale());
        int y = (int) (getCamera().getUnionOfLayerFullBounds().getHeight() * getCamera().getViewScale());
        return new Dimension(x, y);
    }

    /**
     * @return the dimension of the current viewport of the canvas.
     */
    public Dimension getExportScreenDimension() {
        int x = (int) (getCamera().getViewBounds().getWidth() * getCamera().getViewScale());
        int y = (int) (getCamera().getViewBounds().getHeight() * getCamera().getViewScale());
        return new Dimension(x, y);
    }
}
