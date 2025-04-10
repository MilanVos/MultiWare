package nl.multitime.multiWare.listeners;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.game.GameManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final MultiWare plugin;

    public PlayerListener(MultiWare plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        GameManager gameManager = plugin.getGameManager();
        if (gameManager.getGameState() == GameManager.GameState.ACTIVE) {
            gameManager.addPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        GameManager gameManager = plugin.getGameManager();
        gameManager.removePlayer(player.getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            GameManager gameManager = plugin.getGameManager();

            if (!gameManager.isPvPEnabled()) {
                event.setCancelled(true);
            }
        }
    }
}
