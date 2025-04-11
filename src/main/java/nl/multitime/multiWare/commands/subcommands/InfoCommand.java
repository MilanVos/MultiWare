package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class InfoCommand extends SubCommand {

    private final MultiWare plugin;

    public InfoCommand(MultiWare plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getDescription() {
        return "Toont informatie over een minigame";
    }

    @Override
    public String getSyntax() {
        return "/mw info <minigame>";
    }

    @Override
    public String getPermission() {
        return "multiware.command.info";
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

        MinigameConfig config = plugin.getGameManager().getMinigameConfig(minigameName);
        sender.sendMessage(ChatColor.GOLD + "=== Minigame Informatie: " + config.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + config.getType());
        sender.sendMessage(ChatColor.YELLOW + "Status: " + (config.isEnabled() ? ChatColor.GREEN + "Ingeschakeld" : ChatColor.RED + "Uitgeschakeld"));
        sender.sendMessage(ChatColor.YELLOW + "Duur: " + ChatColor.WHITE + config.getDuration() + " seconden");
        sender.sendMessage(ChatColor.YELLOW + "PvP: " + (config.isPvpEnabled() ? ChatColor.GREEN + "Ingeschakeld" : ChatColor.RED + "Uitgeschakeld"));

        if (config.getSpawnLocation() != null) {
            sender.sendMessage(ChatColor.YELLOW + "Spawn locatie: " + ChatColor.WHITE +
                    "Wereld: " + config.getSpawnLocation().getWorld().getName() +
                    ", X: " + config.getSpawnLocation().getBlockX() +
                    ", Y: " + config.getSpawnLocation().getBlockY() +
                    ", Z: " + config.getSpawnLocation().getBlockZ());
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Spawn locatie: " + ChatColor.RED + "Niet ingesteld");
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
