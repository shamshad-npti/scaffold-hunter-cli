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

package edu.udo.scaffoldhunter.model.db;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javassist.NotFoundException;

import com.google.common.collect.Table;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.dataimport.MergeIterator;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * @author Dominic Sacré
 * 
 */
public interface DbManager {
    /**
     * @return the connectionDriverClass
     */
    public String getConnectionDriverClass();

    /**
     * @return the connectionUrl
     */
    public String getConnectionUrl();

    /**
     * @return the connectionUsername
     */
    public String getConnectionUsername();

    /**
     * @return the connectionPassword
     */
    public String getConnectionPassword();

    /**
     * @return the hibernateDialect
     */
    public String getHibernateDialect();

    /**
     * Initialises the Session Factory for all Hibernate Operations. All other
     * Methods will only work after the SessioFactory is initialised.
     * 
     * @throws DatabaseException
     */
    public void initializeSessionFactory() throws DatabaseException;

    /**
     * Drops the used schema and recreates it. Including Hibernate schema
     * export. Therefore creates all Tables, indices, constraints, ...
     * 
     * Attention: All data in the schema will be lost!
     * 
     * @throws DatabaseException
     */
    public void createAndExportSchema() throws DatabaseException;

    /**
     * Validates the database schema. It checks whether the existing database
     * schema matches the Hibernate mapping files
     * 
     * @return whether the schema is valid or not
     * @throws DatabaseException
     */
    public boolean validateSchema() throws DatabaseException;

    /**
     * Checks the existence of a database schema
     * 
     * @return whether the schema exists
     * @throws DatabaseException
     */
    public boolean schemaExists() throws DatabaseException;

    /**
     * @return Is connected to database?
     */
    public boolean isConnected();

    /**
     * @return a list of all Profiles
     * @throws DatabaseException
     */
    public List<String> getAllProfileNames() throws DatabaseException;

    /**
     * @return a list of all {@link Dataset} names
     * @throws DatabaseException
     */
    public List<String> getAllDatasetNames() throws DatabaseException;

    /**
     * Getter for a {@link Profile} specified by username
     * 
     * @param username
     *            the username to specify the Profile
     * @return the Profile
     * @throws DatabaseException
     *             if a database error occurs
     * @throws NotFoundException
     *             if the specified username is not found in database
     */
    public Profile getProfile(String username) throws DatabaseException, NotFoundException;

    /**
     * Getter for the titles of all {@link Session}s are availible for a
     * {@link Profile}
     * 
     * @param profile
     *            the {@link Profile}
     * @return all {@link Session} titles
     * @throws DatabaseException
     */
    public List<String> getAllSessionTitles(Profile profile) throws DatabaseException;

    /**
     * Getter for the {@link SessionInformation} of all
     * {@link edu.udo.scaffoldhunter.model.db.Session}s for the specified
     * {@link Profile}
     * 
     * @param profile
     *            the {@link Profile}
     * @return all {@link SessionInformation}s
     * @throws DatabaseException
     */
    public List<SessionInformation> getAllSessionsInformation(Profile profile) throws DatabaseException;

    /**
     * Updates the title of a {@link edu.udo.scaffoldhunter.model.db.Session}
     * specified by a {@link SessionInformation}
     * 
     * @param info
     *            the {@link SessionInformation}
     * @throws DatabaseException
     */
    public void updateSessionTitle(SessionInformation info) throws DatabaseException;

    /**
     * Deletes a {@link edu.udo.scaffoldhunter.model.db.Session} specified by
     * the {@link SessionInformation}
     * 
     * @param info
     *            the {@link SessionInformation}
     * 
     * @throws DatabaseException
     */
    public void deleteSession(SessionInformation info) throws DatabaseException;

    /**
     * Getter for a {@link Session} depending on a {@link Profile} and the
     * {@link Session} title.
     * 
     * @param profile
     *            the {@link Profile}
     * @param title
     *            the {@link Session} title
     * @return the specified {@link Session}
     * @throws DatabaseException
     */
    public Session getSession(Profile profile, String title) throws DatabaseException;

    /**
     * Loads the current {@link edu.udo.scaffoldhunter.model.db.Session} into
     * the Profile
     * 
     * @param profile
     *            the Profile for which the current
     *            {@link edu.udo.scaffoldhunter.model.db.Session} should be
     *            loaded
     * @throws DatabaseException
     */
    public void loadCurrentSession(Profile profile) throws DatabaseException;

    /**
     * Checks if the session can be saved using the current database connection.
     * The session can be to large for some database configurations (e.g. MySQL
     * defaults to 1 MiB maximum package size)
     * 
     * @param session
     *            The {@link Session} to be saved
     * @return if the {@link Session} can be saved
     * @throws DatabaseException
     *             if retrieving the max package size fails
     */
    boolean canSaveSession(Session session) throws DatabaseException;

    /**
     * Getter for all {@link Dataset}s
     * 
     * @return a list of all {@link Dataset}s
     * @throws DatabaseException
     *             if a database error occurs
     */
    public List<Dataset> getAllDatasets() throws DatabaseException;

    /**
     * Returns all molecules in the given dataset. The molecules will always be
     * a new instance of the molecule. Use this function with caution! It should
     * only be used in special cases.
     * 
     * @param dataset
     *            The dataset where the molecules belongs to.
     * @return A list with new instances of all molecules in the dataset
     * @throws DatabaseException
     */
    public List<Molecule> getAllMolecules(Dataset dataset) throws DatabaseException;

    /**
     * Getter for a new root {@link Subset} including all {@link Molecule
     * Molecules} of the current {@link Dataset} which is detemined by the given
     * {@link Session}
     * 
     * @param session
     *            the Session for which the new Subset should be generated.
     * @return a new Subset
     * @throws DatabaseException
     */
    public Subset getRootSubset(Session session) throws DatabaseException;

    /**
     * Getter for a new root {@link Subset} including Molecules of the current
     * {@link Dataset} which is detemined by the given {@link Session} and
     * filtered by the given {@link Filterset}
     * 
     * @param session
     *            the {@link Session} for which the new {@link Subset} should be
     *            generated.
     * @param filterset
     *            the {@link Filterset}
     * @return a new {@link Subset}
     * @throws DatabaseException
     */
    public Subset getRootSubset(Session session, Filterset filterset) throws DatabaseException;

    /**
     * Getter for the molecule count of a root {@link Subset} in the given
     * {@link Dataset} filtered by the given {@link Filterset}
     * 
     * @param dataset
     *            the {@link Dataset} for which the {@link Molecule} count
     *            should be calculated.
     * @param filterset
     *            the {@link Filterset}
     * @return the {@link Molecule} count
     * @throws DatabaseException
     */
    public int getRootSubsetSize(Dataset dataset, Filterset filterset) throws DatabaseException;

    /**
     * Filters a {@link Subset} according the {@link Filterset} and creates a
     * new filtered {@link Subset}
     * 
     * @param subset
     *            the {@link Subset} to be filtered
     * @param filterset
     *            the {@link Filterset}
     * @return the filtered {@link Subset}
     * @throws DatabaseException
     */
    public Subset getFilteredSubset(Subset subset, Filterset filterset) throws DatabaseException;

    /**
     * Calculate the size of the {@link Subset} that will be created using
     * getFilteredSubset() with the same arguments
     * 
     * @param subset
     *            the {@link Subset} to be filtered
     * @param filterset
     *            the {@link Filterset}
     * @return the filtered {@link Subset} size
     * @throws DatabaseException
     */
    public int getFilteredSubsetSize(Subset subset, Filterset filterset) throws DatabaseException;

    /**
     * Getter for a set of {@link Scaffold}s that belong to the given
     * {@link Tree} and have {@link Molecule}s in the given {@link Subset} or
     * are a parent of these
     * 
     * @param subset
     *            the Subset in which the molecules of the scaffolds have to be
     *            in. This subset must be saved in the database.
     * @param cutStem
     *            if <code>true</code> virtual scaffolds at the root will be
     *            removed if they have only one child.
     * @return the root of the Scaffold tree
     * @throws DatabaseException
     */
    public Scaffold getScaffolds(Subset subset, boolean cutStem) throws DatabaseException;

    /**
     * Getter for all {@link Link Links}
     * 
     * @return a list of all Links
     * @throws DatabaseException
     */
    public List<Link> getAllLinks() throws DatabaseException;

    /**
     * Stores one {@link DbObject} in the Database. The {@link DbObject} will be
     * updated if it already exists.
     * 
     * @param obj
     *            a object to store in database
     * @throws DatabaseException
     */
    public void saveOrUpdate(DbObject obj) throws DatabaseException;

    /**
     * Stores a {@link Collection} of {@link DbObject DbObjects} in the
     * Database. The {@link DbObject DbObjects} will be updated if they already
     * exists.
     * 
     * @param objs
     *            a collection of objects to store in database
     * @throws DatabaseException
     */
    public void saveOrUpdateAll(Iterable<? extends DbObject> objs) throws DatabaseException;

    /**
     * Stores one {@link DbObject} in the Database. The {@link DbObject} will be
     * stored with a new id, also if it already exists.
     * 
     * @param obj
     *            a object to store in database
     * @throws DatabaseException
     */
    public void saveAsNew(DbObject obj) throws DatabaseException;

    /**
     * Stores a {@link Collection} of {@link DbObject DbObjects} in the
     * Database. The {@link DbObject DbObjects} will be stored with a new id,
     * also if they already exists.
     * 
     * @param objs
     *            a collection of objects to store in database
     * @throws DatabaseException
     */
    public void saveAllAsNew(Iterable<? extends DbObject> objs) throws DatabaseException;

    /**
     * Deletes a {@link Collection} of objects in the Database.
     * 
     * @param objs
     *            a Collection of objects that should be deleted in database
     * @throws DatabaseException
     */
    public void deleteAll(Iterable<? extends DbObject> objs) throws DatabaseException;

    /**
     * Deletes an {@link DbObject} in the database. This only deletes the
     * specified {@link DbObject}s and all {@link DbObject}s that cascade on
     * delete to it. For complex {@link DbObject}s use the corresponding
     * {@link DbManager} functions.
     * 
     * @param obj
     *            the object that should be deleted in database
     * @throws DatabaseException
     */
    public void delete(DbObject obj) throws DatabaseException;

    /**
     * For a given {@link Dataset} delete all {@link Molecule}s together with
     * their {@link Property}s and {@link Banner}s but keeps the {@link Comment}
     * s because they are not directly linked to a {@link Molecule}.
     * {@link Comment}s will only be deleted if the {@link Dataset} is deleted.
     * 
     * @param dataset
     *            the {@link Dataset} whose {@link Molecule}s will be deleted
     * @throws DatabaseException
     */
    public void deleteAllMolecules(Dataset dataset) throws DatabaseException;

    /**
     * Lazy loads a {@link Property} to a {@link Structure} and gains a lock on
     * it (will not be removed from memory) until unlockAndUnload() is called.
     * If a {@link Property} is not available in the database it will set a null
     * {@link Property}.
     * 
     * @param propDef
     *            the {@link PropertyDefinition} for the Property to load
     * @param structure
     *            the {@link Structure} for which the {@link Property} should be
     *            loaded
     * @throws DatabaseException
     */
    public void lockAndLoad(PropertyDefinition propDef, Structure structure) throws DatabaseException;

    /**
     * Lazy loads a Collection of {@link Property}s to a Collection of
     * {@link Structure}s and gains a lock on them (will not be removed from
     * memory) until unlockAndUnload() is called. If a {@link Property} is not
     * available in the database it will set a null {@link Property}.
     * 
     * @param propDefs
     *            the {@link PropertyDefinition}s for the {@link Property}s to
     *            load
     * @param structures
     *            the {@link Structure}s for which the {@link Property}s should
     *            be loaded
     * @throws DatabaseException
     */
    public void lockAndLoad(Iterable<PropertyDefinition> propDefs, Iterable<? extends Structure> structures)
            throws DatabaseException;

    /**
     * Releases one lock on a {@link Property} and unloads the {@link Property}
     * when no more locks gain ownership of this {@link Property}
     * 
     * @param propDef
     *            the {@link PropertyDefinition} for the {@link Property} to
     *            unload
     * @param structure
     *            the {@link Structure} for which the {@link Property} should be
     *            unloaded
     */
    public void unlockAndUnload(PropertyDefinition propDef, Structure structure);

    /**
     * Releases one lock on a Collection of {@link Property}s and unloads the
     * {@link Property}s when no more locks gain ownership of this
     * {@link Property}s
     * 
     * @param propDefs
     *            the {@link PropertyDefinition}s for the {@link Property}s to
     *            unload
     * @param structures
     *            the {@link Structure}s for which the {@link Property}s should
     *            be unloaded
     */
    public void unlockAndUnload(Iterable<PropertyDefinition> propDefs, Iterable<? extends Structure> structures);

    /**
     * Fetches the SVG String for one {@link Structure} from Database
     * 
     * @param structure
     * @return the SVG String for the given {@link Structure}
     * @throws DatabaseException
     */
    public String getSvgString(Structure structure) throws DatabaseException;

    /**
     * Fetches the Mol String for one {@link Structure} from Database
     * 
     * @param structure
     * @return the Mol String for the given {@link Structure}
     * @throws DatabaseException
     */
    public String getStrucMol(Structure structure) throws DatabaseException;

    /**
     * Calculates accumulations of a property over the whole dataset. This is a
     * flat query over the complete dataset. Subsets are not respected.
     * <p>
     * The following accumulations are calculated:
     * <ul>
     * <li>average
     * <li>minimum
     * <li>maximum
     * <li>sum
     * </ul>
     * 
     * @param property
     *            the property for which accumulations are calculated
     * @return a map containing the calculated accumulation functions and the
     *         corresponding values
     * @throws DatabaseException
     */
    Map<AccumulationFunction, Double> getAccNumPropertyDataset(PropertyDefinition property) throws DatabaseException;

    /**
     * Calculates and returns a given accumulated {@link MoleculeNumProperty} for a
     * given scaffold.
     * 
     * @param property
     *            The requested {@link PropertyDefinition}. Must be a
     *            {@link MoleculeNumProperty}.
     * @param function
     *            The accumulation function
     * @param scaffold
     *            The scaffold
     * @param withSubtree
     *            Should the property be calculated for the subtree of the
     *            scaffold? Default is false
     * @return The accumulated value over the {@link Scaffold}s {@link Molecule}
     *         s or null if the {@link Property} is not defined for all
     *         {@link Molecule}s
     * 
     * @throws DatabaseException
     * @throws IllegalArgumentException
     *             If the subset has no molecules
     */
    public Double getAccNumPropertyScaffold(PropertyDefinition property, AccumulationFunction function,
            Scaffold scaffold, boolean withSubtree) throws DatabaseException;

    /**
     * Calculates and returns the accumulated NumProperties for all Scaffolds in
     * the Subtree rooted at <code>root</code>. For molecule properties the values 
     * for each scaffold must always be accumulated. The <code>subtreeCumulative</code>
     * flag determines how the values are accumulated. If the flag set, a scaffold
     * considers all molecules, which are contained in its subtree. If the flag is
     * unset, a scaffold only considers its direct molecule children. For scaffold 
     * properties the values can either be directly taken from the scaffolds 
     * themselves (flag unset) or accumulated over all scaffolds in the corresponding
     * subtrees.
     * 
     * @param propDef
     *            The requested {@link PropertyDefinition}
     * @param accumulation
     *            The accumulation function
     * @param subset
     *            Only molecules in this subset will be taken into account for
     *            computation of accumulated values. Use the root subset for global 
     *            minimum and maximum.
     * @param root
     *            The root of the subtree for which values will be calculated
     * @param subtreeCumulative
     *            whether accumulation is calculated over the whole subtree of each scaffold or only over the direct children
     * @return a map containing the accumulated property values for each
     *         scaffold in the subtree rooted at root. If a scaffold has no
     *         molecules, it will not be contained in the map
     * @throws DatabaseException
     */
    public Map<Scaffold, Double> getAccNumProperties(PropertyDefinition propDef,
            AccumulationFunction accumulation, Subset subset, Scaffold root, boolean subtreeCumulative)
            throws DatabaseException;

    /**
     * Calculates and returns a given accumulated property for a given subset.
     * This method does not consider any tree structure, but just accumulates
     * over all molecules or scaffolds in a subset and returns the single number
     * result.
     * 
     * @param property
     *            The requested {@link PropertyDefinition}
     * @param function
     *            The accumulation function
     * @param subset
     *            The subset on which the value should be accumulated
     * @return The accumulated value over the subsets molecules
     * @throws DatabaseException
     * @throws IllegalArgumentException
     *             If the subset has no molecules
     */
    public Double getAccNumPropertySubset(PropertyDefinition property, AccumulationFunction function, Subset subset)
            throws DatabaseException;

    /**
     * Calculates the minimum and maximum (accumulated) property value, which can
     * be found in a scaffold tree. For molecule properties this method considers
     * all property values from the molecules themselves and all accumulated values,
     * which are calculated according to the given parameters. Keep in mind, that
     * for a sum accumulation the minimum and maximum values calculated by this
     * method may exceed the minimum and maximum, which can found along the molecules
     * themselves. A more detailed description of the parameters and their impact
     * on the calculation can be found in the description of the method {@link 
     * #getAccNumProperties(PropertyDefinition propDef, AccumulationFunction accumulation, Subset subset, Scaffold root, boolean subtreeCumulative)
     *  getAccNumProperties}.
     * 
     * @param tree
     *            the scaffold tree for which minimum and maximum are
     *            calculated.
     * @param propDef
     *            The requested {@link PropertyDefinition}
     * @param acc
     *            The accumulation function
     * @param subset
     *            Only molecules in this subset will be taken into account for
     *            computation of accumulated values. Use the root subset for global 
     *            minimum and maximum.
     * @param subtreeCumulative 
     *            whether accumulation is calculated over the whole subtree of each scaffold or only over the direct children
     * @param removeVirtualRoot 
     *            whether the value of the root should be ignored if it is a virtual root
     * @param includeMoleculeData
     *            whether the raw molecule property values should be included to calculate the minimum and maximum.
     *            Is only relevant for molecule properties and will be ignored for scaffold properties.
     * @return A double array containing two entries. The first one being the
     *         minimum and the second one being the maximum accumulated value
     *         found in the scaffold tree. The array will contain Double.NaN in both entries, if there
     *         are no valid minimum and maximum (e.g. if there are no molecules in the tree
     *         where the property is defined).
     * @throws DatabaseException
     */
    public double[] getAccPropertyMinMax(Tree tree, PropertyDefinition propDef, AccumulationFunction acc, Subset subset,
            boolean subtreeCumulative, boolean removeVirtualRoot, boolean includeMoleculeData)
            throws DatabaseException;

    /**
     * Returns the number of distinct string values for a property definition.
     * 
     * @param propDef
     *            The requested {@link PropertyDefinition}.
     * @return The number of distinct values known for this property definition
     * @throws DatabaseException
     */
    public long getDistinctValueCount(PropertyDefinition propDef) throws DatabaseException;

    /**
     * Returns a list of distinct string values for a property definition
     * 
     * @param propDef
     *            The requested {@link PropertyDefinition} which describes a
     *            string property.
     * @return A list containing all distinct string values associated with that
     *         property definition.
     * @throws DatabaseException
     */
    public List<String> getDistinctStrings(PropertyDefinition propDef) throws DatabaseException;

    /**
     * Calculates the distribution of Molecule String Property values to
     * scaffolds. The distribution is returned in table. The rows of the table
     * hold the scaffolds, its columns hold the distinct string values. The
     * table entries show how many molecules of a scaffold have a specific
     * string value.
     * 
     * @param root
     *            the root of the scaffold tree for which the distribution is
     *            calculated.
     * @param subset
     *            the subset on whose molecules the calculation is limited
     * @param propDef
     *            The requested {@link PropertyDefinition} which describes a
     *            molecule string property.
     * @return A table whose entries show how many molecules of the scaffold in
     *         the row have the string value in the column.
     * @throws DatabaseException
     */
    public Table<Scaffold, String, Integer> getStringDistribution(Scaffold root, Subset subset,
            PropertyDefinition propDef) throws DatabaseException;

    /**
     * Calculates and returns a sort order for the given {@link Subset} based on
     * the given {@link PropertyDefinition}
     * 
     * @param subset
     *            The subset that should be sorted
     * @param property
     *            The property the subset should be sorted by
     * @param ascending
     *            Ascending or descending sort order
     * @return A Hashmap mapping the molecules ids to their position in the sort
     *         order
     * @throws DatabaseException
     */
    public Map<Integer, Integer> getSortOrder(Subset subset, PropertyDefinition property, boolean ascending)
            throws DatabaseException;

    /**
     * Creates and stores a new local (only for this {@link Tree}) or global
     * (for all {@link Tree}s) {@link Comment} in the database or updates an
     * existing {@link Comment} with the same preferences
     * 
     * @param comment
     *            the comment to create
     * @param priv
     *            if the {@link Comment} is private or public
     * @param tree
     *            the Tree to which the {@link Comment} belongs. If the
     *            {@link Tree} is null it is a global {@link Comment}
     * @param profile
     *            the {@link Profile} of the user who created the
     *            {@link Comment}
     * @param structure
     *            the {@link Structure} too which the {@link Comment} belongs
     * @return the created or updated {@link Comment}
     * @throws DatabaseException
     */
    public Comment createOrUpdateComment(String comment, boolean priv, Tree tree, Profile profile, Structure structure)
            throws DatabaseException;

    /**
     * Creates and stores a new {@link Banner} in the database
     * 
     * @param priv
     *            if the {@link Banner} is private or public
     * @param tree
     *            The {@link Tree} to which the {@link Banner} belongs.
     * @param profile
     *            the {@link Profile} of the user who created the {@link Banner}
     * @param structure
     *            the {@link Structure} too which the {@link Banner} belongs
     * @return the created {@link Banner}
     * @throws DatabaseException
     */
    public Banner createBanner(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException;

    /**
     * Fetches a {@link Comment} for a given {@link Structure} from the
     * Database.
     * 
     * Attention: The modifiedBy property is not loaded for public
     * {@link Comment}s. If you want to delete a {@link Comment} this is a
     * necessary {@link Property} as it does not allow null values. Therefore
     * you need to set it manually
     * 
     * @param priv
     *            whether a private or public {@link Comment} should be fetched
     * @param tree
     *            Set the {@link Tree} to null if you want to fetch a global
     *            {@link Comment}. Otherwise it will fetch a local
     *            {@link Comment}
     * @param profile
     *            the {@link Profile} of the current user
     * @param structure
     *            the {@link Structure} for which the {@link Comment} should be
     *            fetched
     * @return the specified {@link Comment} or null
     * @throws DatabaseException
     */
    public Comment getComment(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException;

    /**
     * Fetches a {@link Banner} for a given {@link Structure} from the Database
     * 
     * Attention: The createdBy property is not loaded for public {@link Banner}
     * 
     * @param priv
     *            whether a private or public {@link Banner} should be fetched
     * @param tree
     *            the {@link Tree} to which the {@link Banner} belongs
     * @param profile
     *            the {@link Profile} of the current user
     * @param structure
     *            the {@link Structure} for which the {@link Banner} should be
     *            fetched
     * @return the specified {@link Banner} or null
     * @throws DatabaseException
     */
    public Banner getBanner(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException;

    /**
     * Retrieves all {@link Banner} that belong to {@link Molecule}s in the
     * given {@link Subset} or a {@link Scaffold} which is a child of root
     * 
     * @param subset
     *            the {@link Subset} for which the {@link Banner}s should be
     *            retrieved
     * @param root
     *            the root {@link Scaffold} of the {@link Tree} for which the
     *            {@link Banner}s should be retrieved
     * @return a {@link List} of all {@link Banner}s
     * @throws DatabaseException
     */
    public List<Banner> getAllBanners(Subset subset, Scaffold root) throws DatabaseException;

    /**
     * Fetches a {@link Banner} for a given {@link Structure} from the Database
     * 
     * Attention: The createdBy property is not loaded for public {@link Banner}
     * 
     * @param priv
     *            whether a private or public {@link Banner} should be fetched
     * @param session
     *            container for the {@link Tree} to which the {@link Banner}
     *            belongs
     * @param profile
     *            the {@link Profile} of the current user
     * @param structure
     *            the {@link Structure} for which the {@link Banner} should be
     *            fetched
     * @return the specified {@link Banner} or null
     * @throws DatabaseException
     */
    public Banner getBanner(boolean priv, Session session, Profile profile, Structure structure)
            throws DatabaseException;

    /**
     * Searches the requested molecule in the database and returns it or null,
     * if it was not found. The molecule will always be a new instance of this
     * molecule.
     * 
     * @param dataset
     *            The dataset where the molecule belongs to.
     * @param smiles
     *            The Smiles of the molecule.
     * @return A new instance of the searched molecule or null, if the searched
     *         molecule does not exist in the given dataset.
     * @throws DatabaseException
     */
    public Molecule getMolecule(Dataset dataset, String smiles) throws DatabaseException;

    /**
     * Getter for all {@link Ruleset Rulesets}
     * 
     * @return a list of all Rulesets
     * @throws DatabaseException
     *             if a database error occurs
     */
    public List<Ruleset> getAllRulesets() throws DatabaseException;

    /**
     * Get the creator of a {@link Dataset}
     * 
     * @param dataset
     *            the {@link Dataset}
     * @return The name of the user who created the {@link Dataset}
     * @throws DatabaseException
     */
    public String getCreationUserName(Dataset dataset) throws DatabaseException;

    /**
     * Get the creator of a {@link Tree}
     * 
     * @param tree
     *            the {@link Tree}
     * @return The name of the user who created the {@link Tree}
     * @throws DatabaseException
     */
    public String getCreationUserName(Tree tree) throws DatabaseException;

    /**
     * Get the creator of a {@link Comment}
     * 
     * @param comment
     *            the {@link Comment}
     * @return The name of the user who created the {@link Comment}
     * @throws DatabaseException
     */
    public String getCreationUserName(Comment comment) throws DatabaseException;

    /**
     * Merge all molecules of an import job into the database using a
     * {@link MergeIterator}.
     * 
     * @param mergeIterator
     *            The merge iterator which is used to merge the molecules into
     *            the database
     * @throws DatabaseException
     */
    public void mergeMoleculesIntoDBbySMILES(MergeIterator mergeIterator) throws DatabaseException;

    /**
     * Merge properties into the database using the specified property to
     * identify molecules.
     * 
     * @param mergeIterator
     *            The merge iterator which is used to merge molecules into the
     *            database
     * @param mergeBy
     *            The property which is used to identify the molecules to which
     *            data is added
     * @param mergeInto
     *            The dataset these molecules are located in
     * @throws DatabaseException
     */
    public void mergeMoleculesIntoDBbyProperty(MergeIterator mergeIterator, PropertyDefinition mergeBy,
            Dataset mergeInto) throws DatabaseException;
}