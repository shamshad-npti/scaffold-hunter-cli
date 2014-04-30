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

import java.util.List;
import java.util.Map;

import org.hibernate.envers.tools.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

import edu.udo.scaffoldhunter.model.XMLSerialization;
import edu.udo.scaffoldhunter.view.View;


/**
 * An XStream converter for {@link ViewManager} objects.
 * 
 * @author Dominic Sacré
 */
public class ViewManagerConverter implements Converter {

    private static final Logger logger = LoggerFactory.getLogger(XMLSerialization.class);


    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class type) {
        return type == ViewManager.class;
    }


    @Override
    public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
        ViewManager viewManager = (ViewManager) source;

        // iterate through all windows
        for (Window window : viewManager.getWindows()) {
            writer.startNode("window");
            marshalWindow((MainWindow)window, viewManager, writer, context);
            writer.endNode();
        }
    }

    /**
     * Serializes a single window.
     * 
     * @param window
     * @param viewManager
     * @param writer
     * @param context
     */
    private void marshalWindow(MainWindow window, final ViewManager viewManager,
                               HierarchicalStreamWriter writer, MarshallingContext context) {
        // serialize the window's state
        writer.startNode("state");
        context.convertAnother(window.getState());
        writer.endNode();

        if (viewManager.getWindowNumSplits(window) == 1) {
            // the window is not split. iterate through all its views and
            // create "tab" nodes directly under the "window" node
            for (View view : viewManager.getViews(window, 0)) {
                writer.startNode("tab");
                marshalTab(view, viewManager.getViewState(view), writer, context);
                writer.endNode();
            }
        } else {
            // the window is split. iterate through splits and create a "split"
            // node for each
            for (int split = 0; split < viewManager.getWindowNumSplits(window); ++split) {
                writer.startNode("split");
                marshalSplit(viewManager.getViews(window, split), viewManager, writer, context);
                writer.endNode();
            }
        }
    }

    /**
     * Serializes a split.
     * 
     * @param views
     * @param viewManager
     * @param writer
     * @param context
     */
    private void marshalSplit(List<View> views, ViewManager viewManager,
                              HierarchicalStreamWriter writer, MarshallingContext context) {
        // iterate through all tabs in the current split, creating a "tab" node
        // for each
        for (View view : views) {
            writer.startNode("tab");
            marshalTab(view, viewManager.getViewState(view), writer, context);
            writer.endNode();
        }
    }

    /**
     * Serializes a single tab.
     * 
     * @param view
     * @param externalState
     * @param writer
     * @param context
     */
    private void marshalTab(View view, ViewExternalState externalState,
                            HierarchicalStreamWriter writer, MarshallingContext context) {
        // serialize the view itself
        writer.startNode("view");
        context.convertAnother(view);
        writer.endNode();

        // serialize the view's external state
        writer.startNode("externalState");
        context.convertAnother(externalState);
        writer.endNode();
    }


    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
        GUIController ctrl = (GUIController) context.get("ctrl");
        GUISession session = (GUISession) context.get("session");

        ViewManager viewManager = new ViewManager(ctrl, session);

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            try {
                if (reader.getNodeName().equals("window")) {
                    unmarshalWindow(viewManager, reader, context);
                } else {
                    logger.debug("node '{}' not recognized", reader.getNodeName());
                }
            }
            finally {
                reader.moveUp();
            }
        }

        return viewManager;
    }

    /**
     * Deserializes a single window and adds it to the view manager.
     * 
     * @param viewManager
     * @param reader
     * @param context
     */
    private void unmarshalWindow(ViewManager viewManager, HierarchicalStreamReader reader,
                                 UnmarshallingContext context) {
        MainWindowState state = null;

        // the window can't be created until all sub-nodes have been read,
        // so we need to save all views until they can finally be added.
        Map<ViewPosition, Pair<View, ViewExternalState>> views = Maps.newLinkedHashMap();

        // the current view position. view positions are not stored in
        // the serialized data, so we rely on the order of the nodes instead.
        ViewPosition pos = new ViewPosition(0, 0);
    
        while (reader.hasMoreChildren()) {
            reader.moveDown();
    
            try {
                if (reader.getNodeName().equals("state")) {
                    // deserialize the window's state
                    state = unmarshalWindowState(reader, context);
                }
                else if (reader.getNodeName().equals("tab")) {
                    // deserialize a single tab
                    unmarshalTab(views, pos, reader, context);
                }
                else if (reader.getNodeName().equals("split")) {
                    // deserialize a split and all the tabs therein
                    unmarshalSplit(views, pos, reader, context);
                }
                else {
                    logger.debug("node '{}' not recognized", reader.getNodeName());
                }
            }
            finally {
                reader.moveUp();
            }
        }

        GUISession session = (GUISession) context.get("session");

        recreateWindow(session, viewManager, state, views, pos);
    }

    /**
     * Deserializes a split.
     * 
     * @param views
     *          the map in which deserialized views are inserted
     * @param pos
     *          the current view position
     * @param reader
     * @param context
     */
    private void unmarshalSplit(Map<ViewPosition, Pair<View, ViewExternalState>> views, ViewPosition pos,
                                HierarchicalStreamReader reader, UnmarshallingContext context) {
        pos.tab = 0;

        // iterate through all child-nodes of the split
        while (reader.hasMoreChildren()) {
            reader.moveDown();

            try {
                // deserialize a single tab
                if (reader.getNodeName().equals("tab")) {
                    unmarshalTab(views, pos, reader, context);
                }
                else {
                    logger.debug("node '{}' not recognized", reader.getNodeName());
                }
            }
            finally {
                reader.moveUp();
            }
        }

        ++pos.split;
    }

    /**
     * Deserializes a single tab.
     * 
     * @param views
     *          the map in which deserialized views are inserted
     * @param pos
     *          the current view position
     * @param reader
     * @param context
     */
    private void unmarshalTab(Map<ViewPosition, Pair<View, ViewExternalState>> views, ViewPosition pos,
                              HierarchicalStreamReader reader, UnmarshallingContext context) {
//        try {
            View view = null;
            ViewExternalState externalState = null;

            while (reader.hasMoreChildren()) {
                reader.moveDown();

                try {
                    if (reader.getNodeName().equals("view")) {
                        // deserialize the view itself
                        view = (View) context.convertAnother(null, View.class);
                    }
                    else if (reader.getNodeName().equals("externalState")) {
                        // deserialize the view's external state
                        externalState = unmarshalViewExternalState(reader, context);
                    }
                    else {
                        logger.debug("node '{}' not recognized", reader.getNodeName());
                    }
                }
                finally {
                    reader.moveUp();
                }
            }

            Pair<View, ViewExternalState> pair = Pair.make(view, externalState);
            views.put(new ViewPosition(pos), pair);

            ++pos.tab;
//        }
//        catch (Exception ex) {
//            logger.error("an error occured while restoring a view", ex);
//        }
    }

    private MainWindowState unmarshalWindowState(HierarchicalStreamReader reader, UnmarshallingContext context) {
//        try {
            return (MainWindowState) context.convertAnother(null, MainWindowState.class);
//        } catch (Exception ex) {
//            logger.error("main window state could not be restored, using default", ex);
//
//            // if an exception is thrown, we somehow end up (at least) one
//            // level down from where we came. i can only assume that the
//            // xstream developers have never heard of the finally keyword.
//            // our best bet is to just go up one level...
//            reader.moveUp();
//
//            return new MainWindowState();
//        }
    }

    private ViewExternalState unmarshalViewExternalState(HierarchicalStreamReader reader, UnmarshallingContext context) {
//        try {
            return (ViewExternalState) context.convertAnother(null, ViewExternalState.class);
//        } catch (Exception ex) {
//            logger.error("a view's state could not be restored, using default", ex);
//            
//            // see above
//            reader.moveUp();
//
//            return new ViewExternalState();
//        }
    }

    /**
     * Re-creates a window using the given state and views.
     * 
     * @param viewManager
     * @param state
     * @param views
     * 
     * @return  the window
     */
    private MainWindow recreateWindow(GUISession session, ViewManager viewManager, MainWindowState state,
                                      Map<ViewPosition, Pair<View, ViewExternalState>> views, ViewPosition position) {
        Preconditions.checkNotNull(state);

        // create the main window, and add it to the view manager
        MainWindow window = viewManager.createMainWindow(session, state);
        viewManager.addWindow(window);
        
        // this is necessary, because a split can contain no views/tabs
        if(position.split > 0)
            viewManager.addSplit(window);

        // save the active view position, as it'll be overwritten when the
        // first view tab is added (nothing obvious we can do about that)
        ViewPosition activeViewPos = state.getActiveViewPosition();

        // add all previously created views to the main window
        for (Map.Entry<ViewPosition, Pair<View, ViewExternalState>> entry : views.entrySet()) {
            ViewPosition pos = entry.getKey();
            View view = entry.getValue().getFirst();
            ViewExternalState externalState = entry.getValue().getSecond();

            viewManager.addView(view, window, pos, externalState);
        }

//        try {
            window.selectView(activeViewPos, true);
//        } catch (Exception ex) {
//            // the most likely reason for an exception is that one or more
//            // views could not be restored, so the position would point to
//            // an invalid index
//            logger.error("failed to set a window's active view", ex);
//        }

        window.updateView();

        //FIXME: the return value is actually never used?
        return window;
    }

}
