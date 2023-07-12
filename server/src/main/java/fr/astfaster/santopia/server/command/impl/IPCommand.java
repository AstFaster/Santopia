package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class IPCommand extends SantopiaCommand {

    public IPCommand() {
        super("ip");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(Message.IP.value());
    }

}
