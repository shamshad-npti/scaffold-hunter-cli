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

package edu.udo.scaffoldhunter.view;

import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.udo.scaffoldhunter.gui.ConfigManager;
import edu.udo.scaffoldhunter.gui.GUIController;
import edu.udo.scaffoldhunter.gui.GUISession;
import edu.udo.scaffoldhunter.model.GlobalConfig;
import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.model.XMLSerialization;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Subsets;


/**
 * An XStream converter for objects implementing the {@link View} interface.
 *
 * @author Dominic Sacré
 */
public class ViewConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(XMLSerialization.class);


    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return View.class.isAssignableFrom(type);
    }

    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        View view = (View) source;

        String typeName = view.getClass().getSimpleName();
        writer.addAttribute("type", typeName);

        writer.startNode("subset");
        writer.addAttribute("id", Integer.toString(view.getSubset().getId()));
        writer.endNode();

        writer.startNode("config");
        context.convertAnother(view.getInstanceConfig());
        writer.endNode();

        writer.startNode("state");
        context.convertAnother(view.getState());
        writer.endNode();
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        GUIController ctrl = (GUIController) context.get("ctrl");
        GUISession session = (GUISession) context.get("session");
        ConfigManager configManager = ctrl.getConfigManager();

        String typeName = reader.getAttribute("type");

        Class<? extends View> klass = ViewClassRegistry.getClassForName(typeName);
        Class<? extends ViewInstanceConfig> instanceConfigKlass = ViewClassRegistry.getInstanceConfigClass(klass);
        Class<? extends ViewState> stateKlass = ViewClassRegistry.getStateClass(klass);

        ViewClassConfig classConfig = configManager.getViewClassConfig(klass);

        GlobalConfig globalConfig = configManager.getGlobalConfig();

        Integer subsetId = null;
        ViewInstanceConfig instanceConfig = null;
        ViewState state = null;

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            try {
                if (reader.getNodeName().equals("subset")) {
                    subsetId = Integer.parseInt(reader.getAttribute("id"));
                }
                else if (reader.getNodeName().equals("config")) {
                    instanceConfig = (ViewInstanceConfig) context.convertAnother(null, instanceConfigKlass);
                }
                else if (reader.getNodeName().equals("state")) {
                    state = (ViewState) context.convertAnother(null, stateKlass);
                }
                else {
                    logger.debug("node '{}' not recognized", reader.getNodeName());
                }
            }
            finally {
                reader.moveUp();
            }
        }

        return recreateView(klass, session, subsetId, instanceConfig, classConfig, globalConfig, state);
    }

    /**
     * Re-creates a view using the given subset, config and state.
     *
     * @param klass
     * @param session
     * @param subsetId
     * @param instanceConfig
     * @param classConfig
     * @param globalConfig
     * @param state
     *
     * @return  the view
     */
    private View recreateView(Class<? extends View> klass, GUISession session, Integer subsetId,
                              ViewInstanceConfig instanceConfig, ViewClassConfig classConfig,
                              GlobalConfig globalConfig, ViewState state) {
        Preconditions.checkNotNull(klass);
        Preconditions.checkNotNull(subsetId);
        Preconditions.checkNotNull(instanceConfig);
        Preconditions.checkNotNull(state);

        Subset subset = findSubset(session.getDbSession().getSubset(), subsetId);

        return ViewClassRegistry.instantiate(klass, session, subset, instanceConfig, classConfig, globalConfig, state);
    }

    /**
     * Finds the subset corresponding to the given database id.
     *
     * @param root
     *          the root of the subset tree to be searched
     * @param subsetId
     *          the database id
     *
     * @return  the subset found
     *
     * @throws  NoSuchElementException
     */
    private Subset findSubset(Subset root, final int subsetId) {
        Iterable<Subset> subsetIterable = Subsets.getSubsetTreeIterable(root);

        return Iterables.find(subsetIterable, new Predicate<Subset>() {
            @Override
            public boolean apply(Subset input) {
                return input.getId() == subsetId;
            }
        });
    }

}
