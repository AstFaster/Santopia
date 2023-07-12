package fr.astfaster.santopia.server.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public abstract class SantopiaCommand extends Command {

    public SantopiaCommand(String name) {
        super(name);
    }

    public SantopiaCommand(String name, String description, String usageMessage, List<String> aliases) {
        super(name, description, usageMessage, aliases);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof final Player player) {
            this.execute(player, label, args);
        }
        return true;
    }

    public void execute(Player player, String label, String[] args) {}

}
