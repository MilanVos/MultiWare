package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RemoveCommand extends SubCommand {

    private final MultiWare plugin;

    public RemoveCommand(MultiWare plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Verwijdert een minigame";
    }

    @Override
    public String getSyntax() {
        return "/mw remove <minigame>";
    }

    @Override
    public String getPermission() {
        return "multiware.command.remove";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(ChatColor.RED + "Je hebt geen toestemming om dit commando uit te voeren!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Gebruik: " + getSyntax());
            return true;
        }

        String minigameName = args[0];
        if (!plugin.getGameManager().minigameExists(minigameName)) {
            sender.sendMessage(ChatColor.RED + "Minigame '" + minigameName + "' bestaat niet!");
            return true;
        }

        if (plugin.getGameManager().deleteMinigameConfig(minigameName)) {
            sender.sendMessage(ChatColor.GREEN + "Minigame '" + minigameName + "' is verwijderd!");
        } else {
            sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het verwijderen van minigame '" + minigameName + "'!");
        }

        return true;
    }


    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(plugin.getGameManager().getAllMinigameNames());
        }

        return completions;
    }
}
