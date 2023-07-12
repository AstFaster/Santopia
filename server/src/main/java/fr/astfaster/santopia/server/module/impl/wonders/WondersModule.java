package fr.astfaster.santopia.server.module.impl.wonders;

import fr.astfaster.santopia.server.command.SantopiaCommand;
import fr.astfaster.santopia.server.module.Module;
import org.bukkit.event.Listener;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class WondersModule extends Module {

    @Override
    protected void init() {

    }

    @Override
    protected Supplier<List<Listener>> listeners() {
        return () -> Arrays.asList(

        );
    }

    @Override
    protected Supplier<List<SantopiaCommand>> commands() {
        return () -> Arrays.asList(

        );
    }

}
