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

package edu.udo.scaffoldhunter.view.util;

import java.awt.Color;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Structure;

/**
 * A cache which provides <code>SVG</code>s given a <code>Structure</code> and
 * an optional <code>Color</code>. Entries will be automatically discarded if
 * they have not been accessed for some time.
 * 
 * @author Henning Garus
 * @author Till Schäfer
 */
public class SVGCache {
    private static Logger logger = LoggerFactory.getLogger(SVGCache.class);

    private final DbManager db;
    private final LoadingCache<Key, SVG> entries;

    /**
     * Constructor
     * 
     * @param db
     *            the DB manager
     */
    public SVGCache(final DbManager db) {
        this.db = db;
        entries = getNewLoadingCache(db, 500);
    }

    /**
     * Constructor
     * 
     * @param db
     *            the DB manager
     * @param maxSize
     *            the maximum size of the svg cache
     */
    public SVGCache(final DbManager db, int maxSize) {
        this.db = db;
        entries = getNewLoadingCache(db, maxSize);
    }

    /**
     * Creates a LoadingCache instance
     * 
     * @param db
     *            the DB manager
     * @param maxSize
     *            the maximum size if the svg cache
     * @return a new instance
     */
    private LoadingCache<Key, SVG> getNewLoadingCache(final DbManager db, int maxSize) {
        return CacheBuilder.newBuilder().concurrencyLevel(1).softValues().maximumSize(maxSize)
                .build(new CacheLoader<Key, SVG>() {

                    @Override
                    public SVG load(Key key) throws Exception {
                        return new SVG(db, key.structure, key.color, key.background, key.coloredChars, null, true);
                    }

                });
    }

    /**
     * Tries to retrieve a <code>SVG</code> for the given <code>Structure</code>
     * and <code>Color</code> from the cache. If none is found a new one will be
     * created and put into the cache.
     * 
     * @param structure
     *            the structure, which the <code>SVG</code> will visualize
     * @param color
     *            the color which will be applied to the <code>SVG</code>. If
     *            <code>color == null</code> no color will be applied.
     * @param background
     *            the color of the background, if it is null the background will
     *            be transparent
     * @param observer
     *            an observer, which will be added to the <code>SVG</code> to
     *            support background loading.
     * @return A <code>SVG</code> visualizing the given <code>Structure</code>
     *         colored in the specified color or in its default colors should
     *         <code>color</code> be </code>null</code>.
     * 
     * @see SVG
     */
    public SVG getSVG(Structure structure, Color color, Color background, SVGLoadObserver observer) {
        return getSVG(structure, color, background, true, observer);
    }

    /**
     * Tries to retrieve a <code>SVG</code> for the given <code>Structure</code>
     * and <code>Color</code> from the cache. If none is found a new one will be
     * created and put into the cache.
     * 
     * @param structure
     *            the structure, which the <code>SVG</code> will visualize
     * @param color
     *            the color which will be applied to the <code>SVG</code>. If
     *            <code>color == null</code> no color will be applied.
     * @param background
     *            the color of the background, if it is null the background will
     *            be transparent
     * @param coloredChars
     *            <code>true</code> if the characters denoting some molecules
     *            should be colored, otherwise these characters have the same
     *            color as the bonds
     * @param observer
     *            an observer, which will be added to the <code>SVG</code> to
     *            support background loading.
     * @return A <code>SVG</code> visualizing the given <code>Structure</code>
     *         colored in the specified color or in its default colors should
     *         <code>color</code> be </code>null</code>.
     * 
     * @see SVG
     */
    public SVG getSVG(Structure structure, Color color, Color background, boolean coloredChars, SVGLoadObserver observer) {
        SVG svg = null;
        try {
            svg = entries.get(new Key(structure, color, background, coloredChars));
        } catch (ExecutionException e) {
            logger.error("\"new SVG()\" throwed an exception. This should never happen!");
            throw new RuntimeException("\"new SVG()\" throwed an exception. This should never happen!");
        }
        svg.addObserver(observer);
        return svg;
    }

    /**
     * Load the SVG synchronously instead of in a seperate thread.
     * 
     * @param structure
     *            the structure, which the <code>SVG</code> will visualize
     * @param color
     *            the color which will be applied to the <code>SVG</code>. If
     *            <code>color == null</code> no color will be applied.
     * @param background
     *            the color of the background, if it is null the background will
     *            be transparent
     * @return A <code>SVG</code> visualizing the given <code>Structure</code>
     *         colored in the specified color or in its default colors should
     *         <code>color</code> be </code>null</code>.
     * 
     */
    public SVG getSVGSynchronous(Structure structure, Color color, Color background) {
        return new SVG(db, structure, color, background, true, null, false);
    }

    private static class Key {

        private Structure structure;
        private Color color;
        private Color background;
        private boolean coloredChars;

        private Key(Structure structure, Color color, Color background, boolean coloredChars) {
            this.structure = structure;
            this.color = color;
            this.background = background;
            this.coloredChars = coloredChars;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((background == null) ? 0 : background.hashCode());
            result = prime * result + ((color == null) ? 0 : color.hashCode());
            result = prime * result + (coloredChars ? 1231 : 1237);
            result = prime * result + ((structure == null) ? 0 : structure.hashCode());
            return result;
        }

        /*
         * (non-Javadoc)
         * 
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            Key other = (Key) obj;
            if (background == null) {
                if (other.background != null)
                    return false;
            } else if (!background.equals(other.background))
                return false;
            if (color == null) {
                if (other.color != null)
                    return false;
            } else if (!color.equals(other.color))
                return false;
            if (coloredChars != other.coloredChars)
                return false;
            if (structure == null) {
                if (other.structure != null)
                    return false;
            } else if (!structure.equals(other.structure))
                return false;
            return true;
        }

    }

}
