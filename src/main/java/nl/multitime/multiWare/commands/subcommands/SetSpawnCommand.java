package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SetSpawnCommand extends SubCommand {

    private final MultiWare plugin;

    public SetSpawnCommand(MultiWare plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "setspawn";
    }

    @Override
    public String getDescription() {
        return "Stelt de spawn locatie van een minigame in";
    }

    @Override
    public String getSyntax() {
        return "/mw setspawn <minigame>";
    }

    @Override
    public String getPermission() {
        return "multiware.command.setspawn";
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
        config.setSpawnLocation(player.getLocation());

        if (plugin.getGameManager().saveMinigameConfig(config)) {
            player.sendMessage(ChatColor.GREEN + "Spawn locatie voor minigame '" + minigameName + "' is ingesteld op jouw huidige locatie!");
        } else {
            player.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het instellen van de spawn locatie!");
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
