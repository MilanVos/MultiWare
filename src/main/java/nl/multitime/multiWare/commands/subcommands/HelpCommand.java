package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Map;

public class HelpCommand extends SubCommand {

    private final Map<String, SubCommand> commands;

    public HelpCommand(MultiWare plugin, Map<String, SubCommand> commands) {
        super(plugin);
        this.commands = commands;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "=== MultiWare Help ===");

        for (SubCommand command : commands.values()) {
            if (command.getPermission() == null || sender.hasPermission(command.getPermission())) {
                sender.sendMessage(ChatColor.YELLOW + command.getSyntax() + ChatColor.GRAY + " - " + command.getDescription());
            }
        }

        return true;
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Toon deze help informatie";
    }

    @Override
    public String getSyntax() {
        return "/mw help";
    }

    @Override
    public String getPermission() {
        return null; // Geen permissie nodig
    }
}
