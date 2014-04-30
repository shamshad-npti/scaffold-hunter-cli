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

package edu.udo.scaffoldhunter.gui;

import java.util.HashMap;
import java.util.LinkedHashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.udo.scaffoldhunter.model.XMLSerialization;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Session;


/**
 * An XStream converter for {@link GUISession} objects.
 * 
 * @author Dominic Sacré
 */
public class GUISessionConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(XMLSerialization.class);


    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return type == GUISession.class;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        GUISession session = (GUISession) source;

        // serialize the view manager
        writer.startNode("ui");
        context.convertAnother(session.getViewManager());
        writer.endNode();
        writer.startNode("selection");
        for(Molecule m : session.getSelection()) {
            writer.startNode("molecule-id");
            writer.setValue(Integer.toString(m.getId()));
            writer.endNode();
        }
        writer.endNode();
        writer.startNode("selection-id");        
            writer.setValue(Integer.toString(session.getSelection().getSelectedIndex()));
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        GUIController ctrl = (GUIController) context.get("ctrl");
        Session dbSession = (Session) context.get("dbSession");

        GUISession session = new GUISession(ctrl, dbSession);

        context.put("session", session);

        ViewManager viewManager = null;
        
        LinkedHashSet<Integer> molIDs = new LinkedHashSet<Integer>();
        int selectedIndex = 0;

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            try {
                // deserialize the view manager
                if (reader.getNodeName().equals("ui")) {
                    viewManager = (ViewManager) context.convertAnother(null, ViewManager.class);
                }
                else if (reader.getNodeName().equals("selection")) {
                // deserialize the selection
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        try {
                            if (reader.getNodeName().equals("molecule-id")) {
                                molIDs.add(Integer.parseInt(reader.getValue()));
                            }
                            else {
                                logger.debug("node '{}' not recognized", reader.getNodeName());
                            }
                        }
                        finally {
                            reader.moveUp();
                        }
                    }
                }
                else if (reader.getNodeName().equals("selection-id")) {
                    // deserialize the selection-id
                    selectedIndex = Integer.parseInt(reader.getValue());
                }
                else {
                    logger.debug("node '{}' not recognized", reader.getNodeName());
                }
            }
            finally {
                reader.moveUp();
            }
        }

        session.setViewManager(viewManager);
        
        //recreate selected index for the selection bar
        session.getSelection().setSelectedIndex(selectedIndex);
                
        //recreate the ordered list of selected molecules
        HashMap<Integer,Molecule> molecules = new HashMap<Integer, Molecule>();
        for(Molecule m : session.getSubsetController().getRootSubset()) {
            molecules.put(m.getId(), m);
        }
        
        LinkedHashSet<Molecule> newSelection = new LinkedHashSet<Molecule>();
        
        for(Integer i : molIDs) {
            if(molecules.containsKey(i)) {
                newSelection.add(molecules.get(i));
            }
        }
        
        session.getSelection().addAll(newSelection);
        
       return session;
    }

}
