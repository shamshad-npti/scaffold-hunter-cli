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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.db.Banner;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.util.SVG;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGLoadObserver;
import edu.udo.scaffoldhunter.view.util.TooltipNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Philipp Kopp
 * 
 */
public class DendrogramViewNode extends PNode implements SVGLoadObserver, TooltipNode {
    // space between SVGs
    private static final int OFFSETX = 15;

    // arc size used when painting rects with rounded corners
    private static final int ARC_SIZE = 120;

    // semantic zoomlevel threshold
    private static final float SEMANTIC_ZOOMLEVEL = 0.05f;

    // tree color if above selection bar
    private static final Color TREE_COLOR = Color.BLACK;

    private HierarchicalClusterNode<Molecule> model;

    private Dictionary<HierarchicalClusterNode<Molecule>, DendrogramViewNode> dictionary;

    // the visual representation of the edges between this DendrogramTreeElement
    // and his children
    private DendrogramEdge leftEdge;
    private DendrogramEdge rightEdge;

    // the Position of this node, starting with y=0 for a leaf
    private double posX = 0;
    private double posY = 0;

    // total horizontal space needed to draw this DendrogramViewNode and his
    // children
    private int treeWidth = 0;

    private boolean choosen;
    private SVGCache svgCache;
    private Color subTreeColor;

    private int treeSize;

    private int childrenSwitched;

    private DendrogramCanvas canvas;

    private double normalizingFactor;

    private boolean hasPrivateBanner;

    private boolean hasPublicBanner;
    // the normal size of the SVG
    private static final int SVG_HEIGHT = 64;
    // the scalingfactor for this SVG
    private final int SVG_HEIGHT_FACTOR;
    private int adjustSVGWith;
    private static final SVG PRIVATE_BANNER = Resources.getSVG("banner_blue.svg");
    private static final SVG PUBLIC_BANNER = Resources.getSVG("banner_green.svg");

    /**
     * @param model
     *            corresponding model node for this view node
     * @param dictionary
     *            which holds the model view relationship
     * @param startX
     *            the most left position where this node may be painted
     * @param leafNodes
     *            the scene graph node where this node should be stored if it is
     *            a leaf
     * @param svgCache
     *            shared SVG Cache
     * @param canvas
     *            the canvas on which this node is shown
     * @param normalizingFactor
     *            factor to normalize the height of the tree
     * 
     */
    public DendrogramViewNode(HierarchicalClusterNode<Molecule> model,
            Dictionary<HierarchicalClusterNode<Molecule>, DendrogramViewNode> dictionary, double startX,
            PNode leafNodes, SVGCache svgCache, DendrogramCanvas canvas, double normalizingFactor) {

        this.model = model;
        this.dictionary = dictionary;
        this.svgCache = svgCache;
        this.canvas = canvas;
        this.normalizingFactor = normalizingFactor;

        choosen = false;
        subTreeColor = TREE_COLOR;
        childrenSwitched = 1;

        if (isLeaf()) {
            treeWidth = Math.max(model.getContent().getSvgWidth(), canvas.getMinSVGWidth());
            double temp = Math.max(0, (treeWidth - model.getContent().getSvgWidth()) / 2);
            adjustSVGWith = (int) temp;

            SVG_HEIGHT_FACTOR = (int) ((canvas.getMinSVGWidth() / 2d) / SVG_HEIGHT);
            setBounds(0, 0, treeWidth, model.getContent().getSvgHeight() + (SVG_HEIGHT * SVG_HEIGHT_FACTOR));

            // a leaf is always at the bottom
            posY = 0;
            posX = startX + treeWidth / (double) 2;

            // leafs are stored in an own sceneGraph
            leafNodes.addChild(this);

            treeSize = model.getClusterSize();
        } else {
            SVG_HEIGHT_FACTOR = 0;

            DendrogramViewNode leftChild = new DendrogramViewNode(model.getLeftChild(), dictionary, startX, leafNodes,
                    svgCache, canvas, normalizingFactor);

            this.dictionary.put(model.getLeftChild(), leftChild);

            // right subtree starts where left ends + offset
            DendrogramViewNode rightChild = new DendrogramViewNode(model.getRightChild(), dictionary, startX
                    + getLeftChild().getTreeWidth() + OFFSETX, leafNodes, svgCache, canvas, normalizingFactor);

            this.dictionary.put(model.getRightChild(), rightChild);

            calcPosition();

            // size of the clickable node
            double nodeWidth = getRightChild().getPosX() - getLeftChild().getPosX();
            double nodeHeight = 1;
            setBounds(-nodeWidth / 2, 0, nodeWidth, nodeHeight);

            // adds children to scenegraph only if they aren't children
            // separation of tree/leaf nodes
            if (!getLeftChild().isLeaf()) {
                addChild(getLeftChild());
            }
            if (!getRightChild().isLeaf()) {
                addChild(getRightChild());
            }

            // create new scene graph nodes for the edges between this node and
            // its children
            leftEdge = new DendrogramEdge(this, getLeftChild());
            this.addChild(this.leftEdge);
            rightEdge = new DendrogramEdge(this, getRightChild());
            this.addChild(this.rightEdge);

            // number of structures represented
            treeSize = model.getClusterSize();
        }
    }

    @Override
    public void paint(PPaintContext aPaintContext) {
        double s = aPaintContext.getScale();
        Graphics2D g2 = aPaintContext.getGraphics();

        Color selectionColor;

        if (isLeaf()) {
            boolean selected = canvas.getSelection().contains(getModel().getContent());

            if (selected) {
                selectionColor = canvas.getGlobalConfig().getSelectedColor();
            } else {
                selectionColor = canvas.getGlobalConfig().getUnselectedColor();
            }

            if (s < SEMANTIC_ZOOMLEVEL) {// semantic zoom level, no SVGs, only
                                         // placeholders painted
                g2.setPaint(selectionColor);
                g2.fillRoundRect((int) (getBoundsReference().getX()), (int) getBoundsReference().getY(),
                        (int) getBoundsReference().getWidth(), (int) getBoundsReference().getHeight(), ARC_SIZE,
                        ARC_SIZE);

                if (hasPublicBanner) {
                    g2.setPaint(Color.GREEN);
                    g2.fillRoundRect((int) (getBoundsReference().getX()),
                            (int) (getBoundsReference().getY() + (getHeight() * 2 / 3)), (int) (getBoundsReference()
                                    .getWidth() / 2), (int) (getBoundsReference().getHeight() / 3), ARC_SIZE, ARC_SIZE);
                }
                if (hasPrivateBanner) {
                    g2.setPaint(Color.WHITE);
                    g2.fillRoundRect((int) (getBoundsReference().getX() + (getWidth() / 2) - 7),
                            (int) (getBoundsReference().getY() + (getHeight() * 2 / 3) - 7),
                            (int) (getBoundsReference().getWidth() / 2 + 15),
                            (int) (getBoundsReference().getHeight() / 3 + 15), ARC_SIZE, ARC_SIZE);
                    g2.setPaint(Color.BLUE);
                    g2.fillRoundRect((int) (getBoundsReference().getX() + (getWidth() / 2)),
                            (int) (getBoundsReference().getY() + (getHeight() * 2 / 3)), (int) (getBoundsReference()
                                    .getWidth() / 2), (int) (getBoundsReference().getHeight() / 3), ARC_SIZE, ARC_SIZE);
                }
                g2.setPaint(selectionColor);
            } else { // paints the real SVGs
                SVG svg = svgCache.getSVG(getModel().getContent(), selectionColor, null, this);

                AffineTransform scaleBanner = new AffineTransform(SVG_HEIGHT_FACTOR, 0, 0, SVG_HEIGHT_FACTOR, 0, 0);
                AffineTransform moveBannerPrivate = new AffineTransform(1, 0, 0, 1, (getWidth() / 2), getHeight()
                        - SVG_HEIGHT * SVG_HEIGHT_FACTOR);
                AffineTransform moveBannerPublic = new AffineTransform(1, 0, 0, 1, (getWidth() / 2 - SVG_HEIGHT
                        * SVG_HEIGHT_FACTOR), getHeight() - SVG_HEIGHT * SVG_HEIGHT_FACTOR);
                AffineTransform moveSVG = new AffineTransform(1, 0, 0, 1, adjustSVGWith, 0);

                try {
                    Color col = g2.getColor();
                    g2.setColor(Color.black);
                    g2.drawRoundRect((int) getBoundsReference().getX() - 1, (int) getBoundsReference().getY() - 1,
                            (int) getBoundsReference().getWidth() + 2, svg.getComponent().getHeight() + 2, ARC_SIZE,
                            ARC_SIZE);

                    g2.setColor(col);
                    g2.transform(moveSVG);
                    svg.paint(g2);
                    moveSVG.invert();
                    g2.transform(moveSVG);

                    if (hasPrivateBanner) {
                        g2.transform(moveBannerPrivate);
                        // g2.transform(mirrorBanner);
                        g2.transform(scaleBanner);
                        scaleBanner.invert();
                        PRIVATE_BANNER.paint(g2);
                        g2.transform(scaleBanner);
                        scaleBanner.invert();
                        // mirrorBanner.invert();
                        // g2.transform(mirrorBanner);

                        moveBannerPrivate.invert();
                        g2.transform(moveBannerPrivate);
                    }

                    if (hasPublicBanner) {
                        g2.transform(moveBannerPublic);

                        // g2.transform(mirrorBanner);
                        g2.transform(scaleBanner);
                        scaleBanner.invert();
                        PUBLIC_BANNER.paint(g2);
                        g2.transform(scaleBanner);
                        scaleBanner.invert();

                        // mirrorBanner.invert();
                        // g2.transform(mirrorBanner);

                        moveBannerPublic.invert();
                        g2.transform(moveBannerPublic);
                    }
                } catch (NoninvertibleTransformException e) {
                    e.printStackTrace();
                }
            }
        } else {
            g2.setPaint(subTreeColor);
            g2.fill(getBoundsReference());

            // DEBUG OUTPUT (prints Dissimilarities)
            // TODO: Do this in good quality as an option for the user
            // String diss = ((Double)model.getDissimilarity()).toString();
            // g2.setColor(Color.BLACK);
            // g2.drawString(diss,0,30);
        }
    }

    /**
     * inverts the selection of the coresponding subtree
     * 
     * @return true if nodes were selected, false otherwise
     */
    public boolean invertSelection() {
        if (isSelected()) {
            deselect();
            return false;
        } else {
            select();
            return true;
        }
    }

    /**
     * used on subtree/node: discards the selection of this subtree/node
     */
    public void deselect() {
        removeSubtreeToModifiedSelection();
        canvas.updateSelectionDeselect();
    }

    /**
     * used on subtree/node: subtree/node added to selection
     */
    public void select() {
        addSubtreeToModifiedSelection();
        canvas.updateSelectionSelect();
    }

    /**
     * Adds all structures in the leaf nodes of the subtree to modified
     * selection set of the canvas
     */
    void addSubtreeToModifiedSelection() {
        if (isLeaf()) {
            canvas.getModifiedSelection().add(getModel().getContent());
        } else {
            getLeftChild().addSubtreeToModifiedSelection();
            getRightChild().addSubtreeToModifiedSelection();
        }
    }

    /**
     * Adds all structures in the leaf nodes of the subtree to modified
     * selection set of the canvas
     */
    void removeSubtreeToModifiedSelection() {
        if (isLeaf()) {
            canvas.getModifiedSelection().add(getModel().getContent());
        } else {
            getLeftChild().removeSubtreeToModifiedSelection();
            getRightChild().removeSubtreeToModifiedSelection();
        }
    }

    /**
     * calculates the own position in relation to the child nodes
     */
    private void calcPosition() {
        posY = (model.getDissimilarity() * normalizingFactor);
        
        // avoid reversals: ensure this node is placed above its children
        double minPosY = Math.max(getLeftChild().getPosY(), getRightChild().getPosY());
        posY = Math.max(posY, minPosY);

        // x-pos is in the middle between child nodes
        posX = Math.round((getLeftChild().getPosX() + getRightChild().getPosX()) / 2);

        treeWidth = OFFSETX + getLeftChild().getTreeWidth() + getRightChild().getTreeWidth();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.util.SVGLoadObserver#svgLoaded(edu.udo.
     * scaffoldhunter.view.util.SVG)
     */
    @Override
    public void svgLoaded(SVG svg) {
        invalidatePaint();
    }

    // ****************************************************************
    // complex getter/setter
    // ****************************************************************

    /**
     * @return all leafNodes in subtree
     */
    public LinkedList<DendrogramViewNode> getLeafs() {
        LinkedList<DendrogramViewNode> list = new LinkedList<DendrogramViewNode>();
        if (isLeaf()) {
            list.add(this);
        } else {
            list.addAll(getLeftChild().getLeafs());
            list.addAll(getRightChild().getLeafs());
        }
        return list;
    }

    /**
     * Sets the cluster color for this node and all its children (except leaf
     * nodes)
     * 
     * @param color
     *            the color to set
     */
    public void setClusterColorRecursively(Color color) {
        if (!isLeaf()) {
            setClusterColor(color);
            getRightChild().setClusterColorRecursively(color);
            getLeftChild().setClusterColorRecursively(color);
        }
    }

    /**
     * Sets the cluster color for this node
     * 
     * @param color
     *            the color to set
     */
    public void setClusterColor(Color color) {
        subTreeColor = color;
        invalidatePaint();
        if (!isLeaf()) {
            getLeftEdge().setClusterColor(subTreeColor);
            getRightEdge().setClusterColor(subTreeColor);
        }
    }

    /**
     * Finds the clusters for a given threshold (given by the selectionBar).
     * Recursively digs into the tree and stops if the threshold is reached.
     * 
     * @param treshold
     *            normalized crossbar value
     * @return the subtrees chosen by the crossbar represented by their root
     *         node
     */
    public List<DendrogramViewNode> getChosenClusterRoots(double treshold) {
        List<DendrogramViewNode> chosenClusters = new LinkedList<DendrogramViewNode>();

        if (getPosY() > treshold && !isLeaf()) {
            // Recursion - add all found clusters in the subtrees
            chosenClusters.addAll(getLeftChild().getChosenClusterRoots(treshold));
            chosenClusters.addAll(getRightChild().getChosenClusterRoots(treshold));
        } else {
            // threshold reached: this is a cluster root
            chosenClusters.add(this);
        }
        setClusterColor(TREE_COLOR);

        return chosenClusters;
    }

    /**
     * Note: This function may be used only after calling calcPositions() in the
     * root node.
     * 
     * @return the position relative to the parent position
     */
    public Point2D.Double getRelativePos() {
        Point2D.Double point = new Point2D.Double();
        point.x = posX - getModelParent().getPosX();
        point.y = getModelParent().getPosY() - posY;
        return point;
    }

    // ****************************************************************
    // standard getter/setter/ state querys
    // ****************************************************************
    /**
     * @return true if the node is a leaf
     */
    public boolean isLeaf() {
        return model.isLeaf();
    }

    /**
     * @return the left child based on the model
     */
    public DendrogramViewNode getLeftChild() {
        if (childrenSwitched == 1) {
            return dictionary.get(model.getLeftChild());
        } else {
            return dictionary.get(model.getRightChild());
        }
    }

    /**
     * @return the right child based on the model
     */
    public DendrogramViewNode getRightChild() {
        if (childrenSwitched == 1) {
            return dictionary.get(model.getRightChild());
        } else {
            return dictionary.get(model.getLeftChild());
        }
    }

    /**
     * @return the parent based on the model
     */
    public DendrogramViewNode getModelParent() {
        return dictionary.get(model.getParent());
    }

    /**
     * @return the choosen
     */
    public boolean isChoosen() {
        return choosen;
    }

    /**
     * @param choosen
     *            the choosen to set
     */
    public void setChoosen(boolean choosen) {
        this.choosen = choosen;
    }

    /**
     * Note: This function may be used only after calling calcPositions() in the
     * root node.
     * 
     * @return the absolute posX of this node
     */
    double getPosX() {
        return posX;
    }

    /**
     * @param posX
     *            the posX to set
     */
    void setPosX(double posX) {
        this.posX = posX;
    }

    /**
     * Note: this function may be used only after calling calcPositions() in the
     * root node
     * 
     * @return the absolute posY of this node
     */
    double getPosY() {
        return posY;
    }

    /**
     * @param posY
     *            the posY to set
     */
    void setPosY(double posY) {
        this.posY = posY;
    }

    /**
     * Note: this function may be used only after calling calcPositions() in the
     * root node
     * 
     * @return the treeWidth
     */
    public int getTreeWidth() {
        return treeWidth;
    }

    /**
     * @param treeWidth
     *            with of the represented subtree
     */
    void setTreeWidth(int treeWidth) {
        this.treeWidth = treeWidth;
    }

    /**
     * @return the leftEdge
     */
    public DendrogramEdge getLeftEdge() {
        return leftEdge;
    }

    /**
     * @return the rightEdge
     */
    public DendrogramEdge getRightEdge() {
        return rightEdge;
    }

    /**
     * @return the represented molecule/scaffold
     */
    public HierarchicalClusterNode<Molecule> getModel() {
        return model;
    }

    /**
     * @return number of represented structures
     */
    public int getTreeSize() {
        return treeSize;
    }

    /**
     * @return if all leafs under this node are selected
     */
    public boolean isSelected() {
        if (isLeaf()) {

            Structure structure = getModel().getContent();

            if (structure instanceof Molecule) {
                return (canvas.getSelection().contains(structure));
            } else {
                return false;
            }

        } else {
            return (getLeftChild().isSelected() && getRightChild().isSelected());
        }
    }

    /**
     * Invalidates the selection status of this node.
     */
    public void invalidateSelection() {
        if (isLeaf()) {
            invalidatePaint();
        }
    }

    /**
     * @return the hasPrivateBanner
     */
    public boolean hasPrivateBanner() {
        return hasPrivateBanner;
    }

    /**
     * @param hasPrivateBanner
     *            the hasPrivateBanner to set
     */
    public void setHasPrivateBanner(boolean hasPrivateBanner) {
        this.hasPrivateBanner = hasPrivateBanner;
        repaint();
    }

    /**
     * @return the hasPublicBanner
     */
    public boolean hasPublicBanner() {
        return hasPublicBanner;
    }

    /**
     * @param hasPublicBanner
     *            the hasPublicBanner to set
     */
    public void setHasPublicBanner(boolean hasPublicBanner) {
        this.hasPublicBanner = hasPublicBanner;
        repaint();
    }

    /**
     * @param structure
     * @return the leaf node which contains the structure (if present)
     */
    public DendrogramViewNode searchForStructure(Structure structure) {
        if (isLeaf()) {
            if (model.getContent().equals(structure)) {
                return this;
            } else {
                return null;
            }
        } else {
            DendrogramViewNode node = getLeftChild().searchForStructure(structure);
            if (node != null) {
                return node;
            } else {
                node = getRightChild().searchForStructure(structure);
                if (node != null) {
                    return node;
                }
            }
        }

        return null;
    }

    /**
     * @param banner
     * @param map
     */
    public void initialBannerPlacing(List<Banner> banner, HashMap<Structure, DendrogramViewNode> map) {
        if (isLeaf()) {
            for (Banner ban : banner) {
                if (model.getContent().equals(ban.getStructure())) {
                    map.put(model.getContent(), this);
                    if (ban.isPrivate()) {
                        hasPrivateBanner = true;
                    } else {
                        hasPublicBanner = true;
                    }
                }
            }
        } else {
            getLeftChild().initialBannerPlacing(banner, map);
            getRightChild().initialBannerPlacing(banner, map);
        }

    }

    @Override
    public boolean hasTooltip() {
        return isLeaf();
    }
    
    @Override
    public Structure getStructure() {
        return getModel().getContent();
    }
}
