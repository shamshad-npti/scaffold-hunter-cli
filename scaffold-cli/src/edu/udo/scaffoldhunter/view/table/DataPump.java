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

package edu.udo.scaffoldhunter.view.table;

import edu.udo.scaffoldhunter.gui.util.ProgressWorker;
import edu.udo.scaffoldhunter.model.db.Molecule;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;

/**
 * @author Michael Hesse
 * @author Till Schäfer
 * 
 */
public class DataPump {
    DataPumpBlock currentLockedBlock;
    Model model;

    /**
     * @param model
     * 
     */
    public DataPump(Model model) {
        this.model = model;
        currentLockedBlock = new DataPumpBlock();
    }

    /**
     * 
     * @param propertyDefinition
     * @param molecule
     * @return true if the specified value is currently locked
     */
    public boolean contains(PropertyDefinition propertyDefinition, Molecule molecule) {
        return currentLockedBlock.contains(propertyDefinition, molecule);
    }

    /**
     * @param block
     */
    public void load(DataPumpBlock block) {
            Loader loader = new Loader(block);
            loader.execute();
    }

    /**
     * 
     */
    public void destroy() {
        model.getDbManager().unlockAndUnload(currentLockedBlock.getPropertyDefinitions(),
                currentLockedBlock.getMolecules());
    }

    /**
     * 
     * @author Michael Hesse
     * @author Till Schäfer
     * 
     */
    class Loader extends ProgressWorker<Integer, Void> {
        DataPumpBlock block;
        
        public Loader(DataPumpBlock block) {
            super();
            this.block = block;
        }

        /*
         * (non-Javadoc)
         * 
         * @see edu.udo.scaffoldhunter.gui.util.SwingWorker#doInBackground()
         */
        @Override
        protected Integer doInBackground() throws Exception {

            synchronized (DataPump.this) {
                model.getDbManager().lockAndLoad(block.getPropertyDefinitions(), block.getMolecules());
                model.getDbManager().unlockAndUnload(currentLockedBlock.getPropertyDefinitions(),
                        currentLockedBlock.getMolecules());
                currentLockedBlock = block;
            }

            model.repaintTable();
            return 0;
        }

    }
}
