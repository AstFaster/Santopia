package fr.astfaster.santopia.proxy.packet;

import com.velocitypowered.api.proxy.ProxyServer;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.messaging.PacketsHandler;
import fr.astfaster.santopia.api.messaging.SantopiaPacket;
import fr.astfaster.santopia.api.messaging.impl.player.PlayerConnectPacket;
import fr.astfaster.santopia.api.server.SantopiaServer;
import fr.astfaster.santopia.api.server.ServerType;
import fr.astfaster.santopia.proxy.SantopiaPlugin;
import fr.astfaster.santopia.proxy.message.Message;
import net.kyori.adventure.text.Component;

public class PlayersPacketsHandler implements PacketsHandler {

    private final ProxyServer velocity = SantopiaPlugin.instance().server();

    @Override
    public void handle(SantopiaPacket packet) {
        if (packet instanceof final PlayerConnectPacket connect) {
            final ServerType serverType = connect.server();

            this.velocity.getPlayer(connect.player())
                    .ifPresent(player -> this.velocity.getServer(serverType.name())
                            .ifPresent(connection -> {
                                final SantopiaServer server = SantopiaAPI.instance().serversService().server(serverType);

                                if (!server.reachable()) {
                                    player.sendMessage(Message.SERVER_NOT_ONLINE.value());
                                    return;
                                }

                                if (connection.getPlayersConnected().contains(player)) {
                                    player.sendMessage(Message.SERVER_ALREADY_CONNECTED.value());
                                    return;
                                }

                                if (server.players().size() >= serverType.slots()) {
                                    player.sendMessage(Message.SERVER_FULL.value()
                                            .replaceText(builder -> builder.match("%players%").replacement(String.valueOf(server.players().size())))
                                            .replaceText(builder -> builder.match("%slots%").replacement(String.valueOf(server.type().slots()))));
                                    return;
                                }

                                player.sendMessage(Message.SERVER_SENDING.value().replaceText(builder -> builder.match("%server%").replacement(server.toString())));
                                player.createConnectionRequest(connection).connect();
                            }));
        }
    }

}
