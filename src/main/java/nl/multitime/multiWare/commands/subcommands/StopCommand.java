package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class StopCommand extends SubCommand {
    private final MultiWare plugin;

    public StopCommand(MultiWare plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public String getDescription() {
        return "Stopt de huidige minigame";
    }

    @Override
    public String getSyntax() {
        return "/mw stop";
    }

    @Override
    public String getPermission() {
        return "multiware.command.stop";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(ChatColor.RED + "Je hebt geen toestemming om dit commando uit te voeren!");
            return true;
        }

        if (plugin.getGameManager().getGameState() == GameManager.GameState.INACTIVE) {
            sender.sendMessage(ChatColor.RED + "Er is momenteel geen actieve minigame!");
            return true;
        }

        plugin.getGameManager().endGame();
        sender.sendMessage(ChatColor.GREEN + "De minigame is gestopt!");

        return true;
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
