package fr.astfaster.santopia.server.module;

import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.player.PlayerHandler;
import org.bukkit.event.Listener;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class Module {

    protected abstract void init();

    public PlayerHandler createPlayerHandler(UUID playerId) {
        return new PlayerHandler(playerId);
    }

    protected Supplier<List<Listener>> listeners() {
        return Collections::emptyList;
    }

    protected Supplier<List<SantopiaCommand>> commands() {
        return Collections::emptyList;
    }

}
