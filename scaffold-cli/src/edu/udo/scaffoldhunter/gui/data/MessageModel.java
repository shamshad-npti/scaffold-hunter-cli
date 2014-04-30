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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import edu.udo.scaffoldhunter.model.data.Job;
import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageType;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * A tree model containing {@link Message}s. <br>
 * Tree levels:
 * <ol>
 * <li>a placeholder root
 * <li>import jobs (ordered by insertion)
 * <li>message types (ordered alphabetically)
 * <li>property definitions (where applicable otherwise messages) (ordered by
 * insertion)
 * <li>messages (ordered by insertion)
 * </ol>
 * 
 * @author Henning Garus
 * 
 */
public class MessageModel implements TreeModel {

    private static final Logger logger = LoggerFactory.getLogger(MessageModel.class);

    private List<TreeModelListener> listeners = Lists.newArrayList();
    private Map<Job, MessageTreeNode<Job>> jobs = Maps.newLinkedHashMap();

    // stupid placeholder since a treepath can't be empty
    private final Object root = new Object();

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getRoot()
     */
    @Override
    public Object getRoot() {
        return root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
     */
    @Override
    public Object getChild(Object parent, int index) {
        if (parent == root) {
            Object child = Iterables.get(jobs.values(), index, null);
            logger.trace("getChild at index {} : {}", index, child);
            return child;
        }
        if (!(parent instanceof MessageTreeNode)) {
            logger.trace("getChild at index {} : null", index);
            return null;
        }
        Object child = ((MessageTreeNode<?>) parent).getChild(index);
        logger.trace("getChild at index {} : {}", index, child);
        return child;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
     */
    @Override
    public int getChildCount(Object parent) {
        if (parent == root) {
            logger.trace("{} : {}", parent, jobs.size());
            return jobs.size();
        }
        if (!(parent instanceof MessageTreeNode<?>)) {
            logger.trace("{} : -1", parent);
            return -1;
        }
        int childcount = ((MessageTreeNode<?>) parent).getChildCount();
        logger.trace("{} : {}", parent, childcount);
        return childcount;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
     */
    @Override
    public boolean isLeaf(Object node) {
        logger.trace("isLeaf {}", node);
        if (!(node instanceof MessageTreeNode<?>))
            return false;
        return ((MessageTreeNode<?>) node).getValue() instanceof Message;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
     * java.lang.Object)
     */
    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        // should never happen so just ignore it
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
     * java.lang.Object)
     */
    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent == root) {
            int index = 0;
            for (Job j : jobs.keySet()) {
                if (j.equals(((MessageTreeNode<?>) child).getValue())) {
                    logger.trace("getIndexOfChild root, {} : {}", child, index);
                    return index;
                }
                index++;
            }

            logger.trace("getIndexOfChild root, {} : -1", child);
            return -1;

        }
        if (!(parent instanceof MessageTreeNode<?> && child instanceof MessageTreeNode<?>))
            return -1;
        int index = ((MessageTreeNode<?>) parent).getIndexOfChild((MessageTreeNode<?>) child);
        logger.trace("getIndexOfChild {}, {} : {}", new Object[] { root, child, index });
        return index;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
     * TreeModelListener)
     */
    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
     * TreeModelListener)
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    private static NodeValueByToStringOrdering valueByToString = new NodeValueByToStringOrdering();
    /**
     * Add a message to the model. lower level entries will be added as well if
     * they do not exist yet.
     * 
     * @param message
     *            the message to be added to the the model
     */
    public void addMessage(Message message) {
        MessageTreeNode<Job> jobNode = jobs.get(message.getJob());
        if (jobNode == null) {
            jobNode = MessageTreeNode.of(message.getJob());
            jobs.put(message.getJob(), jobNode);
            for (TreeModelListener l : listeners)
                l.treeNodesInserted(new TreeModelEvent(this, new Object[] { root }, new int[] { jobs.size() - 1 },
                        new Object[] { jobNode }));
        }
        MessageTreeNode<MessageType> typeNode = jobNode.getChildByValue(message.getType());
        if (typeNode == null) {
            typeNode = MessageTreeNode.of(message.getType());
            jobNode.addChild(typeNode);
            jobNode.sortChildren(valueByToString);
            for (TreeModelListener l : listeners)
                l.treeStructureChanged(new TreeModelEvent(this, new Object[] { root, jobNode }));
        }

        MessageTreeNode<Message> messageNode = MessageTreeNode.of(message);
        if (message.getPropertyDefinition() != null) {
            MessageTreeNode<PropertyDefinition> propertyNode = typeNode
                    .getChildByValue(message.getPropertyDefinition());
            if (propertyNode == null) {
                propertyNode = MessageTreeNode.of(message.getPropertyDefinition());
                int index = typeNode.addChild(propertyNode);
                for (TreeModelListener l : listeners)
                    l.treeNodesInserted(new TreeModelEvent(this, new Object[] { root, jobNode, typeNode },
                            new int[] { index }, new Object[] { propertyNode }));
            }

            int index = propertyNode.addChild(messageNode);

            for (TreeModelListener l : listeners) {
                l.treeNodesInserted(new TreeModelEvent(this, new Object[] { root, jobNode, typeNode, propertyNode },
                        new int[] { index }, new Object[] { messageNode }));
                l.treeNodesChanged(new TreeModelEvent(this, new Object[] { root, jobNode },
                        new int[] { getIndexOfChild(jobNode, typeNode) }, new Object[] { typeNode }));
                l.treeNodesChanged(new TreeModelEvent(this, new Object[] { root, jobNode, typeNode },
                        new int[] { getIndexOfChild(typeNode, propertyNode) }, new Object[] { propertyNode }));
            }
        } else {
            int index = typeNode.addChild(messageNode);
            for (TreeModelListener l : listeners) {
                l.treeNodesInserted(new TreeModelEvent(this, new Object[] { root, jobNode, typeNode },
                        new int[] { index }, new Object[] { messageNode }));
                l.treeNodesChanged(new TreeModelEvent(this, new Object[] { root, jobNode },
                        new int[] { getIndexOfChild(jobNode, typeNode) }, new Object[] { typeNode }));
            }
        }

    }

    /**
     * A node of this tree model. A node contains some value and a list of
     * children. The type of the children cannot be retrieved directly.
     * 
     * @author Henning Garus
     * 
     * @param <T>
     *            the type of the node's value
     */
    public static class MessageTreeNode<T> {

        private final T value;
        private final List<MessageTreeNode<?>> children = Lists.newArrayList();

        private MessageTreeNode(T value) {
            this.value = value;
        }

        private static <T> MessageTreeNode<T> of(T value) {
            return new MessageTreeNode<T>(value);
        }

        /**
         * 
         * @return the number of children of this node
         */
        public int getChildCount() {
            return children.size();
        }

        /**
         * 
         * @return this node's value
         */
        public T getValue() {
            return value;
        }

        private <V> MessageTreeNode<V> getChildByValue(V value) {
            for (MessageTreeNode<?> c : children) {
                if (c.getValue().equals(value)) {
                    @SuppressWarnings("unchecked")
                    MessageTreeNode<V> ret = (MessageTreeNode<V>) c;
                    return ret;
                }
            }
            return null;
        }

        private int getIndexOfChild(MessageTreeNode<?> child) {
            return children.indexOf(child);
        }

        private MessageTreeNode<?> getChild(int index) {
            return children.get(index);
        }

        private int addChild(MessageTreeNode<?> child) {
            children.add(child);
            return children.size() - 1;
        }

        /**
         * 
         * @return the number of messages located in the subtree rooted at this
         *         node.
         */
        public int getNumMessages() {
            if (children.size() > 0 && children.get(0).getValue() instanceof Message)
                return children.size();
            int sum = 0;
            for (MessageTreeNode<?> n : children) {
                sum += n.getNumMessages();
            }
            return sum;
        }

        private void sortChildren(Comparator<MessageTreeNode<?>> comparator) {
            Collections.sort(children, comparator);
        }

        @Override
        public String toString() {
            return value.toString();
        }

    }

    private static class NodeValueByToStringOrdering extends Ordering<MessageTreeNode<?>> implements Serializable {
        /*
         * (non-Javadoc)
         * 
         * @see com.google.common.collect.Ordering#compare(java.lang.Object,
         * java.lang.Object)
         */
        @Override
        public int compare(MessageTreeNode<?> left, MessageTreeNode<?> right) {
            return left.getValue().toString().compareTo(right.getValue().toString());
        }
    }
}
