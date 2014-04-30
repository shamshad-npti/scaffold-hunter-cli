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

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * @author Dominic Sacré
 *
 */
public class ConfigManagerConverter implements Converter {

//    private static final Logger logger = LoggerFactory.getLogger(XMLSerialization.class);


    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return type == ConfigManager.class;
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ConfigManager configManager = (ConfigManager) source;

        writer.startNode("global");
        context.convertAnother(configManager.getGlobalConfig());
        writer.endNode();
        
        for (Class<? extends View> viewClass: ViewClassRegistry.getClasses()) {
            writer.startNode("class");
            String typeName = viewClass.getSimpleName();
            writer.addAttribute("type", typeName);
            context.convertAnother(configManager.getViewClassConfig(viewClass));
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        GUIController ctrl = (GUIController) context.get("ctrl");

        ConfigManager configManager = new ConfigManager(ctrl);
        
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            
            try {
                if (reader.getNodeName().equals("global")) {
                    GlobalConfig globalConfig = (GlobalConfig) context.convertAnother(null, GlobalConfig.class);
                    configManager.setGlobalConfig(globalConfig);
                }
                else if (reader.getNodeName().equals("class")) {
                    String typeName = reader.getAttribute("type");
                    Class<? extends View> viewClass = ViewClassRegistry.getClassForName(typeName);
                    Class<? extends ViewClassConfig> configClass = ViewClassRegistry.getClassConfigClass(viewClass);
                    ViewClassConfig config = (ViewClassConfig) context.convertAnother(null, configClass);
                    configManager.setViewClassConfig(viewClass, config);
                }
//            } catch (Exception ex) {
//                logger.error("an error occured while restoring a configuration", ex);
            } finally {
                reader.moveUp();
            }
        }

        return configManager;
    }

}
