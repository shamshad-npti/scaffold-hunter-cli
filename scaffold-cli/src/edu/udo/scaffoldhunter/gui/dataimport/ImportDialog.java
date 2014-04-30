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
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
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

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.gui.util.SelectAllOnFocus;
import edu.udo.scaffoldhunter.model.dataimport.ImportJob;
import edu.udo.scaffoldhunter.model.dataimport.ImportProcess;
import edu.udo.scaffoldhunter.model.dataimport.ImportProcess.JobsModel;
import edu.udo.scaffoldhunter.plugins.PluginSettingsPanel;
import edu.udo.scaffoldhunter.plugins.dataimport.ImportPlugin;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * @author Henning Garus
 * 
 */
public class ImportDialog extends JDialog implements ListSelectionListener, ListDataListener, DocumentListener {

    private final ImportProcess importProcess;
    private final JobsModel jobs;

    private JPanel pluginPanel;
    private JList pluginList;
    private JList joblist;
    private JTextField datasetName;
    private JTextArea datasetDescription;
    private JTextField jobname;
    private JButton newJobButton;
    private JButton continueButton;

    private JScrollPane pluginSettingsPane = new JScrollPane();
    private PluginSettingsPanel pluginSettingsPanel;

    private Result result = Result.NONE;

    /**
     * Create a new ImportDialog
     * 
     * @param owner
     *            the owner of the dialog
     * @param pluginListModel
     *            the list of plugins shown in the dialog
     * @param importProcess
     *            the import process backing this dialog
     */
    public ImportDialog(Window owner, ListModel pluginListModel, ImportProcess importProcess) {
        super(owner, _("DataImport.CreateJobs"), ModalityType.APPLICATION_MODAL);
        this.importProcess = importProcess;
        this.jobs = importProcess.getJobsModel();

        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                result = Result.CANCEL;
                dispose();
            }
        });

        PanelBuilder builder = new PanelBuilder(new FormLayout("p:g", "p, 3dlu, p, 3dlu, f:p:g"));
        setMinimumSize(new Dimension(640, 480));

        continueButton = buildOKButton();

        builder.setDefaultDialogBorder();
        if (importProcess.getDataset() == null) {
            builder.add(buildDatasetNamePanel(), CC.xy(1, 1));
            builder.add(new JSeparator(), CC.xy(1, 3));
        }
        builder.add(buildImportPanel(pluginListModel), CC.xy(1, 5));

        jobs.addListDataListener(this);

        add(builder.getPanel(), BorderLayout.CENTER);
        getRootPane().setDefaultButton(continueButton);
        pack();

        setMinimumSize(getSize());
        setLocationRelativeTo(owner);
    }

    private JPanel buildImportPanel(ListModel plugins) {
        PanelBuilder builder = new PanelBuilder(new FormLayout("p, 3dlu, p, 3dlu, p:g", "f:p:g, 3dlu, p"));

        builder.add(buildControlPanel(plugins, jobs), CC.xy(1, 1));
        builder.add(new JSeparator(SwingConstants.VERTICAL), CC.xy(3, 1));

        pluginPanel = buildPluginPanel();
        builder.add(pluginPanel, CC.xy(5, 1));
        builder.add(ButtonBarFactory.buildOKCancelBar(continueButton, buildCancelButton()), CC.xyw(1, 3, 5));

        return builder.getPanel();
    }

    private JPanel buildDatasetNamePanel() {
        PanelBuilder builder = new PanelBuilder(
                new FormLayout("p,3dlu, p:g(0.5),10dlu,p,3dlu,p:g(1.0)", "max(p;60dlu)"));

        builder.addLabel(_("DataImport.DatasetName"));
        builder.nextColumn(2);
        datasetName = new JTextField();
        datasetName.setDocument(importProcess.getDatasetNameDocument());
        datasetName.getDocument().addDocumentListener(this);
        datasetName.setColumns(20);
        SelectAllOnFocus.addTo(datasetName);
        builder.add(datasetName);
        builder.nextColumn(2);
        builder.addLabel(_("DataImport.DatasetDescription"));
        builder.nextColumn(2);
        datasetDescription = new JTextArea(importProcess.getDatasetDescriptionDocument());
        datasetDescription.setLineWrap(true);
        /*
         * set preferredSize und minimumSize explicitly. Otherwise the textarea
         * will grow but won't shrink again as the preferred size would have
         * grown
         */
        JScrollPane p = new JScrollPane(datasetDescription);
        p.setMinimumSize(p.getMinimumSize());
        p.setPreferredSize(p.getPreferredSize());
        builder.add(p, CC.xy(7, 1, "f, f"));

        return builder.getPanel();
    }

    private JPanel buildControlPanel(ListModel pluginListModel, JobsModel jobs) {
        PanelBuilder builder = new PanelBuilder(new FormLayout("min(120dlu;p):g, p",
                "3dlu, p, 3dlu, f:p:g(0.25), 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, f:p:g(0.75)"));
        builder.addSeparator(_("DataImport.Plugins"), CC.rcw(2, 1, 2));
        pluginList = new JList(pluginListModel);
        pluginList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pluginList.setSelectedIndex(0);
        builder.add(new JScrollPane(pluginList), CC.rcw(4, 1, 2));

        builder.addSeparator(_("DataImport.ImportJobs"), CC.rcw(6, 1, 2));
        joblist = new JList(jobs);
        joblist.setSelectionModel(jobs.getListSelectionModel());
        joblist.clearSelection();
        builder.add(new JScrollPane(joblist), CC.rchw(8, 1, 6, 1));
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
        pluginSettingsPanel = ((ImportPlugin) pluginList.getSelectedValue()).getSettingsPanel(null, null);
        pluginSettingsPane.getViewport().add(pluginSettingsPanel);
        pluginSettingsPane.setPreferredSize(new Dimension(200, 400));
        panel.add(pluginSettingsPane, BorderLayout.CENTER);
        JPanel commonControls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        commonControls.add(new JLabel(_("DataImport.Jobname")));
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
        return new AbstractAction(_("DataImport.AddNewImportJob")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                ImportPlugin cur = (ImportPlugin) pluginList.getSelectedValue();
                Object pluginArguments = pluginSettingsPanel.getArguments();
                String checkResult = cur.checkArguments(pluginArguments);
                if (checkResult == null) {
                    jobs.add(new ImportJob(jobname.getText(), cur, pluginArguments));

                    // cause selection event to fire
                    int i = pluginList.getSelectedIndex();
                    pluginList.clearSelection();
                    pluginList.setSelectedIndex(i);
                } else {
                    JOptionPane.showMessageDialog(((Component) e.getSource()),
                            _("DataImport.CheckArgumentsErrorMessage", checkResult),
                            _("DataImport.CheckArgumentsErrorTitle"), JOptionPane.ERROR_MESSAGE);
                }
            }
        };
    }

    private JButton buildOKButton() {
        JButton b = new JButton(new AbstractAction(_("DataImport.StartImport")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                pluginList.setSelectedIndex(0);
                result = Result.START_IMPORT;
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
        /** User clicked start import */
        START_IMPORT,
        /** User aborted */
        CANCEL,
    }

    private void setPluginSettingsPanel(PluginSettingsPanel panel) {
        pluginSettingsPane.getViewport().remove(pluginSettingsPanel);
        pluginSettingsPanel = panel;
        pluginSettingsPane.getViewport().add(panel);
        pluginPanel.validate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event
     * .ListSelectionEvent)
     */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (e.getSource().equals(pluginList) && pluginList.getSelectedValue() != null) {
            joblist.clearSelection();
            jobname.setEnabled(true);
            newJobButton.setEnabled(true);
            jobname.setText("");
            setPluginSettingsPanel(((ImportPlugin) pluginList.getSelectedValue()).getSettingsPanel(null, null));
        } else if (e.getSource().equals(joblist) && joblist.getSelectedValue() != null) {
            pluginList.clearSelection();
            ImportJob j = (ImportJob) joblist.getSelectedValue();
            jobname.setText(j.getJobName());
            jobname.setEnabled(false);
            newJobButton.setEnabled(false);
            setPluginSettingsPanel(j.getPlugin().getSettingsPanel(null, j.getPluginArguments()));
        }
    }

    /**
     * Check if the inputs are valid, if so enable the continue button,
     * otherwise disable it.
     */
    public void validateInputs() {
        if (datasetName != null) {
            String text = datasetName.getText();
            boolean valid = !(text.isEmpty() || importProcess.getExistingDatasets().contains(text.toLowerCase()));
            if (valid) {
                datasetName.setBorder(BorderFactory.createEtchedBorder());
            } else {
                datasetName.setBorder(BorderFactory.createLineBorder(Color.RED));
            }
        }

        if (jobs.getSize() <= 0
                || (datasetName != null && (datasetName.getText().isEmpty() || importProcess.getExistingDatasets()
                        .contains(datasetName.getText().toLowerCase())))) {
            continueButton.setEnabled(false);
        } else
            continueButton.setEnabled(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#insertUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void insertUpdate(DocumentEvent e) {
        validateInputs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#removeUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void removeUpdate(DocumentEvent e) {
        validateInputs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.DocumentListener#changedUpdate(javax.swing.event.
     * DocumentEvent)
     */
    @Override
    public void changedUpdate(DocumentEvent e) {
        validateInputs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.event.ListDataListener#intervalAdded(javax.swing.event.
     * ListDataEvent)
     */
    @Override
    public void intervalAdded(ListDataEvent e) {
        validateInputs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ListDataListener#intervalRemoved(javax.swing.event.
     * ListDataEvent)
     */
    @Override
    public void intervalRemoved(ListDataEvent e) {
        validateInputs();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.event.ListDataListener#contentsChanged(javax.swing.event.
     * ListDataEvent)
     */
    @Override
    public void contentsChanged(ListDataEvent e) {
        validateInputs();
    }
}
