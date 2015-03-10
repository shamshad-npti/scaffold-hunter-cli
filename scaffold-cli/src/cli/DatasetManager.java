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

import java.util.Collection;
import java.util.List;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.cli.reader.BooleanReader;
import edu.udo.scaffoldhunter.cli.reader.DefaultOptionModel;
import edu.udo.scaffoldhunter.cli.reader.OptionReader;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;

/**
 * Contains various method to read dataset over command. It includes methods to
 * get dataset by name, list of dataset, size of dataset, molecules in datset
 * etc.
 * 
 * @author Shamshad Alam
 * 
 */
public class DatasetManager {

    /**
     * A reader which read dataset by name over command line
     */
    private static OptionReader<Dataset> datasetReader;

    /**
     * Display confirmation message
     */
    private static BooleanReader confirmer;

    /**
     * Name of the connection
     */
    private String connectionName;

    /**
     * {@code DbManager} to interact with database
     */
    private DbManager db;

    /**
     * Create a {@code DatasetManager} object
     * 
     * @param connectionName
     *            name of the connection which is used to connect to the
     *            database
     */
    public DatasetManager(String connectionName) {
        this.connectionName = connectionName;
        init();
    }

    /**
     * Create a {@code DatasetManager} object
     * 
     * @param db
     *            {@code DbManager} which is used to interact with database
     */
    public DatasetManager(DbManager db) {
        this.db = db;
        init();
    }

    /**
     * Initialize necessary reader
     */
    private void init() {
        datasetReader = new OptionReader<Dataset>(null, _("CLI.DatasetManager.selectPrompt"));
        datasetReader.setPrePromptMessage(_("CLI.DatasetManager.selectPrePrompt"));
        datasetReader.setRequired(true);
        datasetReader.setQuitable(true);

        confirmer = new BooleanReader("");
    }

    /**
     * Get dataset identified by name. If {@code promptSelect} is true and
     * dataset name is either null or there is no dataset with this name user is
     * prompted to select dataset
     * 
     * @param datasetName
     *            name of the dataset
     * @param promptSelect
     *            allow user to select dataset from list
     * @return dataset identified by name or selected by user or null if any
     *         error, including database connection, occurs
     */
    public Dataset getDatasetByName(String datasetName, boolean promptSelect) {
        if (connected()) {

            // read all the datasets
            List<Dataset> datasets = DBExceptionHandler.callDBManager(db, new DBFunction<List<Dataset>>() {
                @Override
                public List<Dataset> call() throws DatabaseException {
                    return db.getAllDatasets();
                }
            });

            if (promptSelect) {
                return selectDataset(datasets, datasetName);
            } else {
                return getDataset(datasets, datasetName);
            }

        }
        return null;
    }

    /**
     * Get the list of all dataset in database or null in case of any connection
     * problem
     * 
     * @return List of all dataset
     */
    public List<Dataset> getAllDatasets() {
        if (connected()) {

            // read all the datasets
            List<Dataset> datasets = DBExceptionHandler.callDBManager(db, new DBFunction<List<Dataset>>() {
                @Override
                public List<Dataset> call() throws DatabaseException {
                    return db.getAllDatasets();
                }
            });

            return datasets;
        } else {
            return null;
        }
    }

    /**
     * @return List of name of all datasets in the database or empty list if
     *         error occurs or not connected to database
     */
    public List<String> getAllDatasetNames() {
        if (connected()) {
            List<String> datasetNames = DBExceptionHandler.callDBManager(db, new DBFunction<List<String>>() {
                @Override
                public List<String> call() throws DatabaseException {
                    return db.getAllDatasetNames();
                }
            });

            return datasetNames;
        }

        return Lists.newArrayList();
    }

    /**
     * Find dataset by title (case insensitive comparison) in the collection
     * 
     * @param datasets
     *            a collection of dataset to search into
     * 
     * @param title
     *            the title of the dataset
     * @return dataset with title specified or null if if no dataset exists with
     *         the title
     */
    public static Dataset getDataset(Collection<Dataset> datasets, String title) {
        Preconditions.checkNotNull(datasets);

        if (title == null) {
            return null;
        }

        for (Dataset dataset : datasets) {
            if (dataset.getTitle().equalsIgnoreCase(title)) {
                return dataset;
            }
        }

        return null;
    }

    /**
     * Find dataset by title. If any dataset in collection has same title as
     * supplied in parameter it is returned otherwise user is prompted to select
     * one of the dataset in collection and selected dataset is return. If user
     * doesn't select any dataset null is returned
     * 
     * @param datasets
     *            the collection of available dataset
     * @param title
     *            title of the dataset to use for search
     * @return dataset by title and if no dataset exists by specified name user
     *         is prompted to select a dataset from list
     */
    public static Dataset selectDataset(Collection<Dataset> datasets, String title) {
        Dataset dataset = getDataset(datasets, title);

        confirmer.setPromptMessage(_("CLI.DatasetManager.confirmSelectDataset"));
        confirmer.setPrePromptMessage(title == null ? _("CLI.DatasetManager.missingDataset")
                : _("CLI.DatasetManager.datasetNotFound"));

        if ((title == null) || (dataset == null && confirmer.read())) {
            datasetReader.setModel(new DefaultOptionModel<Dataset>(datasets));

            dataset = datasetReader.read();
        }

        return dataset;
    }

    /**
     * Find dataset by title. If any dataset in database has same title as
     * supplied in parameter it is returned otherwise user is prompted to select
     * one of the dataset in collection and selected dataset is return. If user
     * doesn't select any dataset null is returned
     * 
     * @param db
     *            {@link DbManager} to retrieve all dataset
     * @param title
     *            title of the dataset
     * @return dataset by title and if no dataset exists by specified title user
     *         is prompted to select a dataset from available dataset list
     */
    public static Dataset selectDataset(DbManager db, String title) {
        DatasetManager manager = new DatasetManager(db);
        return selectDataset(manager.getAllDatasets(), title);
    }

    /**
     * check whether dbManager is connected to current connection data
     * 
     * @return true if db is connected to current connection data
     */
    private boolean connected() {
        // check whether db is already initialized
        if (db != null) {
            return true;
        } else if (connectionName != null) {
            // try to make connection with database
            boolean connected = CLIConnectionManager.connect(connectionName, false);

            // connected? Yes, initialize db
            if (connected) {
                db = CLIConnectionManager.getDbManager();
            }

            // return status
            return connected;
        } else {
            return false;
        }
    }

    /**
     * Get the molecules count of dataset
     * 
     * @param dataset
     *            of which molecules is counted
     * @return number of molecules in the dataset or zero if any database
     *         exception occurs
     */
    public Integer size(final Dataset dataset) {
        Preconditions.checkNotNull(dataset);
        // check status
        if (connected()) {

            // read molecule count
            return DBExceptionHandler.callDBManager(db, new DBFunction<Integer>() {
                @Override
                public Integer call() throws DatabaseException {
                    return db.getRootSubsetSize(dataset, null);
                }
            });
        } else {
            return 0;
        }
    }

    /**
     * Get molecules in the dataset
     * 
     * @param dataset
     *            of which molecules is returned
     * @return all molecules in the dataset or null if any database exception
     *         occurs.
     */
    public List<Molecule> getMolecules(final Dataset dataset) {
        Preconditions.checkNotNull(dataset);

        // check status
        if (connected()) {

            // load the molecules from database
            return DBExceptionHandler.callDBManager(db, new DBFunction<List<Molecule>>() {
                @Override
                public List<Molecule> call() throws DatabaseException {
                    return db.getAllMolecules(dataset);
                }
            });
        } else {
            return null;
        }
    }
}
