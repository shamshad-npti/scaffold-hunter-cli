/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

package edu.udo.scaffoldhunter.gui.dataimport;

import static edu.udo.scaffoldhunter.util.I18n._;

import javax.swing.table.AbstractTableModel;

import edu.udo.scaffoldhunter.model.dataimport.ImportJob;
import edu.udo.scaffoldhunter.model.dataimport.ImportJob.SourcePropertyMapping;
import edu.udo.scaffoldhunter.model.dataimport.MathFunction;
import edu.udo.scaffoldhunter.model.dataimport.MergeStrategy;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * Table model, which can be used to set the property mappings for an attached
 * source. In the table model there is one line for each property. The columns
 * are layed out as follows:
 * <ol>
 * <li>Source Properties (cannot be edited)
 * <li>Property Definitions the source properties are mapped to
 * <li>A transformFunction which is applied to each value of this source
 * property during import
 * <li>A merge strategy to determine behavior if a previous property (either
 * from this source or a preceding source) is mapped to the same property
 * definition
 * </ol>
 * 
 * @author Henning Garus
 * @author Till Schäfer
 */
class MappingTableModel extends AbstractTableModel {

    static final int SOURCE_PROPERTY_COLUMN = 0;
    static final int PROPERTY_DEFINITION_COLUMN = 1;
    static final int TRANSFORM_FUNCTION_COLUMN = 2;
    static final int MERGE_STRATEGY_COLUMN = 3;
    private ImportJob job;
    private String mappingPropName;

    public MappingTableModel(ImportJob job) {
        this.job = job;

    }

    public void setMappingProperty(String propName) {
        this.mappingPropName = propName;
    }

    @Override
    public int getRowCount() {
        return job.getPropertyMappingSize();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
        case SOURCE_PROPERTY_COLUMN:
            return _("ImportMappings.ImportedPropertyColumn");
        case PROPERTY_DEFINITION_COLUMN:
            return _("ImportMappings.InternalPropertyColumn");
        case TRANSFORM_FUNCTION_COLUMN:
            return _("ImportMappings.TransformFunctionColumn");
        case MERGE_STRATEGY_COLUMN:
            return _("ImportMappings.MergeStrategyColumn");
        default:
            throw new AssertionError();
        }

    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
        case SOURCE_PROPERTY_COLUMN:
            return String.class;
        case PROPERTY_DEFINITION_COLUMN:
            return PropertyDefinition.class;
        case TRANSFORM_FUNCTION_COLUMN:
            return MathFunction.class;
        case MERGE_STRATEGY_COLUMN:
            return MergeStrategy.class;
        default:
            throw new AssertionError();
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case SOURCE_PROPERTY_COLUMN:
            return false;
        case PROPERTY_DEFINITION_COLUMN:
            // the mappingProperty column is not editable
            return job.getPropertyName(rowIndex) != mappingPropName;
        case TRANSFORM_FUNCTION_COLUMN:
            /*
             * only editable if the property is numeric and not the mapping
             * Property
             */
            SourcePropertyMapping m = job.getPropertyMapping(rowIndex);
            return m.getPropertyDefiniton() != null && !m.getPropertyDefiniton().isStringProperty()
                    && job.getPropertyName(rowIndex) != mappingPropName;
        case MERGE_STRATEGY_COLUMN:
            /*
             * only editable if the property is already existing and the
             * property is not the mapping property
             */
            return job.getPropertyMapping(rowIndex).getPropertyDefiniton() != null
                    && job.getPropertyName(rowIndex) != mappingPropName;
        default:
            throw new AssertionError();
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (columnIndex == SOURCE_PROPERTY_COLUMN)
            return job.getPropertyName(rowIndex);

        SourcePropertyMapping m = job.getPropertyMapping(rowIndex);
        switch (columnIndex) {
        case PROPERTY_DEFINITION_COLUMN:
            return m.getPropertyDefiniton();
        case TRANSFORM_FUNCTION_COLUMN:
            return m.getTransformFunction();
        case MERGE_STRATEGY_COLUMN:
            return m.getMergeStrategy();
        default:
            throw new AssertionError();
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        SourcePropertyMapping m = job.getPropertyMapping(rowIndex);

        switch (columnIndex) {
        // SOURCE_PROPERTY_COLUMN is not editable
        case PROPERTY_DEFINITION_COLUMN:
            m.setPropertyDefiniton((PropertyDefinition) aValue);
            break;
        case TRANSFORM_FUNCTION_COLUMN:
            m.setTransformFunction((MathFunction) aValue);
            break;
        case MERGE_STRATEGY_COLUMN:
            m.setMergeStrategy((MergeStrategy) aValue);
            break;
        default:
            throw new AssertionError();
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    public void dataUpdated() {
        fireTableDataChanged();
    }
}
