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
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.base.Predicates;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.ButtonBarFactory;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

import edu.udo.scaffoldhunter.gui.util.AbstractAction;
import edu.udo.scaffoldhunter.model.dataimport.ImportJob;
import edu.udo.scaffoldhunter.model.dataimport.ImportProcess;
import edu.udo.scaffoldhunter.model.dataimport.PropertyDefinitionList;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.util.SHPredicates;
import edu.udo.scaffoldhunter.plugins.dataimport.PluginResults;
import edu.udo.scaffoldhunter.util.Orderings;

/**
 * A dialog to set import mappings for {@link ImportProcess}
 * <p>
 * Changes to the <code>ImportJob</code>s will be made, even if the user leaves
 * the dialog by clicking "Cancel".
 * 
 * @author Henning Garus
 * @author Till Schäfer
 */
public class ImportMappingsDialog extends JDialog implements TableModelListener {

    private static final int PREVIEW_SIZE = 100;
    private static final int SCROLL_INCREMENT = 20;

    private final JScrollPane tablePane;
    private final JButton cancelButton;
    private final JButton okButton;
    private final JLabel descriptionLabel;
    private final JPanel previewPanel;
    private ImmutableList<JTable> tables;

    private Result result = Result.NONE;

    private final ImportProcess sources;
    private final PropertyDefinitionList propertyDefinitions;

    private HashMap<ImportJob, MappingTableModel> tableModels = Maps.newHashMap();
    private HashMap<ImportJob, ComboBoxModel> mergeIntoModels = Maps.newHashMap();

    /**
     * Create a new Import Mappings Dialog.
     * 
     * @param owner
     *            this dialog's owner
     * @param sources
     *            the sources represented by this dialog
     */
    public ImportMappingsDialog(Window owner, ImportProcess sources) {
        super(owner);

        this.sources = sources;

        this.propertyDefinitions = new PropertyDefinitionList(sources.getPropertyDefinitions());

        setModal(true);
        setTitle(_("ImportMappings.Title"));
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowCloseListener());

        FormLayout layout = new FormLayout("d, 5dlu, d, f:d:grow", "c:d, 5dlu, f:d:grow, 3dlu, center:d");

        PanelBuilder pb = new PanelBuilder(layout);
        pb.setDefaultDialogBorder();

        tablePane = new JScrollPane(buildTablePanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        tablePane.getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);

        cancelButton = new JButton(new CancelAction());
        okButton = new JButton(new OkAction());

        // restrict preferred width of the label to a sane value
        descriptionLabel = getDescriptionLabel();
        Dimension d = tablePane.getPreferredSize();
        d.height = 50;
        descriptionLabel.setPreferredSize(d);

        pb.add(descriptionLabel, CC.xyw(3, 1, 2));
        pb.add(tablePane, CC.xyw(3, 3, 2));
        previewPanel = new PreviewPanel();
        pb.add(previewPanel, CC.xywh(1, 3, 1, 3));
        pb.add(getPreviewToggle(), CC.xy(3, 5));
        pb.add(ButtonBarFactory.buildOKCancelBar(okButton, cancelButton), CC.xy(4, 5));

        add(pb.getPanel());

        getRootPane().setDefaultButton(okButton);
        pack();

        setLocationRelativeTo(owner);
    }

    /**
     * Check how the user has left this dialog.
     * 
     * @return the result
     */
    public Result getResult() {
        return result;
    }

    /**
     * This creates a {@link JTable} that is used to map each {@link Property}
     * from the {@link ImportJob} to a internal {@link Property}
     * 
     * @param source
     *            the {@link ImportJob}
     * @return a new {@link JTable}
     */
    private JTable getMappingTable(ImportJob source) {
        MappingTableModel tableModel = new MappingTableModel(source);
        tableModels.put(source, tableModel);

        JTable table = new JTable(tableModel);
        table.setBorder(BorderFactory.createEmptyBorder());

        /*
         * PropertyDefinition column
         */
        table.getColumnModel().getColumn(MappingTableModel.PROPERTY_DEFINITION_COLUMN)
                .setCellRenderer(new PropertyDefinitionRenderer(null));
        if (sources.getDataset() == null) {
            /*
             * If we are importing Properties into a new Dataset
             */
            table.getColumnModel()
                    .getColumn(MappingTableModel.PROPERTY_DEFINITION_COLUMN)
                    .setCellEditor(
                            new PropertyDefinitionCellEditor(this, propertyDefinitions, source.getResults()
                                    .getProbablyNumeric(), Collections.<PropertyDefinition> emptyList()));
        } else {
            /*
             * If we are merging Properties into an existing Dataset
             */
            Iterable<PropertyDefinition> existingPropDefs = Iterables.filter(sources.getDataset()
                    .getPropertyDefinitions().values(), Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF));
            table.getColumnModel()
                    .getColumn(MappingTableModel.PROPERTY_DEFINITION_COLUMN)
                    .setCellEditor(
                            new PropertyDefinitionCellEditor(this, propertyDefinitions, source.getResults()
                                    .getProbablyNumeric(), existingPropDefs));
        }

        /*
         * Merge strategy column
         */
        table.getColumnModel().getColumn(MappingTableModel.MERGE_STRATEGY_COLUMN)
                .setCellRenderer(new MergeStrategyRenderer(sources, source, null));
        table.getColumnModel().getColumn(MappingTableModel.MERGE_STRATEGY_COLUMN)
                .setCellEditor(new MergeStrategyEditor());

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(table.getRowHeight() + 10);
        table.setRowMargin(5);
        table.getColumnModel().setColumnMargin(5);
        table.getTableHeader().setReorderingAllowed(false);

        /*
         * sizing the columns
         */
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == MappingTableModel.PROPERTY_DEFINITION_COLUMN)
                table.getColumnModel()
                        .getColumn(i)
                        .setPreferredWidth(
                                table.getColumnModel().getColumn(MappingTableModel.SOURCE_PROPERTY_COLUMN)
                                        .getPreferredWidth());
            else
                packTableColumn(table, table.getColumnModel().getColumn(i));
        }
        return table;
    }

    private int packTableColumn(JTable table, TableColumn column) {
        int columnid = column.getModelIndex();
        int requiredWidth;
        TableCellRenderer renderer = column.getHeaderRenderer();
        if (renderer == null)
            renderer = table.getTableHeader().getDefaultRenderer();
        requiredWidth = renderer.getTableCellRendererComponent(table, column.getHeaderValue(), false, false, 0,
                columnid).getPreferredSize().width;

        renderer = column.getCellRenderer();
        if (renderer == null)
            renderer = table.getDefaultRenderer(Object.class);

        for (int i = 0; i < table.getRowCount(); i++) {
            Object value = table.getValueAt(i, columnid);
            if (value == null)
                continue;

            int w = renderer.getTableCellRendererComponent(table, value, false, false, i, columnid).getPreferredSize().width;
            requiredWidth = Math.max(requiredWidth, w);
        }
        column.setPreferredWidth(requiredWidth);
        return requiredWidth;
    }

    private JLabel getDescriptionLabel() {
        JLabel l = new JLabel();
        // use html so we get line wrapping...
        l.setText("<html>" + (_("ImportMappings.Description") + "</html>"));
        return l;
    }

    private JPanel buildTablePanel() {
        ImmutableList.Builder<JTable> listBuilder = ImmutableList.builder();
        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));

        for (ImportJob job : sources.getJobs()) {
            FormLayout layout = new FormLayout("right:p, 4dlu, 125dlu, 7dlu, right:p, 4dlu, 100dlu, 20dlu, p, p:g",
                    "p, 10dlu, p, 5dlu, p, p, p, 5dlu, p, p, 5dlu");

            PanelBuilder pb = new PanelBuilder(layout);
            pb.setDefaultDialogBorder();

            pb.addSeparator(String.format("%s (%s)", job.getJobName(), job.getPlugin().getTitle()), CC.xyw(1, 1, 10));

            pb.addLabel(_("ImportMappings.MoleculeTitle"), CC.xy(1, 3));
            pb.add(getTitlePropertyComboBox(job.getTitlePropertyModel()), CC.xy(3, 3));

            pb.addLabel(_("ImportMappings.MoleculeTitleMergeStrategy"), CC.xy(5, 3));
            pb.add(getTitleMergeStrategyComboBox(job.getTitleMergeStrategyModel()), CC.xy(7, 3));
            pb.addLabel(_("ImportMappings.StructureMergeStrategy"), CC.xy(5, 5));
            pb.add(getStructureMergeStrategyComboBox(job.getStructureMergeStrategyModel()), CC.xy(7, 5));
            pb.add(new JButton(new MapAllAction(job)), CC.xy(9, 3));

            JTable table = getMappingTable(job);
            pb.add(table.getTableHeader(), CC.xyw(1, 9, 10));
            pb.add(table, CC.xyw(1, 10, 10));
            listBuilder.add(table);
            table.getModel().addTableModelListener(this);

            if (sources.getDataset() != null) {
                pb.add(getSpacedTitledSeparator(_("ImportMappings.MergeProperty")), CC.xyw(1, 6, 10));

                // merge by property
                pb.addLabel(_("ImportMappings.MergeBy"), CC.xy(1, 7));
                JComboBox mergeByComboBox = getMergeByComboBox(job, table);
                pb.add(mergeByComboBox, CC.xy(3, 7));

                // merge to property
                pb.addLabel(_("ImportMappings.MergeTo"), CC.xy(5, 7));
                JComboBox mergeToComboBox = getMergeToComboBox(job, table, mergeByComboBox);
                pb.add(mergeToComboBox, CC.xy(7, 7));
                mergeIntoModels.put(job, mergeToComboBox.getModel());

                /*
                 * the renderer of the table must be aware of the
                 * mergeByComboBox state to color the cell
                 */
                table.setDefaultRenderer(Object.class, new ActivatableTableCellRenderer(mergeByComboBox));
                table.getColumnModel().getColumn(MappingTableModel.PROPERTY_DEFINITION_COLUMN)
                        .setCellRenderer(new PropertyDefinitionRenderer(mergeByComboBox));
                table.getColumnModel().getColumn(MappingTableModel.MERGE_STRATEGY_COLUMN)
                        .setCellRenderer(new MergeStrategyRenderer(sources, job, mergeByComboBox));
            }

            JPanel panel = (pb.getPanel());
            panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
            tablesPanel.add(panel);
        }

        tables = listBuilder.build();
        return tablesPanel;
    }

    /**
     * Constructs a titled {@link JSeparator}
     * 
     * @param label
     *            the title
     * @return the {@link JPanel} with the titled {@link JSeparator}
     */
    private JPanel getSpacedTitledSeparator(String label) {
        PanelBuilder pb = new PanelBuilder(new FormLayout("fill:pref:grow", "5dlu, bottom:pref, 3dlu"));
        pb.addSeparator(label, CC.xy(1, 2));

        return pb.getPanel();
    }

    private JComboBox getMergeByComboBox(ImportJob job, JTable table) {
        ComboBoxModel model = job.getMergeByModel();
        JComboBox box = new JComboBox(model);
        box.addActionListener(new MergeByListener(table));
        box.setRenderer(new CellRendererWithNullValue(_("DataImport.MergeByStructure")));

        box.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOkState();
            }
        });

        return box;
    }

    private JComboBox getMergeToComboBox(final ImportJob job, JTable table, JComboBox mergeByCombobox) {
        Iterable<PropertyDefinition> existingPropDefsIt = Iterables.filter(sources.getDataset()
                .getPropertyDefinitions().values(), Predicates.not(SHPredicates.IS_SCAFFOLD_PROPDEF));
        final Vector<PropertyDefinition> existingPropDefs = new Vector<PropertyDefinition>();
        Iterables.addAll(existingPropDefs, existingPropDefsIt);

        JComboBox mergeToComboBox = new JComboBox();
        final MergeToModel mergeToModel = new MergeToModel(existingPropDefs, mergeToComboBox, mergeByCombobox);
        mergeToComboBox.setModel(mergeToModel);

        mergeToComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateOkState();
                job.setInternalMergeBy((PropertyDefinition) mergeToModel.getSelectedItem());
            }
        });

        return mergeToComboBox;
    }

    private JComboBox getTitlePropertyComboBox(ComboBoxModel model) {
        JComboBox box = new JComboBox(model);
        box.setRenderer(new CellRendererWithNullValue(""));
        return box;
    }

    private JComboBox getTitleMergeStrategyComboBox(ComboBoxModel model) {
        JComboBox box = new JComboBox(model);
        return box;
    }

    private JComboBox getStructureMergeStrategyComboBox(ComboBoxModel model) {
        return new JComboBox(model);
    }

    private JToggleButton getPreviewToggle() {
        JToggleButton b = new JToggleButton();
        b.setText(_("ImportMappings.ShowPreview"));
        b.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    previewPanel.setVisible(true);
                    pack();
                } else {
                    previewPanel.setVisible(false);
                    pack();
                }
            }
        });
        return b;
    }

    @Override
    public void pack() {
        super.pack();
        Dimension d = getSize();
        d.height = 800;
        d.width += 50;
        setSize(d);

    }

    private void updateOkState() {
        boolean allMergePropertiesSelected = true;
        for (ComboBoxModel model : mergeIntoModels.values()) {
            if (model.getSelectedItem() == null) {
                allMergePropertiesSelected = false;
            }
        }

        okButton.setEnabled(!mergeIntoExistingDataset() || allMergePropertiesSelected);
    }

    private boolean mergeIntoExistingDataset() {
        return sources.getDataset() != null;
    }

    /**
     * The result of this dialog.
     */
    public static enum Result {
        /**
         * The dialog is still open, no result has been determined yet.
         */
        NONE,
        /**
         * The dialog has been closed by clicking cancel.
         */
        CANCELED,
        /**
         * The dialog has been closed by clicking OK.
         */
        OK
    }

    private class CancelAction extends AbstractAction {
        public CancelAction() {
            super(_("Button.Cancel"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result = Result.CANCELED;
            dispose();
        }
    }

    private class OkAction extends AbstractAction {
        public OkAction() {
            super(_("Button.OK"));
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            result = Result.OK;
            dispose();
        }
    }

    private class WindowCloseListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            result = Result.CANCELED;
            dispose();
        }
    }

    private class MapAllAction extends AbstractAction {

        private final ImportJob job;

        public MapAllAction(ImportJob job) {
            super(_("ImportMappings.MapUnmappedProperties"));
            this.job = job;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            job.mapAllUnmappedProperties(propertyDefinitions);
            tableModels.get(job).dataUpdated();
        }

    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e.getColumn() == MappingTableModel.PROPERTY_DEFINITION_COLUMN
                || e.getColumn() == MappingTableModel.MERGE_STRATEGY_COLUMN)
            for (JTable table : tables)
                table.repaint();
    }

    private class PreviewPanel extends JPanel implements ListSelectionListener {

        private final Multimap<PluginResults, IAtomContainer> previewCache = HashMultimap.create();
        private final JTable table;
        private PluginResults results = null;
        private String soureProperty = "";

        public PreviewPanel() {
            super(new BorderLayout());
            setVisible(false);
            table = new JTable(PREVIEW_SIZE, 1);
            table.setEnabled(false);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.getColumnModel().getColumn(0).setHeaderValue("");
            table.getColumnModel().getColumn(0).setPreferredWidth(200);
            table.setPreferredScrollableViewportSize(table.getPreferredSize());
            JScrollPane sp = new JScrollPane(table);
            add(sp, BorderLayout.CENTER);
            for (JTable t : tables)
                t.getSelectionModel().addListSelectionListener(this);
        }

        public void setPreview(PluginResults results, String sourceProperty) {
            this.results = results;
            this.soureProperty = sourceProperty;
            if (isVisible())
                updatePreview();
        }

        private void updatePreview() {
            if (results != null) {
                if (!previewCache.containsKey(results)) {
                    // copy PREVIEW_SIZE molecules into the cache
                    Iterables.addAll(previewCache.get(results), Iterables.limit(results.getMolecules(), PREVIEW_SIZE));
                }
                table.getColumnModel().getColumn(0).setHeaderValue(soureProperty);
                table.getTableHeader().resizeAndRepaint();
                int i = 0;
                for (IAtomContainer m : previewCache.get(results))
                    table.setValueAt(m.getProperty(soureProperty), i++, 0);
            }

        }

        @Override
        public void setVisible(boolean aFlag) {
            if (aFlag)
                updatePreview();
            super.setVisible(aFlag);
        }

        @Override
        public void valueChanged(ListSelectionEvent e) {
            DefaultListSelectionModel model = (DefaultListSelectionModel) e.getSource();
            // find the corresponding table (this or one listener per table...)
            JTable table = null;
            for (JTable t : tables) {
                if (t.getSelectionModel().equals(model)) {
                    table = t;
                    break;
                }
            }
            assert (table != null);

            if (table.getSelectedRow() == -1)
                return;
            int index = -1;
            for (int i = 0; i < tables.size(); i++)
                if (tables.get(i) != table)
                    tables.get(i).clearSelection();
                else
                    index = i;
            assert (index >= 0);
            setPreview(sources.getJobs().get(index).getResults(),
                    (String) table.getValueAt(table.getSelectedRow(), MappingTableModel.SOURCE_PROPERTY_COLUMN));
        }
    }

    private static class MergeByListener implements ActionListener {

        private final JTable table;

        MergeByListener(JTable table) {
            super();
            this.table = table;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            /*
             * Inform the PropertyDefinitionCellEditor and the table model about
             * the selected merge property
             */
            JComboBox comboBox = (JComboBox) e.getSource();
            String selected = (String) comboBox.getSelectedItem();
            MappingTableModel tableModel = (MappingTableModel) table.getModel();
            tableModel.setMappingProperty(selected);
            tableModel.dataUpdated();
        }
    }

    private class MergeToModel extends AbstractListModel implements ComboBoxModel {
        private Object mergeToProperty;
        private Vector<PropertyDefinition> existingPropDefs;
        private JComboBox mergeByCombobox;
        private JComboBox mergeToCombobox;

        /**
         * Constructor
         * 
         * @param existingPropDefs
         * @param mergeToCombobox
         * @param mergeByCombobox
         */
        @SuppressWarnings("unchecked")
        public MergeToModel(Vector<PropertyDefinition> existingPropDefs, final JComboBox mergeToCombobox,
                final JComboBox mergeByCombobox) {
            this.mergeToCombobox = mergeToCombobox;
            this.mergeByCombobox = mergeByCombobox;
            this.existingPropDefs = (Vector<PropertyDefinition>) existingPropDefs.clone();

            mergeToCombobox.setEnabled(mergeByCombobox.getSelectedIndex() != -1);
            sort();

            mergeByCombobox.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    sort();
                    mergeToCombobox.setEnabled(mergeByCombobox.getSelectedIndex() != -1);
                }
            });
        }

        /**
         * Sort the items and the selected mergeByItem to the top
         */
        private void sort() {
            /*
             * if there is an existing property definition with the same title
             * move it to the beginning of the vector
             */
            Collections.sort(existingPropDefs, Orderings.PROPERTY_DEFINITION_BY_TITLE);

            PropertyDefinition found = null;
            for (PropertyDefinition propDef : existingPropDefs) {
                Object selectedMergeByItem = mergeByCombobox.getModel().getSelectedItem();
                if (propDef.getTitle().equals(selectedMergeByItem)) {
                    found = propDef;
                    break;
                }
            }
            if (found != null) {
                existingPropDefs.remove(found);
                existingPropDefs.add(0, found);
                mergeToCombobox.setSelectedIndex(0);
            }
        }

        @Override
        public int getSize() {
            return existingPropDefs.size();
        }

        @Override
        public Object getElementAt(int index) {
            return existingPropDefs.get(index);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            mergeToProperty = (anItem != null ? anItem : null);
        }

        @Override
        public Object getSelectedItem() {
            return mergeToProperty;
        }
    }
}
