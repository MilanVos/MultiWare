package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.GameManager;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CreateCommand extends SubCommand {

    private final MultiWare plugin;
    private final List<String> validTypes = Arrays.asList("clickpressureplate", "breakblock");

    public CreateCommand(MultiWare plugin) {
        super(plugin);
        this.plugin = plugin;
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt!");
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Gebruik: " + getSyntax());
            return true;
        }

        Player player = (Player) sender;
        String name = args[1];
        String type = args[2].toLowerCase();

        if (!validTypes.contains(type)) {
            sender.sendMessage(ChatColor.RED + "Ongeldig minigame type! Geldige types: " + String.join(", ", validTypes));
            return true;
        }

        GameManager gameManager = plugin.getGameManager();

        if (gameManager.minigameExists(name)) {
            sender.sendMessage(ChatColor.RED + "Er bestaat al een minigame met de naam '" + name + "'!");
            return true;
        }

        MinigameConfig config = new MinigameConfig(name, type);
        config.setSpawnLocation(player.getLocation());
        config.setEnabled(true);

        if (gameManager.saveMinigameConfig(config)) {
            sender.sendMessage(ChatColor.GREEN + "Minigame '" + name + "' aangemaakt! Gebruik /mw edit " + name + " om de minigame te configureren.");
        } else {
            sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het aanmaken van de minigame!");
        }

        return true;
    }

    @Override
    public String getName() {
        return "create";
    }

    @Override
    public String getDescription() {
        return "Maak een nieuwe minigame aan";
    }

    @Override
    public String getSyntax() {
        return "/mw create <naam> <type>";
    }

    @Override
    public String getPermission() {
        return "multiware.create";
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        if (args.length == 3) {
            return validTypes.stream()
                    .filter(type -> type.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return super.getTabCompletions(sender, args);
    }
}
