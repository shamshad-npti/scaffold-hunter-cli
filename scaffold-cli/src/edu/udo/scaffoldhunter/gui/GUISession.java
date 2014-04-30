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

package edu.udo.scaffoldhunter.gui;

import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * @author Dominic Sacré
 *
 */
public class GUISession {

    private final GUIController ctrl;
    private final Session dbSession;

    private final SubsetController subsetController;
    private final BannerPool bannerPool;

    private ViewManager viewManager;

    private Selection selection;


    /**
     * Creates a new session with no windows and views.
     * 
     * @param ctrl
     *          the GUI controller
     * @param dbSession
     *          the underlying database session
     */
    public GUISession(GUIController ctrl, Session dbSession) {
        this.ctrl = ctrl;
        this.dbSession = dbSession;

        subsetController = new SubsetController(ctrl.getDbManager(), dbSession);
        bannerPool = new BannerPool(ctrl.getDbManager(), dbSession);

        viewManager = new ViewManager(ctrl, this);

        selection = new Selection();
    }

    /**
     * Sets the view manager.
     * 
     * @param viewManager
     */
    public void setViewManager(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    /**
     * @return  the GUI controller
     */
    public GUIController getGUIController() {
        return ctrl;
    }

    /**
     * @return  the subset manager
     */
    public SubsetController getSubsetController() {
        return subsetController;
    }

    /**
     * @return  the view manager
     */
    public ViewManager getViewManager() {
        return viewManager;
    }

    /**
     * @return  the selection
     */
    public Selection getSelection() {
        return selection;
    }

    /**
     * @return  the banner pool
     */
    public BannerPool getBannerPool() {
        return bannerPool;
    }

    /**
     * @return  the DB manager
     */
    public DbManager getDbManager() {
        return ctrl.getDbManager();
    }

    /**
     * @return  the underlying database session
     */
    public Session getDbSession() {
        return dbSession;
    }

    /**
     * @return  the root subset
     */
    public Subset getRootSubset() {
        return dbSession.getSubset();
    }

}
