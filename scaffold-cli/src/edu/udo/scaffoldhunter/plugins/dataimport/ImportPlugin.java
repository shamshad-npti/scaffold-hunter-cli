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

package edu.udo.scaffoldhunter.plugins.dataimport;

import java.io.Serializable;

import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;

import net.xeoh.plugins.base.Plugin;

/**
 * The Basis interface, which has to be implemented by every dataimport plugin
 * 
 * @author Bernhard Dick
 * 
 */
public interface ImportPlugin extends Plugin {
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
     * @param settings
     *            the saved Settings Object from that plugin or null if it's the
     *            first run
     * @param arguments
     *            the Plugin Arguments
     * @return the JComponent containing the settings "dialog" from a plugin
     */
    public PluginSettingsPanel getSettingsPanel(Serializable settings, Object arguments);

    /**
     * Do the real plugin task
     * 
     * @param arguments
     *            the plugin Arguments for a single plugin run
     * @return a PluginResults Object containing the plugin run with the given
     *         Arguments
     */
    public PluginResults getResults(Object arguments);

    /**
     * Check if a plugin should run with the given arguments (to check bad user
     * input)
     * 
     * @param arguments
     *            the plugin arguments that have to be checked
     * @return null if everything is ok, otherwise a String describing the error
     */
    public String checkArguments(Object arguments);
}
