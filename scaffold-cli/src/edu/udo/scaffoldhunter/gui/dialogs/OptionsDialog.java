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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.EventListener;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.EventListenerList;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.ButtonBarFactory;

import edu.udo.scaffoldhunter.gui.util.BeanBinder;
import edu.udo.scaffoldhunter.gui.util.BeanInfoResolver;
import edu.udo.scaffoldhunter.gui.util.PropertySheetPage;
import edu.udo.scaffoldhunter.gui.util.StandardButtonFactory;

/**
 * @author Thorsten Flügel
 * 
 */
public class OptionsDialog extends JDialog {
    JTabbedPane tabs;
    EventListenerList resultListeners;

    /**
     * Creates an empty options dialog.
     * 
     * @param parent
     *            parent window, will be blocked while this dialog is shown
     * @param extraAction
     *            an action for which a button will be inserted in the lower
     *            left corner if not null
     */
    public OptionsDialog(Frame parent, AbstractAction extraAction) {
        super(parent, _("OptionsDialog.Title"), true);

        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                fireResultEvent(Result.CANCEL);
            }
        });

        resultListeners = new EventListenerList();

        tabs = new JTabbedPane();
        getContentPane().add(tabs, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton okButton = StandardButtonFactory.createOKButton(new OkAction());
        JButton cancelButton = StandardButtonFactory.createCancelButton(new CancelAction());
        JButton applyButton = StandardButtonFactory.createApplyButton(new ApplyAction());

        JPanel optionsPanel = ButtonBarFactory.buildOKCancelApplyBar(okButton, cancelButton, applyButton);

        if (extraAction != null) {
            JButton extraButton = new JButton(extraAction);
            JPanel extraPanel = ButtonBarFactory.buildLeftAlignedBar(extraButton);
            extraPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 12));
            buttonPanel.add(extraPanel);
        }
        buttonPanel.add(optionsPanel);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        ((JPanel)getContentPane()).setBorder(Borders.DIALOG_BORDER);

        pack();
        setMinimumSize(new Dimension(Math.max(getWidth(), 480), 360));
        setLocationRelativeTo(parent);
    }

    /**
     * Adds a tab to the options pane. If <code>data</code> does not have any
     * properties, no tab will be added.
     * 
     * @param title
     *            name of the tab
     * @param data
     *            a bean, will be displayed in the tab
     * @param id
     *            an object used to identify the data. See
     *            {@link ResultEventListener}.
     */
    public void addOptionsTab(String title, Object data, Object id) {
        if (new BeanInfoResolver().getBeanInfo(data).getPropertyDescriptors().length > 0) {
            PropertySheetPage page = new PropertySheetPage(data, id);
            tabs.addTab(title, page);
        }
    }

    /**
     * Adds a listener for changes of the beans in the options dialog.
     * 
     * @param listener
     *            will be notified about the changes
     */
    public void addConfigChangeListener(BeanBinder.BeanChangeListener listener) {
        for (int i = 0; i < tabs.getTabCount(); ++i) {
            PropertySheetPage page = (PropertySheetPage) tabs.getComponentAt(i);
            if (page != null) {
                page.addBeanChangedListener(listener);
            }
        }
    }

    /**
     * Removes a listener for changes of the beans in the options dialog.
     * 
     * @param listener
     *            will no longer be notified about the changes
     */
    public void removeConfigChangeListener(BeanBinder.BeanChangeListener listener) {
        for (int i = 0; i < tabs.getTabCount(); ++i) {
            PropertySheetPage page = (PropertySheetPage) tabs.getComponentAt(i);
            page.removePropertyChangeListener(listener);
        }
    }

    /**
     * Adds a listener for the user actions possible in this dialog. See
     * {@link ResultEventListener}.
     * 
     * @param listener
     *            will be informed about the actions
     */
    public void addResultListener(ResultEventListener listener) {
        resultListeners.add(ResultEventListener.class, listener);
    }

    /**
     * Removes a listener for the user actions possible in this dialog.
     * 
     * @param listener
     *            will no longer be informed about the actions
     */
    public void removeResultListener(ResultEventListener listener) {
        resultListeners.remove(ResultEventListener.class, listener);
    }

    /**
     * The listener class for the options result event.
     * 
     * @author Thorsten Flügel
     */
    public interface ResultEventListener extends EventListener {
        /**
         * The result event is fired every time the user clicks a button.
         * 
         * @param result
         *            the option selected by the user
         */
        void processEvent(Result result);
    }

    /**
     * The selected option.
     * 
     * @author Thorsten Flügel
     */
    public enum Result {
        /** User accepted the changes. */
        OK,
        /** User discarded the changes. */
        CANCEL
    };

    private void fireResultEvent(Result result) {
        for (ResultEventListener listener : resultListeners.getListeners(ResultEventListener.class)) {
            listener.processEvent(result);
        }
    }

    private class OkAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            dispose();
            fireResultEvent(Result.OK);
        }
    }

    private class ApplyAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            fireResultEvent(Result.OK);
        }
    }

    private class CancelAction extends AbstractAction {
        @Override
        public void actionPerformed(ActionEvent e) {
            dispose();
            fireResultEvent(Result.CANCEL);
        }
    }

    /**
     * @return the identifier associated with the currently active tab
     */
    public Object getCurrentId() {
        PropertySheetPage page = (PropertySheetPage) tabs.getSelectedComponent();
        return page.getId();
    }

    /**
     * Activates the tab with the given id.
     * 
     * @param id
     *            identifier of the tab that will be activated
     */
    public void setActiveTab(Object id) {
        int index = 0;
        for (int i = 0; i < tabs.getTabCount(); ++i) {
            PropertySheetPage page = (PropertySheetPage) tabs.getComponentAt(i);
            if (page.getId() == id) {
                index = i;
                break;
            }
        }
        if(index < tabs.getTabCount())
            tabs.setSelectedIndex(index);
    }
}
