/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.gui.clustering;

import static edu.udo.scaffoldhunter.util.I18n._;

import java.util.AbstractMap.SimpleEntry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.model.clustering.ClusteringException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Filterset;
import edu.udo.scaffoldhunter.model.db.Property;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * Worker to filter a {@link Subset} to defined {@link Property}s only.
 * 
 * @author Till Schäfer
 */
public class FilterDefinedWorker extends ProgressWorker<SimpleEntry<Boolean, Subset>, Void> {
    private static Logger logger = LoggerFactory.getLogger(FilterDefinedWorker.class);

    private final Subset subset;
    private final Filterset filterset;
    private final DbManager db;

    FilterDefinedWorker(Subset subset, Filterset filterset, DbManager db) {
        super();
        this.subset = subset;
        this.filterset = filterset;
        this.db = db;
    }

    /**
     * @return the filtered {@link Subset} and a boolean value which indicates
     *         if the filtered subset is smaller than the original subset.
     * 
     * @see edu.udo.scaffoldhunter.gui.util.ProgressWorker#doInBackground
     */
    @Override
    protected SimpleEntry<Boolean, Subset> doInBackground() throws Exception {
        super.setProgressIndeterminate(true);

        /*
         * if a distance measure is used that does not need any property the
         * condition is trivially true
         */
        if (filterset.getFilters().size() == 0) {
            return new SimpleEntry<Boolean, Subset>(false, subset);
        }

        // filter subset on database level
        Subset filteredSubset = db.getFilteredSubset(subset, filterset);

        if (filteredSubset.size() == 0) {
            logger.warn("filtered Subset has size 0");
            throw new ClusteringException(_("Clustering.Error.Empty"));
        }

        boolean isFiltered = filteredSubset.size() != subset.size();

        return new SimpleEntry<Boolean, Subset>(isFiltered, isFiltered ? filteredSubset : subset);
    }
}