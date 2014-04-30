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

package edu.udo.scaffoldhunter.gui.dialogs;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.util.StringEscapeUtils;

/**
 * @author Dominic Sacré
 *
 */
public class DatabaseErrorDialog extends JDialog {

    /**
     * The result type of this dialog
     */
    public enum Result {
        /**
         * the retry button was clicked
         */
        RETRY,
        /**
         * the ignore button was clicked
         */
        IGNORE,
        /**
         * the quit button was clicked
         */
        QUIT
    }

    private Result result;

    /**
     * @param parent
     * @param message
     * @param allowIgnore
     */
    public DatabaseErrorDialog(Frame parent, String message, boolean allowIgnore) {
        super(parent, _("DatabaseErrorDialog.Title"), ModalityType.APPLICATION_MODAL);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        FormLayout layout = new FormLayout(
                "pref, 8dlu, pref:grow, 8dlu, 72dlu, 8dlu, 72dlu",
                "pref, 8dlu, top:pref:grow, 8dlu, pref"
        );
        CellConstraints cc = new CellConstraints();
    
        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();
    
        JButton retryButton = new JButton(_("DatabaseErrorDialog.Retry"));
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.RETRY;
                dispose();
            }
        });

        JButton ignoreButton = new JButton(_("DatabaseErrorDialog.Ignore"));
        ignoreButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.IGNORE;
                dispose();
            }
        });

        JButton quitButton = new JButton(Resources.getImageIcon("icons/misc/button.png"));
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.QUIT;
                dispose();
            }
        });

        pb.add(new JLabel(_("DatabaseErrorDialog.Text")), cc.xyw(3, 1, 5));
        pb.add(new JLabel("<html>" + StringEscapeUtils.escapeHTML(message) + "</html>"), cc.xyw(3, 3, 5));
        if (allowIgnore) {
            pb.add(ignoreButton, cc.xy(5, 5));
        }
        pb.add(retryButton, cc.xy(7, 5));
        pb.add(quitButton, cc.xywh(1, 1, 1, 5));

        getRootPane().setDefaultButton(retryButton);
    
        setContentPane(pb.getPanel());
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    /**
     * @return  the result chosen by the user
     */
    public Result getResult() {
        return result;
    }

}
