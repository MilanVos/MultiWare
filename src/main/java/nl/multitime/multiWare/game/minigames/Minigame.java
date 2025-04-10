package nl.multitime.multiWare.game.minigames;

import nl.multitime.multiWare.MultiWare;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public abstract class Minigame implements Listener {

    protected final MultiWare plugin;
    protected final String name;
    protected final String description;
    protected Location spawnLocation;
    public Set<UUID> players = new HashSet<>();
    protected Map<UUID, Integer> scores = new HashMap<>();
    protected boolean active = false;
    protected BukkitTask gameTimer;


    public Minigame(MultiWare plugin, String name, String description) {
        this.plugin = plugin;
        this.name = name;
        this.description = description;
    }


    public void start() {
        active = true;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        gameTimer = Bukkit.getScheduler().runTaskLater(plugin, this::complete, 60 * 20L);

        broadcastMessage(ChatColor.GOLD + "Minigame gestart: " + ChatColor.YELLOW + name);
        broadcastMessage(ChatColor.GOLD + description);
    }

    public void end() {
        active = false;

        if (gameTimer != null) {
            gameTimer.cancel();
            gameTimer = null;
        }

        HandlerList.unregisterAll(this);

        broadcastMessage(ChatColor.GOLD + "Minigame beëindigd: " + ChatColor.YELLOW + name);
    }


    public void complete() {
        end();
    }


    public void addPlayer(UUID playerId) {
        players.add(playerId);
        scores.put(playerId, 0);
    }

    public void removePlayer(UUID playerId) {
        players.remove(playerId);
    }

    public void addScore(UUID playerId, int points) {
        int currentScore = scores.getOrDefault(playerId, 0);
        scores.put(playerId, currentScore + points);
    }

    protected void broadcastMessage(String message) {
        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.sendMessage(message);
            }
        }
    }


    public String getName() {
        return name;
    }


    public String getDescription() {
        return description;
    }


    public Location getSpawnLocation() {
        return spawnLocation;
    }


    public boolean isActive() {
        return active;
    }


    public Map<UUID, Integer> getScores() {
        return new HashMap<>(scores);
    }


    public abstract boolean isPvPEnabled();
}
