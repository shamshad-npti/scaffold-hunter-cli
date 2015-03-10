/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2014 LS11
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

package edu.udo.scaffoldhunter.cli;

import java.util.Set;

import com.beust.jcommander.internal.Sets;
import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.Filter;
import edu.udo.scaffoldhunter.model.db.Filterset;
import edu.udo.scaffoldhunter.model.db.NumFilter;
import edu.udo.scaffoldhunter.model.db.Preset;
import edu.udo.scaffoldhunter.model.db.Profile;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * Manages presets. It has method to get all filterset in a profile, usable
 * filterset based on the properties in a dataset and and to check whether a
 * filterset is usable
 * 
 * @author Shamshad Alam
 * 
 */
public class PresetManager {
    private Profile profile;

    /**
     * Create a {@code PresetManager} object profile is set to null
     */
    public PresetManager() {
        this(null);
    }

    /**
     * @param profile
     */
    public PresetManager(Profile profile) {
        this.profile = profile;
    }

    /**
     * Get all usable filterset for the dataset. A filterset is usable when it
     * uses only those properties which are defined in dataset
     * 
     * @param dataset
     *            dataset to check usability
     * @return all usable filtesets
     * @throws NullPointerException
     *             when profile, dataset or both is null
     */
    public Set<Filterset> getUsableFiltersets(Dataset dataset) {
        return getUsableFiltersets(profile, dataset);
    }

    /**
     * Get all usable properties. A filterset is usable when it uses only those
     * properties which are defined in dataset
     * 
     * @param profile
     *            to retrieve filterset
     * @param dataset
     *            to get properties
     * @return all usable filtersets
     * @throws NullPointerException
     *             if profile or dataset is null
     */
    public static Set<Filterset> getUsableFiltersets(Profile profile, Dataset dataset) {
        Preconditions.checkNotNull(profile);
        Preconditions.checkNotNull(dataset);

        Set<Filterset> filtersets = Sets.newHashSet();

        // iterate over all presets
        for (Preset preset : profile.getPresets()) {
            // check whether preset is an instance of Filterset and it is usable
            if ((preset instanceof Filterset) && usable(dataset, (Filterset) preset)) {
                filtersets.add((Filterset) preset);
            }
        }
        return filtersets;
    }

    /**
     * @return All filtersets in current profile
     */
    public Set<Filterset> getAllFiltersets() {
        return getAllFiltersets(profile);
    }

    /**
     * Get all filtersets in current profile
     * 
     * @param profile
     * @return all
     */
    public static Set<Filterset> getAllFiltersets(Profile profile) {
        Preconditions.checkNotNull(profile);

        Set<Filterset> filtersets = Sets.newHashSet();

        // iterate over filterset
        for (Preset preset : profile.getPresets()) {
            // check whethet preset is instance of filterset
            if (preset instanceof Filterset) {
                filtersets.add((Filterset) preset);
            }
        }
        return filtersets;
    }

    /**
     * Check usability of filterset. A filterset is usable if it has only those
     * properties which is defined in dataset
     * 
     * @param dataset
     * @param filterset
     * @return true if filterset is usable to the dataset i.e. properties which
     *         are used by filters in the filterset belong to the dataset
     * @throws NullPointerException
     *             if either dataset or filterset is null
     */
    public static boolean isUsableFilterset(Dataset dataset, Filterset filterset) {
        Preconditions.checkNotNull(dataset);
        Preconditions.checkNotNull(filterset);

        return usable(dataset, filterset);
    }

    /**
     * check usability of filterset on the dataset
     * 
     * @param dataset
     *            dataset against which filterset is to be checked
     * @param filterset
     *            filterset to check
     * @return true if filterset can be used on dataset
     */
    private static boolean usable(Dataset dataset, Filterset filterset) {

        // iterate over all filters
        for (Filter f : filterset.getFilters()) {
            PropertyDefinition def = f.getPropDef(dataset);
            // check whether property is defined
            if (def == null)
                return false;

            // check for mismatched property types
            if (def.isStringProperty() && f.getClass() == NumFilter.class)
                return false;

        }
        return true;
    }
}
