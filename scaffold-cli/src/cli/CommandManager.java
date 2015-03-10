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

import static edu.udo.scaffoldhunter.util.I18n._;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.internal.Maps;
import com.ibm.icu.util.CaseInsensitiveString;

import edu.udo.scaffoldhunter.cli.args.AbstractArgs;
import edu.udo.scaffoldhunter.cli.command.AbstractCommand;
import edu.udo.scaffoldhunter.cli.command.ConnectionCommand;
import edu.udo.scaffoldhunter.cli.command.FiltersetCommand;
import edu.udo.scaffoldhunter.cli.command.HelpCommand;
import edu.udo.scaffoldhunter.cli.command.SessionCommand;
import edu.udo.scaffoldhunter.cli.command.clustering.ClusteringCommand;
import edu.udo.scaffoldhunter.cli.command.datacalculation.CalcCommand;
import edu.udo.scaffoldhunter.cli.command.dataset.DatasetCommand;
import edu.udo.scaffoldhunter.cli.command.dataset.RulesetCommand;
import edu.udo.scaffoldhunter.cli.command.dataset.TreeCommand;
import edu.udo.scaffoldhunter.cli.command.display.DisplayCommand;
import edu.udo.scaffoldhunter.cli.command.export.ExportCommand;
import edu.udo.scaffoldhunter.cli.command.filter.FilterCommand;
import edu.udo.scaffoldhunter.cli.command.scaffold.GenerateScaffoldCommand;
import edu.udo.scaffoldhunter.data.ConnectionDataManager;
import edu.udo.scaffoldhunter.util.I18n;

/**
 * This is a singleton class which manages all commands object it loads all
 * commands object and additional command object can be added by calling
 * {@link #addCommand(AbstractCommand)} method
 * 
 * IMPROVEMENT: It will be a good idea to load commands dynamically at the run
 * time in order to avoid modification this file for each new command.
 * 
 * @author Shamshad Alam
 * 
 */
public class CommandManager {
    private static CommandManager manager;
    private Map<CaseInsensitiveString, AbstractCommand<? extends AbstractArgs>> commands = Maps.newHashMap();
    private static HelpCommand helpCommand;
    private ConnectionDataManager dataManager;

    private CommandManager(ConnectionDataManager dataManager) {
        // add all command to maps
        //addCommand(new Print());
        //addCommand(new SplitScaffoldCommand());
        this.dataManager = dataManager;
        I18n.initialize(dataManager.getLanguage());

        addCommand(new ConnectionCommand());
        addCommand((helpCommand = new HelpCommand()));
        addCommand(new GenerateScaffoldCommand());
        addCommand(new DisplayCommand());
        addCommand(new SessionCommand());
        addCommand(new FiltersetCommand());
        addCommand(new DatasetCommand());
        addCommand(new RulesetCommand());
        addCommand(new TreeCommand());
        addCommand(new FilterCommand());
        addCommand(new ClusteringCommand());
        addCommand(new CalcCommand());
        addCommand(new ExportCommand());
    }

    /**
     * Singleton instance of command manager
     * 
     * @return CommandManager object
     */
    public static CommandManager getInstance() {
        if (manager == null) {
            manager = new CommandManager(new ConnectionDataManager());
        }
        return manager;
    }

    /**
     * @return the helpCommand
     */
    public static HelpCommand getHelpCommand() {
        return helpCommand;
    }
    
    /**
     * @return the dataManager
     */
    public ConnectionDataManager getDataManager() {
        return dataManager;
    }

    /**
     * Add command to command manager. command is added only if neither
     * <code>command</code> is null nor <code>command.getArguments()</code>
     * returns null
     * 
     * @param command
     *            to add in command manager
     */
    public <T extends AbstractArgs> void addCommand(AbstractCommand<T> command) {
        if (command != null && command.getName() != null && command.getArguments() != null) {
            commands.put(new CaseInsensitiveString(command.getName()), command);
        }
    }

    /**
     * Get an unmodifiable collection of all registered command
     * 
     * @return Unmodifiable collection of all registered command
     */
    public Collection<AbstractCommand<? extends AbstractArgs>> getAllCommand() {
        return Collections.unmodifiableCollection(commands.values());
    }

    /**
     * Identify command and execute it as per the variable args
     * 
     * @param args
     *            required to identify command and its parameters
     */
    public void execute(String... args) {
        JCommander commander = AbstractCommand.getDefaultCommander();
        try {

            if (args == null || args.length == 0) {
                helpCommand.execute();
                return;
            }

            // parse command
            commander.parse(args);

            // execute command
            getCommand(commander.getParsedCommand()).execute();
        } catch (Exception ex) {
            String command = commander.getParsedCommand();
            ex.printStackTrace();
            if (command != null) {
                // display error generated by commander
                CLIUtil.showError(ex.getMessage());

                // command exists and display help about that command
                CLIUtil.show(_("CLI.CommandManager.help", command));
                CLIUtil.separator();
                helpCommand.usage(command);
            } else {
                CLIUtil.show(_("CLI.CommandManager.commandNotImplemented"));
                CLIUtil.separator();
                helpCommand.usage(null);
            }
        }
    }

    /**
     * get the command by name. If command is registered to
     * <code>CommandManager</code>, the command is returned otherwise null is
     * returned
     * 
     * @param commandName
     *            name of the command
     * @return the command identified by the name or null if no command exists
     *         with the specified name
     */
    public AbstractCommand<? extends AbstractArgs> getCommand(String commandName) {
        return commands.get(new CaseInsensitiveString(commandName));
    }
}
