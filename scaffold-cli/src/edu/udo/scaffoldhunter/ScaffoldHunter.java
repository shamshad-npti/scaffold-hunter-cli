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

package edu.udo.scaffoldhunter;

import javax.swing.SwingUtilities;

import edu.udo.scaffoldhunter.data.ConnectionDataManager;
import edu.udo.scaffoldhunter.gui.GUIController;
import edu.udo.scaffoldhunter.gui.util.LookAndFeel;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Dominic Sacré
 * @author Henning Garus
 */
public class ScaffoldHunter {

    private static boolean restart = false;

    private static GUIController gui;

    /**
     * The main entry point for the Scaffold Hunter application.
     * 
     * @param args
     */
    public static void main(String args[]) {
        System.setProperty("java.util.logging.config.file", "logging.properties");
        final ConnectionDataManager dataManager = new ConnectionDataManager();

        for (;;) {
            I18n.initialize(dataManager.getLanguage());
            LookAndFeel.configureLookAndFeel();
            gui = new GUIController();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    gui.start(dataManager);
                }
            });
            /*
             * wait on the class object in case we need to restart from the
             * beginning. This is currently used to switch the language in the
             * login dialog.
             */
            synchronized (ScaffoldHunter.class) {
                while (!restart) {
                    try {
                        ScaffoldHunter.class.wait();
                    } catch (InterruptedException e) {
                        /*
                         * Not sure if this can be thrown at all here.
                         * Swallowing it should be safe, who else should be
                         * interested.
                         */
                    }
                }
                restart = false;
            }
        }
    }

    /**
     * Set to true, then notify the class object to restart the application.
     * 
     * @param restart
     * 
     */
    public static void setRestart(boolean restart) {
        ScaffoldHunter.restart = restart;
    }

}
