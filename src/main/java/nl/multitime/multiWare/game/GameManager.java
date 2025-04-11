package nl.multitime.multiWare.game;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class GameManager {

    private final MultiWare plugin;
    private final Map<String, MinigameConfig> minigames;
    private final Set<UUID> activePlayers;
    private GameState gameState;
    private MinigameConfig currentGame;
    private BukkitTask gameTask;
    private int countdown;
    private Location lobbyLocation;
    private boolean pvpEnabled;
    private FileConfiguration config;

    public GameManager(MultiWare plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        this.minigames = new ConcurrentHashMap<>();
        this.activePlayers = new HashSet<>();
        this.gameState = GameState.INACTIVE;
        this.pvpEnabled = false; //Default value

        FileConfiguration config = plugin.getConfig();
        if (config.contains("lobby")) {
            this.lobbyLocation = (Location) config.get("lobby");
        }
    }

    public void loadMinigames() {
        File minigamesDir = new File(plugin.getDataFolder(), "minigames");
        if (!minigamesDir.exists()) {
            minigamesDir.mkdirs();
            return;
        }

        File[] files = minigamesDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            MinigameConfig minigame = (MinigameConfig) config.get("minigame");

            if (minigame != null) {
                minigames.put(minigame.getName().toLowerCase(), minigame);
            }
        }

        plugin.getLogger().info("Geladen minigames: " + minigames.size());
    }


    public boolean saveMinigameConfig(MinigameConfig config) {
        minigames.put(config.getName().toLowerCase(), config);

        File file = new File(plugin.getDataFolder(), "minigames/" + config.getName().toLowerCase() + ".yml");
        FileConfiguration yamlConfig = new YamlConfiguration();
        yamlConfig.set("minigame", config);

        try {
            yamlConfig.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Kon minigame configuratie niet opslaan: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }



    public boolean deleteMinigameConfig(String name) {
        String lowerName = name.toLowerCase();

        if (minigames.remove(lowerName) == null) {
            return false;
        }

        File file = new File(plugin.getDataFolder(), "minigames/" + lowerName + ".yml");
        return file.delete();
    }

    public MinigameConfig getMinigameConfig(String name) {
        return minigames.get(name.toLowerCase());
    }

    public List<String> getAllMinigameNames() {
        return new ArrayList<>(minigames.keySet());
    }

    public List<MinigameConfig> getEnabledMinigames() {
        return minigames.values().stream()
                .filter(MinigameConfig::isEnabled)
                .collect(Collectors.toList());
    }

    public boolean startRandomGame() {
        if (gameState != GameState.INACTIVE) {
            return false;
        }

        List<MinigameConfig> enabledMinigames = getEnabledMinigames();
        if (enabledMinigames.isEmpty()) {
            Bukkit.broadcastMessage(ChatColor.RED + "Er zijn geen ingeschakelde minigames beschikbaar!");
            return false;
        }

        Random random = new Random();
        MinigameConfig selectedGame = enabledMinigames.get(random.nextInt(enabledMinigames.size()));

        return startGame();
    }

    public boolean startGame() {
        if (gameState != GameState.INACTIVE) {
            return false;
        }
        if (config == null || !currentGame.isEnabled()) {
            return false;
        }

        if (config.getLocation("spawn") == null) {
            Bukkit.broadcastMessage(ChatColor.RED + "De spawn locatie voor minigame '" + config.getName() + "' is niet ingesteld!");
            return false;
        }

        gameState = GameState.STARTING;

        Bukkit.broadcastMessage(ChatColor.GREEN + "Minigame '" + config.getName() + "' start over " + countdown + " seconden!");

        gameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            countdown--;

            if (countdown > 0 && countdown <= 5) {
                Bukkit.broadcastMessage(ChatColor.YELLOW + "Minigame start over " + countdown + " seconden!");
            } else if (countdown == 0) {
                gameState = GameState.ACTIVE;
                for (UUID playerId : activePlayers) {
                    Player player = Bukkit.getPlayer(playerId);
                    if (player != null && player.isOnline()) {
                        player.teleport(config.getLocation("spawn"));
                    }
                }

                Bukkit.broadcastMessage(ChatColor.GOLD + "Minigame '" + config.getName() + "' is gestart!");

                countdown = currentGame.getDuration();

                gameTask.cancel();
                gameTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                    countdown--;

                    if (countdown <= 0) {
                        endGame();
                    } else if (countdown == 30 || countdown == 10 || countdown <= 5 && countdown > 0) {
                        Bukkit.broadcastMessage(ChatColor.YELLOW + "Minigame eindigt over " + countdown + " seconden!");
                    }
                }, 20L, 20L);
            }
        }, 20L, 20L);
        return true;
    }

    public void endGame() {
        if (gameState == GameState.INACTIVE) {
            return;
        }

        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        Bukkit.broadcastMessage(ChatColor.GOLD + "Minigame '" + currentGame.getName() + "' is beëindigd!");

        if (lobbyLocation != null) {
            for (UUID playerId : activePlayers) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null && player.isOnline()) {
                    player.teleport(lobbyLocation);
                }
            }
        }

        gameState = GameState.INACTIVE;
        currentGame = null;
    }

    public void stopAllGames() {
        endGame();
    }

    public void addPlayer(UUID playerId) {
        activePlayers.add(playerId);

        if (gameState == GameState.ACTIVE && currentGame != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.teleport(currentGame.getSpawnLocation());
            }
        }
    }

    public void removePlayer(UUID playerId) {
        activePlayers.remove(playerId);

        if (lobbyLocation != null) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null && player.isOnline()) {
                player.teleport(lobbyLocation);
            }
        }
    }

    public boolean isPlayerInGame(UUID playerId) {
        return activePlayers.contains(playerId);
    }

    public GameState getGameState() {
        return gameState;
    }

    public MinigameConfig getCurrentGame() {
        return currentGame;
    }

    public Location getLobbyLocation() {
        return lobbyLocation;
    }

    public void setLobbyLocation(Location location) {
        this.lobbyLocation = location;

        FileConfiguration config = plugin.getConfig();
        config.set("lobby", location);
        plugin.saveConfig();
    }

    public int getActivePlayerCount() {
        return activePlayers.size();
    }


    public Set<UUID> getActivePlayers() {
        return new HashSet<>(activePlayers);
    }

    public int getEnabledMinigameCount() {
        return (int) minigames.values().stream()
                .filter(MinigameConfig::isEnabled)
                .count();
    }

    public boolean isPvpEnabled() {
        return pvpEnabled;
    }

    public boolean minigameExists(String name) {
        return minigames.containsKey(name.toLowerCase());
    }


    public enum GameState {
        INACTIVE,   // Geen actieve game
        STARTING,   // Game start binnenkort
        ACTIVE      // Game is actief
    }
}
