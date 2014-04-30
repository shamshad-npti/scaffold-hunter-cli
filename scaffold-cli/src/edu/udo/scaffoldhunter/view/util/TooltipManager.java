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

package edu.udo.scaffoldhunter.view.util;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.EclipseTooltip;
import edu.udo.scaffoldhunter.gui.util.HyperlinkedLabel;
import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.util.SHPredicates;
import edu.udo.scaffoldhunter.util.Orderings;
import edu.udo.scaffoldhunter.view.View;

/**
 * Builds and shows tooltips for Structures using {@link EclipseTooltip}.
 * 
 * @author Henning Garus
 * @author Philipp Lewe
 */
public class TooltipManager implements PropertyChangeListener {

    private final DbManager db;
    private final SVGCache svgCache;
    private JPanel tooltipPanel;
    private final Profile profile;
    private final LinkedHashMap<PropertyDefinition, ToolTipPropertyConfigurationEntry> propertyConfigurations = Maps
            .newLinkedHashMap();
    private final List<PropertyDefinition> sortedProperties = Lists.newArrayList();
    private Collection<Structure> lockingStructure = Lists.newArrayList();
    boolean showUndefinedProperties;
    Dimension maxSVGSize;

    private EclipseTooltip tooltip;

    /**
     * Create a new tooltip manager
     * 
     * @param db
     *            the DB manager
     * @param svgCache
     *            the svg cache used to retrieve the {@link Structure} svgs
     * @param profile
     *            the {@link Profile} of the current User
     * @param propertyConfigurations
     *            a {@link Collection} of
     *            {@link ToolTipPropertyConfigurationEntry}s that should be
     *            shown in the tooltip. Note that if you later show the tooltip
     *            with
     *            {@link TooltipManager#showTooltip(Point, JComponent, Rectangle2D, Structure)}
     *            for a subtype of {@link Structure} only those properties are
     *            shown that match for the subtype (example: For a
     *            {@link Molecule} only properties where
     *            {@link PropertyDefinition#isScaffoldProperty()} returns false
     *            are shown).
     * @param showUndefinedProperties
     *            setting this boolean to true shows all properties that are not
     *            defined for the given structure, false hides undefined
     *            properties
     * @param maxSVGSize
     *            the maximum dimension of structure SVGs shown in the tooltip
     *            (bigger SVGs are resized)
     * 
     */
    public TooltipManager(DbManager db, SVGCache svgCache, Profile profile,
            Collection<ToolTipPropertyConfigurationEntry> propertyConfigurations, boolean showUndefinedProperties,
            Dimension maxSVGSize) {
        this.db = db;
        this.svgCache = svgCache;
        this.profile = profile;
        this.showUndefinedProperties = showUndefinedProperties;
        this.maxSVGSize = maxSVGSize;
        setPropertyConfigurations(propertyConfigurations);
    }

    /**
     * 
     * @param db
     *            the db manager
     * @param svgCache
     *            the svg cache
     * @param profile
     *            the current profile
     * @param config
     *            the global config
     */
    public TooltipManager(DbManager db, SVGCache svgCache, Profile profile, GlobalConfig config) {
        this.db = db;
        this.svgCache = svgCache;
        this.profile = profile;
        this.showUndefinedProperties = config.isTooltipShowUndefinedProperties();
        this.maxSVGSize = config.getTooltipMaxSVGSize();
        setPropertyConfigurations(config.getTooltipProperties(profile.getCurrentSession().getDataset()));
    }

    /**
     * Build a new tooltip and show it
     * 
     * @param position
     *            screen position of the tooltip
     * @param owner
     *            owner of the tooltip
     * @param region
     *            region of interest of the tooltip
     * @param structure
     *            structure to be shown in the tooltip
     * @see EclipseTooltip#EclipseTooltip(Point, JComponent, Rectangle2D,
     *      JPanel)
     */
    public void showTooltip(Point position, JComponent owner, Rectangle2D region, Structure structure) {
        // show only one tooltip
        if (tooltip != null && tooltip.isVisible())
            tooltip.destroy();
        tooltip = new EclipseTooltip(position, owner, region, buildPanel(structure));
    }

    private JPanel buildPanel(Structure structure) {
        tooltipPanel = new JPanel();
        FormLayout tooltipPanelLayout = new FormLayout("d, 2dlu, d:g", "d, 2dlu, t:d:g");
        PanelBuilder tooltipPanelBuilder = new PanelBuilder(tooltipPanelLayout, tooltipPanel);
        tooltipPanelBuilder.setDefaultDialogBorder();
        CellConstraints cc = new CellConstraints();

        tooltipPanelBuilder.add(new SizedSVGLabel(structure), cc.rc(1, 1));

        JComponent propertyComponent = buildPropertyPanel(structure);

        tooltipPanelBuilder.add(propertyComponent, cc.rchw(1, 3, 3, 1));

        CommentComponent commentComponent = new CommentComponent(db, structure, profile);

        // Textareas will be added to and removed from one of the
        // CommentComponents child panels
        // when that happens resize the dialog
        for (JPanel p : Iterables.filter(Arrays.asList(commentComponent.getComponents()), JPanel.class))
            p.addContainerListener(new ContainerListener() {

                @Override
                public void componentRemoved(ContainerEvent e) {
                    tooltip.pack();
                }

                @Override
                public void componentAdded(ContainerEvent e) {
                    tooltip.pack();
                }
            });
        tooltipPanelBuilder.add(commentComponent, cc.rcw(3, 1, 1));
        return tooltipPanel;
    }

    private JComponent buildPropertyPanel(Structure structure) {
        JScrollPane propertyScrollPane;
        final JPanel propertyPanel = new JPanel();
        FormLayout propertyPanelLayout = new FormLayout("p, 4dlu, p:g", "");
        DefaultFormBuilder propertyPanelBuilder = new DefaultFormBuilder(propertyPanelLayout, propertyPanel);

        lockingStructure.clear();
        lockingStructure.add(structure);

        try {
            db.lockAndLoad(propertyConfigurations.keySet(), lockingStructure);
        } catch (DatabaseException e) {
            // do nothing here because a popup failure message is annoying when
            // just showing a tooltip
        }

        appendProperties(structure, propertyPanelBuilder);

        appendAccumulatedProperties(structure, propertyPanelBuilder);

        db.unlockAndUnload(propertyConfigurations.keySet(), lockingStructure);

        propertyScrollPane = new JScrollPane(propertyPanel);
        propertyScrollPane.setPreferredSize(new Dimension(20, 20));
        propertyScrollPane.setPreferredSize(new Dimension(400, 400));
        propertyScrollPane.addComponentListener(new ComponentListener() {
            private boolean wasShown = false;

            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                /*
                 * set the viewport of the scrollpane to show the beginning of
                 * the property panel. Located here instead of in method
                 * componentShown(ComponentEvent e) because componentShown is
                 * curiously never called?
                 */
                if (!wasShown) {
                    propertyPanel.scrollRectToVisible(new Rectangle(0, 0, 10, 10));
                    wasShown = true;
                }
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        return propertyScrollPane;
    }

    private void appendProperties(Structure structure, DefaultFormBuilder propertyPanelBuilder) {
        if (structure instanceof Scaffold) {
            propertyPanelBuilder.appendSeparator(_("Tooltip.PropertyTitles.Scaffold"));
            propertyPanelBuilder.nextLine();
        } else if (structure instanceof Molecule) {
            propertyPanelBuilder.appendSeparator(_("Tooltip.PropertyTitles.Molecule"));
            propertyPanelBuilder.nextLine();
        }

        // append structure title
        propertyPanelBuilder.append(String.format("<html><b>%s</b></html>", _("Tooltip.StructureTitle")), new HyperlinkedLabel(
                structure.getTitle()));
        propertyPanelBuilder.nextLine();
        
        // append structure smiles
        propertyPanelBuilder.append(String.format("<html><b>%s</b></html>", "SMILES"), new HyperlinkedLabel(
                structure.getSmiles()));

        String propValue;
        for (PropertyDefinition propDef : Iterables.filter(sortedProperties, new PropertyFitsToStructurePredicate(
                structure))) {
            propValue = null;

            if (propDef.isStringProperty()) {
                if (structure.getStringPropertyValue(propDef) != null) {
                    propValue = structure.getStringPropertyValue(propDef);
                }
            } else {
                if (structure.getNumPropertyValue(propDef) != null) {
                    propValue = Double.toString(structure.getNumPropertyValue(propDef));
                }
            }

            if (propValue == null && showUndefinedProperties) {
                propValue = _("Tooltip.Property.Undefined");
            }

            if (propValue != null) {
                JLabel propertyName = new JLabel();
                propertyName.setText(String.format("<html><b>%s</b></html>", propDef.getTitle()));
                if (!propDef.getDescription().isEmpty()) {
                    propertyName.setToolTipText(propDef.getDescription());
                }
                propertyPanelBuilder.append(propertyName, new HyperlinkedLabel(propValue));
                propertyPanelBuilder.nextLine();
            }
        }
    }

    private void appendAccumulatedProperties(Structure structure, DefaultFormBuilder propertyPanelBuilder) {
        ToolTipPropertyConfigurationEntry entry;

        if (structure instanceof Scaffold) {
            propertyPanelBuilder.appendSeparator(_("Tooltip.PropertyTitles.AccumulatedMolecule"));
            propertyPanelBuilder.nextLine();
        } else {
            return;
        }

        String propValue;
        String accSubtree;
        for (PropertyDefinition propDef : Iterables.filter(sortedProperties,
                Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF))) {
            propValue = null;
            accSubtree = null;

            if (propDef.isStringProperty()) {
                continue;
            } else {
                entry = propertyConfigurations.get(propDef);

                propValue = DBExceptionHandler.callDBManager(
                        db,
                        new GetAccDBFunction(propDef, entry.getAccumulationFunction(), (Scaffold) structure, entry
                                .isAccumulationWithSubtree()));
            }

            if (propValue == null && showUndefinedProperties) {
                propValue = _("Tooltip.Property.Undefined");
            }

            if (propValue != null) {
                accSubtree = entry.isAccumulationWithSubtree() ? _("Model.SubtreeCumulative") : _("Model.Cumulative");
                JLabel propertyName = new JLabel();
                propertyName.setText(String.format("<html><b>%s</b><br>(<i>%s, %s)</i></html>", propDef.getTitle(),
                        entry.getAccumulationFunction().toString(), accSubtree));
                if (!propDef.getDescription().isEmpty()) {
                    propertyName.setToolTipText(propDef.getDescription());
                }
                propertyPanelBuilder.append(propertyName, new HyperlinkedLabel(propValue));
                propertyPanelBuilder.nextLine();
            }
        }
    }

    /**
     * hide the tooltip
     */
    public void hideTooltip() {
        if (tooltip != null)
            tooltip.destroy();
    }

    /**
     * Returns a list of of {@link ToolTipPropertyConfigurationEntry}s the
     * tooltip manager currently uses to display properties in the tooltip
     * 
     * @return the propertyConfigurations
     */
    public Collection<ToolTipPropertyConfigurationEntry> getPropertyConfigurations() {
        return propertyConfigurations.values();
    }

    /**
     * Sets the list of {@link ToolTipPropertyConfigurationEntry}s the tooltip
     * manager will use to display properties in the tooltip
     * 
     * @param propertyConfigurations
     *            the propertyConfigurations to set
     */
    public void setPropertyConfigurations(Collection<ToolTipPropertyConfigurationEntry> propertyConfigurations) {
        this.propertyConfigurations.clear();
        sortedProperties.clear();
        
        PropertyDefinition propDef = null;

        for (ToolTipPropertyConfigurationEntry entry : propertyConfigurations) {
            try {
                propDef = entry.getPropertyDefinition(profile.getCurrentSession().getDataset());
                this.propertyConfigurations.put(propDef, entry);
                this.sortedProperties.add(entry.getPropertyDefinition(profile.getCurrentSession().getDataset()));
            } catch (IllegalArgumentException e) {
                // just ignore the corrupted entry
            }
        }
        
        Collections.sort(sortedProperties, Orderings.PROPERTY_DEFINITION_BY_TITLE);
    }

    /**
     * Returns true if the tooltip manager shows undefined properties for a
     * structure
     * 
     * @return the showUndefinedProperties
     */
    public boolean isShowUndefinedProperties() {
        return showUndefinedProperties;
    }

    /**
     * Sets if the tooltip manager should show undefined properties for a
     * structure
     * 
     * @param showUndefinedProperties
     *            the showUndefinedProperties to set
     */
    public void setShowUndefinedProperties(boolean showUndefinedProperties) {
        this.showUndefinedProperties = showUndefinedProperties;
    }

    /**
     * @return the maxSVGSize
     */
    public Dimension getMaxSVGSize() {
        return maxSVGSize;
    }

    /**
     * @param maxSVGSize
     *            the maxSVGSize to set
     */
    public void setMaxSVGSize(Dimension maxSVGSize) {
        this.maxSVGSize = maxSVGSize;
    }

    private static class PropertyFitsToStructurePredicate implements Predicate<PropertyDefinition> {
        Structure structure;

        public PropertyFitsToStructurePredicate(Structure structure) {
            this.structure = structure;
        }

        @Override
        public boolean apply(PropertyDefinition propDef) {

            if (structure instanceof Molecule) {
                if (propDef.isScaffoldProperty()) {
                    return false;
                } else {
                    return true;
                }
            } else if (structure instanceof Scaffold) {
                if (propDef.isScaffoldProperty()) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
    }

    private class GetAccDBFunction implements DBFunction<String> {
        PropertyDefinition p;
        AccumulationFunction a;
        Scaffold s;
        boolean t;

        GetAccDBFunction(PropertyDefinition p, AccumulationFunction a, Scaffold s, boolean t) {
            this.p = p;
            this.a = a;
            this.s = s;
            this.t = t;
        }

        @Override
        public String call() throws DatabaseException {
            try {
                Double retVal = db.getAccNumPropertyScaffold(p, a, s, t);
                if (retVal != null) {
                    return retVal.toString();
                } else {
                    return null;
                }
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    private class SizedSVGLabel extends JLabel implements SVGLoadObserver {
        SVG svg = null;
        int h = 0;
        int w = 0;

        public SizedSVGLabel(Structure structure) {
            super("test");
            setBackground(EclipseTooltip.BACKGROUND);

            svg = svgCache.getSVG(structure, null, null, null);
            svg.addObserver(this);

            h = structure.getSvgHeight();
            w = structure.getSvgWidth();

            if (h > maxSVGSize.getHeight() || w > maxSVGSize.getWidth()) {
                double hScale = maxSVGSize.getHeight() / h;
                double wScale = maxSVGSize.getWidth() / w;

                double scale = Math.min(hScale, wScale);
                h = (int) (h * scale);
                w = (int) (w * scale);
            }
            setMinimumSize(new Dimension(w, h));
            setPreferredSize(new Dimension(w, h));
            setMaximumSize(new Dimension(w, h));
        }

        @Override
        public void paint(Graphics g) {
            if (svg == null) {
                super.paint(g);
            } else {
                svg.paint((Graphics2D) g, w, h);
            }
        }

        @Override
        public void svgLoaded(SVG svg) {
            repaint();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(View.GLOBAL_CONFIG_PROPERTY)) {
            GlobalConfig gc = (GlobalConfig) evt.getNewValue();
            setPropertyConfigurations(gc.getTooltipProperties(profile.getCurrentSession().getDataset()));
            maxSVGSize = gc.getTooltipMaxSVGSize();
        }
    }
}
