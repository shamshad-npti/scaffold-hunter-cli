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

package edu.udo.scaffoldhunter.view.plot;

import java.beans.PropertyChangeEvent;
import java.util.List;

import edu.udo.scaffoldhunter.model.db.Molecule;


/**
 * @author Micha
 *
 */
public interface Model {

    /**
     * @return
     *  the title of the model
     */
    public String getTitle();
    
    /**
     * @return
     *  the number of channels. each channel can be mapped to an axis.
     *  basicaly this is the number of different num properties.
     */
    public int getNumberOfChannels();
    
    /**
     * @param channel
     *  the channel for which the title should be fetched
     * @return
     *  the title of the channel/num property
     */
    public String getChannelTitle(int channel);

    /**
     * asks the model if it has data on a specified channel
     * 
     * @param channel
     *  the channel in question
     * @return
     *  true if the model can provide data on this channel
     */
    public boolean hasData(int channel);
    
    /**
     * @param channel
     * @param index
     * @return
     *  the data
     */
    public double getData(int channel, int index);

    /**
     * @param channel
     * @return
     *  the min value from the data on this channel
     */
    public double getDataMin(int channel);

    /**
     * @param channel
     * @return
     *  the max value from the data on this channel
     */
    public double getDataMax(int channel);

    /**
     * @return
     *  the number of values
     */
    public int getDataLength();

    /**
     * @param index
     * @return
     *  true if the molecule at the specified index is selected
     */
    public boolean isSelected(int index);

    /**
     * 
     * @param index
     * @param isSelected
     */
    public void setSelected(int index, boolean isSelected);
    
    /**
     * 
     * @param indexes 
     * @param isSelected
     */
    public void setSelected(List<Integer> indexes, boolean isSelected);

    /**
     * 
     * @param index
     * @return
     *  true, if a public banner for this molecule is set
     */
    public boolean hasPublicBanner(int index);
    
    /**
     * 
     * @param index
     * @return
     *  true, if a public banner for this molecule is set
     */
    public boolean hasPrivateBanner(int index);

    /**
     * 
     * @param index
     */
    public void togglePublicBanner(int index);
    
    /**
     * 
     * @param index
     */
    public void togglePrivateBanner(int index);

    /**
     * @param index
     * @return
     *  returns the molecule at the given index. needed for picking.
     */
    public Molecule getMolecule(int index);
    

    /**
     * @param hyperplanePanel 
     * 
     */
    public void setHyperplanePanel(HyperplanePanel hyperplanePanel);
    
    /**
     * @return
     *  the hyperplanepanel
     */
    public HyperplanePanel getHyperplanePanel();
    
    /**
     * @param evt
     *  a listener for various events
     *  currently only for changes of the selection
     */
    public void propertyChange(PropertyChangeEvent evt);

    
    
    /**
     * @param modelChangeListener
     */
    public void addModelChangeListener( ModelChangeListener modelChangeListener );

    /**
     * @param modelChangeListener
     */
    public void removeModelChangeListener( ModelChangeListener modelChangeListener );

    /**
     * @param channel
     * @param moreToCome
     */
    public void fireModelChange(int channel, boolean moreToCome);

}
