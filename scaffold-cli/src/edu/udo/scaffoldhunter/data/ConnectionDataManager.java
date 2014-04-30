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

package edu.udo.scaffoldhunter.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import edu.udo.scaffoldhunter.ScaffoldHunter;
import edu.udo.scaffoldhunter.util.I18n.Language;

/**
 * Manages the database connections stored on the hard disk.
 * 
 * @author Thorsten Flügel
 * @author Henning Garus
 */
public class ConnectionDataManager {
    private static final String LANGUAGE = "language";
    private static final String CONNECTIONS = "connections";
    private static final String DB_URL = "url";
    private static final String DB_NAME = "dbname";
    private static final String DB_USERNAME = "dbusername";
    private static final String DB_PASSWORD = "dbpassword";
    private static final String DB_TYPE = "dbtype";
    private static final String SELECTION = "selected";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String SAVEPASSWORD = "savepassword";
    private static final String LASTSESSION = "uselastsession";

    private final Preferences preferences;
    private String selectedConnection;
    private Map<String, ConnectionData> connections = new HashMap<String, ConnectionData>();
    private String username;
    private byte[] password;
    private Language language;
    private boolean savePassword;
    private boolean useLastSession;
    private DefaultComboBoxModel model = new DefaultComboBoxModel();

    /**
     * @return the useLastSession
     */
    public boolean getUseLastSession() {
        return useLastSession;
    }

    /**
     * @param useLastSession
     *            the useLastSession to set
     */
    public void setUseLastSession(boolean useLastSession) {
        this.useLastSession = useLastSession;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * @param password
     *            the password to set
     */
    public void setPassword(byte[] password) {
        this.password = password;
    }

    /**
     * @return the language
     */
    public Language getLanguage() {
        return language;
    }

    /**
     * @param language the language to set
     */
    public void setLanguage(Language language) {
        this.language = language;
    }

    /**
     * Creates a ConnectionDataManager containing the connections stored in the
     * preferences.
     */
    public ConnectionDataManager() {
        this.preferences = Preferences.userNodeForPackage(ScaffoldHunter.class);
            load();
        if (language == null) {
            language = Language.getDefault();
        }

        for (String name : Ordering.natural().immutableSortedCopy(connections.keySet())) {
            model.addElement(name);
        }
        model.setSelectedItem(selectedConnection);
        model.addListDataListener(new ListDataListener() {
            @Override
            public void intervalRemoved(ListDataEvent e) {
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
            }

            @Override
            public void contentsChanged(ListDataEvent e) {
                selectedConnection = (String) model.getSelectedItem();
            }
        });
    }

    // private void insertSampleConnections() {
    // ConnectionType type = getConnectionTypes()[0];
    // connections.put("Hibernate Test", new ConnectionData("Hibernate Test",
    // "jdbc:mysql://localhost",
    // "hibernate_test", "hibernate", "temp", type));
    // connections.put("Foo", new ConnectionData("Foo",
    // "jdbc:mysql://192.168.1.1:8794", "foo", "foo", null, type));
    // connections.put("Bla", new ConnectionData("Bla",
    // "jdbc:mysql://localhost:1234", "bla", "bla", null, type));
    // selectedConnection = "Hibernate Test";
    // username = "till2";
    // password = "password";
    // useLastSession = true;
    // }

    /**
     * Loads the connections
     */
    private void load() {
        String langString = preferences.get(LANGUAGE, Language.getDefault().name());
        try {
            language = Language.valueOf(langString);
        } catch (IllegalArgumentException e) {
            language = Language.getDefault();
        }

        Iterable<String> connectionNames = Splitter.on(',').omitEmptyStrings().split(preferences.get(CONNECTIONS, ""));
        for (String connectionName : connectionNames) {
            String typeName = preferences.get(connectionName + '.' + DB_TYPE, ConnectionType.MYSQL.name());
            ConnectionType type = ConnectionType.valueOf(typeName);
            String dbURL = preferences.get(connectionName + '.' + DB_URL, "");
            String dbName = preferences.get(connectionName + '.' + DB_NAME, "");
            String dbUserName = preferences.get(connectionName + '.' + DB_USERNAME, "");
            String dbPassword = preferences.get(connectionName + '.' + DB_PASSWORD, null);
            ConnectionData data = new ConnectionData(connectionName, dbURL, dbName, dbUserName, dbPassword, type);
            connections.put(connectionName, data);
        }
        selectedConnection = preferences.get(SELECTION, "");
        if (connections.get(selectedConnection) == null) {
            selectedConnection = null;
        }
        username = preferences.get(USERNAME, "");
        password = preferences.getByteArray(PASSWORD, null);
        savePassword = preferences.getBoolean(SAVEPASSWORD, false);
        useLastSession = preferences.getBoolean(LASTSESSION, false);
    }

    /**
     * Stores the connections
     */
    public void save() {
        preferences.put(LANGUAGE, language.name());

        List<String> connectionNames = Lists.newArrayList();
        for (ConnectionData data : connections.values()) {
            String connectionName = data.getConnectionName().replace(",", "");
            if (connectionNames.contains(connectionName)) {
                continue;
            }
            connectionNames.add(connectionName);
            
            preferences.put(connectionName + '.' + DB_URL, data.getDbUrl());
            preferences.put(connectionName + '.' + DB_NAME, data.getDbName());
            preferences.put(connectionName + '.' + DB_USERNAME, data.getDbUsername());
            // do not add a password element if no password has been set.
            // otherwise "" will be the password after loading
            if (data.getDbPassword() != null) {
                preferences.put(connectionName + '.' + DB_PASSWORD, data.getDbPassword());
            }
            if (data.getDbType() != null)
                preferences.put(connectionName + '.' + DB_TYPE, data.getDbType().name());
        }
        preferences.put(CONNECTIONS, Joiner.on(',').join(connectionNames));
        if (selectedConnection != null) {
            preferences.put(SELECTION, selectedConnection.replace(",", ""));
        }
        if (username != null) {
            preferences.put(USERNAME, username);
        }
        if (password != null) {
            preferences.putByteArray(PASSWORD, password);
        } else {
            preferences.remove(PASSWORD);
        }
        preferences.putBoolean(SAVEPASSWORD, savePassword);
        preferences.putBoolean(LASTSESSION, useLastSession);
    }

    /** 
     * @return the names of all connections
     */
    public List<ConnectionData> getConnections() {
        return ImmutableList.copyOf(connections.values());
    }

    /**
     * @return the model representing the connection names.
     */
    public ComboBoxModel getNamesModel() {
        return model;
    }

    /**
     * @param name
     * @return connection with the passed name, null if there is no connection
     *         with that name
     */
    public ConnectionData getConnection(String name) {
        return connections.get(name);
    }

    /**
     * @param data
     * @return true if the method succeeded. false if there already was a
     *         connection with the same name as in data.
     */
    public boolean addConnection(ConnectionData data) {
        // check if there already is an entry with this name
        if (getConnection(data.getConnectionName()) != null) {
            return false;
        }
        connections.put(data.getConnectionName(), data);
        model.addElement(data.getConnectionName());
        model.setSelectedItem(data.getConnectionName());
        return true;
    }

    /**
     * @param name
     *            name of the connection that will be removed
     */
    public void removeConnection(String name) {
        connections.remove(name);
        name = name.replace(",", "");
        preferences.remove(name + '.' + DB_TYPE);
        preferences.remove(name + '.' + DB_NAME);
        preferences.remove(name + '.' + DB_URL);
        preferences.remove(name + '.' + DB_USERNAME);
        preferences.remove(name + '.' + DB_PASSWORD);
        model.removeElement(name);
        model.setSelectedItem(model.getElementAt(0));
    }

    /**
     * Removes the old data and inserts the new one.
     * 
     * @param oldName
     *            name of the old connection
     * @param data
     *            the new connection
     * @return true if the method succeeded. false if there already was a
     *         connection with the same name as in data.
     */
    public boolean changeConnection(String oldName, ConnectionData data) {
        if (!oldName.equals(data.getConnectionName()) && getConnection(data.getConnectionName()) != null) {
            return false;
        }
        removeConnection(oldName);
        return addConnection(data);
    }

    /**
     * @param savePassword the savePassword to set
     */
    public void setSavePassword(boolean savePassword) {
        this.savePassword = savePassword;
    }

    /**
     * @return the savePassword
     */
    public boolean getSavePassword() {
        return savePassword;
    }

}
