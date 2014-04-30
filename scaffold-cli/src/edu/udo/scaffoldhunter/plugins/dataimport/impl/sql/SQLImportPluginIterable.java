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

import java.io.StringReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.io.MDLReader;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.SilentChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;
import edu.udo.scaffoldhunter.model.dataimport.MergeMessageTypes;
import edu.udo.scaffoldhunter.model.util.MoleculeConfigurator;

/**
 * @author Bernhard Dick
 * 
 */
public class SQLImportPluginIterable implements Iterable<IAtomContainer> {

    SQLImportPluginArguments arguments;
    Connection connection;
    MessageListener messageListener;

    /**
     * @param arguments
     * @param connection
     * @param messageListener
     */
    public SQLImportPluginIterable(SQLImportPluginArguments arguments, Connection connection,
            MessageListener messageListener) {
        this.arguments = arguments;
        this.connection = connection;
        this.messageListener = messageListener;
    }

    private static class SQLImportPluginIterator implements Iterator<IAtomContainer> {
        private SQLImportPluginArguments arguments;
        private ResultSet resultSet;
        private boolean nextSelected;
        MessageListener messageListener;

        public SQLImportPluginIterator(SQLImportPluginArguments arguments, Connection connection,
                MessageListener messageListener) throws SQLException {
            this.arguments = arguments;
            this.messageListener = messageListener;
            Statement statement = connection.createStatement();
            resultSet = statement.executeQuery(arguments.getSelectClause());
        }

        @Override
        public boolean hasNext() {
            if (!nextSelected) {
                try {
                    nextSelected = resultSet.next();
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return false;
                }
            }
            return nextSelected;
        }

        @Override
        public IAtomContainer next() {
            try {
                if (nextSelected || resultSet.next()) {
                    nextSelected = false;
                    IAtomContainer res = null;
                    SmilesParser sp = new SmilesParser(SilentChemObjectBuilder.getInstance());
                    if (!arguments.getMolColumn().equals(" - none - ")) {
                        MDLReader reader = new MDLReader(
                                new StringReader(resultSet.getString(arguments.getMolColumn())));
                        try {
                            res = reader.read(new AtomContainer());
                            MoleculeConfigurator.prepare(res, false);
                        } catch (CDKException e) {
                            messageListener.receiveMessage(new Message(MergeMessageTypes.MOLECULE_BY_MOL_FAILED, "",
                                    null, null));
                        }
                    }
                    if (res == null && !arguments.getSmilesColumn().equals(" - none - ")) {
                        try {
                            res = sp.parseSmiles(resultSet.getString(arguments.getSmilesColumn()));
                        } catch (InvalidSmilesException ise) {
                            messageListener.receiveMessage(new Message(MergeMessageTypes.MOLECULE_BY_SMILES_FAILED, "",
                                    null, null));
                        }
                    }
                    if (res == null) {
                        res = new AtomContainer();
                    }
                    for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                        res.setProperty(resultSet.getMetaData().getColumnName(i), resultSet.getString(i));
                    }
                    return res;
                } else {
                    throw new NoSuchElementException();
                }
            } catch (SQLException e) {
                e.printStackTrace();
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
            return new SQLImportPluginIterator(this.arguments, this.connection, messageListener);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
