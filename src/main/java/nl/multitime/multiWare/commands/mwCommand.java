package nl.multitime.multiWare.commands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.game.GameManager;
import nl.multitime.multiWare.game.minigames.Minigame;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class mwCommand implements CommandExecutor, TabCompleter {

    private final MultiWare plugin;
    private final List<String> subCommands = Arrays.asList(
        "start", "stop", "info", "help",
        "create", "edit", "remove", "disable", "enable",
        "list", "teleport", "setspawn"
    );

    private final Map<UUID, String> playersInEditMode = new HashMap<>();

    public mwCommand(MultiWare plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "start":
                handleStartCommand(sender);
                break;

            case "stop":
                handleStopCommand(sender);
                break;

            case "info":
                handleInfoCommand(sender);
                break;

            case "help":
                sendHelpMessage(sender);
                break;

            case "list":
                handleListCommand(sender);
                break;

            case "create":
                handleCreateCommand(sender, args);
                break;

            case "edit":
                handleEditCommand(sender, args);
                break;

            case "remove":
                handleRemoveCommand(sender, args);
                break;

            case "disable":
                handleDisableCommand(sender, args);
                break;

            case "enable":
                handleEnableCommand(sender, args);
                break;

            case "teleport":
            case "tp":
                handleTeleportCommand(sender, args);
                break;

            case "setspawn":
                handleSetSpawnCommand(sender, args);
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Ongeldige subcommand. Gebruik /mw help voor een lijst met commando's.");
                break;
        }

        return true;
    }

    private boolean checkPermission(CommandSender sender, String permission) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(ChatColor.RED + "Je hebt geen toestemming om dit commando te gebruiken.");
            return false;
        }
        return true;
    }

    private void handleStartCommand(CommandSender sender) {
        if (!checkPermission(sender, "multiware.admin")) return;

        GameManager gameManager = plugin.getGameManager();

        if (gameManager.getGameState() != GameManager.GameState.INACTIVE) {
            sender.sendMessage(ChatColor.RED + "Er is al een game bezig!");
            return;
        }

        if (gameManager.getEnabledMinigameCount() < 3) {
            sender.sendMessage(ChatColor.RED + "Er zijn niet genoeg minigames ingeschakeld (minimaal 3 nodig).");
            sender.sendMessage(ChatColor.YELLOW + "Gebruik /mw list om beschikbare minigames te zien.");
            return;
        }

        if (Bukkit.getOnlinePlayers().size() < 2) {
            sender.sendMessage(ChatColor.RED + "Er zijn niet genoeg spelers online (minimaal 2 nodig).");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "MultiWare game wordt gestart...");
        Bukkit.broadcastMessage(ChatColor.GOLD + "Een nieuwe MultiWare game wordt gestart door " +
                                ChatColor.YELLOW + sender.getName() + ChatColor.GOLD + "!");

        gameManager.startGame();
    }

    private void handleStopCommand(CommandSender sender) {
        if (!checkPermission(sender, "multiware.admin")) return;

        GameManager gameManager = plugin.getGameManager();

        if (gameManager.getGameState() == GameManager.GameState.INACTIVE) {
            sender.sendMessage(ChatColor.RED + "Er is geen game bezig om te stoppen!");
            return;
        }

        sender.sendMessage(ChatColor.GREEN + "MultiWare game wordt gestopt...");
        Bukkit.broadcastMessage(ChatColor.GOLD + "De huidige MultiWare game is gestopt door " +
                                ChatColor.YELLOW + sender.getName() + ChatColor.GOLD + ".");

        gameManager.stopAllGames();
    }

    private void handleInfoCommand(CommandSender sender) {
        GameManager gameManager = plugin.getGameManager();
        GameManager.GameState currentState = gameManager.getGameState();

        sender.sendMessage(ChatColor.GOLD + "=== MultiWare Info ===");
        sender.sendMessage(ChatColor.YELLOW + "Status: " + formatGameState(currentState));

        if (currentState != GameManager.GameState.INACTIVE) {
            sender.sendMessage(ChatColor.YELLOW + "Spelers: " + ChatColor.WHITE + gameManager.getPlayerCount());

            if (currentState == GameManager.GameState.ACTIVE) {
                sender.sendMessage(ChatColor.YELLOW + "Huidige minigame: " +
                                  ChatColor.WHITE + gameManager.getCurrentGameName());

                sender.sendMessage(ChatColor.YELLOW + "Top scores:");
                List<String> topScores = gameManager.getTopScores(3);
                if (topScores.isEmpty()) {
                    sender.sendMessage(ChatColor.GRAY + "Nog geen scores beschikbaar.");
                } else {
                    for (String scoreInfo : topScores) {
                        sender.sendMessage(ChatColor.WHITE + "  " + scoreInfo);
                    }
                }
            }
        } else {
            int totalMinigames = gameManager.getTotalMinigameCount();
            int enabledMinigames = gameManager.getEnabledMinigameCount();

            sender.sendMessage(ChatColor.YELLOW + "Beschikbare minigames: " +
                              ChatColor.WHITE + enabledMinigames + "/" + totalMinigames);

            if (enabledMinigames < 3) {
                sender.sendMessage(ChatColor.RED + "Let op: Er zijn minimaal 3 ingeschakelde minigames nodig om te spelen.");
            }
        }
    }

    private void handleListCommand(CommandSender sender) {
        GameManager gameManager = plugin.getGameManager();
        List<MinigameConfig> minigames = gameManager.getAllMinigameConfigs();

        if (minigames.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Er zijn nog geen minigames geconfigureerd.");
            if (sender.hasPermission("multiware.admin")) {
                sender.sendMessage(ChatColor.YELLOW + "Gebruik /mw create <naam> om een nieuwe minigame aan te maken.");
            }
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== MultiWare Minigames ===");

        for (MinigameConfig config : minigames) {
            String status = config.isEnabled() ?
                ChatColor.GREEN + "✓ Ingeschakeld" :
                ChatColor.RED + "✗ Uitgeschakeld";

            sender.sendMessage(
                ChatColor.YELLOW + config.getName() + " " +
                ChatColor.GRAY + "(" + config.getType() + ") " +
                status
            );
        }
    }

    private void handleCreateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt.");
            return;
        }

        if (!checkPermission(sender, "multiware.admin")) return;

        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /mw create <naam> <type>");
            sender.sendMessage(ChatColor.YELLOW + "Beschikbare types: clickpressureplate, breakblock");
            return;
        }

        String name = args[1];
        String type = args[2].toLowerCase();
        Player player = (Player) sender;

        if (plugin.getGameManager().minigameExists(name)) {
            sender.sendMessage(ChatColor.RED + "Er bestaat al een minigame met de naam '" + name + "'.");
            return;
        }

        List<String> validTypes = Arrays.asList("clickpressureplate", "breakblock");
        if (!validTypes.contains(type)) {
            sender.sendMessage(ChatColor.RED + "Ongeldig minigame type: " + type);
            sender.sendMessage(ChatColor.YELLOW + "Beschikbare types: " + String.join(", ", validTypes));
            return;
        }

        MinigameConfig config = new MinigameConfig(name, type);
        config.setEnabled(true);
        config.setSpawnLocation(player.getLocation());

        if (plugin.getGameManager().saveMinigameConfig(config)) {
            sender.sendMessage(ChatColor.GREEN + "Minigame '" + name + "' is aangemaakt!");
            sender.sendMessage(ChatColor.YELLOW + "Gebruik /mw edit " + name + " om de minigame te configureren.");
        } else {
            sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het aanmaken van de minigame.");
        }
    }

    private void handleEditCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt.");
            return;
        }

        if (!checkPermission(sender, "multiware.admin")) return;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /mw edit <naam>");
            return;
        }

        String name = args[1];
        Player player = (Player) sender;

        if (!plugin.getGameManager().minigameExists(name)) {
            sender.sendMessage(ChatColor.RED + "Er bestaat geen minigame met de naam '" + name + "'.");
            return;
        }

        MinigameConfig config = plugin.getGameManager().getMinigameConfig(name);

        playersInEditMode.put(player.getUniqueId(), name);

        if (config.getSpawnLocation() != null) {
            player.teleport(config.getSpawnLocation());
        }

        sender.sendMessage(ChatColor.GREEN + "Je bent nu in edit mode voor minigame '" + name + "'.");
        sender.sendMessage(ChatColor.YELLOW + "Type: " + config.getType());
        sender.sendMessage(ChatColor.YELLOW + "Status: " + (config.isEnabled() ? "Ingeschakeld" : "Uitgeschakeld"));

        switch (config.getType()) {
            case "clickpressureplate":
                sender.sendMessage(ChatColor.YELLOW + "Instructies:");
                sender.sendMessage(ChatColor.GRAY + "1. Gebruik /mw setspawn " + name + " om de spawn locatie in te stellen");
                sender.sendMessage(ChatColor.GRAY + "2. Plaats een drukplaat waar spelers op moeten drukken");
                break;

            case "breakblock":
                sender.sendMessage(ChatColor.YELLOW + "Instructies:");
                sender.sendMessage(ChatColor.GRAY + "1. Gebruik /mw setspawn " + name + " om de spawn locatie in te stellen");
                sender.sendMessage(ChatColor.GRAY + "2. Bouw een arena met blokken die spelers moeten breken");
                break;
        }

        sender.sendMessage(ChatColor.YELLOW + "Gebruik /mw help voor meer commando's.");
    }

    private void handleRemoveCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "multiware.admin")) return;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /mw remove <naam>");
            return;
        }

        String name = args[1];

        if (!plugin.getGameManager().minigameExists(name)) {
            sender.sendMessage(ChatColor.RED + "Er bestaat geen minigame met de naam '" + name + "'.");
            return;
        }

        playersInEditMode.entrySet().removeIf(entry -> entry.getValue().equals(name));

        if (plugin.getGameManager().removeMinigame(name)) {
            sender.sendMessage(ChatColor.GREEN + "Minigame '" + name + "' is verwijderd!");
        } else {
            sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het verwijderen van de minigame.");
        }
    }

    private void handleDisableCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "multiware.admin")) return;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /mw disable <naam>");
            return;
        }

        String name = args[1];

        if (!plugin.getGameManager().minigameExists(name)) {
            sender.sendMessage(ChatColor.RED + "Er bestaat geen minigame met de naam '" + name + "'.");
            return;
        }

        MinigameConfig config = plugin.getGameManager().getMinigameConfig(name);

        if (!config.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Minigame '" + name + "' is al uitgeschakeld.");
            return;
        }

        config.setEnabled(false);

        if (plugin.getGameManager().saveMinigameConfig(config)) {
            sender.sendMessage(ChatColor.GREEN + "Minigame '" + name + "' is uitgeschakeld!");
        } else {
            sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het uitschakelen van de minigame.");
        }
    }

    private void handleEnableCommand(CommandSender sender, String[] args) {
        if (!checkPermission(sender, "multiware.admin")) return;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /mw enable <naam>");
            return;
        }

        String name = args[1];

        if (!plugin.getGameManager().minigameExists(name)) {
            sender.sendMessage(ChatColor.RED + "Er bestaat geen minigame met de naam '" + name + "'.");
            return;
        }

        MinigameConfig config = plugin.getGameManager().getMinigameConfig(name);

        if (config.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Minigame '" + name + "' is al ingeschakeld.");
            return;
        }

        if (!config.isComplete()) {
            sender.sendMessage(ChatColor.RED + "Minigame '" + name + "' is niet volledig geconfigureerd.");
            sender.sendMessage(ChatColor.YELLOW + "Gebruik /mw edit " + name + " om de minigame te configureren.");
            return;
        }

        config.setEnabled(true);

        if (plugin.getGameManager().saveMinigameConfig(config)) {
            sender.sendMessage(ChatColor.GREEN + "Minigame '" + name + "' is ingeschakeld!");
        } else {
            sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het inschakelen van de minigame.");
        }
    }

    private void handleTeleportCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt.");
            return;
        }

        if (!checkPermission(sender, "multiware.admin")) return;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /mw teleport <naam>");
            return;
        }

        String name = args[1];
        Player player = (Player) sender;

        if (!plugin.getGameManager().minigameExists(name)) {
            sender.sendMessage(ChatColor.RED + "Er bestaat geen minigame met de naam '" + name + "'.");
            return;
        }

        MinigameConfig config = plugin.getGameManager().getMinigameConfig(name);

        if (config.getSpawnLocation() == null) {
            sender.sendMessage(ChatColor.RED + "De spawn locatie voor minigame '" + name + "' is niet ingesteld.");
            if (sender.hasPermission("multiware.admin")) {
                sender.sendMessage(ChatColor.YELLOW + "Gebruik /mw setspawn " + name + " om de spawn locatie in te stellen.");
            }
            return;
        }

        player.teleport(config.getSpawnLocation());
        sender.sendMessage(ChatColor.GREEN + "Je bent geteleporteerd naar minigame '" + name + "'.");
    }

    private void handleSetSpawnCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt.");
            return;
        }

        if (!checkPermission(sender, "multiware.admin")) return;

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: /mw setspawn <naam>");
            return;
        }

        String name = args[1];
        Player player = (Player) sender;

        if (!plugin.getGameManager().minigameExists(name)) {
            sender.sendMessage(ChatColor.RED + "Er bestaat geen minigame met de naam '" + name + "'.");
            return;
        }

        MinigameConfig config = plugin.getGameManager().getMinigameConfig(name);

        config.setSpawnLocation(player.getLocation());

        if (plugin.getGameManager().saveMinigameConfig(config)) {
            sender.sendMessage(ChatColor.GREEN + "Spawn locatie voor minigame '" + name + "' is ingesteld!");
        } else {
            sender.sendMessage(ChatColor.RED + "Er is een fout opgetreden bij het instellen van de spawn locatie.");
        }
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== MultiWare Commando's ===");
        sender.sendMessage(ChatColor.YELLOW + "/mw start " + ChatColor.GRAY + "- Start een nieuwe MultiWare game");
        sender.sendMessage(ChatColor.YELLOW + "/mw stop " + ChatColor.GRAY + "- Stop de huidige MultiWare game");
        sender.sendMessage(ChatColor.YELLOW + "/mw info " + ChatColor.GRAY + "- Toon informatie over de huidige game");
        sender.sendMessage(ChatColor.YELLOW + "/mw list " + ChatColor.GRAY + "- Toon een lijst met beschikbare minigames");

        if (sender.hasPermission("multiware.admin")) {
            sender.sendMessage(ChatColor.GOLD + "=== Admin Commando's ===");
            sender.sendMessage(ChatColor.YELLOW + "/mw create <naam> <type> " + ChatColor.GRAY + "- Maak een nieuwe minigame aan");
            sender.sendMessage(ChatColor.YELLOW + "/mw edit <naam> " + ChatColor.GRAY + "- Bewerk een bestaande minigame");
            sender.sendMessage(ChatColor.YELLOW + "/mw remove <naam> " + ChatColor.GRAY + "- Verwijder een minigame");
            sender.sendMessage(ChatColor.YELLOW + "/mw disable <naam> " + ChatColor.GRAY + "- Schakel een minigame uit");
            sender.sendMessage(ChatColor.YELLOW + "/mw enable <naam> " + ChatColor.GRAY + "- Schakel een minigame in");
            sender.sendMessage(ChatColor.YELLOW + "/mw teleport <naam> " + ChatColor.GRAY + "- Teleporteer naar een minigame");
            sender.sendMessage(ChatColor.YELLOW + "/mw setspawn <naam> " + ChatColor.GRAY + "- Stel de spawn locatie in voor een minigame");
        }

        sender.sendMessage(ChatColor.YELLOW + "/mw help " + ChatColor.GRAY + "- Toon dit helpbericht");
    }

    private String formatGameState(GameManager.GameState state) {
        switch (state) {
            case INACTIVE:
                return ChatColor.RED + "Inactief";
            case STARTING:
                return ChatColor.YELLOW + "Startend";
            case ACTIVE:
                return ChatColor.GREEN + "Actief";
            case ENDING:
                return ChatColor.GOLD + "Eindigend";
            default:
                return ChatColor.GRAY + "Onbekend";
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String partialCommand = args[0].toLowerCase();
            List<String> availableCommands = new ArrayList<>();

            for (String cmd : subCommands) {
                if (cmd.startsWith(partialCommand)) {
                    if (Arrays.asList("create", "edit", "remove", "disable", "enable", "teleport", "setspawn").contains(cmd)) {
                        if (sender.hasPermission("multiware.admin")) {
                            availableCommands.add(cmd);
                        }
                    } else {
                        availableCommands.add(cmd);
                    }
                }
            }

            return availableCommands;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            String partialArg = args[1].toLowerCase();

            switch (subCommand) {
                case "edit":
                case "remove":
                case "disable":
                case "enable":
                case "teleport":
                case "setspawn":
                    if (sender.hasPermission("multiware.admin")) {
                        List<String> minigames = plugin.getGameManager().getAllMinigameNames();
                        return minigames.stream()
                                .filter(name -> name.toLowerCase().startsWith(partialArg))
                                .collect(Collectors.toList());
                    }
                    break;
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("create")) {
            String partialType = args[2].toLowerCase();
            List<String> validTypes = Arrays.asList("clickpressureplate", "breakblock");

            return validTypes.stream()
                    .filter(type -> type.startsWith(partialType))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}
