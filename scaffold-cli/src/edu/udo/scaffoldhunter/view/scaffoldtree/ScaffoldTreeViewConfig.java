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

import java.util.Map;

import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.gui.util.ConfigProperty;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.VisualFeature;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.view.scaffoldtree.Sorting.SortSettings;
import edu.udo.scaffoldhunter.view.scaffoldtree.config.ConfigMapping;

/**
 * @author Bernhard Dick
 * 
 */
public class ScaffoldTreeViewConfig extends ViewInstanceConfig {
    
    @ConfigProperty
    private boolean hideSubtreeEdges = true;
    @ConfigProperty
    private VLayoutsEnum layout = VLayoutsEnum.RADIAL_LAYOUT;
    private String moleculeOrderProperty;
    private Map<VisualFeature, ConfigMapping> mappings = Maps.newEnumMap(VisualFeature.class);
    private SortSettings sortSettings = new SortSettings();
    private SortState sortState = new SortState();

    /**
     * @param hideSubtreeEdges
     *            the hideSubtreeEdges to set
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
     * @param layout
     *            the layout to set
     */
    public void setLayout(VLayoutsEnum layout) {
        this.layout = layout;
    }

    /**
     * @return the layout
     */
    public VLayoutsEnum getLayout() {
        return layout;
    }

    /**
     * @return the mappings
     */
    public Map<VisualFeature, ConfigMapping> getMappings() {
        return mappings;
    }

    /**
     * @param mappings the mappings to set
     */
    public void setMappings(Map<VisualFeature, ConfigMapping> mappings) {
        this.mappings = mappings;
    }

    /**
     * @return the sortSettings
     */
    public SortSettings getSortSettings() {
        return sortSettings;
    }

    /**
     * @param sortSettings the sortSettings to set
     */
    public void setSortSettings(SortSettings sortSettings) {
        this.sortSettings = sortSettings;
    }

    /**
     * @return the sortState
     */
    public SortState getSortState() {
        return sortState;
    }

    /**
     * @param sortState the sortState to set
     */
    public void setSortState(SortState sortState) {
        this.sortState = sortState;
    }

    /**
     * @param dataset the current dataset
     * @return the moleculeOrderProperty
     */
    public PropertyDefinition getMoleculeOrderProperty(Dataset dataset) {
        return dataset.getPropertyDefinitions().get(moleculeOrderProperty);
    }

    /**
     * @param propertyDefinition the moleculeOrderProperty to set
     */
    public void setMoleculeOrderProperty(PropertyDefinition propertyDefinition) {
        this.moleculeOrderProperty = propertyDefinition == null ? null : propertyDefinition.getKey();
    }
    
}
