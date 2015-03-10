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

package edu.udo.scaffoldhunter.cli.args;

import com.beust.jcommander.Parameter;

import edu.udo.scaffoldhunter.cli.command.HelpCommand;

/**
 * This is a super class for each parameters class.
 * It may have some parameters which are common to 
 * all parameter classes such as <code>--help</code>
 * @author Shamshad Alam
 *
 */
public abstract class AbstractArgs {
    
    /**
     * A constructor which whenever a command argument object is created try to add 
     * that command argument to the help command. add() method of help command is 
     * implemented in such a way that it can avoid duplicate entry of any command. 
     * Also please note that command names are case insensitive
     */
    public AbstractArgs() {
        HelpCommand.add(this);
    }
    
    /**
     * Help parameter
     */
    @Parameter(names = {"-h", "--help"}, help=true, description = "Print help", descriptionKey = "", hidden = true)
    public boolean help;
    
    /**
     * Reset the parameter value to its default value
     */
    public void reset() {
        
    }
}
