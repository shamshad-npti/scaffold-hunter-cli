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

package edu.udo.scaffoldhunter.gui;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Iterables;

import edu.udo.scaffoldhunter.model.ViewClassConfig;
import edu.udo.scaffoldhunter.model.ViewInstanceConfig;
import edu.udo.scaffoldhunter.model.ViewState;
import edu.udo.scaffoldhunter.view.MockView;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * @author Dominic Sacré
 *
 */
public class ViewManagerTest {

    private ViewManager viewManager;
    private Window window;

    /**
     * Setup for each test case: create the view manager and add a window.
     */
    @Before
    public void setup() {
        ViewClassRegistry.registerClass(MockView.class, ViewClassConfig.class, ViewInstanceConfig.class,
                                        ViewState.class, null, null);

        viewManager = new ViewManager(null, null);
        window = new MockWindow();

        viewManager.addWindow(window);
    }

    /**
     * Test adding and removing windows.
     */
    @Test
    public void testAddRemoveWindow() {
        assertEquals(1, viewManager.getWindows().size());

        Window w = new MockWindow();
        viewManager.addWindow(w);
        assertEquals(2, viewManager.getWindows().size());
        assertEquals(w, viewManager.getWindows().get(1));

        viewManager.removeWindow(window);
        assertEquals(1, viewManager.getWindows().size());
        assertEquals(w, viewManager.getWindows().get(0));
    }
    
    /**
     * Test adding and removing views.
     */
    @Test
    public void testAddRemoveView() {
        assertEquals(0, Iterables.size(viewManager.getAllViews()));

        View v1 = new MockView();
        viewManager.addView(v1, window);

        assertEquals(1, Iterables.size(viewManager.getAllViews()));
        assertEquals(1, viewManager.getViews(window).size());
        assertEquals(1, viewManager.getViews(window, 0).size());
        assertEquals(1, Iterables.size(viewManager.getAllViewsOfClass(MockView.class)));

        assertEquals(v1, viewManager.getViews(window).get(0));
        assertEquals(v1, viewManager.getViews(window, 0).get(0));

        assertEquals(window, viewManager.getViewWindow(v1));
        assertEquals(new ViewPosition(0, 0), viewManager.getViewPosition(v1));

        View v2 = new MockView();
        viewManager.addView(v2, window);

        assertEquals(2, Iterables.size(viewManager.getAllViews()));
        assertEquals(2, viewManager.getViews(window).size());
        assertEquals(2, viewManager.getViews(window, 0).size());
        assertEquals(2, Iterables.size(viewManager.getAllViewsOfClass(MockView.class)));

        assertEquals(v1, viewManager.getViews(window).get(0));
        assertEquals(v1, viewManager.getViews(window, 0).get(0));
        assertEquals(v2, viewManager.getViews(window).get(1));
        assertEquals(v2, viewManager.getViews(window, 0).get(1));

        assertEquals(window, viewManager.getViewWindow(v2));
        assertEquals(new ViewPosition(0, 1), viewManager.getViewPosition(v2));

        viewManager.removeView(v1);
        assertEquals(1, Iterables.size(viewManager.getAllViews()));
        assertEquals(1, viewManager.getViews(window).size());
        assertEquals(1, viewManager.getViews(window, 0).size());
        assertEquals(1, Iterables.size(viewManager.getAllViewsOfClass(MockView.class)));

        assertEquals(window, viewManager.getViewWindow(v2));
        assertEquals(new ViewPosition(0, 0), viewManager.getViewPosition(v2));
    }

    /**
     * Test adding and removing a window split.
     */
    @Test
    public void testAddRemoveSplit() {
        View v1 = new MockView();
        View v2 = new MockView();

        viewManager.addView(v1, window);
        viewManager.addSplit(window);
        viewManager.addView(v2, window);
        assertEquals(v1, viewManager.getViews(window).get(0));
        assertEquals(v2, viewManager.getViews(window).get(1));

        viewManager.removeSplit(window);
        assertEquals(v1, viewManager.getViews(window).get(0));
        assertEquals(v2, viewManager.getViews(window).get(1));
    }

}
