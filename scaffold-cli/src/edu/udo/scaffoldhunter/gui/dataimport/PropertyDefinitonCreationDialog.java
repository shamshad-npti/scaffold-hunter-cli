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

package edu.udo.scaffoldhunter.gui.dataimport;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.collect.ImmutableSet;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.CustomComboBoxRenderer;
import edu.udo.scaffoldhunter.gui.util.SelectAllOnFocus;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.model.PropertyType;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * A simple dialog for creating new Property Definitions. The dialog will insist
 * on a title being provided for the property definition, if no title has been
 * entered prior to clicking OK, the dialog will stay open and the title
 * textfield will be surrounded by a red border.
 * 
 * @author Henning Garus
 * 
 */
public class PropertyDefinitonCreationDialog extends JDialog {

    private final JTextField titleField = new JTextField();
    private final JTextArea descriptionArea = getDescriptionArea();
    private final JComboBox type = new JComboBox(PropertyType.getImportableTypes());;
    private PropertyDefinition propertyDefinition = null;

    /**
     * Create a new dialog
     * 
     * @param owner
     *            the owner of the new dialog
     * @param title
     *            a proposed title for this property definition
     * @param probablyNumeric
     *            the default behavior for the Numeric checkbox
     */
    public PropertyDefinitonCreationDialog(Dialog owner, String title, boolean probablyNumeric) {
        super(owner, _("DataImport.PropertyDefinitionCreationDialog.Title"), true);
        
        type.setRenderer(new PropertyTypeRenderer());
        if (probablyNumeric) {
            type.setSelectedItem(PropertyType.NumProperty);
        }
        
        FormLayout layout = new FormLayout("r:p, 3dlu, MAX(150dlu;p):g");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);

        builder.setDefaultDialogBorder();
        titleField.setText(title);
        SelectAllOnFocus.addTo(titleField);
        builder.append(_("PropertyDefinition.Title"), titleField);
        builder.append(_("PropertyDefinition.Type"), type);
        builder.append(_("PropertyDefinition.Description"), new JScrollPane(getDescriptionArea()));
        JButton okButton = getOkButton();
        builder.append(ButtonBarFactory.buildOKCancelBar(okButton, getCancelButton()), 3);

        setLayout(new BorderLayout());
        add(builder.getPanel(), BorderLayout.CENTER);

        getRootPane().setDefaultButton(okButton);
        setLocationRelativeTo(owner);
        pack();
    }

    /**
     * @return the propertyDefinition produced from user input, or
     *         <code>null</code> if creation was canceled
     */
    public PropertyDefinition getPropertyDefinition() {
        return propertyDefinition;
    }

    private boolean checkInputs() {
        if (titleField.getText().isEmpty()) {
            if (titleField.getBorder() != null) {
                titleField.setBorder(BorderFactory.createLineBorder(Color.RED));
                titleField.getDocument().addDocumentListener(new DocumentListener() {

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        // not used
                    }

                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        titleField.setBorder(null);
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        // also not used, where is an AbstractDocumentListener
                        // when you need one?
                    }
                });
            }
            return false;
        }
        return true;
    }

    private JTextArea getDescriptionArea() {
        JTextArea area = new JTextArea(5, 1);
        area.setLineWrap(true);
        area.setFont(new JTextField().getFont());
        area.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS,
                ImmutableSet.of(KeyStroke.getKeyStroke("pressed TAB")));
        area.setFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS,
                ImmutableSet.of(KeyStroke.getKeyStroke("shift pressed TAB")));
        return area;
    }

    private JButton getOkButton() {
        class OkAction extends AbstractAction {

            OkAction() {
                super(_("Button.OK"));
            }

            /*
             * (non-Javadoc)
             * 
             * @see
             * java.awt.event.ActionListener#actionPerformed(java.awt.event.
             * ActionEvent)
             */
            @Override
            public void actionPerformed(ActionEvent arg0) {
                if (checkInputs()) {
                    PropertyType propType = (PropertyType)type.getSelectedItem();
                    propertyDefinition = new PropertyDefinition(titleField.getText(), descriptionArea.getText(),
                            propType, null, true, false);
                    dispose();
                }
            }

        }
        return new JButton(new OkAction());
    }

    private JButton getCancelButton() {
        JButton button = StandardButtonFactory.createCancelButton();
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                propertyDefinition = null;
                dispose();
            }
        });
        return button;
    }

    private static class PropertyTypeRenderer extends CustomComboBoxRenderer {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JLabel c = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value != null) {
                PropertyType val = (PropertyType)value;
                c.setToolTipText(val.getDescription());
            }
            return c;
        }
    }
}
