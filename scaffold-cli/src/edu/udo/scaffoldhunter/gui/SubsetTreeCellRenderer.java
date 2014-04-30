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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTree;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.udo.scaffoldhunter.model.db.Subset;
import edu.udo.scaffoldhunter.util.StringEscapeUtils;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * @author Dominic Sacré
 */
public class SubsetTreeCellRenderer extends DefaultTreeCellRenderer {

    private final ViewManager viewManager;
    private final MainWindow window;

    private final Font normalFont;
    private final Font boldFont;

    private final Color highlightColor;
    private final Color foregroundColor;

    private Subset highlightedSubset = null;
    private int highlightCounter;

    /**
     * Constructs a new subset tree cell renderer.
     * 
     * @param viewManager
     *          the view manager
     * @param window
     *          the main window the tree belongs to
     */
    public SubsetTreeCellRenderer(ViewManager viewManager, MainWindow window) {
        super();

        this.viewManager = viewManager;
        this.window = window;

        setLeafIcon(null);
        setClosedIcon(null);
        setOpenIcon(null);

        normalFont = UIManager.getFont("Tree.font");
        boldFont = normalFont.deriveFont(Font.BOLD);

        highlightColor = new Color(1.0f, 0.0f, 0.0f);
        foregroundColor = UIManager.getColor("Tree.textForeground");
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        Subset subset = (Subset)value;

        if (window.getActiveView() != null && subset == window.getActiveView().getSubset()) {
            setFont(boldFont);
        } else {
            setFont(normalFont);
        }

        if (subset == highlightedSubset) {
            float[] rgb1 = new float[3];
            float[] rgb2 = new float[3];

            float r = highlightCounter < 20 ? 0.f : (highlightCounter - 20) / 5.f;
            float ir = 1.f - r;

            foregroundColor.getRGBColorComponents(rgb1);
            highlightColor.getRGBColorComponents(rgb2);

            Color color = new Color(
                    rgb1[0] * r + rgb2[0] * ir,
                    rgb1[1] * r + rgb2[1] * ir,
                    rgb1[2] * r + rgb2[2] * ir
            );

            setForeground(color);
        }

        setToolTipText(makeToolTipText(subset));

        return this;
    }

    /**
     * Highlights a subset for a few seconds, then fades it out.
     * 
     * @param tree
     *          the tree
     * @param subset
     *          the subset to be highlighted
     */
    public void startHighlighting(final JTree tree, final Subset subset) {
        highlightedSubset = subset;

        final Timer timer = new Timer(100, null);

        highlightCounter = 0;

        timer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (++highlightCounter == 25) {
                    highlightedSubset = null;
                    timer.stop();
                }
                tree.repaint();
            }
        });
        timer.start();
    }

    private String makeToolTipText(Subset subset) {
        boolean hasComment = (subset.getComment() != null && subset.getComment() != "");

        StringBuilder sb = new StringBuilder();

        for (Window w : viewManager.getWindows()) {
            boolean windowAdded = false;

            for (View v : viewManager.getViews(w)) {
                if (v.getSubset() == subset) {
                    if (!windowAdded) {
                        sb.append(_("Subset.Tooltip.WindowDescription",
                                w.getNumber()
                        ));
                        windowAdded = true;
                    }

                    sb.append(_("Subset.Tooltip.ViewDescription",
                            StringEscapeUtils.escapeHTML(viewManager.getViewState(v).getTabTitle()),
                            ViewClassRegistry.getClassName(v.getClass())
                    ));
                }
            }
        }

        if (sb.length() == 0) {
            sb.append(_("Subset.Tooltip.SubsetClosed"));
        }

        return _("Subset.Tooltip",
                StringEscapeUtils.escapeHTML(subset.getTitle()),
                subset.size(),
                subset.getCreationDate(),
                hasComment ? StringEscapeUtils.escapeHTML(subset.getComment()) : _("Subset.Tooltip.EmptyComment"),
                sb.toString()
        );
    }

}
