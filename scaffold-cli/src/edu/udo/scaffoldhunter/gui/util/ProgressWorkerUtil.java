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

package edu.udo.scaffoldhunter.gui.util;

import java.awt.Dialog.ModalityType;
import java.awt.Window;

import edu.udo.scaffoldhunter.gui.dialogs.ProgressDialog;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * @author Thorsten Flügel
 *
 */
public class ProgressWorkerUtil {

    /**
     * Executes a progress worker thread and shows a dialog displaying the
     * progress of the thread.
     * 
     * @param parent
     *            The parent dialog of the progress dialog
     * @param title
     *            The title of the progress dialog
     * @param message
     *            The message that will be displayed in the progress dialog
     * @param modality
     *            The modality type of the progress dialog
     * @param worker
     *            The worker thread that will be executed
     * @param <T>
     *            Worker return type
     * @param <V>
     *            Worker intermediate result type
     */
    public static <T, V> void executeWithProgressDialog(Window parent, String title, String message,
            ModalityType modality, final ProgressWorker<T, V> worker) {
        ProgressPanel<T> panel = new ProgressPanel<T>(message);
        final ProgressDialog dialog = new ProgressDialog(parent, title, panel, modality);
        class FinishedListener implements ProgressListener<T> {
            @Override
            public void setProgressValue(int progress) {
            }

            @Override
            public void setProgressBounds(int min, int max) {
            }

            @Override
            public void setProgressIndeterminate(boolean indeterminate) {
            }

            @Override
            public void finished(T result, boolean cancelled) {
                dialog.setVisible(false);
            }
        }

        FinishedListener finishedListener = new FinishedListener();
        worker.addProgressListener(finishedListener);
        worker.addProgressListener(panel);
        worker.execute();
        dialog.setVisible(true);
    }

}
