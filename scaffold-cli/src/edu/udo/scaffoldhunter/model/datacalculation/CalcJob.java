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

package edu.udo.scaffoldhunter.model.datacalculation;

import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.data.Job;
import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPluginResults;

/**
 * Describes a calculation task
 * 
 * @author Henning Garus
 * @author Philipp Lewe
 * 
 */
public class CalcJob implements Job, MessageListener {

    private final CalcPlugin plugin;
    private Object pluginArguments;
    private final String jobName;
    private CalcPluginResults results = null;
    private final List<MessageListener> messageListeners = Lists.newLinkedList();

    /**
     * Creates a new calc job
     * 
     * @param name
     *            the name of the job
     * @param plugin
     *            the plugin responsible for this job
     * @param pluginArguments
     *            the argument which will be given to the plugin to perform this
     *            job
     */
    public CalcJob(String name, CalcPlugin plugin, Object pluginArguments) {
        this.plugin = plugin;
        this.pluginArguments = pluginArguments;
        this.jobName = name;
    }

    /**
     * Calls the plugin with its arguments
     * 
     * @param molecules
     * 
     */
    public void computePluginResults(Iterable<IAtomContainer> molecules) {
        results = plugin.getResults(pluginArguments, molecules, this);
    }

    /**
     * @return the textual description of this source
     */
    public String getDescription() {
        return plugin.getDescription();
    }

    /**
     * @return the pluginArguments
     */
    public Object getPluginArguments() {
        return pluginArguments;
    }

    /**
     * @param pluginArguments
     *            the pluginArguments to set
     */
    public void setPluginArguments(Object pluginArguments) {
        this.pluginArguments = pluginArguments;
    }

    /**
     * @return the plugin
     */
    public CalcPlugin getPlugin() {
        return plugin;
    }

    /**
     * @return the jobName
     */
    public String getJobName() {
        return jobName;
    }

    /**
     * @return the results
     */
    public CalcPluginResults getResults() {
        return results;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", jobName, plugin.getTitle());
    }

    /**
     * add a message listener
     * 
     * @param listener
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /**
     * remove a message listener
     * 
     * @param listener
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    private void sendMessage(Message message) {
        for (MessageListener l : messageListeners)
            l.receiveMessage(message);
    }

    @Override
    public void receiveMessage(Message message) {
        // receive message from job
        Message msg = new Message(message.getType(), message.getMoleculeTitle(), message.getPropertyDefinition(), this);
        sendMessage(msg);
    }
}
