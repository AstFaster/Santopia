package fr.astfaster.santopia.server.command;

import fr.astfaster.santopia.server.util.Reflection;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommandRegistry {

    private final CommandMap commandMap = Reflection.invokeField(Bukkit.getServer(), "commandMap");
    private final Map<String, SantopiaCommand> commands = new HashMap<>();

    public void register(SantopiaCommand command) {
        Objects.requireNonNull(this.commandMap).register("santopia", command);

        this.commands.put(command.getLabel(), command);
    }

    public SantopiaCommand command(String label) {
        return this.commands.get(label);
    }

    public Command bukkitCommand(String label) {
        return this.commandMap.getCommand(label);
    }

}
