package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class JoinCommand extends SubCommand {

    public JoinCommand(MultiWare plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt!");
            return true;
        }

        Player player = (Player) sender;
        GameManager gameManager = plugin.getGameManager();

        if (gameManager.getGameState() == GameManager.GameState.INACTIVE) {
            player.sendMessage(ChatColor.RED + "Er is momenteel geen game actief!");
            return true;
        }

        if (gameManager.isPlayerInGame(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Je doet al mee aan de game!");
            return true;
        }

        gameManager.addPlayer(player.getUniqueId());
        player.sendMessage(ChatColor.GREEN + "Je doet nu mee aan de MultiWare game!");

        return true;
    }

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public String getDescription() {
        return "Doe mee aan de actieve MultiWare game";
    }

    @Override
    public String getSyntax() {
        return "/mw join";
    }

    @Override
    public String getPermission() {
        return null; // Iedereen mag dit commando gebruiken
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("j", "play");
    }
}
