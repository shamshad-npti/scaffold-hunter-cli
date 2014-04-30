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
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.data.ConnectionData;
import edu.udo.scaffoldhunter.data.ConnectionDataManager;
import edu.udo.scaffoldhunter.data.ConnectionType;
import edu.udo.scaffoldhunter.gui.util.FileChooser;
import edu.udo.scaffoldhunter.gui.util.SelectAllOnFocus;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Thorsten Flügel
 * @author Henning Garus
 */
public class ConnectionDialog extends JDialog {
    
    private static final String NO_CONNECTION = "NO CONNECTION";

    private final ConnectionDataManager dataManager;
    
    private JList connectionList;
    /** <code>JTextField</code> to enter the profile name. */
    private JTextField nameText;
    
    private JComboBox typeDBCombo;

    /** Creates a new profile with the entered profile name. */
    private Action okAction;
    /** Cancels the process of creating a new profile. */
    private Action cancelAction;
    
    private final Action removeConnection = new RemoveConnectionAction();
    
    private final ConnectionSelectionListener connectionSelection = new ConnectionSelectionListener();
    private final ValidateListener validateListener = new ValidateListener();

    private final Map<ConnectionType, ProfilePanel> profilePanels = Maps.newEnumMap(ConnectionType.class);
    private JPanel profilePanel;
    
    private ConnectionData needsSaving = null;

    /**
     * Constructs the dialog to create a new profile.
     * 
     * @param title
     *            The title of this dialog.
     * @param parent
     *            The {@link Dialog} from which the dialog is displayed.
     */
    ConnectionDialog(String title, Dialog parent, ConnectionDataManager dataManager, ConnectionData currentConnection) {
        super(parent, true);
        
        setIconImage(Resources.getBufferedImage("images/scaffoldhunter-icon.png"));

        this.dataManager = dataManager;
        
        initActions();
        initGUI(title, parent);
        setConnection(currentConnection);
    }

    /**
     * Initializes the actions of this dialog.
     */
    private void initActions() {
        okAction = new OKAction();
        cancelAction = new CancelAction();

        okAction.setEnabled(false);
    }

    /**
     * Initializes the GUI of this dialog.
     */
    private void initGUI(String title, Component parent) {
        // setting basic attributes of the dialog
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(title);
        setResizable(false);
        setLayout(new BorderLayout());
        
        ((JPanel)getContentPane()).setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        getContentPane().add(getProfilePanel(), BorderLayout.CENTER);
        getContentPane().add(getConnectionList(), BorderLayout.WEST);
        getContentPane().add(getButtonPanel(), BorderLayout.SOUTH);
        
        removeConnection.setEnabled(connectionList.getSelectedIndex() != -1);

        pack();

        setLocationRelativeTo(parent);
    }

    /**
     * Creates a panel that holds all components needed to enter the information
     * for a new profile.
     * 
     * @return The <code>JScrollPane</code> with the embedded panel.
     */
    private JPanel getProfilePanel() {
        
        JPanel panel = new JPanel(new BorderLayout(10, 0));
                
        FormLayout layout = new FormLayout("f:p:g, left:default, 5dlu, max(p;120dlu), f:default:g", "p, 3dlu, p, 3dlu, p");

        PanelBuilder commonBuilder = new PanelBuilder(layout);

        nameText = new JTextField();
        SelectAllOnFocus.addTo(nameText);
        // ignore ',' since they aren't allowed in a connection name
        nameText.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                if (e.getKeyChar() == ',') {
                    e.consume();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
            }
        });

        nameText.getDocument().addDocumentListener(validateListener);
        commonBuilder.addLabel(I18n.get("Profile.Name") + ":", CC.xy(2,1));
        commonBuilder.add(nameText, CC.xy(4,1));

        typeDBCombo = new JComboBox(new DefaultComboBoxModel(ConnectionType.values()));
        typeDBCombo.addItemListener(new TypeListener());
        typeDBCombo.addItemListener(validateListener);
        commonBuilder.addLabel(I18n.get("Profile.ConnectionType") + ":", CC.xy(2,3));
        commonBuilder.add(typeDBCombo, CC.xy(4,3));

        commonBuilder.addSeparator(I18n.get("Profile.DB.Header"), CC.xyw(1,5,5));

        panel.add(commonBuilder.getPanel(), BorderLayout.NORTH);
        
        profilePanel = new JPanel(new CardLayout());
        NetworkPanel np = new NetworkPanel();
        np.addPropertyChangeListener(validateListener);
        profilePanels.put(ConnectionType.MYSQL, np);
        DiskPanel dp = new DiskPanel(I18n.get("Profile.ConnectionType.HSQLDB.Filetype"), "script", 
                ConnectionType.HSQLDB.getUrlPrefix());
        dp.addPropertyChangeListener(validateListener);
        profilePanels.put(ConnectionType.HSQLDB, dp);
        
        for (Entry<ConnectionType, ProfilePanel> e : profilePanels.entrySet()) {
            profilePanel.add((JPanel)e.getValue(), e.getKey().name());
        }
        profilePanel.add(new JPanel(), NO_CONNECTION);
        
        panel.add(profilePanel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel getConnectionList() {
        PanelBuilder builder = new PanelBuilder(new FormLayout("max(p;80dlu), 3dlu,p, 5dlu, p, 5dlu", "p, 3dlu, p, f:p:g"));
        builder.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        
        DefaultListModel model = new DefaultListModel();
        for (ConnectionData data : Ordering.usingToString().immutableSortedCopy(dataManager.getConnections())) {
            model.addElement(data);
        }
        connectionList = new JList(model);
        
        connectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connectionList.getSelectionModel().addListSelectionListener(connectionSelection);

        builder.add(new JScrollPane(connectionList), CC.xywh(1, 1, 1, 4));

        builder.add(new JButton(new AddConnectionAction()), CC.xy(3, 1));
        builder.add(new JButton(removeConnection), CC.xy(3, 3));
        
        builder.add(new JSeparator(JSeparator.VERTICAL), CC.xywh(5, 1, 1, 4));
        return builder.getPanel();
    }

    /**
     * Creates the panel that holds the buttons.
     * 
     * @return The <code>JPanel</code> with all buttons.
     */
    private JPanel getButtonPanel() {
        JButton profileOK = new JButton(okAction);
        JButton profileCancel = new JButton(cancelAction);

        getRootPane().setDefaultButton(profileOK);

        JPanel buttonPanel = ButtonBarFactory.buildOKCancelBar(profileOK, profileCancel);
        buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));

        return buttonPanel;
    }

    private void setConnection(ConnectionData connection) {
        if (connection == null) {
            ((CardLayout)profilePanel.getLayout()).show(profilePanel, NO_CONNECTION);
            nameText.setEnabled(false);
            typeDBCombo.setEnabled(false);
            return;
        } else {
            ((CardLayout)profilePanel.getLayout()).show(profilePanel, connection.getDbType().name());
            nameText.setEnabled(true);
            typeDBCombo.setEnabled(true);
        }
        connectionList.setSelectedValue(connection, true);
        nameText.setText(connection.getConnectionName());
        typeDBCombo.setSelectedItem(connection.getDbType());
        ProfilePanel panel = profilePanels.get(connection.getDbType());
        panel.setData(connection);
        Predicate<ConnectionType> other = Predicates.not(Predicates.equalTo(connection.getDbType()));
        for (ProfilePanel p : Maps.filterKeys(profilePanels, other).values()) {
            p.clearData();
        }
        validateListener.validate();
        if (dataManager.getConnections().contains(connection)) {
            needsSaving = null;
        } else {
            needsSaving = connection;
        }
    }
    
    private boolean saveData(ConnectionData data) {
        ConnectionType currentType = (ConnectionType)typeDBCombo.getSelectedItem();
        ProfilePanel activePanel = profilePanels.get(currentType);
        if (!activePanel.isDataValid()) {
            return false;
        }
        String oldname = data.getConnectionName();
        
        data.setConnectionName(nameText.getText().trim());
        data.setDbUrl(activePanel.getURL());
        data.setDbName(activePanel.getSchemaName());
        data.setDbUsername(activePanel.getUserName());
        data.setDbPassword(activePanel.getSavePassword() ? String.valueOf(activePanel.getPassword()) : null);
        data.setDbType(currentType);
        
        boolean success;
        if (dataManager.getConnections().contains(data)) {
            success = dataManager.changeConnection(oldname, data);
        } else {
            success = dataManager.addConnection(data);
        }
        if (success) {
            dataManager.save();
            return true;
        } else {
            JOptionPane.showMessageDialog(this,
                    I18n.get("Start.ConnectionExists", data.getConnectionName()));
            return false;
        }
    }

    private static interface ProfilePanel {

        static final String URL_PROPERTY = "URL";
        static final String SCHEMA_PROPERTY = "SCHEMA";
        static final String USERNAME_PROPERTY = "NAME";
        static final String PASSWORD_PROPERTY = "PASSWORD";
        
        String getURL();

        String getSchemaName();

        String getUserName();

        String getPassword();

        boolean getSavePassword();

        boolean isDataValid();
        
        void setData(ConnectionData data);
        
        void addPropertyChangeListener(PropertyChangeListener  listener);
        
        void removePropertyChangeListener(PropertyChangeListener listener);
        
        void clearData();
    }

    private static class NetworkPanel extends JPanel implements ProfilePanel {

        /** <code>JTextField</code> to enter the database url. */
        private final JTextField urlDBText;
        /** <code>JTextField</code> to enter the name of the database. */
        private final JTextField nameDBText;
        /** <code>JTextField</code> to enter the database username. */
        private final JTextField usernameDBText;
        /** <code>JPasswordField</code> to enter the database password. */
        private final JPasswordField passwordDBText;
        private final JCheckBox usePasswordBox;
        
        /**
         * Listener to assure a profile name and the database connection have
         * been entered.
         */
        private DocumentListener textListener = new TextListener();

        NetworkPanel() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            FormLayout layout = new FormLayout("left:default, 5dlu, 180dlu, fill:default", "");
            DefaultFormBuilder builder = new DefaultFormBuilder(layout);

            builder.appendGlueRow();
            builder.nextLine();
            
            urlDBText = new JTextField();
            SelectAllOnFocus.addTo(urlDBText);
            urlDBText.getDocument().addDocumentListener(textListener);
            builder.append(I18n.get("Profile.URL") + ":", urlDBText);

            nameDBText = new JTextField();
            nameDBText.getDocument().addDocumentListener(textListener);
            SelectAllOnFocus.addTo(nameDBText);
            builder.append(I18n.get("Profile.DatabaseName") + ":", nameDBText);

            usernameDBText = new JTextField();
            SelectAllOnFocus.addTo(usernameDBText);
            usernameDBText.getDocument().addDocumentListener(textListener);
            builder.append(I18n.get("Profile.UserName") + ":", usernameDBText);

            passwordDBText = new JPasswordField();
            SelectAllOnFocus.addTo(passwordDBText);
            passwordDBText.getDocument().addDocumentListener(textListener);
            passwordDBText.setEnabled(false);
            builder.append(I18n.get("Profile.Password") + ":", passwordDBText);

            usePasswordBox = new JCheckBox(I18n.get("Profile.Password.Save"));
            usePasswordBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    passwordDBText.setEnabled(usePasswordBox.isSelected());
                }
            });
            builder.append(usePasswordBox, 3);

            JLabel warning = new JLabel(I18n.get("Profile.Password.Warning"));
            builder.append(warning, 3);
            builder.appendGlueRow();
            add(builder.getPanel());
        }
        
        @Override
        public String getURL() {
            return urlDBText.getText();
        }

        @Override
        public String getSchemaName() {
            return nameDBText.getText();
        }

        @Override
        public String getUserName() {
            return usernameDBText.getText();
        }

        @Override
        public String getPassword() {
            return String.valueOf(passwordDBText.getPassword());
        }

        
        @Override
        public boolean getSavePassword() {
            return usePasswordBox.isSelected();
        }

        /* (non-Javadoc)
         * @see edu.udo.scaffoldhunter.gui.dialogs.ConnectionDialog.ProfilePanel#isDataValid()
         */
        @Override
        public boolean isDataValid() {
            return !urlDBText.getText().trim().isEmpty() && !nameDBText.getText().trim().isEmpty()
            && !usernameDBText.getText().trim().isEmpty();
        }

        private class TextListener implements DocumentListener {
            @Override
            public void changedUpdate(DocumentEvent e) {
                handle(e);
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                handle(e);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handle(e);
            }
            
            private void handle(DocumentEvent e) {
                if (isDataValid()) {
                    if (e.getDocument().equals(urlDBText.getDocument())) {
                        firePropertyChange(URL_PROPERTY, null, urlDBText.getText());
                    } else if (e.getDocument().equals(nameDBText.getDocument())) {
                        firePropertyChange(SCHEMA_PROPERTY, null, nameDBText.getText());
                    } else if (e.getDocument().equals(usernameDBText.getDocument())) {
                        firePropertyChange(USERNAME_PROPERTY, null, usernameDBText.getText());
                    } else if (e.getDocument().equals(passwordDBText.getDocument())) {
                        firePropertyChange(PASSWORD_PROPERTY, null, String.valueOf(passwordDBText.getPassword()));
                    }
                }
            }
        }

        @Override
        public void setData(ConnectionData data) {
            urlDBText.setText(data.getDbUrl());
            nameDBText.setText(data.getDbName());
            usernameDBText.setText(data.getDbUsername());
            passwordDBText.setText(data.getDbPassword());
            usePasswordBox.setSelected(data.getDbPassword() != null);
        }

        @Override
        public void clearData() {
            urlDBText.setText("");
            nameDBText.setText("");
            usernameDBText.setText("");
            passwordDBText.setText("");
            usePasswordBox.setSelected(false);
        
        }
    }
    
    private static class DiskPanel extends JPanel implements ProfilePanel {
        
        private final JFileChooser fileChooser = new FileChooser();
        private final String jdbcPrefix;
        private final JTextField path = new JTextField();;
        
        public DiskPanel(String fileDescription, String fileExtension, String jdbcPrefix) {
            super(new FormLayout("p, 3dlu, p:g, 3dlu, p", "p:g, p, p:g"));
            this.jdbcPrefix = jdbcPrefix;
            path.getDocument().addDocumentListener(new DocumentListener() {
                
                @Override
                public void removeUpdate(DocumentEvent e) {
                    firePropertyChange(URL_PROPERTY, null, path.getText());
                }
                
                @Override
                public void insertUpdate(DocumentEvent e) {
                    firePropertyChange(URL_PROPERTY, null, path.getText());    
                }
                
                @Override
                public void changedUpdate(DocumentEvent e) {
                    firePropertyChange(URL_PROPERTY, null, path.getText());
                }
            });
            fileChooser.setFileFilter(new FileNameExtensionFilter(fileDescription, fileExtension));
            
            add(new JLabel(I18n.get("Profile.DBPath") + ":"), CC.xy(1, 2));
            add(path, CC.xy(3, 2));
            add(new JButton(new SelectPathAction()), CC.xy(5, 2));
        }

        @Override
        public String getURL() {
            return path.getText();
        }

        @Override
        public String getSchemaName() {
            return "ScaffoldHunter";
        }

        @Override
        public String getUserName() {
            return "SA";
        }

        @Override
        public String getPassword() {
            return "";
        }

        @Override
        public boolean getSavePassword() {
            return true;
        }

        @Override
        public boolean isDataValid() {
            if (path.getText().isEmpty() || path.getText().endsWith(File.separator))
                return false;
            File f = new File(path.getText() + ".script");
            return !f.exists() || f.isFile() && f.canWrite();
        }

        @Override
        public void setData(ConnectionData data) {
            if (data.getDbUrl() != null) {
                String path;
                if (data.getDbUrl().indexOf(jdbcPrefix) == 0)
                    path = data.getDbUrl().substring(jdbcPrefix.length(), data.getDbUrl().length());
                else
                    path = data.getDbUrl();
                this.path.setText(path);
            }
        }
        
        @Override
        public void clearData() {
            path.setText("");
        }
        
        class SelectPathAction extends AbstractAction {
            
            public SelectPathAction() {
                super("...");
                putValue(Action.SHORT_DESCRIPTION, I18n.get("Profile.SelectFile"));
            }
            
            @Override
            public void actionPerformed(ActionEvent e) {
                fileChooser.setSelectedFile(new File(path.getText()));
                fileChooser.showDialog(DiskPanel.this, I18n.get("Button.OK"));    
                File f = fileChooser.getSelectedFile();
                if (f != null) {
                    String p = f.getAbsolutePath();
                    if (p.endsWith(".script"))
                        p = p.substring(0, p.length() - ".script".length());
                    path.setText(p);
                }
            }
        }
    }

    private class OKAction extends AbstractAction {
        public OKAction() {
            super(I18n.get("Button.OK"));
            putValue(Action.SMALL_ICON, Resources.getIcon("apply.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ConnectionData current = (ConnectionData)connectionList.getSelectedValue();
            if (saveData(current)) {
                dispose();
            }
        }
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super(I18n.get("Button.Cancel"));
            putValue(Action.SMALL_ICON, Resources.getIcon("cancel.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    }

    private class AddConnectionAction extends AbstractAction {

        public AddConnectionAction() {
            putValue(Action.SMALL_ICON, Resources.getIcon("plus.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Start.Profile.New.Description"));
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            connectionList.clearSelection();
            ConnectionData newData = new ConnectionData();
            Set<String> connectionNames = Sets.newHashSet();
            for (ConnectionData d : dataManager.getConnections()) {
                connectionNames.add(d.getConnectionName());
            }
            String newName = I18n.get("Profile.NewConnection");
            int i = 1;
            while (connectionNames.contains(newName)) {
                newName = String.format("%s (%d)", newName, i++);
            }
            newData.setConnectionName(newName);
            ((DefaultListModel)connectionList.getModel()).addElement(newData);
            setConnection(newData);
            connectionList.addListSelectionListener(connectionSelection);
        }
    }

    private class RemoveConnectionAction extends AbstractAction {
        
        public RemoveConnectionAction() {
            super();
            putValue(Action.SMALL_ICON, Resources.getIcon("minus.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Start.Profile.Delete"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            ConnectionData selected = (ConnectionData)connectionList.getSelectedValue();
            if (JOptionPane.showConfirmDialog(ConnectionDialog.this, I18n.get("Message.Delete"), null, JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                dataManager.removeConnection(selected.getConnectionName());
                dataManager.save();
                needsSaving = null;
                ((DefaultListModel)connectionList.getModel()).removeElement(selected);
                connectionList.setSelectedIndex(0);
            }
        }
    }

    private class ConnectionSelectionListener implements ListSelectionListener {

        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (connectionList.getSelectedIndex() == -1) {
                setConnection(null);
            }
            removeConnection.setEnabled(connectionList.getSelectedIndex() != -1);
            
            ConnectionData selected = (ConnectionData)connectionList.getSelectedValue();
            if (needsSaving == selected) {
                return;
            }
            if (needsSaving != null) {
                int result = JOptionPane.showConfirmDialog(ConnectionDialog.this,
                        I18n.get("Profile.UnsavedChanges", needsSaving.getConnectionName()));
                switch (result) {
                case JOptionPane.YES_OPTION:
                    if (saveData(needsSaving)) {
                        needsSaving = null;
                    } else {
                        JOptionPane.showMessageDialog(ConnectionDialog.this, I18n.get("Profile.InvalidData"), 
                                I18n.get("Title.Error"), JOptionPane.ERROR_MESSAGE);
                        connectionList.setSelectedValue(needsSaving, true);
                        if (!dataManager.getConnections().contains(selected)) {
                            ((DefaultListModel)connectionList.getModel()).removeElement(selected);
                        }
                        return;
                    }
                    break;
                case JOptionPane.NO_OPTION:
                    if (!dataManager.getConnections().contains(needsSaving)) {
                        ((DefaultListModel)connectionList.getModel()).removeElement(needsSaving);
                    }
                    needsSaving = null;
                    break;
                case JOptionPane.CANCEL_OPTION:
                    connectionList.setSelectedValue(needsSaving, true);
                    if (!dataManager.getConnections().contains(selected)) {
                        ((DefaultListModel)connectionList.getModel()).removeElement(selected);
                    }
                    return;
                }
            }
            if (selected != null)
                setConnection(selected);
        }
    }
    
    private class ValidateListener implements DocumentListener, PropertyChangeListener, ItemListener {
        
        void validate() {
            needsSaving = (ConnectionData)connectionList.getSelectedValue();
            ProfilePanel activePanel = profilePanels.get(typeDBCombo.getSelectedItem());
            okAction.setEnabled(!nameText.getText().trim().isEmpty() && activePanel.isDataValid());
        }
        
        @Override
        public void changedUpdate(DocumentEvent e) {
            validate();
        }
        
        @Override
        public void insertUpdate(DocumentEvent e) {
            validate();
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            validate();    
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            validate();
        }
        
        @Override
        public void itemStateChanged(ItemEvent e) {
            validate();    
        }
    }
    
    private class TypeListener implements ItemListener {
        
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ConnectionType type = (ConnectionType)typeDBCombo.getSelectedItem();
                ((CardLayout)profilePanel.getLayout()).show(profilePanel, type.name());
            }
        }
    }

    
}