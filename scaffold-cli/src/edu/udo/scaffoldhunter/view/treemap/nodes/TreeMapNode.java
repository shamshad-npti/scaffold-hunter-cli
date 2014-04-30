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

package edu.udo.scaffoldhunter.view.treemap.nodes;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.view.treemap.TreeMapCanvas;
import edu.udo.scaffoldhunter.view.treemap.TreeMapView;
import edu.udo.scaffoldhunter.view.treemap.sidebar.ColorLegend;
import edu.udo.scaffoldhunter.view.util.ExportPaintNode;
import edu.udo.scaffoldhunter.view.util.MiniMap;
import edu.udo.scaffoldhunter.view.util.SVG;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGLoadObserver;
import edu.udo.scaffoldhunter.view.util.SelectionState;
import edu.udo.scaffoldhunter.view.util.TooltipNode;
import edu.umd.cs.piccolo.PCamera;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PBounds;
import edu.umd.cs.piccolo.util.PPaintContext;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

/**
 * This class is the basic abstract class for use with the tree map scene graph. It holds all
 * basic functionality related to bounds, size and color of this node, to selection and to
 * drawing. There are two classes, which inherit from this class:
 *  - {@link TreeMapScaffoldNode}: Used to represent scaffolds in the tree map
 *  - {@link TreeMapMoleculeNode}: Used to represent molecules in the tree map
 *  
 *  The title of this node is represented by a {@link TreeMapTextNode}. This class inherits
 *  directly from {@link PNode}.
 * 
 * @author Lappie
 * @author schrins
 *
 */
public abstract class TreeMapNode extends PNode implements SVGLoadObserver, TooltipNode, ExportPaintNode {

    protected static Logger logger = LoggerFactory.getLogger(TreeMapView.class);
    
    /**
     * ID for PNode for bugtracking. Every PNode will get an id in order of creation starting with 0;
     */
    public final int ID;
    
    private static int idTracker = 0;
    
    /**
     * The amount of pixels we need to see before a Node is
     * displayed. Tune this to get faster (lower) or later (higher) Semantic
     * zoom. (semantic zoom)
     */
    private static final int SURFACE_VIEW_THRESHOLD = 10;

    /**
     * The amount of pixels we need to see before details of a Node are being
     * displayed. Tune this to get faster (lower) or later (higher) Semantic
     * zoom. (semantic zoom)
     */
    private static final int SURFACE_VIEW_DETAIL_THRESHOLD = 100;
    
    /**
     * The amount of pixels we need to see before text of a Node are being
     * displayed. Text is an expensive operation to draw and should only be drawn 
     * when it is sure to be read. (semantic zoom)
     */
    private static final int SURFACE_VIEW_TEXT_THRESHOLD = 1000;
    
    /**
     * The threshold for from which point shadow should be added (semantic zoom)
     */
    private static final int SURFACE_VIEW_SHADOW_THRESHOLD = 10000;
    
    /**
     * The threshold for from which point the SVG image of the molecules should be drawn (semantic zoom)
     */
    private static final int SURFACE_VIEW_SVG_THRESHOLD = 1000;
    
    /**
     * Minimal amount of pixels that need to be visible before a node is shown in the minimap
     */
    private final static int MINIMAP_MINIMAL_SIZE = 250;
    
    protected final Color DEFAULT_COLOR = Color.WHITE;
        
    protected Dimension viewerDimension = new Dimension();
    protected TreeMapCanvas canvas;
    protected Selection selection;
    protected GlobalConfig globalConfig;
    
    protected static boolean onlyDrawRootNode = false;
    private int level = 0;
    
    //Only one of these two is set depending on whether this is a Scaffold Node or a Molecule Node
    //(but we need the class separation)
    protected Scaffold scaffold; //TODO: Move to subclasses
    protected Molecule molecule;
    
    private SelectionState selected = SelectionState.UNSELECTED;
    
    private PBounds innerBounds;
    protected TreeMapTextNode text; //contains the text as displayed (with ..)
    private double titlePercHeight;
    
    private double outerBorderWidth = 0.0;
    private double drawSizeRatio = 1.0; // ratio of <area in pixel>:<drawSize>
    
    // Contain a list of all scaffold/molecule nodes
    private ArrayList<TreeMapNode> onlyScaffolds;
    private ArrayList<TreeMapNode> scaffoldsAndMolecules;
    private ArrayList<TreeMapNode> onlyMolecules;
    
    private double drawSize = 0.0;
    private Double sizeValue = 0.0;
    private Double colorValue = null;
    private Double colorPropertyValue = null;
    private boolean defaultNodeColor = true;
    private Color nodeColor = Color.WHITE;
    protected Color borderColor = Color.BLACK;
    private final Stroke zeroStroke = new BasicStroke(0);   
    
    private boolean hasBeenDrawn = false; //has this node actually been drawn
    private float strokeWidth = 2f;    
    
    private boolean markTitleBar = false;
    private boolean borderDashed = false;    
    protected SVGCache svgCache = null;
    private Structure structure = null;
      
    /**
     * @param selection The selected nodes
     * @param config 
     *            the global settings for among others the selection colors
     * @param viewerDimension 
     *            the dimension through which is being viewed, this allows for proper semantic zooming.
     * @param svgCache necessary to load the svg from the scaffolds
     * @param canvas for drawing the SVG
     */
    public TreeMapNode(Selection selection, GlobalConfig config, Dimension viewerDimension, SVGCache svgCache, TreeMapCanvas canvas) {
        super();
        ID = idTracker;
        idTracker++;
        this.viewerDimension = viewerDimension;
        this.canvas = canvas;
        text = new TreeMapTextNode("", canvas, this);
        addChild(text);
        titlePercHeight = 0.075;
        innerBounds = new PBounds();
        setTransparency(1f);
        
        onlyScaffolds = new ArrayList<TreeMapNode>();
        onlyMolecules = new ArrayList<TreeMapNode>();
        scaffoldsAndMolecules = new ArrayList<TreeMapNode>();
        this.selection = selection;
        this.globalConfig = config;
        this.svgCache = svgCache;
        if(isLeaf() && svgCache != null) {
//            setSVGLoaderData(svgCache, scaffold);
            setSVGLoaderData(svgCache, getStructure());
        }
    }
    
    // ****************************************************************
    // CHILD/PARENT FUNCTIONS
    // ****************************************************************
    
    @Override
    public void addChild(final PNode child) {
        if(child instanceof TreeMapNode) {
            // TreeMapNode children are kept in additional list for efficiency reasons
            TreeMapNode node = (TreeMapNode)child;
            if(node.isMoleculeNode()) {
                scaffoldsAndMolecules.add(node);
                onlyMolecules.add(node);
            }
            else {
                scaffoldsAndMolecules.add(node);
                onlyScaffolds.add(node);
            }
        }
        super.addChild(child);
    }
    
    @Override
    public void removeAllChildren() {
        scaffoldsAndMolecules.clear();
        onlyScaffolds.clear();
        onlyMolecules.clear();
        super.removeAllChildren();
        if(text != null)
            addChild(text); // an existing text node should always be a child of this node to maintain the tree structure
    }
    
    /**
     * Return this TreeMapNodes children. Works like {@code getChildrenReference} of {@link PNode}, but 
     * only contains the {@link TreeMapNode} children
     * 
     * @return reference to all tree map children
     */
    public List<TreeMapNode> getTreeMapChildren() {
        return scaffoldsAndMolecules;
    }    
    
    /** 
     * Returns a reference to all children nodes, which will be plotted in the TreeMap due to the currently
     * selected properties. If only scaffolds are plotted, this method will only return children nodes, 
     * which represent scaffolds in the scaffold tree. If plotting molecules, then it will return all
     * children (those for scaffolds and those for molecules).
     * 
     * @return a reference to currently plotted children
     */
    public List<TreeMapNode> getPlottedTreeMapChildren() {
      if(canvas.getDisplayMolecules())
          return scaffoldsAndMolecules;
      else
          return onlyScaffolds;
    }
    
    /** 
     * Returns a reference to all children {@link TreeMapNode}s, which will not be plotted in the TreeMap due to the currently
     * selected properties. If only scaffolds are plotted, this method will only return molecule nodes, 
     * which represent molecules in the scaffold tree. Otherwise this method returns an empty list.
     * @return a reference to currently not plotted children
     */
    public List<TreeMapNode> getUnplottedTreeMapChildren() {
        if(canvas.getDisplayMolecules())
            return new ArrayList<TreeMapNode>();
        else
            return onlyMolecules;
    }
    
    /**
     * Returns the parent of this node. If it is not a {@link TreeMapNode}, null is returned. 
     * @return parent cast to TreeMapNode. null if not a TreeMapNode
     */
    public TreeMapNode getTreeMapParent() {
        PNode parent = getParent();
        if(! (parent instanceof TreeMapNode))
            return null;
        return (TreeMapNode) parent;
    }
    
    private List<TreeMapNode> getAllTreeMapChildrenNodes(List<TreeMapNode> nodes) {
        nodes.add(this);
        for(TreeMapNode child : getTreeMapChildren())
            child.getAllTreeMapChildrenNodes(nodes);
        return nodes;
    }
    
    /**
     * Return a list of all tree map children. Help function, faster than creating something that walks through a tree each time.
     * NOTE: includes oneself too! 
     * @return a list of all children of this node
     */
    public List<TreeMapNode> getAllTreeMapChildrenNodes() {
        return getAllTreeMapChildrenNodes(new ArrayList<TreeMapNode>());
    }
    
    /**
     * Returns the level (=depth) of this node in the tree map. The root has a level of 1.
     * @return the level in the tree map
     */
    public int getLevel() {
        return level;
    }

    /**
     * Sets the level (=depth) of this node in the tree map.
     * @param level the level in the tree map
     */
    protected void setLevel(int level) {
        this.level = level;
        if(level == 1) // for the root node only use half the title height as for other nodes
            titlePercHeight = 0.0375;
        else
            titlePercHeight = 0.075;
    }
    
    /**
     * Set this node to the given level, and update the levels of all children increasingly.
     * @param level the level of this node in the tree map
     */
    public void setNodeAndChildrensLevel(int level) {
        setLevel(level);
        for(TreeMapNode child : getTreeMapChildren()) {
            child.setNodeAndChildrensLevel(level+1);
        }
    }    
    
    /**
     * Whether or not this is a leaf
     * @return true if no children
     */
    public boolean isLeaf() {
        return getChildrenCount() <= 1; // the title is a child in the scene graph, but does not count
    }
    
    // ****************************************************************
    // MOLECULE/SCAFFOLD FUNCTIONS
    // ****************************************************************
    
    /**
     * Returns a list of all molecules related to this node. If this node represents a molecule,
     * the list contains exactly this node, otherwise it is empty.
     * @return all the molecules directly belonging to this node (no child)
     */
    public abstract List<Molecule> getMolecules(); //TODO: Inefficient, contains always zero or one element
    
    private List<Molecule> getAllMolecules(List<Molecule> molecules) {
        molecules.addAll(getMolecules());
        for(TreeMapNode child : getTreeMapChildren()) {
            child.getAllMolecules(molecules);
        }
        return molecules;
    }
    
    /**
     * Returns a list with all molecules, which belong to this node or a node in its subtree.
     * @return all molecules in this subtree
     */
    public List<Molecule> getAllMolecules() {
        return getAllMolecules(new ArrayList<Molecule>());
    }
    
    /**
     * Searches the TreeMapNode that corresponds to the given molecule in the subtree
     * rooted at this node. This is the TreeMapNode representing the molecule or, if 
     * such a node is not visible, the TreeMapNode that represents the scaffold of the 
     * molecule.
     * @param molecule
     * @return the node that corresponds to the given molecule, null if not presents in
     * this subtree
     */
    public TreeMapNode getNodeWith(Molecule molecule) {
        if(this.molecule == molecule) {
            if(canvas.getDisplayMolecules())
                return this;
            else
                return getTreeMapParent();
        }        
        
        TreeMapNode result = null;
        for(TreeMapNode child : getTreeMapChildren()) {
            result = child.getNodeWith(molecule);
            if(result != null)
                return result;
        }
        return null;
    }
    
    /**
     * @return true if moleculeNode. Only then molecule is set
     */
    public abstract boolean isMoleculeNode(); //TODO: Ugly implementation
        
    // ****************************************************************
    // SELECTION FUNCTIONS
    // ****************************************************************
    
    /**
     * @return true if and only if this node is selected
     */
    public boolean isSelected() {
        return selected == SelectionState.SELECTED;
    }
    
    /**
     * Set the selection state of this node manually, without doing anything else. 
     * @param newState
     */
    protected void setSelectionState(SelectionState newState) {
        if (selected != newState) {
            selected = newState;
            if (selected == SelectionState.SELECTED) {
                borderColor = globalConfig.getSelectedColor();
            } else if(selected == SelectionState.HALFSELECTED) {
                borderColor = globalConfig.getPartiallySelectedColor();
            } else {
                borderColor = globalConfig.getUnselectedColor();
            }
            invalidatePaint();   
        }
    }
    
    /**
     * @return the current selection state
     */
    public SelectionState getSelectionState() {
        return selected;
    }
    
    /**
     * Switches this nodes selection. If it is selected, it will be unselected and vice versa. 
     * 
     * If selected: all child nodes will also be selected, and parent nodes will be partially selected. 
     * Vice versa if unselected. 
     */
    public void toggleSelection() {
        if(onlyDrawRootNode) return;
        List<Molecule> molecules = getAllMolecules(new ArrayList<Molecule>()); 
        if(this.selected != SelectionState.SELECTED) {
            selection.addAll(molecules);
        } else {
            selection.removeAll(molecules);
        }
    }
    
    // ****************************************************************
    // MISC
    // ****************************************************************
    
    /**
     * Update the viewer dimension, the pixels that a user uses to watch the canvas. 
     * @param viewerDimension
     */
    protected void setViewerDimension(Dimension viewerDimension) {
        this.viewerDimension = viewerDimension;
    }
    
    @Override
    public String toString() {
        return "TreeMapNode " + getDrawSize() + " - at level " + getLevel();
    }

    /**
     * Whether or not this node contains a virtual structure i.e. it is the root node and
     * does not correspond to a real structure. 
     * @return true if node is virtual
     */
    public boolean isVirtual() {
        return getStructure().getTitle() == null;
//        return getLevel() == 1; //TODO: Remove old code
    }
    
    /**
     * Returns a short version of the title, which contains either the name of the node's
     * structure or the name of the current subset, if it is a root node.
     * @return a short title
     */
    public String getTitleShort() {
        if(getStructure().getTitle() != null)
            return getStructure().getTitle();
        else
            return "";
    }

    @Override
    public boolean hasTooltip() {
        return !isVirtual();
    }
    
    // ****************************************************************
    // BOUNDS FUNCTIONS
    // ****************************************************************
    
    @Override
    protected void internalUpdateBounds(final double x, final double y, final double width, final double height) {
        double minDimension = Math.min(getBounds().getHeight(), getBounds().getWidth());
        if(outerBorderWidth > 0.25 * minDimension) {
            /*
             *  this might indicate deformed nodes. capping the border width might improve the visuals of the node,
             *  but the borders of neighbouring nodes are no longer aligned to each other
             */            
            logger.debug("outerBorderWidth of {} was over 25% of total height or width", getTitle());
            outerBorderWidth = 0.25 * minDimension;
        }
        setInnerBounds(x+outerBorderWidth, y+outerBorderWidth, width - 2*outerBorderWidth, height - 2*outerBorderWidth);
        text.reloadTitle();
    }
    
    /**
     * Update this nodes bound without adding a clearBound around it. 
     * 
     * Mainly used for slice and dice
     * @param x
     * @param y
     * @param width
     * @param height
     */
    public void updateExlusiveBounds(double x, double y, double width, double height) {
        outerBorderWidth = 30.0;
        this.setBounds(x, y, width, height);
        //TODO: Adaptive size for outerBorderWidth
        //TODO: Remove this method and just call setBounds()
    }
    
    /**
     * Sets the bounds of the inner space, which contains all the child nodes (except the title)
     * @param x new x position
     * @param y new y position
     * @param width new width
     * @param height new height
     */
    protected void setInnerBounds(double x, double y, double width, double height) {
        innerBounds.setRect(x, y, width, height);
    }
    
    /**
     * Returns the space that is available for placing other stuff in. 
     * I.e. All space from this node except the title area
     * @return the inner space
     */
    public PBounds getInnerBounds() {
        return innerBounds;
    }
        
    /**
     * Returns the bounds of the area, which is used by the children nodes or the SVG.
     * @return the children area bounds
     */
    public PBounds getChildrenBounds() {
        return new PBounds(innerBounds.getX(), 
                innerBounds.getY()+getTitleHeight(), 
                innerBounds.getWidth(), 
                innerBounds.getHeight() - getTitleHeight());
    }
    
    /**
     * Returns the bounds of the node's title space.
     * @return the title bounds
     */
    public PBounds getTitleBounds() {
        return new PBounds(innerBounds.getX(), innerBounds.getY(), innerBounds.getWidth(), getTitleHeight());
    }

    private PBounds getShadowBounds() {
        PBounds shadowBounds = new PBounds(getInnerBounds());
        shadowBounds.x += shadowBounds.getWidth() * .02;
        shadowBounds.y += shadowBounds.getHeight() * .02;    
        return shadowBounds;
    }

    /**
     * Returns the height of the node's title space
     * 
     * @return height of the title
     */
    private double getTitleHeight() {
        return titlePercHeight * getInnerBounds().getHeight();
    }

    /**
     * Set the loader for loading the SVG of this TitleNode
     * @param svgCache the cache to load the SVG from
     * @param structure the structure to load and display
     */
    public void setSVGLoaderData(SVGCache svgCache, Structure structure) {
        this.svgCache = svgCache;
        this.structure = structure;
    }
    
    // ****************************************************************
    // PAINT FUNCTIONS
    // ****************************************************************

    private int visiblePixels(PCamera camera) {
        double cameraSurface = camera.getViewBounds().getWidth() * camera.getViewBounds().getHeight();
        double nodeSurface = getWidth() * getHeight();
        double viewerSurface = viewerDimension.getWidth() * viewerDimension.getHeight();
        
        //the nr of pixels we see of this node: 
       return (int) ((nodeSurface/cameraSurface) * viewerSurface);
    }    
    
    private boolean drawDetails(PCamera camera) {
        return visiblePixels(camera) > SURFACE_VIEW_DETAIL_THRESHOLD;
    }
    
    private boolean drawText(PCamera camera) {
        if(isMinimap(camera) && getLevel() > 1) //we can show "TreeMap" for the minimap
            return false;
        if(canvas.getAnimating() || canvas.getInteracting())
            return false;
        return visiblePixels(camera) > SURFACE_VIEW_TEXT_THRESHOLD;
    }
    
    protected boolean drawShadow(PCamera camera) {
        if(isMinimap(camera))
            return false;
        if(canvas.getAnimating() || canvas.getInteracting())
            return false;
        return visiblePixels(camera) > SURFACE_VIEW_SHADOW_THRESHOLD;
    }
    
    private boolean drawSVG(PCamera camera) {
        if(isMinimap(camera))
            return false;
        if(!isLeaf())
            return false;
        return visiblePixels(camera) > SURFACE_VIEW_SVG_THRESHOLD;
    }
    
    private Color calculateColor() {
        if(TreeMapNode.onlyDrawRootNode) {
            return DEFAULT_COLOR;
        }
        if(defaultNodeColor) {
            return DEFAULT_COLOR;
        }
        if(colorValue == null) {
            return ColorLegend.UNKNOWN_PROPERTY_COLOR;
        }
        
        return (ColorLegend.getRangeColor(colorValue.floatValue()));
    }
    
    /**
     * Are we at such a zoom and pan level that this node is drawn?
     * This is the semantic zoom part that determines if we'll see this node
     * 
     * @param camera
     *            The camera through which is looked at.
     * @return True when this node is completely in the cameraView and the
     *         surface compared to the camera surface is enough
     */
    public boolean isDrawn(PCamera camera) {
        if(isMinimap(camera)) {
            if(visiblePixels(camera) < MINIMAP_MINIMAL_SIZE)
                return false;
            if(level > 2)
                return false;
            return true;
        }
        return visiblePixels(camera) > SURFACE_VIEW_THRESHOLD;
    }
    
    
    protected boolean isBorderDashed() {
        return false;
    }
    
    private boolean isMinimap(PCamera camera) {
        String cameraName = camera.getName();
        return cameraName != null && cameraName.equals(MiniMap.CAMERA_NAME);
    }
    
    private Stroke getNodeStroke() {
        if(!borderDashed)
            return new PFixedWidthStroke(strokeWidth);
        return new PFixedWidthStroke(strokeWidth, BasicStroke.CAP_ROUND, 
                BasicStroke.JOIN_MITER, 10.0f, new float[] { 4.0f }, 0f);
    }
    
    @Override
    public void fullPaint(final PPaintContext paintContext) {
        boolean oldHasBeenDrawn = hasBeenDrawn;
        hasBeenDrawn = false; 
        
        if(getDrawSize() <= 0 && level != 1) //root node can be drawn in case the size is not set
            return;
        if(onlyDrawRootNode && level > 1)
            return;
        
        PCamera camera = paintContext.getCamera();
        
        //minimap:
        if(isMinimap(camera)) {
            hasBeenDrawn = oldHasBeenDrawn; //this doesn't count for MiniMap
            if(visiblePixels(camera) < MINIMAP_MINIMAL_SIZE)
                return;
            if(level > 2) {
                return;
            }
        }
        
        super.fullPaint(paintContext);
    }
    
    @Override
    public void paint(final PPaintContext paintContext) {
        text.setDrawText(false);
        
        if(getBounds().getWidth() < 0 || getBounds().getHeight() < 0) {
            logger.error("TreeMapPNode: Node is too small to be drawn");
            return;
        }
        
        PCamera camera = paintContext.getCamera();
  
        //semantic zoom
        if(!isDrawn(camera))
            return;
        
        //And now we know for sure, we are drawing
        hasBeenDrawn = true;
        nodeColor = calculateColor();            
        if (nodeColor == null) {
            return;
        }
        Graphics2D g2 = paintContext.getGraphics();
        
        if(innerBounds.getWidth() < 0 || innerBounds.getHeight() < 0) {
            logger.error("TitleNode: Node is too small to be drawn. Wrongful calculations");
            return;
        }
        
        // shadow:
        if (drawShadow(camera)) {
            g2.setPaint(new Color(20, 20, 20, 90));
            g2.setStroke(zeroStroke);
            g2.fill(getShadowBounds());
        }

        ///////// node background color ////////
        g2.setPaint(nodeColor);
        g2.setStroke(zeroStroke);
        g2.fill(getInnerBounds());
        
        if(sizeValue == null) {
            g2.setPaint(Color.GRAY);
            g2.fill(getTitleBounds());
        }
        
        if(markTitleBar) {
            g2.setPaint(Color.RED);
            g2.fill(getTitleBounds());
        }

        if (drawDetails(camera)) {
            // node border
            g2.setPaint(borderColor);
            g2.setStroke(getNodeStroke());
            g2.draw(getInnerBounds());
        }
        
        if(text != null) {
            if(drawText(camera)) {
                //////// title ////////
                g2.setStroke(zeroStroke);
                text.setDrawText(true);
            }
        }
        
        if(svgCache != null && structure != null) {
            if(drawSVG(camera)) {
                if(!canvas.getAnimating() && ! canvas.getInteracting()) { //no SVG while dragging/animating
                    SVG svg = svgCache.getSVG(structure, null, null, this);
                    PBounds bounds = getChildrenBounds();
                    svg.paint(g2, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
                }
            }
        }
    }
    
    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.util.ExportPaintNode#exportPaint(edu.umd.cs.piccolo.util.PPaintContext)
     */
    @Override
    public void exportPaint(PPaintContext paintContext) {
        text.setDrawText(false);
        if(getBounds().getWidth() < 0 || getBounds().getHeight() < 0) {
            logger.error("TreeMapPNode: Node is too small to be drawn");
            return;
        }
        
        PCamera camera = paintContext.getCamera();
          
        //And now we know for sure, we are drawing
        hasBeenDrawn = true;
        nodeColor = calculateColor();            
        if (nodeColor == null) {
            return;
        }
        
        Graphics2D g2 = paintContext.getGraphics();
        
        if(innerBounds.getWidth() < 0 || innerBounds.getHeight() < 0) {
            logger.error("TitleNode: Node is too small to be drawn. Wrongful calculations");
            return;
        }
        
  
        ///////// node background color ////////
        g2.setPaint(nodeColor);
        g2.setStroke(zeroStroke);
        g2.fill(getInnerBounds());
        
        if(sizeValue == null) {
            g2.setPaint(Color.GRAY);
            g2.fill(getTitleBounds());
        }
        
        if(markTitleBar) {
            g2.setPaint(Color.RED);
            g2.fill(getTitleBounds());
        }

        if (drawDetails(camera)) {
            // node border
            g2.setPaint(borderColor);
            g2.setStroke(new BasicStroke(1));
            g2.draw(getInnerBounds());
        }
        
        if(text != null) {
            if(drawText(camera)) {
                //////// title ////////
                g2.setStroke(zeroStroke);
                text.setDrawText(true);
            }
        }

        if(svgCache != null && structure != null) {
            SVG svg = svgCache.getSVG(structure, null, null, this);
            PBounds bounds = getChildrenBounds();
            svg.paint(g2, bounds.getX(), bounds.getY(), bounds.getWidth(), bounds.getHeight());
        }    
    }

    // ****************************************************************
    // GETTERS AND SETTERS
    // ****************************************************************
    
    @Override
    public void setPaint(final Paint color) {
        nodeColor = (Color)color;
        defaultNodeColor = false;
    }

    /**
     * Set the size of this node to draw. Note that this can not be negative, zero or null. 
     * @return the size: how large this node will be drawn. 
     */
    public double getDrawSize() {
        return drawSize;
    }
    
    /**
     * Set the size of this node to draw. Note that this can not be negative, zero or null.
     * 
     * This represents how big the node will be drawn on canvas. If you want to set the size that
     * this node represents, then call getSizeValue().
     *  
     * @param size how large this node will be drawn. 
     */
    public void setDrawSize(double size) {
        Preconditions.checkArgument(size > 0, "size should be bigger than zero, and was: " + size);
        this.drawSize = size;
    }
    
    /**
     * Returns the ratio between the draw size and the size (=area) in pixel
     * @return the drawSizeRatio
     */
    public double getDrawSizeRatio() {
        return drawSizeRatio;
    }

    /**
     * Sets the ratio between the draw size and the size (=area) in pixel
     * @param drawSizeRatio the drawSizeRatio to set
     */
    public void setDrawSizeRatio(double drawSizeRatio) {
        this.drawSizeRatio = drawSizeRatio;
    }

    /**
     * Returns the width of the outer border, which is the gap between the inner bounds and the full bounds
     * @return the width of this node's outer border
     */
    public double getOuterBorderWidth() {
        return outerBorderWidth;
    }

    /**
     * Sets the width of the outer border, which is the gap between the inner bounds and the full bounds
     * @param outerBorderWidth the outerBorderWidth to set
     */
    public void setOuterBorderWidth(double outerBorderWidth) {
        this.outerBorderWidth = outerBorderWidth;
    }
    
    /**
     * @return the value that this node's color is representing
     */
    public Double getColorValue() {
        if(TreeMapNode.onlyDrawRootNode)
            return null;
        return colorValue;
    }
    
    /**
     * Set the value that the color of this node represents. 
     * The node's color is changed to the corresponding color according to the ColorLegend. 
     * @param colorValue
     */
    public void setColorValue(Double colorValue) {
        this.colorValue = colorValue;
        defaultNodeColor = false;
    }
    
    /**
     * @return the size that node represents
     */
    public Double getSizeValue() {
        return sizeValue;
    }
    
    /**
     * Set the value that this nodes represents. This value may be negative or even null if the value is unknown. 
     * 
     * Note: this is not the size that is actually drawn since that can not be negative. See setDrawValue for that. 
     *  
     * @param sizeValue the value to represent
     */
    public void setSizeValue(Double sizeValue) {
        this.sizeValue = sizeValue;
    }
    
    /**
     * @return the colorPropertyValue
     */
    public Double getColorPropertyValue() {
        return colorPropertyValue;
    }

    /**
     * @param colorPropertyValue the colorPropertyValue to set
     */
    public void setColorPropertyValue(Double colorPropertyValue) {
        this.colorPropertyValue = colorPropertyValue;
    }
    
    /**
     * Reset the color of this node to its original color. 
     * Also reset the colorValue to null. 
     */
    public void resetColorValue() {
        colorValue = null;
        defaultNodeColor = true;
    }
    
    /**
     * @return the title of this node. 
     */
    public String getTitle() {
        return text.getTitle();
    }
    
    /**
     * Set the title for this node
     * @param title
     */
    public void setTitle(String title) {
        text.setTitle(title);
    }
    
    /**
     * Mark the titlebar by giving it a different color. 
     * For debugging purposes
     */
    public void markTitleBar() {
        markTitleBar = true;
    }
    
    /**
     * useful for finding a clicked node. 
     * @return has this node been drawn on the last paint event or not
     */
    public boolean hasBeenDrawn() {
        return hasBeenDrawn;
    }
    
    @Override
    public void svgLoaded(SVG svg) {
        invalidatePaint();
    }
    
    @Override
    public void print() {
        System.out.println("Node " + ID + ", title: "+getTitle()+", level: " + level + ", drawSize: " + drawSize);
        System.out.println("W: " + getWidth() + ", H: " + getHeight());
        System.out.println();
    }
}
