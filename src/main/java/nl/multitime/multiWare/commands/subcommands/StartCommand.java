package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class StartCommand extends SubCommand {

    public StartCommand(MultiWare plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        GameManager gameManager = plugin.getGameManager();

        if (gameManager.getGameState() != GameManager.GameState.INACTIVE) {
            sender.sendMessage(ChatColor.RED + "Er is al een game bezig!");
            return true;
        }

        if (gameManager.getEnabledMinigameCount() < 3) {
            sender.sendMessage(ChatColor.RED + "Er zijn niet genoeg minigames beschikbaar om te starten! (Minimaal 3 nodig)");
            return true;
        }

        gameManager.startGame();
        sender.sendMessage(ChatColor.GREEN + "MultiWare game wordt gestart!");
        return true;
    }

    @Override
    public String getName() {
        return "start";
    }

    @Override
    public String getDescription() {
        return "Start een nieuwe MultiWare game";
    }

    @Override
    public String getSyntax() {
        return "/mw start";
    }

    @Override
    public String getPermission() {
        return "multiware.start";
    }
}
