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

import edu.udo.scaffoldhunter.cli.command.DBCommand;

/**
 * A command line action abstract class. Command format of SH command is as
 * follows
 * 
 * <code>
 * <pre>
 * command_name action_name additional_switches
 * </pre>
 * </code> action_name is name of the {@code CLIAction}. {@link DBCommand} has
 * implemented necessary method which automatically identify action and execute
 * it. A command may have many action, however action_name of the each action
 * must be unique for a command
 * 
 * @author Shamshad Alam
 * @param <T>
 * 
 */
public abstract class CLIAction<T extends AbstractArgs> {
    private String name;
    private String description;
    private boolean connectionRequired;
    private boolean profileRequired;

    /**
     * Create a new action object with specified name By default
     * {@code connectionRequired} and {@code profileRequired} is set to false
     * 
     * @param name
     *            name of the action
     */
    public CLIAction(String name) {
        this(name, name);
    }

    /**
     * create a new action object with specified name and description By default
     * {@code connectionRequired} and {@code profileRequired} is set to false
     * 
     * @param name
     *            name of the action
     * @param description
     *            description of the action
     */
    public CLIAction(String name, String description) {
        this(name, description, false);
    }

    /**
     * create a new action object
     * 
     * @param name
     *            name of the action
     * @param description
     *            description of the action
     * @param connectionRequired
     *            whether database connection required for this action
     */
    public CLIAction(String name, String description, boolean connectionRequired) {
        this(name, description, connectionRequired, false);
    }

    /**
     * create a new action object
     * 
     * @param name
     *            name of the action
     * @param description
     *            description of the action
     * @param connectionRequired
     *            whether database connection required
     * @param profileRequired
     *            whether profile is required by this action
     */
    public CLIAction(String name, String description, boolean connectionRequired, boolean profileRequired) {
        this.name = name;
        this.description = description;
        this.connectionRequired = connectionRequired;
        this.profileRequired = profileRequired;
    }

    /**
     * @return the name of the action
     */
    public String getName() {
        return name;
    }

    /**
     * @return the description for the action
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the connectionRequired
     */
    public boolean isConnectionRequired() {
        return connectionRequired;
    }

    /**
     * @param connectionRequired
     *            the connectionRequired to set
     */
    public void setConnectionRequired(boolean connectionRequired) {
        this.connectionRequired = connectionRequired;
    }

    /**
     * @return the profileRequired
     */
    public boolean isProfileRequired() {
        return profileRequired;
    }

    /**
     * @param profileRequired
     *            the profileRequired to set
     */
    public void setProfileRequired(boolean profileRequired) {
        this.profileRequired = profileRequired;
    }

    /**
     * execute action
     * 
     * @param args
     */
    public abstract void execute(T args);
}
