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

package edu.udo.scaffoldhunter.model;

import java.util.EventListener;
import java.util.List;

import javax.swing.event.EventListenerList;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.VoidUnaryDBFunction;
import edu.udo.scaffoldhunter.model.db.Banner;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * @author Dominic Sacré
 * @author Henning Garus
 * 
 */
public class BannerPool {

    private final DbManager db;
    private final Session session;

    private final EventListenerList listeners = new EventListenerList();

    private final Table<Structure, Boolean, Banner> banners = HashBasedTable.create();

    /**
     * @param db
     * @param session
     */
    public BannerPool(DbManager db, Session session) {
        this.db = db;
        this.session = session;
        //FIXME this should probably happen in background
        List<Banner> bannerList = getAllBannersFromDB(session.getSubset());
        for (Banner b : bannerList) {
            banners.put(b.getStructure(), b.isPrivate(), b);
        }
    }

    /**
     * @return a list of all banners for the root subset
     */
    public List<Banner> getAllBanners() {
        return Lists.newArrayList(banners.values());
    }

    /**
     * @param subset
     * 
     * @return a list of all banners for the given subset
     */
    public List<Banner> getAllBanners(Subset subset) {
        List<Banner> bannerList = Lists.newArrayList();
        for (Banner b : banners.values()) {
            if (b.isMolecule() && subset.contains(b.getMolecule()))
                bannerList.add(b);
        }
        return bannerList;
    }

    private List<Banner> getAllBannersFromDB(final Subset subset) {
        return DBExceptionHandler.callDBManager(db, new DBFunction<List<Banner>>() {
            @Override
            public List<Banner> call() throws DatabaseException {
                return db.getAllBanners(subset, db.getScaffolds(subset, false));
            }
        });
    }

    /**
     * Adds a banner to the given structure.
     * 
     * @param structure
     *            the molecule or scaffold to add a banner to
     * @param priv
     *            false for a public banner, true for a private one
     */
    public void addBanner(final Structure structure, final boolean priv) {
        Banner banner = DBExceptionHandler.callDBManager(db, new DBFunction<Banner>() {
            @Override
            public Banner call() throws DatabaseException {
                if (db.getBanner(priv, session.getTree(), session.getProfile(), structure) != null) {
                    // banner already exists
                    return null;
                }
                return db.createBanner(priv, session.getTree(), session.getProfile(), structure);
            }
        });

        if (banner != null) {
            fireBannerAddedEvent(banner);
            banners.put(structure, priv, banner);
        }
    }

    /**
     * Removes a banner from the given structure.
     * 
     * @param structure
     *            the molecule or scaffold to remove a banner from
     * @param priv
     *            false for a public banner, true for a private one
     */
    public void removeBanner(final Structure structure, final boolean priv) {
        Banner banner = banners.get(structure, priv);
        if (banner != null) {
            DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Banner>(banner) {
                @Override
                public void call(Banner banner) throws DatabaseException {
                    db.delete(banner);
                }
            });
            fireBannerRemovedEvent(banner);
            banners.remove(structure, priv);
        }
    }

    /**
     * Check if there is a banner set for some structure
     * 
     * @param struc
     * @param priv
     *            <code>true</code> iff the check is for a private banner
     * @return <code>true</code> iff <code>struc</code> has a banner as selected
     *         by <code>priv</code>
     */
    public boolean hasBanner(Structure struc, boolean priv) {
        return banners.contains(struc, priv);
    }
    
    /**
     * Set a banner if there is none, remove it if there is one.
     * 
     * @param struc
     *          the structure where the banner is added/removed
     * @param priv
     *          <code>true</code> if a private banner is toggled
     */
    public void toggleBanner(Structure struc, boolean priv) {
        if (hasBanner(struc, priv)) {
            removeBanner(struc, priv);
        } else {
            addBanner(struc, priv);
        }
    }

    /**
     * Listener interface for banner changes.
     */
    public interface BannerChangeListener extends EventListener {
        /**
         * Called when a banner was added.
         * 
         * @param banner
         *            the banner that was added
         */

        public void bannerAdded(Banner banner);

        /**
         * Called when a banner was removed.
         * 
         * @param banner
         *            the banner that was removed
         */
        public void bannerRemoved(Banner banner);
    }

    /**
     * @param listener
     */
    public void addBannerChangeListener(BannerChangeListener listener) {
        listeners.add(BannerChangeListener.class, listener);
    }

    /**
     * @param listener
     */
    public void removeBannerChangeListener(BannerChangeListener listener) {
        listeners.remove(BannerChangeListener.class, listener);
    }

    private void fireBannerAddedEvent(Banner banner) {
        for (BannerChangeListener listener : listeners.getListeners(BannerChangeListener.class)) {
            listener.bannerAdded(banner);
        }
    }

    private void fireBannerRemovedEvent(Banner banner) {
        for (BannerChangeListener listener : listeners.getListeners(BannerChangeListener.class)) {
            listener.bannerRemoved(banner);
        }
    }

}
