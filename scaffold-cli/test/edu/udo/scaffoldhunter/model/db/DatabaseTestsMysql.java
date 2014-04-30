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

import static org.junit.Assert.fail;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Assert;

import org.hibernate.LazyInitializationException;
import org.junit.Test;

import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.MappingType;
import edu.udo.scaffoldhunter.model.NumComparisonFunction;
import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.StringComparisonFunction;
import edu.udo.scaffoldhunter.model.VisualFeature;
import edu.udo.scaffoldhunter.model.treegen.prioritization.ScaffoldPrioritization;
import edu.udo.scaffoldhunter.model.util.SHPredicates;
import edu.udo.scaffoldhunter.model.util.Scaffolds;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeView;

/**
 * Tests the database layer
 * 
 * local Mysql database needed:
 * 
 * - schema: sh_hibernate_junit
 * 
 * - user: hibernate
 * 
 * - pass: temp
 * 
 * @author Till Schäfer
 * 
 */
public class DatabaseTestsMysql {
    private static Random random = new Random();

    protected String dbDriver = "com.mysql.jdbc.Driver";
    protected String hibernateDialect = "org.hibernate.dialect.MySQL5InnoDBDialect";
    protected String url = "jdbc:mysql://localhost/";

    /**
     * If the database was already filled with content
     */
    protected static boolean dbInitialized = false;

    private static DbManager db;

    private Profile profile;
    private Session session;

    /**
     * Fills the Database with content
     * 
     * @throws DatabaseException
     * @throws XMLStreamException
     * @throws IOException
     */
    protected void fillDb() throws DatabaseException, XMLStreamException, IOException {
        // create Profile
        Profile profile;
        profile = new Profile();
        profile.setUsername("till2");
        profile.setPasswordEncrypted("password");
        db.saveAsNew(profile);

        // create Session (if not existing)
        Session session = new Session();
        session.setLastUsed(new Date());
        session.setProfile(profile);
        session.setTitle("session_title_1");
        db.saveAsNew(session);

        // update Profile with currentSession
        profile.setCurrentSession(session);
        db.saveOrUpdate(profile);

        // create Bookmark and BookmarkFolder
        BookmarkFolder folder = new BookmarkFolder();
        folder.setName("ROOT");
        folder.setParent(null);
        folder.setProfile(profile);
        db.saveAsNew(folder);
        profile.getBookmarkFolders().add(folder);
        Bookmark bookmark = new Bookmark("Lesezeichen", "CCC", true, folder);
        db.saveAsNew(bookmark);
        folder.getBookmarks().add(bookmark);
        // saving folder again is not necessary

        // create Link
        Link link = new Link();
        link.setTitle("neuer link");
        link.setUrl("http://www.google.de");
        db.saveAsNew(link);

        // create Dataset
        Dataset dataset;
        dataset = new Dataset();
        dataset.setTitle("Dataset 1");
        dataset.setComment("Ein Testdataset...");
        dataset.setFilterset(null);
        dataset.setDatabaseIdentifiers("DB1, DB2");
        dataset.setCreatedBy(profile);
        dataset.setCreationDate(new Date());
        db.saveAsNew(dataset);

        // create PropertyDefinitions
        Map<String, PropertyDefinition> propdeflist = dataset.getPropertyDefinitions();
        Boolean scaffoldProperty = false;
        for (int j = 0; j < 2; j++) {
            for (Integer i = 0; i < 10; i++) {
                PropertyDefinition propdef_string = new PropertyDefinition();
                propdef_string.setDescription("Dies ist eine tolle string Beschreibung in einem ganzen Satz.");
                propdef_string.setKey("tolle_beschreibung_string_" + scaffoldProperty.toString() + "_" + i.toString());
                propdef_string.setMappable(true);
                propdef_string.setPropertyType(PropertyType.StringProperty);
                propdef_string.setScaffoldProperty(scaffoldProperty);
                propdef_string.setTitle("tolle string beschreibung " + i.toString());
                propdef_string.setDataset(dataset);
                propdeflist.put(propdef_string.getKey(), propdef_string);
                db.saveAsNew(propdef_string);

                PropertyDefinition propdef_num = new PropertyDefinition();
                propdef_num.setDescription("Dies ist eine tolle numerische Beschreibung in einem ganzen Satz.");
                propdef_num.setKey("tolle_beschreibung_num_" + scaffoldProperty.toString() + "_" + i.toString());
                propdef_num.setMappable(true);
                propdef_num.setPropertyType(PropertyType.NumProperty);
                propdef_num.setScaffoldProperty(scaffoldProperty);
                propdef_num.setTitle("tolle num beschreibung " + i.toString());
                propdef_num.setDataset(dataset);
                propdeflist.put(propdef_num.getKey(), propdef_num);
                db.saveAsNew(propdef_num);
            }
            scaffoldProperty = true;
        }

        // create filterset and mapping (Presets)
        Filterset filterset = new Filterset();
        filterset.setProfile(profile);
        filterset.setTitle("Lowpass");
        profile.getPresets().add(filterset);
        db.saveAsNew(filterset);

        dataset.setFilterset(filterset);
        db.saveOrUpdate(dataset);
        MappingGradient gradient = new MappingGradient();
        gradient.setAscending(false);
        gradient.setColor1(Color.black);
        gradient.setColor2(Color.white);

        Mapping mapping = new Mapping();
        mapping.setCumulative(false);
        mapping.setMappingType(MappingType.Gradient);
        mapping.setProfile(profile);
        mapping.setGradient(gradient);
        mapping.setPropertyDefinition(propdeflist.values().iterator().next());
        mapping.setTitle("tolles verlauf");
        mapping.setVisualFeature(VisualFeature.Label);

        db.saveAsNew(gradient);
        db.saveAsNew(mapping);
        profile.getPresets().add(mapping);
        mapping = new Mapping();
        mapping.setCumulative(false);
        mapping.setMappingType(MappingType.Interval);
        mapping.setProfile(profile);
        mapping.setPropertyDefinition(propdeflist.values().iterator().next());
        mapping.setTitle("tolles verlauf");
        mapping.setVisualFeature(VisualFeature.NodeSize);

        MappingInterval interval1 = new MappingInterval();
        MappingInterval interval2 = new MappingInterval();
        interval1.setLowerBound(10.0f);
        interval1.setValue(20.0f);
        interval1.setMapping(mapping);
        interval2.setLowerBound(20.0f);
        interval2.setValue(21.0f);
        interval2.setMapping(mapping);

        db.saveAsNew(mapping);
        profile.getPresets().add(mapping);

        db.saveAsNew(interval1);
        db.saveAsNew(interval2);

        mapping.getOrderedIntervals().add(interval1);
        mapping.getOrderedIntervals().add(interval2);
        // end Mapping

        // create Tree
        Tree tree = new Tree();
        tree.setTitle("Tree 1");
        tree.setComment("Ein Testtree...");
        tree.setDataset(dataset);
        tree.setCreatedBy(profile);
        tree.setCreationDate(new Date());
        // TODO: Ruleset
        // tree.setRuleset(ruleset);
        db.saveAsNew(tree);
        dataset.getTrees().add(tree);
        session.setTree(tree);
        db.saveOrUpdate(session);

        // create Filter
        for (PropertyDefinition propDef : dataset.getPropertyDefinitions().values()) {
            if (propDef.isScaffoldProperty()) {
                continue;
            }
            if (propDef.isStringProperty()) {
                StringFilter filter = new StringFilter();
                filter.setAccumulationFunction(AccumulationFunction.Average);
                filter.setFilterset(filterset);
                filter.setPropDef(propDef);
                filter.setValue("hans");
                filter.setComparisonFunction(StringComparisonFunction.Contains);

                db.saveAsNew(filter);
            } else {
                NumFilter filter = new NumFilter();
                filter.setAccumulationFunction(AccumulationFunction.Average);
                filter.setFilterset(filterset);
                filter.setPropDef(propDef);
                filter.setValue(0.0);
                filter.setComparisonFunction(NumComparisonFunction.IsEqual);

                db.saveAsNew(filter);
            }
        }

        // create Subset
        Subset subset = db.getRootSubset(session);
        session.setSubset(subset);
        db.saveOrUpdate(subset);
        db.saveOrUpdate(session);

        // create Molecules
        Subset root = session.getSubset();
        Set<Molecule> molecules = root.getMolecules();
        // subset3 molecules = root molecules -> needed for
        // uniqueMoleculesInSubsetTest()
        Subset subset3 = new Subset(root, "Kleines Subset", null, session, null, null);
        root.addChild(subset3);
        ArrayList<MoleculeStringProperty> stringProps = new ArrayList<MoleculeStringProperty>();
        ArrayList<MoleculeNumProperty> numProps = new ArrayList<MoleculeNumProperty>();
        // adding unique molecule
        Molecule uniqueMolecule = new Molecule();
        uniqueMolecule.setDataset(dataset);
        uniqueMolecule.setTitle("Unique Molecule");
        uniqueMolecule.setSmiles("UMSMILES");
        uniqueMolecule.setSvgString("UMSVG");
        uniqueMolecule.setSvgHeight(200);
        uniqueMolecule.setSvgWidth(200);
        uniqueMolecule.setStrucMol("UMMOL");
        molecules.add(uniqueMolecule);

        Molecule m = new Molecule();
        // adding many molecules more
        for (int i = 1; i <= 100; i++) {
            m = new Molecule();
            m.setDataset(dataset);
            m.setTitle("Molecule" + i);
            m.setSmiles(";-)");
            m.setSvgString("SVG");
            m.setSvgHeight(200);
            m.setSvgWidth(200);
            molecules.add(m);
            subset3.getMolecules().add(m);
            // generate one property per Property definition
            for (PropertyDefinition propdefs : dataset.getPropertyDefinitions().values()) {
                if (propdefs.isScaffoldProperty())
                    continue;
                if (propdefs.isStringProperty()) {
                    MoleculeStringProperty prop = new MoleculeStringProperty();
                    prop.setType(propdefs);
                    prop.setValue("stringvalue");
                    prop.setMolecule(m);
                    m.getStringProperties().put(propdefs.getId(), prop);
                    stringProps.add(prop);
                } else {
                    MoleculeNumProperty prop = new MoleculeNumProperty();
                    prop.setType(propdefs);
                    prop.setValue(i);
                    prop.setMolecule(m);
                    m.getNumProperties().put(propdefs.getId(), prop);
                    numProps.add(prop);
                }
            }
        }
        db.saveAllAsNew(molecules);
        db.saveAllAsNew(stringProps);
        db.saveAllAsNew(numProps);
        // XXX: creating new HashSet is required if the
        // subset_molecule_realtionship should be updated
        root.setMolecules(new HashSet<Molecule>());
        root.getMolecules().addAll(molecules);
        db.saveOrUpdate(root);
        db.saveAsNew(subset3);

        // create ruleset
        Ruleset rules = ScaffoldPrioritization.readRulesFile("resources/rules.txt");
        rules.setTitle("custom test");
        db.saveAsNew(rules);

        // create Scaffolds (if not existing)
        Set<Molecule> moleculesCopy = new HashSet<Molecule>();
        moleculesCopy.addAll(molecules);
        Set<Scaffold> scaffolds = createScaffolds(tree, moleculesCopy);
        ArrayList<ScaffoldStringProperty> scaffStringProps = new ArrayList<ScaffoldStringProperty>();
        ArrayList<ScaffoldNumProperty> scaffNumProps = new ArrayList<ScaffoldNumProperty>();
        for (Scaffold scaffold : scaffolds) {
            // generate one property per Property definition
            for (PropertyDefinition propdefs : dataset.getPropertyDefinitions().values()) {
                if (!propdefs.isScaffoldProperty())
                    continue;
                if (propdefs.isStringProperty()) {
                    ScaffoldStringProperty prop = new ScaffoldStringProperty();
                    prop.setType(propdefs);
                    prop.setValue("stringvalue");
                    prop.setScaffold(scaffold);
                    scaffold.getStringProperties().put(propdefs.getId(), prop);
                    scaffStringProps.add(prop);
                } else {
                    ScaffoldNumProperty prop = new ScaffoldNumProperty();
                    prop.setType(propdefs);
                    prop.setValue(43);
                    prop.setScaffold(scaffold);
                    scaffold.getNumProperties().put(propdefs.getId(), prop);
                    scaffNumProps.add(prop);
                }
            }
        }
        db.saveAllAsNew(scaffStringProps);
        db.saveAllAsNew(scaffNumProps);

        // create Comments & Banner
        db.createOrUpdateComment("testkommentar molekül: privat und lokal", true, tree, profile, uniqueMolecule);
        db.createOrUpdateComment("testkommentar molekül: privat und global", true, null, profile, uniqueMolecule);
        db.createOrUpdateComment("testkommentar molekül: public und lokal", false, tree, profile, uniqueMolecule);
        db.createOrUpdateComment("testkommentar molekül: public und global", false, null, profile, uniqueMolecule);

        Scaffold scaffold = db.getScaffolds(root, false);
        db.createOrUpdateComment("testkommentar scaffold: privat und lokal", true, tree, profile, scaffold);
        db.createOrUpdateComment("testkommentar scaffold: privat und global", true, null, profile, scaffold);
        db.createOrUpdateComment("testkommentar scaffold: public und lokal", false, tree, profile, scaffold);
        db.createOrUpdateComment("testkommentar scaffold: public und global", false, null, profile, scaffold);

        db.createBanner(true, tree, profile, uniqueMolecule);
        db.createBanner(false, tree, profile, uniqueMolecule);
        db.createBanner(true, tree, profile, scaffold);
        db.createBanner(false, tree, profile, scaffold);
        // end comments & Banner
    }

    /**
     * Initializes used variables for the tests
     */
    @org.junit.Before
    public void initialize() {
        // init logging
        System.setProperty("java.util.logging.config.file", "logging.properties");

        if (!dbInitialized) {
            dbInitialized = true;

            // init Database and recreate schema
            try {
                db = new DbManagerHibernate(dbDriver, hibernateDialect, url, "sh_hibernate_junit", "hibernate", "temp",
                        true, true);
            } catch (DatabaseException e) {
                e.printStackTrace();
                fail("Initialisation of Database failed");
            }
            try {
                fillDb();
            } catch (Exception e) {
                AssertionError err = new AssertionError("Filling database with content failed");
                err.initCause(e);
                throw err;
            }
        }

        // Load first Profile and Session
        try {
            profile = db.getProfile(db.getAllProfileNames().get(0));
            db.loadCurrentSession(profile);
            session = profile.getCurrentSession();
        } catch (Exception e) {
            AssertionError err = new AssertionError("Initial loading of Profile or Session failed");
            err.initCause(e);
            throw err;
        }
    }

    /**
     * Checks if the Presets can be loaded successfully
     */
    @org.junit.Test
    public void loadingPresetsTest() {
        // Presets
        for (Preset preset : profile.getPresets()) {
            if (preset.getClass() == Mapping.class) {
                if (((Mapping) preset).getMappingType() == MappingType.Gradient) {
                    MappingGradient gradient = ((Mapping) preset).getGradient();
                    // Test if gradient could be fetched
                    if (gradient == null)
                        fail("gradient should not be null");
                } else if (((Mapping) preset).getMappingType() == MappingType.Interval) {
                    List<MappingInterval> interval = ((Mapping) preset).getOrderedIntervals();
                    // Test if intervals could be fetched
                    if (interval == null)
                        fail("interval should not be null");
                    if (interval.size() == 0)
                        fail("interval.size() should not be 0");
                }
            }
        }
    }

    /**
     * Tests if the Molecules in different Subsets are the same Objects at
     * runtime
     */
    @org.junit.Test
    public void uniqueMoleculesInSubsetTest() {
        Subset root = session.getSubset();
        Subset child = null;
        try {
            child = root.getChildren().iterator().next();
        } catch (NoSuchElementException e) {
            fail("child subset not found");
        }

        if (root.getMolecules().size() == 0 || child.getMolecules().size() == 0) {
            fail("root or child subset have zero Molecules");
        }

        Set<Molecule> others = child.getMolecules();
        for (Molecule molecule : root.getMolecules()) {
            for (Molecule compare : others) {
                if (compare.equalsDb(molecule)) {
                    if (molecule != compare) {
                        fail("same Database Entries are different Objects");
                    }
                    break;
                }
            }
        }
    }

    /**
     * Test lazy loading and locking mechanism for Structure Properties
     */
    @org.junit.Test
    public void lockingTest() {
        Subset root = session.getSubset();
        Molecule molecule = root.getMolecules().iterator().next();
        for (PropertyDefinition propDef : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef.isScaffoldProperty())
                continue;
            boolean unloaded = false;
            // load and unload two times to test the lock counting
            try {
                db.lockAndLoad(propDef, molecule);
                db.lockAndLoad(propDef, molecule);
            } catch (DatabaseException e) {
                AssertionError err = new AssertionError("lockAndLoad failed");
                err.initCause(e);
                throw err;
            }

            if (propDef.isStringProperty()) {
                StringProperty prop = molecule.getStringProperties().get(propDef.getId());
                if (prop == null) {
                    fail("StringProperty not loaded");
                }
                String value = prop.getValue();
                if (value == null) {
                    fail("value is null");
                }
            } else {
                NumProperty prop = molecule.getNumProperties().get(propDef.getId());
                if (prop == null) {
                    fail("NumProperty not loaded");
                }
                Double value = prop.getValue();
                if (value == null) {
                    fail("value is null");
                }
            }
            db.unlockAndUnload(propDef, molecule);
            db.unlockAndUnload(propDef, molecule);
            try {
                if (propDef.isStringProperty()) {
                    @SuppressWarnings("unused")
                    String str = molecule.getStringProperties().get(propDef.getId()).getValue();
                } else {
                    @SuppressWarnings("unused")
                    Double num = molecule.getNumProperties().get(propDef.getId()).getValue();
                }
            } catch (NullPointerException ex) {
                unloaded = true;
            }
            if (!unloaded) {
                fail("Property " + propDef.getId() + " not successfull unloaded");
            }
        }
    }

    /**
     * Try to load the unique molecule
     */
    @org.junit.Test
    public void uniqueMoleculeLoadTest() {
        Molecule um;
        try {
            um = db.getMolecule(session.getDataset(), "UMSMILES");
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("loading unique molecule failed");
            err.initCause(e);
            throw err;
        }
        if (um == null)
            fail("Unique molecule could not be found");
        if (!um.getTitle().equals("Unique Molecule"))
            fail("Unique molecule has the wrong title: " + um.getTitle());
    }

    /**
     * Test the ordering of the Rules in a ruleset
     */
    @org.junit.Test
    public void rulesetOrderingTest() {
        List<Ruleset> rulesets;
        try {
            rulesets = db.getAllRulesets();
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting all Rulesets failed");
            err.initCause(e);
            throw err;
        }
        for (Ruleset ruleset : rulesets) {
            List<Rule> rules = ruleset.getOrderedRules();
            Rule pre = null;
            for (Rule r : rules) {
                if (pre != null)
                    if (r.getOrder() < pre.getOrder())
                        fail("Rules have wrong order");
                pre = r;
            }
        }
    }

    /**
     * Tests if the Scaffold children are set correctly (a parent of b -> b in
     * children list of a)
     */
    @org.junit.Test
    public void scaffoldRelationshipTest() {
        Subset root = session.getSubset();
        Scaffold rootScaffold;
        try {
            rootScaffold = db.getScaffolds(root, false);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting Scaffolds failed");
            err.initCause(e);
            throw err;
        }
        Set<Scaffold> scaffolds = Sets.newHashSet();
        Iterables.addAll(scaffolds, Scaffolds.getSubtreePreorderIterable(rootScaffold));
        for (Scaffold scaffold : scaffolds)
            checkParents(scaffold, scaffolds);
    }

    /**
     * Tests if a {@link Banner} can be loaded
     */
    @org.junit.Test
    public void bannerLoadingTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Molecule um;
        try {
            um = db.getMolecule(session.getDataset(), "UMSMILES");
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting unique Molecule failed");
            err.initCause(e);
            throw err;
        }
        if (um == null) {
            fail("Unique molecule could not be found");
        }

        Banner banner;
        // private Banner
        try {
            banner = db.getBanner(true, session, profile, um);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting Banner failed");
            err.initCause(e);
            throw err;
        }
        if (banner == null) {
            fail("Banner could not be found");
        }
        if (!banner.getCreatedBy().equalsDb(profile)) {
            fail("Fetched Banner with wrong Profile");
        }
        if (!banner.getMolecule().equalsDb(um)) {
            fail("Fetched Banner with wrong Molecule");
        }
        if (!banner.getTree().equalsDb(session.getTree())) {
            fail("Fetched Banner with wrong Tree");
        }
        if (banner.isMolecule() != true) {
            fail("Fetched Banner for a Scaffold which should be for a Molecule");
        }
        if (!banner.isPrivate()) {
            fail("Fetched public Banner where it should be private");
        }

        // public banner
        try {
            banner = db.getBanner(false, session, profile, um);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting Banner failed");
            err.initCause(e);
            throw err;
        }
        if (banner == null) {
            fail("Banner could not be found");
        }
        if (!banner.getMolecule().equalsDb(um)) {
            fail("Fetched Banner with wrong Molecule");
        }
        if (!banner.getTree().equalsDb(session.getTree())) {
            fail("Fetched Banner with wrong Tree");
        }
        if (banner.isMolecule() != true) {
            fail("Fetched Banner for a Scaffold which should be for a Molecule");
        }
        if (banner.isPrivate()) {
            fail("Fetched public Banner where it should be private");
        }
    }

    /**
     * Tests if the getAllBanner loading works
     */
    @org.junit.Test
    public void allBannerLoadingTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        List<Banner> banners;

        try {
            Scaffold root = db.getScaffolds(session.getSubset(), false);
            banners = db.getAllBanners(session.getSubset(), root);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting Banners failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(4, banners.size());
    }

    /**
     * Tests if the SvgStrings are loaded correctly
     */
    @org.junit.Test
    public void SvgLoadingTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Molecule um;
        try {
            um = db.getMolecule(session.getDataset(), "UMSMILES");
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting unique Molecule failed");
            err.initCause(e);
            throw err;
        }
        if (um == null) {
            fail("Unique molecule could not be found");
        }

        String svgString;
        try {
            svgString = db.getSvgString(um);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting svgString failed");
            err.initCause(e);
            throw err;
        }

        if (svgString == null) {
            fail("svgString could not be found");
        }
        if (!svgString.equals("UMSVG")) {
            fail("wrong svgString loaded");
        }
    }

    /**
     * Tests if the SvgStrings are lazy loaded
     */
    @org.junit.Test(expected = LazyInitializationException.class)
    public void SvgLazyTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Molecule um;
        try {
            um = db.getMolecule(session.getDataset(), "UMSMILES");
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting unique Molecule failed");
            err.initCause(e);
            throw err;
        }
        if (um == null) {
            fail("Unique molecule could not be found");
        }

        um.getSvgString();
    }

    /**
     * Tests if the MolStrings are loaded correctly
     */
    @org.junit.Test
    public void MolLoadingTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Molecule um;
        try {
            um = db.getMolecule(session.getDataset(), "UMSMILES");
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting unique Molecule failed");
            err.initCause(e);
            throw err;
        }
        if (um == null) {
            fail("Unique molecule could not be found");
        }

        String molString;
        try {
            molString = db.getStrucMol(um);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting molString failed");
            err.initCause(e);
            throw err;
        }

        if (molString == null) {
            fail("molString could not be found");
        }
        if (!molString.equals("UMMOL")) {
            fail("wrong molString loaded");
        }
    }

    /**
     * Tests if the MolStrings are lazy loaded
     */
    @org.junit.Test(expected = LazyInitializationException.class)
    public void MolLazyTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Molecule um;
        try {
            um = db.getMolecule(session.getDataset(), "UMSMILES");
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getting unique Molecule failed");
            err.initCause(e);
            throw err;
        }
        if (um == null) {
            fail("Unique molecule could not be found");
        }

        String temp = um.getStrucMol();

        System.out.println(temp);
    }

    /**
     * Tests the IsDefined {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeNumFilterIsDefinedTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        NumFilter filter = new NumFilter();
        filter.setAccumulationFunction(null);
        filter.setComparisonFunction(NumComparisonFunction.IsDefined);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter.setPropDef(propDef);
        filter.setFilterset(filterset);
        filterset.getFilters().add(filter);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the IsNotDefined {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeNumFilterIsNotDefinedTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        NumFilter filter = new NumFilter();
        filter.setAccumulationFunction(null);
        filter.setComparisonFunction(NumComparisonFunction.IsNotDefined);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter.setPropDef(propDef);
        filter.setFilterset(filterset);
        filterset.getFilters().add(filter);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());
    }

    /**
     * Tests the IsEqual {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeNumFilterIsEqualTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        NumFilter filter = new NumFilter();
        filter.setAccumulationFunction(null);
        filter.setComparisonFunction(NumComparisonFunction.IsEqual);
        filter.setValue(50);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter.setPropDef(propDef);
        filter.setFilterset(filterset);
        filterset.getFilters().add(filter);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Molecule mol = filteredSubset.getMolecules().iterator().next();
        try {
            db.lockAndLoad(propDef, mol);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Lock and Load failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(50.0, mol.getNumPropertyValue(propDef));
        db.unlockAndUnload(propDef, mol);
    }

    /**
     * Tests the IsNotEqual {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeNumFilterIsNotEqualTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        NumFilter filter = new NumFilter();
        filter.setAccumulationFunction(null);
        filter.setComparisonFunction(NumComparisonFunction.IsNotEqual);
        filter.setValue(50);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter.setPropDef(propDef);
        filter.setFilterset(filterset);
        filterset.getFilters().add(filter);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the IsLess {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeNumFilterIsLessTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        filterset.setConjunctive(true);
        NumFilter filter1 = new NumFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(NumComparisonFunction.IsLess);
        filter1.setValue(50);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(49, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the IsLessOrEqual {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeNumFilterIsLessOrEqualTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        filterset.setConjunctive(true);
        NumFilter filter1 = new NumFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(NumComparisonFunction.IsLessOrEqual);
        filter1.setValue(50);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(50, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the IsGreater {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeNumFilterIsGreaterTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        filterset.setConjunctive(true);
        NumFilter filter1 = new NumFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(NumComparisonFunction.IsGreater);
        filter1.setValue(50);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(50, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the IsGreaterOrEqual {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeNumFilterIsGreaterOrEqualTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        NumFilter filter1 = new NumFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(NumComparisonFunction.IsGreaterOrEqual);
        filter1.setValue(50);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(51, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the IsDefined {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterIsDefinedTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter = new StringFilter();
        filter.setAccumulationFunction(null);
        filter.setComparisonFunction(StringComparisonFunction.IsDefined);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter.setPropDef(propDef);
        filter.setFilterset(filterset);
        filterset.getFilters().add(filter);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the IsNotDefined {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterIsNotDefinedTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter = new StringFilter();
        filter.setAccumulationFunction(null);
        filter.setComparisonFunction(StringComparisonFunction.IsNotDefined);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter.setPropDef(propDef);
        filter.setFilterset(filterset);
        filterset.getFilters().add(filter);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());
    }

    /**
     * Tests the IsEqual {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterIsEqualTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.IsEqual);
        filter1.setValue("stringvalue");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());

        filter1.setValue("value");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(0, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the IsNotEquals {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterIsNotEqualTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.IsNotEqual);
        filter1.setValue("stringvalue");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());

        filter1.setValue("value");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(101, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the Begins {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterBeginsTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.Begins);
        filter1.setValue("stringvalue");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());

        // test a String that is part of the Property but does not begin with it
        filter1.setValue("value");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(0, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the BeginsInverse {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterBeginsInverseTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.BeginsInverse);
        filter1.setValue("stringvaluehannesamstiel");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());

        // test a String that is part of the Property but does not begin with it
        filter1.setValue("value");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(0, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the BeginsNot {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterBeginsNotTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.BeginsNot);
        filter1.setValue("stringval");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());

        filter1.setValue("value");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(101, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the BeginsNotInverse {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterBeginsNotInverseTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.BeginsNotInverse);
        filter1.setValue("stringvaluehannes");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());

        filter1.setValue("value");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(101, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the Ends {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterEndsTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.Ends);
        filter1.setValue("value");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());

        filter1.setValue("string");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(0, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the EndsInverse {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterEndsInverseTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.EndsInverse);
        filter1.setValue("jksdfjaksdlfjstringvalue");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());

        filter1.setValue("stringvaluesadkfsdkjf");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(0, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the EndsNot {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterEndsNotTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.EndsNot);
        filter1.setValue("value");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());

        filter1.setValue("string");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(101, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the EndsNotInverse {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterEndsNotInverseTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.EndsNotInverse);
        filter1.setValue("ahnsstringvalue");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());

        filter1.setValue("stringvalueksjdfksdjf");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(101, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the Contains {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterContainnsTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.Contains);
        filter1.setValue("ingval");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());

        filter1.setValue("abstringvaluexy");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(0, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the ContainsInverse {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterContainnsInverseTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.ContainsInverse);
        filter1.setValue("abxstringvaluexy");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());

        filter1.setValue("ringvaluexy");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(0, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the ContainsNot {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterContainnsNotTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.ContainsNot);
        filter1.setValue("ingval");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());

        filter1.setValue("hans");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(101, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the ContainsNotInverse {@link Filter} on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeStringFilterContainnsNotInverseTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        StringFilter filter1 = new StringFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(StringComparisonFunction.ContainsNotInverse);
        filter1.setValue("dsfkajsdfstringvalueaskdja");
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filterset.getFilters().add(filter1);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(1, filteredSubset.getMolecules().size());
        Assert.assertEquals("UMSMILES", filteredSubset.getMolecules().iterator().next().getSmiles());

        filter1.setValue("abstringabcvaluexy");
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(101, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the conjunction on {@link Filter}s on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeFilterConjunctionTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        filterset.setConjunctive(true);
        NumFilter filter1 = new NumFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(NumComparisonFunction.IsLess);
        filter1.setValue(60);
        NumFilter filter2 = new NumFilter();
        filter2.setAccumulationFunction(null);
        filter2.setComparisonFunction(NumComparisonFunction.IsGreaterOrEqual);
        filter2.setValue(50);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter2.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filter2.setFilterset(filterset);
        filterset.getFilters().add(filter1);
        filterset.getFilters().add(filter2);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(10, filteredSubset.getMolecules().size());
    }

    /**
     * Tests the disjunction on {@link Filter}s on {@link Molecule}s
     */
    @org.junit.Test
    public void MoleculeFilterDisjunctionTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        Subset root = session.getSubset();
        Filterset filterset = new Filterset();
        filterset.setProfile(session.getProfile());
        filterset.setTitle("test");
        filterset.setConjunctive(false);
        NumFilter filter1 = new NumFilter();
        filter1.setAccumulationFunction(null);
        filter1.setComparisonFunction(NumComparisonFunction.IsLess);
        filter1.setValue(60);
        NumFilter filter2 = new NumFilter();
        filter2.setAccumulationFunction(null);
        filter2.setComparisonFunction(NumComparisonFunction.IsGreaterOrEqual);
        filter2.setValue(50);
        PropertyDefinition propDef = null;
        for (PropertyDefinition propDef2 : session.getDataset().getPropertyDefinitions().values()) {
            if (!propDef2.isStringProperty() && !propDef2.isScaffoldProperty()) {
                propDef = propDef2;
                break;
            }
        }
        filter1.setPropDef(propDef);
        filter2.setPropDef(propDef);
        filter1.setFilterset(filterset);
        filter2.setFilterset(filterset);
        filterset.getFilters().add(filter1);
        filterset.getFilters().add(filter2);

        Subset filteredSubset;
        try {
            filteredSubset = db.getFilteredSubset(root, filterset);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Filtering failed");
            err.initCause(e);
            throw err;
        }
        Assert.assertEquals(100, filteredSubset.getMolecules().size());
    }

    /**
     * Test counting of distinct values for a MoleculeNumProperty
     */
    @Test
    public void distinctValueCountTestMoleculeNumProperty() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        PropertyDefinition propDef = Iterables.find(
                session.getDataset().getPropertyDefinitions().values(),
                Predicates.and(Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF),
                        Predicates.not(SHPredicates.IS_STRING_PROPDEF)));
        long count;
        try {
            count = db.getDistinctValueCount(propDef);
        } catch (DatabaseException e) {
            throw new AssertionError(e);
        }
        Assert.assertEquals(100, count);
    }

    /**
     * Test counting of distinct values for a MoleculeStringProperty
     */
    @Test
    public void distinctValueCountTestMoleculeStringProperty() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        PropertyDefinition propDef = Iterables.find(session.getDataset().getPropertyDefinitions().values(),
                Predicates.and(Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF), SHPredicates.IS_STRING_PROPDEF));
        long count;
        try {
            count = db.getDistinctValueCount(propDef);
        } catch (DatabaseException e) {
            throw new AssertionError(e);
        }
        Assert.assertEquals(1, count);
    }

    /**
     * Test retrieving distinct String Values
     */
    @Test
    public void getDistinctStringValuesTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        PropertyDefinition propDef = Iterables.find(session.getDataset().getPropertyDefinitions().values(),
                Predicates.and(Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF), SHPredicates.IS_STRING_PROPDEF));

        List<String> strings;
        try {
            strings = db.getDistinctStrings(propDef);
        } catch (DatabaseException e) {
            throw new AssertionError(e);
        }
        Assert.assertNotNull(strings);
        Assert.assertEquals(1, strings.size());
        Assert.assertEquals("stringvalue", strings.get(0));
    }

    /**
     * Test retrieval of accumulated minimum and maximum
     */
    @Test
    public void accumulatedMinMaxTest() {
        edu.udo.scaffoldhunter.model.db.Session session = this.session;
        PropertyDefinition propDef = Iterables.find(
                session.getDataset().getPropertyDefinitions().values(),
                Predicates.and(Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF),
                        Predicates.not(SHPredicates.IS_STRING_PROPDEF)));

        double[] minMaxMax;
        double[] minMaxMin;
        try {
            minMaxMax = db.getAccPropertyMinMax(session.getTree(), propDef, AccumulationFunction.Maximum, session.getSubset(), true, false, false);
            minMaxMin = db.getAccPropertyMinMax(session.getTree(), propDef, AccumulationFunction.Minimum, session.getSubset(), true, false, false);
        } catch (DatabaseException e) {
            throw new AssertionError(e);
        }
        Assert.assertEquals(100.0, minMaxMax[1]);
        Assert.assertEquals(1.0, minMaxMin[0]);
    }

    /**
     * Simple Test for schema existence (only checks the true case)
     */
    @Test
    public void schemaExistanceTest() {
        try {
            Assert.assertEquals(true, db.schemaExists());
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("Check for schema existance failed");
            err.initCause(e);
            throw err;
        }
    }

    /**
     * Tests if getAllSessionInformation() collects the right Info
     */
    @Test
    public void getAllSessionInformationTest() {
        List<SessionInformation> infos = null;
        SessionInformation info;

        try {
            infos = db.getAllSessionsInformation(profile);
            db.loadCurrentSession(profile);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getAllSessionTitles or loadCurrentSession failed");
            err.initCause(e);
            throw err;
        }

        Assert.assertEquals(1, infos.size());

        info = infos.iterator().next();

        Assert.assertEquals(profile.getCurrentSession().id, info.getSessionId());
        Assert.assertEquals("session_title_1", info.getTitle());
        Assert.assertEquals("Tree 1", info.getTreeName());
        Assert.assertEquals("Dataset 1", info.getDatasetName());
        Assert.assertEquals(101, info.getRootSubsetSize());
    }

    /*
     * Creates Scaffolds from Molecules (Helper Method for fillDb)
     */
    private Set<Scaffold> createScaffolds(Tree tree, Set<Molecule> molecules) throws XMLStreamException,
            DatabaseException {
        Map<Integer, Scaffold> scaffolds = new HashMap<Integer, Scaffold>();
        XMLStreamReader streamReader = null;
        XMLInputFactory readerFactory = XMLInputFactory.newFactory();
        InputStream resourceStream = ScaffoldTreeView.class.getClassLoader().getResourceAsStream("DemoTree.xml");
        streamReader = readerFactory.createXMLStreamReader(resourceStream);

        int rootid = 0;
        Scaffold newScaffold = null;
        if (streamReader != null) {
            while (streamReader.hasNext()) {
                int event = streamReader.next();
                if (event == XMLStreamReader.START_ELEMENT) {
                    if (streamReader.getName().getLocalPart().equals("cbsedatatree")) {
                        rootid = Integer.parseInt(streamReader.getAttributeValue(null, "rootid"));
                    } else if (streamReader.getName().getLocalPart().equals("node")) {
                        int id = Integer.parseInt(streamReader.getAttributeValue(null, "scaffoldid"));
                        newScaffold = scaffolds.get(id);
                        if (newScaffold == null) {
                            newScaffold = new Scaffold();
                            newScaffold.setTree(tree);
                            newScaffold.setSmiles(":-)");
                            scaffolds.put(id, newScaffold);
                        }
                        if (!streamReader.getAttributeValue(null, "parentid").isEmpty()) {
                            int parentid = Integer.parseInt(streamReader.getAttributeValue(null, "parentid"));
                            Scaffold parent = scaffolds.get(parentid);
                            if (parent == null) {
                                parent = new Scaffold();
                                parent.setTree(tree);
                                parent.setSmiles(":-)");
                                scaffolds.put(parentid, parent);
                            }
                            newScaffold.setParent(parent);
                        }
                    } else if (streamReader.getName().getLocalPart().equals("svg")) {
                        try {
                            String svgString = streamReader.getElementText();
                            newScaffold.setSvgString(svgString);
                            String width = svgString.substring(svgString.indexOf("width"), svgString.indexOf("height"));
                            width = width.substring(width.indexOf("\"") + 1);
                            width = width.substring(0, (width.indexOf("\"")));
                            width = width.trim();
                            String aidStr = svgString.substring(svgString.indexOf("height") + 7);
                            String height = aidStr.substring(aidStr.indexOf("\"") + 1);
                            height = height.substring(0, height.indexOf("\""));
                            height = height.trim();
                            newScaffold.setSvgWidth(Integer.parseInt(width));
                            newScaffold.setSvgHeight(Integer.parseInt(height));
                        } catch (Exception e) {
                            newScaffold.setSvgWidth(300);
                            newScaffold.setSvgHeight(300);
                        }

                        // XMLStreamReader reader =
                        // readerFactory.createXMLStreamReader(new
                        // StringReader(newScaffold.getSvgImage()));
                        // while(reader.hasNext()) {
                        // int e = reader.next();
                        // if (e == XMLStreamReader.START_ELEMENT) {
                        // if(reader.getLocalName().equals("svg"))
                        // break;
                        // String w = reader.getAttributeValue(null, "width");
                        // String h = reader.getAttributeValue(null, "height");
                        // if (w != null && h != null) {
                        // newScaffold.setSvgHeight(Integer.parseInt(h));
                        // newScaffold.setSvgWidth(Integer.parseInt(w));
                        // }
                        // break;
                        // }
                        // }
                    } else if (streamReader.getName().getLocalPart().equals("scaffoldid")) {
                        int childid = Integer.parseInt(streamReader.getElementText());
                        Scaffold child = scaffolds.get(childid);
                        if (child == null) {
                            child = new Scaffold();
                            child.setTree(tree);
                            child.setSmiles(":-)");
                            scaffolds.put(childid, child);
                        }
                        newScaffold.getChildren().add(child);
                    }
                } else if (event == XMLStreamReader.END_ELEMENT && streamReader.getName().getLocalPart().equals("node"))
                    newScaffold = null;
            } // end while (streamReader.hasNext())
        } // end if (streamReader != null)

        setHierarchyLevels(scaffolds.get(rootid), 0);
        addMolecules(scaffolds.get(rootid), molecules);

        db.saveAllAsNew(scaffolds.values());
        return new HashSet<Scaffold>(scaffolds.values());
    }

    private void addMolecules(Scaffold scaffold, Set<Molecule> molecules) {
        if (random.nextDouble() > 0.5 || scaffold.getChildren().isEmpty()) {
            int count = random.nextInt(5) + 1;
            Set<Molecule> scaffoldMols = new HashSet<Molecule>();
            for (int i = 0; i < count; i++) {
                if (!molecules.isEmpty()) {
                    Molecule m = molecules.iterator().next();
                    molecules.remove(m);
                    scaffoldMols.add(m);
                }
            }
            scaffold.setGenerationMolecules(scaffoldMols);
        }
        for (Scaffold c : scaffold.getChildren())
            addMolecules(c, molecules);
    }

    private void setHierarchyLevels(Scaffold s, int depth) {
        s.setHierarchyLevel(depth++);
        for (Scaffold c : s.getChildren())
            setHierarchyLevels(c, depth);
    }

    private void checkParents(Scaffold scaffold, Set<Scaffold> scaffolds) {
        if (!scaffolds.contains(scaffold))
            fail("Parent scaffold is not in set of scaffolds!");
        Scaffold parent = scaffold.getParent();
        if (parent != null)
            checkParents(parent, scaffolds);
    }
}
