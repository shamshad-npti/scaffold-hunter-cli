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

package edu.udo.scaffoldhunter.view;

import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * The interface implemented by all views. This is the view's "external"
 * interface, as used elsewhere in Scaffold Hunter. Concrete views should not
 * implement this interface directly, but derive from {@link GenericView}
 * instead.
 * 
 * @author Dominic Sacré
 */
public interface View {

    /**
     * The subset property name
     */
    public static final String SUBSET_PROPERTY = "subset";
    /**
     * The instance config property name
     */
    public static final String INSTANCE_CONFIG_PROPERTY = "instanceConfig";
    /**
     * The class config property name
     */
    public static final String CLASS_CONFIG_PROPERTY = "classConfig";
    /**
     * the global config property name
     */
    public static final String GLOBAL_CONFIG_PROPERTY = "globalConfig";
    /**
     * the view's content property. fired if some parts of the view, for example
     * the component or the menu, are changed from within the view.
     * TODO: split up into separate listeners for each of the parts that may
     * change?
     * FIXME: this is not actually a property (it doesn't have a value)
     */
    public static final String CONTENT_PROPERTY = "viewContent";

    /**
     * Changes the subset shown in this view.
     * 
     * @param subset
     *            the new subset to be shown in this view
     */
    public void setSubset(Subset subset);

    /**
     * @return the subset shown in this view
     */
    public Subset getSubset();

    /**
     * Replaces the view's configuration
     * 
     * @param instanceConfig
     *            the new instance config object
     * 
     * @throws ClassCastException
     *             if the type of configuration is not applicable to this view
     */
    public void setInstanceConfig(ViewInstanceConfig instanceConfig);

    /**
     * @return the view's configuration
     */
    public ViewInstanceConfig getInstanceConfig();

    /**
     * Replaces the view's class configuration
     * 
     * @param classConfig
     *            the new class configuration object
     * 
     * @throws ClassCastException
     *             if the type of configuration is not applicable to this view
     */
    public void setClassConfig(ViewClassConfig classConfig);

    /**
     * Replaces the view's global configuration
     * 
     * @param globalConfig
     *            the new global configuration object
     */
    public void setGlobalConfig(GlobalConfig globalConfig);

    /**
     * @return the view's current state
     */
    public ViewState getState();

    /**
     * Checks if this view may be closed (or its subset changed) without asking
     * for confirmation from the user.
     * 
     * @return true if the view may be closed, otherwise false
     */
    public boolean isDisposable();

    /**
     * @return the actual view component
     */
    public JComponent getComponent();

    /**
     * XXX: the return type is still subject to change.
     * 
     * @return a view-specific menu. may be null if there is no menu associated
     *         with this view
     */
    public JMenu getMenu();

    /**
     * @return a list of actions to be included in the export menu. may be null
     *         if the view has no export functionality
     */
    public List<Action> getExportActions();

    /**
     * @return the view-specific tool bar for this view. may be null if the view
     *         has no tool bar
     */
    public JToolBar getToolBar();

    /**
     * @return a list of components to be included in the side bar. may be null
     *         if the view has no side bar items
     */
    public List<SideBarItem> getSideBarItems();

    /**
     * Called do destroy the view and to any necessary cleanup.
     */
    public void destroy();

    /**
     * Adds a property change listener to this view
     * 
     * @param propertyName
     * @param listener
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener);

    /**
     * Removed a property change listener from this view
     * 
     * @param propertyName
     * @param listener
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener);
    
    /**
     * Focuses the specified molecule
     * 
     * @param molecule
     */
    public void focusMolecule(Molecule molecule);

}