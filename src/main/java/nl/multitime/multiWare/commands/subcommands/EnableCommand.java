package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class EnableCommand extends SubCommand {

    private final MultiWare plugin;

    public EnableCommand(MultiWare plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "enable";
    }

    @Override
    public String getDescription() {
        return "Schakelt een minigame in";
    }

    @Override
    public String getSyntax() {
        return "/mw enable <minigame>";
    }

    @Override
    public String getPermission() {
        return "multiware.command.enable";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(ChatColor.RED + "Je hebt geen toestemming om dit commando uit te voeren!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Gebruik: " + getSyntax());
            return true;
        }

        String minigameName = args[0];
        if (!plugin.getGameManager().minigameExists(minigameName)) {
            sender.sendMessage(ChatColor.RED + "Minigame '" + minigameName + "' bestaat niet!");
            return true;
        }

        MinigameConfig config = plugin.getGameManager().getMinigameConfig(minigameName);
        if (config.isEnabled()) {
            sender.sendMessage(ChatColor.YELLOW + "Minigame '" + minigameName + "' is al ingeschakeld!");
            return true;
        }

        config.setEnabled(true);
        if (plugin.getGameManager().saveMinigameConfig(config)) {
            sender.sendMessage(ChatColor.GREEN + "Minigame '" + minigameName + "' is ingeschakeld!");
        } else {
            sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het inschakelen van minigame '" + minigameName + "'!");
        }

        return true;
    }


    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Alleen minigames tonen die uitgeschakeld zijn
            for (String name : plugin.getGameManager().getAllMinigameNames()) {
                MinigameConfig config = plugin.getGameManager().getMinigameConfig(name);
                if (!config.isEnabled()) {
                    completions.add(name);
                }
            }
        }

        return completions;
    }
}
