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

package edu.udo.scaffoldhunter.model.db;

import java.util.List;
import java.util.Map;

import javassist.NotFoundException;

import com.google.common.collect.Table;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.dataimport.MergeIterator;

/**
 * @author Dominic Sacré
 * 
 */
public class MockDbManager implements DbManager {

    /**
     * 
     */
    public MockDbManager() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#isConnected()
     */
    @Override
    public boolean isConnected() {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getAllProfileNames()
     */
    @Override
    public List<String> getAllProfileNames() throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getAllDatasetNames()
     */
    @Override
    public List<String> getAllDatasetNames() throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getProfile(java.lang.String)
     */
    @Override
    public Profile getProfile(String username) throws DatabaseException, NotFoundException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getAllSessionTitles(edu.udo
     * .scaffoldhunter.model.db.Profile)
     */
    @Override
    public List<String> getAllSessionTitles(Profile profile) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getSession(edu.udo.scaffoldhunter
     * .model.db.Profile, java.lang.String)
     */
    @Override
    public Session getSession(Profile profile, String title) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getAllDatasets()
     */
    @Override
    public List<Dataset> getAllDatasets() throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getRootSubset(edu.udo.
     * scaffoldhunter.model.db.Session)
     */
    @Override
    public Subset getRootSubset(Session session) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getRootSubset(edu.udo.
     * scaffoldhunter.model.db.Session,
     * edu.udo.scaffoldhunter.model.db.Filterset)
     */
    @Override
    public Subset getRootSubset(Session session, Filterset filterset) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getScaffolds(edu.udo.scaffoldhunter
     * .model.db.Tree, edu.udo.scaffoldhunter.model.db.Subset)
     */
    @Override
    public Scaffold getScaffolds(Subset subset, boolean cutStem) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getAllLinks()
     */
    @Override
    public List<Link> getAllLinks() throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#saveOrUpdate(edu.udo.scaffoldhunter
     * .model.db.DbObject)
     */
    @Override
    public void saveOrUpdate(DbObject obj) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#saveOrUpdateAll(java.lang.Iterable
     * )
     */
    @Override
    public void saveOrUpdateAll(Iterable<? extends DbObject> objs) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#saveAsNew(edu.udo.scaffoldhunter
     * .model.db.DbObject)
     */
    @Override
    public void saveAsNew(DbObject obj) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#saveAllAsNew(java.lang.Iterable
     * )
     */
    @Override
    public void saveAllAsNew(Iterable<? extends DbObject> objs) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#deleteAll(java.lang.Iterable)
     */
    @Override
    public void deleteAll(Iterable<? extends DbObject> objs) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#delete(edu.udo.scaffoldhunter
     * .model.db.DbObject)
     */
    @Override
    public void delete(DbObject obj) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#lockAndLoad(edu.udo.scaffoldhunter
     * .model.db.PropertyDefinition, edu.udo.scaffoldhunter.model.db.Structure)
     */
    @Override
    public void lockAndLoad(PropertyDefinition propDef, Structure structure) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#lockAndLoad(java.lang.Iterable,
     * java.lang.Iterable)
     */
    @Override
    public void lockAndLoad(Iterable<PropertyDefinition> propDefs, Iterable<? extends Structure> structures)
            throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#unlockAndUnload(edu.udo.
     * scaffoldhunter.model.db.PropertyDefinition,
     * edu.udo.scaffoldhunter.model.db.Structure)
     */
    @Override
    public void unlockAndUnload(PropertyDefinition propDef, Structure structure) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#unlockAndUnload(java.lang.Iterable
     * , java.lang.Iterable)
     */
    @Override
    public void unlockAndUnload(Iterable<PropertyDefinition> propDefs, Iterable<? extends Structure> structures) {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getSvgString(edu.udo.scaffoldhunter
     * .model.db.Structure)
     */
    @Override
    public String getSvgString(Structure structure) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getStrucMol(edu.udo.scaffoldhunter
     * .model.db.Structure)
     */
    @Override
    public String getStrucMol(Structure structure) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getAccumulatedNumProperty(edu
     * .udo.scaffoldhunter.model.db.PropertyDefinition,
     * edu.udo.scaffoldhunter.model.AccumulationFunction,
     * edu.udo.scaffoldhunter.model.db.Scaffold, boolean)
     */
    @Override
    public Double getAccNumPropertyScaffold(PropertyDefinition property, AccumulationFunction function,
            Scaffold scaffold, boolean withSubtree) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getAccumulatedNumProperty(edu
     * .udo.scaffoldhunter.model.db.PropertyDefinition,
     * edu.udo.scaffoldhunter.model.AccumulationFunction,
     * edu.udo.scaffoldhunter.model.db.Subset)
     */
    @Override
    public Double getAccNumPropertySubset(PropertyDefinition property, AccumulationFunction function, Subset subset)
            throws DatabaseException {

        return 0.0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getSortOrder(edu.udo.scaffoldhunter
     * .model.db.Subset, edu.udo.scaffoldhunter.model.db.PropertyDefinition,
     * boolean)
     */
    @Override
    public Map<Integer, Integer> getSortOrder(Subset subset, PropertyDefinition property, boolean ascending)
            throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#createOrUpdateComment(java.
     * lang.String, boolean, edu.udo.scaffoldhunter.model.db.Tree,
     * edu.udo.scaffoldhunter.model.db.Profile,
     * edu.udo.scaffoldhunter.model.db.Structure)
     */
    @Override
    public Comment createOrUpdateComment(String comment, boolean priv, Tree tree, Profile profile, Structure structure)
            throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#createBanner(boolean,
     * edu.udo.scaffoldhunter.model.db.Tree,
     * edu.udo.scaffoldhunter.model.db.Profile,
     * edu.udo.scaffoldhunter.model.db.Structure)
     */
    @Override
    public Banner createBanner(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getComment(boolean,
     * edu.udo.scaffoldhunter.model.db.Tree,
     * edu.udo.scaffoldhunter.model.db.Profile,
     * edu.udo.scaffoldhunter.model.db.Structure)
     */
    @Override
    public Comment getComment(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getBanner(boolean,
     * edu.udo.scaffoldhunter.model.db.Tree,
     * edu.udo.scaffoldhunter.model.db.Profile,
     * edu.udo.scaffoldhunter.model.db.Structure)
     */
    @Override
    public Banner getBanner(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getBanner(boolean,
     * edu.udo.scaffoldhunter.model.db.Session,
     * edu.udo.scaffoldhunter.model.db.Profile,
     * edu.udo.scaffoldhunter.model.db.Structure)
     */
    @Override
    public Banner getBanner(boolean priv, Session session, Profile profile, Structure structure)
            throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getMolecule(edu.udo.scaffoldhunter
     * .model.db.Dataset, java.lang.String)
     */
    @Override
    public Molecule getMolecule(Dataset dataset, String smiles) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getAllRulesets()
     */
    @Override
    public List<Ruleset> getAllRulesets() throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getCreationUserName(edu.udo
     * .scaffoldhunter.model.db.Dataset)
     */
    @Override
    public String getCreationUserName(Dataset dataset) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#mergeMoleculesIntoDB(edu.udo
     * .scaffoldhunter.model.dataimport.MergeIterator)
     */
    @Override
    public void mergeMoleculesIntoDBbySMILES(MergeIterator mergeIterator) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#mergeMoleculesIntoDBbyProperty
     * (edu.udo.scaffoldhunter.model.dataimport.MergeIterator,
     * edu.udo.scaffoldhunter.model.db.PropertyDefinition,
     * edu.udo.scaffoldhunter.model.db.Dataset)
     */
    @Override
    public void mergeMoleculesIntoDBbyProperty(MergeIterator mergeIterator, PropertyDefinition mergeBy,
            Dataset mergeInto) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#deleteAllMolecules(edu.udo.
     * scaffoldhunter.model.db.Dataset)
     */
    @Override
    public void deleteAllMolecules(Dataset dataset) throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getFilteredSubset(edu.udo.
     * scaffoldhunter.model.db.Subset,
     * edu.udo.scaffoldhunter.model.db.Filterset)
     */
    @Override
    public Subset getFilteredSubset(Subset subset, Filterset filterset) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getFilteredSubsetSize(edu.udo
     * .scaffoldhunter.model.db.Subset,
     * edu.udo.scaffoldhunter.model.db.Filterset)
     */
    @Override
    public int getFilteredSubsetSize(Subset subset, Filterset filterset) throws DatabaseException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getAccumulatedNumProperties
     * (edu.udo.scaffoldhunter.model.db.PropertyDefinition,
     * edu.udo.scaffoldhunter.model.AccumulationFunction,
     * edu.udo.scaffoldhunter.model.db.Subset,
     * edu.udo.scaffoldhunter.model.db.Scaffold, boolean)
     */
    @Override
    public Map<Scaffold, Double> getAccNumProperties(PropertyDefinition propDef,
            AccumulationFunction accumulation, Subset subset, Scaffold root, boolean cumulative)
            throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getAccumulatedPropertyMinMax
     * (edu.udo.scaffoldhunter.model.db.Tree,
     * edu.udo.scaffoldhunter.model.db.PropertyDefinition,
     * edu.udo.scaffoldhunter.model.AccumulationFunction)
     */
    @Override
    public double[] getAccPropertyMinMax(Tree tree, PropertyDefinition propDef, AccumulationFunction acc, 
            Subset subset, boolean subtreeCumulative, boolean removeVirtualRoot, boolean includeMoleculeData)
            throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getDistinctValueCount(edu.udo
     * .scaffoldhunter.model.db.PropertyDefinition)
     */
    @Override
    public long getDistinctValueCount(PropertyDefinition propDef) throws DatabaseException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getDistinctStrings(edu.udo.
     * scaffoldhunter.model.db.PropertyDefinition)
     */
    @Override
    public List<String> getDistinctStrings(PropertyDefinition propDef) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getStringDistribution(edu.udo
     * .scaffoldhunter.model.db.Scaffold,
     * edu.udo.scaffoldhunter.model.db.Subset,
     * edu.udo.scaffoldhunter.model.db.PropertyDefinition)
     */
    @Override
    public Table<Scaffold, String, Integer> getStringDistribution(Scaffold root, Subset subset,
            PropertyDefinition propDef) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getAccumulatedNumProperty(edu
     * .udo.scaffoldhunter.model.db.PropertyDefinition)
     */
    @Override
    public Map<AccumulationFunction, Double> getAccNumPropertyDataset(PropertyDefinition property)
            throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getConnectionDriverClass()
     */
    @Override
    public String getConnectionDriverClass() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getConnectionUrl()
     */
    @Override
    public String getConnectionUrl() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getConnectionUsername()
     */
    @Override
    public String getConnectionUsername() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getRootSubsetSize(edu.udo.
     * scaffoldhunter.model.db.Dataset,
     * edu.udo.scaffoldhunter.model.db.Filterset)
     */
    @Override
    public int getRootSubsetSize(Dataset dataset, Filterset filterset) throws DatabaseException {

        return 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getConnectionPassword()
     */
    @Override
    public String getConnectionPassword() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getAllBanners(edu.udo.
     * scaffoldhunter.model.db.Subset, edu.udo.scaffoldhunter.model.db.Scaffold)
     */
    @Override
    public List<Banner> getAllBanners(Subset subset, Scaffold root) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getCreationUserName(edu.udo
     * .scaffoldhunter.model.db.Tree)
     */
    @Override
    public String getCreationUserName(Tree tree) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#getCreationUserName(edu.udo
     * .scaffoldhunter.model.db.Comment)
     */
    @Override
    public String getCreationUserName(Comment comment) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getHibernateDialect()
     */
    @Override
    public String getHibernateDialect() {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#initializeSessionFactory()
     */
    @Override
    public void initializeSessionFactory() throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#createAndExportSchema()
     */
    @Override
    public void createAndExportSchema() throws DatabaseException {

    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#validateSchema()
     */
    @Override
    public boolean validateSchema() throws DatabaseException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getAllMolecules(edu.udo.
     * scaffoldhunter.model.db.Dataset)
     */
    @Override
    public List<Molecule> getAllMolecules(Dataset dataset) throws DatabaseException {

        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.db.DbManager#schemaExists()
     */
    @Override
    public boolean schemaExists() throws DatabaseException {

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.db.DbManager#loadCurrentSession(edu.udo.
     * scaffoldhunter.model.db.Profile)
     */
    @Override
    public void loadCurrentSession(Profile profile) throws DatabaseException {

    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.db.DbManager#getAllSessionInformations(edu.udo.scaffoldhunter.model.db.Profile)
     */
    @Override
    public List<SessionInformation> getAllSessionsInformation(Profile profile) throws DatabaseException {
        return null;
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.db.DbManager#updateSessionTitle(edu.udo.scaffoldhunter.model.db.SessionInformation)
     */
    @Override
    public void updateSessionTitle(SessionInformation info) throws DatabaseException {
        
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.db.DbManager#deleteSession(edu.udo.scaffoldhunter.model.db.SessionInformation)
     */
    @Override
    public void deleteSession(SessionInformation info) throws DatabaseException {
        
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.db.DbManager#canSaveSession(edu.udo.scaffoldhunter.model.db.Session)
     */
    @Override
    public boolean canSaveSession(Session session) throws DatabaseException {
        return true;
    }

}
