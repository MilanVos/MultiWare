package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TeleportCommand extends SubCommand {

    private final MultiWare plugin;

    public TeleportCommand(MultiWare plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "teleport";
    }

    @Override
    public String getDescription() {
        return "Teleporteert naar de spawn locatie van een minigame";
    }

    @Override
    public String getSyntax() {
        return "/mw teleport <minigame>";
    }

    @Override
    public String getPermission() {
        return "multiware.command.teleport";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt!");
            return true;
        }

        Player player = (Player) sender;
        if (!player.hasPermission(getPermission())) {
            player.sendMessage(ChatColor.RED + "Je hebt geen toestemming om dit commando uit te voeren!");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Gebruik: " + getSyntax());
            return true;
        }

        String minigameName = args[0];
        if (!plugin.getGameManager().minigameExists(minigameName)) {
            player.sendMessage(ChatColor.RED + "Minigame '" + minigameName + "' bestaat niet!");
            return true;
        }

        MinigameConfig config = plugin.getGameManager().getMinigameConfig(minigameName);
        if (config.getSpawnLocation() == null) {
            player.sendMessage(ChatColor.RED + "De spawn locatie voor minigame '" + minigameName + "' is niet ingesteld!");
            return true;
        }

        player.teleport(config.getSpawnLocation());
        player.sendMessage(ChatColor.GREEN + "Je bent geteleporteerd naar de spawn locatie van minigame '" + minigameName + "'!");

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
