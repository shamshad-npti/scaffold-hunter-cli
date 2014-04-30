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
 * @author Dominic Sacré
 * 
 */
public class MockView implements View {

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.View#setSubset(edu.udo.scaffoldhunter.model
     * .db.Subset)
     */
    @Override
    public void setSubset(Subset subset) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#getSubset()
     */
    @Override
    public Subset getSubset() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.View#setInstanceConfig(edu.udo.scaffoldhunter
     * .model.ViewInstanceConfig)
     */
    @Override
    public void setInstanceConfig(ViewInstanceConfig instanceConfig) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#getInstanceConfig()
     */
    @Override
    public ViewInstanceConfig getInstanceConfig() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.View#setClassConfig(edu.udo.scaffoldhunter
     * .model.ViewClassConfig)
     */
    @Override
    public void setClassConfig(ViewClassConfig classConfig) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.View#setGlobalConfig(edu.udo.scaffoldhunter
     * .model.GlobalConfig)
     */
    @Override
    public void setGlobalConfig(GlobalConfig globalConfig) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#getState()
     */
    @Override
    public ViewState getState() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#isDisposable()
     */
    @Override
    public boolean isDisposable() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#getComponent()
     */
    @Override
    public JComponent getComponent() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#getMenu()
     */
    @Override
    public JMenu getMenu() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#getExportActions()
     */
    @Override
    public List<Action> getExportActions() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#getToolBar()
     */
    @Override
    public JToolBar getToolBar() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#getSideBarItems()
     */
    @Override
    public List<SideBarItem> getSideBarItems() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.view.View#destroy()
     */
    @Override
    public void destroy() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.View#addPropertyChangeListener(java.lang.
     * String, java.beans.PropertyChangeListener)
     */
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.view.View#removePropertyChangeListener(java.lang
     * .String, java.beans.PropertyChangeListener)
     */
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {

    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.view.View#focusMolecule(edu.udo.scaffoldhunter.model.db.Molecule)
     */
    @Override
    public void focusMolecule(Molecule molecule) {
        
    }

}
