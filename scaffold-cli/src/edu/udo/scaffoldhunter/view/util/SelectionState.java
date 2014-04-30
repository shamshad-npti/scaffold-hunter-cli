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

package edu.udo.scaffoldhunter.view.util;

import java.awt.Color;

import edu.udo.scaffoldhunter.model.GlobalConfig;

/**
 * The selection state of a scaffold, based on its molecules.
 * 
 * @author Henning Garus
 */
public enum SelectionState {
    /** no Molecules are selected */
    UNSELECTED {
        @Override
        public Color getColor(GlobalConfig config) {
            return config.getUnselectedColor();
        }
    },
    /** at least one Molecule is selected, but not all */
    HALFSELECTED {
        @Override
        public Color getColor(GlobalConfig config) {
            return config.getPartiallySelectedColor();
        }
    },
    /** all Molecules are selected */
    SELECTED {
        @Override
        public Color getColor(GlobalConfig config) {
            return config.getSelectedColor();
        }
    };

    /**
     * @param config
     *            the global configuration
     * @return The color which is associated with the selection state.
     * 
     */
    public abstract Color getColor(GlobalConfig config);

}