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

package edu.udo.scaffoldhunter.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides static access to the language bundles.
 */
public class I18n {

    private static final Logger logger = LoggerFactory.getLogger(I18n.class);

    private static ResourceBundle messages = null;
    private static ResourceBundle defaultMessages = Language.en.getMessageBundle();

    /**
     * Initializes the localization support.
     * 
     * @param language
     *            the language to use for localization
     */
    public static void initialize(Language language) {
        messages = language.getMessageBundle();
    }

    /**
     * Returns the translated string for the given key according to locale set
     * by the user.
     * 
     * @param key
     *            The key whithin the locale bundle
     * @return The translated String from the locale bundle
     */
    public static String get(String key) {
        try {
            return messages.getString(key);
        } catch (MissingResourceException ex) {
            try {
                return defaultMessages.getString(key);

            } catch (MissingResourceException ex2) {
                logger.error("key '{}' missing from resource bundle", key);
                return key;
            }
        }
    }

    /**
     * Retrieves the translated string for the given key according to locale set
     * by the user and then returns the result of
     * {@link MessageFormat#format(String, Object...)} called with the string
     * and the given arguments.
     * 
     * @param key
     *            The key within the locale bundle
     * @param arguments
     *            arguments to be formatted by the retrieved string
     * @return The result of formatting the supplied arguments with the
     *         translated string.
     * 
     * @see MessageFormat
     */
    public static String get(String key, Object... arguments) {
        try {
            return MessageFormat.format(messages.getString(key), arguments);
        } catch (MissingResourceException ex) {
            logger.error("key '{}' missing from resource bundle", key);
            return key;
        }
    }

    /**
     * Returns the translated string for the given key according to locale set
     * by the user. This is identical to get(), but has a name that's more
     * convenient to use with static imports (and will be familiar to anyone
     * used to gettext...)
     * 
     * @param key
     *            The key whithin the locale bundle
     * @return The translated String from the locale bundle
     */
    public static String _(String key) {
        return get(key);
    }

    /**
     * Retrieves the translated string for the given key according to locale set
     * by the user and then returns the result of
     * {@link MessageFormat#format(String, Object...)} called with the string
     * and the given arguments.
     * 
     * @param key
     *            The key within the locale bundle
     * @param arguments
     *            arguments to be formatted by the retrieved string
     * @return The result of formatting the supplied arguments with the
     *         translated string.
     * 
     * @see MessageFormat
     */
    public static String _(String key, Object... arguments) {
        return get(key, arguments);
    }

    /**
     * Languages used to determine which message bundle is used for I18n
     * 
     * @author Henning Garus
     */
    public enum Language {
        /** German */
        de("Messages_German"),
        /** English */
        en("Messages_English");

        private final String messageBundleName;

        private Language(String messageBundleName) {
            this.messageBundleName = messageBundleName;
        }

        private ResourceBundle getMessageBundle() {
            return ResourceBundle.getBundle("edu.udo.scaffoldhunter.resources.i18n." + messageBundleName);
        }

        /**
         * Tries to determine the system default language, if there is no
         * message bundle for that language falls back to English.
         * 
         * @return the language to be used if none is selected
         */
        public static Language getDefault() {
            try {
                return valueOf(Locale.getDefault().getLanguage());
            } catch (IllegalArgumentException e) {
                return en;
            }
        }
    }

}
