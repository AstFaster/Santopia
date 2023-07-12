package fr.astfaster.santopia.proxy.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.player.ResourcePackInfo;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.player.PlayersService;
import fr.astfaster.santopia.api.player.SantopiaAccount;
import fr.astfaster.santopia.api.player.SantopiaSession;
import fr.astfaster.santopia.api.server.ServerType;
import fr.astfaster.santopia.proxy.SantopiaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.function.Function;

public class ConnectionsListener {

    private static final Function<Component, Component> BUILDER = component -> component.append(Component.newline())
            .append(Component.text(SantopiaAPI.instance().config().discordURL()).color(NamedTextColor.YELLOW));

    private static final Component SERVER_FULL_MESSAGE = BUILDER.apply(Component.text("Sant").decorate(TextDecoration.BOLD).color(NamedTextColor.GOLD)
            .append(Component.text("opia").decorate(TextDecoration.BOLD).color(NamedTextColor.AQUA))
            .append(Component.newline())
            .append(Component.text("Le serveur est plein !").color(NamedTextColor.RED)));

    private final ProxyServer server = SantopiaPlugin.instance().server();
    private final PlayersService playersService = SantopiaAPI.instance().playersService();

    @Subscribe
    public void onPreLogin(PreLoginEvent event) {
        if (this.server.getPlayerCount() >= SantopiaAPI.instance().networkService().slots()) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied(SERVER_FULL_MESSAGE));
        }
    }

    @Subscribe
    public EventTask onJoin(PlayerChooseInitialServerEvent event) {
        return EventTask.withContinuation(continuation -> {
            final Player player = event.getPlayer();
            final UUID playerId = player.getUniqueId();

            if (!SantopiaAPI.instance().serversService().server(ServerType.SURVIVAL).reachable()) {
                return;
            }

            // Set initial server
            event.setInitialServer(this.server.getServer(ServerType.SURVIVAL.name()).orElse(null));

            // Load account
            SantopiaAccount account = this.playersService.account(playerId).join();

            if (account == null) {
                account = this.playersService.createAccount(playerId, player.getUsername());
            }

            account.lastLogin(System.currentTimeMillis());
            account.update().join();

            // Load session
            final SantopiaSession session = new SantopiaSession(playerId, null);

            session.update().join();

            continuation.resume();
        });
    }

    @Subscribe
    public EventTask onServerConnected(ServerConnectedEvent event) {
        return EventTask.withContinuation(continuation -> {
            final Player player = event.getPlayer();

            // Check if it's a network connection
            if (event.getPreviousServer().isPresent()) {
                return;
            }

            // Send resource pack
            player.sendResourcePackOffer(this.server.createResourcePackBuilder(SantopiaPlugin.instance().config().resourcePackURL())
                    .setShouldForce(true)
                    .setPrompt(Component.text("Pour une meilleure expérience de jeu sur Santopia, merci d'utiliser notre pack de ressources."))
                    .build());

            continuation.resume();
        });
    }

    @Subscribe
    public EventTask onQuit(DisconnectEvent event) {
        return EventTask.withContinuation(continuation -> {
            final Player player = event.getPlayer();
            final UUID playerId = player.getUniqueId();
            final SantopiaAccount account = this.playersService.account(playerId).join();

            account.addPlayTime(System.currentTimeMillis() - account.lastLogin());
            account.update().join();

            this.playersService.destroySession(playerId).join();

            continuation.resume();
        });
    }

}
