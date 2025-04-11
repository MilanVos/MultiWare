package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.GameManager;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SetArenaCommand extends SubCommand {

    public SetArenaCommand(MultiWare plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Gebruik: " + getSyntax());
            return true;
        }

        Player player = (Player) sender;
        String minigameName = args[1];
        String corner = args[2].toLowerCase();

        GameManager gameManager = plugin.getGameManager();
        MinigameConfig config = gameManager.getMinigameConfig(minigameName);

        if (config == null) {
            sender.sendMessage(ChatColor.RED + "Minigame '" + minigameName + "' bestaat niet!");
            return true;
        }

        Location location = player.getLocation();

        if (corner.equals("min")) {
            config.setExtraData("arenaMin", location);
            player.sendMessage(ChatColor.GREEN + "Arena minimum hoek voor minigame '" + minigameName + "' ingesteld!");
        } else if (corner.equals("max")) {
            config.setExtraData("arenaMax", location);
            player.sendMessage(ChatColor.GREEN + "Arena maximum hoek voor minigame '" + minigameName + "' ingesteld!");
        } else {
            player.sendMessage(ChatColor.RED + "Ongeldig hoekpunt! Gebruik 'min' of 'max'.");
            return true;
        }

        gameManager.saveMinigameConfig(config);

        if (config.getExtraData("arenaMin") != null && config.getExtraData("arenaMax") != null) {
            Location min = (Location) config.getExtraData("arenaMin");
            Location max = (Location) config.getExtraData("arenaMax");

            int volume = (max.getBlockX() - min.getBlockX() + 1) *
                         (max.getBlockY() - min.getBlockY() + 1) *
                         (max.getBlockZ() - min.getBlockZ() + 1);

            player.sendMessage(ChatColor.YELLOW + "Arena grootte: " + volume + " blokken");
        }

        return true;
    }

    @Override
    public String getName() {
        return "setarena";
    }

    @Override
    public String getDescription() {
        return "Stel de arena grenzen in voor een minigame";
    }

    @Override
    public String getSyntax() {
        return "/mw setarena <naam> <min|max>";
    }

    @Override
    public String getPermission() {
        return "multiware.edit";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 2;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("arena");
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        GameManager gameManager = plugin.getGameManager();

        if (args.length == 2) {
            return gameManager.getAllMinigameNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            return Arrays.asList("min", "max").stream()
                    .filter(corner -> corner.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return super.getTabCompletions(sender, args);
    }
}
