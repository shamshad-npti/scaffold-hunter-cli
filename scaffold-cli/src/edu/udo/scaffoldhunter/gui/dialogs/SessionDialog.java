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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.GUIController;
import edu.udo.scaffoldhunter.gui.datasetmanagement.DatasetManagement;
import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.SessionInformation;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.util.StringList;

/**
 * A dialog that is used to choose existing Datasets and Trees or creating new
 * ones.
 * 
 * @author Thomas Schmitz
 */
public class SessionDialog extends JDialog {
    private DbManager db;
    private DatasetManagement controller;

    private Profile profile;

    private boolean result = false;

    private Action addAction;

    private Action cancelAction;

    private Action manageDatasetsAction;

    private Action openAction;

    private Action removeAction;

    private Action renameAction;

    private JTextField sessionTitle;
    private Border defaultBorder;

    private DefaultComboBoxModel datasetModel;

    private JComboBox datasetsCB;

    private DefaultComboBoxModel treeModel;

    private JComboBox treesCB;

    private Map<String, Dataset> datasetMap = new HashMap<String, Dataset>();

    private Map<String, Tree> treeMap = new HashMap<String, Tree>();

    private List<String> sessionNames = new LinkedList<String>();

    private JList sessionsList;

    private DefaultListModel sessionsModel;

    private GUIController guiController;

    /**
     * @return the dialog result
     */
    public boolean getResult() {
        return result;
    }

    /**
     * @return the SessionTitle
     */
    public String getSessionTitle() {
        return ((SessionInformation) sessionsList.getSelectedValue()).getTitle();
    }

    private String getNewSessionTitle() {
        return sessionTitle.getText();
    }

    /**
     * @return the selected Tree
     */
    private Tree getSelectedTree() {
        return treeMap.get(treesCB.getSelectedItem());
    }

    /**
     * @return the selected Dataset
     */
    private Dataset getSelectedDataset() {
        return datasetMap.get(datasetsCB.getSelectedItem());
    }

    /**
     * Constructs the dialog to load and create sessions.
     * 
     * @param db
     *            the DB manager
     * @param datasets
     *            A list of datasets used to fill the datasets combobox. If
     *            null, a dbManager function is called to get all datasets.
     * @param profile
     *            The profile for which the session will be loaded
     * @param sessionTitles
     *            A list of sessionTitles used for sessionTitle verification. If
     *            null, a dbManager function is called to get all sessionTitles.
     * @param guiController
     *            The GUIController
     * @param owner
     *            The owner of this dialog. If null, a quit button is shown
     *            instead of a cancel button.
     */
    public SessionDialog(DbManager db, List<Dataset> datasets, Profile profile, List<String> sessionTitles,
            GUIController guiController, Window owner) {
        super(owner, ModalityType.TOOLKIT_MODAL);

        setIconImage(Resources.getBufferedImage("images/scaffoldhunter-icon.png"));

        this.db = db;
        this.profile = profile;
        this.controller = new DatasetManagement(db, this, profile);
        this.guiController = guiController;

        initActions();
        initModels(datasets, profile, sessionTitles);
        initGUI();

        setLocationRelativeTo(owner);

        updateAllowAdd();
        updateAllowSessionActions();
    }

    /**
     * Initializes all used models for the comboboxes and the sessionTitle
     * verification.
     * 
     * @param datasets
     *            A list of datasets used to fill the datasets combobox. If
     *            null, a dbManager function is called to get all datasets.
     * @param profile
     *            The profile used to get the sessionTitles if sessionTitles is
     *            null
     * @param sessionTitles
     *            A list of sessionTitles used for sessionTitle verification. If
     *            null, a dbManager function is called to get all sessionTitles.
     */
    private void initModels(List<Dataset> datasets, Profile profile, List<String> sessionTitles) {
        datasetModel = new DefaultComboBoxModel();
        updateDatasetModel(datasets);

        treeModel = new DefaultComboBoxModel();
        updateTreeModel();

        if (sessionTitles != null) {
            sessionNames.addAll(sessionTitles);
        } else
            try {
                sessionNames.addAll(db.getAllSessionTitles(profile));
            } catch (DatabaseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        sessionsModel = new DefaultListModel();
        try {
            List<SessionInformation> sessions = db.getAllSessionsInformation(profile);
            for (SessionInformation s : sessions)
                sessionsModel.addElement(s);
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateSessions() {
        sessionNames.clear();
        try {
            sessionNames.addAll(db.getAllSessionTitles(profile));
            List<SessionInformation> sessions = db.getAllSessionsInformation(profile);
            SessionInformation session = ((SessionInformation) sessionsList.getSelectedValue());
            sessionsModel.clear();
            for (SessionInformation s : sessions)
                sessionsModel.addElement(s);
            if (session != null)
                sessionsList.setSelectedValue(findSessionInformation(session.getTitle()), true);
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Updates the datasets combobox.
     * 
     * @param datasets
     *            The datasets to display in the combobox. If null, a dbManager
     *            function is called to get all datasets.
     */
    private void updateDatasetModel(List<Dataset> datasets) {
        datasetModel.removeAllElements();
        datasetMap.clear();
        try {
            if (datasets == null)
                datasets = db.getAllDatasets();
            for (Dataset d : datasets) {
                datasetMap.put(d.getTitle(), d);
                datasetModel.addElement(d.getTitle());
            }
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void updateTreeModel() {
        treeModel.removeAllElements();
        treeMap.clear();
        String dsName = (String) datasetModel.getSelectedItem();
        Dataset ds = datasetMap.get(dsName);
        if (ds != null)
            for (Tree t : ds.getTrees()) {
                treeMap.put(t.getTitle(), t);
                treeModel.addElement(t.getTitle());
            }
    }

    /**
     * Initializes the GUI of this dialog.
     */
    private void initGUI() {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle(_("SessionManagement.Title"));
        setResizable(false);
        getContentPane().setLayout(new BorderLayout());

        getContentPane().add(getSessionsPanel(), BorderLayout.CENTER);

        getContentPane().add(getNewSessionPanel(), BorderLayout.NORTH);

        pack();
    }

    private JPanel getSessionsPanel() {
        FormLayout layout = new FormLayout("l:d, 5dlu, f:d:grow, 5dlu, f:d", // 5
                                                                             // columns
                "f:d, 5dlu, f:d, 5dlu, f:d:grow, 5dlu, f:d"); // 7 rows
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();

        sessionsList = new JList(sessionsModel);
        sessionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sessionsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
                updateAllowSessionActions();
            }
        });
        sessionsList.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && openAction.isEnabled())
                    openAction.actionPerformed(null);
            }
        });
        Session session = profile.getCurrentSession();
        sessionsList.setCellRenderer(new SessionCellRenderer());
        JScrollPane sessionsScroll = new JScrollPane(sessionsList);
        sessionsScroll.setPreferredSize(new Dimension(200, 200));
        pb.add(sessionsScroll, cc.xywh(1, 1, 3, 5));
        if (session != null)
            sessionsList.setSelectedValue(findSessionInformation(session.getTitle()), true);

        JButton renameButton = new JButton(renameAction);
        pb.add(renameButton, cc.xy(5, 1));

        JButton removeButton = new JButton(removeAction);
        pb.add(removeButton, cc.xy(5, 3));

        JButton openButton = new JButton(openAction);
        getRootPane().setDefaultButton(openButton);

        JButton cancelButton = new JButton(cancelAction);
        pb.add(ButtonBarFactory.buildOKCancelBar(openButton, cancelButton), cc.xyw(1, 7, 5));

        JPanel panel = pb.getPanel();
        panel.setBorder(BorderFactory.createTitledBorder(_("Sessions.Title")));

        return panel;
    }

    private static class SessionCellRenderer extends DefaultListCellRenderer {
        /*
         * (non-Javadoc)
         * 
         * @see
         * javax.swing.DefaultListCellRenderer#getListCellRendererComponent(
         * javax.swing.JList, java.lang.Object, int, boolean, boolean)
         */
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            SessionInformation session = (SessionInformation) value;
            label.setText("<HTML><BODY><B>" + session.getTitle() + "</B><BR><i>Dataset:</i> "
                    + session.getDatasetName() + "<BR><i>Tree:</i> " + session.getTreeName() + "<BR><i>Molecules:</i> "
                    + session.getRootSubsetSize() + "</BODY></HTML>");
            return label;
        }
    }

    /**
     * Creates a panel that holds all components to select a dataset and a tree.
     * 
     * @return The <code>JPanel</code>.
     */
    private JPanel getNewSessionPanel() {
        FormLayout layout = new FormLayout("l:d, 5dlu, f:d:grow", // 3 columns
                "f:d:grow, 5dlu, f:d, 5dlu, f:d, 5dlu, f:d"); // 7 rows
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();

        JLabel sessionTitleLabel = new JLabel(I18n.get("NewSession.SessionTitle"));
        pb.add(sessionTitleLabel, cc.xy(1, 1));

        sessionTitle = new JTextField();
        defaultBorder = sessionTitle.getBorder();
        sessionTitle.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateAllowAdd();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateAllowAdd();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateAllowAdd();
            }
        });
        sessionTitle.setText(getFreeSessionTitle());
        pb.add(sessionTitle, cc.xy(3, 1));

        JLabel datasetLabel = new JLabel(I18n.get("NewSession.Dataset"));
        pb.add(datasetLabel, cc.xy(1, 3));

        datasetsCB = new JComboBox(datasetModel);
        datasetsCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateTreeModel();
            }
        });
        pb.add(datasetsCB, cc.xy(3, 3));

        JLabel treeLabel = new JLabel(I18n.get("NewSession.Tree"));
        pb.add(treeLabel, cc.xy(1, 5));

        treesCB = new JComboBox(treeModel);
        treesCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateAllowAdd();
            }
        });
        pb.add(treesCB, cc.xy(3, 5));

        JButton okButton = new JButton(addAction);

        JButton manageDatasetsButton = new JButton(manageDatasetsAction);

        pb.add(ButtonBarFactory.buildRightAlignedBar(manageDatasetsButton, okButton), cc.xyw(1, 7, 3));

        JPanel panel = pb.getPanel();
        panel.setBorder(BorderFactory.createTitledBorder(_("NewSession.Title")));

        return panel;
    }

    private String getFreeSessionTitle() {
        String sessionTitleStart = I18n.get("NewSession.NewSessionTitle");
        String sessionTitle = sessionTitleStart;
        int nr = 1;

        while (sessionNames.contains(sessionTitle)) {
            nr++;
            sessionTitle = sessionTitleStart + " (" + nr + ")";
        }

        return sessionTitle;
    }

    private boolean isSessionTitleOk() {
        if (sessionTitle == null)
            return false;

        if (sessionTitle.getText().isEmpty())
            return false;

        if (StringList.containsIgnoreCase(sessionNames, sessionTitle.getText()))
            return false;

        return true;
    }

    /**
     * 
     */
    private void initActions() {
        addAction = new AddAction();
        cancelAction = new CancelAction();
        manageDatasetsAction = new ManageDatasetsAction();
        openAction = new OpenAction();
        removeAction = new RemoveAction();
        renameAction = new RenameAction();
    }

    private class AddAction extends AbstractAction {
        public AddAction() {
            super(I18n.get("NewSession.Add"));
            putValue(Action.SMALL_ICON, Resources.getIcon("plus.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("NewSession.Add"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String title = getNewSessionTitle();
            guiController.createNewSession(profile, getSelectedDataset(), getSelectedTree(), title, getOwner());
            updateSessions();
            sessionNames.add(title);
            SessionInformation newSession = findSessionInformation(title);
            // sessionsModel.addElement(title);
            sessionsList.setSelectedValue(newSession, true);
            sessionTitle.setText(getFreeSessionTitle());
            // updateAllowOpen();
        }
    }

    private SessionInformation findSessionInformation(String sessionTitle) {
        int size = sessionsModel.getSize();
        for (int i = 0; i < size; i++) {
            SessionInformation session = (SessionInformation) sessionsModel.get(i);
            if (sessionTitle.equals(session.getTitle()))
                return session;
        }
        return null;
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super();
            // super(I18n.get("NewSession.Quit"));
            if (getParent() != null) {
                putValue(Action.NAME, _("NewSession.Cancel"));
                putValue(Action.SMALL_ICON, Resources.getIcon("cancel.png"));
                putValue(Action.SHORT_DESCRIPTION, I18n.get("NewSession.Cancel"));
            } else {
                putValue(Action.NAME, _("NewSession.Quit"));
                putValue(Action.SMALL_ICON, Resources.getIcon("exit.png"));
                putValue(Action.SHORT_DESCRIPTION, I18n.get("NewSession.Quit"));
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result = false;
            dispose();
        }
    }

    private class ManageDatasetsAction extends AbstractAction {

        public ManageDatasetsAction() {
            super(I18n.get("NewSession.ManageDatasets"));
            putValue(Action.SMALL_ICON, Resources.getIcon("edit.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("NewSession.ManageDatasets"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            controller.showDatasetAndTreeManager();
            updateDatasetModel(null);
            updateTreeModel();
            updateAllowAdd();
            updateSessions();
            updateAllowSessionActions();
        }
    }

    private class OpenAction extends AbstractAction {
        public OpenAction() {
            super(_("Sessions.Open"));
            putValue(Action.SMALL_ICON, Resources.getIcon("apply.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Sessions.Open"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result = true;
            dispose();
        }
    }

    private class RemoveAction extends AbstractAction {
        public RemoveAction() {
            super(_("Sessions.Remove"));
            putValue(Action.SMALL_ICON, Resources.getIcon("minus.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Sessions.Remove"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            SessionInformation session = (SessionInformation) sessionsList.getSelectedValue();
            int result = JOptionPane.showConfirmDialog(null, _("Sessions.ConfirmDeleteMessage", session.getTitle()),
                    _("Sessions.ConfirmDeleteTitle"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.YES_OPTION) {
                try {
                    db.deleteSession(session);
                } catch (DatabaseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                updateSessions();
            }
        }
    }

    private class RenameAction extends AbstractAction {
        public RenameAction() {
            super(_("Sessions.Rename"));
            putValue(Action.SMALL_ICON, Resources.getIcon("edit.png"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Sessions.Rename"));
        }

        @Override
        public void actionPerformed(ActionEvent arg0) {
            SessionInformation session = (SessionInformation) sessionsList.getSelectedValue();
            String newTitle = JOptionPane.showInputDialog(_("Sessions.RenameMessage"), session.getTitle());
            if (newTitle != null && !newTitle.equalsIgnoreCase(session.getTitle())) {
                if (StringList.containsIgnoreCase(sessionNames, newTitle)) {
                    JOptionPane.showMessageDialog(null, _("Sessions.SessionTitleExisting"));
                } else {
                    session.setTitle(newTitle);
                    try {
                        db.updateSessionTitle(session);
                    } catch (DatabaseException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    updateSessions();
                    updateAllowAdd();
                }
            }
        }
    }

    private void updateAllowAdd() {
        boolean titleOK = isSessionTitleOk();
        addAction.setEnabled(titleOK && treeModel.getSize() > 0);

        if (titleOK) {
            sessionTitle.setBorder(defaultBorder);
        } else {
            sessionTitle.setBorder(BorderFactory.createLineBorder(Color.RED));
        }
    }

    private void updateAllowSessionActions() {
        boolean allow = sessionsList.getSelectedValue() != null;
        openAction.setEnabled(allow);
        renameAction.setEnabled(allow);
        removeAction.setEnabled(allow);
    }
}
