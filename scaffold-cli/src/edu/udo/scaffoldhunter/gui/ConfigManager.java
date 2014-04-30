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

package edu.udo.scaffoldhunter.gui;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.gui.dialogs.OptionsDialog;
import edu.udo.scaffoldhunter.gui.dialogs.OptionsDialog.Result;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.util.Copyable;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * @author Thorsten Flügel
 * @author Dominic Sacré
 */
public class ConfigManager {

    private final GUIController ctrl;

    private Map<View, ViewInstanceConfig> viewConfigs = Maps.newHashMap();
    private Map<View, ViewInstanceConfig> viewConfigBackups = Maps.newHashMap();
    private Map<Class<? extends View>, ViewClassConfig> viewClassConfigs = Maps.newHashMap();
    private Map<Class<? extends View>, ViewClassConfig> viewClassConfigBackups = Maps.newHashMap();
    private Map<Class<? extends View>, String> viewClassNames = Maps.newHashMap();

    private GlobalConfig globalConfig = new GlobalConfig();
    private GlobalConfig globalConfigBackup = (GlobalConfig) globalConfig.copy();

    /**
     * @param ctrl
     *          the GUI controller
     */
    public ConfigManager(GUIController ctrl) {
        this.ctrl = ctrl;

        for (Class<? extends View> klass : ViewClassRegistry.getClasses()) {
            registerClass(klass, ViewClassRegistry.getClassName(klass),
                          ViewClassRegistry.instantiateClassConfig(klass));
        }
    }

    /**
     * @param viewClass
     * @param viewClassName 
     * @param config
     */
    public void registerClass(Class<? extends View> viewClass, String viewClassName, ViewClassConfig config) {
        viewClassConfigs.put(viewClass, (ViewClassConfig) config.copy());
        viewClassConfigBackups.put(viewClass, (ViewClassConfig) config.copy());
        viewClassNames.put(viewClass, viewClassName);
    }
    
    /**
     * @param viewClass 
     * @return the configuration shared by all instances of viewClass
     */
    public ViewClassConfig getViewClassConfig(Class<? extends View> viewClass) {
        return viewClassConfigs.get(viewClass);
    }
    
    /**
     * sets the class configuration for a specific view class and updates all
     * views of that class.
     * 
     * @param viewClass
     *            the class that receives the new configuration
     * @param config
     *            the new configuration
     */
    public void setViewClassConfig(Class<? extends View> viewClass, ViewClassConfig config) {
        viewClassConfigs.put(viewClass, (ViewClassConfig) config.copy());
        viewClassConfigBackups.put(viewClass, (ViewClassConfig) config.copy());

        if (ctrl.getCurrentSession() != null) {
            updateClassConfigs(ctrl.getCurrentSession().getViewManager().getAllViewsOfClass(viewClass));
        }
    }

    /**
     * @return the global config
     */
    public GlobalConfig getGlobalConfig() {
        return globalConfig;
    }
    
    /**
     * sets the new global configuration and updates the views.
     * 
     * @param globalConfig
     *            the new configuration
     */
    public void setGlobalConfig(GlobalConfig globalConfig) {
        this.globalConfig = (GlobalConfig) globalConfig.copy();
        this.globalConfigBackup = (GlobalConfig) globalConfig.copy();

        if (ctrl.getCurrentSession() != null) {
            updateGlobalConfig(ctrl.getCurrentSession().getViewManager().getAllViews());
        }
    }

    /**
     * @param parent
     *          the dialog's parent window. 
     * @param views 
     * @param activeView
     *          the property page for this view will be activated in the dialog
     *          if possible
     */
    public void showGlobalOptionsDialog(MainFrame parent, List<View> views, View activeView) {
        try {
            OptionsDialog options = new OptionsDialog(parent, null);

            globalConfigBackup = (GlobalConfig) globalConfig.copy();
            options.addOptionsTab(_("OptionsDialog.GeneralConfigurationTab"), globalConfig, globalConfig.getClass());
            
            for (Map.Entry<Class<? extends View>, ViewClassConfig> e : viewClassConfigs.entrySet()) {
                viewClassConfigBackups.put(e.getKey(), (ViewClassConfig) e.getValue().copy());
                options.addOptionsTab(viewClassNames.get(e.getKey()), e.getValue(), e.getKey());           
            }
            
            if (activeView != null) {
                options.setActiveTab(activeView.getClass());
            } else {
                options.setActiveTab(globalConfig.getClass());
            }
            options.addResultListener(new GlobalOptionsResultListener());

            options.setVisible(true);            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "An error ocurred while trying to create the options dialog");
        }
    }
    
    /**
     * @param parent
     *          the dialog's parent window. 
     * @param views 
     * @param activeView
     *          the property page for this view will be activated in the dialog
     *          if possible
     */
    public void showOptionsDialog(MainFrame parent, List<View> views, View activeView) {
        try {
            SetDefaultAction buttonAction = new SetDefaultAction();
            OptionsDialog options = new OptionsDialog(parent, buttonAction);
            buttonAction.setDialog(options);
            
            for (View view : views) {
                if (view.getInstanceConfig() != null) {
                    ViewInstanceConfig viewConfig = (ViewInstanceConfig) view.getInstanceConfig().copy(); 
                    viewConfigs.put(view, viewConfig);
                    viewConfigBackups.put(view, (ViewInstanceConfig) viewConfig.copy());
                    options.addOptionsTab(ctrl.getCurrentSession().getViewManager().getViewState(view).getTabTitle(),
                            viewConfig, view);
                }
            }
            
            if (activeView != null) {
                options.setActiveTab(activeView);
            }
            options.addResultListener(new OptionsResultListener());
    
            options.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parent, "An error ocurred while trying to create the options dialog");
        }
    }
    
    /**
     * updates the global config for the given views' 
     * @param views
     *          the views that will be updated by setting their new class configs
     */
    public void updateGlobalConfig(Iterable<View> views) {
        for (View view : views) {
            GlobalConfig newGlobalConfig = globalConfig;

            if (newGlobalConfig != null) {
                view.setGlobalConfig((GlobalConfig) newGlobalConfig.copy());
            }
        }
    }

    /**
     * updates the given views' class configs
     * @param views
     *          the views that will be updated by setting their new class configs
     */
    public void updateClassConfigs(Iterable<View> views) {
        for (View view : views) {
            ViewClassConfig newViewConfig = viewClassConfigs.get(view.getClass());

            if (newViewConfig != null) {
                view.setClassConfig((ViewClassConfig) newViewConfig.copy());
            }
        }
    }

    /**
     * updates the given views' instance configs
     * @param views
     *          the views that will be updated by setting their new instance configs
     */
    public void updateInstanceConfigs(Iterable<View> views) {
        for (View view : views) {
            ViewInstanceConfig newViewConfig = viewConfigs.get(view);

            if (newViewConfig != null) {
                view.setInstanceConfig((ViewInstanceConfig) newViewConfig.copy());
            }
        }
    }
    
    /**
     * Copies the entries of a source map to a destination map by making deep copies of the values. Entries of destination that don't exist in the source won't be changed.
     * 
     * TODO move to util
     * 
     * @param <K>
     * @param <V>
     * @param source
     * @param destination
     */
    @SuppressWarnings("unchecked")
    private <K, V extends Copyable> void copyMap(Map<K, V> source, Map<K, V> destination) {
        for (Entry<K, V> e : source.entrySet()) {
            destination.put(e.getKey(), (V) e.getValue().copy());
        }
    }

    private class SetDefaultAction extends AbstractAction {
        private WeakReference<OptionsDialog> dialog;

        public SetDefaultAction() {
            super(_("OptionsDialog.SetAsDefault"));
            putValue(SHORT_DESCRIPTION, _("OptionsDialog.SetAsDefault.Description"));
        }

        public void setDialog(OptionsDialog dialog) {
            this.dialog = new WeakReference<OptionsDialog>(dialog);
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            OptionsDialog dlg = dialog.get();
            if (dlg != null) {
                View view = (View) dlg.getCurrentId();
                if (view != null) {
                    ViewClassConfig classConfig = viewClassConfigs.get(view.getClass());
                    ViewInstanceConfig config = viewConfigs.get(view);
                    classConfig.setDefaultConfig(config);
                } else {
                    JOptionPane.showMessageDialog(dlg,
                            "An error occured while trying to set the current configuration as the default");
                }
            }
        }
    }
    
    private class OptionsResultListener implements OptionsDialog.ResultEventListener {
        @Override
        public void processEvent(Result result) {
            if (result == Result.OK) {
                copyMap(viewConfigs, viewConfigBackups);
                updateInstanceConfigs(ctrl.getCurrentSession().getViewManager().getAllViews());
            } else /* Result.CANCEL */{
                copyMap(viewConfigBackups, viewConfigs);
            }
        }
    }

    private class GlobalOptionsResultListener implements OptionsDialog.ResultEventListener {
        @Override
        public void processEvent(Result result) {
            if (result == Result.OK) {
                copyMap(viewClassConfigs, viewClassConfigBackups);
                updateClassConfigs(ctrl.getCurrentSession().getViewManager().getAllViews());
                globalConfigBackup = (GlobalConfig) globalConfig.copy();
                updateGlobalConfig(ctrl.getCurrentSession().getViewManager().getAllViews());
            } else /* Result.CANCEL */{
                copyMap(viewClassConfigBackups, viewClassConfigs);
                globalConfig = (GlobalConfig) globalConfigBackup.copy();
            }
        }
    }
}
