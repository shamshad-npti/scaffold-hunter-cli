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

package edu.udo.scaffoldhunter.gui.data;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.udo.scaffoldhunter.gui.data.MessageModel.MessageTreeNode;
import edu.udo.scaffoldhunter.model.data.Job;
import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageType;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.util.Resources;

/**
 * A Tree cell renderer for rendering Trees based on {@link MessageModel}s.
 * 
 * @author Henning Garus
 *
 */
public class MessageTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static final Icon JOB_ICON = Resources.getIcon("edit-paste.png");
    private static final Icon PROPERTY_DEFINITION_ICON = Resources.getIcon("edit-find-replace.png");

    

    /* (non-Javadoc)
     * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
            boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (!(value instanceof MessageTreeNode<?>))
            return this;
        MessageTreeNode<?> node = (MessageTreeNode<?>)value;
        if (node.getValue() instanceof Message) {
            Message message = (Message)node.getValue();
            setText(message.getType().getMessageString(message));
            setIcon(message.getType().getIcon());
            return this;
        }
        if (node.getValue() instanceof PropertyDefinition) {
            PropertyDefinition propDef = (PropertyDefinition)node.getValue();
            setText(propDef.getTitle() + " (" + node.getNumMessages() +")");
            setIcon(PROPERTY_DEFINITION_ICON);
            return this;
        }
        if (node.getValue() instanceof MessageType) {
            MessageType type = (MessageType)node.getValue();
            setText(type.toString() + " (" + node.getNumMessages() + ")");
            setIcon(type.getIcon());
            return this;
        }
        if (node.getValue() instanceof Job) {
            setIcon(JOB_ICON);
            return this;
        }
        return this;
    }

}
