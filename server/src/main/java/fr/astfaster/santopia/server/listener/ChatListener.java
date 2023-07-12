package fr.astfaster.santopia.server.listener;

import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.command.CommandRegistry;
import fr.astfaster.santopia.server.message.Message;
import fr.astfaster.santopia.server.player.PlayerHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatListener implements Listener {

    private final CommandRegistry commandRegistry = SantopiaPlugin.instance().commandRegistry();

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        event.setFormat("%s §8§l» §f%s");
    }

    @EventHandler
    public void onPreprocessCommand(PlayerCommandPreprocessEvent event) {
        final String command = event.getMessage().substring(1).split(" ")[0];

        if (this.commandRegistry.bukkitCommand(command) == null) {
            event.getPlayer().sendMessage(Message.UNKNOWN_COMMAND.value());
            event.setCancelled(true);
        }
    }

}
