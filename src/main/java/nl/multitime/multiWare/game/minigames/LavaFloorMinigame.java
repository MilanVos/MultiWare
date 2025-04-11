package nl.multitime.multiWare.game.minigames;

import nl.multitime.multiWare.MultiWare;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class LavaFloorMinigame extends Minigame {

    private final Set<UUID> eliminatedPlayers = new HashSet<>();
    private final Set<UUID> completedPlayers = new LinkedHashSet<>();
    private final Map<Location, Integer> lavaCountdowns = new HashMap<>();

    public LavaFloorMinigame(MultiWare plugin, MinigameConfig config) {
        super(plugin, config.getName(), "De vloer verandert in lava! Blijf in leven!");
        this.spawnLocation = config.getSpawnLocation();
    }

    @Override
    public void start() {
        super.start();

        eliminatedPlayers.clear();
        completedPlayers.clear();
        lavaCountdowns.clear();

        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.teleport(spawnLocation);
            }
        }

        broadcastMessage(ChatColor.GOLD + "Blijf rennen! De vloer verandert in lava!");
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!players.contains(uuid) || eliminatedPlayers.contains(uuid)) return;

        Location from = event.getFrom().getBlock().getLocation();
        Location to = event.getTo().getBlock().getLocation();
        if (from.equals(to)) return;

        Location under = to.clone().subtract(0, 1, 0);
        if (lavaCountdowns.containsKey(under)) return;

        lavaCountdowns.put(under, 0);
        startLavaCountdown(under);

        Block feetBlock = player.getLocation().subtract(0, 1, 0).getBlock();
        if (feetBlock.getType() == Material.LAVA || feetBlock.getType() == Material.LAVA_CAULDRON) {
            eliminate(player);
        }
    }

    private void startLavaCountdown(Location loc) {
        new BukkitRunnable() {
            int stage = 0;
            final Material[] stages = {Material.YELLOW_WOOL, Material.ORANGE_WOOL, Material.RED_WOOL, Material.LAVA};

            @Override
            public void run() {
                if (stage >= stages.length) {
                    cancel();
                    return;
                }
                Block block = loc.getBlock();
                block.setType(stages[stage]);
                stage++;
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    private void eliminate(Player player) {
        if (!eliminatedPlayers.contains(player.getUniqueId())) {
            eliminatedPlayers.add(player.getUniqueId());
            broadcastMessage(ChatColor.RED + player.getName() + " is in de lava gevallen!");
            checkForCompletion();
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (isActive()) {
            eliminate(event.getPlayer());
        }
    }

    private void checkForCompletion() {
        int remaining = 0;
        UUID lastStanding = null;
        for (UUID id : players) {
            if (!eliminatedPlayers.contains(id)) {
                remaining++;
                lastStanding = id;
            }
        }

        if (remaining == 0) {
            complete();
        } else if (remaining == 1 && lastStanding != null) {
            completedPlayers.add(lastStanding);
            Bukkit.getScheduler().runTaskLater(plugin, this::complete, 60L);
        }
    }

    @Override
    public void end() {
        super.end();

        List<UUID> survivingPlayers = new ArrayList<>();
        for (UUID id : players) {
            if (!eliminatedPlayers.contains(id)) {
                survivingPlayers.add(id);
            }
        }

        survivingPlayers.addAll(completedPlayers);

        int points = 3;
        for (UUID playerId : survivingPlayers) {
            if (points > 0) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    addScore(playerId, points);
                    player.sendMessage(ChatColor.GREEN + "Je hebt " + points + " punten verdiend!");
                }
                points--;
            }
        }
    }

    @Override
    public boolean isPvPEnabled() {
        return false;
    }
}
