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

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javassist.NotFoundException;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.classic.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.dialect.MySQL5InnoDBDialect;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.dataimport.MergeIterator;
import edu.udo.scaffoldhunter.model.util.SHPredicates;
import edu.udo.scaffoldhunter.model.util.Scaffolds;
import edu.udo.scaffoldhunter.util.Orderings;

/**
 * @author Till Schäfer
 * @author Thomas Schmitz
 * @author Henning Garus
 * 
 */
public class DbManagerHibernate implements DbManager {
    private SessionFactory sessionFactory;
    private String connectionDriverClass;
    private String connectionUrl;
    private String connectionSchema;
    private String connectionUsername;
    private String connectionPassword;
    private String hibernateDialect;
    private Configuration hibernateConfiguration;

    private static Logger logger = LoggerFactory.getLogger(DbManagerHibernate.class);

    @Override
    public String getConnectionDriverClass() {
        return connectionDriverClass;
    }

    @Override
    public String getConnectionUrl() {
        return connectionUrl;
    }

    @Override
    public String getConnectionUsername() {
        return connectionUsername;
    }

    @Override
    public String getConnectionPassword() {
        return connectionPassword;
    }

    @Override
    public String getHibernateDialect() {
        return hibernateDialect;
    }

    /**
     * Constructor
     * 
     * @param driverClass
     *            the JDBC driver class. e.g. com.mysql.jdbc.Driver
     * @param hibernateDialect
     *            The Dialect which should be used by Hibernate. This must be
     *            compatible with the driverClass.
     * @param url
     *            the url for connecting. e.g.
     *            jdbc:mysql://localhost/hibernate_test
     * @param schema
     *            the schema name
     * @param username
     *            the username for connecting
     * @param password
     *            the password for connecting
     * @param autoInitialize
     *            automatically invoke initializeSessionFactory()
     * @param recreate
     *            automatically invoke createAndExportScheme()
     * @throws DatabaseException
     */
    public DbManagerHibernate(String driverClass, String hibernateDialect, String url, String schema, String username,
            String password, boolean autoInitialize, boolean recreate) throws DatabaseException {
        connectionDriverClass = driverClass.trim();
        connectionUrl = url.trim();
        connectionSchema = schema.trim();
        connectionUsername = username.trim();
        if (password != null) {
            connectionPassword = password.trim();
        }
        this.hibernateDialect = hibernateDialect.trim();

        // hibernate configuration
        try {
            hibernateConfiguration = new Configuration().configure();
            hibernateConfiguration.setProperty("connection.driver_class", connectionDriverClass);
            hibernateConfiguration.setProperty("connection.url", connectionUrl);
            hibernateConfiguration.setProperty("connection.username", connectionUsername);
            hibernateConfiguration.setProperty("hibernate.connection.url", connectionUrl);
            hibernateConfiguration.setProperty("hibernate.connection.username", connectionUsername);
            hibernateConfiguration.setProperty("hibernate.dialect", hibernateDialect);
            hibernateConfiguration.setProperty("hibernate.hbm2ddl.auto", "validate");
            hibernateConfiguration.setProperty("hibernate.default_schema", schema);
            hibernateConfiguration.setProperty("hibernate.connection.provider_class",
                    SetSchemaConnectionProvider.class.getCanonicalName());
            if (password != null) {
                hibernateConfiguration.setProperty("connection.password", connectionPassword);
                hibernateConfiguration.setProperty("hibernate.connection.password", connectionPassword);
            }
        } catch (HibernateException e) {
            throw new DatabaseException("The Hibernate configuration could not be read/generated", e);
        }

        if (recreate) {
            createAndExportSchema();
        }

        if (autoInitialize) {
            initializeSessionFactory();
        }
    }

    @Override
    public void initializeSessionFactory() throws DatabaseException {
        try {
            sessionFactory = hibernateConfiguration.buildSessionFactory();
        } catch (HibernateException ex) {
            logger.error("Initialization failed.\n{}\n{}", ex, stacktrace(ex));
            throw new DatabaseException("Initialization failed", ex);
        }
    }

    @Override
    public void createAndExportSchema() throws DatabaseException {
        Connection dbConnection = getNativeDbConnection();

        // drop and recreate schema
        try {
            Statement setupStatement = dbConnection.createStatement();
            if (hibernateDialect.equals(MySQL5InnoDBDialect.class.getCanonicalName())) {
                setupStatement.execute("DROP SCHEMA IF EXISTS " + connectionSchema + ";");
                setupStatement.execute("CREATE SCHEMA " + connectionSchema + ";");
            } else if (hibernateDialect.equals(HSQLDialectValid.class.getCanonicalName())) {
                setupStatement.execute("DROP SCHEMA IF EXISTS " + connectionSchema + " CASCADE;");
                setupStatement.execute("CREATE SCHEMA " + connectionSchema + ";");
            } else {
                throw new AssertionError("Unsupported Dialect");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not excecute recreation of database schema", e);
        } finally {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                throw new DatabaseException("Failed to close database connection.");
            }
        }

        // export hibernate schema
        SchemaExport schemaTool = new SchemaExport(hibernateConfiguration);
        schemaTool.create(false, true);
    }

    @Override
    public boolean validateSchema() throws DatabaseException {
        try {
            new SchemaValidator(hibernateConfiguration).validate();
        } catch (HibernateException e) {
            return false;
        } catch (Exception e) {
            throw new DatabaseException("Unknown validation Exception", e);
        }

        return true;
    }

    @Override
    public boolean schemaExists() throws DatabaseException {
        Connection dbConnection = getNativeDbConnection();

        try {
            if (hibernateDialect.equals(MySQL5InnoDBDialect.class.getCanonicalName())) {
                // don't ask me why MySQL stores the schemas in the catalogs
                // table...
                ResultSet results = dbConnection.getMetaData().getCatalogs();
                while (results.next()) {
                    if (results.getString(1).toUpperCase().equals(connectionSchema.toUpperCase())) {
                        return true;
                    }
                }

            } else {
                ResultSet results = dbConnection.getMetaData().getSchemas();
                while (results.next()) {
                    if (results.getString("TABLE_SCHEM").toUpperCase().equals(connectionSchema.toUpperCase())) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not check if schema exists", e);
        }

        return false;
    }

    @Override
    public boolean isConnected() {
        return sessionFactory != null;
    }

    /**
     * This tries to roll back the last changes and closes the session. This
     * method should be always used if a HibernateException Occurs to not be in
     * an undefined session state
     * 
     * @param session
     *            the Session
     */
    private void closeAndRollBackErroneousSession(Session session) {
        if (session != null) {
            /*
             * two catches to provide closing of session even when rollback
             * failed
             */
            try {
                Transaction t = session.getTransaction();
                if (t != null)
                    t.rollback();
            } catch (HibernateException e) {
                logger.warn("Rollback of erroneous hibernate session failed");
            }

            try {
                if (session.isOpen())
                    session.close();
            } catch (HibernateException e) {
                logger.warn("Closing of erroneous hibernate session failed");
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAllProfileNames() throws DatabaseException {
        List<String> profiles;
        Session hibernateSession = null;
        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            profiles = hibernateSession.createQuery("select username from Profile").list();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Query from Profile failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query from Profile failed", ex);
        }

        return profiles;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAllDatasetNames() throws DatabaseException {
        List<String> datasets;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            datasets = hibernateSession.createQuery("select title from Dataset").list();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Query from Dataset failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query from Dataset failed", ex);
        }

        return datasets;
    }

    @Override
    public Profile getProfile(String username) throws DatabaseException, NotFoundException {
        Profile profile;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("from Profile where username=:user");
            query.setString("user", username);
            profile = (Profile) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Query from Profile failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query from Profile failed", ex);
        }

        if (profile == null) {
            throw new NotFoundException("The username could not be found in Database");
        }

        return profile;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getAllSessionTitles(Profile profile) throws DatabaseException {
        List<String> titles = null;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("Select session.title from Session session "
                    + "where session.profile = :profile order by Title");
            query.setParameter("profile", profile);
            titles = query.list();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Getting all Session titles failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Getting all Session titles failed", ex);
        }
        return titles;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SessionInformation> getAllSessionsInformation(Profile profile) throws DatabaseException {
        List<SessionInformation> retVal = Lists.newLinkedList();
        List<Object[]> informations;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            /*
             * IMPORTANT grouping by all of sessions property is necessary for
             * hsqldb
             */
            Query query = hibernateSession.createQuery("Select s.id, s.title, s.tree.title, s.tree.dataset.title, "
                    + "size(s.subset.molecules) from Session s where s.profile = :profile "
                    + "group by s.id,s.title,s.tree.title,s.tree.dataset.title order by s.title");
            query.setParameter("profile", profile);
            informations = query.list();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Getting all SessionInformations failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Getting all SessionInformations failed", ex);
        }
        for (Object[] information : informations) {
            if (information[0] != null) {
                SessionInformation sessionInfo = new SessionInformation();
                sessionInfo.setSessionId((Integer) information[0]);
                sessionInfo.setTitle((String) information[1]);
                sessionInfo.setTreeName((String) information[2]);
                sessionInfo.setDatasetName((String) information[3]);
                sessionInfo.setRootSubsetSize((Integer) information[4]);

                retVal.add(sessionInfo);
            }
        }
        return retVal;
    }

    @Override
    public void updateSessionTitle(SessionInformation info) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("update Session s set s.title = :title where s.id = :id");
            query.setParameter("title", info.getTitle());
            query.setParameter("id", info.getSessionId());
            query.executeUpdate();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Updating Session.title failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Updating Session.title failed", ex);
        }
    }

    @Override
    public void deleteSession(SessionInformation info) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("delete Session s where s.id = :id");
            query.setParameter("id", info.getSessionId());
            int n = query.executeUpdate();
            logger.debug("The Number of deleted Sessions is {}", n);
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Deleting Session failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Deleting Session failed", ex);
        }
    }

    @Override
    public edu.udo.scaffoldhunter.model.db.Session getSession(Profile profile, String title) throws DatabaseException {
        edu.udo.scaffoldhunter.model.db.Session session = null;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession
                    .createQuery("from Session session where session.profile = :profile AND session.title = :title");
            query.setParameter("profile", profile);
            query.setString("title", title);
            session = (edu.udo.scaffoldhunter.model.db.Session) query.uniqueResult();
            if (session == null) {
                throw new DatabaseException("Session not found");
            }
            session.setProfile(profile);
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Getting Session failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Getting Session failed", ex);
        }
        return session;
    }

    @Override
    public void loadCurrentSession(Profile profile) throws DatabaseException {
        edu.udo.scaffoldhunter.model.db.Session currentSession = null;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("SELECT p.currentSession from Profile p where p = :profile");
            query.setParameter("profile", profile);
            currentSession = (edu.udo.scaffoldhunter.model.db.Session) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Fetching Profile.currentSession failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Fetching Profile.currentSession failed", ex);
        }

        profile.setCurrentSession(currentSession);
    }

    @Override
    public boolean canSaveSession(edu.udo.scaffoldhunter.model.db.Session session) throws DatabaseException {
        Preconditions.checkNotNull(session);

        // only mySQL has this limt
        if (hibernateDialect.equals(MySQL5InnoDBDialect.class.getCanonicalName())) {
            try {
                Connection dbConnection = getNativeDbConnection();
                Statement stmt = dbConnection.createStatement();
                ResultSet result = stmt.executeQuery("SELECT @@max_allowed_packet;");

                if (result.next()) {
                    int maxPackageSize = result.getInt(1);

                    /*
                     * java char is two byte long, but the sql connections uses
                     * utf8, therefore the length of the array should be a rough
                     * approximation of the package size.
                     * 
                     * x1.2 because we usually have some overhead. (the other
                     * objects data, etc)
                     */
                    double estimatedSizeOfPackage = 1.2 * session.getSessionData().toCharArray().length;
                    logger.debug("max package size: {}", maxPackageSize);
                    logger.debug("estimated size of package: {}", estimatedSizeOfPackage);
                    return maxPackageSize > estimatedSizeOfPackage;
                } else {
                    logger.error("Global valiable max_allowed_packet cannot be retrieved. Empty ResultSet.");
                    throw new DatabaseException(
                            "Global valiable max_allowed_packet cannot be retrieved. Empty ResultSet.");
                }
            } catch (SQLException ex) {
                logger.error("SQL error while retrieving global variable max_allowed_packet\n{}\n{}", ex,
                        stacktrace(ex));
                throw new DatabaseException("SQL error while retrieving global variable max_allowed_packet", ex);
            }
        } else if (hibernateDialect.equals(HSQLDialectValid.class.getCanonicalName())) {
            return true;
        } else {
            throw new AssertionError("Unsupported Dialect");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Dataset> getAllDatasets() throws DatabaseException {
        List<Dataset> datasets;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            datasets = hibernateSession.createQuery("from Dataset order by Title").list();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Query from Dataset failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query from Dataset failed", ex);
        }

        return datasets;
    }

    @Override
    public List<Molecule> getAllMolecules(Dataset dataset) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("FROM Molecule molecule WHERE dataset = :dataset");
            query.setParameter("dataset", dataset);
            @SuppressWarnings("unchecked")
            List<Molecule> molecules = query.list();
            hibernateSession.getTransaction().commit();
            return molecules;
        } catch (HibernateException ex) {
            logger.error("Could not fetch Molecules from Database.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Could not fetch Molecules from Database", ex);
        }
    }

    @Override
    public Subset getRootSubset(edu.udo.scaffoldhunter.model.db.Session session) throws DatabaseException {
        return getRootSubset(session, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Subset getRootSubset(edu.udo.scaffoldhunter.model.db.Session session, Filterset filterset)
            throws DatabaseException {
        logger.trace("Entering getRootSubset");

        Session hibernateSession = null;
        Map<String, PropertyDefinition> propDefParameterNames = new HashMap<String, PropertyDefinition>();
        Subset retVal = new Subset(null, "Root", "The root Subset", session, null, new LinkedList<Subset>());
        Dataset dataset = session.getTree().getDataset();
        StringBuilder queryString = new StringBuilder("FROM Molecule m WHERE m.dataset = :dataset ");

        /*
         * Build query string
         */
        if (filterset != null && filterset.getFilters().size() > 0) {
            getFiltersetQueryPart(dataset, filterset, propDefParameterNames, queryString);
        }

        /*
         * Execute query and set parameter
         */
        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery(queryString.toString());
            query.setParameter("dataset", dataset);
            for (Map.Entry<String, PropertyDefinition> propDefParameterEntry : propDefParameterNames.entrySet()) {
                query.setParameter(propDefParameterEntry.getKey(), propDefParameterEntry.getValue());
            }

            retVal.setMolecules(new HashSet<Molecule>(query.list()));
        } catch (HibernateException ex) {
            logger.error("Query to get Root Subset failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query to get Root Subset failed", ex);
        }

        return retVal;
    }

    @Override
    public int getRootSubsetSize(Dataset dataset, Filterset filterset) throws DatabaseException {
        logger.trace("Entering getRootSubsetSize");

        Session hibernateSession = null;
        Map<String, PropertyDefinition> propDefParameterNames = new HashMap<String, PropertyDefinition>();
        Integer retVal = 0;
        StringBuilder queryString = new StringBuilder("SELECT count(m) FROM Molecule m WHERE m.dataset = :dataset ");

        /*
         * Build query string
         */
        if (filterset != null && filterset.getFilters().size() > 0) {
            getFiltersetQueryPart(dataset, filterset, propDefParameterNames, queryString);
        }

        /*
         * Execute query and set parameter
         */
        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery(queryString.toString());
            query.setParameter("dataset", dataset);
            for (Map.Entry<String, PropertyDefinition> propDefParameterEntry : propDefParameterNames.entrySet()) {
                query.setParameter(propDefParameterEntry.getKey(), propDefParameterEntry.getValue());
            }

            retVal = ((Long) query.uniqueResult()).intValue();
        } catch (HibernateException ex) {
            logger.error("Query to get Root Subset failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query to get Root Subset failed", ex);
        }

        return retVal;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Subset getFilteredSubset(Subset subset, Filterset filterset) throws DatabaseException {
        logger.trace("Entering getFilteredSubset");

        Session hibernateSession = null;
        Map<String, PropertyDefinition> propDefParameterNames = new HashMap<String, PropertyDefinition>();
        Set<Integer> filteredIds = null;
        Subset retVal = new Subset(subset, "filtered(" + subset.getTitle() + ")", "Filtered Subset",
                subset.getSession(), new LinkedList<Molecule>(), new LinkedList<Subset>());
        StringBuilder queryString = new StringBuilder("SELECT m.id FROM Molecule m, Subset s JOIN s.molecules submol "
                + "WHERE s = :subset AND m = submol ");

        /*
         * Build query string
         */
        if (filterset != null && filterset.getFilters().size() > 0) {
            getFiltersetQueryPart(subset.getSession().getDataset(), filterset, propDefParameterNames, queryString);
        }

        /*
         * Execute query and set parameter
         */
        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery(queryString.toString());
            query.setParameter("subset", subset);
            for (Map.Entry<String, PropertyDefinition> propDefParameterEntry : propDefParameterNames.entrySet()) {
                query.setParameter(propDefParameterEntry.getKey(), propDefParameterEntry.getValue());
            }

            filteredIds = new HashSet<Integer>(query.list());
        } catch (HibernateException ex) {
            logger.error("Query to filter a Subset failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query to filter a Subset failed", ex);
        }

        /*
         * Fill the new Subset with currently Loaded Molecules
         */
        for (Molecule molecule : subset.getMolecules()) {
            if (filteredIds.contains(molecule.getId())) {
                retVal.getMolecules().add(molecule);
            }
        }
        return retVal;
    }

    @Override
    public int getFilteredSubsetSize(Subset subset, Filterset filterset) throws DatabaseException {
        logger.trace("Entering getFilteredSubsetSize");

        Session hibernateSession = null;
        Map<String, PropertyDefinition> propDefParameterNames = new HashMap<String, PropertyDefinition>();
        int retVal;
        StringBuilder queryString = new StringBuilder("SELECT count(m.id) FROM Molecule m, Subset s JOIN "
                + "s.molecules submol WHERE s = :subset AND m = submol ");

        /*
         * Build query string
         */
        if (filterset != null && filterset.getFilters().size() > 0) {
            getFiltersetQueryPart(subset.getSession().getDataset(), filterset, propDefParameterNames, queryString);
        }

        /*
         * Execute query and set parameter
         */
        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery(queryString.toString());
            query.setParameter("subset", subset);
            for (Map.Entry<String, PropertyDefinition> propDefParameterEntry : propDefParameterNames.entrySet()) {
                query.setParameter(propDefParameterEntry.getKey(), propDefParameterEntry.getValue());
            }

            retVal = ((Long) query.uniqueResult()).intValue();
        } catch (HibernateException ex) {
            logger.error("Query to filter a Subset failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query to filter a Subset failed", ex);
        }

        return retVal;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Scaffold getScaffolds(Subset subset, boolean cutStem) throws DatabaseException {
        Preconditions.checkNotNull(subset.getSession());
        Preconditions.checkNotNull(subset.getSession().getTree());

        List<Scaffold> scaffoldList;
        Tree tree = subset.getSession().getTree();
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            // loading the whole tree and then throwing away the scaffolds we
            // don't need seems to be much faster than retrieving only the
            // scaffolds with generation molecules in the current subset
            Criteria criteriaScaf = hibernateSession.createCriteria(Scaffold.class).add(Restrictions.eq("tree", tree));
            scaffoldList = criteriaScaf.list();
        } catch (HibernateException ex) {
            logger.error("Query from Scaffold failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query from Scaffold failed", ex);
        }

        Map<Integer, Molecule> mols = new HashMap<Integer, Molecule>();
        Set<Molecule> subMols = subset.getMolecules();
        for (Molecule m : subMols)
            mols.put(m.getId(), m);

        Set<Scaffold> scaffolds = Sets.newHashSet();

        /*
         * determine which scaffolds have molecules in the subset and add
         * molecules to scaffolds
         */
        try {
            hibernateSession = sessionFactory.getCurrentSession();

            /*
             * load tuples (ScaffoldId, GenerationMoleculeId) for the current
             * tree
             */
            Criteria criteria = hibernateSession
                    .createCriteria(Scaffold.class)
                    .createAlias("generationMolecules", "mols")
                    .add(Restrictions.eq("tree", tree))
                    .setProjection(
                            Projections.projectionList().add(Projections.id()).add(Projections.property("mols.id")));

            List<Object[]> tuples = criteria.list();
            Multimap<Integer, Molecule> scaffoldMolecules = HashMultimap.create(scaffoldList.size(), 10);
            for (Object[] t : tuples) {
                Molecule mol = mols.get(t[1]);
                if (mol != null)
                    scaffoldMolecules.put((Integer) t[0], mol);
            }

            for (Scaffold s : scaffoldList) {
                if (!scaffoldMolecules.containsKey(s.id))
                    continue;
                Collection<Molecule> subScafMols = scaffoldMolecules.get(s.id);
                s.setMolecules(Sets.newHashSet(subScafMols));
                scaffolds.add(s);
            }

            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Query from Molecule failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query from Molecule failed", ex);
        }

        /*
         * add parent scaffolds to the set, that do not have molecules and thus
         * were not returned from the database
         */
        Set<Scaffold> parents = new HashSet<Scaffold>();
        for (Scaffold s : scaffolds) {
            addParents(s, parents, scaffolds);
        }
        scaffolds.addAll(parents);

        if (scaffolds.isEmpty())
            return null;

        Scaffold root = Scaffolds.getRoot(scaffolds.iterator().next());
        Scaffolds.sort(root, Orderings.STRUCTURE_BY_ID);

        for (Scaffold s : Scaffolds.getSubtreePreorderIterable(root)) {
            s.setTree(tree);
        }
        // remove the imaginary root if it has only one child
        if (root.getChildren().size() == 1) {
            root = root.getChildren().get(0);
            root.setParent(null);
        }
        // remove virtual root scaffolds with only one child
        while (cutStem && root.getChildren().size() == 1 && root.getMolecules().isEmpty()) {
            root = root.getChildren().get(0);
        }
        root.setParent(null);
        return root;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Link> getAllLinks() throws DatabaseException {
        List<Link> links;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            links = hibernateSession.createQuery("from Link").list();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Query from Link failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query from Link failed", ex);
        }

        return links;
    }

    @Override
    public void saveOrUpdate(DbObject obj) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            hibernateSession.saveOrUpdate(obj);
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Storing or updating of Object failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Storing or updating of Object failed", ex);
        }
    }

    @Override
    public void saveOrUpdateAll(Iterable<? extends DbObject> objs) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            for (DbObject obj : objs) {
                hibernateSession.saveOrUpdate(obj);
            }
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Storing or updating of Objects failed.\n{}\n{}", ex, stacktrace(ex));
            try {
                if (hibernateSession != null && hibernateSession.getTransaction() != null)
                    hibernateSession.getTransaction().rollback();
            } catch (HibernateException ex2) {
                logger.error("Rollback failed.\n{}\n{}", ex2, stacktrace(ex2));
            }
            throw new DatabaseException("Storing or updating of Objects failed", ex);
        }
    }

    @Override
    public void saveAsNew(DbObject obj) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            hibernateSession.save(obj);
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Storing of Object failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Storing of Object failed", ex);
        }
    }

    @Override
    public void saveAllAsNew(Iterable<? extends DbObject> objs) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            for (DbObject obj : objs) {
                hibernateSession.save(obj);
            }
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Storing of Objects failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Storing of Objects failed", ex);
        }
    }

    @Override
    public void deleteAll(Iterable<? extends DbObject> objs) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            for (DbObject obj : objs) {
                hibernateSession.delete(obj);
            }
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Deletion of Objects failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Deletion of Objects failed", ex);
        }
    }

    @Override
    public void delete(DbObject obj) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            hibernateSession.delete(obj);
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Deletion of Object failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Deletion of Object failed", ex);
        }
    }

    @Override
    public void deleteAllMolecules(Dataset dataset) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();

            // delete stringProperties
            Query stringPropertyQuery = hibernateSession
                    .createQuery("DELETE MoleculeStringProperty p WHERE p.molecule IN "
                            + "(FROM Molecule m where m.dataset = :dataset)");
            stringPropertyQuery.setParameter("dataset", dataset);
            stringPropertyQuery.executeUpdate();

            // delete numProperties
            Query numPropertyQuery = hibernateSession.createQuery("DELETE MoleculeNumProperty p WHERE p.molecule IN "
                    + "(FROM Molecule m where m.dataset = :dataset)");
            numPropertyQuery.setParameter("dataset", dataset);
            numPropertyQuery.executeUpdate();

            // delete Banner
            Query bannerQuery = hibernateSession.createQuery("DELETE Banner b WHERE b.molecule IN "
                    + "(FROM Molecule m where m.dataset = :dataset)");
            bannerQuery.setParameter("dataset", dataset);
            bannerQuery.executeUpdate();

            // delete Molecules
            Query query = hibernateSession.createQuery("DELETE Molecule m where m.dataset = :dataset");
            query.setParameter("dataset", dataset);
            query.executeUpdate();
        } catch (HibernateException ex) {
            logger.error("An error occured while deleting molecules and depending DbObjects", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("An error occured during molecule deletion.", ex);
        }
    }

    @Override
    public void lockAndLoad(PropertyDefinition propDef, Structure structure) throws DatabaseException {
        lockAndLoad(Collections.singleton(propDef), Collections.singleton(structure));
    }

    private Class<? extends Property> getPropertyClass(PropertyDefinition propDef) {
        if (propDef.isScaffoldProperty()) {
            if (propDef.isStringProperty()) {
                return ScaffoldStringProperty.class;
            } else {
                return ScaffoldNumProperty.class;
            }
        } else {
            if (propDef.isStringProperty()) {
                return MoleculeStringProperty.class;
            } else {
                return MoleculeNumProperty.class;
            }
        }
    }

    /**
     * Increases to lock of a {@link Property} for a given {@link Structure} if
     * they are not already in addedProperties. It adds the {@link Property} to
     * addedProperties.
     * 
     * @param struc
     *            the {@link Structure}
     * @param propDef
     *            The {@link PropertyDefinition} to identify the
     *            {@link Property}
     * @param addedProperties
     *            the already added Properties
     */
    private void increaseLock(Structure struc, PropertyDefinition propDef, Multimap<Structure, Integer> addedProperties) {
        if (!addedProperties.get(struc).contains(propDef.id)) {
            Integer lockCount = struc.locks.get(propDef.id);
            if (lockCount == null) {
                struc.locks.put(propDef.id, 1);
            } else {
                struc.locks.put(propDef.id, lockCount + 1);
            }
            addedProperties.put(struc, propDef.id);
        }
    }

    @Override
    public void lockAndLoad(Iterable<PropertyDefinition> propDefs, Iterable<? extends Structure> structures)
            throws DatabaseException {

        /*
         * We need to remember the found Properties for each Structure. This
         * allows a later detection of undefined Properties. We need to increase
         * the lockCount for them too!
         * 
         * Structure -> PropertyDefinitionId
         */
        Multimap<Structure, Integer> addedProperties = HashMultimap.create();

        /*
         * All Structures that have currently not loaded Properties
         */
        Set<Structure> toLoad = Sets.newHashSet();

        Session hibernateSession = null;

        for (Structure struc : structures) {
            synchronized (struc) {
                for (PropertyDefinition propDef : propDefs) {
                    Integer lockCount = struc.locks.get(propDef.id);
                    if (lockCount == null || lockCount == 0) {
                        toLoad.add(struc);
                    } else {
                        /*
                         * increment lockCount now and not later prevents
                         * parallel unlockAndUnload to destroy the Property
                         * before lockAndLoad is finished
                         */
                        struc.locks.put(propDef.id, lockCount + 1);
                        addedProperties.put(struc, propDef.id);
                    }
                }
            }
        }

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            /*
             * Molecule.id -> Molecule
             */
            Map<Integer, Molecule> mols = Maps.newHashMap();
            for (Molecule mol : Iterables.filter(toLoad, Molecule.class)) {
                mols.put(mol.id, mol);
            }
            /*
             * Scaffold.id -> Scaffold
             */
            Map<Integer, Scaffold> scaffolds = Maps.newHashMap();
            for (Scaffold scaf : Iterables.filter(toLoad, Scaffold.class)) {
                scaffolds.put(scaf.id, scaf);
            }
            /*
             * PropertyDefinition.id -> PropertyDefinition
             */
            Map<Integer, PropertyDefinition> propertyDefinitions = Maps.newHashMap();
            for (PropertyDefinition p : propDefs) {
                propertyDefinitions.put(p.id, p);
            }

            List<Predicate<PropertyDefinition>> preds = ImmutableList.of(SHPredicates.IS_NUMMOL_PROPDEF,
                    SHPredicates.IS_NUMSCAF_PROPDEF, SHPredicates.IS_STRMOL_PROPDEF, SHPredicates.IS_STRSCAF_PROPDEF);

            /*
             * One round for every combination {Molecule,Scaffold} x
             * {String,Numeric}
             */
            for (Predicate<PropertyDefinition> pred : preds) {
                Iterable<PropertyDefinition> predPropDefs = Iterables.filter(propDefs, pred);
                if (Iterables.isEmpty(predPropDefs)) {
                    continue;
                }
                /*
                 * We only need this to differentiate property types
                 * (Scaffold/Molecule) and (String/Num)
                 */
                PropertyDefinition firstPropDef = predPropDefs.iterator().next();
                Class<? extends Property> clazz = getPropertyClass(firstPropDef);
                List<? extends Structure> strucs = Lists.newArrayList(firstPropDef.isScaffoldProperty() ? scaffolds
                        .values() : mols.values());
                if (strucs.isEmpty()) {
                    continue;
                }
                String struc = firstPropDef.isScaffoldProperty() ? "scaffold" : "molecule";

                Function<DbObject, Integer> dbObjectToID = new Function<DbObject, Integer>() {
                    @Override
                    public Integer apply(DbObject input) {
                        return input.id;
                    }
                };

                String propids = Joiner.on(',').join(Iterables.transform(predPropDefs, dbObjectToID));

                /*
                 * Split strucs to a partition with at most 10000 Structures to
                 * avoid too large queries for the database
                 */
                while (strucs.size() > 0) {
                    /*
                     * get the part of strucs that will be used by the (next)
                     * query
                     */
                    List<? extends Structure> subStrucs = strucs.subList(0, Math.min(10000, strucs.size()));

                    String strucids = Joiner.on(',').join(Iterables.transform(subStrucs, dbObjectToID));

                    /*
                     * XXX: This would look much better using criteria, but with
                     * Criteria projecting to an Entity and some values seems
                     * impossible
                     * 
                     * XXX: Performance wise it might be better for large
                     * queries to retrieve the whole subset and then sort out
                     * the values we need
                     */
                    Query query = hibernateSession.createQuery("SELECT struc.id, t.id, prop " + "FROM "
                            + clazz.getName() + " prop JOIN prop.type t JOIN prop." + struc + " struc "
                            + "WHERE struc.id in (" + strucids + ") AND t.id in (" + propids + ")");

                    @SuppressWarnings("unchecked")
                    List<Object[]> results = query.list();

                    /*
                     * Load each Property into the related Structure
                     */
                    if (clazz.equals(MoleculeNumProperty.class)) {
                        for (Object[] r : results) {
                            Molecule mol = mols.get(r[0]);
                            Integer propDefId = (Integer) r[1];
                            PropertyDefinition propDef = propertyDefinitions.get(propDefId);
                            MoleculeNumProperty prop = (MoleculeNumProperty) r[2];
                            prop.setType(propDef);
                            prop.setMolecule(mol);
                            synchronized (mol) {
                                /*
                                 * this prevents the replacement of a concurrent
                                 * loaded property
                                 */
                                if (mol.getNumProperties().get(propDefId) == null) {
                                    mol.getNumProperties().put(propDefId, prop);
                                }
                                increaseLock(mol, propDef, addedProperties);
                            }
                        }
                    } else if (clazz.equals(MoleculeStringProperty.class)) {
                        for (Object[] r : results) {
                            Molecule mol = mols.get(r[0]);
                            Integer propDefId = (Integer) r[1];
                            PropertyDefinition propDef = propertyDefinitions.get(propDefId);
                            MoleculeStringProperty prop = (MoleculeStringProperty) r[2];
                            prop.setType(propDef);
                            prop.setMolecule(mol);
                            synchronized (mol) {
                                /*
                                 * this prevents the replacement of a concurrent
                                 * loaded property
                                 */
                                if (mol.getStringProperties().get(propDefId) == null) {
                                    mol.getStringProperties().put(propDefId, prop);
                                }
                                increaseLock(mol, propDef, addedProperties);
                            }
                        }
                    } else if (clazz.equals(ScaffoldNumProperty.class)) {
                        for (Object[] r : results) {
                            Scaffold scaf = scaffolds.get(r[0]);
                            Integer propDefId = (Integer) r[1];
                            PropertyDefinition propDef = propertyDefinitions.get(propDefId);
                            ScaffoldNumProperty prop = (ScaffoldNumProperty) r[2];
                            prop.setType(propDef);
                            prop.setScaffold(scaf);
                            synchronized (scaf) {
                                /*
                                 * this prevents the replacement of a concurrent
                                 * loaded property
                                 */
                                if (scaf.getNumProperties().get(propDefId) == null) {
                                    scaf.getNumProperties().put(propDefId, prop);
                                }
                                increaseLock(scaf, propDef, addedProperties);
                            }
                        }
                    } else if (clazz.equals(ScaffoldStringProperty.class)) {
                        for (Object[] r : results) {
                            Scaffold scaf = scaffolds.get(r[0]);
                            Integer propDefId = (Integer) r[1];
                            PropertyDefinition propDef = propertyDefinitions.get(propDefId);
                            ScaffoldStringProperty prop = (ScaffoldStringProperty) r[2];
                            prop.setType(propDef);
                            prop.setScaffold(scaf);
                            synchronized (scaf) {
                                /*
                                 * this prevents the replacement of a concurrent
                                 * loaded property
                                 */
                                if (scaf.getStringProperties().get(propDefId) == null) {
                                    scaf.getStringProperties().put(propDefId, prop);
                                }
                                increaseLock(scaf, propDef, addedProperties);
                            }
                        }
                    } else {
                        throw new AssertionError("Unhandled type");
                    }

                    // remove subStrucs form strucs
                    subStrucs.clear();
                }
            }

            /*
             * increases the lock of all undefined Properties
             */
            for (Structure struc : structures) {
                synchronized (struc) {
                    for (PropertyDefinition propDef : propDefs) {
                        increaseLock(struc, propDef, addedProperties);
                    }
                }
            }

            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Querying of Property failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Querying of Property failed", ex);
        }
    }

    @Override
    public void unlockAndUnload(PropertyDefinition propDef, Structure structure) {
        synchronized (structure) {
            Map<Integer, Integer> locks = structure.locks;

            if (locks.containsKey(propDef.id)) {
                Integer numLocks = locks.get(propDef.getId());
                numLocks--;
                if (numLocks == 0) {
                    // removing Property
                    locks.remove(propDef.getId());
                    if (propDef.isStringProperty()) {
                        structure.getStringProperties().remove(propDef.getId());
                    } else {
                        structure.getNumProperties().remove(propDef.getId());
                    }
                } else {
                    locks.put(propDef.getId(), numLocks);
                }
            } else {
                logger.warn("Trying to remove lock where no lock is set");
                throw new UnlockException("Trying to remove lock where no lock is set");
            }
        }
    }

    @Override
    public void unlockAndUnload(Iterable<PropertyDefinition> propDefs, Iterable<? extends Structure> structures) {
        for (Structure structure : structures) {
            for (PropertyDefinition propDef : propDefs) {
                unlockAndUnload(propDef, structure);
            }
        }
    }

    @Override
    public String getSvgString(Structure structure) throws DatabaseException {
        String result;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession
                    .createQuery("select struc.svg.string from Structure as struc where struc=:structure");
            query.setParameter("structure", structure);
            result = (String) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Could not fetch SVG String from Database.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Could not fetch SVG String from Database", ex);
        }
        return result;
    }

    @Override
    public String getStrucMol(Structure structure) throws DatabaseException {
        String result;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession
                    .createQuery("select struc.mol.string from Structure as struc where struc=:structure");
            query.setParameter("structure", structure);
            result = (String) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Could not fetch Mol String from Database.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Could not fetch Mol String from Database", ex);
        }
        return result;
    }

    @Override
    public Map<AccumulationFunction, Double> getAccNumPropertyDataset(PropertyDefinition property)
            throws DatabaseException {
        Map<AccumulationFunction, Double> results = Maps.newEnumMap(AccumulationFunction.class);
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();

            String propertyType;
            if (property.isScaffoldProperty()) {
                propertyType = "ScaffoldNumProperty";
            } else {
                propertyType = MoleculeNumProperty.class.getName();
            }

            Query query = hibernateSession
                    .createQuery("SELECT avg(prop.value), max(prop.value), min(prop.value), sum(prop.value) " + "FROM "
                            + propertyType + " prop " + "WHERE prop.type = :property");
            query.setParameter("property", property);

            Object[] r = (Object[]) query.uniqueResult();
            hibernateSession.getTransaction().commit();

            results.put(AccumulationFunction.Average, (Double) r[0]);
            results.put(AccumulationFunction.Maximum, (Double) r[1]);
            results.put(AccumulationFunction.Minimum, (Double) r[2]);
            results.put(AccumulationFunction.Sum, (Double) r[3]);

            return results;
        } catch (HibernateException ex) {
            hibernateSession.getTransaction().rollback();
            logger.error("Querying of accumulated Property failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Querying of accumulated Property failed", ex);
        }
    }

    @Override
    public Double getAccNumPropertyScaffold(PropertyDefinition property, AccumulationFunction function,
            Scaffold scaffold, boolean withSubtree) throws DatabaseException {
        Preconditions.checkArgument(!property.isScaffoldProperty(), "Only molecule properties accepted!");
        Preconditions.checkArgument(property.getPropertyType() == PropertyType.NumProperty);
        
        Double result = 0.0;
        Set<Molecule> molecules = null;
        Session hibernateSession = null;

        if (withSubtree) {
            molecules = new HashSet<Molecule>();
            addChildMolecules(scaffold, molecules);
        } else {
            molecules = scaffold.getMolecules();
        }

        if (molecules.size() == 0)
            throw new IllegalArgumentException("scaffold has no molecules");

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            String stmt = "";

            switch (function) {
            case Average:
                stmt = "Select avg(prop.value) from MoleculeNumProperty prop where prop.molecule in (:molecules) and type = :type";
                break;
            case Minimum:
                stmt = "Select min(prop.value) from MoleculeNumProperty prop where prop.molecule in (:molecules) and type = :type";
                break;
            case Maximum:
                stmt = "Select max(prop.value) from MoleculeNumProperty prop where prop.molecule in (:molecules) and type = :type";
                break;
            case Sum:
                stmt = "Select sum(prop.value) from MoleculeNumProperty prop where prop.molecule in (:molecules) and type = :type";
                break;
            default:
                throw new DatabaseException("This AccumulationFunction is not supported");
            }
            Query query = hibernateSession.createQuery(stmt);
            query.setParameterList("molecules", molecules);
            query.setParameter("type", property);
            result = (Double) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Querying of accumulated Property failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Querying of accumulated Property failed", ex);
        }

        return result;
    }

    @Override
    public Map<Scaffold, Double> getAccNumProperties(PropertyDefinition propDef,
            AccumulationFunction accumulation, Subset subset, Scaffold root, boolean subtreeCumulative)
            throws DatabaseException {
        Preconditions.checkArgument(!propDef.isStringProperty());
        
        /*
         * Scaffold -> accumulated return values
         */
        Map<Scaffold, Double> ret = Maps.newHashMap();
        /*
         * Scaffold -> how many values are accumulated. They can be different
         * from scaffold.getMolecules.size because there may be undefined
         * properties
         * 
         * only needed for subtreeCumulative
         */
        Map<Scaffold, Integer> counts = Maps.newHashMap();
        Session hibernateSession = null;
        String acc;

        switch (accumulation) {
        case Average:
            acc = "avg";
            break;
        case Maximum:
            acc = "max";
            break;
        case Minimum:
            acc = "min";
            break;
        case Sum:
            acc = "sum";
            break;
        default:
            throw new IllegalArgumentException("Unsupported accumulation function");
        }

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            
            if(!propDef.isScaffoldProperty()) {
                /*
                 * select pairs of scaffold_id and accumulated value for each single
                 * scaffold (not tree accumulated!)
                 */
                hibernateSession.beginTransaction();
                Query query = hibernateSession.createSQLQuery("SELECT scaf.structure_id, " + acc
                        + "(props.value), count(scaf.structure_id) " + "FROM scaffold_data scaf "
                        + "JOIN molecule_scaffold_relationship r ON scaf.structure_id = r.scaffold_id "
                        + "JOIN molecule_num_properties props ON r.molecule_id = props.structure_id "
                        + "JOIN subset_molecule_relationship subs ON r.molecule_id = subs.molecule_id "
                        + "WHERE props.property_id = :propDef " + "AND subs.subset_id = :subset "
                        + "AND scaf.tree_id = :tree " + "GROUP BY scaf.structure_id");
                // HQL Query yielding the same result but taking >5 times as long
                // Query query = session.createQuery("SELECT scaf.id, " + acc +
                // "(prop.value) FROM" +
                // " Scaffold scaf join scaf.generationMolecules mols, MoleculeNumProperty prop"
                // +
                // " WHERE mols = prop.molecule AND mols IN (SELECT submols from Subset subset join subset.molecules as submols where subset = :subset)"
                // +
                // " AND prop.type = :propDef AND scaf.tree = :tree GROUP BY scaf");
                query.setParameter("propDef", propDef);
                query.setParameter("subset", subset);
                query.setParameter("tree", root.getTree());
    
                @SuppressWarnings("unchecked")
                List<Object[]> queryResult = query.list();
                hibernateSession.getTransaction().commit();
    
                Map<Integer, IntegerDoublePair> resultMap = Maps.newHashMapWithExpectedSize(queryResult.size());
                for (Object[] o : queryResult) {
                    Integer count;
                    if(o[2] instanceof BigInteger)
                        count = ((BigInteger) o[2]).intValue();
                    else
                        count = (Integer)(o[2]);
                    Double value = (Double) o[1];
                    resultMap.put((Integer) o[0], new IntegerDoublePair(count, value));
                }
                for (Scaffold scaffold : Scaffolds.getSubtreePreorderIterable(root)) {
                    IntegerDoublePair pair = resultMap.get(scaffold.getId());
                    /*
                     * pairs are null iff all properties are undefined for all
                     * molecules associated with the scaffold
                     */
                    ret.put(scaffold, pair == null ? null : pair.d);
                    counts.put(scaffold, pair == null ? 0 : pair.i);
                }
            }
            else{
                // scaffold properties are passed into the map
                
                final Iterable<Scaffold> scaffolds = Scaffolds.getSubtreePreorderIterable(root);
                
                // acquire data
                lockAndLoad(Arrays.asList(propDef), scaffolds);
                
                for (Scaffold scaffold : scaffolds) {
                    ret.put(scaffold, scaffold.getNumPropertyValue(propDef));
                    counts.put(scaffold, 1);
                }
                
                // unlock data
                unlockAndUnload(Arrays.asList(propDef), scaffolds);

            }

            if (subtreeCumulative) {
                /*
                 * We will go bottom up layer by layer and write the accumulated
                 * value to the parent node
                 */
                List<Scaffold> scaffolds = Orderings.SCAFFOLDS_BY_HIERARCHY_LEVEL.reverse().sortedCopy(
                        Scaffolds.getSubtreePreorderIterable(root));
                // the root has no parent, so we can leave it out
                Scaffold r = scaffolds.remove(scaffolds.size() - 1);
                assert r == root;

                for (Scaffold scaf : scaffolds) {
                    Double val = ret.get(scaf);
                    if (val == null) {
                        /*
                         * all properties of molecules in this subtree are
                         * undefined
                         * 
                         * nothing to propagate upwards
                         */
                        continue;
                    }
                    Scaffold parent = scaf.getParent();
                    assert parent != null;
                    Double parentVal = ret.get(parent);

                    switch (accumulation) {
                    case Average:
                        if (parentVal == null) {
                            /*
                             * there are no molecules with defined properties
                             * for parent yet -> propagate current value up
                             */
                            parentVal = val;
                            counts.put(parent, counts.get(scaf));
                        } else {
                            Integer parentCount = counts.get(parent);
                            Integer count = counts.get(scaf);
                            Integer accumulatedCount = parentCount + count;
                            parentVal = (parentVal * parentCount + val * count) / accumulatedCount;
                            counts.put(parent, accumulatedCount);
                        }
                        ret.put(parent, parentVal);
                        break;
                    case Maximum:
                        if (parentVal == null) {
                            parentVal = Double.NEGATIVE_INFINITY;
                        }
                        ret.put(parent, Math.max(val, parentVal));
                        break;
                    case Minimum:
                        if (parentVal == null) {
                            parentVal = Double.POSITIVE_INFINITY;
                        }
                        ret.put(parent, Math.min(val, parentVal));
                        break;
                    case Sum:
                        if (parentVal == null) {
                            parentVal = 0.0;
                        }
                        ret.put(parent, val + parentVal);
                        break;
                    default:
                        throw new AssertionError("unsupported accumulation function");
                    }
                }
            }
            return ret;
        } catch (HibernateException ex) {
            logger.error("Querying of accumulated Property failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Querying of accumulated Property failed", ex);
        }
    }

    @Override
    public Double getAccNumPropertySubset(PropertyDefinition property, AccumulationFunction function, Subset subset)
            throws DatabaseException {
        Double result = 0.0;
        Set<Molecule> molecules = subset.getMolecules();
        if (molecules.size() == 0)
            throw new IllegalArgumentException("subset has no molecules");

        Session hibernateSession = null;
        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            String stmt = "";
            switch (function) {
            case Average:
                stmt = "Select avg(prop.value) from MoleculeNumProperty prop, Subset subset join subset.molecules mol where subset = :subset and prop.molecule = mol and prop.type = :type";
                break;
            case Minimum:
                stmt = "Select min(prop.value) from MoleculeNumProperty prop, Subset subset join subset.molecules mol where subset = :subset and prop.molecule = mol and prop.type = :type";
                break;
            case Maximum:
                stmt = "Select max(prop.value) from MoleculeNumProperty prop, Subset subset join subset.molecules mol where subset = :subset and prop.molecule = mol and prop.type = :type";
                break;
            case Sum:
                stmt = "Select sum(prop.value) from MoleculeNumProperty prop, Subset subset join subset.molecules mol where subset = :subset and prop.molecule = mol and prop.type = :type";
                break;
            default:
                throw new DatabaseException("This AccumulationFunction is not supported");
            }
            Query query = hibernateSession.createQuery(stmt);
            query.setParameter("subset", subset);
            query.setParameter("type", property);
            result = (Double) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Querying of accumulated Property failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Querying of accumulated Property failed", ex);
        }
        return result;
    }

    @Override
    public double[] getAccPropertyMinMax(Tree tree, PropertyDefinition propDef, AccumulationFunction acc, Subset subset,
            boolean subtreeCumulative, boolean removeVirtualRoot, boolean includeMoleculeData)
            throws DatabaseException {
        Preconditions.checkArgument(!propDef.isStringProperty());

        Session hibernateSession = null;
        hibernateSession = sessionFactory.getCurrentSession();

        try {            
            double[] minmax = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
            
            if(!propDef.isScaffoldProperty()) {
                if(includeMoleculeData) {
                    // search for min/max in all molecule data, if flag is set
                    Transaction transaction = hibernateSession.beginTransaction();
                    Query query = hibernateSession.createSQLQuery("SELECT min(value), max(value) FROM ( "
                            + "SELECT value FROM molecule_num_properties props "
                            + "JOIN subset_molecule_relationship subs ON props.structure_id = subs.molecule_id "
                            + "WHERE props.property_id = :propdef "
                            + "AND subs.subset_id = :subset "
                            + ") virtual_table");
                    query.setParameter("propdef", propDef);
                    query.setParameter("subset", subset);
                    Object[] result = (Object[]) query.uniqueResult();
                    transaction.commit();
                    if (result != null && result[0] != null && result[1] != null) {
                        minmax[0] = Math.min(minmax[0], (Double) result[0]);
                        minmax[1] = Math.max(minmax[1], (Double) result[1]);
                    }
                }
                else {
                    if(acc != AccumulationFunction.Sum) {
                        /*
                         *  if no molecule data is included and the complete search (see if block below) of
                         *  sum accumulation is not executed, then we have to accumulate the property for
                         *  every scaffold. Full recursive accumulation is not required, because we have no
                         *  sum accumulation
                         */
                        String accstring;
                        switch(acc) {
                        case Average:
                            accstring = "avg"; break;
                        case Minimum:
                            accstring = "min"; break;
                        case Maximum:
                            accstring = "max"; break;
                        default:
                            throw new DatabaseException("This AccumulationFunction is not supported");
                        }
                        Transaction transaction = hibernateSession.beginTransaction();
                        Query query = hibernateSession.createSQLQuery("SELECT min(accvalue), max(accvalue) FROM ( "
                                + "SELECT scaf.structure_id, " + accstring
                                + "(props.value) AS accvalue, count(scaf.structure_id) " + "FROM scaffold_data scaf "
                                + "JOIN molecule_scaffold_relationship r ON scaf.structure_id = r.scaffold_id "
                                + "JOIN molecule_num_properties props ON r.molecule_id = props.structure_id "
                                + "JOIN subset_molecule_relationship subs ON r.molecule_id = subs.molecule_id "
                                + "WHERE props.property_id = :propDef " + "AND subs.subset_id = :subset "
                                + "AND scaf.tree_id = :tree " + "GROUP BY scaf.structure_id"
                                + ") virtual_table");                        
                        query.setParameter("propDef", propDef);
                        query.setParameter("subset", subset);
                        query.setParameter("tree", tree);
                        Object[] result = (Object[]) query.uniqueResult();
                        transaction.commit();                
                        if (result != null && result[0] != null && result[1] != null) {
                            minmax[0] = Math.min(minmax[0], (Double) result[0]);
                            minmax[1] = Math.max(minmax[1], (Double) result[1]);
                        }
                    }
                }
                
                // for sum accumulation we have to consider ALL accumulated values of the whole scaffold tree
                if(acc == AccumulationFunction.Sum) {                    
                    Scaffold root = getScaffolds(subset, true);
                    Map<Scaffold, Double> allValues = getAccNumProperties(propDef, acc, subset, root, subtreeCumulative);
                    
                    // remove virtual root if required
                    if(removeVirtualRoot && root.isImaginaryRoot())
                        allValues.remove(root);
                    
                    // get minmax over scaffolds
                    for(Double d : allValues.values()) {
                        if(d != null) {
                            minmax[0] = Math.min(minmax[0], d);
                            minmax[1] = Math.max(minmax[1], d);
                        }
                    }
                }
            }
            else {
                if(subtreeCumulative) {
                    // for accumulation we have to consider all scaffolds and their values
                    Scaffold root = getScaffolds(subset, true);
                    Map<Scaffold, Double> allValues = getAccNumProperties(propDef, acc, subset, root, subtreeCumulative);
                    
                    // remove virtual root if required
                    if(removeVirtualRoot && root.isImaginaryRoot())
                        allValues.remove(root);
                    
                    // get minmax over scaffolds
                    for(Double d : allValues.values()) {
                        if(d != null) {
                            minmax[0] = Math.min(minmax[0], d);
                            minmax[1] = Math.max(minmax[1], d);
                        }
                    }
                }
                else{
                    // without accumulation the scaffold minmax values are sufficient
                    Transaction transaction = hibernateSession.beginTransaction();
                    Query query = hibernateSession.createSQLQuery("SELECT min(scafval), max(scafval) FROM ( "
                            + "SELECT value AS scafval FROM scaffold_num_properties scaf "
                            + "JOIN molecule_scaffold_relationship r ON r.scaffold_id = scaf.structure_id "
                            + "JOIN subset_molecule_relationship subs ON r.molecule_id = subs.molecule_id "
                            + "WHERE scaf.property_id = :propdef "
                            + "AND subs.subset_id = :subset "
                            + ") virtual_table");
                    
                    query.setParameter("propdef", propDef);
                    query.setParameter("subset", subset);
                    Object[] result = (Object[]) query.uniqueResult();
                    transaction.commit();
                    if (result != null && result[0] != null && result[1] != null) {
                        minmax[0] = Math.min(minmax[0], (Double) result[0]);
                        minmax[1] = Math.max(minmax[1], (Double) result[1]);
                    }
                }
            }
            
            if((minmax[0] == Double.POSITIVE_INFINITY && minmax[1] == Double.NEGATIVE_INFINITY) || minmax[0] > minmax[1]) {
                // no valid minimum and maximum have been found
                minmax[0] = Double.NaN;
                minmax[1] = Double.NaN;
            }
            return minmax;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException(ex);
        }
    }

    @Override
    public long getDistinctValueCount(PropertyDefinition propDef) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            String propertyType;
            if (propDef.isStringProperty()) {
                if (propDef.isScaffoldProperty()) {
                    propertyType = "ScaffoldStringProperty";
                } else {
                    propertyType = "MoleculeStringProperty";
                }
            } else {
                if (propDef.isScaffoldProperty()) {
                    propertyType = "ScaffoldNumProperty";
                } else {
                    propertyType = "MoleculeNumProperty";
                }
            }

            Query query = hibernateSession.createQuery("SELECT count(distinct prop.value) " + "FROM " + propertyType
                    + " prop " + "WHERE prop.type = :propdef");
            query.setParameter("propdef", propDef);

            long l = (Long) query.uniqueResult();
            hibernateSession.getTransaction().commit();

            return l;

        } catch (HibernateException ex) {
            ex.printStackTrace();
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException(ex);
        }
    }

    @Override
    public List<String> getDistinctStrings(PropertyDefinition propDef) throws DatabaseException {
        Preconditions.checkArgument(propDef.isStringProperty());

        Session hibernateSession = null;

        String propertyType;
        if (propDef.isScaffoldProperty()) {
            propertyType = "ScaffoldStringProperty";
        } else {
            propertyType = "MoleculeStringProperty";
        }

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();

            Query query = hibernateSession.createQuery("SELECT distinct prop.value " + "FROM " + propertyType
                    + " prop " + "WHERE prop.type = :propdef");
            query.setParameter("propdef", propDef);

            @SuppressWarnings("unchecked")
            List<String> strings = query.list();

            hibernateSession.getTransaction().commit();

            return strings;

        } catch (HibernateException ex) {
            ex.printStackTrace();
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException(ex);
        }
    }

    @Override
    public Table<Scaffold, String, Integer> getStringDistribution(Scaffold root, Subset subset,
            PropertyDefinition propDef) throws DatabaseException {
        Preconditions.checkArgument(propDef.isStringProperty() && !propDef.isScaffoldProperty());

        Table<Scaffold, String, Integer> dist = HashBasedTable.create();
        Map<Integer, Scaffold> scaffolds = Maps.newHashMap();
        Session hibernateSession = null;

        for (Scaffold scaf : Scaffolds.getSubtreePreorderIterable(root)) {
            scaffolds.put(scaf.getId(), scaf);
        }
        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("SELECT scaf.id, prop.value "
                    + "FROM Scaffold AS scaf JOIN scaf.generationMolecules scafmol, "
                    + " MoleculeStringProperty prop, " + "Subset AS subs JOIN subs.molecules submol "
                    + "WHERE scafmol = prop.molecule " + "AND submol = scafmol " + "AND scaf.tree = :tree "
                    + "AND subs = :subset AND prop.type = :propdef");
            query.setParameter("tree", root.getTree());
            query.setParameter("subset", subset);
            query.setParameter("propdef", propDef);

            @SuppressWarnings("unchecked")
            List<Object[]> result = query.list();

            hibernateSession.getTransaction().commit();

            for (Object[] o : result) {
                Scaffold scaf = scaffolds.get(o[0]);
                String str = (String) o[1];
                if (scaf != null) {
                    Integer i = dist.get(scaf, o[1]);
                    if (i == null) {
                        dist.put(scaf, str, 1);
                    } else {
                        dist.put(scaf, str, i + 1);
                    }
                }
            }

            return dist;
        } catch (HibernateException ex) {
            ex.printStackTrace();
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException(ex);
        }
    }

    @Override
    public Map<Integer, Integer> getSortOrder(Subset subset, PropertyDefinition property, boolean ascending)
            throws DatabaseException {
        Map<Integer, Integer> retVal = new HashMap<Integer, Integer>();
        String order, propertyType;
        Session hibernateSession = null;

        if (ascending) {
            order = "ASC";
        } else {
            order = "DESC";
        }

        switch (property.getPropertyType()) {
        case NumProperty:
            propertyType = "MoleculeNumProperty";
            break;
        default:
            propertyType = "MoleculeStringProperty";
        }

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("Select mol.id, prop.value from " + propertyType + " prop, "
                    + "Subset subset join subset.molecules mol where subset = :subset and "
                    + "prop.molecule = mol and prop.type = :type order by prop.value " + order);
            query.setParameter("subset", subset);
            query.setParameter("type", property);
            @SuppressWarnings("unchecked")
            List<Object[]> queryResults = query.list();
            hibernateSession.getTransaction().commit();

            // fill the map
            int i = 0;
            Object propValue = ascending ? Double.MIN_VALUE : Double.MAX_VALUE;
            Object currentPropValue;
            for (Object[] queryResult : queryResults) {
                currentPropValue = queryResult[1];
                if (!currentPropValue.equals(propValue)) {
                    i++;
                    propValue = currentPropValue;
                }
                retVal.put((Integer) queryResult[0], i);
            }
        } catch (HibernateException ex) {
            logger.error("Could not fetch SVG String from Database.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Could not fetch SVG String from Database", ex);
        }
        return retVal;
    }

    @Override
    public Comment createOrUpdateComment(String comment, boolean priv, Tree tree, Profile profile, Structure structure)
            throws DatabaseException {
        Preconditions.checkNotNull(structure);
        Preconditions.checkNotNull(comment);
        Preconditions.checkNotNull(profile);
        Preconditions.checkNotNull(profile.getCurrentSession());

        Comment commentObj = getComment(priv, tree, profile, structure);
        if (commentObj == null) {
            commentObj = new Comment();
        }
        commentObj.setComment(comment);
        commentObj.setModifiedBy(profile);
        commentObj.setModificationDate(new Date());
        commentObj.setPrivate(priv);
        commentObj.setSmiles(structure.getSmiles());
        commentObj.setTree(tree);
        commentObj.setDataset(profile.getCurrentSession().getTree().getDataset());
        commentObj.setMolecule(structure instanceof Molecule);

        saveOrUpdate(commentObj);

        return commentObj;
    }

    @Override
    public Banner createBanner(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException {
        Preconditions.checkNotNull(profile);
        Preconditions.checkNotNull(structure);
        Preconditions.checkNotNull(tree);

        Banner banner = getBanner(priv, tree, profile, structure);
        if (banner == null) {
            banner = new Banner();
        }
        banner.setPrivate(priv);
        banner.setCreatedBy(profile);
        banner.setTree(tree);
        banner.setStructure(structure);

        saveAsNew(banner);

        return banner;
    }

    @Override
    public Comment getComment(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException {
        Comment comment;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query;
            String treeQueryStr = (tree == null) ? "AND com.tree is null " : "AND com.tree=:tree ";
            String privQueryStr = priv ? "AND com.modifiedBy=:profile" : "";
            query = hibernateSession.createQuery("FROM Comment com WHERE com.private=:priv " + treeQueryStr
                    + "AND com.smiles=:smiles " + privQueryStr);
            if (priv) {
                query.setParameter("profile", profile);
            }
            query.setParameter("priv", priv);
            if (tree != null) {
                query.setParameter("tree", tree);
            }
            query.setParameter("smiles", structure.getSmiles());
            comment = (Comment) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Could not fetch unique Comment from Database.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Could not fetch unique Comment from Database", ex);
        }

        // set lazy properties
        if (comment != null) {
            if (priv) {
                comment.setModifiedBy(profile);
            } else {
                comment.setModifiedBy(null);
            }
            comment.setDataset(profile.getCurrentSession().getDataset());
            comment.setTree(tree);
        }

        return comment;
    }

    @Override
    public Banner getBanner(boolean priv, Tree tree, Profile profile, Structure structure) throws DatabaseException {
        Banner banner;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query;
            if (priv) {
                query = hibernateSession.createQuery("FROM Banner ban WHERE ban.private=:priv "
                        + "AND ban.tree=:tree AND ban.structure=:struc AND ban.createdBy=:profile");
                query.setParameter("profile", profile);
            } else {
                query = hibernateSession.createQuery("FROM Banner ban WHERE ban.private=:priv "
                        + "AND ban.tree=:tree AND ban.structure=:struc");
            }
            query.setParameter("priv", priv);
            query.setParameter("tree", tree);
            query.setParameter("struc", structure);
            banner = (Banner) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Could not fetch unique Banner from Database.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Could not fetch unique Banner from Database", ex);
        }

        if (banner != null) {
            if (priv) {
                banner.setCreatedBy(profile);
            } else {
                banner.setCreatedBy(null);
            }
            banner.setStructure(structure);
            banner.setTree(tree);
        }

        return banner;
    }

    @Override
    public Banner getBanner(boolean priv, edu.udo.scaffoldhunter.model.db.Session session, Profile profile,
            Structure structure) throws DatabaseException {
        return getBanner(priv, session.getTree(), profile, structure);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Banner> getAllBanners(Subset subset, Scaffold root) throws DatabaseException {
        Preconditions.checkNotNull(subset);
        Preconditions.checkNotNull(root);

        logger.debug("Root scaffold id is {}", root.id);

        Tree tree = root.getTree();
        Profile currentProfile = subset.getSession().getProfile();
        /*
         * Sctructure.id -> Structure
         */
        Map<Integer, Structure> structures = new HashMap<Integer, Structure>();
        Queue<Scaffold> queue = new LinkedList<Scaffold>(Collections.singleton(root));
        List<Object[]> queryResults = null;
        List<Banner> retVal = new LinkedList<Banner>();
        Banner banner;
        Structure structure;
        Session hibernateSession = null;

        // add all Scaffolds to structures
        Scaffold scaffold;
        while ((scaffold = queue.poll()) != null) {
            queue.addAll(scaffold.getChildren());
            structures.put(scaffold.getId(), scaffold);
            logger.debug("Adding scaffold id {} to the list of all structures", scaffold.id);
        }

        // add all Molecules to structures
        for (Molecule molecule : subset.getMolecules()) {
            structures.put(molecule.getId(), molecule);
            logger.debug("Adding molecule id {} to the list of all structures", molecule.id);
        }

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query;

            query = hibernateSession.createQuery("SELECT ban, submol.id "
                    + "FROM Banner ban, Subset s JOIN s.molecules submol "
                    + "WHERE s = :subset AND ban.structure = submol "
                    + "AND (ban.private = false OR ban.createdBy = :profile)");

            query.setParameter("subset", subset);
            query.setParameter("profile", subset.getSession().getProfile());
            queryResults = query.list();
            query = hibernateSession.createQuery("SELECT ban, scaf.id " + "FROM Banner ban, Scaffold scaf "
                    + "WHERE ban.structure = scaf AND scaf.tree = :tree "
                    + "AND (ban.private = false OR ban.createdBy = :profile)");
            query.setParameter("tree", tree);
            query.setParameter("profile", subset.getSession().getProfile());
            queryResults.addAll(query.list());
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Could not fetch Banners from Database.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Could not fetch Banners from Database", ex);
        }

        for (Object[] object : queryResults) {
            banner = (Banner) object[0];
            structure = structures.get(object[1]);
            if (structure != null) {
                if (banner.isPrivate()) {
                    banner.setCreatedBy(currentProfile);
                } else {
                    banner.setCreatedBy(null);
                }

                banner.setStructure(structure);
                banner.setTree(tree);

                retVal.add(banner);
            } else {
                logger.debug("filtering out banner for structure id {}", object[1]);
            }
        }

        return retVal;
    }

    @Override
    public Molecule getMolecule(Dataset dataset, String smiles) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("from Molecule where dataset = :dataset and smiles = :smiles");
            query.setParameter("dataset", dataset);
            query.setParameter("smiles", smiles);
            Molecule mol = (Molecule) query.uniqueResult();
            hibernateSession.getTransaction().commit();
            return mol;
        } catch (HibernateException ex) {
            logger.error("Could not fetch unique Molecule from Database.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Could not fetch unique Molecule from Database", ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Ruleset> getAllRulesets() throws DatabaseException {
        List<Ruleset> sets;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            sets = hibernateSession.createQuery("from Ruleset order by Title").list();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Query from Ruleset failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Query from Ruleset failed", ex);
        }

        return sets;
    }

    @Override
    public String getCreationUserName(Dataset dataset) throws DatabaseException {
        String userName;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession
                    .createQuery("SELECT data.createdBy.username FROM Dataset data WHERE data = :dataset");
            query.setParameter("dataset", dataset);
            userName = (String) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Fetching username of Dataset creator failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Fetching username of Dataset creator failed", ex);
        }

        if (userName == null) {
            throw new DatabaseException("Fetching username of Dataset creator failed");
        }

        return userName;
    }

    @Override
    public String getCreationUserName(Tree tree) throws DatabaseException {
        String userName;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession.createQuery("SELECT t.createdBy.username FROM Tree t WHERE t = :tree");
            query.setParameter("tree", tree);
            userName = (String) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Fetching username of Tree creator failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Fetching username of Tree creator failed", ex);
        }

        if (userName == null) {
            throw new DatabaseException("Fetching username of Tree creator failed");
        }

        return userName;
    }

    @Override
    public String getCreationUserName(Comment comment) throws DatabaseException {
        String userName;
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query query = hibernateSession
                    .createQuery("SELECT c.modifiedBy.username FROM Comment c WHERE c = :comment");
            query.setParameter("comment", comment);
            userName = (String) query.uniqueResult();
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("Fetching username of Comment creator failed.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("Fetching username of Comment creator failed", ex);
        }

        if (userName == null) {
            throw new DatabaseException("Fetching username of Comment creator failed");
        }

        return userName;
    }

    @Override
    public void mergeMoleculesIntoDBbySMILES(MergeIterator mergeIterator) throws DatabaseException {
        Session hibernateSession = null;

        try {
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();
            Query smilesQuery = hibernateSession
                    .createQuery("SELECT mol.smiles from Molecule mol WHERE mol.dataset = :dataset");
            smilesQuery.setParameter("dataset", mergeIterator.getDataset());
            @SuppressWarnings("unchecked")
            Set<String> smileStrings = Sets.newHashSet(smilesQuery.list());

            Collection<PropertyDefinition> mergedPropDefs = mergeIterator.getMergedProperties();
            Query molQuery = hibernateSession
                    .createQuery("from Molecule where dataset = :dataset and smiles = :smiles");
            molQuery.setParameter("dataset", mergeIterator.getDataset());
            Query numPropertyQuery = hibernateSession
                    .createQuery("from MoleculeNumProperty where molecule=:mol and type=:propdef");
            Query stringPropertyQuery = hibernateSession
                    .createQuery("from MoleculeStringProperty where molecule=:mol and type=:propdef");
            List<Property> newProps = Lists.newArrayList();
            List<Property> updatedProps = Lists.newArrayList();
            while (mergeIterator.hasNext()) {
                String currentSmiles = mergeIterator.next();
                if (Thread.currentThread().isInterrupted()) {
                    // import has been canceled
                    hibernateSession.getTransaction().rollback();
                    return;
                }
                if (smileStrings.contains(currentSmiles)) {
                    // molecule already in DB: merge
                    molQuery.setParameter("smiles", currentSmiles);
                    Molecule currentMol = (Molecule) molQuery.uniqueResult();
                    for (PropertyDefinition propDef : mergedPropDefs) {
                        Query propertyQuery;
                        if (propDef.isStringProperty())
                            propertyQuery = stringPropertyQuery;
                        else
                            propertyQuery = numPropertyQuery;
                        propertyQuery.setParameter("mol", currentMol);
                        propertyQuery.setParameter("propdef", propDef);
                        if (propDef.isStringProperty()) {
                            MoleculeStringProperty stringProp = (MoleculeStringProperty) propertyQuery.uniqueResult();
                            currentMol.getStringProperties().put(propDef.getId(), stringProp);
                        } else {
                            MoleculeNumProperty numProp = (MoleculeNumProperty) propertyQuery.uniqueResult();
                            currentMol.getNumProperties().put(propDef.getId(), numProp);
                        }
                    }
                    newProps.clear();
                    updatedProps.clear();
                    mergeIterator.mergeInto(currentMol, newProps, updatedProps);
                    hibernateSession.update(currentMol);
                    for (DbObject o : updatedProps)
                        hibernateSession.update(o);
                    for (DbObject o : newProps)
                        hibernateSession.save(o);
                } else {
                    // Molecule not in DB create a new one
                    newProps.clear();
                    Molecule newMol = mergeIterator.newMolecule(newProps);
                    if (newMol != null) {
                        hibernateSession.save(newMol);
                        for (DbObject o : newProps) {
                            hibernateSession.save(o);
                        }
                    }
                }
                hibernateSession.flush();
                hibernateSession.clear();
            }
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("An error occured during merging.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("An error occured during merging.", ex);
        }
    }

    @Override
    public void mergeMoleculesIntoDBbyProperty(MergeIterator iterator, PropertyDefinition propertyDefinition,
            Dataset dataset) throws DatabaseException {
        Preconditions.checkArgument(!propertyDefinition.isScaffoldProperty());

        Session hibernateSession = null;

        try {
            // TODO check somewhere if the property is really appropriate for
            // merging (no duplicates)
            hibernateSession = sessionFactory.getCurrentSession();
            hibernateSession.beginTransaction();

            /*
             * PropertyValue -> Molecule
             */
            Map<?, Molecule> molecules;

            Collection<PropertyDefinition> mergedPropDefs = iterator.getMergedProperties();

            /*
             * create this queries once instead of for each molecule one time
             */
            Query numPropertyQuery = hibernateSession
                    .createQuery("from MoleculeNumProperty where molecule=:mol and type=:propdef");
            Query stringPropertyQuery = hibernateSession
                    .createQuery("from MoleculeStringProperty where molecule=:mol and type=:propdef");

            /*
             * Fill the map "molecules" with all merge ids and the concerning
             * Molecule in the database
             */
            if (propertyDefinition.isStringProperty()) {
                Query propQuery = hibernateSession
                        .createQuery("select prop.value, prop.molecule from MoleculeStringProperty prop where prop.type = :propDef");
                propQuery.setParameter("propDef", propertyDefinition);
                Map<String, Molecule> molMap = Maps.newHashMap();
                @SuppressWarnings("unchecked")
                List<Object[]> tuples = propQuery.list();
                for (Object[] t : tuples) {
                    molMap.put((String) t[0], (Molecule) t[1]);
                }
                molecules = molMap;
            } else { // numProperty
                Query propQuery = hibernateSession
                        .createQuery("select prop.value, prop.molecule from MoleculeNumProperty prop where prop.type = :propDef");
                propQuery.setParameter("propDef", propertyDefinition);
                Map<Double, Molecule> molMap = Maps.newHashMap();
                @SuppressWarnings("unchecked")
                List<Object[]> tuples = propQuery.list();
                for (Object[] t : tuples) {
                    molMap.put((Double) t[0], (Molecule) t[1]);
                }
                molecules = molMap;
            }

            /*
             * For each Molecule that should be merged into the DB
             */
            List<Property> newProperties = Lists.newArrayList();
            List<Property> updatedProperties = Lists.newArrayList();
            while (iterator.hasNext()) {
                // retrieve merge-id value
                Object currentID = iterator.nextID();
                // get the associated molecule with this id in the DB
                Molecule currentMol = molecules.get(currentID);
                // if there does not exist a molecule with this id in the DB
                if (currentMol == null) {
                    continue;
                }

                /*
                 * Fill the current Molecule with already existing (in DB)
                 * propertyValues
                 */
                for (PropertyDefinition propDef : mergedPropDefs) {
                    Query propertyQuery;
                    if (propDef.isStringProperty()) {
                        propertyQuery = stringPropertyQuery;
                    } else {
                        propertyQuery = numPropertyQuery;
                    }
                    propertyQuery.setParameter("mol", currentMol);
                    propertyQuery.setParameter("propdef", propDef);
                    if (propDef.isStringProperty()) {
                        MoleculeStringProperty stringProp = (MoleculeStringProperty) propertyQuery.uniqueResult();
                        currentMol.getStringProperties().put(propDef.getId(), stringProp);
                    } else {
                        MoleculeNumProperty numProp = (MoleculeNumProperty) propertyQuery.uniqueResult();
                        currentMol.getNumProperties().put(propDef.getId(), numProp);
                    }
                }

                /*
                 * Merge the existing Properties with the new ones and save them
                 * in the DB
                 */
                newProperties.clear();
                updatedProperties.clear();
                iterator.mergeInto(currentMol, newProperties, updatedProperties);
                hibernateSession.update(currentMol);
                for (Property p : updatedProperties)
                    hibernateSession.update(p);
                for (Property p : newProperties)
                    hibernateSession.save(p);

                /*
                 * Guaranty the persistence of the current Molecule and clean up
                 * Session
                 */
                hibernateSession.flush();
                hibernateSession.clear();
            }
            hibernateSession.getTransaction().commit();
        } catch (HibernateException ex) {
            logger.error("An error occured during merging.\n{}\n{}", ex, stacktrace(ex));
            closeAndRollBackErroneousSession(hibernateSession);
            throw new DatabaseException("An error occured during merging.", ex);
        }
    }

    /**
     * Gives a {@link String} of a stacktrace of an {@link Exception}
     * 
     * @param ex
     *            the Exception
     * @return the stacktrace
     */
    private static String stacktrace(Exception ex) {
        StringBuilder stacktrace = new StringBuilder("Stacktrace:\n");
        for (StackTraceElement elem : ex.getStackTrace()) {
            stacktrace.append(elem.toString() + "\n");
        }
        return stacktrace.toString();
    }

    /**
     * Returns a native SQL Connection to the database
     * 
     * @return the {@link Connection}
     * @throws DatabaseException
     */
    private Connection getNativeDbConnection() throws DatabaseException {
        Connection retVal = null;
        try {
            Class.forName(connectionDriverClass);
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("Could not find database driver", e);
        }
        try {
            retVal = DriverManager.getConnection(connectionUrl, connectionUsername, connectionPassword);
        } catch (SQLException e) {
            throw new DatabaseException("Error connecting to database", e);
        }
        return retVal;
    }

    /**
     * Adds the Molecules of the given Scaffold and all Scaffolds in the Subtree
     * (with root of the given Scaffold) to the the molecules list
     * 
     * @param scaffold
     *            the root Scaffold of the Subtree
     * @param molecules
     *            list where the molecules are added
     */
    private static void addChildMolecules(Scaffold scaffold, Set<Molecule> molecules) {
        molecules.addAll(scaffold.getMolecules());
        for (Scaffold child : scaffold.getChildren())
            addChildMolecules(child, molecules);
    }

    /**
     * Adds all scaffolds parents to the set of parents, if they are not already
     * in the set of scaffolds and adds the scaffolds to the children set of
     * each scaffold
     * 
     * @param scaffold
     *            The scaffold which parents should be added
     * @param parents
     *            The set of parent scaffolds. This set is modified.
     * @param scaffolds
     *            The set of scaffolds. This set is not modified.
     */
    private static void addParents(Scaffold scaffold, Set<Scaffold> parents, Set<Scaffold> scaffolds) {
        Scaffold parent = scaffold.getParent();
        if (parent != null && !parent.getChildren().contains(scaffold))
            parent.getChildren().add(scaffold);
        if (parent != null && !scaffolds.contains(parent) && !parents.contains(parent)) {
            parents.add(parent);
            addParents(parent, parents, scaffolds);
        }
    }

    /*
     * Creates the query Part for a Filterset
     * 
     * Precondition: m must be the Molecule
     */
    private void getFiltersetQueryPart(Dataset dataset, Filterset filterset,
            Map<String, PropertyDefinition> propDefParameterNames, StringBuilder queryString) {
        queryString.append(" AND (");
        int i = 0;
        boolean first = true;
        // Each filter is encapsulated into a sub query
        for (Filter filter : filterset.getFilters()) {
            // logic interconnection
            if (!first) {
                if (filterset.isConjunctive()) {
                    queryString.append(" AND");
                } else {
                    queryString.append(" OR");
                }
            } else {
                first = false;
            }
            String parameterName = "propDefParameterName" + i++;
            propDefParameterNames.put(parameterName, filter.getPropDef(dataset));

            queryString.append(getFilterQueryPart(dataset, filter, parameterName));
        }
        queryString.append(")");
        logger.debug("Filter Query String: {}", queryString.toString());
    }

    /*
     * Creates a query part for a single Filter
     * 
     * e.g. NumFilter->IsDefined or NumFilter->GreaterOrEqual(3)
     */
    private String getFilterQueryPart(Dataset dataset, Filter filter, String propDefParameterName) {
        StringBuilder queryPart = new StringBuilder("");
        final String moleculeIn = " m IN ";
        final String moleculeNotIn = " m NOT IN ";
        String subQueryBody;

        /*
         * NumFilter
         */
        if (filter instanceof NumFilter) {
            if (filter.getPropDef(dataset).isScaffoldProperty()) {
                /*
                 * Scaffold NumProperty Filter
                 */
                subQueryBody = "(SELECT mol FROM Scaffold s INNER JOIN s.generationMolecules AS mol, ScaffoldNumProperty p "
                        + "WHERE p.scaffold = s AND p.type = :" + propDefParameterName;
            } else {
                /*
                 * Molecule NumProperty Filter
                 */
                subQueryBody = "(SELECT mol FROM Molecule mol, MoleculeNumProperty p "
                        + "WHERE p.molecule = mol AND p.type = :" + propDefParameterName;
            }

            switch (((NumFilter) filter).getComparisonFunction()) {
            case IsDefined:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(")");
                break;
            case IsNotDefined:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(")");
                break;
            case IsEqual:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value = ");
                queryPart.append(((NumFilter) filter).getValue());
                queryPart.append(")");
                break;
            case IsNotEqual:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value = ");
                queryPart.append(((NumFilter) filter).getValue());
                queryPart.append(")");
                break;
            case IsGreater:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value > ");
                queryPart.append(((NumFilter) filter).getValue());
                queryPart.append(")");
                break;
            case IsGreaterOrEqual:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value >= ");
                queryPart.append(((NumFilter) filter).getValue());
                queryPart.append(")");
                break;
            case IsLess:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value < ");
                queryPart.append(((NumFilter) filter).getValue());
                queryPart.append(")");
                break;
            case IsLessOrEqual:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value <= ");
                queryPart.append(((NumFilter) filter).getValue());
                queryPart.append(")");
                break;
            default:
                throw new IllegalArgumentException("This NumComparisonFunction is currently not supportet");
            }
            /*
             * StringFilter
             */
        } else {
            if (filter.getPropDef(dataset).isScaffoldProperty()) {
                /*
                 * Scaffold StringProperty Filter
                 */
                subQueryBody = "(SELECT mol FROM Scaffold s INNER JOIN s.generationMolecules AS mol, ScaffoldStringProperty p "
                        + "WHERE p.scaffold = s AND p.type = :" + propDefParameterName;
            } else {
                /*
                 * Molecule StringProperty Filter
                 */
                subQueryBody = "(SELECT mol FROM Molecule mol, MoleculeStringProperty p "
                        + "WHERE p.molecule = mol AND p.type = :" + propDefParameterName;
            }

            switch (((StringFilter) filter).getComparisonFunction()) {
            case IsDefined:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(")");
                break;
            case IsNotDefined:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(")");
                break;
            case IsEqual:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value like '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("')");
                break;
            case IsNotEqual:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value like '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("')");
                break;
            case Begins:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value like '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("%')");
                break;
            case BeginsInverse:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("' like concat(p.value, '%'))");
                break;
            case BeginsNot:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value like '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("%')");
                break;
            case BeginsNotInverse:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("' like concat(p.value, '%'))");
                break;
            case Ends:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value like '%");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("')");
                break;
            case EndsInverse:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("' like concat('%', p.value))");
                break;
            case EndsNot:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value like '%");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("')");
                break;
            case EndsNotInverse:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("' like concat('%', p.value))");
                break;
            case Contains:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value like '%");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("%')");
                break;
            case ContainsInverse:
                queryPart.append(moleculeIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("' like concat('%', concat(p.value, '%')))");
                break;
            case ContainsNot:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND p.value like '%");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("%')");
                break;
            case ContainsNotInverse:
                queryPart.append(moleculeNotIn);
                queryPart.append(subQueryBody);
                queryPart.append(" AND '");
                queryPart.append(((StringFilter) filter).getValue());
                queryPart.append("' like concat('%', concat(p.value, '%')))");
                break;
            default:
                throw new IllegalArgumentException("This StringComparisonFunction is currently not supportet");
            }
        }
        return queryPart.toString();
    }

    private class IntegerDoublePair {
        public Double d;
        public Integer i;

        public IntegerDoublePair(Integer i, Double d) {
            this.i = i;
            this.d = d;
        }
    }

}