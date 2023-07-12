package fr.astfaster.santopia.api;

import fr.astfaster.santopia.api.config.Config;
import fr.astfaster.santopia.api.database.mongodb.MongoDB;
import fr.astfaster.santopia.api.database.redis.Redis;
import fr.astfaster.santopia.api.guild.GuildsService;
import fr.astfaster.santopia.api.guild.claim.ClaimsService;
import fr.astfaster.santopia.api.messaging.MessagingService;
import fr.astfaster.santopia.api.messaging.impl.guild.*;
import fr.astfaster.santopia.api.messaging.impl.player.PlayerConnectPacket;
import fr.astfaster.santopia.api.messaging.impl.server.ServerKeepAlivePacket;
import fr.astfaster.santopia.api.messaging.impl.server.ServerUpdatePlayersPacket;
import fr.astfaster.santopia.api.network.NetworkService;
import fr.astfaster.santopia.api.player.PlayersService;
import fr.astfaster.santopia.api.serializer.DataSerializer;
import fr.astfaster.santopia.api.serializer.JsonSerializers;
import fr.astfaster.santopia.api.server.SantopiaServer;
import fr.astfaster.santopia.api.server.ServersService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

public class SantopiaAPI {

    private static SantopiaAPI instance;

    private ScheduledExecutorService executorService;

    private MongoDB mongoDB;
    private Redis redis;

    private JsonSerializers jsonSerializers;
    private DataSerializer dataSerializer;

    private MessagingService messagingService;
    private PlayersService playersService;
    private GuildsService guildsService;
    private ClaimsService claimsService;
    private ServersService serversService;
    private NetworkService networkService;

    private final Config config;
    private final Process process;

    public static SantopiaAPI create(Config config, Process process) {
        if (instance != null) {
            throw new SantopiaException("Santopia API instance was already created!");
        }
        return new SantopiaAPI(config, process);
    }

    private SantopiaAPI(Config config, Process process) {
        instance = this;

        this.config = config;
        this.process = process;

        this.preInit();
        this.init();
        this.postInit();
    }

    private void preInit() {
        System.out.println("Pre-initializing Santopia API (process: " + this.process + ")...");

        this.executorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());
        this.jsonSerializers = new JsonSerializers();
        this.dataSerializer = new DataSerializer();
        this.mongoDB = new MongoDB();
        this.redis = new Redis();
        this.messagingService = new MessagingService();
    }

    private void init() {
        System.out.println("Initializing Santopia API (process: " + this.process + ")...");

        this.mongoDB.connect();
        this.redis.connect();
        this.messagingService.start();

        this.playersService = new PlayersService();
        this.guildsService = new GuildsService();
        this.claimsService = new ClaimsService();
        this.serversService = new ServersService();
        this.serversService.start();
        this.networkService = new NetworkService();
    }

    private void postInit() {
        System.out.println("Post-initializing Santopia API (process: " + this.process + ")...");

        this.registerPackets();
    }

    private void registerPackets() {
        // Servers
        this.messagingService.registerPacket(0, ServerKeepAlivePacket.class);
        this.messagingService.registerPacket(1, ServerUpdatePlayersPacket.class);

        // Players
        this.messagingService.registerPacket(100, PlayerConnectPacket.class);

        // Guilds
        this.messagingService.registerPacket(200, GuildRequestPacket.class);
        this.messagingService.registerPacket(201, GuildPlayerJoinedPacket.class);
        this.messagingService.registerPacket(202, GuildPlayerLeftPacket.class);
        this.messagingService.registerPacket(203, GuildPlayerKickPacket.class);
        this.messagingService.registerPacket(204, GuildDisbandPacket.class);
        this.messagingService.registerPacket(205, GuildRenamePacket.class);
        this.messagingService.registerPacket(206, GuildPrefixChangePacket.class);
        this.messagingService.registerPacket(207, GuildPrefixColorChangePacket.class);
    }

    public void stop() {
        this.messagingService.stop();
        this.mongoDB.stop();
        this.redis.stop();
        this.executorService.shutdown();
    }

    public static SantopiaAPI instance() {
        return instance;
    }

    public <T> CompletableFuture<T> newFuture(Supplier<T> supplier) {
        return CompletableFuture.supplyAsync(supplier, this.executorService);
    }

    public Config config() {
        return this.config;
    }

    public Process process() {
        return this.process;
    }

    public ScheduledExecutorService executorService() {
        return this.executorService;
    }

    public MongoDB mongoDB() {
        return this.mongoDB;
    }

    public Redis redis() {
        return this.redis;
    }

    public JsonSerializers jsonSerializers() {
        return this.jsonSerializers;
    }

    public DataSerializer dataSerializer() {
        return this.dataSerializer;
    }

    public MessagingService messagingService() {
        return this.messagingService;
    }

    public PlayersService playersService() {
        return this.playersService;
    }

    public GuildsService guildsService() {
        return this.guildsService;
    }

    public ClaimsService claimsService() {
        return this.claimsService;
    }

    public ServersService serversService() {
        return this.serversService;
    }

    public SantopiaServer currentServer() {
        return this.serversService.currentServer();
    }

    public NetworkService networkService() {
        return this.networkService;
    }

    public enum Process {

        SERVER,
        PROXY

    }

}
