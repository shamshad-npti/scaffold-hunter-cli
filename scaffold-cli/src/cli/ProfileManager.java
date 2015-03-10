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
import edu.udo.scaffoldhunter.cli.reader.BooleanReader;
import edu.udo.scaffoldhunter.cli.reader.PasswordReader;
import edu.udo.scaffoldhunter.cli.reader.StringReader;
import edu.udo.scaffoldhunter.data.ConnectionDataManager;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;

/**
 * It manages login action for a profile
 * 
 * @author Shamshad Alam
 * 
 */
public class ProfileManager {
    private DbManager db;
    private PasswordReader passwordReader;
    private StringReader usernameReader;
    private BooleanReader confirmer;
    private Profile profile;

    /**
     * @param db
     *            {@code DbManager} to read profile from
     */
    public ProfileManager(DbManager db) {
        this.db = db;
        passwordReader = new PasswordReader(_("CLI.ProfileManager.passwordPrompt"));
        usernameReader = new StringReader(_("CLI.ProfileManager.usernamePrompt"));
        confirmer = new BooleanReader("");
    }

    /**
     * login to scaffold hunter with specified username and password. If profle
     * doesn't exist with specified user name user is prompted to enter profile
     * name. If password mismatch with the profile saved password user has to
     * enter password again.
     * 
     * @param username
     *            username
     * @param password
     *            password of the user
     * @return true if login successful
     */
    public boolean login(final String username, final String password) {
        Profile profile = selectProfile(username);

        // if profile is selected
        if (profile != null) {
            ConnectionDataManager dm = CommandManager.getInstance().getDataManager();
            if (profile.getUsername().equalsIgnoreCase(dm.getUsername())) {
                return login(profile, dm.getPassword());
            }
            return login(profile, password != null ? password.getBytes() : null);
        }

        return false;
    }

    /**
     * try login with username and prompt user for password
     * 
     * @param username
     *            username
     * @return true if log in successful
     * @see #login(String, String)
     */
    public boolean login(String username) {
        return login(username, null);
    }

    /**
     * try to login with saved username and password if there isn't saved
     * username or password user is prompted to enter the username and password
     * 
     * @return true if login successful
     */
    public boolean login() {
        // get connection data manager
        ConnectionDataManager dataManager = CommandManager.getInstance().getDataManager();

        // read username from connection data
        String username = dataManager.getUsername();

        // get the profile with the username
        Profile profile = getProfileInternal(username);

        // check whether profile exists (ie not null)
        if (profile != null) {

            confirmer.setPrePromptMessage(_("CLI.ProfileManager.confirmUsernamePrePrompt"));
            confirmer.setPromptMessage(_("CLI.ProfileManager.confirmUsernamePrompt", profile.getUsername()));

            // ask user to login with saved username
            if (confirmer.read()) {
                // check whether password is saved
                return login(profile, dataManager.getPassword());
                /*
                 * if (dataManager.getSavePassword()) { } else { return
                 * login(profile, null); }
                 */
            }
        }

        return login(selectProfile(null), null);
    }

    /**
     * get the profile of current logged in user
     * 
     * @return current profile
     */
    public Profile getProfile() {
        return profile;
    }

    /**
     * @param profile
     *            to check login
     * @param password
     *            password of the user
     * @return login status, true - login successful
     */
    private boolean login(Profile profile, byte[] password) {
        boolean login = true;
        if (password == null || !profile.checkPassword(password)) {
            char[] cp = null;
            // check whether password is specified

            if (password == null) {
                cp = passwordReader.read();
            }

            confirmer.setPrePromptMessage(_("CLI.ProfileManager.loginFail"));
            confirmer.setPromptMessage(_("CLI.ProfileManager.reenterPassword"));

            while (!profile.checkPassword(new String(cp))) {
                if (confirmer.read()) {
                    // re-read the password
                    cp = passwordReader.read();
                } else {
                    // login is cancel by user
                    login = false;
                    break;
                }
            }
        }
        // check whether login is successful
        if (login) {
            this.profile = profile;
            CLIUtil.show(_("CLI.ProfileManager.loginSuccess"));
            CLIUtil.separator();
        }

        return login;
    }

    /**
     * Get the profile with specified name. If profile with specified name
     * doesn't exist prompt user to enter new name if he wants so.
     * 
     * @param username
     *            username of the user
     * @return profile with specified or selected name or null if profile is not
     *         found
     */
    private Profile selectProfile(String username) {
        // check whethet username is specified
        if (username == null) {
            username = usernameReader.read();
        }

        // get the profile with the user
        Profile profile = getProfileInternal(username);
        confirmer.setPromptMessage(_("CLI.ProfileManager.confirmReenterUserName"));

        // try to get profile till profile is null and user select yes option
        while (profile == null && confirmer.read()) {
            username = usernameReader.read();
            profile = getProfileInternal(username);
            confirmer.setPrePromptMessage(_("CLI.ProfileManager.profileNotFound", username));
        }

        return profile;
    }

    /**
     * get profile by username
     * 
     * @param username
     *            username
     * @return profile with specified name or null
     */
    private Profile getProfileInternal(final String username) {
        if (username == null) {
            return null;
        }

        Profile profile = DBExceptionHandler.callDBManager(db, new DBFunction<Profile>() {
            @Override
            public Profile call() throws DatabaseException {
                try {
                    return db.getProfile(username);
                } catch (Exception ex) {
                    CLIUtil.showError(ex.getMessage());
                    return null;
                }
            }
        });

        return profile;
    }
}