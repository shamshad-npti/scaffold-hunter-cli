/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See the file README.txt in the root directory of the Scaffold Hunter
 * source tree for details.
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

package edu.udo.scaffoldhunter.model.db;

import java.io.Serializable;

/**
 * Plugin Configs
 * 
 * @author Till Schäfer
 */
public class PluginConfig extends DbObject {
    private Profile profile;
    private String pluginId;
    private Serializable config;

    /**
     * Constructor
     */
    public PluginConfig() {

    }

    /**
     * Constructor
     * 
     * @param profile
     *            the {@link Profile} to which the {@link PluginConfig} belongs
     * @param pluginId
     *            the UUID of the Plugin
     * @param config
     *            the config Obkject
     */
    public PluginConfig(Profile profile, String pluginId, Serializable config) {

    }

    /**
     * @return the profile
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * @param profile
     *            the profile to set
     */
    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    /**
     * @return the pluginId
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * @param pluginId
     *            the pluginId to set
     */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * @return the config
     */
    public Serializable getConfig() {
        return config;
    }

    /**
     * @param config
     *            the config to set
     */
    public void setConfig(Serializable config) {
        this.config = config;
    }
}
