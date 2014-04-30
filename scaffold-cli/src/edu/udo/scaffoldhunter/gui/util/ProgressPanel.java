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

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * @author Thorsten Flügel
 * 
 * @param <T>
 *            the worker's return type
 */
public class ProgressPanel<T> extends JPanel implements ProgressListener<T> {
    private JProgressBar progressBar;
    private JLabel textLabel;

    private static final double PROGRESSBAR_WIDTH_FACTOR = 1.2;
    private static final int PROGRESSBAR_MINIMUM_WIDTH = 100;

    /**
     * @param text
     *            The text that will be displayed above the progress bar
     */
    public ProgressPanel(String text) {
        super(new FormLayout("fill:pref:grow", "p, 5dlu, p"));

        textLabel = new JLabel(text);
        textLabel.setHorizontalAlignment(SwingConstants.CENTER);
        textLabel.addPropertyChangeListener(new PreferredSizeListener());

        add(textLabel, CC.xy(1, 1));

        progressBar = new JProgressBar();
        Dimension size = progressBar.getPreferredSize();
        size.width = (int)(textLabel.getPreferredSize().width * PROGRESSBAR_WIDTH_FACTOR);
        progressBar.setPreferredSize(size);
        size = new Dimension(size);
        size.width = PROGRESSBAR_MINIMUM_WIDTH;
        progressBar.setMinimumSize(size);
        progressBar.setIndeterminate(true);

        add(progressBar, CC.xy(1, 3));

        setBorder(new EmptyBorder(10, 10, 10, 10));
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressValue(int)
     */
    @Override
    public void setProgressValue(int progress) {
        progressBar.setValue(progress);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressBounds(int,
     * int)
     */
    @Override
    public void setProgressBounds(int min, int max) {
        progressBar.setMinimum(min);
        progressBar.setMaximum(max);
        progressBar.setValue(min);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressIndeterminate
     * (boolean)
     */
    @Override
    public void setProgressIndeterminate(boolean indeterminate) {
        progressBar.setIndeterminate(indeterminate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.gui.util.ProgressListener#finished()
     */
    @Override
    public void finished(T result, boolean cancelled) {
        progressBar.setValue(progressBar.getMaximum());
    }

    /**
     * Sets the text shown by this panel's label
     * 
     * @param text
     *            the text to be shown
     */
    public void setLabelText(String text) {
        textLabel.setText(text);
    }
    
    private class PreferredSizeListener implements PropertyChangeListener {
        /* (non-Javadoc)
         * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
         */
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals("preferredSize")) {
                Dimension d = progressBar.getPreferredSize();
                d.width = (int)(((Dimension)evt.getNewValue()).width * PROGRESSBAR_WIDTH_FACTOR);
                progressBar.setPreferredSize(d);
            }
        }
    }
}
