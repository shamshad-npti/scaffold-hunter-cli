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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.gui.SubsetController;
import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.model.BannerPool;
import edu.udo.scaffoldhunter.model.Selection;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.model.util.Scaffolds;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * A context menu for the Scaffold Tree View
 * 
 * @author Henning Garus
 * 
 */
public class ContextMenu extends JPopupMenu {

    private final VCanvas canvas;
    private final ScaffoldNode vnode;
    private final BannerPool bannerPool;
    private final SubsetController subsetManager;
    private final Subset subset;
    private final Selection selection;

    /**
     * 
     * @param canvas
     *            the canvas of the treeview
     * @param vnode
     *            the clicked vnode
     * @param bannerPool
     *            the banner pool
     * @param subsetManager
     *            the subset manager
     * @param subset
     *            the current subset
     * @param selection
     *            the current selection 
     */
    public ContextMenu(VCanvas canvas, ScaffoldNode vnode, BannerPool bannerPool, SubsetController subsetManager, Subset subset, Selection selection) {
        super();
        this.canvas = canvas;
        this.vnode = vnode;
        this.bannerPool = bannerPool;
        this.subsetManager = subsetManager;
        this.subset = subset;
        this.selection = selection;

        addAll(createScaffoldItems());
        addSeparator();

        addAll(createSubtreeItems());
        addSeparator();

        addAll(createBannerItems());
    }

    private void addAll(List<JMenuItem> items) {
        for (JMenuItem item : items) {
            if (item == null) {
                addSeparator();
            } else {
                add(item);
            }
        }
    }

    private void disableLast(List<JMenuItem> items) {
        items.get(items.size() - 1).setEnabled(false);
    }

    private List<JMenuItem> createScaffoldItems() {
        List<JMenuItem> items = Lists.newArrayList();

        if (vnode.isExpandable()) {
            items.add(new JMenuItem(new AbstractAction(_("ScaffoldTreeView.OpenChildren"), Resources
                    .getIcon("plus.png")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    canvas.expand(vnode);
                }
            }));
        }

        if (!vnode.getTreeChildren().isEmpty()) {
            items.add(new JMenuItem(new AbstractAction(_("ScaffoldTreeView.CloseChildren"), Resources
                    .getIcon("minus.png")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    canvas.reduce(vnode);
                }
            }));
        }

        if (!items.isEmpty())
            items.add(null);

        items.add(new JMenuItem(new AbstractAction(_("ScaffoldTreeView.ExpandSubtree")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.expandSubtree(vnode);
            }
        }));
        if (!vnode.isSubtreeExpandable()) {
            disableLast(items);
        }

        items.add(new JMenuItem(new AbstractAction(_("ScaffoldTreeView.ExpandNextLevel"), Resources
                .getIcon("expand-next-level.png")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.expandNextLevel(vnode);
            }
        }));

        items.add(new JMenuItem(new AbstractAction(_("ScaffoldTreeView.CollapseToLevel"), Resources
                .getIcon("reduce-to-level.png")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                canvas.reduceToThisLevel(vnode);
            }
        }));
        
        items.add(new JMenuItem(new TreeViewActions.ExpandAllNodesAction(canvas)));

        return items;
    }

    private List<JMenuItem> createSubtreeItems() {
        List<JMenuItem> items = Lists.newArrayList();

        items.add(new JMenuItem(new AbstractAction(_("ScaffoldTreeView.DeselectSubtree")) {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                Iterable<Scaffold> subtreeScaffolds = Scaffolds.getSubtreePreorderIterable(vnode.getScaffold());
                selection.removeAll(Scaffolds.getMolecules(subtreeScaffolds));
            }
        }));
        
        items.add(new JMenuItem(new AbstractAction(_("ScaffoldTreeView.SelectSubtree")) {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                Iterable<Scaffold> subtreeScaffolds = Scaffolds.getSubtreePreorderIterable(vnode.getScaffold());
                selection.addAll(Scaffolds.getMolecules(subtreeScaffolds));
            }
        }));
        
        items.add(new JMenuItem(new AbstractAction(_("ScaffoldTreeView.SubsetFromSubtree")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                Iterable<Molecule> subtreeMolecules = Scaffolds.getMolecules(Scaffolds.getSubtreePreorderIterable(vnode
                        .getScaffold()));
                Subset newSubset = subsetManager.createSubset(subset,
                        _("ScaffoldTreeView.SubtreeSubsetName", vnode.getScaffold().getTitle()), subtreeMolecules);
                subsetManager.addSubset(newSubset);
            }
        }));

        return items;
    }

    private List<JMenuItem> createBannerItems() {
        List<JMenuItem> items = Lists.newArrayList();

        items.add(new JMenuItem(new AbstractAction(_("Banner.TogglePrivateBanner"), Resources
                .getImageIcon("images/banner_private.png")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                bannerPool.toggleBanner(vnode.getScaffold(), true);
            }
        }));

        items.add(new JMenuItem(new AbstractAction(_("Banner.TogglePublicBanner"), Resources
                .getImageIcon("images/banner_public.png")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                bannerPool.toggleBanner(vnode.getScaffold(), false);
            }
        }));

        return items;
    }

}
