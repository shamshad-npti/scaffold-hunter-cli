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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.sdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import com.google.common.collect.Iterators;

import edu.udo.scaffoldhunter.model.util.MoleculeConfigurator;

/**
 * @author Bernhard Dick
 * 
 */
public class SDFImportPluginIterable implements Iterable<IAtomContainer> {
    private final File file;

    /**
     * @param sdfile 
     * 
     */
    public SDFImportPluginIterable(File sdfile) {
        file = sdfile;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<IAtomContainer> iterator() {
        FileInputStream is;
        try {
            is = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return Iterators.emptyIterator();
        }
        return new SDIterator(is);
    }

    static class SDIterator implements Iterator<IAtomContainer> {

        private final IteratingMDLReader reader;

        public SDIterator(FileInputStream is) {
            reader = new IteratingMDLReader(is, SilentChemObjectBuilder.getInstance());
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#hasNext()
         */
        @Override
        public boolean hasNext() {
            return reader.hasNext();
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#next()
         */
        @Override
        public IAtomContainer next() {
            if (reader.hasNext()) {
                IAtomContainer mol = reader.next();
                MoleculeConfigurator.prepare(mol, false);
                return mol;
            } else {
                throw new NoSuchElementException();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.util.Iterator#remove()
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
