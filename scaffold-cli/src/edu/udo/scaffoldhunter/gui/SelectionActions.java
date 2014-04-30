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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.KeyStroke;

import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.gui.dialogs.RenameDialog;
import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Dominic Sacré
 *
 */
public class SelectionActions {

    private final SubsetController subsetManager;
    private final Selection selection;
    private final BannerPool bannerPool;
    private final MainWindow window;


    /**
     * @param session
     *          the GUI session
     * @param window
     *          the main window
     */
    public SelectionActions(GUISession session, MainWindow window) {
        this.subsetManager = session.getSubsetController();
        this.selection = session.getSelection();
        this.bannerPool = session.getBannerPool();
        this.window = window;
    }


    /**
     * @return  an action that selects all molecules.
     */
    public AbstractAction getSelectAll() {
        return selectAll;
    }

    private AbstractAction selectAll = new AbstractAction() {
        {
            putValues(_("Main.Selection.SelectAll"), null,
                    KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            selection.addAll(subsetManager.getRootSubset());
        }
    };

    /**
     * @return  an action that deselects all molecules.
     */
    public AbstractAction getDeselectAll() {
        return deselectAll;
    }

    private AbstractAction deselectAll = new AbstractAction() {
        {
            putValues(_("Main.Selection.DeselectAll"), null,
                    KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            selection.clear();
        }
    };

    /**
     * @return  an action that inverts the selection
     */
    public AbstractAction getInvert() {
        return invert;
    }

    private AbstractAction invert = new AbstractAction() {
        {
            putValues(_("Main.Selection.Invert"), null, KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            Set<Molecule> s = Sets.newHashSet(selection);
            selection.addAll(subsetManager.getRootSubset());
            selection.removeAll(s);
        }
    };

    /**
     * @return  an action that confines the selection to the current subset
     */
    public AbstractAction getConfineToView() {
        return confineToView;
    }

    private AbstractAction confineToView = new AbstractAction() {
        {
            putValues(_("Main.Selection.ConfineToView"), _("Main.Selection.ConfineToView.Description"),
                    KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            Subset current = window.getActiveView().getSubset();
            selection.retainAll(current);
        }
    };

    /**
     * @return  an action that makes a subset from the entire selection
     */
    public AbstractAction getMakeSubset() {
        return makeSubset;
    }

    private AbstractAction makeSubset = new AbstractAction() {
        {
            putValues(_("Main.Selection.MakeSubset"), _("Main.Selection.MakeSubset.Description"),
                    KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
//            Point location = getLocationOnScreen();
//            location.translate(-25, -25);

            RenameDialog dlg = new RenameDialog(window.getFrame(), _("Subset.NewSubsetName"),
                    _("Subset.DefaultSubsetName")/*, location, RenameDialog.Anchor.TOP_RIGHT*/);
            dlg.setVisible(true);

            final String newTitle = dlg.getNewText();

            if (newTitle != null) {
                Subset newSubset = subsetManager.createSubset(subsetManager.getRootSubset(), newTitle, selection);
                subsetManager.addSubset(newSubset);
            }
        }
    };

    /**
     * @return  an action that makes a subset from the intersection of the
     *          selection and the active subset
     */
    public AbstractAction getMakeViewSubset() {
        return makeViewSubset;
    }

    private AbstractAction makeViewSubset = new AbstractAction() {
        {
            putValues(_("Main.Selection.MakeViewSubset"), _("Main.Selection.MakeViewSubset.Description"),
                    KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
//            Point location = getLocationOnScreen();
//            location.translate(-25, -25);

            RenameDialog dlg = new RenameDialog(window.getFrame(), _("Subset.NewSubsetName"),
                    _("Subset.DefaultSubsetName")/*, location, RenameDialog.Anchor.TOP_RIGHT*/);
            dlg.setVisible(true);

            final String newTitle = dlg.getNewText();

            if (newTitle != null) {
                Subset current = window.getActiveView().getSubset();
    
                Set<Molecule> inView = window.getActiveView().getSubset();

                Set<Molecule> intersection = Sets.intersection(selection, inView);

                Subset newSubset = subsetManager.createSubset(current, newTitle, intersection);
                subsetManager.addSubset(newSubset);
            }
        }
    };

    /**
     * @return  an action that adds public banners to the selected molecules.
     */
    public AbstractAction getAddPublicBanners() {
        return addPublicBanners;
    }

    private AbstractAction addPublicBanners = new AbstractAction() {
        {
            putValues(_("Main.Selection.AddPublicBanners"), null,
                      Resources.getIcon("banner-public.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            for (Molecule m : selection) {
                bannerPool.addBanner(m, false);
            }
        }
    };

    /**
     * @return  an action that adds private banners to the selected molecules.
     */
    public AbstractAction getAddPrivateBanners() {
        return addPrivateBanners;
    }

    private AbstractAction addPrivateBanners = new AbstractAction() {
        {
            putValues(_("Main.Selection.AddPrivateBanners"), null,
                      Resources.getIcon("banner-private.png"), null, null);
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            for (Molecule m : selection) {
                bannerPool.addBanner(m, true);
            }
        }
    };

    /**
     * @return  an action that removes public banners from the selected molecules.
     */
    public AbstractAction getRemovePublicBanners() {
        return removePublicBanners;
    }

    private AbstractAction removePublicBanners = new AbstractAction() {
        {
            putValues(_("Main.Selection.RemovePublicBanners"));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            for (Molecule m : selection) {
                bannerPool.removeBanner(m, false);
            }
        }
    };

    /**
     * @return  an action that removes private banners from the selected molecules.
     */
    public AbstractAction getRemovePrivateBanners() {
        return removePrivateBanners;
    }

    private AbstractAction removePrivateBanners = new AbstractAction() {
        {
            putValues(_("Main.Selection.RemovePrivateBanners"));
        }
        @Override
        public void actionPerformed(ActionEvent ev) {
            for (Molecule m : selection) {
                bannerPool.removeBanner(m, true);
            }
        }
    };

}
