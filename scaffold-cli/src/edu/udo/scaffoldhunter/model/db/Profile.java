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

package edu.udo.scaffoldhunter.model.db;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * 
 */
public class Profile extends DbObject {
    private String username;
    private byte[] password;
    private byte[] salt;
    private String configData;
    private Session currentSession;
    private Set<Preset> presets;
    private Set<BookmarkFolder> bookmarkFolders;

    /**
     * @param username
     * @param password
     * @param currentSession
     * @param presets
     * @param bookmarkFolders
     * @param configData 
     */
    public Profile(String username, String password, Session currentSession, Set<Preset> presets,
            Set<BookmarkFolder> bookmarkFolders, String configData) {
        super();
        this.username = username;
        this.setPasswordEncrypted(password);
        this.currentSession = currentSession;
        this.presets = presets;
        this.bookmarkFolders = bookmarkFolders;
        this.configData = configData;
        generateSalt();
    }

    /**
     * @param password
     */
    public void setPasswordEncrypted(String password) {
        this.password = encryptPassword(password);
    }
    
    private byte[] encryptPassword(String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            // should never happen
        }
        byte[] pw = password.getBytes();
        byte[] saltAndPw = new byte[salt.length + pw.length];
        System.arraycopy(salt, 0, saltAndPw, 0, salt.length);
        System.arraycopy(pw, 0, saltAndPw, salt.length, pw.length);
        if (md != null)
            for (int i = 0; i < 5; i++)
                saltAndPw = md.digest(saltAndPw);
        return saltAndPw;
    }
    
    /**
     * @param password The unencrypted password to check
     * @return true, if the password is equal to the password in database
     */
    public boolean checkPassword(String password) {
        byte[] checkPassword = encryptPassword(password);
        
        return Arrays.equals(checkPassword, this.password);
    }
    
    /**
     * @param password The encrypted password to check
     * @return true, if the password is equal to the password in database
     */
    public boolean checkPassword(byte[] password) {
        return Arrays.equals(password, this.password);
    }

    /**
     * default constructor
     */
    public Profile() {
        this.presets = new HashSet<Preset>();
        this.bookmarkFolders = new HashSet<BookmarkFolder>();
        generateSalt();
    }
    
    private void generateSalt() {
        Random r = new SecureRandom();
        byte[] salt = new byte[20];
        r.nextBytes(salt);
        this.salt = salt;
    }

    /**
     * @return the salt
     */
    byte[] getSalt() {
        return salt;
    }

    /**
     * @param salt the salt to set
     */
    void setSalt(byte[] salt) {
        this.salt = salt;
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
    void setPassword(byte[] password) {
        this.password = password;
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
     * Attention: Lazy Property. Use DbManager.loadCurrentSession() or
     * DbManager.getSession() to load a {@link Session}
     * 
     * @return the currentSession
     */
    public Session getCurrentSession() {
        return currentSession;
    }

    /**
     * @param currentSession
     *            the currentSession to set
     */
    public void setCurrentSession(Session currentSession) {
        this.currentSession = currentSession;
    }

    /**
     * @return the presets
     */
    public Set<Preset> getPresets() {
        return presets;
    }

    /**
     * @param presets
     *            the presets to set
     */
    public void setPresets(Set<Preset> presets) {
        this.presets = presets;
    }

    /**
     * @return the bookmarkFolders
     */
    public Set<BookmarkFolder> getBookmarkFolders() {
        return bookmarkFolders;
    }

    /**
     * @param bookmarkFolders
     *            the bookmarkFolders to set
     */
    public void setBookmarkFolders(Set<BookmarkFolder> bookmarkFolders) {
        this.bookmarkFolders = bookmarkFolders;
    }

    /**
     * @param configData the configData to set
     */
    public void setConfigData(String configData) {
        this.configData = configData;
    }

    /**
     * @return the globalConfig
     */
    public String getConfigData() {
        return configData;
    }
}
