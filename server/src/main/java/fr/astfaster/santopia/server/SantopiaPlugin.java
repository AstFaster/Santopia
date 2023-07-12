package fr.astfaster.santopia.server;

import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.SantopiaException;
import fr.astfaster.santopia.api.messaging.MessagingService;
import fr.astfaster.santopia.api.messaging.PacketsChannel;
import fr.astfaster.santopia.api.messaging.impl.server.ServerKeepAlivePacket;
import fr.astfaster.santopia.api.messaging.impl.server.ServerUpdatePlayersPacket;
import fr.astfaster.santopia.api.server.SantopiaServer;
import fr.astfaster.santopia.api.server.ServerType;
import fr.astfaster.santopia.server.chunk.ChunksService;
import fr.astfaster.santopia.server.command.CommandRegistry;
import fr.astfaster.santopia.server.command.impl.*;
import fr.astfaster.santopia.server.config.ConfigLoader;
import fr.astfaster.santopia.server.config.ServerConfig;
import fr.astfaster.santopia.server.gui.GUIHandler;
import fr.astfaster.santopia.server.item.ItemHandler;
import fr.astfaster.santopia.server.item.ItemRegistry;
import fr.astfaster.santopia.server.item.impl.MainMenuItem;
import fr.astfaster.santopia.server.listener.ChatListener;
import fr.astfaster.santopia.server.listener.ConnectionsListener;
import fr.astfaster.santopia.server.module.Module;
import fr.astfaster.santopia.server.module.ModuleRegistry;
import fr.astfaster.santopia.server.module.impl.creative.CreativeModule;
import fr.astfaster.santopia.server.module.impl.survival.SurvivalModule;
import fr.astfaster.santopia.server.module.impl.wonders.WondersModule;
import fr.astfaster.santopia.server.packet.GuildsPacketsHandler;
import fr.astfaster.santopia.server.player.GameProfilesService;
import fr.astfaster.santopia.server.player.PlayerHandler;
import fr.astfaster.santopia.server.tablist.TabListHandler;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class SantopiaPlugin extends JavaPlugin {

    public static final String DASH_LINE = "-------------------------------------------";
    public static final String COLORED_SERVER_NAME = "&6&lSant&b&lopia";

    private static SantopiaPlugin instance;

    private final Map<UUID, PlayerHandler> playersHandlers = new ConcurrentHashMap<>();

    private ServerConfig config;

    private CommandRegistry commandRegistry;
    private ModuleRegistry moduleRegistry;
    private ItemRegistry itemRegistry;

    private GameProfilesService gameProfilesService;
    private ChunksService chunksService;

    private GUIHandler guiHandler;
    private TabListHandler tabListHandler;

    private Module module;

    @Override
    public void onEnable() {
        instance = this;

        // Load config
        this.config = new ConfigLoader<>(ServerConfig.class, ServerConfig::new).load(Path.of(this.getDataFolder().toPath().toString(), "config.yml"));

        // Initialize API
        SantopiaAPI.create(this.config, SantopiaAPI.Process.SERVER);

        // Initialize server
        SantopiaAPI.instance().serversService().initCurrentServer(this.config.server());

        this.getLogger().info("Initialized '" + this.config.server() + "' server.");

        // Random stuffs
        this.commandRegistry = new CommandRegistry();
        this.itemRegistry = new ItemRegistry();
        this.gameProfilesService = new GameProfilesService();
        this.chunksService = new ChunksService();
        this.guiHandler = new GUIHandler();
        this.tabListHandler = new TabListHandler();

        this.registerListeners();
        this.registerCommands();
        this.registerItems();
        this.registerPacketsHandlers();

        // Load module
        this.moduleRegistry = new ModuleRegistry();
        this.moduleRegistry.registerModule(ServerType.SURVIVAL, SurvivalModule::new);
        this.moduleRegistry.registerModule(ServerType.CREATIVE, CreativeModule::new);
        this.moduleRegistry.registerModule(ServerType.WONDERS, WondersModule::new);
        this.module = this.moduleRegistry.loadModule(this.config.server());
    }

    private void registerListeners() {
        final Consumer<Listener> registry = listener -> this.getServer().getPluginManager().registerEvents(listener, this);

        registry.accept(new ConnectionsListener());
        registry.accept(new ChatListener());
        registry.accept(new ItemHandler());
        registry.accept(this.guiHandler);
    }

    private void registerCommands() {
        this.commandRegistry.register(new SpawnCommand());
        this.commandRegistry.register(new DiscordCommand());
        this.commandRegistry.register(new TwitchCommand());
        this.commandRegistry.register(new TikTokCommand());
        this.commandRegistry.register(new IPCommand());
        this.commandRegistry.register(new GuildCommand());
        this.commandRegistry.register(new MenuCommand());
    }

    private void registerItems() {
        this.itemRegistry.registerItem(new MainMenuItem());
    }

    private void registerPacketsHandlers() {
        final MessagingService messagingService = SantopiaAPI.instance().messagingService();

        messagingService.registerHandler(PacketsChannel.GUILDS, new GuildsPacketsHandler());
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.kickPlayer(ChatColor.RED + "Server restarting.");
        }

        // Set the server as 'not reachable'
        SantopiaAPI.instance().messagingService().send(PacketsChannel.SERVERS, new ServerKeepAlivePacket(this.config.server(), System.currentTimeMillis() - SantopiaServer.TIMED_OUT));
        // Set the server as 'empty"
        SantopiaAPI.instance().messagingService().send(PacketsChannel.SERVERS, new ServerUpdatePlayersPacket(this.config.server(), new HashSet<>()));

        // Stop API
        SantopiaAPI.instance().stop();
    }

    public static SantopiaPlugin instance() {
        return instance;
    }

    public static void runSync(Runnable task) {
        Bukkit.getScheduler().runTask(instance, task);
    }

    public PlayerHandler newPlayerHandler(UUID playerId) {
        final PlayerHandler handler = this.module.createPlayerHandler(playerId);

        if (this.playersHandlers.putIfAbsent(playerId, handler) != null) {
            throw new SantopiaException("Player handler already registered!");
        }
        return handler;
    }

    public boolean destroyPlayerHandler(UUID playerId) {
        final PlayerHandler handler = this.playersHandlers.remove(playerId);

        if (handler != null) {
            handler.onQuit();
            return true;
        }
        return false;
    }

    public PlayerHandler playerHandler(UUID playerId) {
        return this.playersHandlers.get(playerId);
    }

    public Collection<PlayerHandler> playersHandlers() {
        return this.playersHandlers.values();
    }

    public ServerConfig config() {
        return this.config;
    }

    public CommandRegistry commandRegistry() {
        return this.commandRegistry;
    }

    public ModuleRegistry moduleRegistry() {
        return this.moduleRegistry;
    }

    public Module module() {
        return this.module;
    }

    public ItemRegistry itemRegistry() {
        return this.itemRegistry;
    }

    public GameProfilesService gameProfilesService() {
        return this.gameProfilesService;
    }

    public ChunksService chunksService() {
        return this.chunksService;
    }

    public GUIHandler guiHandler() {
        return this.guiHandler;
    }

    public TabListHandler tabListHandler() {
        return this.tabListHandler;
    }

}
