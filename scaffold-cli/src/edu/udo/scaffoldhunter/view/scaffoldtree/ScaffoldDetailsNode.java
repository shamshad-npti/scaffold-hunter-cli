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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

import org.apache.batik.ext.awt.RenderingHintsKeyExt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.Orderings;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.util.SVG;
import edu.udo.scaffoldhunter.view.util.SVGCache;
import edu.udo.scaffoldhunter.view.util.SVGLoadObserver;
import edu.udo.scaffoldhunter.view.util.TooltipNode;
import edu.umd.cs.piccolo.PNode;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * A node which shows details about one specific scaffolds, mainly the Molecules
 * associated with the scaffold.
 * 
 * @author Henning Garus
 * 
 */
public class ScaffoldDetailsNode extends PNode implements SVGLoadObserver {

    private static final double BUTTON_HEIGHT = 8;
    private static final double TEXT_WIDTH = 30;

    private final Scaffold scaffold;
    private final Selection selection;
    private final List<Molecule> molecules;
    private final ImmutableList<MoleculeNode> moleculeNodes;

    private final Rectangle2D drawRegion = new Rectangle2D.Double();
    private final SVGCache cache;
    private static final SVG FORWARD_ARROW = Resources.getSVG("go-next.svg");
    private static final SVG BACKWARD_ARROW = Resources.getSVG("go-previous.svg");

    private int offset = 0;
    private int increment;
    private TextLayout text;

    private final double scaffoldRatio;
    private final Rectangle2D backButton = new Rectangle2D.Double();
    private final Rectangle2D forwardButton = new Rectangle2D.Double();
    private final Rectangle2D textField = new Rectangle2D.Double();
    private int cols;
    private int rows;

    /**
     * Create a new Node showing the molecules associated with a scaffold
     * 
     * @param scaffold
     *            the scaffold represented by this node
     * @param svgCache
     *            the svg cache used by this node
     * @param selection
     *            the selection, which determines if molecules shown by this
     *            node are selected
     * @param molComparator
     *            a comparator which is used to sort the displayed molecules if
     *            this is <code>null</code> the molecules are sorted by SMILES
     *            
     */
    public ScaffoldDetailsNode(Scaffold scaffold, SVGCache svgCache, Selection selection,
            Comparator<? super Molecule> molComparator) {
        this.scaffold = scaffold;
        this.cache = svgCache;
        this.selection = selection;
        molecules = Lists.newArrayList(scaffold.getMolecules());
        if (molComparator == null)
            molComparator = Orderings.STRUCTURE_BY_SMILES;
        Collections.sort(molecules, molComparator);

        addPropertyChangeListener(PROPERTY_PARENT, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (getParent() != null) {
                    parentBoundsChanged();
                }
            }
        });

        scaffoldRatio = (double) scaffold.getSvgWidth() / scaffold.getSvgHeight();

        rows = 3;
        cols = 3;

        increment = rows * cols;
        ImmutableList.Builder<MoleculeNode> b = ImmutableList.builder();
        for (int i = 0; i < increment && i < molecules.size(); i++) {
            b.add(new MoleculeNode(molecules.get(i)));
        }
        moleculeNodes = b.build();
        addChildren(moleculeNodes);
    }

    private void setListOffset(int offset) {
        if (this.offset == offset)
            return;
        this.offset = offset;
        loadMolecules();
    }

    private void loadMolecules() {
        ListIterator<Molecule> m = molecules.listIterator(offset);
        int i = 0;
        for (MoleculeNode n : moleculeNodes) {
            if (!m.hasNext() || i++ >= increment) {
                n.setMolecule(null);
            } else {
                n.setMolecule(m.next());
            }
        }
        setText();
    }

    private void setNumVisibleMols(int increment) {
        if (this.increment == increment) {
            return;
        }
        this.increment = increment;
        loadMolecules();
    }

    void setText() {
        int upper = Math.min(offset + increment, scaffold.getMolecules().size());
        text = new TextLayout(String.format("%d - %d / %d", offset + 1, upper, scaffold.getMolecules().size()),
                new Font(Font.SANS_SERIF, Font.PLAIN, 10), PPaintContext.RENDER_QUALITY_HIGH_FRC);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.umd.cs.piccolo.PNode#parentBoundsChanged()
     */
    @Override
    protected void parentBoundsChanged() {
        super.parentBoundsChanged();
        double minRatio = Double.POSITIVE_INFINITY;
        double maxRatio = Double.NEGATIVE_INFINITY;
        double nodeRatio = getBoundsReference().getWidth() / getBoundsReference().getHeight();
        for (Molecule m : scaffold.getMolecules()) {
            double ratio = (double) m.getSvgWidth() / m.getSvgHeight();
            minRatio = Math.min(minRatio, ratio);
            maxRatio = Math.max(maxRatio, ratio);
        }

        // if the ratios differ somewhat more drastically reduce the number of
        // columns / rows
        if (minRatio < nodeRatio / 2) {
            cols = 2;
        } else {
            cols = 3;
        }
        if (maxRatio > nodeRatio * 2) {
            rows = 2;
        } else {
            rows = 3;
        }
        setBounds(parentToLocal(getParent().getBounds()));
        invalidateLayout();
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.umd.cs.piccolo.PNode#layoutChildren()
     */
    @Override
    protected void layoutChildren() {
        // place the molecules in a grid with up to "cols" columns and "rows"
        // rows

        // reduce the number of columns/rows based on the size of the region
        // which is actually visible
        int maxRows = (int) Math.min(rows,
                Math.round(rows * (drawRegion.getHeight() / (getHeight() / getGlobalScale()))));
        int maxCols = (int) Math
                .min(cols, Math.round(cols * (drawRegion.getWidth() / (getWidth() / getGlobalScale()))));

        setNumVisibleMols(maxRows * maxCols);
        if (increment == 0) {
            return;
        }
        // if the grid can hold more than the number of remaining nodes reduce
        // its size once more
        int remainingMolecules = molecules.size() - offset;
        int r = 1;
        int c = 1;
        while (r * c < remainingMolecules && (r < maxRows || c < maxCols)) {
            if (scaffoldRatio <= 1) {
                if ((r < c || c == maxCols) && r < maxRows) {
                    r++;
                } else {
                    c++;
                }
            } else {
                if ((c < r || r == maxRows) && c < maxCols) {
                    c++;
                } else {
                    r++;
                }
            }
        }
        // place the molecules in the grid
        double height = drawRegion.getHeight() * 0.9;
        double yoffset = drawRegion.getY();
        double w = drawRegion.getWidth() / c;
        double h = height / r;
        outer: for (int i = 0; i < r; i++) {
            double xoffset = drawRegion.getX();
            for (int j = 0; j < c; j++) {
                if (i * c + j >= remainingMolecules)
                    break outer;
                PNode n = moleculeNodes.get(i * c + j);

                n.setOffset(xoffset, yoffset);
                n.setHeight(h);
                n.setWidth(w);
                xoffset += w;
            }
            yoffset += height / r;
        }

        // place the control elements below
        double buttonHeight = Math.min(drawRegion.getHeight() / 10, BUTTON_HEIGHT);
        double textWidth = Math.min(drawRegion.getWidth() / 5, TEXT_WIDTH);

        backButton.setRect(drawRegion.getCenterX() - textWidth / 2 - buttonHeight * 1.25, drawRegion.getMaxY() - 1.25
                * buttonHeight, buttonHeight, buttonHeight);
        forwardButton.setRect(drawRegion.getCenterX() + textWidth / 2 + 0.25 * buttonHeight, drawRegion.getMaxY()
                - 1.25 * buttonHeight, buttonHeight, buttonHeight);
        textField.setRect(drawRegion.getCenterX() - textWidth / 2, drawRegion.getMaxY() - 1.25 * buttonHeight,
                textWidth, buttonHeight);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext)
     */
    @Override
    protected void paint(PPaintContext paintContext) {
        super.paint(paintContext);
        // update the visible rectangle if necessary
        if (VCanvas.MAIN_CAMERA.equals(paintContext.getCamera().getName())) {
            Rectangle2D visibleRect = new Rectangle2D.Double();
            visibleRect = paintContext.getCamera().getBounds();
            paintContext.getCamera().localToView(visibleRect);
            globalToLocal(visibleRect);

            Rectangle2D r = new Rectangle2D.Double();
            Rectangle2D.intersect(getBoundsReference(), visibleRect, r);
            if (!drawRegion.equals(r)) {
                drawRegion.setRect(r);
                invalidateLayout();
            }
        }
        Graphics2D g = paintContext.getGraphics();
        g.setColor(Color.WHITE);
        g.fill(getBoundsReference());
        g.setColor(Color.BLACK);
        g.draw(drawRegion);

        // draw the background scaffold
        SVG scaf = cache.getSVG(scaffold, new Color(0.9f, 0.9f, 0.9f), null, false, null);
        scaf.paint(g, drawRegion.getX(), drawRegion.getY(), drawRegion.getWidth(), drawRegion.getHeight(), true);

        // draw the control elements
        g.draw(backButton);
        double w = backButton.getWidth() * 0.75;
        double offset = backButton.getWidth() * 0.125;
        // without this Batik prints a lot of errors on the console
        g.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING, RenderingHintsKeyExt.VALUE_TRANSCODING_PRINTING);
        BACKWARD_ARROW.paint(g, backButton.getX() + offset, backButton.getY() + offset, w, w, false);
        g.draw(forwardButton);
        FORWARD_ARROW.paint(g, forwardButton.getX() + offset, forwardButton.getY() + offset, w, w, true);

        g.draw(textField);

        Rectangle2D b = text.getBounds();
        AffineTransform t = g.getTransform();
        AffineTransform t2 = (AffineTransform) t.clone();
        // new transform to scale the text to fit inside the rectangle
        double s = Math.min(textField.getHeight() / b.getHeight(), textField.getWidth() / b.getWidth()) * 0.8;
        t2.scale(s, s);
        g.setTransform(t2);
        // center the text inside the rectangle
        text.draw(g, (float) ((textField.getX() + (textField.getWidth() - b.getWidth() * s) / 2) / s),
                (float) ((textField.getMaxY() - ((textField.getHeight() - b.getHeight() * s) / 2)) / s));
        // reset the transform
        g.setTransform(t);

    }

    /**
     * Handle a click on this node.
     * 
     * @param p
     *            the point which was clicked in global coordinates
     */
    public void clicked(Point2D p) {
        globalToLocal(p);
        if (forwardButton.contains(p)) {
            if (offset < scaffold.getMolecules().size() - increment)
                setListOffset(Math.min(offset + increment, scaffold.getMolecules().size() - 1));
        } else if (backButton.contains(p)) {
            setListOffset(Math.max(0, offset - increment));
        }
        ScaffoldDetailsNode.this.invalidateLayout();
    }

    @Override
    public void svgLoaded(SVG svg) {
        invalidatePaint();
    }

    /**
     * Sort the displayed molecules
     * 
     * @param comparator
     *            comparator used for sorting
     */
    public void sortMolecules(Comparator<? super Molecule> comparator) {
        Collections.sort(molecules, comparator);
        loadMolecules();
        invalidateLayout();
    }

    class MoleculeNode extends PNode implements SVGLoadObserver, TooltipNode {

        private Molecule molecule;

        public MoleculeNode(Molecule molecule) {
            super();
            this.molecule = molecule;
        }

        public void setMolecule(Molecule molecule) {
            if (this.molecule != null) {
                cache.getSVG(this.molecule, null, Color.WHITE, null).removeObserver(this);
            }
            setVisible(molecule != null);
            this.molecule = molecule;
            invalidatePaint();
        }

        /**
         * @return the molecule
         */
        public Molecule getMolecule() {
            return molecule;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.umd.cs.piccolo.PNode#paint(edu.umd.cs.piccolo.util.PPaintContext)
         */
        @Override
        protected void paint(PPaintContext paintContext) {
            double w = getBoundsReference().getWidth();
            double h = getBoundsReference().getHeight();

            paintContext.getGraphics().drawRect(0, 0, (int) w, (int) h);
            if (selection.contains(molecule))
                cache.getSVG(molecule, Color.RED, null, this).paint(paintContext.getGraphics(), w, h);
            else
                cache.getSVG(molecule, null, null, this).paint(paintContext.getGraphics(), w, h);
        }

        void mousePressed() {
            if (selection.contains(molecule))
                selection.remove(molecule);
            else
                selection.add(molecule);
            invalidatePaint();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.view.util.SVGLoadObserver#svgLoaded(edu.udo
         * .scaffoldhunter.view.util.SVG)
         */
        @Override
        public void svgLoaded(SVG svg) {
            invalidatePaint();
        }

        @Override
        public boolean hasTooltip() {
            return true; //always has a tooltip
        }

        @Override
        public Structure getStructure() {
            return getMolecule();
        }

    };

}
