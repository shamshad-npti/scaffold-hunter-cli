/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
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

package edu.udo.scaffoldhunter.gui.datasetmanagement;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.gui.util.ProgressPanel;
import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.model.treegen.GeneratorOptions;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldTreeGenerationException;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldTreeGenerator;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * ProgressWorker which generates the tree in background
 * 
 * @author Philipp Lewe
 * 
 */
public class TreeGenProgressWorker extends ProgressWorker<Tree, Void> implements PropertyChangeListener {
    private static final Logger logger = LoggerFactory.getLogger(TreeGenProgressWorker.class);

    private Profile profile;
    private Dataset dataset;
    private GeneratorOptions generatorOptions;
    private ScaffoldTreeGenerator treeGen;
    private ScaffoldTreeGenerator.Progress progress;
    private boolean progressBoundsReported = false;
    private JDialog parent;

    /**
     * Creates a new <code>TreeGenProgressWorker</code>
     * 
     * @param db
     *            the DB manager
     * @param profile
     *            the profile of the user who generates the tree
     * @param dataset
     *            the dataset on which the tree is generated
     * @param generatorOptions
     *            the options for generation
     */
    public TreeGenProgressWorker(DbManager db, Profile profile, Dataset dataset, GeneratorOptions generatorOptions) {
        super();
        this.profile = profile;
        this.dataset = dataset;
        this.generatorOptions = generatorOptions;
        treeGen = new ScaffoldTreeGenerator(db);
        setProgressIndeterminate(false);
    }

    @Override
    protected Tree doInBackground() throws Exception {
        treeGen.addPropertyChangeListener(ScaffoldTreeGenerator.PROPERTY_PROGRESS, this);
        return treeGen.generateAndStoreTree(profile, dataset, generatorOptions);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == ScaffoldTreeGenerator.PROPERTY_PROGRESS) {
            progress = (ScaffoldTreeGenerator.Progress) evt.getNewValue();
            if (!progressBoundsReported) {
                this.setProgressIndeterminate(false);
                setProgressBounds(0, progress.getTotalMolecules());
                progressBoundsReported = true;
            }

            if (!progress.isSaving()) {
                setProgressValue(progress.getProcessedMolecules());
            } else {
                setProgressIndeterminate(true);
            }
        }

    }

    /**
     * Executes this worker thread and shows a dialog displaying the progress of
     * the tree generation
     * 
     * @param parent
     *            the parent window the progress dialog should be associated
     *            with
     */
    public void executeWithProgressDialog(JDialog parent) {
        this.parent = parent;

        final ProgressPanel<Tree> panel = new ProgressPanel<Tree>(String.format("<html>%s<br>&nbsp;</html>",
                _("ScaffoldTreeGeneration.ProcessMessage.PleaseWait")));
        panel.setBorder(null);

        final TreegenProgressDialog dialog = new TreegenProgressDialog(parent,
                _("ScaffoldTreeGeneration.ProcessMessage.Title"), panel, ModalityType.MODELESS, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        cancel(true);
                    }
                });

        class FinishedListener implements ProgressListener<Tree> {
            private int max = 0;

            @Override
            public void setProgressValue(int progress) {
                panel.setLabelText(String.format("<html>%s<br>%s</html>",
                        _("ScaffoldTreeGeneration.ProcessMessage.PleaseWait"),
                        _("ScaffoldTreeGeneration.ProcessMessage.Processing", progress, max)));
            }

            @Override
            public void setProgressBounds(int min, int max) {
                this.max = max;
            }

            @Override
            public void setProgressIndeterminate(boolean indeterminate) {
                if (max != 0) {
                    panel.setLabelText(String.format("<html>%s</html>",
                            _("ScaffoldTreeGeneration.ProcessMessage.Saving")));
                }
            }

            @Override
            public void finished(Tree result, boolean cancelled) {
                if (cancelled) {
                    panel.setLabelText(_("ScaffoldTreeGeneration.ProcessMessage.Cancelling"));
                    panel.setProgressIndeterminate(true);
                    dialog.dispose();
                } else {
                    panel.setLabelText(_("ScaffoldTreeGeneration.ProcessMessage.Finished"));
                    panel.setProgressIndeterminate(false);
                }
            }
        }

        Thread progressThread = new Thread(new Runnable() {
            @Override
            public void run() {
                dialog.setModal(true);
                dialog.setVisible(true);
            }
        });

        treeGen.addPropertyChangeListener(ScaffoldTreeGenerator.PROPERTY_PROGRESS, dialog);

        progressThread.start();
        this.addProgressListener(new FinishedListener());
        this.addProgressListener(panel);
        this.addProgressListener(dialog);
        this.addExceptionListener(new TreeGenerationExceptionListener(dialog));
        this.execute();
    }

    private class TreeGenerationExceptionListener implements WorkerExceptionListener {
        JDialog dialog;

        TreeGenerationExceptionListener(JDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public ExceptionHandlerResult exceptionThrown(Throwable e) {

            if (e instanceof ScaffoldTreeGenerationException) {
                logger.warn("Errors occured during scaffold tree generation.", e);
                JOptionPane.showMessageDialog(parent, e.getMessage(), _("ScaffoldTreeGeneration.Error.Title"),
                        JOptionPane.ERROR_MESSAGE);
            } else {
                StringWriter string = new StringWriter();
                e.printStackTrace(new PrintWriter(string));

                logger.error("Unexpected error occured: {}", string.toString());
                JOptionPane.showMessageDialog(parent, "Please report a bug with the following information:\n\n"
                        + string.toString(), "Unexpected error occured", JOptionPane.ERROR_MESSAGE);
            }
            dialog.dispose();
            return ExceptionHandlerResult.STOP;
        }
    }
}
