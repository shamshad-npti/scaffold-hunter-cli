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

package edu.udo.scaffoldhunter.model.datacalculation;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListSelectionModel;

import org.openscience.cdk.interfaces.IAtomContainer;

import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.data.Message;
import edu.udo.scaffoldhunter.model.data.MessageListener;

/**
 * Represents all {@link CalcJob}s for one calc process and provides methods
 * which work on these calc sources on the whole.
 * 
 * @author Henning Garus
 * 
 */
public class CalcProcess implements MessageListener {
    private final List<CalcJob> calcJobs = Lists.newArrayList();
    private final List<MessageListener> messageListeners = Lists.newLinkedList();

    /**
     * Create a new CalcProcess to add properties to an existing dataset
     * 
     */
    public CalcProcess() {
    }

    /**
     * @return the list of calc sources represented by this class
     */
    public List<CalcJob> getJobs() {
        return Collections.unmodifiableList(calcJobs);
    }

    /**
     * prepares each job for the calc process
     * 
     * @param molecules
     *            an {@link Iterable} over the molecules for which properties
     *            should be generated
     */
    public void prepareCalc(Iterable<IAtomContainer> molecules) {
        Iterator<CalcJob> i = calcJobs.iterator();
        while (i.hasNext()) {
            CalcJob job = i.next();
            job.addMessageListener(this);
            job.computePluginResults(molecules);
        }
    }

    /**
     * 
     * @return a <code>JobsModel</code> allowing access to the jobs in this
     *         process through a ListModel interface.
     */
    public JobsModel getJobsModel() {
        return new JobsModel();
    }

    /**
     * A ListModel which holds all the jobs, which are part of this import
     * process and which provides additional methods to add and remove jobs and
     * to move jobs around in the list.
     */
    public class JobsModel extends AbstractListModel {

        private int selectedIndex = -1;

        @Override
        public int getSize() {
            return calcJobs.size();
        }

        @Override
        public Object getElementAt(int index) {
            if (index < 0 || index >= calcJobs.size())
                return null;
            return calcJobs.get(index);
        }

        /**
         * Add a job to the list
         * 
         * @param job
         *            job to be added to the list
         */
        public void add(CalcJob job) {
            calcJobs.add(job);
            fireIntervalAdded(this, calcJobs.size() - 1, calcJobs.size() - 1);
        }

        /**
         * Remove the currently selected element. If no element is selected do
         * nothing.
         */
        public void removeSelectedElement() {
            if (selectedIndex != -1) {
                calcJobs.remove(selectedIndex);
                fireIntervalRemoved(this, selectedIndex, selectedIndex);
                selectedIndex = -1;
            }
        }

        /**
         * Move the current selection upward/backward in the list. If no element
         * or the uppermost element is selected do nothing.
         */
        public void moveSelectionUp() {
            if (selectedIndex <= 0)
                return;
            CalcJob src = calcJobs.remove(selectedIndex);
            calcJobs.add(selectedIndex - 1, src);
            selectedIndex--;
            fireContentsChanged(this, selectedIndex, selectedIndex + 1);
        }

        /**
         * Move the current selection downward/forward in the list. If no
         * element or the uppermost element is selected do nothing.
         */
        public void moveSelectionDown() {
            if (selectedIndex == calcJobs.size() - 1 || selectedIndex == -1)
                return;
            CalcJob src = calcJobs.remove(selectedIndex);
            calcJobs.add(selectedIndex + 1, src);
            selectedIndex++;
            fireContentsChanged(this, selectedIndex - 1, selectedIndex);
        }

        /**
         * 
         * @return a list selection model holding a single selection from the
         *         JobsModel
         */
        public ListSelectionModel getListSelectionModel() {
            class SelectionModel extends DefaultListSelectionModel {

                @Override
                public int getSelectionMode() {
                    return SINGLE_SELECTION;
                }

                @Override
                public void clearSelection() {
                    int oldindex = selectedIndex;
                    selectedIndex = -1;
                    fireValueChanged(oldindex, oldindex);
                }

                private SelectionModel() {
                    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                }

                @Override
                public boolean isSelectedIndex(int index) {
                    return index == selectedIndex;
                }

                @Override
                public boolean isSelectionEmpty() {
                    return selectedIndex == -1;
                }

                @Override
                public int getLeadSelectionIndex() {
                    return selectedIndex;
                }

                @Override
                public void moveLeadSelectionIndex(int leadIndex) {
                    selectedIndex = leadIndex;
                    super.moveLeadSelectionIndex(leadIndex);
                }

                @Override
                public int getMinSelectionIndex() {
                    return selectedIndex;
                }

                @Override
                public int getMaxSelectionIndex() {
                    return selectedIndex;
                }

                @Override
                public void setSelectionInterval(int index0, int index1) {
                    int oldindex = selectedIndex;
                    selectedIndex = index0;
                    fireValueChanged(Math.min(oldindex, selectedIndex), Math.max(oldindex, selectedIndex));
                }
            }
            return new SelectionModel();
        }
    }

    /**
     * add a message listener
     * 
     * @param listener
     */
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }

    /**
     * remove a message listener
     * 
     * @param listener
     */
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    private void sendMessage(Message message) {
        for (MessageListener l : messageListeners)
            l.receiveMessage(message);
    }
    
    @Override
    public void receiveMessage(Message message) {
        // receive message from 
        sendMessage(message);
    }
}
