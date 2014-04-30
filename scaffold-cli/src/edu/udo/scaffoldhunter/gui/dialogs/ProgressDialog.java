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

package edu.udo.scaffoldhunter.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;

import edu.udo.scaffoldhunter.gui.util.ProgressPanel;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * This class constructs a dialog to show the progress during time-consuming
 * actions.
 * 
 * @author Thorsten Flügel
 */
public class ProgressDialog extends JDialog {
    /**
     * Constructs the dialog that holds the progressbar.
     * 
     * @param parent
     *            The {@link JFrame} from which the dialog is displayed.
     * @param title
     *            The dialog title
     * @param progressPanel
     *            The panel that displays some progress, will be inserted into
     *            this dialog.
     * @param modality
     *            The modality of the created progress dialog.
     */
    public ProgressDialog(JFrame parent, String title, ProgressPanel<?> progressPanel, ModalityType modality) {
        super(parent, title, modality);
        initGUI(parent, progressPanel);
    }

    /**
     * Constructs the dialog that holds the progressbar.
     * 
     * @param parent
     *            The {@link JDialog} from which the dialog is displayed.
     * @param title
     *            The dialog title
     * @param progressPanel
     *            The panel that displays some progress, will be inserted into
     *            this dialog.
     * @param modality
     *            The modality of the created progress dialog.
     */
    public ProgressDialog(Window parent, String title, ProgressPanel<?> progressPanel, ModalityType modality) {
        super(parent, title, modality);
        initGUI(parent, progressPanel);
    }

    /**
     * Initializes the GUI of this dialog.
     */
    private void initGUI(Component parent, ProgressPanel<?> progressPanel) {
        setIconImage(Resources.getBufferedImage("images/scaffoldhunter-icon.png"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(progressPanel, BorderLayout.CENTER);

        pack();

        setLocationRelativeTo(parent);
    }
}
