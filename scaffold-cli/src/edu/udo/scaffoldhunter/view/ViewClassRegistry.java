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

package edu.udo.scaffoldhunter.view;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import javax.swing.Icon;

import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.dendrogram.DendrogramView;
import edu.udo.scaffoldhunter.view.dendrogram.DendrogramViewClassConfig;
import edu.udo.scaffoldhunter.view.dendrogram.DendrogramViewInstanceConfig;
import edu.udo.scaffoldhunter.view.dendrogram.DendrogramViewState;
import edu.udo.scaffoldhunter.view.plot.PlotView;
import edu.udo.scaffoldhunter.view.plot.PlotViewClassConfig;
import edu.udo.scaffoldhunter.view.plot.PlotViewInstanceConfig;
import edu.udo.scaffoldhunter.view.plot.PlotViewState;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeView;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeViewClassConfig;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeViewConfig;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeViewState;
import edu.udo.scaffoldhunter.view.table.TableView;
import edu.udo.scaffoldhunter.view.table.TableViewClassConfig;
import edu.udo.scaffoldhunter.view.table.TableViewInstanceConfig;
import edu.udo.scaffoldhunter.view.table.TableViewState;
import edu.udo.scaffoldhunter.view.treemap.TreeMapView;
import edu.udo.scaffoldhunter.view.treemap.TreeMapViewClassConfig;
import edu.udo.scaffoldhunter.view.treemap.TreeMapViewInstanceConfig;
import edu.udo.scaffoldhunter.view.treemap.TreeMapViewState;

/**
 * This class maintains a list of all supported view classes, as well as their
 * corresponding class config, instance config and state types. It provides
 * methods to instantiate each of these classes.
 * 
 * @author Dominic Sacré
 */
public class ViewClassRegistry {

    private static class ViewClassInfo {
        private final String nameKey;
        private final Icon icon;
        private final Class<? extends ViewClassConfig> klassKonfigKlass;
        private final Class<? extends ViewInstanceConfig> instanceKonfigKlass;
        private final Class<? extends ViewState> stateKlass;

        public ViewClassInfo(Class<? extends ViewClassConfig> klassKonfigKlass,
                             Class<? extends ViewInstanceConfig> instanceKonfigKlass,
                             Class<? extends ViewState> stateKlass,
                             String nameKey,
                             Icon icon) {
            this.klassKonfigKlass = klassKonfigKlass;
            this.instanceKonfigKlass = instanceKonfigKlass;
            this.stateKlass = stateKlass;
            this.nameKey = nameKey;
            this.icon = icon;
        }

        public Class<? extends ViewClassConfig> getClassConfigClass() {
            return klassKonfigKlass;
        }

        public Class<? extends ViewInstanceConfig> getInstanceConfigClass() {
            return instanceKonfigKlass;
        }

        public Class<? extends ViewState> getStateClass() {
            return stateKlass;
        }

        public String getNameKey() {
            return nameKey;
        }

        public Icon getIcon() {
            return icon;
        }
    }

    private static final Map<Class<? extends View>, ViewClassInfo> viewClasses = Maps.newLinkedHashMap();

    static {
        registerClass(
                ScaffoldTreeView.class,
                ScaffoldTreeViewClassConfig.class,
                ScaffoldTreeViewConfig.class,
                ScaffoldTreeViewState.class,
                "Main.Window.AddView.ScaffoldTree",
                Resources.getIcon("view-scaffoldtree.png")
        );

        registerClass(
                DendrogramView.class,
                DendrogramViewClassConfig.class,
                DendrogramViewInstanceConfig.class,
                DendrogramViewState.class,
                "Main.Window.AddView.Dendrogram",
                Resources.getIcon("view-dendrogram.png")
        );

        registerClass(
                PlotView.class,
                PlotViewClassConfig.class,
                PlotViewInstanceConfig.class,
                PlotViewState.class,
                "Main.Window.AddView.Plot",
                Resources.getIcon("view-plot.png")
        );

        registerClass(
                TableView.class,
                TableViewClassConfig.class,
                TableViewInstanceConfig.class,
                TableViewState.class,
                "Main.Window.AddView.Table",
                Resources.getIcon("view-table.png")
        );
        
        registerClass(
                TreeMapView.class,
                TreeMapViewClassConfig.class,
                TreeMapViewInstanceConfig.class,
                TreeMapViewState.class,
                "Main.Window.AddView.TreeMap",
                Resources.getIcon("view-treemap.png")
        );
    }

    
    /**
     * @param klass
     * @param klassKonfigKlass
     * @param instanceKonfigKlass 
     * @param stateKlass
     * @param name
     * @param icon
     */
    public static void registerClass(Class<? extends View> klass,
                                     Class<? extends ViewClassConfig> klassKonfigKlass,
                                     Class<? extends ViewInstanceConfig> instanceKonfigKlass,
                                     Class<? extends ViewState> stateKlass,
                                     String name,
                                     Icon icon) {
        viewClasses.put(klass, new ViewClassInfo(klassKonfigKlass, instanceKonfigKlass, stateKlass, name, icon));
    }

    /**
     * @return  a set containing all available view classes, in the order
     *          they were registered
     */
    public static Set<Class<? extends View>> getClasses() {
        return Collections.unmodifiableSet(viewClasses.keySet());
    }

    /**
     * Calls the constructor of the given view class.
     * 
     * @param klass
     * @param session
     * @param subset
     * @param instanceConfig
     * @param classConfig
     * @param globalConfig
     * @param state
     * 
     * @return  the new view
     */
    public static View instantiate(Class<? extends View> klass,
                                   GUISession session,
                                   Subset subset,
                                   ViewInstanceConfig instanceConfig,
                                   ViewClassConfig classConfig,
                                   GlobalConfig globalConfig,
                                   ViewState state) {
        try {
            Constructor<? extends View> ctor = klass.getConstructor(
                            GUISession.class,
                            Subset.class,
                            ViewInstanceConfig.class,
                            ViewClassConfig.class,
                            GlobalConfig.class,
                            ViewState.class);
            return ctor.newInstance(session, subset, instanceConfig, classConfig, globalConfig, state);

        } catch (SecurityException ex) {
            throw new RuntimeException(ex);
        } catch (NoSuchMethodException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException(ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        } catch (InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param klass
     *          the view class
     * 
     * @return  an object of the class that holds the class config for the
     *          given type of view
     */
    public static ViewClassConfig instantiateClassConfig(Class<? extends View> klass) {
        try {
            return getClassConfigClass(klass).newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param klass
     *          the view class
     * 
     * @return  an object of the class that holds the instance config for the
     *          given type of view
     */
    public static ViewInstanceConfig instantiateInstanceConfig(Class<? extends View> klass) {
        try {
            return getInstanceConfigClass(klass).newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param klass
     *          the view class
     * 
     * @return  an object of the class that holds the state for the given type
     *          of view
     */
    public static ViewState instantiateState(Class<? extends View> klass) {
        try {
            return getStateClass(klass).newInstance();
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @param klass
     *          the view class
     *
     * @return  the class that holds the class config for the given type of
     *          view
     */
    public static Class<? extends ViewClassConfig> getClassConfigClass(Class<? extends View> klass) {
        return viewClasses.get(klass).getClassConfigClass();
    }

    /**
     * @param klass
     *          the view class
     *
     * @return  the class that holds the instance config for the given type of
     *          view
     */
    public static Class<? extends ViewInstanceConfig> getInstanceConfigClass(Class<? extends View> klass) {
        return viewClasses.get(klass).getInstanceConfigClass();
    }

    /**
     * @param klass
     *          the view class
     *
     * @return  the class that holds the state for the given type of view
     */
    public static Class<? extends ViewState> getStateClass(Class<? extends View> klass) {
        return viewClasses.get(klass).getStateClass();
    }

    /**
     * @param klass
     *          the view class
     * 
     * @return  the name the GUI should use for the given type of view
     */
    public static String getClassName(Class<? extends View> klass) {
        if (viewClasses.get(klass).getNameKey() != null) {
            return I18n.get(viewClasses.get(klass).getNameKey());
        } else {
            return null;
        }
    }

    /**
     * @param klass
     *          the view class
     * 
     * @return  the icon the GUI should use for the given type of view
     */
    public static Icon getClassIcon(Class<? extends View> klass) {
        return viewClasses.get(klass).getIcon();
    }

    /**
     * @param name
     *          the name of the class
     *          
     * @return  the view class with the given name
     */
    public static Class<? extends View> getClassForName(String name) {
        for (Class<? extends View> klass : viewClasses.keySet()) {
            if (klass.getSimpleName().equals(name)) {
                return klass;
            }
        }
        return null;
    }

}
