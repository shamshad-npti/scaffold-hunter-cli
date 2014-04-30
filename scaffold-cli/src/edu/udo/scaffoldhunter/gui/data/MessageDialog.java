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

package edu.udo.scaffoldhunter.gui.data;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.ProgressPanel;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.ProgressListener;

/**
 * A dialog showing messages in a {@link MessageModel}.
 * 
 * @author Henning Garus
 * 
 */
public class MessageDialog extends JDialog implements ActionListener, ProgressListener<Void> {

    /**
     * MessageDialog for Import-Process
     */
    public static final int IMPORT = 0;
    /**
     * MessageDialog for Calculation-Process
     */
    public static final int CALCULATION = 1;

    private static final int PROPERTY_DEFINITION_DEPTH = 3;

    private final MessageModel messageModel = new MessageModel();
    private final JTree messageTree;
    private final JButton button = StandardButtonFactory.createCancelButton(this);
    private final ProgressPanel<Void> progressPanel;

    private boolean finished = false;

    /**
     * Create a new MessageDialog.
     * 
     * @param owner
     *            the dialog's owner
     * @param dialogType
     *            the use of this dialog (use constants IMPORT or CALCULATION)
     * @param cancelListener
     *            an action listener who will be notified when the cancel button
     *            is clicked
     */
    public MessageDialog(Window owner, int dialogType, ActionListener cancelListener) {
        super(owner, getTitle(dialogType), ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        // text is set by progress listener in importer
        // set initial text to get correct height for pack.
        progressPanel = new ProgressPanel<Void>(getProgressPanelText(dialogType));
        button.addActionListener(cancelListener);

        messageTree = buildTree();

        PanelBuilder builder = new PanelBuilder(new FormLayout("f:p:g", "p, 5dlu, f:p:g, 5dlu, p"));
        builder.setDefaultDialogBorder();
        builder.add(progressPanel, CC.xy(1, 1));
        JScrollPane scrollPane = new JScrollPane(messageTree);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        builder.add(scrollPane, CC.xy(1, 3));
        builder.add(ButtonBarFactory.buildCenteredBar(button), CC.xy(1, 5));

        setContentPane(builder.getPanel());
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }
    
    private static String getTitle(int dialogType) {
        switch(dialogType) {
        case IMPORT :
            return I18n.get("DataImport.Merging");
        case CALCULATION :
            return I18n.get("DataCalc.Calculating");
        default:
            return "";
        }
    }
    
    private static String getProgressPanelText(int dialogType) {
        switch(dialogType) {
        case IMPORT :
            return "<html><div align=\"center\">" + I18n.get("DataImport.RunningNthJob")
            + "<br>" + I18n.get("DataImport.ImportingNthMolecule") + "</div></html>";
        case CALCULATION :
            return "<html><div align=\"center\">" + I18n.get("DataCalc.RunningNthJob")
            + "<br>" + I18n.get("DataCalc.CalculatingNthMolecule") + "</div></html>";
        default:
            return "";
        }
    }

    private JTree buildTree() {
        JTree tree = new JTree(messageModel) {
            /*
             * (non-Javadoc)
             * 
             * @see javax.swing.JTree#getPreferredScrollableViewportSize()
             */
            @Override
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(400, 300);
            }
        };
        tree.setRootVisible(false);
        tree.setCellRenderer(new MessageTreeCellRenderer());
        messageModel.addTreeModelListener(new TreeModelListener() {

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                if (e.getTreePath().getPathCount() < PROPERTY_DEFINITION_DEPTH)
                    messageTree.expandPath(e.getTreePath());
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                if (e.getTreePath().getPathCount() < PROPERTY_DEFINITION_DEPTH)
                    messageTree.expandPath(e.getTreePath());
            }

            @Override
            public void treeNodesChanged(TreeModelEvent e) {
            }
        });
        return tree;
    }

    /**
     * Sets the text shown above the progress bar.
     * 
     * @param text
     */
    public void setText(String text) {
        progressPanel.setLabelText(text);
    }

    /**
     * add a new Message to this dialog's message model
     * 
     * @param message
     *            the message to be added
     */
    public void addMessage(Message message) {
        messageModel.addMessage(message);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (finished)
            dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressValue(int)
     */
    @Override
    public void setProgressValue(int progress) {
        progressPanel.setProgressValue(progress);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressBounds(int,
     * int)
     */
    @Override
    public void setProgressBounds(int min, int max) {
        progressPanel.setProgressBounds(min, max);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#setProgressIndeterminate
     * (boolean)
     */
    @Override
    public void setProgressIndeterminate(boolean indeterminate) {
        progressPanel.setProgressIndeterminate(indeterminate);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.gui.util.ProgressListener#finished(java.lang.Object
     * , boolean)
     */
    @Override
    public void finished(Void result, boolean cancelled) {
        if (cancelled) {
            // actual cancelling will be handled by the cancelListener
            button.setEnabled(false);
        } else {
            button.setText(I18n.get("Button.Close"));
            finished = true;
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        }
    }
}
