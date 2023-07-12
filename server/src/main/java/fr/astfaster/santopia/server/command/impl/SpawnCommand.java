package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.message.Message;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class SpawnCommand extends SantopiaCommand {

    public SpawnCommand() {
        super("spawn");
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        final Location spawn = SantopiaPlugin.instance().config().spawn().asBukkit();

        player.teleport(spawn);
        player.playSound(spawn, Sound.ENTITY_PARROT_FLY, 1.0F, 1.0F);
        player.sendMessage(Message.SPAWN_TELEPORTED.value());
    }

}
