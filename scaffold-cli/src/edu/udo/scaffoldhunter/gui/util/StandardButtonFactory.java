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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;

import edu.udo.scaffoldhunter.util.Resources;

/**
 * A factory class that provides static methods to create standard dialog
 * buttons.
 * 
 * @author Dominic Sacré
 */
public class StandardButtonFactory {

    /**
     * Creates a standard dialog OK button.
     * 
     * @return  the new button
     */
    public static JButton createOKButton() {
        return createOKButton(null, false);
    }

    /**
     * Creates a standard dialog OK button.
     * 
     * @param listener 
     * 
     * @return  the new button
     */
    public static JButton createOKButton(ActionListener listener) {
        return createOKButton(listener, false);
    }

    /**
     * Creates a standard dialog OK button.
     * 
     * @param listener 
     * @param withIcon 
     * 
     * @return  the new button
     */
    public static JButton createOKButton(ActionListener listener, boolean withIcon) {
        Icon icon = withIcon ? Resources.getIcon("apply.png") : null;

        return createButton(_("Button.OK"), icon, listener);
    }


    /**
     * Creates a standard dialog apply button.
     * 
     * @return  the new button
     */
    public static JButton createApplyButton() {
        return createApplyButton(null, false);
    }

    /**
     * Creates a standard dialog apply button.
     * 
     * @param listener 
     * 
     * @return  the new button
     */
    public static JButton createApplyButton(ActionListener listener) {
        return createApplyButton(listener, false);
    }

    /**
     * Creates a standard dialog apply button.
     * 
     * @param listener 
     * @param withIcon 
     * 
     * @return  the new button
     */
    public static JButton createApplyButton(ActionListener listener, boolean withIcon) {
        Icon icon = withIcon ? Resources.getIcon("apply.png") : null;

        return createButton(_("Button.Apply"), icon, listener);
    }


    /**
     * Creates a standard dialog cancel button.
     * 
     * @return  the new button
     */
    public static JButton createCancelButton() {
        return createCancelButton(null, false);
    }

    /**
     * Creates a standard dialog cancel button.
     * 
     * @param listener 
     * 
     * @return  the new button
     */
    public static JButton createCancelButton(ActionListener listener) {
        return createCancelButton(listener, false);
    }

    /**
     * Creates a standard dialog cancel button.
     * 
     * @param listener 
     * @param withIcon 
     * 
     * @return  the new button
     */
    public static JButton createCancelButton(ActionListener listener, boolean withIcon) {
        Icon icon = withIcon ? Resources.getIcon("cancel.png") : null;   

        return createButton(_("Button.Cancel"), icon, listener);
    }


    /**
     * Creates a standard dialog close button.
     * 
     * @return  the new button
     */
    public static JButton createCloseButton() {
        return createCloseButton(null, false);
    }

    /**
     * Creates a standard dialog close button.
     * 
     * @param listener 
     * 
     * @return  the new button
     */
    public static JButton createCloseButton(ActionListener listener) {
        return createCloseButton(listener, false);
    }

    /**
     * Creates a standard dialog close button.
     * 
     * @param listener 
     * @param withIcon 
     * 
     * @return  the new button
     */
    public static JButton createCloseButton(ActionListener listener, boolean withIcon) {
        Icon icon = withIcon ? Resources.getIcon("close.png") : null;

        return createButton(_("Button.Close"), icon, listener);
    }

    /**
     * Creates a standard dialog help button.
     * 
     * @return  the new button
     */
    public static JButton createHelpButton() {
        return createHelpButton(null, false);
    }

    /**
     * Creates a standard dialog help button.
     * 
     * @param listener 
     * 
     * @return  the new button
     */
    public static JButton createHelpButton(ActionListener listener) {
        return createHelpButton(listener, false);
    }

    /**
     * Creates a standard dialog help button.
     * 
     * @param listener 
     * @param withIcon 
     * 
     * @return  the new button
     */
    public static JButton createHelpButton(ActionListener listener, boolean withIcon) {
        Icon icon = withIcon ? Resources.getIcon("dialog-information") : null;

        return createButton(_("Button.Help"), icon, listener); 
    }


    private static JButton createButton(String text, Icon icon, ActionListener listener) {
        JButton btn = new JButton(text);
        
        if (listener != null) {
            btn.addActionListener(listener);
        }

        if (icon != null) {
            btn.setIcon(icon);
        }

        return btn;
    }

}
