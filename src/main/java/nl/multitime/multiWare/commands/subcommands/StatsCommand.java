package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.stats.PlayerStats;
import nl.multitime.multiWare.stats.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class StatsCommand extends SubCommand {

    public StatsCommand(MultiWare plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        StatsManager statsManager = plugin.getStatsManager();

        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Gebruik: " + getSyntax());
                return true;
            }

            Player player = (Player) sender;
            showStats(sender, player.getName(), statsManager.getPlayerStats(player.getUniqueId()));
            return true;
        }

        String playerName = args[1];

        if (playerName.equalsIgnoreCase("top")) {
            showTopPlayers(sender, statsManager);
            return true;
        }

        OfflinePlayer targetPlayer = null;
        for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
            if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(playerName)) {
                targetPlayer = offlinePlayer;
                break;
            }
        }

        if (targetPlayer == null) {
            sender.sendMessage(ChatColor.RED + "Speler '" + playerName + "' niet gevonden!");
            return true;
        }

        PlayerStats stats = statsManager.getPlayerStats(targetPlayer.getUniqueId());
        showStats(sender, targetPlayer.getName(), stats);
        return true;
    }

    private void showStats(CommandSender sender, String playerName, PlayerStats stats) {
        sender.sendMessage(ChatColor.GOLD + "=== Statistieken van " + playerName + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Gespeelde games: " + ChatColor.WHITE + stats.getGamesPlayed());
        sender.sendMessage(ChatColor.YELLOW + "Gewonnen games: " + ChatColor.WHITE + stats.getGamesWon());
        sender.sendMessage(ChatColor.YELLOW + "Winpercentage: " + ChatColor.WHITE +
                          (stats.getGamesPlayed() > 0 ? String.format("%.1f%%", (stats.getGamesWon() * 100.0 / stats.getGamesPlayed())) : "0.0%"));
        sender.sendMessage(ChatColor.YELLOW + "Totaal punten: " + ChatColor.WHITE + stats.getTotalPoints());
        sender.sendMessage(ChatColor.YELLOW + "Hoogste score: " + ChatColor.WHITE + stats.getHighestScore());

        Map<String, Integer> minigameWins = stats.getMinigameWins();
        if (!minigameWins.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "Top minigames:");
            minigameWins.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .forEach(entry -> {
                    sender.sendMessage(ChatColor.YELLOW + "- " + entry.getKey() + ": " +
                                      ChatColor.WHITE + entry.getValue() + " keer gewonnen");
                });
        }
    }

    private void showTopPlayers(CommandSender sender, StatsManager statsManager) {
        sender.sendMessage(ChatColor.GOLD + "=== Top 10 Spelers ===");

        List<Map.Entry<UUID, PlayerStats>> topPlayers = statsManager.getTopPlayers(10);

        if (topPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Er zijn nog geen statistieken beschikbaar.");
            return;
        }

        int rank = 1;
        for (Map.Entry<UUID, PlayerStats> entry : topPlayers) {
            UUID playerId = entry.getKey();
            PlayerStats stats = entry.getValue();

            String playerName = Bukkit.getOfflinePlayer(playerId).getName();
            if (playerName == null) playerName = "Onbekend";

            sender.sendMessage(ChatColor.YELLOW + "#" + rank + " " + playerName + ": " +
                              ChatColor.WHITE + stats.getTotalPoints() + " punten, " +
                              stats.getGamesWon() + " overwinningen");

            rank++;
        }
    }

    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "Bekijk statistieken van spelers";
    }

    @Override
    public String getSyntax() {
        return "/mw stats [speler|top]";
    }

    @Override
    public String getPermission() {
        return null;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("statistics", "stat");
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 2) {
            List<String> completions = new ArrayList<>();
            completions.add("top");

            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }

            return completions.stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return super.getTabCompletions(sender, args);
    }
}
