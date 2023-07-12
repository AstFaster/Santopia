package fr.astfaster.santopia.server.command.impl;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.player.SantopiaHome;
import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.message.Message;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.List;

public class HomeCommand extends SantopiaCommand {

    public HomeCommand() {
        super("home", "Home command", "/home|/sethome", List.of("sethome"));
    }

    @Override
    public void execute(Player player, String label, String[] args) {
        if (label.equalsIgnoreCase("home")) {
            SantopiaAPI.instance().playersService().account(player.getUniqueId()).thenAcceptAsync(account -> {
                final SantopiaHome home = account.home();

                if (home == null) {
                    player.sendMessage(Message.HOME_NO_HOME.value());
                    return;
                }

                final World world = Bukkit.getWorld(home.world());

                if (world == null) {
                    account.home(null);
                    account.update();

                    player.sendMessage(Message.HOME_NO_HOME.value());
                    return;
                }

                final Location location = new Location(world, home.x(), home.y(), home.z(), home.yaw(), home.pitch());

                SantopiaPlugin.runSync(() -> {
                    player.sendMessage(Message.HOME_TELEPORTING.value());
                    player.teleport(location);
                    player.playSound(location, Sound.ENTITY_PARROT_FLY, 1.0F, 1.0F);
                });
            });
        } else if (label.equalsIgnoreCase("sethome")) {
            SantopiaAPI.instance().playersService().account(player.getUniqueId()).thenAcceptAsync(account -> {
                final Location location = player.getLocation();
                final SantopiaHome home = new SantopiaHome(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());

                account.home(home);
                account.update();

                player.sendMessage(Message.HOME_SET.value());
            });
        }
    }

}
