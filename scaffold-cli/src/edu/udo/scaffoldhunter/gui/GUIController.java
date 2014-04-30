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

package edu.udo.scaffoldhunter.gui;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.data.ConnectionData;
import edu.udo.scaffoldhunter.data.ConnectionDataManager;
import edu.udo.scaffoldhunter.gui.dialogs.AboutDialog;
import edu.udo.scaffoldhunter.gui.dialogs.InitialViewDialog;
import edu.udo.scaffoldhunter.gui.dialogs.ProfileDialog;
import edu.udo.scaffoldhunter.gui.dialogs.SessionDialog;
import edu.udo.scaffoldhunter.gui.dialogs.StartDialog;
import edu.udo.scaffoldhunter.gui.dialogs.StartDialog.Result;
import edu.udo.scaffoldhunter.gui.dialogs.TooltipConfigurationDialog;
import edu.udo.scaffoldhunter.gui.filtering.FilterDialog;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.gui.util.ProgressWorkerUtil;
import edu.udo.scaffoldhunter.gui.util.VoidNullaryDBFunction;
import edu.udo.scaffoldhunter.model.XMLSerialization;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.DbManagerHibernate;
import edu.udo.scaffoldhunter.model.db.Filterset;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.ProgressAdapter;
import edu.udo.scaffoldhunter.util.StringList;
import edu.udo.scaffoldhunter.view.View;

/**
 * Main controller class, managing main windows and basic user actions.
 * 
 * @author Dominic Sacré
 * @author Thomas Schmitz
 */
public class GUIController {

    private final XMLSerialization xmlSerialization = new XMLSerialization();

    private DbManager db;
    private ConfigManager configManager;
    private GUISession session;

    // profile, sessionTitles and datasets used to call db functions in
    // LoginWorker and use the data later
    private Profile profile;
    private List<String> sessionTitles;
    private List<Dataset> datasets;

    /**
     * Creates the initial main window
     * 
     * @param dataManager
     *            the data manager which is used to access configuration data on
     *            disk
     */
    public void start(final ConnectionDataManager dataManager) {
        class Start implements Runnable {
            @Override
            public void run() {
                final StartDialog startDialog = new StartDialog(GUIController.this, dataManager);
                startDialog.setModal(true);
                // setVisible doc says this works, although it blocks the EDT
                startDialog.setVisible(true);
                Result result = startDialog.getResult();
                if (result.equals(Result.OK) && startDialog.getData() != null) {
                    ConnectionData data = startDialog.getData();
                    final boolean useLastSession = startDialog.getUseLastSession();
                    String dbPassword = data.getDbPassword();
                    // ask for password if no password has been set
                    if (dbPassword == null) {
                        // blocks, nevertheless ok on EDT
                        dbPassword = showPasswordDialog(data);
                    }
                    if (dbPassword != null) {

                        LoginWorker login;

                        if (startDialog.isPasswordChanged())
                            login = new LoginWorker(data.getDbType().getDriverClass(), data.getDbType()
                                    .getHibernateDialect(), data.getDbUrl(), data.getDbName(), data.getDbUsername(),
                                    dbPassword, startDialog.getUsername(), startDialog.getNewPassword(), null);
                        else
                            login = new LoginWorker(data.getDbType().getDriverClass(), data.getDbType()
                                    .getHibernateDialect(), data.getDbUrl(), data.getDbName(), data.getDbUsername(),
                                    dbPassword, startDialog.getUsername(), "", startDialog.getSavedPassword());
                        login.addProgressListener(new ProgressAdapter<Boolean>() {
                            @Override
                            public void finished(Boolean success, boolean cancelled) {
                                if (success != null && success && !cancelled) {
                                    if (startDialog.isPasswordChanged() && startDialog.getSavePassword())
                                        startDialog.saveNewPassword(profile.getPassword());
                                    if (!startDialog.getSavePassword())
                                        startDialog.saveNewPassword(null);
                                    createStartingState(profile, useLastSession);
                                } else {
                                    startDialog.saveNewPassword(null);
                                    SwingUtilities.invokeLater(new Start());
                                }
                            }
                        });
                        ProgressWorkerUtil.executeWithProgressDialog(startDialog, _("Message.Info"),
                                _("Message.Connect"), ModalityType.MODELESS, login);
                    } else {
                        SwingUtilities.invokeLater(new Start());
                    }
                } else if (result.equals(Result.QUIT)) {
                    System.exit(0);
                }
            }
        }

        SwingUtilities.invokeLater(new Start());
    }

    private class LoginWorker extends ProgressWorker<Boolean, Void> {
        private String connectionDriver;
        private String hibernateDialect;
        private String connectionUrl;
        private String connectionSchema;
        private String connectionUsername;
        private String connectionPassword;
        private String username;
        private String newPassword;
        private byte[] savedPassword;

        private boolean getUserAndCheckPassword = true;

        /**
         * @param getUserAndCheckPassword
         *            the checkUserAndPassword to set
         */
        public void setGetUserAndCheckPassword(boolean getUserAndCheckPassword) {
            this.getUserAndCheckPassword = getUserAndCheckPassword;
        }

        /**
         * @param connectionDriver
         * @param hibernateDialect
         * @param connectionUrl
         * @param connectionSchema
         * @param connectionUsername
         * @param connectionPassword
         * @param username
         * @param newPassword
         * @param savedPassword
         */
        public LoginWorker(String connectionDriver, String hibernateDialect, String connectionUrl,
                String connectionSchema, String connectionUsername, String connectionPassword, String username,
                String newPassword, byte[] savedPassword) {
            super();
            this.connectionDriver = connectionDriver;
            this.hibernateDialect = hibernateDialect;
            this.connectionUrl = connectionUrl;
            this.connectionSchema = connectionSchema;
            this.connectionUsername = connectionUsername;
            this.connectionPassword = connectionPassword;
            this.username = username;
            this.newPassword = newPassword;
            this.savedPassword = savedPassword;
        }

        @Override
        protected Boolean doInBackground() throws Exception {
            boolean failed = false;
            try {
                setProgressIndeterminate(true);
                if (db == null || !db.isConnected() || !db.getConnectionDriverClass().equals(connectionDriver)
                        || !db.getHibernateDialect().equals(hibernateDialect)
                        || !db.getConnectionUrl().equals(connectionUrl)
                        || !db.getConnectionUsername().equals(connectionUsername)
                        || !db.getConnectionPassword().equals(connectionPassword)) {
                    db = new DbManagerHibernate(connectionDriver, hibernateDialect, connectionUrl, connectionSchema,
                            connectionUsername, connectionPassword, false, false);
                    if (db.schemaExists()) {
                        if (db.validateSchema())
                            db.initializeSessionFactory();
                        else {
                            int result = JOptionPane.showConfirmDialog(null, _("Message.DbInvalid"),
                                    _("Title.Question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                            if (result == JOptionPane.YES_OPTION) {
                                db.createAndExportSchema();
                                db.initializeSessionFactory();
                            } else
                                failed = true;
                        }
                    } else {
                        int result = JOptionPane.showConfirmDialog(null, _("Message.DbNotExisting"),
                                _("Title.Question"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                        if (result == JOptionPane.YES_OPTION) {
                            db.createAndExportSchema();
                            db.initializeSessionFactory();
                        } else
                            failed = true;
                    }
                }
                if (!failed && getUserAndCheckPassword) {
                    List<String> profiles = db.getAllProfileNames();
                    if (!profiles.contains(username)) {
                        JOptionPane.showMessageDialog(null, I18n.get("Message.UserNotFound"), I18n.get("Title.Error"),
                                JOptionPane.ERROR_MESSAGE);
                        failed = true;
                    } else {
                        profile = db.getProfile(username);

                        boolean check;
                        if (savedPassword != null)
                            check = profile.checkPassword(savedPassword);
                        else
                            check = profile.checkPassword(newPassword);
                        if (!check) {
                            JOptionPane.showMessageDialog(null, I18n.get("Message.PasswordWrong"),
                                    I18n.get("Title.Error"), JOptionPane.ERROR_MESSAGE);
                            failed = true;
                        }
                        sessionTitles = db.getAllSessionTitles(profile);
                        datasets = db.getAllDatasets();
                    }
                }
            } catch (DatabaseException dbex) {
                failed = true;
                JOptionPane.showMessageDialog(null, I18n.get("Message.ConnectionProblems"), I18n.get("Title.Error"),
                        JOptionPane.ERROR_MESSAGE);
            }
            return !failed;
        }
    }

    private class NewSessionWorker extends ProgressWorker<Session, Void> {

        private Profile profile;
        private String sessionTitle;
        private Tree tree;
        private Filterset filterset;

        public NewSessionWorker(Profile profile, String sessionTitle, Tree tree, Filterset filterset) {
            this.profile = profile;
            this.sessionTitle = sessionTitle;
            this.tree = tree;
            this.filterset = filterset;
        }

        @Override
        protected Session doInBackground() throws Exception {
            setProgressIndeterminate(true);

            Session newSession = new Session(profile, sessionTitle, new Date(), tree, null, null);

            Subset root = db.getRootSubset(newSession, filterset);
            newSession.setProfile(profile);
            db.saveAsNew(newSession);
            db.saveAsNew(root);
            newSession.setSubset(root);
            db.saveOrUpdate(newSession);

            return newSession;
        }

    }

    private class LoadSessionWorker extends ProgressWorker<GUISession, Void> {
        private Profile profile;
        private String sessionTitle;
        private java.awt.Window parent;

        public LoadSessionWorker(Profile profile, String sessionTitle, java.awt.Window parent) {
            this.profile = profile;
            this.sessionTitle = sessionTitle;
            this.parent = parent;
        }

        @Override
        protected GUISession doInBackground() throws Exception {
            setProgressIndeterminate(true);

            if (sessionTitle == null) {
                db.loadCurrentSession(profile);
            } else {
                Session newCurrentSession = db.getSession(profile, sessionTitle);
                profile.setCurrentSession(newCurrentSession);
                db.saveOrUpdate(profile);
            }

            final Session dbSession = profile.getCurrentSession();

            if (dbSession != null) {
                dbSession.setLastUsed(new Date());
                db.saveOrUpdate(dbSession);

                String xmlConfig = profile.getConfigData();
                final String xmlSession = dbSession.getSessionData();

                configManager = null;
                session = null;

                if (!Strings.isNullOrEmpty(xmlConfig)) {
                    // try restoring existing config
                    try {
                        configManager = xmlSerialization.configFromXML(GUIController.this, xmlConfig);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeAndWait(new Runnable() {
                            @Override
                            public void run() {
                                JOptionPane.showMessageDialog(null, _("ConfigRestoreError.Message"),
                                        _("ConfigRestoreError.Title"), JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                }

                if (configManager == null) {
                    // no existing config, or config failed to load
                    configManager = new ConfigManager(GUIController.this);
                }

                if (!Strings.isNullOrEmpty(xmlSession)) {
                    // try restoring existing session
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                session = xmlSerialization.sessionFromXML(GUIController.this, dbSession, xmlSession);

                                for (edu.udo.scaffoldhunter.gui.Window w : session.getViewManager().getWindows()) {
                                    w.setVisible(true);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                                JOptionPane.showMessageDialog(null, _("SessionRestoreError.Message"),
                                        _("SessionRestoreError.Title"), JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                }

                if (session == null) {
                    // no existing session, or session failed to load
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            session = new GUISession(GUIController.this, dbSession);

                            Subset rootSubset = session.getRootSubset();

                            // create dialog, which asks for initally opened views
                            InitialViewDialog viewDlg = new InitialViewDialog(parent);
                            viewDlg.setModal(true);
                            viewDlg.setVisible(true);
                            viewDlg.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                            
                            List<View> views = Lists.newArrayList();
                            for(Class<? extends View> viewclass : viewDlg.getSelectedViews()) {
                                views.add(session.getViewManager().createView(viewclass, rootSubset));
                            }
                            createWindow(views);
                        }
                    });
                }
            }

            return session;
        }

    }

    /**
     * Creates the starting state of Scaffoldhunter
     */
    private void createStartingState(Profile profile, boolean useLastSession) {
        // Profile profile = db.getProfile(profileName);
        Session currentSession = null;

        if (useLastSession) {
            LoadSessionWorker loadSession = new LoadSessionWorker(profile, null, null);
            ProgressWorkerUtil.executeWithProgressDialog(null, _("Message.Info"), _("Message.LoadSession"),
                    ModalityType.DOCUMENT_MODAL, loadSession);

            currentSession = profile.getCurrentSession();
        }

        if (currentSession == null) {
            if (!showSessionDialog(profile, null))
                System.exit(0);
            currentSession = profile.getCurrentSession();
        }
    }

    /**
     * Opens the dialog to create a new user profile and creates that user in
     * the database.
     * 
     * @param startDialog
     *            The StartDialog
     * @param dataManager
     * 
     * @return Did the user create a new profile?
     */
    public boolean createNewProfile(StartDialog startDialog, ConnectionDataManager dataManager) {
        final ProfileDialog profileDlg = new ProfileDialog(dataManager);
        profileDlg.setModal(true);
        profileDlg.setVisible(true);

        if (profileDlg.getResult()) {
            ConnectionData data = startDialog.getData();

            String dbPassword = data.getDbPassword();
            // ask for password if no password has been set
            if (dbPassword == null) {
                // blocks, nevertheless ok on EDT
                dbPassword = showPasswordDialog(data);
            }
            if (dbPassword != null) {

                startDialog.setUsername(profileDlg.getUsername());
                startDialog.setNewPassword("");

                LoginWorker login = new LoginWorker(data.getDbType().getDriverClass(), data.getDbType()
                        .getHibernateDialect(), data.getDbUrl(), data.getDbName(), data.getDbUsername(), dbPassword,
                        "", "", null);
                login.setGetUserAndCheckPassword(false);
                login.addProgressListener(new ProgressAdapter<Boolean>() {
                    @Override
                    public void finished(Boolean result, boolean cancelled) {
                        final Boolean finalResult = result;
                        final boolean finalCancalled = cancelled;
                    
                        DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {
                            @Override
                            public void voidCall() throws DatabaseException {
                                if (finalResult != null && finalResult && !finalCancalled) {
                                    if (!StringList.containsIgnoreCase(db.getAllProfileNames(), profileDlg.getUsername())) {
                                        Profile profile = new Profile();
                                        profile.setUsername(profileDlg.getUsername());
                                        profile.setPasswordEncrypted(profileDlg.getPassword());
                                        db.saveAsNew(profile);
                                    } else
                                        JOptionPane.showMessageDialog(profileDlg,
                                                _("EditProfile.AlreadyExisting", profileDlg.getUsername()),
                                                _("Title.Warning"), JOptionPane.WARNING_MESSAGE);
                                }
                            }
                        });
                    }
                });
                ProgressWorkerUtil.executeWithProgressDialog(startDialog, _("Message.Info"), _("Message.Connect"),
                        ModalityType.APPLICATION_MODAL, login);
                return true;
            }
        }
        return false;
    }

    /**
     * Shows the session dialog and loads the selected session.
     * 
     * @param profile
     *            The profile for which the session will be loaded
     * @param owner
     *            The owner of this dialog. If null, a quit button is shown
     *            instead of a cancel button.
     * @return The result of the session dialog
     */
    public boolean showSessionDialog(Profile profile, java.awt.Window owner) {
        SessionDialog sessionDlg = new SessionDialog(db, datasets, profile, sessionTitles, this, owner);
        sessionDlg.setModal(true);
        sessionDlg.setVisible(true);

        if (sessionDlg.getResult()) {
            if (session != null) {
                int result = JOptionPane.showConfirmDialog(owner, _("Sessions.ConfirmSaveMessage"),
                        _("Sessions.ConfirmSaveTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
                if (result == JOptionPane.CANCEL_OPTION)
                    return false;
                if (result == JOptionPane.YES_OPTION) {
                    saveSession(owner);
                }

                for (Window w : session.getViewManager().getWindows()) {
                    w.destroy();
                }
            }

            LoadSessionWorker loadSession = new LoadSessionWorker(profile, sessionDlg.getSessionTitle(), owner);
            ProgressWorkerUtil.executeWithProgressDialog(null, _("Message.Info"), _("Message.LoadSession"),
                    ModalityType.DOCUMENT_MODAL, loadSession);
            return true;
        } else
            return false;
    }

    /**
     * Saves the current session.
     * 
     * @param parent
     *            the parent Component for modal dialogs
     */
    public void saveSession(Component parent) {
        String xmlConfig = xmlSerialization.configToXML(configManager);
        String xmlSession = xmlSerialization.sessionToXML(session);
        final Session dbSession = session.getDbSession();

        profile.setConfigData(xmlConfig);
        dbSession.setSessionData(xmlSession);

        boolean canBeSaved = DBExceptionHandler.callDBManager(db, new DBFunction<Boolean>() {

            @Override
            public Boolean call() throws DatabaseException {
                return db.canSaveSession(dbSession);
            }
        });
        if (!canBeSaved) {
            JOptionPane.showMessageDialog(parent, _("Sessions.PacketToLargeMessage"), _("Sessions.PacketToLargeTitle"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {
                @Override
                public void voidCall() throws DatabaseException {
                    db.saveOrUpdate(profile);
                    db.saveOrUpdate(dbSession);
                }
            });
        }
    }

    /**
     * Shows the FilterDialog and creates a new Session with given data and
     * chosen filterset.
     * 
     * @param profile
     *            The profile, the session should be created for
     * @param dataset
     *            The dataset, the session should be created with
     * @param tree
     *            The tree, the session should be created with
     * @param title
     *            The title, the session should be created with
     * @param parent
     *            The parent window for the WorkerDialog
     */
    public void createNewSession(Profile profile, Dataset dataset, Tree tree, String title, java.awt.Window parent) {
        FilterDialog filterDlg = new FilterDialog(null, db, profile, dataset, null);
        filterDlg.setModal(true);
        filterDlg.setVisible(true);

        if (filterDlg.getResult()) {
            NewSessionWorker newSession = new NewSessionWorker(profile, title, tree, filterDlg.getSelectedFilterset());
            ProgressWorkerUtil.executeWithProgressDialog(parent, _("Message.Info"), _("Message.NewSession"),
                    ModalityType.APPLICATION_MODAL, newSession);
        }
    }

    /**
     * Shows a password dialog and blocks the current thread until the dialog is
     * closed.
     * 
     * @param data
     *            url, database name and username of this object are displayed
     *            in the dialog
     * 
     * @return the password if the user entered a password and clicked OK, null
     *         otherwise
     */
    private String showPasswordDialog(ConnectionData data) {
        // Build the password panel
        final JPasswordField pwField = new JPasswordField();
        DefaultFormBuilder builder = new DefaultFormBuilder(new FormLayout("left:default, default, 200dlu"));
        builder.append("URL: ", new JLabel(data.getDbUrl()));
        builder.append("Database: ", new JLabel(data.getDbName()));
        builder.append("Username: ", new JLabel(data.getDbUsername()));
        builder.append(pwField, 3);

        // Show a password dialog with a nasty hack to set the focus to the
        // password field.
        // There is no beautiful way of focusing the field:
        // http://bugs.sun.com./bugdatabase/view_bug.do?bug_id=5018574
        // The solution used here is: repeat setFocus using a timer until user
        // pressed key in password field
        // TODO maybe write a class PasswordDialog which sets up the dialog
        // manually, so that we get rid of this hack
        JOptionPane pwPane = new JOptionPane(builder.getPanel(), JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        JDialog pwDialog = pwPane.createDialog("Enter password:");
        final Timer timer = new Timer(1, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pwField.requestFocusInWindow();
            }
        });
        timer.setRepeats(true);
        timer.start();
        pwField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                timer.stop();
            }
        });
        pwDialog.setVisible(true);

        // Handle return value
        Object returnValue = pwPane.getValue();
        if (returnValue instanceof Integer) {
            int result = (Integer) returnValue;
            if (result == JOptionPane.OK_OPTION) {
                return new String(pwField.getPassword());
            }
        }
        // User clicked cancel or closed the dialog
        return null;
    }

    /**
     * Creates a new main window with no views.
     * 
     * @return the new window
     */
    public MainWindow createWindow() {
        return createWindow(null);
    }

    /**
     * Creates a new main window.
     * 
     * @param views
     *            the views to be shown in this main window, or null for an
     *            empty window
     * 
     * @return the new window
     */
    public MainWindow createWindow(List<View> views) {
        MainWindow window = session.getViewManager().createMainWindow(session);
        session.getViewManager().addWindow(window);

        if (views != null) {
            for (View view : views) {
                session.getViewManager().addView(view, window);
            }
        }

        window.setVisible(true);

        return window;
    }

    /**
     * Closes a main window.
     * 
     * @param window
     *            the window to be closed
     */
    public void closeWindow(MainWindow window) {
        if (session.getViewManager().getWindows().size() == 1) {
            exit(window);
        } else {
            int result = JOptionPane.showConfirmDialog(window.getFrame(), _("CloseWindow.Message"),
                    _("CloseWindow.Title"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                session.getViewManager().removeWindow(window);
            }
        }
    }

    /**
     * Exits the program.
     * 
     * @param window
     *            the window from which the exit was initiated
     */
    public void exit(MainWindow window) {
        int result = JOptionPane.showConfirmDialog(window.getFrame(), _("Sessions.ConfirmSaveMessage"),
                _("Sessions.ConfirmSaveTitle"), JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

        switch (result) {
        case JOptionPane.YES_OPTION:
            saveSession(window.getFrame());
            System.exit(0);
            break;
        case JOptionPane.NO_OPTION:
            System.exit(0);
            break;
        case JOptionPane.CANCEL_OPTION:
            break;
        }
    }

    /**
     * Shows the config dialog for all views of the given main window.
     * 
     * @param window
     */
    public void showOptionsDialog(MainWindow window) {
        configManager.showOptionsDialog(window.getFrame(), session.getViewManager().getViews(window),
                window.getActiveView());
    }

    /**
     * Shows the global config dialog for all views of all frames
     * 
     * @param window
     *            the parent window of the dialog
     */
    public void showGlobalOptionsDialog(MainWindow window) {
        List<View> views = new LinkedList<View>();
        for (View view : session.getViewManager().getAllViews()) {
            views.add(view);
        }
        configManager.showGlobalOptionsDialog(window.getFrame(), views, window.getActiveView());
    }

    /**
     * Shows the tooltip configuration dialog
     * 
     * @param window
     *            the parent window of the dialog
     */
    public void showTooltipDialog(MainWindow window) {
        TooltipConfigurationDialog dialog = new TooltipConfigurationDialog(window.getFrame(), session.getDbSession()
                .getDataset(), configManager.getGlobalConfig());

        dialog.setVisible(true);
        configManager.updateGlobalConfig(session.getViewManager().getAllViews());
    }

    /**
     * Shows the about dialog.
     * 
     * @param window
     *            the dialog's parent window
     */
    public void showAboutDialog(MainWindow window) {
        AboutDialog dlg = new AboutDialog(window.getFrame());
        dlg.setVisible(true);
    }

    /**
     * @return the DB manager
     */
    public DbManager getDbManager() {
        return db;
    }

    /**
     * @return the config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * @return the current session
     */
    public GUISession getCurrentSession() {
        return session;
    }

}
