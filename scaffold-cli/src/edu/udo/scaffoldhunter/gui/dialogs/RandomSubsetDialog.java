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
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.EclipseTooltip;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * Dialog to create a random {@link Subset}
 * 
 * @author Andrey Zhylka
 */
public class RandomSubsetDialog extends JDialog {

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

    private JTextField textField;
    private JLabel maxSizeLabel;
    private JLabel purposeLabel;
    private JButton okButton;
    private JButton cancelButton;
    private Border originalBorder;

    private EclipseTooltip hint;

    private Integer newSize = null;
    private Integer maxSize;
    private Integer minSize = 1;

    /**
     * @param parent
     * @param maxSize
     */
    public RandomSubsetDialog(Frame parent, Integer maxSize) {
        this(parent, null, null, maxSize);
    }

    /**
     * @param parent
     * @param location
     * @param anchor
     * @param maxSize
     */
    public RandomSubsetDialog(Frame parent, Point location, Anchor anchor, Integer maxSize) {
        super(parent, _("Subset.GenerateRandomSubset.Short"), true);

        FormLayout layout = new FormLayout("pref:grow", "pref:grow, 20dlu, fill:pref");
        CellConstraints cc = new CellConstraints();

        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();

        textField = new JTextField(_("Subset.GenerateRandomSubset.DefaultSubsetSize"));
        textField.selectAll();
        originalBorder = textField.getBorder();

        this.maxSize = maxSize;
        maxSizeLabel = new JLabel("/" + maxSize);
        purposeLabel = new JLabel(_("Subset.GenerateRandomSubset.InputSubsetSize"));
        pb.add(purposeLabel, cc.xy(1, 1));

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
        ((AbstractDocument)textField.getDocument())
                .setDocumentFilter(new NumberDocumentFilter());

        JPanel inputPanel = new JPanel();

        Dimension panelSize = pb.getPanel().getPreferredSize();
        Dimension size = textField.getPreferredSize();
        size.width = panelSize.width - maxSizeLabel.getWidth();
        textField.setPreferredSize(size);

        inputPanel.add(textField);
        inputPanel.add(maxSizeLabel);
        pb.add(inputPanel, cc.xy(1, 2));

        okButton = StandardButtonFactory.createOKButton(okAction);
        cancelButton = StandardButtonFactory.createCancelButton(cancelAction);

        okButton.setEnabled(false);

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

    private ActionListener okAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            newSize = Integer.parseInt( textField.getText() );
            dispose();
        }
    };

    private ActionListener cancelAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
        }
    };

    private void updateAllowOK() {
        // checking for matching required to prevent NumberFormatException if default text is passed
        boolean containsANumber = textField.getText().length() > 0 && textField.getText().matches("[0-9]+");

        Integer value = containsANumber ? Integer.parseInt(textField.getText()) : null;
        boolean valid = containsANumber && value >= minSize && value <= maxSize;
        okButton.setEnabled(valid);

        // highlight field only if the size bounds are violated
        if (containsANumber && !valid) {
             textField.setBorder(BorderFactory.createLineBorder(Color.RED));

            if (hint == null || !hint.isVisible()) {
                JPanel hintPanel = new JPanel();
                if (value < minSize) {
                    hintPanel.add(new JLabel(_("Subset.GenerateRandomSubset.SizeZero")));
                } else /* if (value > maxSize) */{
                    hintPanel.add(new JLabel(_("Subset.GenerateRandomSubset.SizeTooBig")));
                }

                Point hintPosition = textField.getLocationOnScreen();
                hintPosition.setLocation(hintPosition.getX(), hintPosition.getY() + textField.getHeight());

                hint = new EclipseTooltip(hintPosition, textField, textField.getBounds(), hintPanel);
            }
        } else {
            textField.setBorder(originalBorder);
            if (hint != null) {
                hint.destroy();
            }
        }
    }

    /**
     * @return user defined size
     */
    public Integer getNewSize() {
        return newSize;
    }

    /**
     * Used to restrict input of non-numerical data to the size field
     * @see javax.swing.text.DocumentFilter
     */
    private class NumberDocumentFilter extends DocumentFilter {
        /**
         * @param fb
         * @param offset
         * @param string
         * @param attr
         * @throws BadLocationException
         */
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException{
            if (string != null && string.matches("[0-9]+")) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException{
            super.remove(fb, offset, length);
        }

        @Override
        public void replace(FilterBypass fb, int offset, int legth, String text, AttributeSet attr)
                throws BadLocationException {
            if (text != null && text.matches("[0-9]*")) {
                super.replace(fb, offset, legth, text, attr);
            }
        }
    }
}
