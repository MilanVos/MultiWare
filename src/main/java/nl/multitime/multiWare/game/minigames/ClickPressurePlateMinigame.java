package nl.multitime.multiWare.game.minigames;

import nl.multitime.multiWare.MultiWare;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ClickPressurePlateMinigame extends Minigame {

    private final Location pressurePlateLocation;
    private final Set<UUID> completedPlayers = new HashSet<>();

    public ClickPressurePlateMinigame(MultiWare plugin, MinigameConfig config) {
        super(plugin, config.getName(), "Druk op de drukplaat!");

        this.pressurePlateLocation = (Location) config.getExtraData("pressurePlateLocation");

        this.spawnLocation = config.getSpawnLocation();
    }

    @Override
    public void start() {
        super.start();

        completedPlayers.clear();

        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.teleport(spawnLocation);
            }
        }

        broadcastMessage(ChatColor.GOLD + "Wees de eerste die de drukplaat vindt en erop drukt!");
    }

    @Override
    public void end() {
        super.end();

        int points = 3; // Begin met 3 punten voor de eerste speler

        for (UUID playerId : completedPlayers) {
            if (points > 0) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    addScore(playerId, points);
                    player.sendMessage(ChatColor.GREEN + "Je hebt " + points + " punten verdiend!");
                }
                points--; // Verminder de punten voor de volgende speler
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!isActive()) return;

        Player player = event.getPlayer();

        if (!players.contains(player.getUniqueId())) return;

        if (event.getAction() != Action.PHYSICAL) return;

        if (event.getClickedBlock() == null) return;
        if (!event.getClickedBlock().getType().name().contains("PRESSURE_PLATE")) return;

        Location clickedLocation = event.getClickedBlock().getLocation();
        if (!isSameLocation(clickedLocation, pressurePlateLocation)) return;

        if (completedPlayers.contains(player.getUniqueId())) return;

        completedPlayers.add(player.getUniqueId());

        broadcastMessage(ChatColor.GOLD + player.getName() + " heeft de drukplaat gevonden!");

        if (completedPlayers.size() >= players.size()) {
            Bukkit.getScheduler().runTaskLater(plugin, this::complete, 20L);
        }
    }

    private boolean isSameLocation(Location loc1, Location loc2) {
        return loc1.getWorld().equals(loc2.getWorld()) &&
               loc1.getBlockX() == loc2.getBlockX() &&
               loc1.getBlockY() == loc2.getBlockY() &&
               loc1.getBlockZ() == loc2.getBlockZ();
    }

    @Override
    public boolean isPvPEnabled() {
        return false; // Deze minigame heeft geen PvP nodig
    }
}
