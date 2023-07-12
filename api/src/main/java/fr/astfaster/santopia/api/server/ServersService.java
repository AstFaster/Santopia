package fr.astfaster.santopia.api.server;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.messaging.PacketsChannel;
import fr.astfaster.santopia.api.messaging.impl.server.ServerKeepAlivePacket;
import fr.astfaster.santopia.api.messaging.impl.server.ServerUpdatePlayersPacket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ServersService {

    private final Map<ServerType, SantopiaServer> servers = new ConcurrentHashMap<>();

    private SantopiaServer currentServer;

    public void start() {
        // Register servers
        for (ServerType serverType : ServerType.values()) {
            this.servers.put(serverType, new SantopiaServer(serverType));
        }

        // Register packets handler
        SantopiaAPI.instance().messagingService().registerHandler(PacketsChannel.SERVERS, packet -> {
            if (packet instanceof final ServerKeepAlivePacket keepAlive) {
                this.servers.get(keepAlive.server()).lastHeartbeat(keepAlive.time());
            } else if (packet instanceof final ServerUpdatePlayersPacket updatePlayers) {
                this.servers.get(updatePlayers.server()).players(updatePlayers.players());
            }
        });
    }

    public SantopiaServer initCurrentServer(ServerType type) throws ServerAlreadyInitializedException {
        if (this.currentServer != null) {
            throw new ServerAlreadyInitializedException();
        }

        this.currentServer = new SantopiaServer(type);

        // Send a keep alive every 10 seconds
        SantopiaAPI.instance()
                .executorService()
                .scheduleAtFixedRate(() -> SantopiaAPI.instance()
                        .messagingService()
                        .send(PacketsChannel.SERVERS, new ServerKeepAlivePacket(type, System.currentTimeMillis())),
                0, SantopiaServer.HEARTBEAT_FREQUENCY, TimeUnit.MILLISECONDS);

        return this.currentServer;
    }

    public SantopiaServer currentServer() {
        return this.currentServer;
    }

    public SantopiaServer server(ServerType type) {
        return this.servers.get(type);
    }

    public Map<ServerType, SantopiaServer> servers() {
        return this.servers;
    }

}
