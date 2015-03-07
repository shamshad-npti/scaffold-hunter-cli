/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.cli;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.util.List;

import edu.udo.scaffoldhunter.cli.reader.BooleanReader;
import edu.udo.scaffoldhunter.cli.reader.DefaultOptionModel;
import edu.udo.scaffoldhunter.cli.reader.OptionReader;
import edu.udo.scaffoldhunter.cli.reader.PasswordReader;
import edu.udo.scaffoldhunter.data.ConnectionData;
import edu.udo.scaffoldhunter.data.ConnectionDataManager;
import edu.udo.scaffoldhunter.data.ConnectionType;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.DbManagerHibernate;

/**
 * Connect to database and manage connection during database session in CLI.
 * Connection is established by {@link #connect(String, boolean)} method and
 * thereafter, if connection is established successfully,
 * {@link #getDbManager()} returns a
 * {@link edu.udo.scaffoldhunter.model.db.DbManager} otherwise it returns
 * <code>null</code>.
 * 
 * @author Shamshad Alam
 * 
 */
public class CLIConnectionManager {
    private static DbManager db;
    private static String connectionName;

    /**
     * get the {@code DbManager} to which you are currently connected. Use
     * {@link #connect(String, boolean)} method to connect to database
     * 
     * @return <code>DbManager</code> object which may be null if you are not
     *         connected to database
     */
    public static DbManager getDbManager() {
        return db;
    }

    /**
     * Try to establish connection with database using connection data specified
     * by connection name. If password is not saved in connection data, user
     * will be prompted to enter the password and if database schema is not
     * valid or does not exist user and parameter
     * <code>promptCreateSchema</code> is true, schema will be recreated after
     * confirmation from user.
     * 
     * @param connectionName
     * @param promptCreateSchema
     * @return true if connected successfully
     */
    public static boolean connect(String connectionName, boolean promptCreateSchema) {

        if (isConnectedTo(connectionName)) {
            return true;
        }

        // create ConnectionDataManager object
        ConnectionDataManager connectionDataManager = CommandManager.getInstance().getDataManager();

        // get requested connection data by name
        ConnectionData connData = connectionDataManager.getConnection(connectionName);

        /*
         * if connection data does not exists with specified name display
         * options input prompt after confirmation from the user
         */
        BooleanReader confirm = new BooleanReader(_("CLI.ConnectionManager.SelectConnection.confirm"));
        confirm.setPrePromptMessage(_("CLI.ConnectionManager.SelectConnection.preConfirm"));

        if (connData == null && confirm.read()) {
            final List<ConnectionData> conns = connectionDataManager.getConnections();
            OptionReader<ConnectionData> optionReader = new OptionReader<ConnectionData>(
                    new DefaultOptionModel<ConnectionData>(conns), null, true, _("CLI.ConnectionManager.SelectConnection.prompt"));
            optionReader.setQuitable(true);
            connData = optionReader.read();
        }

        if (connData != null) {
            char[] password = null;

            // prompt use to enter password if it has not saved in connection
            // data
            if (connData.getDbPassword() == null) {
                PasswordReader passwordReader = new PasswordReader(_("CLI.ConnectionManager.passwordPrompt"));
                password = passwordReader.read();
            }

            ConnectionType connectionType = connData.getDbType();
            try {
                CLIUtil.show(_("CLI.ConnectionManager.connecting"));
                // create a DbManager object
                db = new DbManagerHibernate(connectionType.getDriverClass(), connectionType.getHibernateDialect(),
                        connData.getDbUrl(), connData.getDbName(), connData.getDbUsername(), password == null ? ""
                                : new String(password), true, false);
                // test whether schema exists and it is a valid schema
                if (!db.schemaExists() || !db.validateSchema()) {
                    if (promptCreateSchema) {
                        // create schema
                        return createSchema();
                    } else {
                        CLIUtil.showError(!db.schemaExists() ? _("CLI.ConnectionManager.schemaNotFound")
                                : _("CLI.ConnectionManager.invalidShema"));
                    }
                }
                CLIUtil.show(_("CLI.ConnectionManager.connectionSuccessful"));
                CLIConnectionManager.connectionName = connectionName;
            } catch (DatabaseException e) {
                CLIUtil.showError(e.getMessage());
                return false;
            }
        } else {
            CLIUtil.showError(_("CLI.ConnectionManager.improperData"));
        }
        return db != null;
    }

    /**
     * Recreate database schema after confirmation if schema doesn't exists
     * 
     * @return true if schema is created successfully
     * @throws DatabaseException
     */
    private static boolean createSchema() throws DatabaseException {
        if (CLIUtil.confirm(_("CLI.ConnectionManager.recreateShema"))) {
            db.createAndExportSchema();
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the current connectionName to which {@code DbManager} is
     *         connected
     */
    public static String getConnectionName() {
        return connectionName;
    }

    /**
     * @param connectionName
     * @return true if connected to the database which connection detail is
     *         identified by connection name
     */
    public static boolean isConnectedTo(String connectionName) {
        return db != null && CLIConnectionManager.connectionName.equalsIgnoreCase(connectionName);
    }
}
