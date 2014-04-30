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

package edu.udo.scaffoldhunter.model.dataimport;

import java.awt.Dialog.ModalityType;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.gui.data.MessageDialog;
import edu.udo.scaffoldhunter.gui.dataimport.ImportDialog;
import edu.udo.scaffoldhunter.gui.dataimport.ImportDialog.Result;
import edu.udo.scaffoldhunter.gui.dataimport.ImportMappingsDialog;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.gui.util.ProgressWorkerUtil;
import edu.udo.scaffoldhunter.gui.util.VoidNullaryDBFunction;
import edu.udo.scaffoldhunter.gui.util.VoidUnaryDBFunction;
import edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener;
import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.datacalculation.Calculator;
import edu.udo.scaffoldhunter.model.dataimport.ImportJob.SourcePropertyMapping;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldTreeGenerator;
import edu.udo.scaffoldhunter.plugins.SHPluginManager;
import edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.ProgressAdapter;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * A control class responsible for the construction of a new dataset or for
 * adding properties to an existing one
 * 
 * @author Henning Garus
 * 
 */
public class Importer {

    private static final Logger logger = LoggerFactory.getLogger(Importer.class);

    private final SHPluginManager pluginManager = new SHPluginManager();
    private final ImmutableList<ImportPlugin> plugins;
    private final ImportProcess importProcess;
    private final Set<String> existingDatasetNames;

    private final DbManager db;
    private final Profile profile;

    private List<PropertyDefinition> newPropDefs;

    private Dataset newDataset;
    private boolean finished = false;
    private MergeWorker mergeWorker;

    /**
     * Create a new Importer
     * 
     * @param db
     *            the db manager
     * @param profile
     *            the profile of the user who is importing data
     * @param existingDatasetNames
     *            a list which contains the names of all currently existing
     *            datasets
     * @param dataset
     *            the dataset where data should be merged into or
     *            <code>null</code> if a new dataset should be created.
     * 
     */
    public Importer(DbManager db, Profile profile, List<String> existingDatasetNames, Dataset dataset) {
        this.db = db;
        this.profile = profile;
        Ordering<ImportPlugin> o = new PluginByTitleOrdering();
        plugins = o.immutableSortedCopy(pluginManager.getPlugins());
        Iterable<String> toLowercase = Iterables.transform(existingDatasetNames, new StringToLowerCase());
        this.existingDatasetNames = Sets.newHashSet(toLowercase);

        if (dataset == null)
            importProcess = new ImportProcess(getDefaultDatasetName(), this.existingDatasetNames);
        else
            importProcess = new ImportProcess(dataset);
    }

    /**
     * 
     * @return a list model containing all available plugins.
     */
    public ListModel getPluginListModel() {
        return new AbstractListModel() {

            @Override
            public int getSize() {
                return plugins.size();
            }

            @Override
            public Object getElementAt(int index) {
                return plugins.get(index);
            }
        };
    }

    /**
     * start the import process
     * 
     * @param owner
     *            the owner of the dialogs which will be shown during import
     * @return the new dataset (or null if the user canceled creation)
     */
    public Dataset runImport(Window owner) {

        ImportMappingsDialog mappingsDialog;
        ImportDialog importDialog = new ImportDialog(owner, getPluginListModel(), importProcess);

        /*
         * The import process consists (currently) of three dialogs:
         * 
         * 1. ImportDialog: Import jobs are created
         * 
         * 2. ImportMappingsDialog: Imported Properties are mapped to property
         * definitions.
         * 
         * 3. MessageDialog: Shows messages during the actual dataimport.
         * 
         * The first two dialogs each run inside a loop. When cancel is clicked
         * the loop is aborted and the user is returned to the pevious dialog.
         */
        for (;;) {
            if (importProcess.getDataset() == null) {
                newDataset = new Dataset();
                newDataset.setFilterset(null);
                newDataset.setDatabaseIdentifiers("");
                newDataset.setCreationDate(new Date());
                newDataset.setCreatedBy(profile);
            } else {
                newDataset = importProcess.getDataset();
            }
            final Map<String, PropertyDefinition> propertyDefinitions = Maps.newHashMap(newDataset
                    .getPropertyDefinitions());
            importDialog.setVisible(true);
            if (importDialog.getResult() == Result.CANCEL)
                return null;
            assert (importDialog.getResult() == Result.START_IMPORT);
            newDataset.setTitle(importProcess.getDatasetTitle());
            newDataset.setComment(importProcess.getDatasetComment());
            NewDatasetWorker w = new NewDatasetWorker(propertyDefinitions);
            ProgressWorkerUtil.executeWithProgressDialog(importDialog, I18n.get("DataImport.PreparingImport"),
                    I18n.get("DataImport.PreparingJobs"), ModalityType.DOCUMENT_MODAL, w);

            if (!importProcess.isPrepared()) {
                if (w.isNameTaken()) {
                    importDialog.validateInputs();
                    JOptionPane.showMessageDialog(importDialog, I18n.get("DataImport.DatasetNameTaken"),
                            I18n.get("Title.Error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(importDialog, I18n.get("DataImport.DatabaseError"),
                            I18n.get("Title.Error"), JOptionPane.ERROR_MESSAGE);
                }
                continue;
            }
            for (;;) {
                mappingsDialog = new ImportMappingsDialog(owner, importProcess);
                mappingsDialog.setVisible(true);
                if (mappingsDialog.getResult() == ImportMappingsDialog.Result.OK) {

                    saveDatasetAndPropertyDefinitions(newDataset, propertyDefinitions);

                    final MessageDialog messageDialog = new MessageDialog(owner, MessageDialog.IMPORT,
                            new ActionListener() {
                                /*
                                 * Handle cancel events from the MessageDialog
                                 * here otherwise we have the control logic for
                                 * cancel in the dialog, which would be more
                                 * weird than this already is.
                                 */
                                @Override
                                public void actionPerformed(ActionEvent e) {
                                    logger.trace("cancelling import");
                                    // if the worker is finished cancel does
                                    // nothing
                                    mergeWorker.cancel(true);
                                }
                            });
                    mergeWorker = new MergeWorker(messageDialog, importProcess.getJobs(), newDataset,
                            importProcess.getMergedPropeties());
                    mergeWorker.addProgressListener(messageDialog);
                    mergeWorker.addProgressListener(new MergeProgressListener(messageDialog));

                    mergeWorker.addExceptionListener(new WorkerExceptionListener() {
                        /*
                         * (non-Javadoc)
                         * 
                         * @see
                         * edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener
                         * #exceptionThrown(edu.udo.scaffoldhunter.gui.util.
                         * ProgressWorker, java.lang.Throwable)
                         */
                        @Override
                        public ExceptionHandlerResult exceptionThrown(Throwable e) {
                            if (e instanceof DatabaseException) {
                                DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {

                                    // TODO instead of deleting everything and
                                    // restarting ask the user and use the
                                    // cleanup worker
                                    @Override
                                    public void voidCall() throws DatabaseException {
                                        db.delete(newDataset);
                                    }
                                });
                                // if we got here we still have a database and
                                // can retry the whole import
                                return ExceptionHandlerResult.STOP;
                            } else if (e instanceof RuntimeException) {
                                throw (RuntimeException) e;
                            } else {
                                // apart from DatabaseExceptions only
                                // RuntimeExceptions should end up here.
                                throw new AssertionError(e);
                            }
                        }
                    });
                    mergeWorker.execute();
                    messageDialog.setVisible(true);
                    if (finished)
                        return newDataset;
                    else { // user canceled import

                    }
                } else { // user clicked cancel in propertyMapping dialog
                    if (importProcess.getDataset() == null)
                        DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {

                            @Override
                            public void voidCall() throws DatabaseException {
                                db.delete(newDataset);
                            }
                        }, true);
                    break;
                }
            } // end propertyMappingDialog loop
        } // end ImportDialog loop
    }

    private String getDefaultDatasetName() {
        String ret;
        for (int i = 1; /* return when new name is found */; i++) {
            ret = String.format("%s %d", I18n.get("DataImport.Dataset"), i);
            if (!existingDatasetNames.contains(ret.toLowerCase()))
                return ret;
        }
    }

    private List<PropertyDefinition> saveDatasetAndPropertyDefinitions(Dataset dataset,
            Map<String, PropertyDefinition> propDefs) {
        for (ImportJob j : importProcess.getJobs()) {
            for (Map.Entry<String, SourcePropertyMapping> e : j.getPropertyMappings().entrySet()) {
                if (e.getValue().getPropertyDefiniton() != null) {
                    PropertyDefinition propDef = e.getValue().getPropertyDefiniton();
                    // find unique key if key is not set
                    if (propDef.getKey() == null || propDef.getKey().isEmpty()) {
                        String newKey;
                        for (int i = 0;; i++) {
                            newKey = propDef.getTitle().toUpperCase() + i;

                            /*
                             * prevents key collision with scaffold properties
                             * and calculated properties
                             */
                            if (newKey.startsWith(ScaffoldTreeGenerator.SCAFFOLD_PROPERTY_KEY_PREFIX)
                                    || newKey.startsWith(Calculator.CALC_PLUGINS_PROPERTY_KEY_PREFIX)) {
                                newKey = "_" + newKey;
                            }

                            if (!propDefs.containsKey(newKey))
                                break;
                        }
                        propDef.setKey(newKey);
                    } else {
                        /*
                         * prevents key collision with scaffold properties and
                         * calculated properties
                         */
                        if (propDef.getKey().startsWith(ScaffoldTreeGenerator.SCAFFOLD_PROPERTY_KEY_PREFIX)
                                || propDef.getKey().startsWith(Calculator.CALC_PLUGINS_PROPERTY_KEY_PREFIX)) {
                            propDef.setKey("_" + propDef.getKey());
                        }
                    }
                    propDef.setDataset(dataset);
                    propDefs.put(propDef.getKey(), propDef);
                }
            }
        }

        if (importProcess.getDataset() != null) {
            Predicate<PropertyDefinition> notOld = Predicates.and(Predicates.notNull(),
                    Predicates.not(Predicates.in(importProcess.getDataset().getPropertyDefinitions().values())));
            newPropDefs = Lists.newArrayList(Iterables.filter(propDefs.values(), notOld));
        } else {
            newPropDefs = Lists.newArrayList(propDefs.values());
        }

        dataset.setPropertyDefinitions(propDefs);
        DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Dataset>(dataset) {
            @Override
            public void call(Dataset arg) throws DatabaseException {
                db.saveOrUpdate(newDataset);
                db.saveAllAsNew(newPropDefs);
            }
        });
        return newPropDefs;
    }

    private static class PluginByTitleOrdering extends Ordering<ImportPlugin> implements Serializable {

        @Override
        public int compare(ImportPlugin left, ImportPlugin right) {
            return String.CASE_INSENSITIVE_ORDER.compare(left.getTitle(), right.getTitle());
        }

    }

    private class MergeWorker extends ProgressWorker<Void, Message> implements MessageListener {

        private final List<ImportJob> importJobs;
        private final Dataset dataset;
        private final Multimap<ImportJob, PropertyDefinition> mergedProperties;
        private int overallProgress = 0;
        private final MessageDialog messageDialog;

        MergeWorker(MessageDialog dialog, List<ImportJob> jobs, Dataset dataset,
                Multimap<ImportJob, PropertyDefinition> mergedProperties) {
            this.messageDialog = dialog;
            this.importJobs = jobs;
            this.dataset = dataset;
            this.mergedProperties = mergedProperties;
        }

        @Override
        protected Void doInBackground() throws DatabaseException {
            int overallLength = 0;
            for (ImportJob j : importJobs) {
                overallLength += j.getResults().getNumMolecules();
            }
            setProgressBounds(0, overallLength);
            setProgressIndeterminate(false);

            for (ImportJob j : importJobs) {
                j.addMessageListener(this);
                boolean mergeByNumProperty = false;
                if (j.getInternalMergeBy() != null)
                    mergeByNumProperty = !j.getInternalMergeBy().isStringProperty();
                MergeIterator mergeIterator = new MergeIterator(j, dataset, mergedProperties.get(j),
                        j.getSourceMergeBy(), mergeByNumProperty);
                mergeIterator.addMessageListener(this);
                mergeIterator.addProgressListener(new ProgressListener<Void>() {

                    @Override
                    public void setProgressValue(int progress) {
                        MergeWorker.this.setProgressValue(overallProgress + progress);
                    }

                    @Override
                    public void setProgressIndeterminate(boolean indeterminate) {
                    }

                    @Override
                    public void setProgressBounds(int min, int max) {
                    }

                    @Override
                    public void finished(Void v, boolean cancelled) {
                    }
                });
                if (j.getInternalMergeBy() == null) {
                    db.mergeMoleculesIntoDBbySMILES(mergeIterator);
                } else {
                    db.mergeMoleculesIntoDBbyProperty(mergeIterator, j.getInternalMergeBy(), dataset);
                }
                if (Thread.interrupted()) {
                    return null;
                }
                overallProgress += j.getResults().getNumMolecules();
                j.removeMessageListener(this);
            }
            return null;
        }

        @Override
        public void receiveMessage(Message message) {
            publish(message);
        }

        @Override
        protected void process(List<Message> chunks) {
            super.process(chunks);
            if (!isCancelled()) {
                for (Message m : chunks)
                    messageDialog.addMessage(m);
            }
        }

    }

    private class MergeProgressListener extends ProgressAdapter<Void> {

        private final MessageDialog messageDialog;
        private boolean canceled = false;

        private MergeProgressListener(MessageDialog messageDialog) {
            super();
            this.messageDialog = messageDialog;
        }

        @Override
        public void finished(Void result, boolean canceled) {
            if (canceled) {
                this.canceled = true;
                logger.trace("creating cleanup worker");
                // on cancel delete all imported properties
                CleanupWorker cleanup = new CleanupWorker();
                cleanup.addProgressListener(new ProgressAdapter<Void>() {
                    @Override
                    public void finished(Void result, boolean cancelled) {
                        messageDialog.dispose();
                    }
                });
                messageDialog.setText(I18n.get("DataImport.ImportCanceled"));
                messageDialog.setProgressIndeterminate(true);
                cleanup.execute();
                // TODO handle exceptions thrown during cleanup
            } else {
                messageDialog.setText(I18n.get("DataImport.ImportFinished"));
                finished = true;
            }
        }

        @Override
        public void setProgressValue(int progress) {
            if (canceled)
                return;
            int overallProgress = 0;
            int i = 1;
            // determine the current job
            for (ImportJob j : importProcess.getJobs()) {
                int jobProgress = overallProgress + j.getResults().getNumMolecules();
                // just having \n working as expected would be to easy...
                // let's center this stuff, while we're at it
                if (progress < jobProgress) {
                    messageDialog.setText("<html><div align=\"center\">"
                            + I18n.get("DataImport.RunningNthJob", i, importProcess.getJobs().size(), j)
                            + "<br>"
                            + I18n.get("DataImport.ImportingNthMolecule", progress + 1 - overallProgress, j
                                    .getResults().getNumMolecules()) + "</div></html>");
                    break;
                }
                i++;
                overallProgress = jobProgress;
            }
        }
    }

    private class CleanupWorker extends ProgressWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            logger.trace("starting cleanup");
            for (PropertyDefinition p : newPropDefs) {
                newDataset.getPropertyDefinitions().remove(p.getKey());
            }
            DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {

                @Override
                public void voidCall() throws DatabaseException {
                    db.saveOrUpdate(newDataset);
                    db.deleteAll(newPropDefs);
                }
            });
            return null;
        }
    }

    private static class StringToLowerCase implements Function<String, String> {
        @Override
        public String apply(String input) {
            return input.toLowerCase();
        }
    }

    private class NewDatasetWorker extends ProgressWorker<Void, Void> {
        private boolean nameTaken = false;

        private final Map<String, PropertyDefinition> propertyDefinitions;

        NewDatasetWorker(Map<String, PropertyDefinition> propertyDefinitions) {
            this.propertyDefinitions = propertyDefinitions;
        }

        @Override
        protected Void doInBackground() throws Exception {
            if (importProcess.getDataset() == null) {
                try {
                    db.saveAsNew(newDataset);
                } catch (DatabaseException e) {
                    if (e.getCause() instanceof ConstraintViolationException) {
                        List<String> datasetNames = DBExceptionHandler.callDBManager(db,
                                new DBFunction<List<String>>() {
                                    @Override
                                    public List<String> call() throws DatabaseException {
                                        return db.getAllDatasetNames();
                                    }
                                });
                        Iterables.addAll(importProcess.getExistingDatasets(),
                                Iterables.transform(datasetNames, new StringToLowerCase()));
                        nameTaken = true;
                        return null;
                    } else {
                        return null;
                    }
                }
            }
            importProcess.prepareImport(propertyDefinitions);
            return null;
        }

        /**
         * @return if the dataset name is taken
         */
        public boolean isNameTaken() {
            return nameTaken;
        }
    }
}