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
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * @author Bernhard Dick
 * 
 */
public class SDFImportPluginResults implements PluginResults {

    private SDFImportPluginArguments arguments;
    private Map<String, PropertyDefinition> sourceProperties;
    private final Set<String> probablyNumeric;
    private int numMolecules;

    /**
     * @param arguments
     */
    public SDFImportPluginResults(SDFImportPluginArguments arguments) {
        this.arguments = arguments;
        this.sourceProperties = new TreeMap<String, PropertyDefinition>(String.CASE_INSENSITIVE_ORDER);
        this.numMolecules = 0;

        File sdfFile = new File(arguments.getFilename());
        IAtomContainer cur;
        Set<String> notNumeric = Sets.newHashSet();
        IteratingMDLReader reader = null;
        try {
            reader = new IteratingMDLReader(new FileInputStream(sdfFile), SilentChemObjectBuilder.getInstance());
            while (reader.hasNext()) {
                // only properties required here, no need to configure molecule
                cur = reader.next();
                for (Entry<Object, Object> e : cur.getProperties().entrySet()) {
                    if (e.getValue() == null) {
                        continue;
                    }
                    if (!sourceProperties.containsKey(e.getKey())) {
                        sourceProperties.put((String) e.getKey(), null);
                    }
                    if (e.getValue().toString().isEmpty()) {
                        continue;
                    }
                    if (!notNumeric.contains(e.getKey())) {
                        boolean numeric = true;
                        try {
                            double d = Double.parseDouble((String) e.getValue());
                            if (Double.isNaN(d) || Double.isInfinite(d)) {
                                numeric = false;
                            }   
                        } catch (NumberFormatException ex) {
                            numeric = false;
                        }
                        if (!numeric) notNumeric.add((String) e.getKey());                        
                    }
                }
                numMolecules++;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch(IOException ex){}
        }
        probablyNumeric = Sets.difference(sourceProperties.keySet(), notNumeric);
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
        // TODO maybe make this somewhat nicer
        return "cdk:Title";
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
        File sdfFile = new File(arguments.getFilename());
        return new SDFImportPluginIterable(sdfFile);
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

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#addMessageListener(edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void addMessageListener(MessageListener listener) {
        
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#removeMessageListener(edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void removeMessageListener(MessageListener listener) {
        
    }
}
