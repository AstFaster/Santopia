package fr.astfaster.santopia.server.listener;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.player.PlayersService;
import fr.astfaster.santopia.server.SantopiaPlugin;
import fr.astfaster.santopia.server.message.Message;
import fr.astfaster.santopia.server.player.PlayerHandler;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConnectionsListener implements Listener {

    private final List<UUID> networkConnections = new CopyOnWriteArrayList<>();
    private final PlayersService playersService = SantopiaAPI.instance().playersService();

    @EventHandler
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        try {
            final UUID playerId = event.getUniqueId();

            this.playersService.session(playerId).thenAcceptAsync(session -> {
                // The player connected on the network
                if (session.currentServer() == null) {
                    this.networkConnections.add(playerId);
                } else { // In case
                    this.networkConnections.remove(playerId);
                }

                session.currentServer(SantopiaAPI.instance().currentServer().type());
                session.update().join();
            }).join();
        } catch (Exception e) {
            e.printStackTrace();

            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, ChatColor.RED + "Une erreur est survenue lors du chargement de votre profil !");
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();

        event.setJoinMessage(Message.JOIN_FORMAT.value().replace("%player%", player.getName()));

        try {
            SantopiaPlugin.instance().newPlayerHandler(playerId).onJoin(this.networkConnections.remove(playerId));
            SantopiaAPI.instance().currentServer().addPlayer(playerId);
        } catch (Exception e) {
            e.printStackTrace();

            player.kickPlayer(ChatColor.RED + "Une erreur est survenue lors du chargement de votre profil !");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();
        final UUID playerId = player.getUniqueId();

        event.setQuitMessage(Message.QUIT_FORMAT.value().replace("%player%", player.getName()));

        SantopiaPlugin.instance().destroyPlayerHandler(playerId);
        SantopiaAPI.instance().currentServer().removePlayer(playerId);
    }

}
