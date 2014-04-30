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

package edu.udo.scaffoldhunter.view.scaffoldtree;

import java.io.Serializable;
import java.util.Arrays;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import edu.umd.cs.piccolo.PNode;

/**
 * Specifies different semantic zoom levels, which are used to render scaffolds
 * in a different manner depending on how far the user has zoomed in. 
 * <p>
 * Threshold values are scale values, so the higher the value the closer the
 * user has zoomed in.
 * 
 * @author Henning Garus
 * 
 */
enum SemanticZoomLevel {
    VERY_CLOSE(2.5), CLOSE(0.14), MEDIUM(0.09), DISTANT(0);

    private static final ThresholdOrdering order = new ThresholdOrdering();
    private static final ImmutableList<SemanticZoomLevel> levels = order.reverse().immutableSortedCopy(
            Arrays.asList(SemanticZoomLevel.values()));

    private final double threshold;

    /**
     * 
     */
    SemanticZoomLevel(double threshold) {
        this.threshold = threshold;
    }

    double getThreshold() {
        return threshold;
    }

    boolean scaleIsBelowThreshold(PNode node) {
        return scaleIsBelowThreshold(node.getScale());
    }

    boolean scaleIsBelowThreshold(double scale) {
        return scale < threshold;
    }

    static SemanticZoomLevel getByThreshold(double zoomScale) {
        for (SemanticZoomLevel l : levels)
            if (zoomScale > l.threshold)
                return l;
        return levels.get(levels.size());
    }

    static class ThresholdOrdering extends Ordering<SemanticZoomLevel> implements Serializable {

        /*
         * (non-Javadoc)
         * 
         * @see com.google.common.collect.Ordering#compare(java.lang.Object,
         * java.lang.Object)
         */
        @Override
        public int compare(SemanticZoomLevel left, SemanticZoomLevel right) {
            return Double.compare(left.threshold, right.threshold);
        }

    }
}
