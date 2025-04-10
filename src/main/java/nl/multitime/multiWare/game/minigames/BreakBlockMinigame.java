package nl.multitime.multiWare.game.minigames;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import nl.multitime.multiWare.MultiWare;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BreakBlockMinigame extends Minigame {

    private final Location arenaMin;
    private final Location arenaMax;
    private final Map<UUID, Integer> blocksDestroyed = new HashMap<>();

    public BreakBlockMinigame(MultiWare plugin, MinigameConfig config) {
        super(plugin, config.getName(), "Breek zoveel mogelijk blokken!");

        this.arenaMin = (Location) config.getExtraData("arenaMin");
        this.arenaMax = (Location) config.getExtraData("arenaMax");

        this.spawnLocation = config.getSpawnLocation();
    }

    @Override
    public void start() {
        super.start();

        blocksDestroyed.clear();

        for (UUID playerId : players) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                player.teleport(spawnLocation);
                blocksDestroyed.put(playerId, 0);
            }
        }

        broadcastMessage(ChatColor.GOLD + "Breek zoveel mogelijk blokken binnen de tijd!");
    }

    @Override
    public void end() {
        super.end();

        for (Map.Entry<UUID, Integer> entry : blocksDestroyed.entrySet()) {
            UUID playerId = entry.getKey();
            int blocks = entry.getValue();

            int points = Math.max(1, blocks / 5);

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                addScore(playerId, points);
                player.sendMessage(ChatColor.GREEN + "Je hebt " + blocks + " blokken gebroken en " +
                                  points + " punten verdiend!");
            }
        }

        broadcastMessage(ChatColor.GOLD + "Top 3 blokkenbrekers:");
        blocksDestroyed.entrySet().stream()
            .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player != null) {
                    broadcastMessage(ChatColor.YELLOW + player.getName() + ": " +
                                    entry.getValue() + " blokken");
                }
            });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isActive()) return;

        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (!players.contains(player.getUniqueId())) return;

        if (!isBlockInArena(block.getLocation())) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Je kunt alleen blokken binnen de arena breken!");
            return;
        }

        if (isUnbreakableBlock(block.getType())) {
            event.setCancelled(true);
            return;
        }

        int currentCount = blocksDestroyed.getOrDefault(player.getUniqueId(), 0);
        blocksDestroyed.put(player.getUniqueId(), currentCount + 1);

        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GOLD + "Blokken gebroken: " + ChatColor.WHITE + (currentCount + 1)));
    }
    private boolean isBlockInArena(Location location) {
        return location.getWorld().equals(arenaMin.getWorld()) &&
               location.getBlockX() >= arenaMin.getBlockX() && location.getBlockX() <= arenaMax.getBlockX() &&
               location.getBlockY() >= arenaMin.getBlockY() && location.getBlockY() <= arenaMax.getBlockY() &&
               location.getBlockZ() >= arenaMin.getBlockZ() && location.getBlockZ() <= arenaMax.getBlockZ();
    }

    private boolean isUnbreakableBlock(Material material) {
        return material == Material.BEDROCK ||
               material == Material.BARRIER ||
               material == Material.COMMAND_BLOCK ||
               material == Material.STRUCTURE_BLOCK;
    }

    @Override
    public boolean isPvPEnabled() {
        return false; // Deze minigame heeft geen PvP nodig
    }
}
