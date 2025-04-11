package nl.multitime.multiWare.game.minigames;

import nl.multitime.multiWare.MultiWare;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeMinigame extends Minigame {

    private final Set<UUID> failedPlayers = new HashSet<>();
    private final HashMap<UUID, Location> initialPositions = new HashMap<>();

    public FreezeMinigame(MultiWare plugin, MinigameConfig config) {
        super(plugin, config.getName(), "Blijf 5 seconden helemaal stil!");
        this.spawnLocation = config.getSpawnLocation();
    }

    @Override
    public void start() {
        super.start();

        failedPlayers.clear();
        initialPositions.clear();

        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.teleport(spawnLocation);
                initialPositions.put(playerId, player.getLocation());
            }
        }

        broadcastMessage(ChatColor.GOLD + "Blijf helemaal stil voor 5 seconden!");

        Bukkit.getScheduler().runTaskLater(plugin, this::end, 100L); // 5 seconds
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!players.contains(uuid) || failedPlayers.contains(uuid)) return;

        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null) return;
        if (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ()) {
            failedPlayers.add(uuid);
            player.sendMessage(ChatColor.RED + "Je hebt bewogen en bent af!");
        }
    }

    public void end() {
        for (UUID playerId : players) {
            if (!failedPlayers.contains(playerId)) {
                addScore(playerId, 1);
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    player.sendMessage(ChatColor.GREEN + "Je bleef stil en verdient 1 punt!");
                }
            }
        }
        complete();
    }

    @Override
    public boolean isPvPEnabled() {
        return false;
    }
}
