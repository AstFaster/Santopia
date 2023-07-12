package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.message.Message;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class DiscordCommand extends SantopiaCommand {

    public DiscordCommand() {
        super("discord");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        player.sendMessage(Message.DISCORD.value().replace("%discord%", SantopiaPlugin.instance().config().discordURL()));
    }

}
