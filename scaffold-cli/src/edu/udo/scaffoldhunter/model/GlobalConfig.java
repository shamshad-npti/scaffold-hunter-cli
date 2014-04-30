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

package edu.udo.scaffoldhunter.model;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.List;

import javax.swing.JComponent;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.gui.util.ConfigProperty;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.Copyable;
import edu.udo.scaffoldhunter.view.RenderingQuality;
import edu.udo.scaffoldhunter.view.util.ToolTipPropertyConfigurationEntry;
import edu.udo.scaffoldhunter.view.util.TooltipManager;

/**
 * @author Dominic Sacré
 */
public class GlobalConfig extends Copyable {

    private final static Color SELECTED = Color.RED;
    private final static Color PARTIALLY_SELECTED = Color.ORANGE;
    private final static Color UNSELECTED = Color.BLACK;
    private final static Color NOT_SELECTABLE = Color.GRAY;

    @ConfigProperty
    private RenderingQuality renderingQuality = RenderingQuality.AUTO;
    @ConfigProperty
    private boolean showTooltip = true;
    @ConfigProperty
    private int tooltipDelay = 2500;
    @ConfigProperty
    private boolean tooltipShowUndefinedProperties = false;
    @ConfigProperty
    private Dimension tooltipMaxSVGSize = new Dimension(400, 400);
    private final List<ToolTipPropertyConfigurationEntry> tooltipProperties = Lists.newArrayList();
    private boolean ttPropertiesInitialized = false;

    /**
     * @return the color for selected structures
     */
    public Color getSelectedColor() {
        return SELECTED;
    }

    /**
     * @return the color for partially selected structures
     */
    public Color getPartiallySelectedColor() {
        return PARTIALLY_SELECTED;
    }

    /**
     * @return the color for unselected structures
     */
    public Color getUnselectedColor() {
        return UNSELECTED;
    }

    /**
     * @return the color for structures which are not selectable
     */
    public Color getNotSelectableColor() {
        return NOT_SELECTABLE;
    }

    /**
     * @return the tooltipDelay
     */
    public int getTooltipDelay() {
        return tooltipDelay;
    }

    /**
     * @param tooltipDelay
     *            the tooltipDelay to set
     */
    public void setTooltipDelay(int tooltipDelay) {
        this.tooltipDelay = tooltipDelay;
    }

    /**
     * @return the showTooltip
     */
    public boolean isShowTooltip() {
        return showTooltip;
    }

    /**
     * @param showTooltip
     *            the showTooltip to set
     */
    public void setShowTooltip(boolean showTooltip) {
        this.showTooltip = showTooltip;
    }

    /**
     * @param renderingQuality
     *            the renderingQuality to set
     */
    public void setRenderingQuality(RenderingQuality renderingQuality) {
        this.renderingQuality = renderingQuality;
    }

    /**
     * @return the renderingQuality
     */
    public RenderingQuality getRenderingQuality() {
        return renderingQuality;
    }

    /**
     * @return the tooltipShowUndefinedProperties
     */
    public boolean isTooltipShowUndefinedProperties() {
        return tooltipShowUndefinedProperties;
    }

    /**
     * @param tooltipShowUndefinedProperties
     *            the tooltipShowUndefinedProperties to set
     */
    public void setTooltipShowUndefinedProperties(boolean tooltipShowUndefinedProperties) {
        this.tooltipShowUndefinedProperties = tooltipShowUndefinedProperties;
    }

    /**
     * @param tooltipMaxSVGSize
     *            the tooltipMaxSVGSize to set
     */
    public void setTooltipMaxSVGSize(Dimension tooltipMaxSVGSize) {
        this.tooltipMaxSVGSize = tooltipMaxSVGSize;
    }

    /**
     * @return the tooltipMaxSVGSize
     */
    public Dimension getTooltipMaxSVGSize() {
        return tooltipMaxSVGSize;
    }

    /**
     * a List of {@link ToolTipPropertyConfigurationEntry}s that should be shown
     * in the tooltip. Note that if you later show the tooltip with
     * {@link TooltipManager#showTooltip(Point, JComponent, Rectangle2D, Structure)}
     * for a subtype of {@link Structure} only those properties are shown that
     * match for the subtype (example: For a {@link Molecule} only properties
     * where {@link PropertyDefinition#isScaffoldProperty()} returns false are
     * shown).
     * 
     * @param dataset 
     *          the current dataset
     * 
     * @return the tooltipProperties
     */
    public List<ToolTipPropertyConfigurationEntry> getTooltipProperties(Dataset dataset) {
        if (!ttPropertiesInitialized) {
            for (PropertyDefinition propertyDefinition : dataset.getPropertyDefinitions().values()) {
                tooltipProperties.add(new ToolTipPropertyConfigurationEntry(propertyDefinition.getKey()));
            }
            ttPropertiesInitialized = true;
        }
        return tooltipProperties;
    }
}
