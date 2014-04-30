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

package edu.udo.scaffoldhunter.model.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * A {@link HierarchicalClusterNode} is a tree node in the dendrogram which is
 * the result of a {@link HierarchicalClustering}.<br>
 * 
 * <b>Attention</b>: Be careful when using the same
 * {@link HierarchicalClusterNode} in different {@link NNSearch} modules.
 * Normally these modules need exclusive access to the property externalId.
 * 
 * @author Philipp Kopp
 * @author Till Schäfer
 * @param <S>
 *            molecule or scaffold
 */
public class HierarchicalClusterNode<S extends Structure> implements Serializable {
    private static Logger logger = LoggerFactory.getLogger(HierarchicalClusterNode.class);
    
    private HierarchicalClusterNode<S> leftChild;
    private HierarchicalClusterNode<S> rightChild;
    private HierarchicalClusterNode<S> parent;
    private double dissimilarity;
    transient private S content;
    private int clusterSize = 1;
    private int contentDbId = -1;
    /**
     * This id is needed for a deterministic comparison of two
     * {@link HierarchicalClusterNode}s (@see {@link TreeSetDistanceList} and @see
     * {@link BestFrontierNNSearch})
     */
    private UUID uniqueID = UUID.randomUUID();

    /**
     * Used to store some external id. This is often needed for indexing. E.g.
     * using arrays to store some additional informations about the
     * {@link HierarchicalClusterNode}.
     */
    private int externalId;

    /**
     * Constructor for leafs (no children are set) and default cluster size 1
     * 
     * Warning: Only use this Constructor for leafs, otherwise unexpected
     * behavior can occur.
     * 
     * @param content
     *            the {@link Structure} which belongs to the cluster node
     */
    public HierarchicalClusterNode(S content) {
        Preconditions.checkNotNull(content, "A leafs content must not be null");
        this.content = content;
        contentDbId = content.getId();

        // by convention leafs have dissimilarity 0
    }

    /**
     * Constructor for cluster size 1
     * 
     * @param leftChild
     *            the left child
     * @param rightChild
     *            the right child
     * @param dissimilarity
     *            the difference between left and right child
     */
    public HierarchicalClusterNode(HierarchicalClusterNode<S> leftChild, HierarchicalClusterNode<S> rightChild,
            double dissimilarity) {
        Preconditions.checkNotNull(leftChild);
        Preconditions.checkNotNull(rightChild);

        this.leftChild = leftChild;
        this.leftChild.setParent(this);
        this.rightChild = rightChild;
        this.rightChild.setParent(this);
        this.dissimilarity = dissimilarity;
        contentDbId = Math.min(leftChild.getContentDbId(), rightChild.getContentDbId());
    }

    /**
     * Constructor
     * 
     * @param leftChild
     *            the left child
     * @param rightChild
     *            the right child
     * @param dissimilarity
     *            the difference between left and right child
     * @param clusterSize
     *            the cluster size
     */
    public HierarchicalClusterNode(HierarchicalClusterNode<S> leftChild, HierarchicalClusterNode<S> rightChild,
            double dissimilarity, int clusterSize) {
        Preconditions.checkNotNull(leftChild);
        Preconditions.checkNotNull(rightChild);

        this.leftChild = leftChild;
        this.leftChild.setParent(this);
        this.rightChild = rightChild;
        this.rightChild.setParent(this);
        this.dissimilarity = dissimilarity;
        this.clusterSize = clusterSize;
        contentDbId = Math.min(leftChild.getContentDbId(), rightChild.getContentDbId());
    }

    /**
     * Constructor
     * 
     * @param leftChild
     *            the left child
     * @param rightChild
     *            the right child
     * @param dissimilarity
     *            the difference between left and right child
     * @param clusterSize
     *            the cluster size
     * @param content
     *            the {@link Structure} which belongs to the cluster node
     */
    public HierarchicalClusterNode(HierarchicalClusterNode<S> leftChild, HierarchicalClusterNode<S> rightChild,
            double dissimilarity, int clusterSize, S content) {
        Preconditions.checkNotNull(leftChild);
        Preconditions.checkNotNull(rightChild);

        this.leftChild = leftChild;
        this.leftChild.setParent(this);
        this.rightChild = rightChild;
        this.rightChild.setParent(this);
        this.dissimilarity = dissimilarity;
        this.clusterSize = clusterSize;
        this.content = content;
        contentDbId = Math.min(leftChild.getContentDbId(), rightChild.getContentDbId());
    }

    /**
     * Constructor
     */
    protected HierarchicalClusterNode() {

    }

    /**
     * @return true if the node is a leaf
     */
    public boolean isLeaf() {
        return (leftChild == null && rightChild == null);
    }

    /**
     * @return the parent
     */
    public HierarchicalClusterNode<S> getParent() {
        return parent;
    }

    /**
     * @param parent
     *            the parent to set
     */
    void setParent(HierarchicalClusterNode<S> parent) {
        if (parent == null) {
            throw new NullPointerException(this.getClass().toString() + ": parent must not be null");
        }
        this.parent = parent;
    }

    /**
     * @param content
     *            the content to set
     */
    public void setContent(S content) {
        this.content = content;
    }

    /**
     * @return the content
     */
    public S getContent() {
        return content;
    }

    /**
     * @return the leftChild
     */
    public HierarchicalClusterNode<S> getLeftChild() {
        return leftChild;
    }

    /**
     * @return the rightChild
     */
    public HierarchicalClusterNode<S> getRightChild() {
        return rightChild;
    }

    /**
     * @return the dissimilarity
     */
    public double getDissimilarity() {
        return dissimilarity;
    }

    /**
     * @return a list of all structures in the leafs of this DendrogramModelNode
     */
    public List<S> getStructuresInLeafs() {
        List<S> structures = new LinkedList<S>();
        addStructuresToList(structures);

        assert structures.size() == getClusterSize();

        return structures;
    }

    /**
     * @return the clusterSize
     */
    public int getClusterSize() {
        return clusterSize;
    }

    /**
     * @param clusterSize
     *            the clusterSize to set
     */
    public void setClusterSize(int clusterSize) {
        this.clusterSize = clusterSize;
    }

    // TODO: logging

    /**
     * @return the matrixId
     */
    public int getExternalId() {
        return externalId;
    }

    /**
     * @param matrixId
     *            the matrixId to set
     */
    public void setExternalId(int matrixId) {
        this.externalId = matrixId;
    }

    /**
     * switches the children e.g. for sorting
     */
    public void switchChildren() {
        HierarchicalClusterNode<S> temp = leftChild;
        leftChild = rightChild;
        rightChild = temp;
    }

    /**
     * @return the db id of the structure
     */
    public int getContentDbId() {
        if (isLeaf()) {
            return contentDbId;
        }
        return 0;
    }

    /**
     * Returns a UUID for this object. This is randomly generated at creation
     * time
     * 
     * @return the uniqueID
     */
    public UUID getUniqueID() {
        return uniqueID;
    }

    /**
     * sorts the tree recursively by the db Id in the leafs the child with the
     * minimal id stays/becomes the left child
     * 
     * @return minimal id of this tree
     */
    public double sortById() {
        double leftMinId, rightMinId;
        if (isLeaf()) {
            return contentDbId;
        } else {
            leftMinId = leftChild.sortById();
            rightMinId = rightChild.sortById();
            if (leftMinId < rightMinId) {
                return leftMinId;
            } else {
                switchChildren();
                return rightMinId;
            }
        }
    }

    /**
     * Assign a continuous unique externalId to each
     * {@link HierarchicalClusterNode} in nodes
     * 
     * @param nodes
     *            the nodes to assign the IDs to
     */
    public static <S extends Structure> void assignHcnIds(Collection<HierarchicalClusterNode<S>> nodes) {
        int counter = 0;
        for (HierarchicalClusterNode<S> hcn : nodes) {
            hcn.setExternalId(counter++);
        }
    }

    /**
     * Returns for each level a {@link List} with the
     * {@link HierarchicalClusterNode}s of this level.
     * 
     * Level 0 contains only the root node. Level (subset.getMolecules().size()
     * - 1) contains all singleton clusters.
     * 
     * @param stepWith
     *            Each n-th level should be measured. i.e. stepWith=1 means
     *            every level is measured.
     * @return the levels
     */
    public ArrayList<LinkedList<HierarchicalClusterNode<S>>> getAllLevels(int stepWith) {

        int clusterSize = getClusterSize();
        ArrayList<LinkedList<HierarchicalClusterNode<S>>> levels = Lists.newArrayListWithExpectedSize(clusterSize);

        LinkedList<HierarchicalClusterNode<S>> level = Lists.newLinkedList();
        level.add(this);
        levels.add(level);

        while (level.size() != clusterSize) {
            LinkedList<HierarchicalClusterNode<S>> nextLevel = Lists.newLinkedList();
            double maxDist = Double.NEGATIVE_INFINITY;
            HierarchicalClusterNode<S> maxNode = null;

            // Find the maximal node (it should be splitted next)
            for (HierarchicalClusterNode<S> node : level) {
                if (node.getDissimilarity() > maxDist && !node.isLeaf()) {
                    maxDist = node.getDissimilarity();
                    maxNode = node;
                }
            }

            logger.debug("maxNode distance: {}", maxDist);

            // nextLevel has all nodes of level, but maxNode which is splitted
            for (HierarchicalClusterNode<S> node : level) {
                if (node != maxNode) {
                    nextLevel.add(node);
                } else {
                    nextLevel.add(maxNode.getLeftChild());
                    nextLevel.add(maxNode.getRightChild());
                }
            }

            level = nextLevel;
            if (level.size() % stepWith == 0) {
                levels.add(level);
            }
        }

        return levels;
    }

    private void addStructuresToList(List<S> list) {
        if (isLeaf()) {
            list.add(getContent());
        } else {
            getLeftChild().addStructuresToList(list);
            getRightChild().addStructuresToList(list);
        }
    }
}
