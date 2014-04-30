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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.example4;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.silent.AtomContainer;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.dataimport.MergeMessageTypes;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * @author Bernhard Dick
 * 
 */
public class Example4ImportPluginResults implements PluginResults {

    private Example4ImportPluginArguments arguments;
    private final List<MessageListener> messageListeners = Lists.newLinkedList();

    /**
     * @param arguments
     * 
     */
    public Example4ImportPluginResults(Example4ImportPluginArguments arguments) {
        this.arguments = arguments;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getSourceProperties
     * ()
     */
    @Override
    public Map<String, PropertyDefinition> getSourceProperties() {
        HashMap<String, PropertyDefinition> sourceProperties = new HashMap<String, PropertyDefinition>();

        sourceProperties.put("title", null);
        sourceProperties.put("number", null);

        return sourceProperties;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getProbablyNumeric
     * ()
     */
    @Override
    public Set<String> getProbablyNumeric() {
        Set<String> probablyNumeric = Sets.newHashSet();
        probablyNumeric.add("number");
        return probablyNumeric;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getTitleMapping()
     */
    @Override
    public String getTitleMapping() {
        return "title";
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getMolecules()
     */
    @Override
    public Iterable<IAtomContainer> getMolecules() {
        return new Iterable<IAtomContainer>() {

            @Override
            public Iterator<IAtomContainer> iterator() {
                return new Iterator<IAtomContainer>() {
                    boolean notRead = true;

                    @Override
                    public boolean hasNext() {
                        return notRead;
                    }

                    @Override
                    public IAtomContainer next() {
                        if (notRead) {
                            if (arguments.generateMessage) {
                                Message message = new Message(MergeMessageTypes.MOLECULE_BY_SMILES_FAILED, "", null,
                                        null);
                                for (MessageListener l : messageListeners) {
                                    l.receiveMessage(message);
                                }
                            }
                            IAtomContainer molecule = new AtomContainer();
                            molecule.setProperty("title", arguments.moleculeTitle);
                            molecule.setProperty("number", 2342);
                            notRead = false;
                            return molecule;
                        } else {
                            throw new NoSuchElementException();
                        }
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#getNumMolecules()
     */
    @Override
    public int getNumMolecules() {
        return 1;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#addMessageListener
     * (edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.plugins.dataimport.PluginResults#removeMessageListener
     * (edu.udo.scaffoldhunter.model.data.MessageListener)
     */
    @Override
    public void removeMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

}
