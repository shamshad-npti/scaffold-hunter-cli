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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.google.common.collect.Sets;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeEvent;
import edu.udo.scaffoldhunter.util.GenericPropertyChangeListener;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.util.SVGCache;

/**
 * @author Dominic Sacré
 */
public class SelectionPane extends JPanel {

    private final Selection selection;
    private final MainWindow window;

    private final SelectionBrowserPane selectionIteratorPane;
    private final JLabel selectionSize;
    private final JLabel selectionInView;
    private final JButton makeSubsetButton;
    private final JButton makeViewSubsetButton;
    private final JButton clearAllButton;
    
    /**
     * the selection size
     */
    public static final String SELECTION_SIZE = "selectionSize";

    /**
     * @param selection
     *          the selection
     * @param window
     *          the main window
     * @param selectionActions
     *          the selection actions
     * @param dbManager
     *          the database manager for reloading SVGs
     */
    public SelectionPane(Selection selection, MainWindow window, SelectionActions selectionActions, DbManager dbManager) {
        super(new FormLayout(
            "left:pref, 8dlu, pref:grow",
            "pref, 6dlu, pref, 2dlu, pref, 6dlu, pref, 2dlu, pref, 6dlu, pref"
        ));

        this.selection = selection;
        this.window = window;

        setBorder(new EmptyBorder(8, 3, 4, 3));

        CellConstraints cc = new CellConstraints();
        
        // selection browser
        SVGCache svgCache = new SVGCache(dbManager);
        selectionIteratorPane = new SelectionBrowserPane(window, selection, svgCache);
        add(selectionIteratorPane, cc.xyw(1, 1, 3));


        // selection label
        add(new JLabel(_("Selection.Label")), cc.xy(1, 3));
        selectionSize = new JLabel("0");
        selectionSize.setFont(selectionSize.getFont().deriveFont(Font.BOLD));
        add(selectionSize, cc.xy(3, 3));

        makeSubsetButton = new JButton(_("Selection.MakeSubset"), Resources.getIcon("make_subset_arrow.png"));
        makeSubsetButton.setToolTipText(_("Main.Selection.MakeSubset.Description"));
        makeSubsetButton.addActionListener(selectionActions.getMakeSubset());
        makeSubsetButton.setEnabled(false);
        add(makeSubsetButton, cc.xyw(1, 5, 3));

        add(new JLabel(_("Selection.InViewLabel")), cc.xy(1, 7));
        selectionInView = new JLabel("0");
        selectionInView.setFont(selectionInView.getFont().deriveFont(Font.BOLD));
        add(selectionInView, cc.xy(3, 7));

        makeViewSubsetButton = new JButton(_("Selection.MakeSubset"), Resources.getIcon("make_subset_arrow.png"));
        makeViewSubsetButton.setToolTipText(_("Main.Selection.MakeViewSubset.Description"));
        makeViewSubsetButton.addActionListener(selectionActions.getMakeViewSubset());
        makeViewSubsetButton.setEnabled(false);
        add(makeViewSubsetButton, cc.xyw(1, 9, 3));

        clearAllButton = new JButton(_("Selection.Clear"));
        clearAllButton.addActionListener(selectionActions.getDeselectAll());
        add(clearAllButton, cc.xyw(1, 11, 3));

        selection.addPropertyChangeListener(Selection.SELECTION_PROPERTY, selectionListener);

        window.addPropertyChangeListener(MainWindow.ACTIVE_SUBSET_PROPERTY, activeSubsetChangeListener);
    }

    /**
     * Performs cleanup to ensure that the object can be garbage-collected.
     */
    public void destroy() {
        selection.removePropertyChangeListener(Selection.SELECTION_PROPERTY, selectionListener);
    }


    private PropertyChangeListener selectionListener = new GenericPropertyChangeListener<Set<Structure>>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Set<Structure>> ev) {
            int n = ev.getNewValue().size();
            selectionSize.setText(Integer.toString(n));
            makeSubsetButton.setEnabled(n > 0);

            updateSelectionInView();
            
            firePropertyChange(SELECTION_SIZE, 0, 1); // values are not important, so make sure they are different
        }
    };

    private PropertyChangeListener activeSubsetChangeListener = new GenericPropertyChangeListener<Subset>() {
        @Override
        public void propertyChange(GenericPropertyChangeEvent<Subset> ev) {
            updateSelectionInView();
        }
    };

    private void updateSelectionInView() {
        int selectedInView;

        if (window.getActiveView() != null) {
            Set<Molecule> inView = window.getActiveView().getSubset();
            Set<Molecule> intersection = Sets.intersection(selection, inView);
            selectedInView = intersection.size();
        } else {
            selectedInView = 0;
        }

        selectionInView.setText(Integer.toString(selectedInView));

        makeViewSubsetButton.setEnabled(selectedInView > 0);
    }

}
