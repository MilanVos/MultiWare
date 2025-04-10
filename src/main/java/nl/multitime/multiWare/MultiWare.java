package nl.multitime.multiWare;

import nl.multitime.multiWare.commands.mwCommand;
import nl.multitime.multiWare.game.GameManager;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import nl.multitime.multiWare.listeners.PlayerListener;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class MultiWare extends JavaPlugin {

    private GameManager gameManager;

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(MinigameConfig.class);

        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File minigamesDir = new File(getDataFolder(), "minigames");
        if (!minigamesDir.exists()) {
            minigamesDir.mkdirs();
        }

        saveDefaultConfig();

        gameManager = new GameManager(this);

        gameManager.loadMinigames();

        getCommand("mw").setExecutor(new mwCommand(this));

        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("MultiWare is ingeschakeld!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stopAllGames();
        }

        getLogger().info("MultiWare is uitgeschakeld!");
    }


    public GameManager getGameManager() {
        return gameManager;
    }
}
