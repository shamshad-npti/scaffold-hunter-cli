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

package edu.udo.scaffoldhunter.model.dataexport;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.silent.Molecule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.gui.util.DBExceptionHandler;
import edu.udo.scaffoldhunter.gui.util.VoidNullaryDBFunction;
import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.MoleculeConfigurator;

/**
 * @author Philipp Kopp
 * 
 */
public class ExportIterable implements Iterable<IAtomContainer> {
    private static Logger logger = LoggerFactory.getLogger(ExportIterable.class);
    private ArrayList<edu.udo.scaffoldhunter.model.db.Molecule> molecules;
    private Collection<PropertyDefinition> propDefs;
    private DbManager db;
    private boolean exportDescriptions;
    private boolean exportSmiles;

    /**
     * @param subset
     * @param propDefs
     * @param db
     * @param exportDescriptions 
     * @param exportSmiles
     */
    public ExportIterable(Subset subset, Collection<PropertyDefinition> propDefs, DbManager db,
            boolean exportDescriptions, boolean exportSmiles) {
        this.db = db;
        this.exportDescriptions = exportDescriptions;
        this.exportSmiles = exportSmiles;
        molecules = new ArrayList<edu.udo.scaffoldhunter.model.db.Molecule>();
        for (edu.udo.scaffoldhunter.model.db.Molecule molecule : subset.getMolecules()) {
            molecules.add(molecule);
        }

        this.propDefs = propDefs;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<IAtomContainer> iterator() {
        return new Iterator<IAtomContainer>() {
            private int count = 0;

            @Override
            public boolean hasNext() {
                return count < molecules.size();
            }

            @Override
            public IAtomContainer next() {
                final edu.udo.scaffoldhunter.model.db.Molecule toCopy = molecules.get(count);
                count++;
                String molString;
                try {
                    molString = db.getStrucMol(toCopy);
                } catch (DatabaseException e) {
                    throw new RuntimeException(e);
                }
                MDLReader reader = new MDLReader(new StringReader(molString));
                IMolecule mol = new Molecule();
                try {
                    mol = reader.read(mol);
                } catch (CDKException e) {
                    throw new RuntimeException(e);
                }
                MoleculeConfigurator.prepare(mol, false);
                mol.setID(toCopy.getTitle());
                if (exportSmiles) {
                    mol.setProperty("SMILES", toCopy.getSmiles());
                }
                for (final PropertyDefinition prop : propDefs) {
                    DBExceptionHandler.callDBManager(db, new VoidNullaryDBFunction() {
                        @Override
                        public void voidCall() throws DatabaseException {
                            db.lockAndLoad(prop, toCopy);
                        }
                    });
                    
                    String title = prop.getTitle();
                    if (exportDescriptions) {
                        title += " (".concat(prop.getDescription()).concat(")");
                    }
                    if (prop.isStringProperty()) {
                        if (toCopy.getStringProperties().get(prop.getId()) != null) {
                            mol.setProperty(title, toCopy.getStringPropertyValue(prop));
                        }
                    } else {
                        if (toCopy.getNumProperties().get(prop.getId()) != null) {
                            mol.setProperty(title, toCopy.getNumPropertyValue(prop).toString());
                        }
                    }

                    try {
                        db.unlockAndUnload(prop, toCopy);
                    } catch (Exception e) {
                        logger.warn("Trying to unlock data, where no data has been locked before");
                    }
                }

                return mol;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
