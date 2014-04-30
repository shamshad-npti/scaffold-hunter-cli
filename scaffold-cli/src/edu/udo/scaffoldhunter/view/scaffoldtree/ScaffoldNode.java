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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.util.ExportPaintNode;
import edu.udo.scaffoldhunter.view.util.SVG;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGLoadObserver;
import edu.udo.scaffoldhunter.view.util.SelectionState;
import edu.udo.scaffoldhunter.view.util.TooltipNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * This class represent a scaffold which is part of a VTree
 * and is displayed in a Piccolo scenegraph.
 * <p>
 * Methods like addChild() refer to the scenegraph, methods
 * referring to the VTree are called addTreeChild/Parent().
 * 
 * @version 1.0
 * @author Gorecki
 * @author Kriege
 * @author Schrader
 * @author Wiesniewski
 * @author Henning Garus
 */
public class ScaffoldNode extends VNode implements SVGLoadObserver, PropertyChangeListener, ExportPaintNode, TooltipNode {

    private static final float cursorStrokeWidth = 6f;
    private static final int BANNER_HEIGHT = 96;
    
    private static final SVG PRIVATE_BANNER = Resources.getSVG("banner_blue.svg");
    private static final SVG PUBLIC_BANNER = Resources.getSVG("banner_green.svg");
    
    /**
     * a link to the corresponding <code>Scaffold</code>. Each VNode
     * is representing a Scaffold, it is possible that one Scaffold has multiple
     * corresponding VNodes.
     */
    private Scaffold scaffold;
    
    private SelectionState selected;

    private ExpandReduceIcon icon;
    
    private boolean showDetailsNode = false;
    private Comparator<? super Molecule> moleculeComparator;
    private ScaffoldDetailsNode detailsNode;


    private final Selection selection;
    private final SVGCache svgCache;
    private final BannerPool bannerPool;
    
    private InfoBar infobar = null;

    private final ScaffoldLabel label;
        
    
    /**
     * @param scaffold
     *          The scaffold we want to create this <code>VNode</code> for.
     * @param svgCache
     *          The svgCache which will be used to hold the SVG shown by this
     *          <code>VNode</code>.
     * @param selection 
     * @param bannerPool 
     * @param globalConfig 
     */
    public ScaffoldNode(Scaffold scaffold, SVGCache svgCache, Selection selection, BannerPool bannerPool, GlobalConfig globalConfig){
        super(globalConfig, Math.max(scaffold.getSvgHeight(), scaffold.getSvgWidth()));
        Preconditions.checkNotNull(scaffold);
        
        this.scaffold = scaffold;
        this.selection = selection;
        this.svgCache = svgCache;
        this.bannerPool = bannerPool;
        this.setBounds(0,0,Math.max(2 * BANNER_HEIGHT, scaffold.getSvgWidth() + BANNER_HEIGHT / 2), scaffold.getSvgHeight());
        

        this.cursor = false;
        this.color = null;
        this.label = new ScaffoldLabel(this);
        setPaint(Color.WHITE);
        updateIcon();
        
        setSelection(selection.getSet());
        selection.addPropertyChangeListener(Selection.SELECTION_PROPERTY, this);
    }

    private void updateIcon () {
        if (!scaffold.getChildren().isEmpty() && !scaffold.isImaginaryRoot() ) {
            if (icon == null)
                icon = new ExpandReduceIcon();
        } else {
            icon = null;
        }
    }
    
    

    /**
     * Show an {@link InfoBar} below the node. The info bar displays the
     * <code>propertyBinValues</code>. If there is already an info bar on
     * display it is updated to show the new values.
     * 
     * @param propertyBinValues A collection of property bin values, which are
     *          shown by this node's info bar.
     */
    public void showInfoBar(Collection<PropertyBinValue> propertyBinValues) {
        // dont show anything if no data is given
        if (propertyBinValues == null) {
            removeInfoBar();
        } else {
            if (infobar ==  null) {
                infobar = new InfoBar(getBoundsReference().width);
                addChild(infobar);
                infobar.setOffset(0, getBoundsReference().height);
            }
            infobar.show(propertyBinValues);
        }
    }

    /**
     * Removes the info bar, if one is displayed.
     */
    public void removeInfoBar() {
        if (infobar != null) {
            removeChild(infobar);
            infobar = null;
        }
    }
    
    /**
     * 
     * @param text
     */
    public void showLabel(String text) {
        this.label.setText(text);
    }


    /**
     * 
     */
    public void removeLabel() {
        this.label.setText("");
    }

    //****************************************************************
    //  Color
    //****************************************************************


    /**
     * Returns the <code>Scaffold</code> corresponding to this
     * <code>VNode</code>.
     * 
     * @return the scaffold corresponding to this node
     */
    public Scaffold getScaffold(){
        return this.scaffold;
    }

    @Override
    public void exportPaint(PPaintContext paintContext) {
        final int x =(int)getBoundsReference().x;
        final int y = (int)getBoundsReference().y;
        final int w = (int)getBoundsReference().width;
        final int h = (int)getBoundsReference().height;
        final Graphics2D g = paintContext.getGraphics();
        
        if (getPaint().getTransparency() != Transparency.OPAQUE) {
            g.setPaint(Color.WHITE);
            g.fillRect(x, y, w, h);
        }
        g.setPaint(getPaint());
        g.fillRect(x, y, w, h);

        if (color != null) {
            g.setPaint(Color.WHITE);
            g.fillRoundRect(x,y,w,h,12,12);
            g.setPaint(this.getColor());
            g.fillRoundRect(x,y,w,h,12,12);
        }

        SVG svg = null;
        if (scaffold.getMolecules().isEmpty())
            svg = svgCache.getSVGSynchronous(scaffold, globalConfig.getNotSelectableColor(), Color.WHITE);
        else
            svg = svgCache.getSVGSynchronous(scaffold, null, Color.WHITE);
        
        svg.paint(g, w, h);
    }

    private static final Set<SemanticZoomLevel> dontShowDetailsNode = 
        EnumSet.of(SemanticZoomLevel.MEDIUM, SemanticZoomLevel.DISTANT);
    /**
     * This method paints the node dependent on the zoom level
     */
    @Override
    public void paint(PPaintContext aPaintContext) {
        Graphics2D g = aPaintContext.getGraphics();

        double s = aPaintContext.getCamera().getViewScale()*getScale();

        int x = (int)getBoundsReference().x;
        int y = (int)getBoundsReference().y;
        int w = (int)getBoundsReference().width;
        int h = (int)getBoundsReference().height;
        SemanticZoomLevel zoomLevel = SemanticZoomLevel.getByThreshold(s);
        switch (zoomLevel) {
        case VERY_CLOSE:
        case CLOSE:
            if (VCanvas.MAIN_CAMERA.equals(aPaintContext.getCamera().getName()) && showDetailsNode && scaffold.getMolecules().size() != 0) {
                if (detailsNode == null) {
                    detailsNode = new ScaffoldDetailsNode(scaffold, svgCache, selection, moleculeComparator);
                    addChild(detailsNode);
                    break;
                } else {
                    break;
                }
            }
            semanticZoomLevel1(aPaintContext, g, x, y, w, h);
            break;
        case MEDIUM:
            semanticZoomLevel2(g, x, y, w, h);
            break;
        case DISTANT:
            semanticZoomLevel3(g, x, y, w, h);
            break;
        }
        if (VCanvas.MAIN_CAMERA.equals(aPaintContext.getCamera().getName()) && dontShowDetailsNode.contains(zoomLevel) && detailsNode != null) {
            removeChild(detailsNode);
            detailsNode = null;
        }
    }

    private void semanticZoomLevel1 (PPaintContext p, Graphics2D g, int x, int y, int w, int h) {
        // paint a white rectangle if the background is translucent
        if (getPaint().getTransparency() != Transparency.OPAQUE) {
            g.setPaint(Color.WHITE);
            g.fillRect(x, y, w, h);
        }
        g.setPaint(getPaint());
        g.fillRect(x, y, w, h);

        if (color != null) {
            g.setPaint(Color.WHITE);
            g.fillRoundRect(x,y,w,h,12,12);
            g.setPaint(this.getColor());
            g.fillRoundRect(x,y,w,h,12,12);
        }

        if (cursor) {
            //TODO universal config
            g.setPaint(Color.BLUE);
            g.setStroke(new BasicStroke (cursorStrokeWidth));
            g.drawRect(x,y,w,h);
        }

        SVG svg = null;
        if (scaffold.getMolecules().isEmpty())
            svg = svgCache.getSVG(scaffold, globalConfig.getNotSelectableColor(), Color.WHITE, this);
        else if (selected == SelectionState.SELECTED)
            svg = svgCache.getSVG(scaffold,globalConfig.getSelectedColor(), Color.WHITE, this);
        else if (selected == SelectionState.HALFSELECTED)
            svg = svgCache.getSVG(scaffold, globalConfig.getPartiallySelectedColor(), Color.WHITE, this);
        else
            svg = svgCache.getSVG(scaffold, null, Color.WHITE, this);
        
        if (!svg.paint(g, w, h)) {
            // paint a placeholder with the size of this nodes bounds
            //g.setPaint(Color.lightGray);
            //g.fillRect(x, y, w, h);
        } else {
            if (bannerPool.hasBanner(scaffold, true)) {
                PRIVATE_BANNER.paint(g, w - BANNER_HEIGHT/2, y + (h - BANNER_HEIGHT) / 2, BANNER_HEIGHT, BANNER_HEIGHT);
            } 
            if (bannerPool.hasBanner(scaffold, false)) {
                PUBLIC_BANNER.paint(g, -BANNER_HEIGHT/2, y + (h - BANNER_HEIGHT) / 2, BANNER_HEIGHT, BANNER_HEIGHT);  
            }
              
        }
    }

    private void semanticZoomLevel2 (Graphics2D g, int x, int y, int w, int h) {
        g.setPaint(Color.WHITE);
        g.fillRoundRect(x,y,w,h,90,90);

        if (color != null)
            g.setPaint(this.getColor());
        else
            if ( scaffold.getMolecules().isEmpty())
                g.setPaint(globalConfig.getNotSelectableColor());
            else
                g.setPaint(globalConfig.getUnselectedColor());
        g.fillRoundRect(x,y,w,h,90,90);

        if (selected == SelectionState.SELECTED)
            g.setPaint(globalConfig.getSelectedColor());
        else if (selected == SelectionState.HALFSELECTED)
            g.setPaint(globalConfig.getPartiallySelectedColor());
        if (selected != SelectionState.UNSELECTED) {
            int xborder = w / 5;
            int yborder = h / 5;
            g.fillRoundRect(x + xborder, y + yborder, w - 2 * xborder, h - 2 * yborder, 90, 90);
        }
        if (cursor) {
            //TODO universal config
            g.setPaint(Color.BLUE);
            g.setStroke(new BasicStroke (17));
            g.drawRoundRect(x,y,w,h,90,90);
        }
            double bh = 2 * BANNER_HEIGHT;
        if (bannerPool.hasBanner(scaffold, true)) {
            PRIVATE_BANNER.paint(g, w - bh/2, y + (h - bh) / 2, bh, bh);
        }
        if (bannerPool.hasBanner(scaffold, false)) {
            PUBLIC_BANNER.paint(g, -bh/2, y + (h - bh) / 2, bh, bh);
        }
    }

    private void semanticZoomLevel3(Graphics2D g, int x, int y, int w, int h) {
        g.setPaint(Color.WHITE);
        g.fillOval(x,y,w,h);

        if (color != null)
            g.setColor(this.getColor());
        else
            if (scaffold.getMolecules().isEmpty())
                g.setColor(globalConfig.getNotSelectableColor());
            else
                g.setColor(globalConfig.getUnselectedColor());
        g.fillOval(x, y, w, h);
        
        if (selected == SelectionState.SELECTED)
            g.setPaint(globalConfig.getSelectedColor());
        else if (selected == SelectionState.HALFSELECTED)
            g.setPaint(globalConfig.getPartiallySelectedColor());
        if (selected != SelectionState.UNSELECTED) {
            int xborder = w / 5;
            int yborder = w / 5;
            g.fillOval(x + xborder, y + yborder, w - 2 * xborder, h - 2 * yborder);
        }
        if (cursor) {
            //TODO universal config
            g.setPaint(Color.BLUE);
            g.fillOval(x,y,w,h);
        }
            double bh = 3 * BANNER_HEIGHT;
            if (bannerPool.hasBanner(scaffold, true)) {
                PRIVATE_BANNER.paint(g, w, y + (h - bh) / 2, bh, bh);
            }
            if (bannerPool.hasBanner(scaffold, false)) {
                PUBLIC_BANNER.paint(g, -bh, y + (h - bh) / 2, bh, bh);
            }
    }


    //****************************************************************
    //  Expand & reduce
    //****************************************************************


    /**
     * 
     * @return the icon associated with this node
     */
    public ExpandReduceIcon getIcon() {
        return icon;
    }

    //****************************************************************
    //  Inner Class : Expand Reduce Icon
    //****************************************************************


    /**
     * This inner class represents a button to expand or cut off
     * the VTree at this VNode.
     */
    public class ExpandReduceIcon extends PNode {

        /**
         * standard constructor
         */
        public ExpandReduceIcon() {
            this.setBounds(0, 0, 12, 12);
        }

        /**
         * @return the vnode associated with this icon
         */
        public ScaffoldNode getVNode () {
            return ScaffoldNode.this;
        }

        @Override
        public void paint(PPaintContext aPaintContext) {
            Graphics2D g = aPaintContext.getGraphics();
            g.setStroke(new BasicStroke(1));
            g.setPaint(Color.WHITE); // Backgroundcolor
            g.fill(getBounds()); // draws Background

            g.setPaint(Color.BLACK);

            g.drawRect(0, 0, 12, 12); // draws Rectangle

            g.drawLine(3, 6, 9, 6); 		// draws Minus
            if (getVNode().isExpandable())
                g.drawLine(6, 3, 6, 9); 	// draws Plus
        }
    }

    //****************************************************************
    //  More Get & Set
    //****************************************************************

   

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.util.SVGLoadObserver#svgLoaded(edu.udo.scaffoldhunter.view.util.SVG)
     */
    @Override
    public void svgLoaded(SVG svg) {
        this.invalidatePaint();
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return scaffold.hashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null ||! (obj instanceof ScaffoldNode))
            return false;
        return ((ScaffoldNode) obj).getScaffold().equals(scaffold);
    }

    /**
     * @return the label
     */
    ScaffoldLabel getLabel() {
        return label;
    }
    
    /**
     * 
     * @return this nodes selection state
     */
    SelectionState getSelection() {
        return selected;
    }
    
    /**
     * set the selection status of this node based on all selected molecules
     * 
     * @param selection the molecules which are selected
     * @return <code>true</code> if the selection status changed otherwise <code>false</code>
     */
    private boolean setSelection(Set<? extends Structure> selection) {
        int intersectionSize = Sets.intersection(scaffold.getMolecules(),selection).size();
        SelectionState newState;
        if ( intersectionSize == 0) {
            newState = SelectionState.UNSELECTED;
        } else if ( intersectionSize < scaffold.getMolecules().size() ) {
            newState = SelectionState.HALFSELECTED;
        } else {
            newState = SelectionState.SELECTED;
        }
        if (newState == selected)
            return false;
        selected = newState;
        return true;
    }
    

    /**
     * @return the showDetailNode
     */
    public boolean isShowDetailsNode() {
        return showDetailsNode;
    }

    /**
     * @param showDetailsNode the showDetailNode to set
     */
    public void setShowDetailsNode(boolean showDetailsNode) {
        this.showDetailsNode = showDetailsNode;
        if (showDetailsNode == false) {
            removeChild(detailsNode);
            detailsNode = null;
        }
        invalidatePaint();
    }


    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    @SuppressWarnings("unchecked")
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(setSelection((Set<Molecule>)evt.getNewValue())) {
            invalidatePaint();
            for (VEdge e : getEdges())
                e.invalidatePaint();
        }
    }
    
    /**
     * Function to transform a VNode into a Scaffold
     */
    public static Function<ScaffoldNode, Scaffold> VNODE_TO_SCAFFOLD = new Function<ScaffoldNode, Scaffold>() {
        @Override
        public Scaffold apply(ScaffoldNode input) {
            return input.getScaffold();
        }
    };


    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.scaffoldtree.VNode#isExpandable()
     */
    @Override
    public boolean isExpandable() {
        return scaffold.getChildren().size() > children.size();
    }

    /**
     * @return the moleculeComparator
     */
    public Comparator<? super Molecule> getMoleculeComparator() {
        return moleculeComparator;
    }

    /**
     * @param moleculeComparator the moleculeComparator to set
     */
    public void setMoleculeComparator(Comparator<? super Molecule> moleculeComparator) {
        this.moleculeComparator = moleculeComparator;
        if (detailsNode != null) {
            detailsNode.sortMolecules(moleculeComparator);
        }
    }

    @Override
    public boolean hasTooltip() {
        return true; //always have a tooltip to show
    }

    @Override
    public Structure getStructure() {
        return getScaffold();
    }

}