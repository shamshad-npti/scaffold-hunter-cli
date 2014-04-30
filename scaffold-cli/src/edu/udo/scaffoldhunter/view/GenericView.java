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
import java.beans.PropertyChangeSupport;
import java.util.List;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.gui.SubsetController;
import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;

/**
 * The common base class for all views. Each view has local (per instance) as
 * well as global (per view type) configuration options. Every view also has
 * a state object that should contain all information necessary to save and
 * restore the view's current state.
 *
 * @param <InstanceConfigType>
 *          the type of this view's local configuration, derived from
 *          {@link ViewInstanceConfig}
 * @param <ClassConfigType>
 *          the type of this view's class configuration, derived from
 *          {@link ViewClassConfig}
 * @param <StateType>
 *          the type of this view's state, derived from {@link ViewState}
 *
 * @author Dominic Sacré
 */
public abstract class GenericView <
        InstanceConfigType extends ViewInstanceConfig,
        ClassConfigType extends ViewClassConfig,
        StateType extends ViewState
    > implements View {

    private static final Logger logger = LoggerFactory.getLogger(GenericView.class);

    // GenericView is not a bean and never will be, but PropertyChangeSupport
    // is handy none the less
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private final GUISession session;
    private Subset subset;
    private InstanceConfigType instanceConfig;
    private ClassConfigType classConfig;
    private GlobalConfig globalConfig;
    private final StateType state;


    /**
     * @param session
     *          the GUI session
     * @param subset
     *          the subset to be shown in this view
     * @param instanceConfig
     *          the view's instance configuration
     * @param classConfig
     *          the view's class configuration
     * @param globalConfig
     *          the global configuration
     * @param state
     *          the view's state
     *
     * @throws ClassCastException
     *          if the type of configuration, class configuration or state
     *          is not applicable to this view
     */
    protected GenericView(GUISession session,
                          Subset subset,
                          ViewInstanceConfig instanceConfig,
                          ViewClassConfig classConfig,
                          GlobalConfig globalConfig,
                          ViewState state) {
        logger.trace("session={}, subset={}, instanceConfig={}, classConfig={}, globalConfig={}, state={}",
                     new Object[]{session, subset, instanceConfig, classConfig, globalConfig, state});

        this.session = Preconditions.checkNotNull(session);

        this.subset = Preconditions.checkNotNull(subset);

        @SuppressWarnings("unchecked")
        InstanceConfigType ic = (InstanceConfigType) instanceConfig;
        this.instanceConfig = Preconditions.checkNotNull(ic);

        @SuppressWarnings("unchecked")
        ClassConfigType cc = (ClassConfigType) classConfig;
        this.classConfig = Preconditions.checkNotNull(cc);

        this.globalConfig = Preconditions.checkNotNull(globalConfig);

        @SuppressWarnings("unchecked")
        StateType s = (StateType) state;
        this.state = Preconditions.checkNotNull(s);
    }

    /**
     * @return  the DB manager
     */
    public final DbManager getDbManager() {
        return session.getGUIController().getDbManager();
    }

    /**
     * @return  the subset manager
     */
    public final SubsetController getSubsetManager() {
        return session.getSubsetController();
    }

    /**
     * @return  the selection
     */
    public final Selection getSelection() {
        return session.getSelection();
    }

    /**
     * @return  the banner pool
     */
    public final BannerPool getBannerPool() {
        return session.getBannerPool();
    }

    @Override
    public final void setSubset(Subset subset) {
        logger.trace("subset={}", subset);

        Subset oldSubset = this.subset;

        this.subset = Preconditions.checkNotNull(subset);

        firePropertyChange(SUBSET_PROPERTY, oldSubset, this.subset);
    }

    @Override
    public final Subset getSubset() {
        return subset;
    }

    @Override
    public final void setInstanceConfig(ViewInstanceConfig instanceConfig) {
        logger.trace("instanceConfig={}", instanceConfig);

        ViewInstanceConfig oldInstanceConfig = this.instanceConfig;

        @SuppressWarnings("unchecked")
        InstanceConfigType c = (InstanceConfigType) instanceConfig;
        this.instanceConfig = Preconditions.checkNotNull(c);

        firePropertyChange(INSTANCE_CONFIG_PROPERTY, oldInstanceConfig, this.instanceConfig);
    }

    @Override
    public final InstanceConfigType getInstanceConfig() {
        return instanceConfig;
    }

    @Override
    public final void setClassConfig(ViewClassConfig classConfig) {
        logger.trace("classConfig={}", classConfig);

        ViewClassConfig oldClassConfig = this.classConfig;

        @SuppressWarnings("unchecked")
        ClassConfigType c = (ClassConfigType) classConfig;
        this.classConfig = Preconditions.checkNotNull(c);

        firePropertyChange(CLASS_CONFIG_PROPERTY, oldClassConfig, this.classConfig);
    }

    /**
     * @return  the view's class configuration
     */
    public final ClassConfigType getClassConfig() {
        return classConfig;
    }

    @Override
    public final void setGlobalConfig(GlobalConfig globalConfig) {
        logger.trace("globalConfig={}", globalConfig);

        GlobalConfig oldGlobalConfig = this.globalConfig;

        this.globalConfig = Preconditions.checkNotNull(globalConfig);

        firePropertyChange(GLOBAL_CONFIG_PROPERTY, oldGlobalConfig, this.globalConfig);
    }

    /**
     * @return  the global configuration
     */
    public final GlobalConfig getGlobalConfig() {
        return globalConfig;
    }

    @Override
    public StateType getState() {
        return state;
    }

    @Override
    public boolean isDisposable() {
        return true;
    }

    @Override
    public JMenu getMenu() {
        return null;
    }

    @Override
    public List<Action> getExportActions() {
        return null;
    }

    @Override
    public JToolBar getToolBar() {
        return null;
    }

    @Override
    public List<SideBarItem> getSideBarItems() {
        return null;
    }


    @Override
    public final void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public final void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(propertyName, listener);
    }

    protected final <T> void firePropertyChange(String propertyName, T oldValue, T newValue) {
        if (logger.isTraceEnabled()) {
            logger.trace("propertyName={}, oldValue={}, newValue={}", new Object[]{propertyName, oldValue, newValue});
            PropertyChangeListener[] listeners = propertyChangeSupport.getPropertyChangeListeners(propertyName);
            logger.trace("listeners={}", new Object[]{listeners});
        }

        GenericPropertyChangeEvent<T> ev = new GenericPropertyChangeEvent<T>(this, propertyName, oldValue, newValue);
        propertyChangeSupport.firePropertyChange(ev);
    }

}
