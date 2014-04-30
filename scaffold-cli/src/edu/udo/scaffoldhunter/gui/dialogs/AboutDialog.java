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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Dominic Sacré
 */
public class AboutDialog extends JDialog {

    /**
     * @param parent
     */
    public AboutDialog(JFrame parent) {
        super(parent, "About Scaffold Hunter", true);
        setSize(400, 320);
        setResizable(false);
        setLocationRelativeTo(parent);

        Properties runProps = Resources.getProperties("run.properties");
        String version = runProps.getProperty("version", "devel");
        
        Box b = Box.createVerticalBox();
        b.add(new JLabel(Resources.getImageIcon("images/scaffoldhunter-logo.png"), JLabel.CENTER));
        b.add(Box.createVerticalStrut(20));
        b.add(new JLabel("<html>Version: " + version + "<br><br>" + 
                "(C) 2006-2014 The Scaffold Hunter developers</html>", 
                JLabel.CENTER));
        getContentPane().add(b, BorderLayout.CENTER);

        JButton close = StandardButtonFactory.createCloseButton(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent evt) {
                dispose();
            }
        });

        JPanel p = ButtonBarFactory.buildCloseBar(close);
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        getContentPane().add(p, BorderLayout.SOUTH);
    }
}