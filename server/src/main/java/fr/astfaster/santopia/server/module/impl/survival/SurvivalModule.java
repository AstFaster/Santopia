package fr.astfaster.santopia.server.module.impl.survival;

import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.command.impl.*;
import fr.astfaster.santopia.server.listener.ClaimsListener;
import fr.astfaster.santopia.server.module.Module;
import fr.astfaster.santopia.server.player.PlayerHandler;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class SurvivalModule extends Module {

    @Override
    protected void init() {

    }

    @Override
    public PlayerHandler createPlayerHandler(UUID playerId) {
        return new SurvivalPlayerHandler(playerId);
    }

    @Override
    protected Supplier<List<Listener>> listeners() {
        return () -> Arrays.asList(
            new ClaimsListener()
        );
    }

    @Override
    protected Supplier<List<SantopiaCommand>> commands() {
        return () -> Arrays.asList(
            new TeleportationCommand(), new UnclaimCommand(), new HomeCommand(), new WelcomeCommand()
        );
    }

}
