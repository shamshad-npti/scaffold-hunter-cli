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

package edu.udo.scaffoldhunter.model;

import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.DataHolder;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.xml.Xpp3DomDriver;

import edu.udo.scaffoldhunter.gui.ConfigManager;
import edu.udo.scaffoldhunter.gui.ConfigManagerConverter;
import edu.udo.scaffoldhunter.gui.GUIController;
import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.gui.GUISessionConverter;
import edu.udo.scaffoldhunter.gui.ViewManagerConverter;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.view.ViewConverter;


/**
 * Serialized and deserializes objects using the XStream library.
 * 
 * @author Dominic Sacré
 */
public class XMLSerialization {

    private static final Logger logger = LoggerFactory.getLogger(XMLSerialization.class);

    private final XStream xstream;
    private final HierarchicalStreamDriver driver;

    /**
     * Default constructor
     */
    public XMLSerialization() {
        driver = new Xpp3DomDriver();
        xstream = new XStream(driver);

        xstream.alias("config", ConfigManager.class);
        xstream.alias("session", GUISession.class);

        xstream.registerConverter(new ConfigManagerConverter());
        xstream.registerConverter(new GUISessionConverter());
        xstream.registerConverter(new ViewManagerConverter());
        xstream.registerConverter(new ViewConverter());
    }


    /**
     * Serializes the global configuration into an XML string.
     * 
     * @param configManager
     *          the config manager
     * 
     * @return  the XML string
     */
    public String configToXML(ConfigManager configManager) {
        String xml = xstream.toXML(configManager);
        logger.debug("serialized config data:\n{}", xml);
        return xml;
    }

    /**
     * @param ctrl
     *          the GUI controller
     * @param xml
     *          the XML string to be deserialized
     *
     * @return  the deserialized configuration
     */
    public ConfigManager configFromXML(GUIController ctrl, String xml) {
        logger.debug("config data being deserialized:\n{}", xml);

        HierarchicalStreamReader reader = driver.createReader(new StringReader(xml));

        // put the GUI controller into the data holder, to make it accessible
        // to all converters
        DataHolder dataHolder = xstream.newDataHolder();
        dataHolder.put("ctrl", ctrl);
        
        return (ConfigManager) xstream.unmarshal(reader, null, dataHolder);
    }

    /**
     * Serializes a session into an XML string.
     * 
     * @param session
     *          the session to be serialized
     * 
     * @return  the XML string
     */
    public String sessionToXML(GUISession session) {
        String xml = xstream.toXML(session);
        logger.debug("serialized session data:\n{}", xml);
        return xml;
    }

    /**
     * Deserializes a session from an XML string.
     * 
     * @param ctrl
     *          the GUI controller
     * @param dbSession
     *          the DB session
     * @param xml
     *          the XML string to be deserialized
     *          
     * @return  the deserialized session
     */
    public GUISession sessionFromXML(GUIController ctrl, Session dbSession, String xml) {
        logger.debug("session data being deserialized:\n{}", xml);

        HierarchicalStreamReader reader = driver.createReader(new StringReader(xml));

        // put the GUI controller and database session into the data holder, to
        // make it accessible to all converters
        DataHolder dataHolder = xstream.newDataHolder();
        dataHolder.put("ctrl", ctrl);
        dataHolder.put("dbSession", dbSession);
        
        return (GUISession) xstream.unmarshal(reader, null, dataHolder);
    }

}
