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

package edu.udo.scaffoldhunter.gui.datasetmanagement;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.ProgressPanel;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldTreeGenerator;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * @author Philipp Lewe
 * 
 */
public class TreegenProgressDialog extends JDialog implements ProgressListener<Tree>, PropertyChangeListener {
    ActionListener cancelAction;
    JButton cancelCloseButton;
    JScrollPane scrollpane;
    JTextArea messages;
    boolean shouldClose = false;
    

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
     * @param cancelAction
     *            an {@link ActionListener} that will be invoked when the user
     *            activates the cancel button
     */
    public TreegenProgressDialog(JFrame parent, String title, ProgressPanel<?> progressPanel, ModalityType modality,
            ActionListener cancelAction) {
        super(parent, title, modality);
        initGUI(parent, progressPanel);
        this.cancelAction = cancelAction;
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
     * @param cancelAction
     *            an {@link ActionListener} that will be invoked when the user
     *            activates the cancel button
     */
    public TreegenProgressDialog(JDialog parent, String title, ProgressPanel<?> progressPanel, ModalityType modality,
            ActionListener cancelAction) {
        super(parent, title, modality);
        initGUI(parent, progressPanel);
        this.cancelAction = cancelAction;
    }

    /**
     * Initializes the GUI of this dialog.
     */
    private void initGUI(Component parent, ProgressPanel<?> progressPanel) {
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setResizable(false);
        JPanel contenPanel = (JPanel) getContentPane();
        contenPanel.setBorder(null);
        contenPanel.setBorder(Borders.DIALOG_BORDER);
        getContentPane().setLayout(new FormLayout("p", "f:p:g, 5dlu, f:p:g, p"));
        getContentPane().add(progressPanel, CC.rc(1, 1));
        
        messages = new JTextArea();
        messages.setEditable(false);
        messages.setRows(10);
        messages.setColumns(25);
        
        scrollpane = new JScrollPane(messages);
        getContentPane().add(scrollpane, CC.rc(3, 1));

        cancelCloseButton = new JButton(new CancelCloseAction());
        getContentPane().add(ButtonBarFactory.buildCenteredBar(cancelCloseButton), CC.rc(4, 1));

        pack();

        setLocationRelativeTo(parent);
    }

    private void switchToClose() {
        shouldClose = true;
        cancelCloseButton.setText(_("Button.Close"));
    }

    private class CancelCloseAction extends AbstractAction {

        public CancelCloseAction() {
            putValues(_("Button.Cancel"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            if (shouldClose) {
                dispose();
            } else {
                cancelAction.actionPerformed(e);
            }
        }
    }

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
    public void finished(Tree result, boolean cancelled) {
        if (!cancelled) {
            switchToClose();
            cancelCloseButton.setEnabled(true);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == ScaffoldTreeGenerator.PROPERTY_PROGRESS) {
            ScaffoldTreeGenerator.Progress progress = (ScaffoldTreeGenerator.Progress) evt.getNewValue();

            StringBuffer buff = new StringBuffer();

            for (String string : progress.getErrorList()) {
                buff.append(string);
                buff.append("\n");
            }

            this.messages.setText(buff.toString());

            if (progress.isSaving()) {
                cancelCloseButton.setEnabled(false);
            }
        }
    }

}
