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

package edu.udo.scaffoldhunter.model.datacalculation;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractListModel;
import javax.swing.JOptionPane;
import javax.swing.ListModel;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import edu.udo.scaffoldhunter.gui.data.MessageDialog;
import edu.udo.scaffoldhunter.gui.datacalculation.CalcDialog;
import edu.udo.scaffoldhunter.gui.datacalculation.CalcDialog.Result;
import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.gui.util.UnaryDBFunction;
import edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener;
import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.SHPluginManager;
import edu.udo.scaffoldhunter.plugins.datacalculation.AbstractCalcPluginArguments;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * A control class responsible for the construction of a new dataset or for
 * adding properties to an existing one
 * 
 * @author Henning Garus
 * @author Philipp Lewe
 * 
 */
public class Calculator {

    private static final Logger logger = LoggerFactory.getLogger(Calculator.class);

    private final SHPluginManager pluginManager = new SHPluginManager();
    private final ImmutableList<CalcPlugin> plugins;
    private final CalcProcess calcProcess;
    private final Map<String, PropertyDefinition> existingPropertyDefinitions;

    /**
     * Mapping: DB PropertyDefinition -> copied Property Definition (Input for
     * plugin)
     */
    private Map<PropertyDefinition, PropertyDefinition> availableProperties;

    /**
     * Prefix used to store plugin
     */
    public final static String CALC_PLUGINS_PROPERTY_KEY_PREFIX = "SH_MOL_CALC";

    private final static String LARGESTFRAGMENT_KEY = "LARGEST_FRAGMENT";
    private final static String LARGESTFRAGMENT_TITLE = "largest fragment";
    private final static String DEGLYCOSILATED_KEY = "DEGLYCOSILATED";
    private final static String DEGLYCOSILATED_TITLE = "deglycosilated";
    private final static String RECALC2DCOORDS_KEY = "2D_COORDS_RECALCULATED";
    private final static String RECALC2DCOORDS_TITLE = "2d coordinates recalculated";

    /**
     * Mapping: Plugin PropertyDefinition (Output from plugin) -> validated new
     * DB Property Definition
     */
    private final Map<PropertyDefinition, PropertyDefinition> registeredPropertyDefinitions = Maps.newHashMap();

    private final DbManager db;

    private Dataset dataset;

    private CalculateWorker calculateWorker;

    /**
     * Create a new Calculator
     * 
     * @param db
     *            the db manager
     * 
     * @param dataset
     *            the dataset where calculated properties should be inserted
     * 
     */
    public Calculator(DbManager db, Dataset dataset) {
        this.db = db;
        this.dataset = dataset;
        Ordering<CalcPlugin> o = new PluginByTitleOrdering();
        plugins = o.immutableSortedCopy(pluginManager.getCalcPlugins());
        existingPropertyDefinitions = dataset.getPropertyDefinitions();

        // only molecule property definitions should be available to the plugins
        availableProperties = deepCopy(Iterables.filter(existingPropertyDefinitions.values(),
                new MoleculePropertyDefinitionPredicate()));

        // initialize all plugins with a immutable copy of available property
        // definitions
        for (CalcPlugin plugin : plugins) {
            plugin.setAvailableProperties(ImmutableSet.copyOf(availableProperties.values()));
        }

        calcProcess = new CalcProcess();
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
     * Makes a deep copy of all property definitions
     * 
     * @param propertyDefinitions
     *            the property definitions of an existing dataset
     * @return a map mapping from input property definitions to copies of them
     */
    private Map<PropertyDefinition, PropertyDefinition> deepCopy(Iterable<PropertyDefinition> propertyDefinitions) {
        Map<PropertyDefinition, PropertyDefinition> copiedMap = Maps.newHashMap();

        for (PropertyDefinition propDef : propertyDefinitions) {
            PropertyDefinition copy = new PropertyDefinition(propDef);
            // remove reference on existing dataset
            copy.setDataset(null);
            copiedMap.put(propDef, copy);
        }
        return copiedMap;
    }

    /**
     * Creates a validated and registered copy of the given
     * {@link PropertyDefinition}. First the given {@link PropertyDefinition} is
     * copied. Then it will be modified so that the
     * {@link PropertyDefinition#getKey()} doesn't collide with any existing
     * {@link PropertyDefinition} in the dataset. At last the correct dataset is
     * set and the propDef is registered with the copy in
     * {@link #registeredPropertyDefinitions}
     * 
     * @param propDef
     *            the propDef to copy, validate and register
     */
    private void copyValidateAndRegister(CalcJob job, PropertyDefinition propDef) {
        Preconditions.checkNotNull(job);
        Preconditions.checkNotNull(propDef);

        // only accept molecule properties
        if (!propDef.isScaffoldProperty()) {
            PropertyDefinition copy = new PropertyDefinition(propDef);

            List<String> keyList = Lists.newArrayListWithCapacity(3);
            List<String> titleList = Lists.newArrayListWithCapacity(3);
            StringBuilder keyAddition = new StringBuilder();
            StringBuilder titleAddition = new StringBuilder();

            if (job.getPluginArguments() instanceof AbstractCalcPluginArguments) {
                AbstractCalcPluginArguments arguments = (AbstractCalcPluginArguments) job.getPluginArguments();

                if (arguments.isUseLargestFragments()) {
                    keyList.add(LARGESTFRAGMENT_KEY);
                    titleList.add(LARGESTFRAGMENT_TITLE);
                }
                if (arguments.isDeglycosilate()) {
                    keyList.add(DEGLYCOSILATED_KEY);
                    titleList.add(DEGLYCOSILATED_TITLE);
                }
                if (arguments.isRecalculate2Dcoords()) {
                    keyList.add(RECALC2DCOORDS_KEY);
                    titleList.add(RECALC2DCOORDS_TITLE);
                }

                assert keyList.size() == titleList.size();

                for (String key : keyList) {
                    keyAddition.append("_");
                    keyAddition.append(key);
                }
                Iterator<String> titleIter = titleList.iterator();
                while (titleIter.hasNext()) {
                    titleAddition.append(titleIter.next());
                    if (titleIter.hasNext()) {
                        titleAddition.append(", ");
                    }
                }

                if (!titleList.isEmpty()) {
                    titleAddition.insert(0, "(");
                    titleAddition.append(")");
                }

            }

            // add key prefix
            copy.setKey(CALC_PLUGINS_PROPERTY_KEY_PREFIX + keyAddition.toString() + "_" + copy.getKey().toUpperCase());
            copy.setTitle(copy.getTitle() + titleAddition.toString());

            // search for an unused key
            String key = copy.getKey();
            String title = copy.getTitle();
            int count = 1;
            while (existingPropertyDefinitions.containsKey(key)) {
                key = String.format("%s-%d", copy.getKey(), count);
                title = String.format("%s-%d", copy.getTitle(), count);
                count++;
            }

            copy.setKey(key);
            copy.setTitle(title);
            copy.setDataset(dataset);

            registeredPropertyDefinitions.put(propDef, copy);
            existingPropertyDefinitions.put(copy.getKey(), copy);

            logger.trace("registered new property. title: {}, key: {}", copy.getTitle(), copy.getKey());
        }
    }

    private void cleanUp() {
        existingPropertyDefinitions.clear();
        existingPropertyDefinitions.putAll(dataset.getPropertyDefinitions());

        // remove saved property definitions
        try {
            db.deleteAll(registeredPropertyDefinitions.values());
        } catch (DatabaseException e) {
            logger.warn("cleanup of property definitions failed", e);
        }
        registeredPropertyDefinitions.clear();
    }

    /**
     * start the calc process
     * 
     * @param owner
     *            the owner of the dialogs which will be shown during
     *            calculation
     */
    public void runCalc(Window owner) {
        final CalcDialog calcDialog;
        final ImmutableCollection<Molecule> dbMolecules;
        final Iterable<IAtomContainer> cdkMoleculeIterable;

        dbMolecules = ImmutableList.copyOf(DBExceptionHandler.callDBManager(db,
                new UnaryDBFunction<Collection<Molecule>, Dataset>(dataset) {
                    @Override
                    public Collection<Molecule> call(Dataset dataset) throws DatabaseException {
                        return db.getAllMolecules(dataset);
                    }
                }));

        cdkMoleculeIterable = Iterables.transform(dbMolecules, new DBMoleculeToCDKMoleculeTransform(db,
                availableProperties));

        calcDialog = new CalcDialog(owner, getPluginListModel(), calcProcess);
        calcDialog.setVisible(true);

        if (calcDialog.getResult() == Result.CANCEL) {
            return;
        }

        assert (calcDialog.getResult() == Result.START_CALCULATION);

        final MessageDialog messageDialog = new MessageDialog(owner, MessageDialog.CALCULATION, new ActionListener() {
            // Handle cancel events from the MessageDialog here
            // otherwise we have the control logic for cancel in the
            // dialog, which would be more weird than this already
            // is.
            @Override
            public void actionPerformed(ActionEvent e) {
                logger.trace("user cancelled or closed calculation");
                // if the worker is finished cancel does nothing
                calculateWorker.cancel(true);
            }
        });

        calculateWorker = new CalculateWorker(cdkMoleculeIterable, dbMolecules, messageDialog);
        calculateWorker.addProgressListener(messageDialog);
        calculateWorker.addProgressListener(new ProgressListener<Void>() {

            @Override
            public void setProgressValue(int progress) {
            }

            @Override
            public void setProgressIndeterminate(boolean indeterminate) {
            }

            @Override
            public void setProgressBounds(int min, int max) {
            }

            @Override
            public void finished(Void result, boolean cancelled) {
                if (!cancelled) {
                    messageDialog.setText(_("DataCalc.CalcFinished"));
                } else {
                    messageDialog.dispose();
                }
            }
        });
        calculateWorker.addExceptionListener(new CalculateExceptionHandler(owner, messageDialog));
        calculateWorker.execute();
        messageDialog.setVisible(true);
    }

    private class CalculateExceptionHandler implements WorkerExceptionListener {
        Window owner;
        MessageDialog messageDialog;

        CalculateExceptionHandler(Window owner, MessageDialog messageDialog) {
            this.owner = owner;
            this.messageDialog = messageDialog;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener#exceptionThrown
         * (edu.udo.scaffoldhunter.gui.util.ProgressWorker, java.lang.Throwable)
         */
        @Override
        public ExceptionHandlerResult exceptionThrown(Throwable e) {
            if (e instanceof CalculationException) {
                messageDialog.dispose();

                logger.warn("Errors occured during Calculation.", e);
                JOptionPane.showMessageDialog(owner, _("DataCalc.CalculationError.Message"),
                        _("DataCalc.CalculationError.Title"), JOptionPane.ERROR_MESSAGE);
            } else {
                StringWriter string = new StringWriter();
                e.printStackTrace(new PrintWriter(string));

                messageDialog.dispose();
                logger.warn("Unexpected error occured", e);
                JOptionPane.showMessageDialog(owner,
                        "Please report a bug with the following information:\n\n" + string.toString(),
                        "Unexpected error occured", JOptionPane.ERROR_MESSAGE);
            }
            return WorkerExceptionListener.ExceptionHandlerResult.STOP;
        }
    };

    private class CalculateWorker extends ProgressWorker<Void, Message> implements MessageListener {
        int totalProgress = 0;
        int progressBoundsMax;
        int moleculeCount = 0;

        Iterable<IAtomContainer> cdkMoleculeIterable;
        ImmutableCollection<Molecule> dbMolecules;
        MessageDialog messageDialog;

        CalculateWorker(Iterable<IAtomContainer> cdkMoleculeIterable,
                ImmutableCollection<Molecule> dbMolecules, MessageDialog messageDialog) {
            this.cdkMoleculeIterable = cdkMoleculeIterable;
            this.dbMolecules = dbMolecules;
            this.messageDialog = messageDialog;

            progressBoundsMax = calcProcess.getJobs().size() * dbMolecules.size();
        }

        @Override
        protected Void doInBackground() throws Exception {
            setProgressIndeterminate(true);

            try {
                calcProcess.addMessageListener(this);
                calcProcess.prepareCalc(cdkMoleculeIterable);

                // register property definitions of all jobs
                for (CalcJob job : calcProcess.getJobs()) {
                    for (PropertyDefinition propDef : job.getResults().getCalculatedProperties()) {
                        copyValidateAndRegister(job, propDef);
                    }
                }

                db.saveAllAsNew(registeredPropertyDefinitions.values());

                setProgressBounds(0, progressBoundsMax);
                setProgressIndeterminate(false);

                int jobcount = 0;
                for (CalcJob job : calcProcess.getJobs()) {
                    PluginPropertyIterator propertyIter = new PluginPropertyIterator(job.getResults().getMolecules()
                            .iterator(), dbMolecules.iterator(), ImmutableMap.copyOf(registeredPropertyDefinitions));
                    jobcount++;

                    moleculeCount = 0;
                    while (propertyIter.hasNext()) {
                        moleculeCount++;
                        totalProgress++;
                        setProgressValue(totalProgress);

                        messageDialog.setText("<html><div align=\"center\">"
                                + _("DataCalc.RunningNthJob", jobcount, calcProcess.getJobs().size(), job) + "<br>"
                                + _("DataCalc.CalculatingNthMolecule", moleculeCount, dbMolecules.size())
                                + "</div></html>");

                        db.saveAllAsNew(propertyIter.next());

                        // exit on "normal" cancel
                        if (Thread.interrupted()) {
                            cancel();
                            return null;
                        }
                    }
                }
            } catch (DBMoleculeToCDKMoleculeTransformException e) {
                cancel();
                throw new CalculationException(e);
            } catch (DatabaseException e) {
                cancel();
                throw new CalculationException(e);
            } catch (RuntimeException e) {
                cancel();
                throw new RuntimeException(e);
            } catch (Exception e) {
                cancel();
                throw new Exception(e);
            }

            return null;
        }

        private void cancel() {
            messageDialog.setText(_("DataCalc.CalcCanceled"));
            calcProcess.removeMessageListener(this);
            cleanUp();
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

    private static class PluginByTitleOrdering extends Ordering<CalcPlugin> implements Serializable {

        @Override
        public int compare(CalcPlugin left, CalcPlugin right) {
            return String.CASE_INSENSITIVE_ORDER.compare(left.getTitle(), right.getTitle());
        }
    }

    private static class MoleculePropertyDefinitionPredicate implements Predicate<PropertyDefinition> {

        /*
         * (non-Javadoc)
         * 
         * @see com.google.common.base.Predicate#apply(java.lang.Object)
         */
        @Override
        public boolean apply(PropertyDefinition propDef) {
            return !propDef.isScaffoldProperty();
        }

    }
}