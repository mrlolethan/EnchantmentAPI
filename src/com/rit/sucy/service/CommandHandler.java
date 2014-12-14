package com.rit.sucy.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import com.rit.sucy.EnchantmentAPI;

/**
 * Abstract class to handle the majority of the logic dealing with commands.
 * Allows for a nested structure of commands.
 */
@SuppressWarnings("SameParameterValue")
public abstract class CommandHandler implements CommandExecutor
{
    /**
     * Registered commands for this handler.
     */
    private final Map<String, ICommand> registeredCommands = new HashMap<String, ICommand>();
    /**
     * Registered subcommands and the handler associated with it.
     */
    private final Map<String, CommandHandler> registeredHandlers = new HashMap<String, CommandHandler>();
    /**
     * Root plugin so that commands and handlers have access to the information.
     */
    protected final EnchantmentAPI plugin;

    /**
     * Command name.
     */
    private final String cmd;

    /**
     * Constructor.
     *
     * @param plugin - Root plugin.
     */
    protected CommandHandler(EnchantmentAPI plugin, String cmd)
    {
        this.plugin = plugin;
        this.cmd = cmd;
    }

    /**
     * Register a command with an execution handler.
     *
     * @param label   - Command to listen for.
     * @param command - Execution handler that will handle the logic behind the
     *                command.
     */
    protected void registerCommand(String label, ICommand command)
    {
        if (registeredCommands.containsKey(label))
        {
            plugin.getLogger().warning("Replacing existing command for: " + label);
        }
        registeredCommands.put(label, command);
    }

    /**
     * Unregister a command for this handler.
     *
     * @param label - Command to stop handling.
     */
    public void unregisterCommand(String label)
    {
        registeredCommands.remove(label);
    }

    /**
     * Register a subcommand with a command handler.
     *
     * @param handler - Command handler.
     */
    public void registerHandler(CommandHandler handler)
    {
        if (registeredHandlers.containsKey(handler.getCommand()))
        {
            plugin.getLogger().warning("Replacing existing handler for: " + handler.getCommand());
        }
        registeredHandlers.put(handler.getCommand(), handler);
    }

    /**
     * Unregister a subcommand.
     *
     * @param label - Subcommand to remove.
     */
    public void unregisterHandler(String label)
    {
        registeredHandlers.remove(label);
    }

    /**
     * Command loop that will go through the linked handlers until it finds the
     * appropriate handler or command execution handler to do the logic for.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if (args.length == 0)
        {
            return noArgs(sender, command, label);
        }
        else
        {
            final String subcmd = args[0].toLowerCase();
            // Check known handlers first and pass to them
            final CommandHandler handler = registeredHandlers.get(subcmd);
            if (handler != null)
            {
                return handler.onCommand(sender, command, label, shortenArgs(args));
            }
            // Its our command, so handle it if its registered.
            final ICommand subCommand = registeredCommands.get(subcmd);
            if (subCommand == null)
            {
                return unknownCommand(sender, command, label, args);
            }
            // Execute command
            boolean value = true;
            try
            {
                value = subCommand.execute(plugin, sender, command, label, shortenArgs(args));
            } catch (ArrayIndexOutOfBoundsException e)
            {
                sender.sendMessage(ChatColor.GRAY + plugin.getTag() + ChatColor.RED + " Missing parameters.");
            }
            return value;
        }
    }

    /**
     * Method that is called on a CommandHandler if there is no additional
     * arguments given that specify a specific command.
     *
     * @param sender  - Sender of the command.
     * @param command - Command used.
     * @param label   - Command label.
     * @return True if handled. Should not need to return false...
     */
    protected abstract boolean noArgs(CommandSender sender, Command command, String label);

    /**
     * Allow for the command handler to have special logic for unknown commands.
     * Useful for when expecting a player name parameter on a root command
     * handler command.
     *
     * @param sender  - Sender of the command.
     * @param command - Command used.
     * @param label   - Command label.
     * @param args    - Arguments.
     * @return True if handled. Should not need to return false...
     */
    protected abstract boolean unknownCommand(CommandSender sender, Command command, String label, String[] args);

    /**
     * Shortens the given string array by removing the first entry.
     *
     * @param args - Array to shorten.
     * @return Shortened array.
     */
    String[] shortenArgs(String[] args)
    {
        if (args.length == 0)
        {
            return args;
        }
        final List<String> argList = new ArrayList<String>();
        for (int i = 1; i < args.length; i++)
        {
            argList.add(args[i]);
        }
        return argList.toArray(new String[argList.size()]);
    }

    /**
     * Get the command for this handler.
     *
     * @return Command
     */
    String getCommand()
    {
        return cmd;
    }
}
