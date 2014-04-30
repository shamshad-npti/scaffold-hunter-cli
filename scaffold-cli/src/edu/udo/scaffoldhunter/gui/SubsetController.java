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

import java.awt.Dialog.ModalityType;
import java.awt.Frame;
import java.util.EventListener;
import java.util.HashSet;
import java.util.LinkedList;

import javax.swing.event.EventListenerList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.gui.util.BinaryDBFunction;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.gui.util.ProgressWorkerUtil;
import edu.udo.scaffoldhunter.gui.util.VoidNullaryDBFunction;
import edu.udo.scaffoldhunter.gui.util.VoidUnaryDBFunction;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Filterset;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Subsets;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * @author Dominic Sacré
 * @author Shamshad Alam
 * 
 */
public class SubsetController {

    private static final Logger logger = LoggerFactory.getLogger(SubsetController.class);

    private final DbManager db;
    private final Session session;
    private Frame parentFrame = null;

    private EventListenerList subsetChangeListeners = new EventListenerList();


    /**
     * Controller class to handle all {@link Subset} specific actions
     * 
     * @param db
     *            the {@link DbManager}
     * @param session
     *            the {@link Session}
     */
    public SubsetController(DbManager db, Session session) {
        this.db = db;
        this.session = session;
    }
    
    /**
     * @param parentFrame the parentFrame for modal dialogs, etc
     */
    public void setParentFrame(Frame parentFrame) {
        this.parentFrame = parentFrame;
    }

    /**
     * @return the root subset of the associated session
     */
    public Subset getRootSubset() {
        return session.getSubset();
    }

    /**
     * Creates a new subset, but does not add it to the subset tree.
     * 
     * @param parent
     *            the parent subset, or null to attach the new subset directly
     *            to the root subset
     * @param title
     *            the new subset's name
     * @param molecules
     *            the molecules to be included in the new subset, must not be
     *            null
     * 
     * @return the newly created subset
     */
    public Subset createSubset(Subset parent, String title, Iterable<Molecule> molecules) {
        logger.trace("parent={}, title={}, molecules={}", new Object[] { parent, title, molecules });
        Preconditions.checkNotNull(molecules);

        if (parent == null) {
            parent = session.getSubset();
        }

        return new Subset(parent, title, null, session, molecules, null);
    }

    /**
     * Adds a subset to the subset tree by attaching it to its parent subset,
     * and saves the subset in the database. Note: Use
     * {@link #addSubsets(Iterable)} to add multiple subsets at once.
     * 
     * @see #addSubsets(Iterable)
     * @param subset
     *            the subset to be added
     */
    public synchronized void addSubset(final Subset subset) {
        Preconditions.checkNotNull(parentFrame, "The parent Frame must be set for this mehtod, to show a modal dialog");
        Preconditions.checkNotNull(subset);
        Preconditions.checkArgument(subset.size() > 0);

        Subset parent = subset.getParent();

        parent.addChild(subset);

        ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Subset>(subset) {
                    @Override
                    public void call(Subset subset) throws DatabaseException {
                        // updating the parent in the DB is not needed, as it has no
                        // references to its children at the DB level
                        db.saveAsNew(subset);
                    }
                });
                
                return null;
            }
        };

        ProgressWorkerUtil.executeWithProgressDialog(parentFrame, _("Message.Info"), I18n.get("Message.Processing"),
                ModalityType.APPLICATION_MODAL, worker);
        
        fireSubsetAdded(subset);
    }

    /**
     * Adds multiple subsets to the subset tree by attaching them to their
     * parent subsets, and saves the subsets in the database. Note: This is more
     * efficient than calling {@link #addSubset(Subset)} multiple times and
     * fires different subset change events.
     * 
     * @see #addSubset(Subset)
     * @param subsets
     *            the subsets to be added
     */
    public synchronized void addSubsets(final Iterable<Subset> subsets) {
        Preconditions.checkNotNull(parentFrame, "The parent Frame must be set for this mehtod, to show a modal dialog");
        for (Subset subset : subsets) {
            Preconditions.checkNotNull(subset);
            Preconditions.checkArgument(subset.size() > 0);
        }

        for (Subset subset : subsets) {
            Subset parent = subset.getParent();
            parent.addChild(subset);
        }

        
        ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {
                    @Override
                    public void voidCall() throws DatabaseException {
                        // save all new subset to database at once
                        db.saveAllAsNew(subsets);
                    }
                });
                
                return null;
            }
        };

        ProgressWorkerUtil.executeWithProgressDialog(parentFrame, _("Message.Info"), I18n.get("Message.Processing"),
                ModalityType.APPLICATION_MODAL, worker);
        

        fireSubsetsAdded(subsets);
    }

    /**
     * Removes a subset from the subset tree and the database.
     * 
     * @param subset
     *            the subset to be removed
     */
    public synchronized void removeSubset(final Subset subset) {
        logger.trace("subset={}", subset);
        Preconditions.checkNotNull(parentFrame, "The parent Frame must be set for this mehtod, to show a modal dialog");
        Preconditions.checkNotNull(subset);
        Preconditions.checkArgument(subset.getParent() != null);

        Subset parent = subset.getParent();
        parent.removeChild(subset);

        // attach the children of the subset being deleted to its parent
        for (Subset child : subset.getChildren()) {
            parent.addChild(child);
            child.setParent(parent);
        }

        ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Subset>(subset) {
                    @Override
                    public void call(Subset subset) throws DatabaseException {
                        for (Subset child : subset.getChildren()) {
                            db.saveOrUpdate(child);
                        }
                        
                        // updating the parent in the DB is not needed, as it has no
                        // references to its children at the DB level
                        db.delete(subset);
                    }
                });
                
                return null;
            }
        };

        ProgressWorkerUtil.executeWithProgressDialog(parentFrame, _("Message.Info"), I18n.get("Message.Processing"),
                ModalityType.APPLICATION_MODAL, worker);
        

        fireSubsetRemoved(subset);
    }
    
    /**
     * Removes multiple subsets from the subset tree and the database.
     * 
     * @param subsets
     *            the subsets to be removed
     */
    public synchronized void removeSubsets(final Iterable<Subset> subsets) {
        Preconditions.checkNotNull(parentFrame, "The parent Frame must be set for this mehtod, to show a modal dialog");
        Preconditions.checkNotNull(subsets);
        for (Subset subset : subsets) {
            Preconditions.checkArgument(subset.getParent() != null);
        }

        ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                HashSet<Subset> subsetsSet = Sets.newHashSet(subsets);
                final LinkedList<Subset> childrenToUpdate = Lists.newLinkedList();
                
                for (Subset subset : subsets) {
                    Subset parent = subset.getParent();
                    parent.removeChild(subset);

                    /*
                     * attach the children of the subset being deleted to its
                     * parent
                     */
                    for (Subset child : subset.getChildren()) {
                        if (!subsetsSet.contains(child)) {
                            parent.addChild(child);
                            child.setParent(parent);
                            childrenToUpdate.add(child);
                        }
                    }

                }
                
                DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {
                    @Override
                    public void voidCall() throws DatabaseException {
                        /*
                         * updating the parent in the DB is not needed, as it
                         * has no references to its children at the DB level
                         */
                        db.saveOrUpdateAll(childrenToUpdate);
                        db.deleteAll(subsets);
                    }
                });

                return null;
            }
        };
        
        ProgressWorkerUtil.executeWithProgressDialog(parentFrame, _("Message.Info"), I18n.get("Message.Processing"),
                ModalityType.APPLICATION_MODAL, worker);
        
        
        fireSubsetsRemoved(subsets);
    }

    /**
     * Renames a subset. The subset must already have been saved in the
     * database.
     * 
     * @param subset
     *            the subset to be renamed
     * @param title
     *            the subset's new name
     */
    public void renameSubset(Subset subset, String title) {
        logger.trace("subset={}, title={}", subset, title);
        Preconditions.checkNotNull(subset);
        Preconditions.checkNotNull(title);
        Preconditions.checkArgument(title.length() > 0);

        subset.setTitle(title);

        DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Subset>(subset) {
            @Override
            public void call(Subset subset) throws DatabaseException {
                db.saveOrUpdate(subset);
            }
        }, true);

        fireSubsetChanged(subset);
    }

    /**
     * Changes a subset's comment. The subset must already have been saved in
     * the database.
     * 
     * @param subset
     *            the subset to be modified
     * @param comment
     *            the subset's new comment
     */
    public synchronized void changeSubsetComment(Subset subset, String comment) {
        logger.trace("subset={}, comment={}", subset, comment);
        Preconditions.checkNotNull(subset);

        subset.setComment(comment);

        DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Subset>(subset) {
            @Override
            public void call(Subset subset) throws DatabaseException {
                db.saveOrUpdate(subset);
            }
        }, true);

        fireSubsetChanged(subset);
    }

    /**
     * Creates a new subset by filtering the given subset, but does not add it
     * to the subset tree.
     * 
     * @param parent
     *            the parent subset, or null to attach the new subset directly
     *            to the root subset
     * @param filterset
     *            the filterset to filter the given subset
     * @return the newly created subset
     */
    public Subset createFilteredSubset(Subset parent, Filterset filterset) {
        if (parent == null) {
            parent = session.getSubset();
        }

        Subset newSubset = DBExceptionHandler.callDBManager(db, new BinaryDBFunction<Subset, Subset, Filterset>(parent,
                filterset) {
            @Override
            public Subset call(Subset subset, Filterset filterset) throws DatabaseException {
                return db.getFilteredSubset(subset, filterset);
            }
        });

        newSubset.setParent(parent);

        return newSubset;
    }

    /**
     * Creates a new subset by random sampling the given subset, but does not
     * add it to the subset tree.
     * 
     * @param parent
     *            the parent subset, or null to attach the new subset directly
     *            to the root subset
     * @param size
     *            the size of the random sample
     * @return the newly created subset
     */
    public Subset createRandomSubset(Subset parent, int size) {
        logger.trace("parent={}, size={}", new Object[] { parent, size });
        if (parent == null) {
            parent = session.getSubset();
        }

        Subset randomSubset = Subsets.random(parent, size);
        randomSubset.setTitle(defaultRandomName(parent, size));

        return randomSubset;
    }

    /**
     * Creates the union of an arbitrary number of subsets.
     * 
     * @param parent
     *            the parent subset, or null to use the lowest common ancestor
     * @param title
     *            the new subset's name, or null to generate one automatically
     * @param subsets
     *            the input subsets
     * 
     * @return the newly created subset
     */
    public Subset createUnion(Subset parent, String title, Iterable<Subset> subsets) {
        logger.trace("parent={}, title={}, subsets={}", new Object[] { parent, title, subsets });

        Subset unionSubset = Subsets.union(parent, subsets);
        unionSubset.setTitle(title == null ? defaultUnionName(subsets) : title);

        return unionSubset;
    }

    /**
     * Creates the intersection of an arbitrary number of subsets.
     * 
     * @param parent
     *            the parent subset, or null to use the first input subset
     * @param title
     *            the new subset's name, or null to generate one automatically
     * @param subsets
     *            the input subsets
     * 
     * @return the newly created subset
     */
    public Subset createIntersection(Subset parent, String title, Iterable<Subset> subsets) {
        logger.trace("parent={}, title={}, subsets={}", new Object[] { parent, title, subsets });

        Subset intersecSubset = Subsets.intersection(parent, subsets);
        intersecSubset.setTitle(title == null ? defaultIntersectionName(subsets) : title);

        return intersecSubset;
    }

    /**
     * Creates the difference of one subset and an arbitrary number of other
     * subsets.
     * 
     * @param parent
     *            the parent subset, or null to use the first input subset
     * @param title
     *            the new subset's name, or null to generate one automatically
     * @param source
     *            the first input subset (from which the others are subtracted)
     * @param subtract
     *            the other input subsets
     * 
     * @return the newly created subset
     */
    public Subset createDifference(Subset parent, String title, Subset source, Iterable<Subset> subtract) {
        logger.trace("parent={}, title={}, source={}, subtract={}", new Object[] { parent, title, source, subtract });
        
        Subset differenceSubset = Subsets.difference(parent, source, subtract);
        differenceSubset.setTitle(title == null ? defaultDifferenceName(source, subtract) : title);

        return differenceSubset;
    }

    /**
     * Creates a subset for each of the given scaffolds. The subset for a
     * scaffold contains the molecules associated with the scaffold and any of
     * its descendants in the scaffold tree. 
     * 
     * @param parent
     *            the parent subset
     * @param title
     *            prefix of the title of the subsets. We add a unique number as a suffix.
     * @param scaffolds
     *            Scaffolds to create a subtree subset for
     * @return the newly created subsets
     */
    public Iterable<Subset> subsetsFromSubtrees(Subset parent, String title, Iterable<Scaffold> scaffolds) {
        logger.trace("parent={}, title={}, scaffolds={}", new Object[] { parent, title, scaffolds });

        parent = parent == null ? session.getSubset() : parent;
        Iterable<Subset> subsetsFromSubtrees = Subsets.subsetsFromSubtrees(parent, title, scaffolds);
       
        return subsetsFromSubtrees;
    }

    /**
     * @param subsets
     * 
     * @return the default name for the union of the given subsets
     */
    public String defaultUnionName(Iterable<Subset> subsets) {
        String joined = Joiner.on(", ").join(Iterables.transform(subsets, Subsets.getSubsetTitleFunction));
        return "union(" + joined + ")";
    }

    /**
     * @param subsets
     * 
     * @return the default name for the intersection of the given subsets
     */
    public String defaultIntersectionName(Iterable<Subset> subsets) {
        String joined = Joiner.on(", ").join(Iterables.transform(subsets, Subsets.getSubsetTitleFunction));
        return "intersection(" + joined + ")";
    }

    /**
     * @param source
     * @param subtract
     * 
     * @return the default name for the difference of the given subsets
     */
    public String defaultDifferenceName(Subset source, Iterable<Subset> subtract) {
        if (Iterables.size(subtract) == 1) {
            return "difference(" + source.getTitle() + ", " + Iterables.get(subtract, 0).getTitle() + ")";
        } else {
            return "difference(" + source.getTitle() + ", " + defaultUnionName(subtract) + ")";
        }
    }

    /**
     * @param source
     * @param filterset
     * @return the default name for the filter of a subset
     */
    public String defaultFilterName(Subset source, Filterset filterset) {
        return "filter(" + source.getTitle() + ", " + filterset.getTitle() + ")";
    }

    /**
     * @param source
     * @param size
     * @return the default name for the random subset
     */
    public String defaultRandomName(Subset source, int size) {
        return "random(" + source.getTitle() + ", " + size + ")";
    }

    /**
     * Listener interface for changes in the subset tree.
     */
    public interface SubsetChangeListener extends EventListener {
        /**
         * Fired when a subset has been added.
         * 
         * @param subset
         *            the newly added subset
         */
        public void subsetAdded(Subset subset);

        /**
         * Fired when multiple subsets have been added.
         * 
         * @param subsets
         *            the newly added subsets
         */
        public void subsetsAdded(Iterable<Subset> subsets);

        /**
         * Fired when a subset has been removed.
         * 
         * @param subset
         *            the subset that was removed
         */
        public void subsetRemoved(Subset subset);
        
        /**
         * Fired when multiple subsets are removed.
         * 
         * @param subsets
         *            the subset that was removed
         */
        public void subsetsRemoved(Iterable<Subset> subsets);

        /**
         * Fired when a subset changed.
         * 
         * @param subset
         *            the subset that has changes
         */
        public void subsetChanged(Subset subset);
    }

    /**
     * Adds a subset change listener.
     * 
     * @param listener
     */
    public void addSubsetChangeListener(SubsetChangeListener listener) {
        subsetChangeListeners.add(SubsetChangeListener.class, listener);
    }

    /**
     * Removes a subset change listener.
     * 
     * @param listener
     */
    public void removeSubsetChangeListener(SubsetChangeListener listener) {
        subsetChangeListeners.remove(SubsetChangeListener.class, listener);
    }

    private void fireSubsetAdded(Subset subset) {
        for (SubsetChangeListener l : subsetChangeListeners.getListeners(SubsetChangeListener.class)) {
            l.subsetAdded(subset);
        }
    }

    private void fireSubsetsAdded(Iterable<Subset> subsets) {
        for (SubsetChangeListener l : subsetChangeListeners.getListeners(SubsetChangeListener.class)) {
            l.subsetsAdded(subsets);
        }
    }

    private void fireSubsetRemoved(Subset subset) {
        for (SubsetChangeListener l : subsetChangeListeners.getListeners(SubsetChangeListener.class)) {
            l.subsetRemoved(subset);
        }
    }
    private void fireSubsetsRemoved(Iterable<Subset> subsets) {
        for (SubsetChangeListener l : subsetChangeListeners.getListeners(SubsetChangeListener.class)) {
            l.subsetsRemoved(subsets);
        }
    }

    private void fireSubsetChanged(Subset subset) {
        for (SubsetChangeListener l : subsetChangeListeners.getListeners(SubsetChangeListener.class)) {
            l.subsetChanged(subset);
        }
    }

}
