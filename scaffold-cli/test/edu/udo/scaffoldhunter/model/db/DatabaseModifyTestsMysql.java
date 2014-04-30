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
import java.util.Random;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.Assert;
import edu.udo.scaffoldhunter.model.AccumulationFunction;
import edu.udo.scaffoldhunter.model.MappingType;
import edu.udo.scaffoldhunter.model.NumComparisonFunction;
import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.StringComparisonFunction;
import edu.udo.scaffoldhunter.model.VisualFeature;
import edu.udo.scaffoldhunter.model.treegen.prioritization.ScaffoldPrioritization;
import edu.udo.scaffoldhunter.view.scaffoldtree.ScaffoldTreeView;

/**
 * Tests the database layer for deletion operations
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
public class DatabaseModifyTestsMysql {
    private static Random random = new Random();

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
    @org.junit.Before
    public void fillDb() throws DatabaseException, XMLStreamException, IOException {
        // init logging
        System.setProperty("java.util.logging.config.file", "logging.properties");

        // init Database with drop-create (always drop and recreates the
        // database)
        try {
            if (this.getClass() == DatabaseModifyTestsMysql.class) {
                db = new DbManagerHibernate("com.mysql.jdbc.Driver", "org.hibernate.dialect.MySQL5InnoDBDialect",
                        "jdbc:mysql://localhost/", "sh_hibernate_junit", "hibernate", "temp", true, true);
            } else if (this.getClass() == DatabaseModifyTestsHsql.class) {
                db = new DbManagerHibernate("org.hsqldb.jdbcDriver",
                        "edu.udo.scaffoldhunter.model.db.HSQLDialectValid", "jdbc:hsqldb:file:./testdb/hsqldb",
                        "myschema", "hibernate", "temp", true, true);
            }
        } catch (DatabaseException e) {
            fail("Initialisation of Database failed");
        }

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
        session.setTitle("ganz toll");
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
            if (propDef.isScaffoldProperty())
                continue;
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
                    prop.setValue(random.nextInt(500));
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

        Scaffold scaffold = scaffolds.iterator().next();
        db.createOrUpdateComment("testkommentar scaffold: privat und lokal", true, tree, profile, scaffold);
        db.createOrUpdateComment("testkommentar scaffold: privat und global", true, null, profile, scaffold);
        db.createOrUpdateComment("testkommentar scaffold: public und lokal", false, tree, profile, scaffold);
        db.createOrUpdateComment("testkommentar scaffold: public und global", false, null, profile, scaffold);

        db.createBanner(true, tree, profile, uniqueMolecule);
        db.createBanner(false, tree, profile, uniqueMolecule);
        // end comments & Banner

        // /////////////////////////////////////////////////

        // Load first Profile and Session
        try {
            this.profile = db.getProfile(db.getAllProfileNames().get(0));
            db.loadCurrentSession(profile);
            this.session = profile.getCurrentSession();
        } catch (Exception e) {
            AssertionError err = new AssertionError();
            err.initCause(e);
            throw err;
        }
    }

    /**
     * Tests if the Tree can be deleted without errors
     */
    @org.junit.Test
    public void naiveTreeDeletionTest() {
        Tree tree = session.getTree();

        try {
            db.delete(tree);
        } catch (DatabaseException e) {
            e.printStackTrace();
            AssertionError err = new AssertionError("Deletion of Tree failed");
            err.initCause(e);
            throw err;
        }
    }

    /**
     * Tests if the Dataset can be deleted without errors
     */
    @org.junit.Test
    public void naiveDatasetDeletionTest() {
        Dataset dataset = session.getDataset();

        try {
            db.delete(dataset);
        } catch (DatabaseException e) {
            e.printStackTrace();
            AssertionError err = new AssertionError("Deletion of Tree failed");
            err.initCause(e);
            throw err;
        }
    }

    /**
     * Tests if the deletion of a Subset modifies the Molecules (it should not)
     */
    @org.junit.Test
    public void subsetDeletionMoleculeCascadeTest() {
        Subset root = profile.getCurrentSession().getSubset();
        int oldSize, newSize;

        try {
            oldSize = db.getRootSubsetSize(session.getDataset(), null);
        } catch (DatabaseException e) {
            e.printStackTrace();
            AssertionError err = new AssertionError("Counting old Molecules failed");
            err.initCause(e);
            throw err;
        }

        try {
            db.delete(root);
        } catch (DatabaseException e) {
            e.printStackTrace();
            AssertionError err = new AssertionError("Deletion of Subset failed");
            err.initCause(e);
            throw err;
        }

        try {
            newSize = db.getRootSubsetSize(session.getDataset(), null);
        } catch (DatabaseException e) {
            e.printStackTrace();
            AssertionError err = new AssertionError("Counting new Molecules failed");
            err.initCause(e);
            throw err;
        }

        Assert.assertEquals(oldSize, newSize);
    }

    /**
     * Tests the updating of the Session title
     */
    @org.junit.Test
    public void updateSessionTitleTest() {
        List<SessionInformation> infos = null;
        SessionInformation info;

        try {
            infos = db.getAllSessionsInformation(profile);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getAllSessionTitles failed");
            err.initCause(e);
            throw err;
        }

        Assert.assertEquals(1, infos.size());

        info = infos.iterator().next();

        info.setTitle("new_title");
        try {
            db.updateSessionTitle(info);
            db.loadCurrentSession(profile);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("updateSessionTitle or loadCurrentSession failed");
            err.initCause(e);
            throw err;
        }

        Assert.assertEquals("new_title", profile.getCurrentSession().getTitle());
    }

    /**
     * Tests the deletion of a Session
     */
    @org.junit.Test
    public void deleteSessionTest() {
        List<SessionInformation> infos = null;
        SessionInformation info;

        try {
            infos = db.getAllSessionsInformation(profile);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("getAllSessionTitles failed");
            err.initCause(e);
            throw err;
        }

        Assert.assertEquals(1, infos.size());

        info = infos.iterator().next();

        info.setTitle("new_title");
        try {
            db.deleteSession(info);
            infos = db.getAllSessionsInformation(profile);
        } catch (DatabaseException e) {
            AssertionError err = new AssertionError("deleteSession or getAllSessionInformations failed");
            err.initCause(e);
            throw err;
        }

        Assert.assertEquals(0, infos.size());
    }

    /*
     * Creates Scaffolds from Molecules (Helper Method for fillDb)
     */
    private static Set<Scaffold> createScaffolds(Tree tree, Set<Molecule> molecules) throws XMLStreamException,
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

    private static void addMolecules(Scaffold scaffold, Set<Molecule> molecules) {
        if (random.nextDouble() > 0.5) {
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

    private static void setHierarchyLevels(Scaffold s, int depth) {
        s.setHierarchyLevel(depth++);
        for (Scaffold c : s.getChildren())
            setHierarchyLevels(c, depth);
    }
}
