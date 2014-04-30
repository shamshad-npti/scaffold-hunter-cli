/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Set;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.StructureSVGPanel;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.util.SVGCache;


/**
 * Displays a list showing the molecules in the selection; supports
 * a simple text list and SVG depiction of molecules.
 * 
 * @author Nils Kriege
 */
public class SelectionBrowserPane extends JPanel {
    
    /**
     * Width of the SVG/List panel.
     */
    public static int PANEL_WIDTH = 200;
    /**
     * Height of the SVG/List panel.
     */
    public static int PANEL_HEIGHT = 180;

    private final MainWindow window;
    private final Selection baseSelection;
    JCheckBox listViewCheckBox;
    StructureSVGPanel svgPanel;
    JList<String> selectionList;
    JScrollPane selectionListScrollPane;
    Molecule[] selection;
    int selectedIndex;
    JButton backwardButton;
    JButton deselectButton;
    JButton focusButton;
    JButton forwardButton;
    
    boolean saveSelectedIndex;
        
    /**
     * Creates a selection browser.
     * 
     * @param window the main window
     * @param baseSelection the selection this view is synchronized with 
     * @param svgCache instance to load structure SVGs from
     */
    public SelectionBrowserPane(MainWindow window, Selection baseSelection, SVGCache svgCache) {
        super(new BorderLayout());
        this.window = window;
        this.baseSelection = baseSelection;
        
        listViewCheckBox = new JCheckBox(_("SelectionBrowser.ListView"));
        listViewCheckBox.addItemListener(listViewListener);

        backwardButton = new JButton(backwardAction);
        deselectButton = new JButton(deselectAction);
        focusButton = new JButton(focusAction);
        forwardButton = new JButton(forwardAction);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttons.add(backwardButton);
        buttons.add(deselectButton);
        buttons.add(focusButton);
        buttons.add(forwardButton);
        
        svgPanel = new StructureSVGPanel(svgCache);
        svgPanel.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        svgPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        
        selectionList = new JList<String>();
        selectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionList.getSelectionModel().addListSelectionListener(selectionListListener);
        selectionListScrollPane = new JScrollPane(selectionList);
        selectionListScrollPane.setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        selectionListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        selectionListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        
        add(listViewCheckBox, BorderLayout.NORTH);
        add(svgPanel, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        
        baseSelection.addPropertyChangeListener(Selection.SELECTION_PROPERTY, selectionListener);
        selection = baseSelection.toArray(new Molecule[0]);
        selectedIndex = selection.length-1;
        
        window.addPropertyChangeListener(MainWindow.ACTIVE_VIEW_PROPERTY, activeViewListener);
        window.addPropertyChangeListener(MainWindow.ACTIVE_SUBSET_PROPERTY, activeSubsetListener);
        
        updateGUIElements();
    }
    
    private void updateGUIElements() {
        
        if(selectedIndex >= 0) {
            // index is only saved in selection object, when the initial selection has been completely built
            baseSelection.setSelectedIndex(selectedIndex);
        }
                
        backwardButton.setEnabled(selectedIndex != 0 && selectedIndex != -1);
        deselectButton.setEnabled(selectedIndex != -1);
        
        focusButton.setEnabled(selectedIndex != -1 && window.getActiveView() != null);
        forwardButton.setEnabled(selectedIndex != selection.length-1 && selectedIndex != -1);
        
        focusButton.setEnabled(selectedIndex != -1 &&
                window.getActiveView() != null &&
                window.getActiveView().getSubset().contains(selection[selectedIndex]));
        
        if (listViewCheckBox.isSelected()) {
            selectionList.setModel(new AbstractListModel<String>() {
                @Override
                public int getSize() {
                    return selection.length;
                }
                @Override
                public String getElementAt(int index) {
                    return selection[index].getTitle();
                }
            });
            selectionList.setSelectedIndex(selectedIndex);
        } else {
            if (selectedIndex == -1) {
                svgPanel.updateSVG(null);
            } else {
                svgPanel.updateSVG(selection[selectedIndex]);
            }
        }
    }
    
    private ListSelectionListener selectionListListener = new ListSelectionListener() {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            ListSelectionModel lsm = (ListSelectionModel)e.getSource();
            int newSelectedIndex = lsm.getMinSelectionIndex();
            if (newSelectedIndex != -1 && newSelectedIndex != selectedIndex) {
                selectedIndex = newSelectedIndex;
                updateGUIElements();
            }
        }
    };
    
    private ItemListener listViewListener = new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                // install list view
                remove(svgPanel);
                add(selectionListScrollPane, BorderLayout.CENTER);
            } else {
                // install svg view
                remove(selectionListScrollPane);
                add(svgPanel, BorderLayout.CENTER);
            }
            updateGUIElements();
            validate();
            repaint();
        }
    };
    
    private PropertyChangeListener selectionListener = new GenericPropertyChangeListener<Set<Structure>>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Set<Structure>> ev) {
            Selection newSelection = ((Selection)ev.getSource());
            Structure[] oldSelection = selection;
            selection = ((Selection)ev.getSource()).toArray(new Molecule[0]);
            Structure oldStructure = (selectedIndex == -1) ? null : oldSelection[selectedIndex];

            if (newSelection.contains(oldStructure) || oldStructure == null) {
                if(selectedIndex != baseSelection.getSelectedIndex()) {
                    /*
                     * this happens when the selection is initially built
                     * reads the saved index from the baseSelection object
                     */
                    selectedIndex = Math.min(selection.length - 1, baseSelection.getSelectedIndex());
                }
                else {
                    if (selectedIndex == oldSelection.length-1) {
                        /*
                         * if in the old selection the last molecule was selected, then the last molecule from the new
                         * selection is automatically selected
                         */
                        selectedIndex = selection.length-1;
                    } else {
                        selectedIndex = Arrays.asList(selection).indexOf(oldStructure);
                    }
                }                    
            } else {
                Structure newStructure = oldStructure;
                while (selectedIndex > 0 && !newSelection.contains(newStructure)) {
                    newStructure = oldSelection[--selectedIndex];
                }
                if (newSelection.contains(newStructure)) {
                    selectedIndex = Arrays.asList(selection).indexOf(newStructure);
                } else {
                    selectedIndex = selection.length-1;
                }
            }
            updateGUIElements();
        }
    };
    
    private PropertyChangeListener activeViewListener = new GenericPropertyChangeListener<Object>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Object> ev) {
            updateGUIElements();
        }
    };
    
    private PropertyChangeListener activeSubsetListener = new GenericPropertyChangeListener<Object>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Object> ev) {
            updateGUIElements();
        }
    };
    
    private AbstractAction backwardAction = new AbstractAction() {
        {
            putValues(null, _("Button.Backward"), Resources.getIcon("left.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedIndex--;
            updateGUIElements();
        }
    };
    
    private AbstractAction deselectAction = new AbstractAction() {
        {
            putValues(null, _("SelectionBrowser.Deselect"), Resources.getIcon("close.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            baseSelection.remove(selection[selectedIndex]);
        }
    };
    
    private AbstractAction focusAction = new AbstractAction() {
        {
            putValues(null, _("SelectionBrowser.Focus"), Resources.getIcon("zoom-fit-selection.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
        	assert(window.getActiveView() != null);
            if(window.getActiveView() != null)
                window.getActiveView().focusMolecule(selection[selectedIndex]);
        }
    };

    private AbstractAction forwardAction = new AbstractAction() {
        {
            putValues(null, _("Button.Forward"), Resources.getIcon("right.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            selectedIndex++;
            updateGUIElements();
        }
    };    
        
}
