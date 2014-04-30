
package edu.udo.scaffoldhunter.model.filtering.subsearch.match.pattern;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Labels used to represent the matching behavior of labels including
 * wildcards.
 */
public interface GraphQueryLabel {

    /**
     * @param o a label
     * @return true iff this label matches the given label
     */
    public boolean matches(Object o);

    /**
     * Represents the '*' wildcard label.
     */
    public class Wildcard implements GraphQueryLabel {
        /**
         * Symbol of wildcard label.
         */
        public static final String WILDCARD_LABEL = "*";

        @Override
        public boolean matches(Object o) {
            return true;
        }

        @Override
        public String toString() {
            return WILDCARD_LABEL;
        }
    }

    /**
     * Represents a list of labels.
     */
    public abstract class AbstractList implements GraphQueryLabel {

        HashSet<Object> labelList;

        /**
         * Initializes a new instance with an empty list of labels.
         */
        public AbstractList() {
            labelList = new HashSet<Object>();
        }

        /**
         * Initializes a new instance with the given list of labels.
         * @param labelList list of labels
         */
        public AbstractList(Collection<Object> labelList) {
            super();
            setLabelList(labelList);
        }

        /**
         * Initializes a new instance with the given list of labels.
         * @param labelList list of labels
         */
        public AbstractList(Object[] labelList) {
            super();
            setLabelList(labelList);
        }

        /**
         * Changes the list of labels.
         * @param labelList the new list of labels
         */
        public void setLabelList(Collection<Object> labelList) {
            this.labelList.clear();
            this.labelList.addAll(labelList);
        }

        /**
         * Changes the list of labels.
         * @param labelList the new list of labels
         */
        public void setLabelList(Object[] labelList) {
            this.labelList.clear();
            for (Object o : labelList) {
                this.labelList.add(o);
            }
        }

        /**
         * @return the collection of labels
         */
        public HashSet<Object> getLabelList() {
            return labelList;
        }

        @Override
        public abstract boolean matches(Object o);

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append('[');
            if (!labelList.isEmpty()) {
                Iterator<Object> i = labelList.iterator();
                sb.append(i.next());
                while (i.hasNext()) {
                    sb.append(",");
                    sb.append(i.next());
                }
            }
            sb.append(']');
            return sb.toString();
        }
    }

    /**
     * Matches all labels of the given list.
     */
    public class List extends AbstractList {
        /**
         * Initializes a new instance with an empty list of allowed labels.
         */
        public List() {
            super();
        }

        /**
         * @param labelList allowed labels
         */
        public List(Collection<Object> labelList) {
            super(labelList);
        }

        /**
         * @param labelList allowed labels
         */
        public List(Object[] labelList) {
            super(labelList);
        }

        @Override
        public boolean matches(Object o) {
            return labelList.contains(o);
        }
    }

    /**
     * Matches all labels except those in the list.
     */
    public class NotList extends AbstractList {
        /**
         * Initializes a new instance with an empty list of forbidden labels.
         */
        public NotList() {
            super();
        }

        /**
         * @param labelList forbidden labels
         */
        public NotList(Collection<Object> labelList) {
            super(labelList);
        }

        /**
         * @param labelList forbidden labels
         */
        public NotList(Object[] labelList) {
            super(labelList);
        }

        @Override
        public boolean matches(Object o) {
            return !labelList.contains(o);
        }

        @Override
        public String toString() {
            return "!" + super.toString();
        }
    }

}
