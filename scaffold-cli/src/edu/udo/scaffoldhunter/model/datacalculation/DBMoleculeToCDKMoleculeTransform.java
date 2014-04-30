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

package edu.udo.scaffoldhunter.model.datacalculation;

import java.io.StringReader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.silent.Molecule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;

import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.NumProperty;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.StringProperty;
import edu.udo.scaffoldhunter.model.util.MoleculeConfigurator;

/**
 * Transform function to convert an {@link Iterable} over
 * {@link edu.udo.scaffoldhunter.model.db.Molecule}s into an {@link Iterable}
 * over {@link org.openscience.cdk.interfaces.IAtomContainer}. The function will load the
 * existing properties of a {@link edu.udo.scaffoldhunter.model.db.Molecule} and
 * attach them to the newly created {@link org.openscience.cdk.Molecule}.
 * 
 * @author Philipp Lewe
 * 
 */
public class DBMoleculeToCDKMoleculeTransform implements
        Function<edu.udo.scaffoldhunter.model.db.Molecule, IAtomContainer> {

    private static final Logger logger = LoggerFactory.getLogger(DBMoleculeToCDKMoleculeTransform.class);

    private final DbManager db;

    /**
     * Mapping: DB PropertyDefinition -> copied Property Definition (Input for
     * plugin)
     */
    private final Map<PropertyDefinition, PropertyDefinition> properties;

    /**
     * Default constructor
     * 
     * @param db
     *            the db manager
     * @param properties
     *            the available properties
     */
    public DBMoleculeToCDKMoleculeTransform(DbManager db, Map<PropertyDefinition, PropertyDefinition> properties) {
        this.db = db;
        this.properties = properties;
    }

    @Override
    public IAtomContainer apply(edu.udo.scaffoldhunter.model.db.Molecule dbMolecule) {
        try {
            String molString = db.getStrucMol(dbMolecule);
            MDLReader reader = new MDLReader(new StringReader(molString));
            IMolecule cdkMolecule = new Molecule();
            cdkMolecule = reader.read(cdkMolecule);
            MoleculeConfigurator.prepare(cdkMolecule, false);
            Collection<edu.udo.scaffoldhunter.model.db.Molecule> lockList = Collections.singletonList(dbMolecule);
            
            
            // set cdk title to db title (used for message handlers to identify molecule)
            cdkMolecule.setProperty(CDKConstants.TITLE, dbMolecule.getTitle());
            cdkMolecule.setProperty(CDKConstants.SMILES, dbMolecule.getSmiles());

            // load db property definitions
            db.lockAndLoad(properties.keySet(), lockList);

            for (NumProperty prop : dbMolecule.getNumProperties().values()) {
                PropertyDefinition copiedPropDef = properties.get(prop.getType());
                cdkMolecule.getProperties().put(copiedPropDef, prop.getValue());
            }

            for (StringProperty prop : dbMolecule.getStringProperties().values()) {
                PropertyDefinition copiedPropDef = properties.get(prop.getType());
                cdkMolecule.getProperties().put(copiedPropDef, prop.getValue());
            }

            // unload db property definitions
            db.unlockAndUnload(properties.keySet(), lockList);

            return cdkMolecule;
        } catch (CDKException e) {
            logger.warn("Error while reading molecule structure from db", e);
            throw new DBMoleculeToCDKMoleculeTransformException(e);
        } catch (DatabaseException e) {
            logger.warn("Error while parsing molecule structure from mol file", e);
            throw new DBMoleculeToCDKMoleculeTransformException(e);
        }
    }

}
