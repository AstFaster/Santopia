package fr.astfaster.santopia.proxy;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.util.Favicon;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.proxy.config.ConfigLoader;
import fr.astfaster.santopia.api.messaging.MessagingService;
import fr.astfaster.santopia.api.messaging.PacketsChannel;
import fr.astfaster.santopia.proxy.config.ProxyConfig;
import fr.astfaster.santopia.proxy.listener.ConnectionsListener;
import fr.astfaster.santopia.proxy.listener.ServersListener;
import fr.astfaster.santopia.proxy.packet.PlayersPacketsHandler;
import org.slf4j.Logger;

import java.net.InetSocketAddress;
import java.nio.file.Path;

@Plugin(id = "santopia", name = "Santopia", version = "1.0.0", authors = {"AstFaster"}, dependencies = {@Dependency(id = "luckperms")})
public class SantopiaPlugin {

    private static SantopiaPlugin instance;

    private ProxyConfig config;

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    @Inject
    public SantopiaPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;

        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Load config
        this.config = new ConfigLoader<>(ProxyConfig.class, ProxyConfig::new).load(Path.of(this.dataDirectory.toString(), "config.yml"));

        // Initialize API
        SantopiaAPI.create(this.config, SantopiaAPI.Process.PROXY);

        // Register servers
        this.config.servers().forEach((type, info) -> this.server.registerServer(new ServerInfo(type.name(), new InetSocketAddress(info.hostname(), info.port()))));

        // Register listeners
        final EventManager eventManager = this.server.getEventManager();

        eventManager.register(this, new ConnectionsListener());
        eventManager.register(this, new ServersListener());

        // Register packets handlers
        final MessagingService messagingService = SantopiaAPI.instance().messagingService();

        messagingService.registerHandler(PacketsChannel.PLAYERS, new PlayersPacketsHandler());
    }

    @Subscribe
    public void onPing(ProxyPingEvent event) {
        final ServerPing.Builder ping = event.getPing().asBuilder();

        ping.maximumPlayers(SantopiaAPI.instance().networkService().slots());
        ping.favicon(new Favicon(this.config.favicon()));

        event.setPing(ping.build());
    }

    public static SantopiaPlugin instance() {
        return instance;
    }

    public ProxyServer server() {
        return this.server;
    }

    public ProxyConfig config() {
        return this.config;
    }

}
