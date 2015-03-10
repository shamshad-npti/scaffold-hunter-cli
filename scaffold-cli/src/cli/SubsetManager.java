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
import java.util.List;
import java.util.Stack;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;

import edu.udo.scaffoldhunter.cli.reader.BooleanReader;
import edu.udo.scaffoldhunter.cli.reader.DefaultOptionModel;
import edu.udo.scaffoldhunter.cli.reader.OptionReader;
import edu.udo.scaffoldhunter.cli.reader.StringReader;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Subsets;
/**
 * 
 * A class which allows user to get and select subset by title, get list of
 * available subset in a particular root subset.
 * 
 * @author Shamshad Alam
 * 
 */
public class SubsetManager {

    private DbManager db;
    private Profile profile;
    private OptionReader<String> sessionReader;
    private OptionReader<Subset> subsetReader;
    private StringReader subsetNameReader;
    private BooleanReader confirmer;

    /**
     * Create a {@code SubsetManager} object
     * 
     * @param db
     *            {@code DbManager} to perform search
     * @param profile
     *            {@code Profile} to while required to retrieve session
     * @throws NullPointerException
     *             if {@code DbManager}, {@code Profile} or both are null
     */
    public SubsetManager(DbManager db, Profile profile) {
        checkDbAndProfile(db, profile);

        this.db = db;
        this.profile = profile;

        this.sessionReader = new OptionReader<String>(new DefaultOptionModel<String>(getAllSessionTitles()),
                _("CLI.SubsetManager.sessionPrompt"));
        this.sessionReader.setPrePromptMessage(_("CLI.SubsetManager.sessionPrePrompt"));
        this.sessionReader.setRequired(true);
        this.sessionReader.setQuitable(true);

        this.subsetReader = new OptionReader<Subset>(null, _("CLI.SubsetManager.selectSubsetPrompt")) {
            @Override
            protected String getAsString(Subset value) {
                return SubsetManager.toString(value);
            }
        };

        this.subsetReader.setRequired(true);
        this.subsetReader.setQuitable(true);

        this.subsetNameReader = new StringReader(_("CLI.SubsetManager.enterSubsetNamePrompt"));
        this.confirmer = new BooleanReader("");
    }

    /**
     * 
     * @return all available session titles in current profile
     */
    private List<String> getAllSessionTitles() {
        try {
            return db.getAllSessionTitles(profile);
        } catch (Exception ex) {
            return Lists.newArrayList();
        }
    }

    /**
     * Prompt user to select a session from available list of the session
     * 
     * @return selected session or null if no session is selected
     */
    private Session selectSession() {
        String title = sessionReader.read();
        try {
            return db.getSession(profile, title);
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * This method allows user to select subset by title
     * 
     * @param title
     *            title of the subset
     * @return the subset by title if exists otherwise it prompt user to select
     *         subset
     */
    public Subset selectSubset(String title) {

        Session session = selectSession();

        if (session == null) {
            return null;
        }

        return selectSubset(session, title);
    }

    /**
     * 
     * @param session
     *            to get root subset
     * @param title
     *            title of the subset
     * @return subset by title exists in the session
     * 
     */
    public Subset selectSubset(Session session, String title) {
        Preconditions.checkNotNull(session, "Session is required to select the subset");

        return selectSubset(session.getSubset(), title);
    }

    /**
     * Search for a
     * 
     * @param subtree
     *            subtree to search in
     * @param title
     *            title of the subset
     * @return selected subset which is either root or its children or null
     */
    public Subset selectSubset(Subset subtree, String title) {

        if (title == null) {
            title = readSubsetName();
        }

        Iterable<Subset> unfilteredSubsets = Subsets.getSubsetTreeIterable(subtree);
        List<Subset> orderedList = null;

        if (title != null) {
            Iterable<Subset> filteredSubsets = Iterables.filter(unfilteredSubsets, new StartWithTitlePredicate(title));

            orderedList = CASE_INSENSITIVE_TITLE_ORDERING.immutableSortedCopy(filteredSubsets);

            Iterable<Subset> exactMatchSubsets = Iterables
                    .filter(orderedList, new TitleLengthPredicate(title.length()));

            List<Subset> exactMatch = Lists.newLinkedList(exactMatchSubsets);

            if (exactMatch.size() == 1) {
                // unique match found
                return exactMatch.get(0);
            }

            if (orderedList.isEmpty()) {

                confirmer.setPrePromptMessage(_("CLI.SubsetManager.confirmSelectPrePrompt", title));
                confirmer.setPromptMessage(_("CLI.SubsetManager.confirmSelectPrompt"));

                if (!confirmer.read()) {
                    return null;
                }

                orderedList = CASE_INSENSITIVE_TITLE_ORDERING.immutableSortedCopy(unfilteredSubsets);
            }

        } else {

            orderedList = CASE_INSENSITIVE_TITLE_ORDERING.immutableSortedCopy(unfilteredSubsets);

            if (orderedList.size() > 100) {

                confirmer.setPrePromptMessage(_("CLI.SubsetManager.confirmSelectOnNullPrePrompt"));
                confirmer.setPromptMessage(_("CLI.SubsetManager.confirmSelectOnNullPrompt"));

                if (!confirmer.read()) {
                    return null;
                }
            }
        }

        this.subsetReader.setModel(new DefaultOptionModel<Subset>(orderedList));

        return subsetReader.read();
    }

    /**
     * @param subset
     *            which size is to be known
     * @return number of molecules in the subset or zero if either subset is
     *         empty or {@code DatabaseException} is thrown
     * @throws NullPointerException
     */
    public int subsetSize(Subset subset) {
        Preconditions.checkNotNull(subset, "Subset which size is to be known is null");
        try {
            return db.getFilteredSubsetSize(subset, null);
        } catch (DatabaseException e) {
            return 0;
        }
    }

    /**
     * Select a subset by title in database and session selected by user
     * 
     * @param db
     *            {@code DbManager} for the database
     * @param profile
     *            {@code Profile} to retrieve session
     * @param title
     *            title of the subset that can be null
     * @return subset which exist in db identified by title or selected by user
     *         if either no subset exist or multiple subsets exist with the same
     *         title
     */
    public static Subset selectSubset(DbManager db, Profile profile, String title) {
        return new SubsetManager(db, profile).selectSubset(title);
    }

    private static void checkDbAndProfile(DbManager db, Profile profile) {
        Preconditions.checkNotNull(db, "DbManager is required to retrieve/manipulate subset");
        Preconditions.checkNotNull(profile, "Profile is required to retrieve/manipulate subset");
    }

    /**
     * Read title of the subset over command line
     * 
     * @return title entered by user
     */
    private String readSubsetName() {
        return subsetNameReader.read();
    }

    /**
     * Create a formatted path representation of subset<br />
     * The subset path string looks like
     * 
     * <pre>
     *  Root
     *   -Subset title of level(1)
     *     -subset title of level(2)
     *       -The subset title
     * </pre>
     * 
     * @param prefixSpaceLength
     *            number of spaces prefixed to each line
     * @param subset
     *            the subset which path representation is to be created
     * @return full subset path string
     */
    public static String fullSubsetPathString(Subset subset, int prefixSpaceLength) {
        Stack<Subset> stack = new Stack<Subset>();

        char[] spacePrefix = new char[prefixSpaceLength];
        char[] levelSpaces = { ' ', ' ' };
        Arrays.fill(spacePrefix, ' ');

        StringBuilder sb = new StringBuilder();
        StringBuilder prefix = new StringBuilder();

        prefix.append(levelSpaces);

        while (subset != null) {
            stack.push(subset);
            subset = subset.getParent();
        }

        while (!stack.isEmpty()) {
            subset = stack.pop();
            sb.append(subset.getTitle());
            if (!stack.isEmpty()) {
                sb.append('\n').append(prefix).append(spacePrefix).append('-');
                prefix.append(levelSpaces);
            }
        }

        return sb.toString();
    }

    private static String toString(Subset subset) {
        return toStringWithEllipse(subset.getParent()).concat(subset.getTitle());
    }

    /**
     * Reduce the size of any string to 50. by replacing extra char with '....'
     * 
     * @param subset
     *            which elliptical path is to be prepared
     * @return
     */
    private static String toStringWithEllipse(Subset subset) {
        if (subset == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        while (subset != null) {
            sb.insert(0, subset.getTitle().concat("/"));
            subset = subset.getParent();
        }

        if (sb.length() > 50) {
            sb.replace(24, sb.length() - 22, "....");

        }

        return sb.toString();
    }

    /**
     * Predicate to check whether subset title starts with specified title
     * 
     * @author Shamshad Alam
     * 
     */
    private static class StartWithTitlePredicate implements Predicate<Subset> {
        private String title;

        /**
         * @param title
         */
        public StartWithTitlePredicate(String title) {
            this.title = title.toLowerCase();
        }

        @Override
        public boolean apply(Subset input) {
            return input.getTitle().toLowerCase().startsWith(title);
        }
    }

    /**
     * The predicate which check whether length of the subset title is equals to
     * length specified
     * 
     * @author Shamshad Alam
     * 
     */
    private static class TitleLengthPredicate implements Predicate<Subset> {
        private int length;

        /**
         * @param length
         */
        public TitleLengthPredicate(int length) {
            this.length = length;
        }

        @Override
        public boolean apply(Subset input) {
            return this.length == input.getTitle().length();
        }
    }

    /**
     * Case insensitive ordering of the subset by title
     */
    private static final Ordering<Subset> CASE_INSENSITIVE_TITLE_ORDERING = new Ordering<Subset>() {

        @Override
        public int compare(Subset left, Subset right) {
            return String.CASE_INSENSITIVE_ORDER.compare(left.getTitle(), right.getTitle());
        }
    };
}
