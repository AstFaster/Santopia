package fr.astfaster.santopia.server.module;

import fr.astfaster.santopia.api.server.ServerType;
import fr.astfaster.santopia.server.SantopiaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

public class ModuleRegistry {

    private final Map<ServerType, Supplier<? extends Module>> modules = new HashMap<>();

    public void registerModule(ServerType serverType, Supplier<? extends Module> moduleSupplier) {
        this.modules.put(serverType, moduleSupplier);
    }

    public Module loadModule(ServerType serverType) {
        final Supplier<? extends Module> moduleSupplier = this.modules.get(serverType);

        if (moduleSupplier != null) {
            final Logger logger = SantopiaPlugin.instance().getLogger();

            logger.info("Loading '" + serverType + "' module...");

            final Module module = moduleSupplier.get();

            logger.info("Loading '" + serverType + "' listeners...");
            module.listeners().get().forEach(listener -> SantopiaPlugin.instance().getServer().getPluginManager().registerEvents(listener, SantopiaPlugin.instance()));

            logger.info("Loading '" + serverType + "' commands...");
            module.commands().get().forEach(command -> SantopiaPlugin.instance().commandRegistry().register(command));

            return module;
        }
        return null;
    }

}
