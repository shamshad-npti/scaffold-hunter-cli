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

import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;
import edu.udo.scaffoldhunter.util.I18n;
import edu.udo.scaffoldhunter.util.Resources;
import edu.udo.scaffoldhunter.view.View;
import edu.udo.scaffoldhunter.view.ViewClassRegistry;

/**
 * @author Sven Schrinner
 * 
 */
public class InitialViewDialog extends JDialog {

    private final LinkedHashSet<String> selectedViews = Sets.newLinkedHashSet();
    private final LinkedHashMap<String, Class<? extends View>> views = Maps.newLinkedHashMap();

    private final List<JCheckBox> checkBoxes = Lists.newArrayList();
    
    private final SelectionListener selectionListener = new SelectionListener();

    /**
     * 
     * @param owner
     *            the parent window
     */
    public InitialViewDialog(Window owner) {
        super(owner, I18n.get("Session.InitialViews.Title"), ModalityType.APPLICATION_MODAL);
        setIconImage(Resources.getBufferedImage("images/scaffoldhunter-icon.png"));

        JPanel pane = new JPanel(new FormLayout("p, p", "p, 3dlu, p, 3dlu, p"));
        pane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setContentPane(pane);
        this.setResizable(false);
        
        for(Class<? extends View> viewclass : ViewClassRegistry.getClasses()) {
            views.put(ViewClassRegistry.getClassName(viewclass), viewclass);
        }

        JTextPane description = new JTextPane();
        description.setEditable(false);
        description.setAlignmentX(LEFT_ALIGNMENT);
        description.setFocusable(false);
        description.setBackground(SystemColor.control);
        description.setText(I18n.get("Session.InitialViews.Description"));
        add(description, CC.xyw(1,1,2));
        
        JPanel propertyPanel = buildPropertyPanel();
        propertyPanel.setBorder(BorderFactory.createTitledBorder(I18n.get("Session.InitialViews.AvailableViews")));
        JScrollPane scrollPane = new JScrollPane(propertyPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(propertyPanel, CC.xyw(1, 3, 2));        

        JButton okButton = StandardButtonFactory.createOKButton(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        JPanel buttonBar = ButtonBarFactory.buildLeftAlignedBar(new JButton(new SelectAll()), new JButton(new SelectNone()), okButton);
        add(buttonBar, CC.xy(1, 5));
        
        // this fixes the width of the text pane, so the preferred height can be calculated properly
        description.setSize((int)buttonBar.getPreferredSize().getWidth(),Short.MAX_VALUE);
        
        getRootPane().setDefaultButton(okButton);
        pack();
        setLocationRelativeTo(owner);
    }
    
    /**
     * Returns a list of Class-objects corresponding to the selected views in the dialog.
     * @return the selected view classes.
     */
    public List<Class<? extends View>> getSelectedViews() {
        // this is done to create the view in a deterministic order
        for(JCheckBox box : checkBoxes) {
            if(box.isSelected()) {
                selectedViews.remove(box.getActionCommand());
                selectedViews.add(box.getActionCommand());
            }
        }
        
        List<Class<? extends View>> result = Lists.newArrayList();
        for(String view : selectedViews)
            result.add(views.get(view));
        
        return result;
    }

    private JPanel buildPropertyPanel() {
        FormLayout layout = new FormLayout("p, 3dlu, c:p, 3dlu, c:p");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        builder.setDefaultDialogBorder();
        for (String viewName : views.keySet()) {
            JCheckBox view = new JCheckBox();
            view.setText(viewName);
            view.setActionCommand(viewName);
            view.addActionListener(selectionListener);
            view.setSelected(true);
            selectedViews.add(viewName);
            builder.append(view);
            this.checkBoxes.add(view);            
            builder.nextLine();
        }
        return builder.getPanel();
    }
    
    private class SelectionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox b = (JCheckBox) e.getSource();
            if (b.isSelected()) {
                selectedViews.add(e.getActionCommand());
            } else {
                selectedViews.remove(e.getActionCommand());
            }
        }

    }

    private class SelectAll extends AbstractAction {

        SelectAll() {
            super(I18n.get("Session.InitialViews.SelectAll"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (JCheckBox b : checkBoxes) {
                b.setSelected(true);
            }
            selectedViews.addAll(views.keySet());
        }
    }

    private class SelectNone extends AbstractAction {

        SelectNone() {
            super(I18n.get("Session.InitialViews.SelectNone"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            for (JCheckBox b : checkBoxes) {
                b.setSelected(false);
            }
            selectedViews.clear();
        }
    }
}
