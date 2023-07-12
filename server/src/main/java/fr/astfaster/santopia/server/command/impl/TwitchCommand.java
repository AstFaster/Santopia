package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TwitchCommand extends SantopiaCommand {

    public TwitchCommand() {
        super("twitch");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(Message.TWITCH.value());
    }

}
