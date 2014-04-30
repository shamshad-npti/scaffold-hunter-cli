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

import java.util.List;

/**
 * @author Micha
 *
 */
public class SvgLoadingTrigger extends Thread {

    private ViewComponent viewComponent;
    private Model model;
    private long updatetime;
    private long delay = 200;
    private boolean threadIsRunning;
    
    /**
     * @param viewComponent 
     * @param model 
     * 
     */
    public SvgLoadingTrigger(ViewComponent viewComponent, Model model) {
        super();
        this.viewComponent = viewComponent;
        this.model = model;
        updatetime = Long.MAX_VALUE;
    }
    
    
    /**
     * 
     */
    @Override
    public void run() {
        threadIsRunning = true;
        
        while (threadIsRunning) {
            if( System.currentTimeMillis() > updatetime ) {
                updatetime = Long.MAX_VALUE;
                // when the column with the svgs is not visible then the rowlist will be empty
                
                // load the svgs
                List <Integer> rowList = viewComponent.getCurrentlyVisibleModelRows();
                for(Integer row : rowList) {
                    model.loadAnSvg(row);       // repainting of the loaded svg will be done by the table
                }
            }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    /**
     * call this method when the table is repainted. SVGs will be loaded
     * 200ms after the last call to update().
     */
    public void update() {
        updatetime = System.currentTimeMillis() + delay;
    }
    
    /**
     * ends the thread
     */
    public void shutdown() {
        threadIsRunning = false;
    }
}
