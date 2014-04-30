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

package edu.udo.scaffoldhunter.plugins;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import net.xeoh.plugins.base.PluginManager;
import net.xeoh.plugins.base.impl.PluginManagerFactory;
import net.xeoh.plugins.base.options.addpluginsfrom.OptionReportAfter;
import net.xeoh.plugins.base.util.PluginManagerUtil;
import net.xeoh.plugins.base.util.uri.ClassURI;

import com.google.common.base.Splitter;

import edu.udo.scaffoldhunter.ScaffoldHunter;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin;
import edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * The ImportPluginManager is a simple wrapper arround the jspf to load
 * ImportPlugin Instances
 * 
 * @author Bernhard Dick
 * @author Henning Garus
 * 
 */
public class SHPluginManager {
    PluginManager pm;
    PluginManagerUtil pmu;

    /**
     * Starts a new ImportPluginManager, loads all Plugins and initializes the
     * PluginManagerUtil
     */
    public SHPluginManager() {
        super();
        pm = PluginManagerFactory.createPluginManager();
        
        Properties runProps = Resources.getProperties("run.properties");
        String pluginPath = runProps.getProperty("PluginPath");
        String plugins = runProps.getProperty("PluginClasses");
        if (pluginPath != null) {
            // get the path to the Scaffold Hunter jar
            File jarPath = new File(ScaffoldHunter.class.getProtectionDomain().getCodeSource().getLocation().getPath());
            System.out.println(jarPath);
            pm.addPluginsFrom(new File(jarPath.getParent(), pluginPath).toURI());
        } else if (plugins != null) {
            for (String s : Splitter.on(' ').trimResults().omitEmptyStrings().split(plugins)) {
                pm.addPluginsFrom(ClassURI.CLASSPATH(s));
            }
        } else {    
            pm.addPluginsFrom(ClassURI.CLASSPATH, new OptionReportAfter());
        }
        
        pmu = new PluginManagerUtil(pm);
    }

    /**
     * @return a Collection of all available ImportPlugins
     */
    public Collection<ImportPlugin> getPlugins() {
        return pmu.getPlugins(ImportPlugin.class);
    }
    
    /**
     * @return a Collection of all available CalcPlugins
     */
    public Collection<CalcPlugin> getCalcPlugins() {
        return pmu.getPlugins(CalcPlugin.class);
    }

}
