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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.com.bytecode.opencsv.CSVReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * @author Bernhard Dick
 * 
 */
public class CSVImportPluginResults implements PluginResults, MessageListener {
    private static Logger logger = LoggerFactory.getLogger(CSVImportPluginResults.class);
    
    private CSVImportPluginArguments arguments;
    private Map<String, PropertyDefinition> sourceProperties;
    private final Set<String> probablyNumeric = Sets.newHashSet();
    private int numMolecules;
    private String[] propertyNames;
    private final List<MessageListener> messageListeners = Lists.newLinkedList();

    /**
     * @param arguments
     */
    public CSVImportPluginResults(CSVImportPluginArguments arguments) {
        this.arguments = arguments;
        this.sourceProperties = new TreeMap<String, PropertyDefinition>(String.CASE_INSENSITIVE_ORDER);
        this.numMolecules = 0;

        File csvFile = new File(arguments.getFilename());
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader(csvFile), arguments.getSeparator(),
                    arguments.getQuotechar(), arguments.isStrictQuotes());
            propertyNames = csvReader.readNext();
            boolean[] maybeNumeric = new boolean[propertyNames.length];
            Arrays.fill(maybeNumeric, true);
            if (arguments.isFirstRowHeader()) {
                for (String string : propertyNames) {
                    sourceProperties.put(string, null);
                }
            } else {
                propertyNames = CSVImportPlugin.getDefaultColumnNames(propertyNames.length);
                for (String s : propertyNames) {
                    sourceProperties.put(s, null);
                }
            }
            String[] cur;
            while ((cur = csvReader.readNext()) != null) {
                for (int i = 0; i < maybeNumeric.length; i++) {
                    if (maybeNumeric[i] && cur[i] != null && !cur[i].equals("")) {
                        try {
                            double d = Double.parseDouble(cur[i]);
                            if (Double.isNaN(d) || Double.isInfinite(d)) {
                                maybeNumeric[i] = false;
                            }   
                        } catch (NumberFormatException e) {
                            maybeNumeric[i] = false;
                        }
                    }
                }
                numMolecules++;
            }

            for (int i = 0; i < propertyNames.length; i++) {
                if (maybeNumeric[i]) {
                    probablyNumeric.add(propertyNames[i]);
                }
            }

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } catch (IOException e) {
                    logger.error("csvReader could not be closed: {}", e.getMessage());
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#
     * getSourceProperties()
     */
    @Override
    public Map<String, PropertyDefinition> getSourceProperties() {
        return sourceProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#getTitleMapping
     * ()
     */
    @Override
    public String getTitleMapping() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#getMolecules
     * ()
     */
    @Override
    public Iterable<IAtomContainer> getMolecules() {
        return new CSVImportPluginIterable(arguments, propertyNames, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#getNumMolecules
     * ()
     */
    @Override
    public int getNumMolecules() {
        return numMolecules;
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#
     * getProbablyNumeric()
     */
    @Override
    public Set<String> getProbablyNumeric() {
        return Collections.unmodifiableSet(probablyNumeric);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#
     * addMessageListener(edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#
     * removeMessageListener(edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    private void sendMessage(Message message) {
        for (MessageListener l : messageListeners)
            l.receiveMessage(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.data.MessageListener#receiveMessage(edu.
     * udo.scaffoldhunter.model.data.Message)
     */
    @Override
    public void receiveMessage(Message message) {
        this.sendMessage(message);
    }

}
