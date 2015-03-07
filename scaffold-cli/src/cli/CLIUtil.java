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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import com.beust.jcommander.internal.Lists;

import edu.udo.scaffoldhunter.cli.reader.OptionReader;
/**
 * Contains static CLI utility methods
 * 
 * @author Shamshad Alam
 * 
 */
public class CLIUtil {
    private static Scanner scanner = new Scanner(System.in);
    private static final String NEW_LINE = "\n";

    /**
     * @param message
     *            to display
     * @return true if user enters y
     */
    public static boolean confirm(String message) {
        out(NEW_LINE, message + " (Y/N): ");

        String response = scanner.nextLine();
        return response != null && response.trim().toUpperCase().equals("Y");
    }

    /**
     * @param message
     *            to display
     * @param defaultValue
     *            defaultValue
     * @return true if user enters y, false if user enter n and all other cases
     *         it returns defaultValue
     */
    public static boolean confirm(String message, boolean defaultValue) {
        out(NEW_LINE, message + (defaultValue ? " [Y] " : " [N] ") + "(Y/N): ");
        String response = scanner.nextLine();
        return response == null ? defaultValue : response.trim().toUpperCase().equals("Y") ? true : response.trim()
                .equalsIgnoreCase("N") ? false : defaultValue;
    }

    /**
     * @param message
     *            to display
     * @return Command line input string
     */
    public static String input(String message) {
        out(NEW_LINE, message + ": ");
        return scanner.nextLine();
    }

    /**
     * Display separator on Command prompt. A separator is a string of fifty
     * contiguous '-' character
     */
    public static void separator() {
        out(NEW_LINE, "---------------------------------------------------");
    }

    /**
     * Display a line of dash on screen half of {@link #separator()} in size
     */
    public static void halfSeparator() {
        out(NEW_LINE, "--------------------------");
    }

    /**
     * @param message
     *            to display
     * @param prefix
     *            a short string such as '[Warning]:' for warning MessagePrefix
     *            is prepended
     */
    public static void show(final String message, final MessagePrefix prefix) {
        switch (prefix) {
        case ERROR:
            showError(message);
            break;
        case MESSAGE:
            showMessage(message);
            break;
        case WARNING:
            showWarning(message);
            break;
        case NONE:
            out(NEW_LINE, message);
        }
    }

    /**
     * @param message
     *            to display without any prefix
     */
    public static void show(String message) {
        show(message, MessagePrefix.NONE);
    }

    /**
     * Display the message formatted by {@code String.format()}
     * 
     * @param format
     * @param args
     */
    public static void show(String format, Object... args) {
        CLIUtil.show(String.format(format, args));
    }

    /**
     * 
     * @param message
     *            to display with '[Message]:' prefix
     */
    public static void showMessage(String message) {
        out(NEW_LINE, MessagePrefix.MESSAGE + message);
    }

    /**
     * @param error
     *            Error message to display with '[Error]:' prefix
     */
    public static void showError(String error) {
        out(NEW_LINE, MessagePrefix.ERROR + error);
    }

    /**
     * @param warning
     *            message to display with '[Warning]:' prefix
     */
    public static void showWarning(String warning) {
        out(NEW_LINE, MessagePrefix.WARNING + warning);
    }

    /**
     * Display a formatted long message in multiple line. Each line contains
     * number of characters equal to or less than {@code charPerLine}.
     * {@code paddingLeft} indicates the number of spaces prepended to each
     * line. And final {@code padFirstLine} tells whether the first line is to
     * be padded
     * 
     * @param longMessage
     *            a long message to display over command line
     * @param charPerLine
     *            maximum number of character per line
     * @param paddingLeft
     *            number of spaces prepended to each line
     * @param padFirstLine
     *            if true first line is also prepended
     * 
     */
    public static void show(String longMessage, int charPerLine, int paddingLeft, boolean padFirstLine) {
        show(format(longMessage, charPerLine, paddingLeft, padFirstLine));
    }

    /**
     * Format long message in multiple line in such a way that each line does
     * not contain more {@code charPerLine}. Each line contains number of
     * characters equal to or less than {@code charPerLine}. {@code paddingLeft}
     * indicates the number of spaces prepended to each line. And final
     * {@code padFirstLine} tells whether the first line is to be padded
     * 
     * @param longMessage
     *            a long message to display over command line
     * @param charPerLine
     *            maximum number of character per line
     * @param paddingLeft
     *            number of spaces prepended to each line
     * @param padFirstLine
     *            if true first line is also prepended
     * @return multi-line formatted message
     */
    public static String format(String longMessage, int charPerLine, int paddingLeft, boolean padFirstLine) {
        char[] padChar = new char[paddingLeft];
        longMessage = longMessage.replaceAll("<[/]*[a-zA-Z]+>", "");
        Arrays.fill(padChar, ' ');

        StringBuilder builder = new StringBuilder();

        if (padFirstLine) {
            builder.append(padChar);
        }

        String[] parts = longMessage.split(" ");
        int processed = 0;
        for (String message : parts) {
            if (processed >= charPerLine) {
                builder.append(NEW_LINE).append(padChar).append(message).append(' ');
                processed = message.length() + 1;
            } else {
                builder.append(message).append(' ');
                processed += message.length() + 1;
            }
        }
        return builder.toString();
    }

    /**
     * This method has been deprecated use {@link OptionReader} instead of this method
     * 
     * @param message
     *            to display before prompt
     * @param options
     *            for the prompt
     * @return index selected option or -1 if no option is selected or error
     *         occurs
     */
    @Deprecated
    public static int options(String message, String... options) {
        return options(null, message, options);
    }

    /**
     * This method has been deprecated use {@link OptionReader} instead of this method.
     * @param preMessage
     *            to display before prompt
     * @param promptMessage
     * @param options
     *            for the prompt
     * @return index selected option or -1 if no option is selected or error
     *         occurs
     */
    @Deprecated
    public static int options(String preMessage, String promptMessage, String[] options) {
        OptionController<Object> controller = new OptionController<Object>(options);
        controller.prompt(preMessage, promptMessage);
        return controller.parseInput(scanner.nextLine());
    }

    /**
     * 
     * @param message
     *            to display before prompt
     * @param options
     *            for the prompt
     * @param stringConverter
     * @return index of selected option or -1 if no option is selected or error
     *         occurs
     */
    public static <T> int options(String message, Collection<T> options, StringConverter<T> stringConverter) {
        return options(null, message, options, stringConverter);
    }

    /**
     * This method has been deprecated use {@link OptionReader} instead of this method.     * 
     * @param preMessage
     *            message to be displayed before the option
     * @param propmtMessage
     *            message after option
     * @param options
     *            list of options
     * @param stringConverter
     *            a converter to convert object into string
     * @return index of selected option or -1 if no option is selected or error
     *         occurs
     */
    @Deprecated
    public static <T> int options(String preMessage, String propmtMessage, Collection<T> options,
            StringConverter<T> stringConverter) {
        OptionController<T> controller = new OptionController<T>(options, stringConverter);
        controller.prompt(preMessage, propmtMessage);
        return controller.parseInput(scanner.nextLine());
    }

    /**
     * Display all message to console
     * 
     * @param messages
     */
    public static void out(String... messages) {
        for (String message : messages) {
            System.out.print(message);
        }
    }

    /**
     * Prepare and holds options
     * 
     * @author Shamshad Alam
     * 
     * @param <T>
     */
    private static class OptionController<T> {
        private List<Option> options = Lists.newArrayList();

        public OptionController(Collection<T> data, StringConverter<T> stringConverter) {
            int i = 0;
            for (T object : data) {
                String text = stringConverter.toString(object);
                addOption(text, text, i++, "" + i);
            }
        }

        public OptionController(String... options) {
            this.options = Lists.newArrayList();
            int i = 0;
            for (String option : options) {
                addOption(option, i++);
            }
        }

        private void addOption(String option, int index) {
            int i = option.indexOf("&");
            String mn;
            String displayString;
            if (i != -1) {
                String left = option.substring(0, i);
                String right = option.substring(i + 1);
                mn = "" + right.charAt(0);
                displayString = left + right;
            } else {
                mn = "" + option.charAt(0);
                displayString = option;
            }
            addOption(displayString, option, index, mn);
        }

        private void addOption(String displayString, String option, int index, String mnemonics) {
            this.options.add(new Option(displayString, option, index, mnemonics));
        }

        /**
         * @param option
         *            to parse
         * @return index of selected option or -1 if input does not belongs to
         *         any option
         */
        public int parseInput(String option) {
            if (option != null) {
                for (Option op : options) {
                    if (option.equalsIgnoreCase(op.mnemonics) || option.equalsIgnoreCase(op.displayString)) {
                        return op.index;
                    }
                }
            }
            return -1;
        }

        /**
         * 
         * @param message
         */
        @SuppressWarnings("unused")
        public void prompt(String message) {
            prompt(null, message);
        }

        public void prompt(String preMessage, String message) {
            out(NEW_LINE);
            if (preMessage != null) {
                out(preMessage);
            }
            int size = options.size();
            for (int i = 0; i < size - 1; i++) {
                out(options.get(i).toString(), NEW_LINE);
            }
            out(options.get(size - 1).toString(), NEW_LINE, message + " : ");
        }
    }

    /**
     * An interface used by options() method to convert object into string
     * 
     * @author Shamshad Alam
     * 
     * @param <T>
     */
    public static interface StringConverter<T> {

        /**
         * @param obj
         * @return human readable string representation of object
         */
        public String toString(T obj);
    }

    /**
     * holds option object
     * 
     * @author Shamshad Alam
     * 
     */
    private static class Option {
        @SuppressWarnings("unused")
        private String option;
        private String mnemonics;
        private int index;
        private String displayString;

        public Option(String displayString, String option, int index, String mnemonics) {
            this.option = option;
            this.displayString = displayString;
            this.index = index;
            this.mnemonics = mnemonics;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "  " + this.mnemonics + " - " + this.displayString;
        }
    }

    /**
     * Enum for message prefix There are four prefix available
     * <ul>
     * <li>[Message]:</li>
     * <li>[Error]:</li>
     * <li>[Warning]:
     * <li>
     * <li><no-prefix></li>
     * </ul>
     * 
     * @author Shamshad Alam
     * 
     */
    public static enum MessagePrefix {
        /**
         * Prefixed by [Error]:
         */
        ERROR("error"),
        /**
         * Prefixed by [Message]:
         */
        MESSAGE("message"),
        /**
         * Prefixed by [Warning]:
         */
        WARNING("warning"),
        /**
         * No prefix
         */
        NONE("none");

        private String prefix;

        private MessagePrefix(String prefix) {
            this.prefix = prefix;
        }

        /**
         * @return the prefix
         */
        public String getPrefix() {
            return prefix;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return _("CLI.Message.Label." + prefix);
        }
    }
}
