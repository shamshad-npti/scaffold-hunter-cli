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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;

/**
 * @author Bernhard Dick
 * 
 */
public class SQLImportPluginResults implements PluginResults, MessageListener {
    private Connection connection;
    private SQLImportPluginArguments arguments;
    private Map<String, PropertyDefinition> sourceProperties;
    private final Set<String> probablyNumeric;
    private int numMolecules;
    private final List<MessageListener> messageListeners = Lists.newLinkedList();

    /**
     * @param arguments
     * 
     */
    public SQLImportPluginResults(SQLImportPluginArguments arguments) {
        this.arguments = arguments;
        probablyNumeric = Sets.newHashSet();
        sourceProperties = Maps.newTreeMap();
        Properties connProps = new Properties();
        connProps.put("user", arguments.getUser());
        connProps.put("password", arguments.getPass());
        try {
            this.connection = DriverManager.getConnection(arguments.getUrl(), connProps);

            numMolecules = 0;
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(arguments.getSelectClause());
            ResultSetMetaData meta = result.getMetaData();
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                if (meta.getColumnType(i) == java.sql.Types.INTEGER || meta.getColumnType(i) == java.sql.Types.DOUBLE) {
                    probablyNumeric.add(meta.getColumnName(i));
                }
                sourceProperties.put(meta.getColumnName(i), null);
            }
            while (result.next()) {
                numMolecules++;
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            numMolecules = 0;
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
     * @see edu.udo.scaffoldhunter.model.dataimport.plugins.PluginResults#
     * getProbablyNumeric()
     */
    @Override
    public Set<String> getProbablyNumeric() {
        return probablyNumeric;
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
        // TODO Auto-generated method stub
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
        return new SQLImportPluginIterable(arguments, connection, this);
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
