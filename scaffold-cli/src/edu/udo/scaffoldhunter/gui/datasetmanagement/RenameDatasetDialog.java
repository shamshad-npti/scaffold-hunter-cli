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

import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.model.db.Dataset;

/**
 * @author Philipp Lewe
 * 
 */
public class RenameDatasetDialog extends JDialog {
    /**
     * The user hit cancel
     */
    public static int CANCEL = 0;
    /**
     * The user hit ok
     */
    public static int OK = 1;

    private int result = CANCEL;

    private Dataset dataset;
    private Collection<Dataset> existingDatasets;

    private JButton okButton;
    private JButton cancelButton;

    private JTextField title;
    private JTextArea comment;
    private JScrollPane commentScrollPane;

    private Border defaultTitleBorder;
    private Border defaultCommentBorder;

    /**
     * Creates new dialog for renaming of datasets
     * 
     * @param owner
     *            the owner of this dialog
     * @param dataset
     *            the dataset that should be modified
     * @param existingDatasets
     *            all existing datasets (incl. the edited one)
     */
    public RenameDatasetDialog(Window owner, Dataset dataset, Collection<Dataset> existingDatasets) {
        super(owner);
        this.dataset = dataset;
        this.existingDatasets = existingDatasets;
        initGUI();
        pack();
        setLocationRelativeTo(getOwner());
    }

    private void initGUI() {
        setTitle(_("RenameDataset"));
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        okButton = StandardButtonFactory.createOKButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert check();
                result = OK;
                dispose();
            }
        });

        cancelButton = StandardButtonFactory.createCancelButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = CANCEL;
                dispose();
            }
        });

        title = new JTextField(dataset.getTitle());
        title.setColumns(30);
        title.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }
        });

        defaultTitleBorder = title.getBorder();
        comment = new JTextArea(dataset.getComment());
        comment.setColumns(30);
        comment.setRows(5);
        comment.setWrapStyleWord(false);
        comment.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                check();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                check();
            }
        });
        defaultCommentBorder = comment.getBorder();

        commentScrollPane = new JScrollPane(comment);

        FormLayout layout = new FormLayout("p, 2dlu, p", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout, (JPanel) getContentPane());
        builder.setDefaultDialogBorder();

        builder.append(_("DataImport.DatasetName"), title, true);
        builder.append(_("DataImport.DatasetDescription"), commentScrollPane, true);

        builder.append(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), 3);
    }

    private boolean check() {
        notifyUserAboutValidStatus(validTitle(), validComment());
        return (validTitle() && validComment());
    }

    private boolean validTitle() {
        String titleString = title.getText().trim();

        if (titleString.isEmpty()) {
            return false;
        }

        if (titleString.length() > 120) {
            return false;
        }

        // check for collision with existing dataset names
        for (Dataset set : existingDatasets) {
            if (set.getTitle().toLowerCase().equals(titleString.toLowerCase()) && (set != dataset)) {
                return false;
            }
        }

        return true;
    }

    private boolean validComment() {
        String commentString = comment.getText().trim();

        if (commentString.length() > 500) {
            return false;
        }

        return true;
    }

    private void notifyUserAboutValidStatus(boolean validTitle, boolean validComment) {
        if (validTitle) {
            title.setBorder(defaultTitleBorder);
        } else {
            title.setBorder(BorderFactory.createLineBorder(Color.RED));
        }

        if (validComment) {
            comment.setBorder(defaultCommentBorder);
        } else {
            comment.setBorder(BorderFactory.createLineBorder(Color.RED));
        }

        if (validTitle && validComment) {
            okButton.setEnabled(true);
        } else {
            okButton.setEnabled(false);
        }
    }

    /**
     * @return the result status of the dialog
     */
    public int getResult() {
        return result;
    }

    /**
     * @return the new dataset title
     */
    public String getDatasetTitle() {
        return title.getText().trim();
    }

    /**
     * @return the new dataset comment
     */
    public String getDatasetComment() {
        return comment.getText().trim();
    }
}
