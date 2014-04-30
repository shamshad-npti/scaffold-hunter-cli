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

package edu.udo.scaffoldhunter.gui.dialogs;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;

/**
 * @author Dominic Sacré
 *
 */
public class RenameDialog extends JDialog {

    /**
     * Defines which corner of this dialog window the location refers to
     */
    public enum Anchor {
        /**
         * Location is the top-left corner
         */
        TOP_LEFT,
        /**
         * Location is the top-right corner
         */
        TOP_RIGHT,
    }

    private final JTextField textField;
    private final JButton okButton;
    private final JButton cancelButton;

    private String newText = null;

    private boolean allowEmpty = false;


    /**
     * @param parent
     * @param title
     * @param currentText
     */
    public RenameDialog(Frame parent, String title, String currentText) {
        this(parent, title, currentText, null, null);
    }

    /**
     * @param parent
     * @param title
     * @param currentText 
     * @param location
     * @param anchor
     */
    public RenameDialog(Frame parent, String title, String currentText, Point location, Anchor anchor) {
        super(parent, title, true);

        FormLayout layout = new FormLayout("pref:grow", "pref, 8dlu, pref");
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();

        textField = new JTextField(currentText);
        textField.selectAll();
        int textHeight = (int) textField.getPreferredSize().getHeight();
        textField.setPreferredSize(new Dimension(400, textHeight));

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(DocumentEvent e) {
                updateAllowOK();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateAllowOK();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        pb.add(textField, cc.xy(1, 1));

        okButton = StandardButtonFactory.createOKButton(okAction);
        cancelButton = StandardButtonFactory.createCancelButton(cancelAction);

        JPanel p = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
        pb.add(p, cc.xy(1, 3));
        getRootPane().setDefaultButton(okButton);

        // allow escape key to close the dialog
        getRootPane().registerKeyboardAction(cancelAction,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        setContentPane(pb.getPanel());
        setMinimumSize(new Dimension(280, 0));
        pack();
        setResizable(false);

        if (location == null) {
            setLocationRelativeTo(parent);
        } else {
            switch (anchor) {
            case TOP_LEFT:
                // nothing to do
                break;
            case TOP_RIGHT:
                location.translate(-getWidth(), 0);
                break;
            }
    
            // make sure the dialog stays on screen
            Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
            location.x = Math.min(location.x, screen.width - getWidth());
            location.y = Math.min(location.y, screen.height - getHeight());
    
            setLocation(location);
        }
    }

    /**
     * @param allowEmpty
     *          if true this dialog will accept empty strings
     */
    public void setAllowEmpty(boolean allowEmpty) {
        this.allowEmpty = allowEmpty;
    }

    private ActionListener okAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ev) {
            newText = textField.getText();
            dispose();
        }
    };

    private ActionListener cancelAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent ev) {
            dispose();
        }
    };

    private void updateAllowOK() {
        if (!allowEmpty) {
            okButton.setEnabled(textField.getText().length() > 0);
        }
    }

    /**
     * @return  the new text
     */
    public String getNewText() {
        return newText;
    }

}
