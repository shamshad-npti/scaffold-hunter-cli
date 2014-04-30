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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.data.ConnectionDataManager;
import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Thomas Schmitz
 * 
 */
public class ProfileDialog extends JDialog {

    private JTextField username;
    private JTextField password;
    private JTextField password2;
    private Border defaultBorder;

    private boolean result = false;

    private Action okAction;

    private Action cancelAction;

    private ConnectionDataManager dataManager;

    /**
     * @return the dialog result
     */
    public boolean getResult() {
        return result;
    }

    /**
     * @return The username
     */
    public String getUsername() {
        return username.getText();
    }

    /**
     * @param username
     *            The username
     */
    public void setUsername(String username) {
        this.username.setText(username);
    }

    /**
     * @param editable
     *            If the username is editable
     */
    public void setUsernameEditable(boolean editable) {
        username.setEnabled(editable);
    }

    /**
     * @return The password
     */
    public String getPassword() {
        return password.getText();
    }

    /**
     * @param password
     *            The password
     */
    public void setPassword(String password) {
        this.password.setText(password);
        password2.setText(password);
    }

    /**
     * Constructs the Dialog to edit profile data
     */
    public ProfileDialog() {
        super();

        init();
    }

    /**
     * Constructs the Dialog to edit profile data with a ComboBox to choose the
     * database connection
     * 
     * @param dataManager
     */
    public ProfileDialog(ConnectionDataManager dataManager) {
        super();
        
        setIconImage(Resources.getBufferedImage("images/scaffoldhunter-icon.png"));

        this.dataManager = dataManager;

        init();
    }

    private void init() {
        initActions();
        initGUI();
        updateAllowOK();
        username.requestFocus();
    }

    private void initGUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(_("EditProfile.TitleNew"));
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(getProfilePanel(), BorderLayout.CENTER);

        pack();

        setLocationRelativeTo(null);
    }

    private JPanel getProfilePanel() {
        FormLayout layout = new FormLayout("l:d, 5dlu, f:d:grow", // 3 columns
                "f:d:grow, 5dlu, f:d, 5dlu, f:d, 5dlu, f:d, 5dlu, f:d"); // 9
        // rows
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();

        if (dataManager != null) {
            JLabel connectionLabel = new JLabel(_("Start.Login") + ":");
            pb.add(connectionLabel, cc.xy(1, 1));

            JComboBox connectionCombo = new JComboBox(dataManager.getNamesModel());
            pb.add(connectionCombo, cc.xy(3, 1));
        }

        JLabel usernameLabel = new JLabel(_("EditProfile.Username"));
        pb.add(usernameLabel, cc.xy(1, 3));

        username = new JTextField();
        Dimension size = username.getPreferredSize();
        size.width = 180;
        username.setPreferredSize(size);
        username.getDocument().addDocumentListener(new UpdateOk());
        defaultBorder = username.getBorder();
        pb.add(username, cc.xy(3, 3));

        JLabel passwordLabel = new JLabel(_("EditProfile.Password"));

        pb.add(passwordLabel, cc.xy(1, 5));

        password = new JPasswordField();
        password.getDocument().addDocumentListener(new UpdateOk());
        pb.add(password, cc.xy(3, 5));

        JLabel password2Label = new JLabel(_("EditProfile.Password2"));
        pb.add(password2Label, cc.xy(1, 7));

        password2 = new JPasswordField();
        password2.getDocument().addDocumentListener(new UpdateOk());
        pb.add(password2, cc.xy(3, 7));

        JButton okButton = new JButton(okAction);
        getRootPane().setDefaultButton(okButton);

        JButton cancelButton = new JButton(cancelAction);

        pb.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), cc.xyw(1, 9, 3));

        return pb.getPanel();
    }

    private void initActions() {
        okAction = new OKAction();
        cancelAction = new CancelAction();
    }

    private void updateAllowOK() {
        boolean usernameOK = !username.getText().isEmpty();
        boolean passwordsOK = password.getText().equals(password2.getText());
        okAction.setEnabled(usernameOK && passwordsOK
                && (dataManager == null || dataManager.getNamesModel().getSelectedItem() != null));

        if (usernameOK) {
            username.setBorder(defaultBorder);
        } else {
            username.setBorder(BorderFactory.createLineBorder(Color.RED));
        }

        if (passwordsOK) {
            password.setBorder(defaultBorder);
            password2.setBorder(defaultBorder);
        } else {
            password.setBorder(BorderFactory.createLineBorder(Color.RED));
            password2.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
    }

    private class OKAction extends AbstractAction {
        public OKAction() {
            super(_("EditProfile.OK"));
            putValue(Action.SMALL_ICON, Resources.getIcon("apply.png"));
            putValue(Action.SHORT_DESCRIPTION, _("EditProfile.OK"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result = true;
            dispose();
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super(_("EditProfile.Cancel"));
            putValue(Action.SMALL_ICON, Resources.getIcon("cancel.png"));
            putValue(Action.SHORT_DESCRIPTION, _("EditProfile.OK"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result = false;
            dispose();
        }
    }

    private class UpdateOk implements DocumentListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#changedUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public void changedUpdate(DocumentEvent e) {
            updateAllowOK();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#insertUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateAllowOK();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.event.DocumentListener#removeUpdate(javax.swing.event
         * .DocumentEvent)
         */
        @Override
        public void removeUpdate(DocumentEvent e) {
            updateAllowOK();
        }
    }
}
