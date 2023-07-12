package fr.astfaster.santopia.proxy.listener;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChooseInitialServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.astfaster.santopia.api.SantopiaAPI;
import fr.astfaster.santopia.api.player.PlayersService;
import fr.astfaster.santopia.api.player.SantopiaAccount;
import fr.astfaster.santopia.api.player.SantopiaSession;
import fr.astfaster.santopia.api.server.SantopiaServer;
import fr.astfaster.santopia.api.server.ServerType;
import fr.astfaster.santopia.proxy.SantopiaPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.UUID;
import java.util.function.Function;

public class ServersListener {

    private final ProxyServer server = SantopiaPlugin.instance().server();

    @Subscribe
    public EventTask onKick(KickedFromServerEvent event) {
        return EventTask.withContinuation(continuation -> {
            if (event.getServer().getServerInfo().getName().equals(ServerType.SURVIVAL.name()) || event.kickedDuringServerConnect()) {
                continuation.resume();
                return;
            }

            final SantopiaServer survival = SantopiaAPI.instance().serversService().server(ServerType.SURVIVAL);

            if (survival.reachable() && survival.players().size() < survival.type().slots()) {
                this.server.getServer(survival.type().name()).ifPresent(server -> event.setResult(KickedFromServerEvent.RedirectPlayer.create(server)));
            }

            continuation.resume();
        });
    }

}
