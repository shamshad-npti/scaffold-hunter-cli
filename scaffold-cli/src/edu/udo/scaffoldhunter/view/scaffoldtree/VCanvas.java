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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.view.RenderingQuality;
import edu.udo.scaffoldhunter.view.util.AbstractIndependentCanvas;
import edu.udo.scaffoldhunter.view.util.ExportCamera;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGGenerator;
import edu.udo.scaffoldhunter.view.util.SelectionState;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.PLayer;
import edu.umd.cs.piccolo.activities.PActivity;
import edu.umd.cs.piccolo.activities.PActivity.PActivityDelegate;
import edu.umd.cs.piccolo.activities.PTransformActivity;
import edu.umd.cs.piccolo.event.PInputEvent;
import edu.umd.cs.piccolo.util.PAffineTransform;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;


/**
 * <b>VCanvas</b> is a Swing component that can be embedded into
 * a Java Swing application (e.g. into JTabbedPane). VCanvas will display
 * a subset of the underlying DTree and forwards mouse and keyboard events
 * to the camera to allow interaction like pan, zoom and show/hide
 * subtrees.
 * This class also offers methods to manipulate the tree and the view on
 * the tree.
 * @author Gorecki
 * @author Kriege
 * @author Schrader
 * @author Wiesniewski
 * @author Rakov
 * @author Henning Garus
 */
public class VCanvas extends AbstractIndependentCanvas {
    /** Used to listen for changes of the layout property */
    public static final String LAYOUT_CHANGING = "layoutChanging";
    /** Used to listen for changes of the number of displayed scaffolds */
    public static final String SCAFFOLD_COUNT = "scaffoldCount";
    /** Used to listen for changes to the zoom factor */
    public static final String ZOOM_FACTOR = "zoomFactor";
    
    /**  A Scaffold has been selected  */
    public static final String SELECTED_SCAFFOLD = "selectedScaffold";
    /**  A Scaffold has been deselected  */
    public static final String DESELECTED_SCAFFOLD = "deselectedScaffold";
    
    /**
     *  The name of the "main" camera, the camera belonging to this canvas,
     *  which is returned when getName is called on the camera.
     */
    public static final String MAIN_CAMERA = "main";
    
    private static Logger logger = LoggerFactory.getLogger(VCanvas.class);

    //configuration from VISControl
    
    private boolean cursorAnimation = false;
    private boolean focusAfterAction = true;
    
    private boolean cursorSiblingWraparound = false;
    //TODO find a better place, only used by radial layouts
    private boolean hideSubtreeEdges=true;
    
    private boolean zoomOnResize = false;
    
    private final ScaffoldTreeViewState state;
    private final Selection selection;
    private Tree scaffoldTree;
    private VTree vtree;
    private PLayer nodelayer;
    private PLayer edgelayer;

    private VZoomHandler zoomhandler;
    private PCamera camera;
    private VAnimation animation;
    private VActivity updateActivity;
    
    private Sorting sorting;

    private int scaffoldCount = 0;
    //TODO private, or move to dbtree instead

    /**
     * A list of nodes that are currently displayed on the canvas
     */
    private HashSet<ScaffoldNode> visibleNodes = new HashSet<ScaffoldNode>();

    /**
     * If this flag is <b>true</b> the list of visible nodes will
     * be updated during the next call of getVisibleNodes().
     */
    private boolean visibleNodesInvalid = true;

    /**
     * The ScaffoldID of the VNode where the actual cursor position is.
     */
    private VNode cursor;

    
    /**
     * Create and set up basic scene graph, install event handlers.
     * @param svgCache the cache used to hold svgs of the scaffolds
     * @param selection 
     * @param bannerPool 
     * @param globalConfig 
     * @param state 
     */
    public VCanvas(SVGCache svgCache, Selection selection, BannerPool bannerPool, GlobalConfig globalConfig, ScaffoldTreeViewState state) {
        super();
        this.state = state;
        this.selection = selection;
        initializeSceneGraph(svgCache);
        this.vtree = new VTree(this, svgCache, selection, bannerPool, globalConfig);
        vtree.setShowScaffoldDetailsNodes(state.isShowDetailsNodes());
    }

    /**
     * Displays a subtree of the given Tree defined by a
     * root node and the maximum height of the subtree.
     * 
     * Note: If this method is called before the canvas has been
     * embedded in a Swing Component, the camera position might
     * be inappropriate.
     * 
     * @param scaffoldTree the underlying DTree
     * @param root the node that becomes the root of the subtree
     * @param levels the maximum depth of a node in the subtree
     */
    public void showTree(Tree scaffoldTree, Scaffold root, int levels) {
        Preconditions.checkState(vtree.getLayout() != null, "No layout set for the vtree");
        Preconditions.checkState(vtree.getRoot() == null, "VTree not empty.");

        this.scaffoldTree = scaffoldTree;
        ScaffoldNode vroot = vtree.createVRoot(root);
        vtree.buildBranch(vroot, levels);
        initialize();
    }
    
    /**
     * Displays a subtree of the given Tree defined by a
     * root node and the state.
     * 
     * Note: If this method is called before the canvas has been
     * embedded in a Swing Component, the camera position might
     * be inappropriate.
     * 
     * @param scaffoldTree the underlying DTree
     * @param root the node that becomes the root of the subtree
     * @param state the view state
     */
    public void showTree(Tree scaffoldTree, Scaffold root, ScaffoldTreeViewState state) {
        if (vtree.getRoot()!=null) {
            throw new IllegalStateException("VTree not empty.");
        }

        this.scaffoldTree = scaffoldTree;
        ScaffoldNode vroot = vtree.createVRoot(root);
        vtree.buildBranch(vroot, state);
        initialize();
    }

    /**
     * Displays a subtree of the given DTree defined by a set of nodes.
     * Additional nodes are added to assure connectivity.
     * 
     * Note: If this method is called before the canvas has been
     * embedded in a Swing Component, the camera position might
     * be inappropriate.
     * 
     * @param scaffoldTree the underlying Tree
     * @param scaffolds identifies the nodes that should be part of the subtree
     */
    public void showScaffolds(Tree scaffoldTree, Collection<Scaffold> scaffolds) {
        if (vtree.getRoot()!=null) {
            throw new IllegalStateException("VTree not empty.");
        }

        this.scaffoldTree = scaffoldTree;
        // find nodes in the dtree
        Set<Scaffold> allowedScaffolds = new HashSet<Scaffold>();
        
        //allowedScaffolds.retainAll(scaffoldTree.getScaffolds());
        if (allowedScaffolds.isEmpty())
            return;
        // build a tree containing all the allowed scaffolds
        Iterator<Scaffold> sit = allowedScaffolds.iterator();
        vtree.createVRoot(sit.next());
        while (sit.hasNext())
            vtree.installVNode(sit.next(), false);
        initialize();
    }

    /**
     * Create and set up basic scene graph, install event handlers etc.
     */
    private void initializeSceneGraph(SVGCache svgCache) {
        this.camera = getCamera();
        camera.setName(VCanvas.MAIN_CAMERA);
        this.nodelayer = camera.getLayer(0);
        this.edgelayer = new PLayer();
        getRoot().addChild(edgelayer);

        this.zoomhandler = new VZoomHandler(this); // zoom

        camera.addLayer(0,edgelayer);
        camera.addInputEventListener(zoomhandler);
        
        // apply customized view constraint
        camera.addPropertyChangeListener(PCamera.PROPERTY_VIEW_TRANSFORM, new PropertyChangeListener() {
            public final static int MIN_CONTENT_WIDTH = 200;
            boolean recursion = false;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (recursion || camera.getViewScale() == 0) return;
                
                final PBounds viewBounds = camera.getViewBounds();
                final PBounds layerBounds = (PBounds) camera.globalToLocal(nodelayer.getFullBounds());

                double dw = MIN_CONTENT_WIDTH/camera.getViewScale();
                
                double lix = layerBounds.getMinX();
                double lxx = layerBounds.getMaxX();
                double cix = viewBounds.getMinX();
                double cxx = viewBounds.getMaxX();
                double liy = layerBounds.getMinY();
                double lxy = layerBounds.getMaxY();
                double ciy = viewBounds.getMinY();
                double cxy = viewBounds.getMaxY();

                double deltaX = 0, deltaY = 0;
                if (cxx < lix + dw) deltaX = lix + dw - cxx;
                if (cix > lxx - dw) deltaX = lxx - dw - cix;
                if (cxy < liy + dw) deltaY = liy + dw - cxy;
                if (ciy > lxy - dw) deltaY = lxy - dw - ciy;
                
                // this flag avoids infinite recursions since each translateView leads
                // to another property change again
                recursion = true;
                camera.translateView(-deltaX, -deltaY);
                recursion = false;
            }
        });
        
        this.addKeyListener(new CursorListener());

        this.setZoomEventHandler(null);

        animation = new VAnimation(this);

        updateActivity = new VActivity(this);

    }

    /**
     * Runs initial layout, etc.
     */
    private void initialize () {
        // start the update activity
        getRoot().getActivityScheduler().addActivity(updateActivity, true);

        vtree.getLayout().doLayout(true);
        updateScaffoldCount();
        initCursor();
    }

    /**
     * Removes all references to this <b>VCanvas</b>.
     */
    @SuppressWarnings("deprecation")
    public void dispose() {
        // stop all activities
        getRoot().getActivityScheduler().removeAllActivities();

        // PCanvas sets a static reference CURRENT_ZCANVAS to every
        // canvas at creation time. This may result in a memory leak if
        // a canvas is created and closed again. Setting the inherited
        // reference to null should prevent this.
        if (PCanvas.CURRENT_ZCANVAS == this)
            PCanvas.CURRENT_ZCANVAS = null;

        removeInputSources();
    }

    //****************************************************************
    // Zooming
    //****************************************************************


    /**
     * Interface for GUI; zooms in at center view position
     * @param viewzoompoint
     */
    @Override
    public void zoomIn (Point2D viewzoompoint) {
        zoomhandler.zoomIn(camera, viewzoompoint);
    }

    /**
     * Interface for GUI; zooms out at center view position
     * @param viewzoompoint
     */
    @Override
    public void zoomOut (Point2D viewzoompoint) {
        zoomhandler.zoomOut(camera, viewzoompoint);
    }

    /**
     * Interface for GUI; zooms to zoomfactor at center view position
     * @param viewzoompoint
     * @param zoomfactor in percent
     */
    public void setZoomFactor (Point2D viewzoompoint, int zoomfactor) {
        viewzoompoint = camera.localToView(viewzoompoint);
        zoomhandler.setZoom(camera, viewzoompoint, zoomfactor);
    }

    /**
     * Returns the zoomfactor.
     * @return the current dynamic zoomfactor in percent
     */
    public double getZoomFactor () {
        return zoomhandler.getZoom(camera);
    }

    /**
     * Zooms to an overview of the whole graph.
     */
    public void zoomToOverview () {
        zoomToOverview(false);
    }

    /**
     * Zooms to an overview of the whole graph.
     * @param animationDisabled disables the animation while zooming to the overview
     */
    public void zoomToOverview(boolean animationDisabled) {
        setZoomOnResize(false);
        int speed;
        if (animationDisabled)
            speed = 0;
        else
            speed = cameraAnimation ? animationSpeed : 0;

        PActivity a = camera.animateViewToCenterBounds(nodelayer.getFullBounds().getBounds2D(), true, speed);
        if (a != null) {
            a.setDelegate(new PActivityDelegate() {

                int stepsBetweenUpdates;
                int i = 0;
                
                @Override
                public void activityStepped(PActivity activity) {
                    if (i < stepsBetweenUpdates) {
                        i++;
                    } else {
                        zoomhandler.setOverviewScale(camera);
                        updateLayout();
                        i = 0;
                    }
                }

                @Override
                public void activityStarted(PActivity activity) {
                    stepsBetweenUpdates = 50 / (int)activity.getStepRate();
                }

                @Override
                public void activityFinished(PActivity activity) {
                    updateLayout();
                    zoomToOverview(true);
                    setZoomOnResize(true);
                }
            });
        } else {
            updateLayout();
            camera.animateViewToCenterBounds(nodelayer.getFullBounds().getBounds2D(), true, 0);
            setZoomOnResize(true);
        }
        zoomhandler.setOverviewScale(camera); // this piccolo scale is now equal to 100%
        firePropertyChange(ZOOM_FACTOR, 0d, 100d);
    }
    
    /**
     * Zooms to the bounding box containing all selected (and expanded) Scaffolds
     * @param animationDisabled disables the animation during zoom
     */
    public void zoomToSelectionOverview(boolean animationDisabled) {
        int speed;
        if (animationDisabled)
            speed = 0;
        else
            speed = cameraAnimation ? animationSpeed : 0;
        
        PBounds overviewBounds = new PBounds();
        Set<SelectionState> selected = EnumSet.of(SelectionState.HALFSELECTED, SelectionState.SELECTED);
        for (ScaffoldNode n : vtree.getVNodes()) {
            if (selected.contains(n.getSelection())) {
                overviewBounds.add(n.getGlobalFullBounds());
            }
        }
        camera.animateViewToCenterBounds(overviewBounds, true, speed);
    }
    
    /**
     * Zooms to overview if and only if auto-zoom on resize is active in the canvas. Performs no animation.
     */
    public void autoZoomToOverview() {
        if(isZoomOnResize()) {
            zoomToOverview(true);
        }
    }

    /**
     * Enables/disables zoom possibility in this VCanvas
     * @param b if true zoom is enabled
     */
    public void enableZoom (boolean b) {
        zoomhandler.setZoomActive(b);
    }

    /**
     * Checks whether the glass is visible or not
     * @return visible or not?
     */
    public boolean magnifyingGlassEnabled () {
        return true;
    }


    //****************************************************************
    // Cursor
    //****************************************************************


    /**
     * This methods initializes the cursor.
     * First it tries to set the cursor to the home molecule.
     * If the home molecule is not defined or not in the actual
     * VTree and the root is imaginary, the first child of
     * the root is the cursornode.
     * If the root is not imaginary, the VTree is a subtree and the
     * root is the cursornode.
     */
    public void initCursor() {
        VNode newCursor;
        if (!vtree.getRoot().getScaffold().isImaginaryRoot()) {
            newCursor = vtree.getRoot();
        } else if (!vtree.getRoot().getTreeChildren().isEmpty()) {
            newCursor = vtree.getRoot().getTreeChildren().get(0);
        } else {
            // this should only occur with an empty vtree
            return;
        }

        newCursor.setCursor(true);
        cursor = newCursor;
    }

    /**
     * Sets the cursor to the node associated with the given
     * <code>Scaffold</code>.
     * 
     * @param scaffold the scaffold whose node the cursor is set on
     */
    public void setCursor(Scaffold scaffold) {
        // assure that the id is valid
        if (!vtree.containsScaffold(scaffold)) {
            return;
        }
        VNode oldCursorNode = cursor;
        if (oldCursorNode != null) {
            oldCursorNode.setCursor(false); // deletes actual cursor
        }
        cursor = vtree.getVNode(scaffold);
        vtree.getVNode(scaffold).setCursor(true);
        focusOnCursor();
    }

    /**
     * Sets the cursor to the first child of the actual cursornode.
     */
    public void setCursorToFirstChild() {
        VNode oldCursor = cursor;
        // assure that a valid child exists
        if (oldCursor == null || oldCursor.getTreeChildren().isEmpty()) {
            return;
        }
        setNewCursor(oldCursor.getTreeChildren().get(0));
        focusOnCursor();
    }

    /**
     * Sets the cursor to the parent of the current cursor node.
     */
    public void setCursorToParent() {
        VNode cursorNode = cursor;
        // assure that a valid parent exists
        if (cursorNode == null || cursorNode == vtree.getRoot()) {
            return;
        }

        VNode parent = cursorNode.getTreeParent();
        if ((parent instanceof ScaffoldNode) && !((ScaffoldNode)parent).getScaffold().isImaginaryRoot()) {
            setNewCursor(parent);
            focusOnCursor();
        }
    }

    /**
     * Sets the cursor to the right sibling of the current cursor node.
     */
    public void setCursorToRightSibling() {
        VNode nextChild = cursor.getClockwiseSibling();
        setNewCursor(nextChild);
        focusOnCursor();
    }

    /**
     * Sets the cursor to the left sibling of the current cursor node.
     */
    public void setCursorToLeftSibling() {
        VNode nextChild = cursor.getAnticlockwiseSibling();
        setNewCursor (nextChild);
        focusOnCursor();
    }

    /**
     * Sets the cursor to the right node in the same layer
     * of the current cursor node.
     */
    public void setCursorToRightNode() {
        VNode nextChild = vtree.getRightLayerNode(cursor);
        setNewCursor(nextChild);
        focusOnCursor();
    }

    /**
     * Sets the cursor to the left node in the same layer
     * of the current cursor node.
     */
    public void setCursorToLeftNode() {
        VNode nextChild = vtree.getLeftLayerNode(cursor);
        setNewCursor (nextChild);
        focusOnCursor();
    }

    /**
     * Sets the cursor to the specified <code>VNode</code>
     * 
     * @param newCursorNode the vnode the cursor is set on
     */
    void setNewCursor (VNode vnode) {
        VNode cursorNode = cursor;
        if (cursorNode != null) {
            cursorNode.setCursor(false); // deletes actual cursor
        }
        vnode.setCursor(true);   // sets new cursor
        cursor = vnode;
    }

    /**
     * Returns the current current cursor position.
     * 
     * @return the vnode, which is the current cursor position
     */
    VNode getCursorVNode() {
        return cursor;
    }

    /**
     * Focus the camera on the cursor node.
     */
    public void focusOnCursor() {
        focusOn(cursor, false, cursorAnimation);
    }

    //****************************************************************
    //  Inner Class : Keyboard Listener
    //****************************************************************

    /**
     * This inner class defines the main keyboard control settings.
     * For more details of the function of each key look at the
     * manual.
     */
    private class CursorListener extends KeyAdapter {
        
        private Timer hideDetailsNodes = new Timer(200, new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                vtree.setShowScaffoldDetailsNodes(false);
            }
        });
        
//        int x = getHeight()/2;
//        int y = getWidth()/2;

        @Override
        public void keyPressed(KeyEvent e) {
            switch(e.getKeyCode()) {

            case KeyEvent.VK_LEFT:
                if(e.isControlDown()) {
                    setCursorToLeftSibling();
                } else {
                    if(cursorSiblingWraparound)
                        setCursorToLeftSibling();
                    else
                        setCursorToLeftNode();
                }
                e.consume();
                break;

            case KeyEvent.VK_RIGHT:
                if(e.isControlDown()) {
                    setCursorToRightSibling();
                } else {
                    if(cursorSiblingWraparound)
                        setCursorToRightSibling();
                    else
                        setCursorToRightNode();
                }
                e.consume();
                break;

            case KeyEvent.VK_UP:
                if(e.isControlDown()) {
                    vtree.getLayout().updateRadii(1.1);
                } else {
                    setCursorToFirstChild();
                }
                e.consume();
                break;

            case KeyEvent.VK_DOWN:
                if(e.isControlDown()) {
                    vtree.getLayout().updateRadii(1/1.1);
                } else {
                    setCursorToParent();
                }
                e.consume();
                break;

            case KeyEvent.VK_PAGE_UP:
                if(e.isAltDown()) {
                    scaleSelectedNodes(1.2);
                } else {
                    if(!e.isControlDown()) scaleNode(cursor, 1.2);
                    else
                        System.out.println("tab forward");
                }
                break;

            case KeyEvent.VK_PAGE_DOWN:
                if(e.isAltDown()) {
                    scaleSelectedNodes(0.8);
                } else {
                    if(!e.isControlDown()) scaleNode(cursor, 0.8);
                    else
                        System.out.println("tab backward");
                }
                break;
/*
            case KeyEvent.VK_F:
                vtree.getLayout().setFixedLayout(!vtree.getLayout().getFixedLayout());
                //TODO inform about fixed radius being set (whatever that is)
                e.consume();
                break;
*/
            case KeyEvent.VK_E:
                if (!e.isControlDown()) {
                    if (cursor.isExpandable()) {
                        expandNextLevel(cursor);
                    } else {
                        reduceToThisLevel(cursor);
                    }
                    // e.consume();
                }
                break;

            case KeyEvent.VK_SPACE:
                Set<Molecule> molecules = ((ScaffoldNode)getCursorVNode()).getScaffold().getMolecules();
                if (selection.containsAll(molecules))
                    selection.removeAll(molecules);
                else
                    selection.addAll(molecules);
                break;

            case KeyEvent.VK_ENTER:
                if(e.isControlDown()) {
                    if (cursor.isExpandable()) expandSubtree(cursor);
                    else reduce(cursor);
                } else  {
                    if (cursor.isExpandable()) expand(cursor);
                    else reduce(cursor);
                }
//                e.consume();
                break;

/*
            case KeyEvent.VK_EQUALS:
            case KeyEvent.VK_PLUS:
                zoomIn(new Point(x,y));
                e.consume();
                break;

            case KeyEvent.VK_MINUS:
                zoomOut(new Point(x,y));
                e.consume();
                break;
*/
            case KeyEvent.VK_M:
                hideDetailsNodes.stop();
                if(!e.isControlDown()) {
                    vtree.setShowScaffoldDetailsNodes(true);
                }
                break;

            case KeyEvent.VK_T:
                //TODO Change tooltip configuration Option
                //VISControl.getInstance().setShowTooltip(!VISControl.getInstance().getShowTooltip());
                //TODO show a popup
                e.consume();
                break;

            case KeyEvent.VK_C:
                focusOn(cursor, true);
//                e.consume();
                break;
/*
            case KeyEvent.VK_N:
                if(e.isControlDown()) {
                    normalizeSelectedNodes();
                } else {
                    normalizeNode(cursor);
                }
                break;

                // TODO: assign shortcuts to specific layouts via layout plugin system
                //					case KeyEvent.VK_L:
                //						layoutNum%=3;
                //						switchLayoutTo(layoutNum);
                //						GUIController.getInstance().switchLayoutTo(layoutNum);
                //						layoutNum++;
                //			    		break;

            case KeyEvent.VK_0:
                zoomToOverview();
                e.consume();
                break;
*/
            }
            
            
        }

        /* (non-Javadoc)
         * @see java.awt.event.KeyAdapter#keyReleased(java.awt.event.KeyEvent)
         */
        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
            case KeyEvent.VK_M:
                if (!state.isShowDetailsNodes()) {
                    hideDetailsNodes.start();
                }
            }
        }
    }


    //****************************************************************
    // Selection
    //****************************************************************

    /**
     * Finds all nodes in the rectangle described by start point
     * and end point.
     * 
     * @param start point at which the selection was started
     * @param end point at which the selection has ended
     * @return the nodes inside the rectangle
     */
    public List<Scaffold> findScaffoldsInArea (Point2D start, Point2D end){
        List<Scaffold> selectionList = Lists.newArrayList();
        for (ScaffoldNode tempNode : vtree.getVNodes()) {
            Point2D node = this.getCamera().viewToLocal(tempNode.getFullBoundsReference().getCenter2D());
            if (((node.getX() > start.getX()) && (node.getX() < end.getX())) ||
                    ((node.getX() < start.getX()) && (node.getX() > end.getX())))
                if (((node.getY() > start.getY()) && (node.getY() < end.getY())) ||
                        ((node.getY() < start.getY()) && (node.getY() > end.getY())))
                    if (!tempNode.getScaffold().isImaginaryRoot())
                        selectionList.add(tempNode.getScaffold());
        }
        return selectionList;
    }
    
    //****************************************************************
    // Rendering & Layout
    //****************************************************************

    /**
     * Change the rendering quality.
     * 
     * @param newquality quality to be set
     */
    public void setRenderingQuality (RenderingQuality newquality) {
        newquality.setQuality(this);
    }



    /**
     * Focus the camera on the given <code>Scaffold</code>. If no
     * <code>VNode</code> with the scaffold is found, one will be added.
     * 
     * @param scaffold the scaffold which the camera will be focused on
     */
    public void focusOn(Scaffold scaffold){
        // expand the displayed tree if it doesn't contain the node with the given id
        if (!vtree.containsScaffold(scaffold)) {
            // add the required node
            getVTree().installVNode(scaffold, true);
            // focus node after animation
            VNode node = vtree.getVNode(scaffold);
            animation.delayedFocusOn(node, true);
        } else {
            focusOn(vtree.getVNode(scaffold), true);
        }
    }

    protected void focusOn(VNode node, boolean scale) {
        focusOn(node.getFullBounds().getCenter2D(), scale, true);
    }

    /**
     * Focus the camera on the <code>VNode v</code>.
     * If <code>boolean scale</code> is <b>true</b> it will be zoomed
     * to the given node.
     * If <code>boolean animation</code> is <b>true</b> the zoom
     * to the given node is animated.
     * @param node
     * @param scale zoom to the given node
     * @param animation animate the zoom to the given node
     */
    public void focusOn(VNode node, boolean scale, boolean animation) {
        focusOn(node.getFullBounds().getCenter2D(), scale, animation);
    }

    /**
     * Moves the camera to the node depicting <code>scaffold</code>. If
     * the current view dows not contain the scaffold, it is added to 
     * the tree. 
     * @param scaffold
     */
    public void panTo(final Scaffold scaffold) {
        if (scaffold.isImaginaryRoot())
            return;
        VNode node = vtree.getVNode(scaffold);
        if (node == null) {
            PActivity a = camera.animateViewToCenterBounds(nodelayer.getFullBounds().getBounds2D(), 
                    true, cameraAnimation ? animationSpeed : 0);
            a.setDelegate(new PActivityDelegate() {
                @Override
                public void activityStepped(PActivity activity) {}
                @Override
                public void activityStarted(PActivity activity) {}
                @Override
                public void activityFinished(PActivity activity) {
                    focusOn(scaffold);
                }
            });
        } else {
            panTo(node.getFullBoundsReference().getCenter2D());
        }
    }

    /**
     * Moves the camera to the coordinates given by <code>p</code>
     * 
     * @param p
     */
    public void panTo(final Point2D p) {
        PBounds viewBounds = camera.getViewBounds();
        Point2D start = viewBounds.getCenter2D();
        double distance = start.distance(p);

        // TODO: determine direction of panning and adjust ratio accordingly
        double ratio = distance / viewBounds.getWidth();

        // zoom out if required
        if (ratio > 1.5) {
            double width = viewBounds.getWidth()*ratio;
            double height = viewBounds.getHeight()*ratio;
            double x = viewBounds.getX() - (width-viewBounds.getWidth())/2;
            double y = viewBounds.getY() - (height-viewBounds.getHeight())/2;
            PBounds zoomOutBounds = new PBounds(x, y, width, height);
            PTransformActivity zoomOutActivity = camera.animateViewToCenterBounds(zoomOutBounds,
                    true, cameraAnimation ? animationSpeed : 0);
            zoomOutActivity.setDelegate(new PActivity.PActivityDelegate() {
                @Override
                public void activityFinished(PActivity activity) {
                    focusOn(p, true, true);
                }
                @Override
                public void activityStarted(PActivity activity) {}
                @Override
                public void activityStepped(PActivity activity) {}
            });
        } else {
            focusOn(p, true, true);
        }
    }        
        
    /**
     * Cuts off a subtree starting at the node associated with the scaffold.
     * @param node root of the subtree to be cut
     * @return <b>true</b> if a node associated with scaffold is displayed and
     * children to remove existed.
     */
    public boolean reduce(VNode node) {
            if(focusAfterAction)
                animation.fixCameraOnNode(node);
            return vtree.reduce(node);
    }

    /**
     * Cuts off a subtree starting at vnode.
     * @param vnode root of the subtree to be cut
     * @return <b>true</b> if vnode had children to reduce
     */
    public boolean reduce(ScaffoldNode vnode) {
        return vtree.reduce(vnode);
    }

    /**
     * Expands the node associated with scaffold by its children.
     * @param node the node to be expanded 
     * @return <b>true</b> if a node representing scaffold was displayed
     * without its children.
     */
    public boolean expand(VNode node) {

        if(focusAfterAction)
            animation.fixCameraOnNode(node);
        return vtree.expand(node);
    }

    /**
     * Expands <code>vnode</code>
     * 
     * @param vnode the node to be expanded
     * @return  <b>true</b> if <code>vnode</code> was displayed
     * without its children.
     */
    public boolean expand(ScaffoldNode vnode) {
        return vtree.expand(vnode);
    }


    /**
     * Expands all nodes on the level of <code>node</code>
     * 
     * @param node
     * @see #expand
     */
    public void expandNextLevel(VNode node){

        // Im BalloonLayout wird nicht die ganze Ebene geöffnet,
        // nur die Geschwister des clickednode.
        if (vtree.getLayout() instanceof VBalloonLayout){
            for (VNode v : node.getTreeParent().getTreeChildren())
                vtree.expand(v);
        } else {
            int level = this.vtree.getNodesDepth(node);
            for(VNode v : vtree.getNodesOnLevel(level))
                vtree.expand(v);
        }

        if(focusAfterAction)
            animation.fixCameraOnNode(node);
    }
    
    /**
     * Expand all nodes in the tree
     * 
     * @param zoomToOverview
     *            should expansion be followed by a zoom to overview? 
     */
    public void expandAll(boolean zoomToOverview) {
        class NodeAddedListener implements VNodeListener {
            boolean nodeAdded = false;

            @Override
            public void vnodeAdded(ScaffoldNode vnode) {
                nodeAdded = true;
            }
            @Override
            public void vnodeRemoved(ScaffoldNode vnode) {    
            }
        }
        NodeAddedListener l = new NodeAddedListener();
        animation.setDelayedOverview(zoomToOverview);
        vtree.addVNodeListener(l);
        vtree.buildBranch(vtree.getRoot(), -1);
        vtree.removeVNodeListener(l);
        // if no nodes were added, the tree was not layed out again and we
        // have to reset this manually
        if (!l.nodeAdded)
            animation.setDelayedOverview(false);
    }

    /**
     * Reduce all nodes below the level of the node associated with scaffold.
     * 
     * @param node
     * 
     * @see #reduce
     */
    public void reduceToThisLevel(VNode node){
        int level = vtree.getNodesDepth(node);
        for (VNode v : vtree.getNodesOnLevel(level))
            vtree.reduce(v);
        if(focusAfterAction)
            animation.fixCameraOnNode(node);
    }

    /**
     * Expands the tree by the entire subtree starting at the node
     * associated with scaffold.
     * @param node the root of the subtree to be expanded
     * @return <b>true</b> if there were nodes to be expanded
     */
    public boolean expandSubtree (VNode node) {
        //Nach dem Aufklappen wird der Knoten neu fokussiert!
        if(focusAfterAction)
            animation.fixCameraOnNode(node);
        return vtree.expandSubtree(node);
    }

    /**
     * Returns the VNodes that are currently viewed by the camera.
     * 
     * @return nodes viewed by the camera
     */
    public Collection<ScaffoldNode> getVisibleNodes() {
        if (visibleNodesInvalid) {
            visibleNodes.clear();
            
            // create a new array list to store results
            ArrayList<ScaffoldNode> visibleNodesList = new ArrayList<ScaffoldNode>();
            getNodeLayer().findIntersectingNodes(getCamera().getViewBounds(), visibleNodesList);
            
            // add list to hash set
            visibleNodes.addAll(visibleNodesList);
            visibleNodesInvalid = false;
        }
        return visibleNodes;
    }

    /**
     * Sets the <code>visibleNodesInvalid</code> flag and the list of
     * visible nodes will be updated during the next call of getVisibleNodes().
     */
    public void invalidateVisibleNodes() {
        visibleNodesInvalid = true;
    }


    /**
     * Checks if the node associated with scaffold is reducible
     * 
     * @param scaffold
     * @return <code>true</code> if the node associated with scaffold is
     *  reducible
     * 
     * @see ScaffoldNode#isReducible
     */
    public boolean isReducible(Scaffold scaffold) {
        if (vtree.containsScaffold(scaffold))
            return vtree.getVNode(scaffold).isReducible();
        else
            return false;
    }

    /**
     *
     * @param scaffold
     * @return <code>true</code> if the node associated with scaffold is
     *  expandable
     * 
     *  @see ScaffoldNode#isExpandable
     */
    public boolean isExpandable(Scaffold scaffold) {
        if (vtree.containsScaffold(scaffold))
            return vtree.getVNode(scaffold).isExpandable();
        else
            return false;
    }

    /**
     * @param scaffold
     * @return <code>true</code> if any nodes in the subtree rooted at scaffold
     * are expandable
     * 
     * @see ScaffoldNode#isExpandable
     */
    public boolean isSubtreeExpandable(Scaffold scaffold) {
        if (vtree.containsScaffold(scaffold))
            return vtree.getVNode(scaffold).isSubtreeExpandable();
        else
            return false;
    }


    /**
     * This method can be used for general updates in the layout algorithm.
     */
    public void updateLayout() {
        vtree.getLayout().updateLayout();
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

        // required by PFixedWidthStroke
//        PDebug.startProcessingOutput();

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

        // temporary camera for export
        ExportCamera exportCam = new ExportCamera();
        @SuppressWarnings("unchecked")
        List<PLayer> layersReference = getCamera().getLayersReference();
        for (PLayer layer : layersReference) {
            exportCam.addLayer(layer);
        }
        // find unscaled nodes
        ArrayList<UnscaledNode> unscaledNodes = new ArrayList<UnscaledNode>();
        for (Object node : camera.getChildrenReference()) {
            if (node instanceof ScaffoldLabel) {
                ScaffoldLabel sl = (ScaffoldLabel) node;
                unscaledNodes.add(sl);
            }
        }
        exportCam.setBounds(outline);
        exportCam.setViewBounds(graphArea);
        
        // paint background
        g.setPaint(Color.white);
        g.fill(outline);

        // paint everything
        exportCam.exportPaint(paintContext, unscaledNodes);

        while (exportCam.getLayerCount() > 0) {
            exportCam.removeLayer(exportCam.getLayerCount() - 1);
        }
        
//        PDebug.endProcessingOutput(g);

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
    
    /**
     * Updates the property SCAFFOLD_COUNT and fires
     * a property change event.
     */
    protected void updateScaffoldCount() {
        int oldValue = scaffoldCount;
        scaffoldCount = vtree.getVNodes().size();
        if (!vtree.isSubtree()) scaffoldCount--;
        firePropertyChange(SCAFFOLD_COUNT, oldValue, scaffoldCount);
    }

    
    //****************************************************************
    // Get & Set
    //****************************************************************

    /**
     * @return the vtree displayed by this canvas
     */
    public VTree getVTree() { return this.vtree; }
    
    /**
     * @return the scaffold tree displayed by this canvas.
     */
    Tree getScaffoldTree() { return this.scaffoldTree; }

    /**
     * @return the node layer of this canvas
     */
    @Override
    public PLayer getNodeLayer() { return this.nodelayer; }
    
    /**
     * @return the edgeLayer of this canvas
     */
    public PLayer getEdgeLayer() { return this.edgelayer; }


    VActivity getVActivity() { return this.updateActivity; }
    VAnimation getVAnimation() { return this.animation;	}
   
    /**
     * @return the number of scaffolds currently displayed on this canvas.
     */
    public int getScaffoldCount() { return scaffoldCount; }

    /**
     * @return a set of all scaffolds which are displayed by this vtree
     */
    public Set<Scaffold> getScaffolds() {
        Set<Scaffold> ret = new HashSet<Scaffold>();
        for(ScaffoldNode v : vtree.getVNodes())
            ret.add(v.getScaffold());
        return ret;
    }

    /**
     * Moves and resizes this component. The new location of the top-left corner
     * is specified by x and y, and the new size is specified by width and height.
     * @param x the new x-coordinate of this component
     * @param y the new y-coordinate of this component
     * @param w the new width of this component
     * @param h the new height of this component
     */
    @Override
    public void setBounds (int x, int y, int h, int w) {
        // negative values here cause the canvas to be mirrored;
        // the effect remains even when height and width are set
        // correctly later.
        if ((h<=0) || (w<=0)) return;
        super.setBounds(x, y, h, w);
        autoZoomToOverview();
    }


    //****************************************************************
    // Scale & Normalize
    //****************************************************************

    /**
     * Scales the VNode <code>v</code> around the <code>factor</code>
     * and focuses him without animation.
     * @param v the node to scale
     * @param factor 1.0 = 100%
     */
    public void scaleNode(VNode v, double factor) {
        if(v!=vtree.getRoot()) {
            v.scaleNode(factor);
            updateLayout();
            focusOn(v,false,false);
        }
    }

    /**
     * Scales all VNodes around the <code>factor</code>.
     * @param factor 1.0 = 100%
     */
    public void scaleAllNodes(double factor) {
        for (VNode node : vtree.getVNodes()) {
            if(node!=vtree.getRoot()) node.setScale(factor);
        }
        updateLayout();
    }

    /**
     * Scales all selected VNodes around the <code>factor</code>.
     * @param factor 1.0 = 100%
     */
    public void scaleSelectedNodes(double factor) {
        for(ScaffoldNode node : vtree.getVNodes()) {
            if(node.getSelection() == SelectionState.SELECTED && node != vtree.getRoot())
                node.scaleNode(factor);
        }
        updateLayout();
    }

    /**
     * Sets the VNode <code>v</code> to the original scale
     * and focuses it without animation.
     * @param v the vnode which is rescaled to the original scale and focused
     */
    public void normalizeNode(VNode v) {
        if(v!=vtree.getRoot() && v.getScale()!=1.0) {
            v.normalizeNode();
            updateLayout();
            focusOn(v,false,false);
        }
    }

    /**
     * Sets all VNodes to the original scale.
     */
    public void normalizeAllNodes() {
        for (ScaffoldNode node : vtree.getVNodes()) {
            if(node!=vtree.getRoot()) node.normalizeNode();
        }
        updateLayout();
    }

    /**
     * Sets all selected VNodes to the original scale.
     */
    public void normalizeSelectedNodes() {
        for(ScaffoldNode node : vtree.getVNodes()) {
            if(node.getSelection() == SelectionState.SELECTED && node !=vtree.getRoot()) 
                node.normalizeNode();
        }
        updateLayout();
    }

    /**
     * @return the cursorAnimation
     */
    public boolean isCursorAnimation() {
        return cursorAnimation;
    }

    /**
     * @param cursorAnimation the cursorAnimation to set
     */
    public void setCursorAnimation(boolean cursorAnimation) {
        this.cursorAnimation = cursorAnimation;
    }

    /**
     * @param focusAfterAction the focusAfterAction to set
     */
    public void setFocusAfterAction(boolean focusAfterAction) {
        this.focusAfterAction = focusAfterAction;
    }

    /**
     * @return the focusAfterAction
     */
    public boolean isFocusAfterAction() {
        return focusAfterAction;
    }

    /**
     * @param cursorSiblingWraparound the cursorSiblingWraparound to set
     */
    public void setCursorSiblingWraparound(boolean cursorSiblingWraparound) {
        this.cursorSiblingWraparound = cursorSiblingWraparound;
    }

    /**
     * @return the cursorSiblingWraparound
     */
    public boolean isCursorSiblingWraparound() {
        return cursorSiblingWraparound;
    }

    /**
     * @param hideSubtreeEdges the hideSubtreeEdges to set
     */
    public void setHideSubtreeEdges(boolean hideSubtreeEdges) {
        this.hideSubtreeEdges = hideSubtreeEdges;
    }

    /**
     * @return the hideSubtreeEdges
     */
    public boolean isHideSubtreeEdges() {
        return hideSubtreeEdges;
    }

    /**
     * @return the zoomhandler
     */
    public VZoomHandler getZoomhandler() {
        return zoomhandler;
    }

    @Override
    public void handleRightMouseButton(PInputEvent event) {
        return; // do nothing on right mouse button
    }

    /**
     * @param sorting the Sorting object used by the VTree to sort expanded subtrees
     */
    public void setSorting(Sorting sorting) {
        this.sorting = sorting;
    }
    
    /**
     * @return the Sorting object used by the VTree to sort expanded subtrees
     */
    public Sorting getSorting() {
        return sorting;
    }

    /**
     * Returns true, when the canvas automatically fits the graph to the window size on resizing
     * @return whether auto-fit of the graph is active
     */
    public boolean isZoomOnResize() {
        return zoomOnResize;
    }

    /**
     * Sets whether the canvas should automatically zoom to overview on window resizing
     * @param zoomOnResize whether to auto-zoom or not
     */
    public void setZoomOnResize(boolean zoomOnResize) {
        this.zoomOnResize = zoomOnResize;
    }

}