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

package edu.udo.scaffoldhunter.gui.datasetmanagement;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Window;
import java.util.Collection;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.DBFunction;
import edu.udo.scaffoldhunter.gui.util.UnaryDBFunction;
import edu.udo.scaffoldhunter.gui.util.VoidUnaryDBFunction;
import edu.udo.scaffoldhunter.model.datacalculation.Calculator;
import edu.udo.scaffoldhunter.model.dataimport.Importer;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Ruleset;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.model.treegen.GeneratorOptions;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * Controller class to create dialogs for dataset management, tree management
 * and ruleset management
 * 
 * @author Philipp Lewe
 * 
 */
public class DatasetManagement {

    private DbManager db;
    private Window owner;
    private Profile profile;

    /**
     * @param db
     * @param owner
     * @param profile
     */
    public DatasetManagement(DbManager db, Window owner, Profile profile) {
        this.db = db;
        this.owner = owner;
        this.profile = profile;
    }

    /**
     * Shows the DatasetAndTreeManager dialog
     */
    public void showDatasetAndTreeManager() {
        JDialog manager = new DatasetAndTreeManagerDialog(owner, this);
        manager.setModal(true);
        manager.setVisible(true);
    }

    /**
     * Shows a dialog to create a new dataset and integrate new data
     * 
     * @param owner
     *            the owner of the dialog
     * @return the new Dataset (or null)
     */
    public Dataset newDatasetDialog(Window owner) {

        List<String> datasets = DBExceptionHandler.callDBManager(db, new DBFunction<List<String>>() {
            @Override
            public List<String> call() throws DatabaseException {
                return db.getAllDatasetNames();
            }
        });

        Importer importer = new Importer(db, profile, datasets, null);
        return importer.runImport(owner);
    }

    /**
     * Shows a dialog integrate new data into an existing dataset
     * 
     * @param owner
     *            the owner of the dialog
     * @param dataset
     *            the dataset in which new data should be integrated
     */
    public void editDatasetDialog(Window owner, Dataset dataset) {
        List<String> datasets = DBExceptionHandler.callDBManager(db, new DBFunction<List<String>>() {
            @Override
            public List<String> call() throws DatabaseException {
                return db.getAllDatasetNames();
            }
        });

        Importer importer = new Importer(db, profile, datasets, dataset);
        importer.runImport(owner);
    }

    /**
     * Shows a dialog to rename an existing dataset
     * 
     * @param owner
     *            the owner of the dialog
     * @param existingDatasets
     *            all existing datasets for checking valid names
     * @param dataset
     *            the dataset that should be renamed
     */
    public void renameDatasetDialog(Window owner, Collection<Dataset> existingDatasets, Dataset dataset) {

       RenameDatasetDialog dialog = new RenameDatasetDialog(owner, dataset, existingDatasets);
       
       dialog.setVisible(true);
       
       if(dialog.getResult() == RenameDatasetDialog.OK) {
           dataset.setTitle(dialog.getDatasetTitle());
           dataset.setComment(dialog.getDatasetComment());
           
           DBExceptionHandler.callDBManager(db, new UnaryDBFunction<Void, Dataset>(dataset) {
               @Override
               public Void call(Dataset dataset) throws DatabaseException {
                   db.saveOrUpdate(dataset);
                   return null;
               }
           }, true);
       }
    }

    /**
     * Shows a dialog to calculate new properties for a given dataset
     * 
     * @param owner
     *            the owner of the dialog
     * @param dataset
     *            the dataset for which new properties should be calculated
     */
    public void calculatePropertiesDialog(Window owner, Dataset dataset) {
        Calculator calculator = new Calculator(db, dataset);
        calculator.runCalc(owner);
    }

    /**
     * Shows a dialog to delete an existing dataset
     * 
     * @param owner
     *            the owner of the dialog
     * @param dataset
     *            the dataset to delete
     * @return true if the user confirmed the deletion of the dataset
     */
    public boolean deleteDatasetDialog(Window owner, Dataset dataset) {
        int decision = JOptionPane.showConfirmDialog(owner,
                _("DatasetAndTreeManager.DeleteDatasetConfirmation", dataset.getTitle()),
                _("DatasetAndTreeManager.DeleteConfirmationTitle"), JOptionPane.YES_NO_OPTION);

        if (decision == JOptionPane.YES_OPTION) {
            // delete dataset
            DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Dataset>(dataset) {
                @Override
                public void call(Dataset dataset) throws DatabaseException {
                    db.delete(dataset);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shows a dialog to create new trees
     * 
     * @param owner
     *            the owner of the dialog
     * @param dataset
     *            the dataset in which the tree should be generated
     * @param progressListener
     *            a progress listener that will bee added to the tree generation
     *            task
     */
    public void showNewTreeDialog(JDialog owner, Dataset dataset, ProgressListener<Tree> progressListener) {
        GeneratorOptions options;
        TreeGenProgressWorker genTask;

        TreeGenDialog dlg = new TreeGenDialog(owner, dataset, this);
        dlg.setVisible(true);

        if (dlg.getResult()) {
            options = dlg.getGeneratorOptions();
            genTask = new TreeGenProgressWorker(db, profile, dataset, options);
            genTask.executeWithProgressDialog(owner);
            genTask.addProgressListener(progressListener);
        }
    }

    /**
     * Shows a dialog to edit existing trees
     * 
     * @param owner
     *            the owner of the dialog
     * @param tree
     *            the tree which should be deleted
     */
    public void showEditTreeDialog(Window owner, Tree tree) {
        GeneratorOptions options;

        TreeGenDialog dlg = new TreeGenDialog(owner, tree, this);
        dlg.setVisible(true);

        if (dlg.getResult()) {
            options = dlg.getGeneratorOptions();

            tree.setTitle(options.getTitle());
            tree.setComment(options.getComment());

            // update tree
            DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Tree>(tree) {
                @Override
                public void call(Tree tree) throws DatabaseException {
                    db.saveOrUpdate(tree);
                }
            });
        }
    }

    /**
     * Shows a dialog to delete existing trees
     * 
     * @param owner
     *            the owner of the dialog
     * @param tree
     *            the tree which should be deleted
     * @return true if the user confirmed the deletion of the tree
     */
    public boolean showDeleteTreeDialog(Window owner, Tree tree) {
        int decision = JOptionPane.showConfirmDialog(owner,
                _("DatasetAndTreeManager.DeleteTreeConfirmation", tree.getTitle()),
                _("DatasetAndTreeManager.DeleteConfirmationTitle"), JOptionPane.YES_NO_OPTION);

        if (decision == JOptionPane.YES_OPTION) {
            // delete tree
            DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Tree>(tree) {
                @Override
                public void call(Tree tree) throws DatabaseException {
                    db.delete(tree);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    /**
     * Shows a dialog for management of rulesets
     * 
     * @param owner
     *            the owner of the dialog
     */
    public void showRulesetManagementDialog(Window owner) {
        JDialog dlg = DBExceptionHandler.callDBManager(db, new ManageRulesetsDialogDBFunction(owner, this));
        dlg.setVisible(true);
    }

    private static class ManageRulesetsDialogDBFunction implements DBFunction<JDialog> {

        Window owner;
        DatasetManagement controller;

        ManageRulesetsDialogDBFunction(Window owner, DatasetManagement controller) {
            this.owner = owner;
            this.controller = controller;
        }

        @Override
        public JDialog call() throws DatabaseException {
            return new ManageRulesetsDialog(owner, controller);
        }
    };

    /**
     * Saves a ruleset to the database
     * 
     * @param ruleset
     *            the ruleset to save
     */
    public void saveRuleset(Ruleset ruleset) {

        DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Ruleset>(ruleset) {
            @Override
            public void call(Ruleset ruleset) throws DatabaseException {
                db.saveOrUpdate(ruleset);
            }
        });
    }

    /**
     * Shows a dialog to delete rulesets
     * 
     * @param owner
     *            the owner of the dialog
     * @param ruleset
     *            the ruleset which should be deleted
     * @return true if the user confirmed the deletion of the ruleset
     */
    public boolean showDeleteRulesetDialog(Window owner, Ruleset ruleset) {
        int decision = JOptionPane.showConfirmDialog(owner,
                _("ManageRulesets.ConfirmDeleteRuleset", ruleset.getTitle()),
                _("ManageRulesets.ConfirmDeleteRulesetTitle"), JOptionPane.YES_NO_OPTION);

        if (decision == JOptionPane.YES_OPTION) {
            // delete tree
            DBExceptionHandler.callDBManager(db, new VoidUnaryDBFunction<Ruleset>(ruleset) {
                @Override
                public void call(Ruleset ruleset) throws DatabaseException {
                    db.delete(ruleset);
                }
            });
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the DB manager
     */
    public DbManager getDbManager() {
        return db;
    }

}
