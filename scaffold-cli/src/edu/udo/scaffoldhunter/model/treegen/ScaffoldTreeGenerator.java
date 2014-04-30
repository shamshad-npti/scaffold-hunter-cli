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

package edu.udo.scaffoldhunter.model.treegen;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import junit.framework.Assert;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.silent.Molecule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.RuleType;
import edu.udo.scaffoldhunter.model.datacalculation.Calculator;
import edu.udo.scaffoldhunter.model.dataimport.Importer;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.DbObject;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.ScaffoldNumProperty;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.db.Tree;
import edu.udo.scaffoldhunter.model.treegen.prioritization.ScaffoldPrioritization;
import edu.udo.scaffoldhunter.model.util.SVGGen;
import edu.udo.scaffoldhunter.model.util.SVGGenResult;

/**
 * Class for generating scaffold trees and storing them in the database
 * 
 * @author Philipp Lewe
 * @author Till Schäfer
 * 
 */
public class ScaffoldTreeGenerator {
    private static Logger logger = LoggerFactory.getLogger(ScaffoldTreeGenerator.class);
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);

    /**
     * The prefix all scaffold property keys begin with. The {@link Importer}
     * and the {@link Calculator} should never allow creation of any property
     * keys beginning with this prefix
     */
    public static final String SCAFFOLD_PROPERTY_KEY_PREFIX = "SH_SC_";

    private Progress progress;

    /**
     * String for the progress property for the use by
     * <code>PropertyChangeListeners</code>
     */
    public static final String PROPERTY_PROGRESS = "progress";

    private final DbManager db;

    private PropertyCalculator[] propCalcs = { new SCPnoLinkerBonds(), new SCPdelta(), new SCPabsDelta(),
            new SCPnoAroRings(), new SCPnoHetAt(), new SCPnoNAt(), new SCPnoOAt(), new SCPnoSAt(), new SCPnoRings() };

    /*
     * Mapping of smiles-strings to scaffolds
     */
    private Map<String, Scaffold> scaffolds;

    /*
     * Mapping from scaffolds to all the molecules that lead to their (the
     * scaffolds) creation.
     */
    private Multimap<Scaffold, edu.udo.scaffoldhunter.model.db.Molecule> generationMolecules;

    /*
     * Collection of all properties
     */
    private Collection<Property> properties;

    /*
     * Mapping from all property definition keys to the corresponding property
     * definition. Used to collect all property definitions and access them
     * efficiently based on their keys.
     */
    private Map<String, PropertyDefinition> propertyDefs;

    /*
     * List with all new property definitions
     */
    private List<PropertyDefinition> newPropDefs;

    /**
     * Creates a new scaffold tree generator
     * 
     * @param db
     *            the DB manager
     */
    public ScaffoldTreeGenerator(DbManager db) {
        this.db = db;
    }

    /**
     * Generates a new scaffold tree and stores it in the database. Note: This
     * function is not threadsave, do not call it synchronously.
     * 
     * @param profile
     *            the user profile which creates the tree
     * @param dataset
     *            the dataset for which the tree should be generated
     * @param genOptions
     *            the options for tree generation
     * @return the newly generated {@link Tree} or null if the tree generation
     *         was cancelled
     * @throws ScaffoldTreeGenerationException
     */
    public Tree generateAndStoreTree(Profile profile, Dataset dataset, GeneratorOptions genOptions)
            throws ScaffoldTreeGenerationException {
        Tree tree;
        Session session;
        Subset subset;
        Set<edu.udo.scaffoldhunter.model.db.Molecule> molecules;
        ScaffoldPrioritization scaffoldSelector;
        MDLReader reader;
        String molString;
        IMolecule mol;

        ScaffoldContainer murckoScaffold;
        Vector<ScaffoldContainer> allParents;
        ScaffoldContainer parent;
        Progress oldProgress;

        scaffolds = new Hashtable<String, Scaffold>();
        generationMolecules = LinkedHashMultimap.create();
        properties = new LinkedList<Property>();
        propertyDefs = dataset.getPropertyDefinitions();
        newPropDefs = new LinkedList<PropertyDefinition>();

        logger.debug("scaffold tree generation started");

        setupPropertyDefinitions(dataset);

        tree = createTree(profile, dataset, genOptions);

        session = new Session();
        session.setTree(tree);

        try {
            subset = db.getRootSubset(session);
        } catch (DatabaseException e1) {
            throw new ScaffoldTreeGenerationException(_("ScaffoldTreeGeneration.Exception.DatabaseConnectionLost"));
        }

        molecules = subset.getMolecules();
        progress = new Progress(0, molecules.size(), false);

        scaffoldSelector = new ScaffoldPrioritization();

        if (genOptions.isCustomrules()) {
            scaffoldSelector.setCustomRules(genOptions.getRuleset());
        }

        // Calculate scaffolds for each molecule in the dataset
        int count = 0;
        for (edu.udo.scaffoldhunter.model.db.Molecule molecule : molecules) {
            count++;

            if (molecule.getSmiles().isEmpty()) {
                logger.warn(_("ScaffoldTreeGeneration.Warning.MoleculeEmptySmiles", molecule.getId()));
                progress.addErrorMessage(_("ScaffoldTreeGeneration.Warning.MoleculeEmptySmiles", molecule.getId()));
                continue;
            } else {
                logger.debug("Processed Molecule: {}", molecule.getSmiles());
            }

            // inform listeners about process
            oldProgress = progress.clone();
            progress.setProcessedMolecules(count);
            changes.firePropertyChange(ScaffoldTreeGenerator.PROPERTY_PROGRESS, oldProgress, progress.clone());

            try { // try to read molecule structure
                molString = db.getStrucMol(molecule);

                reader = new MDLReader(new StringReader(molString));
                mol = new Molecule();
                mol = reader.read(mol);

                // only the largest fragment is used to build the scaffold tree
                // the rest (i.e. solvents) is ignored
                mol = (IMolecule)CDKHelpers.getLargestFragment(mol);

                // We do not prepare the molecule here because this is not required
                // for scaffold generation/deglycosilation performed in ScaffoldContainer.
                // However, preparing the molecule here causes bugs because some atoms
                // seem to remain in an erroneous state after pruning side chains for
                // scaffold generation.
                //MoleculeConfigurator.prepare(mol, false);

                // MurckoScaffold
                murckoScaffold = new ScaffoldContainer(mol, true, genOptions.isDeglycosilate());

                logger.debug("MurckoScaffold: {}", murckoScaffold.getSMILES());

                if (murckoScaffold.getSCPnumRings() > 0 && !murckoScaffold.getSMILES().isEmpty()) {

                    // Insert murcko scaffold
                    boolean isNew = addNewScaffold(murckoScaffold, molecule, tree);
                    if (!isNew) continue;

                    // ## Generation parent scaffolds
                    allParents = murckoScaffold.getAllParentScaffolds();

                    while (allParents.size() > 0) {
                        parent = null;

                        if (genOptions.isCustomrules()) {
                            parent = scaffoldSelector.selectParentScaffoldCustomRules(allParents);
                        } else {
                            parent = scaffoldSelector.selectParentScaffoldOriginalRules(allParents);
                        }

                        /*
                         * insert new scaffold if it isn't new: break loop of
                         * parent generation, if parent is already generated
                         */
                        if (!addNewScaffold(parent, null, tree)) {
                            break;
                        }

                        logger.debug("Parent Scaffold: {}", parent.getSMILES());

                        allParents = parent.getAllParentScaffolds();
                    }
                }
            } catch (CDKException e) {
                // molecule structure could not be read
                progress.addErrorMessage(_("ScaffoldTreeGeneration.Warning.StructureCorrupt", molecule.getSmiles()));
            } catch (DatabaseException e) {
                // molecule structure could not be loaded
                throw new ScaffoldTreeGenerationException(_("ScaffoldTreeGeneration.Exception.DatabaseConnectionLost"));
            }

            if (Thread.interrupted()) {
                return null;
            }
        }

        // inform listeners about saving
        oldProgress = progress.clone();
        progress.setSaving(true);
        changes.firePropertyChange(ScaffoldTreeGenerator.PROPERTY_PROGRESS, oldProgress, progress.clone());

        createImaginaryRootAndHierarchyLevels(tree);

        // save tree
        try {
            writeToDatabase(tree);
        } catch (DatabaseException e) {
            cleanup(tree);
            throw new ScaffoldTreeGenerationException(_("ScaffoldTreeGeneration.Exception.DatabaseConnectionLost"));
        }
        logger.debug("scaffold tree generation finished");

        return tree;
    }

    /**
     * Generates a new scaffold tree object for the given
     * 
     * @param profile
     *            the user profile which creates the tree
     * @param dataset
     *            the dataset for which the tree should be generated
     * @param genOptions
     *            the generation options
     * @return a reference to the new tree
     */
    private Tree createTree(Profile profile, Dataset dataset, GeneratorOptions genOptions) {
        Tree tree = new Tree();
        tree.setCreatedBy(profile);
        tree.setDataset(dataset);
        if (genOptions.isCustomrules()) {
            tree.setRuleset(genOptions.getRuleset());
        }
        tree.setTitle(genOptions.getTitle());
        tree.setComment(genOptions.getComment());
        tree.setCreationDate(new Date());
        tree.setDeglycosilate(genOptions.isDeglycosilate());

        return tree;
    }

    /**
     * Adds the Scaffold and properties to the collection of scaffolds (if it
     * does not already exist)
     * 
     * @param scaffoldContainer
     *            the {@link ScaffoldContainer} which holds the scaffold that
     *            should be stored in the database
     * @param molecule
     *            the molecule which lead to the generation of the
     *            scaffoldContainer or null if another scaffold lead to the
     *            generation
     * @param tree
     *            the tree in which the new scaffold should be inserted
     * @return boolean value indicating whether the scaffold is inserted for
     *         first time (true = first time, false = already existing in
     *         database)
     */
    private boolean addNewScaffold(ScaffoldContainer scaffoldContainer,
            edu.udo.scaffoldhunter.model.db.Molecule molecule, Tree tree) {
        String smiles = scaffoldContainer.getSMILES();

        if (smiles.isEmpty()) {
            throw new AssertionError("smiles string for scaffold is empty");
        }

        // if scaffold exists
        if (scaffolds.containsKey(smiles)) {
            Scaffold scaffold = scaffolds.get(smiles);

            if (molecule == null) { // scaffold is generated by pruning another scaffold
                Scaffold childScaffold = scaffolds.get(scaffoldContainer.getChildSmiles());
                if (childScaffold == null) {
                    throw new AssertionError(
                            "Error: Referencing unknown smiles string - child scaffold does not exist in collection of scaffolds");
                }
                if (childScaffold.getParent() != null && childScaffold.getParent() != scaffold) {
                    throw new AssertionError("Error: Non-unique parent scaffold.");
                }
                scaffold.getChildren().add(childScaffold);
                childScaffold.setParent(scaffold);
            } else {
                generationMolecules.put(scaffold, molecule);
            }

            return false;

        } // if scaffold is new
        else {
            SVGGenResult svgResult = SVGGen.getSVG(scaffoldContainer);

            Scaffold scaffold = new Scaffold();
            scaffold.setStrucMol(scaffoldContainer.getMDLCTab());
            scaffold.setTree(tree);
            scaffold.setSmiles(smiles);
            scaffold.setTitle(smiles);
            scaffold.setSvgString(svgResult.getSvgString());
            scaffold.setSvgHeight(svgResult.getHeight());
            scaffold.setSvgWidth(svgResult.getWidth());
            // TODO:
            // scaffold.setDeletionRuleParent(scaffoldContainer.getDeletionRule());

            addScaffoldProperties(scaffoldContainer, scaffold);

            // add scaffold to scaffold map
            scaffolds.put(scaffoldContainer.getSMILES(), scaffold);

            if (molecule == null) {
                Assert.assertTrue(
                        "Error: referencing unknown smiles string - child scaffold does not exist in collection of scaffolds",
                        scaffolds.containsKey(scaffoldContainer.getChildSmiles()));
                Scaffold childScaffold = scaffolds.get(scaffoldContainer.getChildSmiles());
                if (childScaffold.getParent() != null && childScaffold.getParent() != scaffold) {
                    throw new AssertionError("Error: Non-unique parent scaffold.");
                }
                scaffold.getChildren().add(childScaffold);
                childScaffold.setParent(scaffold);
            } else {
                generationMolecules.put(scaffold, molecule);
            }

            return true;
        }

    }

    /**
     * Creates all missing property definitions and adds them to the collection
     * of properties
     * 
     * @param dataset
     *            the dataset to which the properties should be belong
     */
    private void setupPropertyDefinitions(Dataset dataset) {
        // create 'new' scaffold properties
        for (int k = 0; k < propCalcs.length; k++) {
            if (!propertyDefs.containsKey(propCalcs[k].getKey())) {
                PropertyDefinition propdef = new PropertyDefinition();

                propdef.setDescription(propCalcs[k].getDescription());
                propdef.setKey(propCalcs[k].getKey());
                propdef.setMappable(true);
                propdef.setPropertyType(PropertyType.NumProperty);
                propdef.setScaffoldProperty(true);
                propdef.setTitle(propCalcs[k].getTitle());
                propdef.setDataset(dataset);

                newPropDefs.add(propdef);
                // add new property into lookup table
                propertyDefs.put(propdef.getKey(), propdef);

                /**
                 * update properties definitions list
                 */
                dataset.getPropertyDefinitions().put(propdef.getKey(), propdef);
            }
        }
    }

    /**
     * Adds the properties of the given scaffold to the collection of properties
     * 
     * @param sc
     *            the <code>ScaffoldContainer</code> which holds the properties
     * @param scaffold
     *            the database object to which the property is associated
     */
    private void addScaffoldProperties(ScaffoldContainer sc, Scaffold scaffold) {

        /*
         * create a scaffold property for each property calculator and fill it
         * with the value returned by the calculation
         */
        for (PropertyCalculator propCalc : propCalcs) {
            PropertyDefinition propDef = propertyDefs.get(propCalc.getKey());

            ScaffoldNumProperty prop = new ScaffoldNumProperty();
            prop.setType(propDef);
            prop.setValue(propCalc.calculate(sc));
            prop.setScaffold(scaffold);

            // add to collection
            properties.add(prop);
        }
    }

    /**
     * Sets the right hierarchy level for all scaffolds
     * 
     * @param root
     *            the root scaffold of the tree
     */
    private static void setHierarchyLevels(Scaffold root) {
        List<Scaffold> processingList = new LinkedList<Scaffold>();

        root.setHierarchyLevel(0);
        processingList.add(root);

        Scaffold s;
        while (!processingList.isEmpty()) {
            s = processingList.remove(0);

            for (Scaffold scaffold : s.getChildren()) {
                scaffold.setHierarchyLevel(s.getHierarchyLevel() + 1);
                processingList.add(scaffold);
            }

        }
    }

    /**
     * Writes the collected scaffolds and their properties to the database
     * 
     * @param tree
     *            the scaffold tree
     * @throws DatabaseException
     */
    private void writeToDatabase(Tree tree) throws DatabaseException {
        // save new property definitions, the tree, the scaffolds and the
        // scaffold properties
        Iterable<DbObject> toSave = Iterables.concat(newPropDefs, Collections.singletonList(tree), scaffolds.values(),
                properties);

        // TODO ask the user to retry on error
        db.saveAllAsNew(toSave);
    }

    private void cleanup(Tree tree) {
        try {
            db.delete(tree);
        } catch (DatabaseException e) {
            logger.warn("cleanup after tree generation failure failed", e);
        }
    }

    private void createImaginaryRootAndHierarchyLevels(Tree tree) {
        List<Scaffold> treeRoots = Lists.newArrayList();
        Scaffold imaginaryRoot = new Scaffold();

        // set the list of associated molecules for each murcko scaffold
        for (Scaffold scaffold : generationMolecules.keySet()) {
            Collection<edu.udo.scaffoldhunter.model.db.Molecule> c = generationMolecules.get(scaffold);
            Set<edu.udo.scaffoldhunter.model.db.Molecule> s = new HashSet<edu.udo.scaffoldhunter.model.db.Molecule>(c);
            scaffold.setGenerationMolecules(s);
        }

        // collect all roots of unconnected scaffold trees
        for (Scaffold scaffold : scaffolds.values()) {
            if (scaffold.getParent() == null) {
                treeRoots.add(scaffold);
                scaffold.setParent(imaginaryRoot);
            }
        }

        // create and add virtual root to scaffold map
        imaginaryRoot.setStrucMol("this is an imaginary root scaffold and has no structure information");
        imaginaryRoot.setSmiles("this is an imaginary root scaffold and has no smiles");
        imaginaryRoot.setSvgString("this is an imaginary root scaffold and has no svg string");
        imaginaryRoot.setChildren(treeRoots);
        imaginaryRoot.setTree(tree);
        scaffolds.put(imaginaryRoot.getSmiles(), imaginaryRoot);

        // calculate and set hierarchy levels
        setHierarchyLevels(imaginaryRoot);
    }

    /**
     * Add a PropertyChangeListener for a specific property. The listener will
     * be invoked only when a call on firePropertyChange names that specific
     * property.
     * 
     * @param propertyName
     *            the name of the property to listen on.
     * @param listener
     *            the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changes.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a PropertyChangeListener for a specific property.
     * 
     * @param propertyName
     *            the name of the property that was listened on.
     * @param listener
     *            the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changes.removePropertyChangeListener(propertyName, listener);
    }

    /**
     * Wrapper class to store the progress of the
     * <code>ScaffoldTreeGenerator</code>
     * 
     * @author Philipp Lewe
     * 
     */
    public class Progress {
        private int processedMolecules;
        private int totalMolecules;
        private boolean isSaving;
        private List<String> errorList;

        /**
         * Creates a new Progress
         * 
         * @param processedMolecules
         *            the number of processed molecules
         * @param totalMolecules
         *            the total number of molecules
         * @param isSaving
         *            boolean value indicating that the generator is actually
         *            saving the tree to the database
         */
        public Progress(int processedMolecules, int totalMolecules, boolean isSaving) {
            setProcessedMolecules(processedMolecules);
            setTotalMolecules(totalMolecules);
            setSaving(isSaving);
            errorList = new LinkedList<String>();
        }

        private Progress(int processedMolecules, int totalMolecules, boolean isSaving, List<String> errorList) {
            setProcessedMolecules(processedMolecules);
            setTotalMolecules(totalMolecules);
            setSaving(isSaving);
            setErrorList(errorList);
        }

        /**
         * Returns the number of processed molecules
         * 
         * @return the number of processed molecules
         */
        public int getProcessedMolecules() {
            return processedMolecules;
        }

        /**
         * Sets the number of processed molecules
         * 
         * @param processedMolecules
         *            the number of processed molecules
         */
        void setProcessedMolecules(int processedMolecules) {
            this.processedMolecules = processedMolecules;
        }

        /**
         * Returns the total number of molecules
         * 
         * @return the total number of molecules
         */
        public int getTotalMolecules() {
            return totalMolecules;
        }

        /**
         * Sets the total number of molecules
         * 
         * @param totalMolecules
         *            the total number of molecules
         */
        void setTotalMolecules(int totalMolecules) {
            this.totalMolecules = totalMolecules;
        }

        /**
         * Sets the isSaving property
         * 
         * @param isSaving
         */
        public void setSaving(boolean isSaving) {
            this.isSaving = isSaving;
        }

        /**
         * Returns value indicating that the tree generation is actually saving
         * 
         * @return true if the tree generation is actually saving the tree
         */
        public boolean isSaving() {
            return isSaving;
        }

        /**
         * @param errorList
         *            the errorList to set
         */
        public void setErrorList(List<String> errorList) {
            this.errorList = errorList;
        }

        /**
         * @return the errorList
         */
        public List<String> getErrorList() {
            return errorList;
        }

        /**
         * Adds the given error message to this progress
         * 
         * @param msg
         *            the error message
         */
        public void addErrorMessage(String msg) {
            errorList.add(msg);
        }

        @Override
        public Progress clone() {
            List<String> list = new LinkedList<String>();
            list.addAll(errorList);
            return new Progress(processedMolecules, totalMolecules, isSaving, list);
        }

    };

    /**
     * Convenience class to define title, description and calculation method of
     * a scaffold property. All subclasses have to override the calculate method
     * with their specific calculation and have to override the default
     * constructor to set title and description of the property.
     * 
     * @author Philipp Lewe
     */
    private abstract class PropertyCalculator {
        private RuleType ruletype;

        /**
         * Creates a new PropertyCalculator for the given RuleType
         * 
         * @param ruletype
         *            the ruletype the calculation function calculates
         * 
         */
        private PropertyCalculator(RuleType ruletype) {
            this.ruletype = ruletype;
        }

        /**
         * Calculates and returns the property of the given
         * <code>ScaffoldContainer</code>
         * 
         * @param sc
         *            the <code>ScaffoldContainer</code> for which the property
         *            should be calculated
         * @return the <code>int</code> result of the calculation
         */
        public abstract int calculate(ScaffoldContainer sc);

        /**
         * @return the key of this property
         */
        public String getKey() {
            return (SCAFFOLD_PROPERTY_KEY_PREFIX + ruletype.name().toUpperCase());
        }

        /**
         * @return the title of this property
         */
        public String getTitle() {
            return ruletype.name();
        }

        /**
         * @return the description of this property
         */
        public String getDescription() {
            return ruletype.getDescription();
        }
    }

    private class SCPnoLinkerBonds extends PropertyCalculator {
        public SCPnoLinkerBonds() {
            super(RuleType.SCPnoLinkerBonds);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPnumALB();
        }
    }

    private class SCPdelta extends PropertyCalculator {
        public SCPdelta() {
            super(RuleType.SCPdelta);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPdelta();
        }
    }

    private class SCPabsDelta extends PropertyCalculator {
        public SCPabsDelta() {
            super(RuleType.SCPabsDelta);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPabsDelta();
        }
    }

    private class SCPnoAroRings extends PropertyCalculator {
        public SCPnoAroRings() {
            super(RuleType.SCPnoAroRings);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPnumAroRings();
        }
    }

    private class SCPnoHetAt extends PropertyCalculator {
        public SCPnoHetAt() {
            super(RuleType.SCPnoHetAt);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPnumHetAt();
        }
    }

    private class SCPnoNAt extends PropertyCalculator {
        public SCPnoNAt() {
            super(RuleType.SCPnoNAt);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPnumNAt();
        }
    }

    private class SCPnoOAt extends PropertyCalculator {
        public SCPnoOAt() {
            super(RuleType.SCPnoOAt);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPnumOAt();
        }
    }

    private class SCPnoSAt extends PropertyCalculator {
        public SCPnoSAt() {
            super(RuleType.SCPnoSAt);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPnumSAt();
        }
    }

    private class SCPnoRings extends PropertyCalculator {
        public SCPnoRings() {
            super(RuleType.SCPnoRings);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * edu.udo.scaffoldhunter.treegen.ScaffoldTreeGenerator.PropertyCalculator
         * #calculate(edu.udo.scaffoldhunter.treegen.ScaffoldContainer)
         */
        @Override
        public int calculate(ScaffoldContainer sc) {
            return sc.getSCPnumRings();
        }
    }
}
