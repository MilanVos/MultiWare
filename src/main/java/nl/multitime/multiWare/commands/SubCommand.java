package nl.multitime.multiWare.commands;

import nl.multitime.multiWare.MultiWare;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class SubCommand {

    protected final MultiWare plugin;

    public SubCommand(MultiWare plugin) {
        this.plugin = plugin;
    }


    public abstract boolean execute(CommandSender sender, String[] args);

    public abstract String getName();


    public abstract String getDescription();

    public abstract String getSyntax();

    public abstract String getPermission();

    public boolean isPlayerOnly() {
        return false;
    }

    public int getMinArgs() {
        return 0;
    }


    public List<String> getAliases() {
        return Collections.emptyList();
    }


    public List<String> getTabCompletions(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }
}
