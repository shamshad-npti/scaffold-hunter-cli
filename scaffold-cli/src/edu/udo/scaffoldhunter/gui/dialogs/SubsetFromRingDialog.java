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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
/**
 * @author Shamshad Alam
 *
 */
public class SubsetFromRingDialog extends JDialog {

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
    private final JComboBox<Integer> ringLevelComboBox;
    private final JButton okButton;
    private final JButton cancelButton;
    private final JLabel errorLabel;
    private String newText = null;
    private Integer ringLevel;
    private int returnValue;
    private boolean allowEmpty = false;


    /**
     * @param parent
     * @param minRingCount 
     * @param maxRingCount 
     */
    public SubsetFromRingDialog(Frame parent, int minRingCount, int maxRingCount) {
        this(parent, minRingCount, maxRingCount, null, null);
    }

    /**
     * @param parent
     * @param minRingCount 
     * @param maxRingCount 
     * @param location
     * @param anchor
     */
    public SubsetFromRingDialog(Frame parent, final int minRingCount, final int maxRingCount, Point location, Anchor anchor) {
        super(parent, _("ScaffoldTreeView.SubsetFromRing.Title"), true);
        
        returnValue = JOptionPane.CANCEL_OPTION;
        
        FormLayout layout = new FormLayout("pref:grow", "");

        DefaultFormBuilder pb = new DefaultFormBuilder(layout);
        pb.setDefaultDialogBorder();
        JLabel description = new JLabel("<html><div width=300>" 
                + _("ScaffoldTreeView.SubsetFromRing.Description") + "</div></html>");
        pb.append(description);
        pb.appendSeparator();
        
        ringLevelComboBox = new JComboBox<Integer>();
        ringLevelComboBox.setModel(new DefaultComboBoxModel<Integer>(){

            @Override
            public Integer getElementAt(int index) {
                return index == 0 ? null : index + minRingCount - 1;
            }
            
            @Override
            public int getSize() {
                return maxRingCount - minRingCount + 2;
            }
        });
        ringLevelComboBox.addItemListener(new ItemListener() {
            
            @Override
            public void itemStateChanged(ItemEvent evt) {
                updateAllowOK();
            }
        });
        pb.append(_("ScaffoldTreeView.SubsetFromRing.SelectRingLevel"), ringLevelComboBox);

        textField = new JTextField(_("ScaffoldTreeView.SubsetFromRing.DefaultSplitName"));
        
        textField.selectAll();

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
        pb.append(_("ScaffoldTreeView.SubsetFromRing.SubsetName"), textField);

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(Color.red);
        
        pb.append(errorLabel);
        okButton = StandardButtonFactory.createOKButton(okAction);
        okButton.setEnabled(false);
        
        cancelButton = StandardButtonFactory.createCancelButton(cancelAction);
        JPanel p = ButtonBarFactory.buildOKCancelBar(okButton, cancelButton);
        pb.append(p);
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
            ringLevel = (Integer)ringLevelComboBox.getSelectedItem();
            returnValue = JOptionPane.OK_OPTION;
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
            okButton.setEnabled(textField.getText().length() > 0 && ringLevelComboBox.getSelectedItem() != null);
            if(ringLevelComboBox.getSelectedItem() == null) {
                errorLabel.setText(_("ScaffoldTreeView.SubsetFromRing.ErrorSelectRingLevel"));
            } else if(textField.getText().isEmpty()) {
                errorLabel.setText(_("ScaffoldTreeView.SubsetFromRing.ErrorSubsetName"));
            } else {
                errorLabel.setText(" ");                
            }
        }
    }

    /**
     * @return Dialog Result
     */
    public int showDialog() {
        setVisible(true);
        return returnValue;
    }
    /**
     * @return  the new text
     */
    public String getNewText() {
        return newText;
    }
    
    /**
     * @return the ringLevel
     */
    public Integer getRingLevel() {
        return ringLevel;
    }

}
