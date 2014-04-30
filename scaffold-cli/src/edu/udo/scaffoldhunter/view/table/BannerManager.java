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

package edu.udo.scaffoldhunter.view.table;

import java.util.ArrayList;
import java.util.List;

import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener;
import edu.udo.scaffoldhunter.model.db.Banner;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * a class to maintain the banners of a subset 
 * 
 * @author Michael Hesse
 *
 */
public class BannerManager implements BannerChangeListener {

    enum BannerState { PRIVATE, PUBLIC, CLEARED, UNKNOWN };
    enum BannerChangeProcessState { READY, STOP };
    BannerChangeProcessState bannerChangeProcessState;

    private final BannerPool bp;

    private List <BannerManagerListener> listeners = new ArrayList <BannerManagerListener> ();
    
    /**
     * @param subs
     * @param bp 
     */
    public BannerManager (Subset subs, BannerPool bp) {
        this.bp = bp;
        bp.addBannerChangeListener(this);
        bannerChangeProcessState = BannerChangeProcessState.READY;
    }
    
    /**
     * 
     */
    public void destroy() {
        bp.removeBannerChangeListener(this);
    }
    
    /**
     * @param molecule
     * @return
     *  the current state
     */
    public synchronized BannerState getPrivateBanner(Molecule molecule) {
        if (bp.hasBanner(molecule, true))
            return BannerState.PRIVATE;
        else
            return BannerState.CLEARED;
    }

    /**
     * @param molecule
     * @return
     *  the current state
     */
    public synchronized BannerState getPublicBanner(Molecule molecule) {
        if (bp.hasBanner(molecule, false))
            return BannerState.PUBLIC;
        else
            return BannerState.CLEARED;
    }

    /**
     * @param molecule
     * @param state
     */
    public synchronized void  setPublicBanner(Molecule molecule, BannerState state) {
        if(bannerChangeProcessState == BannerChangeProcessState.READY) {
            bannerChangeProcessState = BannerChangeProcessState.STOP;
            if( bp.hasBanner(molecule, false) && (state == BannerState.CLEARED) ) {
                // clear banner
                bp.removeBanner(molecule, false);
            } else if( !bp.hasBanner(molecule, false) && (state == BannerState.PUBLIC) ){
                // set Banner
                bp.addBanner(molecule, false);
            }
            fire(molecule, false, state);
            bannerChangeProcessState = BannerChangeProcessState.READY;
        }
    }

    /**
     * @param molecule
     * @param state
     */
    public synchronized void  setPrivateBanner(Molecule molecule, BannerState state) {
        if(bannerChangeProcessState == BannerChangeProcessState.READY) {
            bannerChangeProcessState = BannerChangeProcessState.STOP;
            if( bp.hasBanner(molecule, true) && (state == BannerState.CLEARED) ) {
                // clear banner
                bp.removeBanner(molecule, true);
            } else if( !bp.hasBanner(molecule, true) && (state == BannerState.PRIVATE) ){
                // set Banner
                bp.addBanner(molecule, true);
            }
            fire(molecule, true, state);
            bannerChangeProcessState = BannerChangeProcessState.READY;
        }
    }


    
    /**
     * @param listener
     */
    public void addListener(BannerManagerListener listener) {
        listeners.add(listener);
    }
    
    /**
     * @param listener
     */
    public void removeListener(BannerManagerListener listener) {
        listeners.remove(listener);
    }

    /**
     * @param molecule
     * @param priv
     * @param state
     */
    public void fire(Molecule molecule, boolean priv, BannerState state) {
        for(BannerManagerListener l : listeners)
            l.BannerStateChanged(molecule, priv, state);
    }


    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener#bannerAdded(edu.udo.scaffoldhunter.model.db.Banner)
     */
    @Override
    public void bannerAdded(Banner banner) {
        if(bannerChangeProcessState == BannerChangeProcessState.READY) {

            bannerChangeProcessState = BannerChangeProcessState.STOP;
            if( banner.isPrivate() ) {
                setPrivateBanner( banner.getMolecule(), BannerState.PRIVATE);
            } else {
                setPublicBanner( banner.getMolecule(), BannerState.PUBLIC);
            }
            bannerChangeProcessState = BannerChangeProcessState.READY;
        }
    }


    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.BannerPool.BannerChangeListener#bannerRemoved(edu.udo.scaffoldhunter.model.db.Banner)
     */
    @Override
    public void bannerRemoved(Banner banner) {
        if(bannerChangeProcessState == BannerChangeProcessState.READY) {
            bannerChangeProcessState = BannerChangeProcessState.STOP;
            if( banner.isPrivate() ) {
                setPrivateBanner( banner.getMolecule(), BannerState.CLEARED);
            } else {
                setPublicBanner( banner.getMolecule(), BannerState.CLEARED);
            }
            bannerChangeProcessState = BannerChangeProcessState.READY;
        }
    }
    
}
