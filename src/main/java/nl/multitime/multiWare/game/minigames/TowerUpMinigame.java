package nl.multitime.multiWare.game.minigames;

import nl.multitime.multiWare.MultiWare;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TowerUpMinigame extends Minigame {

    private final Map<UUID, Location> startingHeights = new HashMap<>();
    private final Set<UUID> finishedPlayers = new LinkedHashSet<>();
    private final Set<UUID> eliminatedPlayers = new HashSet<>();

    public TowerUpMinigame(MultiWare plugin, MinigameConfig config) {
        super(plugin, config.getName(), "Bouw omhoog tot 15 blokken hoog!");
        this.spawnLocation = config.getSpawnLocation();
    }

    @Override
    public void start() {
        super.start();
        finishedPlayers.clear();
        eliminatedPlayers.clear();
        startingHeights.clear();

        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.teleport(spawnLocation);
                startingHeights.put(playerId, spawnLocation.clone());
                giveStartItems(player);
            }
        }

        broadcastMessage(ChatColor.GOLD + "Bouw omhoog tot 15 blokken hoog! Gooi sneeuwballen om anderen te stoppen!");
    }

    private void giveStartItems(Player player) {
        Material wool = Material.WHITE_WOOL;

        Material[] woolTypes = {
                Material.WHITE_WOOL, Material.RED_WOOL, Material.BLUE_WOOL,
                Material.GREEN_WOOL, Material.YELLOW_WOOL, Material.PINK_WOOL,
                Material.PURPLE_WOOL, Material.ORANGE_WOOL
        };
        wool = woolTypes[new Random().nextInt(woolTypes.length)];

        player.getInventory().clear();
        player.getInventory().addItem(new ItemStack(wool, 64));
        player.getInventory().addItem(new ItemStack(Material.SNOWBALL, 16));
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isActive()) return;

        Player player = event.getPlayer();
        if (!players.contains(player.getUniqueId())) return;
        if (eliminatedPlayers.contains(player.getUniqueId())) return;

        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.5f);

        Location base = startingHeights.get(player.getUniqueId());
        if (base == null) return;

        int heightDiff = event.getBlockPlaced().getY() - base.getBlockY();
        if (heightDiff >= 15 && !finishedPlayers.contains(player.getUniqueId())) {
            finishedPlayers.add(player.getUniqueId());
            broadcastMessage(ChatColor.GREEN + player.getName() + " heeft de top bereikt!");

            if (finishedPlayers.size() >= 3 || finishedPlayers.size() + eliminatedPlayers.size() >= players.size()) {
                Bukkit.getScheduler().runTaskLater(plugin, this::complete, 40L);
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive()) return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!players.contains(uuid)) return;
        if (eliminatedPlayers.contains(uuid)) return;
        if (player.getLocation().getY() < spawnLocation.getY() - 5) {
            eliminatedPlayers.add(uuid);
            player.sendMessage(ChatColor.RED + "Je bent gevallen en geëlimineerd!");
            broadcastMessage(ChatColor.RED + player.getName() + " is geëlimineerd!");

            if (finishedPlayers.size() + eliminatedPlayers.size() >= players.size()) {
                Bukkit.getScheduler().runTaskLater(plugin, this::complete, 40L);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (isActive()) {
            eliminatedPlayers.add(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onVoidDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID && isActive()) {
                UUID uuid = player.getUniqueId();
                if (players.contains(uuid)) {
                    eliminatedPlayers.add(uuid);
                    player.sendMessage(ChatColor.RED + "Je bent in de leegte gevallen!");
                    broadcastMessage(ChatColor.RED + player.getName() + " is geëlimineerd!");
                    event.setCancelled(true);
                    player.teleport(spawnLocation);

                    if (finishedPlayers.size() + eliminatedPlayers.size() >= players.size()) {
                        Bukkit.getScheduler().runTaskLater(plugin, this::complete, 40L);
                    }
                }
            }
        }
    }

    @Override
    public void end() {
        super.end();

        int points = 3;
        for (UUID playerId : finishedPlayers) {
            if (points > 0) {
                Player player = Bukkit.getPlayer(playerId);
                if (player != null) {
                    addScore(playerId, points);
                    player.sendMessage(ChatColor.GREEN + "Je hebt " + points + " punten verdiend!");
                }
                points--;
            }
        }

        for (UUID uuid : players) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                player.getInventory().clear();
            }
        }
    }

    @Override
    public boolean isPvPEnabled() {
        return false; // Snowball fun only, no PvP
    }
}