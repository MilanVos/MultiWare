package nl.multitime.multiWare.stats;

import nl.multitime.multiWare.MultiWare;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class StatsManager {

    private final MultiWare plugin;
    private final Map<UUID, PlayerStats> playerStats;
    private final File statsFile;
    private FileConfiguration statsConfig;

    public StatsManager(MultiWare plugin) {
        this.plugin = plugin;
        this.playerStats = new ConcurrentHashMap<>();
        this.statsFile = new File(plugin.getDataFolder(), "stats.yml");

        loadStats();
    }

    private void loadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
                statsConfig = YamlConfiguration.loadConfiguration(statsFile);
                statsConfig.save(statsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Kon stats.yml niet aanmaken: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        } else {
            statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        }

        if (statsConfig.contains("players")) {
            Map<String, Object> players = statsConfig.getConfigurationSection("players").getValues(false);

            for (Map.Entry<String, Object> entry : players.entrySet()) {
                UUID playerId = UUID.fromString(entry.getKey());
                Map<String, Object> statsMap = (Map<String, Object>) entry.getValue();
                PlayerStats stats = new PlayerStats(statsMap);
                playerStats.put(playerId, stats);
            }

            plugin.getLogger().info("Statistieken geladen voor " + playerStats.size() + " spelers.");
        }
    }

    public void saveStats() {
        if (statsConfig == null) {
            statsConfig = YamlConfiguration.loadConfiguration(statsFile);
        }

        for (Map.Entry<UUID, PlayerStats> entry : playerStats.entrySet()) {
            UUID playerId = entry.getKey();
            PlayerStats stats = entry.getValue();

            statsConfig.set("players." + playerId.toString(), stats.serialize());
        }

        try {
            statsConfig.save(statsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Kon stats.yml niet opslaan: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public PlayerStats getPlayerStats(UUID playerId) {
        return playerStats.computeIfAbsent(playerId, PlayerStats::new);
    }

    public void recordGamePlayed(Player player) {
        PlayerStats stats = getPlayerStats(player.getUniqueId());
        stats.incrementGamesPlayed();
    }

    public void recordGameWon(Player player) {
        PlayerStats stats = getPlayerStats(player.getUniqueId());
        stats.incrementGamesWon();
    }

    public void addPoints(Player player, int points) {
        PlayerStats stats = getPlayerStats(player.getUniqueId());
        stats.addPoints(points);
    }

    public void recordMinigameWin(Player player, String minigameName) {
        PlayerStats stats = getPlayerStats(player.getUniqueId());
        stats.addMinigameWin(minigameName);
    }

    public List<Map.Entry<UUID, PlayerStats>> getTopPlayers(int limit) {
        return playerStats.entrySet().stream()
                .sorted(Map.Entry.<UUID, PlayerStats>comparingByValue(
                        Comparator.comparingInt(PlayerStats::getTotalPoints).reversed()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void resetStats(UUID playerId) {
        playerStats.remove(playerId);

        if (statsConfig != null) {
            statsConfig.set("players." + playerId.toString(), null);
            try {
                statsConfig.save(statsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Kon stats.yml niet opslaan: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void resetAllStats() {
        playerStats.clear();

        if (statsConfig != null) {
            statsConfig.set("players", null);
            try {
                statsConfig.save(statsFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Kon stats.yml niet opslaan: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
