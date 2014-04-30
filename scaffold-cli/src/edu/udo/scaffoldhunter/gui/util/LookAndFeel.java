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

import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.jgoodies.forms.util.LayoutStyle;

import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Dominic Sacré
 */
public class LookAndFeel {

    /**
     * Sets the application's look and feel.
     */
    public static void configureLookAndFeel() {
        configureNativeLookAndFeel();
        configureTaskPaneUI();
        configureOptionPaneUI();
        configureTableUI();

        // don't close tool tips automatically after a timeout
        ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
    }

    /**
     * Tries to set the closest thing we can get to a native look and feel.
     */
    private static void configureNativeLookAndFeel() {
        try {
            String nativeLF = UIManager.getSystemLookAndFeelClassName();
            
            // on *nix desktops other than GNOME, Java reports its default
            // MetalLookAndFeel as the system look and feel. In most cases, the
            // user's GTK theme will look better and more 'native'.
            if (nativeLF.equals("javax.swing.plaf.metal.MetalLookAndFeel")) {
                nativeLF = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
            }

            UIManager.setLookAndFeel(nativeLF);
        } catch (Exception e) {
            // things will probably look ugly, but continue anyway
        }

        if (isGTKLookAndFeel()) {
            // fix the layout style for JGoodies components.
            // JGoodies itself only recognizes Mac and Windows styles, and
            // defaults to Windows
            LayoutStyle.setCurrent(GTKLayoutStyle.INSTANCE);
        }
    }

    /**
     * Makes sure the l2fprod task pane uses system colors, but otherwise
     * utilizes the same plain look and feel on all platforms
     */
    private static void configureTaskPaneUI() {
        UIManager.put("TaskPaneUI", "com.l2fprod.common.swing.plaf.basic.BasicTaskPaneUI");
        UIManager.put("TaskPaneGroupUI", "com.l2fprod.common.swing.plaf.misc.GlossyTaskPaneGroupUI");

        UIManager.put("TaskPane.useGradient", false);
        UIManager.put("TaskPane.background", UIManager.get("Panel.background"));

        UIManager.put("TaskPaneGroup.background", UIManager.get("Panel.background"));
        UIManager.put("TaskPaneGroup.titleForeground", UIManager.get("Button.foreground"));
        UIManager.put("TaskPaneGroup.titleOver", UIManager.get("Button.foreground"));
        UIManager.put("TaskPaneGroup.titleBackgroundGradientStart", UIManager.get("Button.background"));
        UIManager.put("TaskPaneGroup.titleBackgroundGradientEnd", UIManager.get("Button.shadow"));
    }

    /**
     * Brings the look and feel of the MessageBox, err, JOptionPane into line
     * with the rest of Scaffold Hunter.
     */
    private static void configureOptionPaneUI() {
        if (isGTKLookAndFeel()) {
            UIManager.put("OptionPaneUI", "javax.swing.plaf.basic.BasicOptionPaneUI");
            UIManager.put("OptionPane.buttonMinimumWidth", 80);
        }

        UIManager.put("OptionPane.yesIcon", null);
        UIManager.put("OptionPane.noIcon", null);
        UIManager.put("OptionPane.okIcon", null);
        UIManager.put("OptionPane.cancelIcon", null);

        UIManager.put("OptionPane.yesButtonMnemonic", "");
        UIManager.put("OptionPane.noButtonMnemonic", "");
        UIManager.put("OptionPane.okButtonMnemonic", "");
        UIManager.put("OptionPane.cancelButtonMnemonic", "");

        UIManager.put("OptionPane.errorIcon", Resources.getImageIcon("icons/misc/dialog-error.png"));
        UIManager.put("OptionPane.informationIcon", Resources.getImageIcon("icons/misc/dialog-info.png"));
        UIManager.put("OptionPane.warningIcon", Resources.getImageIcon("icons/misc/dialog-warning.png"));
        UIManager.put("OptionPane.questionIcon", Resources.getImageIcon("icons/misc/dialog-question.png"));
    }

    private static void configureTableUI() {
        if (isGTKLookAndFeel()) {
            UIManager.put("TableUI", "javax.swing.plaf.basic.BasicTableUI");
    
            UIManager.put("Table.background", UIManager.getColor("text"));
            UIManager.put("Table.foreground", UIManager.getColor("textText"));
        }
    }

    /**
     * @return  true if we're using the GTK look and feel
     */
    public static boolean isGTKLookAndFeel() {
        return UIManager.getLookAndFeel().getName().equals("GTK look and feel");
    }

}
