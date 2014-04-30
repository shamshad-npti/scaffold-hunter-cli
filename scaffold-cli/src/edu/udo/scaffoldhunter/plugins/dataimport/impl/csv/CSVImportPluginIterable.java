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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import au.com.bytecode.opencsv.CSVReader;
import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.dataimport.MergeMessageTypes;

/**
 * @author Bernhard Dick
 * 
 */
public class CSVImportPluginIterable implements Iterable<IAtomContainer> {
    String[] propertyNames;
    CSVImportPluginArguments arguments;
    MessageListener messageListener;

    /**
     * @param arguments
     * @param propertyNames
     *            an Array with the property names by row
     * @param messageListener
     */
    public CSVImportPluginIterable(CSVImportPluginArguments arguments, String[] propertyNames,
            MessageListener messageListener) {
        this.arguments = arguments;
        this.propertyNames = propertyNames;
        this.messageListener = messageListener;
    }

    private class CSVIterator implements Iterator<IAtomContainer> {
        private String[] lastLine, propertyNames;
        private CSVReader csvReader;
        private MessageListener messageListener;

        public CSVIterator(CSVImportPluginArguments arguments, String[] propertyNames, MessageListener messageListener)
                throws IOException {
            this.propertyNames = propertyNames;
            this.messageListener = messageListener;
            File csvFile = new File(arguments.getFilename());
            csvReader = new CSVReader(new FileReader(csvFile), arguments.getSeparator(), arguments.getQuotechar(),
                    arguments.isStrictQuotes());
            // TODO also escape character?
            if (arguments.isFirstRowHeader()) {
                csvReader.readNext();
            }
        }

        @Override
        public boolean hasNext() {
            if (lastLine == null) {
                try {
                    lastLine = csvReader.readNext();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return lastLine != null;
        }

        @Override
        public IAtomContainer next() {
            if (lastLine == null) {
                try {
                    lastLine = csvReader.readNext();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (lastLine != null) { // Check if we read a new line
                IAtomContainer res;
                try {
                    SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
                    String smiles = arguments.getSmilesColumnId() < lastLine.length ? 
                            lastLine[arguments.getSmilesColumnId()] : "";
                    res = sp.parseSmiles(smiles);
                    if (res.getAtomCount() == 0) {
                        throw new InvalidSmilesException("Empty molecule read from SMILES");
                    }
                } catch (InvalidSmilesException ise) {
                    messageListener.receiveMessage(new Message(MergeMessageTypes.MOLECULE_BY_SMILES_FAILED, "", null,
                            null));
                    res = new AtomContainer();
                }
                for (int i = 0; i < propertyNames.length; i++) {
                    res.setProperty(propertyNames[i], lastLine[i]);
                }

                lastLine = null;
                return res;
            } else {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<IAtomContainer> iterator() {
        try {
            return new CSVIterator(arguments, propertyNames, messageListener);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
}
