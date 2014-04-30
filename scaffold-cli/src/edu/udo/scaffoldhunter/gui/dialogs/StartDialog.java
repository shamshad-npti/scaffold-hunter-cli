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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.udo.scaffoldhunter.ScaffoldHunter;
import edu.udo.scaffoldhunter.data.ConnectionData;
import edu.udo.scaffoldhunter.data.ConnectionDataManager;
import edu.udo.scaffoldhunter.gui.GUIController;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.I18n.Language;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * A dialog that is used to login and to manage connections.
 * 
 * @author Thorsten Flügel
 * @author Thomas Schmitz
 */
public class StartDialog extends JDialog {

    private final GUIController ctrl;

    private Result result = Result.NONE;

    /** Contains the names of the connections */
    private JComboBox connectionsCombo;

    private JTextField username;

    private JTextField password;

    private JComboBox language;
    
    private JCheckBox savePassword;

    private JCheckBox lastSession;

    /** Starts the login process. */
    private Action okAction;
    /** Quits the program. */
    private Action quitAction;

    private Action createUserAction;

    private ConnectionDataManager dataManager;

    private ConnectionData data = null;

    private boolean passwordChanged = false;

    /**
     * @return the passwordChanged
     */
    public boolean isPasswordChanged() {
        return passwordChanged;
    }

    /**
     * @return the dialog result
     */
    public Result getResult() {
        return result;
    }

    /**
     * @return the data
     */
    public ConnectionData getData() {
        return data;
    }

    /**
     * @return the username
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
     * @return the password, that was typed in the password field
     */
    public String getNewPassword() {
        return password.getText();
    }

    /**
     * @param password
     *            the password of the password field
     */
    public void setNewPassword(String password) {
        this.password.setText(password);
        passwordChanged = true;
    }

    /**
     * @return the password, that was saved in data manager
     */
    public byte[] getSavedPassword() {
        return dataManager.getPassword();
    }
    
    /**
     * @return the savePassword
     */
    public boolean getSavePassword() {
        return savePassword.isSelected();
    }

    /**
     * @return the useLastSession
     */
    public boolean getUseLastSession() {
        return lastSession.isSelected();
    }

    /**
     * Constructs the dialog to log into the program.
     * 
     * @param ctrl
     *            the GUI controller
     * @param dataManager
     *            the data manager
     */
    public StartDialog(GUIController ctrl, ConnectionDataManager dataManager) {
        super(null, ModalityType.TOOLKIT_MODAL);

        setIconImage(Resources.getBufferedImage("images/scaffoldhunter-icon.png"));

        this.ctrl = ctrl;
        this.dataManager = dataManager;

        initActions();
        initGUI();
        
        if (username.getText().isEmpty())
            username.requestFocus();
        else
            password.requestFocus();
    }

    /**
     * Initializes the actions of this dialog.
     */
    private void initActions() {
        okAction = new OKAction(this);
        quitAction = new CancelAction();
        createUserAction = new CreateUserAction(this);

        okAction.setEnabled(false);
    }

    /**
     * Initializes the GUI of this dialog.
     */
    private void initGUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(I18n.get("Start.Title"));
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(getLoginPanel(), BorderLayout.CENTER);

        pack();

        setLocationRelativeTo(null);
    }

    /**
     * Creates a panel that hold all components to log into the program.
     * 
     * @return The <code>JPanel</code> to log into the program.
     */
    private JPanel getLoginPanel() {
        JPanel loginPanel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);

        JLabel logo = new JLabel(Resources.getImageIcon("images/scaffoldhunter-logo.png"));
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        loginPanel.add(logo, c);

        JLabel nameLabel = new JLabel(I18n.get("Start.Login") + ":");
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        loginPanel.add(nameLabel, c);

        c.weightx = 1.0;

        // insert all profiles
        connectionsCombo = new JComboBox(dataManager.getNamesModel());
        connectionsCombo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateButtonState();
                data = dataManager.getConnection((String) connectionsCombo.getSelectedItem());
            }
        });
        data = dataManager.getConnection((String) connectionsCombo.getSelectedItem());
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 0.5;
        loginPanel.add(connectionsCombo, c);

        JLabel usernameLabel = new JLabel(I18n.get("Start.Username") + ":");
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        loginPanel.add(usernameLabel, c);

        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        username = new JTextField(dataManager.getUsername());
        username.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                dataManager.setUsername(username.getText());
                updateButtonState();
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                dataManager.setUsername(username.getText());
                updateButtonState();
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                dataManager.setUsername(username.getText());
                updateButtonState();
            }
        });
        username.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent e) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void focusGained(FocusEvent e) {
                username.select(0, username.getText().length());
            }
        });
        loginPanel.add(username, c);

        JLabel passwordLabel = new JLabel(I18n.get("Start.Password") + ":");
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        loginPanel.add(passwordLabel, c);

        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        if (dataManager.getPassword() != null)
            password = new JPasswordField("12345");
        else {
            password = new JPasswordField();
            passwordChanged = true;
        }
        password.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent arg0) {
                passwordChanged = true;
            }

            @Override
            public void insertUpdate(DocumentEvent arg0) {
                passwordChanged = true;
            }

            @Override
            public void changedUpdate(DocumentEvent arg0) {
                passwordChanged = true;
            }
        });
        password.addFocusListener(new FocusListener() {
            
            @Override
            public void focusLost(FocusEvent arg0) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void focusGained(FocusEvent arg0) {
                password.select(0, password.getText().length());
            }
        });
        loginPanel.add(password, c);

        JLabel languageLabel = new JLabel(I18n.get("Start.Language") + ":");
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 1;
        loginPanel.add(languageLabel, c);

        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        language = new JComboBox(Language.values());
        language.setSelectedItem(dataManager.getLanguage());
        language.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Language selected = (Language) language.getSelectedItem();
                if (!selected.equals(dataManager.getLanguage())) {
                    /*
                     * When the language has been changed write it to the
                     * configuration and then restart the application.
                     */
                    dataManager.setLanguage(selected);
                    dataManager.save();

                    synchronized (ScaffoldHunter.class) {
                        ScaffoldHunter.setRestart(true);
                        ScaffoldHunter.class.notify();
                    }
                    dispose();
                }
            }
        });
        loginPanel.add(language, c);
        
        c.weightx = 0.0;
        c.weighty = 0.0;
        c.gridwidth = 2;
        
        savePassword = new JCheckBox(I18n.get("Start.SavePassword") );
        savePassword.setSelected(dataManager.getSavePassword());
        savePassword.addChangeListener(new ChangeListener() {
            
            @Override
            public void stateChanged(ChangeEvent e) {
                dataManager.setSavePassword(savePassword.isSelected());
                validate();
            }
        });
        loginPanel.add(savePassword, c);
        
        c.weightx = 1.0;
        c.gridwidth = GridBagConstraints.REMAINDER;

        lastSession = new JCheckBox(I18n.get("Start.LastSession"));
        lastSession.setSelected(dataManager.getUseLastSession());
        lastSession.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                dataManager.setUseLastSession(lastSession.isSelected());
                validate();
            }
        });
        loginPanel.add(lastSession, c);

        /*
         * c.gridwidth = 1; loginPanel.add(new JLabel(), c);
         */

        c.gridwidth = GridBagConstraints.REMAINDER;
        loginPanel.add(getButtonPanel(), c);

        updateButtonState();

        return loginPanel;
    }

    /**
     * Creates the panel that holds the buttons to manage profiles, to login and
     * to quit the application.
     * 
     * @return The <code>JPanel</code> with all buttons.
     */
    private JPanel getButtonPanel() {
        JButton loginProfile = new JButton(new ConnectionsAction());
        JButton loginUser = new JButton(createUserAction);
        JButton loginOK = new JButton(okAction);
        JButton loginCancel = new JButton(quitAction);

        getRootPane().setDefaultButton(loginOK);

        JPanel okCancelPanel = ButtonBarFactory.buildOKCancelBar(loginOK, loginCancel);

        JPanel profileUserPanel = ButtonBarFactory.buildLeftAlignedBar(new JButton[] { loginProfile, loginUser }, true);
        profileUserPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        buttonPanel.add(profileUserPanel);
        buttonPanel.add(okCancelPanel);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

        return buttonPanel;
    }

    private void updateButtonState() {
        String connection = (String) connectionsCombo.getSelectedItem();
        boolean enable = connection != null && connection.length() > 0;
        okAction.setEnabled(enable && !username.getText().isEmpty());
     }

    private class ConnectionsAction extends AbstractAction {
        public ConnectionsAction() {
            super(I18n.get("Start.Profile"));
            putValue(Action.SMALL_ICON, Resources.getIcon("server.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Start.Profile.Description"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ConnectionDialog connectionDialog =  new ConnectionDialog(I18n.get("Profile.LoginTitle"),
                    StartDialog.this, dataManager, data);
            connectionDialog.setVisible(true);

        }
    }

    private class CreateUserAction extends AbstractAction {
        private StartDialog dialog;

        public CreateUserAction(StartDialog dialog) {
            super(I18n.get("Start.CreateUser"));
            putValue(Action.SMALL_ICON, Resources.getIcon("person.png"));
            this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ctrl.createNewProfile(dialog, dialog.dataManager);
        }
    }

    private class OKAction extends AbstractAction {
        // private StartDialog dialog;

        public OKAction(StartDialog dialog) {
            super(I18n.get("Start.OK"));
            putValue(Action.SMALL_ICON, Resources.getIcon("apply.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Start.OK"));
            // this.dialog = dialog;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // saved every time the data changes, but since this might fail,
            // try it once more
            dataManager.save();

            data = dataManager.getConnection((String) connectionsCombo.getSelectedItem());
            result = Result.OK;
            dispose();
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super(I18n.get("Start.Cancel"));
            putValue(Action.SMALL_ICON, Resources.getIcon("exit.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Start.Cancel.Description"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result = Result.QUIT;
            dispose();
        }
    }
    
    /**
     * @param password The encrypted password to save
     */
    public void saveNewPassword(byte[] password) {
        dataManager.setPassword(password);
        dataManager.save();
    }

    /**
     * The dialog result
     */
    public enum Result {
        /**
         * the user has neither clicked OK, nor cancel
         */
        NONE,
        /**
         * the user has clicked OK
         */
        OK,
        /**
         * the user has cliecked Quit
         */
        QUIT
    }
}
