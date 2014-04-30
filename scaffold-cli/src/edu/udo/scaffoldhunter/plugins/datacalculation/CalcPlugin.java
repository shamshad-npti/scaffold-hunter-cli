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

package edu.udo.scaffoldhunter.plugins.datacalculation;

import java.io.Serializable;
import java.util.Set;

import net.xeoh.plugins.base.Plugin;

import org.openscience.cdk.interfaces.IAtomContainer;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

/**
 * @author Bernhard Dick
 * 
 */
public interface CalcPlugin extends Plugin {
    /**
     * @return a short name of a the plugin
     */
    public String getTitle();

    /**
     * @return an hopefully unique ID of the plugin
     */
    public String getID();

    /**
     * @return a short description of the plugin
     */
    public String getDescription();

    /**
     * @param availableProperties
     *            the properties that are available in the dataset and can be
     *            used to calculate new properties
     */
    public void setAvailableProperties(Set<PropertyDefinition> availableProperties);

    /**
     * @param settings
     *            the saved Settings Object from that plugin or null if it's the
     *            first run
     * @param arguments
     *            The plugin Arguments
     * @return the JComponent containing the settings "dialog" from a plugin
     */
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments);

    /**
     * Do the real plugin task
     * 
     * @param arguments
     *            the plugin Arguments for a single plugin run
     * @param molecules
     *            an Iterator with the plugins for whose the calc plugin should
     *            calculate its results
     * @param msgListener
     *            a {@link MessageListener} which can be informed with
     *            {@link Message}s by the plugin.
     * @return a CalcPluginResults Object containing the plugin run with the
     *         given Arguments
     */
    public CalcPluginResults getResults(Object arguments, Iterable<IAtomContainer> molecules, MessageListener msgListener);
}
