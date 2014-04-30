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
import java.util.Collection;
import java.util.HashMap;

import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterNode;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.umd.cs.piccolo.util.PBounds;

/**
 * @author Philipp Kopp
 * 
 */
public class DendrogramViewState extends ViewState {
    private HierarchicalClusterNode<Molecule> root = null;
    private double selectionbarPosition = 0;
    private double verticalZoomFactor = 0;
    private double horizontalZoomFactor = 0;
    private Rectangle scrollPosition = null;
    private boolean tableExpanded = false;
    private int tablePosition = 0;
    private Collection<String> propDefs = null;
    private Linkages linkage = null;
    private Distances distance = null;
    private NNSearchs nnSearch = null;
    private NNSearchParameters nnSearchParameters = null;
    private PBounds canvasSize = null;
    private HashMap<Integer, HierarchicalClusterNode<Molecule>> idMap = null;
    private DendrogramClusteringSettingsSave settingSave = null;
    private boolean isValid = false;

    /**
     * 
     */
    public DendrogramViewState() {

    }

    /**
     * @param root
     *            the root to set
     */
    public void setTree(HierarchicalClusterNode<Molecule> root) {
        this.root = root;
        idMap = new HashMap<Integer, HierarchicalClusterNode<Molecule>>();
        fillMap(root);
    }

    private void fillMap(HierarchicalClusterNode<Molecule> root) {
        if (root.isLeaf()) {
            idMap.put(root.getContentDbId(), root);
        } else {
            fillMap(root.getLeftChild());
            fillMap(root.getRightChild());
        }
    }

    /**
     * @param selectionbarPosition
     *            the selectionbarPosition to set
     */
    public void setSelectionbarPosition(double selectionbarPosition) {
        this.selectionbarPosition = selectionbarPosition;
    }

    /**
     * @param verticalZoomFactor
     *            the verticalZoomFactor to set
     */
    public void setVerticalZoomFactor(double verticalZoomFactor) {
        this.verticalZoomFactor = verticalZoomFactor;
    }

    /**
     * @param horizontalZoomFactor
     *            the horizontalZoomFactor to set
     */
    public void setHorizontalZoomFactor(double horizontalZoomFactor) {
        this.horizontalZoomFactor = horizontalZoomFactor;
    }

    /**
     * @param scrollPosition
     *            the scrollPosition to set
     */
    public void setScrollPosition(Rectangle scrollPosition) {
        this.scrollPosition = scrollPosition;
    }

    /**
     * /**
     * 
     * @param tableExpanded
     *            the tableExpanded to set
     */
    public void setTableExpanded(boolean tableExpanded) {
        this.tableExpanded = tableExpanded;
    }

    /**
     * @param tablePosition
     *            the tablePosition to set
     */
    public void setTablePosition(int tablePosition) {
        this.tablePosition = tablePosition;
    }

    /**
     * @param propDefs
     *            the propDefs to set
     */
    public void setPropDefs(Collection<String> propDefs) {
        this.propDefs = propDefs;
    }

    /**
     * @param linkage
     *            the {@link Linkages} to set
     */
    public void setLinkage(Linkages linkage) {
        this.linkage = linkage;
    }

    /**
     * @param distance
     *            the {@link Distances} to set
     */
    public void setDistance(Distances distance) {
        this.distance = distance;
    }

    /**
     * @param subset
     * @return the root
     */
    public HierarchicalClusterNode<Molecule> getTree(Subset subset) {
        recoverStructures(subset);
        return root;
    }

    private void recoverStructures(Subset subset) {
        for (Molecule molecule : subset) {
            idMap.get(molecule.getId()).setContent(molecule);
        }
    }

    /**
     * @return the selectionbarPosition
     */
    public double getSelectionbarPosition() {
        return selectionbarPosition;
    }

    /**
     * @return the verticalZoomFactor
     */
    public double getVerticalZoomFactor() {
        return verticalZoomFactor;
    }

    /**
     * @return the horizontalZoomFactor
     */
    public double getHorizontalZoomFactor() {
        return horizontalZoomFactor;
    }

    /**
     * @return the scrollPosition
     */
    public Rectangle getScrollPosition() {
        return scrollPosition;
    }

    /**
     * /**
     * 
     * @return the tableExpanded
     */
    public boolean isTableExpanded() {
        return tableExpanded;
    }

    /**
     * @return the tablePosition
     */
    public int getTablePosition() {
        return tablePosition;
    }

    /**
     * @return the propDefs
     */
    public Collection<String> getPropDefs() {

        return propDefs;
    }

    /**
     * @return the {@link Linkages}
     */
    public Linkages getLinkage() {
        return linkage;
    }

    /**
     * @return the {@link Distances}
     */
    public Distances getDistance() {
        return distance;
    }

    /**
     * @return the nnSearch
     */
    public NNSearchs getNnSearch() {
        return nnSearch;
    }

    /**
     * @param nnSearch the nnSearch to set
     */
    public void setNnSearch(NNSearchs nnSearch) {
        this.nnSearch = nnSearch;
    }

    /**
     * @return the nnSearchParameters
     */
    public NNSearchParameters getNnSearchParameters() {
        return nnSearchParameters;
    }

    /**
     * @param nnSearchParameters
     *            the nnSearchParameters to set
     */
    public void setNnSearchParameters(NNSearchParameters nnSearchParameters) {
        this.nnSearchParameters = nnSearchParameters;
    }

    /**
     * @param canvasSize
     */
    public void setCanvasSize(PBounds canvasSize) {
        this.canvasSize = canvasSize;
    }

    /**
     * @return the canvas size
     */
    public PBounds getCanvasSize() {
        return canvasSize;
    }

    /**
     * @param settingSave
     */
    public void setSettingSave(DendrogramClusteringSettingsSave settingSave) {
        this.settingSave = settingSave;
    }

    /**
     * @return the settingSave
     */
    public DendrogramClusteringSettingsSave getSettingSave() {
        return settingSave;
    }

    /**
     * if the saved state is valid
     * 
     * @param isValid
     */
    public void setValid(boolean isValid) {
        this.isValid = isValid;
    }

    /**
     * @return if the saved state is valid
     */
    public boolean isValid() {
        return isValid;
    }
}
