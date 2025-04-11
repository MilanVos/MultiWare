package nl.multitime.multiWare.commands.subcommands;

import nl.multitime.multiWare.MultiWare;
import nl.multitime.multiWare.commands.SubCommand;
import nl.multitime.multiWare.game.GameManager;
import nl.multitime.multiWare.game.minigames.MinigameConfig;
import nl.multitime.multiWare.utils.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EditCommand extends SubCommand {

    public EditCommand(MultiWare plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Dit commando kan alleen door spelers worden gebruikt!");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Gebruik: " + getSyntax());
            return true;
        }

        Player player = (Player) sender;
        String name = args[1];

        GameManager gameManager = plugin.getGameManager();
        MinigameConfig config = gameManager.getMinigameConfig(name);

        if (config == null) {
            sender.sendMessage(ChatColor.RED + "Minigame '" + name + "' bestaat niet!");
            return true;
        }

        if (args.length > 2) {
            return handlePropertyEdit(player, config, args);
        }

        openEditGUI(player, config);
        return true;
    }

    private boolean handlePropertyEdit(Player player, MinigameConfig config, String[] args) {
        String property = args[2].toLowerCase();
        GameManager gameManager = plugin.getGameManager();

        switch (property) {
            case "spawn":
                config.setSpawnLocation(player.getLocation());
                player.sendMessage(ChatColor.GREEN + "Spawn locatie voor minigame '" + config.getName() + "' ingesteld!");
                break;

            case "enable":
                config.setEnabled(true);
                player.sendMessage(ChatColor.GREEN + "Minigame '" + config.getName() + "' ingeschakeld!");
                break;

            case "disable":
                config.setEnabled(false);
                player.sendMessage(ChatColor.GREEN + "Minigame '" + config.getName() + "' uitgeschakeld!");
                break;

            case "pvp":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Gebruik: /mw edit " + config.getName() + " pvp <true/false>");
                    return true;
                }
                boolean pvpEnabled = Boolean.parseBoolean(args[3]);
                config.setPvpEnabled(pvpEnabled);
                player.sendMessage(ChatColor.GREEN + "PvP voor minigame '" + config.getName() + "' " +
                                  (pvpEnabled ? "ingeschakeld!" : "uitgeschakeld!"));
                break;

            case "duration":
                if (args.length < 4) {
                    player.sendMessage(ChatColor.RED + "Gebruik: /mw edit " + config.getName() + " duration <seconden>");
                    return true;
                }
                try {
                    int duration = Integer.parseInt(args[3]);
                    if (duration < 10 || duration > 300) {
                        player.sendMessage(ChatColor.RED + "De duur moet tussen 10 en 300 seconden liggen!");
                        return true;
                    }
                    config.setDuration(duration);
                    player.sendMessage(ChatColor.GREEN + "Duur voor minigame '" + config.getName() + "' ingesteld op " + duration + " seconden!");
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Ongeldige duur: " + args[3]);
                    return true;
                }
                break;

            case "pressureplate":
                if (config.getType().equalsIgnoreCase("clickpressureplate")) {
                    config.setExtraData("pressurePlateLocation", player.getLocation());
                    player.sendMessage(ChatColor.GREEN + "Drukplaat locatie voor minigame '" + config.getName() + "' ingesteld!");
                } else {
                    player.sendMessage(ChatColor.RED + "Deze eigenschap is alleen beschikbaar voor minigames van het type 'clickpressureplate'!");
                    return true;
                }
                break;

            case "arena":
                if (config.getType().equalsIgnoreCase("breakblock")) {
                    if (args.length < 4) {
                        player.sendMessage(ChatColor.RED + "Gebruik: /mw edit " + config.getName() + " arena <min/max>");
                        return true;
                    }

                    String corner = args[3].toLowerCase();
                    if (corner.equals("min")) {
                        config.setExtraData("arenaMin", player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "Arena minimum hoek voor minigame '" + config.getName() + "' ingesteld!");
                    } else if (corner.equals("max")) {
                        config.setExtraData("arenaMax", player.getLocation());
                        player.sendMessage(ChatColor.GREEN + "Arena maximum hoek voor minigame '" + config.getName() + "' ingesteld!");
                    } else {
                        player.sendMessage(ChatColor.RED + "Ongeldig hoekpunt! Gebruik 'min' of 'max'.");
                        return true;
                    }
                } else {
                    player.sendMessage(ChatColor.RED + "Deze eigenschap is alleen beschikbaar voor minigames van het type 'breakblock'!");
                    return true;
                }
                break;

            default:
                player.sendMessage(ChatColor.RED + "Onbekende eigenschap: " + property);
                player.sendMessage(ChatColor.YELLOW + "Beschikbare eigenschappen: spawn, enable, disable, pvp, duration, pressureplate, arena");
                return true;
        }

        gameManager.saveMinigameConfig(config);
        return true;
    }

    private void openEditGUI(Player player, MinigameConfig config) {
        Inventory inventory = Bukkit.createInventory(null, 27, ChatColor.DARK_PURPLE + "Bewerk: " + config.getName());

        inventory.setItem(4, new ItemBuilder(Material.NAME_TAG)
                .name(ChatColor.GOLD + config.getName())
                .lore(ChatColor.GRAY + "Type: " + config.getType(),
                      ChatColor.GRAY + "Ingeschakeld: " + (config.isEnabled() ? ChatColor.GREEN + "Ja" : ChatColor.RED + "Nee"),
                      ChatColor.GRAY + "Duur: " + config.getDuration() + " seconden",
                      ChatColor.GRAY + "PvP: " + (config.isPvpEnabled() ? ChatColor.GREEN + "Aan" : ChatColor.RED + "Uit"))
                .build());

        inventory.setItem(10, new ItemBuilder(Material.ENDER_PEARL)
                .name(ChatColor.AQUA + "Spawn Locatie")
                .lore(ChatColor.GRAY + "Klik om de spawn locatie in te stellen",
                      ChatColor.GRAY + "op je huidige positie.")
                .build());

        inventory.setItem(12, new ItemBuilder(config.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name(config.isEnabled() ? ChatColor.GREEN + "Uitschakelen" : ChatColor.GREEN + "Inschakelen")
                .lore(ChatColor.GRAY + "Klik om de minigame " +
                      (config.isEnabled() ? "uit te schakelen." : "in te schakelen."))
                .build());

        inventory.setItem(14, new ItemBuilder(config.isPvpEnabled() ? Material.DIAMOND_SWORD : Material.WOODEN_SWORD)
                .name(ChatColor.GOLD + "PvP: " + (config.isPvpEnabled() ? ChatColor.GREEN + "Aan" : ChatColor.RED + "Uit"))
                .lore(ChatColor.GRAY + "Klik om PvP " +
                      (config.isPvpEnabled() ? "uit te schakelen." : "in te schakelen."))
                .build());

        inventory.setItem(16, new ItemBuilder(Material.CLOCK)
                .name(ChatColor.YELLOW + "Duur: " + config.getDuration() + " seconden")
                .lore(ChatColor.GRAY + "Klik om de duur aan te passen.")
                .build());
        if (config.getType().equalsIgnoreCase("clickpressureplate")) {
            inventory.setItem(20, new ItemBuilder(Material.STONE_PRESSURE_PLATE)
                    .name(ChatColor.LIGHT_PURPLE + "Drukplaat Locatie")
                    .lore(ChatColor.GRAY + "Klik om de drukplaat locatie in te stellen",
                          ChatColor.GRAY + "op je huidige positie.")
                    .build());
        } else if (config.getType().equalsIgnoreCase("breakblock")) {
            inventory.setItem(20, new ItemBuilder(Material.STONE_PICKAXE)
                    .name(ChatColor.LIGHT_PURPLE + "Arena Minimum")
                    .lore(ChatColor.GRAY + "Klik om de minimum hoek van de arena",
                          ChatColor.GRAY + "in te stellen op je huidige positie.")
                    .build());

            inventory.setItem(22, new ItemBuilder(Material.DIAMOND_PICKAXE)
                    .name(ChatColor.LIGHT_PURPLE + "Arena Maximum")
                    .lore(ChatColor.GRAY + "Klik om de maximum hoek van de arena",
                          ChatColor.GRAY + "in te stellen op je huidige positie.")
                    .build());
        }

        inventory.setItem(26, new ItemBuilder(Material.BARRIER)
                .name(ChatColor.RED + "Verwijderen")
                .lore(ChatColor.GRAY + "Klik om deze minigame te verwijderen.",
                      ChatColor.RED + "Let op: Dit kan niet ongedaan worden gemaakt!")
                .build());

        player.openInventory(inventory);
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getDescription() {
        return "Bewerk een bestaande minigame";
    }

    @Override
    public String getSyntax() {
        return "/mw edit <naam> [eigenschap] [waarde]";
    }

    @Override
    public String getPermission() {
        return "multiware.edit";
    }

    @Override
    public boolean isPlayerOnly() {
        return true;
    }

    @Override
    public int getMinArgs() {
        return 1;
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("e", "modify");
    }

    @Override
    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        GameManager gameManager = plugin.getGameManager();

        if (args.length == 2) {
            return gameManager.getAllMinigameNames().stream()
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 3) {
            return Arrays.asList("spawn", "enable", "disable", "pvp", "duration", "pressureplate", "arena")
                    .stream()
                    .filter(prop -> prop.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 4) {
            String property = args[2].toLowerCase();

            if (property.equals("pvp")) {
                return Arrays.asList("true", "false").stream()
                        .filter(val -> val.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (property.equals("duration")) {
                return Arrays.asList("30", "60", "90", "120").stream()
                        .filter(val -> val.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (property.equals("arena")) {
                return Arrays.asList("min", "max").stream()
                        .filter(val -> val.startsWith(args[3].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }

        return super.getTabCompletions(sender, args);
    }
}
