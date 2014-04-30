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

package edu.udo.scaffoldhunter.gui.datacalculation;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.WordWrapLabel;
import edu.udo.scaffoldhunter.model.datacalculation.CalcJob;
import edu.udo.scaffoldhunter.model.datacalculation.CalcProcess;
import edu.udo.scaffoldhunter.model.datacalculation.CalcProcess.JobsModel;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.datacalculation.CalcPlugin;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Henning Garus
 * 
 */
public class CalcDialog extends JDialog implements ListSelectionListener, ListDataListener, DocumentListener {

    @SuppressWarnings("unused")
    private final CalcProcess calcProcess;
    private final JobsModel jobs;

    private JPanel pluginPanel;
    private JList pluginList;
    private JList joblist;
    private JTextField jobname;
    private JButton newJobButton;
    private JButton continueButton;

    private PluginSettingsPanel pluginSettingsPanel;
    private JPanel pluginDescriptionPanel;

    private Result result = Result.NONE;

    /**
     * Create a new CalcDialog
     * 
     * @param owner
     *            the owner of the dialog
     * @param pluginListModel
     *            the list of plugins shown in the dialog
     * @param calcProcess
     *            the calc process backing this dialog
     */
    public CalcDialog(Window owner, ListModel pluginListModel, CalcProcess calcProcess) {
        super(owner, _("DataCalc.CreateJobs"), ModalityType.APPLICATION_MODAL);
        this.calcProcess = calcProcess;
        this.jobs = calcProcess.getJobsModel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                result = Result.CANCEL;
                dispose();
            }
        });
        PanelBuilder builder = new PanelBuilder(new FormLayout("p:g", "f:p:g"));
        setMinimumSize(new Dimension(640, 480));

        continueButton = buildOKButton();

        builder.setDefaultDialogBorder();
        builder.add(buildCalcPanel(pluginListModel), CC.xy(1, 1));

        jobs.addListDataListener(this);

        setContentPane(builder.getPanel());
        pack();
        setLocationRelativeTo(owner);
    }

    private JPanel buildCalcPanel(ListModel plugins) {
        PanelBuilder builder = new PanelBuilder(new FormLayout("p, 3dlu, p, 3dlu, p:g", "f:p:g, 3dlu, p"));

        builder.add(buildControlPanel(plugins, jobs), CC.xy(1, 1));
        builder.add(new JSeparator(SwingConstants.VERTICAL), CC.xy(3, 1));

        pluginPanel = buildPluginPanel();
        builder.add(pluginPanel, CC.xy(5, 1));
        builder.add(ButtonBarFactory.buildOKCancelBar(continueButton, buildCancelButton()), CC.xyw(1, 3, 5));

        return builder.getPanel();
    }

    private JPanel buildControlPanel(ListModel pluginListModel, JobsModel jobs) {
        PanelBuilder builder = new PanelBuilder(new FormLayout("max(120dlu;p):g, p",
                "3dlu, p, 3dlu, f:p:g(0.5), 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, f:p:g(0.75)"));
        builder.addSeparator(_("DataCalc.Plugins"), CC.rcw(2, 1, 2));
        pluginList = new JList(pluginListModel);
        pluginList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginList.setSelectedIndex(0);
        builder.add(pluginList, CC.rcw(4, 1, 2));

        builder.addSeparator(_("DataCalc.CalcJobs"), CC.rcw(6, 1, 2));
        joblist = new JList(jobs);
        joblist.setSelectionModel(jobs.getListSelectionModel());
        joblist.clearSelection();        
        JScrollPane jobscrollpane = new JScrollPane(joblist);
        builder.add(jobscrollpane, CC.rchw(8, 1, 6, 1));
        builder.add(getMoveUpButton(), CC.rc(8, 2));
        builder.add(getMoveDownButton(), CC.rc(10, 2));
        builder.add(getDeleteButton(), CC.rc(12, 2));

        pluginList.addListSelectionListener(this);
        joblist.addListSelectionListener(this);

        return builder.getPanel();
    }

    private JButton getDeleteButton() {
        return new JButton(new AbstractAction("", Resources.getIcon("cancel.png")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jobs.removeSelectedElement();
                if (jobs.getSize() <= 0)
                    pluginList.setSelectedIndex(0);
            }
        });
    }

    private JButton getMoveDownButton() {
        return new JButton(new AbstractAction("", Resources.getIcon("down.png")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jobs.moveSelectionDown();
            }
        });
    }

    private JButton getMoveUpButton() {
        return new JButton(new AbstractAction("", Resources.getIcon("up.png")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jobs.moveSelectionUp();
            }
        });
    }

    private JPanel buildPluginPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        // TODO store/retrieve settings in db
        CalcPlugin plugin = ((CalcPlugin) pluginList.getSelectedValue());
        pluginSettingsPanel = plugin.getSettingsPanel(null, null);
        pluginDescriptionPanel = createPluginDescriptionPanel(plugin);
        panel.add(pluginDescriptionPanel, BorderLayout.NORTH);
        panel.add(pluginSettingsPanel, BorderLayout.CENTER);
        JPanel commonControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        commonControls.add(new JLabel(_("DataCalc.Jobname")));
        Action addJob = newAddJobAction();
        jobname = new JTextField(20);
        jobname.addActionListener(addJob);
        commonControls.add(jobname);
        newJobButton = new JButton(addJob);
        commonControls.add(ButtonBarFactory.buildOKBar(newJobButton), BorderLayout.SOUTH);
        panel.add(commonControls, BorderLayout.SOUTH);
        return panel;
    }

    private Action newAddJobAction() {
        return new AbstractAction(_("DataCalc.AddNewCalcJob")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                jobs.add(new CalcJob(jobname.getText(), (CalcPlugin) pluginList.getSelectedValue(), pluginSettingsPanel
                        .getArguments()));

                // cause selection event to fire
                int i = pluginList.getSelectedIndex();
                pluginList.clearSelection();
                pluginList.setSelectedIndex(i);
            }
        };
    }

    private JButton buildOKButton() {
        JButton b = new JButton(new AbstractAction(_("DataCalc.StartCalc")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                pluginList.setSelectedIndex(0);
                result = Result.START_CALCULATION;
                dispose();
            }
        });
        b.setEnabled(false);
        return b;
    }

    private JButton buildCancelButton() {
        return new JButton(new AbstractAction(_("Button.Cancel")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                result = Result.CANCEL;
                dispose();
            }
        });
    }

    /**
     * @return the result
     */
    public Result getResult() {
        return result;
    }

    /**
     * The result of this dialog
     */
    public enum Result {
        /** Nothing happened yet */
        NONE,
        /** User clicked start calculation */
        START_CALCULATION,
        /** User aborted */
        CANCEL,
    }
    
    private JPanel createPluginDescriptionPanel(CalcPlugin plugin) {
        FormLayout layout = new FormLayout("p", "");
        DefaultFormBuilder builder = new DefaultFormBuilder(layout);
        WordWrapLabel description = new WordWrapLabel(plugin.getDescription());
        description.setMaximumWidth(600);
        
        builder.appendSeparator(_("DataCalc.PluginDescription"));
        builder.append(description);
        builder.appendUnrelatedComponentsGapRow();
        
        return builder.getPanel();
    }

    private void setPluginSettingsPanel(PluginSettingsPanel panel) {
        pluginPanel.remove(pluginSettingsPanel);
        pluginPanel.add(panel, BorderLayout.CENTER);
        pluginSettingsPanel = panel;
        pluginPanel.validate();
    }
    
    private void setPluginDescriptionPanel(JPanel panel) {
        pluginPanel.remove(pluginDescriptionPanel);
        pluginPanel.add(panel, BorderLayout.NORTH);
        pluginDescriptionPanel = panel;
        pluginPanel.validate();
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource().equals(pluginList) && pluginList.getSelectedValue() != null) {
            CalcPlugin plugin = (CalcPlugin) pluginList.getSelectedValue();
            joblist.clearSelection();
            jobname.setEnabled(true);
            newJobButton.setEnabled(true);
            jobname.setText("");
            setPluginSettingsPanel(plugin.getSettingsPanel(null, null));
            setPluginDescriptionPanel(createPluginDescriptionPanel(plugin));
        } else if (e.getSource().equals(joblist) && joblist.getSelectedValue() != null) {
            pluginList.clearSelection();
            CalcJob j = (CalcJob) joblist.getSelectedValue();
            jobname.setText(j.getJobName());
            jobname.setEnabled(false);
            newJobButton.setEnabled(false);
            setPluginSettingsPanel(j.getPlugin().getSettingsPanel(null, j.getPluginArguments()));
            setPluginDescriptionPanel(createPluginDescriptionPanel(j.getPlugin()));
        }
    }

    private void validateInputs() {
        if (jobs.getSize() <= 0) {
            continueButton.setEnabled(false);
        } else
            continueButton.setEnabled(true);
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        validateInputs();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        validateInputs();
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        validateInputs();
    }

    @Override
    public void intervalAdded(ListDataEvent e) {
        validateInputs();
    }

    @Override
    public void intervalRemoved(ListDataEvent e) {
        validateInputs();
    }

    @Override
    public void contentsChanged(ListDataEvent e) {
        validateInputs();
    }
}
