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

package edu.udo.scaffoldhunter.model.dataimport;

import static edu.udo.scaffoldhunter.util.I18n._;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import edu.udo.scaffoldhunter.model.data.MessageType;
/**
 * Specifies different strategies for handling conflicts, which occur when
 * source properties from two sources are mapped to the same internal property
 * during import and the intersection of the molecules from both sources is not
 * empty. Since we merge sources iteratively we only handle two conflicting entries
 * at a time. 
 * <p>
 * <b>Example:</b> Two sources, such as an sd file and a database specify the property molecular
 * weight and some molecules are contained in both sources. The user maps both molecular
 * weight properties to the same <code>PropertyDefinition</code>, now we have conflicts.
 * 
 * @author Henning Garus
 *
 */
public enum MergeStrategy {
    
    /** 
     * No strategy specified. This should only be used to indicate a new 
     * Property which cannot produce conflicts ever.
     */
    NONE("NONE", null),
    /**
     * Overwrite existing entries, if we have new values.
     */
    OVERWRITE(_("DataImport.MergeStrategy.Overwrite"), MergeMessageTypes.OVERWRITE)
    {
        @Override
        public String apply(String first, String second) {
            return second == null ? first : second;
        }
        @Override
        public Double apply(Double first, Double second) {
            return second == null ? first : second;
        }
    },
    /**
     * Do not overwrite existing values, if there is already an entry just ignore
     * the new value.
     */
    DONT_OVERWRITE(_("DataImport.MergeStrategy.DontOverwrite"), MergeMessageTypes.DONT_OVERWRITE)
    {
        @Override
        public String apply(String first, String second) {
            return (first == null ? second : first);
        }
        @Override
        public Double apply(Double first, Double second) {
            return first == null ? second : first;
        }
    },
    /**
     * The new Value is produced by concatenating the old and the new value.
     * <i>because we can</i>
     */
    CONCATENATE(_("DataImport.MergeStrategy.Concatenate"), MergeMessageTypes.CONCATENATE)
    {
        @Override
        public String apply(String first, String second) {
            return Strings.nullToEmpty(first) + second;
        }        
    },
    /**
     * The new value is the minimum of the old and the new value.
     */
    MIN(_("DataImport.MergeStrategy.Minimum"), MergeMessageTypes.MINIMUM)
    {
        @Override
        public Double apply(Double first, Double second) {
            if (first == null)
                return second;
            else if (second == null)
                return first;
            return Math.min(first, second);
            
        }
    },
    /**
     * The new value is the maximum of the old and the new value. 
     */
    MAX(_("DataImport.MergeStrategy.Maximum"), MergeMessageTypes.MAXIMUM)
    
    {
        @Override
        public Double apply(Double first, Double second) {
            if (first == null)
                return second;
            else if (second == null)
                return first;
            return Math.max(first, second);
        }
    };
    
    private static final ImmutableSet<MergeStrategy> STRING_STRATEGIES = Sets.immutableEnumSet(OVERWRITE, DONT_OVERWRITE, CONCATENATE); 
    private static final ImmutableSet<MergeStrategy> NUM_STRATEGIES = Sets.immutableEnumSet(OVERWRITE, DONT_OVERWRITE, MIN, MAX);
    private final String toStringValue;
    private final MessageType messageType;
    
    private MergeStrategy(String toStringValue, MessageType messageType) {
        this.toStringValue = toStringValue;
        this.messageType = messageType;
    }
    
    /*
     * I tried to write this as a generic method but failed since you cannot
     * specialize generic methods in java. So concatenation and min/max are not
     * really possible since they return a specific type.
     */

    /**
     * Apply the merge strategy to the given values. This method is overridden 
     * by each strategy. If the types don't make sense for a strategy, such as
     * <b>String</b> for <code>MAX</code> an <code>AssertionError</code> will
     * be thrown.
     * @param first 
     * @param second 
     * @return the result of applying this strategy to both inputs
     **/
    public String apply(String first, String second) {
        throw new AssertionError();
    }

    /**
     * Apply the merge strategy to the given values. This method is overridden 
     * by each strategy. If the types don't make sense for a strategy, such as
     * <b>String</b> for <code>MAX</code> an <code>AssertionError</code> will
     * be thrown.
     * @param first 
     * @param second 
     * @return the result of applying this strategy to both inputs
     **/
    public Double apply(Double first, Double second) {
        throw new AssertionError();
    }
    
    /**
     * 
     * @return the message type associated with this MergeStrategy
     */
    public MessageType getMessageType() {
        return messageType;
    }
    
    
    
    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return toStringValue;
    }

    /**
     * 
     * @return a set of Strategies suitable for merging strings
     */
    public static ImmutableSet<MergeStrategy> getStringStrategies() {
        return STRING_STRATEGIES;
    }
    
    /**
     * 
     * @return a set of strategies suitable for merging numbers
     */
    public static ImmutableSet<MergeStrategy> getNumStrategies() {
        return NUM_STRATEGIES;
    }
    
    
}

