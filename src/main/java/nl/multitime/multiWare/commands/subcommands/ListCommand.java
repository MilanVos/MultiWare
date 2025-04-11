package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ListCommand extends SubCommand {

    private final MultiWare plugin;

    public ListCommand(MultiWare plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "Toont een lijst van alle minigames";
    }

    @Override
    public String getSyntax() {
        return "/mw list";
    }

    @Override
    public String getPermission() {
        return "multiware.command.list";
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (!sender.hasPermission(getPermission())) {
            sender.sendMessage(ChatColor.RED + "Je hebt geen toestemming om dit commando uit te voeren!");
            return true;
        }

        List<String> minigameNames = plugin.getGameManager().getAllMinigameNames();
        if (minigameNames.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Er zijn geen minigames beschikbaar!");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Beschikbare Minigames (" + minigameNames.size() + ") ===");
        for (String name : minigameNames) {
            MinigameConfig config = plugin.getGameManager().getMinigameConfig(name);
            String status = config.isEnabled() ? ChatColor.GREEN + "✓" : ChatColor.RED + "✗";
            sender.sendMessage(status + " " + ChatColor.YELLOW + config.getName() + ChatColor.GRAY + " - " + config.getType());
        }

        return true;
    }


    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>(); // Geen tab completions nodig voor dit commando
    }
}
