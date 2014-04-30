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

package edu.udo.scaffoldhunter.view.scaffoldtree.config;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.ColorEditor;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * A panel to configure the gradient of a gradient mapping.
 * 
 * @author Henning Garus
 * 
 */
public class GradientPanel extends JPanel implements ActionListener {

    private static final String ASCENDING = "ASCENDING";
    private static final String DESCENDING = "DESCENDING";

    private final ConfigMapping mapping;

    /**
     * Create a new gradient panel
     * 
     * @param color
     *            should the panel provide color selection?
     * @param mapping
     *            the mapping whose gradient is configured by this panel
     */
    public GradientPanel(boolean color, ConfigMapping mapping) {
        super(new FormLayout("p, 3dlu, max(20dlu;p), p:g", "p:g, p, 3dlu, p, 3dlu, p, 5dlu, p, 3dlu, max(20dlu;p), 3dlu, max(20dlu;p), p:g"));
        this.mapping = mapping;

        DefaultComponentFactory componentFactory = new DefaultComponentFactory();
        ButtonGroup bg = new ButtonGroup();

        JRadioButton ascending = new JRadioButton(I18n.get("VisualMappings.Gradient.Ascending"));
        ascending.setActionCommand(ASCENDING);
        ascending.addActionListener(this);
        bg.add(ascending);

        JRadioButton descending = new JRadioButton(I18n.get("VisualMappings.Gradient.Descending"));
        descending.setActionCommand(DESCENDING);
        descending.addActionListener(this);
        bg.add(descending);

        ascending.setSelected(mapping.isGradientAscending());
        descending.setSelected(!mapping.isGradientAscending());

        add(componentFactory.createSeparator(I18n.get("VisualMappings.Direction")), CC.xyw(1, 2, 4));
        
        add(ascending, CC.xyw(1, 4, 4));
        add(descending, CC.xyw(1, 6, 4));
        

        if (color) {
            add(componentFactory.createSeparator(I18n.get("VisualMappings.Color")), CC.xyw(1,8, 4));

            add(new JLabel(I18n.get("VisualMappings.Gradient.Color1") + ":"), CC.xy(1, 10));
            ColorEditor ce1 = new ColorEditor(mapping.getGradientColor1());
            ce1.addPropertyChangeListener(ColorEditor.COLOR_PROPERTY, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    GradientPanel.this.mapping.setGradientColor1((Color) evt.getNewValue());
                }
            });
            add(ce1, CC.xy(3, 10));

            add(new JLabel(I18n.get("VisualMappings.Gradient.Color2") + ":"), CC.xy(1, 12));
            ColorEditor ce2 = new ColorEditor(mapping.getGradientColor2());
            ce2.addPropertyChangeListener(ColorEditor.COLOR_PROPERTY, new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    GradientPanel.this.mapping.setGradientColor2((Color) evt.getNewValue());
                }
            });
            add(ce2, CC.xy(3, 12));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(ASCENDING)) {
            mapping.setGradientAscending(true);
        } else if (e.getActionCommand().equals(DESCENDING)) {
            mapping.setGradientAscending(false);
        } else {
            throw new AssertionError("Unhandled ActionCommand.");
        }
    }
}
