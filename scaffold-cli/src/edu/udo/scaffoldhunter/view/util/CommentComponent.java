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

package edu.udo.scaffoldhunter.view.util;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.text.DateFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.VoidUnaryDBFunction;
import edu.udo.scaffoldhunter.model.db.Comment;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * The {@link CommentComponent} is a {@link JPanel} which allows to add, delete
 * and modify the {@link Comment}s for a {@link Structure}.
 * 
 * @author Till Schäfer
 * 
 */
public class CommentComponent extends JPanel {
    private JPanel comments = new JPanel();
    private AddAction addAction = new AddAction();
    private JComboBox<TextAreaSaveDelete> addNewComboBox = new JComboBox<TextAreaSaveDelete>();
    private JButton addButton;
    private DbManager db;
    private Tree tree;
    private Profile profile;
    private Structure structure;
    private static Logger logger = LoggerFactory.getLogger(CommentComponent.class);

    /**
     * Constructor
     * 
     * @param db
     *            the DB manager
     * @param structure
     *            the {@link Structure} for whitch the {@link Comment}s should
     *            be modified
     * @param profile
     *            The profile of the current user. Needed for the private
     *            {@link Comment}s
     */
    public CommentComponent(DbManager db, Structure structure, Profile profile) {
        this.db = db;
        this.structure = structure;
        this.profile = profile;
        tree = profile.getCurrentSession().getTree();

        this.setBorder(BorderFactory.createTitledBorder(_("CommentComponent.Title")));
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        comments.setLayout(new BoxLayout(comments, BoxLayout.Y_AXIS));

        // prepare TextAreaSaveDelete components and add them to the combobox or
        // comments panel
        TextAreaSaveDelete[] textAreas = { new TextAreaSaveDelete(true, true), new TextAreaSaveDelete(true, false),
                new TextAreaSaveDelete(false, true), new TextAreaSaveDelete(false, false) };
        for (TextAreaSaveDelete textAreaSaveDelete : textAreas) {
            if (textAreaSaveDelete.isInDatabase()) {
                comments.add(textAreaSaveDelete);
            } else {
                addNewComboBox.addItem(textAreaSaveDelete);
            }
        }

        // The Panel with JComboBox (available TextAreaSaveDelete Components)
        // and ADD JButton
        JPanel addPanel = new JPanel();
        addPanel.add(addNewComboBox);
        this.addButton = new JButton(addAction);
        addPanel.add(addButton);

        // add components to main panel
        add(comments);
        add(addPanel);

        // disable addButton if there is no comment to add
        if (addNewComboBox.getItemCount() == 0) {
            addButton.setEnabled(false);
        }
    }

    /**
     * Removes the {@link CommentComponent.TextAreaSaveDelete} from the comments {@link JPanel}
     * and adds it to the {@link JComboBox}
     * 
     * @param component
     *            the component which should be removed
     */
    public void removeComment(TextAreaSaveDelete component) {
        comments.remove(component);
        addNewComboBox.addItem(component);
        addButton.setEnabled(true);
        invalidate();
    }

    /**
     * Adds the selected {@link CommentComponent.TextAreaSaveDelete} to the comments
     * {@link JPanel} and removes it from the {@link JComboBox}
     */
    public void addComponent() {
        TextAreaSaveDelete current = (TextAreaSaveDelete) addNewComboBox.getSelectedItem();
        comments.add(current);
        addNewComboBox.removeItem(current);
        // disable addButton if there is no comment to add
        if (addNewComboBox.getItemCount() == 0) {
            addButton.setEnabled(false);
        }
        invalidate();
        current.saveComment();
    }

    /**
     * A {@link JTextArea} with two {@link JButton}s which represents a
     * {@link Comment} and the functions Save and Delete
     * 
     * @author Till Schäfer
     * 
     */
    private class TextAreaSaveDelete extends JPanel {
        boolean priv;
        boolean global;
        private String title;
        private boolean inDatabase = false;
        private JTextArea textArea = new JTextArea(3, 1);
        private Comment comment = null;
        private JLabel creationInfo = new JLabel("ERROR");
        private JButton saveButton = new JButton(new SaveAction(this));
        private JButton deleteButton = new JButton(new DeleteAction(this));

        public TextAreaSaveDelete(boolean priv, boolean global) {
            this.priv = priv;
            this.global = global;
            title = (priv ? _("CommentComponent.Private") : _("CommentComponent.Public")) + " & "
                    + (global ? _("CommentComponent.Global") : _("CommentComponent.Local"));

            textArea.setLineWrap(true);
            textArea.getDocument().addDocumentListener(new CommentChangedListener());
            saveButton.setEnabled(false);

            setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(getForeground()), title));

            FormLayout layout = new FormLayout("p:g, p", "p, p, p");
            PanelBuilder panelBuilder = new PanelBuilder(layout, this);
            setLayout(layout);

            panelBuilder.add(new JScrollPane(textArea), CC.rchw(1, 1, 2, 1));
            panelBuilder.add(creationInfo, CC.rcw(3, 1, 2));
            panelBuilder.add(saveButton, CC.rc(1, 2));
            panelBuilder.add(deleteButton, CC.rc(2, 2));

            comment = DBExceptionHandler.callDBManager(db, new GetCommentExceptionHandler(priv, global ? null : tree,
                    profile, structure), true);

            if (comment == null) {
                inDatabase = false;
            } else {
                inDatabase = true;
                textArea.setText(comment.getComment());
                updateCreationInfo(comment);
            }
        }

        private void updateCreationInfo(Comment comment) {
            String modifiedBy = "ERROR";
            try {
                modifiedBy = db.getCreationUserName(comment);
            } catch (DatabaseException e) {
                Writer stacktrace = new StringWriter();
                e.printStackTrace(new PrintWriter(stacktrace));
                logger.error(e.getMessage() + stacktrace.toString());
            }

            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
            creationInfo.setText(_("CommentComponent.ModifiedBy") + ": " + modifiedBy + ", "
                    + df.format(comment.getModificationDate()));
        }

        /**
         * @return whether the {@link Comment} for this
         *         {@link CommentComponent.TextAreaSaveDelete} is present in Database or not
         */
        public boolean isInDatabase() {
            return inDatabase;
        }

        @Override
        public String toString() {
            return title;
        }

        /**
         * Saves the this {@link Comment} in the Database
         */
        public void saveComment() {
            comment = DBExceptionHandler.callDBManager(db, new SaveCommentExceptionHandler(textArea.getText(), priv,
                    global ? null : tree, profile, structure), true);

            updateCreationInfo(comment);
            saveButton.setEnabled(false);
        }

        /**
         * Deletes the this {@link Comment} from the Database
         */
        public void deleteComment() {
            /*
             * Hibernate does not accept null properties if not-null=true even
             * for deletion of objects
             */
            comment.setModifiedBy(profile);

            DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Comment>(comment) {
                @Override
                public void call(Comment arg) throws DatabaseException {
                    db.delete(comment);
                    comment = null;
                }
            });
        }

        private class CommentChangedListener implements DocumentListener {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }

            public void changed() {
                saveButton.setEnabled(true);
            }
        }
    }

    /**
     * Action for pressing the + Button
     * 
     * @author Till Schäfer
     * 
     */
    private class AddAction extends AbstractAction {
        public AddAction() {
            super("+");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addComponent();
        }
    }

    /**
     * Action for pressing a delete button
     * 
     * @author Till Schäfer
     * 
     */
    private class DeleteAction extends AbstractAction {
        TextAreaSaveDelete source;

        public DeleteAction(TextAreaSaveDelete source) {
            super("", Resources.getIcon("delete.png"));

            Preconditions.checkNotNull(source);
            this.source = source;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source.deleteComment();
            removeComment(source);
        }
    }

    /**
     * Action for pressing a save button
     * 
     * @author Till Schäfer
     * 
     */
    private static class SaveAction extends AbstractAction {
        TextAreaSaveDelete source;

        public SaveAction(TextAreaSaveDelete source) {
            super("", Resources.getIcon("save.png"));

            Preconditions.checkNotNull(source);
            this.source = source;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            source.saveComment();
        }
    }

    private class SaveCommentExceptionHandler implements DBFunction<Comment> {
        String text;
        boolean priv;
        Tree tree;
        Profile profile;
        Structure structure;

        SaveCommentExceptionHandler(String text, boolean priv, Tree tree, Profile profile, Structure structure) {
            this.priv = priv;
            this.profile = profile;
            this.structure = structure;
            this.text = text;
            this.tree = tree;
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.udo.scaffoldhunter.gui.util.DBFunction#call()
         */
        @Override
        public Comment call() throws DatabaseException {
            return db.createOrUpdateComment(text, priv, tree, profile, structure);
        }

    }

    private class GetCommentExceptionHandler implements DBFunction<Comment> {
        boolean priv;
        Tree tree;
        Profile profile;
        Structure structure;

        GetCommentExceptionHandler(boolean priv, Tree tree, Profile profile, Structure structure) {
            this.priv = priv;
            this.profile = profile;
            this.structure = structure;
            this.tree = tree;
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.udo.scaffoldhunter.gui.util.DBFunction#call()
         */
        @Override
        public Comment call() throws DatabaseException {
            return db.getComment(priv, tree, profile, structure);
        }

    }
}
