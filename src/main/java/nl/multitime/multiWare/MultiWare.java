package nl.multitime.multiWare;

import nl.multitime.multiWare.commands.CommandManager;
import nl.multitime.multiWare.game.GameManager;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import nl.multitime.multiWare.listeners.PlayerListener;
import nl.multitime.multiWare.stats.PlayerStats;
import nl.multitime.multiWare.stats.StatsManager;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MultiWare extends JavaPlugin {

    private GameManager gameManager;
    private StatsManager statsManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(MinigameConfig.class);
        ConfigurationSerialization.registerClass(PlayerStats.class);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File minigamesDir = new File(getDataFolder(), "minigames");
        if (!minigamesDir.exists()) {
            minigamesDir.mkdirs();
        }

        saveDefaultConfig();

        statsManager = new StatsManager(this);
        gameManager = new GameManager(this);

        gameManager.loadMinigames();

        commandManager = new CommandManager(this);
        getCommand("mw").setExecutor(commandManager);
        getCommand("mw").setTabCompleter(commandManager);

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("MultiWare is ingeschakeld!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopAllGames();
        }

        if (statsManager != null) {
            statsManager.saveStats();
        }

        getLogger().info("MultiWare is uitgeschakeld!");
    }


    public GameManager getGameManager() {
        return gameManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }
}
