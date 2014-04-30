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

package edu.udo.scaffoldhunter.gui.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractButton;
import javax.swing.Icon;

/**
 * An abstact action that automatically toggles the SELECTED_KEY property when
 * activated. Instead of overriding actionPerformed() as with other actions,
 * derived classes should override toggleActionPerformed(), which is called
 * after the SELECTED_KEY property has been changed.
 * 
 * @author Dominic Sacré
 */
public abstract class AbstractToggleAction extends AbstractAction {

    /**
     * 
     */
    public AbstractToggleAction() {
    }

    /**
     * @param name
     */
    public AbstractToggleAction(String name) {
        super(name);
    }
    
    /**
     * @param name
     * @param icon
     */
    public AbstractToggleAction(String name, Icon icon) {
        super(name, icon);
    }

    @Override
    public final void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof AbstractButton) {
            AbstractButton ab = (AbstractButton)e.getSource();
            setSelected(ab.isSelected());
            toggleActionPerformed(e);
        }
    }

    /**
     * Called to perform the action, after the state has been toggled
     * 
     * @param e
     */
    public abstract void toggleActionPerformed(ActionEvent e);

    /**
     * Switches the toggle action on or off
     * 
     * @param selected
     */
    public void setSelected(boolean selected) {
        putValue(SELECTED_KEY, selected);        
    }

    /**
     * @return  whether the toggle action switched on
     */
    public boolean isSelected() {
        return (Boolean)getValue(SELECTED_KEY);
    }

}
