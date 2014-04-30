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

import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import org.apache.batik.transcoder.TranscoderException;

import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.gui.util.ProgressWorkerUtil;
import edu.udo.scaffoldhunter.gui.util.WorkerExceptionListener;
import edu.udo.scaffoldhunter.util.FileType;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.util.ExportDialog;
import edu.udo.scaffoldhunter.view.util.SVGGenerator;

/**
 * A collection of Actions dealing with the Scaffold Tree.
 * 
 * @author Henning Garus
 */
public class TreeViewActions {

    static class RadialLayoutAction extends AbstractAction {
        private final ScaffoldTreeView view;

        RadialLayoutAction(ScaffoldTreeView view) {
            super(I18n.get("Layout.Radial"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Layout.Radial"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('r'));
            this.view = view;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            view.updateLayout(VLayoutsEnum.RADIAL_LAYOUT);
        }
    }

    static class BalloonLayoutAction extends AbstractAction {
        private final ScaffoldTreeView view;

        public BalloonLayoutAction(ScaffoldTreeView view) {
            super(I18n.get("Layout.Balloon"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Layout.Balloon"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('b'));
            this.view = view;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            view.updateLayout(VLayoutsEnum.BALLOON_LAYOUT);
        }
    }

    static class LinearLayoutAction extends AbstractAction {
        private final ScaffoldTreeView view;

        public LinearLayoutAction(ScaffoldTreeView view) {
            super(I18n.get("Layout.Linear"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Layout.Linear"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('l'));
            this.view = view;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            view.updateLayout(VLayoutsEnum.LINEAR_LAYOUT);
        }
    }

    static class ZoomInAction extends AbstractAction {
        private final VCanvas canvas;

        public ZoomInAction(VCanvas canvas) {
            super(I18n.get("Menu.Edit.Zoomin"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomin"));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-in.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-in.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('+'));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int x = canvas.getHeight() / 2;
            int y = canvas.getWidth() / 2;
            Point2D p = canvas.getCamera().localToView(new Point(x, y));
            canvas.zoomIn(p);
        }
    }

    static class ZoomComboAction extends AbstractAction {

        private final VCanvas canvas;

        public ZoomComboAction(VCanvas canvas) {
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            @SuppressWarnings("unchecked")
            JComboBox<String> zoomCombo = (JComboBox<String>) e.getSource();
            String item = (String) zoomCombo.getSelectedItem();
            if (item.endsWith("%")) {
                StringBuffer buffer = new StringBuffer(item);
                buffer.deleteCharAt(item.length() - 1);
                item = buffer.toString();
            }
            Double value = new Double(item);
            int percentage = value.intValue();
            // TODO see TODO in VZoomHandler, if constant leave it, otherwise
            // make it configurable
            if (percentage > 1000) {
                percentage = 1000;
            }
            zoomCombo.getEditor().setItem(percentage + "%");
            int x = canvas.getHeight() / 2;
            int y = canvas.getWidth() / 2;
            canvas.setZoomFactor(new Point(x, y), percentage);
        }
    }

    static class ZoomOutAction extends AbstractAction {
        private final VCanvas canvas;

        public ZoomOutAction(VCanvas canvas) {
            super(I18n.get("Menu.Edit.Zoomout"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.Zoomout"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('-'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-out.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-out.png"));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int x = canvas.getHeight() / 2;
            int y = canvas.getWidth() / 2;
            Point2D p = canvas.getCamera().localToView(new Point(x, y));
            canvas.zoomOut(p);
        }
    }

    static class ZoomToFitAction extends AbstractAction {
        private final VCanvas canvas;

        public ZoomToFitAction(VCanvas canvas) {
            super(I18n.get("Menu.Edit.ZoomToFit"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.Edit.ZoomToFit"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('0'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-best-fit.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-best-fit.png"));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.zoomToOverview();
        }
    }

    static class ZoomToSelection extends AbstractAction {
        private final VCanvas canvas;

        public ZoomToSelection(VCanvas canvas) {
            super(I18n.get("ScaffoldTreeView.Menu.ZoomToSelection"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("ScaffoldTreeView.Menu.ZoomToSelection"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('s'));
            putValue(Action.SMALL_ICON, Resources.getIcon("zoom-fit-selection.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("zoom-fit-selection.png"));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.zoomToSelectionOverview(false);
        }
    }

    static class IncreaseRadiusAction extends AbstractAction {
        private final VCanvas canvas;

        public IncreaseRadiusAction(VCanvas canvas) {
            super(I18n.get("Menu.View.IncreaseRadius"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.IncreaseRadius"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("increase-radius.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_UP, InputEvent.CTRL_DOWN_MASK));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.getVTree().getLayout().updateRadii(1.1);
        }
    }

    static class DecreaseRadiusAction extends AbstractAction {
        private final VCanvas canvas;

        public DecreaseRadiusAction(VCanvas canvas) {
            super(I18n.get("Menu.View.DecreaseRadius"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.DecreaseRadius"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("decrease-radius.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, InputEvent.CTRL_DOWN_MASK));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.getVTree().getLayout().updateRadii(1/1.1);
        }
    }

    static class FixRadiiAction extends AbstractAction {
        private final ScaffoldTreeView view;

        public FixRadiiAction(ScaffoldTreeView view) {
            super(I18n.get("Menu.View.FixRadii"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.FixRadii"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("fix-radius.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke('f'));
            this.view = view;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            view.setFixedRadii(!view.getState().isFixedRadii());
        }
    }
    
    static class ResetRadiusAction extends AbstractAction {
        private final VCanvas canvas;

        public ResetRadiusAction(VCanvas canvas) {
            super(I18n.get("Menu.View.ResetRadius"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.ResetRadius"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("reset-radius.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.getVTree().getLayout().resetRadii();
        }
    }

    static class ExpandAllNodesAction extends AbstractAction {
        private final VCanvas canvas;

        public ExpandAllNodesAction(VCanvas canvas) {
            super(I18n.get("Menu.View.ExpandAllNodes"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.ExpandAllNodes"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("expand-all-nodes.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
            // TODO add small icon
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.expandAll(true);
        }
    }

    static class ExpandToDefaultLevelAction extends AbstractAction {
        private final VCanvas canvas;
        private final ScaffoldTreeViewClassConfig config;

        public ExpandToDefaultLevelAction(VCanvas canvas, ScaffoldTreeViewClassConfig config) {
            super(I18n.get("Menu.View.ExpandToDefaultLevel"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.ExpandToDefaultLevel"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("expand-to-default.png"));
            putValue(Action.ACCELERATOR_KEY,
                    KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK));
            this.canvas = canvas;
            this.config = config;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // TODO I think that will be very ressource intensive with big
            // datasets -> find a better way
            for (VNode n : canvas.getVTree().getNodesOnLevel(config.getInitiallyOpenRings())) {
                canvas.getVTree().reduce(n);
            }
            canvas.getVTree().buildBranch(canvas.getVTree().getRoot(), config.getInitiallyOpenRings());
        }
    }

    static class MaximizeNodeAction extends AbstractAction {
        private final VCanvas canvas;

        public MaximizeNodeAction(VCanvas canvas) {
            super(I18n.get("Menu.View.MaximizeNode"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.MaximizeNode"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("increase-node-size.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, 0));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.scaleNode(canvas.getCursorVNode(), 1.2);
        }
    }

    static class MinimizeNodeAction extends AbstractAction {
        private final VCanvas canvas;

        public MinimizeNodeAction(VCanvas canvas) {
            super(I18n.get("Menu.View.MinimizeNode"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.MinimizeNode"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("decrease-node-size.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, 0));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.scaleNode(canvas.getCursorVNode(), 0.8);
        }
    }

    static class NormalizeNodeAction extends AbstractAction {
        private final VCanvas canvas;

        public NormalizeNodeAction(VCanvas canvas) {
            super(I18n.get("Menu.View.NormalizeNode"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.NormalizeNode"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("normalize-node-size.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, 0));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.normalizeNode(canvas.getCursorVNode());
        }
    }

    static class MaximizeSelectedNodesAction extends AbstractAction {
        private final VCanvas canvas;

        public MaximizeSelectedNodesAction(VCanvas canvas) {
            super(I18n.get("Menu.View.MaximizeSelectedNodes"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.MaximizeSelectedNodes"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("increase-selected-node-size.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.ALT_DOWN_MASK));

            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.scaleSelectedNodes(1.2);
        }
    }

    static class MinimizeSelectedNodesAction extends AbstractAction {
        private final VCanvas canvas;

        public MinimizeSelectedNodesAction(VCanvas canvas) {
            super(I18n.get("Menu.View.MinimizeSelectedNodes"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.MinimizeSelectedNodes"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("decrease-selected-node-size.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.ALT_DOWN_MASK));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.scaleSelectedNodes(0.8);
        }
    }

    static class NormalizeSelectedNodesAction extends AbstractAction {
        private final VCanvas canvas;

        public NormalizeSelectedNodesAction(VCanvas canvas) {
            super(I18n.get("Menu.View.NormalizeSelectedNodes"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.NormalizeSelectedNodes"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("normalize-selected-node-size.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.normalizeSelectedNodes();
        }
    }

    static class NormalizeAllNodesAction extends AbstractAction {
        private final VCanvas canvas;

        public NormalizeAllNodesAction(VCanvas canvas) {
            super(I18n.get("Menu.View.NormalizeAllNodes"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Menu.View.NormalizeAllNodes"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("normalize-all-nodes.png"));
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, 0));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            canvas.normalizeAllNodes();
        }
    }

    static class ExportCanvasAction extends AbstractAction {
        private final VCanvas canvas;

        public ExportCanvasAction(VCanvas canvas) {
            super(I18n.get("Export.ImageDescription"));
            putValue(Action.SHORT_DESCRIPTION, I18n.get("Export.ImageDescription"));
            putValue(Action.SMALL_ICON, Resources.getIcon("save.png"));
            putValue(Action.LARGE_ICON_KEY, Resources.getLargeIcon("save.png"));
            this.canvas = canvas;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Window parent = (Window) canvas.getTopLevelAncestor();
            final ExportDialog d = new ExportDialog(parent, canvas.getExportScreenDimension(),
                    canvas.getExportAllDimension());
            int ret = d.showExportDialog();
            if (ret == JFileChooser.CANCEL_OPTION)
                return;
            ProgressWorker<Void, Void> worker = new ProgressWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    Dimension dim = d.getDimension();
                    FileType type = d.getFileType();
                    File file = d.getFile();
                    SVGGenerator gen = canvas.exportSVG(dim, d.isExportAll());

                    try {
                        gen.transcode(file, type);
                    } catch (TranscoderException e) {
                        if (file.exists())
                            file.delete();
                        throw e;
                    }
                    return null;
                }
            };
            worker.addExceptionListener(new WorkerExceptionListener() {
                @Override
                public ExceptionHandlerResult exceptionThrown(Throwable e) {
                    if (e instanceof TranscoderException) {
                        JOptionPane.showMessageDialog(canvas, I18n.get("Export.Error"));
                        return ExceptionHandlerResult.STOP;
                    } else {
                        return ExceptionHandlerResult.NOT_HANDLED;
                    }
                }
            });
            ProgressWorkerUtil.executeWithProgressDialog(parent, I18n.get("Export.Title"), I18n.get("Export.Progress"),
                    ModalityType.APPLICATION_MODAL, worker);
        }
    }

    static class ToggleDetailsNodesAction extends AbstractAction {
        private final VCanvas canvas;
        private final ScaffoldTreeViewState state;

        public ToggleDetailsNodesAction(VCanvas canvas, ScaffoldTreeViewState state) {
            super(I18n.get("ScaffoldTreeView.ShowDetailsNodes"));
            this.canvas = canvas;
            this.state = state;
            putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.CTRL_DOWN_MASK));
            putValue(SHORT_DESCRIPTION, I18n.get("ScaffoldTreeView.ShowDetailsNodes"));
            putValue(LARGE_ICON_KEY, Resources.getLargeIcon("show-molecules.png"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            boolean toggled = ((AbstractButton) e.getSource()).getModel().isSelected();
            canvas.getVTree().setShowScaffoldDetailsNodes(toggled);
            state.setShowDetailsNodes(toggled);
        }
    }
}
