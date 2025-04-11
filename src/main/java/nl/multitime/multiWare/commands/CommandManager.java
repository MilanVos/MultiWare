package nl.multitime.multiWare.commands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.subcommands.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final MultiWare plugin;
    private final Map<String, SubCommand> commands = new HashMap<>();

    public CommandManager(MultiWare plugin) {
        this.plugin = plugin;

        registerCommand(new StartCommand(plugin));
        registerCommand(new StopCommand(plugin));
        registerCommand(new InfoCommand(plugin));
        registerCommand(new ListCommand(plugin));
        registerCommand(new CreateCommand(plugin));
        registerCommand(new EditCommand(plugin));
        registerCommand(new RemoveCommand(plugin));
        registerCommand(new EnableCommand(plugin));
        registerCommand(new DisableCommand(plugin));
        registerCommand(new TeleportCommand(plugin));
        registerCommand(new SetSpawnCommand(plugin));
        registerCommand(new HelpCommand(plugin, commands));
    }

    private void registerCommand(SubCommand command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            commands.get("help").execute(sender, args);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = commands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage(ChatColor.RED + "Onbekend commando. Gebruik /mw help voor een lijst met commando's.");
            return true;
        }

        if (subCommand.getPermission() != null && !sender.hasPermission(subCommand.getPermission())) {
            sender.sendMessage(ChatColor.RED + "Je hebt geen toestemming om dit commando te gebruiken.");
            return true;
        }

        return subCommand.execute(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return commands.values().stream()
                    .filter(cmd -> cmd.getPermission() == null || sender.hasPermission(cmd.getPermission()))
                    .map(SubCommand::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length > 1) {
            SubCommand subCommand = commands.get(args[0].toLowerCase());
            if (subCommand != null) {
                return subCommand.getTabCompletions(sender, args);
            }
        }

        return new ArrayList<>();
    }
}
