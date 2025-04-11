package nl.multitime.multiWare.game;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.game.minigames.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class GameManager {

    private final MultiWare plugin;
    public GameState gameState = GameState.INACTIVE;
    public List<Minigame> availableMinigames = new ArrayList<>();
    public Minigame currentMinigame;
    public Set<UUID> players = new HashSet<>();
    public Map<UUID, Integer> totalScores = new HashMap<>();
    public BukkitTask gameLoopTask;
    public int roundsPlayed = 0;
    public final int maxRounds;

    public GameManager(MultiWare plugin) {
        this.plugin = plugin;
        this.maxRounds = plugin.getConfig().getInt("game.maxRounds", 5);
    }


    public void startGame() {
        if (gameState != GameState.INACTIVE) {
            return;
        }

        if (availableMinigames.size() < 3) {
            plugin.getLogger().warning("Niet genoeg minigames beschikbaar om te starten!");
            return;
        }

        gameState = GameState.STARTING;
        roundsPlayed = 0;
        totalScores.clear();

        players.clear();
        for (Player player : Bukkit.getOnlinePlayers()) {
            players.add(player.getUniqueId());
            totalScores.put(player.getUniqueId(), 0);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "MultiWare game wordt gestart!");
        Bukkit.broadcastMessage(ChatColor.YELLOW + "Er zullen " + maxRounds + " minigames worden gespeeld.");

        Bukkit.getScheduler().runTaskLater(plugin, this::startNextMinigame, 5 * 20L);
    }

    private void startNextMinigame() {
        if (gameState == GameState.INACTIVE) {
            return;
        }

        if (roundsPlayed >= maxRounds) {
            endGame();
            return;
        }

        List<Minigame> availableGames = new ArrayList<>(availableMinigames);
        if (currentMinigame != null) {
            availableGames.remove(currentMinigame);
        }

        if (availableGames.isEmpty()) {
            availableGames = new ArrayList<>(availableMinigames);
        }

        Random random = new Random();
        currentMinigame = availableGames.get(random.nextInt(availableGames.size()));

        currentMinigame.players.clear();
        currentMinigame.getScores().clear();
        for (UUID playerId : players) {
            currentMinigame.addPlayer(playerId);
        }

        gameState = GameState.ACTIVE;
        roundsPlayed++;

        Bukkit.broadcastMessage(ChatColor.GOLD + "Ronde " + roundsPlayed + "/" + maxRounds + ": " +
                ChatColor.YELLOW + currentMinigame.getName());

        currentMinigame.start();

        gameLoopTask = Bukkit.getScheduler().runTaskLater(plugin, this::endCurrentMinigame, 60 * 20L);
    }

    private void endCurrentMinigame() {
        if (currentMinigame == null || gameState != GameState.ACTIVE) {
            return;
        }

        gameState = GameState.ENDING;

        currentMinigame.end();

        Map<UUID, Integer> minigameScores = currentMinigame.getScores();
        for (Map.Entry<UUID, Integer> entry : minigameScores.entrySet()) {
            UUID playerId = entry.getKey();
            int score = entry.getValue();

            int currentTotal = totalScores.getOrDefault(playerId, 0);
            totalScores.put(playerId, currentTotal + score);
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "Huidige stand:");
        List<Map.Entry<UUID, Integer>> sortedScores = totalScores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < Math.min(5, sortedScores.size()); i++) {
            Map.Entry<UUID, Integer> entry = sortedScores.get(i);
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + String.valueOf(i + 1) + ". " + player.getName() + ": " +
                        ChatColor.WHITE + entry.getValue() + " punten");
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, this::startNextMinigame, 10 * 20L);
    }

    private void endGame() {
        if (gameState == GameState.INACTIVE) {
            return;
        }

        gameState = GameState.INACTIVE;

        if (gameLoopTask != null) {
            gameLoopTask.cancel();
            gameLoopTask = null;
        }

        if (currentMinigame != null && currentMinigame.isActive()) {
            currentMinigame.end();
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "MultiWare game is beëindigd!");
        Bukkit.broadcastMessage(ChatColor.GOLD + "Eindstand:");

        List<Map.Entry<UUID, Integer>> sortedScores = totalScores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());

        for (int i = 0; i < Math.min(5, sortedScores.size()); i++) {
            Map.Entry<UUID, Integer> entry = sortedScores.get(i);
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + String.valueOf(i + 1) + ". " + player.getName() + ": " +
                        ChatColor.WHITE + entry.getValue() + " punten");
            }
        }

        if (!sortedScores.isEmpty()) {
            UUID winnerId = sortedScores.get(0).getKey();
            Player winner = Bukkit.getPlayer(winnerId);
            if (winner != null) {
                Bukkit.broadcastMessage(ChatColor.GOLD + "Winnaar: " + ChatColor.GREEN + winner.getName() +
                        ChatColor.GOLD + " met " + ChatColor.GREEN + sortedScores.get(0).getValue() +
                        ChatColor.GOLD + " punten!");
            }
        }

        currentMinigame = null;
        players.clear();
        totalScores.clear();
    }

    public void stopAllGames() {
        if (gameState == GameState.INACTIVE) {
            return;
        }

        if (gameLoopTask != null) {
            gameLoopTask.cancel();
            gameLoopTask = null;
        }

        if (currentMinigame != null && currentMinigame.isActive()) {
            currentMinigame.end();
        }

        gameState = GameState.INACTIVE;
        currentMinigame = null;
        players.clear();
        totalScores.clear();

        Bukkit.broadcastMessage(ChatColor.GOLD + "MultiWare game is gestopt!");
    }


    public void addPlayer(UUID playerId) {
        if (gameState == GameState.INACTIVE) {
            return;
        }

        players.add(playerId);
        totalScores.put(playerId, 0);

        if (currentMinigame != null && currentMinigame.isActive()) {
            currentMinigame.addPlayer(playerId);
        }
    }


    public void removePlayer(UUID playerId) {
        players.remove(playerId);

        if (currentMinigame != null && currentMinigame.isActive()) {
            currentMinigame.removePlayer(playerId);
        }
    }


    public GameState getGameState() {
        return gameState;
    }


    public String getCurrentGameName() {
        return currentMinigame != null ? currentMinigame.getName() : null;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public boolean isPvPEnabled() {
        return currentMinigame != null && currentMinigame.isActive() && currentMinigame.isPvPEnabled();
    }

    public List<String> getTopScores(int limit) {
        List<String> result = new ArrayList<>();

        List<Map.Entry<UUID, Integer>> sortedScores = totalScores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());

        for (int i = 0; i < sortedScores.size(); i++) {
            Map.Entry<UUID, Integer> entry = sortedScores.get(i);
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                result.add((i + 1) + ". " + player.getName() + ": " + entry.getValue() + " punten");
            }
        }

        return result;
    }

    public boolean minigameExists(String name) {
        File configFile = new File(plugin.getDataFolder(), "minigames/" + name + ".yml");
        return configFile.exists();
    }

    public MinigameConfig getMinigameConfig(String name) {
        File configFile = new File(plugin.getDataFolder(), "minigames/" + name + ".yml");
        if (!configFile.exists()) {
            return null;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return (MinigameConfig) config.get("config");
    }
    public boolean saveMinigameConfig(MinigameConfig config) {
        File minigamesDir = new File(plugin.getDataFolder(), "minigames");
        if (!minigamesDir.exists()) {
            minigamesDir.mkdirs();
        }

        File configFile = new File(minigamesDir, config.getName() + ".yml");
        FileConfiguration yamlConfig = new YamlConfiguration();
        yamlConfig.set("config", config);

        try {
            yamlConfig.save(configFile);

            loadMinigames();

            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Kon minigame configuratie niet opslaan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean removeMinigame(String name) {
        File configFile = new File(plugin.getDataFolder(), "minigames/" + name + ".yml");
        if (!configFile.exists()) {
            return false;
        }

        boolean success = configFile.delete();

        if (success) {
            loadMinigames();
        }

        return success;
    }

    public void loadMinigames() {
        availableMinigames.clear();

        File minigamesDir = new File(plugin.getDataFolder(), "minigames");
        if (!minigamesDir.exists()) {
            minigamesDir.mkdirs();
            return;
        }

        File[] configFiles = minigamesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (configFiles == null) {
            return;
        }

        for (File file : configFiles) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            MinigameConfig minigameConfig = (MinigameConfig) config.get("config");

            if (minigameConfig != null && minigameConfig.isEnabled() && minigameConfig.isComplete()) {
                Minigame minigame = createMinigameInstance(minigameConfig);
                if (minigame != null) {
                    availableMinigames.add(minigame);
                }
            }
        }

        plugin.getLogger().info("Geladen minigames: " + availableMinigames.size());
    }


    private Minigame createMinigameInstance(MinigameConfig config) {
        switch (config.getType()) {
            case "clickpressureplate":
                return new ClickPressurePlateMinigame(plugin, config);

            case "breakblock":
                return new BreakBlockMinigame(plugin, config);

            case "towerup":
                return new TowerUpMinigame(plugin, config);
            case "freeze":
                return new FreezeMinigame(plugin, config);


            default:
                plugin.getLogger().warning("Onbekend minigame type: " + config.getType());
                return null;
        }
    }


    public List<String> getAllMinigameNames() {
        List<String> names = new ArrayList<>();

        File minigamesDir = new File(plugin.getDataFolder(), "minigames");
        if (!minigamesDir.exists()) {
            return names;
        }

        File[] configFiles = minigamesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (configFiles == null) {
            return names;
        }

        for (File file : configFiles) {
            String name = file.getName().replace(".yml", "");
            names.add(name);
        }

        return names;
    }

    public List<MinigameConfig> getAllMinigameConfigs() {
        List<MinigameConfig> configs = new ArrayList<>();

        for (String name : getAllMinigameNames()) {
            MinigameConfig config = getMinigameConfig(name);
            if (config != null) {
                configs.add(config);
            }
        }

        return configs;
    }

    public int getTotalMinigameCount() {
        return getAllMinigameNames().size();
    }

    public int getEnabledMinigameCount() {
        return (int) getAllMinigameConfigs().stream()
                .filter(MinigameConfig::isEnabled)
                .filter(MinigameConfig::isComplete)
                .count();
    }

    public List<Minigame> getAvailableMinigames() {
        return new ArrayList<>(availableMinigames);
    }

    public int getRoundsPlayed() {
        return roundsPlayed;
    }

    public int getMaxRounds() {
        return maxRounds;
    }

    public enum GameState {
        INACTIVE,   // Geen game actief
        STARTING,   // Game wordt gestart
        ACTIVE,     // Game is actief
        ENDING      // Game wordt beëindigd
    }
}
